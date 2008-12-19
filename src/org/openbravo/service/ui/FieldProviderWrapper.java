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

package org.openbravo.service.ui;

import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.data.FieldProvider;

/**
 * Wraps the FieldProvider interface over the BaseOBObject.
 * 
 * @author Martin Taal
 */
public class FieldProviderWrapper implements BaseOBObjectDef, FieldProvider {

    private BaseOBObjectDef baseOBObjectDef;

    @Override
    public String getField(String fieldName) {
        if (fieldName.equalsIgnoreCase("name")) {
            return getIdentifier();
        }
        // get the property using the column name
        final Property p = getEntity().getPropertyByColumnName(fieldName);

        final Object value = get(p.getName());
        if (value instanceof BaseOBObject) {
            return ((BaseOBObject) value).getIdentifier();
        }
        if (value == null) {
            return "";
        }
        // TODO: do user specific format, taking the locale into account
        return value.toString();
    }

    public Object get(String propertyName) {
        return baseOBObjectDef.get(propertyName);
    }

    public Entity getEntity() {
        return baseOBObjectDef.getEntity();
    }

    public Object getId() {
        return baseOBObjectDef.getId();
    }

    public void set(String propertyName, Object value) {
        baseOBObjectDef.set(propertyName, value);
    }

    public String getIdentifier() {
        return baseOBObjectDef.getIdentifier();
    }

}
