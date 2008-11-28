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
 * All portions are Copyright (C) 2008 Openbravo SL 
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

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.AntExecutor;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
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
public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } if (vars.commandIn("STARTAPPLY")){
      startApply(response,vars);
    } if (vars.commandIn("UPDATELOG")){
      updateLog(response,vars);
    } if (vars.commandIn("GETERR")){
      getError(response,vars);
    } else pageError(response);
  }

  /**
   * Prints the default page for the process, showing the process description and a OK and Cancel buttons.
   * First it checks whether the server has write permissions to be able to execute the process.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    //Check for permissions to apply modules from application server.
    final File f = new File(vars.getSessionValue("#sourcePath"));
    if (!f.canWrite()) {
      bdErrorGeneralPopUp(response, Utility.messageBD(this, "Error", vars.getLanguage()), 
                                    Utility.messageBD(this, "NoApplicableModules", vars.getLanguage()));
      return;
    }
    
    final XmlDocument xmlDocument=xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/ApplyModules").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("help", ApplyModulesData.getHelp(this, vars.getLanguage()));
    {
      final OBError myMessage = vars.getMessage("ApplyModules");
      vars.removeMessage("ApplyModules");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    final PrintWriter out = response.getWriter();
    response.setContentType("text/html; charset=UTF-8");
    out.println(xmlDocument.print());
    out.close();
  }
  
  /**
   * Method to be called via AJAX. Creates a new AntExecutor object, saves it in session and executes the apply modules task on it.
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void startApply(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    //final PrintStream oldOut=System.out;
    try {
      final AntExecutor ant=new AntExecutor(vars.getSessionValue("#sourcePath"));
      String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+"-apply.log";
      //final OBPrintStream obps=new OBPrintStream(new PrintStream(response.getOutputStream()));
      //System.setOut(obps);
      
      //ant.setOBPrintStreamLog(response.getWriter());
      final PrintStream out = new PrintStream(response.getOutputStream());
      ant.setOBPrintStreamLog(new PrintStream(out));
      fileName = ant.setLogFile(fileName);
      //obps.setLogFile(new File(fileName+".db"));
      ant.setLogFileInOBPrintStream(new File(fileName));
      vars.setSessionObject("ApplyModules|Log", ant);
      
      final Vector<String> tasks = new Vector<String>();
      
      final String unnappliedModules = getUnnapliedModules();
      
      if (ApplyModulesData.isUpdatingCore(this)) {
          tasks.add("update.database");
          tasks.add("core.lib");
          tasks.add("wad.lib");
          tasks.add("trl.lib");
          tasks.add("compile.complete.deploy");
          ant.setProperty("apply.on.create", "true");
      } else if (ApplyModulesData.selectUninstalledModules(this)) { //there're uninstalled modules
        tasks.add("update.database");
        tasks.add("generate.entities");
        tasks.add("compile.deploy");
        if (!unnappliedModules.equals("")) { //There are also installed modules, let's compile them
            ant.setProperty("module", unnappliedModules);
        } else {
            ant.setProperty("tab", "xx"); //Only uninstall, compile no window to re-generate web.xml
        }
        ant.setProperty("apply.on.create", "true");
      } else {
        tasks.add("apply.modules");
        ant.setProperty("module", unnappliedModules);
      }
      
      ant.runTask(tasks);
     
      ant.setFinished(true);
      
      
      response.setContentType("text/plain; charset=UTF-8");
      out.println("finished");
      out.close();
    } catch (final Exception e) {e.printStackTrace();}  
    finally{
     // System.setOut(oldOut);
    }
  }
  
  /**
   * Method to be called via AJAX. It is intended to be called periodically to show the log generated after the las call
   *  
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void updateLog(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    try {
      final AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant!=null) ant.setPrintWriter(response.getWriter());
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method to be called via AJAX. It returns a XML structure with the error messages (if any) or a Success one 
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void getError(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: print page errors");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/businessUtility/MessageJS").createXmlDocument();
    String type = "Hidden";
    String title = "";
    String description = "";
    final String strLanguage = vars.getLanguage();
    
    try {
      final AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant!=null) description = ant.getErr();
      if (description.startsWith("SuccessRebuild")) {
        type="Success";
        title= Utility.messageBD(this, type, strLanguage);
        description =  "<![CDATA[" +Utility.messageBD(this, description, strLanguage)+ "]]>";
      } else {
        type="Error";
        title= Utility.messageBD(this, type, strLanguage);
        description= "<![CDATA[" +description+"]]>";
      }
      xmlDocument.setParameter("type", type);
      xmlDocument.setParameter("title", title);
      xmlDocument.setParameter("description", description);
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
   * Returns a String of comma separated values for all the modules that are installed but not applied
   * 
   * @return
   * @throws IOException
   * @throws ServletException
   */
  private String getUnnapliedModules() throws IOException, ServletException{
    String rt = "";
    final ApplyModulesData[] data=ApplyModulesData.selectUnappliedModules(this);
    if (data!=null) {
      for (int i=0; i<data.length; i++) {
        if (!rt.equals("")) rt += ", ";
        rt += data[i].name;
      }
    }
    return rt;
  }
}
