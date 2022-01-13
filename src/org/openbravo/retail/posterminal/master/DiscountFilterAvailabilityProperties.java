/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
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
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("pav.id", "id"));
        add(new HQLProperty("pav.promotionDiscount.id", "priceAdjustment"));

        add(new HQLProperty("pav.day", "day"));
        add(new HQLProperty("pav.startingTime", "startingTime"));
        add(new HQLProperty("pav.endingTime", "endingTime"));

        add(new HQLProperty(
            "(case when pav.active = 'Y' and pav.promotionDiscount.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
