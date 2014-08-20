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
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingTransaction;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLineTemp;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnMatchTransactionActionHandler extends BaseActionHandler {
  private static Logger log = LoggerFactory.getLogger(UnMatchTransactionActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      final JSONObject jsonData = new JSONObject(data);
      String strBankStatementLineId = jsonData.getString("bankStatementLineId");
      FIN_BankStatementLine bsline = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strBankStatementLineId);
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
   * Merges given bank statement line with other bank statement lines with the same line number and
   * not matched with any transaction.
   * 
   * @param bsline
   *          Bank Statement Line.
   */
  private void mergeBankStatementLine(FIN_BankStatementLine bsline) {
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
  private List<FIN_ReconciliationLineTemp> getRecTempLines(FIN_BankStatementLine bsline) {
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
}