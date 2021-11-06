/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

/**
 * 
 * Retrieves sales order lines candidates to be delivered. Supports filtering by order date,
 * business partner, delivery mode, delivery date and document number
 */
public class PendingOrderLines extends ProcessHQLQuery {

  public static final Logger log = LogManager.getLogger(PendingOrderLines.class);
  public static final String PENDINGORDERLINESPROPERTYEXTENSION = "OBRDM_PendingOrderLinesPropertyExtension";

  private static final String JAVA_DATE_FORMAT = "yyyy/MM/dd";
  private static final String DELIVERY_DATE_FIELD = "deliveryDate";
  private static final String DELIVERY_METHOD_FIELD = "deliveryMode";
  private static final String ORDER_IDS_FIELD = "orderIds";
  private static final String BUSINESS_PARTNER_FIELD = "businessPartner";
  private static final String DOCUMENT_NO_FIELD = "documentNo";
  private static final String ORDER_DATE_FIELD = "orderDate";
  private static final String ORDER_BY_FIELD = "orderby";
  private static final String LINE_NO_FIELD = "lineNo";

  private static final SimpleDateFormat DATETIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat JAVADATEFORMAT = new SimpleDateFormat(JAVA_DATE_FORMAT);

  @Inject
  @Any
  @Qualifier(PENDINGORDERLINESPROPERTYEXTENSION)
  private Instance<ModelExtension> propextension;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> params = new HashMap<>();
    String orgId = jsonsent.getString("organization");
    params.put("orgId", orgId);
    JSONArray storeDocTypes = jsonsent.getJSONArray("storeDocTypes");
    params.put("docTypes", storeDocTypes);
    String excluded = jsonsent.getString("excluded");
    params.put("excluded", getExcludedDeliveryModesJSONArray(excluded));
    Map<String, Object> paramValues = getFilters(jsonsent);
    for (Entry<String, Object> entry : paramValues.entrySet()) {
      String column = entry.getKey();
      Object value = entry.getValue();
      if (ORDER_DATE_FIELD.equals(column)) {
        params.put(ORDER_DATE_FIELD, JAVADATEFORMAT.format(value));
      } else if (DOCUMENT_NO_FIELD.equals(column)) {
        params.put(DOCUMENT_NO_FIELD, value);
      } else if (BUSINESS_PARTNER_FIELD.equals(column)) {
        params.put(BUSINESS_PARTNER_FIELD, value);
      } else if (ORDER_IDS_FIELD.equals(column)) {
        params.put(ORDER_IDS_FIELD, Utility.stringToArrayList((String) value));
      } else if (DELIVERY_METHOD_FIELD.equals(column)) {
        params.put(DELIVERY_METHOD_FIELD, value);
      } else if (DELIVERY_DATE_FIELD.equals(column)) {
        params.put(DELIVERY_DATE_FIELD, JAVADATEFORMAT.format(value));
      }
    }
    return params;
  }

  private JSONArray getExcludedDeliveryModesJSONArray(final String excluded) {
    final JSONArray excludedDeliveryModes = new JSONArray();
    if (!StringUtils.isBlank(excluded)) {
      final String[] excludedDeliveryModesStr = excluded.replaceAll("\'", StringUtils.EMPTY)
          .split(",");
      for (String excludedDeliveryMode : excludedDeliveryModesStr) {
        excludedDeliveryModes.put(excludedDeliveryMode);
      }
    }
    return excludedDeliveryModes;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList queryHQLProperties = ModelExtensionUtils.getPropertyExtensions(propextension);
    Map<String, Object> paramValues = getFilters(jsonsent);

    StringBuilder hqlPendingLines = new StringBuilder("select " //
        + queryHQLProperties.getHqlSelect() //
        + " from OrderLine as ol " //
        + "join ol.product as p " //
        + "left join p.attributeSetValue attr " //
        + "join ol.salesOrder as salesOrder " //
        + "join salesOrder.businessPartner as bp " //
        + "left join salesOrder.partnerAddress as loc "
        + "left join loc.locationAddress as locAddress "
        + "left join locAddress.country as country " //
        + "left join p.brand as brand ");

    hqlPendingLines.append(" where salesOrder.transactionDocument.id in :docTypes " //
        + " and p.productType = 'I' " //
        + " and (ol.orderedQuantity - ol.deliveredQuantity) > 0 " //
        + " and salesOrder.documentStatus='CO' " //
        + " and ol.organization.id = :orgId" //
        + " and salesOrder.obposApplications is not null " //
        + " and salesOrder.obposIsDeleted = false " //
        + " and ol.obrdmDeliveryMode not in :excluded" //
        + " and ol.obposCanbedelivered = 'Y'" //
        + " and salesOrder.obposIslayaway = false");

    hqlPendingLines.append(addParameters(paramValues));
    hqlPendingLines.append(addOrderBy(jsonsent));

    return Arrays.asList(hqlPendingLines.toString());
  }

  private String addParameters(Map<String, Object> paramValues) {
    StringBuilder hqlPendingLines = new StringBuilder();
    for (Entry<String, Object> entry : paramValues.entrySet()) {
      String column = entry.getKey();
      if (ORDER_DATE_FIELD.equals(column)) {
        hqlPendingLines.append(" and salesOrder.orderDate = to_date(:orderDate, 'YYYY/MM/DD')");
      } else if (DOCUMENT_NO_FIELD.equals(column)) {
        hqlPendingLines.append(" and upper(salesOrder.documentNo) like :documentNo");
      } else if (BUSINESS_PARTNER_FIELD.equals(column)) {
        hqlPendingLines.append(" and bp.id = :businessPartner");
      } else if (ORDER_IDS_FIELD.equals(column)) {
        hqlPendingLines.append(" and salesOrder.id in :orderIds");
      } else if (DELIVERY_METHOD_FIELD.equals(column)) {
        hqlPendingLines.append(" and ol.obrdmDeliveryMode = :deliveryMode");
      } else if (DELIVERY_DATE_FIELD.equals(column)) {
        hqlPendingLines.append(" and ol.obrdmDeliveryDate = to_date(:deliveryDate, 'YYYY/MM/DD')");
      }
    }
    return hqlPendingLines.toString();
  }

  private String addOrderBy(JSONObject jsonsent) throws JSONException {
    final String DEFAULT_ORDER_CRITERIA = "ol.lineNo, salesOrder.documentNo";
    String hqlPendingLines = StringUtils.EMPTY;
    if (jsonsent.has(ORDER_BY_FIELD) && !jsonsent.isNull(ORDER_BY_FIELD)) {
      JSONObject orderby = jsonsent.getJSONObject(ORDER_BY_FIELD);
      String column = orderby.getString("name");
      String fullColumn = StringUtils.EMPTY;
      if (ORDER_DATE_FIELD.equals(column)) {
        fullColumn = "salesOrder.orderDate";
      } else if (DOCUMENT_NO_FIELD.equals(column)) {
        fullColumn = "salesOrder.documentNo";
      } else if (BUSINESS_PARTNER_FIELD.equals(column)) {
        fullColumn = "bp.name";
      } else if (DELIVERY_METHOD_FIELD.equals(column)) {
        fullColumn = "ol.obrdmDeliveryMode";
      } else if (DELIVERY_DATE_FIELD.equals(column)) {
        fullColumn = "ol.obrdmDeliveryDate";
      } else if (LINE_NO_FIELD.equals(column)) {
        fullColumn = "ol.lineNo";
      }
      if (StringUtils.isNotBlank(fullColumn)) {
        hqlPendingLines += " order by " + fullColumn + " " + getOrderDrection(orderby) + ", "
            + DEFAULT_ORDER_CRITERIA;
      }
    } else {
      hqlPendingLines += " order by " + DEFAULT_ORDER_CRITERIA;
    }
    return hqlPendingLines;
  }

  private String getOrderDrection(JSONObject orderby) throws JSONException {
    return StringUtils.equalsIgnoreCase(orderby.getString("direction"), "ASC") ? "asc" : "desc";
  }

  private Map<String, Object> getFilters(JSONObject jsonsent) throws JSONException {
    JSONArray filters = jsonsent.getJSONArray("remoteFilters");
    Map<String, Object> paramValues = new HashMap<>();
    for (int i = 0; i < filters.length(); i++) {
      JSONObject flt = filters.getJSONObject(i);
      String operator = flt.getString("operator");
      String column = flt.getString("column");
      String value = flt.getString("value");
      setFilterValue(paramValues, operator, column, value);
    }
    return paramValues;
  }

  private void setFilterValue(Map<String, Object> paramValues, String operator, String column,
      String value) {
    if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value.trim())) {
      String valueStr = StringEscapeUtils.escapeHtml(value);
      if (ORDER_DATE_FIELD.equals(column) || DELIVERY_DATE_FIELD.equals(column)) {
        try {
          Date date = new Date(DATETIMEFORMAT.parse(valueStr).getTime());
          paramValues.put(column, date);
        } catch (ParseException e) {
          log.error("Error parsing Date {}", valueStr, e);
        }
      } else {
        paramValues.put(column,
            operator.equalsIgnoreCase("contains") ? "%" + valueStr.toUpperCase() + "%" : valueStr);
      }
    }
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
