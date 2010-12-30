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
package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.criterion.Expression;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_PaymentProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("Fin_Payment_ID");
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final boolean isReceipt = payment.isReceipt();

      payment.setProcessNow(true);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();
      if (strAction.equals("P") || strAction.equals("D")) {
        Set<String> invoiceDocNos = new TreeSet<String>();
        Set<String> orderDocNos = new TreeSet<String>();
        Set<String> glitems = new TreeSet<String>();
        BigDecimal paymentAmount = BigDecimal.ZERO;
        BigDecimal paymentWriteOfAmount = BigDecimal.ZERO;

        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          String strRefundCredit = "";
          // update payment schedule amount
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();

          // Show error message when payment has no lines
          if (paymentDetails.size() == 0) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
                "@APRM_PaymentNoLines@"));
            bundle.setResult(msg);
            return;
          }
          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              paymentAmount = paymentAmount.add(paymentScheduleDetail.getAmount());
              BigDecimal writeoff = paymentScheduleDetail.getWriteoffAmount();
              if (writeoff == null)
                writeoff = BigDecimal.ZERO;
              paymentWriteOfAmount = paymentWriteOfAmount.add(writeoff);
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                invoiceDocNos.add(paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                    .getDocumentNo());
                FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                    .getInvoicePaymentSchedule(), paymentDetail.getAmount(), paymentDetail
                    .getWriteoffAmount());
                updateCustomerCredit(paymentScheduleDetail.getInvoicePaymentSchedule(),
                    paymentDetail.getAmount(), strAction);
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                orderDocNos.add(paymentScheduleDetail.getOrderPaymentSchedule().getOrder()
                    .getDocumentNo());
                FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                    .getOrderPaymentSchedule(), paymentDetail.getAmount(), paymentDetail
                    .getWriteoffAmount());
              }
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null) {
                if (paymentDetail.isRefund())
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_RefundAmount", vars
                      .getLanguage());
                else {
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_CreditAmount", vars
                      .getLanguage());
                  payment.setGeneratedCredit(paymentDetail.getAmount());
                }
                strRefundCredit += ": " + paymentDetail.getAmount().toString();
              }
            }
            if (paymentDetail.getGLItem() != null)
              glitems.add(paymentDetail.getGLItem().getName());
          }
          // Set description
          StringBuffer description = new StringBuffer();
          if (payment.getDescription() != null && !payment.getDescription().equals(""))
            description.append(payment.getDescription()).append("\n");
          if (!invoiceDocNos.isEmpty()) {
            description.append(Utility.messageBD(conProvider, "InvoiceDocumentno", vars
                .getLanguage()));
            description.append(": ").append(
                invoiceDocNos.toString().substring(1, invoiceDocNos.toString().length() - 1));
            description.append("\n");
          }
          if (!orderDocNos.isEmpty()) {
            description.append(Utility
                .messageBD(conProvider, "OrderDocumentno", vars.getLanguage()));
            description.append(": ").append(
                orderDocNos.toString().substring(1, orderDocNos.toString().length() - 1));
            description.append("\n");
          }
          if (!glitems.isEmpty()) {
            description.append(Utility.messageBD(conProvider, "APRM_GLItem", vars.getLanguage()));
            description.append(": ").append(
                glitems.toString().substring(1, glitems.toString().length() - 1));
            description.append("\n");
          }
          if (!"".equals(strRefundCredit))
            description.append(strRefundCredit).append("\n");

          String truncateDescription = (description.length() > 255) ? description.substring(0, 252)
              .concat("...").toString() : description.toString();
          payment.setDescription(truncateDescription);

        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }
        if (paymentAmount.compareTo(payment.getAmount()) != 0)
          payment.setUsedCredit(paymentAmount.subtract(payment.getAmount()));
        if (payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0)
          updateUsedCredit(payment.getUsedCredit(), payment.getBusinessPartner(), payment
              .isReceipt());

        payment.setWriteoffAmount(paymentWriteOfAmount);
        payment.setProcessed(true);
        payment.setAPRMProcessPayment("R");
        // Execution Process
        if (dao.isAutomatedExecutionPayment(payment.getAccount(), payment.getPaymentMethod(),
            payment.isReceipt())) {
          try {
            payment.setStatus("RPAE");
            payment.setProcessNow(false);
            OBDal.getInstance().save(payment);
            OBDal.getInstance().flush();

            if (dao.hasNotDeferredExecutionProcess(payment.getAccount(),
                payment.getPaymentMethod(), payment.isReceipt())) {
              PaymentExecutionProcess executionProcess = dao.getExecutionProcess(payment);
              if (dao.isAutomaticExecutionProcess(executionProcess)) {
                final List<FIN_Payment> payments = new ArrayList<FIN_Payment>(1);
                payments.add(payment);
                FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
                executePayment.init("APP", executionProcess, payments, null, payment
                    .getOrganization());
                OBError result = executePayment.execute();
                if ("Error".equals(result.getType())) {
                  msg.setType("Warning");
                  msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
                      result.getMessage()));
                } else if (!"".equals(result.getMessage())) {
                  String execProcessMsg = Utility.parseTranslation(conProvider, vars, vars
                      .getLanguage(), result.getMessage());
                  if (!"".equals(msg.getMessage()))
                    msg.setMessage(msg.getMessage() + "<br>");
                  msg.setMessage(msg.getMessage() + execProcessMsg);
                }
              }
            }
          } catch (final NoExecutionProcessFoundException e) {
            e.printStackTrace(System.err);
            msg.setType("Warning");
            msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
                "@NoExecutionProcessFound@"));
            bundle.setResult(msg);
            return;
          } catch (final Exception e) {
            e.printStackTrace(System.err);
            msg.setType("Warning");
            msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
                "@IssueOnExecutionProcess@"));
            bundle.setResult(msg);
            return;
          }
        } else {
          payment.setStatus(isReceipt ? "RPR" : "PPM");
          if ((FIN_Utility.isAutomaticDepositWithdrawn(payment) || strAction.equals("D"))
              && payment.getAmount().compareTo(BigDecimal.ZERO) != 0)
            triggerAutomaticFinancialAccountTransaction(vars, conProvider, payment);
        }

        // ***********************
        // Reactivate Payment
        // ***********************
      } else if (strAction.equals("R")) {
        // Already Posted Document
        if ("Y".equals(payment.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
              "@PostedDocument@" + ": " + payment.getDocumentNo()));
          bundle.setResult(msg);
          return;
        }
        // Transaction exists
        if (hasTransaction(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
              "@APRM_TransactionExists@"));
          bundle.setResult(msg);
          return;
        }
        // Payment with generated credit already used on other payments.
        if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
            && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
              "@APRM_PaymentGeneratedCreditIsUsed@"));
          bundle.setResult(msg);
          return;
        }
        // Initialize amounts
        payment.setProcessed(false);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
        payment.setWriteoffAmount(BigDecimal.ZERO);
        payment.setAmount(BigDecimal.ZERO);

        if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
            && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1)
          undoUsedCredit(payment.getUsedCredit(), payment.getBusinessPartner(), payment.isReceipt());
        payment.setGeneratedCredit(BigDecimal.ZERO);
        payment.setUsedCredit(BigDecimal.ZERO);

        payment.setStatus("RPAP");
        payment.setDescription("");
        payment.setAPRMProcessPayment("P");
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();

        final List<FIN_PaymentDetail> removedPD = new ArrayList<FIN_PaymentDetail>();
        List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
        final List<String> removedPDIds = new ArrayList<String>();
        // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
        // removed when new security implementation is done
        OBContext.setAdminMode();
        try {
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                    .getInvoicePaymentSchedule(), paymentDetail.getAmount().negate(), paymentDetail
                    .getWriteoffAmount().negate());
                updateCustomerCredit(paymentScheduleDetail.getInvoicePaymentSchedule(),
                    paymentDetail.getAmount(), strAction);
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                FIN_AddPayment.updatePaymentScheduleAmounts(paymentScheduleDetail
                    .getOrderPaymentSchedule(), paymentDetail.getAmount().negate(), paymentDetail
                    .getWriteoffAmount().negate());
              }
              FIN_AddPayment.mergePaymentScheduleDetails(paymentScheduleDetail);
              removedPDS.add(paymentScheduleDetail);

            }
            paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
            OBDal.getInstance().getSession().refresh(paymentDetail);
            removedPD.add(paymentDetail);
            removedPDIds.add(paymentDetail.getId());
            OBDal.getInstance().save(paymentDetail);
          }
          for (String pdToRm : removedPDIds) {
            OBDal.getInstance().remove(OBDal.getInstance().get(FIN_PaymentDetail.class, pdToRm));
          }
          payment.getFINPaymentDetailList().removeAll(removedPD);
          OBDal.getInstance().save(payment);

        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }
      }
      payment.setProcessNow(false);
      OBDal.getInstance().save(payment);
      OBDal.getInstance().flush();

      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
    }
  }

  /**
   * Method used to update the credit used when the user doing invoice processing or payment
   * processing
   * 
   * @param invoicePaymentSchedule
   *          . Invoice payment schedule for the sales invoice. {FIN_PaymentSchedule}.
   * @param amount
   *          . Payment amount.
   * @param strAction
   *          . Indicate the action of the request.
   */
  private void updateCustomerCredit(FIN_PaymentSchedule invoicePaymentSchedule, BigDecimal amount,
      String strAction) {
    BusinessPartner bPartner = invoicePaymentSchedule.getInvoice().getBusinessPartner();
    BigDecimal creditUsed = bPartner.getCreditUsed();
    if (strAction.equals("P")) {
      creditUsed = creditUsed.subtract(amount);
    } else if (strAction.equals("R")) {
      creditUsed = creditUsed.add(amount);
    }
    bPartner.setCreditUsed(creditUsed);
    OBDal.getInstance().save(bPartner);
    OBDal.getInstance().flush();
  }

  @SuppressWarnings("unused")
  private void triggerAutomaticFinancialAccountTransaction(VariablesSecureApp vars,
      ConnectionProvider connectionProvider, FIN_Payment payment) {
    FIN_FinaccTransaction transaction = TransactionsDao.createFinAccTransaction(payment);
    TransactionsDao.process(transaction);
    return;
  }

  private static boolean hasTransaction(FIN_Payment payment) {
    OBCriteria<FIN_FinaccTransaction> transaction = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    transaction.add(Expression.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    List<FIN_FinaccTransaction> list = transaction.list();
    if (list == null || list.size() == 0)
      return false;
    return true;
  }

  private static void updateUsedCredit(BigDecimal usedAmount, BusinessPartner bp, boolean isReceipt) {
    List<FIN_Payment> payments = dao.getCustomerPaymentsWithCredit(bp, isReceipt);
    BigDecimal pendingToAllocateAmount = usedAmount;
    for (FIN_Payment payment : payments) {
      BigDecimal availableAmount = payment.getGeneratedCredit().subtract(payment.getUsedCredit());
      if (pendingToAllocateAmount.compareTo(availableAmount) == 1) {
        payment.setUsedCredit(payment.getUsedCredit().add(availableAmount));
        pendingToAllocateAmount = pendingToAllocateAmount.subtract(availableAmount);
        OBDal.getInstance().save(payment);
      } else {
        payment.setUsedCredit(payment.getUsedCredit().add(pendingToAllocateAmount));
        OBDal.getInstance().save(payment);
        break;
      }
    }
  }

  private void undoUsedCredit(BigDecimal usedAmount, BusinessPartner bp, Boolean isReceipt) {
    List<FIN_Payment> payments = dao.getCustomerPaymentsWithUsedCredit(bp, isReceipt);
    BigDecimal pendingDeallocateAmount = usedAmount;
    for (FIN_Payment payment : payments) {
      BigDecimal paymentUsedAmount = payment.getUsedCredit();
      if (pendingDeallocateAmount.compareTo(usedAmount) == 1) {
        payment.setUsedCredit(BigDecimal.ZERO);
        pendingDeallocateAmount = pendingDeallocateAmount.subtract(paymentUsedAmount);
        OBDal.getInstance().save(payment);
      } else {
        payment.setUsedCredit(payment.getUsedCredit().subtract(pendingDeallocateAmount));
        OBDal.getInstance().save(payment);
        break;
      }
    }
  }
}
