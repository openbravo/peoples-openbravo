/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Arrays;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(AssociateOrderLines.AssociateOrderLinesPropertyExtension)
public class AssociateOrderLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    return Arrays.asList(
    //
        new HQLProperty("salesOrder.id", "orderId"), //
        new HQLProperty("salesOrder.documentNo", "documentNo"), //
        new HQLProperty("salesOrder.orderDate", "orderDate"), //
        new HQLProperty("salesOrder.grandTotalAmount", "orderTotal "), //
        new HQLProperty("ol.id", "orderlineId"), //
        new HQLProperty("ol.lineNo", "lineNo"), //
        new HQLProperty("ol.orderedQuantity", "qty"), //
        new HQLProperty("COALESCE(ol.deliveredQuantity, 0)", "deliveredQuantity"), //
        new HQLProperty("ol.lineNetAmount", "net"), //
        new HQLProperty("ol.lineGrossAmount", "gross"), //

        new HQLProperty("olo.priceAdjustment.id", "discount_ruleId"), //
        new HQLProperty("offer.id", "discountType_id"), //
        new HQLProperty("offer.name", "discountType_name"), //
        new HQLProperty("olo.priceAdjustmentAmt", "discount_userAmt"), //
        new HQLProperty("olo.totalAmount", "discount_totalAmt"), //
        new HQLProperty("olo.displayedTotalAmount", "discount_displayedTotalAmount"), //
        new HQLProperty("olo.obdiscQtyoffer", "discount_actualAmt"), //

        new HQLProperty("bp.id", "bpId"), //
        new HQLProperty("bp.name", "bpName"), //
        new HQLProperty("p.id", "productId"), //
        new HQLProperty("p.name", "productName")); //

  }
}
