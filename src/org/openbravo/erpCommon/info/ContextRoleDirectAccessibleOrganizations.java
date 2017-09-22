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

package org.openbravo.erpCommon.info;

import java.util.Map;

import org.openbravo.base.model.AccessLevel;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.springframework.util.StringUtils;

/**
 * Filter expression for the "Context Role Direct Accessible Organizations" reference.
 * 
 * It filters the organizations to be displayed taking into account if their access has been
 * explicitly defined for the role in the current context. It also takes into account if the access
 * level of the process definition allows to include the organization '*'.
 */
public class ContextRoleDirectAccessibleOrganizations implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {
    StringBuilder filterExpression = new StringBuilder("");
    String userOrgs = RequestContext.get().getVariablesSecureApp().getSessionValue("#User_Org");
    if (StringUtils.isEmpty(userOrgs)) {
      return filterExpression.toString();
    }
    filterExpression.append("e.id IN(" + userOrgs + ")");
    if (!requestMap.containsKey("_processDefinitionId")) {
      return filterExpression.toString();
    }
    int accessLevel = getProcessDefinitionAccessLevel(requestMap.get("_processDefinitionId"));
    if (AccessLevel.ORGANIZATION.getDbValue() == accessLevel) {
      filterExpression.append(" AND e.id <> '0'");
    }
    return filterExpression.toString();
  }

  private int getProcessDefinitionAccessLevel(String processDefinitionId) {
    Process processDefinition = OBDal.getInstance().get(Process.class, processDefinitionId);
    if (processDefinition == null) {
      return -1;
    }
    return Integer.parseInt(processDefinition.getDataAccessLevel());
  }
}
