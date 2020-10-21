/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSSafeBoxPaymentMethod;

public class SafeBoxPaymentMethodEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(OBPOSSafeBoxPaymentMethod.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final OBPOSSafeBoxPaymentMethod safeBoxPaymentMethod = (OBPOSSafeBoxPaymentMethod) event
        .getTargetInstance();

    checkUniquePaymentMethod(safeBoxPaymentMethod, false);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final OBPOSSafeBoxPaymentMethod safeBoxPaymentMethod = (OBPOSSafeBoxPaymentMethod) event
        .getTargetInstance();
    checkUniquePaymentMethod(safeBoxPaymentMethod, true);
  }

  private void checkUniquePaymentMethod(final OBPOSSafeBoxPaymentMethod safeBoxPaymentMethod,
      final boolean isNew) {
    String whereClause = " as e where e.obposSafebox.id = :safebox and e.paymentMethod.id = :paymentMethod ";
    whereClause += " and e.fINFinancialaccount.currency.id = :currency ";
    if (!isNew) {
      whereClause += " and e.id != :safeBoxPaymentMethod ";
    }
    OBQuery<OBPOSSafeBoxPaymentMethod> query = OBDal.getInstance()
        .createQuery(OBPOSSafeBoxPaymentMethod.class, whereClause);
    query.setFilterOnActive(false);
    query.setNamedParameter("safebox", safeBoxPaymentMethod.getObposSafebox().getId());
    query.setNamedParameter("paymentMethod", safeBoxPaymentMethod.getPaymentMethod().getId());
    query.setNamedParameter("currency",
        safeBoxPaymentMethod.getFINFinancialaccount().getCurrency().getId());
    if (!isNew) {
      query.setNamedParameter("safeBoxPaymentMethod", safeBoxPaymentMethod.getId());
    }
    OBPOSSafeBoxPaymentMethod obposSafeBoxPaymentMethod = query.uniqueResult();
    if (obposSafeBoxPaymentMethod != null) {
      throw new OBException(OBMessageUtils.messageBD("OBPOS_SafeBoxPaymentMethodUnique"));
    }
  }

}
