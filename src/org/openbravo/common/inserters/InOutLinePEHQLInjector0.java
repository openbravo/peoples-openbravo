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
public class InOutLinePEHQLInjector0 extends HqlInserter {

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
    hql.append(" and sh.salesTransaction = :issotrx");
    hql.append(" and sh.logistic <> 'Y'");
    hql.append(" and sh.processed = 'Y'");
    hql.append(" and sh.documentStatus in ('CO', 'CL')");
    hql.append(" and sh.businessPartner.id = :bp");
    hql.append(" and (pl.id is null or pl.priceIncludesTax = :plIncTax)");
    hql.append(" and (o.id is null or o.currency.id = :cur)");

    if (isSalesTransaction) {
      hql.append(" and sh.completelyInvoiced = 'N'");
      hql.append(" and (o.id is null or not ((o.invoiceTerms = 'O' and o.delivered = 'N') or o.invoiceTerms = 'N'))");
      hql.append(" and (");
      hql.append("   (e.movementQuantity >= 0 and e.movementQuantity > ");
      hql.append("    (select coalesce(sum(il.invoicedQuantity),0) FROM InvoiceLine il WHERE il.invoice.id = i.id and i.documentStatus = 'CO' and il.goodsShipmentLine.id = e.id)");
      hql.append("   ) or (e.movementQuantity < 0 and e.movementQuantity < ");
      hql.append("    (select coalesce(sum(il.invoicedQuantity),0) FROM InvoiceLine il WHERE il.invoice.id = i.id and i.documentStatus = 'CO' and il.goodsShipmentLine.id = e.id)");
      hql.append("   ) or e.explode = 'Y')");
    } else {
      hql.append(" and ((e.movementQuantity - (select coalesce(sum(mi.quantity),0) from e.procurementReceiptInvoiceMatchList mi) <> 0) or e.explode = 'Y')");
    }

    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);

    return hql.toString();
  }
}
