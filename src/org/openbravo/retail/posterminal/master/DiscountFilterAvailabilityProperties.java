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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(DiscountFilterAvailability.discFilterAvailabilityPropertyExtension)
public class DiscountFilterAvailabilityProperties extends ModelExtension {

  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    List<HQLProperty> list = Arrays.asList(//
        new HQLProperty("pav.id", "id"), //
        new HQLProperty("pav.promotionDiscount.id", "priceAdjustment"), //
        new HQLProperty("pav.day", "day"), //
        new HQLProperty("pav.startingTime", "startingTime"), //
        new HQLProperty("pav.endingTime", "endingTime"), //
        new HQLProperty(
            "(case when pav.active = 'Y' and pav.promotionDiscount.active = 'Y' then true else false end)",
            "active"));
    return list;
  }
}
