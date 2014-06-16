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

package org.openbravo.common.datasource;

import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("CDB9DC9655F24DF8AB41AA0ADBD04390")
public class ReturnToFromCustomerVendorHQLTransformer extends HqlQueryTransformer {

  private static final String unitPriceLeftClause = "(case when (select e.salesOrderLine.salesOrder.priceList.priceIncludesTax from ProcurementPOInvoiceMatch as e where e.goodsShipmentLine = iol) = true then  coalesce((select ol.unitPrice from OrderLine as ol where ol.salesOrder.id = :salesOrderId and ol.goodsShipmentLine = iol), (select e.salesOrderLine.grossUnitPrice from ProcurementPOInvoiceMatch as e where e.goodsShipmentLine = iol)) else   coalesce((select ol.unitPrice from OrderLine as ol where ol.salesOrder.id = :salesOrderId and ol.goodsShipmentLine = iol), (select e.salesOrderLine.unitPrice from ProcurementPOInvoiceMatch as e where e.goodsShipmentLine = iol)) end)";
  private static final String orderNoLeftClause = " coalesce ((select e.salesOrderLine.salesOrder.documentNo from ProcurementPOInvoiceMatch as e where e.goodsShipmentLine = iol), '0')";
  private static final String returnedLeftClause = " coalesce((select ol.orderedQuantity from OrderLine as ol where ol.salesOrder.id = :salesOrderId and ol.goodsShipmentLine = iol),0)";
  private static final String returnedOthersLeftClause = " coalesce((select sum(ol.orderedQuantity) from OrderLine as ol left join ol.salesOrder as o where ol.goodsShipmentLine = iol and o.processed = true and o.documentStatus <> 'VO'), 0)";
  private static final String returnReasonLeftClause = " coalesce((select ol.returnReason from OrderLine as ol where ol.salesOrder.id = :salesOrderId and ol.goodsShipmentLine = iol), '')";
  private static final String returnReasonCountQuery = " select count(distinct e.name) from ReturnReason as e where exists (select distinct ol.returnReason from OrderLine as ol where ol.returnReason = e and ol.salesOrder.id = :salesOrderId) ";
  private static final String returnReasonDataQuery = " select distinct e.name from ReturnReason as e where exists (select distinct ol.returnReason from OrderLine as ol where ol.returnReason = e and ol.salesOrder.id = :salesOrderId) ";

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // Sets the named parameters
    String salesOrderId = requestParameters.get("@Order.id@");
    String businessPartnerId = requestParameters.get("@Order.businessPartner@");
    queryNamedParameters.put("salesOrderId", salesOrderId);
    queryNamedParameters.put("businessPartnerId", businessPartnerId);

    // uses the subqueries of the columns in the left clauses
    String transformedHqlQuery = hqlQuery.replace("@unitPriceLeftClause@", unitPriceLeftClause);
    transformedHqlQuery = transformedHqlQuery.replace("@orderNoLeftClause@", orderNoLeftClause);
    transformedHqlQuery = transformedHqlQuery.replace("@returnedLeftClause@", returnedLeftClause);
    transformedHqlQuery = transformedHqlQuery.replace("@returnedOthersLeftClause@",
        returnedOthersLeftClause);

    String distinctProperty = requestParameters.get("_distinct");
    if ("returnReason".equals(distinctProperty)) {
      // Uses custom queries for the return reason column
      String justCount = requestParameters.get("_justCount");
      if ("true".equals(justCount)) {
        transformedHqlQuery = returnReasonCountQuery;
      } else {
        transformedHqlQuery = returnReasonDataQuery;
      }
    } else {
      transformedHqlQuery = transformedHqlQuery.replace("@returnReasonLeftClause@.name",
          returnReasonLeftClause);
    }
    return transformedHqlQuery;
  }

}