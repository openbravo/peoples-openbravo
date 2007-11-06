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
import org.openbravo.erpCommon.utility.ComboTableData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class DebtPayment extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      printPageFrame1(response, vars);
    } else if (vars.commandIn("FRAME2")) {
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strCal1 = vars.getStringParameter("inpCal1");
      String strCalc2 = vars.getStringParameter("inpCal2");
      String strPaymentRule = vars.getStringParameter("inpCPaymentRuleId");
      String strIsReceipt = vars.getStringParameter("inpIsReceipt", "N");
      String strIsPaid = vars.getStringParameter("inpIsPaid", "N");
      String strIsPending = vars.getStringParameter("inpPending");
      String strInvoice = vars.getStringParameter("inpInvoice");
      String strOrder = vars.getStringParameter("inpOrder");
      printPageFrame2(response, vars, strBpartnerId, strDateFrom, strDateTo, strCal1, strCalc2, strPaymentRule, strIsReceipt, strIsPaid, strIsPending, strOrder, strInvoice);
    } else if (vars.commandIn("FIND")) {
      String strBpartnerId = vars.getStringParameter("inpBpartnerId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strCal1 = vars.getStringParameter("inpCal1");
      String strCalc2 = vars.getStringParameter("inpCal2");
      String strPaymentRule = vars.getStringParameter("inpCPaymentRuleId");
      String strIsReceipt = vars.getStringParameter("inpIsReceipt", "N");
      String strIsPaid = vars.getStringParameter("inpIsPaid", "N");
      String strIsPending = vars.getStringParameter("inpPending");
      String strInvoice = vars.getStringParameter("inpInvoice");
      String strOrder = vars.getStringParameter("inpOrder");
      
      vars.setSessionValue("DebtPayment.initRecordNumber", "0");

      printPageFrame2(response, vars, strBpartnerId, strDateFrom, strDateTo, strCal1, strCalc2, strPaymentRule, strIsReceipt, strIsPaid, strIsPending,strOrder, strInvoice);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("DebtPayment.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "DebtPayment");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("DebtPayment.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("DebtPayment.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("DebtPayment.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "DebtPayment");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("DebtPayment.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: DebtPayments seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the DebtPayments seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment_F1").createXmlDocument();
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", "DebtPayment"), Utility.getContext(this, vars, "#User_Client", "DebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "DebtPayment", "");
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strBpartnerId, String strDateFrom, String strDateTo, String strCal1, String strCalc2, String strPaymentRule, String strIsReceipt, String strIsPaid, String strIsPending, String strOrder, String strInvoice) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the DebtPayments seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "DebtPayment");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("DebtPayment.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    DebtPaymentData[] data = null;
    if (vars.commandIn("DEFAULT") && strBpartnerId.equals("") && strDateFrom.equals("") && strDateTo.equals("") && strCal1.equals("") && strCalc2.equals("")&& strPaymentRule.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", DebtPaymentData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      data = DebtPaymentData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "DebtPayment"), Utility.getContext(this, vars, "#User_Org", "DebtPayment"), strBpartnerId,  strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strCal1, strCalc2, strPaymentRule, strIsPaid, strIsReceipt, strIsPending, strInvoice, strOrder, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment_F2", discard).createXmlDocument();
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the DebtPayments seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que DebtPayments seeker";
  } // end of getServletInfo() method
}
