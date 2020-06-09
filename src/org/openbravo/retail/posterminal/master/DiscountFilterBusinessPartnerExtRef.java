/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

@MasterDataModel("DiscountFilterBusinessPartnerExtRef")
public class DiscountFilterBusinessPartnerExtRef extends Discount {
  public static final String DiscFilterBPExtRefPropertyExtension = "PricingAdjustmentBusinessPartnerExtRef";
  @Inject
  @Any
  @Qualifier(DiscFilterBPExtRefPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList properties = ModelExtensionUtils.getPropertyExtensions(extensions);
    return Arrays.asList( //
        "SELECT " + properties.getHqlSelect() //
            + "FROM PricingAdjustmentBusinessPartnerExtRef bpextref " //
            + "WHERE ((bpextref.$incrementalUpdateCriteria) " + jsonsent.get("operator")
            + " (bpextref.promotionDiscount.$incrementalUpdateCriteria)) " //
            + "AND EXISTS (SELECT 1 " + getPromotionsHQL(jsonsent, false) + " " //
            + "AND bpextref.promotionDiscount = p) " //
            + "ORDER BY bpextref.promotionDiscount.id ASC");
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }
}
