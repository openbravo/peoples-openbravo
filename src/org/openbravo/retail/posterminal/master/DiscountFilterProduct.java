/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

public class DiscountFilterProduct extends Discount {
  public static final String discFilterProductPropertyExtension = "PricingAdjustmentProduct";
  @Inject
  @Any
  @Qualifier(discFilterProductPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    final String posId = jsonsent.getString("pos");
    final OBPOSApplications pos = POSUtils.getTerminalById(posId);
    final boolean isCrossStoreEnabled = POSUtils.isCrossStoreEnabled(pos);
    final Set<String> productListIds = POSUtils.getProductListCrossStore(posId,
        isCrossStoreEnabled);

    final Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("productListIds", productListIds);
    return paramValues;
  }

  @Override
  protected List<String> prepareQuery(final JSONObject jsonsent) throws JSONException {
    final HQLPropertyList regularDiscFilProductPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    return Arrays.asList(getDiscountFilterProductHqlString(
        regularDiscFilProductPropertyExtensionHQLProperties, jsonsent));
  }

  private String getDiscountFilterProductHqlString(
      final HQLPropertyList regularDiscFilProductPropertyExtensionHQLProperties,
      final JSONObject jsonsent) throws JSONException {
    final String operator = jsonsent.getString("operator");
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularDiscFilProductPropertyExtensionHQLProperties.getHqlSelect());
    query.append(" from PricingAdjustmentProduct ap");
    query.append(" where (ap.$incrementalUpdateCriteria");
    query.append(" " + operator + " ap.priceAdjustment.$incrementalUpdateCriteria");
    query.append(" " + operator + " ap.product.$incrementalUpdateCriteria)");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from OBRETCO_Prol_Product ppl");
    query.append("   where ppl.product.id = ap.product.id");
    query.append("   and ppl.obretcoProductlist.id in :productListIds");
    query.append(" )");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   " + getPromotionsHQL(jsonsent, false));
    query.append("   and ap.priceAdjustment.id = p.id");
    query.append(" )");
    query.append(" order by ap.id");
    return query.toString();
  }
}
