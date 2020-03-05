/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
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

@MasterDataModel("DiscountFilterBusinessPartnerGroup")
public class DiscountFilterBusinessPartnerGroup extends Discount {
  public static final String discFilterBPGPropertyExtension = "PricingAdjustmentBusinessPartnerGroup";
  @Inject
  @Any
  @Qualifier(discFilterBPGPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularDiscFilBPGPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "select" + regularDiscFilBPGPropertyExtensionHQLProperties.getHqlSelect()
        + "from PricingAdjustmentBusinessPartnerGroup bpg where ((bpg.$incrementalUpdateCriteria) "
        + jsonsent.get("operator") + " (bpg.priceAdjustment.$incrementalUpdateCriteria)) ";
    hql += " and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "              and bpg.priceAdjustment = p) ";
    hql += "order by bpg.priceAdjustment.id asc";

    return Arrays.asList(hql);
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);

  }
}
