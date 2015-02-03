/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSAppPayment;
import org.openbravo.retail.posterminal.OBPOSApplications;

/**
 * @author guillermogil
 * 
 */

public class AppPaymentEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OBPOSAppPayment.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkFinancialAccount(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkFinancialAccount(event);
  }

  private void checkFinancialAccount(EntityPersistenceEvent event) {
    OBPOSAppPayment appPayment = (OBPOSAppPayment) event.getTargetInstance();
    OBCriteria<OBPOSAppPayment> queryAppPayments = OBDal.getInstance().createCriteria(
        OBPOSAppPayment.class);
    queryAppPayments.add(Restrictions.eq(OBPOSAppPayment.PROPERTY_FINANCIALACCOUNT,
        appPayment.getFinancialAccount()));
    if (!appPayment.getPaymentMethod().isShared()) {
      queryAppPayments.setMaxResults(1);
      if (queryAppPayments.list().size() > 0) {
        throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_PaymentMethodNotAllowed",
            new String[] {}));
      }
    } else {
      OBCriteria<OBPOSApplications> queryApplications = OBDal.getInstance().createCriteria(
          OBPOSApplications.class);
      String masterterminal = "";
      if (appPayment.getObposApplications().getMasterterminal() != null) {
        masterterminal = appPayment.getObposApplications().getMasterterminal().getId();
      }
      queryApplications.add(Restrictions.or(Restrictions.eq(OBPOSApplications.PROPERTY_ID,
          masterterminal), Restrictions.or(Restrictions.eq(
          OBPOSApplications.PROPERTY_MASTERTERMINAL, appPayment.getObposApplications()),
          Restrictions.eq(OBPOSApplications.PROPERTY_MASTERTERMINAL, appPayment
              .getObposApplications().getMasterterminal()))));
      List<OBPOSApplications> terminalList = queryApplications.list();

      ScrollableResults scrollableResults = queryAppPayments.scroll(ScrollMode.FORWARD_ONLY);

      if (scrollableResults.next()) {
        OBPOSAppPayment appPaymentResult = (OBPOSAppPayment) scrollableResults.get(0);
        if (appPaymentResult.getPaymentMethod() != appPayment.getPaymentMethod()
            || !terminalList.contains(appPaymentResult.getObposApplications())) {
          throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_PaymentMethodNotAllowed",
              new String[] {}));
        }
      }
    }
  }
}