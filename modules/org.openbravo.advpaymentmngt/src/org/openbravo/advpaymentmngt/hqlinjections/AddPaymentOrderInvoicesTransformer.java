/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.service.datasource.HQLDataSourceService;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.AdvancedQueryBuilder;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

@ComponentProvider.Qualifier("58AF4D3E594B421A9A7307480736F03E")
public class AddPaymentOrderInvoicesTransformer extends HqlQueryTransformer {
  final static String RDBMS = new DalConnectionProvider(false).getRDBMS();
  final static String TABLE_ID = "58AF4D3E594B421A9A7307480736F03E";

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    List<String> selectedPSDs = new ArrayList<String>();
    boolean hasCriteria = requestParameters.containsKey("criteria");
    JSONObject criteria = new JSONObject();
    if (hasCriteria) {
      try {
        criteria = JsonUtils.buildCriteria(requestParameters);
        transformCriteria(criteria, selectedPSDs);
      } catch (JSONException ignore) {
      }
    }
    boolean hasSelectedIds = !selectedPSDs.isEmpty();
    String hqlQuery = _hqlQuery;
    // Retrieve Parameters
    String transactionType = requestParameters.get("transaction_type");
    String strJustCount = requestParameters.get("_justCount");
    boolean justCount = strJustCount.equalsIgnoreCase("true");

    StringBuffer selectClause = getSelectClause(transactionType, hasSelectedIds);
    StringBuffer whereClause = getWhereClause(transactionType, requestParameters, selectedPSDs);
    StringBuffer groupByClause = getGroupByClause(transactionType);
    StringBuffer orderByClause = new StringBuffer();
    if (!justCount) {
      orderByClause = getOrderByClause(transactionType, selectedPSDs, requestParameters);
    }

    // grid filters need to be removed from where clause and added as a having criteria.
    if (hasCriteria) {
      hqlQuery = removeGridFilters(hqlQuery);
      queryNamedParameters.clear();
      hqlQuery = calculateHavingClause(hqlQuery, transactionType, criteria, queryNamedParameters);
    } else {
      hqlQuery = hqlQuery.replace("@havingClause@", "");
    }

    // Remove alias @@ from Order By clause
    if (requestParameters.containsKey("_sortBy")) {
      String sortBy = requestParameters.get("_sortBy");
      if (sortBy.startsWith("-")) {
        sortBy = sortBy.substring(1);
      }
      hqlQuery = hqlQuery.replace("@" + sortBy + "@", sortBy);
    }

