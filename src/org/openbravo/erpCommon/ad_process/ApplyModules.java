/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.AntExecutor;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.ModuleLog;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.service.system.ReloadContext;
import org.openbravo.service.system.RestartTomcat;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Servlet for the Apply Modules method.
 * 
 */
public class ApplyModules extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(request, response, vars);
    } else if (vars.commandIn("STARTAPPLY")) {
      startApply(response, vars);
    } else if (vars.commandIn("TOMCAT")) {
      printPageTomcat(request, response, vars);
    } else if (vars.commandIn("RESTART")) {
      restartApplicationServer(response, vars);
    } else if (vars.commandIn("RELOAD")) {
      reloadContext(response, vars);
    } else {
      pageError(response);
    }
  }

  /**
   * Prints a page that only allows the user to restart or reload Tomcat
   */
  private void printPageTomcat(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars) throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartTomcat").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Reloads the application context to after building the application to apply modules.
   * 
   * @param response
   *          the HttpServletResponse to write to
   * @param vars
   *          the application variables
   * @throws IOException
   * @throws ServletException
   */
  private void reloadContext(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartingContext").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String message = Utility.messageBD(this, "CONTEXT_RELOAD", vars.getLanguage());
    xmlDocument.setParameter("message", Utility.formatMessageBDToHtml(message));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    response.flushBuffer();

    ReloadContext.reload();
  }

  /**
   * Restarts the application server after building the application to apply modules.
   * 
   * @param response
   *          the HttpServletResponse to write to
   * @param vars
   *          the application variables
   * @throws IOException
   * @throws ServletException
   */
  private void restartApplicationServer(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartingContext").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String message = Utility.messageBD(this, "TOMCAT_RESTART", vars.getLanguage());
    xmlDocument.setParameter("message", Utility.formatMessageBDToHtml(message));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    response.flushBuffer();

    RestartTomcat.restart();
  }

  /**
   * Prints the default page for the process, showing the process description and a OK and Cancel
   * buttons. First it checks whether the server has write permissions to be able to execute the
   * process.
   */
  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars) throws IOException, ServletException {
    // Check for permissions to apply modules from application server.
    final File f = new File(vars.getSessionValue("#sourcePath"));
    if (!f.canWrite()) {
      bdErrorGeneralPopUp(request, response, Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.messageBD(this, "NoApplicableModules", vars.getLanguage()));
      return;
    }

    String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/ApplyModules").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("buttonLog", fileName);
    xmlDocument.setParameter("logfile", fileName);
    {
      final OBError myMessage = vars.getMessage("ApplyModules");
      vars.removeMessage("ApplyModules");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Method to be called via AJAX. Creates a new AntExecutor object, saves it in session and
   * executes the apply modules task on it.
   */
  private void startApply(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    User currentUser = OBContext.getOBContext().getUser();
    vars.setSessionValue("ApplyModules|Last_Line_Number_Log", "-1");
    boolean admin = OBContext.getOBContext().setInAdministratorMode(true);
    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    PreparedStatement ps3 = null;
    PreparedStatement ps4 = null;
    PreparedStatement updateSession = null;
    AntExecutor ant = null;
    try {
      // We first shutdown the background process, so that it doesn't interfere
      // with the rebuild process
      try {
        if (OBScheduler.getInstance().getScheduler().isStarted())
          OBScheduler.getInstance().getScheduler().shutdown(true);
      } catch (Exception e) {
        // We will not log an exception if the scheduler complains. The user shouldn't notice this
      }
      ps = getPreparedStatement("DELETE FROM AD_ERROR_LOG");
      ps.executeUpdate();
      ps2 = getPreparedStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='RB11'");
      ps2.executeUpdate();

      Properties props = new Properties();
      props.setProperty("log4j.appender.DB", "org.openbravo.utils.OBRebuildAppender");
      props.setProperty("log4j.appender.DB.Basedir", vars.getSessionValue("#sourcePath"));
      props.setProperty("log4j.rootCategory", "INFO,DB");
      PropertyConfigurator.configure(props);

      String sourcePath = vars.getSessionValue("#sourcePath");
      ant = new AntExecutor(sourcePath);

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();

      // do not execute translation process (all entries should be already in the module)
      ant.setProperty("tr", "no");
      // We will show special, friendly warnings when they are available
      ant.setProperty("friendlyWarnings", "true");
      ant.setProperty("logFileName", vars.getStringParameter("logfile"));

      final Vector<String> tasks = new Vector<String>();
      tasks.add("UIrebuild");

      // We also cancel sessions opened for users different from the current one
      updateSession = getPreparedStatement("UPDATE AD_SESSION SET SESSION_ACTIVE='N' WHERE CREATEDBY<>?");
      updateSession.setString(1, currentUser.getId());
      updateSession.executeUpdate();
      ant.runTask(tasks);

      PreparedStatement psErr = getPreparedStatement("SELECT MESSAGE FROM AD_ERROR_LOG WHERE ERROR_LEVEL='ERROR'");
      psErr.executeQuery();
      ResultSet rsErr = psErr.getResultSet();
      if (!rsErr.next()) {
        ps3 = getPreparedStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='RB60'");
        ps3.executeUpdate();
        ps4 = getPreparedStatement("UPDATE AD_MODULE SET STATUS='A' WHERE STATUS='P'");
        ps4.executeUpdate();
      } else {
        ps3 = getPreparedStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='RB59'");
        ps3.executeUpdate();
      }
      out.close();
    } catch (final Exception e) {
      // rolback the old transaction and start a new one
      // to store the build log
      OBDal.getInstance().rollbackAndClose();
      createModuleLog(false, e.getMessage());
      OBDal.getInstance().commitAndClose();
    } finally {
      try {
        Properties props = new Properties();
        props.setProperty("log4j.rootCategory", "INFO,R");
        PropertyConfigurator.configure(props);
        ant.closeLogFile();
        releasePreparedStatement(ps);
        releasePreparedStatement(ps2);
        releasePreparedStatement(ps3);
        releasePreparedStatement(ps4);
        releasePreparedStatement(updateSession);
      } catch (SQLException e) {
      }
      OBContext.getOBContext().setInAdministratorMode(admin);
    }
  }

  /**
   * Creates a new module log entry for a build with the action set to B and no module information
   * set.
   * 
   * @param success
   *          if true then the build was successfull, false if not successfull
   * @param msg
   *          optional additional message
   */
  private static void createModuleLog(boolean success, String msg) {
    final ModuleLog ml = OBProvider.getInstance().get(ModuleLog.class);
    ml.setAction("B");
    if (success) {
      ml.setLog("Build successfull");
    } else {
      final int prefixLength = "Build failed, message: ".length();
      final int maxMsgLength = 2000 - prefixLength;
      if (msg == null) {
        ml.setLog("Build failed");
      } else if (msg.length() > maxMsgLength) {
        ml.setLog("Build failed, message: " + msg.substring(0, maxMsgLength));
      } else {
        ml.setLog("Build failed, message: " + msg);
      }
    }
    OBDal.getInstance().save(ml);
  }

  /**
   * Returns a String of comma separated values for all the modules that are installed but not
   * applied
   * 
   * @return
   * @throws IOException
   * @throws ServletException
   */
  private String getUnnapliedModules() throws IOException, ServletException {
    String rt = "";
    final ApplyModulesData[] data = ApplyModulesData.selectUnappliedModules(this);
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        if (!rt.equals(""))
          rt += ", ";
        rt += data[i].name;
      }
    }
    return rt;
  }
}
