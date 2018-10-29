/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

@Qualifier(ProductStock.productStockPropertyExtension)
public class ProductStockProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    List<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("storagedetail.product.id", "m_product_id"));
    list.add(new HQLProperty("locator.warehouse.id", "m_warehouse_id"));
    list.add(new HQLProperty("sum(storagedetail.quantityOnHand) - sum(storagedetail.reservedQty)",
        "availableStock", false));

    return list;

  }
}
