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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.authentication;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

/**
 * Automatically deactivates the sessions of users that are being locked
 */
class DeactivateSessionOnUserLockEventHandler extends EntityPersistenceEventObserver {

  private static final Logger log = LogManager.getLogger();

  static private Entity userEntity = ModelProvider.getInstance().getEntity(User.ENTITY_NAME);
  static private Property lockedProperty = userEntity.getProperty(User.PROPERTY_LOCKED);

  private static final Entity[] ENTITIES = { userEntity };

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    User user = (User) event.getTargetInstance();
    if (justTransitionedToLocked(event, user)) {
      deactivateUserSessions(user);
    }
  }

  private void deactivateUserSessions(User user) {
    //@formatter:off
    String updateSql = " UPDATE AD_Session "
                     + " SET session_active = 'N' "
                     + " WHERE username = :username and session_active = 'Y'";
    //@formatter:on
    int updates = OBDal.getInstance()
        .getSession()
        .createNativeQuery(updateSql)
        .setParameter("username", user.getUsername())
        .executeUpdate();
    log.debug("{} session(s) were deactivated after locking user {}", updates, user.getUsername());
  }

  private boolean justTransitionedToLocked(EntityUpdateEvent event, User user) {
    return user.isLocked() && !((boolean) event.getPreviousState(lockedProperty));
  }

}
