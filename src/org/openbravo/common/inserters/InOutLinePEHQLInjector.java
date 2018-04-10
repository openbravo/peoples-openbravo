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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.common.inserters;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.datasource.hql.HQLInserterQualifier;
import org.openbravo.service.datasource.hql.HqlInserter;

@HQLInserterQualifier.Qualifier(tableId = "631D227DC83A4898BBD041D46D829D27", injectionId = "0")
public class InOutLinePEHQLInjector extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    boolean isSalesTransaction = StringUtils.equals(
        requestParameters.get("@Invoice.salesTransaction@"), "true");
    final String strInvoicePriceListId = requestParameters.get("@Invoice.priceList@");
    final PriceList priceList = OBDal.getInstance().get(PriceList.class, strInvoicePriceListId);
    final String strBusinessPartnerId = requestParameters.get("@Invoice.businessPartner@");
    final String strCurrencyId = requestParameters.get("@Invoice.currency@");

    StringBuilder hql = new StringBuilder();
    hql.append(" and s.salesTransaction = :issotrx");
    hql.append(" and s.logistic <> 'Y'");
    hql.append(" and s.documentStatus in ('CO', 'CL')");
    hql.append(" and s.businessPartner.id = :bp");

    if (isSalesTransaction) {
      hql.append(" and s.completelyInvoiced ='N'");
      hql.append(" and exists(");
      hql.append("   select 1 ");
      hql.append("   from MaterialMgmtShipmentInOutLine l");
      hql.append("     left join l.salesOrderLine ol");
      hql.append("     left join ol.salesOrder o");
      hql.append("     left join o.priceList pl");
      hql.append("     left join l.invoiceLineList il");
      hql.append("     left join il.invoice i");
      hql.append("   where");
      hql.append("     l.shipmentReceipt.id = s.id");
      hql.append("     and (l.id is null or pl.id is null or pl.priceIncludesTax = :plIncTax)");
      hql.append("     and (l.id is null or o.id is null or o.currency.id = :cur)");
      hql.append("   group by l.id, l.movementQuantity");
      hql.append("   having (");
      hql.append("     (l.movementQuantity >= 0 and l.movementQuantity > sum(coalesce");
      hql.append("     (case when (i.documentStatus = 'CO') then il.invoicedQuantity else 0 end,0)))");
      hql.append("     or (l.movementQuantity < 0 and l.movementQuantity < sum(coalesce");
      hql.append("     (case when (i.documentStatus = 'CO') then il.invoicedQuantity else 0 end,0)))");
      hql.append("   )");
      hql.append(" )");
      hql.append(" and not exists(select 1 from Order so where so.id = s.salesOrder.id");
      hql.append(" and ((so.invoiceTerms = 'O' and so.delivered = 'N') or so.invoiceTerms = 'N'))");
    } else {
      hql.append(" and exists(");
      hql.append("   select 1 ");
      hql.append("   from MaterialMgmtShipmentInOutLine l");
      hql.append("     left join l.procurementReceiptInvoiceMatchList mi");
      hql.append("     left join l.salesOrderLine ol");
      hql.append("     left join ol.salesOrder o");
      hql.append("     left join o.priceList pl");
      hql.append("   where");
      hql.append("     l.shipmentReceipt.id = s.id");
      hql.append("     and (l.id is null or pl.id is null or pl.priceIncludesTax = :plIncTax)");
      hql.append("     and (l.id is null or o.id is null or o.currency.id = :cur)");
      hql.append("   group by l.id, l.movementQuantity");
      hql.append("   having (l.movementQuantity - sum(coalesce(mi.quantity,0)) <> 0)");
      hql.append(" )");
    }
    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);

    return hql.toString();
  }
}
