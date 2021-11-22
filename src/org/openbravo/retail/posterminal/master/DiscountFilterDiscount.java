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

@MasterDataModel("DiscountFilterDiscount")
public class DiscountFilterDiscount extends Discount {
  public static final String discFilterDiscountPropertyExtension = "PricingAdjustmentDiscount";

  @Inject
  @Any
  @Qualifier(discFilterDiscountPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(final JSONObject jsonsent) throws JSONException {
    final HQLPropertyList regularDiscFilDiscountPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    return Arrays.asList(getDiscountFilterDiscountHqlString(
        regularDiscFilDiscountPropertyExtensionHQLProperties, jsonsent));
  }

  private String getDiscountFilterDiscountHqlString(
      final HQLPropertyList regularDiscFilDiscountPropertyExtensionHQLProperties,
      final JSONObject jsonsent) throws JSONException {
    final String operator = jsonsent.getString("operator");
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularDiscFilDiscountPropertyExtensionHQLProperties.getHqlSelect());
    query.append(" from PriceAdjustmentDiscount apd");
    query.append(" where (apd.$incrementalUpdateCriteria");
    query.append(" " + operator + " apd.promotionDiscount.$incrementalUpdateCriteria )");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   " + getPromotionsHQL(jsonsent, false));
    query.append("   and apd.promotionDiscount.id = p.id");
    query.append(" )");
    query.append(" and apd.$paginationByIdCriteria");
    query.append(" order by apd.id");
    return query.toString();
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);

  }
}
