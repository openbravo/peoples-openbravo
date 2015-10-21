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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;

public class PaidReceiptsHeader extends ProcessHQLQuery {
  public static final Logger log = Logger.getLogger(PaidReceiptsHeader.class);

  @Inject
  @Any
  private Instance<PaidReceiptsHeaderHook> paidReceiptHeaderHooks;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    if (!jsonsent.getJSONObject("filters").getString("filterText").isEmpty()) {
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("filterT1", ("%"
          + jsonsent.getJSONObject("filters").getString("filterText").trim() + "%"));
      return paramValues;
    } else {
      return null;
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    // OBContext.setAdminMode(true);
    JSONObject json = jsonsent.getJSONObject("filters");
    String strIsLayaway = "false";
    boolean isHgvol = false;
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
        + "' and ord.obposIsDeleted = false ";

    if (!json.getString("filterText").isEmpty()) {
      String hqlFilter = "ord.documentNo like :filterT1 or REPLACE(ord.documentNo, '/', '') like :filterT1 or upper(ord.businessPartner.name) like upper(:filterT1)";
            + "%' or upper(ord.businessPartner.name) like upper('%" + filterText + "%')";
        for (PaidReceiptsHeaderHook hook : paidReceiptHeaderHooks) {
          try {
          String hql = hook.exec(hqlFilter, json.getString("filterText"));
            hqlFilter = hql;
          } catch (Exception e) {
            throw new OBException("An error happened when computing a filter in PaidReceipts", e);
          }
        }
        hqlPaidReceipts += " and (" + hqlFilter + ") ";
      }

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
      hqlPaidReceipts += " and ord.orderDate >= to_date('" + json.getString("startDate")
          + "', 'YYYY/MM/DD')";
    }
    if (!json.getString("endDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate <= to_date('" + json.getString("endDate")
          + "', 'YYYY/MM/DD')";
    }

    if (json.has("isQuotation") && json.getBoolean("isQuotation")) {
      // not more filters
    } else if (json.getBoolean("isLayaway")) {
      // (It is Layaway)
      hqlPaidReceipts += " and ord.obposApplications is not null and (select sum(deliveredQuantity) from ord.orderLineList where orderedQuantity > 0)=0 and ord.documentStatus = 'CO' ";
    } else if (json.getBoolean("isReturn")) {
      // (It is a Return)
      hqlPaidReceipts += " and ord.obposApplications is not null and (exists( select 1 from ord.orderLineList where deliveredQuantity != 0) and exists( select 1 from ord.orderLineList where orderedQuantity > 0)) ";
    } else {
      // (It is not Layaway and It is not a Return)
      hqlPaidReceipts += " and ord.obposApplications is not null and exists(select 1 from ord.orderLineList where deliveredQuantity != 0) ";
    }

    hqlPaidReceipts += " order by ord.orderDate desc, ord.documentNo desc";
    return Arrays.asList(new String[] { hqlPaidReceipts });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}