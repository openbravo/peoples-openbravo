/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.LoginHandler;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.window.servlet.CalloutHttpServletResponse;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;

public class POSLoginHandler extends LoginHandler {

  private static final long serialVersionUID = 1L;
  private static final String WEB_POS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";
  private static final String WEB_POS_SESSION = "OBPOS_POS";
  private static final String LOGIN_CONCURRENT_USERS_ERROR = "CUR";
  private static final String LOGIN_SOFT_USERS_WARN = "SUR";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
      ServletException {
    try {
      res.setContentType("application/json;charset=UTF-8");
      CalloutHttpServletResponse loginHandlerResponse = new CalloutHttpServletResponse(res);
      super.doPost(req, loginHandlerResponse);

      JSONObject originalResult = new JSONObject(loginHandlerResponse.getOutputFromWriter());

      final VariablesSecureApp vars = new VariablesSecureApp(req);

      final String sessionId = vars.getSessionValue("#AD_Session_ID");
      System.out.println("Session " + sessionId);
      OBContext.setAdminMode();

      Session session = null;

      if (!StringUtils.isEmpty(sessionId)) {
        session = OBDal.getInstance().get(Session.class, sessionId);
      }

      if (originalResult.has("showMessage") && originalResult.getBoolean("showMessage")) {
        // there's an error in login, discard concurrent users problems

        if (session != null
            && (LOGIN_CONCURRENT_USERS_ERROR.equals(session.getLoginStatus()) || LOGIN_SOFT_USERS_WARN
                .equals(session))) {
          // no problem continue, mark this session to be POS
        } else {
          // other errors should be rose
          PrintWriter q = res.getWriter();
          q.write(loginHandlerResponse.getOutputFromWriter());
          q.close();
          return;
        }
      }

      final String userId = (String) req.getSession().getAttribute("#Authenticated_user");
      Role role = getPOSRole(userId);
      if (role != null) {
        vars.setSessionValue("#AD_Role_ID", (String) DalUtil.getId(role));
        session.setLoginStatus(WEB_POS_SESSION);
        OBDal.getInstance().flush();
      } else {
        session.setSessionActive(false);
        session.setLoginStatus("F");
        OBDal.getInstance().flush();

        vars.clearSession(true);

        Client systemClient = OBDal.getInstance().get(Client.class, "0");
        String language = systemClient.getLanguage().getLanguage();

        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("showMessage", true);
        jsonMsg.put("messageType", "Error");
        jsonMsg.put("messageTitle", Utility.messageBD(this, "OBPOS_NO_POS_ROLE_TITLE", language));
        jsonMsg.put("messageText", Utility.messageBD(this, "OBPOS_NO_POS_ROLE_MSG", language));
        final PrintWriter out = res.getWriter();
        out.print(jsonMsg.toString());
        out.close();
      }
    } catch (Exception e) {
      log4j.error("Error in POS login", e);
      try {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("showMessage", true);
        jsonMsg.put("messageType", "Error");
        jsonMsg.put("messageTitle", e.getMessage());
        final PrintWriter out = res.getWriter();
        out.print(jsonMsg.toString());
        out.close();
      } catch (Exception e1) {
        log4j.error("Error setting error msg", e1);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private Role getPOSRole(String userId) {
    final User user = OBDal.getInstance().get(User.class, userId);

    // get default POS role
    Role role = user.getOBPOSDefaultPOSRole();
    if (role != null) {
      if (hasPOSAccess(role)) {
        return role;
      } else {
        log4j.warn("Default POS role (" + role.getName() + ") of user " + user
            + " has no Web POS access");
      }
    }

    // get standard default role
    user.getDefaultRole();
    if (role != null) {
      if (hasPOSAccess(role)) {
        return role;
      }
    }

    // take first role with POS access
    for (UserRoles r : user.getADUserRolesList()) {
      role = r.getRole();
      if (hasPOSAccess(role)) {
        return role;
      }
    }

    // no rule to use
    log4j.warn("User " + user + " has no POS role");
    return null;

  }

  private boolean hasPOSAccess(Role role) {
    for (FormAccess formAccess : role.getADFormAccessList()) {
      if (DalUtil.getId(formAccess.getSpecialForm()).equals(WEB_POS_FORM_ID)) {
        return true;
      }
    }
    return false;
  }
}
