/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.security;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.retail.posterminal.OBPOSApplications;

/**
 * Event handler observing POS Terminal entity for new rows and activation of inactive records in
 * order to guarantee license limit of allowed terminals is not exceeded.
 * 
 * @author alostale
 *
 */
public class TerminalRestrictionsEventHandler extends EntityPersistenceEventObserver {
  private static final Entity terminalEntity = ModelProvider.getInstance().getEntity(
      OBPOSApplications.ENTITY_NAME);
  private static final Entity[] entities = { terminalEntity };
  private static final Property activeProperty = terminalEntity
      .getProperty(OBPOSApplications.PROPERTY_ACTIVE);

  @Inject
  POSLicenseRestrictions licenseRestrictions;

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  /** Checks it is allowed to create a new terminal */
  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    licenseRestrictions.resetNumberOfTerminals();
    licenseRestrictions.checkRestrictionForNewTerminal();
  }

  /** Checks it is allowed to activate an inactive terminal */
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    licenseRestrictions.resetNumberOfTerminals();
    Boolean wasActive = (Boolean) event.getPreviousState(activeProperty);
    Boolean isActive = (Boolean) event.getCurrentState(activeProperty);
    if (!wasActive && isActive) {
      licenseRestrictions.checkRestrictionForNewTerminal();
    }
  }

  /** Checks it is allowed to create a new terminal */
  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    licenseRestrictions.resetNumberOfTerminals();
  }
}
