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
    String strFinPaymentId = requestParameters.get("fin_payment_id");
    String strInvoiceId = requestParameters.get("c_invoice_id");
    String strOrderId = requestParameters.get("c_order_id");
    String strJustCount = requestParameters.get("_justCount");
    boolean justCount = strJustCount.equalsIgnoreCase("true");

    String transformedHql = null;
    StringBuffer selectClause = new StringBuffer();
    StringBuffer whereClause = new StringBuffer();
    StringBuffer groupByClause = new StringBuffer();
    StringBuffer orderByClause = new StringBuffer();
    StringBuffer joinClause = new StringBuffer();

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
      selectClause.append(" case when 0 < 1 then false else true end as writeoff, ");
      if (strFinPaymentId != null) {
        selectClause.append(" case when COALESCE(fp.id, ips.invoice.id, ops.order.id) = '"
            + strFinPaymentId + "' then true else false end as OB_Selected ");
      } else {
        selectClause.append(" case when 0 < 1 then false else true end as OB_Selected ");
      }

      // Create WhereClause
      whereClause.append(" (psd.paymentDetails is null");
      // If opened from Payment Window, add payment details lines
      if (strFinPaymentId != null) {
        whereClause.append(" or fp.id = '" + strFinPaymentId + "'");
      } else if (strInvoiceId != null) {
        whereClause.append(" or inv.id = '" + strInvoiceId + "'");
      } else if (strOrderId != null) {
        whereClause.append(" or ord.id = '" + strOrderId + "'");
      }
      whereClause.append(") ");

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
      groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount), ");
      if (strInvoiceId != null) {
        groupByClause.append(" COALESCE(ips.invoice.id, ops.order.id) ");
      } else if (strOrderId != null) {
        groupByClause.append(" COALESCE(ops.order.id, ips.invoice.id) ");
      } else if (strFinPaymentId != null) {
        groupByClause.append(" COALESCE(fp.id, ips.invoice.id, ops.order.id) ");
      }

      // Replace where filters and having count clause
      if (requestParameters.containsKey("criteria")) {
        hqlQuery = replaceFiltersAndHavingClause(hqlQuery, transactionType);
      } else {
        hqlQuery = hqlQuery.replace("@havingClause@", "");
      }

      if (!justCount) {
        // Create OrderBy Clause based on parent window (Invoice, Order, Payment)
        orderByClause = createOrderByClause(strInvoiceId, strFinPaymentId, strOrderId);
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
      selectClause.append(" case when 0 < 1 then false else true end as writeoff, ");
      if (strFinPaymentId != null) {
        selectClause.append(" case when COALESCE(fp.id, ips.invoice.id, ops.order.id) = '"
            + strFinPaymentId + "' then true else false end as OB_Selected ");
      } else {
        selectClause.append(" case when 0 < 1 then false else true end as OB_Selected ");
      }

      // Create WhereClause
      whereClause.append(" (psd.paymentDetails is null");
      // If opened from Payment Window, add payment details lines
      if (strFinPaymentId != null) {
        whereClause.append(" or fp.id = '" + strFinPaymentId + "'");
      } else if (strInvoiceId != null) {
        whereClause.append(" or inv.id = '" + strInvoiceId + "'");
      } else if (strOrderId != null) {
        whereClause.append(" or ord.id = '" + strOrderId + "'");
      }
      whereClause.append(") ");
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
      groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount), ");
      if (strInvoiceId != null) {
        groupByClause.append(" COALESCE(ips.invoice.id, ops.order.id) ");
      } else if (strOrderId != null) {
        groupByClause.append(" COALESCE(ops.order.id, ips.invoice.id) ");
      } else if (strFinPaymentId != null) {
        groupByClause.append(" COALESCE(fp.id, ops.order.id, ips.invoice.id) ");
      }

      // Replace where filters and having count clause
      if (requestParameters.containsKey("criteria")) {
        hqlQuery = replaceFiltersAndHavingClause(hqlQuery, transactionType);
      } else {
        hqlQuery = hqlQuery.replace("@havingClause@", "");
      }

      if (!justCount) {
        // Create OrderBy Clause based on parent window (Invoice, Order, Payment)
        orderByClause = createOrderByClause(strInvoiceId, strFinPaymentId, strOrderId);
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
      selectClause.append(" case when 0 < 1 then false else true end as writeoff, ");
      if (strFinPaymentId != null) {
        selectClause.append(" case when COALESCE(fp.id, ips.invoice.id, ops.order.id) = '"
            + strFinPaymentId + "' then true else false end as OB_Selected ");
      } else {
        selectClause.append(" case when 0 < 1 then false else true end as OB_Selected ");
      }

      // Create WhereClause
      whereClause.append(" (psd.paymentDetails is null");
      // If opened from Payment Window, add payment details lines
      if (strFinPaymentId != null) {
        whereClause.append(" or fp.id = '" + strFinPaymentId + "'");
      } else if (strInvoiceId != null) {
        whereClause.append(" or inv.id = '" + strInvoiceId + "'");
      } else if (strOrderId != null) {
        whereClause.append(" or ord.id = '" + strOrderId + "'");
      }
      whereClause.append(") ");
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

      if (!justCount) {
        // Create OrderBy Clause based on parent window (Invoice, Order, Payment)
        orderByClause = createOrderByClause(strInvoiceId, strFinPaymentId, strOrderId);
      }
    }

    // Create Join Clause
    if (strFinPaymentId != null) {
      joinClause.append(" left outer join psd.paymentDetails as pd ");
      joinClause.append(" left outer join pd.finPayment as fp ");
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
    transformedHql = transformedHql.replace("@joinClause@ ", joinClause.toString());
    transformedHql = transformedHql.replace("@whereClause@ ", whereClause.toString());
    transformedHql = transformedHql.replace("@groupByClause@", groupByClause.toString());
    transformedHql = replaceOrderByClause(transformedHql, orderByClause, justCount);

    return transformedHql;
  }

  /**
   * @param _hqlQuery
   * @param transactionType
   * @return
   */
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
   * @param strInvoiceId
   * @param strFinPaymentId
   * @param strOrderId
   * @return
   */
  private StringBuffer createOrderByClause(String strInvoiceId, String strFinPaymentId,
      String strOrderId) {
    StringBuffer orderByClause = new StringBuffer();
    if (strInvoiceId != null) {
      orderByClause.append(" CASE WHEN ");
      orderByClause.append(" COALESCE(ips.invoice.id, ops.order.id) = '" + strInvoiceId + "'");
      orderByClause.append(" THEN 0");
      orderByClause.append(" ELSE 1");
      orderByClause.append(" END");
    } else if (strOrderId != null) {
      orderByClause.append(" CASE WHEN ");
      orderByClause.append(" COALESCE(ops.order.id, ips.invoice.id) = '" + strOrderId + "'");
      orderByClause.append(" THEN 0");
      orderByClause.append(" ELSE 1");
      orderByClause.append(" END");
    } else if (strFinPaymentId != null) {
      orderByClause.append(" CASE WHEN ");
      orderByClause.append(" COALESCE(fp.id, ips.invoice.id, ops.order.id) = '" + strFinPaymentId
          + "'");
      orderByClause.append(" THEN 0");
      orderByClause.append(" ELSE 1");
      orderByClause.append(" END");
    }
    return orderByClause;
  }

  /**
   * @param _hqlQuery
   * @param orderByClause
   * @return
   */
  private String replaceOrderByClause(String _hqlQuery, StringBuffer orderByClause,
      boolean justCount) {
    String hqlQuery = _hqlQuery;
    if (justCount) {
      hqlQuery = hqlQuery.replace("@orderByClause@", "");
    } else {
      if (hqlQuery.contains(" ORDER BY ")) {
        // remove @orderByClause@ from original query
        hqlQuery = hqlQuery.replace("@orderByClause@", "");
        hqlQuery = hqlQuery.concat(", " + orderByClause.toString());
      } else {
        hqlQuery = hqlQuery.replace("@orderByClause@", " ORDER BY " + orderByClause.toString());
      }
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