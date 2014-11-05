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
@Qualifier(DiscountFilterCharacteristic.discFilterCharPropertyExtension)
public class DiscountFilterCharacteristicProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(DiscountFilterCharacteristicProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("distinct(coalesce(c.id,cvl.characteristicValue.id))", "id"));
        add(new HQLProperty("c.characteristic.id", "characteristic"));
        add(new HQLProperty("cvl.characteristicValue.id", "chValue"));
        add(new HQLProperty("c.offer.id", "offer"));
        add(new HQLProperty("c.characteristic.name", "_identifier"));
        add(new HQLProperty(
            "(case when c.active = 'Y' and c.offer.active = 'Y' then true else false end)",
            "active"));
      }
    };
    return list;
  }
}
