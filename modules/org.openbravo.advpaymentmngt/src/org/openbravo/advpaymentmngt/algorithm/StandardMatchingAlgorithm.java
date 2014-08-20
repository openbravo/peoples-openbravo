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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.algorithm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.advpaymentmngt.utility.FIN_MatchedTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingAlgorithm;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.advpaymentmngt.utility.Value;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class StandardMatchingAlgorithm implements FIN_MatchingAlgorithm {

  public FIN_MatchedTransaction match(FIN_BankStatementLine line,
      List<FIN_FinaccTransaction> excluded) throws ServletException {

    MatchingAlgorithm algorithm = line.getBankStatement().getAccount().getMatchingAlgorithm();

    Date transactionDate = (algorithm.isMatchtransactiondate()) ? line.getTransactionDate() : null;
    String reference = (algorithm.isMatchreference()) ? line.getReferenceNo() : "";

    List<FIN_FinaccTransaction> transactions = new ArrayList<FIN_FinaccTransaction>();
    if (line.getGLItem() != null) {
      transactions = MatchTransactionDao.getMatchingGLItemTransaction(line.getBankStatement()
          .getAccount().getId(), line.getGLItem(), line.getTransactionDate(),
          (line.getCramount().subtract(line.getDramount())), excluded);
      if (transactions.isEmpty()) {
        transactions = MatchTransactionDao.getMatchingGLItemTransaction(line.getBankStatement()
            .getAccount().getId(), line.getGLItem(), null,
            (line.getCramount().subtract(line.getDramount())), excluded);
        if (!transactions.isEmpty()) {
          return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.WEAK);
        }
      } else {
        return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.STRONG);
      }
    }
    if (algorithm.isMatchbpname()) {
      transactions = MatchTransactionDao.getMatchingFinancialTransaction(line.getBankStatement()
          .getAccount().getId(), transactionDate, reference,
          (line.getCramount().subtract(line.getDramount())), line.getBpartnername(), excluded);
    } else {
      transactions = MatchTransactionDao.getMatchingFinancialTransaction(line.getBankStatement()
          .getAccount().getId(), transactionDate, reference,
          (line.getCramount().subtract(line.getDramount())), excluded);
    }

    if (!transactions.isEmpty())
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.STRONG);
    if (algorithm.isMatchtransactiondate()) {
      transactions = MatchTransactionDao.getMatchingFinancialTransaction(line.getBankStatement()
          .getAccount().getId(), line.getTransactionDate(),
          line.getCramount().subtract(line.getDramount()), excluded);
    } else {
      transactions = MatchTransactionDao.getMatchingFinancialTransaction(line.getBankStatement()
          .getAccount().getId(), line.getCramount().subtract(line.getDramount()), excluded);
    }
    if (!transactions.isEmpty())
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.WEAK);

    return new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
  }

  public void unmatch(FIN_FinaccTransaction _transaction) throws ServletException {
    if (_transaction == null)
      return;
    FIN_Payment payment = _transaction.getFinPayment();
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
            .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId());
    ConnectionProvider conn = new DalConnectionProvider();
    if (_transaction.isCreatedByAlgorithm()) {
      removeTransaction(vars, conn, _transaction);
    } else
      return;
    if (payment.isCreatedByAlgorithm()) {
      removePayment(vars, conn, payment);
    }
    return;
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
  void removeTransaction(VariablesSecureApp vars, ConnectionProvider conn,
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
  void removePayment(VariablesSecureApp vars, ConnectionProvider conn, FIN_Payment payment) {
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
