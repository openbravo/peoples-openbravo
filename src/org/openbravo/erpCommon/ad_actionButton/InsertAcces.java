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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_actionButton;

import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_combos.ModuleComboData;

public class InsertAcces extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    
    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getGlobalVariable("inpadRoleId", strWindow + "|AD_Role_ID");
      String strMessage="";
      printPage(response, vars, strKey, strWindow, strProcessId, strMessage, strTab);
    } else if(vars.commandIn("GENERATE")){
      String strKey = vars.getStringParameter("inpadRoleId");
      String strWindow = vars.getStringParameter("inpwindowId");
      vars.setSessionValue(strWindow + "|AD_Role_ID", strKey);
      String strTab = vars.getStringParameter("inpTabId");
      String strModule = vars.getStringParameter("inpModules");
      String strType = vars.getStringParameter("inpType");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      OBError myMessage = getPrintPage(response, vars, strKey, strModule,strType);
      vars.setMessage(strTab, myMessage);
      //vars.setSessionValue(strWindow + "|" + strTabName + ".message", messageResult);
      printPageClosePopUp(response, vars, strWindowPath);      
    } else pageErrorPopUp(response);
    

  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String windowId, String strProcessId, String strMessage, String strTab)
    throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button insert acces");
      ActionButtonDefaultData[] data = null;
      String strHelp="", strDescription="";
      if (vars.getLanguage().equals("en_US")) data = ActionButtonDefaultData.select(this, strProcessId);
      else data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
      if (data!=null && data.length!=0) {
        strDescription = data[0].description;
        strHelp = data[0].help;
      }
      String[] discard = {""};
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/InsertAcces", discard).createXmlDocument();
      xmlDocument.setParameter("key", strKey);
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setData("reportModules_S","liststructure", ModuleComboData.select(this));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("description", strDescription);
      xmlDocument.setParameter("help", strHelp);
      
      //xmlDocument.setParameter("message",strMessage.equals("")?"":"alert('" + strMessage + "');");

      {
        OBError myMessage = vars.getMessage("InsertAcces");
        vars.removeMessage("InsertAcces");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  OBError getPrintPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strModule, String strType) throws IOException, ServletException {
	  OBError myMessage = null;
	  myMessage = new OBError();
	  myMessage.setTitle("");	  
    try{
    InsertAccesData[] accesData = InsertAccesData.select(this);
    generateAcces(vars, accesData, strKey, strModule,strType);
    myMessage.setType("Success");
    myMessage.setMessage(Utility.messageBD(this, "ProcessOK", vars.getLanguage()));
    return myMessage;
    //return Utility.messageBD(this, "ProcessOK", vars.getLanguage());
    } catch (Exception e) {
      log4j.warn(e);
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
      //return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
  }


  public void generateAcces(VariablesSecureApp vars, InsertAccesData[] accesData, String roleid, String indice, String strType) throws ServletException {
    log4j.error("longitud accesdata: " +accesData.length + " indice: " + indice + " roleid: " + roleid);
    for (int i=0;i<accesData.length;i++) {
      if (accesData[i].parentId.equals(indice)) {
        if (accesData[i].issummary.equals("Y")) generateAcces(vars, accesData, roleid, accesData[i].nodeId, strType);
        else {
          if (accesData[i].action.equals("W") && (InsertAccesData.selectWindow(this, roleid, accesData[i].adwindowid)==null || InsertAccesData.selectWindow(this, roleid, accesData[i].adwindowid).equals("")) && (strType.equals("W") || strType.equals(""))){
              log4j.error("Action: " + accesData[i].action + " window: " + accesData[i].adwindowid);
              InsertAccesData.insertWindow(this, accesData[i].adwindowid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          } else if (accesData[i].action.equals("P") && (InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid)==null || InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid).equals("")) && (strType.equals("P") || strType.equals(""))) {
              log4j.error("Action: " + accesData[i].action + " process: " + accesData[i].adprocessid);
              InsertAccesData.insertProcess(this, accesData[i].adprocessid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          } else if (accesData[i].action.equals("R") && (InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid)==null || InsertAccesData.selectProcess(this, roleid, accesData[i].adprocessid).equals("")) && (strType.equals("R") || strType.equals(""))) {
              log4j.error("Action: " + accesData[i].action + " report: " + accesData[i].adprocessid);
              InsertAccesData.insertProcess(this, accesData[i].adprocessid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          } else if (accesData[i].action.equals("X") && (InsertAccesData.selectForm(this, roleid, accesData[i].adformid)==null || InsertAccesData.selectForm(this, roleid, accesData[i].adformid).equals("")) && (strType.equals("X") || strType.equals(""))) {
              log4j.error("Action: " + accesData[i].action + " form: " + accesData[i].adformid);
              InsertAccesData.insertForm(this, accesData[i].adformid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          } else if (accesData[i].action.equals("F") && (InsertAccesData.selectWorkflow(this, roleid, accesData[i].adworkflowid)==null || InsertAccesData.selectWorkflow(this, roleid, accesData[i].adworkflowid).equals("")) && (strType.equals("F") || strType.equals(""))) {
              log4j.error("Action: " + accesData[i].action + " workflow: " + accesData[i].adworkflowid);
              InsertAccesData.insertWorkflow(this, accesData[i].adworkflowid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          } else if (accesData[i].action.equals("T") && (InsertAccesData.selectTask(this, roleid, accesData[i].adtaskid)==null || InsertAccesData.selectTask(this, roleid, accesData[i].adtaskid)==null) && (strType.equals("T") || strType.equals(""))) {
              log4j.error("Action: " + accesData[i].action + " task: " + accesData[i].adtaskid);
              InsertAccesData.insertTask(this, accesData[i].adtaskid, roleid, vars.getClient(), vars.getOrg(), vars.getUser());
          }
        }
      }
    }
  }

  public String getServletInfo() {
    return "Servlet for the application's roles and permissions generation.";
    // Servlet created by Galder
  } // end of getServletInfo() method
}


