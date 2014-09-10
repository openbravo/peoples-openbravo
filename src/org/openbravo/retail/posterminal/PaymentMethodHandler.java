/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

public class PaymentMethodHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      TerminalTypePaymentMethod.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkOnlyOneDefaultCashMethod((TerminalTypePaymentMethod) event.getTargetInstance());
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkOnlyOneDefaultCashMethod((TerminalTypePaymentMethod) event.getTargetInstance());
  }

  private void checkOnlyOneDefaultCashMethod(TerminalTypePaymentMethod newPaymentMethod) {
    if (!newPaymentMethod.isDefaultCashPaymentMethod()) {
      // no need to check, get away
      return;
    }
    for (TerminalTypePaymentMethod paymentMethod : newPaymentMethod.getObposTerminaltype()
        .getOBPOSAppPaymentTypeList()) {
      if (!paymentMethod.isActive() || !paymentMethod.isDefaultCashPaymentMethod()) {
        // no need to check
        continue;
      }
      if (newPaymentMethod.getId() == null
          || !newPaymentMethod.getId().equals(paymentMethod.getId())) {
        String language = OBContext.getOBContext().getLanguage().getLanguage();
        ConnectionProvider conn = new DalConnectionProvider(false);
        throw new OBException(Utility.messageBD(conn, "OBPOS_OneDefaultCashAllowed", language));
      }
    }
  }
}
