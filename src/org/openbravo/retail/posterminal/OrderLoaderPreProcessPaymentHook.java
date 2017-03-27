/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public abstract class OrderLoaderPreProcessPaymentHook {

  public abstract void exec(JSONObject jsonorder, Order order, JSONObject jsonpayment,
      FIN_Payment payment) throws Exception;
}