/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(PendingOrderLines.PENDINGORDERLINESPROPERTYEXTENSION)
public class PendingOrderLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    return Arrays.asList(new HQLProperty("salesOrder.id", "orderId"), //
        new HQLProperty("ol.id", "lineId"), //
        new HQLProperty("salesOrder.documentNo", "documentNo"), //
        new HQLProperty("ol.description", "description"), //
        new HQLProperty("salesOrder.description", "headerDescription"), //
        new HQLProperty("salesOrder.orderDate", "orderDate"), //
        new HQLProperty("bp.id", "bpId"), //
        new HQLProperty("bp.name", "bpName"), //
        new HQLProperty("bp.searchKey", "bpSearchKey"), //
        new HQLProperty("bp.invoiceTerms", "bpInvoiceTerms"), //
        new HQLProperty("locAddress.id", "bpLocId"), //
        new HQLProperty("locAddress.addressLine1", "bpLocName"), //
        new HQLProperty("locAddress.addressLine2", "addressLine2"),
        new HQLProperty("locAddress.postalCode||' '||locAddress.cityName", "bpCityName"), //
        new HQLProperty("country.name", "bpCountryName"), //
        new HQLProperty(
            "(select max(usr.phone) from ADUser usr where usr.businessPartner.id = bp.id and usr.active=true)",
            "bpPhone"), //
        new HQLProperty(
            "(select grt.name from BusinessPartner bpr left join bpr.greeting.greetingTrlList grt where bp.id = bpr.id and grt.language='"
                + OBContext.getOBContext().getLanguage().getLanguage() + "')",
            "bpGreetingName"), //
        new HQLProperty("ol.lineNo", "lineNo"), //
        new HQLProperty("ol.orderedQuantity", "qtyOrdered"), //
        new HQLProperty("ol.deliveredQuantity", "qtyDelivered"), //
        new HQLProperty("ol.obrdmDeliveryDate", "dateDelivered"), //
        new HQLProperty("p.id", "productId"), //
        new HQLProperty("p.searchKey", "productSearchKey"), //
        new HQLProperty("p.name", "productName"), //
        new HQLProperty("p.uPCEAN", "uPCEAN"), //
        new HQLProperty("coalesce(p.characteristicDescription, '')", "characteristicDescription"), //
        new HQLProperty("coalesce(attr.description, '')", "attributeDescription"), //
        new HQLProperty("brand.name", "brand"), //
        new HQLProperty("ol.createdBy.name", "createdBy"), //
        new HQLProperty("ol.updatedBy.name", "updatedBy"), //
        new HQLProperty("ol.obrdmDeliveryMode", "deliveryMode"), //
        new HQLProperty("ol.warehouse.id", "warehouseId"), //
        new HQLProperty("ol.lineGrossAmount", "lineTotal"), //
        new HQLProperty("salesOrder.grandTotalAmount", "orderTotal "),
        new HQLProperty("salesOrder.obposPrepaymentamt", "obposPrepaymentamt"), //
        new HQLProperty("salesOrder.obposPrepaymentlimitamt", "obposPrepaymentlimitamt"), //
        new HQLProperty(
            "(SELECT coalesce(sum(psd.amount), 0) FROM FIN_Payment_ScheduleDetail AS psd JOIN psd.orderPaymentSchedule AS ps WHERE ps.order.id = salesOrder.id AND psd.paymentDetails IS NOT NULL)",
            "payment"));
  }
}
