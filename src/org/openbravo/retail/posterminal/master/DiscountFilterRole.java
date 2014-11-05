/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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

public class DiscountFilterRole extends Discount {
  public static final String discFilterRolePropertyExtension = "OBDISC_Offer_Role";
  @Inject
  @Any
  @Qualifier(discFilterRolePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularDiscFilRolePropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String hql = "select" + regularDiscFilRolePropertyExtensionHQLProperties.getHqlSelect()
        + "from OBDISC_Offer_Role r where ((r.$incrementalUpdateCriteria) "
        + jsonsent.get("operator") + " (r.priceAdjustment.$incrementalUpdateCriteria)) ";

    hql += " and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "              and r.priceAdjustment = p)";

    return Arrays.asList(new String[] { hql });
  }
}
