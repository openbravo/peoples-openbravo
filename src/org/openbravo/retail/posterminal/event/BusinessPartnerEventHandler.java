/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.pricing.pricelist.PriceList;

public class BusinessPartnerEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(BusinessPartner.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final BusinessPartner businessPartner = (BusinessPartner) event.getTargetInstance();

    // Update PriceList Audit
    if (businessPartner.isCustomer()) {
      final Property priceListProperty = entities[0]
          .getProperty(BusinessPartner.PROPERTY_PRICELIST);
      final PriceList previousPriceList = (PriceList) event.getPreviousState(priceListProperty);
      final PriceList currentPriceList = (PriceList) event.getCurrentState(priceListProperty);
      if (previousPriceList != currentPriceList) {
        currentPriceList.setUpdated(new Date());
        OBDal.getInstance().save(currentPriceList);
      }
    }
  }
}
