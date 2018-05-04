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

@HQLInserterQualifier.Qualifier(tableId = "7EB9FFD7BD4E4113A13A096EB879D358", injectionId = "2")
public class OrderLinePEHQLInjector2 extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    boolean isSalesTransaction = StringUtils.equals(
        requestParameters.get("@Invoice.salesTransaction@"), "true");

    StringBuilder hql = new StringBuilder();
    hql.append(" TO_NUMBER(M_GET_CONVERTED_AUMQTY(p.id, e.orderedQuantity - e.invoicedQuantity ");
    if (!isSalesTransaction) {
      hql.append(" - (select coalesce(sum(m.quantity),0) from e.procurementPOInvoiceMatchList m where m.invoiceLine.id is not null) ");
    }
    hql.append(" , coalesce(aum.id, TO_CHAR(M_GET_DEFAULT_AUM_FOR_DOCUMENT(p.id, dt.id)))))");

    return hql.toString();
  }
}
