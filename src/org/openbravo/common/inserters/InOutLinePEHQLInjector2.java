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

@HQLInserterQualifier.Qualifier(tableId = "631D227DC83A4898BBD041D46D829D27", injectionId = "2")
public class InOutLinePEHQLInjector2 extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    boolean isSalesTransaction = StringUtils.equals(
        requestParameters.get("@Invoice.salesTransaction@"), "true");

    StringBuilder hql = new StringBuilder();

    if (isSalesTransaction) {
      hql.append("coalesce(e.operativeQuantity, M_GET_CONVERTED_AUMQTY(p.id, e.movementQuantity, aum_defaum.id)) ");
      hql.append("- sum(coalesce(case when (i.documentStatus <> 'CO') then 0 ");
      hql.append(" else M_GET_CONVERTED_AUMQTY(p.id, il.invoicedQuantity, aum_defaum.id) end, 0))");
    } else {
      hql.append("coalesce(e.operativeQuantity, M_GET_CONVERTED_AUMQTY(p.id, e.movementQuantity, aum_defaum.id)) ");
      hql.append("- M_GET_CONVERTED_AUMQTY(p.id, sum(coalesce(mi.quantity,0)), aum_defaum.id)");
    }

    return hql.toString();
  }
}
