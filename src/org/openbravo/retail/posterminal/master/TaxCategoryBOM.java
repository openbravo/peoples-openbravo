/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class TaxCategoryBOM extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    hqlQueries
        .add("select taxcategory.id as id "
            + "from FinancialMgmtTaxCategory taxcategory "
            + "where (taxcategory.$incrementalUpdateCriteria) AND ($naturalOrgCriteria) and $readableClientCriteria "
            + "AND taxcategory.obposAsbom=true AND taxcategory.active=true");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}