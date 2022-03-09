/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.math.BigDecimal;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * @author eduardo.becerra
 *
 */

public class PreferenceEventHandler extends EntityPersistenceEventObserver {
  private static final String sessionTimeout = "OBPOS_SessionTimeout";
  private static final BigDecimal minValue = new BigDecimal("120");

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(Preference.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Preference preference = (Preference) event.getTargetInstance();
    checkOfflineSessionTimeExpiration(preference);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Preference preference = (Preference) event.getTargetInstance();
    checkOfflineSessionTimeExpiration(preference);
  }

  private void checkOfflineSessionTimeExpiration(Preference preference) {
    if (sessionTimeout.equals(preference.getProperty())) {
      BigDecimal value = new BigDecimal((String) preference.getSearchKey());
      if (value.compareTo(minValue) <= 0) {
        ConnectionProvider conn = new DalConnectionProvider(false);
        String language = OBContext.getOBContext().getLanguage().getLanguage();
        throw new OBException(Utility.messageBD(conn, "OBPOS_InvalidSessionTimeout", language));
      }
    }
  }
}
