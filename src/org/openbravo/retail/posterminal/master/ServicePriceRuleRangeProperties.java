/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ServicePriceRuleRange.servicePriceRuleRangePropertyExtension)
public class ServicePriceRuleRangeProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("sprr.id", "id"));
        add(new HQLProperty("sprr.active", "active"));
        add(new HQLProperty("sprr.afterDiscounts", "afterdiscounts"));
        add(new HQLProperty("sprr.amountUpTo", "amountUpTo"));
        add(new HQLProperty("sprr.percentage", "percentage"));
        add(new HQLProperty("sprr.priceList.id", "priceList"));
        add(new HQLProperty("sprr.ruleType", "ruleType"));
        add(new HQLProperty("sprr.servicepricerule.id", "servicepricerule"));
      }
    };
    return list;
  }

}