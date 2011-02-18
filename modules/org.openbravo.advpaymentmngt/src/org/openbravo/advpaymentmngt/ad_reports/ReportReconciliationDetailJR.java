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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.ad_reports;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.Utility;

public class ReportReconciliationDetailJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strFinReconciliationID = vars.getGlobalVariable("inpfinReconciliationId", "");
      String strFinFinancialAccountName = vars.getGlobalVariable("inpfinFinancialAccountId_R", "");
      String strDateTo = vars.getGlobalVariable("inpdateto", "");
      String reportTitle = "Reconciliation Report";
      printPageDataPDF(request, response, vars, strFinReconciliationID, strFinFinancialAccountName,
          strDateTo, reportTitle);
    }
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strFinReconciliationId, String strFinFinancialAccountName,
      String strDateTo, String reportTitle) throws IOException, ServletException {
    response.setContentType("text/html; charset=UTF-8");
    dao = new AdvPaymentMngtDao();
    FieldProvider[] data = null;

    log4j.debug("Output: PDF report");
    data = dao.getReconciliationDetailReport(vars, strDateTo, strFinReconciliationId);

    if (data == null || data.length == 0) {
      advisePopUp(request, response, "WARNING", Utility.messageBD(this, "NoDataFound", vars
          .getLanguage()));
    } else {

      String strLanguage = vars.getLanguage();

      String strBaseDesign = getBaseDesignPath(strLanguage);
      JasperReport subRptOutPmtJRP;
      JasperReport subRptOutDepJRP;
      JasperReport subRptUnRecJRP;
      JasperReport subRptCloBalJRP;
      try {
        subRptOutPmtJRP = Utility.getTranslatedJasperReport(this, strBaseDesign
            + "/org/openbravo/advpaymentmngt/ad_reports/OutstandingPayment.jrxml", vars
            .getLanguage(), strBaseDesign);

        subRptOutDepJRP = Utility.getTranslatedJasperReport(this, strBaseDesign
            + "/org/openbravo/advpaymentmngt/ad_reports/OutstandingDeposit.jrxml", vars
            .getLanguage(), strBaseDesign);

        subRptUnRecJRP = Utility.getTranslatedJasperReport(this, strBaseDesign
            + "/org/openbravo/advpaymentmngt/ad_reports/UnReconciledBankStmt.jrxml", vars
            .getLanguage(), strBaseDesign);

        subRptCloBalJRP = Utility.getTranslatedJasperReport(this, strBaseDesign
            + "/org/openbravo/advpaymentmngt/ad_reports/AdjustedAccountBalance.jrxml", vars
            .getLanguage(), strBaseDesign);

      } catch (JRException e) {
        log4j.error("Could not load/compile jrxml-file", e);
        throw new ServletException(e);
      }

      String strMainReportName = "@basedesign@/org/openbravo/advpaymentmngt/ad_reports/ReportReconciliationDetailPDF.jrxml";
      HashMap<String, Object> parameters = new HashMap<String, Object>();

      parameters.put("Title", reportTitle);
      StringBuilder strSubTitle = new StringBuilder();
      strSubTitle.append(Utility.messageBD(this, "per", strLanguage) + " " + strDateTo + "\n");
      strSubTitle.append(Utility.messageBD(this, "for", strLanguage) + " "
          + strFinFinancialAccountName + "\n");
      parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
      parameters.put("SubRptOutPmt", subRptOutPmtJRP);
      parameters.put("SubRptOutDep", subRptOutDepJRP);
      parameters.put("SubRptUnRecon", subRptUnRecJRP);
      parameters.put("SubRptCloBal", subRptCloBalJRP);

      renderJR(vars, response, strMainReportName, "pdf", parameters, data, null);
    }
  }
}
