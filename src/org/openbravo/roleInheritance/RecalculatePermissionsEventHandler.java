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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.roleInheritance;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.Role;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecalculatePermissionsEventHandler extends BaseActionHandler {
  final static private Logger log = LoggerFactory
      .getLogger(RecalculatePermissionsEventHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);
      final String action = request.getString("action");
      String roleId = request.getString("roleId");
      Role role = OBDal.getInstance().get(Role.class, roleId);

      String[] messageParameter = { role.getName() };
      String successMessage;
      // Possible actions:
      // - DEFAULT: Recalculate permissions for the selected (non template) role
      // - TEMPLATE: Recalculate permissions for all roles inheriting by the selected template role
      if ("TEMPLATE".equals(action)) {
        RoleInheritanceManager.recalculateAllAccessesFromTemplate(role);
        successMessage = "RecalculateTemplatePermissionsSuccess";
      } else {
        RoleInheritanceManager.recalculateAllAccessesForRole(role);
        successMessage = "RecalculatePermissionsSuccess";
      }

      // Create success message
      JSONObject message = new JSONObject();
      message.put("severity", "success");
      message.put("title", "Success");
      message.put("text", OBMessageUtils.getI18NMessage(successMessage, messageParameter));
      response.put("message", message);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("RecalculatePermissionsEventHandler error: " + e.getMessage(), e);
      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (JSONException ignore) {
      }
    }
    return response;
  }
}
