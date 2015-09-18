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

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.client.application.ViewRoleAccess;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.client.myob.WidgetClassAccess;
import org.openbravo.model.ad.access.FieldAccess;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.TabAccess;
import org.openbravo.model.ad.access.TableAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.roleInheritance.RoleInheritanceManager;
import org.openbravo.roleInheritance.RoleInheritanceManager.AccessType;

public class InheritedAccessEnabledEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(RoleOrganization.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(WindowAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(TabAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(FieldAccess.ENTITY_NAME),
      ModelProvider.getInstance()
          .getEntity(org.openbravo.model.ad.access.ProcessAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(FormAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(WidgetClassAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(ViewRoleAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(
          org.openbravo.client.application.ProcessAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(TableAccess.ENTITY_NAME),
      ModelProvider.getInstance().getEntity(AlertRecipient.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final BaseOBObject bob = event.getTargetInstance();
    final InheritedAccessEnabled access = (InheritedAccessEnabled) bob;
    final String entityName = bob.getEntity().getName();
    final AccessType accessType = AccessType.getAccessType(entityName);
    final Role role = accessType.getRole(access);
    if (role.isTemplate()) { // Propagate permissions just for roles marked as template
      RoleInheritanceManager.propagateNewAccess(role, access, accessType);
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final BaseOBObject bob = event.getTargetInstance();
    final InheritedAccessEnabled access = (InheritedAccessEnabled) bob;
    final String entityName = bob.getEntity().getName();
    final AccessType accessType = AccessType.getAccessType(entityName);
    final Role role = accessType.getRole(access);
    if (role.isTemplate()) { // Propagate permissions just for roles marked as template
      RoleInheritanceManager.propagateUpdatedAccess(role, access, accessType);
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
  }
}
