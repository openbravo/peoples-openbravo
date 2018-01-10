/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
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

@Qualifier("OBPOS_ProductServiceLinkedExtension")
public class ProductServiceLinkedProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    return Arrays.asList( //
        new HQLProperty("psl.id", "id"), //
        new HQLProperty("psl.product.id", "product"), //
        new HQLProperty("psl.productCategory.id", "productCategory"), //
        new HQLProperty("psl.taxCategory.id", "taxCategory"));
  }
}