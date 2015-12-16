/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONObject;

public interface ProcessCashMgmtHook {

  public void exec(JSONObject jsonsent, String type, OBPOSAppPayment paymentMethod,
      OBPOSAppCashup cashup, OBPOSPaymentcashupEvents paymentcashupEvent, BigDecimal amount,
      BigDecimal origAmount) throws Exception;
}
