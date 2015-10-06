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
package org.openbravo.role.inheritance;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.FICExtension;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.ui.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This FICExtension is used to show a warning message to the user when editing an access which
 * belongs to a role marked as template. When this type of accesses are edited, the changes are
 * propagated to the roles which are using that template to inherit permissions. With this class,
 * the user will be warned before saving the changes.
 */
public class RoleInheritanceWarningFICExtension implements FICExtension {
  private static final Logger log = LoggerFactory
      .getLogger(RoleInheritanceWarningFICExtension.class);

  @Override
  public void execute(String mode, Tab tab, Map<String, JSONObject> columnValues, BaseOBObject row,
      List<String> changeEventCols, List<JSONObject> calloutMessages, List<JSONObject> attachments,
      List<String> jsExcuteCode, Map<String, Object> hiddenInputs, int noteCount,
      List<String> overwrittenAuxiliaryInputs) {

    if ("EDIT".equals(mode)) {
      String entityClassName = ModelProvider.getInstance()
          .getEntityByTableId((String) DalUtil.getId(tab.getTable())).getClassName();
      RoleInheritanceManager manager = WeldUtils
          .getInstanceFromStaticBeanManager(RoleInheritanceManager.class);
      if (!manager.existsInjector(entityClassName)) {
        return;
      }
      InheritedAccessEnabled access = (InheritedAccessEnabled) row;
      Role role = manager.getRole(access, entityClassName);
      String childRoleList = "";
      if (role.isTemplate()) {
        for (RoleInheritance inheritance : role.getADRoleInheritanceInheritFromList()) {
          childRoleList += ", " + inheritance.getRole().getName();
        }
        if (!StringUtils.isEmpty(childRoleList)) {
          String[] msgParam = { childRoleList.substring(1) };
          addWarningMessage(calloutMessages, "EditTemplateRoleAccess", msgParam);
        }
      }
    }
  }

  private void addWarningMessage(List<JSONObject> calloutMessages, String message,
      String[] parameters) {
    try {
      JSONObject msg = new JSONObject();
      String text = OBMessageUtils.getI18NMessage(message, parameters);
      msg.put("text", text);
      msg.put("severity", "TYPE_WARNING");
      calloutMessages.add(msg);
    } catch (JSONException e) {
      log.error("Error parsing JSON Object.", e);
    }
  }
}
