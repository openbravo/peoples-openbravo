/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.PaidReceipts;
import org.openbravo.service.json.JsonConstants;

public class OpenRelatedReceipts extends JSONProcessSimple {

  private final String ORDERID_PROPERTY = "orderid";
  private final String TERMINAL_ORGANIZATION_PROPERTY = "organization";

  @Override
  public JSONObject exec(JSONObject jsonsent) throws ServletException, JSONException {

    final JSONObject result = new JSONObject();

    final JSONArray orders = new JSONArray();
    final PaidReceipts paidReceiptService = WeldUtils
        .getInstanceFromStaticBeanManager(PaidReceipts.class);

    final JSONArray orderIds = jsonsent.getJSONArray("orders");

    for (int i = 0; i < orderIds.length(); i++) {
      final JSONObject args = new JSONObject();
      args.put(ORDERID_PROPERTY, orderIds.getJSONObject(i).getString("id"));
      args.put(TERMINAL_ORGANIZATION_PROPERTY, jsonsent.getString(TERMINAL_ORGANIZATION_PROPERTY));
      final JSONObject response = paidReceiptService.exec(args);
      final JSONObject order = getOrderFromResponse(response);
      if (order != null) {
        orders.put(order);
      }
    }

    result.put(JsonConstants.RESPONSE_DATA, orders);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return result;
  }

  private JSONObject getOrderFromResponse(JSONObject response) throws JSONException {
    if (response == null || !response.has(JsonConstants.RESPONSE_DATA)) {
      return null;
    }

    return response.getJSONArray(JsonConstants.RESPONSE_DATA).getJSONObject(0);
  }
}
