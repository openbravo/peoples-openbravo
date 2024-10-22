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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FundTransferRec;
import org.openbravo.advpaymentmngt.actionHandler.FundTransferRecordHookCaller;
import org.openbravo.advpaymentmngt.actionHandler.FundsTransferHookCaller;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.service.db.DalConnectionProvider;

public class FundsTransferUtility {
  private static final String BP_DEPOSIT = "BPD";
  private static final String BP_WITHDRAWAL = "BPW";
  private static final String BANK_FEE = "BF";
  private static final String PROCESS_ACTION = "P";

  /**
   * Create all the transactions for a funds transfer between two accounts
   * 
   * @param date
   *          used for the transactions, current date is used if this is null.
   * @param accountFrom
   *          source account.
   * @param accountTo
   *          target account.
   * @param glitem
   *          gl item used in transactions.
   * @param amount
   *          the transfer amount.
   * @param manualConversionRate
   *          conversion rate to override the system one.
   * @param bankFeeFrom
   *          fee on the source bank.
   * @param bankFeeTo
   *          fee on the target bank.
   * @param description
   *          description set by the user in the Funds Transfer Process.
   */
  public static APRM_FundTransferRec createTransfer(Date date, FIN_FinancialAccount accountFrom,
      FIN_FinancialAccount accountTo, GLItem glitem, BigDecimal amount,
      BigDecimal manualConversionRate, BigDecimal bankFeeFrom, BigDecimal bankFeeTo,
      String description) {
    List<FIN_FinaccTransaction> transactions = new ArrayList<FIN_FinaccTransaction>();
    Date trxDate = date;

    if (trxDate == null) {
      trxDate = new Date();
    }

    try {
      LineNumberUtil lineNoUtil = new LineNumberUtil();
      BigDecimal targetAmount = convertAmount(amount, accountFrom, accountTo, trxDate,
          manualConversionRate);

      // Source Account
      FIN_FinaccTransaction sourceTrx = createTransaction(accountFrom, BP_WITHDRAWAL, trxDate,
          glitem, amount, lineNoUtil, description);
      transactions.add(sourceTrx);
      if (bankFeeFrom != null && BigDecimal.ZERO.compareTo(bankFeeFrom) != 0) {
        FIN_FinaccTransaction sourceFeeTrx = createTransaction(accountFrom, BANK_FEE, trxDate,
            glitem, bankFeeFrom, lineNoUtil, description);
        transactions.add(sourceFeeTrx);
      }

      // Target Account
      FIN_FinaccTransaction destinationTrx = createTransaction(accountTo, BP_DEPOSIT, trxDate,
          glitem, targetAmount, lineNoUtil, sourceTrx, description);
      transactions.add(destinationTrx);

      if (bankFeeTo != null && BigDecimal.ZERO.compareTo(bankFeeTo) != 0) {
        FIN_FinaccTransaction destinationFeeTrx = createTransaction(accountTo, BANK_FEE, trxDate,
            glitem, bankFeeTo, lineNoUtil, sourceTrx, description);
        transactions.add(destinationFeeTrx);
      }

      OBDal.getInstance().flush();
      processTransactions(transactions);

      // Fund transfer record
      APRM_FundTransferRec fundTransferRecord = createFundTransferRecord(
          sourceTrx.getOrganization(), accountFrom, accountTo, sourceTrx, destinationTrx, trxDate,
          amount, description);

      OBDal.getInstance().save(fundTransferRecord);

      for (FIN_FinaccTransaction transaction : transactions) {
        transaction.setAprmFundTransferRec(fundTransferRecord);
        OBDal.getInstance().save(transaction);
      }

      // Needed the flush for the push api, if not it didn't found the register to export
      OBDal.getInstance().flush();

      WeldUtils.getInstanceFromStaticBeanManager(FundsTransferHookCaller.class)
          .executeHook(transactions);

      WeldUtils.getInstanceFromStaticBeanManager(FundTransferRecordHookCaller.class)
          .executeHook(fundTransferRecord);

      return fundTransferRecord;

    } catch (Exception e) {
      String message = OBMessageUtils.parseTranslation(e.getMessage());
      OBDal.getInstance().rollbackAndClose();
      throw new OBException(message, e);
    }
  }

