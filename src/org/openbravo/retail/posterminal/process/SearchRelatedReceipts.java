/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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
    StringBuffer hqlRelatedReceipts = new StringBuffer();
    hqlRelatedReceipts.append("SELECT ");
    hqlRelatedReceipts.append("o.id AS id, o.documentNo AS documentNo ");
    hqlRelatedReceipts.append("FROM Order AS o ");
    hqlRelatedReceipts.append("JOIN o.fINPaymentScheduleList pp ");
    hqlRelatedReceipts.append("JOIN pp.fINPaymentScheduleDetailOrderPaymentScheduleList pd ");
    hqlRelatedReceipts.append("WHERE pd.canceled = false ");
    hqlRelatedReceipts.append("AND pd.paymentDetails IS NULL ");
    hqlRelatedReceipts.append("AND o.businessPartner.id = :bp ");
    hqlRelatedReceipts.append("AND o.id <> :currentOrder ");
    hqlRelatedReceipts.append("AND o.$readableSimpleCriteria AND o.$naturalOrgCriteria ");
    hqlRelatedReceipts.append("GROUP BY o.id, o.documentNo ");

    return Arrays.asList(new String[] { hqlRelatedReceipts.toString() });
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);

      Map<String, Object> paramValues = new HashMap<String, Object>();

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