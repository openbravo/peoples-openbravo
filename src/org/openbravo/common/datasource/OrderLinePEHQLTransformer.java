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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.common.datasource;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("7EB9FFD7BD4E4113A13A096EB879D358")
public class OrderLinePEHQLTransformer extends HqlQueryTransformer {
  protected static final String EMPTY_STRING = "";
  protected boolean isSalesTransaction;

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    isSalesTransaction = StringUtils.equals(requestParameters.get("@Invoice.salesTransaction@"),
        "true");
    final String strInvoicePriceListId = requestParameters.get("@Invoice.priceList@");
    final PriceList priceList = OBDal.getInstance().get(PriceList.class, strInvoicePriceListId);
    final String strBusinessPartnerId = requestParameters.get("@Invoice.businessPartner@");
    final String strCurrencyId = requestParameters.get("@Invoice.currency@");
    final String strInvoiceId = requestParameters.get("@Invoice.id@");

    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);
    if (!isSalesTransaction) {
      queryNamedParameters.put("invId", strInvoiceId);
    }

    String transformedHql = _hqlQuery.replace("@selectClause@", getSelectClauseHQL());
    transformedHql = transformedHql.replace("@fromClause@", getFromClauseHQL());
    transformedHql = transformedHql.replace("@whereClause@", getWhereClauseHQL());
    transformedHql = transformedHql.replace("@groupByClause@", getGroupByHQL());
    transformedHql = transformedHql.replace("@orderedQuantity@", getOrderedQuantityHQL());
    transformedHql = transformedHql.replace("@operativeQuantity@", getOperativeQuantityHQL());
    transformedHql = transformedHql.replace("@orderQuantity@", getOrderQuantityHQL());
    return transformedHql;
  }

  protected String getSelectClauseHQL() {
    return EMPTY_STRING;
  }

  protected String getGroupByHQL() {
    StringBuilder groupByClause = new StringBuilder();
    groupByClause.append("  o.id,");
    groupByClause.append("  o.documentNo,");
    groupByClause.append("  o.orderDate,");
    groupByClause.append("  o.scheduledDeliveryDate,");
    groupByClause.append("  e.orderedQuantity,");
    groupByClause.append("  e.orderDate,");
    groupByClause.append("  uom.name,");
    groupByClause.append("  p.id,");
    groupByClause.append("  p.name,");
    groupByClause.append("  e.lineNo,");
    groupByClause.append("  e.id,");
    groupByClause.append("  org.id,");
    groupByClause.append("  org.name,");
    groupByClause.append("  COALESCE(e.asset.id, o.asset.id),");
    groupByClause.append("  COALESCE(e.project.id, o.project.id),");
    groupByClause.append("  COALESCE(e.costcenter.id, o.costcenter.id),");
    groupByClause.append("  COALESCE(e.stDimension.id, o.stDimension.id),");
    groupByClause.append("  COALESCE(e.ndDimension.id, o.ndDimension.id),");
    groupByClause.append("  e.explode,");
    groupByClause.append("  aum.id,");
    groupByClause.append("  e.operativeQuantity,");
    groupByClause.append("  dt.id,");
    groupByClause.append("  pl.id,");
    groupByClause.append("  bp.id,");
    groupByClause.append("  il.id,");
    groupByClause.append("  wh.id,");
    groupByClause.append("  wh.name,");
    groupByClause.append("  ma.id,");
    groupByClause.append("  ma.serialNo,");
    groupByClause.append("  ma.expirationDate,");
    groupByClause.append("  ma.lotName,");
    groupByClause.append("  @orderQuantity@");
    if (isSalesTransaction) {
      groupByClause.append(" , e.invoicedQuantity");
    } else {
      groupByClause
          .append(" HAVING ((e.explode='Y') OR ((e.orderedQuantity-SUM(COALESCE(m.quantity,0))) <> 0))");
    }
    return groupByClause.toString();
  }

  protected String getWhereClauseHQL() {
    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" and o.salesTransaction = :issotrx");
    whereClause.append(" and bp.id = :bp");
    whereClause.append(" and pl.priceIncludesTax = :plIncTax");
    whereClause.append(" and o.currency.id = :cur");
    if (isSalesTransaction) {
      whereClause.append(" and (");
      whereClause.append("     ic.term in ('D', 'S') and ic.deliveredQuantity <> 0");
      whereClause.append("  or (ic.term = 'I' AND EXISTS");
      whereClause.append("   (SELECT 1");
      whereClause.append("    FROM OrderLine ol2");
      whereClause.append("    WHERE ol2.salesOrder = o.id");
      whereClause.append("    GROUP BY ol2.id ");
      whereClause.append("    HAVING SUM(ol2.orderedQuantity) - SUM(ol2.invoicedQuantity) <> 0))");
      whereClause.append("  or (ic.term = 'O' and ic.orderedQuantity = ic.deliveredQuantity)");
      whereClause.append(" )");
    } else {
      whereClause.append(" and o.documentStatus in ('CO', 'CL')");
      whereClause.append(" and o.invoiceTerms <> 'N'");
      whereClause.append(" and NOT EXISTS");
      whereClause.append("(SELECT 1");
      whereClause.append(" FROM OrderLine co1");
      whereClause.append("   left join co1.invoiceLineList ci1");
      whereClause
          .append("   left join co1.procurementPOInvoiceMatchList mp1 with mp1.invoiceLine.id is not null");
      whereClause.append(" WHERE co1.id = e.id");
      whereClause.append("   AND ci1.invoice.id = :invId");
      whereClause.append(" GROUP BY ci1.salesOrderLine.id,  co1.orderedQuantity");
      whereClause
          .append(" HAVING (SUM(COALESCE(ci1.invoicedQuantity, 0))-(COALESCE(co1.orderedQuantity,0)-SUM(COALESCE(mp1.quantity,0)))) >= 0)");
    }
    return whereClause.toString();
  }

  protected String getFromClauseHQL() {
    StringBuilder fromClause = new StringBuilder();
    if (isSalesTransaction) {
      fromClause.append(" InvoiceCandidateV ic");
      fromClause.append(" join ic.order o");
      fromClause.append(" join o.orderLineList as e");
    } else {
      fromClause.append(" OrderLine e");
      fromClause.append(" join e.salesOrder o");
    }
    fromClause.append(" join o.priceList pl");
    fromClause.append(" join e.uOM uom");
    fromClause.append(" join e.product p");
    fromClause.append(" join o.businessPartner bp");
    fromClause.append(" join o.documentType dt");
    fromClause.append(" join o.organization org");
    fromClause.append(" join o.warehouse wh");
    fromClause.append(" left join e.attributeSetValue ma");
    fromClause.append(" left join e.operativeUOM aum");
    fromClause.append(" left join e.bOMParent bomParent");
    fromClause.append(" left join e.goodsShipmentLine il");

    if (!isSalesTransaction) {
      fromClause
          .append(" left join e.procurementPOInvoiceMatchList m with m.invoiceLine.id is not null");

    }
    return fromClause.toString();
  }

  protected String getOrderQuantityHQL() {
    StringBuilder orderQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
      orderQuantityHql.append(" COALESCE(il.orderQuantity ");
      orderQuantityHql
          .append(" , e.orderQuantity * TO_NUMBER(C_DIVIDE((e.orderedQuantity - coalesce(e.invoicedQuantity,0)), e.orderedQuantity)))");
    } else {
      orderQuantityHql.append(" COALESCE(il.orderQuantity ");
      orderQuantityHql
          .append(" , e.orderQuantity * TO_NUMBER(C_DIVIDE((e.orderedQuantity - coalesce(e.invoicedQuantity,0)), e.orderedQuantity)))");
    }
    return orderQuantityHql.toString();
  }

  protected String getOperativeQuantityHQL() {
    StringBuilder operativeQuantityHql = new StringBuilder();
    operativeQuantityHql
        .append(" coalesce(e.operativeQuantity, to_number(M_GET_CONVERTED_AUMQTY(p.id, e.orderedQuantity, coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id))))))");
    if (isSalesTransaction) {
      operativeQuantityHql
          .append(" - SUM(COALESCE(to_number(M_GET_CONVERTED_AUMQTY(p.id, e.invoicedQuantity, coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id))))), 0))");
    } else {
      operativeQuantityHql
          .append("  -SUM(COALESCE(to_number(M_GET_CONVERTED_AUMQTY(p.id, m.quantity, aum.id)),0))-COALESCE(");
      operativeQuantityHql
          .append(" (SELECT SUM(COALESCE(to_number(M_GET_CONVERTED_AUMQTY(p.id, ci.invoicedQuantity, coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id))))), 0))");
      operativeQuantityHql.append("  FROM OrderLine co");
      operativeQuantityHql.append("    LEFT JOIN co.invoiceLineList ci");
      operativeQuantityHql.append("  WHERE ci.invoice.id = :invId");
      operativeQuantityHql.append("    AND co.id = e.id");
      operativeQuantityHql.append("  GROUP BY ci.salesOrderLine.id , co.orderedQuantity),0)");
    }
    return operativeQuantityHql.toString();
  }

  protected String getOrderedQuantityHQL() {
    StringBuilder orderedQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
      orderedQuantityHql.append(" e.orderedQuantity-COALESCE(e.invoicedQuantity,0)");
    } else {
      orderedQuantityHql.append(" e.orderedQuantity-SUM(COALESCE(m.quantity,0))-COALESCE(");
      orderedQuantityHql.append(" (SELECT SUM(COALESCE(ci.invoicedQuantity, 0))");
      orderedQuantityHql.append("  FROM OrderLine co");
      orderedQuantityHql.append("    LEFT JOIN co.invoiceLineList ci");
      orderedQuantityHql.append("  WHERE ci.invoice.id= :invId");
      orderedQuantityHql.append("    AND co.id = e.id");
      orderedQuantityHql.append("  GROUP BY ci.salesOrderLine.id , co.orderedQuantity),0)");
    }
    return orderedQuantityHql.toString();
  }
}