/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base.secureApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.xmlEngine.XmlDocument;

public class LoginHandler extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;
  private String strServletPorDefecto;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    strServletPorDefecto = config.getServletContext().getInitParameter("DefaultServlet");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled()) {
      log4j.debug("start doPost");
    }
    final VariablesSecureApp vars = new VariablesSecureApp(req);

    // Empty session
    req.getSession(true).setAttribute("#Authenticated_user", null);

    final String strUser = vars.getStringParameter("user");

    OBContext.enableAsAdminContext();
    try {

      UserLock lockSettings = new UserLock(strUser);
      Client systemClient = OBDal.getInstance().get(Client.class, "0");

      String language = systemClient.getLanguage().getLanguage();

      delayResponse(lockSettings);

      if (strUser.equals("")) {
        res.sendRedirect(res.encodeRedirectURL(strDireccion + "/security/Login_F1.html"));
      } else {
        final String strPass = vars.getStringParameter("password");
        final String strUserAuth = LoginUtils.getValidUserId(myPool, strUser, strPass);

        String sessionId = createDBSession(req, strUser, strUserAuth);
        if (strUserAuth != null && !lockSettings.isLockedUser()) {
          HttpSession session = req.getSession(true);
          session.setAttribute("#Authenticated_user", strUserAuth);
          session.setAttribute("#AD_SESSION_ID", sessionId);
          // #logginigIn attribute is used in HttpSecureAppServlet to determine whether the logging
          // process is complete or not. At this stage is not complete, we only have a user ID, but
          // no the rest of session info: client, org, role...
          session.setAttribute("#LOGGINGIN", "Y");
          log4j.debug("Correct user/password. Username: " + strUser + " - Session ID:" + sessionId);
          checkLicenseAndGo(res, vars, strUserAuth, sessionId);
        } else {
          String failureTitle;
          String failureMessage;
          if (strUserAuth == null) {
            log4j
                .debug("Failed user/password. Username: " + strUser + " - Session ID:" + sessionId);
            lockSettings.addFail(); // Adds fail to lock the user if needed
            failureTitle = Utility.messageBD(this, "IDENTIFICATION_FAILURE_TITLE", language);
            failureMessage = Utility.messageBD(this, "IDENTIFICATION_FAILURE_MSG", language);
          } else {
            // lockSettings.isLockedUser()
            failureTitle = Utility.messageBD(this, "LOCKED_USER_TITLE", language);
            failureMessage = strUser + " " + Utility.messageBD(this, "LOCKED_USER_MSG", language);
            log4j.debug(strUser + " is blocked cannot activate session ID " + sessionId);
            updateDBSession(sessionId, false, "LU");
          }

          goToRetry(res, vars, failureMessage, failureTitle, "Error", "../security/Login_FS.html");
        }
      }
    } finally {
      OBContext.resetAsAdminContext();
    }
  }

  /**
   * Delays the response of checking in case it is configured in Openbravo.properties
   * (login.trial.delay.increment and login.trial.delay.max), and the current username has login
   * attempts failed.
   */
  private void delayResponse(UserLock lockSettings) {
    int delay = lockSettings.getDelay();
    if (delay > 0) {
      log4j.debug("Delaying response " + delay + " seconds because of the previous login failed.");
      try {
        Thread.sleep(delay * 1000);
      } catch (InterruptedException e) {
        log4j.error("Error delaying login response", e);
      }
    }

  }

  /**
   * Stores session in DB. If the user is valid, it is inserted in the createdBy column, if not user
   * 0 is used.
   */
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
      sl.save(this);
      return sl.getSessionID();
    } catch (Exception e) {
      log4j.error("Error creating DB session", e);
      return null;
    }
  }

  private void checkLicenseAndGo(HttpServletResponse res, VariablesSecureApp vars,
      String strUserAuth, String sessionId) throws IOException {
    OBContext.enableAsAdminContext();
    try {
      ActivationKey ak = new ActivationKey();
      boolean hasSystem = false;

      try {
        hasSystem = SeguridadData.hasSystemRole(this, strUserAuth);
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
        String msg = Utility.messageBD(myPool, "NUMBER_OF_CONCURRENT_USERS_REACHED", vars
            .getLanguage());
        String title = Utility.messageBD(myPool, "NUMBER_OF_CONCURRENT_USERS_REACHED_TITLE", vars
            .getLanguage());
        log4j.warn("Concurrent Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "CUR");
        goToRetry(res, vars, msg, title, msgType, action);
        return;
      case NUMBER_OF_SOFT_USERS_REACHED:
        msg = Utility.messageBD(myPool, "NUMBER_OF_SOFT_USERS_REACHED", vars.getLanguage());
        title = Utility.messageBD(myPool, "NUMBER_OF_SOFT_USERS_REACHED_TITLE", vars.getLanguage());
        action = "../security/Menu.html";
        msgType = "Warning";
        log4j.warn("Soft Users Reached - Session: " + sessionId);
        updateDBSession(sessionId, true, "SUR");
        goToRetry(res, vars, msg, title, msgType, action);
        return;
      case OPS_INSTANCE_NOT_ACTIVE:
        msg = Utility.messageBD(myPool, "OPS_INSTANCE_NOT_ACTIVE", vars.getLanguage());
        title = Utility.messageBD(myPool, "OPS_INSTANCE_NOT_ACTIVE_TITLE", vars.getLanguage());
        log4j.warn("Innactive OBPS instance - Session: " + sessionId);
        updateDBSession(sessionId, msgType.equals("Warning"), "IOBPS");
        goToRetry(res, vars, msg, title, msgType, action);
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
        goToRetry(res, vars, msg, title, msgType, action);
        return;
      }

      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      if (sysInfo.getSystemStatus() == null || sysInfo.getSystemStatus().equals("RB70")
          || this.globalParameters.getOBProperty("safe.mode", "false").equalsIgnoreCase("false")) {
        // Last build went fine and tomcat was restarted. We should login as usual
        goToTarget(res, vars);
      } else if (sysInfo.getSystemStatus().equals("RB60")
          || sysInfo.getSystemStatus().equals("RB50")) {
        String msg = Utility.messageBD(myPool, "TOMCAT_NOT_RESTARTED", vars.getLanguage());
        String title = Utility.messageBD(myPool, "TOMCAT_NOT_RESTARTED_TITLE", vars.getLanguage());
        log4j.warn("Tomcat not restarted");
        updateDBSession(sessionId, true, "RT");
        goToRetry(res, vars, msg, title, "Warning", "../security/Menu.html");
      } else {
        String msg = Utility.messageBD(myPool, "LAST_BUILD_FAILED", vars.getLanguage());
        String title = Utility.messageBD(myPool, "LAST_BUILD_FAILED_TITLE", vars.getLanguage());
        updateDBSession(sessionId, msgType.equals("Warning"), "LBF");
        goToRetry(res, vars, msg, title, msgType, action);
      }
    } finally {
      OBContext.resetAsAdminContext();
    }

  }

  private void updateDBSession(String sessionId, boolean sessionActive, String status) {
    try {
      OBContext.enableAsAdminContext();
      Session session = OBDal.getInstance().get(Session.class, sessionId);
      session.setSessionActive(sessionActive);
      session.setLoginStatus(status);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error("Error updating session in DB", e);
    } finally {
      OBContext.resetAsAdminContext();
    }

  }

  private void goToTarget(HttpServletResponse response, VariablesSecureApp vars) throws IOException {

    final String target = vars.getSessionValue("target");
    if (target.equals("")) {
      response.sendRedirect(strDireccion + "/security/Menu.html");
    } else {
      response.sendRedirect(target);
    }
  }

  private void goToRetry(HttpServletResponse response, VariablesSecureApp vars, String message,
      String title, String msgType, String action) throws IOException {
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
    String msg = (message != null && !message.equals("")) ? message
        : "Please enter your username and password.";
    xmlDocument.setParameter("messageMessage", msg.replaceAll("\\\\n", "<br>"));

    response.setContentType("text/html");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "User-login control Servlet";
  } // end of getServletInfo() method

  /**
   * Utility class to manage user locking and time delays
   * 
   */
  private class UserLock {
    private int delay;
    private int lockAfterTrials;

    private String userName;
    private int numberOfFails;
    private User user;

    public UserLock(String userName) {
      // Read Openbravo.properties for locking configuration. If it's properly configured, it tries
      // to read from the properties file in the source directory not to force to deploy the file to
      // change configuration.
      String sourcePath = globalParameters.getOBProperty("source.path", null);
      Properties obProp;
      if (sourcePath != null && new File(sourcePath + "/config/Openbravo.properties").exists()) {
        try {
          InputStream obPropFile = new FileInputStream(new File(sourcePath
              + "/config/Openbravo.properties"));
          obProp = new Properties();
          obProp.load(obPropFile);
        } catch (Exception e) {
          log4j.error("Error reading properties", e);
          obProp = globalParameters.getOBProperties();
        }
      } else {
        obProp = globalParameters.getOBProperties();
      }
      String propInc = obProp.getProperty("login.trial.delay.increment", "0");
      String propMax = obProp.getProperty("login.trial.delay.max", "0");
      String propLock = obProp.getProperty("login.trial.user.lock", "0");
      if (propInc.equals("")) {
        propInc = "0";
      }
      if (propMax.equals("")) {
        propMax = "0";
      }
      if (propLock.equals("")) {
        propLock = "0";
      }
      int delayInc;
      int delayMax;
      try {
        delayInc = Integer.parseInt(propInc);
      } catch (NumberFormatException e) {
        log4j.error("Could not set login.trial.delay.increment property " + propInc, e);
        delayInc = 0;
      }
      try {
        delayMax = Integer.parseInt(propMax);
      } catch (NumberFormatException e) {
        log4j.error("Could not set login.trial.delay.max property " + propMax, e);
        delayMax = 0;
      }
      try {
        lockAfterTrials = Integer.parseInt(propLock);
      } catch (NumberFormatException e) {
        log4j.error("Could not set login.trial.user.lock property" + propMax, e);
        lockAfterTrials = 0;
      }

      this.userName = userName;
      setUser();
      // Count the how many times this user has failed without success
      StringBuilder hql = new StringBuilder();
      hql.append("select count(*)");
      hql.append("  from ADSession s ");
      hql.append(" where s.loginStatus='F'");
      hql.append("   and s.username = :name");
      hql
          .append("   and s.creationDate > (select coalesce(max(s1.creationDate), s.creationDate-1)");
      hql.append("                           from ADSession s1");
      hql.append("                          where s1.username = s.username");
      hql.append("                            and s1.loginStatus!='F')");
      Query q = OBDal.getInstance().getSession().createQuery(hql.toString());
      q.setParameter("name", userName);

      numberOfFails = ((Long) q.list().get(0)).intValue();
      if (numberOfFails == 0) {
        delay = 0;
        return;
      }

      if (numberOfFails > 0) {
        log4j.warn("Number of failed logins for user " + userName + ": " + numberOfFails);
      }

      delay = delayInc * numberOfFails;
      if (delayMax > 0 && delay > delayMax) {
        delay = delayMax;
      }

    }

    private void setUser() {
      OBCriteria<User> obCriteria = OBDal.getInstance().createCriteria(User.class);
      obCriteria.add(Expression.eq(User.PROPERTY_USERNAME, userName));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);

      user = (User) obCriteria.uniqueResult();
    }

    /**
     * A new failed login attempt, increments the count of fails and blocks the user if needed
     */
    public void addFail() {
      numberOfFails++;
      boolean lockUser = (lockAfterTrials != 0) && (numberOfFails > lockAfterTrials);
      log4j.debug("lock: " + lockUser + " -lock after:" + lockAfterTrials + "- fails:"
          + numberOfFails + " - user:" + user);
      if (lockUser) {
        // Try to lock the user in database
        delay = 0;
        if (user != null) {
          try {
            OBContext.setAdminContext();

            user.setLocked(true);
            OBDal.getInstance().flush();
            log4j.warn(userName + " is locked after " + numberOfFails + " failed logins.");
            return;
          } finally {
            OBContext.resetAsAdminContext();
          }
        }
      }
    }

    public int getDelay() {
      return delay;
    }

    public boolean isLockedUser() {
      if (lockAfterTrials == 0) {
        return false;
      }
      return user != null && user.isLocked();
    }
  }
}
