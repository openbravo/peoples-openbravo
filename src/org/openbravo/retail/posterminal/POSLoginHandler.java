/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.LoginHandler;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.window.servlet.CalloutHttpServletResponse;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Warehouse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class POSLoginHandler extends LoginHandler {

  private static final long serialVersionUID = 1L;
  private static final String WEB_POS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";
  private static final String WEB_POS_SESSION = "OBPOS_POS";
  private static final String LOGIN_CONCURRENT_USERS_ERROR = "CUR";
  private static final String LOGIN_SOFT_USERS_WARN = "SUR";
  private static final String LOGIN_RESTRICTED_ROLES_ERROR = "RESTR";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
      ServletException {
    try {
      res.setContentType("application/json;charset=UTF-8");
      CalloutHttpServletResponse loginHandlerResponse = new CalloutHttpServletResponse(res);
      final VariablesSecureApp vars = new VariablesSecureApp(req);
      vars.clearSession(true);

      super.doPost(req, loginHandlerResponse);

      JSONObject originalResult = new JSONObject(loginHandlerResponse.getOutputFromWriter());

      final String sessionId = vars.getSessionValue("#AD_Session_ID");
      OBContext.setAdminMode();

      Session session = null;

      if (!StringUtils.isEmpty(sessionId)) {
        session = OBDal.getInstance().get(Session.class, sessionId);
      }

      if (originalResult.has("showMessage") && originalResult.getBoolean("showMessage")) {
        // there's an error in login, discard concurrent users problems

        if (session != null
            && (LOGIN_CONCURRENT_USERS_ERROR.equals(session.getLoginStatus())
                || LOGIN_SOFT_USERS_WARN.equals(session.getLoginStatus()) || LOGIN_RESTRICTED_ROLES_ERROR
                  .equals(session.getLoginStatus()))) {
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

      JSONObject jsonMsg = new JSONObject();

      if (role != null) {
        final String terminalSearchKey = vars.getStringParameter("terminal");
        OBCriteria<OBPOSApplications> qApp = OBDal.getInstance().createCriteria(
            OBPOSApplications.class);
        qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalSearchKey));
        qApp.setFilterOnReadableOrganization(false);
        qApp.setFilterOnReadableClients(false);
        List<OBPOSApplications> apps = qApp.list();
        if (apps.isEmpty()) {
          log4j.error("Cannot find terminal " + terminalSearchKey);
          errorLogin(res, vars, session, "OBPOS_NO_POS_TERMINAL_TITLE",
              "OBPOS_NO_POS_TERMINAL_MSG", new ArrayList<String>() {
                private static final long serialVersionUID = 1L;
                {
                  add(terminalSearchKey);
                }
              });
          return;
        }

        completeLogin(vars, role, userId, apps.get(0));

        vars.setSessionValue("#AD_Role_ID", (String) DalUtil.getId(role));
        session.setLoginStatus(WEB_POS_SESSION);
        session.setSessionActive(true);

        jsonMsg.put("showMessage", false);
        jsonMsg.put("userId", userId);
      } else {
        errorLogin(res, vars, session, "OBPOS_NO_POS_ROLE_TITLE", "OBPOS_NO_POS_ROLE_MSG",
            new ArrayList<String>());
        return;
      }

      final PrintWriter out = res.getWriter();
      out.print(jsonMsg.toString());
      out.close();
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
      OBDal.getInstance().flush(); // flushing in admin mode
      OBContext.restorePreviousMode();
    }
  }

  private void errorLogin(HttpServletResponse res, VariablesSecureApp vars, Session session,
      String title, String msg, List<String> arguments) throws JSONException, IOException {
    session.setSessionActive(false);
    session.setLoginStatus("F");
    vars.clearSession(true);

    Client systemClient = OBDal.getInstance().get(Client.class, "0");
    String language = systemClient.getLanguage().getLanguage();

    String finalMsg = Utility.messageBD(this, msg, language);
    int i = 0;
    for (String arg : arguments) {
      finalMsg = finalMsg.replace("%" + i, arg);
      i++;
    }

    JSONObject jsonMsg = new JSONObject();
    jsonMsg.put("showMessage", true);
    jsonMsg.put("messageType", "Error");
    jsonMsg.put("messageTitle", Utility.messageBD(this, title, language));
    jsonMsg.put("messageText", finalMsg);
    final PrintWriter out = res.getWriter();
    out.print(jsonMsg.toString());
    out.close();
  }

  private void completeLogin(VariablesSecureApp vars, Role role, String userId,
      OBPOSApplications terminal) throws ServletException {
    String strLanguage = "";
    String strIsRTL = "";
    String strRole = "";
    String strClient = "";
    String strOrg = "";
    String strWarehouse = "";

    final User user = OBDal.getInstance().get(User.class, userId);

    strRole = role.getId();

    // terminal defines client, org and warehouse
    strClient = (String) DalUtil.getId(terminal.getClient());
    strOrg = (String) DalUtil.getId(terminal.getOrganization());
    Warehouse warehouse = POSUtils.getWarehouseForTerminal(terminal);
    strWarehouse = warehouse != null ? (String) DalUtil.getId(warehouse) : null;

    if (user.getDefaultLanguage() != null) {
      strLanguage = user.getDefaultLanguage().getLanguage();
      strIsRTL = user.getDefaultLanguage().isRTLLanguage() ? "Y" : "N";
    } else {
      strLanguage = "en_US";
      strIsRTL = "N";
    }

    // note fill session arguments will set the LOGGINGIN session var
    // to N
    if (LoginUtils.fillSessionArguments(this, vars, userId, strLanguage, strIsRTL, strRole,
        strClient, strOrg, strWarehouse)) {
      readProperties(vars, globalParameters.getOpenbravoPropertiesPath());
      readNumberFormat(vars, globalParameters.getFormatPath());
    } else {
      // Re-login
      log4j.error("Unable to fill session Arguments for: " + userId);
      throw new OBException("Unable to do login");
    }

    // Login process if finished, set the flag as not logging in
    // this flag may not be removed from the session, it must be set
    // to N to prevent re-initializing the session continuously
    vars.setSessionValue("#loggingIn", "N");
  }

  /**
   * Copied from HSAS
   */
  protected void readProperties(VariablesSecureApp vars, String strFileProperties) {
    // Read properties file.
    final Properties properties = new Properties();
    try {

      properties.load(new FileInputStream(strFileProperties));
      final String javaDateFormat = properties.getProperty("dateFormat.java");
      vars.setSessionValue("#AD_JavaDateFormat", javaDateFormat);

      final String javaDateTimeFormat = properties.getProperty("dateTimeFormat.java");
      vars.setSessionValue("#AD_JavaDateTimeFormat", javaDateTimeFormat);

      final String sqlDateTimeFormat = properties.getProperty("dateTimeFormat.sql");
      vars.setSessionValue("#AD_SqlDateTimeFormat", sqlDateTimeFormat);

      final String jsDateFormat = properties.getProperty("dateFormat.js");
      vars.setSessionValue("#AD_JsDateFormat", jsDateFormat);

      final String sqlDateFormat = properties.getProperty("dateFormat.sql");
      vars.setSessionValue("#AD_SqlDateFormat", sqlDateFormat);

      final String pentahoServer = properties.getProperty("pentahoServer");
      vars.setSessionValue("#pentahoServer", pentahoServer);

      final String sourcePath = properties.getProperty("source.path");
      vars.setSessionValue("#sourcePath", sourcePath);

      if (log4j.isDebugEnabled()) {
        log4j.debug("strFileProperties: " + strFileProperties);
        log4j.debug("javaDateFormat: " + javaDateFormat);
        log4j.debug("javaDateTimeFormat: " + javaDateTimeFormat);
        log4j.debug("jsDateFormat: " + jsDateFormat);
        log4j.debug("sqlDateFormat: " + sqlDateFormat);
        log4j.debug("pentahoServer: " + pentahoServer);
        log4j.debug("sourcePath: " + sourcePath);
      }
    } catch (final IOException e) {
      // catch possible io errors from readLine()
      log4j.error("Error reading properties", e);
    }
  }

  /**
   * Copied from HSAS
   */
  protected void readNumberFormat(VariablesSecureApp vars, String strFormatFile) {
    String strNumberFormat = "###,##0.00"; // Default number format
    String strGroupingSeparator = ","; // Default grouping separator
    String strDecimalSeparator = "."; // Default decimal separator
    final String formatNameforJrxml = "euroInform"; // Name of the format to use
    final HashMap<String, String> formatMap = new HashMap<String, String>();

    try {
      // Reading number format configuration
      final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      final Document doc = docBuilder.parse(new File(strFormatFile));
      doc.getDocumentElement().normalize();
      final NodeList listOfNumbers = doc.getElementsByTagName("Number");
      final int totalNumbers = listOfNumbers.getLength();
      for (int s = 0; s < totalNumbers; s++) {
        final Node NumberNode = listOfNumbers.item(s);
        if (NumberNode.getNodeType() == Node.ELEMENT_NODE) {
          final Element NumberElement = (Element) NumberNode;
          final String strNumberName = NumberElement.getAttributes().getNamedItem("name")
              .getNodeValue();
          // store in session all the formats
          final String strFormatOutput = NumberElement.getAttributes().getNamedItem("formatOutput")
              .getNodeValue();
          formatMap.put(strNumberName, strFormatOutput);
          vars.setSessionValue("#FormatOutput|" + strNumberName, strFormatOutput);
          vars.setSessionValue("#DecimalSeparator|" + strNumberName, NumberElement.getAttributes()
              .getNamedItem("decimal").getNodeValue());
          vars.setSessionValue("#GroupSeparator|" + strNumberName, NumberElement.getAttributes()
              .getNamedItem("grouping").getNodeValue());
          // set the numberFormat to be used in the renderJR function
          if (strNumberName.equals(formatNameforJrxml)) {
            strDecimalSeparator = NumberElement.getAttributes().getNamedItem("decimal")
                .getNodeValue();
            strGroupingSeparator = NumberElement.getAttributes().getNamedItem("grouping")
                .getNodeValue();
            strNumberFormat = strFormatOutput;
          }
        }
      }
    } catch (final Exception e) {
      log4j.error("error reading number format", e);
    }
    vars.setSessionObject("#FormatMap", formatMap);
    vars.setSessionValue("#AD_ReportNumberFormat", strNumberFormat);
    vars.setSessionValue("#AD_ReportGroupingSeparator", strGroupingSeparator);
    vars.setSessionValue("#AD_ReportDecimalSeparator", strDecimalSeparator);
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
    role = user.getDefaultRole();
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
