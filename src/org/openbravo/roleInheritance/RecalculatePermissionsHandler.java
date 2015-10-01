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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.roleInheritance.RoleInheritanceManager.AccessType;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecalculatePermissionsHandler extends BaseActionHandler {
  final static private Logger log = LoggerFactory.getLogger(RecalculatePermissionsHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);
      final String action = request.getString("action");
      String roleId = request.getString("roleId");
      Role role = OBDal.getInstance().get(Role.class, roleId);

      String[] successMessageParameter = { role.getName() };
      String successMessage;
      String textMessage;
      // Possible actions:
      // - DEFAULT: Recalculate permissions for the selected (non template) role
      // - TEMPLATE: Recalculate permissions for all roles inheriting by the selected template role
      if ("TEMPLATE".equals(action)) {
        List<Role> updatedRoles = RoleInheritanceManager.recalculateAllAccessesFromTemplate(role);
        if (updatedRoles.size() > 0) {
          textMessage = composeTemplateAccessMessageText(updatedRoles);
        } else {
          textMessage = Utility.messageBD(new DalConnectionProvider(false),
              "TemplatePermissionsNotModified", OBContext.getOBContext().getLanguage()
                  .getLanguage());
        }
        successMessage = "RecalculateTemplatePermissionsSuccess";
      } else {
        Map<AccessType, List<Integer>> accessCount = RoleInheritanceManager
            .recalculateAllAccessesForRole(role);
        textMessage = composeAccessMessageText(accessCount);
        if (StringUtils.isEmpty(textMessage)) {
          textMessage = Utility.messageBD(new DalConnectionProvider(false),
              "PermissionsNotModified", OBContext.getOBContext().getLanguage().getLanguage());
        }
        successMessage = "RecalculatePermissionsSuccess";
      }

      // Create success message
      JSONObject message = new JSONObject();
      message.put("severity", "success");
      message.put("title", OBMessageUtils.getI18NMessage(successMessage, successMessageParameter));
      message.put("text", textMessage);
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

  private String composeAccessMessageText(Map<AccessType, List<Integer>> map) {
    String text = "";
    try {
      for (AccessType accessType : map.keySet()) {
        List<Integer> counters = (List<Integer>) map.get(accessType);
        int updated = counters.get(0);
        int created = counters.get(1);
        if (updated > 0 || created > 0) {
          Class<?> myClass = Class.forName(accessType.getClassName());
          Entity entity = ModelProvider.getInstance().getEntity(myClass);
          String[] params = { updated + "", created + "" };
          text += OBMessageUtils.getI18NMessage(entity.getName() + "_PermissionsCount", params)
              + " ";
        }
      }
    } catch (Exception ex) {
      log.error("Error creating the text for the returned message", ex);
    }
    return text;
  }

  private String composeTemplateAccessMessageText(List<Role> updatedRoles) {
    String text, updatedRoleList = "";
    for (Role updatedRole : updatedRoles) {
      updatedRoleList += ", " + updatedRole.getName();
    }
    String[] msgParam = { updatedRoleList.substring(1) };
    text = OBMessageUtils.getI18NMessage("PermissionsModifiedForRoles", msgParam);
    return text;
  }
}
