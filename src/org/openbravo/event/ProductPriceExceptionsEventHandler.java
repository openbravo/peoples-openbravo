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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
