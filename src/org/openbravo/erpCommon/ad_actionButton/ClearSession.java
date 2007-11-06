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

import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class ClearSession extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      printPage(response, vars, strProcessId);
    } else if (vars.commandIn("CLEAR")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      clear(response, vars, strProcessId);
    } else pageErrorPopUp(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strProcessId) throws IOException, ServletException {
    log4j.debug("Output: clear");
//    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ClearSession").createXmlDocument();
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
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ClearSession", discard).createXmlDocument();

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void clear(HttpServletResponse response, VariablesSecureApp vars, String strProcessId) throws IOException, ServletException {
	  OBError myMessage = null;
    log4j.debug("process");

      String strRol = vars.getSessionValue("role");
      String strClient = vars.getSessionValue("client");
      String strOrg = vars.getSessionValue("organization");
      String strWarehouse = vars.getSessionValue("warehouse");


    String user = vars.getSessionValue("#AD_User_ID");
    String role = vars.getSessionValue("#AD_Role_ID");
    String language = vars.getSessionValue("#AD_Language");
    String client = vars.getSessionValue("#AD_Client_ID");
    String organization = vars.getSessionValue("#AD_Org_ID");
    String userClient = vars.getSessionValue("#User_Client");
    String userOrganization = vars.getSessionValue("#User_Org");
    String warehouse = vars.getSessionValue("#M_Warehouse_ID");
    String dbSessionID = vars.getSessionValue("#AD_Session_ID");
   

      vars.clearSession(false);

    vars.setSessionValue("#AD_User_ID",user);
    vars.setSessionValue("#AD_Role_ID",role);
    vars.setSessionValue("#AD_Language",language);
    vars.setSessionValue("#AD_Client_ID",client);
    vars.setSessionValue("#AD_Org_ID",organization);
    vars.setSessionValue("#User_Client",userClient);
    vars.setSessionValue("#User_Org",userOrganization);
    vars.setSessionValue("#M_Warehouse_ID",warehouse);
    vars.setSessionValue("#AD_Session_ID",dbSessionID);

      vars.setSessionValue("role",strRol);
      vars.setSessionValue("client",strClient);
      vars.setSessionValue("strOrg",strOrg);
      vars.setSessionValue("warehouse",strWarehouse);

     if (log4j.isDebugEnabled()) log4j.debug("role:"+role      
                                            + ",client:"+client   
                                            + ",strOrg:"+organization    
                                            + ",warehouse:"+warehouse);





    
//    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ClearSession").createXmlDocument();
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
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ClearSession", discard).createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
   
    myMessage = new OBError();    
    myMessage.setTitle("");
    
    //String strMessage;

    if (!LoginUtils.fillSessionArguments(this, vars, user , vars.getLanguage(), role, client, organization, warehouse)){
    	myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "ProcessFailed", vars.getLanguage()));
    	//strMessage = Utility.messageBD(this, "ProcessFailed", vars.getLanguage());
    }      
    else{
    	myMessage.setType("Success");
        myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
      //strMessage = Utility.messageBD(this, "Success", vars.getLanguage());
    }
    vars.setMessage("ClearSession", myMessage);

    //xmlDocument.setParameter("message",strMessage.equals("")?"":"alert('" + strMessage + "');window.close();");

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    out.println(xmlDocument.print());
    out.close();  
  }
}

