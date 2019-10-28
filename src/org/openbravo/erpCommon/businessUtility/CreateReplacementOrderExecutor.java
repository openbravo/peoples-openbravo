/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.math.BigDecimal;
import java.util.Date;

import javax.enterprise.context.Dependent;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderlineServiceRelation;

@Dependent
class CreateReplacementOrderExecutor {
  private Order oldOrder;
  private Organization organization;
  private Warehouse warehouse;

  @SuppressWarnings("hiding")
  void init(Order oldOrder, Organization organization, Warehouse warehouse) {
    this.oldOrder = oldOrder;
    this.organization = organization;
    this.warehouse = warehouse;
  }

  /**
   * Process that creates a replacement order in temporary status in order to Cancel and Replace an
   * original order
   * 
   * @param oldOrder
   *          Order that will be cancelled and replaced
   */
  Order run() {
    return createReplacementOrder();
  }

  private Order createReplacementOrder() {
    // Create new Order header
    Order newOrder = (Order) DalUtil.copy(oldOrder, false, true);
    // Change order values
    newOrder.setOrganization(organization);
    newOrder.setWarehouse(warehouse);
    newOrder.setProcessed(false);
    newOrder.setPosted("N");
    newOrder.setDocumentStatus("TMP");
    newOrder.setDocumentAction("CO");
    newOrder.setGrandTotalAmount(BigDecimal.ZERO);
    newOrder.setSummedLineAmount(BigDecimal.ZERO);
    Date today = new Date();
    newOrder.setOrderDate(today);
    newOrder.setReplacedorder(oldOrder);
    String newDocumentNo = CancelAndReplaceUtils.getNextCancelDocNo(oldOrder.getDocumentNo());
    newOrder.setDocumentNo(newDocumentNo);
    OBDal.getInstance().save(newOrder);

    // Create new Order lines
    long i = 0;
    try (final ScrollableResults orderLines = CancelAndReplaceUtils.getOrderLineList(oldOrder)) {
      while (orderLines.next()) {
        OrderLine oldOrderLine = (OrderLine) orderLines.get(0);
        // Skip discount lines as they will be created when booking the replacement order
        if (oldOrderLine.getOrderDiscount() != null) {
          continue;
        }
        OrderLine newOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        newOrderLine.setOrganization(organization);
        newOrderLine.setWarehouse(warehouse);
        newOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        newOrderLine.setReservedQuantity(BigDecimal.ZERO);
        newOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
        newOrderLine.setSalesOrder(newOrder);
        newOrderLine.setReplacedorderline(oldOrderLine);
        newOrder.getOrderLineList().add(newOrderLine);
        OBDal.getInstance().save(newOrderLine);
        if ((++i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
          newOrder = OBDal.getInstance().get(Order.class, newOrder.getId());
        }
      }
    }

    // Flush before updating Relations between Products and services to ensure all the Order Lines
    // have been calculated properly
    OBDal.getInstance().flush();
    updateRelationsBetweenOrderLinesProductsAndServices(newOrder);
    return newOrder;
  }

  /**
   * Update the relationships between the services and products in the new order lines. After all
   * the lines are created, it is needed to update relations taking into account if those relations
   * were present in the lines they are replacing
   * 
   * @param order
   *          The new created order where the lines will be updated
   */
  private void updateRelationsBetweenOrderLinesProductsAndServices(Order order) {
    int i = 0;
    try (
        final ScrollableResults newOrderLines = getOrderLinesListWithReplacedLineWithRelatedService(
            order)) {
      while (newOrderLines.next()) {
        updateOrderLineRelatedServices((OrderLine) newOrderLines.get(0));

        if ((++i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
      }
    }
  }

  private ScrollableResults getOrderLinesListWithReplacedLineWithRelatedService(Order order) {
    final StringBuilder hql = new StringBuilder("");
    hql.append(" select ol ");
    hql.append(" from OrderLine ol");
    hql.append(" join ol.replacedorderline rol "); // Explicit join to avoid null values
    hql.append(" where rol.orderlineServiceRelationList is not empty");
    hql.append(" and ol.salesOrder.id = :orderId");

    return OBDal.getInstance()
        .getSession()
        .createQuery(hql.toString(), OrderLine.class)
        .setParameter("orderId", order.getId())
        .scroll(ScrollMode.FORWARD_ONLY);
  }

  private void updateOrderLineRelatedServices(OrderLine orderLine) {
    final Order order = orderLine.getSalesOrder();
    final OrderLine replacedOrderLine = orderLine.getReplacedorderline();

    for (OrderlineServiceRelation replacedRelatedService : replacedOrderLine
        .getOrderlineServiceRelationList()) {
      final OrderLine replacedRelatedOrderLine = replacedRelatedService.getOrderlineRelated();
      final OrderLine orderLineReplacingRelatedOrderLine = getOrderLineReplacingRelatedOrderLine(
          order, replacedRelatedOrderLine);
      addNewOrderLineServiceRelation(orderLine, orderLineReplacingRelatedOrderLine);
    }
  }

  /**
   * Method returns the order line of an order that is replacing an specific order line
   * 
   * @param order
   *          The order where the order line will be searched
   * @param replacedOrderLine
   *          The replaced order line that is searching for
   * @return The order line that is replacing the one passed as parameter
   */
  private OrderLine getOrderLineReplacingRelatedOrderLine(Order order,
      OrderLine replacedOrderLine) {
    return (OrderLine) OBDal.getInstance()
        .createCriteria(OrderLine.class)
        .add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, order))
        .add(Restrictions.eq(OrderLine.PROPERTY_REPLACEDORDERLINE, replacedOrderLine))
        .setMaxResults(1)
        .uniqueResult();
  }

  private void addNewOrderLineServiceRelation(OrderLine orderLine, OrderLine orderLineRelated) {
    final OrderlineServiceRelation newOrderLineServiceRelation = getNewOrderLineServiceRelation(
        orderLine, orderLineRelated);
    orderLine.getOrderlineServiceRelationList().add(newOrderLineServiceRelation);
  }

  private OrderlineServiceRelation getNewOrderLineServiceRelation(OrderLine orderLine,
      OrderLine orderLineRelated) {
    final OrderlineServiceRelation newOrderLineServiceRelation = OBProvider.getInstance()
        .get(OrderlineServiceRelation.class);
    newOrderLineServiceRelation.setClient(orderLine.getClient());
    newOrderLineServiceRelation.setOrganization(orderLine.getOrganization());
    newOrderLineServiceRelation.setAmount(orderLine.getLineGrossAmount());
    newOrderLineServiceRelation.setOrderlineRelated(orderLineRelated);
    newOrderLineServiceRelation.setQuantity(orderLine.getOrderedQuantity());
    newOrderLineServiceRelation.setSalesOrderLine(orderLine);
    OBDal.getInstance().save(newOrderLineServiceRelation);
    return newOrderLineServiceRelation;
  }

}
