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
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Expression;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.xmlEngine.XmlDocument;

public class AddPaymentFromTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final RequestFilter docTypeFilter = new ValueListFilter("RCIN", "PDOUT");
      final boolean isReceipt = vars.getRequiredStringParameter("inpDocumentType", docTypeFilter)
          .equals("RCIN");
      final String strFinancialAccountId = vars.getRequiredStringParameter(
          "inpFinFinancialAccountId", IsIDFilter.instance);
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      printPage(response, vars, strFinancialAccountId, isReceipt, strFinBankStatementLineId);

    } else if (vars.commandIn("GRIDLIST")) {
      final String strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "");
      final String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId",
          IsIDFilter.instance);
      final String strDueDateFrom = vars.getStringParameter("inpDueDateFrom", "");
      final String strDueDateTo = vars.getStringParameter("inpDueDateTo", "");
      final String strDocumentType = vars.getStringParameter("inpDocumentType", "");
      final String strSelectedPaymentDetails = vars.getInStringParameter(
          "inpScheduledPaymentDetailId", IsIDFilter.instance);
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");

      printGrid(response, vars, strFinancialAccountId, strBusinessPartnerId, strDueDateFrom,
          strDueDateTo, strDocumentType, strSelectedPaymentDetails, isReceipt);

    } else if (vars.commandIn("PAYMENTMETHODCOMBO")) {
      final String strBusinessPartnerId = vars.getRequestGlobalVariable("inpcBpartnerId", "");
      final String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId",
          IsIDFilter.instance);
      refreshPaymentMethod(response, strBusinessPartnerId, strFinancialAccountId);

    } else if (vars.commandIn("LOADCREDIT")) {
      final String strBusinessPartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      final boolean isReceipt = "Y".equals(vars.getRequiredStringParameter("isReceipt"));
      String customerCredit = dao.getCustomerCredit(
          OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId), isReceipt)
          .toString();
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.println(customerCredit);
      out.close();

    } else if (vars.commandIn("SAVE")) {
      String strAction = vars.getRequiredStringParameter("inpActionDocument");
      String strPaymentDocumentNo = vars.getRequiredStringParameter("inpDocNumber");
      String strReceivedFromId = vars.getRequiredStringParameter("inpcBpartnerId");
      String strPaymentMethodId = vars.getRequiredStringParameter("inpPaymentMethod");
      String strFinancialAccountId = vars.getRequiredStringParameter("inpFinancialAccountId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
      String strPaymentDate = vars.getRequiredStringParameter("inpPaymentDate");
      String strSelectedScheduledPaymentDetailIds = vars.getRequiredInParameter(
          "inpScheduledPaymentDetailId", IsIDFilter.instance);
      String strDifferenceAction = vars.getRequiredStringParameter("inpDifferenceAction");
      BigDecimal refundAmount = BigDecimal.ZERO;
      if (strDifferenceAction.equals("refund"))
        refundAmount = new BigDecimal(vars.getRequiredNumericParameter("inpDifference"));
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      String strReferenceNo = vars.getStringParameter("inpReferenceNo", "");
      OBError message = null;
      // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
      // removed when new security implementation is done
      OBContext.setAdminMode();
      try {

        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
            FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = FIN_AddPayment
            .getSelectedPaymentDetailsAndAmount(vars, selectedPaymentDetails);

        final List<Object> parameters = new ArrayList<Object>();
        parameters.add(vars.getClient());
        parameters.add(dao.getObject(FIN_FinancialAccount.class, strFinancialAccountId)
            .getOrganization().getId());
        parameters.add((isReceipt ? "ARR" : "APP"));
        // parameters.add(null);
        String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
            parameters, null);

        if (strPaymentDocumentNo.startsWith("<")) {
          // get DocumentNo
          strPaymentDocumentNo = Utility.getDocumentNo(this, vars, "AddPaymentFromTransaction",
              "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
        }

        FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt, dao.getObject(
            DocumentType.class, strDocTypeId), strPaymentDocumentNo, dao.getObject(
            BusinessPartner.class, strReceivedFromId), dao.getObject(FIN_PaymentMethod.class,
            strPaymentMethodId), dao.getObject(FIN_FinancialAccount.class, strFinancialAccountId),
            strPaymentAmount, FIN_Utility.getDate(strPaymentDate), dao.getObject(
                FIN_FinancialAccount.class, strFinancialAccountId).getOrganization(),
            strReferenceNo, selectedPaymentDetails, selectedPaymentDetailAmounts,
            strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"));

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {
            message = FIN_AddPayment.processPayment(vars, this,
                (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);

            // PPW: process made payment and withdrawal
            // PRD: process made payment and deposit
            if ((strAction.equals("PRD") || strAction.equals("PPW"))
                && !"Error".equals(message.getType())) {
              vars.setSessionValue("AddPaymentFromTransaction|closeAutomatically", "Y");
              vars.setSessionValue("AddPaymentFromTransaction|PaymentId", payment.getId());
            }
            if (strDifferenceAction.equals("refund")) {
              Boolean newPayment = !payment.getFINPaymentDetailList().isEmpty();
              FIN_Payment refundPayment = FIN_AddPayment.createRefundPayment(this, vars, payment,
                  refundAmount.negate());
              OBError auxMessage = FIN_AddPayment.processPayment(vars, this, (strAction
                  .equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
              if (newPayment) {
                final String strNewRefundPaymentMessage = Utility.parseTranslation(this, vars, vars
                    .getLanguage(), "@APRM_RefundPayment@" + ": " + refundPayment.getDocumentNo())
                    + ".";
                message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage());
                if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
                  payment.setDescription(payment.getDescription() + strNewRefundPaymentMessage
                      + "\n");
                  OBDal.getInstance().save(payment);
                  OBDal.getInstance().flush();
                }
              } else {
                message = auxMessage;
              }
            }
          } catch (Exception ex) {
            message = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            log4j.error(ex);
            if (!message.isConnectionAvailable()) {
              bdErrorConnection(response);
              return;
            }
          }
        }

      } finally {
        OBContext.restorePreviousMode();
      }

      log4j.debug("Output: PopUp Response");
      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/base/secureApp/PopUp_Close_Refresh").createXmlDocument();
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();

      // "../org.openbravo.advpaymentmngt.ad_actionbutton/AddTransaction.html");
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, boolean isReceipt, String strFinBankStatementLineId)
      throws IOException, ServletException {
    log4j.debug("Output: Add Payment button pressed on Add Transaction popup.");
    dao = new AdvPaymentMngtDao();
    String defaultPaymentMethod = "";

    final FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentFromTransaction")
        .createXmlDocument();

    if (!strFinBankStatementLineId.isEmpty()) {
      FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
          strFinBankStatementLineId);
      xmlDocument.setParameter("actualPayment", (isReceipt) ? bsline.getCramount().subtract(
          bsline.getDramount()).toString() : BigDecimal.ZERO.toString());
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
          "dateFormat.java");
      SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
      xmlDocument.setParameter("paymentDate", dateFormater.format(bsline.getTransactionDate()));
      if (bsline.getBusinessPartner() == null) {
        OBCriteria<BusinessPartner> obcBP = OBDal.getInstance().createCriteria(
            BusinessPartner.class);
        obcBP.add(Expression.eq(BusinessPartner.PROPERTY_NAME, bsline.getBpartnername()));
        if (obcBP.list() != null && obcBP.list().size() > 0) {
          xmlDocument.setParameter("businessPartner", obcBP.list().get(0).getId());
          defaultPaymentMethod = (obcBP.list().get(0).getPaymentMethod() != null) ? obcBP.list()
              .get(0).getPaymentMethod().getId() : "";
        }
      } else {
        xmlDocument.setParameter("businessPartner", bsline.getBusinessPartner().getId());
      }
    } else
      xmlDocument.setParameter("paymentDate", DateTimeData.today(this));

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("isReceipt", (isReceipt) ? "Y" : "N");
    xmlDocument.setParameter("finBankStatementLineId", strFinBankStatementLineId);

    // get DocumentNo
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(vars.getClient());
    parameters.add(financialAccount.getOrganization().getId());
    parameters.add((isReceipt ? "ARR" : "APP"));
    // parameters.add(null);
    String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
        parameters, null);
    String strDocNo = Utility.getDocumentNo(this, vars, "AddPaymentFromTransaction", "FIN_Payment",
        strDocTypeId, strDocTypeId, false, false);
    xmlDocument.setParameter("documentNumber", "<" + strDocNo + ">");
    xmlDocument.setParameter("documentType", dao.getObject(DocumentType.class, strDocTypeId)
        .getName());

    xmlDocument.setParameter("financialAccountId", strFinancialAccountId);
    xmlDocument.setParameter("financialAccount", financialAccount.getIdentifier());
    OBContext.setAdminMode();
    try {
      xmlDocument.setParameter("currency", financialAccount.getCurrency().getISOCode());
      xmlDocument.setParameter("precision", financialAccount.getCurrency().getStandardPrecision()
          .toString());
    } finally {
      OBContext.restorePreviousMode();
    }

    // Payment Method combobox
    String paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(defaultPaymentMethod,
        strFinancialAccountId, financialAccount.getOrganization().getId(), true, true);
    xmlDocument.setParameter("sectionDetailPaymentMethod", paymentMethodComboHtml);

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // Action Regarding Document
    xmlDocument.setParameter("ActionDocument", (isReceipt ? "PRD" : "PPW"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          (isReceipt ? "F903F726B41A49D3860243101CEEBA25" : "F15C13A199A748F1B0B00E985A64C036"),
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromTransaction"),
          Utility.getContext(this, vars, "#User_Client", "AddPaymentFromTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddPaymentFromTransaction", "");

      xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strBusinessPartnerId, String strDueDateFrom,
      String strDueDateTo, String strDocumentType, String strSelectedPaymentDetails,
      boolean isReceipt) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");

    dao = new AdvPaymentMngtDao();
    FIN_FinancialAccount financialAccount = dao.getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentGrid").createXmlDocument();

    // Pending Payments from invoice
    final List<FIN_PaymentScheduleDetail> invoiceScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    // selected scheduled payments list
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = FIN_AddPayment
        .getSelectedPaymentDetails(invoiceScheduledPaymentDetails, strSelectedPaymentDetails);

    List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = new ArrayList<FIN_PaymentScheduleDetail>();
    if (!"".equals(strBusinessPartnerId))
      // filtered scheduled payments list
      filteredScheduledPaymentDetails = dao.getFilteredScheduledPaymentDetails(financialAccount
          .getOrganization(), dao.getObject(BusinessPartner.class, strBusinessPartnerId),
          financialAccount.getCurrency(), FIN_Utility.getDate(strDueDateFrom), FIN_Utility
              .getDate(DateTimeData.nDaysAfter(this, strDueDateTo, "1")), strDocumentType, null,
          selectedScheduledPaymentDetails, isReceipt);

    final FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, false, null);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void refreshPaymentMethod(HttpServletResponse response, String strBusinessPartnerId,
      String strFinancialAccountId) throws IOException, ServletException {
    log4j.debug("Callout: Business Partner has changed to" + strBusinessPartnerId);

    String paymentMethodComboHtml = "";
    if (!strBusinessPartnerId.isEmpty()) {
      FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
          strFinancialAccountId);
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId);
      paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(
          (bp.getPaymentMethod() != null) ? bp.getPaymentMethod().getId() : null,
          strFinancialAccountId, account.getOrganization().getId(), true, true);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(paymentMethodComboHtml.replaceAll("\"", "\\'"));
    out.close();
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("finScheduledPaymentId", "");
    empty.put("salesOrderNr", "");
    empty.put("salesInvoiceNr", "");
    empty.put("dueDate", "");
    empty.put("invoicedAmount", "");
    empty.put("expectedAmount", "");
    empty.put("paymentAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  public String getServletInfo() {
    return "Servlet that presents the payment proposal";
    // end of getServletInfo() method
  }

}
