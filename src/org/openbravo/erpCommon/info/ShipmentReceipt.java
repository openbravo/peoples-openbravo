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
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;

import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;


public class ShipmentReceipt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceipt.name");
      if (!strNameValue.equals("")) vars.setSessionValue("ShipmentReceipt.name", strNameValue + "%");
      String windowId = vars.getStringParameter("WindowID");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      if (strSOTrx.equals("")) vars.setSessionValue("ShipmentReceipt.isSOTrx", "N");
      else vars.setSessionValue("ShipmentReceipt.isSOTrx", strSOTrx);
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceipt.name");
      vars.setSessionValue("ShipmentReceipt.name", strKeyValue + "%");
      String windowId = vars.getStringParameter("WindowID");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      if (strSOTrx.equals("")) strSOTrx = "N";
      vars.setSessionValue("ShipmentReceipt.isSOTrx", strSOTrx);
      ShipmentReceiptData[] data = ShipmentReceiptData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceipt"), strSOTrx, strKeyValue + "%");
      if (data!=null && data.length==1) {
        printPageKey(response, vars, data);
      } else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strNameValue = vars.getGlobalVariable("inpName", "ShipmentReceipt.name", "");
      String strSOTrx = vars.getSessionValue("ShipmentReceipt.isSOTrx");
      printPageFrame1(response, vars, strNameValue, strSOTrx);
    } else if (vars.commandIn("FRAME2")) {
      String strName = vars.getGlobalVariable("inpKey", "ShipmentReceipt.name", "");
      String strSalesTransaction = vars.getStringParameter("inpSalesTransaction");
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpFechaTo");
      String strDescription = vars.getStringParameter("inpDescription");
      String strOrderReference = vars.getStringParameter("inpOrderReference");
      printPageFrame2(response, vars, strName, strSalesTransaction, strBpartnerId, strDateFrom, strDateTo, strDescription, strOrderReference);
    } else if (vars.commandIn("FIND")) {
      String strName = vars.getGlobalVariable("inpKey", "ShipmentReceipt.name", "");
      String strSalesTransaction = vars.getStringParameter("inpSalesTransaction", "N");
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpFechaTo");
      String strDescription = vars.getStringParameter("inpDescription");
      String strOrderReference = vars.getStringParameter("inpOrderReference");

      vars.setSessionValue("ShipmentReceipt.initRecordNumber", "0");

      printPageFrame2(response, vars, strName, strSalesTransaction, strBpartnerId, strDateFrom, strDateTo, strDescription, strOrderReference);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ShipmentReceipt.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceipt");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ShipmentReceipt.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ShipmentReceipt.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ShipmentReceipt.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceipt");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ShipmentReceipt.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: business partners seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceipt_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ShipmentReceiptData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: invoices seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ShipmentReceiptData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].clave + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].documentno, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strNameValue, String strSOTrx) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the invoice seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceipt_F1").createXmlDocument();
    if (strNameValue.equals("")) {
    xmlDocument.setParameter("key", "%");
    } else {
      String substr;
      String white = " ";
      int index = strNameValue.indexOf(white, 0);
      substr = strNameValue.substring(0,index);
    xmlDocument.setParameter("key", substr);
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("salesTransaction", strSOTrx);
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strName, String strSalesTransaction, String strBpartnerId, String strDateFrom, String strDateTo, String strDescription, String strOrderReference) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the shipment seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ShipmentReceipt");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ShipmentReceipt.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strName.equals("") && strSalesTransaction.equals("") && strBpartnerId.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDescription.equals("") && strOrderReference.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceipt_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", ShipmentReceiptData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      ShipmentReceiptData[] data = ShipmentReceiptData.select(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceipt"), strName, strDescription, strBpartnerId, strOrderReference, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strSalesTransaction, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceipt_F2", discard).createXmlDocument();
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
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the business partners seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceipt_F3").createXmlDocument();
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
