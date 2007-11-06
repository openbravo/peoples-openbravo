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
package org.openbravo.erpCommon.ad_reports;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ReportSalesOrderOpenItem extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasProcessAccess(this, vars, "", "RV_ReportSalesOrderOpenItem")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "ReportSalesOrderOpenItem|Warehouse", "");
      printPageDataSheet(response, vars, strWarehouse);
    } else if (vars.commandIn("FIND")) {
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "ReportSalesOrderOpenItem|Warehouse");
      printPageDataHtml(response, vars, strWarehouse);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strWarehouse)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"discard"};
    XmlDocument xmlDocument;
    ReportSalesOrderOpenItemData[] data=ReportSalesOrderOpenItemData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItem"), Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderOpenItem"), strWarehouse);
    if (data == null || data.length == 0){
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItemEdit", discard).createXmlDocument();
      data = ReportSalesOrderOpenItemData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItemEdit").createXmlDocument();
    }

    
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }
  
   void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWarehouse)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument;

    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItem").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportSalesOrderOpenItem.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportSalesOrderOpenItem");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesOrderOpenItem");
      vars.removeMessage("ReportSalesOrderOpenItem");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderOpenItem"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItem"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderOpenItem", strWarehouse);
      xmlDocument.setData("reportM_WAREHOUSESHIPPER","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    
    out.println(xmlDocument.print());
    out.close();
  }
  
 /*  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strWarehouse)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"discard"};
    XmlDocument xmlDocument;
    ReportSalesOrderOpenItemData[] data=ReportSalesOrderOpenItemData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItem"), Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderOpenItem"), strWarehouse);
    if (data == null || data.length == 0){
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItem", discard).createXmlDocument();
      data = ReportSalesOrderOpenItemData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderOpenItem").createXmlDocument();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportSalesOrderOpenItem.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportSalesOrderOpenItem");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderOpenItem.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesOrderOpenItem");
      vars.removeMessage("ReportSalesOrderOpenItem");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderOpenItem"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderOpenItem"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderOpenItem", strWarehouse);
      xmlDocument.setData("reportM_WAREHOUSESHIPPER","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet ReportSalesOrderOpenItem. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}

