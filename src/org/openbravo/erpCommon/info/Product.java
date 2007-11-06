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
import org.openbravo.utils.Replace;

import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.ad_combos.WarehouseComboData;
import org.openbravo.erpCommon.ad_combos.PriceListVersionComboData;


public class Product extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Product.name");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActual(this, vars.getLanguage(), strNameValue, strIDValue);
        if (!strNameAux.equals("")) strNameValue = strNameAux;
      }
      String strPriceList = vars.getRequestGlobalVariable("inpPriceList", "Product.priceList");
      String strDate = vars.getRequestGlobalVariable("inpDate", "Product.date");
      String windowId = vars.getRequestGlobalVariable("WindowID", "Product.windowId");
      vars.getGlobalVariable("inpWarehouse", "Product.warehouse", Utility.getContext(this, vars, "M_Warehouse_ID", windowId));
      vars.removeSessionValue("Product.key");
      if (!strNameValue.equals("")) vars.setSessionValue("Product.name", strNameValue + "%");
      if (strPriceList.equals("")) {
        strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID", windowId);
        if (strPriceList.equals("")) strPriceList = ProductData.priceListDefault(this, Utility.getContext(this, vars, "#User_Client", "Product"), Utility.getContext(this, vars, "#User_Org", "Product"));
        vars.setSessionValue("Product.priceList", strPriceList);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateOrdered", windowId);
        if (log4j.isDebugEnabled()) log4j.debug("DateOrdered:"+strDate);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateInvoiced", windowId);
        if (log4j.isDebugEnabled()) log4j.debug("DateInvoiced:"+strDate);
      }
      if (strDate.equals("")) strDate = DateTimeData.today(this);
      vars.setSessionValue("Product.date", strDate);

      vars.setSessionValue("Product.priceListVersion", getPriceListVersion(vars, strPriceList, strDate));

      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Product.key");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActualValue(this, vars.getLanguage(), strKeyValue, strIDValue);
        if (!strNameAux.equals("")) strKeyValue = strNameAux;
      }
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "Product.warehouse");
      String strPriceList = vars.getRequestGlobalVariable("inpPriceList", "Product.priceList");
      String strDate = vars.getRequestGlobalVariable("inpDate", "Product.date");
      String windowId = vars.getRequestGlobalVariable("WindowID", "Product.windowId");
      vars.removeSessionValue("Product.name");
      if (!strKeyValue.equals("")) vars.setSessionValue("Product.key", strKeyValue + "%");
      if (strWarehouse.equals("")) strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("Product.warehouse", strWarehouse);
      if (strPriceList.equals("")) {
        strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID", windowId);
        if (strPriceList.equals("")) strPriceList = ProductData.priceListDefault(this, Utility.getContext(this, vars, "#User_Client", "Product"), Utility.getContext(this, vars, "#User_Org", "Product"));
        vars.setSessionValue("Product.priceList", strPriceList);
      }
      
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateOrdered", windowId);
        if (log4j.isDebugEnabled()) log4j.debug("DateOrdered:"+strDate);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateInvoiced", windowId);
        if (log4j.isDebugEnabled()) log4j.debug("DateInvoiced:"+strDate);
      }
      vars.setSessionValue("Product.date", strDate);

      String strPriceListVersion = getPriceListVersion(vars, strPriceList, strDate);
      vars.setSessionValue("Product.priceListVersion", strPriceListVersion);
      ProductData[] data = ProductData.select(this, vars.getLanguage(), strWarehouse, strKeyValue + "%", "", strPriceListVersion, Utility.getContext(this, vars, "#User_Client", "Product"), Utility.getContext(this, vars, "#User_Org", "Product"));
      if (data!=null && data.length==1) printPageKey(response, vars, data, strWarehouse, strPriceListVersion);
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strKeyValue = vars.getGlobalVariable("inpKey", "Product.key", "");
      String strNameValue = vars.getGlobalVariable("inpName", "Product.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "Product.warehouse", "");
      String strPriceList = vars.getGlobalVariable("inpPriceList", "Product.priceList", "");
      String strPriceListVersion = vars.getGlobalVariable("inpPriceListVersion", "Product.priceListVersion", "");
      String windowId = vars.getGlobalVariable("WindowID", "Product.windowId", "");
      printPageFrame1(response, vars, strKeyValue, strNameValue, strWarehouse, strPriceList, strPriceListVersion, windowId);
    } else if (vars.commandIn("FRAME2")) {
      String strKey = vars.getGlobalVariable("inpKey", "Product.key", "");
      String strName = vars.getGlobalVariable("inpName", "Product.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "Product.warehouse", "");
      String strPriceListVersion = vars.getGlobalVariable("inpPriceListVersion", "Product.priceListVersion", "");
      printPageFrame2(response, vars, strKey, strName, strWarehouse, strPriceListVersion);
    } else if (vars.commandIn("FIND")) {
      String strKey = vars.getRequestGlobalVariable("inpKey", "Product.key");
      String strName = vars.getRequestGlobalVariable("inpName", "Product.name");
      String strWarehouse = vars.getRequiredGlobalVariable("inpWarehouse", "Product.warehouse");
      String strPriceListVersion = vars.getRequiredGlobalVariable("inpPriceListVersion", "Product.priceListVersion");

      vars.setSessionValue("Product.initRecordNumber", "0");

      printPageFrame2(response, vars, strKey, strName, strWarehouse, strPriceListVersion);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("Product.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Product");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("Product.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("Product.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("Product.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Product");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("Product.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }


  String getPriceListVersion(VariablesSecureApp vars, String strPriceList, String strDate) throws IOException,ServletException {
    PriceListVersionComboData[] data = PriceListVersionComboData.selectActual(this, strPriceList, strDate, Utility.getContext(this, vars, "#User_Client", "Product"));
    if (log4j.isDebugEnabled()) log4j.debug("Selecting pricelistversion date:"+strDate+" - pricelist:"+strPriceList);
    if (data==null || data.length==0) return "";
    return data[0].mPricelistVersionId;
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ProductData[] data, String strWarehouse, String strPriceListVersion) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data, strWarehouse, strPriceListVersion));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ProductData[] data, String strWarehouse, String strPriceListVersion) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].mProductId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUomId + "\"),\n");
    html.append("new SearchElements(\"_PSTD\", true, \"" + data[0].pricestd + "\"),\n");
    html.append("new SearchElements(\"_PLIM\", true, \"" + data[0].pricelimit + "\"),\n");
    html.append("new SearchElements(\"_CURR\", true, \"" + data[0].cCurrencyId + "\"),\n");
    html.append("new SearchElements(\"_PLIST\", true, \"" + data[0].pricelist + "\")\n");
    html.append(");\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto, parameter);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue, String strNameValue, String strWarehouse, String strPriceList, String strPriceListVersion, String windowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the products seeker");
    String[] discard=new String[1];
    if (windowId.equals("800004")) {
      discard[0] = new String("NotReducedSearch");
    } else {
      discard[0] = new String("ReducedSearch");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product_F1", discard).createXmlDocument();

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
    xmlDocument.setParameter("priceListVersion", strPriceListVersion);
    xmlDocument.setData("structure1", WarehouseComboData.selectFilter(this, Utility.getContext(this, vars, "#User_Client", "Product")));
    xmlDocument.setData("structure2", PriceListVersionComboData.select(this, strPriceList, Utility.getContext(this, vars, "#User_Client", "Product")));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strName, String strWarehouse, String strPriceListVersion) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the products seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Product");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("Product.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
    boolean hasPrevious = false, hasNext = false;

    if (strKey.equals("") && strName.equals("")) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", ProductData.set());
    } else {
      hasPrevious = hasNext = true;
      ProductData[] data = ProductData.select(this, vars.getLanguage(), strWarehouse, strKey, strName, strPriceListVersion, Utility.getContext(this, vars, "#User_Client", "Product"), Utility.getContext(this, vars, "#User_Org", "Product"), initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) hasPrevious = false;
      if (data==null || data.length==0 || data.length<intRecordRange) hasNext = false;
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product_F2").createXmlDocument();
      xmlDocument.setData("structure1", data);
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "Product_F2", false, "document.frmMain.inpClave", "", "", false, "info", strReplaceWith, false, true);
    toolbar.prepareInfoTemplate(hasPrevious, hasNext, vars.getSessionValue("#ShowTest", "N").equals("Y"));
    xmlDocument.setParameter("toolbar", toolbar.toString());
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
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the products seeker";
  } // end of getServletInfo() method
}
