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


/**
 * 类加载
 * @author 吴扬
 * 扩展类优先加载
 */

public class ExtensionClassLoader extends ClassLoader
{

    public ExtensionClassLoader(ClassLoader baseLoader)
    {
        super(baseLoader);
    }

    /**
     *  We override loadClass (and not findClass) because we want to give priority to the
     * extension classes over the parent classes.
     */

    synchronized public Class loadClass(String name) throws ClassNotFoundException
    {
        Object value = Extensions.getExtensionClass(name);
        if (value == null)
            return getParent().loadClass(name);
        else
        {
            return (Class) value;
        }
    }

}

