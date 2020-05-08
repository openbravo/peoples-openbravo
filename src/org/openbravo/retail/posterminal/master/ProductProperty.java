/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Product.productPropertyExtension)
public class ProductProperty extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    return Arrays.asList(new HQLProperty("product.obrdmDeliveryMode", "obrdmDeliveryMode"),
        new HQLProperty("product.obrdmDeliveryModeLyw", "obrdmDeliveryModeLyw"),
        new HQLProperty("product.obrdmIsdeliveryservice", "obrdmIsdeliveryservice"));
  }
}
