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
import java.util.Date;
import java.util.Objects;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;

/**
 * Validates mandatory to choose Priority Rule if there is max quantity
 */
public class PriceAdjustmentHandler extends EntityPersistenceEventObserver {
  private static final Entity[] ENTITIES = {
      ModelProvider.getInstance().getEntity(PriceAdjustment.ENTITY_NAME) };

  private final Entity priceAdjustmentEntity = ModelProvider.getInstance()
      .getEntity(PriceAdjustment.ENTITY_NAME);
  private final Property startingDateProperty = priceAdjustmentEntity
      .getProperty(PriceAdjustment.PROPERTY_STARTINGDATE);
  private final Property endingDateProperty = priceAdjustmentEntity
      .getProperty(PriceAdjustment.PROPERTY_ENDINGDATE);

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateData(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateData(event);
    updateOrganizationDates((PriceAdjustment) event.getTargetInstance(),
        (Date) event.getPreviousState(startingDateProperty),
        (Date) event.getPreviousState(endingDateProperty));
  }

  private void validateData(EntityPersistenceEvent event) {
    final Entity offerEntity = ModelProvider.getInstance()
        .getEntity(event.getTargetInstance().getEntityName());
    final BigDecimal maxQty = (BigDecimal) event
        .getCurrentState(offerEntity.getProperty(PriceAdjustment.PROPERTY_MAXQUANTITY));
    final Boolean applyToProduct = (Boolean) event.getCurrentState(
        offerEntity.getProperty(PriceAdjustment.PROPERTY_APPLYTOPRODUCTSUPTOTHEMAXQUANTITY));
    if (maxQty != null && maxQty.compareTo(BigDecimal.ZERO) > 0 && applyToProduct) {
      final String priorityRule = (String) event
          .getCurrentState(offerEntity.getProperty(PriceAdjustment.PROPERTY_PRIORITYRULE));
      if (priorityRule == null) {
        throw new OBException("@SelectPriorityRule@");
      }
    }
  }

  private void updateOrganizationDates(PriceAdjustment discount, Date previousStartingDate,
      Date previousEndingDate) {
    final boolean isStartingDateChanged = !Objects.equals(previousStartingDate,
        discount.getStartingDate());
    final boolean isEndingDateChanged = !Objects.equals(previousEndingDate,
        discount.getEndingDate());
    if (isStartingDateChanged || isEndingDateChanged) {
    //@formatter:off
      final String hql =
          "update from PricingAdjustmentOrganization e" +
          "  set e.startingDate = :startingDate," +
          "    e.endingDate = :endingDate," +
          "    e.updated = :updated," +
          "    e.updatedBy = :updatedBy" +
          " where e.priceAdjustment.id = :discountId";
      //@formatter:on
      OBDal.getInstance()
          .getSession()
          .createQuery(hql)
          .setParameter("discountId", discount.getId())
          .setParameter("startingDate", discount.getStartingDate())
          .setParameter("endingDate", discount.getEndingDate())
          .setParameter("updated", discount.getUpdated())
          .setParameter("updatedBy", discount.getUpdatedBy())
          .executeUpdate();
    }
  }
}
