/*
 ************************************************************************************
 * Copyright (C) 2018-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class AssociateOrderLines extends ProcessHQLQuery {
  public static final Logger log = LogManager.getLogger();
  public static final String AssociateOrderLinesPropertyExtension = "AssociateOrderLinesPropertyExtension";

  @Inject
  @Any
  @Qualifier(AssociateOrderLinesPropertyExtension)
  private Instance<ModelExtension> propextension;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> params = new HashMap<String, Object>();
    Map<String, Object> paramValues = getFilters(jsonsent);
    params.put("excluded",
        new HashSet<String>(Arrays.asList(paramValues.get("excluded").toString().split(","))));
    params.put("productId", paramValues.get("productId"));
    params.put("excludedOrder", paramValues.get("excludedOrder"));

    Iterator<?> it = paramValues.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      Map.Entry entry = (Map.Entry) it.next();
      String column = (String) entry.getKey();
      Object value = entry.getValue();
      if ("orderDate".equals(column)) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        params.put("orderDate", dateFormat.format(value));
      } else if ("documentNo".equals(column)) {
        params.put("documentNo", value);
      } else if ("bpId".equals(column)) {
        params.put("bpId", value);
      } else if ("orderId".equals(column)) {
        params.put("orderId", value);
      }
    }
    return params;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList queryHQLProperties = ModelExtensionUtils.getPropertyExtensions(propextension);
    Map<String, Object> paramValues = getFilters(jsonsent);
    final StringBuilder hqlPendingLines = new StringBuilder();

    hqlPendingLines.append("SELECT ");
    hqlPendingLines.append(queryHQLProperties.getHqlSelect());
    hqlPendingLines.append(" FROM OrderLine AS ol");
    hqlPendingLines.append(" LEFT JOIN ol.orderLineOfferList AS olo");
    hqlPendingLines.append(" LEFT JOIN olo.priceAdjustment AS offer");
    hqlPendingLines.append(" LEFT JOIN offer.discountType AS discType");
    hqlPendingLines.append(" JOIN ol.product AS p");
    hqlPendingLines.append(" JOIN ol.salesOrder AS salesOrder");
    hqlPendingLines.append(" JOIN salesOrder.businessPartner AS bp");
    hqlPendingLines.append(" WHERE salesOrder.client.id =  $clientId");
    hqlPendingLines.append(" AND salesOrder.$naturalOrgCriteria");
    hqlPendingLines.append(" AND p.productType = 'I' AND ol.orderedQuantity > 0");
    hqlPendingLines.append(" AND salesOrder.obposApplications IS NOT NULL");
    hqlPendingLines.append(" AND salesOrder.obposIsDeleted = false");
    hqlPendingLines.append(" AND salesOrder.documentStatus <> 'CL'");
    hqlPendingLines.append(" AND salesOrder.transactionDocument.sOSubType NOT LIKE 'OB'");

    if (!paramValues.containsKey("excludedOrder")) {
      hqlPendingLines.append(" AND ol.id NOT IN ( :excluded )");
    } else {
      hqlPendingLines.append(" AND ol.salesOrder.id <> :excludedOrder");
    }

    if ("N".equals(paramValues.get("includeProductCategories"))) {
      hqlPendingLines.append(" AND EXISTS");
      hqlPendingLines.append(
          " (FROM ServiceProductCategory AS spc WHERE spc.productCategory=p.productCategory AND spc.product.id = :productId ) ");
    } else if ("Y".equals(paramValues.get("includeProductCategories"))) {
      hqlPendingLines.append(" AND NOT EXISTS");
      hqlPendingLines.append(
          " (FROM ServiceProductCategory AS spc WHERE spc.productCategory=p.productCategory AND spc.product.id = :productId )");
    }

    if ("N".equals(paramValues.get("includeProducts"))) {
      hqlPendingLines.append(" AND EXISTS");
      hqlPendingLines.append(
          " (FROM ServiceProduct AS spc WHERE spc.relatedProduct=p.id AND spc.product.id = :productId )");
    } else if ("Y".equals(paramValues.get("includeProducts"))) {
      hqlPendingLines.append(" AND NOT EXISTS");
      hqlPendingLines.append(
          "  (FROM ServiceProduct AS spc WHERE spc.relatedProduct=p.id AND spc.product.id = :productId )");
    }

    Iterator<?> it = paramValues.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      Map.Entry entry = (Map.Entry) it.next();
      String column = (String) entry.getKey();
      if ("orderDate".equals(column)) {
        hqlPendingLines.append(" AND salesOrder.orderDate = to_date(:orderDate, 'YYYY/MM/DD')");
      } else if ("documentNo".equals(column)) {
        hqlPendingLines.append(" AND upper(salesOrder.documentNo) like :documentNo");
      } else if ("bpId".equals(column)) {
        hqlPendingLines.append(" AND bp.id = :bpId");
      } else if ("orderId".equals(column)) {
        hqlPendingLines.append(" AND salesOrder.id = :orderId");
      }
    }

    if ((jsonsent.has("orderByClause") && jsonsent.get("orderByClause") != JSONObject.NULL)
        || (jsonsent.has("orderByProperties")
            && jsonsent.get("orderByProperties") != JSONObject.NULL)) {
      hqlPendingLines.append(" $orderByCriteria");
    }

    return Arrays.asList(new String[] { hqlPendingLines.toString() });
  }

  private Map<String, Object> getFilters(JSONObject jsonsent) throws JSONException {
    JSONArray filters = jsonsent.getJSONArray("remoteFilters");
    Map<String, Object> paramValues = new HashMap<String, Object>();
    if (filters.length() > 0) {
      for (int i = 0; i < filters.length(); i++) {
        JSONObject flt = filters.getJSONObject(i);
        String operator = flt.getString("operator");
        String column = flt.getString("columns");
        String value = flt.getString("value");
        if (!"".equals(value.trim())) {
          if ("orderDate".equals(column) || "deliveryDate".equals(column)) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
              Date date = new Date(dateTimeFormat.parse(value).getTime());
              paramValues.put(column, date);
            } catch (ParseException e) {
              log.error("Error parsing Date", e);
            }
          } else {
            paramValues.put(column,
                operator.equalsIgnoreCase("contains") ? "%" + value.toUpperCase() + "%" : value);
          }
        }
      }
    }
    return paramValues;
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean mustHaveRemoteFilters() {
    return true;
  }

}
