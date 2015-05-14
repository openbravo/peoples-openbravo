/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class PaidReceiptsHeader extends ProcessHQLQuery {

  @Inject
  @Any
  private Instance<PaidReceiptsHeaderHook> paidReceiptHeaderHooks;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    // OBContext.setAdminMode(true);
    JSONObject json = jsonsent.getJSONObject("filters");
    String strIsLayaway = "false";
    if (json.getBoolean("isLayaway")) {
      strIsLayaway = "true";
    }
    String hqlPaidReceipts = "select ord.id as id, ord.documentNo as documentNo, ord.orderDate as orderDate, "
        + "ord.businessPartner.name as businessPartner, ord.grandTotalAmount as totalamount, ord.documentType.id as documentTypeId, '"
        + strIsLayaway
        + "' as isLayaway from Order as ord "
        + "where ord.client='"
        + json.getString("client")
        + "' and ord.organization='"
        + json.getString("organization")
        + "'";

    final String filterText = sanitizeString(json.getString("filterText"));
    if (!filterText.isEmpty()) {
      String hqlFilter = "ord.documentNo like '%" + filterText
          + "%' or REPLACE(ord.documentNo, '/', '') like '%" + filterText
          + "%' or upper(ord.businessPartner.name) like upper('%" + filterText + "%')";
      for (PaidReceiptsHeaderHook hook : paidReceiptHeaderHooks) {
        try {
          String hql = hook.exec(hqlFilter, filterText);
          hqlFilter = hql;
        } catch (Exception e) {
          throw new OBException("An error happened when computing a filter in PaidReceipts", e);
        }
      }
      hqlPaidReceipts += " and (" + hqlFilter + ") ";
    }
    if (!json.isNull("documentType")) {
      JSONArray docTypes = json.getJSONArray("documentType");
      hqlPaidReceipts += " and ( ";
      for (int docType_i = 0; docType_i < docTypes.length(); docType_i++) {
        hqlPaidReceipts += "ord.documentType.id='" + docTypes.getString(docType_i) + "'";
        if (docType_i != docTypes.length() - 1) {
          hqlPaidReceipts += " or ";
        }
      }
      hqlPaidReceipts += " )";
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

    if (json.has("isQuotation") && json.getBoolean("isQuotation")) {
      // not more filters
    } else if (json.getBoolean("isLayaway")) {
      // (It is Layaway)
      hqlPaidReceipts += " and ord.obposApplications is not null and (select sum(deliveredQuantity) from ord.orderLineList where orderedQuantity > 0)=0 and ord.documentStatus = 'CO' ";
    } else if (json.getBoolean("isReturn")) {
      // (It is not Layaway and It is not a Return)
      hqlPaidReceipts += " and ord.obposApplications is not null and ((select count(deliveredQuantity) from ord.orderLineList where deliveredQuantity != 0) > 0 and (select count(orderedQuantity) from ord.orderLineList where orderedQuantity > 0) > 0) ";
    } else {
      // (It is not Layaway or it is a Return)
      hqlPaidReceipts += " and ord.obposApplications is not null and exists(select 1 from ord.orderLineList where deliveredQuantity != 0) ";
    }

    hqlPaidReceipts += " order by ord.orderDate desc, ord.documentNo desc";
    return Arrays.asList(new String[] { hqlPaidReceipts });
  }

  protected String sanitizeString(String oldString) {
    String result = oldString;
    // Cleaning '
    result = result.replace("'", "");
    // Cleaing -
    result = result.replace("-", "");
    // Cleaning other characters here...
    return result;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}