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
package org.openbravo.common.inserters;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.util.Check;
import org.openbravo.model.materialmgmt.cost.LandedCostType;
import org.openbravo.service.datasource.hql.HQLInserterQualifier;
import org.openbravo.service.datasource.hql.HqlInserter;

@HQLInserterQualifier.Qualifier(tableId = "B2960E2BDCCD4F7599A2433F2681847F", injectionId = "0")
public class LCMatchFromInvoiceInserter extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // The query fails with a NPE when using the queryNamedParameters
    final String strInvoiceLineID = requestParameters.get("@InvoiceLine.id@");
    Check.isTrue(IsIDFilter.instance.accept(strInvoiceLineID), "Value " + strInvoiceLineID
        + " is not a valid id.");
    String strWhereClause = " (il is null or il.id = '" + strInvoiceLineID + "') ";

    final String strProductId = requestParameters.get("@InvoiceLine.product@");
    if (StringUtils.isNotEmpty(strProductId)) {
      Check.isTrue(IsIDFilter.instance.accept(strProductId), "Value " + strProductId
          + " is not a valid id.");
      strWhereClause += " and lct." + LandedCostType.PROPERTY_PRODUCT + ".id = '" + strProductId
          + "' ";
      // strWhereClause = "lct." + LandedCostType.PROPERTY_PRODUCT + ".id = :typeproduct ";
      // queryNamedParameters.put("typeproduct", strProductId);
    }

    final String strGLItemId = requestParameters.get("@InvoiceLine.account@");
    if (StringUtils.isNotEmpty(strGLItemId)) {
      Check.isTrue(IsIDFilter.instance.accept(strGLItemId), "Value " + strGLItemId
          + " is not a valid id.");
      strWhereClause += " and lct." + LandedCostType.PROPERTY_ACCOUNT + ".id = '" + strGLItemId
          + "' ";
      // strWhereClause += " and lct." + LandedCostType.PROPERTY_ACCOUNT + ".id = :typeglitem ";
      // queryNamedParameters.put("typeglitem", strGLItemId);
    }
    return strWhereClause;
  }

}
