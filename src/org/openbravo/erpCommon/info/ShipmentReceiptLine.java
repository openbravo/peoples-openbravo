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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
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


public class ShipmentReceiptLine extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceiptLine.name");
      String windowId = vars.getRequestGlobalVariable("WindowID", "ShipmentReceiptLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ShipmentReceiptLine.isSOTrx", strSOTrx);
      vars.getRequestGlobalVariable("inpProduct", "ShipmentReceiptLine.product");
      vars.getRequestGlobalVariable("inpBPartner", "ShipmentReceiptLine.bpartner");
      if (!strNameValue.equals("")) {
        int i=0, count=1, inicio=0;
        String search = " - ", token="";
        do {
          i = strNameValue.indexOf(search, inicio);
          if (i>=0) {
            token = strNameValue.substring(inicio, i);
            inicio = i + search.length();
          } else {
            token = strNameValue.substring(inicio);
          }

          switch (count) {
            case 1: 
              vars.setSessionValue("ShipmentReceiptLine.line", token.trim());
              break;
            case 2: 
              vars.setSessionValue("ShipmentReceiptLine.movementqty", token.trim());
              break;
            case 3: 
              vars.setSessionValue("ShipmentReceiptLine.documentno", token.trim());
              break;
            case 4: 
              vars.setSessionValue("ShipmentReceiptLine.datefrom", token.trim());
              vars.setSessionValue("ShipmentReceiptLine.dateto", token.trim());
              break;
            case 5: 
              String ID = ShipmentReceiptLineData.getProductID(this, token);
              if (!ID.equals("")) vars.setSessionValue("ShipmentReceiptLine.product", ID);
              break;
          }
          count++;
        } while (i!=-1);
      }
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceiptLine.key");
      String windowId = vars.getRequestGlobalVariable("WindowID", "ShipmentReceiptLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ShipmentReceiptLine.isSOTrx", strSOTrx);
      vars.setSessionValue("ShipmentReceiptLine.documentno", strKeyValue + "%");
      ShipmentReceiptLineData[] data = null;
      if (strSOTrx.equals("Y")) data = ShipmentReceiptLineData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strKeyValue + "%");
      else data = ShipmentReceiptLineData.selectKeySOTrx(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strKeyValue + "%");
      if (data!=null && data.length==1) printPageKey(response, vars, data);
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "ShipmentReceiptLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "ShipmentReceiptLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "ShipmentReceiptLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ShipmentReceiptLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ShipmentReceiptLine.dateto", "");
      printPageFrame1(response, vars, strBPartner, strProduct, strDocumentNo, strDateFrom, strDateTo);
    } else if (vars.commandIn("FRAME2")) {
      String strBpartnerId = vars.getGlobalVariable("inpcBpartnerId", "ShipmentReceiptLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "ShipmentReceiptLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "ShipmentReceiptLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ShipmentReceiptLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ShipmentReceiptLine.dateto", "");
      String strDescription = vars.getStringParameter("inpDescription");
      String strOrder = vars.getStringParameter("inpOrder");
      String strInvoiced = vars.getStringParameter("inpinvoiced", "N");
      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strOrder, strProduct, strInvoiced);
    } else if (vars.commandIn("FIND")) {
      String strBpartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "ShipmentReceiptLine.bpartner");
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "ShipmentReceiptLine.product");
      String strDocumentNo = vars.getRequestGlobalVariable("inpdocumentno", "ShipmentReceiptLine.documentno");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ShipmentReceiptLine.datefrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ShipmentReceiptLine.dateto");
      String strDescription = vars.getStringParameter("inpDescription");
      String strOrder = vars.getStringParameter("inpOrder");
      String strInvoiced = vars.getStringParameter("inpinvoiced", "N");

      vars.setSessionValue("ShipmentReceiptLine.initRecordNumber", "0");

      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strOrder, strProduct, strInvoiced);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ShipmentReceiptLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceiptLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ShipmentReceiptLine.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ShipmentReceiptLine.initRecordNumber", strInitRecord);
      }

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ShipmentReceiptLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceiptLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ShipmentReceiptLine.initRecordNumber", strInitRecord);

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: delivery note lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ShipmentReceiptLineData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: delivery note lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ShipmentReceiptLineData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].mInoutlineId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].lineText, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strProduct, String strDocumentNo, String strDateFrom, String strDateTo) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the delivery lines seekern");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine_F1").createXmlDocument();
    if (strBPartner.equals("") && strProduct.equals("") && strDocumentNo.equals("") && strDateFrom.equals("") && strDateTo.equals("")) {
      strDocumentNo = "%";
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("documentno", strDocumentNo);
    xmlDocument.setParameter("datefrom", strDateFrom);
    xmlDocument.setParameter("dateto", strDateTo);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("cBpartnerId_DES", ShipmentReceiptLineData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("mProductId", strProduct);
    xmlDocument.setParameter("mProductId_DES", ShipmentReceiptLineData.selectProduct(this, strProduct));
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDocumentNo, String strBpartnerId, String strDateFrom, String strDateTo, String strDescription, String strOrder, String strProduct, String strInvoiced) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the delivery note lines seeker");
    XmlDocument xmlDocument;
    String strSOTrx = vars.getSessionValue("ShipmentReceiptLine.isSOTrx");
    ShipmentReceiptLineData[] data = null;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceiptLine");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ShipmentReceiptLine.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strDocumentNo.equals("") && strBpartnerId.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDescription.equals("") && strOrder.equals("") && strProduct.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine_F2", discard).createXmlDocument();
      data = ShipmentReceiptLineData.set();
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      if (strSOTrx.equals("Y")) {
        data = ShipmentReceiptLineData.select(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, strInvoiced, initRecordNumber, intRecordRange);
      } else {
        data = ShipmentReceiptLineData.selectSOTrx(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, (strInvoiced.equals("Y")?"=":"<>"), initRecordNumber, intRecordRange);
      }
      if (data==null || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine_F2", discard).createXmlDocument();
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
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the delivery note lines seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the delivery-note lines seeker";
  } // end of getServletInfo() method
}
