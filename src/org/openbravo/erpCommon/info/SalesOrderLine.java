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
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;

import org.openbravo.erpCommon.utility.DateTimeData;


public class SalesOrderLine extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "SalesOrderLine.name");
      String windowId = vars.getRequestGlobalVariable("WindowID", "SalesOrderLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("SalesOrderLine.isSOTrx", strSOTrx);
      vars.getRequestGlobalVariable("inpmProductId", "SalesOrderLine.product");
      vars.getRequestGlobalVariable("inpcBpartnerId", "SalesOrderLine.bpartner");
      if (!strNameValue.equals("")) {
        int i=0, count=1, inicio=0;
        String search = " - ", token="";
        do {
          i = strNameValue.indexOf(search, inicio);
          if (i>=0) {
            token = strNameValue.substring(inicio, i).trim();
            inicio = i + search.length();
          } else {
            token = strNameValue.substring(inicio).trim();
          }

          switch (count) {
            case 1: 
              vars.setSessionValue("SalesOrderLine.documentno", token);
              break;
            case 2: 
              vars.setSessionValue("SalesOrderLine.datefrom", token);
              vars.setSessionValue("SalesOrderLine.dateto", token);
              break;
            case 3: 
              vars.setSessionValue("SalesOrderLine.grandtotalfrom", token);
              vars.setSessionValue("SalesOrderLine.grandtotalto", token);
              break;
            case 4: 
              vars.setSessionValue("SalesOrderLine.lineno", token);
              break;
            case 5: 
              vars.setSessionValue("SalesOrderLine.linenet", token);
              break;
          }
          count++;
        } while (i!=-1);
      }
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "SalesOrderLine.key");
      String windowId = vars.getRequestGlobalVariable("WindowID", "SalesOrderLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("SalesOrderLine.isSOTrx", strSOTrx);
      vars.setSessionValue("SalesOrderLine.documentno", strKeyValue + "%");
      SalesOrderLineData[] data = null;
      if (strSOTrx.equals("Y")) data = SalesOrderLineData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"), Utility.getContext(this, vars, "#User_Org", "SalesOrderLine"), strKeyValue + "%");
      else data = SalesOrderLineData.selectKeySOTrx(this, Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"), Utility.getContext(this, vars, "#User_Org", "SalesOrderLine"), strKeyValue + "%");
      if (data!=null && data.length==1) printPageKey(response, vars, data);
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "SalesOrderLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "SalesOrderLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "SalesOrderLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "SalesOrderLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "SalesOrderLine.dateto", "");
      String strCal1 = vars.getGlobalVariable("inpCal1", "SalesOrderLine.grandtotalfrom", "");
      String strCal2 = vars.getGlobalVariable("inpCalc2", "SalesOrderLine.grandtotalto", "");
      printPageFrame1(response, vars, strBPartner, strProduct, strDocumentNo, strDateFrom, strDateTo, strCal1, strCal2);
    } else if (vars.commandIn("FRAME2")) {
      String strBpartnerId = vars.getGlobalVariable("inpcBpartnerId", "SalesOrderLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "SalesOrderLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "SalesOrderLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "SalesOrderLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "SalesOrderLine.dateto", "");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getGlobalVariable("inpCal1", "SalesOrderLine.grandtotalfrom", "");
      String strCal2 = vars.getGlobalVariable("inpCalc2", "SalesOrderLine.grandtotalto", "");
      String strOrder = vars.getStringParameter("inpOrder");
      String strDelivered = vars.getStringParameter("inpdelivered", "N");
      String strInvoiced = vars.getStringParameter("inpinvoiced", "N");
      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strCal1, strCal2, strOrder, strProduct, strDelivered, strInvoiced);
    } else if (vars.commandIn("FIND")) {
      String strBpartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "SalesOrderLine.bpartner");
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "SalesOrderLine.product");
      String strDocumentNo = vars.getRequestGlobalVariable("inpdocumentno", "SalesOrderLine.documentno");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "SalesOrderLine.datefrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "SalesOrderLine.dateto");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getRequestGlobalVariable("inpCal1", "SalesOrderLine.grandtotalfrom");
      String strCal2 = vars.getRequestGlobalVariable("inpCalc2", "SalesOrderLine.grandtotalto");
      String strOrder = vars.getStringParameter("inpOrder");
      String strDelivered = vars.getStringParameter("inpdelivered", "N");
      String strInvoiced = vars.getStringParameter("inpinvoiced", "N");

      vars.setSessionValue("SalesOrderLine.initRecordNumber", "0");

      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strCal1, strCal2, strOrder, strProduct, strDelivered, strInvoiced);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("SalesOrderLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "SalesOrderLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("SalesOrderLine.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("SalesOrderLine.initRecordNumber", strInitRecord);
      }

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("SalesOrderLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "SalesOrderLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("SalesOrderLine.initRecordNumber", strInitRecord);

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: sale-order-lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SalesOrderLine_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, SalesOrderLineData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: sale-order-lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(SalesOrderLineData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].cOrderlineId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].lineText, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strProduct, String strDocumentNo, String strDateFrom, String strDateTo, String strCal1, String strCal2) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of sale-order-lines seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SalesOrderLine_F1").createXmlDocument();
    if (strBPartner.equals("") && strProduct.equals("") && strDocumentNo.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strCal1.equals("") && strCal2.equals("")) {
      strDocumentNo = "%";
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("documentno", strDocumentNo);
    xmlDocument.setParameter("datefrom", strDateFrom);
    xmlDocument.setParameter("dateto", strDateTo);
    xmlDocument.setParameter("grandtotalfrom", strCal1);
    xmlDocument.setParameter("grandtotalto", strCal2);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("cBpartnerId_DES", SalesOrderLineData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("mProductId", strProduct);
    xmlDocument.setParameter("mProductId_DES", SalesOrderLineData.selectProduct(this, strProduct));
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDocumentNo, String strBpartnerId, String strDateFrom, String strDateTo, String strDescription, String strCal1, String strCalc2, String strOrder, String strProduct, String strDelivered, String strInvoiced) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the sale-order-lines seeker");
    XmlDocument xmlDocument;
    String strSOTrx = vars.getSessionValue("SalesOrderLine.isSOTrx");
    SalesOrderLineData[] data = null;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "SalesOrderLine");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("SalesOrderLine.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strDocumentNo.equals("") && strBpartnerId.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDescription.equals("") && strCal1.equals("") && strCalc2.equals("") && strOrder.equals("") && strProduct.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SalesOrderLine_F2", discard).createXmlDocument();
      data = SalesOrderLineData.set();
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      if (strSOTrx.equals("Y")) {
        data = SalesOrderLineData.select(this, Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"), Utility.getContext(this, vars, "#User_Org", "SalesOrderLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strCal1,  strCalc2, strProduct, (strDelivered.equals("Y")?"isdelivered":""), (strInvoiced.equals("Y")?"isinvoiced":""), initRecordNumber, intRecordRange);
      } else {
        data = SalesOrderLineData.selectSOTrx(this, Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"), Utility.getContext(this, vars, "#User_Org", "SalesOrderLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strCal1,  strCalc2, strProduct, (strDelivered.equals("Y")?"isdelivered":""), (strInvoiced.equals("Y")?"isinvoiced":""), initRecordNumber, intRecordRange);
      }
      if (data==null || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SalesOrderLine_F2", discard).createXmlDocument();
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the sale-order-lines seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SalesOrderLine_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que sale-orders lines seeker";
  } // end of getServletInfo() method
}
