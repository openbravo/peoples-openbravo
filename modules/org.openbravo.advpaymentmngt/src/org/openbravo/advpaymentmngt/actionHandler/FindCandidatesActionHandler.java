/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.advpaymentmngt.utility.FIN_CandidateRecord;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindCandidatesActionHandler extends BaseActionHandler {
  private static Logger log = LoggerFactory.getLogger(FindCandidatesActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
            .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId());
    ConnectionProvider conn = new DalConnectionProvider();
    try {
      final JSONObject jsonData = new JSONObject(data);
      String strBankStatementLineId = jsonData.getString("bankStatementLineId");
      String strFinCandidateRecordId = jsonData.getString("finCandidateRecordId");
      FIN_BankStatementLine bsline = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strBankStatementLineId);
      FIN_CandidateRecord candidateRecord = OBDal.getInstance().get(FIN_CandidateRecord.class,
          strFinCandidateRecordId);
      FIN_FinaccTransaction finTrans = bsline.getFinancialAccountTransaction();

      if (finTrans == null) {
        if (candidateRecord.getTransaction() != null) {
          finTrans = candidateRecord.getTransaction();
        } else if (candidateRecord.getPayment() != null) {
          finTrans = getPaymentTransaction(candidateRecord);
        } else if (candidateRecord.getInvoice() != null) {
          finTrans = getInvoiceTransaction(bsline, candidateRecord);
        } else if (candidateRecord.getOrder() != null) {
          finTrans = getOrderTransaction(bsline, candidateRecord);
        } else {
          finTrans = createCredit(bsline);
        }
      }

      FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal.getInstance()
          .get(FIN_FinancialAccount.class, finTrans.getAccount().getId()), "N");
      if (reconciliation == null) {
        reconciliation = MatchTransactionDao.addNewReconciliation(conn, vars, finTrans.getAccount()
            .getId());
      }

      if (finTrans != null) {
        APRM_MatchingUtility.matchBankStatementLine(strBankStatementLineId, finTrans.getId(),
            reconciliation.getId(), null);
      }

      String message = "Ok";
      errorMessage.put("severity", "success");
      errorMessage.put("text", message);
      result.put("message", errorMessage);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", message);
        result.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  /**
   * Creates a transaction record for a candidate that has a payment
   * 
   * @param candidateRecord
   *          Candidate record from which a transaction is needed
   * @return
   */
  FIN_FinaccTransaction getPaymentTransaction(FIN_CandidateRecord candidateRecord) {
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    FIN_FinaccTransaction transaction = dao.getFinancialTransaction(candidateRecord.getPayment());
    // Flag transaction as created by algorithm
    transaction.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(transaction);
    OBDal.getInstance().flush();
    if (!transaction.isProcessed()) {
      TransactionsDao.process(transaction);
    }
    return transaction;
  }

  /**
   * Creates a transaction record and a payment for a candidate that has an invoice
   * 
   * @param bsline
   *          Bank Statement Line Id
   * @param candidateRecord
   *          Candidate record from which a transaction is needed
   * @return
   */
  FIN_FinaccTransaction getInvoiceTransaction(FIN_BankStatementLine bsline,
      FIN_CandidateRecord candidateRecord) {
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    BigDecimal amount = bsline.getCramount().subtract(bsline.getDramount());
    boolean isReceipt = amount.signum() > 0;
    HashMap<String, BigDecimal> hm = new HashMap<String, BigDecimal>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    OBCriteria<FIN_PaymentSchedule> obcPS = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    obcPS.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, candidateRecord.getInvoice()));
    final FIN_PaymentSchedule paymentSchedule = obcPS.list().get(0);
    final List<FIN_PaymentScheduleDetail> paymentScheduleDetails = paymentSchedule
        .getFINPaymentScheduleDetailInvoicePaymentScheduleList();

    DocumentType docType = FIN_Utility.getDocumentType(paymentScheduleDetails.get(0)
        .getOrganization(), isReceipt ? "ARR" : "APP");
    // get DocumentNo
    for (int i = 0; i < paymentScheduleDetails.size(); i++) {
      hm.put(paymentScheduleDetails.get(i).getId(), paymentScheduleDetails.get(i).getAmount());
      totalAmount = totalAmount.add(paymentScheduleDetails.get(i).getAmount());
    }

    String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
        docType.getTable() != null ? docType.getTable().getDBTableName() : "");
    FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt, docType,
        strPaymentDocumentNo, candidateRecord.getInvoice().getBusinessPartner(), paymentSchedule
            .getFinPaymentmethod(), bsline.getBankStatement().getAccount(), totalAmount.toString(),
        bsline.getTransactionDate(), paymentScheduleDetails.get(0).getOrganization(), bsline
            .getReferenceNo(), paymentScheduleDetails, hm, false, false);
    payment.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(payment);
    OBDal.getInstance().flush();
    try {
      ConnectionProvider conn = new DalConnectionProvider();
      FIN_AddPayment.processPayment(new VariablesSecureApp(OBContext.getOBContext().getUser()
          .getId(), OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId()), conn,
          "P", payment);
    } catch (Exception e) {
      return null;
    }
    FIN_FinaccTransaction transaction = dao.getFinancialTransaction(payment);
    // Flag transaction as created by algorithm
    transaction.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(transaction);
    OBDal.getInstance().flush();
    if (!transaction.isProcessed()) {
      TransactionsDao.process(transaction);
    }
    return transaction;
  }

  /**
   * Creates a transaction record and a payment for a candidate that has an order
   * 
   * @param bsline
   *          Bank Statement Line Id
   * @param candidateRecord
   *          Candidate record from which a transaction is needed
   * @return
   */
  FIN_FinaccTransaction getOrderTransaction(FIN_BankStatementLine bsline,
      FIN_CandidateRecord candidateRecord) {
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    BigDecimal amount = bsline.getCramount().subtract(bsline.getDramount());
    boolean isReceipt = amount.signum() > 0;
    HashMap<String, BigDecimal> hm = new HashMap<String, BigDecimal>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    OBCriteria<FIN_PaymentSchedule> obcPS = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    obcPS.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, candidateRecord.getOrder()));
    final FIN_PaymentSchedule paymentSchedule = obcPS.list().get(0);
    final List<FIN_PaymentScheduleDetail> paymentScheduleDetails = paymentSchedule
        .getFINPaymentScheduleDetailOrderPaymentScheduleList();

    DocumentType docType = FIN_Utility.getDocumentType(paymentScheduleDetails.get(0)
        .getOrganization(), isReceipt ? "ARR" : "APP");
    // get DocumentNo
    for (int i = 0; i < paymentScheduleDetails.size(); i++) {
      hm.put(paymentScheduleDetails.get(i).getId(), paymentScheduleDetails.get(i).getAmount());
      totalAmount = totalAmount.add(paymentScheduleDetails.get(i).getAmount());
    }

    String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
        docType.getTable() != null ? docType.getTable().getDBTableName() : "");
    FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt, docType,
        strPaymentDocumentNo, candidateRecord.getOrder().getBusinessPartner(), paymentSchedule
            .getFinPaymentmethod(), bsline.getBankStatement().getAccount(), totalAmount.toString(),
        bsline.getTransactionDate(), paymentScheduleDetails.get(0).getOrganization(), bsline
            .getReferenceNo(), paymentScheduleDetails, hm, false, false);
    payment.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(payment);
    OBDal.getInstance().flush();
    try {
      ConnectionProvider conn = new DalConnectionProvider();
      FIN_AddPayment.processPayment(new VariablesSecureApp(OBContext.getOBContext().getUser()
          .getId(), OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId()), conn,
          "P", payment);
    } catch (Exception e) {
      return null;
    }
    FIN_FinaccTransaction transaction = dao.getFinancialTransaction(payment);
    // Flag transaction as created by algorithm
    transaction.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(transaction);
    OBDal.getInstance().flush();
    if (!transaction.isProcessed()) {
      TransactionsDao.process(transaction);
    }
    return transaction;
  }

  /**
   * Create a credit payment and a transaction for a candidate that has no payment, nor order or
   * invoice
   * 
   * @param bsline
   *          Bank Statement Line Id
   * @return
   */
  FIN_FinaccTransaction createCredit(FIN_BankStatementLine bsline) {
    BusinessPartner bp = bsline.getBusinessPartner();
    BigDecimal amount = bsline.getCramount().subtract(bsline.getDramount());
    boolean isReceipt = amount.signum() > 0;
    if (bp == null) {
      return null;
    }
    FIN_PaymentMethod pm = isReceipt ? bp.getPaymentMethod() : bp.getPOPaymentMethod();
    if (pm == null) {
      return null;
    }
    if (!getAllowedPaymentMethods(bsline.getBankStatement().getAccount(), isReceipt).contains(pm)) {
      return null;
    }
    PriceList priceList = isReceipt ? bp.getPriceList() : bp.getPurchasePricelist();
    if (priceList == null) {
      return null;
    }
    DocumentType docType = FIN_Utility.getDocumentType(bsline.getOrganization(), isReceipt ? "ARR"
        : "APP");
    // get DocumentNo
    String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
        docType.getTable() != null ? docType.getTable().getDBTableName() : "");
    FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt, docType,
        strPaymentDocumentNo, bp, pm, bsline.getBankStatement().getAccount(), amount.abs()
            .toString(), bsline.getTransactionDate(), bsline.getOrganization(), bsline
            .getReferenceNo(), new ArrayList<FIN_PaymentScheduleDetail>(),
        new HashMap<String, BigDecimal>(), false, false);
    // Flag payment as created by algorithm
    payment.setCreatedByAlgorithm(true);
    OBDal.getInstance().save(payment);
    OBDal.getInstance().flush();
    try {
      ConnectionProvider conn = new DalConnectionProvider();
      FIN_AddPayment.processPayment(new VariablesSecureApp(OBContext.getOBContext().getUser()
          .getId(), OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId()), conn,
          "P", payment);
    } catch (Exception e) {
      return null;
    }
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    FIN_FinaccTransaction transaction = dao.getFinancialTransaction(payment);
    // Flag transaction as created by algorithm
    transaction.setCreatedByAlgorithm(true);
    // transaction.isProcessed()
    OBDal.getInstance().save(transaction);
    OBDal.getInstance().flush();
    if (!transaction.isProcessed()) {
      TransactionsDao.process(transaction);
    }
    return transaction;
  }

  /**
   * 
   * 
   * @param account
   *          Financial Account Id
   * @param isReceipt
   *          Is Sales Transaction
   * @return
   */
  private List<FIN_PaymentMethod> getAllowedPaymentMethods(FIN_FinancialAccount account,
      boolean isReceipt) {
    List<FIN_PaymentMethod> allowedPaymentMethods = new ArrayList<FIN_PaymentMethod>();
    OBContext.setAdminMode();
    try {
      OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, account));
      if (isReceipt) {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINALLOW, true));
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYINEXECUTIONTYPE, "M"));
      } else {
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTALLOW, true));
        obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYOUTEXECUTIONTYPE, "M"));
      }
      for (FinAccPaymentMethod pm : obc.list()) {
        allowedPaymentMethods.add(pm.getPaymentMethod());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return allowedPaymentMethods;
  }
}