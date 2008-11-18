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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.File;
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
import org.openbravo.erpCommon.utility.OBPrintStream;
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
  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

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
    File f = new File(vars.getSessionValue("#sourcePath"));
    if (!f.canWrite()) {
      bdErrorGeneralPopUp(response, Utility.messageBD(this, "Error", vars.getLanguage()), 
                                    Utility.messageBD(this, "NoApplicableModules", vars.getLanguage()));
      return;
    }
    
    XmlDocument xmlDocument=xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/ApplyModules").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("help", ApplyModulesData.getHelp(this, vars.getLanguage()));
    {
      OBError myMessage = vars.getMessage("ApplyModules");
      vars.removeMessage("ApplyModules");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    PrintWriter out = response.getWriter();
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
    PrintStream oldOut=System.out;
    try {
      AntExecutor ant=new AntExecutor(vars.getSessionValue("#sourcePath"));
      String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+"-apply.log";
      OBPrintStream obps=new OBPrintStream(new PrintStream(response.getOutputStream()));
      System.setOut(obps);
      
      //ant.setOBPrintStreamLog(response.getWriter());
      ant.setOBPrintStreamLog(new PrintStream(response.getOutputStream()));
      fileName = ant.setLogFile(fileName);
      obps.setLogFile(new File(fileName+".db"));
      ant.setLogFileInOBPrintStream(new File(fileName));
      vars.setSessionObject("ApplyModules|Log", ant);
      
      Vector<String> tasks = new Vector<String>();
      
      if (ApplyModulesData.selectUninstalledModules(this)) { //there're uninstalled modules
        tasks.add("update.database");
        tasks.add("generate.entities");
        tasks.add("compile");
        tasks.add("war");
        ant.setProperty("module", getUnnapliedModules());
        ant.setProperty("apply.on.create", "true");
      } else {
        tasks.add("apply.modules");
        ant.setProperty("module", getUnnapliedModules());
      }
      
      ant.runTask(tasks);
      ant.setFinished(true);
      
      PrintWriter out = response.getWriter();
      response.setContentType("text/plain; charset=UTF-8");
      out.println("finished");
      out.close();
    } catch (Exception e) {e.printStackTrace();}  
    finally{
      System.setOut(oldOut);
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
      AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant!=null) ant.setPrintWriter(response.getWriter());
    } catch (Exception e) {
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
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/businessUtility/MessageJS").createXmlDocument();
    String type = "Hidden";
    String title = "";
    String description = "";
    String strLanguage = vars.getLanguage();
    
    try {
      AntExecutor ant = (AntExecutor) vars.getSessionObject("ApplyModules|Log");
      if (ant!=null) description = ant.getErr();
      if (description.equals("Success")) {
        type="Success";
        title= Utility.messageBD(this, type, strLanguage);
        description =  "<![CDATA[" +Utility.messageBD(this, type, strLanguage)+ "]]>";
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
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    } catch (Exception e) {
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
    ApplyModulesData[] data=ApplyModulesData.selectUnappliedModules(this);
    if (data!=null) {
      for (int i=0; i<data.length; i++) {
        if (!rt.equals("")) rt += ", ";
        rt += data[i].name;
      }
    }
    return rt;
  }
}
