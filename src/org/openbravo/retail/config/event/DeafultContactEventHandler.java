/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.event;

import javax.enterprise.event.Observes;

import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;

public class DeafultContactEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(User.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    User usr = (User) event.getTargetInstance();
    if (usr.getBusinessPartner() != null) {
      if (usr.isOBRETCODefaultPosContact()) {
        resetOldDefaultContact((User) event.getTargetInstance());
      } else {
        validateDefaultContactExists((User) event.getTargetInstance());
      }
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Property defaultContactProperty = entities[0]
        .getProperty(User.PROPERTY_OBRETCODEFAULTPOSCONTACT);

    if (event.getPreviousState(defaultContactProperty)
        .equals(event.getCurrentState(defaultContactProperty))) {
      return;
    }

    User usr = (User) event.getTargetInstance();
    if (usr.getBusinessPartner() != null) {
      if (usr.isOBRETCODefaultPosContact()) {
        resetOldDefaultContact((User) event.getTargetInstance());
      } else {
        validateDefaultContactExists((User) event.getTargetInstance());
      }
    }
  }

  private void validateDefaultContactExists(User usr) {
    //@formatter:off
    String hql =
            "SELECT u.id " +
            "FROM ADUser u " +
            "WHERE u.oBRETCODefaultPosContact = true " +
            "AND u.id <> :userId " +
            "AND u.businessPartner.id = :bPartnerId";
    //@formatter:on

    Query<String> statusQuery = OBDal.getInstance().getSession().createQuery(hql, String.class);
    statusQuery.setParameter("userId", usr.getId());
    statusQuery.setParameter("bPartnerId", usr.getBusinessPartner().getId());
    statusQuery.setMaxResults(1);
    if (statusQuery.uniqueResult() == null) {
      throw new OBException(
          OBMessageUtils.getI18NMessage("OBRETCO_ContactDefaultMustExists", null));
    }
  }

  private int resetOldDefaultContact(User usr) {
    //@formatter:off
    String hql =
            "UPDATE ADUser " +
            "SET oBRETCODefaultPosContact = 'N' " +
            "WHERE id <> :userId " +
            "AND businessPartner.id = :bPartnerId " +
            "AND oBRETCODefaultPosContact = 'Y' ";
    //@formatter:on

    return OBDal.getInstance()
        .getSession()
        .createQuery(hql)
        .setParameter("userId", usr.getId())
        .setParameter("bPartnerId", usr.getBusinessPartner().getId())
        .executeUpdate();
  }
}