    String transformedHql = hqlQuery.replace("@selectClause@", selectClause.toString());
    transformedHql = transformedHql.replace("@joinClause@", " ");
    transformedHql = transformedHql.replace("@whereClause@", whereClause.toString());
    transformedHql = transformedHql.replace("@groupByClause@", groupByClause.toString());
    transformedHql = appendOrderByClause(transformedHql, orderByClause, justCount);
    // Sets parameters
    queryNamedParameters.put("currencyId", requestParameters.get("c_currency_id"));
    queryNamedParameters.put("isSalesTransaction",
        "true".equals(requestParameters.get("issotrx")) ? true : false);
    queryNamedParameters.put("businessPartnerId", requestParameters.get("received_from"));
    queryNamedParameters.put("paymentId", requestParameters.get("fin_payment_id"));
    return transformedHql;
  }

  private StringBuffer getSelectClause(String transactionType, boolean hasSelectedIds) {
    StringBuffer selectClause = new StringBuffer();
    // Create Select Clause
    selectClause.append(getAggregatorFunction("psd.id") + " as paymentScheduleDetail, ");
    if ("I".equals(transactionType)) {
      selectClause.append(getAggregatorFunction("ord.documentNo") + " as salesOrderNo, ");
      selectClause.append(" inv.documentNo as invoiceNo, ");
      selectClause
          .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(ipsfp.name, opsfp.name) as paymentMethodName, ");
      selectClause.append(" COALESCE(invbp.id, ordbp.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
    } else if ("O".equals(transactionType)) {
      selectClause.append(" ord.documentNo as salesOrderNo, ");
      selectClause.append(getAggregatorFunction("inv.documentNo") + " as invoiceNo, ");
      selectClause
          .append(" COALESCE(ops.finPaymentmethod.id, ips.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(opsfp.name, ipsfp.name) as paymentMethodName, ");
      selectClause.append(" COALESCE(invbp.id, ordbp.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(ops.expectedDate, ips.expectedDate) as expectedDate, ");
    } else {
      selectClause.append(" ord.documentNo as salesOrderNo, ");
      selectClause.append(" inv.documentNo as invoiceNo, ");
      selectClause
          .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(ipsfp.name, opsfp.name) as paymentMethodName, ");
      selectClause.append(" COALESCE(invbp.id, ordbp.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
    }
    selectClause.append(" sum(COALESCE(ips.amount, ops.amount)) as expectedAmount, ");
    selectClause
        .append(" sum(COALESCE(inv.grandTotalAmount, ord.grandTotalAmount)) as invoicedAmount, ");
    selectClause.append(" SUM(psd.amount + psd.writeoffAmount) as outstandingAmount, ");
    selectClause.append(" COALESCE(sum(pd.amount), 0) as amount, ");
    selectClause
        .append(" case when sum(psd.writeoffAmount) <> 0 then true else false end as writeoff, ");
    if (hasSelectedIds) {
      // if there are selected ids selection is done in the client.
      selectClause.append(" case when 1 < 0 then true else false end as OB_Selected ");
    } else {
      selectClause
          .append(" case when max(fp.id) is not null then true else false end as OB_Selected ");
    }
    return selectClause;
  }

  private StringBuffer getWhereClause(String transactionType,
      Map<String, String> requestParameters, List<String> selectedPSDs) {
    String strBusinessPartnerId = requestParameters.get("received_from");
    String strFinPaymentId = requestParameters.get("fin_payment_id");

    StringBuffer whereClause = new StringBuffer();
    // Create WhereClause
    whereClause.append(" (psd.paymentDetails is null");
    // If opened from Payment Window, add payment details lines
    if (strFinPaymentId != null) {
      whereClause.append(" or (fp.id = :paymentId and (ips is not null or ops is not null)) ");
    }
    whereClause.append(") ");

    whereClause.append(" and (oinfo is null or oinfo.active = true) ");
    whereClause.append("  and ( ");
    if (!selectedPSDs.isEmpty()) {
      whereClause.append(" psd.id in (");
      boolean isFirst = true;
      int i = 1;
      for (String strPSD : selectedPSDs) {
        if (isFirst) {
          isFirst = false;
        } else {
          whereClause.append(",");
        }
        whereClause.append("'" + strPSD + "'");

        i++;
        if (i % 2000 == 0) {
          whereClause.append(")");
          whereClause.append(" or psd.id in (");
          isFirst = true;
        }
      }
      whereClause.append(")");
    } else {
      whereClause.append(" fp.id = :paymentId ");
    }
    whereClause.append("  or ");
    if ("I".equals(transactionType)) {

      whereClause.append(" (inv is not null ");
      if (strBusinessPartnerId != null && !"null".equals(strBusinessPartnerId)) {
        whereClause.append(" and invbp.id = :businessPartnerId ");
      }
      whereClause.append(" and inv.salesTransaction = :isSalesTransaction");
      whereClause.append(" and inv.currency.id = :currencyId ) ");

    } else if ("O".equals(transactionType)) {
      whereClause.append(" (ord is not null ");
      if (strBusinessPartnerId != null && !"null".equals(strBusinessPartnerId)) {
        whereClause.append(" and ordbp.id = :businessPartnerId ");
      }
      whereClause.append(" and ord.salesTransaction = :isSalesTransaction");
      whereClause.append(" and ord.currency.id = :currencyId ) ");

    } else {

      whereClause.append(" (inv is not null ");
      if (strBusinessPartnerId != null && !"null".equals(strBusinessPartnerId)) {
        whereClause.append(" and invbp.id = :businessPartnerId ");
      }
      whereClause.append(" and inv.salesTransaction = :isSalesTransaction");
      whereClause.append(" and inv.currency.id = :currencyId ) ");
      whereClause.append(" or (ord is not null ");
      if (strBusinessPartnerId != null && !"null".equals(strBusinessPartnerId)) {
        whereClause.append(" and ordbp.id = :businessPartnerId");
      }
      whereClause.append(" and ord.salesTransaction = :isSalesTransaction");
      whereClause.append(" and ord.currency.id = :currencyId ) ");

    }

    whereClause.append(")");
    return whereClause;

  }

  private StringBuffer getGroupByClause(String transactionType) {
    StringBuffer groupByClause = new StringBuffer();
    // Create GroupBy Clause
    if ("I".equals(transactionType)) {
      groupByClause.append(" inv.documentNo, ");
      groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
      groupByClause.append(" COALESCE(ipsfp.name, opsfp.name), ");
      groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
      groupByClause.append(" COALESCE(ipriority.priority, opriority.priority), ");
    } else if ("O".equals(transactionType)) {
      groupByClause.append(" ord.documentNo, ");
      groupByClause.append(" COALESCE(ops.finPaymentmethod.id, ips.finPaymentmethod.id), ");
      groupByClause.append(" COALESCE(opsfp.name, ipsfp.name), ");
      groupByClause.append(" COALESCE(ops.expectedDate, ips.expectedDate), ");
      groupByClause.append(" COALESCE(opriority.priority, ipriority.priority), ");
    } else {
      groupByClause.append(" inv.documentNo, ");
      groupByClause.append(" ord.documentNo, ");
      groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
      groupByClause.append(" COALESCE(ipsfp.name, opsfp.name), ");
      groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
      groupByClause.append(" COALESCE(ipriority.priority, opriority.priority), ");
    }
    groupByClause.append(" COALESCE(invbp.id, ordbp.id), ");
    groupByClause.append(" COALESCE(invbp.name, ordbp.name) ");
    return groupByClause;
  }

  /**
   * Order by selectedPSDs, scheduled date and document number
   * 
   * @param selectedPSDs
   * @param transactionType
   * @return
   */
  private StringBuffer getOrderByClause(String transactionType, List<String> selectedPSDs,
      Map<String, String> requestParameters) {
    StringBuffer orderByClause = new StringBuffer();
    if (selectedPSDs.size() == 0) {
      String strInvoiceId = requestParameters.get("c_invoice_id");
      String strOrderId = requestParameters.get("c_order_id");
      if (strInvoiceId != null) {
        orderByClause.append(" CASE WHEN MAX(inv.id) = '" + strInvoiceId + "' THEN 0 ELSE 1 END ");
      } else if (strOrderId != null) {
        orderByClause.append(" CASE WHEN MAX(ord.id) = '" + strOrderId + "' THEN 0 ELSE 1 END ");
      } else {
        orderByClause.append(" CASE WHEN MAX(fp.id) IS NOT NULL THEN 0 ELSE 1 END ");
      }
    } else {
      String strAggId = getAggregatorFunction("psd.id");
      orderByClause.append(" CASE WHEN ");
      boolean isFirst = true;
      for (String strPSDId : selectedPSDs) {
        if (!isFirst) {
          orderByClause.append(" OR ");
        }
        orderByClause.append(strAggId + " LIKE '%" + strPSDId + "%'");
        isFirst = false;
      }
      orderByClause.append(" THEN 0 ELSE 1 END ");
    }
    if ("O".equals(transactionType)) {
      orderByClause.append(", COALESCE(opriority.priority, ipriority.priority) ");
      orderByClause.append(", COALESCE(ops.expectedDate, ips.expectedDate) ");
    } else {
      orderByClause.append(", COALESCE(ipriority.priority, opriority.priority) ");
      orderByClause.append(", COALESCE(ips.expectedDate, ops.expectedDate) ");
    }
    if ("O".equals(transactionType)) {
      orderByClause.append(", ord.documentNo ");
    } else {
      orderByClause.append(", inv.documentNo ");
    }

    return orderByClause;
  }

  private String removeGridFilters(String _hqlQuery) {
    String hqlQuery = _hqlQuery;
    // Get the substring of grid filter inside where clause, if transaction type is "Orders" or
    // "Invoices", put in the having clause
    int whereIndex = hqlQuery.indexOf(" where ");
    int orgFilterIndex = hqlQuery.indexOf(" psd.organization in ", whereIndex);
    int beginIndex = hqlQuery.indexOf(" AND ", orgFilterIndex);
    int endIndex = hqlQuery.indexOf("and @whereClause@");
    String gridFilters = hqlQuery.substring(beginIndex, endIndex);

    hqlQuery = hqlQuery.replace(gridFilters, " ");
    return hqlQuery;
  }

  /**
   * @param _hqlQuery
   * @param transactionType
   * @param criteria
   * @param queryNamedParameters
   * @return
   */
  private String calculateHavingClause(String _hqlQuery, String transactionType,
      JSONObject criteria, Map<String, Object> queryNamedParameters) {
    String hqlQuery = _hqlQuery;
    StringBuffer havingClause = new StringBuffer();

    AdvancedQueryBuilder queryBuilder = new AdvancedQueryBuilder();
    queryBuilder.setEntity(ModelProvider.getInstance().getEntityByTableId(TABLE_ID));
    queryBuilder.setCriteria(criteria);
    String havingGridFilters = queryBuilder.getWhereClause();
    queryNamedParameters.putAll(queryBuilder.getNamedParameters());
    if (!havingGridFilters.trim().isEmpty()) {
      // if the filter where clause contains the string 'where', get rid of it
      havingGridFilters = havingGridFilters.replaceAll("(?i)WHERE", " ");
    }

    // replace the property names with the column alias
    Table table = OBDal.getInstance().get(Table.class, TABLE_ID);
    havingGridFilters = replaceParametersWithAlias(table, havingGridFilters);

    if (havingGridFilters.contains("@paymentScheduleDetail@")) {
      havingGridFilters = havingGridFilters.replaceAll("@paymentScheduleDetail@",
          getAggregatorFunction("psd.id"));
    }
    if ("I".equals(transactionType)) {
      if (havingGridFilters.contains("@salesOrderNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@salesOrderNo@",
            getAggregatorFunction("ord.documentNo"));
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@invoiceNo@", "inv.documentNo");
      }
      if (havingGridFilters.contains("@paymentMethod@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethod@",
            "COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id)");
      }
      if (havingGridFilters.contains("@paymentMethodName@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethodName@",
            "COALESCE(ipsfp.name, opsfp.name)");
      }
      if (havingGridFilters.contains("@expectedDate@")) {
        havingGridFilters = havingGridFilters.replaceAll("@expectedDate@",
            "COALESCE(ips.expectedDate, ops.expectedDate)");
      }
    } else if ("O".equals(transactionType)) {
      if (havingGridFilters.contains("@salesOrderNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@salesOrderNo@", "ord.documentNo");
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@invoiceNo@",
            getAggregatorFunction("inv.documentNo"));
      }
      if (havingGridFilters.contains("@paymentMethod@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethod@",
            "COALESCE(ops.finPaymentmethod.id, ips.finPaymentmethod.id)");
      }
      if (havingGridFilters.contains("@paymentMethodName@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethodName@",
            "COALESCE(opsfp.name, ipsfp.name)");
      }
      if (havingGridFilters.contains("@expectedDate@")) {
        havingGridFilters = havingGridFilters.replaceAll("@expectedDate@",
            "COALESCE(ops.expectedDate, ips.expectedDate)");
      }
    } else {
      if (havingGridFilters.contains("@salesOrderNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@salesOrderNo@", "ord.documentNo");
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@invoiceNo@", "inv.documentNo");
      }
      if (havingGridFilters.contains("@paymentMethod@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethod@",
            "COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id)");
      }
      if (havingGridFilters.contains("@paymentMethodName@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentMethodName@",
            "COALESCE(ipsfp.name, opsfp.name)");
      }
      if (havingGridFilters.contains("@expectedDate@")) {
        havingGridFilters = havingGridFilters.replaceAll("@expectedDate@",
            "COALESCE(ips.expectedDate, ops.expectedDate)");
      }
    }
    if (havingGridFilters.contains("@outstandingAmount@")) {
      havingGridFilters = havingGridFilters.replaceAll("@outstandingAmount@", "SUM(psd.amount)");
    }
    if (havingGridFilters.contains("@amount@")) {
      havingGridFilters = havingGridFilters.replaceAll("@amount@", "COALESCE(sum(fp.amount), 0)");
    }

    havingClause.append(" having ( " + havingGridFilters + " )");
    hqlQuery = hqlQuery.replace("@havingClause@", havingClause.toString());

    return hqlQuery;
  }

  /**
   * @param _hqlQuery
   * @param transactionType
   * @param selectedPSDs
   * @param orderByClause
   * @return
   */
  private String appendOrderByClause(String _hqlQuery, StringBuffer orderByClause, boolean justCount) {
    String hqlQuery = _hqlQuery;
    if (!justCount) {
      if (hqlQuery.contains(" ORDER BY ")) {
        hqlQuery = hqlQuery.concat(", ");
      } else {
        hqlQuery = hqlQuery.concat(" ORDER BY ");
      }
      hqlQuery = hqlQuery.concat(orderByClause.toString());
    }
    return hqlQuery;
  }

  private void transformCriteria(JSONObject buildCriteria, List<String> selectedPSDs)
      throws JSONException {
    JSONArray criteriaArray = buildCriteria.getJSONArray("criteria");
    JSONArray newCriteriaArray = new JSONArray();
    for (int i = 0; i < criteriaArray.length(); i++) {
      JSONObject criteria = criteriaArray.getJSONObject(i);
      if (criteria.has("fieldName") && criteria.getString("fieldName").equals("id")) {
        String value = criteria.getString("value");
        for (String psdID : value.split(",")) {
          JSONObject newCriteria = criteria;
          newCriteria.put("value", psdID.trim());
          newCriteria.put("operator", "iContains");
          selectedPSDs.add(psdID.trim());
          newCriteriaArray.put(newCriteria);
        }
      } else {
        newCriteriaArray.put(criteria);
      }
    }
    buildCriteria.put("criteria", newCriteriaArray);
  }

  /**
   * @param expression
   * @return
   */
  private String getAggregatorFunction(String expression) {
    return " hqlagg(" + expression + ")";
  }

  /**
   * @see HQLDataSourceService#replaceParametersWithAlias(Table, String)
   */
  private String replaceParametersWithAlias(Table table, String whereClause) {
    if (whereClause.trim().isEmpty()) {
      return whereClause;
    }
    String updatedWhereClause = whereClause.toString();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    for (Column column : table.getADColumnList()) {
      // look for the property name, replace it with the column alias
      Property property = entity.getPropertyByColumnName(column.getDBColumnName());
      Map<String, String> replacementMap = new HashMap<String, String>();
      String propertyNameBefore = null;
      String propertyNameAfter = null;
      if (property.isPrimitive()) {
        // if the property is a primitive, just replace the property name with the column alias
        propertyNameBefore = property.getName();
        propertyNameAfter = column.getEntityAlias();
      } else {
        // if the property is a FK, then the name of the identifier property of the referenced
        // entity has to be appended

        if (column.isLinkToParentColumn()) {
          propertyNameBefore = property.getName() + "." + JsonConstants.ID;
          propertyNameAfter = column.getEntityAlias() + "." + JsonConstants.ID;
        } else {
          Entity refEntity = property.getReferencedProperty().getEntity();
          String identifierPropertyName = refEntity.getIdentifierProperties().get(0).getName();
          propertyNameBefore = property.getName() + "." + identifierPropertyName;
          propertyNameAfter = column.getEntityAlias() + "." + identifierPropertyName;
        }

      }
      replacementMap.put(" " + propertyNameBefore + " ", " " + propertyNameAfter + " ");
      replacementMap.put("(" + propertyNameBefore + ")", "(" + propertyNameAfter + ")");
      for (String toBeReplaced : replacementMap.keySet()) {
        if (updatedWhereClause.contains(toBeReplaced)) {
          updatedWhereClause = updatedWhereClause.replace(toBeReplaced,
              replacementMap.get(toBeReplaced));
        }
      }
    }
    return updatedWhereClause;
  }

}