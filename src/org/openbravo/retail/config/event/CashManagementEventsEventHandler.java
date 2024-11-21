/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
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
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.retail.config.CashManagementEvents;

public class CashManagementEventsEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(CashManagementEvents.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkFinancialAccount((FIN_FinancialAccount) event.getTargetInstance().get("financialAccount"),
        (String) event.getTargetInstance().get("eventtype"));
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkFinancialAccount((FIN_FinancialAccount) event.getTargetInstance().get("financialAccount"),
        (String) event.getTargetInstance().get("eventtype"));
  }

  private void checkFinancialAccount(FIN_FinancialAccount financialAccount, String eventType) {

    if (financialAccount == null && (eventType.equals("IN") || eventType.equals("OUT"))) {
      throw new OBException(OBMessageUtils.messageBD("OBRETCO_FinancialAccEventType"));

    }
  }

}
