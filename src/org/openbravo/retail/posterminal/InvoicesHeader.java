/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class InvoicesHeader extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    JSONObject filters = jsonsent.getJSONObject("filters");
    Map<String, Object> paramValues = new HashMap<String, Object>();
    paramValues.put("client", filters.getString("client"));
    paramValues.put("orderId", filters.getString("orderId"));
    return paramValues;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    String hqlInvoices = "select i.id as id, i.documentNo as documentNo, i.invoiceDate as orderDate, "
        + " i.businessPartner.name as businessPartner, i.grandTotalAmount as totalamount "
        + " from InvoiceLine il join il.salesOrderLine ol join ol.salesOrder ord  join il.invoice i  "
        + " where ord.client.id=:client and ord.id =:orderId "
        + " group by i.id, i.documentNo, i.invoiceDate, i.businessPartner.name, i.grandTotalAmount "
        + " order by i.documentNo asc";

    return Arrays.asList(new String[] { hqlInvoices });
  }

  @Override
  protected String getProperty() {
    return "OBPOS_print.receipt";
  }
}
