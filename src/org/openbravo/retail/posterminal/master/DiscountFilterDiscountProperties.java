/*
 ************************************************************************************
 * Copyright (C) 2014-2018 Openbravo S.L.U.
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

/**
 * @author migueldejuana
 * 
 */
@Qualifier(DiscountFilterDiscount.discFilterDiscountPropertyExtension)
public class DiscountFilterDiscountProperties extends ModelExtension {

  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("apd.id", "id"));
        add(new HQLProperty("apd.promotionDiscount.id", "priceAdjustment"));

        add(new HQLProperty("apd.offerOfferType.id", "discountPromotionType"));
        add(new HQLProperty(
            "(case when apd.priceAdjustmentDiscount is null then null else apd.priceAdjustmentDiscount.id end)",
            "priceAdjustmentDiscount"));

        add(new HQLProperty(
            "concat(apd.promotionDiscount.name, ' - ', apd.offerOfferType.commercialName)",
            "_identifier"));

        add(new HQLProperty(
            "(case when apd.active = 'Y' and apd.promotionDiscount.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
