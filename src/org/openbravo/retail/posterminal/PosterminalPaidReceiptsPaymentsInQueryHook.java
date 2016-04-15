/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

@ApplicationScoped
public class PosterminalPaidReceiptsPaymentsInQueryHook implements PaidReceiptsPaymentsInQueryHook {

  @Override
  public void exec(JSONObject paymentIn, String paymentId) throws Exception {

    FIN_Payment finPayment = OBDal.getInstance().get(FIN_Payment.class, paymentId);
    try {
      // ensure that just valid JSONObjects are accepted
      paymentIn.put("paymentData", new JSONObject((String) finPayment.getObposPaymentdata()));
    } catch (Exception e) {
      // This property will not exist
    }
  }
}