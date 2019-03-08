/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
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
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class DiscountBusinessPartnerSet extends ProcessHQLQuery {
  public static final String discountBPPropertyExtension = "OBPOS_DiscountBusinessPartnerSetExtension";

  @Inject
  @Any
  @Qualifier(discountBPPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularCountryHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String hql = "select" + regularCountryHQLProperties.getHqlSelect()
        + "from PricingAdjustmentBusinessPartnerSet c where c.$incrementalUpdateCriteria "
        + "and c.$naturalOrgCriteria "
        + "and c.$readableSimpleClientCriteria and c.$activeCriteria order by c.id asc";

    return Arrays.asList(new String[] { hql });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Qualifier(discountBPPropertyExtension)
  public static class DiscountBPProperties extends ModelExtension {

    @Override
    public List<HQLProperty> getHQLProperties(Object params) {
      ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
      list.add(new HQLProperty("c.id", "id"));
      list.add(new HQLProperty("c.bpSet.id", "bpSet"));
      list.add(new HQLProperty("c.promotionDiscount.id", "discount"));
      return list;
    }

  }

}
