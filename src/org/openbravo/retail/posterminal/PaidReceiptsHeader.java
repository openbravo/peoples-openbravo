/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;

public class PaidReceiptsHeader extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    JSONArray respArray = new JSONArray();
    OBContext.setAdminMode(true);
    JSONObject json = jsonsent.getJSONObject("filters");
    String client = json.getString("client");
    String organization = json.getString("organization");

    String hqlPaidReceipts = "select ord.id as id, ord.documentNo as documentNo, ord.orderDate as orderDate, "
        + "ord.businessPartner.name as businessPartner, ord.grandTotalAmount as totalamount from Order as ord where ord.client=? and ord.organization=? and ord.obposApplications is not null";
    if (!json.getString("filterText").isEmpty()) {
      hqlPaidReceipts += " and (ord.documentNo like '%" + json.getString("filterText")
          + "%' or upper(ord.businessPartner.name) like upper('%" + json.getString("filterText")
          + "%')) ";
    }
    if (!json.getString("documentType").isEmpty()) {
      hqlPaidReceipts += " and ord.documentType.id='" + json.getString("documentType") + "'";
    }
    if (!json.getString("docstatus").isEmpty() && !json.getString("docstatus").equals("null")) {
      hqlPaidReceipts += " and ord.documentStatus='" + json.getString("docstatus") + "'";
    }
    if (!json.getString("startDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate >='" + json.getString("startDate") + "'";
    }
    if (!json.getString("endDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate <='" + json.getString("endDate") + "'";
    }
    Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
    paidReceiptsQuery.setString(0, client);
    paidReceiptsQuery.setString(1, organization);

    for (Object obj : paidReceiptsQuery.list()) {
      Object[] objpaidReceipts = (Object[]) obj;
      JSONObject paidReceipt = new JSONObject();
      paidReceipt.put("orderid", objpaidReceipts[0]);
      paidReceipt.put("documentNo", objpaidReceipts[1]);
      paidReceipt.put("orderDate", (objpaidReceipts[2]));
      paidReceipt.put("businessPartner", objpaidReceipts[3]);
      paidReceipt.put("totalamount", objpaidReceipts[4]);

      respArray.put(paidReceipt);
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}