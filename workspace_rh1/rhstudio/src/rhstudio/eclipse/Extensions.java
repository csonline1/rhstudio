/************************************************************************************************
 * Copyright (c) 2003-2005 rhstudio Software Ltd. and others.
 * All rights reserved.
 *
 * This program is made available under the terms of the GNU General Public License v2,
 * which is part of this distribution and is available at http://www.gnu.org/licenses/gpl.txt.
 *
 * Contributors:
 *     rhstudio Software Ltd.  Initial API and implementation
 *************************************************************************************************/
 
package rhstudio.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;


/**
 * 扩展类
 * @author 
 *
 */
public class Extensions
{
    private static HashMap<String, Class<?>> extensionClasses;

    public static File[] getModelLibraries()
    {
        ArrayList<File> libraryFileList = new ArrayList<File>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry
                .getExtensionPoint("rhstudio.modelLibraries");
        if (point != null)
        {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++)
            {
                IExtension extension = extensions[i];
                Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
                IConfigurationElement[] elements = extension
                        .getConfigurationElements();
                for (int j = 0; j < elements.length; j++)
                {
                    IConfigurationElement element = elements[j];
                    if ("file".equals(element.getName()))
                    {
                        libraryFileList.add(getLibraryFile(bundle, element
                                .getValue()));
                    }
                }
            }
        }
        File[] libraryFiles = new File[libraryFileList.size()];
        libraryFileList.toArray(libraryFiles);
        return libraryFiles;

    }

    private static File getLibraryFile(Bundle bundle, String path)
    {
        URL installURL = bundle.getEntry(path);
        if (installURL == null)
            return null;
        URL resolvedURL;
        try
        {
            resolvedURL = FileLocator.resolve(installURL);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to resolve url for library "
                    + installURL, e);
        }
        String filename = resolvedURL.getFile();
        return new File(filename);
    }
    
    synchronized private static void loadExtensions()
    {
        if (extensionClasses != null)
            return;
        extensionClasses = new HashMap<String,Class<?>>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry
                .getExtensionPoint("rhstudio.extensionClasses");
        if (point != null)
        {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++)
            {
                IExtension extension = extensions[i];
                Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
                IConfigurationElement[] elements = extension
                        .getConfigurationElements();
                for (int j = 0; j < elements.length; j++)
                {
                    IConfigurationElement element = elements[j];
                    if ("class".equals(element.getName()))
                    {
                        String className = element.getValue();
                        Class<?> c;
                        try
                        {
                            c = bundle.loadClass(className);
                            extensionClasses.put(className, c);
                        }
                        catch (ClassNotFoundException e)
                        {
                            e.printStackTrace();
                            System.err.println("WARNING: Missing extension class '"+className+"'");
                        }
                    }
                }
            }
        }
    }
    
    public static Class<?> getExtensionClass(String className)
    {
        loadExtensions();
        return extensionClasses.get(className);
    }
    
    @SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getExtensionClasses(Class<T> baseType)
    {
        loadExtensions();
        ArrayList<Class<T>> classes = new ArrayList<Class<T>>();
        for ( Class<?> extenstionClass : extensionClasses.values())
        {
            if (baseType.isAssignableFrom(extenstionClass))
            {
                classes.add((Class<T>)extenstionClass);
            }
        }
        return classes;
    }
}
