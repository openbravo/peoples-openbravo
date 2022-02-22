/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
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

@MasterDataModel("DiscountFilterAvailability")
public class DiscountFilterAvailability extends Discount {
  public static final String discFilterAvailabilityPropertyExtension = "PricingAdjustmentAvailability";

  @Inject
  @Any
  @Qualifier(discFilterAvailabilityPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(final JSONObject jsonsent) throws JSONException {
    final HQLPropertyList regularDiscountFilterAvailabilityPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    return Arrays.asList(getDiscountFilterAvailabilityHqlString(
        regularDiscountFilterAvailabilityPropertyExtensionHQLProperties, jsonsent));
  }

  private String getDiscountFilterAvailabilityHqlString(
      final HQLPropertyList regularDiscountFilterAvailabilityPropertyExtensionHQLProperties,
      final JSONObject jsonsent) throws JSONException {
    final String operator = jsonsent.getString("operator");

    // @formatter:off
    String query = " select " +
                     regularDiscountFilterAvailabilityPropertyExtensionHQLProperties.getHqlSelect() + 
                   " from PriceAdjustmentAvailability pav " +
                   " where (pav.$incrementalUpdateCriteria " +
                     operator + " pav.promotionDiscount.$incrementalUpdateCriteria ) " +
                   " and exists ( " +
                   "   select 1 " +
                       getPromotionsHQL(jsonsent, false) +
                   "   and pav.promotionDiscount.id = p.id " +
                   " )" +
                   " and pav.$paginationByIdCriteria " +
                   " order by pav.id";
    return query;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);

  }
}
