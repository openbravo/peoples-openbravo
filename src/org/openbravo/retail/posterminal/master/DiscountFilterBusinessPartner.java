/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
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

@MasterDataModel("DiscountFilterBusinessPartner")
public class DiscountFilterBusinessPartner extends Discount {
  public static final String discFilterBPPropertyExtension = "PricingAdjustmentBusinessPartner";

  @Inject
  @Any
  @Qualifier(discFilterBPPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<>();
    HQLPropertyList regularDiscountFilterBPHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularDiscountFilterBPHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularDiscFilBPPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "select" + regularDiscFilBPPropertyExtensionHQLProperties.getHqlSelect()
        + "from PricingAdjustmentBusinessPartner bp where  $filtersCriteria AND ((bp.$incrementalUpdateCriteria) "
        + jsonsent.get("operator") + " (bp.priceAdjustment.$incrementalUpdateCriteria)) ";

    hql += " and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "              and bp.priceAdjustment = p) ";
    hql += "and bp.$paginationByIdCriteria ";
    hql += "order by bp.priceAdjustment.id asc";

    return Arrays.asList(hql);
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }
}
