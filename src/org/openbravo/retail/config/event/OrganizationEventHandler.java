/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.event;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;

public class OrganizationEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(Organization.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity orgEntity = ModelProvider.getInstance().getEntity(Organization.ENTITY_NAME);
    final Organization org = (Organization) event.getTargetInstance();

    final Property productListProperty = orgEntity
        .getProperty(Organization.PROPERTY_OBRETCOPRODUCTLIST);
    final OBRETCOProductList previousProductList = (OBRETCOProductList) event
        .getPreviousState(productListProperty);
    final OBRETCOProductList currentProductList = (OBRETCOProductList) event
        .getCurrentState(productListProperty);
    if (previousProductList == null && currentProductList != null) {
      addPropertyToOrgChildren(org, Organization.PROPERTY_OBRETCOPRODUCTLIST, currentProductList);
    }

    final Property priceListProperty = orgEntity
        .getProperty(Organization.PROPERTY_OBRETCOPRICELIST);
    final PriceList previousPriceList = (PriceList) event.getPreviousState(priceListProperty);
    final PriceList currentPriceList = (PriceList) event.getCurrentState(priceListProperty);
    if (previousPriceList == null && currentPriceList != null) {
      addPropertyToOrgChildren(org, Organization.PROPERTY_OBRETCOPRICELIST, currentPriceList);
    }

  }

  private void addPropertyToOrgChildren(final Organization org, final String property,
      final Object propertyValue) {
    final String update = " update Organization o set " + property + " = :propertyValue "
        + " where o." + property + " is null and o.oBRETCORetailOrgType = 'S' "
        + " and ad_isorgincluded(o.id, :orgId, o.client.id) > 1";

    OBDal.getInstance()
        .getSession()
        .createQuery(update)
        .setParameter("orgId", org.getId())
        .setParameter("propertyValue", propertyValue)
        .executeUpdate();
  }

}
