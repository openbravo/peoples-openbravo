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
import java.util.HashMap;

import org.openbravo.erpCommon.utility.ComboTableData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportSalesOrderInvoicedJasper extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")){
      String strdateFrom = vars.getStringParameter("inpDateFrom", "");
      String strdateTo = vars.getStringParameter("inpDateTo", "");
      String strcBpartnerId = vars.getStringParameter("inpcBPartnerId", "");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId", "");
      String strcProjectId = vars.getStringParameter("inpcProjectId", "");
      String strmCategoryId = vars.getStringParameter("inpProductCategory", "");
      String strProjectkind = vars.getStringParameter("inpProjectkind", "");
      String strcRegionId = vars.getStringParameter("inpcRegionId", "");
      String strProjectpublic = vars.getStringParameter("inpProjectpublic", "");
      String strProduct = vars.getStringParameter("inpProductId", "");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId, strProjectpublic, strProduct);
    }else if(vars.commandIn("FIND")){
      String strdateFrom = vars.getStringParameter("inpDateFrom");
      String strdateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnerId = vars.getStringParameter("inpcBPartnerId");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      String strcProjectId = vars.getStringParameter("inpcProjectId");
      String strmCategoryId = vars.getStringParameter("inpProductCategory");
      String strProjectkind = vars.getStringParameter("inpProjectkind");
      String strcRegionId = vars.getStringParameter("inpcRegionId");
      String strProjectpublic = vars.getStringParameter("inpProjectpublic");
      String strProduct = vars.getStringParameter("inpmProductId");
      printPageDataSheetJasper(response, vars, strdateFrom, strdateTo, strcBpartnerId, strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId, strProjectpublic, strProduct);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnerId, String strmWarehouseId, String strcProjectId, String strmCategoryId, String strProjectkind, String strcRegionId, String strProjectpublic, String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"sectionPartner"};
    String strTitle = "";
    XmlDocument xmlDocument=null;
    if (vars.commandIn("DEFAULT")){
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderInvoicedJasper").createXmlDocument();

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesOrderInvoicedJasper", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportSalesOrderInvoicedJasper");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportSalesOrderInvoicedJasper.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesOrderInvoicedJasper.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportSalesOrderInvoicedJasper");
        vars.removeMessage("ReportSalesOrderInvoicedJasper");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      } 

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
      xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
      xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
      xmlDocument.setParameter("cProjectId", strcProjectId);
      xmlDocument.setParameter("mProductCategoryId", strmCategoryId);
      xmlDocument.setParameter("cProjectKind", strProjectkind);
      xmlDocument.setParameter("cRegionId", strcRegionId);
      xmlDocument.setParameter("cProjectPublic", strProjectpublic);
      xmlDocument.setParameter("projectName", ReportProjectBuildingSiteData.selectProject(this, strcProjectId));
      xmlDocument.setParameter("paramBPartnerDescription", ReportSalesOrderInvoicedData.bPartnerDescription(this, strcBpartnerId));
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderInvoicedJasper", strmWarehouseId);
        xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderInvoicedJasper", strmCategoryId);
		xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Projectkind_ID", "Projectkind", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderInvoicedJasper", strProjectkind);
        xmlDocument.setData("reportC_PROJECTKIND","liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID", "", "C_Region of Country", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderInvoicedJasper", strcRegionId);
        xmlDocument.setData("reportC_REGIONID","liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_Public_ID", "PublicPrivate", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderInvoicedJasper", strProjectpublic);
        xmlDocument.setData("reportC_PROJECTPUBLIC","liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      SubCategoryProductData[] dataSub = SubCategoryProductData.select(this, Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"));
      xmlDocument.setParameter("product", arrayDobleEntrada("array", SubCategoryProductData.selectProduct(this, Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoicedJasper"), Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoicedJasper"))));
      
      xmlDocument.setData("structureCategory", dataSub);
      xmlDocument.setData("structureProduct", SubCategoryProductData.selectCategoryProduct(this, dataSub[0].id));
    }
    else {
      ReportSalesOrderInvoicedData[] data = ReportSalesOrderInvoicedData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoiced"), Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoiced"), strdateFrom, DateTimeData.nDaysAfter(this, strdateTo,"1"), strcBpartnerId, strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId, strProjectpublic, strProduct);
      if (data == null || data.length == 0){
        xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderInvoicedPop", discard).createXmlDocument();
        xmlDocument.setData("structure1", ReportSalesOrderInvoicedData.set());
      }
      else {
        xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportSalesOrderInvoicedPop").createXmlDocument();
        xmlDocument.setData("structure1", data);
      }
      if (!strmWarehouseId.equals("")) strTitle +=  " " + Utility.messageBD(this, "ForWarehouse", vars.getLanguage()) + " " + ReportSalesOrderInvoicedData.selectWarehouse(this, strmWarehouseId);
      if (!strcRegionId.equals("")) strTitle += ", " + Utility.messageBD(this, "InRegion", vars.getLanguage()) + " " + ReportSalesOrderInvoicedData.selectRegionId(this, strcRegionId);
      if (!strmCategoryId.equals("")) strTitle += ", " + Utility.messageBD(this, "ForProductCategory", vars.getLanguage()) + " "  + ReportSalesOrderInvoicedData.selectCategoryId(this, strmCategoryId);
      if (!strProjectkind.equals("")) strTitle += ", " + Utility.messageBD(this, "ProjectType", vars.getLanguage()) + " " + ReportSalesOrderInvoicedData.selectProjectkind(this, vars.getLanguage(), strProjectkind);
      if (!strProjectpublic.equals("")) strTitle += ", " + Utility.messageBD(this, "WithInitiativeType", vars.getLanguage()) + " " + ReportSalesOrderInvoicedData.selectProjectpublic(this, vars.getLanguage(), strProjectpublic);
      if (!strdateFrom.equals("")) strTitle +=  ", " + Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom;
      if (!strdateTo.equals("")) strTitle += " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
      if (!strProduct.equals("")) strTitle += ", " + Utility.messageBD(this, "ForProduct", vars.getLanguage()) + " " + ReportSalesOrderInvoicedData.selectProduct(this, strProduct);
      xmlDocument.setParameter("title", strTitle);
    } 
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


//Aquí empieza el que llama al Jasper




  void printPageDataSheetJasper(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnerId, String strmWarehouseId, String strcProjectId, String strmCategoryId, String strProjectkind, String strcRegionId, String strProjectpublic, String strProduct) throws IOException, ServletException {

    ReportSalesOrderInvoicedData[] data = ReportSalesOrderInvoicedData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportSalesOrderInvoiced"), Utility.getContext(this, vars, "#User_Org", "ReportSalesOrderInvoiced"), strdateFrom, DateTimeData.nDaysAfter(this, strdateTo,"1"), strcBpartnerId, strmWarehouseId, strcProjectId, strmCategoryId, strProjectkind, strcRegionId, strProjectpublic, strProduct);

   String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportSalesOrderInvoicedJasper.jrxml";
   String strOutput = "html";
    if (strOutput.equals("pdf")) response.setHeader("Content-disposition", "inline; filename=ReportSalesOrderInvoiced.pdf");

   HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("REPORT_TITLE", classInfo.name);
		parameters.put("REPORT_SUBTITLE", "From "+strdateFrom +" to "+strdateTo);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null ); 

  }

  public String getServletInfo() {
    return "Servlet ReportSalesOrderInvoicedJasper. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}

