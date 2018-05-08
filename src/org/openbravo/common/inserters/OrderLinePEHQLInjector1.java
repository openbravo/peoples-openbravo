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
import org.openbravo.service.datasource.hql.HQLInserterQualifier;
import org.openbravo.service.datasource.hql.HqlInserter;

@HQLInserterQualifier.Qualifier(tableId = "7EB9FFD7BD4E4113A13A096EB879D358", injectionId = "1")
public class OrderLinePEHQLInjector1 extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    boolean isSalesTransaction = StringUtils.equals(
        requestParameters.get("@Invoice.salesTransaction@"), "true");

    StringBuilder hql = new StringBuilder();

    if (isSalesTransaction) {
      hql.append(" COALESCE((select sum(il.movementQuantity) from e.materialMgmtShipmentInOutLineList il join il.shipmentReceipt io where il.reinvoice = 'N' and io.processed = 'Y') ");
      hql.append(" , (e.orderedQuantity - e.invoicedQuantity))");
    } else {
      hql.append(" COALESCE((select il.movementQuantity - sum(mi.quantity) from e.materialMgmtShipmentInOutLineList il join il.shipmentReceipt io join il.procurementReceiptInvoiceMatchList mi where io.processed = 'Y' and io.documentStatus <> 'VO' group by il.movementQuantity) ");
      hql.append(" , (e.orderedQuantity - (select coalesce(sum(mp.quantity),0) from e.procurementPOInvoiceMatchList mp where mp.invoiceLine.id is not null) - (select coalesce(sum(ci.invoicedQuantity),0) from e.procurementPOInvoiceMatchList mp join mp.invoiceLine ci)) ");
      hql.append(" )");
    }

    return hql.toString();
  }
}
