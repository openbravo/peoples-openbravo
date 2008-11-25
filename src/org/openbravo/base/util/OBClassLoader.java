/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
        } catch (final Exception e) {
            throw new OBException("Exception while loading class " + className
                    + ", " + e.getMessage(), e);
        }
    }

    public static class ClassOBClassLoader extends OBClassLoader {

        @Override
        public Class<?> loadClass(String className) {
            try {
                return Class.forName(className);
            } catch (final Exception e) {
                throw new OBException("Exception while loading class "
                        + className + ", " + e.getMessage(), e);
            }
        }
    }
}