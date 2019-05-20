/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

class DeferredServiceDelivery {

  static void calculateQtyToDeliver(JSONObject jsonorder) throws JSONException {

    JSONArray jsonorderlines = jsonorder.getJSONArray("lines");

    for (int i = 0; i < jsonorderlines.length(); i++) {

      JSONObject jsonOrderLine = jsonorderlines.getJSONObject(i);
      BigDecimal orderedQuantity = BigDecimal.valueOf(jsonOrderLine.getDouble("qty"));

      if (jsonOrderLine.optBoolean("isDeferredService", false)
          && orderedQuantity.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal deferredQtyToDeliver = BigDecimal.ZERO;
        BigDecimal relatedDeliveredQty = BigDecimal.ZERO;
        JSONArray relatedLines = jsonOrderLine.getJSONArray("relatedLines");

        for (int j = 0; j < relatedLines.length(); j++) {
          JSONObject relatedLine = relatedLines.getJSONObject(j);
          if (relatedLine.optBoolean("deferred", false)) {
            OrderLine ol = OBDal.getInstance().get(OrderLine.class, relatedLine.get("orderlineId"));
            deferredQtyToDeliver = deferredQtyToDeliver.add(ol.getDeliveredQuantity());
          } else {
            JSONObject orderlinefromjson = getOrderLineFromJSONArray(jsonorderlines,
                relatedLine.getString("orderlineId"));
            relatedDeliveredQty = relatedDeliveredQty
                .add(BigDecimal.valueOf(orderlinefromjson.getDouble("obposQtytodeliver")));
          }
        }

        BigDecimal qtytodeliver = relatedDeliveredQty.add(deferredQtyToDeliver)
            .min(orderedQuantity);

        if (jsonorder.optBoolean("deliver", true) && qtytodeliver.compareTo(orderedQuantity) != 0) {
          jsonorder.put("deliver", false);
        }

        jsonOrderLine.put("obposQtytodeliver", qtytodeliver.doubleValue());

      }
    }
  }

  private static JSONObject getOrderLineFromJSONArray(JSONArray jsonorderlines, String orderlineId)
      throws JSONException {
    for (int w = 0; w < jsonorderlines.length(); w++) {
      JSONObject orderlinetocheck = jsonorderlines.getJSONObject(w);
      if (orderlinetocheck.getString("id").equals(orderlineId)) {
        return orderlinetocheck;
      }
    }
    return null;
  }

