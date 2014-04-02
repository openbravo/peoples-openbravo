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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_TransactionProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;
  /** Transaction type - Financial Account */
  public static final String TRXTYPE_BPDeposit = "BPD";
  public static final String TRXTYPE_BPWithdrawal = "BPW";
  public static final String TRXTYPE_BankFee = "BF";

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
      final String recordID = (String) bundle.getParams().get("Fin_FinAcc_Transaction_ID");
      final FIN_FinaccTransaction transaction = dao
          .getObject(FIN_FinaccTransaction.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();

      OBContext.setAdminMode();
      try {
        if (strAction.equals("P")) {
          // ***********************
          // Process Transaction
          // ***********************
          boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(transaction.TABLE_NAME,
              transaction.getId(), transaction.TABLE_NAME + "_ID", "LE");
          boolean documentEnabled = getDocumentConfirmation(conProvider, transaction.getId());
          if (documentEnabled
              && !FIN_Utility.isPeriodOpen(transaction.getClient().getId(),
                  AcctServer.DOCTYPE_FinAccTransaction, transaction.getOrganization().getId(),
                  OBDateUtils.formatDate(transaction.getDateAcct())) && orgLegalWithAccounting) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@PeriodNotAvailable@"));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;
          }
          final FIN_FinancialAccount financialAccount = transaction.getAccount();
          financialAccount.setCurrentBalance(financialAccount.getCurrentBalance().add(
              transaction.getDepositAmount().subtract(transaction.getPaymentAmount())));
          transaction.setProcessed(true);
          FIN_Payment payment = transaction.getFinPayment();
          if (payment != null) {
            if (transaction.getBusinessPartner() == null) {
              transaction.setBusinessPartner(payment.getBusinessPartner());
            }
            payment.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
            transaction.setStatus(payment.isReceipt() ? "RDNC" : "PWNC");
            OBDal.getInstance().save(payment);
            if (transaction.getDescription() == null || "".equals(transaction.getDescription())) {
              transaction.setDescription(payment.getDescription());
            }
            Boolean invoicePaidold = false;
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                invoicePaidold = psd.isInvoicePaid();
                if (!invoicePaidold) {
                  if ((FIN_Utility.invoicePaymentStatus(payment).equals(payment.getStatus()))) {
                    psd.setInvoicePaid(true);
                  }
                  if (psd.isInvoicePaid()) {
                    FIN_Utility.updatePaymentAmounts(psd);
                  }
                  OBDal.getInstance().save(psd);
                }
              }
            }
            FIN_Utility.updateBusinessPartnerCredit(payment);
          } else {
            transaction.setStatus(transaction.getDepositAmount().compareTo(
                transaction.getPaymentAmount()) > 0 ? "RDNC" : "PWNC");
          }
          if (transaction.getForeignCurrency() != null
              && !transaction.getCurrency().equals(transaction.getForeignCurrency())
              && getConversionRateDocument(transaction).size() == 0) {
            insertConversionRateDocument(transaction);
          }
          OBDal.getInstance().save(financialAccount);
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();
          bundle.setResult(msg);

        } else if (strAction.equals("R")) {
          // ***********************
          // Reactivate Transaction
          // ***********************
          // Already Posted Document
          if ("Y".equals(transaction.getPosted())) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
                + ": " + transaction.getIdentifier()));
            bundle.setResult(msg);
            return;
          }
          // Already Reconciled
          if (transaction.getReconciliation() != null || "RPPC".equals(transaction.getStatus())) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
                "@APRM_ReconciledDocument@" + ": " + transaction.getIdentifier()));
            bundle.setResult(msg);
            return;
          }
          // Remove conversion rate at document level for the given transaction
          OBContext.setAdminMode();
          try {
            OBCriteria<ConversionRateDoc> obc = OBDal.getInstance().createCriteria(
                ConversionRateDoc.class);
            obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION,
                transaction));
            for (ConversionRateDoc conversionRateDoc : obc.list()) {
              OBDal.getInstance().remove(conversionRateDoc);
            }
            OBDal.getInstance().flush();
          } finally {
            OBContext.restorePreviousMode();
          }
          transaction.setProcessed(false);
          final FIN_FinancialAccount financialAccount = transaction.getAccount();
          financialAccount.setCurrentBalance(financialAccount.getCurrentBalance()
              .subtract(transaction.getDepositAmount()).add(transaction.getPaymentAmount()));
          OBDal.getInstance().save(financialAccount);
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();
          FIN_Payment payment = transaction.getFinPayment();
          if (payment != null) {
            Boolean invoicePaidold = false;
            for (FIN_PaymentDetail pd : payment.getFINPaymentDetailList()) {
              for (FIN_PaymentScheduleDetail psd : pd.getFINPaymentScheduleDetailList()) {
                invoicePaidold = psd.isInvoicePaid();
                if (invoicePaidold) {
                  boolean restore = (FIN_Utility.seqnumberpaymentstatus(payment.getStatus())) == (FIN_Utility
                      .seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment)));
                  if (restore) {
                    FIN_Utility.restorePaidAmounts(psd);
                  }
                }
              }
            }
            payment.setStatus(payment.isReceipt() ? "RPR" : "PPM");
            transaction.setStatus(payment.isReceipt() ? "RPR" : "PPM");
            OBDal.getInstance().save(payment);
          } else {
            transaction.setStatus(transaction.getDepositAmount().compareTo(
                transaction.getPaymentAmount()) > 0 ? "RPR" : "PPM");
          }
          OBDal.getInstance().save(transaction);
          OBDal.getInstance().flush();
          bundle.setResult(msg);

        }
        bundle.setResult(msg);
      } finally {
        OBContext.restorePreviousMode();
      }
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

  private List<ConversionRateDoc> getConversionRateDocument(FIN_FinaccTransaction transaction) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ConversionRateDoc> obc = OBDal.getInstance().createCriteria(
          ConversionRateDoc.class);
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, transaction.getForeignCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, transaction.getCurrency()));
      obc.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION, transaction));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ConversionRateDoc insertConversionRateDocument(FIN_FinaccTransaction transaction) {
    OBContext.setAdminMode();
    try {
      ConversionRateDoc newConversionRateDoc = OBProvider.getInstance()
          .get(ConversionRateDoc.class);
      newConversionRateDoc.setOrganization(transaction.getOrganization());
      newConversionRateDoc.setCurrency(transaction.getForeignCurrency());
      newConversionRateDoc.setToCurrency(transaction.getCurrency());
      newConversionRateDoc.setRate(transaction.getForeignConversionRate());
      newConversionRateDoc.setForeignAmount(transaction.getForeignAmount());
      newConversionRateDoc.setFinancialAccountTransaction(OBDal.getInstance().get(
          APRM_FinaccTransactionV.class, transaction.getId()));
      OBDal.getInstance().save(newConversionRateDoc);
      OBDal.getInstance().flush();
      return newConversionRateDoc;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /*
   * Checks if this step is configured to generate accounting for the selected financial account
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          strRecordId);
      List<FIN_FinancialAccountAccounting> accounts = transaction.getAccount()
          .getFINFinancialAccountAcctList();
      FIN_Payment payment = transaction.getFinPayment();
      if (payment != null) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT,
            transaction.getAccount()));
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
            payment.getPaymentMethod()));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if (payment.isReceipt()) {
            if (("INT").equals(lines.get(0).getUponDepositUse())
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (("DEP").equals(lines.get(0).getUponDepositUse())
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponDepositUse())
                && account.getClearedPaymentAccount() != null)
              confirmation = true;

            else if (null == (lines.get(0).getUponDepositUse())
                && null == (lines.get(0).getINUponClearingUse())
                && transaction.getAccount() != payment.getAccount()) {
              confirmation = true;
            }
          } else {
            if (("INT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (("WIT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponWithdrawalUse())
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
        }

      } else {
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if ((TRXTYPE_BPDeposit.equals(transaction.getTransactionType()) && account
              .getDepositAccount() != null)
              || (TRXTYPE_BPWithdrawal.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null)
              || (TRXTYPE_BankFee.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null))
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

}
