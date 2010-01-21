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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportInvoiceCustomerEdition extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportInvoiceCustomerEdition|DateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportInvoiceCustomerEdition|DateTo",
          "");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId",
          "ReportInvoiceCustomerEdition|cProjectId", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportInvoiceCustomerEdition|cBPartnerId_IN", "", IsIDFilter.instance);
      String strmCategoryId = vars.getInGlobalVariable("inpmProductCategoryId",
          "ReportInvoiceCustomerEdition|mCategoryId", "", IsIDFilter.instance);
      String strProjectkind = vars.getInGlobalVariable("inpProjectkind",
          "ReportInvoiceCustomerEdition|Projectkind", "", IsIDFilter.instance);
      String strProjectstatus = vars.getInGlobalVariable("inpProjectstatus",
          "ReportInvoiceCustomerEdition|Projectstatus", "", IsIDFilter.instance);
      String strProjectphase = vars.getInGlobalVariable("inpProjectphase",
          "ReportInvoiceCustomerEdition|Projectphase", "", IsIDFilter.instance);
      String strProduct = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportInvoiceCustomerEdition|mProductId_IN", "", IsIDFilter.instance);
      String strProjectpublic = vars.getGlobalVariable("inpProjectpublic",
          "ReportInvoiceCustomerEdition|Projectpublic", "");
      String strSalesRep = vars.getGlobalVariable("inpSalesRepId",
          "ReportInvoiceCustomerEdition|SalesRepId", "");
      String strcRegionId = vars.getInGlobalVariable("inpcRegionId",
          "ReportInvoiceCustomerEdition|cRegionId", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcProjectId, strcBpartnerId,
          strmCategoryId, strProjectkind, strProjectstatus, strProjectphase, strProduct,
          strProjectpublic, strSalesRep, strcRegionId);
    } else if (vars.commandIn("EDIT_HTML")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportInvoiceCustomerEdition|DateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportInvoiceCustomerEdition|DateTo");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportInvoiceCustomerEdition|cProjectId");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportInvoiceCustomerEdition|cBPartnerId_IN", IsIDFilter.instance);
      String strmCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportInvoiceCustomerEdition|mCategoryId", IsIDFilter.instance);
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind",
          "ReportInvoiceCustomerEdition|Projectkind", IsIDFilter.instance);
      String strProjectstatus = vars.getRequestInGlobalVariable("inpProjectstatus",
          "ReportInvoiceCustomerEdition|Projectstatus", IsIDFilter.instance);
      String strProjectphase = vars.getRequestInGlobalVariable("inpProjectphase",
          "ReportInvoiceCustomerEdition|Projectphase", IsIDFilter.instance);
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportInvoiceCustomerEdition|mProductId_IN", IsIDFilter.instance);
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic",
          "ReportInvoiceCustomerEdition|Projectpublic");
      String strSalesRep = vars.getRequestGlobalVariable("inpSalesRepId",
          "ReportInvoiceCustomerEdition|SalesRepId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportInvoiceCustomerEdition|cRegionId", IsIDFilter.instance);
      printPageHtml(response, vars, strdateFrom, strdateTo, strcProjectId, strcBpartnerId,
          strmCategoryId, strProjectkind, strProjectstatus, strProjectphase, strProduct,
          strProjectpublic, strSalesRep, strcRegionId);
    } else if (vars.commandIn("EDIT_PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportInvoiceCustomerEdition|DateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportInvoiceCustomerEdition|DateTo");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportInvoiceCustomerEdition|cProjectId");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportInvoiceCustomerEdition|cBPartnerId_IN", IsIDFilter.instance);
      String strmCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportInvoiceCustomerEdition|mCategoryId", IsIDFilter.instance);
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind",
          "ReportInvoiceCustomerEdition|Projectkind", IsIDFilter.instance);
      String strProjectstatus = vars.getRequestInGlobalVariable("inpProjectstatus",
          "ReportInvoiceCustomerEdition|Projectstatus", IsIDFilter.instance);
      String strProjectphase = vars.getRequestInGlobalVariable("inpProjectphase",
          "ReportInvoiceCustomerEdition|Projectphase", IsIDFilter.instance);
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportInvoiceCustomerEdition|mProductId_IN", IsIDFilter.instance);
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic",
          "ReportInvoiceCustomerEdition|Projectpublic");
      String strSalesRep = vars.getRequestGlobalVariable("inpSalesRepId",
          "ReportInvoiceCustomerEdition|SalesRepId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportInvoiceCustomerEdition|cRegionId", IsIDFilter.instance);
      printPagePdf(request, response, vars, strdateFrom, strdateTo, strcProjectId, strcBpartnerId,
          strmCategoryId, strProjectkind, strProjectstatus, strProjectphase, strProduct,
          strProjectpublic, strSalesRep, strcRegionId);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcProjectId, String strcBpartnerId,
      String strmCategoryId, String strProjectkind, String strProjectstatus,
      String strProjectphase, String strProduct, String strProjectpublic, String strSalesRep,
      String strcRegionId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInvoiceCustomerFilter").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoiceCustomerFilter", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportInvoiceCustomerEdition");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportInvoiceCustomerEdition.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportInvoiceCustomerEdition.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoiceCustomerEdition");
      vars.removeMessage("ReportInvoiceCustomerEdition");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("cProjectId", strcProjectId);
    xmlDocument.setParameter("projectName", InvoiceCustomerEditionData.selectProject(this,
        strcProjectId));
    xmlDocument.setParameter("mProductCatId", strmCategoryId);
    xmlDocument.setParameter("cProjectKind", strProjectkind);
    xmlDocument.setParameter("cRegionId", strcRegionId);
    xmlDocument.setParameter("cProjectPhase", strProjectphase);
    xmlDocument.setParameter("cProjectStatus", strProjectstatus);
    xmlDocument.setParameter("cProjectPublic", strProjectpublic);
    xmlDocument.setParameter("salesRep", strSalesRep);

    try {
      ComboTableData comboTableData = new ComboTableData(this, "LIST", "", "Projectkind", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceCustomerEdition"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strProjectkind);
      xmlDocument.setData("reportC_PROJECTKIND", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "LIST", "", "Projectphase", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceCustomerEdition"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strProjectphase);
      xmlDocument.setData("reportC_PROJECTPHASE", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "LIST", "", "ProjectStatus", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceCustomerEdition"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strProjectstatus);
      xmlDocument.setData("reportC_PROJECTSTATUS", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "LIST", "", "PublicPrivate", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInvoiceCustomerEdition"),
          Utility.getContext(this, vars, "#User_Client", "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strProjectpublic);
      xmlDocument.setData("reportC_PROJECTPUBLIC", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "M_PRODUCT_CATEGORY_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportInvoiceCustomerEdition"), Utility.getContext(this, vars, "#User_Client",
              "ReportInvoiceCustomerEdition"), 0);
      comboTableData.fillParameters(null, "ReportInvoiceCustomerEdition", "");
      xmlDocument
          .setData("reportC_PRODUCTCATREGORY", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "TABLEDIR", "C_REGION_ID", "",
          "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportInvoiceCustomerEdition"), Utility.getContext(this, vars, "#User_Client",
              "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strcRegionId);
      xmlDocument.setData("reportC_REGIONID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "TABLE", "", "190",
          "AD_User SalesRep", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportInvoiceCustomerEdition"), Utility.getContext(this, vars, "#User_Client",
              "ReportInvoiceCustomerEdition"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInvoiceCustomerEdition",
          strSalesRep);
      xmlDocument.setData("reportSALESREP", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", SelectorUtilityData
        .selectBpartner(this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility
            .getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("reportMProductId_IN", "liststructure", SelectorUtilityData.selectMproduct(
        this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this,
            vars, "#User_Client", ""), strProduct));

    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcProjectId, String strcBpartnerId,
      String strmCategoryId, String strProjectkind, String strProjectstatus,
      String strProjectphase, String strProduct, String strProjectpublic, String strSalesRep,
      String strcRegionId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print html");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInvoiceCustomerEdition").createXmlDocument();
    InvoiceCustomerEditionData[] data = null;
    // Get user Client's base currency
    String strCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    data = InvoiceCustomerEditionData.select(this, strCurrencyId, Utility.getContext(this, vars,
        "#AccessibleOrgTree", "InvoiceCustomerFilter"), Utility.getContext(this, vars,
        "#User_Client", "InvoiceCustomerFilter"), strdateFrom, strdateTo, strcBpartnerId,
        strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
        strProjectpublic, strcRegionId, strSalesRep, strProduct);
    xmlDocument.setData("structure1", data);

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePdf(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcProjectId,
      String strcBpartnerId, String strmCategoryId, String strProjectkind, String strProjectstatus,
      String strProjectphase, String strProduct, String strProjectpublic, String strSalesRep,
      String strcRegionId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print pdf");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInvoiceCustomerEdition_Pdf").createXmlDocument();
    InvoiceCustomerEditionData[] data = null;
    // Get user Client's base currency
    String strCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    data = InvoiceCustomerEditionData.select(this, strCurrencyId, Utility.getContext(this, vars,
        "#AccessibleOrgTree", "InvoiceCustomerFilter"), Utility.getContext(this, vars,
        "#User_Client", "InvoiceCustomerFilter"), strdateFrom, strdateTo, strcBpartnerId,
        strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
        strProjectpublic, strcRegionId, strSalesRep, strProduct);
    xmlDocument.setData("structure1", data);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, request, response);
  }

  public String getServletInfo() {
    return "Servlet InvoiceCustomerFilter. This Servlet was made by Jon Alegría";
  } // end of the getServletInfo() method
}
