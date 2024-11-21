/*
 ************************************************************************************
 * Copyright (C) 2023 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.event;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.config.OBRETCOProductList;

public class RemoteAssortmentEventHandler extends EntityPersistenceEventObserver {
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
    validConfiguration(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validConfiguration(event);
  }

  private void validConfiguration(EntityPersistenceEvent event) {
    final Entity orgEntity = ModelProvider.getInstance().getEntity(Organization.ENTITY_NAME);

    final Property productListProperty = orgEntity
        .getProperty(Organization.PROPERTY_OBRETCOPRODUCTLIST);
    final Property remoteProductListProperty = orgEntity
        .getProperty(Organization.PROPERTY_OBRETCOEXTENDEDPRODLIST);

    final OBRETCOProductList productList = (OBRETCOProductList) event
        .getCurrentState(productListProperty);
    final OBRETCOProductList remoteProductList = (OBRETCOProductList) event
        .getCurrentState(remoteProductListProperty);

    if (null != productList && productList == remoteProductList) {
      throw new OBException(OBMessageUtils.getI18NMessage("OBRETCO_WrongRemoteConfiguration"));
    }
  }

}
