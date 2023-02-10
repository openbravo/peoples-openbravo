/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.event;

import java.math.BigDecimal;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;

/**
 * Validates mandatory to choose Priority Rule if there is max quantity
 */
public class PriceAdjustmentHandler extends EntityPersistenceEventObserver {
  private static final Entity[] ENTITIES = {
      ModelProvider.getInstance().getEntity(PriceAdjustment.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onSave(@Observes EntityNewEvent event) {
    validateData(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    validateData(event);
  }

  private void validateData(EntityPersistenceEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity offerEntity = ModelProvider.getInstance()
        .getEntity(event.getTargetInstance().getEntityName());
    final BigDecimal maxQty = (BigDecimal) event
        .getCurrentState(offerEntity.getProperty(PriceAdjustment.PROPERTY_MAXQUANTITY));
    if (maxQty != null && maxQty.compareTo(BigDecimal.ZERO) > 0) {
      final String priorityRule = (String) event
          .getCurrentState(offerEntity.getProperty(PriceAdjustment.PROPERTY_PRIORITYRULE));
      if (priorityRule == null) {
        throw new OBException("@SelectPriorityRule@");
      }
    }
  }
}