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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.retail.posterminal.OBPOSAppPaymentRounding;
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
    checkRoundingGLItemForPaymentRounding(event);
    checkPaymentRoundingCurrency(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkNoAutomaticDepositNotInCashup(event);
    checkIfCurrencyRoundingExists(event);
    checkRoundingGLItemForPaymentRounding(event);
  }

  private void checkNoAutomaticDepositNotInCashup(EntityPersistenceEvent event) {
    final TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    if (!ttpm.isCountpaymentincashup().booleanValue()
        && !ttpm.getPaymentMethod().isAutomaticDeposit().booleanValue()) {
      throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
          ttpm.isRounding().booleanValue() ? "OBPOS_NotAutoPayNotDepositRounding"
              : "OBPOS_NotAutoPayNotDepositCashup",
          OBContext.getOBContext().getLanguage().getLanguage()));
    }
  }

  private void checkIfCurrencyRoundingExists(EntityPersistenceEvent event) {
    final TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
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

  private void checkRoundingGLItemForPaymentRounding(EntityPersistenceEvent event) {
    final TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
    OBContext.setAdminMode(true);
    try {
      if (ttpm.isRounding().booleanValue() && ttpm.getGlitemRound() == null) {
        throw new OBException(OBMessageUtils.messageBD("OBPOS_GLItemForPaymentRoundingRequired"));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void checkPaymentRoundingCurrency(EntityUpdateEvent event) {
    OBContext.setAdminMode(true);
    try {
      final TerminalTypePaymentMethod ttpm = (TerminalTypePaymentMethod) event.getTargetInstance();
      final Entity ttpmEntity = ModelProvider.getInstance()
          .getEntity(TerminalTypePaymentMethod.ENTITY_NAME);
      final Currency previousCurrency = (Currency) event
          .getPreviousState(ttpmEntity.getProperty(TerminalTypePaymentMethod.PROPERTY_CURRENCY));
      final Currency currentCurrency = (Currency) event
          .getCurrentState(ttpmEntity.getProperty(TerminalTypePaymentMethod.PROPERTY_CURRENCY));

      final OBCriteria<OBPOSAppPaymentRounding> relatedPaymentRoundingQuery = OBDal.getInstance()
          .createCriteria(OBPOSAppPaymentRounding.class)
          .add(Restrictions.eq(OBPOSAppPaymentRounding.PROPERTY_OBPOSAPPROUNDINGTYPE + ".id",
              ttpm.getId()))
          .setFilterOnActive(false)
          .setMaxResults(1);

      if (ttpm.isRounding().booleanValue()
          && !StringUtils.equals(previousCurrency.getId(), currentCurrency.getId())
          && relatedPaymentRoundingQuery.uniqueResult() != null) {
        throw new OBException("@OBPOS_PaymentRoundingCurrencyInUse@");
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
