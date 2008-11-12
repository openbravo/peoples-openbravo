/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.openbravo.base.util;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * The OBClassLoader which can be from the outside. Two classloaders are
 * supported: the context (the default) and the class classloader.
 * 
 * @author mtaal
 */

public class OBClassLoader implements OBSingleton {

    private static OBClassLoader instance;

    public static OBClassLoader getInstance() {
	if (instance == null) {
	    instance = OBProvider.getInstance().get(OBClassLoader.class);
	}
	return instance;
    }

    public Class<?> loadClass(String className) {
	try {
	    return Thread.currentThread().getContextClassLoader().loadClass(
		    className);
	} catch (Exception e) {
	    throw new OBException("Exception while loading class " + className
		    + ", " + e.getMessage(), e);
	}
    }

    public static class ClassOBClassLoader extends OBClassLoader {

	@Override
	public Class<?> loadClass(String className) {
	    try {
		return Class.forName(className);
	    } catch (Exception e) {
		throw new OBException("Exception while loading class "
			+ className + ", " + e.getMessage(), e);
	    }
	}
    }
}