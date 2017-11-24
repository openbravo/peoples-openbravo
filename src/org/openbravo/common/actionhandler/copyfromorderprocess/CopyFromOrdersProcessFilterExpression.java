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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.copyfromorderprocess;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;

public class CopyFromOrdersProcessFilterExpression implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {
    try {
      String context = requestMap.get("context");
      JSONObject contextJSON = new JSONObject(context);
      String organizationId = contextJSON.getString("inpadOrgId");
      return getLegalEntityId(organizationId);
    } catch (Exception e) {
      return null;
    }
  }

  private String getLegalEntityId(String organizationId) {
    final Organization organization = OBDal.getInstance().getProxy(Organization.class,
        organizationId);
    return OBContext.getOBContext()
        .getOrganizationStructureProvider(organization.getClient().getId())
        .getLegalEntity(organization).getId();
  }
}
