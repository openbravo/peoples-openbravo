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
package org.openbravo.role.inheritance;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;

/**
 * Listens to delete, update and save events for all classes implementing the
 * {@link InheritedAccessEnabled} interface. This handler takes care of propagating the changes
 * according to the affected role inheritance settings.
 */
public class InheritedAccessEnabledEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {};

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isInheritedAccessEnabled(event)) {
      return;
    }

    final BaseOBObject bob = event.getTargetInstance();
    String entityClassName = ModelProvider.getInstance().getEntity(bob.getEntity().getName())
        .getClassName();
    RoleInheritanceManager manager = WeldUtils
        .getInstanceFromStaticBeanManager(RoleInheritanceManager.class);
    final InheritedAccessEnabled access = (InheritedAccessEnabled) bob;
    final Role role = manager.getRole(access, entityClassName);
    if (role != null && role.isTemplate()) {
      // Propagate new access just for roles marked as template
      manager.propagateNewAccess(role, access, entityClassName);
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isInheritedAccessEnabled(event)) {
      return;
    }

    final BaseOBObject bob = event.getTargetInstance();
    String entityClassName = ModelProvider.getInstance().getEntity(bob.getEntity().getName())
        .getClassName();
    RoleInheritanceManager manager = WeldUtils
        .getInstanceFromStaticBeanManager(RoleInheritanceManager.class);
    final InheritedAccessEnabled access = (InheritedAccessEnabled) bob;
    final Role role = manager.getRole(access, entityClassName);
    if (role != null && role.isTemplate()) {
      // Propagate updated access just for roles marked as template
      manager.propagateUpdatedAccess(role, access, entityClassName);
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isInheritedAccessEnabled(event)) {
      return;
    }

    final BaseOBObject bob = event.getTargetInstance();
    String entityClassName = ModelProvider.getInstance().getEntity(bob.getEntity().getName())
        .getClassName();
    RoleInheritanceManager manager = WeldUtils
        .getInstanceFromStaticBeanManager(RoleInheritanceManager.class);
    final InheritedAccessEnabled access = (InheritedAccessEnabled) bob;
    final Role role = manager.getRole(access, entityClassName);
    if (notDeletingParent(role, access)) {
      if (access.getInheritedFrom() != null) {
        Utility.throwErrorMessage("NotDeleteInheritedAccess");
      }
      if (role != null && role.isTemplate()) {
        // Propagate access removal just for roles marked as template
        manager.propagateDeletedAccess(role, access, entityClassName);
      }
    }
  }

  private boolean isInheritedAccessEnabled(EntityPersistenceEvent event) {
    // Disable event handlers if data is being imported
    if (TriggerHandler.getInstance().isDisabled()) {
      return false;
    }
    if (event.getTargetInstance() instanceof InheritedAccessEnabled) {
      return true;
    } else {
      return false;
    }
  }

  private boolean notDeletingParent(Role role, InheritedAccessEnabled access) {
    if (role == null) {
      return true;
    }
    return OBDal.getInstance().exists(Role.ENTITY_NAME, (String) DalUtil.getId(role));
  }
}
