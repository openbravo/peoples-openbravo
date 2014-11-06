/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

/**
 * @author migueldejuana
 * 
 */
@Qualifier(DiscountFilterBusinessPartner.discFilterBPPropertyExtension)
public class DiscountFilterBusinessPartnerProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(DiscountFilterBusinessPartnerProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bp.id", "id"));
        add(new HQLProperty("bp.priceAdjustment.id", "priceAdjustment"));
        add(new HQLProperty("bp.businessPartner.id", "businessPartner"));
        add(new HQLProperty("concat(bp.priceAdjustment.name, ' - ', bp.businessPartner.name)",
            "_identifier"));
        add(new HQLProperty(
            "(case when bp.active = 'Y' and bp.priceAdjustment.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
