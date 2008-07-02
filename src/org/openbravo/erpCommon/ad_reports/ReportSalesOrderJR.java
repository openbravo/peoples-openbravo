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
 * All portions are Copyright (C) 2007-2008 Openbravo SL 
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

import java.util.*;

import org.openbravo.erpCommon.utility.ComboTableData;

import org.openbravo.erpCommon.utility.ToolBar;

public class ReportSalesOrderJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")){
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportSalesOrderJR|dateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportSalesOrderJR|dateTo", "");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId", "ReportSalesOrderJR|projectId", "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId", "ReportSalesOrderJR|warehouseId", "");
      String strProjectpublic = vars.getGlobalVariable("inpProjectpublic", "ReportSalesOrderJR|projectpublic", "");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strmWarehouseId, strcProjectId, strProjectpublic);
    }else if (vars.commandIn("EDIT_HTML","EDIT_PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportSalesOrderJR|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportSalesOrderJR|dateTo");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId", "ReportSalesOrderJR|warehouseId");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId", "ReportSalesOrderJR|projectId");
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic", "ReportSalesOrderJR|projectpublic");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId", "ReportSalesOrderJR|regionId");
      String strmProductCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId", "ReportSalesOrderJR|productCategoryId");
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind", "ReportSalesOrderJR|projectKind");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportSalesOrderJR|bpartnerId");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportSalesOrderJR|productId");
      printPageHtml(response, vars, strdateFrom, strdateTo, strmWarehouseId, strcProjectId, strProjectpublic, strcRegionId, strmProductCategoryId, strProjectkind, strcBpartnerId, strmProductId);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmWarehouseId, String strcProjectId, String strProjectpublic) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderFilterJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderJR", false, "", "", "openServletNewWindow('EDIT_PDF', true, 'ReportSalesOrderJR.pdf', 'ReportSalesOrderFilterJR', null, false, '700', '1000', true);return false;",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportSalesOrderJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportSalesOrderFilterJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesOrderJR");
      vars.removeMessage("ReportSalesOrderJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }  


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("paramBPartnerId", "");
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("cProjectId", strcProjectId);
    xmlDocument.setParameter("projectName", OrderEditionData.selectProject(this, strcProjectId));
    xmlDocument.setParameter("cProjectKind", "");
    xmlDocument.setParameter("cRegionId", "");
    xmlDocument.setParameter("cProjectPublic", strProjectpublic);
    xmlDocument.setParameter("mProductCatId", "");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client", "SalesOrderFilter"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR", "");
      xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Projectkind_ID", "Projectkind", "", Utility.getContext(this, vars, "#User_Org", "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR", "");
      xmlDocument.setData("reportC_PROJECTKIND","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID", "", "C_Region of Country", Utility.getContext(this, vars, "#User_Org", "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilterJR", "");
      xmlDocument.setData("reportC_REGIONID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Projectkind_ID", "PublicPrivate", "", Utility.getContext(this, vars, "#User_Org", "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "SalesOrderFilter", "");
      xmlDocument.setData("reportC_PROJECTKIND","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportProjectBuildingSiteData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), ""));
    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportProjectBuildingSiteData.selectMproduct(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), ""));
    xmlDocument.setData("reportC_PRODUCTCATREGORY","liststructure",SubCategoryProductData.select(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", "")));
    response.setContentType("text/html; charset=UTF-8");
    
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    
    
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmWarehouseId, String strcProjectId, String strProjectpublic, String strcRegionId, String strmProductCategoryId, String strProjectkind, String strcBpartnerId, String strmProductId) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
    OrderEditionData[] data = null;
    data = OrderEditionData.select(this, Utility.getContext(this, vars, "#User_Org", "SalesOrderFilterJR"), Utility.getContext(this, vars, "#User_Client", "SalesOrderFilterJR"), strdateFrom, strdateTo, strmWarehouseId, strcProjectId, strProjectpublic, strcRegionId, strmProductCategoryId, strProjectkind, strcBpartnerId, strmProductId);
    if (data == null || data.length == 0) data = OrderEditionData.set();
  
    String strOutput = vars.commandIn("EDIT_HTML")?"html":"pdf";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportSalesOrderJR.jrxml";
    
    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " "+strdateFrom+" " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strdateTo;
  
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_TITLE", classInfo.name);
    parameters.put("REPORT_SUBTITLE", strSubTitle);
    
    renderJR(vars, response, strReportName, strOutput, parameters, data, null );
  }
   



  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}

