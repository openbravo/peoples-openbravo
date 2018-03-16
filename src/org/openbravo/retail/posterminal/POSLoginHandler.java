/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
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
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Language;
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
    User currentUser = OBDal.getInstance().get(User.class, userId);
    Role defaultRole = currentUser.getDefaultRole();
    Role defaultPosRole = currentUser.getOBPOSDefaultPOSRole();

    // When defaults role are not defined, then system will try to find the best role to login in
    // the desired store.
    // Apart from that, if defaults are not valid (-1) system will try to find a better role.
    if ((defaultRole == null && defaultPosRole == null)
        || getDistanceToStoreOrganizationForCertainRole(roleSelectedByDefault, posTerminal) < 1) {
      List<UserRoles> lstCurrentUserRoles = currentUser.getADUserRolesList();
      // skip complex query if there aren't more options
      if (lstCurrentUserRoles.size() > 1) {
        // Look for a better role
        Role newRoleFound = getNearestRoleValidToLoginInWebPosTerminalForCertainUser(currentUser,
            posTerminal);
        // Better role found! is it different to the initial one? -> apply change
        if (newRoleFound != null && !newRoleFound.getId().equals(roleSelectedByDefault.getId())) {
          newRoleId = newRoleFound.getId();
          log.info("A more appropiate role has been found: -" + newRoleFound.getIdentifier()
              + "-. This role will be used instead of  -" + roleSelectedByDefault.getIdentifier()
              + "-.");
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
          "where searchKey = :terminalSearchKey  and ((ad_isorgincluded("
              + "(select organization from ADUser where id= :userId)"
              + ", organization, client.id) <> -1) or " + "(ad_isorgincluded(organization, "
              + "(select organization from ADUser where id= :userId)" + ", client.id) <> -1)) ");
      appQry.setFilterOnReadableClients(false);
      appQry.setFilterOnReadableOrganization(false);
      appQry.setNamedParameter("terminalSearchKey", terminalSearchKey);
      appQry.setNamedParameter("userId", userId);
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

  // Executes a logic to find the best role to be used by a specific user to login in a specific
  // terminal. This logic will try to find a role which covers the following conditions:
  // 1. It is in the list of roles available for the user
  // 2. User-Role relation is active
  // 3. Role is active
  // 4. Role is not marked as portal role
  // 5. Role have access to Web POS form
  // 6. Role-FormAcces relation is active
  // 7. Role-OrgAccess relation is active
  // 8. Distance between store orgnization and organizations allowed for that role is the smallest
  // one
  private Role getNearestRoleValidToLoginInWebPosTerminalForCertainUser(User currentUser,
      OBPOSApplications terminal) {
    StringBuilder hqlQueryStr = new StringBuilder();
    hqlQueryStr
        .append("SELECT rolOrg.role.id, to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) as distance ");
    hqlQueryStr.append("FROM ADRoleOrganization rolOrg ");
    hqlQueryStr.append("WHERE rolOrg.active = true and ");
    hqlQueryStr.append("      rolOrg.role.active = true and ");
    hqlQueryStr.append("      rolOrg.role.forPortalUsers = false and ");
    hqlQueryStr.append("      rolOrg.role.id in (");
    hqlQueryStr.append("        SELECT usrol.role.id FROM ADUserRoles usrol ");
    hqlQueryStr.append("        WHERE usrol.userContact.id = :userId and ");
    hqlQueryStr.append("              usrol.active = true ");
    hqlQueryStr.append("      ) and exists (");
    hqlQueryStr.append("        SELECT 1 FROM ADFormAccess frmacc ");
    hqlQueryStr.append("        WHERE rolOrg.role.id = frmacc.role.id and ");
    hqlQueryStr.append("              frmacc.active = true and ");
    hqlQueryStr.append("              frmacc.specialForm.id = :formId");
    hqlQueryStr.append("      ) and ");
    hqlQueryStr
        .append("      to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) > 0 ");
    hqlQueryStr
        .append("ORDER BY to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) ASC, rolOrg.role.name ASC");
    final org.hibernate.Session hibernateSession = OBDal.getInstance().getSession();
    final Query query = hibernateSession.createQuery(hqlQueryStr.toString());
    query.setParameter("stOrgId", terminal.getOrganization().getId());
    query.setParameter("clientId", terminal.getClient().getId());
    query.setParameter("userId", currentUser.getId());
    query.setParameter("formId", POSUtils.WEB_POS_FORM_ID);
    query.setMaxResults(1);

    @SuppressWarnings("unchecked")
    List<Object> listResults = query.list();
    if (!listResults.isEmpty()) {
      Object[] result = (Object[]) listResults.get(0);
      String selectedRoleId = (String) result[0];
      Role foundRole = OBDal.getInstance().get(Role.class, selectedRoleId);
      log.debug("A more appropiate role has been found: " + foundRole.getIdentifier()
          + " The distance is " + ((BigDecimal) result[1]).toString());
      return foundRole;
    } else {
      return null;
    }
  }

  private int getDistanceToStoreOrganizationForCertainRole(Role currentRole,
      OBPOSApplications terminal) {
    if (hasMobileAccess(currentRole, POSConstants.APP_NAME)) {
      StringBuilder hqlQueryStr = new StringBuilder();
      hqlQueryStr
          .append("SELECT to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) as distance ");
      hqlQueryStr.append("FROM ADRoleOrganization rolOrg ");
      hqlQueryStr.append("WHERE rolOrg.role.id = :roleId and ");
      hqlQueryStr
          .append("      to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) > 0 ");
      hqlQueryStr
          .append("ORDER BY to_number(ad_isorgincluded(:stOrgId, rolOrg.organization.id, :clientId)) ASC, rolOrg.role.name ASC");
      final org.hibernate.Session hibernateSession = OBDal.getInstance().getSession();
      final Query query = hibernateSession.createQuery(hqlQueryStr.toString());
      query.setParameter("stOrgId", terminal.getOrganization().getId());
      query.setParameter("clientId", terminal.getClient().getId());
      query.setParameter("roleId", currentRole.getId());
      query.setMaxResults(1);
      BigDecimal distance = BigDecimal.ONE;
      distance.negate();
      try {
        distance = (BigDecimal) query.uniqueResult();
      } catch (Exception ex) {
        distance = BigDecimal.ONE;
        distance.negate();
      }
      if (distance.compareTo(BigDecimal.ZERO) > 0) {
        log.debug("Default role: " + currentRole.getIdentifier()
            + " has been checked. It is valid for terminal " + terminal.getIdentifier() + ".");
      } else {
        log.info("Default role: " + currentRole.getIdentifier()
            + " has been checked. It is not valid for terminal " + terminal.getIdentifier()
            + ". System will try to find a better role");
      }
      return distance.intValue();
    } else {
      log.error("Default role: " + currentRole.getIdentifier()
          + " does not have access to web POS mobile App ");
      return -1;
    }
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
