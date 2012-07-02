/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.retail.config.CashManagementEvents;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashMgmt extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    OBContext.setAdminMode(true);
    final JSONObject jsonData = new JSONObject();
    try {
      String description = jsonsent.getString("description");
      BigDecimal amount = BigDecimal.valueOf(jsonsent.getDouble("amount"));
      String key = jsonsent.getString("key");
      String type = jsonsent.getString("type");
      String cashManagementReasonId = jsonsent.getString("reasonId");

      OBCriteria<OBPOSAppPayment> payments = OBDal.getInstance().createCriteria(
          OBPOSAppPayment.class);
      payments.add(Restrictions.eq(OBPOSAppPayment.PROPERTY_SEARCHKEY, key));
      payments.setFilterOnActive(false);

      OBPOSAppPayment paymentMethod = payments.list().get(0);
      TerminalTypePaymentMethod terminalPaymentMethod = paymentMethod.getPaymentMethod();

      GLItem glItemMain;
      GLItem glItemSecondary;
      if (type.equals("drop")) {
        glItemMain = terminalPaymentMethod.getGLItemForDrops();
        glItemSecondary = terminalPaymentMethod.getGLItemForDeposits();
      } else {
        glItemMain = terminalPaymentMethod.getGLItemForDeposits();
        glItemSecondary = terminalPaymentMethod.getGLItemForDrops();
      }
      FIN_FinancialAccount account = paymentMethod.getFinancialAccount();

      FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
      transaction.setCurrency(terminalPaymentMethod.getCurrency());
      transaction.setAccount(account);
      transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
      transaction.setGLItem(glItemMain);
      if (type.equals("drop")) {
        transaction.setPaymentAmount(amount);
      } else {
        transaction.setDepositAmount(amount);
      }
      transaction.setProcessed(true);
      transaction.setTransactionType("BPW");
      transaction.setDescription(paymentMethod.getCommercialName() + " - " + description);
      transaction.setTransactionDate(new Date());
      transaction.setStatus("RPPC");

      OBDal.getInstance().save(transaction);

      CashManagementEvents event = OBDal.getInstance().get(CashManagementEvents.class,
          cashManagementReasonId);

      FIN_FinaccTransaction secondTransaction = OBProvider.getInstance().get(
          FIN_FinaccTransaction.class);
      secondTransaction.setCurrency(terminalPaymentMethod.getCurrency());
      secondTransaction.setAccount(event.getFinancialAccount());
      secondTransaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(event
          .getFinancialAccount()) + 10);
      secondTransaction.setGLItem(glItemSecondary);
      // The second transaction describes the opposite movement of the first transaction.
      // If the first is a deposit, the second is a drop
      if (type.equals("deposit")) {
        secondTransaction.setPaymentAmount(amount);
      } else {
        secondTransaction.setDepositAmount(amount);
      }
      secondTransaction.setProcessed(true);
      secondTransaction.setTransactionType("BPW");
      secondTransaction.setDescription(paymentMethod.getCommercialName() + " - " + description);
      secondTransaction.setTransactionDate(new Date());
      secondTransaction.setStatus("RPPC");
      OBDal.getInstance().save(secondTransaction);

      if (type.equals("drop")) {
        jsonData.put("drop", amount);
        jsonData.put("deposit", 0);
      } else {
        jsonData.put("drop", 0);
        jsonData.put("deposit", amount);
      }
      jsonData.put("name", paymentMethod.getCommercialName());
      jsonData.put("description", paymentMethod.getCommercialName() + " - " + description);

    } finally {
      OBContext.restorePreviousMode();
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, jsonData);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}
