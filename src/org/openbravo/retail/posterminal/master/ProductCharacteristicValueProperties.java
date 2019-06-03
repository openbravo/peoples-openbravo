/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
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

@Qualifier(ProductCharacteristicValue.productCharacteristicValuePropertyExtension)
public class ProductCharacteristicValueProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {
    final ArrayList<HQLProperty> list = new ArrayList<>();
    list.add(new HQLProperty("pcv.id", "id"));
    list.add(new HQLProperty("product.id", "product"));
    list.add(new HQLProperty("characteristic.id", "characteristic"));
    list.add(new HQLProperty("characteristic.name", "_identifier"));
    list.add(new HQLProperty("characteristic.active", "active"));
    list.add(new HQLProperty("characteristic.obposFilteronwebpos", "obposFilteronwebpos"));
    list.add(new HQLProperty("characteristicValue.id", "characteristicValue"));
    return list;
  }
}
