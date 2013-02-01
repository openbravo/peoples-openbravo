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
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.login.MobileCoreLoginHandler;
import org.openbravo.model.ad.access.Session;

public class POSLoginHandler extends MobileCoreLoginHandler {

  private static final long serialVersionUID = 1L;
  private static final String WEB_POS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";
  private static final String WEB_POS_SESSION = "OBPOS_POS";

  @Override
  protected String getFormId() {
    return WEB_POS_FORM_ID;
  }

  @Override
  protected RoleDefaults getDefaults(HttpServletRequest req, HttpServletResponse res,
      String userId, String roleId, Session session) {
    final VariablesSecureApp vars = new VariablesSecureApp(req);
    final String terminalSearchKey = vars.getStringParameter("terminal");
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

    RoleDefaults defaults = new RoleDefaults();
    defaults.role = roleId;

    // terminal defines client, org and warehouse
    OBPOSApplications terminal = apps.get(0);
    defaults.client = (String) DalUtil.getId(terminal.getClient());
    defaults.org = (String) DalUtil.getId(terminal.getOrganization());
    defaults.warehouse = (String) DalUtil.getId(terminal.getOrganization().getObretcoMWarehouse());
    return defaults;
  }

  @Override
  protected String getSessionType() {
    return WEB_POS_SESSION;
  }
}
