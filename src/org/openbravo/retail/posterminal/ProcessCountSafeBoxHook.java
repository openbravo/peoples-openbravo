/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public interface ProcessCountSafeBoxHook {
  public void exec(JSONObject jsonCountSafeBox, JSONObject jsonCurrentSafeBox, OBPOSSafeBox safeBox,
      OBPOSSafeBoxPaymentMethod paymentMethod, FIN_FinaccTransaction finAccPaymentTransaction,
      FIN_FinaccTransaction finAccDepositTransaction) throws Exception;
}
