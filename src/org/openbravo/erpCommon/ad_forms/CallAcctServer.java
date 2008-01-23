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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.*;
import org.openbravo.erpCommon.ad_background.*;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class CallAcctServer extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String adProcessId = CallAcctServerData.processID(this);
    PeriodicBackground acctServer = getBackgroundProcess(adProcessId);
    if (vars.commandIn("DEFAULT")) {
      String strTableId = vars.getStringParameter("inpadTableId", "");
      printPage(response,vars, strTableId, "");
    } else if (vars.commandIn("CANCELAR")) {
      String strTableId = vars.getStringParameter("inpadTableId", "");
      acctServer.cancelDirectProcess();
      printPage(response,vars, strTableId, "");
    } else if (vars.commandIn("REFRESH_INFO")) {
      String strMessage = "";
      if (!acctServer.getOut().equals("")) {
        strMessage = acctServer.getOut();
        acctServer.clearLastLog();
      } else if (!acctServer.isProcessing()) {
        strMessage = "ENDOFPROCESS";
      }
      printPageAjax(response,vars, strMessage);
    } else if (vars.commandIn("RUN")) {
      String strTableId = vars.getStringParameter("inpadTableId");
      if (log4j.isDebugEnabled()) log4j.debug(strTableId);
      runProcess(response, vars, strTableId, acctServer, adProcessId);
    } else pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTableId, String strMessage) throws IOException, ServletException{
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/CallAcctServer").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CallAcctServer", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

	try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.CallAcctServer");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CallAcctServer.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CallAcctServer.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CallAcctServer");
      vars.removeMessage("CallAcctServer");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("body", (strMessage.equals("")?"":"alert('" + strMessage + "');"));


    
    AcctServerData[] data = AcctServerData.selectTables(this,  vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", ""));
    if (log4j.isDebugEnabled()) log4j.debug("select tables org:"+Utility.getContext(this, vars, "#User_Org", "")+", Client:"+Utility.getContext(this, vars, "#User_Client", "")+",lang:"+vars.getLanguage()); 
    if (log4j.isDebugEnabled()) log4j.debug("lenght:"+data.length);
    xmlDocument.setData("reportadTableId", "liststructure", data);

    xmlDocument.setParameter("adTableId", strTableId);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
	}
  }
  
  private void runProcess(HttpServletResponse response, VariablesSecureApp vars, String strTableId, PeriodicBackground acctServer, String adProcessId) throws IOException, ServletException {
	  OBError myMessage = new OBError();
	  myMessage.setTitle("");
	  try {
		  String adPinstanceId = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
		  PInstanceProcessData.insertPInstance(this, adPinstanceId, adProcessId, "0", "N", vars.getUser(), vars.getClient(), vars.getOrg());
		  PInstanceProcessData.insertPInstanceParamNumber(this, adPinstanceId, "10", "AD_Table_ID", strTableId, vars.getClient(), vars.getOrg(), vars.getUser());
		  if (acctServer.directLaunch(vars, adPinstanceId)) {
		      while(acctServer.isDirectProcess() && !acctServer.isProcessing()){ 
		    	  if (log4j.isDebugEnabled()) log4j.debug("Waiting for the thread to begin");
		      }		      
		      myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), acctServer.getOut()));
		      myMessage.setType("Info");
		  }
		  else {
			  myMessage.setMessage(Utility.messageBD(this, "BP_RUNNING", vars.getLanguage()));
			  myMessage.setType("Error");
		  }
	  }
	  catch(Exception e) {
		  log4j.error(e.getMessage());
	  }
	  finally {
		  vars.setMessage("CallAcctServer", myMessage);
		  printPage(response,vars, strTableId, "");
	  }
  }

  public void printPageAjax(HttpServletResponse response, VariablesSecureApp vars, String strMessage) throws IOException, ServletException {
    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    out.println(strMessage);
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that calls the contabilization process";
  } // end of getServletInfo() method
}
