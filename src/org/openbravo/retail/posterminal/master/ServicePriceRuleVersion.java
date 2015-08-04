/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ServicePriceRuleVersion extends ProcessHQLQuery {
  public static final String servicePriceRuleVersionPropertyExtension = "OBPOS_ServicePriceRuleVersionExtension";

  @Inject
  @Any
  @Qualifier(servicePriceRuleVersionPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties() {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularPriceRuleVersionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    propertiesList.add(regularPriceRuleVersionHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularPriceRuleVersionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries.add("select" + regularPriceRuleVersionHQLProperties.getHqlSelect()
        + "from ServicePriceRuleVersion sprv "
        + "where sprv.$naturalOrgCriteria and sprv.$incrementalUpdateCriteria ");

    return hqlQueries;
  }
}