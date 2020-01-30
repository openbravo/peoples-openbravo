/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceOrderHook;
import org.openbravo.model.common.order.Order;

public class CancelAndReplaceHook extends CancelAndReplaceOrderHook {

  /**
   * Hook executed during the Cancel and Replace and Cancel Layaway processes. If this processes are
   * executed from the Web POS, the OBPOSAppCashup is assigned to the order that inverses the
   * original one.
   */
  @Override
  public void exec(boolean replaceOrder, boolean triggersDisabled, Order oldOrder, Order newOrder,
      Order inverseOrder, JSONObject jsonorder) throws Exception {
    if (jsonorder != null) {
      if (oldOrder.isObposIslayaway().booleanValue()) {
        oldOrder.setObposIslayaway(false);
        inverseOrder.setObposIslayaway(false);
      }
      oldOrder.setObposAppCashup(jsonorder.getString("obposAppCashup"));
      inverseOrder.setObposAppCashup(jsonorder.getString("obposAppCashup"));
      final OBPOSApplications posTerminal = OBDal.getInstance()
          .get(OBPOSApplications.class, jsonorder.getString("posTerminal"));
      inverseOrder.setObposApplications(posTerminal);

      if (inverseOrder.getObposRoundingAmount() != null
          && inverseOrder.getObposRoundingAmount().compareTo(BigDecimal.ZERO) != 0) {
        inverseOrder.setObposRoundingAmount(inverseOrder.getObposRoundingAmount().negate());
      }
    }
  }
}
