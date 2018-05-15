/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
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

public class ProductStatus extends QueryTerminalProperty {
  public static final String productStatusPropertyExtension = "OBPOS_ProductStatusExtension";

  @Inject
  @Any
  @Qualifier(productStatusPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList productStatusHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    return Arrays
        .asList(new String[] { "SELECT "
            + productStatusHQLProperties.getHqlSelect()
            + " FROM ProductStatus AS ps WHERE ps.$readableSimpleCriteria AND ps.$activeCriteria AND $naturalOrgCriteria" });
  }

  @Override
  public String getProperty() {
    return "productStatusList";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
