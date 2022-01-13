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
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularDiscountFilterAvailabilityPropertyExtensionHQLProperties.getHqlSelect());
    query.append(" from PriceAdjustmentAvailability pav");
    query.append(" where (pav.$incrementalUpdateCriteria");
    query.append(" " + operator + " pav.promotionDiscount.$incrementalUpdateCriteria )");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   " + getPromotionsHQL(jsonsent, false));
    query.append("   and pav.promotionDiscount.id = p.id");
    query.append(" )");
    query.append(" and pav.$paginationByIdCriteria");
    query.append(" order by pav.id");
    return query.toString();
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);

  }
}