  static void createShipmentLinesForDeferredServices(JSONObject jsonorder, Order order, ShipmentInOut shipment) {

    if (jsonorder.optBoolean("isQuotation", false) || jsonorder.optBoolean("obposIsDeleted", false)
        || !jsonorder.optBoolean("generateShipment", false)) {
      return;
    }

    Boolean hasDeliveredLines = false;
    for (OrderLine ol : order.getOrderLineList()) {
      if (ol.isObposIspaid()) {
        hasDeliveredLines = true;
      }
    }
    if (!hasDeliveredLines) {
      return;
    }

    final Session session = OBDal.getInstance().getSession();

    // Check if there's any deferred service to deliver. Otherwise, continue
    final StringBuilder checkServiceToDeliverHQL = new StringBuilder();
    checkServiceToDeliverHQL.append("SELECT 1 ");
    checkServiceToDeliverHQL.append("FROM OrderlineServiceRelation AS olsr ");
    checkServiceToDeliverHQL.append("JOIN olsr.orderlineRelated AS pol ");
    checkServiceToDeliverHQL.append("JOIN olsr.salesOrderLine AS sol ");
    checkServiceToDeliverHQL.append("WHERE pol.salesOrder.id = :orderId ");
    checkServiceToDeliverHQL.append("AND pol.salesOrder.id <> sol.salesOrder.id ");
    checkServiceToDeliverHQL.append("AND sol.obposIspaid = true");
    final Query<String> checkServiceToDeliverQuery = session
        .createQuery(checkServiceToDeliverHQL.toString(), String.class);
    checkServiceToDeliverQuery.setParameter("orderId", order.getId());
    checkServiceToDeliverQuery.setMaxResults(1);

    if (checkServiceToDeliverQuery.uniqueResult() == null) {
      return;
    }

    final String deferredLinesHqlQuery = "select osr.salesOrderLine"
        + " from OrderlineServiceRelation osr join osr.orderlineRelated olr"
        + " where olr.salesOrder.id =:orderId"
        + " and olr.salesOrder.id <> osr.salesOrderLine.salesOrder.id"
        + " and osr.salesOrderLine.obposIspaid = true";
    final Query<OrderLine> query = session.createQuery(deferredLinesHqlQuery, OrderLine.class);
    query.setParameter("orderId", order.getId());

    for (final OrderLine serviceOrderLine : query.list()) {
      if (serviceOrderLine.getDeliveredQuantity() != null && serviceOrderLine.getDeliveredQuantity()
          .compareTo(serviceOrderLine.getOrderedQuantity()) == 0) {
        continue;
      }
      if ("UQ".equals(serviceOrderLine.getProduct().getQuantityRule())) {
        String relatedDeliveredLinesHqlQuery = "select count(olsr.id) " //
            + "from OrderlineServiceRelation olsr " //
            + "join olsr.orderlineRelated as relatedLine " //
            + "where olsr.salesOrderLine.id = :orderLineId " //
            + "and relatedLine.deliveredQuantity <> 0";
        final Session relatedLinesSession = OBDal.getInstance().getSession();
        final Query<Long> deliveredRelatedLinesCountQuery = relatedLinesSession
            .createQuery(relatedDeliveredLinesHqlQuery, Long.class);
        deliveredRelatedLinesCountQuery.setParameter("orderLineId", serviceOrderLine.getId());
        BigDecimal deliveredRelatedLinesQty = BigDecimal
            .valueOf(deliveredRelatedLinesCountQuery.uniqueResult());
        if (deliveredRelatedLinesQty.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal qtyPendingToBeDelivered = BigDecimal.ONE
              .subtract(serviceOrderLine.getDeliveredQuantity());
          if (qtyPendingToBeDelivered.compareTo(BigDecimal.ZERO) != 0) {
            serviceOrderLine.setDeliveredQuantity(BigDecimal.ONE);
            createShipmentLine(shipment, serviceOrderLine, BigDecimal.ONE);
          }
        }
      } else {
        String relatedLinesHqlQtyQuery = "select sum(relatedLine.deliveredQuantity) as quantity "
            + "from OrderlineServiceRelation olsr " //
            + "join olsr.orderlineRelated as relatedLine " //
            + "where olsr.salesOrderLine.id = :orderLineId";
        final Session relatedLinesSession = OBDal.getInstance().getSession();
        final Query<BigDecimal> relatedLinesQuery = relatedLinesSession
            .createQuery(relatedLinesHqlQtyQuery, BigDecimal.class);
        relatedLinesQuery.setParameter("orderLineId", serviceOrderLine.getId());
        BigDecimal relatedLinesDeliveredQty = relatedLinesQuery.uniqueResult();
        BigDecimal qtyPendingToBeDelivered = relatedLinesDeliveredQty
            .subtract(serviceOrderLine.getDeliveredQuantity());
        if (qtyPendingToBeDelivered.compareTo(BigDecimal.ZERO) != 0) {
          serviceOrderLine.setDeliveredQuantity(relatedLinesDeliveredQty);
          createShipmentLine(shipment, serviceOrderLine, qtyPendingToBeDelivered);
        }
      }
    }
  }

  private static ShipmentInOutLine createShipmentLine(ShipmentInOut shipment, OrderLine orderLine,
      BigDecimal qty) {

    ShipmentInOutLine shipmentLine = OBProvider.getInstance().get(ShipmentInOutLine.class);

    try {
      // Triggers are disabled here to add a new shipment line to the shipment (already processed)
      // There is no impact on the data as services do not generate transactions.
      TriggerHandler.getInstance().disable();

      shipmentLine.setOrganization(shipment.getOrganization());
      shipmentLine.setShipmentReceipt(shipment);
      shipmentLine.setSalesOrderLine(orderLine);
      Long lineNo = (shipment.getMaterialMgmtShipmentInOutLineList().size() + 1) * 10L;
      shipmentLine.setLineNo(lineNo);
      shipmentLine.setProduct(orderLine.getProduct());
      shipmentLine.setUOM(orderLine.getUOM());
      shipmentLine.setMovementQuantity(qty);

      String description = orderLine.getDescription();
      if (description != null && description.length() > 255) {
        description = description.substring(0, 254);
      }
      shipmentLine.setDescription(description);
      if (orderLine.getBOMParent() != null) {
        OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance()
            .createCriteria(ShipmentInOutLine.class);
        obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
        obc.add(
            Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, orderLine.getBOMParent()));
        obc.setMaxResults(1);
        shipmentLine.setBOMParent((ShipmentInOutLine) obc.uniqueResult());
      }

      OBDal.getInstance().save(shipmentLine);
      shipment.getMaterialMgmtShipmentInOutLineList().add(shipmentLine);
      OBDal.getInstance().save(shipment);
      OBDal.getInstance().flush();
    } finally {
      TriggerHandler.getInstance().enable();
    }
    return shipmentLine;
  }
}
