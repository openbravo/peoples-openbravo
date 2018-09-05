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
        new HQLProperty("ol.orderedQuantity - ol.deliveredQuantity", "qtyPending"), //
        new HQLProperty("ol.lineGrossAmount", "lineTotal"), //
        new HQLProperty("ol.lineNetAmount", "net"), //
        new HQLProperty("ol.lineGrossAmount", "gross"), //
        new HQLProperty("bp.id", "businessPartner"), //
        new HQLProperty("bp.name", "businessPartnerName"), //
        new HQLProperty("p.id", "productId"), //
        new HQLProperty("p.name", "productName")); //

  }
}
