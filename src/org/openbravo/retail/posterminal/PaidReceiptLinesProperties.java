/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

/**
 * Defines hql properties for the PaidReceipsLines Order header
 * 
 * @author ral
 * 
 */

@Qualifier(PaidReceipts.paidReceiptsLinesPropertyExtension)
public class PaidReceiptLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("ordLine.product.id", "id"));
        add(new HQLProperty("ordLine.product.name", "name"));
        add(new HQLProperty("ordLine.product.uOM.id", "uOM"));
        add(new HQLProperty("ordLine.orderedQuantity", "quantity"));
        add(new HQLProperty("ordLine.baseGrossUnitPrice", "unitPrice"));
        add(new HQLProperty("ordLine.lineGrossAmount", "linegrossamount"));
        add(new HQLProperty("ordLine.id", "lineId"));
        add(new HQLProperty("ordLine.standardPrice", "baseNetUnitPrice"));
        add(new HQLProperty("ordLine.salesOrder.currency.pricePrecision", "pricePrecision"));
        add(new HQLProperty("ordLine.warehouse.id", "warehouse"));
        add(new HQLProperty("ordLine.warehouse.name", "warehousename"));
      }
    };

    return list;
  }
}
