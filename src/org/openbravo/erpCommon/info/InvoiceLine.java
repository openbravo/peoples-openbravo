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


public class InvoiceLine extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "InvoiceLine.name");
      vars.getRequestGlobalVariable("WindowID", "InvoiceLine.windowId");
      vars.getRequestGlobalVariable("inpProduct", "InvoiceLine.product");
      vars.getRequestGlobalVariable("inpBPartner", "InvoiceLine.bpartner");
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
              vars.setSessionValue("InvoiceLine.documentno", token);
              break;
            case 2: 
              vars.setSessionValue("InvoiceLine.datefrom", token);
              vars.setSessionValue("InvoiceLine.dateto", token);
              break;
            case 3: 
              vars.setSessionValue("InvoiceLine.grandtotalfrom", token);
              vars.setSessionValue("InvoiceLine.grandtotalto", token);
              break;
            case 4: 
              vars.setSessionValue("InvoiceLine.lineno", token);
              break;
            case 5: 
              vars.setSessionValue("InvoiceLine.linenet", token);
              break;
          }
          count++;
        } while (i!=-1);
      }
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "InvoiceLine.key");
      vars.getRequestGlobalVariable("WindowID", "InvoiceLine.windowId");

      vars.setSessionValue("InvoiceLine.documentno", strKeyValue + "%");
      InvoiceLineData[] data = null;
      
        
      data = InvoiceLineData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "InvoiceLine"), Utility.getContext(this, vars, "#User_Org", "InvoiceLine"), strKeyValue + "%");
      
      if (data!=null && data.length==1) printPageKey(response, vars, data);
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "InvoiceLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "InvoiceLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "InvoiceLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "InvoiceLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "InvoiceLine.dateto", "");
      String strCal1 = vars.getGlobalVariable("inpCal1", "InvoiceLine.grandtotalfrom", "");
      String strCal2 = vars.getGlobalVariable("inpCalc2", "InvoiceLine.grandtotalto", "");
      printPageFrame1(response, vars, strBPartner, strProduct, strDocumentNo, strDateFrom, strDateTo, strCal1, strCal2);
    } else if (vars.commandIn("FRAME2")) {
      String strBpartnerId = vars.getGlobalVariable("inpcBpartnerId", "InvoiceLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "InvoiceLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "InvoiceLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "InvoiceLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "InvoiceLine.dateto", "");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getGlobalVariable("inpCal1", "InvoiceLine.grandtotalfrom", "");
      String strCal2 = vars.getGlobalVariable("inpCalc2", "InvoiceLine.grandtotalto", "");
      String strOrder = vars.getStringParameter("inpInvoice");
      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strCal1, strCal2, strOrder, strProduct);
    } else if (vars.commandIn("FIND")) {
      String strBpartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "InvoiceLine.bpartner");
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "InvoiceLine.product");
      String strDocumentNo = vars.getRequestGlobalVariable("inpdocumentno", "InvoiceLine.documentno");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "InvoiceLine.datefrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "InvoiceLine.dateto");
      String strDescription = vars.getStringParameter("inpDescription");
      String strCal1 = vars.getRequestGlobalVariable("inpCal1", "InvoiceLine.grandtotalfrom");
      String strCal2 = vars.getRequestGlobalVariable("inpCalc2", "InvoiceLine.grandtotalto");
      String strOrder = vars.getStringParameter("inpInvoice");

      vars.setSessionValue("InvoiceLine.initRecordNumber", "0");

      printPageFrame2(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strCal1, strCal2, strOrder, strProduct);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("InvoiceLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "InvoiceLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("InvoiceLine.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("InvoiceLine.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("InvoiceLine.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "InvoiceLine");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("InvoiceLine.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: sale-order-lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/InvoiceLine_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, InvoiceLineData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: sale-order-lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(InvoiceLineData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    if (log4j.isDebugEnabled()) log4j.debug("Save- clave:"+data[0].cInvoicelineId+" txt:"+data[0].lineText);
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].cInvoicelineId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].lineText, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strProduct, String strDocumentNo, String strDateFrom, String strDateTo, String strCal1, String strCal2) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of sale-order-lines seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/InvoiceLine_F1").createXmlDocument();
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
    xmlDocument.setParameter("cBpartnerId_DES", InvoiceLineData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("mProductId", strProduct);
    xmlDocument.setParameter("mProductId_DES", InvoiceLineData.selectProduct(this, strProduct));
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDocumentNo, String strBpartnerId, String strDateFrom, String strDateTo, String strDescription, String strCal1, String strCalc2, String strOrder, String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the sale-order-lines seeker");
    XmlDocument xmlDocument;

    InvoiceLineData[] data = null;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "InvoiceLine");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("InvoiceLine.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strDocumentNo.equals("") && strBpartnerId.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strDescription.equals("") && strCal1.equals("") && strCalc2.equals("") && strOrder.equals("") && strProduct.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/InvoiceLine_F2", discard).createXmlDocument();
      data = InvoiceLineData.set();
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};

      data = InvoiceLineData.select(this, Utility.getContext(this, vars, "#User_Client", "InvoiceLine"), Utility.getContext(this, vars, "#User_Org", "InvoiceLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strCal1,  strCalc2, strProduct, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/InvoiceLine_F2", discard).createXmlDocument();
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
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/InvoiceLine_F3").createXmlDocument();
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
