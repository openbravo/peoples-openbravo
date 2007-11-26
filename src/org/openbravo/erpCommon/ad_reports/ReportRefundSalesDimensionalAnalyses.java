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

public class ReportRefundSalesDimensionalAnalyses extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")){
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportRefundSalesDimensionalAnalyses|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportRefundSalesDimensionalAnalyses|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef", "ReportRefundSalesDimensionalAnalyses|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef", "ReportRefundSalesDimensionalAnalyses|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup", "ReportRefundSalesDimensionalAnalyses|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN", "ReportRefundSalesDimensionalAnalyses|partner", "");
      String strProductCategory = vars.getGlobalVariable("inpProductCategory", "ReportRefundSalesDimensionalAnalyses|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN", "ReportRefundSalesDimensionalAnalyses|product", "");
      String strNotShown = vars.getInGlobalVariable("inpNotShown", "ReportRefundSalesDimensionalAnalyses|notShown", "");
      String strShown = vars.getInGlobalVariable("inpShown", "ReportRefundSalesDimensionalAnalyses|shown", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRefundSalesDimensionalAnalyses|org", "0");
      String strsalesrepId = vars.getGlobalVariable("inpSalesrepId", "ReportRefundSalesDimensionalAnalyses|salesrep", "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId", "ReportRefundSalesDimensionalAnalyses|warehouseId", "");
      String strOrder = vars.getGlobalVariable("inpOrder","ReportRefundSalesDimensionalAnalyses|order","Normal");
      String strMayor = vars.getGlobalVariable("inpMayor", "ReportRefundSalesDimensionalAnalyses|mayor", "");
      String strMenor = vars.getGlobalVariable("inpMenor", "ReportRefundSalesDimensionalAnalyses|menor", "");
      String strRatioMayor = vars.getGlobalVariable("inpRatioMayor", "ReportRefundSalesDimensionalAnalyses|ratioMayor", "");
      String strRatioMenor = vars.getGlobalVariable("inpRatioMenor", "ReportRefundSalesDimensionalAnalyses|ratioMenor", "");
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE")) strComparative = vars.getRequestGlobalVariable("inpComparative", "ReportRefundSalesDimensionalAnalyses|comparative");
      else strComparative = vars.getGlobalVariable("inpComparative", "ReportRefundSalesDimensionalAnalyses|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strmWarehouseId, strOrder, strMayor, strMenor, strRatioMayor, strRatioMenor);
    }else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportRefundSalesDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportRefundSalesDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "ReportRefundSalesDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "ReportRefundSalesDimensionalAnalyses|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup", "ReportRefundSalesDimensionalAnalyses|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportRefundSalesDimensionalAnalyses|partner");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportRefundSalesDimensionalAnalyses|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportRefundSalesDimensionalAnalyses|product");
      String strNotShown = vars.getInStringParameter("inpNotShown");
      String strShown = vars.getInStringParameter("inpShown");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportRefundSalesDimensionalAnalyses|org", "0");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId", "ReportRefundSalesDimensionalAnalyses|salesrep");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId", "ReportRefundSalesDimensionalAnalyses|warehouseId");
      String strOrder = vars.getRequestGlobalVariable("inpOrder","ReportRefundSalesDimensionalAnalyses|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strRatioMayor = vars.getStringParameter("inpRatioMayor", "");
      String strRatioMenor = vars.getStringParameter("inpRatioMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      printPageHtml(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strmWarehouseId, strOrder, strMayor, strMenor, strRatioMayor, strRatioMenor);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String strOrg, String strsalesrepId, String strmWarehouseId, String strOrder, String strMayor, String strMenor, String strRatioMayor, String strRatioMenor) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"selEliminarHeader1"};
    String strCommand = "EDIT_PDF";
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
      strCommand = "EDIT_PDF_COMPARATIVE";
    }
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportRefundSalesDimensionalAnalysesFilter", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportRefundSalesDimensionalAnalysesFilter", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());  

    try {
      KeyMap key = new KeyMap(this, vars, "ReportRefundSalesDimensionalAnalyses.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportRefundSalesDimensionalAnalyses");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportRefundSalesDimensionalAnalyses.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportRefundSalesDimensionalAnalyses.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportRefundSalesDimensionalAnalyses");
      vars.removeMessage("ReportRefundSalesDimensionalAnalyses");
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
    xmlDocument.setParameter("cBpGroupId", strPartnerGroup);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("salesRepId", strsalesrepId);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("ratioasc", strOrder);
    xmlDocument.setParameter("ratiodesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("ratioMayor", strRatioMayor);
    xmlDocument.setParameter("ratioMenor", strRatioMenor);
    xmlDocument.setParameter("comparative", strComparative);
    xmlDocument.setParameter("command", strCommand);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars, "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportRefundSalesDimensionalAnalyses", strPartnerGroup);
      xmlDocument.setData("reportC_BP_GROUPID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars, "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportRefundSalesDimensionalAnalyses", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID", "AD_User SalesRep", "", Utility.getContext(this, vars, "#User_Org", "ReportRefundSalesDimensionalAnalyses"), Utility.getContext(this, vars, "#User_Client", "ReportRefundSalesDimensionalAnalyses"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportRefundSalesDimensionalAnalyses", strsalesrepId);
      xmlDocument.setData("reportSalesRep_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportRefundSalesDimensionalAnalysesData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportRefundSalesDimensionalAnalysesData.selectMproduct(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strmProductId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportSalesDimensionalAnalyze"), Utility.getContext(this, vars, "#User_Client", "ReportSalesDimensionalAnalyze"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesDimensionalAnalyze", strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }




    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1", ReportRefundSalesDimensionalAnalysesData.selectNotShown(this, strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportRefundSalesDimensionalAnalysesData[0]:ReportRefundSalesDimensionalAnalysesData.selectShown(this, strShown));
    } else {
      xmlDocument.setData("structure1", ReportRefundSalesDimensionalAnalysesData.selectNotShownTrl(this,vars.getLanguage(), strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportRefundSalesDimensionalAnalysesData[0]:ReportRefundSalesDimensionalAnalysesData.selectShownTrl(this,vars.getLanguage(),strShown));
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String strOrg, String strsalesrepId, String strmWarehouseId, String strOrder, String strMayor, String strMenor, String strRatioMayor, String strRatioMenor) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
    XmlDocument xmlDocument=null;
    String strOrderby = "";
    String[] discard = {"", "", "", "", "","",""};
    String[] discard1={"selEliminarBody1", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard"};
    if (strComparative.equals("Y")) discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " "+strDateFrom+" " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strDateTo;
    if (!strPartnerGroup.equals("")) strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage()) + " "+ReportRefundSalesDimensionalAnalysesData.selectBpgroup(this, strPartnerGroup);
    if (!strProductCategory.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "ProductCategory", vars.getLanguage()) + " "+ReportRefundSalesDimensionalAnalysesData.selectProductCategory(this, strProductCategory);
    if (!strsalesrepId.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "TheSalesRep", vars.getLanguage()) + " "+ReportRefundSalesDimensionalAnalysesData.selectSalesrep(this, strsalesrepId);
    if (!strmWarehouseId.equals("")) strTitle = strTitle+" " + Utility.messageBD(this, "And", vars.getLanguage()) + " " + Utility.messageBD(this, "TheWarehouse", vars.getLanguage()) + " "+ReportRefundSalesDimensionalAnalysesData.selectMwarehouse(this, strmWarehouseId);

    ReportRefundSalesDimensionalAnalysesData[] data = null;
    String[] strShownArray = {"", "", "", "", "", "", ""};
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
    String[] strTextShow = {"", "", "", "", "","",""};
    int intDiscard = 0;
    int intAuxDiscard = -1;
    for (int i = 0; i<7; i++){
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
      }
      else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('C_Bpartner'), to_char( C_BPARTNER.C_BPARTNER_ID), to_char( 'es_ES'))";
        intDiscard++;
      }
      else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
      }
      else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER(to_char('M_Product'), to_char( M_PRODUCT.M_PRODUCT_ID), to_char( 'es_ES'))";
        intAuxDiscard = i;
      }
      else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_ORDER.DOCUMENTNO";
        intDiscard++;
      }
      else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        intDiscard++;
      }
      else if (strShownArray[i].equals("7")) {
        strTextShow[i] = "M_WAREHOUSE.NAME";
        intDiscard++;
      }
      else {
        strTextShow[i] = "''";
        discard[i] = "display:none;";
      }
    }
    if (intDiscard != 0){
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
          strOrderby = " ORDER BY LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby = " ORDER BY LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")){
          strOrderby = " ORDER BY RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")){
          strOrderby = " ORDER BY RATIO DESC";
        }else{
          strOrderby = "1";
        }
      } else{
        if (strOrder.equals("Normal")){
          strOrderby += "NIVEL"+k;
        } else if (strOrder.equals("Amountasc")){
          strOrderby += "LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby += "LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")){
          strOrderby += "RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")){
          strOrderby += "RATIO DESC";
        } else{
          strOrderby = "1";
        }
      }

    } else{
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {strHaving = " HAVING SUM(LINENETAMT) > "+strMayor+" AND SUM(LINENETAMT) < "+strMenor;}
    else if (!strMayor.equals("") && strMenor.equals("")) {strHaving = " HAVING SUM(LINENETAMT) > "+strMayor;}
    else if (strMayor.equals("") && !strMenor.equals("")) {strHaving = " HAVING SUM(LINENETAMT) < "+strMenor;}
    else{}
    if (strHaving.equals("")){
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {strHaving = " HAVING DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "+strRatioMayor+" AND DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "+strRatioMenor;}
      else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {strHaving = " HAVING DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "+strRatioMayor;}
      else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {strHaving = " HAVING DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "+strRatioMenor;}
      else{}
    } else {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {strHaving = " AND DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "+strRatioMayor+" AND DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "+strRatioMenor;}
      else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {strHaving = " AND DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "+strRatioMayor;}
      else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {strHaving = " AND DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "+strRatioMenor;}
      else{}
    }
    strOrderby = strHaving + strOrderby;
    if (strComparative.equals("Y")){
      data = ReportRefundSalesDimensionalAnalysesData.select(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5], strTextShow[6], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strsalesrepId, strmWarehouseId, strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"), strOrderby);
    } else {
      data = ReportRefundSalesDimensionalAnalysesData.selectNoComparative(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5], strTextShow[6], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strsalesrepId, strmWarehouseId, strOrderby);
    }

    if (data.length == 0 || data == null){
      //discard1[0] = "selEliminar1";
      data = ReportRefundSalesDimensionalAnalysesData.set();
    } else {
      int contador = intDiscard;
      if (intAuxDiscard != -1) contador = intAuxDiscard;
      int k = 1;
      if (strComparative.equals("Y")){
        for (int j = contador; j>0; j--){
          discard1[k] = "fieldTotalQtyNivel"+String.valueOf(j);
          discard1[k+12] = "fieldTotalRefundQtyNivel"+String.valueOf(j);
          discard1[k+24] = "fieldUomsymbol"+String.valueOf(j);
          discard1[k+6] = "fieldTotalRefQtyNivel"+String.valueOf(j);
          discard1[k+18] = "fieldTotalRefRefundQtyNivel"+String.valueOf(j);
          k++;
        }
      } else {
        for (int j = contador; j>0; j--){
          discard1[k] = "fieldNoncomparativeTotalQtyNivel"+String.valueOf(j);
          discard1[k+10] = "fieldNoncomparativeTotalRefundQtyNivel"+String.valueOf(j);
          discard1[k+20] = "fieldNoncomparativeUomsymbol"+String.valueOf(j);
          k++;
        }
      }

    }
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportRefundSalesDimensionalAnalysesEdition", discard1).createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("eliminar2", discard[1]);
    xmlDocument.setParameter("eliminar3", discard[2]);
    xmlDocument.setParameter("eliminar4", discard[3]);
    xmlDocument.setParameter("eliminar5", discard[4]);
    xmlDocument.setParameter("eliminar6", discard[5]);
    xmlDocument.setParameter("eliminar7", discard[6]);
    xmlDocument.setParameter("title", strTitle);
    xmlDocument.setParameter("constante", "100");
    if (strComparative.equals("Y")){
      xmlDocument.setData("structure1", data);
    } else {
      xmlDocument.setData("structure2", data);
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportRefundSalesDimensionalAnalyses. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}

