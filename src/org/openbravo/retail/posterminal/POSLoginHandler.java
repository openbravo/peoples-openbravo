/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.LoginUtils.RoleDefaults;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.login.MobileCoreLoginHandler;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;

public class POSLoginHandler extends MobileCoreLoginHandler {
  private static final Logger log = Logger.getLogger(OrderLoader.class);
  private static final long serialVersionUID = 1L;
  public static final String WEB_POS_SESSION = "OBPOS_POS";

  @Override
  protected RoleDefaults getDefaults(HttpServletRequest req, HttpServletResponse res,
      String userId, String roleId, Session session) {
    final VariablesSecureApp vars = new VariablesSecureApp(req);
    final String terminalSearchKey = vars.getStringParameter("terminalName");

    Role roleSelectedByDefault = null;
    OBPOSApplications posTerminal = null;
    if (roleId != null) {
      roleSelectedByDefault = OBDal.getInstance().get(Role.class, roleId);
    }

    /* Get Current POS Terminal */
    OBCriteria<OBPOSApplications> qApp = OBDal.getInstance()
        .createCriteria(OBPOSApplications.class);
    qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalSearchKey));
    qApp.setFilterOnReadableOrganization(false);
    qApp.setFilterOnReadableClients(false);
    List<OBPOSApplications> apps = qApp.list();
    if (apps.isEmpty()) {
      log4j.error("Cannot find terminal " + terminalSearchKey);
      try {
        errorLogin(res, vars, session, "OBPOS_NO_POS_TERMINAL_TITLE", "OBPOS_NO_POS_TERMINAL_MSG",
            new ArrayList<String>() {
              private static final long serialVersionUID = 1L;
              {
                add(terminalSearchKey);
              }
            });
      } catch (Exception e) {
        log4j.error("Error in login", e);
        return null;
      }
      return null;
    } else if (apps.size() == 1) {
      posTerminal = apps.get(0);
      session.setObposStoreOrg(posTerminal.getOrganization());
    } else {
      // Should never happen
      log4j.error("Terminal " + terminalSearchKey + " is duplicated");
      try {
        errorLogin(res, vars, session, "OBPOS_NO_POS_TERMINAL_TITLE", "OBPOS_NO_POS_TERMINAL_MSG",
            new ArrayList<String>() {
              private static final long serialVersionUID = 1L;
              {
                add(terminalSearchKey);
              }
            });
      } catch (Exception e) {
        log4j.error("Error in login", e);
        return null;
      }
      return null;
    }
    /* End get current pos Terminal */

    String newRoleId = roleSelectedByDefault.getId();
    Boolean newRoleIdFound = false;

    User currentUser = OBDal.getInstance().get(User.class, userId);
    if (!isValidRoleForCurrentWebPOSTerminal(roleSelectedByDefault, posTerminal)) {
      // If the default role selected by mobile core infrastructure is not valid for the terminal
      // where the user is trying to login then:
      // -> Execute a logic to find a role which match with the user
      // -> This logic will iterate roles allowed by user trying to find one which has access to Web
      // POS form and access to the organization of the current terminal
      List<UserRoles> lstCurrentUserRoles = currentUser.getADUserRolesList();
      if (lstCurrentUserRoles.size() > 1) {
        for (UserRoles r : lstCurrentUserRoles) {
          Role roleToAnalyze = r.getRole();
          if (isValidRoleForCurrentWebPOSTerminal(roleToAnalyze, posTerminal)) {
            if (!roleSelectedByDefault.getId().equals(roleToAnalyze.getId())) {
              log.info("Original selected role -" + roleSelectedByDefault.getIdentifier()
                  + "- has been changed for a new role -" + roleToAnalyze.getIdentifier() + "-");
            }
            newRoleId = roleToAnalyze.getId();
            newRoleIdFound = true;
            break;
          }
          if (newRoleIdFound) {
            break;
          }
        }
      }
    }

    OBContext.setAdminMode(false);
    try {
      // Terminal access will be checked to ensure that the user has access to the terminal
      OBQuery<TerminalAccess> accessCrit = OBDal.getInstance().createQuery(TerminalAccess.class,
          "where userContact.id='" + userId + "'");
      accessCrit.setFilterOnReadableClients(false);
      accessCrit.setFilterOnReadableOrganization(false);
      List<TerminalAccess> accessList = accessCrit.list();
      boolean hasAccess = false;
      if (accessList.size() != 0) {
        for (TerminalAccess access : accessList) {
          if (access.getPOSTerminal().getSearchKey().equals(terminalSearchKey)) {
            hasAccess = true;
          }
        }
        if (!hasAccess) {
          try {
            errorLogin(res, vars, session, "OBPOS_USER_NO_ACCESS_TO_TERMINAL_TITLE",
                "OBPOS_USER_NO_ACCESS_TO_TERMINAL_MSG", new ArrayList<String>() {
                  private static final long serialVersionUID = 1L;
                  {
                    add(terminalSearchKey);
                  }
                });
          } catch (Exception e) {
            log4j.error("Error in login", e);
            return null;
          }
        }
      }
      // Issue 28142: We also need to check if the organization of the user belongs to the natural
      // organization tree of the Terminal
      OBQuery<OBPOSApplications> appQry = OBDal.getInstance().createQuery(
          OBPOSApplications.class,
          "where searchKey = '" + terminalSearchKey + "'" + " and ((ad_isorgincluded("
              + "(select organization from ADUser where id='" + userId + "')"
              + ", organization, client.id) <> -1) or " + "(ad_isorgincluded(organization, "
              + "(select organization from ADUser where id='" + userId + "')"
              + ", client.id) <> -1)) ");
      appQry.setFilterOnReadableClients(false);
      appQry.setFilterOnReadableOrganization(false);
      List<OBPOSApplications> appList = appQry.list();
      if (appList.isEmpty()) {
        try {
          errorLogin(res, vars, session, "OBPOS_USER_NO_ACCESS_TO_TERMINAL_TITLE",
              "OBPOS_USER_TERMINAL_DIFFERENT_ORG_MSG", new ArrayList<String>() {
                private static final long serialVersionUID = 1L;
                {
                  add(terminalSearchKey);
                }
              });
        } catch (Exception e) {
          log4j.error("Error in login", e);
          return null;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    RoleDefaults defaults = new RoleDefaults();
    defaults.role = newRoleId;

    // terminal defines client, org and warehouse
    OBPOSApplications terminal = apps.get(0);
    defaults.client = terminal.getClient().getId();
    defaults.org = terminal.getOrganization().getId();
    Warehouse warehouse = POSUtils.getWarehouseForTerminal(terminal);
    defaults.warehouse = warehouse != null ? warehouse.getId() : null;
    RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());
    return defaults;
  }

  // This function will return TRUE (role is valid to be used by Web POS) when
  // 1. Has access to web POS form
  // 2. Has access to the organization of the terminal
  private boolean isValidRoleForCurrentWebPOSTerminal(Role currentRole, OBPOSApplications terminal) {
    boolean validRoleFound = false;
    if (currentRole != null) {
      if (hasMobileAccess(currentRole, POSConstants.APP_NAME)) {
        List<RoleOrganization> lstRoleOrganizationAccess = currentRole.getADRoleOrganizationList();
        for (RoleOrganization rorg : lstRoleOrganizationAccess) {
          if (rorg.isActive()) {
            Organization orgToAnalyze = rorg.getOrganization();
            if (orgToAnalyze.getId().equals(terminal.getOrganization().getId())) {
              validRoleFound = true;
              break;
            }
          }
        }
      }
    }
    return validRoleFound;
  }

  @Override
  protected String getSessionType() {
    return WEB_POS_SESSION;
  }

  @Override
  protected Language getDefaultLanguage(User user, RoleDefaults defaults) {
    if (user.getDefaultLanguage() != null) {
      return user.getDefaultLanguage();
    }

    if (!StringUtils.isEmpty(defaults.role)) {
      Role role = OBDal.getInstance().get(Role.class, defaults.role);
      if (role.getOBPOSDefaultPosLanguage() != null) {
        return role.getOBPOSDefaultPosLanguage();
      }
    }
    return (Language) OBDal.getInstance().createCriteria(Language.class)
        .add(Restrictions.eq(Language.PROPERTY_LANGUAGE, "en_US")).list().get(0);
  }

  @Override
  protected boolean isLoginAccessRestrictedInStoreServer(VariablesSecureApp vars) {
    // access to the POS login is granted, even if the ERP access is restricted in the store server
    // even though access is granted to the POS Login, the onlySystemAdminRoleShouldBeAvailableInErp
    // session flag is set to prevent opening the backend from the POS (this is controlled in the
    // index.jsp file)
    if (isErpAccessRestrictedInStoreServer()) {
      vars.setSessionValue("onlySystemAdminRoleShouldBeAvailableInErp", "Y");
    }
    return false;
  }
}
