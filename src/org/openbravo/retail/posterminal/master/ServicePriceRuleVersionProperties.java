/*
 ************************************************************************************
 * Copyright (C) 2015-2018 Openbravo S.L.U.
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

@Qualifier(ServicePriceRuleVersion.servicePriceRuleVersionPropertyExtension)
public class ServicePriceRuleVersionProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("sprv.id", "id"));
        add(new HQLProperty("sprv.active", "active"));
        add(new HQLProperty("sprv.product.id", "product"));
        add(new HQLProperty("to_char(sprv.validFromDate,'yyyy-mm-dd')", "validFromDate"));
        add(new HQLProperty("sprv.servicePriceRule.id", "servicePriceRule"));
        add(new HQLProperty("sprv.obposMinimum", "minimum"));
        add(new HQLProperty("sprv.obposMaximum", "maximum"));
      }
    };
    return list;
  }

}