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

public class BPTaxCategory extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select bpTaxCategory.id as id, ");
    queryBuilder.append("  bpTaxCategory.name as name ");
    queryBuilder.append("from BusinessPartnerTaxCategory as bpTaxCategory ");
    queryBuilder.append("where (bpTaxCategory.$incrementalUpdateCriteria) ");
    queryBuilder.append("  AND ($naturalOrgCriteria) and $readableClientCriteria ");
    queryBuilder.append("  AND bpTaxCategory.active=true ");
    queryBuilder.append("order by bpTaxCategory.id asc");

    return Arrays.asList(queryBuilder.toString());
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}