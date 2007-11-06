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

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.ComboTableData;



public class MaterialTransactions extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strDesde = vars.getStringParameter("inpDesde", "");
      String strHasta = vars.getStringParameter("inpHasta", "");
      String strmovementType = vars.getStringParameter("inpmovementType", "");
      String stradOrgId = vars.getStringParameter("inpadOrgId", "");
      String strmLocatorId = vars.getStringParameter("inpmLocatorId", "");
      String strmProductId = vars.getStringParameter("inpmProductId", "");
      printPage(response, vars, strDesde, strHasta, strmovementType, stradOrgId, strmLocatorId, strmProductId);
    } else if (vars.commandIn("FIND")) {
      String strDesde = vars.getRequiredStringParameter("inpDesde");
      String strHasta = vars.getRequiredStringParameter("inpHasta");
      String strmovementType = vars.getStringParameter("inpmovementType");
      String stradOrgId = vars.getStringParameter("inpadOrgId");
      String strmLocatorId = vars.getStringParameter("inpmLocatorId");
      String strmProductId = vars.getStringParameter("inpmProductId");
      printPage(response, vars, strDesde, strHasta, strmovementType, stradOrgId, strmLocatorId, strmProductId);
    } else if (vars.commandIn("GO")) {
      String strMTransactionId = vars.getRequiredStringParameter("inpMTransactionId");
      String strmovementType =  MaterialTransactionsData.tipoMovimiento(this, strMTransactionId);
      if(strmovementType.equals("C-") || strmovementType.equals("C+")){
        vars.setSessionValue("169|M_InOut_ID", MaterialTransactionsData.claveMovimiento(this, strMTransactionId));
        vars.setSessionValue("169|M_InOutLine_ID", MaterialTransactionsData.claveLinea(this, strMTransactionId));
        response.sendRedirect(strDireccion + "/ShipmentCustomer/ShipmentLine_Relation.html?COMMAND=RELATION");
      } else if(strmovementType.equals("I+") || strmovementType.equals("I-")){
        vars.setSessionValue("168|M_Inventory_ID", MaterialTransactionsData.claveMovimiento(this, strMTransactionId));
        vars.setSessionValue("168|M_InventoryLine_ID", MaterialTransactionsData.claveLinea(this, strMTransactionId));
        response.sendRedirect(strDireccion + "/PhysicalInventory/Lines_Relation.html?COMMAND=RELATION");
      } else if(strmovementType.equals("V-") || strmovementType.equals("V+")){
        vars.setSessionValue("184|M_InOut_ID", MaterialTransactionsData.claveMovimiento(this, strMTransactionId));
        vars.setSessionValue("184|M_InOutLine_ID", MaterialTransactionsData.claveLinea(this, strMTransactionId));
        response.sendRedirect(strDireccion + "/GoodsReceipt/Lines_Relation.html?COMMAND=RELATION");
      } else if(strmovementType.equals("P+") || strmovementType.equals("P-")){
        vars.setSessionValue("191|M_Production_ID", MaterialTransactionsData.claveProduccion(this, strMTransactionId));
        vars.setSessionValue("191|M_ProductionPlan_ID", MaterialTransactionsData.claveMovimiento(this, strMTransactionId));
        vars.setSessionValue("191|M_ProductionLine_ID", MaterialTransactionsData.claveLinea(this, strMTransactionId));
        response.sendRedirect(strDireccion + "/BillofMaterialsProduction/IOProducts.html?COMMAND=RELATION");
      } else if(strmovementType.equals("M+") || strmovementType.equals("M-")){
        vars.setSessionValue("170|M_Movement_ID", MaterialTransactionsData.claveMovimiento(this, strMTransactionId));
        vars.setSessionValue("170|M_MovementLine_ID", MaterialTransactionsData.claveLinea(this, strMTransactionId));
        response.sendRedirect(strDireccion + "/GoodsMovements/Lines_Relation.html?COMMAND=RELATION");
      }
    } else pageError(response);
  }

