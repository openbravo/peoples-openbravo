/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.RejectReason;
import org.openbravo.service.json.JsonConstants;

public class QuotationsReject extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      String orderid = jsonsent.getString("orderid");
      String rejectReasonId = jsonsent.getString("rejectReasonId");
      Order order = OBDal.getInstance().get(Order.class, orderid);
      RejectReason reason = OBDal.getInstance().get(RejectReason.class, rejectReasonId);
      if (order != null && reason != null) {
        order.setDocumentStatus("CJ");
        order.setRejectReason(reason);
        OBDal.getInstance().save(order);
      }
      JSONArray respArray = new JSONArray();
      respArray.put(order);
      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

}
