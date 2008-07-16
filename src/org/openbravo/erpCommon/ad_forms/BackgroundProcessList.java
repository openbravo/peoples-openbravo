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
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_background.*;


public class BackgroundProcessList extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("START")) {
      String adProcessId = vars.getStringParameter("inpKey");
      PeriodicBackground bg = getBackgroundProcess(adProcessId);
      bg.setActive(true);
      bg = null;
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("STOP")) {
      String adProcessId = vars.getStringParameter("inpKey");
      PeriodicBackground bg = getBackgroundProcess(adProcessId);
      bg.setActive(false);
      bg = null;
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("LOG")) {
      String adProcessId = vars.getStringParameter("inpKey");
      printPageLog(response, vars, adProcessId);
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/BackgroundProcessList").createXmlDocument();
    BackgroundProcessListData[] data = null;
    if (vars.getLanguage().equals("en_US")) {
      data = BackgroundProcessListData.select(this);
    } else {
      data = BackgroundProcessListData.selectLanguage(this, vars.getLanguage());
    }
    if (data!=null && data.length>0) {
      String strActivate = Utility.messageBD(this, "Activate", vars.getLanguage());
      String strDeactivate = Utility.messageBD(this, "Deactivate", vars.getLanguage());
      for (int i=0;i<data.length;i++) {
        PeriodicBackground bg = getBackgroundProcess(data[i].id);
        if (bg!=null) {
          data[i].status = (bg.isActive()?strDeactivate:strActivate);
          data[i].commandName = (bg.isActive()?"STOP":"START");
          data[i].isprocessing = (bg.isProcessing()?Utility.messageBD(this, "Y", vars.getLanguage()):Utility.messageBD(this, "N", vars.getLanguage()));
          bg = null;
        }
      }
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "BackgroundProcessList", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
	try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.BackgroundProcessList");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "BackgroundProcessList.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "BackgroundProcessList.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("BackgroundProcessList");
      vars.removeMessage("BackgroundProcessList");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    
    data = removePeriodicHeartbeat(data); // Remove PeriodicHeartbeat
    

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageLog(HttpServletResponse response, VariablesSecureApp vars, String adProcessId) throws IOException, ServletException {
    response.setContentType("text/html; charset=UTF-8");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Log_POP").createXmlDocument();
    xmlDocument.setParameter("log", getBackgroundProcess(adProcessId).getLog());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  

  /**
   * Removes PeriodicHeartbeat from the list of Background Processes.
   * 
   * Issue 0004325 - PeriodicHeartbeat can not be disabled through BackgroundProcess window
   * so it needs to be removed. 
   * 
   * @param original
   * @return
   */
  private BackgroundProcessListData[] removePeriodicHeartbeat(BackgroundProcessListData[] original) {
    List<BackgroundProcessListData> list = new ArrayList<BackgroundProcessListData>();
    if (original!=null && original.length>0) {
      for (BackgroundProcessListData bpld : original) {
        if (bpld.getField("description") != null && 
            !bpld.getField("description").equals("PeriodicHeartbeat")) {
          list.add(bpld);
        }
      }
    }
    return list.toArray(new BackgroundProcessListData[list.size()]);
  }

  public String getServletInfo() {
    return "Servlet for the background processes control";
  } // end of getServletInfo() method
}
