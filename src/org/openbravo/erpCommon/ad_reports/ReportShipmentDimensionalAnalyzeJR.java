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
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.Replace;
import org.openbravo.erpCommon.ad_combos.OrganizationComboData;


import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportShipmentDimensionalAnalyzeJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")){
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportShipmentDimensionalAnalyzeJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportShipmentDimensionalAnalyzeJR|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef", "ReportShipmentDimensionalAnalyzeJR|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef", "ReportShipmentDimensionalAnalyzeJR|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup", "ReportShipmentDimensionalAnalyzeJR|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN", "ReportShipmentDimensionalAnalyzeJR|partner", "");
      String strProductCategory = vars.getGlobalVariable("inpProductCategory", "ReportShipmentDimensionalAnalyzeJR|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN", "ReportShipmentDimensionalAnalyzeJR|product", "");
      String strNotShown = vars.getInGlobalVariable("inpNotShown", "ReportShipmentDimensionalAnalyzeJR|notShown", "");
      String strShown = vars.getInGlobalVariable("inpShown", "ReportShipmentDimensionalAnalyzeJR|shown", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportShipmentDimensionalAnalyzeJR|org", "0");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId", "ReportShipmentDimensionalAnalyzeJR|warehouse", "");
      String strsalesrepId = vars.getGlobalVariable("inpSalesrepId", "ReportShipmentDimensionalAnalyzeJR|salesrep", "");
      String strOrder = vars.getGlobalVariable("inpOrder","ReportShipmentDimensionalAnalyzeJR|order","Normal");
      String strMayor = vars.getGlobalVariable("inpMayor", "ReportShipmentDimensionalAnalyzeJR|mayor", "");
      String strMenor = vars.getGlobalVariable("inpMenor", "ReportShipmentDimensionalAnalyzeJR|menor", "");
      String strPartnerSalesRepId = vars.getGlobalVariable("inpPartnerSalesrepId", "ReportShipmentDimensionalAnalyzeJR|partnersalesrep", "");
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE")) strComparative = vars.getRequestGlobalVariable("inpComparative", "ReportShipmentDimensionalAnalyzeJR|comparative");
      else strComparative = vars.getGlobalVariable("inpComparative", "ReportShipmentDimensionalAnalyzeJR|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder, strMayor, strMenor, strPartnerSalesRepId);
    }else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportShipmentDimensionalAnalyzeJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportShipmentDimensionalAnalyzeJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "ReportShipmentDimensionalAnalyzeJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "ReportShipmentDimensionalAnalyzeJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup", "ReportShipmentDimensionalAnalyzeJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportShipmentDimensionalAnalyzeJR|partner");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportShipmentDimensionalAnalyzeJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportShipmentDimensionalAnalyzeJR|product");
      String strNotShown = vars.getInStringParameter("inpNotShown");
      String strShown = vars.getInStringParameter("inpShown");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId", "ReportShipmentDimensionalAnalyzeJR|warehouse");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportShipmentDimensionalAnalyzeJR|org", "0");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId", "ReportShipmentDimensionalAnalyzeJR|salesrep");
      String strOrder = vars.getRequestGlobalVariable("inpOrder","ReportShipmentDimensionalAnalyzeJR|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strPartnerSalesrepId = vars.getRequestGlobalVariable("inpPartnerSalesrepId", "ReportShipmentDimensionalAnalyzeJR|partnersalesrep");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      printPageHtml(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder, strMayor, strMenor, strPartnerSalesrepId, "html");
    }else if (vars.commandIn("EDIT_PDF", "EDIT_PDF_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportShipmentDimensionalAnalyzeJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportShipmentDimensionalAnalyzeJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "ReportShipmentDimensionalAnalyzeJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "ReportShipmentDimensionalAnalyzeJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup", "ReportShipmentDimensionalAnalyzeJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportShipmentDimensionalAnalyzeJR|partner");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportShipmentDimensionalAnalyzeJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportShipmentDimensionalAnalyzeJR|product");
      String strNotShown = vars.getInStringParameter("inpNotShown");
      String strShown = vars.getInStringParameter("inpShown");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId", "ReportShipmentDimensionalAnalyzeJR|warehouse");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportShipmentDimensionalAnalyzeJR|org", "0");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId", "ReportShipmentDimensionalAnalyzeJR|salesrep");
      String strOrder = vars.getRequestGlobalVariable("inpOrder","ReportShipmentDimensionalAnalyzeJR|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strPartnerSalesrepId = vars.getRequestGlobalVariable("inpPartnerSalesrepId", "ReportShipmentDimensionalAnalyzeJR|partnersalesrep");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      printPageHtml(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strOrder, strMayor, strMenor, strPartnerSalesrepId, "pdf");
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String
      strcBpartnerId, String strProductCategory, String strmProductId, String strmWarehouseId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String
      strOrg, String strsalesrepId, String strOrder, String strMayor, String strMenor, String strPartnerSalesrepId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"selEliminarHeader1"};
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
    }
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportShipmentDimensionalAnalyzeJRFilter", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportShipmentDimensionalAnalyzeJRFilter", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());  

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportShipmentDimensionalAnalyzeJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportShipmentDimensionalAnalyzeJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportShipmentDimensionalAnalyzeJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportShipmentDimensionalAnalyzeJR");
      vars.removeMessage("ReportShipmentDimensionalAnalyzeJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    } 

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRef", strDateFromRef);
    xmlDocument.setParameter("dateFromRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRef", strDateToRef);
    xmlDocument.setParameter("dateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    /*xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
      xmlDocument.setParameter("bPartnerDescription", ReportShipmentDimensionalAnalyzeJRData.selectBpartner(this, strcBpartnerId));
      xmlDocument.setParameter("mProduct", strmProductId);
      xmlDocument.setParameter("productDescription", ReportShipmentDimensionalAnalyzeJRData.selectMproduct(this, strmProductId));*/
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("cBpGroupId", strPartnerGroup);
    xmlDocument.setParameter("salesRepId", strsalesrepId);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    //xmlDocument.setParameter("sales", strSales);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("comparative", strComparative);
    xmlDocument.setParameter("partnerSalesRepId", strPartnerSalesrepId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportShipmentDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipmentDimensionalAnalyzeJR", strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportShipmentDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipmentDimensionalAnalyzeJR", strPartnerGroup);
      xmlDocument.setData("reportC_BP_GROUPID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportShipmentDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipmentDimensionalAnalyzeJR", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID", "AD_User SalesRep", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client", "ReportSalesDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesDimensionalAnalyzeJR", strsalesrepId);
      xmlDocument.setData("reportSalesRep_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportShipmentDimensionalAnalyzeJRData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportShipmentDimensionalAnalyzeJRData.selectMproduct(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strmProductId));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "", "C_BPartner SalesRep", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesDimensionalAnalyzeJR"), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipmentDimensionalAnalyzeJR", strPartnerSalesrepId);
      xmlDocument.setData("reportPartnerSalesRep_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1", ReportShipmentDimensionalAnalyzeJRData.selectNotShown(this, strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportShipmentDimensionalAnalyzeJRData[0]:ReportShipmentDimensionalAnalyzeJRData.selectShown(this, strShown));
    } else {
      xmlDocument.setData("structure1", ReportShipmentDimensionalAnalyzeJRData.selectNotShownTrl(this,vars.getLanguage(), strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportShipmentDimensionalAnalyzeJRData[0]:ReportShipmentDimensionalAnalyzeJRData.selectShownTrl(this,vars.getLanguage(),strShown));
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
      }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String
      strcBpartnerId, String strProductCategory, String strmProductId, String strmWarehouseId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String
      strOrg, String strsalesrepId, String strOrder, String strMayor, String strMenor, String strPartnerSalesrepId, String strOutput) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
    String strOrderby = "";
    String[] discard = {"", "", "", "", "", "", "", "", ""};
    String[] discard1={"selEliminarBody1", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard"};
    if (strComparative.equals("Y")) discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " "+strDateFrom+" " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strDateTo;
    if (!strPartnerGroup.equals("")) strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage()) + " "+ReportShipmentDimensionalAnalyzeJRData.selectBpgroup(this, strPartnerGroup);
    if (!strProductCategory.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "ProductCategory", vars.getLanguage()) + " "+ReportShipmentDimensionalAnalyzeJRData.selectProductCategory(this, strProductCategory);
    if (!strsalesrepId.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "TheSalesRep", vars.getLanguage()) + " "+ReportInvoiceCustomerDimensionalAnalysesData.selectSalesrep(this, strsalesrepId);
    if (!strPartnerSalesrepId.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "TheClientSalesRep", vars.getLanguage()) + " "+ReportInvoiceCustomerDimensionalAnalysesData.selectSalesrep(this, strPartnerSalesrepId);  
    if (!strmWarehouseId.equals("")) strTitle = strTitle+" " + Utility.messageBD(this, "And", vars.getLanguage()) + " " + Utility.messageBD(this, "TheWarehouse", vars.getLanguage()) + " "+ReportShipmentDimensionalAnalyzeJRData.selectMwarehouse(this, strmWarehouseId);

    ReportShipmentDimensionalAnalyzeJRData[] data = null;
    String[] strShownArray = {"", "", "", "", "", "", "", "", ""};
    if (strShown.startsWith("(")) strShown = strShown.substring(1, strShown.length()-1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      int intContador = 0;
      while (st.hasMoreTokens()){
        strShownArray[intContador] = st.nextToken();
        intContador++;
      }

    }
    ReportSalesDimensionalAnalyzeJRData[] dimensionLabel = null;
    if (vars.getLanguage().equals("en_US")) {
      dimensionLabel = ReportSalesDimensionalAnalyzeJRData.selectNotShown(this, "");
    } else {
      dimensionLabel = ReportSalesDimensionalAnalyzeJRData.selectNotShownTrl(this, vars.getLanguage(), "");
    }
    
    String[] strLevelLabel = {"", "", "", "", "", "", "", "", ""};
    String[] strTextShow = {"", "", "", "", "", "", "", "", ""};
    int intDiscard = 0;
    int intProductLevel = 10;
    int intAuxDiscard = -1;
    for (int i = 0; i<9; i++){
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[0].name;
      }
      else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('C_Bpartner'), to_char( C_BPARTNER.C_BPARTNER_ID), to_char( 'es_ES'))";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[1].name;
      }
      else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[2].name;
      }
      else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('M_Product'), to_char( M_PRODUCT.M_PRODUCT_ID), to_char( 'es_ES'))";
        intAuxDiscard = i;
        intDiscard++;
        intProductLevel = i+1;
        strLevelLabel[i] = dimensionLabel[3].name;
      }
      else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "M_INOUT.DOCUMENTNO";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[4].name;
      }
      else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[5].name;
      }
      else if (strShownArray[i].equals("7")) {
        strTextShow[i] = "M_WAREHOUSE.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[6].name;
      }
      else if (strShownArray[i].equals("8")) {
        strTextShow[i] = "AD_ORG.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[7].name;
      }
      else if (strShownArray[i].equals("9")) {
        strTextShow[i] = "CB.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[8].name;
      }
      else {
        strTextShow[i] = "''";
        discard[i] = "display:none;";
      }
    }
    if (intDiscard != 0 || intAuxDiscard != -1){
      int k=1;
      strOrderby = " ORDER BY NIVEL"+k+",";
      while (k<intDiscard){
        strOrderby = strOrderby+"NIVEL"+k+",";
        k++;
      }
      if (k==1){
        if (strOrder.equals("Normal")){
          strOrderby = " ORDER BY NIVEL"+k;
        } else if (strOrder.equals("Amountasc")){
          strOrderby = " ORDER BY QTY ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby = " ORDER BY QTY DESC";
        } else{
          strOrderby = "1";
        }
      } else{
        if (strOrder.equals("Normal")){
          strOrderby += "NIVEL"+k;
        } else if (strOrder.equals("Amountasc")){
          strOrderby += "QTY ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby += "QTY DESC";
        } else{
          strOrderby = "1";
        }
      }

    } else{
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {strHaving = " HAVING SUM(MOVEMENTQTY) > "+strMayor+" AND SUM(MOVEMENTQTY) < "+strMenor;}
    else if (!strMayor.equals("") && strMenor.equals("")) {strHaving = " HAVING SUM(MOVEMENTQTY) > "+strMayor;}
    else if (strMayor.equals("") && !strMenor.equals("")) {strHaving = " HAVING SUM(MOVEMENTQTY) < "+strMenor;}
    else{ 
      if (strComparative.equals("Y")) strHaving = " HAVING SUM(MOVEMENTQTY) <> 0 OR SUM(MOVEMENTQTYREF) <> 0";
      else strHaving = " HAVING SUM(MOVEMENTQTY) <> 0";
    }
    strOrderby = strHaving + strOrderby;
    String strReportPath = "";
    if (strComparative.equals("Y")){
      strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/WeightDimensionalComparative.jrxml";
      data = ReportShipmentDimensionalAnalyzeJRData.select(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5], strTextShow[6], strTextShow[7], strTextShow[8], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strsalesrepId, strPartnerSalesrepId, strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"), strOrderby);
    } else{
      strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/WeightDimensionalNoComparative.jrxml";
      data = ReportShipmentDimensionalAnalyzeJRData.selectNoComparative(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5], strTextShow[6], strTextShow[7], strTextShow[8], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportShipmentDimensionalAnalyzeJR"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strmWarehouseId, strsalesrepId, strPartnerSalesrepId, strOrderby);
    }
    if (data.length == 0 || data == null){
      //discard1[0] = "selEliminar1";
      data = ReportShipmentDimensionalAnalyzeJRData.set();
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("LEVEL1_LABEL", strLevelLabel[0]);
    parameters.put("LEVEL2_LABEL", strLevelLabel[1]);
    parameters.put("LEVEL3_LABEL", strLevelLabel[2]);
    parameters.put("LEVEL4_LABEL", strLevelLabel[3]);
    parameters.put("LEVEL5_LABEL", strLevelLabel[4]);
    parameters.put("LEVEL6_LABEL", strLevelLabel[5]);
    parameters.put("LEVEL7_LABEL", strLevelLabel[6]);
    parameters.put("LEVEL8_LABEL", strLevelLabel[7]);
    parameters.put("LEVEL9_LABEL", strLevelLabel[8]);
    parameters.put("DIMENSIONS", new Integer(intDiscard));
    parameters.put("REPORT_TITLE", classInfo.name);
    parameters.put("REPORT_SUBTITLE", strTitle);
    parameters.put("PRODUCT_LEVEL", new Integer(intProductLevel));
    renderJR(vars, response, strReportPath, strOutput, parameters, data, null );

  }

  public String getServletInfo() {
    return "Servlet ReportShipmentDimensionalAnalyzeJR.";
  } // end of getServletInfo() method
}

