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
package org.openbravo.common.inserters;

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
    final String strInvoiceId = requestParameters.get("@Invoice.id@");

    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);
    if (!isSalesTransaction) {
      queryNamedParameters.put("invId", strInvoiceId);
    }

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

    if (isSalesTransaction) {

    } else {
      groupByClause.append("  sh.id,");
      groupByClause.append("  sh.documentNo,");
      groupByClause.append("  sh.movementDate,");
      groupByClause.append("  e.movementQuantity,");
      groupByClause.append("  uom.id,");
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
      groupByClause.append("  mi.id,");
      groupByClause.append("  ol.id,");
      groupByClause.append("  o.id,");
      groupByClause.append("  pl.id,");
      groupByClause.append("  ma.id");
      groupByClause
          .append(" HAVING ((e.movementQuantity-SUM(COALESCE(mi.quantity,0))) <> 0 OR (e.explode='Y'))");
    }
    return groupByClause.toString();
  }

  private String getWhereClauseHQL() {
    StringBuilder whereClause = new StringBuilder();
    if (isSalesTransaction) {

    } else {
      whereClause.append(" and sh.salesTransaction = :issotrx");
      whereClause.append(" and sh.documentStatus in ('CO', 'CL')");
      whereClause.append(" and sh.processed = 'Y'");
      whereClause.append(" and (pl.id is null or pl.priceIncludesTax = :plIncTax)");
      whereClause.append(" and sh.logistic <> 'Y'");
      whereClause.append(" and sh.businessPartner.id = :bp");
      whereClause.append(" and (o.id is null or o.currency.id = :cur)");
    }
    return whereClause.toString();
  }

  private String getFromClauseHQL() {
    StringBuilder fromClause = new StringBuilder();
    if (isSalesTransaction) {

    } else {
      fromClause.append(" MaterialMgmtShipmentInOutLine e");
      fromClause.append(" join e.shipmentReceipt sh");
      fromClause.append(" join sh.documentType dt");
      fromClause.append(" join e.uOM uom");
      fromClause.append(" join e.product p");
      fromClause.append(" left join e.attributeSetValue ma");
      fromClause.append(" left join e.procurementReceiptInvoiceMatchList mi");
      fromClause.append(" left join e.salesOrderLine ol");
      fromClause.append(" left join ol.salesOrder o");
      fromClause.append(" left join o.priceList pl");
      fromClause.append(" left join e.operativeUOM aum");
      fromClause.append(" left join e.bOMParent bomParent");
    }

    return fromClause.toString();
  }

  private String getOrderQuantityHQL() {
    StringBuilder orderQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
    } else {
      orderQuantityHql
          .append(" e.orderQuantity * TO_NUMBER(C_DIVIDE((e.movementQuantity - coalesce(SUM(mi.quantity),0)), e.movementQuantity))");
    }
    return orderQuantityHql.toString();
  }

  private String getOperativeQuantityHQL() {
    StringBuilder operativeQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
    } else {
      operativeQuantityHql
          .append(" e.operativeQuantity - SUM(COALESCE(to_number(M_GET_CONVERTED_AUMQTY(p.id, mi.quantity, aum.id)), 0))");
    }
    return operativeQuantityHql.toString();
  }

  private String getMovementQuantityHQL() {
    StringBuilder orderedQuantityHql = new StringBuilder();
    if (isSalesTransaction) {
    } else {
      orderedQuantityHql.append(" (e.movementQuantity-SUM(COALESCE(mi.quantity,0)))");
    }
    return orderedQuantityHql.toString();
  }
}