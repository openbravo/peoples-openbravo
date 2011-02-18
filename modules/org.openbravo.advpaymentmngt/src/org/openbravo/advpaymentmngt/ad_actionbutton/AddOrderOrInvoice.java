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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.xmlEngine.XmlDocument;

public class AddOrderOrInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N", "");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    dao = new AdvPaymentMngtDao();

    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getGlobalVariable("inpwindowId", "AddOrderOrInvoice|Window_ID");
      String strTabId = vars.getGlobalVariable("inpTabId", "AddOrderOrInvoice|Tab_ID");
      String strPaymentId = vars.getGlobalVariable("inpfinPaymentId", strWindowId + "|"
          + "FIN_Payment_ID");

      printPage(response, vars, strPaymentId, strWindowId, strTabId);

    } else if (vars.commandIn("GRIDLIST")) {
      String strBusinessPartnerId = vars.getRequestGlobalVariable("inpBusinessPartnerId", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");
      String strDueDateFrom = vars.getStringParameter("inpDueDateFrom", "");
      String strDueDateTo = vars.getStringParameter("inpDueDateTo", "");
      String strDocumentType = vars.getStringParameter("inpDocumentType", "");
      String strSelectedPaymentDetails = vars.getInStringParameter("inpScheduledPaymentDetailId",
          IsIDFilter.instance);
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      Boolean showAlternativePM = "Y".equals(vars.getStringParameter("inpAlternativePaymentMethod",
          filterYesNo));

      printGrid(response, vars, strBusinessPartnerId, strPaymentId, strOrgId, strDueDateFrom,
          strDueDateTo, strDocumentType, strSelectedPaymentDetails, isReceipt, showAlternativePM);
    } else if (vars.commandIn("GLITEMGRIDLIST")) {
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");

      printGLItemGrid(response, strPaymentId);

    } else if (vars.commandIn("ADDGLITEM")) {
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");
      String strGLItemId = vars.getRequestGlobalVariable("inpcGlitemId", "");
      String strGLItemAmount = vars.getRequiredNumericParameter("inpGLItemAmount", "0");

      String errorMessage = "";
      try {
        FIN_AddPayment.saveGLItem(dao.getObject(FIN_Payment.class, strPaymentId), new BigDecimal(
            strGLItemAmount), dao.getObject(GLItem.class, strGLItemId));
      } catch (Exception e) {
        errorMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage())
            .getMessage();
        log4j.error(e);
      }
      printGLItem(response, errorMessage);
    } else if (vars.commandIn("REMOVEGLITEM")) {
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");
      String strPaymentDetailId = vars.getRequestGlobalVariable("inpDeleteGLItem", "");

      String errorMessage = "";
      try {
        FIN_AddPayment.removeGLItem(dao.getObject(FIN_Payment.class, strPaymentId), dao.getObject(
            FIN_PaymentDetail.class, strPaymentDetailId));
      } catch (Exception e) {
        errorMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage())
            .getMessage();
        log4j.error(e);
      }
      printGLItem(response, errorMessage);
    } else if (vars.commandIn("REMOVEALLGLITEM")) {
      String strPaymentId = vars.getRequestGlobalVariable("inpfinPaymentId", "");

      String errorMessage = "";
      try {
        FIN_AddPayment.removeGLItem(dao.getObject(FIN_Payment.class, strPaymentId), null);
      } catch (Exception e) {
        errorMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage())
            .getMessage();
        log4j.error(e);
      }
      printGLItem(response, errorMessage);

    } else if (vars.commandIn("SAVE")) {
      String strAction = vars.getRequiredStringParameter("inpActionDocument");
      String strPaymentId = vars.getRequiredStringParameter("inpfinPaymentId");
      String strSelectedScheduledPaymentDetailIds = vars.getInParameter(
          "inpScheduledPaymentDetailId", "", IsIDFilter.instance);
      String strDifferenceAction = vars.getRequiredStringParameter("inpDifferenceAction");
      BigDecimal refundAmount = BigDecimal.ZERO;
      if (strDifferenceAction.equals("refund"))
        refundAmount = new BigDecimal(vars.getRequiredNumericParameter("inpDifference"));
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strPaymentAmount = vars.getRequiredNumericParameter("inpActualPayment");
      boolean isReceipt = vars.getRequiredStringParameter("isReceipt").equals("Y");
      OBError message = null;
      // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
      // removed when new security implementation is done
      OBContext.setAdminMode();
      try {

        List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
            FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
        HashMap<String, BigDecimal> selectedPaymentDetailAmounts = FIN_AddPayment
            .getSelectedPaymentDetailsAndAmount(vars, selectedPaymentDetails);

        FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);
        BigDecimal newPaymentAmount = new BigDecimal(strPaymentAmount);
        if (newPaymentAmount.compareTo(payment.getAmount()) != 0)
          payment.setAmount(newPaymentAmount);
        payment = FIN_AddPayment.savePayment(payment, isReceipt, null, null, null, null, null,
            null, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
            strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"));

        if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
            || strAction.equals("PPW")) {
          try {
            // If Action PRP o PPW, Process payment but as well create a financial transaction
            message = FIN_AddPayment.processPayment(vars, this,
                (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
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

      vars.setMessage(strTabId, message);
      printPageClosePopUpAndRefreshParent(response, vars);
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentId, String strWindowId, String strTabId) throws IOException,
      ServletException {
    log4j.debug("Output: Add Payment button pressed on Make / Receipt Payment windows");

    FIN_Payment payment = new AdvPaymentMngtDao().getObject(FIN_Payment.class, strPaymentId);
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddOrderOrInvoice").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("businessPartner", payment.getBusinessPartner().getIdentifier());
    xmlDocument.setParameter("businessPartnerId", payment.getBusinessPartner().getId());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", payment.getOrganization().getId());
    xmlDocument.setParameter("paymentId", strPaymentId);
    xmlDocument.setParameter("actualPayment", payment.getAmount().toString());
    xmlDocument.setParameter("headerAmount", payment.getAmount().toString());
    xmlDocument.setParameter("isReceipt", (payment.isReceipt() ? "Y" : "N"));
    xmlDocument.setParameter("credit", dao.getCustomerCredit(payment.getBusinessPartner(),
        payment.isReceipt()).toString());
    xmlDocument.setParameter("generatedCredit", payment.getGeneratedCredit() != null ? payment
        .getGeneratedCredit().toString() : BigDecimal.ZERO.toString());
    OBContext.setAdminMode();
    xmlDocument.setParameter("customerBalance",
        payment.getBusinessPartner().getCreditUsed() != null ? payment.getBusinessPartner()
            .getCreditUsed().toString() : BigDecimal.ZERO.toString());
    try {
      xmlDocument
          .setParameter("precision", payment.getCurrency().getStandardPrecision().toString());
    } finally {
      OBContext.restorePreviousMode();
    }
    boolean forcedFinancialAccountTransaction = false;
    forcedFinancialAccountTransaction = FIN_AddPayment.isForcedFinancialAccountTransaction(payment);
    // Action Regarding Document
    xmlDocument.setParameter("ActionDocument", (payment.isReceipt() ? "PRP" : "PPP"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", (payment
          .isReceipt() ? "F903F726B41A49D3860243101CEEBA25" : "F15C13A199A748F1B0B00E985A64C036"),
          forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70" : "", Utility
              .getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"), Utility
              .getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AddOrderOrInvoice", "");
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
      String strBusinessPartnerId, String strPaymentId, String strOrgId, String strDueDateFrom,
      String strDueDateTo, String strDocumentType, String strSelectedPaymentDetails,
      boolean isReceipt, boolean showAlternativePM) throws IOException, ServletException {

    log4j.debug("Output: Grid with pending payments");
    dao = new AdvPaymentMngtDao();

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddPaymentGrid").createXmlDocument();

    // Pending Payments from invoice
    final List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails = FIN_AddPayment
        .getSelectedPaymentDetails(null, strSelectedPaymentDetails);

    FIN_Payment payment = dao.getObject(FIN_Payment.class, strPaymentId);

    // filtered scheduled payments list
    final List<FIN_PaymentScheduleDetail> filteredScheduledPaymentDetails = dao
        .getFilteredScheduledPaymentDetails(dao.getObject(Organization.class, strOrgId), dao
            .getObject(BusinessPartner.class, strBusinessPartnerId), payment.getCurrency(),
            FIN_Utility.getDate(strDueDateFrom), FIN_Utility.getDate(DateTimeData.nDaysAfter(this,
                strDueDateTo, "1")), strDocumentType, showAlternativePM ? null : payment
                .getPaymentMethod(), selectedScheduledPaymentDetails, isReceipt);

    final FieldProvider[] data = FIN_AddPayment.getShownScheduledPaymentDetails(vars,
        selectedScheduledPaymentDetails, filteredScheduledPaymentDetails, false, null);
    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGLItemGrid(HttpServletResponse response, String strPaymentId)
      throws IOException, ServletException {
    dao = new AdvPaymentMngtDao();

    log4j.debug("Output: Grid with GLItem payment details");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/PaymentGLItemGrid").createXmlDocument();
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      final List<FIN_PaymentDetail> paymentDetails = dao.getObject(FIN_Payment.class, strPaymentId)
          .getFINPaymentDetailList();

      FIN_PaymentDetail[] paymentDetailArray = new FIN_PaymentDetail[0];
      paymentDetailArray = paymentDetails.toArray(paymentDetailArray);

      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(paymentDetails);
      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "cglitemid", paymentDetailArray[i].getGLItem()
            .getIdentifier());
        FieldProviderFactory.setField(data[i], "amount", paymentDetailArray[i].getAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "finpaymentdetailid", paymentDetailArray[i].getId());
      }

      xmlDocument.setData("structure", (data == null) ? set() : data);
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void printGLItem(HttpServletResponse response, String errorMessage) throws IOException {
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(errorMessage);
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
