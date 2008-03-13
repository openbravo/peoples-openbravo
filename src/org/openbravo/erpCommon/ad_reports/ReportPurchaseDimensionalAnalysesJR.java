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
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
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


public class ReportPurchaseDimensionalAnalysesJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")){
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportPurchaseDimensionalAnalysesJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportPurchaseDimensionalAnalysesJR|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef", "ReportPurchaseDimensionalAnalysesJR|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef", "ReportPurchaseDimensionalAnalysesJR|dateToRef", "");
      String strPartnerGroup = vars.getGlobalVariable("inpPartnerGroup", "ReportPurchaseDimensionalAnalysesJR|partnerGroup", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN", "ReportPurchaseDimensionalAnalysesJR|partner", "");
      String strProductCategory = vars.getGlobalVariable("inpProductCategory", "ReportPurchaseDimensionalAnalysesJR|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN", "ReportPurchaseDimensionalAnalysesJR|product", "");
      String strNotShown = vars.getInGlobalVariable("inpNotShown", "ReportPurchaseDimensionalAnalysesJR|notShown", "");
      String strShown = vars.getInGlobalVariable("inpShown", "ReportPurchaseDimensionalAnalysesJR|shown", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseDimensionalAnalysesJR|org", "0");
      String strOrder = vars.getGlobalVariable("inpOrder","ReportPurchaseDimensionalAnalyze|order","Normal");
      String strMayor = vars.getGlobalVariable("inpMayor", "ReportPurchaseDimensionalAnalyze|mayor", "");
      String strMenor = vars.getGlobalVariable("inpMenor", "ReportPurchaseDimensionalAnalyze|menor", "");
      String strComparative = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE")) strComparative = vars.getRequestGlobalVariable("inpComparative", "ReportPurchaseDimensionalAnalysesJR|comparative");
      else strComparative = vars.getGlobalVariable("inpComparative", "ReportPurchaseDimensionalAnalysesJR|comparative", "N");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor);
    }else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportPurchaseDimensionalAnalysesJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportPurchaseDimensionalAnalysesJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "ReportPurchaseDimensionalAnalysesJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "ReportPurchaseDimensionalAnalysesJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup", "ReportPurchaseDimensionalAnalysesJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportPurchaseDimensionalAnalysesJR|partner");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportPurchaseDimensionalAnalysesJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportPurchaseDimensionalAnalysesJR|product");
      String strNotShown = vars.getInStringParameter("inpNotShown");
      String strShown = vars.getInStringParameter("inpShown");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseDimensionalAnalysesJR|org", "0");
      String strOrder = vars.getRequestGlobalVariable("inpOrder","ReportPurchaseDimensionalAnalysesJR|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      printPageHtml(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor, "html");
    }else if (vars.commandIn("EDIT_PDF", "EDIT_PDF_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportPurchaseDimensionalAnalysesJR|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportPurchaseDimensionalAnalysesJR|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "ReportPurchaseDimensionalAnalysesJR|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "ReportPurchaseDimensionalAnalysesJR|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup", "ReportPurchaseDimensionalAnalysesJR|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportPurchaseDimensionalAnalysesJR|partner");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportPurchaseDimensionalAnalysesJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportPurchaseDimensionalAnalysesJR|product");
      String strNotShown = vars.getInStringParameter("inpNotShown");
      String strShown = vars.getInStringParameter("inpShown");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseDimensionalAnalysesJR|org", "0");
      String strOrder = vars.getRequestGlobalVariable("inpOrder","ReportPurchaseDimensionalAnalysesJR|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      //if (strComparative.equals("N")) 
      printPageHtml(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor, "pdf");
      //else printPagePdf(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef, strDateToRef, strOrg, strOrder, strMayor, strMenor);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String strOrg, String strOrder, String strMayor, String strMenor) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"selEliminarHeader1"};
    if (strComparative.equals("Y")) {
      discard[0] = "selEliminarHeader2";
    }
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportPurchaseDimensionalAnalysesFilterJR", discard).createXmlDocument();
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportPurchaseDimensionalAnalysesFilterJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());  
    
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
    xmlDocument.setParameter("bPartnerDescription", ReportPurchaseDimensionalAnalysesJRData.selectBpartner(this, strcBpartnerId));
    xmlDocument.setParameter("mProduct", strmProductId);
    xmlDocument.setParameter("productDescription", ReportPurchaseDimensionalAnalysesJRData.selectMproduct(this, strmProductId));*/
    xmlDocument.setParameter("cBpGroupId", strPartnerGroup);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("normal", strOrder);
    xmlDocument.setParameter("amountasc", strOrder);
    xmlDocument.setParameter("amountdesc", strOrder);
    xmlDocument.setParameter("mayor", strMayor);
    xmlDocument.setParameter("menor", strMenor);
    xmlDocument.setParameter("comparative", strComparative);

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportPurchaseDimensionalAnalysesJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportPurchaseDimensionalAnalysesFilterJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportPurchaseDimensionalAnalysesFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportPurchaseDimensionalAnalysesJR");
      vars.removeMessage("ReportPurchaseDimensionalAnalysesJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


  try {
    ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BP_Group_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportPurchaseDimensionalAnalysesJR"), Utility.getContext(this, vars, "#User_Client", "ReportPurchaseDimensionalAnalysesJR"), 0);
    Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPurchaseDimensionalAnalysesJR", strPartnerGroup);
    xmlDocument.setData("reportC_BP_GROUPID","liststructure", comboTableData.select(false));
    comboTableData = null;
  } catch (Exception ex) {
    throw new ServletException(ex);
  }


  try {
    ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportPurchaseDimensionalAnalysesJR"), Utility.getContext(this, vars, "#User_Client", "ReportPurchaseDimensionalAnalysesJR"), 0);
    Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPurchaseDimensionalAnalysesJR", strProductCategory);
    xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
    comboTableData = null;
  } catch (Exception ex) {
    throw new ServletException(ex);
  }

    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportPurchaseDimensionalAnalysesJRData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("reportMProductId_IN", "liststructure",  ReportPurchaseDimensionalAnalysesJRData.selectMproduct(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strmProductId));
   
       
     if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setData("structure1", ReportPurchaseDimensionalAnalysesJRData.selectNotShown(this, strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportPurchaseDimensionalAnalysesJRData[0]:ReportPurchaseDimensionalAnalysesJRData.selectShown(this, strShown));
    } else {
      xmlDocument.setData("structure1", ReportPurchaseDimensionalAnalysesJRData.selectNotShownTrl(this, vars.getLanguage(), strShown)); 
      xmlDocument.setData("structure2", strShown.equals("")?new ReportPurchaseDimensionalAnalysesJRData[0]:ReportPurchaseDimensionalAnalysesJRData.selectShownTrl(this, vars.getLanguage(), strShown));
    }
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup, String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown, String strShown, String strDateFromRef, String strDateToRef, String strOrg, String strOrder, String strMayor, String strMenor, String strOutput) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    String strOrderby = "";
    String[] discard = {"", "", "", "", ""};
    String[] discard1={"selEliminarBody1", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard"};
    if (strComparative.equals("Y")) discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strDateTo;
    if (!strPartnerGroup.equals("")) strTitle = strTitle+", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage()) + " "+ReportPurchaseDimensionalAnalysesJRData.selectBpgroup(this, strPartnerGroup);
    
    if (!strProductCategory.equals("")) strTitle = strTitle+" " + Utility.messageBD(this, "And", vars.getLanguage()) + " " + Utility.messageBD(this, "ProductCategory", vars.getLanguage()) + " "+ReportPurchaseDimensionalAnalysesJRData.selectProductCategory(this, strProductCategory);
    

    ReportPurchaseDimensionalAnalysesJRData[] data = null;
    String[] strShownArray = {"", "", "", "", ""};
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
    
    ReportPurchaseDimensionalAnalysesJRData[] dimensionLabel = null;
    if (vars.getLanguage().equals("en_US")) {
      dimensionLabel = ReportPurchaseDimensionalAnalysesJRData.selectNotShown(this, "");
    } else {
      dimensionLabel = ReportPurchaseDimensionalAnalysesJRData.selectNotShownTrl(this, vars.getLanguage(), "");
    }
    
    String[] strLevelLabel = {"", "", "", "", ""};
    String[] strTextShow = {"", "", "", "", ""};
    int intDiscard = 0;
    int intProductLevel = 6;
    int intAuxDiscard = -1;
    for (int i = 0; i<5; i++){
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[0].name;
      }
      else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER('C_Bpartner', TO_CHAR(C_BPARTNER.C_BPARTNER_ID), 'es_ES')";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[1].name;
      }
      else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[2].name;
      }
      else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER('M_Product', TO_CHAR(M_PRODUCT.M_PRODUCT_ID), 'es_ES')";
        intProductLevel = i+1;
        intDiscard++;
        intAuxDiscard = i;
        strLevelLabel[i] = dimensionLabel[3].name;
      }
      else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_ORDER.DOCUMENTNO";
        intDiscard++;
        strLevelLabel[i] = dimensionLabel[4].name;
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
          strOrderby = " ORDER BY AMOUNT ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby = " ORDER BY AMOUNT DESC";
        } else{
          strOrderby = "1";
        }
      } else{
        if (strOrder.equals("Normal")){
          strOrderby += "NIVEL"+k;
        } else if (strOrder.equals("Amountasc")){
          strOrderby += "AMOUNT ASC";
        } else if (strOrder.equals("Amountdesc")){
          strOrderby += "AMOUNT DESC";
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
    else{ strHaving = " HAVING SUM(LINENETAMT) <> 0 OR SUM(LINENETREF) <> 0";}
    strOrderby = strHaving + strOrderby;

    String strReportPath;
    if (strComparative.equals("Y")){
      strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/SimpleDimensionalComparative.jrxml";
      
      data = ReportPurchaseDimensionalAnalysesJRData.select(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportPurchaseDimensionalAnalysesJR"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"), strOrderby);


    } else { //no comparative report using JasperReports
      strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/SimpleDimensionalNoComparative.jrxml";

      data = ReportPurchaseDimensionalAnalysesJRData.selectNoComparative(this, strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility.getContext(this, vars, "#User_Client", "ReportPurchaseDimensionalAnalysesJR"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strOrderby);
    }
    parameters.put("LEVEL1_LABEL", strLevelLabel[0]);
    parameters.put("LEVEL2_LABEL", strLevelLabel[1]);
    parameters.put("LEVEL3_LABEL", strLevelLabel[2]);
    parameters.put("LEVEL4_LABEL", strLevelLabel[3]);
    parameters.put("LEVEL5_LABEL", strLevelLabel[4]);
    parameters.put("DIMENSIONS", new Integer(intDiscard));
    parameters.put("REPORT_TITLE", classInfo.name);
    parameters.put("REPORT_SUBTITLE", strTitle);
    parameters.put("PRODUCT_LEVEL", new Integer(intProductLevel));
    renderJR(vars, response, strReportPath, strOutput, parameters, data, null );
  }

  public String getServletInfo() {
    return "Servlet ReportPurchaseDimensionalAnalysesJR.";
  } // end of getServletInfo() method
}

