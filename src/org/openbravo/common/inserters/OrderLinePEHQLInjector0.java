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

@HQLInserterQualifier.Qualifier(tableId = "7EB9FFD7BD4E4113A13A096EB879D358", injectionId = "0")
public class OrderLinePEHQLInjector0 extends HqlInserter {

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
    hql.append(" and o.salesTransaction = :issotrx");
    hql.append(" and bp.id = :bp");
    hql.append(" and pl.priceIncludesTax = :plIncTax");
    hql.append(" and o.currency.id = :cur");

    if (isSalesTransaction) {
      hql.append(" and o.documentStatus in ('CO', 'CL', 'IP')");
      hql.append(" and dt.documentCategory = 'SOO'");
      hql.append(" and dt.sOSubType not in ('ON','OB', 'WR')");
      hql.append(" and (");
      hql.append("   o.invoiceTerms in ('I', 'O', 'D', 'S') ");
      hql.append("   and si.invoiceFrequency is null or si.invoiceFrequency in ('D', 'W', 'T')");
      hql.append("   and o.orderDate <= (trunc(now(),'MM') + si.invoiceCutOffDay - 1)");
      hql.append("   and trunc(now()) >= (trunc(o.orderDate,'MM') + si.dayOfTheMonth - 1)");
      hql.append("   or o.orderDate <= (trunc(now(),'MM') + si.invoiceCutOffDay + 14)");
      hql.append("   and trunc(now()) >= (trunc(o.orderDate,'MM') + si.dayOfTheMonth + 14)");
      hql.append("   or si.invoiceFrequency = 'M'");
      hql.append("   and o.orderDate <= (trunc(now(),'MM') + si.invoiceCutOffDay - 1)");
      hql.append("   and trunc(now()) >= (trunc(o.orderDate,'MM') + si.dayOfTheMonth - 1)");
      hql.append(" )");
      hql.append(" and (e.orderedQuantity <> e.invoicedQuantity or e.deliveredQuantity <> e.invoicedQuantity)");
      hql.append(" and (");
      hql.append("   o.invoiceTerms in ('D', 'S') and e.deliveredQuantity <> 0");
      hql.append("   or (o.invoiceTerms = 'I' and e.orderedQuantity <> e.invoicedQuantity)");
      hql.append("   or (o.invoiceTerms = 'O' and e.orderedQuantity = e.deliveredQuantity)");
      hql.append(" )");
    } else {
      hql.append(" and o.documentStatus in ('CO', 'CL')");
      hql.append(" and o.invoiceTerms <> 'N'");
      hql.append(" and ((e.explode = 'Y') or ((e.orderedQuantity - (select coalesce(sum(mp.quantity),0) from e.procurementPOInvoiceMatchList mp where mp.invoiceLine.id is not null)) <> 0)) ");
    }

    queryNamedParameters.put("issotrx", isSalesTransaction);
    queryNamedParameters.put("bp", strBusinessPartnerId);
    queryNamedParameters.put("plIncTax", priceList.isPriceIncludesTax());
    queryNamedParameters.put("cur", strCurrencyId);

    return hql.toString();
  }
}
