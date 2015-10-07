/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.json.JsonConstants;

public class IsOrderCancelled extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    JSONObject result = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      String orderId = jsonData.getString("orderId");
      Boolean cancelOrder = jsonData.getBoolean("setCancelled");
      Order order = OBDal.getInstance().get(Order.class, orderId);

      if (order != null) {
        if (order.isCancelledandreplaced()) {
          data.put("orderCancelled", true);
        } else {
          data.put("orderCancelled", false);
          if (cancelOrder) {
            order.setCancelledandreplaced(true);
          }
        }
      } else {
        // This flow should never be executed
        data.put("orderCancelled", true);
      }

      result.put("data", data);
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    } catch (Exception e) {
      result.put("status", JsonConstants.RPCREQUEST_STATUS_FAILURE);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }
}