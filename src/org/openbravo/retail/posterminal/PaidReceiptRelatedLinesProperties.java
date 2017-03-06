/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
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
        add(new HQLProperty("olsr.orderlineRelated.id", "orderlineId"));
        add(new HQLProperty("olsr.orderlineRelated.product.name", "productName"));
        add(new HQLProperty("olsr.orderlineRelated.salesOrder.documentNo", "orderDocumentNo"));
        add(new HQLProperty("olsr.orderlineRelated.salesOrder.id", "orderId"));
        add(new HQLProperty("olsr.orderlineRelated.orderedQuantity", "qty"));
        add(new HQLProperty(
            "olsr.orderlineRelated.baseGrossUnitPrice * olsr.orderlineRelated.orderedQuantity",
            "gross"));
        add(new HQLProperty(
            "olsr.orderlineRelated.standardPrice * olsr.orderlineRelated.orderedQuantity", "net"));
        add(new HQLProperty(
            "(case when olsr.salesOrderLine.salesOrder.id != olsr.orderlineRelated.salesOrder.id "
                + "then true else false end)", "deferred"));
        add(new HQLProperty("olsr.orderlineRelated.obposCanbedelivered", "obposCanbedelivered"));
        add(new HQLProperty("olsr.orderlineRelated.obposIspaid", "obposIspaid"));
      }
    };

    return list;
  }
}
