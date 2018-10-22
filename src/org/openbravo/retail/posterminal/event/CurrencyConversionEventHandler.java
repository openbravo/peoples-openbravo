/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
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
import org.openbravo.retail.posterminal.OBPOSCurrencyRounding;
import org.openbravo.retail.posterminal.TerminalTypePaymentMethod;

public class CurrencyConversionEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OBPOSCurrencyRounding.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkIfAnyChangeLogicInTerminalTypePaymentMethodExistsForCurrency(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkIfAnyChangeLogicInTerminalTypePaymentMethodExistsForCurrency(event);
  }

  private void checkIfAnyChangeLogicInTerminalTypePaymentMethodExistsForCurrency(
      EntityPersistenceEvent event) {
    OBPOSCurrencyRounding currencyRounding = (OBPOSCurrencyRounding) event.getTargetInstance();
    OBContext.setAdminMode(true);
    try {
      StringBuilder hql = new StringBuilder();
      hql.append("select " + TerminalTypePaymentMethod.PROPERTY_ID);
      hql.append(" from " + TerminalTypePaymentMethod.ENTITY_NAME + " ttpm");
      hql.append(" where " + TerminalTypePaymentMethod.PROPERTY_CURRENCY + ".id = :currencyId");
      hql.append(" and " + TerminalTypePaymentMethod.PROPERTY_ACTIVE + " = true");
      hql.append(" and " + TerminalTypePaymentMethod.PROPERTY_CHANGEPAYMENTTYPE + " is not null");
      hql.append(" and " + TerminalTypePaymentMethod.PROPERTY_CHANGEPAYMENTTYPE + " is not null");
      hql.append(" and ad_isorgincluded(ttpm.organization.id, :organizationId, :clientId) <> -1");
      Query<String> qry = OBDal.getInstance().getSession()
          .createQuery(hql.toString(), String.class);
      qry.setParameter("currencyId", currencyRounding.getCurrency().getId());
      qry.setParameter("organizationId", currencyRounding.getOrganization().getId());
      qry.setParameter("clientId", currencyRounding.getClient().getId());
      qry.setMaxResults(1);
      String currencyRoundingId = (String) qry.uniqueResult();
      if (currencyRoundingId != null) {
        throw new OBException(String.format(OBMessageUtils
            .messageBD("OBPOS_CurrencyRoundingNotAllowed"), currencyRounding.getCurrency()
            .getISOCode()));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
