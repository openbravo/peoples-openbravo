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


public class Invoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Invoice.name");
      String strWindowId = vars.getRequestGlobalVariable("WindowID", "Invoice.windowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      if (!strWindowId.equals("")) {
        vars.setSessionValue("Invoice.isSOTrx", (strSOTrx.equals("")?"N":strSOTrx));
      }
      if (!strNameValue.equals("")) vars.setSessionValue("Invoice.name", strNameValue + "%");
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Invoice.name");
      String strWindowId = vars.getRequestGlobalVariable("WindowID", "Invoice.windowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      if (!strWindowId.equals("")) {
        vars.setSessionValue("Invoice.isSOTrx", (strSOTrx.equals("")?"N":strSOTrx));
      }
      vars.setSessionValue("Invoice.name", strKeyValue + "%");
      InvoiceData[] data = InvoiceData.selectKey(this, vars.getSqlDateFormat(), Utility.getContext(this, vars, "#User_Client", "Invoice"), Utility.getContext(this, vars, "#User_Org", "Invoice"), strSOTrx, strKeyValue + "%");
      if (data!=null && data.length==1) {
        printPageKey(response, vars, data);
      } else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strNameValue = vars.getGlobalVariable("inpName", "Invoice.name", "");
      String strWindow = vars.getGlobalVariable("inpwindowId", "Invoice.windowId", "");
      printPageFrame1(response, vars, strNameValue, strWindow);
    } else if (vars.commandIn("FRAME2")) {
      String strName = vars.getGlobalVariable("inpKey", "Invoice.name", "");
      String strPaid = vars.getStringParameter("inpPaid");
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strFechaTo = vars.getStringParameter("inpFechaTo");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getStringParameter("inpCal1");
      String strCalc2 = vars.getStringParameter("inpCalc2");
      String strOrder = vars.getStringParameter("inpOrder");
      String strSOTrx = vars.getStringParameter("inpisSOTrx");
      printPageFrame2(response, vars, strName, strPaid, strBpartnerId, strDateFrom, strFechaTo, strDescription, strCal1, strCalc2, strOrder, strSOTrx);
    } else if (vars.commandIn("FIND")) {
      String strName = vars.getGlobalVariable("inpKey", "Invoice.name", "");
      String strPaid = vars.getStringParameter("inpPaid");
      if (strPaid.equals(""))strPaid = "N";
      if (strPaid.equals("-1"))strPaid = "Y";
      if (log4j.isDebugEnabled()) log4j.debug("the value of inpPaid is: "+strPaid);
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strFechaTo = vars.getStringParameter("inpFechaTo");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getStringParameter("inpCal1");
      String strCalc2 = vars.getStringParameter("inpCalc2");
      String strOrder = vars.getStringParameter("inpOrder");
      String strSOTrx = vars.getStringParameter("inpisSOTrx");

      vars.setSessionValue("Invoice.initRecordNumber", "0");

      printPageFrame2(response, vars, strName, strPaid, strBpartnerId, strDateFrom, strFechaTo, strDescription, strCal1, strCalc2, strOrder, strSOTrx);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("Invoice.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Invoice");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("Invoice.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("Invoice.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("Invoice.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Invoice");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("Invoice.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: business partners seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, InvoiceData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Invoice seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(InvoiceData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].cInvoiceId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strNameValue, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of invoices seeker");
    String strSOTrx = vars.getSessionValue("Invoice.isSOTrx");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice_F1").createXmlDocument();
    if (strNameValue.equals("")) {
    xmlDocument.setParameter("key", "%");
    } else {
/*      String substr;
      String white = " ";
      int index = strNameValue.indexOf(white, 0);
      substr = strNameValue.substring(0,index);
    xmlDocument.setParameter("key", substr);
*/
      xmlDocument.setParameter("key", strNameValue);
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("isSOTrxCompra", strSOTrx);
    xmlDocument.setParameter("isSOTrxVenta", strSOTrx);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    StringBuffer total = new StringBuffer();
    total.append("arrTeclas = new Array(\n");
    total.append("new Teclas(\"ENTER\", \"openSearch(null, null, '../Invoice_FS.html', 'SELECTOR_INVOICE', false, 'frmMain', 'inpNewcInvoiceId', 'inpNewcInvoiceId_DES', document.frmMain.inpNewcInvoiceId_DES.value, 'Command', 'KEY', 'WindowID', '");
    total.append(strWindow).append("');\", \"inpNewcInvoiceId_DES\", \"null\")\n");
    total.append(");\n");
    total.append("activarControlTeclas();\n");
    xmlDocument.setParameter("WindowIDArray", total.toString());
    xmlDocument.setParameter("WindowID", strWindow);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strName, String strPaid, String strBpartnerId, String strDateFrom, String strFechaTo, String strDescription, String strCal1, String strCalc2, String strOrder, String strSOTrx) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the invoice seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Invoice");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("Invoice.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strName.equals("") && strPaid.equals("") && strBpartnerId.equals("") && strDateFrom.equals("") && strFechaTo.equals("") && strDescription.equals("") && strCal1.equals("") && strCalc2.equals("") && strOrder.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", InvoiceData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      InvoiceData[] data = InvoiceData.select(this, vars.getSqlDateFormat(), Utility.getContext(this, vars, "#User_Client", "Invoice"), Utility.getContext(this, vars, "#User_Org", "Invoice"), strName, strDescription, strBpartnerId, strOrder, strDateFrom, DateTimeData.nDaysAfter(this,strFechaTo, "1"), strCal1,  strCalc2, strSOTrx, strPaid, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice_F2", discard).createXmlDocument();
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
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 iof the business partners seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the business partners seeker";
  } // end of getServletInfo() method
}
