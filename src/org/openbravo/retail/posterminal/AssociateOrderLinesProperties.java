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
        new HQLProperty("ol.id", "lineId"), //
        new HQLProperty("salesOrder.documentNo", "documentNo"), //
        new HQLProperty("salesOrder.description", "description"), //
        new HQLProperty("salesOrder.orderDate", "orderDate"), //
        new HQLProperty("bp.id", "bpId"), //
        new HQLProperty("bp.name", "bpName"), //
        new HQLProperty("bp.searchKey", "bpSearchKey"), //
        new HQLProperty("ol.lineNo", "lineNo"), //
        new HQLProperty("ol.orderedQuantity", "qtyOrdered"), //
        new HQLProperty("ol.deliveredQuantity", "qtyDelivered"), //
        new HQLProperty("p.id", "productId"), //
        new HQLProperty("p.name", "productName"), //
        new HQLProperty("p.uPCEAN", "uPCEAN"), //
        new HQLProperty("ol.createdBy.name", "createdBy"), //
        new HQLProperty("ol.updatedBy.name", "updatedBy"), //
        new HQLProperty("ol.lineGrossAmount", "lineTotal"), //
        new HQLProperty("ol.lineNetAmount", "net"), //
        new HQLProperty("ol.lineGrossAmount", "gross"), //
        new HQLProperty("salesOrder.grandTotalAmount", "orderTotal "));

  }
}
