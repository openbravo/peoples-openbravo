/*
 ************************************************************************************
 * Copyright (C) 2016-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

@MasterDataModel("Country")
public class Country extends MasterDataProcessHQLQuery {
  public static final String countryPropertyExtension = "OBPOS_CountryExtension";

  @Inject
  @Any
  @Qualifier(countryPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularCountryHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String hql = "select" + regularCountryHQLProperties.getHqlSelect()
        + "from Country c where (c.$incrementalUpdateCriteria) "
        + "and c.$readableSimpleClientCriteria and c.$activeCriteria order by c.name asc";

    return Arrays.asList(new String[] { hql });
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    final Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
    return paramValues;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }

}