  public static APRM_FundTransferRec completeTransfer(APRM_FundTransferRec fundTransferRecord) {
    String fundsTransferNo = fundTransferRecord.getDocumentNo();
    FIN_FinancialAccount accountFrom = fundTransferRecord.getFINAccFrom();
    FIN_FinancialAccount accountTo = fundTransferRecord.getFINAccTo();
    GLItem glitem = fundTransferRecord.getGLItem();
    BigDecimal amount = fundTransferRecord.getAmount();
    String description = fundTransferRecord.getDescription();

    List<FIN_FinaccTransaction> transactions = new ArrayList<FIN_FinaccTransaction>();
    Date trxDate = fundTransferRecord.getDate();

    if (trxDate == null) {
      trxDate = new Date();
    }

    try {
      LineNumberUtil lineNoUtil = new LineNumberUtil();
      BigDecimal targetAmount = convertAmount(amount, accountFrom, accountTo, trxDate, null);

      // Source Account
      FIN_FinaccTransaction sourceTrx = createTransaction(accountFrom, BP_WITHDRAWAL, trxDate,
          glitem, amount, lineNoUtil, description);
      transactions.add(sourceTrx);

      // Target Account
      FIN_FinaccTransaction destinationTrx = createTransaction(accountTo, BP_DEPOSIT, trxDate,
          glitem, targetAmount, lineNoUtil, sourceTrx, description);
      transactions.add(destinationTrx);

      OBDal.getInstance().flush();
      processTransactions(transactions);

      // Fund transfer record
      APRM_FundTransferRec newFundTransferRecord = completeFundTransferRecord(fundTransferRecord,
          fundsTransferNo, accountFrom, accountTo, sourceTrx, destinationTrx, trxDate, amount,
          description);

      OBDal.getInstance().save(newFundTransferRecord);

      for (FIN_FinaccTransaction transaction : transactions) {
        transaction.setAprmFundTransferRec(newFundTransferRecord);
        OBDal.getInstance().save(transaction);
      }

      // Needed the flush for the push api, if not it didn't found the register to export
      OBDal.getInstance().flush();

      WeldUtils.getInstanceFromStaticBeanManager(FundTransferRecordHookCaller.class)
          .executeHook(newFundTransferRecord);

      return fundTransferRecord;

    } catch (Exception e) {
      String message = OBMessageUtils.parseTranslation(e.getMessage());
      OBDal.getInstance().rollbackAndClose();
      throw new OBException(message, e);
    }
  }

  private static APRM_FundTransferRec createFundTransferRecord(Organization organization,
      FIN_FinancialAccount fromAccount, FIN_FinancialAccount toAccount,
      FIN_FinaccTransaction fromTrx, FIN_FinaccTransaction toTrx, Date trxDate, BigDecimal amount,
      String description) {
    APRM_FundTransferRec fundTransferRecord = OBProvider.getInstance()
        .get(APRM_FundTransferRec.class);

    String fundsTransferNo = Utility.getDocumentNo(new DalConnectionProvider(false),
        OBContext.getOBContext().getCurrentClient().getId(), "APRM_Fund_Transfer_Rec", true);

    if ("".equals(fundsTransferNo) && !existsSequence("APRM_Fund_Transfer_Rec")) {
      createSequence("APRM_Fund_Transfer_Rec");
      fundsTransferNo = Utility.getDocumentNo(new DalConnectionProvider(false),
          OBContext.getOBContext().getCurrentClient().getId(), "APRM_Fund_Transfer_Rec", true);
    }

    fundTransferRecord.setOrganization(organization);
    fundTransferRecord.setDocumentNo(fundsTransferNo);
    fundTransferRecord.setDate(trxDate);
    fundTransferRecord.setAmount(amount);
    fundTransferRecord.setFINAccFrom(fromAccount);
    fundTransferRecord.setFINAccTo(toAccount);
    fundTransferRecord.setFINAccTranFrom(fromTrx);
    fundTransferRecord.setFINAccTranTo(toTrx);
    fundTransferRecord.setStatus("CO");

    return fundTransferRecord;
  }

  private static APRM_FundTransferRec completeFundTransferRecord(
      APRM_FundTransferRec fundTransferRecord, String fundsTransferNo,
      FIN_FinancialAccount fromAccount, FIN_FinancialAccount toAccount,
      FIN_FinaccTransaction fromTrx, FIN_FinaccTransaction toTrx, Date trxDate, BigDecimal amount,
      String description) {

    fundTransferRecord.setDocumentNo(fundsTransferNo);
    fundTransferRecord.setDate(trxDate);
    fundTransferRecord.setAmount(amount);
    fundTransferRecord.setFINAccFrom(fromAccount);
    fundTransferRecord.setFINAccTo(toAccount);
    fundTransferRecord.setFINAccTranFrom(fromTrx);
    fundTransferRecord.setFINAccTranTo(toTrx);
    fundTransferRecord.setStatus("CO");

    return fundTransferRecord;
  }

