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
package org.openbravo.client.application.navigationbarcomponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.KernelServlet;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.SessionDynamicTemplateComponent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseRestriction;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.system.SystemInformation;

/**
 * Component that provides the context information of the current user within the 'Profile' widget.
 */
public class UserInfoComponent extends SessionDynamicTemplateComponent {
  // private static final Logger log = Logger.getLogger(UserInfoComponent.class);
  private static final String COMPONENT_ID = "UserInfo";
  private static final String TEMPLATE_ID = "CB89E38CF75545499BF0B91FA6B233E5";

  private List<Role> userRoles;

  @Override
  public String getId() {
    return COMPONENT_ID;
  }

  @Override
  protected String getTemplateId() {
    return TEMPLATE_ID;
  }

  public String getContextRoleId() {
    return OBContext.getOBContext().getRole().getId();
  }

  public String getContextClientId() {
    return OBContext.getOBContext().getRole().getClient().getIdentifier();
  }

  public String getContextOrganizationId() {
    return OBContext.getOBContext().getCurrentOrganization().getId();
  }

  public String getContextWarehouseId() {
    if (OBContext.getOBContext().getWarehouse() != null) {
      return OBContext.getOBContext().getWarehouse().getId();
    }
    return "";
  }

  public String getContextLanguageId() {
    return OBContext.getOBContext().getLanguage().getId();
  }

  public List<Language> getLanguages() {
    final OBQuery<Language> languages = OBDal.getInstance().createQuery(
        Language.class,
        "(" + Language.PROPERTY_SYSTEMLANGUAGE + "=true or " + Language.PROPERTY_BASELANGUAGE
            + "=true)");
    languages.setFilterOnReadableClients(false);
    languages.setFilterOnReadableOrganization(false);
    return languages.list();
  }

  public List<Role> getUserRolesSorted() {
    final List<Role> sortedRoles = new ArrayList<Role>(getUserRoles());
    DalUtil.sortByIdentifier(sortedRoles);
    return sortedRoles;
  }

  public List<Role> getUserRoles() {
    if (userRoles != null) {
      return userRoles;
    }
    ActivationKey ak = ActivationKey.getInstance();
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
    boolean correctSystemStatus = sysInfo.getSystemStatus() == null
        || KernelServlet.getGlobalParameters().getOBProperty("safe.mode", "false")
            .equalsIgnoreCase("false") || sysInfo.getSystemStatus().equals("RB70");
    if (!correctSystemStatus) {
      return Collections.singletonList(OBDal.getInstance().get(Role.class, "0"));
    }

    if (getParameters().get(KernelConstants.HTTP_SESSION) != null) {
      final HttpSession session = (HttpSession) getParameters().get(KernelConstants.HTTP_SESSION);
      final String dbSessionId = (String) session.getAttribute("#AD_Session_ID".toUpperCase());
      LicenseRestriction limitation = ak.checkOPSLimitations(dbSessionId);
      if (limitation == LicenseRestriction.OPS_INSTANCE_NOT_ACTIVE
          || limitation == LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED
          || limitation == LicenseRestriction.MODULE_EXPIRED
          || limitation == LicenseRestriction.NOT_MATCHED_INSTANCE
          || limitation == LicenseRestriction.HB_NOT_ACTIVE
          || limitation == LicenseRestriction.ON_DEMAND_OFF_PLATFORM
          || limitation == LicenseRestriction.POS_TERMINALS_EXCEEDED) {
        return Collections.singletonList(OBDal.getInstance().get(Role.class, "0"));
      }
    }

    final HttpServletRequest request = RequestContext.get().getRequest();
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    boolean onlySystemAdminAccess = "Y".equals(vars
        .getSessionValue("onlySystemAdminRoleShouldBeAvailableInErp"));
    if (onlySystemAdminAccess) {
      return Collections.singletonList(OBDal.getInstance().get(Role.class, "0"));
    }

    // return the complete role list for the current user
    final OBQuery<UserRoles> rolesQuery = OBDal.getInstance().createQuery(UserRoles.class,
        " userContact.id=? and role.active=true and role.isrestrictbackend=false");
    rolesQuery.setFilterOnReadableClients(false);
    rolesQuery.setFilterOnReadableOrganization(false);
    rolesQuery.setParameters(Collections.singletonList((Object) OBContext.getOBContext().getUser()
        .getId()));
    userRoles = new ArrayList<Role>();
    for (UserRoles userRole : rolesQuery.list()) {
      if (!userRoles.contains(userRole.getRole())) {
        userRoles.add(userRole.getRole());
      }
    }
    return userRoles;
  }

  public List<RoleInfo> getUserRolesInfo() {
    List<RoleInfo> list = new ArrayList<>();
    for (Role role : getUserRoles()) {
      list.add(new RoleInfo(role));
    }
    return list;
  }
}
