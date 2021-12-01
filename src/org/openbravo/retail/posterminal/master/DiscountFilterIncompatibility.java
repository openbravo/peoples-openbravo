/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
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

@MasterDataModel("DiscountFilterIncompatibility")
public class DiscountFilterIncompatibility extends Discount {
  public static final String discFilterIncompatibilityPropertyExtension = "PricingAdjustmentIncompatibility";

  @Inject
  @Any
  @Qualifier(discFilterIncompatibilityPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(final JSONObject jsonsent) throws JSONException {
    final HQLPropertyList regularDiscountFilterIncompatibilityPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    return Arrays.asList(getDiscountFilterIncompatibilityHqlString(
        regularDiscountFilterIncompatibilityPropertyExtensionHQLProperties, jsonsent));
  }

  private String getDiscountFilterIncompatibilityHqlString(
      final HQLPropertyList regularDiscountFilterIncompatibilityPropertyExtensionHQLProperties,
      final JSONObject jsonsent) throws JSONException {
    final String operator = jsonsent.getString("operator");
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularDiscountFilterIncompatibilityPropertyExtensionHQLProperties.getHqlSelect());
    query.append(" from PriceAdjustmentIncompatibility pai");
    query.append(" where (pai.$incrementalUpdateCriteria");
    query.append(" " + operator + " pai.promotionDiscount.$incrementalUpdateCriteria )");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   " + getPromotionsHQL(jsonsent, false));
    query.append("   and pai.promotionDiscount.id = p.id");
    query.append(" )");
    query.append(" and pai.$paginationByIdCriteria");
    query.append(" order by pai.id");
    return query.toString();
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);

  }
}
