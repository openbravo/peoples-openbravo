/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
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
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

public class DiscountFilterProduct extends Discount {
  public static final String discFilterProductPropertyExtension = "PricingAdjustmentProduct";
  @Inject
  @Any
  @Qualifier(discFilterProductPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    final OBRETCOProductList productList = POSUtils.getProductListByPosterminalId(jsonsent
        .getString("pos"));
    HQLPropertyList regularDiscFilProductPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "select" + regularDiscFilProductPropertyExtensionHQLProperties.getHqlSelect();
    hql += " from PricingAdjustmentProduct ap, OBRETCO_Prol_Product ppl ";
    hql += " where ap.product.id = ppl.product.id and ppl.active = true ";
    hql += " and ppl.obretcoProductlist.id ='" + productList.getId() + "' ";
    hql += " and ((ap.$incrementalUpdateCriteria) " + jsonsent.get("operator")
        + " (ap.priceAdjustment.$incrementalUpdateCriteria) " + jsonsent.get("operator")
        + " (ap.product.$incrementalUpdateCriteria) " + jsonsent.get("operator")
        + " (ppl.$incrementalUpdateCriteria)) ";
    hql += " and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "              and ap.priceAdjustment = p) ";
    hql += "order by ap.id asc";

    return Arrays.asList(new String[] { hql });
  }
}
