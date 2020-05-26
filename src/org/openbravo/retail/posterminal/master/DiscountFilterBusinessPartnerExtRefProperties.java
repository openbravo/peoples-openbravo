/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(DiscountFilterBusinessPartnerExtRef.DiscFilterBPExtRefPropertyExtension)
public class DiscountFilterBusinessPartnerExtRefProperties extends ModelExtension {

  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    return Arrays.asList( //
        new HQLProperty("bpextref.id", "id"), //
        new HQLProperty("bpextref.promotionDiscount.id", "priceAdjustment"), //
        new HQLProperty("bpextref.externalBusinessPartnerReference",
            "externalBusinessPartnerReference"), //
        new HQLProperty(
            "concat(bpextref.promotionDiscount.name, ' - ', bpextref.externalBusinessPartnerReference)",
            "_identifier"), //
        new HQLProperty(
            "(case when bpextref.active = 'Y' and bpextref.promotionDiscount.active = 'Y' then true else false end)",
            "active"));
  }
}
