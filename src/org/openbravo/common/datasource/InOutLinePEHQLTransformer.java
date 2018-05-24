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

@ComponentProvider.Qualifier("631D227DC83A4898BBD041D46D829D27")
public class InOutLinePEHQLTransformer extends HqlQueryTransformer {
  private boolean isSalesTransaction;

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    isSalesTransaction = StringUtils.equals(requestParameters.get("@Invoice.salesTransaction@"),
        "true");
    final String strInvoicePriceListId = requestParameters.get("@Invoice.priceList@");
    final PriceList priceList = OBDal.getInstance().get(PriceList.class, strInvoicePriceListId);
    final String strBusinessPartnerId = requestParameters.get("@Invoice.businessPartner@");
    final String strCurrencyId = requestParameters.get("@Invoice.currency@");

    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);

    String transformedHql = _hqlQuery.replace("@fromClause@", getFromClauseHQL());
    transformedHql = transformedHql.replace("@whereClause@", getWhereClauseHQL());
    transformedHql = transformedHql.replace("@groupByClause@", getGroupByHQL());
    transformedHql = transformedHql.replace("@movementQuantity@", getMovementQuantityHQL());
    transformedHql = transformedHql.replace("@operativeQuantity@", getOperativeQuantityHQL());
    transformedHql = transformedHql.replace("@orderQuantity@", getOrderQuantityHQL());
    return transformedHql;
  }

  private String getGroupByHQL() {
    StringBuilder groupByClause = new StringBuilder();
    groupByClause.append("  sh.id,");
    groupByClause.append("  sh.documentNo,");
    groupByClause.append("  sh.movementDate,");
    groupByClause.append("  e.movementQuantity,");
    groupByClause.append("  uom.id,");
    groupByClause.append("  uom.name,");
    groupByClause.append("  uom.symbol,");
    groupByClause.append("  p.id,");
    groupByClause.append("  p.name,");
    groupByClause.append("  e.id,");
    groupByClause.append("  e.lineNo,");
    groupByClause.append("  ol.id,");
    groupByClause.append("  COALESCE(e.asset.id, sh.asset.id),");
    groupByClause.append("  COALESCE(e.project.id, sh.project.id),");
    groupByClause.append("  COALESCE(e.costcenter.id, sh.costcenter.id),");
    groupByClause.append("  COALESCE(e.stDimension.id, sh.stDimension.id),");
    groupByClause.append("  COALESCE(e.ndDimension.id, sh.ndDimension.id),");
    groupByClause.append("  e.explode,");
    groupByClause.append("  bomParent.id,");
    groupByClause.append("  aum.id,");
    groupByClause.append("  e.operativeQuantity,");
    groupByClause.append("  dt.id,");
    groupByClause.append("  ol.id,");
    groupByClause.append("  o.id,");
    groupByClause.append("  pl.id,");
    groupByClause.append("  ma.id,");
    groupByClause.append("  ma.serialNo,");
    groupByClause.append("  @orderQuantity@,");
    if (isSalesTransaction) {
      groupByClause.append("  il.id,");
      groupByClause.append("  i.id");
      groupByClause
          .append(" HAVING (e.movementQuantity >= 0 AND e.movementQuantity > SUM(COALESCE(CASE WHEN i.documentStatus = 'CO' THEN il.invoicedQuantity ELSE 0 END, 0)))");
      groupByClause
          .append("  OR (e.movementQuantity < 0 AND e.movementQuantity < SUM(COALESCE(CASE WHEN i.documentStatus = 'CO' THEN il.invoicedQuantity ELSE 0 END, 0)))");
      groupByClause.append("  OR (e.explode='Y')");
    } else {
      groupByClause.append("  mi.id");
      groupByClause
          .append(" HAVING ((e.movementQuantity-SUM(COALESCE(mi.quantity,0))) <> 0 OR (e.explode='Y'))");
    }
    return groupByClause.toString();
  }

  private String getWhereClauseHQL() {
    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" and sh.salesTransaction = :issotrx");
    whereClause.append(" and sh.documentStatus in ('CO', 'CL')");
    whereClause.append(" and sh.processed = 'Y'");
    whereClause.append(" and sh.logistic <> 'Y'");
    whereClause.append(" and sh.businessPartner.id = :bp");
    whereClause.append(" and (ol.id is null or pl.priceIncludesTax = :plIncTax)");
    whereClause.append(" and (o.id is null or o.currency.id = :cur)");
    if (isSalesTransaction) {
      whereClause.append(" and sh.completelyInvoiced = 'N'");
      whereClause.append(" and NOT EXISTS");
      whereClause.append(" (SELECT 1");
      whereClause.append(" FROM Order o2");
      whereClause.append(" WHERE o2.id = o.id");
      whereClause
          .append(" AND ((o2.invoiceTerms = 'O' and o2.delivered = 'N') or o2.invoiceTerms = 'N'))");
    } else {
    }
    return whereClause.toString();
  }

  private String getFromClauseHQL() {
    StringBuilder fromClause = new StringBuilder();
    fromClause.append(" MaterialMgmtShipmentInOutLine e");
    fromClause.append(" join e.shipmentReceipt sh");
    fromClause.append(" join sh.documentType dt");
    fromClause.append(" join e.uOM uom");
    fromClause.append(" join e.product p");
    fromClause.append(" left join e.attributeSetValue ma");
    fromClause.append(" left join e.salesOrderLine ol");
    fromClause.append(" left join ol.salesOrder o");
    fromClause.append(" left join o.priceList pl");
    fromClause.append(" left join e.operativeUOM aum");
    fromClause.append(" left join e.bOMParent bomParent");
    if (isSalesTransaction) {
      fromClause.append(" left join e.invoiceLineList il");
      fromClause.append(" left join il.invoice i");
    } else {
      fromClause.append(" left join e.procurementReceiptInvoiceMatchList mi");
    }

    return fromClause.toString();
  }

  private String getOrderQuantityHQL() {
    StringBuilder orderQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
      orderQuantityHql
          .append(" e.orderQuantity - coalesce((case when i.documentStatus = 'CO' then il.orderQuantity else 0 end),0)");

    } else {
      orderQuantityHql
          .append(" e.orderQuantity * TO_NUMBER(C_DIVIDE((e.movementQuantity - coalesce(mi.quantity,0)), e.movementQuantity))");
    }
    return orderQuantityHql.toString();
  }

  private String getOperativeQuantityHQL() {
    StringBuilder operativeQuantityHql = new StringBuilder();
    operativeQuantityHql
        .append(" coalesce(e.operativeQuantity, to_number(M_GET_CONVERTED_AUMQTY(p.id, e.movementQuantity, coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id))))))");
    if (isSalesTransaction) {
      operativeQuantityHql
          .append(" - SUM(COALESCE(CASE WHEN i.documentStatus = 'CO' THEN to_number(M_GET_CONVERTED_AUMQTY(p.id, il.invoicedQuantity, coalesce(aum.id, uom.id))) ELSE 0 END, 0))");
    } else {
      operativeQuantityHql
          .append(" - SUM(COALESCE(to_number(M_GET_CONVERTED_AUMQTY(p.id, mi.quantity, coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id))))), 0))");
    }
    return operativeQuantityHql.toString();
  }

  private String getMovementQuantityHQL() {
    StringBuilder movementQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
      movementQuantityHql
          .append(" (e.movementQuantity - sum(COALESCE(CASE WHEN i.documentStatus = 'CO' THEN il.invoicedQuantity ELSE 0 END, 0)))");
    } else {
      movementQuantityHql.append(" (e.movementQuantity-SUM(COALESCE(mi.quantity,0)))");
    }
    return movementQuantityHql.toString();
  }
}