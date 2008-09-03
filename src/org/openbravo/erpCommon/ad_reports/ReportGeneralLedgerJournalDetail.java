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
import org.openbravo.erpCommon.businessUtility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ReportGeneralLedgerJournalDetail extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strFactAcctGroupId = vars.getStringParameter("inpFactAcctGroupId");
      printPageDataSheet(response, vars, strFactAcctGroupId, "",null, "");
    } else if (vars.commandIn("DIRECT")) {
      String strFactAcctGroupId = vars.getStringParameter("inpFactAcctGroupId");
      String strDateAcct = getValue(strFactAcctGroupId,0);
      strFactAcctGroupId = getValue(strFactAcctGroupId,1);
      printPageDataSheet(response, vars, strFactAcctGroupId, strDateAcct, null, "");
    } else if (vars.commandIn("DP")) {
      String strDPId = vars.getStringParameter("inpDPid");
      String strcAcctSchemaId = getValue(strDPId,0);
      strDPId = getValue(strDPId,1);
      printPageDataSheet(response, vars, null, null, strDPId, strcAcctSchemaId);     
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedgerJournalDetail");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedgerJournalDetail");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strFactAcctGroupId, String strDateacct, String strDPId, String strcAcctSchemaId) throws IOException, ServletException {
    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedgerJournalDetail");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournalDetail.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportGeneralLedgerJournalDetailData[] data=null;
    //  String[] discard = {"withoutPrevious", "withoutNext"};

    if (strDPId==null) 
      data = ReportGeneralLedgerJournalDetailData.select(this, strFactAcctGroupId, strDateacct, initRecordNumber, intRecordRange);
    else
      data = ReportGeneralLedgerJournalDetailData.selectByDP(this, strDPId, strcAcctSchemaId);


    // if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
    // if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");

    boolean hasPrevious = !(data==null || data.length==0 || initRecordNumber<=1);
    boolean hasNext     = !(data==null || data.length==0 || data.length<intRecordRange);
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGeneralLedger", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareRelationBarTemplate(hasPrevious, hasNext);
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerJournalDetail").createXmlDocument();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportGeneralLedgerJournalDetail");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGeneralLedgerJournalDetail.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGeneralLedgerJournalDetail.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGeneralLedgerJournalDetail");
      vars.removeMessage("ReportGeneralLedgerJournalDetail");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getValue(String strText, int index) {
    log4j.warn("***************strText: " + strText);
    String [] tokens = strText.split("/");
    log4j.warn("***************size: " + tokens.length);
    return tokens[index];
  } // end of getServletInfo() method


  public String getServletInfo() {
    return "Servlet ReportGeneralLedgerJournalDetail.";
  } // end of getServletInfo() method
}

