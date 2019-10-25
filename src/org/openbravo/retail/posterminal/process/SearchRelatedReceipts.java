/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class SearchRelatedReceipts extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    // Getting orderId for the documentNo
    final String hqlRelatedReceipts = "SELECT o.id AS id, o.documentNo AS documentNo, " //
        + "o.orderDate AS orderDate, o.grandTotalAmount AS amount, " //
        + "sum(psd.amount) AS pending " //
        + "FROM FIN_Payment_ScheduleDetail AS psd " //
        + "JOIN psd.orderPaymentSchedule AS ps " //
        + "JOIN ps.order AS o " //
        + "WHERE o.documentStatus <> 'CL' " //
        + "AND psd.paymentDetails IS NULL " //
        + "AND o.businessPartner.id = :bp " //
        + "AND o.id <> :currentOrder " //
        + "AND o.$readableSimpleCriteria AND o.$naturalOrgCriteria " //
        + "GROUP BY o.id, o.documentNo, o.orderDate, o.grandTotalAmount " //
        + "ORDER BY o.orderDate"; //

    return Arrays.asList(hqlRelatedReceipts);
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);

      final Map<String, Object> paramValues = new HashMap<String, Object>();

      paramValues.put("bp", jsonsent.getString("bp"));
      paramValues.put("currentOrder", jsonsent.getString("orderId"));

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
