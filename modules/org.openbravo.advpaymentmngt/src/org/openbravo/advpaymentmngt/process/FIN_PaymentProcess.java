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
 * All portions are Copyright (C) 2010-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment_Credit;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class FIN_PaymentProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    final String language = bundle.getContext().getLanguage();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", language));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");
      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("Fin_Payment_ID");
      final FIN_Payment payment = dao.getObject(FIN_Payment.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();

      final ConnectionProvider conProvider = bundle.getConnection();
      final boolean isReceipt = payment.isReceipt();

      if (strAction.equals("P") || strAction.equals("D")) {
        if (payment.getBusinessPartner() != null) {
          if (FIN_Utility.isBlockedBusinessPartner(payment.getBusinessPartner().getId(), isReceipt,
              4)) {
            // If the Business Partner is blocked for Payments, the Payment will not be completed.
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(OBMessageUtils.messageBD("ThebusinessPartner") + " "
                + payment.getBusinessPartner().getIdentifier() + " "
                + OBMessageUtils.messageBD("BusinessPartnerBlocked"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }
        } else {
          OBContext.setAdminMode(true);
          try {
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                BusinessPartner bPartner = null;
                if (psd.getInvoicePaymentSchedule() != null) {
                  bPartner = psd.getInvoicePaymentSchedule().getInvoice().getBusinessPartner();
                } else if (psd.getOrderPaymentSchedule() != null) {
                  bPartner = psd.getOrderPaymentSchedule().getOrder().getBusinessPartner();
                }
                if (bPartner != null
                    && FIN_Utility.isBlockedBusinessPartner(bPartner.getId(), payment.isReceipt(),
                        4)) {
                  // If the Business Partner is blocked for Payments, the Payment will not be
                  // completed.
                  msg.setType("Error");
                  msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                  msg.setMessage(OBMessageUtils.messageBD("ThebusinessPartner") + " "
                      + bPartner.getIdentifier() + " "
                      + OBMessageUtils.messageBD("BusinessPartnerBlocked"));
                  bundle.setResult(msg);
                  OBDal.getInstance().rollbackAndClose();
                  return;
                }
              }
            }
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }

      OBDal.getInstance().flush();
      if (strAction.equals("P") || strAction.equals("D")) {
        // Guess if this is a refund payment
        boolean isRefund = false;
        OBContext.setAdminMode(false);
        try {
          if (payment.getFINPaymentDetailList().size() > 0
              && payment.getFINPaymentDetailList().get(0).isRefund()) {
            isRefund = true;
          }
        } finally {
          OBContext.restorePreviousMode();
        }
        if (!isRefund) {
          // Undo Used credit as it will be calculated again
          payment.setUsedCredit(BigDecimal.ZERO);
          OBDal.getInstance().save(payment);
        }
        // Set APRM_Ready preference
        if (vars.getSessionValue("APRMT_MigrationToolRunning", "N").equals("Y")
            && !dao.existsAPRMReadyPreference()) {
          dao.createAPRMReadyPreference();
        }

        boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(payment.TABLE_NAME,
            payment.getId(), payment.TABLE_NAME + "_ID", "LE");
        if (!FIN_Utility.isPeriodOpen(payment.getClient().getId(), payment.getDocumentType()
            .getDocumentCategory(), payment.getOrganization().getId(), OBDateUtils
            .formatDate(payment.getPaymentDate()))
            && orgLegalWithAccounting) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@PeriodNotAvailable@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        Set<String> documentOrganizations = OBContext.getOBContext()
            .getOrganizationStructureProvider(payment.getClient().getId())
            .getNaturalTree(payment.getOrganization().getId());
        if (!documentOrganizations.contains(payment.getAccount().getOrganization().getId())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_FinancialAccountNotInNaturalTree@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
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
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@APRM_PaymentNoLines@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
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
                final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                    .getInvoice();
                invoiceDocNos.add(FIN_Utility.getDesiredDocumentNo(payment.getOrganization(),
                    invoice));
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                orderDocNos.add(paymentScheduleDetail.getOrderPaymentSchedule().getOrder()
                    .getDocumentNo());
              }
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null) {
                if (paymentDetail.isRefund())
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_RefundAmount", language);
                else {
                  strRefundCredit = Utility.messageBD(conProvider, "APRM_CreditAmount", language);
                  payment.setGeneratedCredit(paymentDetail.getAmount());
                }
                strRefundCredit += ": " + paymentDetail.getAmount().toString();
              }
            }
            if (paymentDetail.getGLItem() != null)
              glitems.add(paymentDetail.getGLItem().getName());
          }
          // Set description
          if (bundle.getParams().get("isPOSOrder") == null
              || !bundle.getParams().get("isPOSOrder").equals("Y")) {
            StringBuffer description = new StringBuffer();

            if (payment.getDescription() != null && !payment.getDescription().equals(""))
              description.append(payment.getDescription()).append("\n");
            if (!invoiceDocNos.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "InvoiceDocumentno", language));
              description.append(": ").append(
                  invoiceDocNos.toString().substring(1, invoiceDocNos.toString().length() - 1));
              description.append("\n");
            }
            if (!orderDocNos.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "OrderDocumentno", language));
              description.append(": ").append(
                  orderDocNos.toString().substring(1, orderDocNos.toString().length() - 1));
              description.append("\n");
            }
            if (!glitems.isEmpty()) {
              description.append(Utility.messageBD(conProvider, "APRM_GLItem", language));
              description.append(": ").append(
                  glitems.toString().substring(1, glitems.toString().length() - 1));
              description.append("\n");
            }
            if (!"".equals(strRefundCredit))
              description.append(strRefundCredit).append("\n");

            String truncateDescription = (description.length() > 255) ? description
                .substring(0, 251).concat("...").toString() : description.toString();
            payment.setDescription(truncateDescription);
          }

          if (paymentAmount.compareTo(payment.getAmount()) != 0) {
            payment.setUsedCredit(paymentAmount.subtract(payment.getAmount()));
          }
          if (payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            updateUsedCredit(payment);
          }

          payment.setWriteoffAmount(paymentWriteOfAmount);
          payment.setProcessed(true);
          payment.setAPRMProcessPayment("RE");
          if (payment.getGeneratedCredit() == null) {
            payment.setGeneratedCredit(BigDecimal.ZERO);
          }
          if (BigDecimal.ZERO.compareTo(payment.getUsedCredit()) != 0
              || BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0) {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            if (businessPartner == null) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_CreditWithoutBPartner@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            PriceList priceList = payment.isReceipt() ? businessPartner.getPriceList()
                : businessPartner.getPurchasePricelist();
            if (!payment.getCurrency().getId()
                .equals(priceList != null ? priceList.getCurrency().getId() : "")) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(String.format(
                  Utility.parseTranslation(conProvider, vars, language, "@APRM_CreditCurrency@"),
                  priceList != null ? priceList.getCurrency().getISOCode() : Utility
                      .parseTranslation(conProvider, vars, language,
                          "@APRM_CreditNoPricelistCurrency@")));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          }
          // Execution Process
          if (dao.isAutomatedExecutionPayment(payment.getAccount(), payment.getPaymentMethod(),
              payment.isReceipt())) {
            try {
              payment.setStatus("RPAE");

              if (dao.hasNotDeferredExecutionProcess(payment.getAccount(),
                  payment.getPaymentMethod(), payment.isReceipt())) {
                PaymentExecutionProcess executionProcess = dao.getExecutionProcess(payment);
                if (dao.isAutomaticExecutionProcess(executionProcess)) {
                  final List<FIN_Payment> payments = new ArrayList<FIN_Payment>(1);
                  payments.add(payment);
                  FIN_ExecutePayment executePayment = new FIN_ExecutePayment();
                  executePayment.init("APP", executionProcess, payments, null,
                      payment.getOrganization());
                  OBError result = executePayment.execute();
                  if ("Error".equals(result.getType())) {
                    msg.setType("Warning");
                    msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                        result.getMessage()));
                  } else if (!"".equals(result.getMessage())) {
                    String execProcessMsg = Utility.parseTranslation(conProvider, vars, language,
                        result.getMessage());
                    if (!"".equals(msg.getMessage()))
                      msg.setMessage(msg.getMessage() + "<br>");
                    msg.setMessage(msg.getMessage() + execProcessMsg);
                  }
                }
              }
            } catch (final NoExecutionProcessFoundException e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@NoExecutionProcessFound@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            } catch (final Exception e) {
              e.printStackTrace(System.err);
              msg.setType("Warning");
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@IssueOnExecutionProcess@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
          } else {
            BusinessPartner businessPartner = payment.getBusinessPartner();
            // When credit is used (consumed) we compensate so_creditused as this amount is already
            // included in the payment details. Credit consumed should not affect to so_creditused
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
              if (isReceipt) {
                increaseCustomerCredit(businessPartner, payment.getUsedCredit());
              } else {
                decreaseCustomerCredit(businessPartner, payment.getUsedCredit());
              }
            }
            for (FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              // Get payment schedule detail list ordered by amount asc.
              // First negative if they exist and then positives
              OBCriteria<FIN_PaymentScheduleDetail> obcPSD = OBDal.getInstance().createCriteria(
                  FIN_PaymentScheduleDetail.class);
              obcPSD.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS,
                  paymentDetail));
              obcPSD.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);

              for (FIN_PaymentScheduleDetail paymentScheduleDetail : obcPSD.list()) {
                BigDecimal amount = paymentScheduleDetail.getAmount().add(
                    paymentScheduleDetail.getWriteoffAmount());
                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  // BP SO_CreditUsed
                  businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                      .getBusinessPartner();
                  // Payments update credit opposite to invoices
                  if (isReceipt) {
                    decreaseCustomerCredit(businessPartner, amount);
                  } else {
                    increaseCustomerCredit(businessPartner, amount);
                  }
                  FIN_AddPayment.updatePaymentScheduleAmounts(paymentDetail,
                      paymentScheduleDetail.getInvoicePaymentSchedule(),
                      paymentScheduleDetail.getAmount(), paymentScheduleDetail.getWriteoffAmount());
                }
                if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                  FIN_AddPayment.updatePaymentScheduleAmounts(
                      paymentScheduleDetail.getOrderPaymentSchedule(),
                      paymentScheduleDetail.getAmount(), paymentScheduleDetail.getWriteoffAmount());
                }
                // when generating credit for a BP SO_CreditUsed is also updated
                if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                    && paymentScheduleDetail.getOrderPaymentSchedule() == null
                    && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                    && !paymentDetail.isRefund()) {
                  // BP SO_CreditUsed
                  if (isReceipt) {
                    decreaseCustomerCredit(businessPartner, amount);
                  } else {
                    increaseCustomerCredit(businessPartner, amount);
                  }
                }
              }
            }
            payment.setStatus(isReceipt ? "RPR" : "PPM");
            if ((strAction.equals("D") || FIN_Utility.isAutomaticDepositWithdrawn(payment))
                && payment.getAmount().compareTo(BigDecimal.ZERO) != 0)
              triggerAutomaticFinancialAccountTransaction(vars, conProvider, payment);
          }
          if (!payment.getAccount().getCurrency().equals(payment.getCurrency())
              && getConversionRateDocument(payment).size() == 0) {
            insertConversionRateDocument(payment);
          }
        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

        // ***********************
        // Reverse Payment
        // ***********************
      } else if (strAction.equals("RV")) {
        FIN_Payment reversedPayment = (FIN_Payment) DalUtil.copy(payment, false);
        final String paymentDate = (String) bundle.getParams().get("paymentdate");
        OBContext.setAdminMode();
        try {
          if (BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0
              && BigDecimal.ZERO.compareTo(payment.getUsedCredit()) != 0) {
            throw new OBException("@APRM_CreditConsumed@");
          } else if (BigDecimal.ZERO.compareTo(payment.getGeneratedCredit()) != 0
              && BigDecimal.ZERO.compareTo(payment.getUsedCredit()) == 0) {
            reversedPayment.setUsedCredit(payment.getGeneratedCredit());
            reversedPayment.setGeneratedCredit(BigDecimal.ZERO);
          } else {
            reversedPayment.setUsedCredit(BigDecimal.ZERO);
            reversedPayment.setGeneratedCredit(BigDecimal.ZERO);
          }
          reversedPayment.setDocumentNo("*R*"
              + FIN_Utility.getDocumentNo(payment.getDocumentType(), "FIN_Payment"));
          reversedPayment.setPaymentDate(FIN_Utility.getDate(paymentDate));
          reversedPayment.setDescription("");
          reversedPayment.setProcessed(false);
          reversedPayment.setPosted("N");
          reversedPayment.setProcessNow(false);
          reversedPayment.setAPRMProcessPayment("P");
          reversedPayment.setStatus("RPAP");
          // Amounts
          reversedPayment.setAmount(payment.getAmount().negate());
          reversedPayment.setWriteoffAmount(payment.getWriteoffAmount().negate());
          reversedPayment.setFinancialTransactionAmount(payment.getFinancialTransactionAmount()
              .negate());
          OBDal.getInstance().save(reversedPayment);

          List<FIN_PaymentDetail> reversedDetails = new ArrayList<FIN_PaymentDetail>();

          OBDal.getInstance().save(reversedPayment);
          List<FIN_Payment_Credit> credits = payment.getFINPaymentCreditList();

          for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
            FIN_PaymentDetail reversedPaymentDetail = (FIN_PaymentDetail) DalUtil.copy(pd, false);
            reversedPaymentDetail.setFinPayment(reversedPayment);
            reversedPaymentDetail.setAmount(pd.getAmount().negate());
            reversedPaymentDetail.setWriteoffAmount(pd.getWriteoffAmount().negate());
            if (pd.isRefund()) {
              reversedPaymentDetail.setPrepayment(true);
              reversedPaymentDetail.setRefund(false);
              reversedPayment.setGeneratedCredit(reversedPayment.getGeneratedCredit().add(
                  pd.getAmount()));
              credits = new ArrayList<FIN_Payment_Credit>();
              OBDal.getInstance().save(reversedPayment);
            } else if (pd.isPrepayment()
                && pd.getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() == null) {
              reversedPaymentDetail.setPrepayment(true);
              reversedPaymentDetail.setRefund(true);
            }
            List<FIN_PaymentScheduleDetail> reversedSchedDetails = new ArrayList<FIN_PaymentScheduleDetail>();
            OBDal.getInstance().save(reversedPaymentDetail);
            // Create or update PSD of orders and invoices to set the new outstanding amount
            for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
              if (psd.getInvoicePaymentSchedule() != null || psd.getOrderPaymentSchedule() != null) {
                OBCriteria<FIN_PaymentScheduleDetail> unpaidSchedDet = OBDal.getInstance()
                    .createCriteria(FIN_PaymentScheduleDetail.class);
                if (psd.getInvoicePaymentSchedule() != null)
                  unpaidSchedDet.add(Restrictions.eq(
                      FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
                      psd.getInvoicePaymentSchedule()));
                if (psd.getOrderPaymentSchedule() != null)
                  unpaidSchedDet.add(Restrictions.eq(
                      FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE,
                      psd.getOrderPaymentSchedule()));
                unpaidSchedDet.add(Restrictions
                    .isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
                List<FIN_PaymentScheduleDetail> openPSDs = unpaidSchedDet.list();
                // If invoice/order not fully paid, update outstanding amount
                if (openPSDs.size() > 0) {
                  FIN_PaymentScheduleDetail openPSD = openPSDs.get(0);
                  BigDecimal openAmount = openPSD.getAmount().add(psd.getAmount());
                  if (openAmount.compareTo(BigDecimal.ZERO) == 0) {
                    OBDal.getInstance().remove(openPSD);
                  } else {
                    openPSD.setAmount(openAmount);
                  }
                } else {
                  // If invoice is fully paid create a new schedule detail.
                  FIN_PaymentScheduleDetail openPSD = (FIN_PaymentScheduleDetail) DalUtil.copy(psd,
                      false);
                  openPSD.setPaymentDetails(null);
                  // Amounts
                  openPSD.setWriteoffAmount(BigDecimal.ZERO);
                  openPSD.setAmount(psd.getAmount());

                  openPSD.setCanceled(false);
                  OBDal.getInstance().save(openPSD);
                }
              }
              FIN_PaymentScheduleDetail reversedPaymentSchedDetail = (FIN_PaymentScheduleDetail) DalUtil
                  .copy(psd, false);
              reversedPaymentSchedDetail.setPaymentDetails(reversedPaymentDetail);
              // Amounts
              reversedPaymentSchedDetail.setWriteoffAmount(psd.getWriteoffAmount().negate());
              reversedPaymentSchedDetail.setAmount(psd.getAmount().negate());
              OBDal.getInstance().save(reversedPaymentSchedDetail);
              reversedSchedDetails.add(reversedPaymentSchedDetail);
            }
            reversedPaymentDetail.setFINPaymentScheduleDetailList(reversedSchedDetails);
            OBDal.getInstance().save(reversedPaymentDetail);
            reversedDetails.add(reversedPaymentDetail);
          }
          reversedPayment.setFINPaymentDetailList(reversedDetails);
          OBDal.getInstance().save(reversedPayment);

          List<FIN_Payment_Credit> reversedCredits = new ArrayList<FIN_Payment_Credit>();
          for (FIN_Payment_Credit pc : credits) {
            FIN_Payment_Credit reversedPaymentCredit = (FIN_Payment_Credit) DalUtil.copy(pc, false);
            reversedPaymentCredit.setAmount(pc.getAmount().negate());
            reversedPaymentCredit.setCreditPaymentUsed(pc.getCreditPaymentUsed());
            pc.getCreditPaymentUsed().setUsedCredit(
                pc.getCreditPaymentUsed().getUsedCredit().add(pc.getAmount().negate()));
            reversedPaymentCredit.setPayment(reversedPayment);
            OBDal.getInstance().save(pc.getCreditPaymentUsed());
            OBDal.getInstance().save(reversedPaymentCredit);
            reversedCredits.add(reversedPaymentCredit);
          }

          reversedPayment.setFINPaymentCreditList(reversedCredits);
          OBDal.getInstance().save(reversedPayment);

          List<ConversionRateDoc> conversions = new ArrayList<ConversionRateDoc>();
          for (ConversionRateDoc cr : payment.getCurrencyConversionRateDocList()) {
            ConversionRateDoc reversedCR = (ConversionRateDoc) DalUtil.copy(cr, false);
            reversedCR.setForeignAmount(cr.getForeignAmount().negate());
            reversedCR.setPayment(reversedPayment);
            OBDal.getInstance().save(reversedCR);
            conversions.add(reversedCR);
          }
          reversedPayment.setCurrencyConversionRateDocList(conversions);
          OBDal.getInstance().save(reversedPayment);

          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }

        payment.setReversedPayment(reversedPayment);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();

        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("Fin_Payment_ID", reversedPayment.getId());
        parameterMap.put("action", "P");
        parameterMap.put("isReversedPayment", "Y");
        bundle.setParams(parameterMap);
        execute(bundle);

        return;

        // ***********************
        // Reactivate Payment
        // ***********************
      } else if (strAction.equals("R") || strAction.equals("RE")) {
        // Already Posted Document
        if ("Y".equals(payment.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
              + ": " + payment.getDocumentNo()));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        // Reversed Payment
        if (payment.getReversedPayment() != null) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_PaymentReversed@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        // Reverse Payment
        if (strAction.equals("RE") && FIN_Utility.isReversePayment(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_ReversePayment@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not reactive the payment if it is tax payment
        if (payment.getFinancialMgmtTaxPaymentList().size() != 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_TaxPaymentReactivation@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Transaction exists
        if (hasTransaction(payment)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_TransactionExists@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }
        // Payment with generated credit already used on other payments.
        if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
            && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_PaymentGeneratedCreditIsUsed@"));
          bundle.setResult(msg);
          OBDal.getInstance().rollbackAndClose();
          return;
        }

        // Do not restore paid amounts if the payment is awaiting execution.
        boolean restorePaidAmounts = !"RPAE".equals(payment.getStatus());
        // Initialize amounts
        payment.setProcessed(false);
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
        payment.setWriteoffAmount(BigDecimal.ZERO);

        payment.setDescription("");

        // if all line are deleted then update amount to zero
        if (strAction.equals("R")) {
          payment.setAmount(BigDecimal.ZERO);
        }

        payment.setStatus("RPAP");
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
          BusinessPartner businessPartner = payment.getBusinessPartner();
          // When credit is used (consumed) we compensate so_creditused as this amount is already
          // included in the payment details. Credit consumed should not affect to so_creditused
          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            if (isReceipt) {
              decreaseCustomerCredit(businessPartner, payment.getUsedCredit());
            } else {
              increaseCustomerCredit(businessPartner, payment.getUsedCredit());
            }
          }
          List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
          List<ConversionRateDoc> conversionRates = payment.getCurrencyConversionRateDocList();
          Set<String> invoiceDocNos = new HashSet<String>();
          // Undo Reversed payment relationship
          List<FIN_Payment> revPayments = new ArrayList<FIN_Payment>();
          for (FIN_Payment reversedPayment : payment.getFINPaymentReversedPaymentList()) {
            reversedPayment.setReversedPayment(null);
            OBDal.getInstance().save(reversedPayment);
          }
          payment.setFINPaymentReversedPaymentList(revPayments);
          OBDal.getInstance().save(payment);
          for (FIN_PaymentDetail paymentDetail : paymentDetails) {
            removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                .getFINPaymentScheduleDetailList()) {
              BigDecimal psdWriteoffAmount = paymentScheduleDetail.getWriteoffAmount();
              BigDecimal psdAmount = paymentScheduleDetail.getAmount();
              BigDecimal amount = psdAmount.add(psdWriteoffAmount);
              if (psdWriteoffAmount.signum() != 0 && strAction.equals("RE")) {
                // Restore write off
                List<FIN_PaymentScheduleDetail> outstandingPDSs = FIN_AddPayment
                    .getOutstandingPSDs(paymentScheduleDetail);
                BigDecimal outstandingDebtAmount = BigDecimal.ZERO;
                if (outstandingPDSs.size() > 0) {
                  outstandingPDSs.get(0).setAmount(
                      outstandingPDSs.get(0).getAmount().add(psdWriteoffAmount));
                  OBDal.getInstance().save(outstandingPDSs.get(0));
                } else {
                  FIN_PaymentScheduleDetail outstandingPSD = (FIN_PaymentScheduleDetail) DalUtil
                      .copy(paymentScheduleDetail, false);
                  outstandingPSD.setAmount(psdWriteoffAmount);
                  if (paymentScheduleDetail.getDoubtfulDebtAmount().signum() != 0) {
                    if (psdWriteoffAmount.compareTo(paymentScheduleDetail.getDoubtfulDebtAmount()) >= 0) {
                      outstandingDebtAmount = paymentScheduleDetail.getDoubtfulDebtAmount();
                    } else {
                      outstandingDebtAmount = psdWriteoffAmount;
                    }
                  }
                  outstandingPSD.setDoubtfulDebtAmount(outstandingDebtAmount);
                  outstandingPSD.setWriteoffAmount(BigDecimal.ZERO);
                  outstandingPSD.setPaymentDetails(null);
                  OBDal.getInstance().save(outstandingPSD);
                }
                paymentScheduleDetail.setWriteoffAmount(BigDecimal.ZERO);
                paymentScheduleDetail.setDoubtfulDebtAmount(paymentScheduleDetail
                    .getDoubtfulDebtAmount().subtract(outstandingDebtAmount));
                paymentScheduleDetail.getPaymentDetails().setWriteoffAmount(BigDecimal.ZERO);
                OBDal.getInstance().save(paymentScheduleDetail.getPaymentDetails());
                OBDal.getInstance().save(paymentScheduleDetail);
              }
              if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                // Remove invoice description related to the credit payments
                final Invoice invoice = paymentScheduleDetail.getInvoicePaymentSchedule()
                    .getInvoice();
                invoiceDocNos.add(invoice.getDocumentNo());
                final String invDesc = invoice.getDescription();
                if (invDesc != null) {
                  final String creditMsg = Utility.messageBD(new DalConnectionProvider(),
                      "APRM_InvoiceDescUsedCredit", vars.getLanguage());
                  if (creditMsg != null) {
                    StringBuffer newDesc = new StringBuffer();
                    for (final String line : invDesc.split("\n")) {
                      if (!line.startsWith(creditMsg.substring(0, creditMsg.lastIndexOf("%s")))) {
                        newDesc.append(line);
                        if (!"".equals(line))
                          newDesc.append("\n");
                      }
                    }
                    if (newDesc.length() > 255) {
                      newDesc = newDesc.delete(251, newDesc.length());
                      newDesc = newDesc.append("...\n");
                    }
                    invoice.setDescription(newDesc.toString());
                  }
                }
                if (restorePaidAmounts) {
                  FIN_AddPayment.updatePaymentScheduleAmounts(paymentDetail,
                      paymentScheduleDetail.getInvoicePaymentSchedule(), psdAmount.negate(),
                      psdWriteoffAmount.negate());
                  // BP SO_CreditUsed
                  businessPartner = paymentScheduleDetail.getInvoicePaymentSchedule().getInvoice()
                      .getBusinessPartner();
                  if (isReceipt) {
                    increaseCustomerCredit(businessPartner, amount);
                  } else {
                    decreaseCustomerCredit(businessPartner, amount);
                  }
                }
              }
              if (paymentScheduleDetail.getOrderPaymentSchedule() != null && restorePaidAmounts) {
                FIN_AddPayment.updatePaymentScheduleAmounts(
                    paymentScheduleDetail.getOrderPaymentSchedule(), psdAmount.negate(),
                    psdWriteoffAmount.negate());
              }
              // when generating credit for a BP SO_CreditUsed is also updated
              if (paymentScheduleDetail.getInvoicePaymentSchedule() == null
                  && paymentScheduleDetail.getOrderPaymentSchedule() == null
                  && paymentScheduleDetail.getPaymentDetails().getGLItem() == null
                  && restorePaidAmounts && !paymentDetail.isRefund()) {
                // BP SO_CreditUsed
                if (isReceipt) {
                  increaseCustomerCredit(businessPartner, amount);
                } else {
                  decreaseCustomerCredit(businessPartner, amount);
                }
              }
              if (strAction.equals("R")
                  || (strAction.equals("RE")
                      && paymentScheduleDetail.getInvoicePaymentSchedule() == null
                      && paymentScheduleDetail.getOrderPaymentSchedule() == null && paymentScheduleDetail
                      .getPaymentDetails().getGLItem() == null)) {
                FIN_AddPayment.mergePaymentScheduleDetails(paymentScheduleDetail);
                removedPDS.add(paymentScheduleDetail);
              }
            }
            paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
            if (strAction.equals("R")) {
              OBDal.getInstance().getSession().refresh(paymentDetail);
            }
            // If there is any schedule detail with amount zero, those are deleted
            for (FIN_PaymentScheduleDetail psd : removedPDS) {
              if (BigDecimal.ZERO.compareTo(psd.getAmount()) == 0
                  && BigDecimal.ZERO.compareTo(psd.getWriteoffAmount()) == 0) {
                paymentDetail.getFINPaymentScheduleDetailList().remove(psd);
                OBDal.getInstance().getSession().refresh(paymentDetail);
                psd.getInvoicePaymentSchedule()
                    .getFINPaymentScheduleDetailInvoicePaymentScheduleList().remove(psd);
                psd.getOrderPaymentSchedule().getFINPaymentScheduleDetailOrderPaymentScheduleList()
                    .remove(psd);
                OBDal.getInstance().remove(psd);
              }
            }
            if (paymentDetail.getFINPaymentScheduleDetailList().size() == 0) {
              removedPD.add(paymentDetail);
              removedPDIds.add(paymentDetail.getId());
            }
            OBDal.getInstance().save(paymentDetail);
          }
          for (String pdToRm : removedPDIds) {
            OBDal.getInstance().remove(OBDal.getInstance().get(FIN_PaymentDetail.class, pdToRm));
          }
          payment.getFINPaymentDetailList().removeAll(removedPD);
          if (strAction.equals("R")) {
            payment.getCurrencyConversionRateDocList().removeAll(conversionRates);
            payment.setFinancialTransactionConvertRate(BigDecimal.ZERO);
          }
          OBDal.getInstance().save(payment);

          if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
              && payment.getUsedCredit().compareTo(BigDecimal.ZERO) != 0) {
            undoUsedCredit(payment, vars, invoiceDocNos);
          }

          List<FIN_Payment> creditPayments = new ArrayList<FIN_Payment>();
          for (final FIN_Payment_Credit pc : payment.getFINPaymentCreditList()) {
            creditPayments.add(pc.getCreditPaymentUsed());
          }
          for (final FIN_Payment creditPayment : creditPayments) {
            // Update Description
            final String payDesc = creditPayment.getDescription();
            if (payDesc != null) {
              final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
                  "APRM_CreditUsedinInvoice", vars.getLanguage());
              if (invoiceDocNoMsg != null) {
                final StringBuffer newDesc = new StringBuffer();
                for (final String line : payDesc.split("\n")) {
                  boolean include = true;
                  if (line.startsWith(invoiceDocNoMsg.substring(0,
                      invoiceDocNoMsg.lastIndexOf("%s")))) {
                    for (final String docNo : invoiceDocNos) {
                      if (line.indexOf(docNo) > 0) {
                        include = false;
                        break;
                      }
                    }
                  }
                  if (include) {
                    newDesc.append(line);
                    if (!"".equals(line))
                      newDesc.append("\n");
                  }
                }
                creditPayment.setDescription(newDesc.toString());
              }
            }
          }
          payment.getFINPaymentCreditList().clear();
          payment.setGeneratedCredit(BigDecimal.ZERO);
          if (strAction.equals("R")) {
            payment.setUsedCredit(BigDecimal.ZERO);
            for (FIN_PaymentScheduleDetail psd : removedPDS) {
              List<FIN_PaymentPropDetail> ppds = psd.getFINPaymentPropDetailList();
              if (ppds.size() > 0) {
                for (FIN_PaymentPropDetail ppd : ppds) {
                  FIN_PaymentProposal paymentProposal = OBDal.getInstance().get(
                      FIN_PaymentProposal.class, ppd.getFinPaymentProposal().getId());
                  paymentProposal.setProcessed(false);
                  OBDal.getInstance().save(paymentProposal);
                  OBDal.getInstance().remove(ppd);
                  OBDal.getInstance().flush();
                  paymentProposal.setProcessed(true);
                }
              }
            }
          }
        } finally {
          OBDal.getInstance().flush();
          OBContext.restorePreviousMode();
        }

      } else if (strAction.equals("V")) {
        // Void
        OBContext.setAdminMode();
        try {
          if (payment.isProcessed()) {
            // Already Posted Document
            if ("Y".equals(payment.getPosted())) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@PostedDocument@" + ": " + payment.getDocumentNo()));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            // Transaction exists
            if (hasTransaction(payment)) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_TransactionExists@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            // Payment with generated credit already used on other payments.
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 1
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentGeneratedCreditIsUsed@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }
            // Payment not in Awaiting Execution
            if (!"RPAE".equals(payment.getStatus())) {
              msg.setType("Error");
              msg.setTitle(Utility.messageBD(conProvider, "Error", language));
              msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                  "@APRM_PaymentNotRPAE_NotVoid@"));
              bundle.setResult(msg);
              OBDal.getInstance().rollbackAndClose();
              return;
            }

            /*
             * Void the payment
             */
            payment.setStatus("RPVOID");

            /*
             * Cancel all payment schedule details related to the payment
             */
            final List<FIN_PaymentScheduleDetail> removedPDS = new ArrayList<FIN_PaymentScheduleDetail>();
            Set<String> invoiceDocNos = new HashSet<String>();
            for (final FIN_PaymentDetail paymentDetail : payment.getFINPaymentDetailList()) {
              for (final FIN_PaymentScheduleDetail paymentScheduleDetail : paymentDetail
                  .getFINPaymentScheduleDetailList()) {
                BigDecimal outStandingAmt = BigDecimal.ZERO;

                if (paymentScheduleDetail.getInvoicePaymentSchedule() != null) {
                  // Related to invoices
                  for (final FIN_PaymentScheduleDetail invScheDetail : paymentScheduleDetail
                      .getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
                    if (invScheDetail.isCanceled()) {
                      continue;
                    }
                    if (invScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount()).add(
                          invScheDetail.getWriteoffAmount());
                      removedPDS.add(invScheDetail);
                    } else if (invScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(invScheDetail.getAmount()).add(
                          invScheDetail.getWriteoffAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                    invoiceDocNos.add(paymentScheduleDetail.getInvoicePaymentSchedule()
                        .getInvoice().getDocumentNo());
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail.setInvoicePaymentSchedule(paymentScheduleDetail
                        .getInvoicePaymentSchedule());
                    mergedScheduleDetail.setOrderPaymentSchedule(paymentScheduleDetail
                        .getOrderPaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() != null) {
                  // Related to orders
                  for (final FIN_PaymentScheduleDetail ordScheDetail : paymentScheduleDetail
                      .getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
                    if (ordScheDetail.isCanceled()) {
                      continue;
                    }
                    if (ordScheDetail.getPaymentDetails() == null) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount()).add(
                          ordScheDetail.getWriteoffAmount());
                      removedPDS.add(ordScheDetail);
                    } else if (ordScheDetail.equals(paymentScheduleDetail)) {
                      outStandingAmt = outStandingAmt.add(ordScheDetail.getAmount()).add(
                          ordScheDetail.getWriteoffAmount());
                      paymentScheduleDetail.setCanceled(true);
                    }
                  }
                  // Create merged Payment Schedule Detail with the pending to be paid amount
                  if (outStandingAmt.compareTo(BigDecimal.ZERO) != 0) {
                    final FIN_PaymentScheduleDetail mergedScheduleDetail = dao
                        .getNewPaymentScheduleDetail(payment.getOrganization(), outStandingAmt);
                    mergedScheduleDetail.setOrderPaymentSchedule(paymentScheduleDetail
                        .getOrderPaymentSchedule());
                    OBDal.getInstance().save(mergedScheduleDetail);
                  }
                } else if (paymentDetail.getGLItem() != null) {
                  paymentScheduleDetail.setCanceled(true);
                } else if (paymentScheduleDetail.getOrderPaymentSchedule() == null
                    && paymentScheduleDetail.getInvoicePaymentSchedule() == null) {
                  // Credit payment
                  payment.setGeneratedCredit(payment.getGeneratedCredit().subtract(
                      paymentScheduleDetail.getAmount()));
                  removedPDS.add(paymentScheduleDetail);
                }

                OBDal.getInstance().save(payment);
                OBDal.getInstance().flush();
              }
              paymentDetail.getFINPaymentScheduleDetailList().removeAll(removedPDS);
              for (FIN_PaymentScheduleDetail removedPD : removedPDS) {
                if (removedPD.getOrderPaymentSchedule() != null) {
                  removedPD.getOrderPaymentSchedule()
                      .getFINPaymentScheduleDetailOrderPaymentScheduleList().remove(removedPD);
                  OBDal.getInstance().save(removedPD.getOrderPaymentSchedule());
                }
                if (removedPD.getInvoicePaymentSchedule() != null) {
                  removedPD.getInvoicePaymentSchedule()
                      .getFINPaymentScheduleDetailInvoicePaymentScheduleList().remove(removedPD);
                  OBDal.getInstance().save(removedPD.getInvoicePaymentSchedule());
                }
                OBDal.getInstance().remove(removedPD);
              }
              OBDal.getInstance().flush();
              removedPDS.clear();
            }
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) == 0
                && payment.getUsedCredit().compareTo(BigDecimal.ZERO) == 1) {
              undoUsedCredit(payment, vars, invoiceDocNos);
            }
            payment.getFINPaymentCreditList().clear();
            payment.setUsedCredit(BigDecimal.ZERO);
          }
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }

      bundle.setResult(msg);

    } catch (final Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }
  }

  /**
   * Method used to update the credit used when the user doing invoice processing or payment
   * processing
   * 
   * @param amount
   *          Payment amount
   */
  private void updateCustomerCredit(BusinessPartner businessPartner, BigDecimal amount, boolean add) {
    BigDecimal creditUsed = businessPartner.getCreditUsed();
    if (add) {
      creditUsed = creditUsed.add(amount);
    } else {
      creditUsed = creditUsed.subtract(amount);
    }
    businessPartner.setCreditUsed(creditUsed);
    OBDal.getInstance().save(businessPartner);
    // OBDal.getInstance().flush();
  }

  private void increaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, true);
  }

  private void decreaseCustomerCredit(BusinessPartner businessPartner, BigDecimal amount) {
    updateCustomerCredit(businessPartner, amount, false);
  }

  private void triggerAutomaticFinancialAccountTransaction(VariablesSecureApp vars,
      ConnectionProvider connectionProvider, FIN_Payment payment) {
    FIN_FinaccTransaction transaction = TransactionsDao.createFinAccTransaction(payment);
    try {
      processTransaction(vars, connectionProvider, "P", transaction);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
    }
    return;
  }

  private static boolean hasTransaction(FIN_Payment payment) {
    OBCriteria<FIN_FinaccTransaction> transaction = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    transaction.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    List<FIN_FinaccTransaction> list = transaction.list();
    if (list == null || list.size() == 0)
      return false;
    return true;
  }

  private void updateUsedCredit(FIN_Payment newPayment) {
    if (newPayment.getFINPaymentCreditList().isEmpty()) {
      // We process the payment from the Payment In/Out window (not from the Process Invoice flow)
      final BigDecimal usedAmount = newPayment.getUsedCredit();
      final BusinessPartner bp = newPayment.getBusinessPartner();
      final boolean isReceipt = newPayment.isReceipt();
      final Organization Org = newPayment.getOrganization();

      List<FIN_Payment> creditPayments = dao.getCustomerPaymentsWithCredit(Org, bp, isReceipt);
      BigDecimal pendingToAllocateAmount = usedAmount;
      for (FIN_Payment creditPayment : creditPayments) {
        BigDecimal availableAmount = creditPayment.getGeneratedCredit().subtract(
            creditPayment.getUsedCredit());
        if (pendingToAllocateAmount.compareTo(availableAmount) == 1) {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(availableAmount));
          pendingToAllocateAmount = pendingToAllocateAmount.subtract(availableAmount);
          linkCreditPayment(newPayment, availableAmount, creditPayment);
          OBDal.getInstance().save(creditPayment);
        } else {
          creditPayment.setUsedCredit(creditPayment.getUsedCredit().add(pendingToAllocateAmount));
          linkCreditPayment(newPayment, pendingToAllocateAmount, creditPayment);
          OBDal.getInstance().save(creditPayment);
          break;
        }
      }
    }
  }

  public static void linkCreditPayment(FIN_Payment newPayment, BigDecimal usedAmount,
      FIN_Payment creditPayment) {
    final FIN_Payment_Credit creditInfo = OBProvider.getInstance().get(FIN_Payment_Credit.class);
    creditInfo.setPayment(newPayment);
    creditInfo.setAmount(usedAmount);
    creditInfo.setCurrency(newPayment.getCurrency());
    creditInfo.setCreditPaymentUsed(creditPayment);
    creditInfo.setOrganization(newPayment.getOrganization());
    creditInfo.setClient(newPayment.getClient());
    newPayment.getFINPaymentCreditList().add(creditInfo);
  }

  private void undoUsedCredit(FIN_Payment myPayment, VariablesSecureApp vars,
      Set<String> invoiceDocNos) {
    final List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
    for (final FIN_Payment_Credit pc : myPayment.getFINPaymentCreditList()) {
      final FIN_Payment creditPaymentUsed = pc.getCreditPaymentUsed();
      creditPaymentUsed.setUsedCredit(creditPaymentUsed.getUsedCredit().subtract(pc.getAmount()));
      payments.add(creditPaymentUsed);
    }

    for (final FIN_Payment payment : payments) {
      // Update Description
      final String payDesc = payment.getDescription();
      if (payDesc != null) {
        final String invoiceDocNoMsg = Utility.messageBD(new DalConnectionProvider(),
            "APRM_CreditUsedinInvoice", vars.getLanguage());
        if (invoiceDocNoMsg != null) {
          final StringBuffer newDesc = new StringBuffer();
          for (final String line : payDesc.split("\n")) {
            boolean include = true;
            if (line.startsWith(invoiceDocNoMsg.substring(0, invoiceDocNoMsg.lastIndexOf("%s")))) {
              for (final String docNo : invoiceDocNos) {
                if (line.indexOf(docNo) > 0) {
                  include = false;
                  break;
                }
              }
            }
            if (include) {
              newDesc.append(line);
              if (!"".equals(line))
                newDesc.append("\n");
            }
          }
          payment.setDescription(newDesc.toString());
        }
      }
    }
  }

  private List<ConversionRateDoc> getConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ConversionRateDoc> obc = OBDal.getInstance().createCriteria(
          ConversionRateDoc.class);
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, payment.getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, payment.getAccount()
          .getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_PAYMENT, payment));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ConversionRateDoc insertConversionRateDocument(FIN_Payment payment) {
    OBContext.setAdminMode();
    try {
      ConversionRateDoc newConversionRateDoc = OBProvider.getInstance()
          .get(ConversionRateDoc.class);
      newConversionRateDoc.setOrganization(payment.getOrganization());
      newConversionRateDoc.setCurrency(payment.getCurrency());
      newConversionRateDoc.setToCurrency(payment.getAccount().getCurrency());
      newConversionRateDoc.setRate(payment.getFinancialTransactionConvertRate());
      newConversionRateDoc.setForeignAmount(payment.getFinancialTransactionAmount());
      newConversionRateDoc.setPayment(payment);
      newConversionRateDoc.setClient(payment.getClient());
      OBDal.getInstance().save(newConversionRateDoc);
      OBDal.getInstance().flush();
      return newConversionRateDoc;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

}
