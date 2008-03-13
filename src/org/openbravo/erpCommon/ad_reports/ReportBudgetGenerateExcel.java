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



public class ReportBudgetGenerateExcel extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")){
      printPageDataSheet(response, vars);
    }else if(vars.commandIn("EXCEL")){

      vars.removeSessionValue("ReportBudgetGenerateExcel|inpTabId");
      String strBPartner = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportBudgetGenerateExcel|inpcBPartnerId_IN");
      String strBPGroup = vars.getRequestInGlobalVariable("inpcBPGroupID", "ReportBudgetGenerateExcel|inpcBPGroupID");
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportBudgetGenerateExcel|inpmProductId_IN");
      String strProdCategory = vars.getRequestInGlobalVariable("inpmProductCategoryId", "ReportBudgetGenerateExcel|inpmProductCategoryId");
      //String strUser1 = vars.getRequestInGlobalVariable("inpUser1", "ReportBudgetGenerateExcel|inpUser1");
      //String strUser2 = vars.getRequestInGlobalVariable("inpUser2", "ReportBudgetGenerateExcel|inpUser2");
      String strSalesRegion = vars.getRequestInGlobalVariable("inpcSalesRegionId", "ReportBudgetGenerateExcel|inpcSalesRegionId");
      String strCampaign = vars.getRequestInGlobalVariable("inpcCampaingId", "ReportBudgetGenerateExcel|inpcCampaingId");
      String strActivity = vars.getRequestInGlobalVariable("inpcActivityId", "ReportBudgetGenerateExcel|inpcActivityId");
      String strProject = vars.getRequestInGlobalVariable("inpcProjectId", "ReportBudgetGenerateExcel|inpcProjectId");
      String strTrxOrg = vars.getRequestInGlobalVariable("inpTrxOrg", "ReportBudgetGenerateExcel|inpTrxOrg");
      String strMonth = vars.getRequestInGlobalVariable("inpMonth", "ReportBudgetGenerateExcel|inpMonthId");
      String strValidcombination = vars.getRequestGlobalVariable("inpcValidcombinationId", "ReportBudgetGenerateExcel|inpcValidcombinationId");
      printPageDataExcel(response, vars, strBPartner, strBPGroup, strProduct, strProdCategory, /*strUser1, strUser2,*/ strSalesRegion, strCampaign, strActivity, strProject, strTrxOrg, strMonth, strValidcombination);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars/*, String strdateFrom, String strdateTo, String strcBpartnerId, String strcProjectId, String strmCategoryId, String strProjectkind, String strProjectphase, String strProjectstatus, String strProjectpublic,String strcRegionId, String strSalesRep, String strProduct*/) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBudgetGenerateExcel").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBudgetGenerateExcel", false, "", "", "",false, "ad_reports",  strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());  

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportBudgetGenerateExcel");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportBudgetGenerateExcel.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportBudgetGenerateExcel.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportBudgetGenerateExcel");
      vars.removeMessage("ReportBudgetGenerateExcel");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));

    //    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportBudgetGenerateExcelData.selectBpartner(this));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCBPGroupId","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    //    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportBudgetGenerateExcelData.selectMproduct(this));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportM_PRODUCTCATEGORY","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    /*	try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_ElementValue_ID(this, vars.getLanguage", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
        xmlDocument.setData("reportUser1","liststructure", comboTableData.select(false));
        comboTableData = null;
        } catch (Exception ex) {
        throw new ServletException(ex);
        }*/


    /*	try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_ElementValue_ID(this, vars.getLanguage", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
        xmlDocument.setData("reportUser2","liststructure", comboTableData.select(false));
        comboTableData = null;
        } catch (Exception ex) {
        throw new ServletException(ex);
        }*/


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_SalesRegion_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCSalesRegionId","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Campaign_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCCampaignId","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Activity_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCActivityId","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Project_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCProjectId","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportTrxOrg","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportMonth","liststructure",ReportBudgetGenerateExcelData.selectMonth(this));
    /*try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_ValidCombination_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportBudgetGenerateExcel"), Utility.getContext(this, vars, "#User_Client", "ReportBudgetGenerateExcel"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportBudgetGenerateExcel", "");
      xmlDocument.setData("reportCValidCombinationId","liststructure", comboTableData.select(false));
      comboTableData = null;
      } catch (Exception ex) {
      throw new ServletException(ex);
      }*/
     //added by gro 03/06/2007
        OBError myMessage = vars.getMessage("ReportBudgetGenerateExcel");
        vars.removeMessage("ReportBudgetGenerateExcel");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageDataExcel(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strBPGroup, String strProduct, String strProdCategory, /*String strUser1, String strUser2,*/ String strSalesRegion, String strCampaign, String strActivity,String strProject, String strTrxOrg, String strMonth, String strValidcombination) throws IOException, ServletException {

    if (log4j.isDebugEnabled()) log4j.debug("Output: EXCEL");
    StringBuffer columns= new StringBuffer();
    StringBuffer tables= new StringBuffer();

    if (strBPartner != null && strBPartner != "") {
      columns.append("PARTNER, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_BPARTNER', TO_CHAR(C_BPARTNER_ID), '" ).append( vars.getLanguage()).append("') AS PARTNER, C_BPARTNER_ID FROM C_BPARTNER WHERE C_BPARTNER_ID IN" ).append( strBPartner ).append(")");
    } else columns.append("' ' AS PARTNER, ");
    if (strBPGroup != null && strBPGroup != "") {
      columns.append("PARTNERGROUP, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_BP_GROUP', TO_CHAR(C_BP_GROUP_ID), '" ).append( vars.getLanguage() ).append( "') AS PARTNERGROUP FROM C_BP_GROUP WHERE C_BP_GROUP_ID IN" ).append( strBPGroup ).append( ")");
    } else columns.append("' ' AS PARTNERGROUP, ");
    if (strProduct != null && strProduct != "") {
      columns.append("PRODUCT, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('M_PRODUCT', TO_CHAR(M_PRODUCT_ID), '" ).append( vars.getLanguage() ).append( "') AS PRODUCT, M_PRODUCT_ID FROM M_PRODUCT WHERE M_PRODUCT_ID IN" ).append( strProduct ).append( ")");
    } else columns.append("' ' AS PRODUCT, ");
    if (strProdCategory != null && strProdCategory != "") {
      columns.append("PRODCATEGORY, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('M_PRODUCT_CATEGORY', TO_CHAR(M_PRODUCT_CATEGORY_ID), '" ).append( vars.getLanguage() ).append( "') AS PRODCATEGORY FROM M_PRODUCT_CATEGORY WHERE M_PRODUCT_CATEGORY_ID IN" ).append( strProdCategory ).append( ")");
    } else columns.append("' ' AS PRODCATEGORY, ");
    /*if (strUser1 != null && strUser1 != "") {
      columns.append("USER1, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_ELEMENTVALUE', TO_CHAR(C_ELEMENTVALUE_ID), '" ).append( vars.getLanguage() ).append( "') AS USER1 FROM C_ELEMENTVALUE WHERE C_ELEMENTVALUE_ID IN" ).append( strUser1 ).append( ")");
      } else columns.append("' ' AS USER1, ");
      if (strUser2 != null && strUser2 != "") {
      columns.append("USER2, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_ELEMENTVALUE', TO_CHAR(C_ELEMENTVALUE_ID), '" ).append( vars.getLanguage() ).append( "') AS USER2 FROM C_ELEMENTVALUE WHERE C_ELEMENTVALUE_ID IN" ).append( strUser2 ).append( ")");
      } else columns.append("' ' AS USER2, ");*/
    if (strSalesRegion != null && strSalesRegion != "") {
      columns.append("SALESREGION, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_SALESREGION', TO_CHAR(C_SALESREGION_ID), '" ).append( vars.getLanguage() ).append( "') AS SALESREGION FROM C_SALESREGION WHERE C_SALESREGION_ID IN" ).append( strSalesRegion ).append( ")");
    } else columns.append("' ' AS SALESREGION, ");
    if (strCampaign != null && strCampaign != "") {
      columns.append("CAMPAIGN, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_CAMPAIGN', TO_CHAR(C_CAMPAIGN_ID), '" ).append( vars.getLanguage() ).append( "') AS CAMPAIGN FROM C_CAMPAIGN WHERE C_CAMPAIGN_ID IN" ).append( strCampaign ).append( ")");
    } else columns.append("' ' AS CAMPAIGN, ");
    if (strActivity != null && strActivity != "") {
      columns.append("ACTIVITY, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_ACTIVITY', TO_CHAR(C_ACTIVITY_ID), '" ).append( vars.getLanguage() ).append( "') AS ACTIVITY FROM C_ACTIVITY WHERE C_ACTIVITY_ID IN" ).append( strActivity ).append( ")");
    } else columns.append("' ' AS ACTIVITY, ");
    if (strProject != null && strProject != "") {
      columns.append("PROJECT, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_PROJECT', TO_CHAR(C_PROJECT_ID), '" ).append( vars.getLanguage() ).append( "') AS PROJECT FROM C_PROJECT WHERE C_PROJECT_ID IN" ).append( strProject ).append( ")");
    } else columns.append("' ' AS PROJECT, ");
    if (strTrxOrg != null && strTrxOrg != "") {
      columns.append("TRXORG, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('AD_ORG', TO_CHAR(AD_ORG_ID), '" ).append( vars.getLanguage() ).append( "') AS TRXORG FROM AD_ORG WHERE AD_ORG_ID IN").append( strTrxOrg ).append( ")");
    } else columns.append("' ' AS TRXORG, ");
    if (strMonth != null && strMonth != "") {
      columns.append("MONTH, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('AD_MONTH', TO_CHAR(AD_MONTH_ID), '" ).append( vars.getLanguage() ).append( "') AS MONTH FROM AD_MONTH WHERE  AD_MONTH_ID IN").append( strMonth ).append( ")");
    } else columns.append("' ' AS MONTH, ");
    if (strValidcombination != null && strValidcombination != "") {
      columns.append("VALIDCOMBINATION, ");
      tables.append(", (SELECT AD_COLUMN_IDENTIFIER('C_VALIDCOMBINATION', TO_CHAR(C_VALIDCOMBINATION_ID), '" ).append( vars.getLanguage() ).append( "' ) AS VALIDCOMBINATION FROM C_VALIDCOMBINATION WHERE C_VALIDCOMBINATION_ID = ").append( strValidcombination ).append( ")");
    } else columns.append("' ' AS VALIDCOMBINATION, ");

    //Adds currency to the excel sheet, Euro is default currency
    columns.append(" 'EUR' AS CURRENCY ");

    response.setContentType("application/xls");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument=null;
    ReportBudgetGenerateExcelData[] data=null;
    data = ReportBudgetGenerateExcelData.select(this, columns.toString(), tables.toString());

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBudgetGenerateExcelXLS").createXmlDocument();

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportBudgetGenerateExcel.";
  } // end of getServletInfo() method
}
