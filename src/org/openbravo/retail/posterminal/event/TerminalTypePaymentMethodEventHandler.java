/*
 ************************************************************************************
 * Copyright (C) 2017-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.retail.posterminal.OBPOSCurrencyRounding;
import org.openbravo.retail.posterminal.TerminalTypePaymentMethod;
import org.openbravo.service.db.DalConnectionProvider;

public class TerminalTypePaymentMethodEventHandler extends EntityPersistenceEventObserver {

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
    checkNoAutomaticDepositNotInCashup(event);
    checkIfCurrencyRoundingExists(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkNoAutomaticDepositNotInCashup(event);
    checkIfCurrencyRoundingExists(event);
  }

  private void checkNoAutomaticDepositNotInCashup(EntityPersistenceEvent event) {
    TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    if (!ttpm.isCountpaymentincashup() && !ttpm.getPaymentMethod().isAutomaticDeposit()) {
      throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
          "OBPOS_NotAutoPayNotDepositCashup", OBContext.getOBContext().getLanguage().getLanguage()));
    }
  }

  private void checkIfCurrencyRoundingExists(EntityPersistenceEvent event) {
    TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    OBContext.setAdminMode(true);
    try {
      if (ttpm.getChangeLessThan() != null && ttpm.getChangePaymentType() != null) {
        StringBuilder hql = new StringBuilder();
        hql.append("select " + OBPOSCurrencyRounding.PROPERTY_ID);
        hql.append(" from " + OBPOSCurrencyRounding.ENTITY_NAME + " cr");
        hql.append(" where " + OBPOSCurrencyRounding.PROPERTY_CURRENCY + ".id = :currencyId");
        hql.append(" and " + OBPOSCurrencyRounding.PROPERTY_ACTIVE + " = true");
        hql.append(" and ad_isorgincluded(:organizationId, cr.organization.id, :clientId) <> -1");
        Query qry = OBDal.getInstance().getSession().createQuery(hql.toString());
        qry.setParameter("currencyId", ttpm.getCurrency().getId());
        qry.setParameter("organizationId", ttpm.getOrganization().getId());
        qry.setParameter("clientId", ttpm.getClient().getId());
        qry.setMaxResults(1);
        String currencyRoundingId = (String) qry.uniqueResult();
        if (currencyRoundingId != null) {
          throw new OBException(String.format(OBMessageUtils
              .messageBD("OBPOS_ChangeLogicNotAllowed"), ttpm.getCurrency().getISOCode()));
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}