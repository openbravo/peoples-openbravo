/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.process.ProcessHQLQuery;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

public class ProductChValue extends ProcessHQLQuery {
  public static final String productChValuePropertyExtension = "OBPOS_ProductChValueExtension";

  @Inject
  @Any
  @Qualifier(productChValuePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsChValueHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries
        .add("select"
            + regularProductsChValueHQLProperties.getHqlSelect()
            + "from CharacteristicValue cv, ADTreeNode node "
            + "where cv.characteristic.tree =  node.tree and cv.id = node.node "
            + "and cv.$naturalOrgCriteria and cv.$readableClientCriteria and (cv.$incrementalUpdateCriteria)");

    return hqlQueries;
  }
}