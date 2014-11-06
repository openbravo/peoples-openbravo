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
@Qualifier(DiscountFilterRole.discFilterRolePropertyExtension)
public class DiscountFilterRoleProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(DiscountFilterRoleProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("r.id", "id"));
        add(new HQLProperty("r.priceAdjustment.id", "priceAdjustment"));
        add(new HQLProperty("r.role.id", "role"));
        add(new HQLProperty("concat(r.priceAdjustment.name, ' - ', r.role.name)", "_identifier"));
        add(new HQLProperty(
            "(case when r.active = 'Y' and r.priceAdjustment.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
