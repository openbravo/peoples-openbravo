/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.math.BigDecimal;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.common.order.OrderLine;

/**
 * @author airaceburu
 * 
 */

public class OrderLineEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(OrderLine.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (event.getTargetInstance().getEntity() != getObservedEntities()[0]) {
      return;
    }
    updateCanBeDelivered(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (event.getTargetInstance().getEntity() != getObservedEntities()[0]) {
      return;
    }
    updateCanBeDelivered(event);
  }

  private void updateCanBeDelivered(EntityPersistenceEvent event) {
    OrderLine orderLine = (OrderLine) event.getTargetInstance();
    BigDecimal deliveredQuantity = orderLine.getDeliveredQuantity();
    if (deliveredQuantity != null) {
      if (deliveredQuantity.compareTo(orderLine.getOrderedQuantity()) == 0) {
        final Entity orderLineEntity = ModelProvider.getInstance().getEntity(OrderLine.ENTITY_NAME);
        final Property canBeDeliveredProperty = orderLineEntity.getProperty("obposCanbedelivered");
        event.setCurrentState(canBeDeliveredProperty, false);
      }
    }
  }
}