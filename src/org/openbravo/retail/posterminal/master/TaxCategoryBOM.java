/*
 ************************************************************************************
 * Copyright (C) 2014-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;

@MasterDataModel("TaxCategoryBOM")
public class TaxCategoryBOM extends MasterDataProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<>();

    hqlQueries.add("select taxcategory.id as id " + "from FinancialMgmtTaxCategory taxcategory "
        + "where (taxcategory.$incrementalUpdateCriteria) AND ($naturalOrgCriteria) and $readableClientCriteria "
        + "AND taxcategory.asbom=true AND taxcategory.active=true order by taxcategory.id asc");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return ModelProvider.getInstance()
        .getEntity(org.openbravo.model.financialmgmt.tax.TaxCategory.class)
        .getProperties()
        .stream()
        .map(Property::getName)
        .collect(Collectors.toList());
  }
}
