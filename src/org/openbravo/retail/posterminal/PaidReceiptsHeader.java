/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
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
    try {
      OBContext.setAdminMode(true);
      boolean useContains = true;
      try {
        OBContext.setAdminMode(false);
        useContains = "Y".equals(Preferences.getPreferenceValue(
            "OBPOS_remote.receipt_usesContains", true, OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error getting preference OBPOS_remote.receipt_usesContains " + e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }
      Map<String, Object> paramValues = new HashMap<String, Object>();

      paramValues.put("organization", jsonsent.getString("organization"));
      JSONObject json = jsonsent.getJSONObject("filters");
      if (!json.getString("filterText").isEmpty()) {
        if (useContains) {
          paramValues.put("filterT1", ("%" + json.getString("filterText").trim() + "%"));
        } else {
          paramValues.put("filterT1", (json.getString("filterText").trim() + "%"));
        }
      }
      if (jsonsent.has("filters")) {
        if (!json.isNull("documentType")) {
          paramValues.put("documentTypes", json.getJSONArray("documentType"));
        }
        if (!json.getString("docstatus").isEmpty() && !json.getString("docstatus").equals("null")) {
          paramValues.put("docstatus", json.getString("docstatus"));
        }
        try {
          if (!json.getString("startDate").isEmpty()) {
            paramValues
                .put("startDate", getDateFormated(json.getString("startDate"), "yyyy-MM-dd"));
          }
          if (!json.getString("endDate").isEmpty()) {
            paramValues.put("endDate", getDateFormated(json.getString("endDate"), "yyyy-MM-dd"));
          }
        } catch (ParseException e) {
          log.error(e.getMessage(), e);
        }
      }
      return paramValues;

    } finally {
      OBContext.restorePreviousMode();
    }
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
        + "' and ord.organization.id= :organization"
        + " and ord.obposIsDeleted = false ";

    if (!json.getString("filterText").isEmpty()) {
      String hqlFilter = "ord.documentNo like :filterT1 or REPLACE(ord.documentNo, '/', '') like :filterT1 or upper(ord.businessPartner.name) like upper(:filterT1)";
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
    if (!json.isNull("documentType")) {
      hqlPaidReceipts += " and ( ord.documentType.id in (:documentTypes) ) ";
    }
    if (!json.getString("docstatus").isEmpty() && !json.getString("docstatus").equals("null")) {
      hqlPaidReceipts += " and ord.documentStatus= :docstatus";
    }
    if (!json.getString("startDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate >= :startDate ";
    }
    if (!json.getString("endDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate <= :endDate ";
    }

    if (json.has("isQuotation") && json.getBoolean("isQuotation")) {
      // not more filters
    } else if (json.getBoolean("isLayaway")) {
      // (It is Layaway)
      hqlPaidReceipts += " and ord.obposApplications is not null and (select sum(deliveredQuantity) from ord.orderLineList where orderedQuantity > 0)=0 and ord.documentStatus = 'CO' ";
    } else if (json.getBoolean("isReturn")) {
      // (It is a Return)
      hqlPaidReceipts += " and ord.obposApplications is not null and ord.iscancelled != true and (exists( select 1 from ord.orderLineList where deliveredQuantity != 0) and exists( select 1 from ord.orderLineList where orderedQuantity > 0)) ";
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
