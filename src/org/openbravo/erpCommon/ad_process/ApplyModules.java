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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.AntExecutor;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
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
    } else if (vars.commandIn("UPDATELOG")) {
      updateLog(response, vars);
    } else if (vars.commandIn("GETERR")) {
      getError(response, vars);
    } else if (vars.commandIn("RESTART")) {
      restartApplicationServer(response, vars);
    } else if (vars.commandIn("RELOAD")) {
      reloadContext(response, vars);
    } else if (vars.commandIn("DONOTHING")) {
      // Won't be called.
    } else {
      pageError(response);
    }
  }

  /**
   * Prints the default page for the process, showing the process description and a OK and Cancel
   * buttons. First it checks whether the server has write permissions to be able to execute the
   * process.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
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

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/ApplyModules").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("help", ApplyModulesData.getHelp(this, vars.getLanguage()));
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
   * Method to be called via AJAX. Creates a new AntExecutor object, saves it in session and
   * executes the apply modules task on it.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void startApply(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    // final PrintStream oldOut=System.out;
    try {
      final AntExecutor ant = new AntExecutor(vars.getSessionValue("#sourcePath"));
      String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-apply.log";
      // final OBPrintStream obps=new OBPrintStream(new
      // PrintStream(response.getOutputStream()));
      // System.setOut(obps);

      // ant.setOBPrintStreamLog(response.getWriter());
      final PrintStream out = new PrintStream(response.getOutputStream());
      ant.setOBPrintStreamLog(new PrintStream(out));

      fileName = ant.setLogFile(fileName);
      // obps.setLogFile(new File(fileName+".db"));
      ant.setLogFileInOBPrintStream(new File(fileName));
      vars.setSessionObject("ApplyModules|Log", ant);

      // do not execute tranlsation process (all entries should be already in the module)
      ant.setProperty("tr", "no");

      final Vector<String> tasks = new Vector<String>();

      final String unnappliedModules = getUnnapliedModules();
      if (ApplyModulesData.isUpdatingCoreOrTemplate(this)) {
        tasks.add("update.database");
        tasks.add("core.lib");
        tasks.add("wad.lib");
        tasks.add("trl.lib");
        tasks.add("compile.complete.deploy");
        ant.setProperty("apply.on.create", "true");
      } else {
        if (ApplyModulesData.compileCompleteNeeded(this)) {
          // compile complete is needed for templates because in this case it is not needed which
          // elements belong to the template and for uninistalling modules in order to remove old
          // files and references
          ant.setProperty("apply.modules.complete.compilation", "true");
        }
        ant.setProperty("force", "true");
        tasks.add("apply.modules");
        ant.setProperty("module", unnappliedModules);
      }
      response.setContentType("text/plain; charset=UTF-8");
      out.print("Shutting down scheduler (background processes) ...");
      // We first shutdown the background process, so that it doesn't interfere
      // with the rebuild process
      OBScheduler.getInstance().getScheduler().shutdown(true);
      out.println(" done.\n");
      ant.runTask(tasks);

      ant.setFinished(true);

      if (ant.hasErrorOccured()) {
        createModuleLog(false, ant.getErr());
      } else {
        createModuleLog(true, null);
      }

      out.println("finished");
      out.close();
    } catch (final Exception e) {
      e.printStackTrace();
      // rolback the old transaction and start a new one
      // to store the build log
      OBDal.getInstance().rollbackAndClose();
      createModuleLog(false, e.getMessage());
      OBDal.getInstance().commitAndClose();
    } finally {
      // System.setOut(oldOut);
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
   * Method to be called via AJAX. It is intended to be called periodically to show the log
   * generated after the las call
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void updateLog(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    try {
      final AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant != null)
        ant.setPrintWriter(response.getWriter());
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method to be called via AJAX. It returns a XML structure with the error messages (if any) or a
   * Success one
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void getError(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page errors");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/MessageJS").createXmlDocument();
    String type = "Hidden";
    String title = "";
    String description = "";
    final String strLanguage = vars.getLanguage();

    try {
      final AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant != null)
        description = ant.getErr();
      if (description.startsWith("SuccessRebuild")) {
        type = "Success";
        title = Utility.messageBD(this, type, strLanguage);
        description = "<![CDATA[" + Utility.messageBD(this, description, strLanguage) + "]]>";
      } else {
        type = "Error";
        title = Utility.messageBD(this, type, strLanguage);
        description = "<![CDATA[" + description + "]]>";
      }
      xmlDocument.setParameter("type", type);
      xmlDocument.setParameter("title", title);
      xmlDocument.setParameter("description", Utility.formatMessageBDToHtml(description));
      response.setContentType("text/xml; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    } catch (final Exception e) {
      description = "";
    }
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