  private static BigDecimal convertAmount(BigDecimal amount, FIN_FinancialAccount accountFrom,
      FIN_FinancialAccount accountTo, Date date, BigDecimal rate) {
    if (rate != null) {
      int precision = accountTo.getCurrency().getStandardPrecision().intValue();
      return amount.multiply(rate).setScale(precision, RoundingMode.HALF_UP);
    } else {
      return FinancialUtils.getConvertedAmount(amount, accountFrom.getCurrency(),
          accountTo.getCurrency(), date, accountFrom.getOrganization(), null);
    }
  }

  private static FIN_FinaccTransaction createTransaction(FIN_FinancialAccount account,
      String trxType, Date trxDate, GLItem glitem, BigDecimal amount, LineNumberUtil lineNoUtil,
      String description) {
    return createTransaction(account, trxType, trxDate, glitem, amount, lineNoUtil, null,
        description);
  }

  private static FIN_FinaccTransaction createTransaction(FIN_FinancialAccount account,
      String trxType, Date trxDate, GLItem glitem, BigDecimal amount, LineNumberUtil lineNoUtil,
      FIN_FinaccTransaction sourceTrx, String description) {
    FIN_FinaccTransaction trx = OBProvider.getInstance().get(FIN_FinaccTransaction.class);

    trx.setAccount(account);
    trx.setTransactionType(trxType);
    trx.setTransactionDate(trxDate);
    trx.setDateAcct(trxDate);
    trx.setGLItem(glitem);
    trx.setCurrency(account.getCurrency());
    if (BP_DEPOSIT.equalsIgnoreCase(trxType)) {
      trx.setDepositAmount(amount);
    } else {
      trx.setPaymentAmount(amount);
    }
    // If the user has access to the Organization of the Financial Account, the Transaction is
    // created for it. If not, the Organization of the context is used instead
    if (OBContext.getOBContext()
        .getWritableOrganizations()
        .contains(account.getOrganization().getId())) {
      trx.setOrganization(account.getOrganization());
    } else {
      trx.setOrganization(OBContext.getOBContext().getCurrentOrganization());
    }

    Long line = lineNoUtil.getNextLineNumber(account);
    trx.setLineNo(line);

    trx.setAprmFinaccTransOrigin(sourceTrx);
    if (StringUtils.isNotEmpty(description)) {
      trx.setDescription(description);
    } else {
      trx.setDescription(OBMessageUtils.messageBD("FundsTransfer"));
    }

    OBDal.getInstance().save(trx);

    return trx;
  }

  private static void processTransactions(List<FIN_FinaccTransaction> transactions) {
    for (FIN_FinaccTransaction trx : transactions) {
      FIN_TransactionProcess.doTransactionProcess(PROCESS_ACTION, trx);
    }
  }

  /**
   * This class exists because TransactionsDao.getTransactionMaxLineNo does not take into account
   * not flushed transactions
   * 
   */
  private static class LineNumberUtil {
    private HashMap<FIN_FinancialAccount, Long> lastLineNo = new HashMap<FIN_FinancialAccount, Long>();

    protected Long getNextLineNumber(FIN_FinancialAccount account) {
      Long lineNo = lastLineNo.get(account);

      if (lineNo == null) {
        lineNo = TransactionsDao.getTransactionMaxLineNo(account);
      }
      lineNo += 10;
      lastLineNo.put(account, lineNo);

      return lineNo;
    }
  }

  private static boolean existsSequence(String name) {
    String sequenceName = "DocumentNo_" + name;
    OBCriteria<Sequence> sequenceQuery = OBDal.getInstance().createCriteria(Sequence.class);
    sequenceQuery.add(Restrictions.eq(Sequence.PROPERTY_NAME, sequenceName));
    sequenceQuery.setMaxResults(1);
    Sequence sequence = (Sequence) sequenceQuery.uniqueResult();
    if (sequence == null) {
      return false;
    }
    return true;
  }

  private static void createSequence(String name) {
    String sequenceName = "DocumentNo_" + name;
    final Sequence newSequence = OBProvider.getInstance().get(Sequence.class);
    newSequence.setName(sequenceName);
    OBDal.getInstance().save(newSequence);
    OBDal.getInstance().flush();
  }

}
