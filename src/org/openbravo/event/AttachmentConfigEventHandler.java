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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.window.AttachmentUtils;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentConfigEventHandler extends EntityPersistenceEventObserver {
  private static final Logger logger = LoggerFactory.getLogger(AttachmentConfigEventHandler.class);

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      AttachmentConfig.ENTITY_NAME) };
  private static Property propActive = entities[0].getProperty(AttachmentConfig.PROPERTY_ACTIVE);

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final AttachmentConfig newAttConfig = (AttachmentConfig) event.getTargetInstance();

    isAnyActivated(event);
    String strClient = (String) DalUtil.getId(newAttConfig.getClient());
    if ((Boolean) event.getCurrentState(propActive)) {
      AttachmentUtils.setAttachmentConfig(strClient, event.getId());
    } else if ((Boolean) event.getPreviousState(propActive)) {
      // Deactivating a config reset AttachmentUtils state
      AttachmentUtils.setAttachmentConfig(strClient, null);
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final AttachmentConfig newAttConfig = (AttachmentConfig) event.getTargetInstance();

    isAnyActivated(event);
    String strClient = (String) DalUtil.getId(newAttConfig.getClient());
    if ((Boolean) event.getCurrentState(propActive)) {
      AttachmentUtils.setAttachmentConfig(strClient, newAttConfig.getId());
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final AttachmentConfig deletedAttachmentConfig = (AttachmentConfig) event.getTargetInstance();
    String strClient = (String) DalUtil.getId(deletedAttachmentConfig.getClient());
    if (deletedAttachmentConfig.isActive()) {
      // The active config of the client is deleted. Update AttachmentUtils with an empty config
      AttachmentUtils.setAttachmentConfig(strClient, null);
    }

  }

  private void isAnyActivated(EntityPersistenceEvent event) {
    final AttachmentConfig newAttachmentConfig = (AttachmentConfig) event.getTargetInstance();
    if (!newAttachmentConfig.isActive()) {
      return;
    }
    final OBQuery<AttachmentConfig> attachmentConfigQuery = OBDal.getInstance().createQuery(
        AttachmentConfig.class, "id!=:id");
    attachmentConfigQuery.setNamedParameter("id", newAttachmentConfig.getId());
    // Ensure that filtering by client and active is done.
    attachmentConfigQuery.setFilterOnReadableClients(true);
    attachmentConfigQuery.setFilterOnActive(true);

    if (!attachmentConfigQuery.list().isEmpty()) {
      logger.error("Error saving, more than one active config detected.");
      throw new OBException(OBMessageUtils.messageBD("AD_EnabledAttachmentMethod"));
    }
  }

}
