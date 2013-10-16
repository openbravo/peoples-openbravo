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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_ReconciliationProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;
  Set<FIN_FinaccTransaction> transactionsToBePosted = new HashSet<FIN_FinaccTransaction>();

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    OBContext.setAdminMode();
    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      String recordID = (String) bundle.getParams().get("FIN_Reconciliation_ID");
      // This code is kept to maintain compatibility with previous tab which was built
      // on to of a view
      if (recordID == null || "".equals(recordID)) {
        recordID = (String) bundle.getParams().get("Aprm_Reconciliation_V_ID");
      }
      final FIN_Reconciliation reconciliation = dao.getObject(FIN_Reconciliation.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();

      reconciliation.setProcessNow(true);
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
      if (strAction.equals("P")) {
        // Check lines exist
        if (reconciliation.getFINReconciliationLineVList().size() == 0) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_ReconciliationNoLines@" + ": " + reconciliation.getDocumentNo()));
          bundle.setResult(msg);
          return;
        } else {
          if (getDocumentConfirmation(conProvider, recordID)) {
            for (FIN_ReconciliationLine_v recLine : reconciliation.getFINReconciliationLineVList()) {
              boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(recLine
                  .getFinancialAccountTransaction().getReconciliation().TABLE_NAME, recLine
                  .getFinancialAccountTransaction().getReconciliation().getId(), recLine
                  .getFinancialAccountTransaction().getReconciliation().TABLE_NAME + "_ID", "LE");
              if (!FIN_Utility.isPeriodOpen(recLine.getFinancialAccountTransaction().getClient()
                  .getId(), AcctServer.DOCTYPE_Reconciliation, recLine
                  .getFinancialAccountTransaction().getOrganization().getId(),
                  OBDateUtils.formatDate(recLine.getFinancialAccountTransaction().getDateAcct()))
                  && orgLegalWithAccounting) {
                msg.setType("Error");
                msg.setTitle(Utility.messageBD(conProvider, "Error", language));
                msg.setMessage(String.format(Utility.parseTranslation(conProvider, vars, language,
                    "@APRM_PeriodNotAvailableClearedItem@"), recLine.getIdentifier()));
                bundle.setResult(msg);
                OBDal.getInstance().rollbackAndClose();
                return;
              }
            }
          } else {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", language));
            msg.setMessage(String.format(Utility.parseTranslation(conProvider, vars, language,
                "@DocumentDisabled@")));
            bundle.setResult(msg);
            OBDal.getInstance().rollbackAndClose();
            return;

          }
        }
        reconciliation.setProcessed(true);
        reconciliation.setAPRMProcessReconciliation("R");
        reconciliation.setAprmProcessRec("R");
        reconciliation.setDocumentStatus("CO");
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();

        // ***********************
        // Reactivate Reconciliation
        // ***********************
      } else if (strAction.equals("R")) {
        // Already Posted Document
        if ("Y".equals(reconciliation.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language, "@PostedDocument@"
              + ": " + reconciliation.getDocumentNo()));
          bundle.setResult(msg);
          return;
        }
        // Transaction exists
        if (!isLastReconciliation(reconciliation)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", language));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, language,
              "@APRM_FutureReconciliationExists@"));
          bundle.setResult(msg);
          return;
        }
        reconciliation.setProcessed(false);
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
        reconciliation.setDocumentStatus("DR");
        reconciliation.setAPRMProcessReconciliation("P");
        reconciliation.setAprmProcessRec("P");
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
      }
      reconciliation.setProcessNow(false);
      OBDal.getInstance().save(reconciliation);
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
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_FinaccTransaction> getTransactionList(FIN_Reconciliation reconciliation) {
    OBContext.setAdminMode();
    List<FIN_FinaccTransaction> transactions = null;
    try {
      OBCriteria<FIN_FinaccTransaction> trans = OBDal.getInstance().createCriteria(
          FIN_FinaccTransaction.class);
      trans.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_RECONCILIATION, reconciliation));
      trans.setFilterOnReadableClients(false);
      trans.setFilterOnReadableOrganization(false);
      transactions = trans.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return transactions;
  }

  /*
   * Checks if this step (Reconciliation) is configured to generate accounting for the selected
   * financial account
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          strRecordId);
      List<FIN_FinaccTransaction> transactions = getTransactionList(reconciliation);
      List<FIN_FinancialAccountAccounting> accounts = reconciliation.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinaccTransaction transaction : transactions) {
        FIN_Payment payment = transaction.getFinPayment();
        // If payment exists, check Payment Method + financial Account Configuration
        if (payment != null) {
          OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
              FinAccPaymentMethod.class);
          obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT,
              reconciliation.getAccount()));
          obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
              payment.getPaymentMethod()));
          obCriteria.setFilterOnReadableClients(false);
          obCriteria.setFilterOnReadableOrganization(false);
          List<FinAccPaymentMethod> lines = obCriteria.list();
          for (FIN_FinancialAccountAccounting account : accounts) {
            if (payment.isReceipt()) {
              if (("INT").equals(lines.get(0).getINUponClearingUse())
                  && account.getInTransitPaymentAccountIN() != null) {
                transactionsToBePosted.add(transaction);
              } else if (("DEP").equals(lines.get(0).getINUponClearingUse())
                  && account.getDepositAccount() != null) {
                transactionsToBePosted.add(transaction);
              } else if (("CLE").equals(lines.get(0).getINUponClearingUse())
                  && account.getClearedPaymentAccount() != null) {
                transactionsToBePosted.add(transaction);
              }
            } else {
              if (("INT").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getFINOutIntransitAcct() != null) {
                transactionsToBePosted.add(transaction);
              } else if (("WIT").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getWithdrawalAccount() != null) {
                transactionsToBePosted.add(transaction);
              } else if (("CLE").equals(lines.get(0).getOUTUponClearingUse())
                  && account.getClearedPaymentAccountOUT() != null) {
                transactionsToBePosted.add(transaction);
              }
            }
          }
        } else if (transaction.getGLItem() != null) {
          for (FIN_FinancialAccountAccounting account : accounts) {
            if ("BPD".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccount() != null) {
              transactionsToBePosted.add(transaction);
            } else if ("BPW".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              transactionsToBePosted.add(transaction);
            }
          }
        } else {
          for (FIN_FinancialAccountAccounting account : accounts) {
            if ("BF".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              transactionsToBePosted.add(transaction);
            }
          }
        }
      }
    } catch (Exception e) {
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
    if (transactionsToBePosted.size() == 0) {
      return false;
    }
    return true;
  }

  private boolean isLastReconciliation(FIN_Reconciliation reconciliation) {
    final OBCriteria<FIN_Reconciliation> obc = OBDal.getInstance().createCriteria(
        FIN_Reconciliation.class);
    obc.add(Restrictions.ge(FIN_Reconciliation.PROPERTY_ENDINGDATE, reconciliation.getEndingDate()));
    obc.add(Restrictions.gt(FIN_Reconciliation.PROPERTY_CREATIONDATE,
        reconciliation.getCreationDate()));
    obc.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, reconciliation.getAccount()));
    obc.addOrder(Order.asc(FIN_Reconciliation.PROPERTY_ENDINGDATE));
    obc.addOrder(Order.asc(FIN_Reconciliation.PROPERTY_CREATIONDATE));
    final List<FIN_Reconciliation> reconciliations = obc.list();
    if (reconciliations.size() == 0) {
      return true;
    } else if (reconciliations.size() == 1) {
      if (reconciliations.get(0).isProcessed()) {
        return false;
      } else if (reconciliations.get(0).getFINReconciliationLineVList().size() == 0) {
        FIN_Reconciliation reconciliationToDelete = OBDal.getInstance().get(
            FIN_Reconciliation.class, reconciliations.get(0).getId());
        for (FIN_BankStatement bst : reconciliationToDelete.getFINBankStatementList()) {
          FIN_BankStatement bankstatement = OBDal.getInstance().get(FIN_BankStatement.class,
              bst.getId());
          bankstatement.setFINReconciliation(null);
          OBDal.getInstance().save(bankstatement);
          OBDal.getInstance().flush();
        }
        OBDal.getInstance().remove(reconciliationToDelete);
        OBDal.getInstance().flush();
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
