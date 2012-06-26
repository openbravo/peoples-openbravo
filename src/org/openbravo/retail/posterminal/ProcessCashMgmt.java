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
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSAppPayment;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashMgmt extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    OBContext.setAdminMode(true);
    try {
      String name = jsonsent.getString("name");
      BigDecimal amount = BigDecimal.valueOf(jsonsent.getDouble("amount"));
      String key = jsonsent.getString("key");
      String type = jsonsent.getString("type");

      OBCriteria<OBPOSAppPayment> payments = OBDal.getInstance().createCriteria(
          OBPOSAppPayment.class);
      payments.add(Restrictions.eq(OBPOSAppPayment.PROPERTY_SEARCHKEY, key));
      payments.setFilterOnActive(false);

      OBPOSAppPayment paymentMethod = payments.list().get(0);

      GLItem glItem = paymentMethod.getGlitemChanges();
      FIN_FinancialAccount account = paymentMethod.getFinancialAccount();
      FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
      transaction.setCurrency(account.getCurrency());
      transaction.setAccount(account);
      transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
      transaction.setGLItem(glItem);
      if (type.equals("drop")) {
        transaction.setPaymentAmount(amount);
      } else {
        transaction.setDepositAmount(amount);
      }
      transaction.setProcessed(true);
      transaction.setTransactionType("BPW");
      transaction.setDescription(name);
      transaction.setTransactionDate(new Date());
      transaction.setStatus("RPPC");

      OBDal.getInstance().save(transaction);
    } finally {
      OBContext.restorePreviousMode();
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}
