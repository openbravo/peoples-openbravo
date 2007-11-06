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

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportInvoices extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasProcessAccess(this, vars, "", "RV_C_InvoiceLine")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      String strC_BPartner_ID = vars.getGlobalVariable("inpcBpartnerId", "ReportInvoices|C_BPartner_ID", "");
      String strM_Product_ID = vars.getGlobalVariable("inpmProductId", "ReportInvoices|M_Product_ID", "");
      String strDateFrom = vars.getGlobalVariable("inpDateInvoiceFrom", "ReportInvoices|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateInvoiceTo", "ReportInvoices|DateTo", "");
      String strDocumentNo = vars.getGlobalVariable("inpInvoicedocumentno", "ReportInvoices|DocumentNo", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "ReportInvoices|Order", "SalesOrder");
      String strC_BpGroup_ID = vars.getGlobalVariable("inpcBpGroupId", "ReportInvoices|C_BpGroup_ID", "");
      String strM_Product_Category_ID = vars.getGlobalVariable("inpmProductCategoryId", "ReportInvoices|M_Product_Category_ID", "");
      printPageDataSheet(response, vars, strC_BPartner_ID, strM_Product_ID, strDateFrom, strDateTo, strDocumentNo, strOrder, strC_BpGroup_ID, strM_Product_Category_ID);
    } else if (vars.commandIn("FIND")) {
      String strC_BPartner_ID = vars.getRequestGlobalVariable("inpcBpartnerId", "ReportInvoices|C_BPartner_ID");
      String strM_Product_ID = vars.getRequestGlobalVariable("inpmProductId", "ReportInvoices|M_Product_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateInvoiceFrom", "ReportInvoices|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateInvoiceTo", "ReportInvoices|DateTo");
      String strDocumentNo = vars.getRequestGlobalVariable("inpInvoicedocumentno", "ReportInvoices|DocumentNo");
      String strOrder = vars.getGlobalVariable("inpOrder", "ReportInvoices|Order");
      String strC_BpGroup_ID = vars.getRequestGlobalVariable("inpcBpGroupId", "ReportInvoices|C_BpGroup_ID");
      String strM_Product_Category_ID = vars.getRequestGlobalVariable("inpmProductCategoryId", "ReportInvoices|M_Product_Category_ID");
      printPageDataHtml(response, vars, strC_BPartner_ID, strM_Product_ID, strDateFrom, strDateTo, strDocumentNo, strOrder, strC_BpGroup_ID, strM_Product_Category_ID);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strC_BPartner_ID, String strM_Product_ID, String strDateFrom, String strDateTo, String strDocumentNo, String strOrder, String strC_BpGroup_ID, String strM_Product_Category_ID)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"sectionBPartner"};
    XmlDocument xmlDocument=null;
    ReportInvoicesData[] data=null;
    if (strC_BPartner_ID.equals("") && strM_Product_ID.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDocumentNo.equals("") && strC_BpGroup_ID.equals("") && strM_Product_Category_ID.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoicesEdit", discard).createXmlDocument();
      data = ReportInvoicesData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoicesEdit").createXmlDocument();
      data = ReportInvoicesData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), strC_BpGroup_ID, strM_Product_Category_ID, strC_BPartner_ID, strM_Product_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strDocumentNo, (strOrder.equals("PurchaseOrder"))?"":"sales", (strOrder.equals("PurchaseOrder"))?"purchase":"");
    }

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strC_BPartner_ID, String strM_Product_ID, String strDateFrom, String strDateTo, String strDocumentNo, String strOrder, String strC_BpGroup_ID, String strM_Product_Category_ID)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
   
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoices").createXmlDocument();
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoices", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      KeyMap key = new KeyMap(this, vars, "ReportInvoices.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportInvoices");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInvoices.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoices.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoices");
      vars.removeMessage("ReportInvoices");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("paramMProductId", strM_Product_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramDocumentNo", strDocumentNo);
    xmlDocument.setParameter("paramCBpGroupID", strC_BpGroup_ID);
    xmlDocument.setParameter("paramMProductCategoryID", strM_Product_Category_ID);
    xmlDocument.setParameter("sales", strOrder);
    xmlDocument.setParameter("purchase", strOrder);
    xmlDocument.setParameter("paramBPartnerDescription", ReportInvoicesData.bPartnerDescription(this, strC_BPartner_ID));
    xmlDocument.setParameter("paramMProductIDDES", ReportInvoicesData.mProductDescription(this, strM_Product_ID));


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strC_BpGroup_ID);
      xmlDocument.setData("reportC_Bp_Group","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }



    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strM_Product_Category_ID);
      xmlDocument.setData("reportM_Product_Category","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    
    out.println(xmlDocument.print());
    out.close();
  }
  
 /* void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strC_BPartner_ID, String strM_Product_ID, String strDateFrom, String strDateTo, String strDocumentNo, String strOrder, String strC_BpGroup_ID, String strM_Product_Category_ID)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"sectionBPartner"};
    XmlDocument xmlDocument=null;
    ReportInvoicesData[] data=null;
    if (strC_BPartner_ID.equals("") && strM_Product_ID.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDocumentNo.equals("") && strC_BpGroup_ID.equals("") && strM_Product_Category_ID.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoices", discard).createXmlDocument();
      data = ReportInvoicesData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoices").createXmlDocument();
      data = ReportInvoicesData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), strC_BpGroup_ID, strM_Product_Category_ID, strC_BPartner_ID, strM_Product_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strDocumentNo, (strOrder.equals("PurchaseOrder"))?"":"sales", (strOrder.equals("PurchaseOrder"))?"purchase":"");
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoices", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      KeyMap key = new KeyMap(this, vars, "ReportInvoices.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportInvoices");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInvoices.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoices.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoices");
      vars.removeMessage("ReportInvoices");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("paramMProductId", strM_Product_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("paramDocumentNo", strDocumentNo);
    xmlDocument.setParameter("paramCBpGroupID", strC_BpGroup_ID);
    xmlDocument.setParameter("paramMProductCategoryID", strM_Product_Category_ID);
    xmlDocument.setParameter("sales", strOrder);
    xmlDocument.setParameter("purchase", strOrder);
    xmlDocument.setParameter("paramBPartnerDescription", ReportInvoicesData.bPartnerDescription(this, strC_BPartner_ID));
    xmlDocument.setParameter("paramMProductIDDES", ReportInvoicesData.mProductDescription(this, strM_Product_ID));


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strC_BpGroup_ID);
      xmlDocument.setData("reportC_Bp_Group","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }



    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportInvoices"), Utility.getContext(this, vars, "#User_Client", "ReportInvoices"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoices", strM_Product_Category_ID);
      xmlDocument.setData("reportM_Product_Category","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet ReportInvoices. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}

