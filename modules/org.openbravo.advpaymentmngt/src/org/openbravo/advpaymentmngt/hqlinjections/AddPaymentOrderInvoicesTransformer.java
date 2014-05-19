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

import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.db.DalConnectionProvider;

@ComponentProvider.Qualifier("58AF4D3E594B421A9A7307480736F03E")
public class AddPaymentOrderInvoicesTransformer extends HqlQueryTransformer {
  final static String RDBMS = new DalConnectionProvider(false).getRDBMS();

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    String hqlQuery = _hqlQuery;
    // Retrieve Parameters
    String transactionType = requestParameters.get("transaction_type");
    String strBusinessPartnerId = requestParameters.get("received_from");
    String strCurrencyId = requestParameters.get("c_currency_id");
    String strFinPaymentMethodId = requestParameters.get("fin_paymentmethod_id");
    boolean isSalesTransaction = "true".equals(requestParameters.get("issotrx")) ? true : false;

    String transformedHql = null;
    StringBuffer selectClause = new StringBuffer();
    StringBuffer whereClause = new StringBuffer();
    StringBuffer groupByClause = new StringBuffer();

    if ("I".equals(transactionType)) {

      // Create Select Clause
      selectClause.append(getAggregatorFunction("psd.id") + " as paymentScheduleDetail, ");
      selectClause.append(getAggregatorFunction("ord.documentNo") + " as salesOrderNo, ");
      selectClause.append(" inv.documentNo as invoiceNo, ");
      selectClause
          .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(ipsfp.name, opsfp.name) as paymentMethodName, ");
      selectClause
          .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
      selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
      selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
      selectClause
          .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
      selectClause.append(" SUM(psd.amount) as outstandingAmount, ");
      selectClause.append(" 0 as amount, ");
      selectClause.append(" 'N' as writeoff ");

      // Create WhereClause
      whereClause.append(" psd.paymentDetails is null ");
      whereClause.append(" and (oinfo is null or oinfo.active = true) ");
      whereClause.append(" and ((inv is not null ");
      if (strBusinessPartnerId != null) {
        whereClause.append(" and inv.businessPartner.id = '" + strBusinessPartnerId + "'");
      }
      if (strFinPaymentMethodId != null) {
        whereClause.append(" and ips.finPaymentmethod.id = '" + strFinPaymentMethodId + "'");
      }
      whereClause.append(" and inv.salesTransaction = " + isSalesTransaction);
      whereClause.append(" and inv.currency.id = '" + strCurrencyId + "' )) ");

      // Create GroupBy Clause
      groupByClause.append(" inv.documentNo, ");
      groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
      groupByClause.append(" COALESCE(ipsfp.name, opsfp.name), ");
      groupByClause.append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id), ");
      groupByClause.append(" COALESCE(invbp.name, ordbp.name), ");
      groupByClause.append(" COALESCE(inv.invoiceDate, ord.orderDate), ");
      groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
      groupByClause.append(" COALESCE(ips.amount, ops.amount), ");
      groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) ");

      // Replace where filters and having count clause
      if (requestParameters.containsKey("criteria")) {
        hqlQuery = replaceFiltersAndHavingClause(hqlQuery, transactionType);
      } else {
        hqlQuery = hqlQuery.replace("@havingClause@", "");
      }

    } else if ("O".equals(transactionType)) {

      // Create Select Clause
      selectClause.append(getAggregatorFunction("psd.id") + " as paymentScheduleDetail, ");
      selectClause.append(" ord.documentNo as salesOrderNo, ");
      selectClause.append(getAggregatorFunction("inv.documentNo") + " as invoiceNo, ");
      selectClause
          .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(ipsfp.name, opsfp.name) as paymentMethodName, ");
      selectClause
          .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
      selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
      selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
      selectClause
          .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
      selectClause.append(" SUM(psd.amount) as outstandingAmount, ");
      selectClause.append(" 0 as amount, ");
      selectClause.append(" 'N' as writeoff ");

      // Create WhereClause
      whereClause.append(" psd.paymentDetails is null ");
      whereClause.append(" and (oinfo is null or oinfo.active = true) ");
      whereClause.append(" and ((ord is not null ");
      if (strBusinessPartnerId != null) {
        whereClause.append(" and ord.businessPartner.id = '" + strBusinessPartnerId + "'");
      }
      if (strFinPaymentMethodId != null) {
        whereClause.append(" and ops.finPaymentmethod.id = '" + strFinPaymentMethodId + "'");
      }
      whereClause.append(" and ord.salesTransaction = " + isSalesTransaction);
      whereClause.append(" and ord.currency.id = '" + strCurrencyId + "' )) ");

      // Create GroupBy Clause
      groupByClause.append(" ord.documentNo, ");
      groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
      groupByClause.append(" COALESCE(ipsfp.name, opsfp.name), ");
      groupByClause.append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id), ");
      groupByClause.append(" COALESCE(invbp.name, ordbp.name), ");
      groupByClause.append(" COALESCE(inv.invoiceDate, ord.orderDate), ");
      groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
      groupByClause.append(" COALESCE(ips.amount, ops.amount), ");
      groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) ");

      // Replace where filters and having count clause
      if (requestParameters.containsKey("criteria")) {
        hqlQuery = replaceFiltersAndHavingClause(hqlQuery, transactionType);
      } else {
        hqlQuery = hqlQuery.replace("@havingClause@", "");
      }

    } else {
      // Create Select Clause
      selectClause.append(" psd.id as paymentScheduleDetail, ");
      selectClause.append(" ord.documentNo as salesOrderNo, ");
      selectClause.append(" inv.documentNo as invoiceNo, ");
      selectClause
          .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
      selectClause.append(" COALESCE(ipsfp.name, opsfp.name) as paymentMethodName, ");
      selectClause
          .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
      selectClause.append(" COALESCE(invbp.name, ordbp.name) as businessPartnerName, ");
      selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
      selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
      selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
      selectClause
          .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
      selectClause.append(" psd.amount as outstandingAmount, ");
      selectClause.append(" 0 as amount, ");
      selectClause.append(" 'N' as writeoff ");

      // Create WhereClause
      whereClause.append(" psd.paymentDetails is null ");
      whereClause.append(" and (oinfo is null or oinfo.active = true) ");
      whereClause.append(" and ((inv is not null ");
      if (strBusinessPartnerId != null) {
        whereClause.append(" and inv.businessPartner.id = '" + strBusinessPartnerId + "'");
      }
      if (strFinPaymentMethodId != null) {
        whereClause.append(" and ips.finPaymentmethod.id = '" + strFinPaymentMethodId + "'");
      }
      whereClause.append(" and inv.salesTransaction = " + isSalesTransaction);
      whereClause.append(" and inv.currency.id = '" + strCurrencyId + "' ) ");
      whereClause.append(" or (ord is not null ");
      if (strBusinessPartnerId != null) {
        whereClause.append(" and ord.businessPartner.id = '" + strBusinessPartnerId + "'");
      }
      if (strFinPaymentMethodId != null) {
        whereClause.append(" and ops.finPaymentmethod.id = '" + strFinPaymentMethodId + "'");
      }
      whereClause.append(" and ord.salesTransaction = " + isSalesTransaction);
      whereClause.append(" and ord.currency.id = '" + strCurrencyId + "' )) ");

      // There is no Group By Clause
      hqlQuery = hqlQuery.replace("group by", "");

      // Replace where filters and having count clause
      if (requestParameters.containsKey("criteria")) {
        hqlQuery = replaceFiltersAndHavingClause(hqlQuery, transactionType);
      } else {
        hqlQuery = hqlQuery.replace("@havingClause@", "");
      }
    }

    // Remove alias @@ from Order By clause
    if (requestParameters.containsKey("_sortBy")) {
      String sortBy = requestParameters.get("_sortBy");
      if (sortBy.startsWith("-")) {
        sortBy = sortBy.substring(1);
      }
      hqlQuery = hqlQuery.replace("@" + sortBy + "@", sortBy);
    }

    transformedHql = hqlQuery.replace("@selectClause@ ", selectClause.toString());
    transformedHql = transformedHql.replace("@whereClause@ ", whereClause.toString());
    transformedHql = transformedHql.replace("@groupByClause@", groupByClause.toString());

    return transformedHql;
  }

  private String replaceFiltersAndHavingClause(String _hqlQuery, String transactionType) {
    String hqlQuery = _hqlQuery;
    StringBuffer havingClause = new StringBuffer();

    // Get the substring of grid filter inside where clause, if transaction type is "Orders" or
    // "Invoices", put in the having clause
    int whereIndex = hqlQuery.indexOf(" where ");
    int orgFilterIndex = hqlQuery.indexOf(" psd.organization in ", whereIndex);
    int beginIndex = hqlQuery.indexOf(" AND ", orgFilterIndex);
    int endIndex = hqlQuery.indexOf("and @whereClause@");
    String gridFilters = hqlQuery.substring(beginIndex, endIndex);
    String havingGridFilters = gridFilters.substring(4, gridFilters.length());

    if ("I".equals(transactionType)) {
      hqlQuery = hqlQuery.replace(gridFilters, " ");

      if (havingGridFilters.contains("@paymentScheduleDetail@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentScheduleDetail@",
            getAggregatorFunction("psd.id"));
      }
      if (havingGridFilters.contains("@salesOrderNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@salesOrderNo@",
            getAggregatorFunction("ord.documentNo"));
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@invoiceNo@", "inv.documentNo");
      }
      if (havingGridFilters.contains("@outstandingAmount@")) {
        havingGridFilters = havingGridFilters.replaceAll("@outstandingAmount@", "SUM(psd.amount)");
      }
      havingClause.append(" having ( " + havingGridFilters + " )");
      hqlQuery = hqlQuery.replace("@havingClause@", havingClause.toString());
    } else if ("O".equals(transactionType)) {
      hqlQuery = hqlQuery.replace(gridFilters, " ");

      if (havingGridFilters.contains("@paymentScheduleDetail@")) {
        havingGridFilters = havingGridFilters.replaceAll("@paymentScheduleDetail@",
            getAggregatorFunction("psd.id"));
      }
      if (havingGridFilters.contains("@salesOrderNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@salesOrderNo@", "ord.documentNo");
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        havingGridFilters = havingGridFilters.replaceAll("@invoiceNo@",
            getAggregatorFunction("inv.documentNo"));
      }
      if (havingGridFilters.contains("@outstandingAmount@")) {
        havingGridFilters = havingGridFilters.replaceAll("@outstandingAmount@", "SUM(psd.amount)");
      }
      havingClause.append(" having ( " + havingGridFilters + " )");
      hqlQuery = hqlQuery.replace("@havingClause@", havingClause.toString());
    } else {
      if (havingGridFilters.contains("@paymentScheduleDetail@")) {
        hqlQuery = hqlQuery.replaceAll("@paymentScheduleDetail@", "psd.id");
      }
      if (havingGridFilters.contains("@salesOrderNo@")) {
        hqlQuery = hqlQuery.replaceAll("@salesOrderNo@", "ord.documentNo");
      }
      if (havingGridFilters.contains("@invoiceNo@")) {
        hqlQuery = hqlQuery.replaceAll("@invoiceNo@", "inv.documentNo");
      }
      if (havingGridFilters.contains("@outstandingAmount@")) {
        hqlQuery = hqlQuery.replaceAll("@outstandingAmount@", "psd.amount");
      }
      hqlQuery = hqlQuery.replace("@havingClause@", "");
    }
    return hqlQuery;
  }

  /**
   * @param expression
   * @return
   */
  private String getAggregatorFunction(String expression) {
    if (RDBMS.equals("ORACLE")) {
      return " stragg(to_char(" + expression + "))";
    }
    return " array_to_string(array_agg(" + expression + "), ',')";
  }
}