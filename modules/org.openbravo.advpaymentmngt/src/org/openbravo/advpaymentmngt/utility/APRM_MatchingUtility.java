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

package org.openbravo.advpaymentmngt.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_ReconciliationProcess;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.cashmgmt.BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLineTemp;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APRM_MatchingUtility {
  private static final Logger log4j = LoggerFactory.getLogger(APRM_MatchingUtility.class);
  private static AdvPaymentMngtDao dao;

  /**
   * Get reconciliation lines of a reconciliation that had been matched manually
   * 
   * @param reconciliation
   *          Reconciliation to be checked
   * @return
   */
  public static List<FIN_FinaccTransaction> getManualReconciliationLines(
      FIN_Reconciliation reconciliation) {
    List<FIN_FinaccTransaction> result = new ArrayList<FIN_FinaccTransaction>();
    OBContext.setAdminMode();
    try {
      final OBCriteria<FIN_ReconciliationLine_v> obc = OBDal.getInstance().createCriteria(
          FIN_ReconciliationLine_v.class);
      obc.add(Restrictions.eq(FIN_ReconciliationLine_v.PROPERTY_RECONCILIATION, reconciliation));
      obc.add(Restrictions.isNull(FIN_ReconciliationLine_v.PROPERTY_BANKSTATEMENTLINE));
      obc.setMaxResults(1);
      for (FIN_ReconciliationLine_v line : obc.list()) {
        result.add(line.getFinancialAccountTransaction());
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Fix wrong financial transaction of a reconciliation
   * 
   * @param mixedLine
   *          The financial transaction to be fixed
   */
  public static void fixMixedLine(FIN_FinaccTransaction mixedLine) {
    boolean isReceipt = mixedLine.getDepositAmount().compareTo(BigDecimal.ZERO) != 0;
    mixedLine.setStatus(isReceipt ? "RDNC" : "PWNC");
    mixedLine.setReconciliation(null);
    OBDal.getInstance().save(mixedLine);
    if (mixedLine.getFinPayment() != null) {
      mixedLine.getFinPayment().setStatus(isReceipt ? "RDNC" : "PWNC");
      OBDal.getInstance().save(mixedLine.getFinPayment());
    }
  }

  /**
   * Process to process a reconciliation
   * 
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param reconciliation
   *          Reconciliation that needs to be processed
   * @return
   * @throws Exception
   */
  public static OBError processReconciliation(ConnectionProvider conn, VariablesSecureApp vars,
      String strAction, FIN_Reconciliation reconciliation) throws Exception {
    ProcessBundle pb = new ProcessBundle("FF8080812E2F8EAE012E2F94CF470014", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("FIN_Reconciliation_ID", reconciliation.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_ReconciliationProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

  /**
   * Match a bank statement line with a financial account transaction
   * 
   * @param strFinBankStatementLineId
   *          Bank Statement Line Id
   * @param strFinancialTransactionId
   *          Financial Account Transaction Id
   * @param strReconciliationId
   *          Reconciliation Id
   * @param matchLevel
   *          Match Level
   */
  public static void matchBankStatementLine(String strFinBankStatementLineId,
      String strFinancialTransactionId, String strReconciliationId, String matchLevel) {
    OBContext.setAdminMode(false);
    try {
      FIN_BankStatementLine bsl = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strFinBankStatementLineId);
      OBDal.getInstance().getSession().buildLockRequest(LockOptions.NONE)
          .lock(BankStatementLine.ENTITY_NAME, bsl);
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          strFinancialTransactionId);
      if (transaction != null) {
        if (bsl.getFinancialAccountTransaction() != null) {
          log4j.error("Bank Statement Line Already Matched: " + bsl.getIdentifier());
          unmatch(bsl);
        }
        bsl.setFinancialAccountTransaction(transaction);
        if (matchLevel == null || "".equals(matchLevel)) {
          matchLevel = FIN_MatchedTransaction.MANUALMATCH;
        }
        bsl.setMatchingtype(matchLevel);
        transaction.setStatus("RPPC");
        transaction.setReconciliation(MatchTransactionDao.getObject(FIN_Reconciliation.class,
            strReconciliationId));
        if (transaction.getFinPayment() != null) {
          transaction.getFinPayment().setStatus("RPPC");
        }
        OBDal.getInstance().save(transaction);
        OBDal.getInstance().save(bsl);
        OBDal.getInstance().flush();
        OBDal.getInstance().getConnection().commit();
      }
    } catch (Exception e) {
      log4j.error("Error during matchBankStatementLine");
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Remove the match of a bank statement line with a transaction
   * 
   * @param bsline
   *          Bank Statement Line to be unmatched from a transaction
   */
  public static void unmatch(FIN_BankStatementLine bsline) {
    OBContext.setAdminMode();
    try {
      bsline = OBDal.getInstance().get(FIN_BankStatementLine.class, bsline.getId());
      FIN_FinaccTransaction finTrans = bsline.getFinancialAccountTransaction();
      // if (finTrans == null) {
      // String strTransactionId = vars.getStringParameter("inpFinancialTransactionId_"
      // + bsline.getId());
      // if (strTransactionId != null && !"".equals(strTransactionId)) {
      // finTrans = OBDal.getInstance().get(FIN_FinaccTransaction.class, strTransactionId);
      // }
      // }
      if (finTrans != null) {
        finTrans.setReconciliation(null);
        finTrans.setStatus((finTrans.getDepositAmount().subtract(finTrans.getPaymentAmount())
            .signum() == 1) ? "RDNC" : "PWNC");
        bsline.setFinancialAccountTransaction(null);
        OBDal.getInstance().save(finTrans);
        // OBDal.getInstance().flush();
      }
      bsline.setMatchingtype(null);
      OBDal.getInstance().save(bsline);
      // OBDal.getInstance().flush();

      // merge if the bank statement line was split before
      mergeBankStatementLine(bsline);

      if (finTrans != null) {
        if (finTrans.getFinPayment() != null) {
          finTrans.getFinPayment().setStatus(
              (finTrans.getFinPayment().isReceipt()) ? "RDNC" : "PWNC");
        }
        boolean isReceipt = false;
        if (finTrans.getFinPayment() != null) {
          isReceipt = finTrans.getFinPayment().isReceipt();
        } else {
          isReceipt = finTrans.getDepositAmount().compareTo(finTrans.getPaymentAmount()) > 0;
        }
        finTrans.setStatus(isReceipt ? "RDNC" : "PWNC");
        finTrans.setReconciliation(null);
        OBDal.getInstance().save(finTrans);
        // OBDal.getInstance().flush();
      }
      // Execute un-matching logic defined by algorithm
      MatchingAlgorithm ma = bsline.getBankStatement().getAccount().getMatchingAlgorithm();
      FIN_MatchingTransaction matchingTransaction = new FIN_MatchingTransaction(
          ma.getJavaClassName());
      matchingTransaction.unmatch(finTrans);

      // Do not allow bank statement lines of 0
      if (bsline.getCramount().compareTo(BigDecimal.ZERO) == 0
          && bsline.getDramount().compareTo(BigDecimal.ZERO) == 0) {
        FIN_BankStatement bs = bsline.getBankStatement();
        bs.setProcessed(false);
        OBDal.getInstance().save(bs);
        OBDal.getInstance().flush();
        OBDal.getInstance().remove(bsline);
        OBDal.getInstance().flush();
        bs.setProcessed(true);
        OBDal.getInstance().save(bs);
        OBDal.getInstance().flush();
      }
      OBDal.getInstance().getConnection().commit();
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Merges given bank statement line with other bank statement lines with the same line number and
   * not matched with any transaction.
   * 
   * @param bsline
   *          Bank Statement Line.
   */
  public static void mergeBankStatementLine(FIN_BankStatementLine bsline) {
    BigDecimal totalCredit = bsline.getCramount();
    BigDecimal totalDebit = bsline.getDramount();
    FIN_BankStatement bs = bsline.getBankStatement();
    OBCriteria<FIN_BankStatementLine> obc = OBDal.getInstance().createCriteria(
        FIN_BankStatementLine.class);
    obc.add(Restrictions.eq(FIN_BankStatementLine.PROPERTY_BANKSTATEMENT, bsline.getBankStatement()));
    obc.add(Restrictions.eq(FIN_BankStatementLine.PROPERTY_LINENO, bsline.getLineNo()));
    obc.add(Restrictions.ne(FIN_BankStatementLine.PROPERTY_ID, bsline.getId()));
    obc.add(Restrictions.isNull(FIN_BankStatementLine.PROPERTY_FINANCIALACCOUNTTRANSACTION));

    if (obc.list().size() > 0) {
      bs.setProcessed(false);
      OBDal.getInstance().save(bs);
      OBDal.getInstance().flush();

      for (FIN_BankStatementLine bsl : obc.list()) {
        totalCredit = totalCredit.add(bsl.getCramount());
        totalDebit = totalDebit.add(bsl.getDramount());
        for (FIN_ReconciliationLineTemp tempbsline : getRecTempLines(bsl)) {
          tempbsline.setBankStatementLine(bsline);
          OBDal.getInstance().save(tempbsline);
        }
        OBDal.getInstance().remove(bsl);
      }

      if (totalCredit.compareTo(BigDecimal.ZERO) != 0 && totalDebit.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal total = totalCredit.subtract(totalDebit);
        if (total.compareTo(BigDecimal.ZERO) == -1) {
          bsline.setCramount(BigDecimal.ZERO);
          bsline.setDramount(total.abs());
        } else {
          bsline.setCramount(total);
          bsline.setDramount(BigDecimal.ZERO);
        }
      } else {
        bsline.setCramount(totalCredit);
        bsline.setDramount(totalDebit);
      }

      OBDal.getInstance().save(bsline);
      OBDal.getInstance().flush();

      bs.setProcessed(true);
      OBDal.getInstance().save(bs);
      OBDal.getInstance().flush();
    }

  }

  /**
   * This method retrieves all the reconciliation snapshot lines linked to the given bank statement
   * line.
   * 
   * @param bsline
   *          Bank Statement Line.
   * @return All the reconciliation snapshot lines linked to the given bank statement line.
   */
  public static List<FIN_ReconciliationLineTemp> getRecTempLines(FIN_BankStatementLine bsline) {
    OBContext.setAdminMode();
    try {
      final OBCriteria<FIN_ReconciliationLineTemp> obc = OBDal.getInstance().createCriteria(
          FIN_ReconciliationLineTemp.class);
      obc.add(Restrictions.eq(FIN_ReconciliationLineTemp.PROPERTY_BANKSTATEMENTLINE, bsline));
      return obc.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Removes a financial account transaction
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param transaction
   *          Financial Account Transaction to be removed
   */
  public static void removeTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_FinaccTransaction transaction) {
    final String FIN_FINACC_TRANSACTION_TABLE = "4D8C3B3C31D1410DA046140C9F024D17";
    try {
      if ("Y".equals(transaction.getPosted())) {
        List<AccountingFact> accountingEntries = FIN_Utility.getAllInstances(
            AccountingFact.class,
            false,
            false,
            new Value(AccountingFact.PROPERTY_TABLE, OBDal.getInstance().get(Table.class,
                FIN_FINACC_TRANSACTION_TABLE)), new Value(AccountingFact.PROPERTY_RECORDID,
                transaction.getId()));
        for (AccountingFact accountingEntry : accountingEntries) {
          OBDal.getInstance().remove(accountingEntry);
          OBDal.getInstance().flush();
        }
        transaction.setPosted("N");
        OBDal.getInstance().save(transaction);
        OBDal.getInstance().flush();
      }
      OBError msg = processTransaction(vars, conn, "R", transaction);
      if ("Success".equals(msg.getType())) {
        OBContext.setAdminMode();
        try {
          OBDal.getInstance().remove(transaction);
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } catch (Exception e) {
      throw new OBException("Process failed deleting the financial account Transaction", e);
    }
  }

  /**
   * Removes a payment
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param payment
   *          Payment to be removed
   */
  public static void removePayment(VariablesSecureApp vars, ConnectionProvider conn,
      FIN_Payment payment) {
    final String FIN_PAYMENT_TABLE = "D1A97202E832470285C9B1EB026D54E2";
    try {
      if ("Y".equals(payment.getPosted())) {
        List<AccountingFact> accountingEntries = FIN_Utility.getAllInstances(
            AccountingFact.class,
            false,
            false,
            new Value(AccountingFact.PROPERTY_TABLE, OBDal.getInstance().get(Table.class,
                FIN_PAYMENT_TABLE)), new Value(AccountingFact.PROPERTY_RECORDID, payment.getId()));
        for (AccountingFact accountingEntry : accountingEntries) {
          OBDal.getInstance().remove(accountingEntry);
          OBDal.getInstance().flush();
        }
        payment.setPosted("N");
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
      }
      OBError msg = FIN_AddPayment.processPayment(vars, conn, "R", payment);
      if ("Success".equals(msg.getType())) {
        OBContext.setAdminMode();
        try {
          OBDal.getInstance().remove(payment);
          OBDal.getInstance().flush();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } catch (Exception e) {
      throw new OBException("Process failed deleting payment", e);
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
  public static OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
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
