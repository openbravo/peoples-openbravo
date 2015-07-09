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
 * All portions are Copyright (C) 2013-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.ServicePriceUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderlineServiceRelation;
import org.openbravo.model.common.plm.Product;

public class ServiceRelationEventHandler extends EntityPersistenceEventObserver {
  private static final Object UNIQUE_QUANTITY = "UQ";
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OrderlineServiceRelation.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(ServiceRelationEventHandler.class);

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity productEntity = ModelProvider.getInstance().getEntity(
        OrderlineServiceRelation.ENTITY_NAME);
    final Property solProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_SALESORDERLINE);
    final Property amountProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_AMOUNT);
    final Property quantityProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_QUANTITY);
    BigDecimal amount = (BigDecimal) event.getCurrentState(amountProperty);
    BigDecimal quantity = (BigDecimal) event.getCurrentState(quantityProperty);
    OrderLine orderLine = (OrderLine) event.getCurrentState(solProperty);
    updateOrderLine(orderLine, amount, quantity, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity productEntity = ModelProvider.getInstance().getEntity(
        OrderlineServiceRelation.ENTITY_NAME);
    final Property solProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_SALESORDERLINE);
    final Property amountProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_AMOUNT);
    final Property quantityProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_QUANTITY);
    BigDecimal currentAmount = (BigDecimal) event.getCurrentState(amountProperty);
    BigDecimal currentQuantity = (BigDecimal) event.getCurrentState(quantityProperty);
    BigDecimal oldAmount = (BigDecimal) event.getPreviousState(amountProperty);
    BigDecimal oldQuantity = (BigDecimal) event.getPreviousState(quantityProperty);
    OrderLine orderLine = (OrderLine) event.getCurrentState(solProperty);
    updateOrderLine(orderLine, currentAmount, currentQuantity, oldAmount, oldQuantity);
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity productEntity = ModelProvider.getInstance().getEntity(
        OrderlineServiceRelation.ENTITY_NAME);
    final Property solProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_SALESORDERLINE);
    final Property amountProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_AMOUNT);
    final Property quantityProperty = productEntity
        .getProperty(OrderlineServiceRelation.PROPERTY_QUANTITY);
    BigDecimal oldAmount = (BigDecimal) event.getCurrentState(amountProperty);
    BigDecimal oldQuantity = (BigDecimal) event.getCurrentState(quantityProperty);
    OrderLine orderLine = (OrderLine) event.getCurrentState(solProperty);
    updateOrderLine(orderLine, BigDecimal.ZERO, BigDecimal.ZERO, oldAmount, oldQuantity);
  }

  private void updateOrderLine(OrderLine orderLine, BigDecimal currentAmount,
      BigDecimal currentqty, BigDecimal oldAmount, BigDecimal oldQuantity) {
    BigDecimal serviceQty = BigDecimal.ONE;
    BigDecimal dbAmount = ServicePriceUtils.getRelatedAmountAndQty(orderLine).get("amount");
    BigDecimal serviceAmount = ServicePriceUtils.getServiceAmount(orderLine,
        dbAmount.add(currentAmount.subtract(oldAmount)));
    Product service = orderLine.getProduct();
    Currency currency = orderLine.getCurrency();
    if (UNIQUE_QUANTITY.equals(service.getQuantityRule())) {

      orderLine.setOrderedQuantity(BigDecimal.ONE);
    } else {
      orderLine.setOrderedQuantity(orderLine.getOrderedQuantity().add(currentqty)
          .subtract(oldQuantity));
    }
    if (BigDecimal.ZERO.compareTo(serviceQty) == 0) {
      throw new OBException("ZeroQuantityService");
    }
    BigDecimal servicePrice = serviceAmount.divide(serviceQty, currency.getPricePrecision()
        .intValue(), RoundingMode.HALF_UP);
    if (orderLine.getSalesOrder().isPriceIncludesTax()) {
      orderLine.setGrossUnitPrice(servicePrice);
      orderLine.setLineGrossAmount(serviceAmount);
    } else {
      orderLine.setUnitPrice(servicePrice);
      orderLine.setLineNetAmount(serviceAmount);
    }
    OBDal.getInstance().save(orderLine);
  }
}