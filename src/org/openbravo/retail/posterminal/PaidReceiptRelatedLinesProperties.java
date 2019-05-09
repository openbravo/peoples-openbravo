/*
 ************************************************************************************
 * Copyright (C) 2015-2018 Openbravo S.L.U.
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
 * Defines hql properties for the PaidReceipsShipLines Order
 * 
 * @author ACA
 * 
 */

@Qualifier(PaidReceipts.paidReceiptsRelatedLinesPropertyExtension)
public class PaidReceiptRelatedLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("rpl.id", "orderlineId"));
        add(new HQLProperty("rp.name", "productName"));
        add(new HQLProperty("rpl.salesOrder.documentNo", "orderDocumentNo"));
        add(new HQLProperty("rpl.salesOrder.id", "orderId"));
        add(new HQLProperty("rpl.orderedQuantity", "qty"));
        add(new HQLProperty("COALESCE(rpl.deliveredQuantity, 0)", "deliveredQuantity"));
        add(new HQLProperty("rpl.baseGrossUnitPrice * rpl.orderedQuantity", "gross"));
        add(new HQLProperty("rpl.standardPrice * rpl.orderedQuantity", "net"));
        add(new HQLProperty(
            "(CASE WHEN rsl.salesOrder.id != rpl.salesOrder.id THEN true ELSE false END)",
            "deferred"));
        add(new HQLProperty("rpl.obposCanbedelivered", "obposCanbedelivered"));
        add(new HQLProperty("rpl.obposIspaid", "obposIspaid"));

        add(new HQLProperty("rpl.product.id", "productId"));
        add(new HQLProperty("rpl.product.productCategory.id", "productCategory"));
      }
    };

    return list;
  }
}
