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
 * All portions are Copyright (C) 2010-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.myob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.kernel.SessionDynamicTemplateComponent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Creates the Workspace properties list which is initially loaded in the client.
 * 
 * @author mtaal
 */
public class MyOpenbravoComponent extends SessionDynamicTemplateComponent {

  static final String COMPONENT_ID = "MyOpenbravo";
  private static final String TEMPLATEID = "CA8047B522B44F61831A8CAA3AE2A7CD";

  private List<WidgetInstance> widgets = null;
  private Logger log = Logger.getLogger(MyOpenbravoComponent.class);

  @Inject
  private MyOBUtils myOBUtils;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected String getTemplateId() {
    return TEMPLATEID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getId()
   */
  public String getId() {
    return COMPONENT_ID;
  }

  List<String> getAvailableWidgetClasses(String roleId, boolean shouldBeDisplayed) throws Exception {
    OBContext.setAdminMode();
    try {
      String strConditionQuery = WidgetClass.PROPERTY_SUPERCLASS + " is false";
      if (shouldBeDisplayed) {
        strConditionQuery += " and " + WidgetClass.PROPERTY_AVAILABLEINWORKSPACE + " is true";
      }
      final OBQuery<WidgetClass> widgetClassesQry = OBDal.getInstance().createQuery(
          WidgetClass.class, strConditionQuery);
      List<String> widgetClassDefinitions = new ArrayList<String>();
      for (WidgetClass widgetClass : widgetClassesQry.list()) {
        if (isAccessible(widgetClass, roleId)) {
          WidgetClassInfo widgetClassInfo = myOBUtils.getWidgetClassInfo(widgetClass);
          if (widgetClassInfo == null) {
            log.debug("Not found information for widget class with id " + widgetClass.getId());
            continue;
          }
          widgetClassDefinitions.add(widgetClassInfo.getWidgetClassProperties());
          if (!StringUtils.isEmpty(widgetClassInfo.getWidgetClassDefinition())) {
            widgetClassDefinitions.add(widgetClassInfo.getWidgetClassDefinition());
          }
        }
      }
      log.debug("Available Widget Classes: " + widgetClassDefinitions.size());
      return widgetClassDefinitions;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<String> getWidgetInstanceDefinitions() {
    OBContext.setAdminMode();
    try {
      final List<String> result = new ArrayList<String>();
      for (WidgetInstance widget : getContextWidgetInstances()) {
        final JSONObject jsonObject = myOBUtils.getWidgetProvider(widget.getWidgetClass())
            .getWidgetInstanceDefinition(widget);
        result.add(jsonObject.toString());
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getEnableAdminMode() {
    if (ApplicationUtils.isClientAdmin() || ApplicationUtils.isOrgAdmin()
        || ApplicationUtils.isRoleAdmin()) {
      return "true";
    }
    return "false";
  }

  // when changing code, check the ApplicationUtils.getAdminFormSettings
  // method also
  public String getAdminModeValueMap() {
    if (getEnableAdminMode().equals("false")) {
      return "{}";
    }

    try {

      final JSONObject valueMap = new JSONObject();
      final JSONObject jsonLevels = new JSONObject();

      final Role currentRole = OBDal.getInstance().get(Role.class,
          OBContext.getOBContext().getRole().getId());

      if (currentRole.getId().equals("0")) {
        Map<String, String> systemLevel = new HashMap<String, String>();
        systemLevel.put("system", "OBKMO_AdminLevelSystem");
        valueMap.put("level", systemLevel);
        valueMap.put("levelValue", JSONObject.NULL);
        return valueMap.toString();
      }

      final List<RoleOrganization> adminOrgs = ApplicationUtils.getAdminOrgs();
      final List<UserRoles> adminRoles = ApplicationUtils.getAdminRoles();

      if (ApplicationUtils.isClientAdmin()) {
        jsonLevels.put("client", "OBKMO_AdminLevelClient");
      }

      if (adminOrgs.size() > 0) {
        jsonLevels.put("org", "OBKMO_AdminLevelOrg");
      }

      if (adminRoles.size() > 0) {
        jsonLevels.put("role", "OBKMO_AdminLevelRole");
      }

      valueMap.put("level", jsonLevels);

      final Map<String, String> client = new HashMap<String, String>();
      client.put(OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentClient().getName());

      final Map<String, String> org = new HashMap<String, String>();
      for (RoleOrganization currentRoleOrg : adminOrgs) {
        org.put(currentRoleOrg.getOrganization().getId(), currentRoleOrg.getOrganization()
            .getName());
      }

      final Map<String, String> role = new HashMap<String, String>();
      for (UserRoles currentUserRole : adminRoles) {
        role.put(currentUserRole.getRole().getId(), currentUserRole.getRole().getName());
      }

      final JSONObject levelValueMap = new JSONObject();
      levelValueMap.put("client", client);
      levelValueMap.put("org", org);
      levelValueMap.put("role", role);

      valueMap.put("levelValue", levelValueMap);

      return valueMap.toString();
    } catch (JSONException e) {
      log.error("Error building 'Admin Mode' value map: " + e.getMessage(), e);
    }
    return "{}";
  }

  private List<WidgetInstance> getContextWidgetInstances() {
    if (widgets != null) {
      return widgets;
    }
    copyWidgets();

    widgets = new ArrayList<WidgetInstance>();
    final List<WidgetInstance> userWidgets = new ArrayList<WidgetInstance>(
        MyOBUtils.getUserWidgetInstances());
    log.debug("Defined User widgets:" + userWidgets.size());
    // filter on the basis of role access
    for (WidgetInstance widget : userWidgets) {
      if (isAccessible(widget.getWidgetClass(), null)) {
        widgets.add(widget);
      }
    }

    log.debug("Available User widgets:" + widgets.size());
    return widgets;
  }

  private void copyWidgets() {
    final List<WidgetInstance> userWidgets = new ArrayList<WidgetInstance>(
        MyOBUtils.getUserWidgetInstances(false));
    final User user = OBContext.getOBContext().getUser();
    final Role role = OBContext.getOBContext().getRole();
    final Client client = OBContext.getOBContext().getCurrentClient();
    final Set<WidgetInstance> defaultWidgets = getRoleDefaultWidgets(OBContext.getOBContext()
        .getRole(), client.getId(), OBContext.getOBContext().getWritableOrganizations());

    log.debug("Copying new widget instances on user: " + user.getId() + " role: " + role.getId());

    // remove the default widgets which are already defined on the user
    for (WidgetInstance widget : userWidgets) {
      if (widget.getCopiedFrom() != null) {
        defaultWidgets.remove(widget.getCopiedFrom());
      }
    }
    // now copy all the default widgets that are not defined on the user
    final Organization orgZero = OBDal.getInstance().get(Organization.class, "0");
    boolean copyDone = false;
    for (WidgetInstance widget : defaultWidgets) {
      final WidgetInstance copy = (WidgetInstance) DalUtil.copy(widget);
      copy.setClient(client);
      copy.setOrganization(orgZero);
      copy.setVisibleAtRole(role);
      copy.setVisibleAtUser(user);
      copy.setCopiedFrom(widget);
      OBDal.getInstance().save(copy);
      log.debug("Copied widget instance: " + copy.getId() + " of Widget Class: "
          + copy.getWidgetClass().getWidgetTitle());
      copyDone = true;
    }
    if (copyDone) {
      OBDal.getInstance().flush();
    }
  }

  private Set<WidgetInstance> getRoleDefaultWidgets(Role role, String clientId, Set<String> orgs) {
    final Set<WidgetInstance> defaultWidgets = new HashSet<WidgetInstance>();

    if (!role.isForPortalUsers()) {
      // do not include global widgets in portal roles
      defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("OB", null));
      defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("SYSTEM", null));
    }

    defaultWidgets.addAll(MyOBUtils.getDefaultWidgetInstances("CLIENT", new String[] { clientId }));
    defaultWidgets
        .addAll(MyOBUtils.getDefaultWidgetInstances("ORG", orgs.toArray(new String[] {})));
    defaultWidgets
        .addAll(MyOBUtils.getDefaultWidgetInstances("ROLE", new String[] { role.getId() }));

    return defaultWidgets;
  }

  private boolean isAccessible(WidgetClass widgetClass, String _roleId) {
    if (widgetClass.isAllowAnonymousAccess()) {
      return true;
    }
    String roleId = _roleId;
    if (StringUtils.isEmpty(roleId)) {
      roleId = OBContext.getOBContext().getRole().getId();
    }
    for (WidgetClassAccess widgetClassAccess : widgetClass.getOBKMOWidgetClassAccessList()) {
      if (widgetClassAccess.getRole().getId().equals(roleId)) {
        return true;
      }
    }
    return false;
  }
}
