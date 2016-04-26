/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

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

public class DiscountFilterProduct extends Discount {
  public static final String discFilterProductPropertyExtension = "PricingAdjustmentProduct";
  @Inject
  @Any
  @Qualifier(discFilterProductPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularDiscFilProductPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "select" + regularDiscFilProductPropertyExtensionHQLProperties.getHqlSelect()
        + "from PricingAdjustmentProduct ap where ((ap.$incrementalUpdateCriteria) "
        + jsonsent.get("operator") + " (ap.priceAdjustment.$incrementalUpdateCriteria))";

    hql += " and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "              and ap.priceAdjustment = p)";

    return Arrays.asList(new String[] { hql });
  }
}
