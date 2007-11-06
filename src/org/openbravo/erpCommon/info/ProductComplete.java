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
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.utility.*;

import org.openbravo.erpCommon.ad_combos.WarehouseComboData;


public class ProductComplete extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.name");
      String strIDValue = vars.getStringParameter("inpIDValue");
      // This if allows correctly filling the key and name fields and the products selector when we open it from the adecuadamente cuando lo abrimos desde la línea de albarán.
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActual(this, vars.getLanguage(), strNameValue, strIDValue);
        if (!strNameAux.equals("")) strNameValue = strNameAux;
      }
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "ProductComplete.warehouse");
      if (strWarehouse.equals("")) strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      vars.removeSessionValue("ProductComplete.key");
      if (!strNameValue.equals("")) vars.setSessionValue("ProductComplete.name", strNameValue + "%");
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals("")) isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);

      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.key");
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "ProductComplete.warehouse");
      if (strWarehouse.equals("")) strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals("")) isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);
      String strStore = vars.getStringParameter("inpWithStoreLines", isSOTrx);
      vars.removeSessionValue("ProductComplete.name");
      if (!strKeyValue.equals("")) vars.setSessionValue("ProductComplete.key", strKeyValue + "%");
      
      ProductCompleteData[] data = null;
      String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
      String strOrgs = Utility.getContext(this, vars, "#User_Org", "ProductComplete");
      if (strStore.equals("Y")) {
        if (vars.getLanguage().equals("en_US")) data = ProductCompleteData.select(this, strKeyValue + "%", "", strWarehouse, vars.getRole(), strBpartner, strClients);
        else data = ProductCompleteData.selecttrl(this, vars.getLanguage(), strKeyValue + "%", "", strWarehouse, vars.getRole(), strBpartner, strClients);
      }else {
        if (vars.getLanguage().equals("en_US")) data = ProductCompleteData.selectNotStored(this, strKeyValue + "%", "", strBpartner, strClients, strOrgs);
        else data = ProductCompleteData.selectNotStoredtrl(this, vars.getLanguage(), strKeyValue + "%", "", strBpartner, strClients, strOrgs);
      }
      if (data!=null && data.length==1) printPageKey(response, vars, data, strWarehouse);
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strKeyValue = vars.getGlobalVariable("inpKey", "ProductComplete.key", "");
      String strNameValue = vars.getGlobalVariable("inpName", "ProductComplete.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "ProductComplete.warehouse", "");
      String strBpartner = vars.getGlobalVariable("inpBPartner", "ProductComplete.bpartner", "");
      String windowId = vars.getGlobalVariable("WindowID", "ProductComplete.windowId", "");
      String strStore = vars.getStringParameter("inpWithStoreLines", vars.getSessionValue("ProductComplete.isSOTrx"));
      printPageFrame1(response, vars, strKeyValue, strNameValue, strWarehouse, windowId, strStore, strBpartner);
    } else if (vars.commandIn("FRAME2")) {
      String strKey = vars.getGlobalVariable("inpKey", "ProductComplete.key", "");
      String strName = vars.getGlobalVariable("inpName", "ProductComplete.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "ProductComplete.warehouse", "");
      String strBpartner = vars.getGlobalVariable("inpBPartner", "ProductComplete.bpartner", "");
      vars.getGlobalVariable("WindowID", "ProductComplete.windowId", "");
      String strStore = vars.getStringParameter("inpWithStoreLines", vars.getSessionValue("ProductComplete.isSOTrx"));
      printPageFrame2(response, vars, strKey, strName, strWarehouse, strStore, strBpartner);
    } else if (vars.commandIn("FIND")) {
      String strKey = vars.getRequestGlobalVariable("inpKey", "ProductComplete.key");
      String strName = vars.getRequestGlobalVariable("inpName", "ProductComplete.name");
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "ProductComplete.warehouse");
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      String strStore = vars.getStringParameter("inpWithStoreLines", "N");

      vars.setSessionValue("ProductComplete.initRecordNumber", "0");

      printPageFrame2(response, vars, strKey, strName, strWarehouse, strStore, strBpartner);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ProductComplete.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductComplete");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ProductComplete.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ProductComplete.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ProductComplete.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductComplete");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ProductComplete.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }


  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductComplete_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ProductCompleteData[] data, String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data, strWarehouse));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ProductCompleteData[] data, String strWarehouse) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].mProductId + "\";\n");
    html.append("var texto = \"" + FormatUtilities.replaceJS(data[0].name) + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_LOC\", true, \"" + data[0].mLocatorId + "\"),\n");
    html.append("new SearchElements(\"_ATR\", true, \"" + data[0].mAttributesetinstanceId + "\"),\n");
    html.append("new SearchElements(\"_PQTY\", true, \"" + (data[0].qtyorder.equals("0")?"":data[0].qtyorder) + "\"),\n");
    html.append("new SearchElements(\"_PUOM\", true, \"" + data[0].cUom2Id + "\"),\n");
    html.append("new SearchElements(\"_QTY\", true, \"" + data[0].qty + "\"),\n");
    html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUom1Id + "\")\n");
    html.append(");\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto, parameter);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue, String strNameValue, String strWarehouse, String windowId, String strStore, String strBpartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the product seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductComplete_F1").createXmlDocument();

    if (strKeyValue.equals("") && strNameValue.equals("")) {
    xmlDocument.setParameter("key", "%");
    } else {
    xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("warehouse", strWarehouse);
    xmlDocument.setParameter("store", strStore);
    xmlDocument.setParameter("bpartner", strBpartner);
    xmlDocument.setData("structure1", WarehouseComboData.select(this, vars.getRole(), vars.getClient()));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strName, String strWarehouse, String strStore, String strBpartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the products seeker");
    XmlDocument xmlDocument;
    String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
    String strOrgs = Utility.getContext(this, vars, "#User_Org", "ProductComplete");

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductComplete");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ProductComplete.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strKey.equals("") && strName.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductComplete_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", ProductCompleteData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      ProductCompleteData[] data = null;
//      if (strStore.equals("Y")) data = ProductCompleteData.select(this, strKey, strName, strWarehouse, vars.getRole(), strBpartner, strClients, initRecordNumber, intRecordRange);
//      else data = ProductCompleteData.selectNotStored(this, strKey, strName, strBpartner, strClients, strOrgs, initRecordNumber, intRecordRange);

    
      
      
      if (strStore.equals("Y")) {
        if (vars.getLanguage().equals("en_US")) data = ProductCompleteData.select(this, strKey, strName, strWarehouse, vars.getRole(), strBpartner, strClients, initRecordNumber, intRecordRange);
        else data = ProductCompleteData.selecttrl(this, vars.getLanguage(), strKey, strName, strWarehouse, vars.getRole(), strBpartner, strClients, initRecordNumber, intRecordRange);
      }else {
        if (vars.getLanguage().equals("en_US")) data = ProductCompleteData.selectNotStored(this, strKey, strName, strBpartner, strClients, strOrgs, initRecordNumber, intRecordRange);
        else data = ProductCompleteData.selectNotStoredtrl(this, vars.getLanguage(), strKey, strName, strBpartner, strClients, strOrgs,  initRecordNumber, intRecordRange);
      }
      
      
      
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductComplete_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the products seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductComplete_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que products seeker";
  } // end of getServletInfo() method
}
