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

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ProductBOM.ProductBOMPropertyExtension)
public class ProductBOMProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bom.id", "id"));
        add(new HQLProperty("bom.product.id", "product"));
        add(new HQLProperty("bom.bOMProduct.id", "bomproduct"));
        add(new HQLProperty("bom.bOMProduct.taxCategory.id", "bomtaxcategory"));
        add(new HQLProperty("bom.bOMQuantity", "bomquantity"));
        add(new HQLProperty("bom.obposBomprice", "bomprice"));
      }
    };
    return list;
  }
}