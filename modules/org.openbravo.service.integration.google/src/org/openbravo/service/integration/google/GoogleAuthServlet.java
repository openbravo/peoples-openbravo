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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.integration.google;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.exception.ConstraintViolationException;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBVersion;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.service.integration.openid.OBSOIDUserIdentifier;
import org.openbravo.service.integration.openid.OpenIDManager;
import org.openbravo.xmlEngine.XmlDocument;
import org.openid4java.discovery.Identifier;

/**
 * @author iperdomo
 */
public class GoogleAuthServlet extends HttpBaseServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger log = Logger.getLogger(GoogleAuthServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    VariablesBase vars = new VariablesBase(req);

    OBContext.setAdminMode(false);

    try {

      String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

      if (!ActivationKey.getInstance().isActive()) {
        OBError error = new OBError();
        // messageDB escapes double-quotes
        error.setTitle("");
        error.setMessage(Utility.messageBD(this, "OBSEIG_Activate", lang)
            .replaceAll("&quot;", "\""));
        error.setType("Error");
        vars.setSessionObject("LoginErrorMsg", error);
        resp.sendRedirect(strDireccion);
        return;
      }

      if ("true".equals(vars.getStringParameter("is_return"))) {
        processReturn(req, resp);
      } else {
        if ("true".equals(vars.getStringParameter("is_association"))) {
          vars.setSessionValue("is_association", "true");
        }

        OpenIDManager.getInstance()
            .authRequest(OpenIDManager.GOOGLE_OPENID_DISCOVER_URL, req, resp);
      }

    } catch (Exception e) {
      log4j.error("Error trying to authenticate using Google Auth service: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void processReturn(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final String loginPageURL = getServletConfig().getServletContext().getInitParameter(
        "ServletSinIdentificar");

    final VariablesSecureApp vars = new VariablesSecureApp(req);

    try {
      OBContext.setAdminMode(false);

      String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

      Identifier oid = OpenIDManager.getInstance().getIdentifier(req);

      if ("true".equals(vars.getSessionValue("is_association"))) {
        vars.removeSessionValue("is_association");
        try {
          OpenIDManager.getInstance().associateAccount(oid, req, resp);
          vars.setSessionValue("startup-message", Utility.messageBD(this, "OBSEIG_LinkedOK", lang)
              .replaceAll("&quot;", "\""));
          vars.setSessionValue("startup-message-title", Utility.messageBD(this, "ProcessOK", lang)
              .replaceAll("&quot;", "\""));

        } catch (ConstraintViolationException e) {
          log.error("Error trying to associate account with OpenID identifier: " + oid.toString(),
              e);
          // User notification
          vars.setSessionValue(
              "startup-message",
              Utility.messageBD(this, "OBSEIG_DuplicatedIdentifier", lang).replaceAll("&quot;",
                  "\""));
          vars.setSessionValue("startup-message-title",
              Utility.messageBD(this, "ProcessFailed", lang).replaceAll("&quot;", "\""));

        }
        resp.sendRedirect(strDireccion);
        return;
      }

      User user = OpenIDManager.getInstance().getUser(oid);

      if (user == null) {
        user = createUser(oid, req, resp);
        if (user == null) {
          return;
        }
      }

      HttpSession session = req.getSession(true);
      session.setAttribute("#Authenticated_user", null);

      // TODO: Refactor LoginHandler/AuthenticationManager/HttpSecureAppServlet to make this part
      // of the code extensible with a module
      // Code copied from LoginHandler
      String sessionId = createDBSession(req, user.getUsername(), user.getId());
      session.setAttribute("#Authenticated_user", user.getId());
      session.setAttribute("#AD_SESSION_ID", sessionId);
      session.setAttribute("#LOGGINGIN", "Y");

      checkLicenseAndGo(req, resp, vars, user.getId(), sessionId, true);

    } catch (Exception e) {
      log.error("Error processing return of Google Auth Service:" + e.getMessage(), e);
      this.getServletContext().getRequestDispatcher(loginPageURL).forward(req, resp);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unchecked")
  private User createUser(Identifier oid, HttpServletRequest req, HttpServletResponse resp)
      throws OBException, IOException {
    Map<String, String> attributes = (Map<String, String>) req.getAttribute("attributes");

    OBSEIGDefaults newUserDefaults;
    VariablesBase vars = new VariablesBase(req);

    String lang = OBDal.getInstance().get(Client.class, "0").getLanguage().getLanguage();

    if (attributes.get(OpenIDManager.ATTRIBUTE_FIRSTNAME) == null
        || attributes.get(OpenIDManager.ATTRIBUTE_LASTNAME) == null
        || attributes.get(OpenIDManager.ATTRIBUTE_EMAIL) == null) {
      throw new OBException("Google Integration: OpenID identifier attributes missing");
    }

    OBCriteria<OBSEIGDefaults> defaults = OBDal.getInstance().createCriteria(OBSEIGDefaults.class);
    defaults.setFilterOnReadableClients(false);
    defaults.setFilterOnReadableOrganization(false);

    if (defaults.count() == 0) {
      OBError error = new OBError();
      error.setMessage(Utility.messageBD(this, "OBSEIG_NoDefaultConf", lang)
          .replaceAll("@@email@@", attributes.get(OpenIDManager.ATTRIBUTE_EMAIL))
          .replaceAll("&quot;", "\""));
      error.setTitle("");
      error.setType("Error");
      vars.setSessionObject("LoginErrorMsg", error);
      resp.sendRedirect(strDireccion);
      return null;
    }

    if (defaults.count() > 1) {
      OBError error = new OBError();
      error.setMessage(Utility.messageBD(this, "OBSEIG_TooMuchConf", lang)
          .replaceAll("@@email@@", attributes.get(OpenIDManager.ATTRIBUTE_EMAIL))
          .replaceAll("&quot;", "\""));
      error.setTitle("");
      error.setType("Error");
      vars.setSessionObject("LoginErrorMsg", error);
      resp.sendRedirect(strDireccion);
      return null;
    }

    newUserDefaults = defaults.list().get(0);

    User newUser = OBProvider.getInstance().get(User.class);
    final String name = attributes.get(OpenIDManager.ATTRIBUTE_FIRSTNAME) + " "
        + attributes.get(OpenIDManager.ATTRIBUTE_LASTNAME);

    newUser.setName(name);
    newUser.setEmail(attributes.get(OpenIDManager.ATTRIBUTE_EMAIL));
    newUser.setOrganization(newUserDefaults.getRole().getOrganization());
    newUser.setClient(newUserDefaults.getRole().getClient());
    newUser.setActive(newUserDefaults.isNewuseractive());
    newUser.setUsername(name);

    OBDal.getInstance().save(newUser);

    OBSOIDUserIdentifier userIdentifier = OBProvider.getInstance().get(OBSOIDUserIdentifier.class);
    userIdentifier.setUserContact(newUser);
    userIdentifier.setClient(newUserDefaults.getRole().getClient());
    userIdentifier.setOrganization(newUserDefaults.getRole().getOrganization());
    userIdentifier.setOpenIDIdentifier(oid.toString());
    userIdentifier.setActive(true);

    OBDal.getInstance().save(userIdentifier);

    UserRoles uRoles = OBProvider.getInstance().get(UserRoles.class);
    uRoles.setClient(newUserDefaults.getRole().getClient());
    uRoles.setOrganization(newUserDefaults.getRole().getOrganization());
    uRoles.setUserContact(newUser);
    uRoles.setRole(newUserDefaults.getRole());

    OBDal.getInstance().save(uRoles);

    newUser.setDefaultRole(newUserDefaults.getRole());

    OBDal.getInstance().flush();

    return newUser;
  }

  // All the methods below are a copy and modified from LoginHandler.java

  private String createDBSession(HttpServletRequest req, String strUser, String strUserAuth) {
    try {
      String usr = strUserAuth == null ? "0" : strUserAuth;

      final SessionLogin sl = new SessionLogin(req, "0", "0", usr);

      if (strUserAuth == null) {
        sl.setStatus("F");
      } else {
        sl.setStatus("S");
      }

      sl.setUserName(strUser);
      sl.setServerUrl(strDireccion);
      sl.save();
      return sl.getSessionID();
    } catch (Exception e) {
      log4j.error("Error creating DB session", e);
      return null;
    }
  }

  @SuppressWarnings("incomplete-switch")
  private void checkLicenseAndGo(HttpServletRequest req, HttpServletResponse res,
      VariablesSecureApp vars, String strUserAuth, String sessionId, boolean doRedirect)
      throws IOException, ServletException {
    OBContext.setAdminMode();
    try {
      ActivationKey ak = ActivationKey.getInstance();
      boolean hasSystem = false;

      try {
        hasSystem = hasSystemRole(strUserAuth);
      } catch (Exception ignore) {
        log4j.error(ignore);
      }
      String msgType, action;
      if (hasSystem) {
        msgType = "Warning";
        action = "../security/Menu.html";
      } else {
        msgType = "Error";
        action = "../security/Login_FS.html";
      }

      // We check if there is a Openbravo Professional Subscription restriction in the license,
      // or if the last rebuild didn't go well. If any of these are true, then the user is
      // allowed to login only as system administrator
      switch (ak.checkOPSLimitations(sessionId)) {
      case NUMBER_OF_CONCURRENT_USERS_REACHED:
        String msg = Utility.messageBD(myPool, "NUMBER_OF_CONCURRENT_USERS_REACHED",
            vars.getLanguage());
        String title = Utility.messageBD(myPool, "NUMBER_OF_CONCURRENT_USERS_REACHED_TITLE",
            vars.getLanguage());
        log4j.warn("Concurrent Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "CUR");
        goToRetry(res, vars, msg, title, msgType, action, doRedirect);
        return;
      case NUMBER_OF_SOFT_USERS_REACHED:
        msg = Utility.messageBD(myPool, "NUMBER_OF_SOFT_USERS_REACHED", vars.getLanguage());
        title = Utility.messageBD(myPool, "NUMBER_OF_SOFT_USERS_REACHED_TITLE", vars.getLanguage());
        action = "../security/Menu.html";
        msgType = "Warning";
        log4j.warn("Soft Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, true, "SUR");
        goToRetry(res, vars, msg, title, msgType, action, doRedirect);
        return;
      case OPS_INSTANCE_NOT_ACTIVE:
        msg = Utility.messageBD(myPool, "OPS_INSTANCE_NOT_ACTIVE", vars.getLanguage());
        title = Utility.messageBD(myPool, "OPS_INSTANCE_NOT_ACTIVE_TITLE", vars.getLanguage());
        log4j.warn("Innactive OBPS instance - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "IOBPS");
        goToRetry(res, vars, msg, title, msgType, action, doRedirect);
        return;
      case MODULE_EXPIRED:
        msg = Utility.messageBD(myPool, "OPS_MODULE_EXPIRED", vars.getLanguage());
        title = Utility.messageBD(myPool, "OPS_MODULE_EXPIRED_TITLE", vars.getLanguage());
        StringBuffer expiredMoudules = new StringBuffer();
        log4j.warn("Expired modules - Session: " + sessionId);
        for (Module module : ak.getExpiredInstalledModules()) {
          expiredMoudules.append("<br/>").append(module.getName());
          log4j.warn("  module:" + module.getName());
        }
        msg += expiredMoudules.toString();
        updateDBSession(sessionId, msgType.equals("Warning"), "ME");
        goToRetry(res, vars, msg, title, msgType, action, doRedirect);
        return;
      }

      // Build checks
      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      if (sysInfo.getSystemStatus() == null || sysInfo.getSystemStatus().equals("RB70")
          || this.globalParameters.getOBProperty("safe.mode", "false").equalsIgnoreCase("false")) {
        // Last build went fine and tomcat was restarted. We should continue with the rest of checks
      } else if (sysInfo.getSystemStatus().equals("RB60")
          || sysInfo.getSystemStatus().equals("RB51")) {
        String msg = Utility.messageBD(myPool, "TOMCAT_NOT_RESTARTED", vars.getLanguage());
        String title = Utility.messageBD(myPool, "TOMCAT_NOT_RESTARTED_TITLE", vars.getLanguage());
        log4j.warn("Tomcat not restarted");
        updateDBSession(sessionId, true, "RT");
        goToRetry(res, vars, msg, title, "Warning", "../security/Menu.html", doRedirect);
        return;
      } else {
        String msg = Utility.messageBD(myPool, "LAST_BUILD_FAILED", vars.getLanguage());
        String title = Utility.messageBD(myPool, "LAST_BUILD_FAILED_TITLE", vars.getLanguage());
        updateDBSession(sessionId, msgType.equals("Warning"), "LBF");
        goToRetry(res, vars, msg, title, msgType, action, doRedirect);
        return;
      }

      // All checks passed successfully, continue logging in
      res.sendRedirect(strDireccion);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void updateDBSession(String sessionId, boolean sessionActive, String status) {
    try {
      OBContext.setAdminMode();
      Session session = OBDal.getInstance().get(Session.class, sessionId);
      session.setSessionActive(sessionActive);
      session.setLoginStatus(status);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error("Error updating session in DB", e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void goToRetry(HttpServletResponse response, VariablesSecureApp vars, String message,
      String title, String msgType, String action, boolean doRedirect) throws IOException,
      ServletException {
    String msg = (message != null && !message.equals("")) ? message
        : "Please enter your username and password.";

    if (OBVersion.getInstance().is30() && !doRedirect) {
      // 3.0 instances show the message in the same login window, return a json object with the info
      // to print the message
      try {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("showMessage", true);
        jsonMsg.put("target", "Error".equals(msgType) ? null : action);
        jsonMsg.put("messageType", msgType);
        jsonMsg.put("messageTitle", title);
        jsonMsg.put("messageText", msg);

        response.setContentType("application/json;charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.print(jsonMsg.toString());
        out.close();
      } catch (JSONException e) {
        log4j.error("Error setting login msg", e);
        throw new ServletException(e);
      }
    } else {
      // 2.50 instances show the message in a new window, print that window
      String discard[] = { "" };

      if (msgType.equals("Error")) {
        discard[0] = "continueButton";
      } else {
        discard[0] = "backButton";
      }

      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/base/secureApp/HtmlErrorLogin", discard).createXmlDocument();

      // pass relevant mesasge to show inside the error page
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("messageType", msgType);
      xmlDocument.setParameter("action", action);
      xmlDocument.setParameter("messageTitle", title);
      xmlDocument.setParameter("messageMessage", msg.replaceAll("\\\\n", "<br>"));

      response.setContentType("text/html");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private boolean hasSystemRole(String userId) throws Exception {
    OBQuery<UserRoles> urQuery = OBDal.getInstance().createQuery(UserRoles.class,
        "userContact.id = :userid and role.userLevel = 'S'");
    urQuery.setNamedParameter("userid", userId);
    urQuery.setFilterOnReadableClients(false);
    urQuery.setFilterOnReadableOrganization(false);

    return urQuery.count() > 0;
  }

}
