/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ServiceDeliverUtility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.retail.posterminal.InvoiceShipmentHook;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.json.JsonConstants;

/**
 * 
 * Generate Shipment documents, perform delivery of services and invoice the document, if possible.
 */
public class IssueSalesOrderLines extends JSONProcessSimple {

  private static final Logger log = LogManager.getLogger();

  private GoodsShipmentGenerator shipmentGenerator;

  @Inject
  @Any
  private Instance<InvoiceShipmentHook> invoiceShipmentHook;

  @Override
  public JSONObject exec(JSONObject json) {
    final JSONObject jsonResponse = new JSONObject();
    OBContext.setAdminMode(false);
    try {

      JSONArray ordersFromJson = json.getJSONArray("orders");
      StringBuilder shipmentDocumentNumbers = new StringBuilder();
      Map<String, BigDecimal> qtyDeliveredByOrderLine = new HashMap<>();
      final JSONArray deliveredOrders = new JSONArray();
      for (int i = 0; i < ordersFromJson.length(); i++) {
        final JSONObject orderFromJson = (JSONObject) ordersFromJson.get(i);
        JSONArray linesFromJson = orderFromJson.getJSONArray("lines");
        shipmentGenerator = WeldUtils
            .getInstanceFromStaticBeanManager(GoodsShipmentGenerator.class);
        ShipmentInOut shipment = createNewShipment(orderFromJson);
        Set<String> orders = new HashSet<>();
        for (int j = 0; j < linesFromJson.length(); j++) {
          JSONObject jsonLine = linesFromJson.getJSONObject(j);
          qtyDeliveredByOrderLine.put(jsonLine.getString("lineId"), BigDecimal.ZERO);
          orders.add(jsonLine.getString("orderId"));
          addLineToShipment(jsonLine);
        }
        linkOrderWithShipmentIfOrderIsUnique(shipment, orders);
        OBDal.getInstance().flush(); // Persists the shipment document in order to be ready for
                                     // further processing
        ServiceDeliverUtility.deliverServices(shipment);
        shipmentGenerator.processShipment();
        final Invoice invoice = shipmentGenerator.invoiceShipmentIfPossible();

        executeHooks(invoiceShipmentHook, orderFromJson, shipment, invoice);
        final JSONObject jsonOrder = new JSONObject();
        jsonOrder.put("id", orderFromJson.getString("order"));
        jsonOrder.put("invoiceId", invoice != null ? invoice.getId() : "null");
        deliveredOrders.put(jsonOrder);

        if (i > 0) {
          shipmentDocumentNumbers.append(", ");
        }
        shipmentDocumentNumbers.append(shipment.getDocumentNo());
      }

      updateQtyDeliveredByOrderLines(qtyDeliveredByOrderLine);

      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      JSONObject jsonData = new JSONObject();
      jsonData.put("response", String.format(OBMessageUtils.messageBD("OBRDM_ShipmentDocNoCreated"),
          shipmentDocumentNumbers.toString()));
      jsonData.put("qtyDeliveredByOrderLine", qtyDeliveredByOrderLine);
      jsonData.put("deliveredOrders", deliveredOrders);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);

    } catch (Exception e) {
      try {
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
        jsonResponse.put(JsonConstants.RESPONSE_ERRORMESSAGE, cleanUpMessage(e.getMessage()));
        log.debug(e.getMessage());
      } catch (JSONException e1) {
        log.debug(e1.getMessage());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonResponse;
  }

  private ShipmentInOut createNewShipment(final JSONObject orderFromJson) throws JSONException {
    Organization organization = OBDal.getInstance()
        .getProxy(Organization.class, orderFromJson.getString("organizationId"));
    Warehouse warehouse = OBDal.getInstance()
        .getProxy(Warehouse.class, orderFromJson.getString("warehouseId"));
    BusinessPartner businessPartner = OBDal.getInstance()
        .getProxy(BusinessPartner.class, orderFromJson.getString("bpId"));
    Order salesOrder = OBDal.getInstance().getProxy(Order.class, orderFromJson.getString("order"));
    return shipmentGenerator.createNewGoodsShipment(organization, warehouse, businessPartner,
        salesOrder);
  }

  private void addLineToShipment(final JSONObject jsonLine) throws JSONException {
    Product product = OBDal.getInstance().getProxy(Product.class, jsonLine.getString("productId"));
    BigDecimal quantity = new BigDecimal(jsonLine.getString("toPrepare"));
    OrderLine salesOrderLine = OBDal.getInstance()
        .getProxy(OrderLine.class, jsonLine.getString("lineId"));
    shipmentGenerator.createShipmentLines(product, quantity, salesOrderLine);
  }

  private void linkOrderWithShipmentIfOrderIsUnique(final ShipmentInOut shipment,
      final Set<String> orders) {
    if (orders.size() == 1) {
      Order order = OBDal.getInstance().getProxy(Order.class, orders.iterator().next());
      shipment.setSalesOrder(order);
      if (order.getInvoiceList().size() == 1) {
        shipment.setInvoice(order.getInvoiceList().get(0));
      }
    }
  }

  private void updateQtyDeliveredByOrderLines(
      final Map<String, BigDecimal> qtyDeliveredByOrderLine) {
    for (Entry<String, BigDecimal> qtyDeliveredByLine : qtyDeliveredByOrderLine.entrySet()) {
      qtyDeliveredByOrderLine.put(qtyDeliveredByLine.getKey(),
          getQtyDeliveredByOrderLine(qtyDeliveredByLine.getKey()));
    }
  }

  private BigDecimal getQtyDeliveredByOrderLine(final String orderLineId) {
    return OBDal.getInstance().getProxy(OrderLine.class, orderLineId).getDeliveredQuantity();
  }

  /**
   * If an attempt of deliver sales order lines is done on an organization with AWO flow enabled, an
   * error message including an hyperlink is returned. This method removes the hyperlink to simplify
   * the message to be shown to the user, due to limited space in UI to show the response.
   */
  private String cleanUpMessage(String message) {
    if (!StringUtils.contains(message, "<a")) {
      return message;
    }
    return message.replaceAll("<.*?>", "");
  }

  private void executeHooks(Instance<? extends Object> hooks, JSONObject jsonorder,
      ShipmentInOut shipment, Invoice invoice) throws Exception {
    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      ((InvoiceShipmentHook) proc).exec(jsonorder, shipment, invoice);
    }
  }
}
