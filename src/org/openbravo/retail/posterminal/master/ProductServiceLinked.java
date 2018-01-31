/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Collections;
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

public class ProductServiceLinked extends ProcessHQLQuery {

  @Inject
  @Any
  @Qualifier("OBPOS_ProductServiceLinkedExtension")
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    return Collections.singletonList(ModelExtensionUtils.getPropertyExtensions(extensions));
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Collections.singletonList( //
        "select" //
            + ModelExtensionUtils.getPropertyExtensions(extensions).getHqlSelect() //
            + "from M_PRODUCT_SERVICELINKED psl " //
            + "where psl.$filtersCriteria and psl.$naturalOrgCriteria and psl.$incrementalUpdateCriteria");
  }
}