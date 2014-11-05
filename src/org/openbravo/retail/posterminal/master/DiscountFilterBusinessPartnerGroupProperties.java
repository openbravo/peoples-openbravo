/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
@Qualifier(DiscountFilterBusinessPartnerGroup.discFilterBPGPropertyExtension)
public class DiscountFilterBusinessPartnerGroupProperties extends ModelExtension {

  public static final Logger log = Logger
      .getLogger(DiscountFilterBusinessPartnerGroupProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bpg.id", "id"));
        add(new HQLProperty("bpg.priceAdjustment.id", "priceAdjustment"));
        add(new HQLProperty("bpg.businessPartnerCategory.id", "businessPartnerCategory"));
        add(new HQLProperty(
            "concat(bpg.priceAdjustment.name, ' - ', bpg.businessPartnerCategory.name)",
            "_identifier"));
        add(new HQLProperty(
            "(case when bpg.active = 'Y' and bpg.priceAdjustment.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
