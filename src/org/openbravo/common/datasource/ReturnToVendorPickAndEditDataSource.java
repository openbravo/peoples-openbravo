/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.datasource;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class ReturnToVendorPickAndEditDataSource extends ReadOnlyDataSourceService {

  private static Logger log4j = Logger.getLogger(ReturnToVendorPickAndEditDataSource.class);
  private final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
  private static final String AD_TABLE_ID = "FCB35AE2A9CA48EFAACCB06CCD17BED5";
  private List<Map<String, Object>> data;

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public String fetch(Map<String, String> parameters) {
    int startRow = 0;
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }

    final List<JSONObject> jsonObjects = fetchJSONObject(parameters);

    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() + startRow - 1);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, getCount(parameters));
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (JSONException e) {
    }

    return jsonResult.toString();
  }

  private List<JSONObject> fetchJSONObject(Map<String, String> parameters) {
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);
    int startRow = -1;
    int endRow = -1;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }
    if (endRowStr != null) {
      endRow = Integer.parseInt(endRowStr);
    }
    data = getData(parameters, startRow, endRow);
    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
    return toJsonConverter.convertToJsonObjects(data);
  }

  @Override
  protected int getCount(Map<String, String> parameters) {
    if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
      return data.size();
    }
    Long count = 0l;
    String strOrderId = parameters.get("@Order.id@");
    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    String where = parameters.get("_where");
    OBContext.setAdminMode(true);
    try {
      Map<String, String> filterCriteria = getFilterCriteria(parameters);
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select count(iol.id)");
      hqlString.append(" from MaterialMgmtShipmentInOutLine as iol");
      hqlString.append(" join iol.shipmentReceipt as io");
      // Adds joins when required by filters
      hqlString.append(getJoinArgumentsFromFilters(filterCriteria, ""));
      // Starting where clause
      hqlString.append(" where io.businessPartner = :businessPartner");
      hqlString.append(" and io.processed = true");
      hqlString.append(" and io.documentStatus <> 'VO'");
      hqlString.append(" and io.salesTransaction = false");
      hqlString.append(" and iol.organization in :organizations");
      hqlString
          .append(" and not exists (select 1 from OrderLine as ol where ol.salesOrder = :order and ol.goodsShipmentLine = iol)");
      if (where != null && !"".equals(where) && !"null".equalsIgnoreCase(where)) {
        hqlString.append(" and " + where);
      }
      // Adds filters
      hqlString.append(getWhereArgumentsFromFilters(filterCriteria));
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("order", order);
      query.setParameter("businessPartner", order.getBusinessPartner());
      query.setParameterList("organizations", getOrganizations(order.getOrganization()));
      for (Object o : query.list()) {
        if (o != null && o instanceof Long) {
          count = (Long) o;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return count.intValue()
        + (order.getOrderLineList() != null ? order.getOrderLineList().size() : 0);
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    String strOrderId = parameters.get("@Order.id@");
    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    // Distinct is being used when calling for filters data
    if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
      String distinct = (String) parameters.get(JsonConstants.DISTINCT_PARAMETER);
      log4j.debug("Distinct param: " + distinct);
      if ("product".equals(distinct)) {
        result = getProductFilterData(parameters, order);
      } else if ("attributeSetValue".equals(distinct)) {
        result = getAttributeSetInstanceFilterData(parameters, order);
      } else if ("uOM".equals(distinct)) {
        result = getUOMFilterData(parameters, order);
      } else if ("returnReason".equals(distinct)) {
        result = getReturnReasonFilterData(parameters, order);
      }
    } else {
      Map<String, String> filterCriteria = getFilterCriteria(parameters);
      String where = parameters.get("_where");
      if (startRow == 0) {
        result.addAll(getSelectedLines(order));
      }
      result.addAll(getReceiptLines(order, getOrganizations(order.getOrganization()), where,
          filterCriteria, parameters.get("_sortBy"), startRow, endRow));

    }
    return result;
  }

  private Map<String, String> getFilterCriteria(Map<String, String> parameters) {
    Map<String, String> filterCriteria = new HashMap<String, String>();
    try {
      // Builds the criteria based on the fetch parameters
      JSONArray criterias = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criterias.length(); i++) {
        final JSONObject criteria = criterias.getJSONObject(i);
        // Multiple selection
        if (criteria.has("criteria") && criteria.has("operator")) {
          JSONArray mySon = new JSONArray(criteria.getString("criteria"));
          for (int j = 0; j < mySon.length(); j++) {
            if (filterCriteria.containsKey(mySon.getJSONObject(j).getString("fieldName"))) {
              JSONArray values = new JSONArray(filterCriteria.get(mySon.getJSONObject(j).getString(
                  "fieldName")));
              filterCriteria.put(mySon.getJSONObject(j).getString("fieldName"),
                  values.put(mySon.getJSONObject(j)).toString());
            } else {
              filterCriteria.put(mySon.getJSONObject(j).getString("fieldName"), new JSONArray()
                  .put(mySon.getJSONObject(j)).toString());
            }
          }
          // lessOrEqual
        } else if (criteria.has("operator")
            && ("greaterThan".equals(criteria.getString("operator"))
                || "lessThan".equals(criteria.getString("operator"))
                || "greaterOrEqual".equals(criteria.getString("operator")) || "lessOrEqual"
                  .equals(criteria.getString("operator")))) {
          filterCriteria.put(criteria.getString("fieldName"), criteria.toString());
        } else if (criteria.has("operator")
            && ("iEquals".equals(criteria.getString("operator")) || "iContains".equals(criteria
                .getString("operator")))) {
          if (filterCriteria.containsKey(criteria.getString("fieldName"))) {
            JSONArray myson = new JSONArray(filterCriteria.get(criteria.getString("fieldName")));
            filterCriteria.put(criteria.getString("fieldName"), myson.put(criteria).toString());
          } else {
            JSONArray myson = new JSONArray();
            filterCriteria.put(criteria.getString("fieldName"), myson.put(criteria).toString());
          }
        } else {
          filterCriteria.put(criteria.getString("fieldName"),
              criteria.has("value") ? criteria.getString("value") : criteria.toString());
        }
      }
    } catch (JSONException e) {
      log4j.error("Error while building the criteria", e);
    }

    return filterCriteria;
  }

  private List<Map<String, Object>> getReturnReasonFilterData(Map<String, String> parameters,
      Order order) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select ol.returnReason");
      hqlString.append(" from OrderLine as ol");
      hqlString.append(" where ol.salesOrder = :order");
      hqlString.append(" and ol.goodsShipmentLine is not null");
      hqlString.append(" order by ol.lineNo");
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("order", order);
      for (Object o : query.list()) {
        ReturnReason returnReason = (ReturnReason) o;
        result.add(getFilterMap(returnReason));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private Map<String, Object> getFilterMap(BaseOBObject baseOBObject) {
    Map<String, Object> myMap = new HashMap<String, Object>();
    myMap.put("id", baseOBObject.getId());
    myMap.put("name", baseOBObject.getIdentifier());
    myMap.put("_identifier", baseOBObject.getIdentifier());
    myMap.put("_entityName", baseOBObject.getEntityName());
    return myMap;
  }

  private List<Map<String, Object>> getSelectedLines(Order order) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select ol");
      hqlString.append(" from OrderLine as ol");
      hqlString.append(" where ol.salesOrder = :order");
      hqlString.append(" and ol.goodsShipmentLine is not null");
      hqlString.append(" order by ol.lineNo");
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("order", order);
      for (Object o : query.list()) {
        OrderLine orderLine = (OrderLine) o;
        result.add(getGridmap(orderLine.getGoodsShipmentLine(), orderLine));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<Map<String, Object>> getReceiptLines(Order order, Set<Organization> organizations,
      String where, Map<String, String> filterCriteria, String sortBy, int startRow, int endRow) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select iol");
      hqlString.append(" from MaterialMgmtShipmentInOutLine as iol");
      hqlString.append(" join iol.shipmentReceipt as io");
      // Adds joins when required by filters
      hqlString.append(getJoinArgumentsFromFilters(filterCriteria, sortBy));
      // Starting where clause
      hqlString.append(" where io.businessPartner = :businessPartner");
      hqlString.append(" and io.processed = true");
      hqlString.append(" and io.documentStatus <> 'VO'");
      hqlString.append(" and io.salesTransaction = false");
      hqlString.append(" and iol.organization in :organizations");
      hqlString
          .append(" and not exists (select 1 from OrderLine as ol where ol.salesOrder = :order and ol.goodsShipmentLine = iol)");
      if (where != null && !"".equals(where) && !"null".equalsIgnoreCase(where)) {
        hqlString.append(" and " + where);
      }
      // Adds filters
      hqlString.append(getWhereArgumentsFromFilters(filterCriteria));
      // Adds ordering
      hqlString.append(getOrderBy(sortBy));
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("order", order);
      query.setParameter("businessPartner", order.getBusinessPartner());
      query.setParameterList("organizations", organizations);
      query.setFirstResult(startRow);
      query.setMaxResults(endRow - startRow);
      query.setFetchSize(endRow - startRow);
      for (Object o : query.list()) {
        ShipmentInOutLine inOutLine = (ShipmentInOutLine) o;
        result.add(getGridmap(inOutLine, null));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private Object getOrderBy(String sortBy) {
    StringBuilder hqlString = new StringBuilder();
    if (sortBy == null || "".equals(sortBy)) {
      hqlString.append(" order by io.movementDate desc, io.documentNo desc, iol.lineNo");
    } else {
      boolean asc = true;
      if (sortBy.startsWith("-")) {
        asc = false;
        sortBy = sortBy.substring(1);
      }
      hqlString.append(" order by ");
      if (sortBy.equals("shipmentNumber")) {
        hqlString.append("io.documentNo").append(asc ? ", " : " desc, ");
      } else if (sortBy.equals("movementDate")) {
        hqlString.append("io.movementDate").append(asc ? ", " : " desc, ");
      } else if (sortBy.equals("product$_identifier")) {
        hqlString.append("p.name").append(asc ? ", " : " desc, ");
      } else if (sortBy.equals("attributeSetValue$_identifier")) {
        hqlString.append("a.description").append(asc ? ", " : " desc, ");
      } else if (sortBy.equals("uOM$_identifier")) {
        hqlString.append("u.name").append(asc ? ", " : " desc, ");
      } else if (sortBy.equals("movementQuantity")) {
        hqlString.append("iol.movementQuantity").append(asc ? ", " : " desc, ");
      }
      hqlString.append("io.movementDate desc, io.documentNo desc, iol.lineNo");
    }
    return hqlString.toString();
  }

  private String getWhereArgumentsFromFilters(Map<String, String> filterCriteria) {
    // Adds filters
    StringBuilder hqlString = new StringBuilder();
    if (filterCriteria.containsKey("product$_identifier")) {
      hqlString.append(getWhereComboClause("p.name", filterCriteria.get("product$_identifier")));
    }
    if (filterCriteria.containsKey("attributeSetValue$_identifier")) {
      hqlString.append(getWhereComboClause("a.description",
          filterCriteria.get("attributeSetValue$_identifier")));
    }
    if (filterCriteria.containsKey("uOM$_identifier")) {
      hqlString.append(getWhereComboClause("u.name", filterCriteria.get("uOM$_identifier")));
    }
    if (filterCriteria.containsKey("movementDate")) {
      hqlString.append(getWhereDateClause("io.movementDate", filterCriteria.get("movementDate")));
    }
    if (filterCriteria.containsKey("shipmentNumber")) {
      hqlString.append(getWhereComboClause("io.documentNo", filterCriteria.get("shipmentNumber")));
    }
    if (filterCriteria.containsKey("orderNo")) {
      hqlString.append(" and exists (select ord.documentNo" + " from ProcurementPOInvoiceMatch mpo"
          + " join mpo.salesOrderLine as ordLine "
          + " join ordLine.salesOrder as ord where mpo.goodsShipmentLine = iol");
      hqlString.append(getWhereComboClause("ord.documentNo", filterCriteria.get("orderNo")))
          .append(")");
    }
    if (filterCriteria.containsKey("movementQuantity")) {
      hqlString.append(getWhereNumericClause("iol.movementQuantity",
          filterCriteria.get("movementQuantity")));
    }
    if (filterCriteria.containsKey("unitPrice")) {
      hqlString.append(" and exists (select 1" + " from ProcurementPOInvoiceMatch mpo"
          + " join mpo.salesOrderLine as ordLine "
          + " join ordLine.salesOrder as ord where mpo.goodsShipmentLine = iol");
      hqlString.append(
          getWhereNumericClause("ordLine.standardPrice", filterCriteria.get("unitPrice"))).append(
          ")");
    }
    return hqlString.toString();
  }

  private String getJoinArgumentsFromFilters(Map<String, String> filterCriteria, String sortBy) {
    StringBuilder result = new StringBuilder();
    // Adds joins when required by filters
    if (filterCriteria.containsKey("product$_identifier")
        || (sortBy != null && sortBy.contains("product$_identifier"))) {
      result.append(" join iol.product as p");
    }
    if (filterCriteria.containsKey("attributeSetValue$_identifier")
        || (sortBy != null && sortBy.contains("attributeSetValue$_identifier"))) {
      result.append(" left join iol.attributeSetValue as a");
    }
    if (filterCriteria.containsKey("uOM$_identifier")
        || (sortBy != null && sortBy.contains("uOM$_identifier"))) {
      result.append(" join iol.uOM as u");
    }
    return result.toString();
  }

  private String getWhereComboClause(String propertyName, String jsonString) {
    StringBuffer whereClause = new StringBuffer();
    try {
      JSONArray jsonArray = new JSONArray(jsonString);
      if (jsonArray.length() > 0) {
        whereClause.append(" and (");
        for (int j = 0; j < jsonArray.length(); j++) {
          String value = jsonArray.getJSONObject(j).getString("value");
          String comparator = " like ";
          String clause = new StringBuffer().append(propertyName).append(comparator).append("'%")
              .append(value).append("%'").toString();
          if (jsonArray.getJSONObject(j).has("operator")
              && "iContains".equals(jsonArray.getJSONObject(j).getString("operator"))) {
            value = StringUtils.lowerCase(value);
            clause = new StringBuffer().append("lower(").append(propertyName).append(")")
                .append(comparator).append("'%").append(value).append("%'").toString();
          }
          if (j == 0) {
            whereClause.append(clause);
          } else {
            whereClause.append(" or ").append(clause);
          }
        }
        whereClause.append(")");
      }
    } catch (JSONException e) {
      log4j.error("RTV: error parsing criteria", e);
    }
    return whereClause.toString();
  }

  private String getWhereNumericClause(String propertyName, String filterCriteria) {
    StringBuffer whereClause = new StringBuffer();
    if (filterCriteria.startsWith("[")) {
      try {
        JSONArray jsonArray = new JSONArray(filterCriteria);
        if (jsonArray.length() > 0) {
          for (int j = 0; j < jsonArray.length(); j++) {
            whereClause.append(" and ");
            String value = jsonArray.getJSONObject(j).getString("value");
            String comparator = " = ";
            JSONObject o = jsonArray.getJSONObject(j);
            if (o.has("operator") && "greaterOrEqual".equals(o.getString("operator"))) {
              comparator = " >= ";
            } else if (o.has("operator") && "lessOrEqual".equals(o.getString("operator"))) {
              comparator = " <= ";
            } else if (o.has("operator") && "greaterThan".equals(o.getString("operator"))) {
              comparator = " > ";
            } else if (o.has("operator") && "lessThan".equals(o.getString("operator"))) {
              comparator = " < ";
            }
            whereClause.append(propertyName).append(comparator).append(value).toString();
          }
        }
      } catch (JSONException e) {
        log4j.error("RTV: error parsing criteria", e);
      }
    } else if (filterCriteria.startsWith("{")) {
      try {
        String comparator = " = ";
        JSONObject o = new JSONObject(filterCriteria);
        if (o.has("operator") && "greaterOrEqual".equals(o.getString("operator"))) {
          comparator = " >= ";
        } else if (o.has("operator") && "lessOrEqual".equals(o.getString("operator"))) {
          comparator = " <= ";
        } else if (o.has("operator") && "greaterThan".equals(o.getString("operator"))) {
          comparator = " > ";
        } else if (o.has("operator") && "lessThan".equals(o.getString("operator"))) {
          comparator = " < ";
        }
        String value = new JSONObject(filterCriteria).getString("value");
        whereClause.append(" and ").append(propertyName).append(comparator).append(value);
      } catch (JSONException e) {
        log4j.error("RTV: error parsing criteria", e);
      }
    } else {
      whereClause.append(" and ").append(propertyName).append(" = ").append(filterCriteria);
    }
    return whereClause.toString();
  }

  private String getWhereDateClause(String propertyName, String filterCriteria) {
    StringBuffer whereClause = new StringBuffer();
    if (filterCriteria.startsWith("[")) {
      try {
        JSONArray jsonArray = new JSONArray(filterCriteria);
        if (jsonArray.length() > 0) {
          for (int j = 0; j < jsonArray.length(); j++) {
            whereClause.append(" and ");
            String value = jsonArray.getJSONObject(j).getString("value");
            String comparator = " >= ";
            if (jsonArray.getJSONObject(j).has("operator")
                && "lessOrEqual".equals(jsonArray.getJSONObject(j).getString("operator"))) {
              comparator = " <= ";
            }
            whereClause.append("trunc(").append(propertyName).append(")").append(comparator)
                .append("'").append(value).append("'").toString();
          }
        }
      } catch (JSONException e) {
        log4j.error("RTV: error parsing criteria", e);
      }
    } else if (filterCriteria.startsWith("{")) {
      try {
        String comparator = " = ";
        JSONObject o = new JSONObject(filterCriteria);
        if (o.has("operator") && "greaterOrEqual".equals(o.getString("operator"))) {
          comparator = " >= ";
        } else if (o.has("operator") && "lessOrEqual".equals(o.getString("operator"))) {
          comparator = " <= ";
        }
        String value = new JSONObject(filterCriteria).getString("value");
        whereClause.append(" and trunc(").append(propertyName).append(")").append(comparator)
            .append("'").append(value).append("'");
      } catch (JSONException e) {
        log4j.error("RTV: error parsing criteria", e);
      }
    } else {
      whereClause.append(" and trunc(").append(propertyName).append(") = '").append(filterCriteria)
          .append("'");
    }
    return whereClause.toString();
  }

  private Map<String, Object> getGridmap(ShipmentInOutLine inOutLine, OrderLine orderLine) {
    Map<String, Object> myMap = new HashMap<String, Object>();
    myMap.put("obSelected", orderLine != null ? true : false);
    myMap.put("id", inOutLine.getId());
    myMap.put("client", inOutLine.getClient().getId());
    myMap.put("client$_identifier", inOutLine.getClient().getIdentifier());
    if (inOutLine.getAttributeSetValue() != null) {
      myMap.put("attributeSetValue", inOutLine.getAttributeSetValue().getId());
      myMap.put("attributeSetValue$_identifier", inOutLine.getAttributeSetValue().getIdentifier());
    }
    myMap.put("businessPartner", inOutLine.getShipmentReceipt().getBusinessPartner().getId());
    myMap.put("businessPartner$_identifier", inOutLine.getShipmentReceipt().getBusinessPartner()
        .getIdentifier());
    myMap.put("goodsShipmentLine", inOutLine.getId());
    myMap.put("goodsShipmentLine$_identifier", inOutLine.getIdentifier());
    myMap.put("product", inOutLine.getProduct().getId());
    myMap.put("product$_identifier", inOutLine.getProduct().getIdentifier());
    myMap.put("organization", inOutLine.getOrganization().getId());
    myMap.put("organization$_identifier", inOutLine.getOrganization().getIdentifier());
    myMap.put("uOM", inOutLine.getUOM().getId());
    myMap.put("uOM$_identifier", inOutLine.getUOM().getIdentifier());
    myMap.put("shipmentNumber", inOutLine.getShipmentReceipt().getDocumentNo());
    myMap.put("movementDate", inOutLine.getShipmentReceipt().getMovementDate());
    myMap.put("movementQuantity", inOutLine.getMovementQuantity());
    long init = System.currentTimeMillis();
    // BigDecimal returnedInOthers = BigDecimal.ZERO;
    BigDecimal returnedInOthers = getReturnedQty(inOutLine);

    myMap.put("returnQtyOtherRM", returnedInOthers);
    myMap.put("returned", orderLine == null ? "" : orderLine.getOrderedQuantity().negate());

    if (orderLine != null && orderLine.getReturnReason() != null) {
      myMap.put("returnReason", orderLine.getReturnReason().getId());
      myMap.put("returnReason$_identifier", orderLine.getReturnReason().getIdentifier());
    }
    if (inOutLine.getProcurementPOInvoiceMatchList() != null
        && inOutLine.getProcurementPOInvoiceMatchList().size() > 0
        && inOutLine.getProcurementPOInvoiceMatchList().get(0).getSalesOrderLine() != null) {
      myMap.put("orderNo", inOutLine.getProcurementPOInvoiceMatchList().get(0).getSalesOrderLine()
          .getSalesOrder().getDocumentNo());
      myMap.put("tax", inOutLine.getProcurementPOInvoiceMatchList().get(0).getSalesOrderLine()
          .getTax().getId());
      myMap.put("tax$_identifier", inOutLine.getProcurementPOInvoiceMatchList().get(0)
          .getSalesOrderLine().getTax().getId());
      if (inOutLine.getProcurementPOInvoiceMatchList().get(0).getSalesOrderLine().getSalesOrder()
          .getPriceList().isPriceIncludesTax()) {
        myMap.put("unitPrice", inOutLine.getProcurementPOInvoiceMatchList().get(0)
            .getSalesOrderLine().getGrossUnitPrice());
      } else {
        myMap.put("unitPrice", inOutLine.getProcurementPOInvoiceMatchList().get(0)
            .getSalesOrderLine().getUnitPrice());
      }
    }
    if (orderLine != null) {
      myMap.put("tax", orderLine.getTax().getId());
      myMap.put("tax$_identifier", orderLine.getTax().getId());
      if (orderLine.getSalesOrder().getPriceList().isPriceIncludesTax()) {
        myMap.put("unitPrice", orderLine.getGrossUnitPrice());
      } else {
        myMap.put("unitPrice", orderLine.getUnitPrice());
      }
    }

    return myMap;
  }

  private List<Map<String, Object>> getProductFilterData(Map<String, String> parameters, Order order) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select  p.id");
      hqlString.append(" from MaterialMgmtShipmentInOutLine as iol");
      hqlString.append(" join iol.shipmentReceipt as io");
      hqlString.append(" join iol.product as p");
      hqlString.append(" where io.businessPartner = :businessPartner");
      hqlString.append(" and io.processed = true");
      hqlString.append(" and io.documentStatus <> 'VO'");
      hqlString.append(" and io.salesTransaction = false");
      hqlString.append(" and iol.organization in :organizations");
      hqlString.append(" group by p.id, p.name");
      hqlString.append(" order by p.name");
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("businessPartner", order.getBusinessPartner());
      query.setParameterList("organizations", getOrganizations(order.getOrganization()));
      for (Object o : query.list()) {
        String productid = (String) o;
        Product product = OBDal.getInstance().get(Product.class, productid);
        result.add(getFilterMap(product));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<Map<String, Object>> getAttributeSetInstanceFilterData(
      Map<String, String> parameters, Order order) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select a.id");
      hqlString.append(" from MaterialMgmtShipmentInOutLine as iol");
      hqlString.append(" join iol.shipmentReceipt as io");
      hqlString.append(" join iol.attributeSetValue as a");
      hqlString.append(" where io.businessPartner = :businessPartner");
      hqlString.append(" and io.processed = true");
      hqlString.append(" and io.documentStatus <> 'VO'");
      hqlString.append(" and io.salesTransaction = false");
      hqlString.append(" and iol.organization in :organizations");
      hqlString.append(" group by a.id , a.serialNo, a.lot, a.lotName");
      hqlString.append(" order by a.serialNo, a.lot, a.lotName");
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("businessPartner", order.getBusinessPartner());
      query.setParameterList("organizations", getOrganizations(order.getOrganization()));
      for (Object o : query.list()) {
        String attributeid = (String) o;
        AttributeSetInstance attributeset = OBDal.getInstance().get(AttributeSetInstance.class,
            attributeid);
        result.add(getFilterMap(attributeset));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private List<Map<String, Object>> getUOMFilterData(Map<String, String> parameters, Order order) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hqlString = new StringBuilder();
      hqlString.append("select u.id");
      hqlString.append(" from MaterialMgmtShipmentInOutLine as iol");
      hqlString.append(" join iol.shipmentReceipt as io");
      hqlString.append(" join iol.uOM as u");
      hqlString.append(" where io.businessPartner = :businessPartner");
      hqlString.append(" and io.processed = true");
      hqlString.append(" and io.documentStatus <> 'VO'");
      hqlString.append(" and io.salesTransaction = false");
      hqlString.append(" and iol.organization in :organizations");
      hqlString.append(" group by u.id, u.name");
      hqlString.append(" order by u.name");
      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlString.toString());
      query.setParameter("businessPartner", order.getBusinessPartner());
      query.setParameterList("organizations", getOrganizations(order.getOrganization()));
      for (Object o : query.list()) {
        UOM uom = OBDal.getInstance().get(UOM.class, o);
        result.add(getFilterMap(uom));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private BigDecimal getReturnedQty(ShipmentInOutLine goodsShipmentLine) {
    final StringBuilder hqlString = new StringBuilder();
    hqlString.append("select sum(ol.orderedQuantity)");
    hqlString.append(" from OrderLine as ol");
    hqlString.append(" left join ol.salesOrder as o");
    hqlString.append(" where ol.goodsShipmentLine = :goodsShipmentLine");
    hqlString.append(" and o.processed = true");
    hqlString.append(" and o.documentStatus <> 'VO'");
    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(hqlString.toString());
    query.setParameter("goodsShipmentLine", goodsShipmentLine);

    for (Object resultObject : query.list()) {
      if (resultObject != null && resultObject.getClass().isInstance(BigDecimal.ONE)) {
        return ((BigDecimal) resultObject).negate();
      }
    }
    return BigDecimal.ZERO;
  }

  private Set<Organization> getOrganizations(Organization organization) {
    Set<Organization> organizations = new HashSet<Organization>();
    for (String orgId : new OrganizationStructureProvider()
        .getChildTree(organization.getId(), true)) {
      organizations.add(OBDal.getInstance().get(Organization.class, orgId));
    }
    return organizations;
  }

}