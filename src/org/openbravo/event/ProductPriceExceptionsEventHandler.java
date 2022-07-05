/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.ProductPriceException;

public class ProductPriceExceptionsEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductPriceException.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    updateOrgDepth(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    updateOrgDepth(event);
  }

  private void updateOrgDepth(EntityPersistenceEvent event) {
    final Entity ppeEntity = ModelProvider.getInstance()
        .getEntity(ProductPriceException.ENTITY_NAME);
    final Property orgProperty = ppeEntity.getProperty(ProductPriceException.PROPERTY_ORGANIZATION);
    final Property orgDepthProperty = ppeEntity
        .getProperty(ProductPriceException.PROPERTY_ORGDEPTH);

    final Organization org = (Organization) event.getCurrentState(orgProperty);

    event.setCurrentState(orgDepthProperty, getOrgDepth(org));
  }

  private long getOrgDepth(Organization org) {
    return calculateOrgDepth(0, org);

  }

  private long calculateOrgDepth(int depth, Organization org) {
    OrganizationStructureProvider osp = null;
    try {
      osp = OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId());
    } catch (Exception e) {
      logger.error("Error trying to get organization structure: ", e);
    }
    if (org.getId().equals("0")) {
      return depth;
    } else {
      return calculateOrgDepth(depth + 1, osp.getParentOrg(org));
    }
  }

}
