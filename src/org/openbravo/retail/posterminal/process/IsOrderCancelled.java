/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.json.JsonConstants;

public class IsOrderCancelled extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws OBException, JSONException {
    JSONObject result = new JSONObject();
    JSONObject data = new JSONObject();

    String orderId = jsonData.getString("orderId");
    String documentNo = jsonData.getString("documentNo");
    Boolean cancelOrder = jsonData.getBoolean("setCancelled");
    Order order = OBDal.getInstance().get(Order.class, orderId);

    if (order != null) {
      if (order.isCancelled()) {
        data.put("orderCancelled", true);
      } else {
        data.put("orderCancelled", false);
        if (cancelOrder) {
          order.setCancelled(true);
        }
      }
    } else {
      // The layaway was not found in the database.
      throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_OrderNotFound",
          new String[] { documentNo }));
    }

    result.put("data", data);
    result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return result;
  }
}