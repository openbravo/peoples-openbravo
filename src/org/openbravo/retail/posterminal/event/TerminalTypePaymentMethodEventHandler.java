/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
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
import org.openbravo.retail.posterminal.TerminalTypePaymentMethod;
import org.openbravo.service.db.DalConnectionProvider;

public class TerminalTypePaymentMethodEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(TerminalTypePaymentMethod.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

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
    checkIfRoundingPaymentExists(event);
  }

  private void checkNoAutomaticDepositNotInCashup(EntityPersistenceEvent event) {
    TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    if (!ttpm.isCountpaymentincashup() && !ttpm.getPaymentMethod().isAutomaticDeposit()) {
      throw new OBException(
          Utility.messageBD(new DalConnectionProvider(false), "OBPOS_NotAutoPayNotDepositCashup",
              OBContext.getOBContext().getLanguage().getLanguage()));
    }
  }

  private void checkIfCurrencyRoundingExists(EntityPersistenceEvent event) {
    TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    OBContext.setAdminMode(true);
    try {
      if (ttpm.getChangeLessThan() != null && ttpm.getChangePaymentType() != null) {
        String hql = "select cr.id from OBPOS_CurrencyRounding cr "
            + " where cr.currency.id = :currencyId and OBPOS_CurrencyRounding.active = true "
            + " and ad_isorgincluded(:organizationId, cr.organization.id, :clientId) <> -1";
        Query<String> qry = OBDal.getInstance().getSession().createQuery(hql, String.class);
        qry.setParameter("currencyId", ttpm.getCurrency().getId());
        qry.setParameter("organizationId", ttpm.getOrganization().getId());
        qry.setParameter("clientId", ttpm.getClient().getId());
        qry.setMaxResults(1);
        String currencyRoundingId = qry.uniqueResult();
        if (currencyRoundingId != null) {
          throw new OBException(
              String.format(OBMessageUtils.messageBD("OBPOS_ChangeLogicNotAllowed"),
                  ttpm.getCurrency().getISOCode()));
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void checkIfRoundingPaymentExists(EntityPersistenceEvent event) {
    TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    OBContext.setAdminMode(true);
    try {
      if (ttpm.isRounding()) {
        //@formatter:off
        String hql = "select p.name from OBPOS_App_Payment_Type p "
            + " where p.obposTerminaltype.id = :posId "
            + "   and p.isRounding = true ";
        //@formatter:on
        Query<String> query = OBDal.getInstance().getSession().createQuery(hql, String.class);
        query.setParameter("posId", ttpm.getObposTerminaltype().getId());
        query.setMaxResults(1);
        String paymentRoundingName = query.uniqueResult();
        if (paymentRoundingName != null) {
          throw new OBException(String.format(
              OBMessageUtils.messageBD("OBPOS_PaymentRoundingNotAllowed"), paymentRoundingName));
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
