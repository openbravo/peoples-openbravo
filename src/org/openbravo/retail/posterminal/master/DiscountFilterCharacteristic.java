/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
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

public class DiscountFilterCharacteristic extends Discount {
  public static final String discFilterCharPropertyExtension = "PricingAdjustmentCharacteristic";
  @Inject
  @Any
  @Qualifier(discFilterCharPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularDiscFilCharPropertyExtensionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "select" + regularDiscFilCharPropertyExtensionHQLProperties.getHqlSelect()
        + " from PricingAdjustmentCharacteristic c  ";
    hql += "   left join c.characteristic.productCharacteristicValueList cvl ";
    hql += " where ((c.$incrementalUpdateCriteria) " + jsonsent.get("operator")
        + " (c.offer.$incrementalUpdateCriteria)) ";
    hql += "   and m_isparent_ch_value(cvl.characteristicValue.id, c.chValue.id, c.characteristic.id) != -1 ";
    hql += "   and exists (select 1 " + getPromotionsHQL(jsonsent, false);
    hql += "                and c.offer = p)";

    return Arrays.asList(new String[] { hql });
  }
}
