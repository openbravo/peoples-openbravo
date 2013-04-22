/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.LoginUtils.RoleDefaults;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.login.MobileCoreLoginHandler;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.common.enterprise.Warehouse;

public class POSLoginHandler extends MobileCoreLoginHandler {

  private static final long serialVersionUID = 1L;
  private static final String WEB_POS_SESSION = "OBPOS_POS";

  @Override
  protected RoleDefaults getDefaults(HttpServletRequest req, HttpServletResponse res,
      String userId, String roleId, Session session) {

    final VariablesSecureApp vars = new VariablesSecureApp(req);
    final String terminalSearchKey = vars.getStringParameter("terminalName");
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
    } finally {
      OBContext.restorePreviousMode();
    }

    RoleDefaults defaults = new RoleDefaults();
    defaults.role = roleId;

    // terminal defines client, org and warehouse
    OBPOSApplications terminal = apps.get(0);
    defaults.client = (String) DalUtil.getId(terminal.getClient());
    defaults.org = (String) DalUtil.getId(terminal.getOrganization());
    Warehouse warehouse = POSUtils.getWarehouseForTerminal(terminal);
    defaults.warehouse = warehouse != null ? (String) DalUtil.getId(warehouse) : null;
    RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());
    return defaults;
  }

  @Override
  protected String getSessionType() {
    return WEB_POS_SESSION;
  }
}
