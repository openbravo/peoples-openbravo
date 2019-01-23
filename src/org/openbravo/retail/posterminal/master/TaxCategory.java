/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class TaxCategory extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select taxcategory.id as id, ");
    queryBuilder.append("  taxcategory.name as name ");
    queryBuilder.append("from FinancialMgmtTaxCategory as taxcategory ");
    queryBuilder.append("where (taxcategory.$incrementalUpdateCriteria) ");
    queryBuilder.append("  AND ($naturalOrgCriteria) and $readableClientCriteria ");
    queryBuilder.append("  AND taxcategory.asbom=false ");
    queryBuilder.append("  AND taxcategory.active=true ");
    queryBuilder.append("order by taxcategory.id asc");

    return Arrays.asList(queryBuilder.toString());
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
