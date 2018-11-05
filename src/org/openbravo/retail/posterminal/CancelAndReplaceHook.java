/*
 ************************************************************************************
 * Copyright (C) 2016-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceOrderHook;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.DalConnectionProvider;

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
      // Do not allow to do a C&R or a CL in the case that the order was not fully updated
      final String loaded = jsonorder.has("loaded") ? jsonorder.getString("loaded") : null, updated = OBMOBCUtils
          .convertToUTCDateComingFromServer(oldOrder.getUpdated());
      if (loaded == null || loaded.compareTo(updated) != 0) {
        throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_outdatedLayaway", OBContext.getOBContext().getLanguage().getLanguage()));
      }
      if (oldOrder.isObposIslayaway()) {
        oldOrder.setObposIslayaway(false);
        inverseOrder.setObposIslayaway(false);
      }
      oldOrder.setObposAppCashup(jsonorder.getString("obposAppCashup"));
      inverseOrder.setObposAppCashup(jsonorder.getString("obposAppCashup"));
      final OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonorder.getString("posTerminal"));
      inverseOrder.setObposApplications(posTerminal);
    }
  }
}