/*  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: MaterialTransactions seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/
  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDesde, String  strHasta, String strmovementType, String stradOrgId, String  strmLocatorId, String  strmProductId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the MaterialTransactions seeker");
    //XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions_F1").createXmlDocument();
    XmlDocument xmlDocument = null;
    /*if (strDesde.equals("") && strHasta.equals("") && strmovementType.equals("") && stradOrgId.equals("") && strmLocatorId.equals("") && strmProductId.equals("")) {
       //String[] discard = {"sectionDetail"};
       xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions").createXmlDocument();
       xmlDocument.setData("structure1", MaterialTransactionsData.set());
    } else {*/
    String[] discard = {"discard"};
    MaterialTransactionsData[] data = null;
    if (strDesde.equals("") && strHasta.equals("") && strmovementType.equals("") && stradOrgId.equals("") && strmLocatorId.equals("") && strmProductId.equals("")) {
    	data = MaterialTransactionsData.set();
	discard[0] = "sectionDetail";
    } else {
//       xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions").createXmlDocument();
       data =  MaterialTransactionsData.select(this, vars.getLanguage(), "(" + Utility.getContext(this, vars, "#User_Client", "MaterialTransactions") + ")",stradOrgId, strmovementType, strmLocatorId, strmProductId, "(" + Utility.getContext(this, vars, "#User_Org", "MaterialTransactions") + ")", strDesde, strHasta);
       if (data==null || data.length == 0) {
       	data = new MaterialTransactionsData[0];
	discard[0] = "sectionDetail";
       }
    }
       xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions", discard).createXmlDocument();
       //xmlDocument.setData("structure1", data);
    //}
   

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "MaterialTransactions", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    log4j.debug("parte1");
	try {
      KeyMap key = new KeyMap(this, vars, "MaterialTransactions.html");
      xmlDocument.setParameter("keyMap", key.getActionButtonKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    log4j.debug("parte2");
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.MaterialTransactions");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "MaterialTransactions.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "MaterialTransactions.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    log4j.debug("parte3");
    {
      OBError myMessage = vars.getMessage("MaterialTransactions");
      vars.removeMessage("MaterialTransactions");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    log4j.debug("parte4");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    log4j.debug("parte5");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "AD_Org Security validation", Utility.getContext(this, vars, "#User_Org", "MaterialTransactions"), Utility.getContext(this, vars, "#User_Client", "MaterialTransactions"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "MaterialTransactions", "");
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      log4j.debug("combo1");
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    log4j.debug("parte6");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "M_Transaction Movement Type", "", Utility.getContext(this, vars, "#User_Org", "MaterialTransactions"), Utility.getContext(this, vars, "#User_Client", "MaterialTransactions"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "MaterialTransactions", "");
      log4j.debug("combo2");
      xmlDocument.setData("reportMovementType","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setData("structure1", data);
    log4j.debug("parte7");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    log4j.debug("salida estandar: "+xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

/*  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDesde, String strHasta, String strmovementType, String stradOrgId,
  String strmLocatorId, String strmProductId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the MaterialTransactionss seeker");
    XmlDocument xmlDocument;
    if (strDesde.equals("") && strHasta.equals("") && strmovementType.equals("") && stradOrgId.equals("") && strmLocatorId.equals("") && strmProductId.equals("")) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", MaterialTransactionsData.set());
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MaterialTransactions_F2").createXmlDocument();
      xmlDocument.setData("structure1", MaterialTransactionsData.select(this, vars.getLanguage(), "(" + Utility.getContext(this, vars, "#User_Client", "MaterialTransactions") + ")",
      stradOrgId, strmovementType, strmLocatorId, strmProductId, "(" + Utility.getContext(this, vars, "#User_Org", "MaterialTransactions") + ")", strDesde, strHasta));
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet that presents the MaterialTransactions seeker";
  } // end of getServletInfo() method
}
