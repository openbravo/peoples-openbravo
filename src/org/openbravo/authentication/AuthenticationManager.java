/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.secureApp.DefaultValidationException;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * @author adrianromero
 * @author iperdomo
 */
public abstract class AuthenticationManager {

  private static final Logger log4j = Logger.getLogger(AuthenticationManager.class);

  protected ConnectionProvider conn = null;
  protected String defaultServletUrl = null;
  protected String localAdress = null;

  public AuthenticationManager() {
  }

  public AuthenticationManager(HttpServlet s) throws AuthenticationException {
    init(s);
  }

  protected void bdErrorAjax(HttpServletResponse response, String strType, String strTitle,
      String strText) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    out.println("<xml-structure>\n");
    out.println("  <status>\n");
    out.println("    <type>" + strType + "</type>\n");
    out.println("    <title>" + strTitle + "</title>\n");
    out.println("    <description><![CDATA[" + strText + "]]></description>\n");
    out.println("  </status>\n");
    out.println("</xml-structure>\n");
    out.close();
  }

  public void init(HttpServlet s) throws AuthenticationException {
    if (s instanceof ConnectionProvider) {
      conn = (ConnectionProvider) s;
    } else {
      conn = new DalConnectionProvider();
    }
    defaultServletUrl = s.getServletConfig().getServletContext()
        .getInitParameter("ServletSinIdentificar");
  }

  public final String authenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException {

    if (localAdress == null) {
      localAdress = HttpBaseUtils.getLocalAddress(request);
    }

    final String userId = doAuthenticate(request, response);

    if (userId == null && !response.isCommitted()) {
      response.sendRedirect(localAdress + defaultServletUrl);
      return null;
    }

    return userId;
  }

  public final void logout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesBase vars = new VariablesBase(request);
    vars.clearSession(true);

    doLogout(request, response);
  }

  protected abstract String doAuthenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException;

  protected abstract void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException;

  private void checkLicense(HttpServletResponse res, VariablesSecureApp vars, String strUserAuth,
      String sessionId) throws IOException, ServletException {
    OBContext.setAdminMode();
    try {
      final ActivationKey ak = ActivationKey.getInstance();
      final OBError errorMsg = new OBError();
      String msgType = "Warning", msg = null, title = null;

      try {
        msgType = hasSystemRole(strUserAuth) ? "Warning" : "Error";
      } catch (Exception ignore) {
      }

      // We check if there is a Openbravo Professional Subscription restriction in the license,
      // or if the last rebuild didn't go well. If any of these are true, then the user is
      // allowed to login only as system administrator
      switch (ak.checkOPSLimitations(sessionId)) {
      case NUMBER_OF_CONCURRENT_USERS_REACHED:
        msg = Utility.messageBD(conn, "NUMBER_OF_CONCURRENT_USERS_REACHED", vars.getLanguage());
        title = Utility.messageBD(conn, "NUMBER_OF_CONCURRENT_USERS_REACHED_TITLE",
            vars.getLanguage());
        log4j.warn("Concurrent Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "CUR");
        errorMsg.setMessage(msg);
        errorMsg.setTitle(title);
        break;
      case NUMBER_OF_SOFT_USERS_REACHED:
        msg = Utility.messageBD(conn, "NUMBER_OF_SOFT_USERS_REACHED", vars.getLanguage());
        title = Utility.messageBD(conn, "NUMBER_OF_SOFT_USERS_REACHED_TITLE", vars.getLanguage());
        log4j.warn("Soft Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, true, "SUR");
        errorMsg.setMessage(msg);
        errorMsg.setTitle(title);
        break;
      case OPS_INSTANCE_NOT_ACTIVE:
        msg = Utility.messageBD(conn, "OPS_INSTANCE_NOT_ACTIVE", vars.getLanguage());
        title = Utility.messageBD(conn, "OPS_INSTANCE_NOT_ACTIVE_TITLE", vars.getLanguage());
        log4j.warn("Innactive OBPS instance - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "IOBPS");
        errorMsg.setMessage(msg);
        errorMsg.setTitle(title);
        break;
      case MODULE_EXPIRED:
        msg = Utility.messageBD(conn, "OPS_MODULE_EXPIRED", vars.getLanguage());
        title = Utility.messageBD(conn, "OPS_MODULE_EXPIRED_TITLE", vars.getLanguage());
        StringBuffer expiredMoudules = new StringBuffer();
        log4j.warn("Expired modules - Session: " + sessionId);
        for (Module module : ak.getExpiredInstalledModules()) {
          expiredMoudules.append("<br/>").append(module.getName());
          log4j.warn("  module:" + module.getName());
        }
        msg += expiredMoudules.toString();
        updateDBSession(sessionId, msgType.equals("Warning"), "ME");
        errorMsg.setMessage(msg);
        errorMsg.setTitle(title);
        return;
      case NO_RESTRICTION:
        break;
      }

      // Build checks
      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      if (sysInfo.getSystemStatus() == null
          || sysInfo.getSystemStatus().equals("RB70")
          || OBPropertiesProvider.getInstance().getOpenbravoProperties()
              .getProperty("safe.mode", "false").equalsIgnoreCase("false")) {
        // Last build went fine and tomcat was restarted. We should continue with the rest of checks
      } else if (sysInfo.getSystemStatus().equals("RB60")
          || sysInfo.getSystemStatus().equals("RB51")) {
        msg = Utility.messageBD(conn, "TOMCAT_NOT_RESTARTED", vars.getLanguage());
        title = Utility.messageBD(conn, "TOMCAT_NOT_RESTARTED_TITLE", vars.getLanguage());
        log4j.warn("Tomcat not restarted");
        updateDBSession(sessionId, true, "RT");

        return;
      } else {
        msg = Utility.messageBD(conn, "LAST_BUILD_FAILED", vars.getLanguage());
        title = Utility.messageBD(conn, "LAST_BUILD_FAILED_TITLE", vars.getLanguage());
        updateDBSession(sessionId, msgType.equals("Warning"), "LBF");

        return;
      }

      try {
        LoginUtils.getLoginDefaults(strUserAuth, "", conn);
      } catch (DefaultValidationException e) {
        updateDBSession(sessionId, false, "F");
        title = Utility.messageBD(conn, "InvalidDefaultLoginTitle", vars.getLanguage()).replace(
            "%0", e.getDefaultField());
        msg = Utility.messageBD(conn, "InvalidDefaultLoginMsg", vars.getLanguage()).replace("%0",
            e.getDefaultField());

        return;
      }

      // All checks passed successfully, continue logging in

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

  private void goToTarget(HttpServletResponse response, VariablesSecureApp vars, boolean doRedirect)
      throws IOException, ServletException {

    String target = vars.getSessionValue("target");

    if (target.equals("")) {
      target = localAdress + "/security/Menu.html";
    }

    if (doRedirect) {
      response.sendRedirect(target);
      return;
    }

    try {
      JSONObject jsonResult = new JSONObject();
      jsonResult.put("showMessage", false);
      jsonResult.put("target", target);

      response.setContentType("application/json;charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.print(jsonResult.toString());
      out.close();
    } catch (JSONException e) {
      log4j.error("Error setting login msg", e);
      throw new ServletException(e);
    }
  }

  private void goToRetry(HttpServletResponse response, VariablesSecureApp vars, String message,
      String title, String msgType, String action, boolean doRedirect) throws IOException,
      ServletException {
    String msg = (message != null && !message.equals("")) ? message
        : "Please enter your username and password.";

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
