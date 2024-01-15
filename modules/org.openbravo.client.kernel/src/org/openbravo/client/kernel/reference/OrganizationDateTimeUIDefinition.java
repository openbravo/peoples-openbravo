/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.ui.Field;

/**
 * UI definition to represent organization time zone fields
 */
public class OrganizationDateTimeUIDefinition extends StringUIDefinition {

  @Override
  public String getGridFieldProperties(Field field) {
    String properties = super.getGridFieldProperties(field);
    properties = removeAttributeFromString(properties, "canSort");
    properties = removeAttributeFromString(properties, "canFilter");
    return properties + ", canSort: false, canFilter: false";
  }

  @Override
  public synchronized Object createFromClassicString(String value) {
    try {
      BaseOBObject bob = getBOBFromCurrentContext();
      // TODO
      // 1- get the organization with OBDateUtils.getTimeZoneOrganization(bob) if bob!=null
      // 2- get the timezone from the org
      // 3- return OBDateUtils.formatZonedDateTime(zonedDateTime)
      return null;
    } catch (Exception ex) {
      log.error("Could not convert {} into the organization timezone", value, ex);
      return null;
    }
  }

  private BaseOBObject getBOBFromCurrentContext() {
    String entityName = RequestContext.get().getRequestParameter("_entityName");
    Entity entity = ModelProvider.getInstance().getEntity(entityName);
    String idColumn = RequestContext.get().getRequestParameter("keyColumnName") != null
        ? RequestContext.get().getRequestParameter("keyColumnName")
        : entity.getIdProperties().get(0).getColumnName();
    String id = RequestContext.get()
        .getRequestParameter("inp" + Sqlc.TransformaNombreColumna(idColumn));
    return (BaseOBObject) OBDal.getInstance().get(entity.getMappingClass(), id);
  }
}
