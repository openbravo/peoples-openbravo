/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

@Qualifier(Invoices.invoicesLinesPropertyExtension)
public class InvoiceLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("invLine.salesOrderLine.product.id", "id"));
        add(new HQLProperty("invLine.salesOrderLine.product.name", "name"));
        add(new HQLProperty("invLine.salesOrderLine.product.uOM.id", "uOM"));
        add(new HQLProperty("invLine.invoicedQuantity", "quantity"));
        add(new HQLProperty("invLine.salesOrderLine.baseGrossUnitPrice", "unitPrice"));
        add(new HQLProperty("invLine.grossUnitPrice", "grossUnitPrice"));
        add(new HQLProperty("invLine.grossListPrice", "grossListPrice"));
        add(new HQLProperty("invLine.grossAmount", "lineGrossAmount"));
        add(new HQLProperty("invLine.lineNetAmount", "linenetamount"));
        add(new HQLProperty("invLine.id", "lineId"));
        add(new HQLProperty("invLine.salesOrderLine.id", "orderlineId"));
        add(new HQLProperty("invLine.salesOrderLine.standardPrice", "baseNetUnitPrice"));
        add(new HQLProperty("invLine.salesOrderLine.salesOrder.currency.pricePrecision",
            "pricePrecision"));
        add(new HQLProperty("invLine.salesOrderLine.warehouse.id", "warehouse"));
        add(new HQLProperty("invLine.salesOrderLine.warehouse.name", "warehousename"));
        add(new HQLProperty("invLine.salesOrderLine.description", "description"));
        add(new HQLProperty(
            "(invLine.salesOrderLine.deliveredQuantity - (select coalesce(abs(sum(deliveredQuantity)),0) from OrderLine where goodsShipmentLine.salesOrderLine.id =invLine.salesOrderLine.id))",
            "remainingQuantity"));
        add(new HQLProperty(
            "coalesce(invLine.salesOrderLine.product.overdueReturnDays, 999999999999)",
            "overdueReturnDays"));
        add(new HQLProperty("invLine.salesOrderLine.product.productType", "productType"));
        add(new HQLProperty("invLine.salesOrderLine.product.returnable", "returnable"));
        add(new HQLProperty("invLine.tax.taxCategory.id", "taxCategory"));

      }
    };

    return list;
  }
}
