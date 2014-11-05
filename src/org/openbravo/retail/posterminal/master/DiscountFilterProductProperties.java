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
@Qualifier(DiscountFilterProduct.discFilterProductPropertyExtension)
public class DiscountFilterProductProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(DiscountFilterProductProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("ap.id", "id"));
        add(new HQLProperty("ap.priceAdjustment.id", "priceAdjustment"));
        add(new HQLProperty("ap.product.id", "product"));
        add(new HQLProperty("ap.obdiscIsGift", "obdiscIsGift"));
        add(new HQLProperty("ap.obdiscQty", "obdiscQty"));
        add(new HQLProperty("ap.obdiscGifqty", "obdiscGifqty"));
        add(new HQLProperty("concat(ap.priceAdjustment.name, ' - ', ap.product.name)",
            "_identifier"));
        add(new HQLProperty(
            "(case when ap.active = 'Y' and ap.priceAdjustment.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
