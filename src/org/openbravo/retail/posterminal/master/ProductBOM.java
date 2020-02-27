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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

@MasterDataModel("ProductBOM")
public class ProductBOM extends MasterDataProcessHQLQuery {

  public static final String ProductBOMPropertyExtension = "OBPOS_ProductBOMExtension";

  @Inject
  @Any
  @Qualifier(ProductBOMPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<>();
    HQLPropertyList productBOMProperties = ModelExtensionUtils.getPropertyExtensions(extensions);

    hqlQueries.add("select" + productBOMProperties.getHqlSelect() //
        + "from ProductBOM bom "
        + "where bom.$readableSimpleClientCriteria and bom.$naturalOrgCriteria and (bom.$incrementalUpdateCriteria) "
        + "order by bom.id asc");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return ModelExtensionUtils.getPropertyExtensions(extensions)
        .getProperties()
        .stream()
        .map(HQLProperty::getHqlProperty)
        .collect(Collectors.toList());
  }
}
