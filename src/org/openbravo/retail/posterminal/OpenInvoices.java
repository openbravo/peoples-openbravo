/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.service.json.JsonConstants;

public class OpenInvoices extends JSONProcessSimple {

  private final String INVOICE_PROPERTY = "invoiceid";

  @Override
  public JSONObject exec(JSONObject jsonsent) throws ServletException, JSONException {

    final JSONObject result = new JSONObject();

    final JSONArray invoices = new JSONArray();
    final Invoices invoicesService = WeldUtils.getInstanceFromStaticBeanManager(Invoices.class);

    final JSONArray invoicesIds = jsonsent.getJSONArray("invoices");

    for (int i = 0; i < invoicesIds.length(); i++) {
      final JSONObject args = new JSONObject();
      args.put(INVOICE_PROPERTY, invoicesIds.getJSONObject(i).getString("id"));
      final JSONObject response = invoicesService.exec(args);
      final JSONObject invoice = getInvoiceFromResponse(response);
      if (invoice != null) {
        invoices.put(invoice);
      }
    }

    result.put(JsonConstants.RESPONSE_DATA, invoices);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return result;
  }

  private JSONObject getInvoiceFromResponse(JSONObject response) throws JSONException {
    if (response == null || !response.has(JsonConstants.RESPONSE_DATA)) {
      return null;
    }

    return response.getJSONArray(JsonConstants.RESPONSE_DATA).getJSONObject(0);
  }

  @Override
  protected String getProperty() {
    return "OBPOS_print.receipt";
  }
}
