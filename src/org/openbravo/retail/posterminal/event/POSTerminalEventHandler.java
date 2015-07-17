/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.retail.posterminal.OBPOSApplications;

/**
 * @author guillermogil
 * 
 */

public class POSTerminalEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OBPOSApplications.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    OBPOSApplications posTerminal = (OBPOSApplications) event.getTargetInstance();
    OBPOSApplications posTerminalMaster = posTerminal.getMasterterminal();
    boolean isMaster = posTerminal.isMaster();
    if (isMaster && posTerminalMaster != null) {
      final Entity masterterminalEntity = ModelProvider.getInstance().getEntity(
          OBPOSApplications.ENTITY_NAME);
      final Property masterterminalProperty = masterterminalEntity.getProperty("masterterminal");
      event.setCurrentState(masterterminalProperty, null);
    }

  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    OBPOSApplications posTerminal = (OBPOSApplications) event.getTargetInstance();
    OBPOSApplications posTerminalMaster = posTerminal.getMasterterminal();
    boolean isMaster = posTerminal.isMaster();
    if (isMaster && posTerminalMaster != null) {
      final Entity masterterminalEntity = ModelProvider.getInstance().getEntity(
          OBPOSApplications.ENTITY_NAME);
      final Property masterterminalProperty = masterterminalEntity.getProperty("masterterminal");
      event.setCurrentState(masterterminalProperty, null);
    }

  }
}