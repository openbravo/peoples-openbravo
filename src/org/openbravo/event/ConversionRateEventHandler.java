/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2015-2020 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;

class ConversionRateEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ConversionRate.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    // Check if exists another record using this currencyFrom - currencyTo in the same dates
    final ConversionRate conversionRate = (ConversionRate) event.getTargetInstance();
    if (existsRecord(conversionRate.getId(), conversionRate.getClient(),
        conversionRate.getCurrency(), conversionRate.getToCurrency(),
        conversionRate.getValidFromDate(), conversionRate.getValidToDate())) {
      throw new OBException(OBMessageUtils.messageBD("20504"));
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    // Check if exists another record using this currencyFrom - currencyTo in the same dates
    final ConversionRate conversionRate = (ConversionRate) event.getTargetInstance();
    if (existsRecord(conversionRate.getId(), conversionRate.getClient(),
        conversionRate.getCurrency(), conversionRate.getToCurrency(),
        conversionRate.getValidFromDate(), conversionRate.getValidToDate())) {
      throw new OBException(OBMessageUtils.messageBD("20504"));
    }
  }

  // Check if exists another record using this currencyFrom - currencyTo in the same dates
  private boolean existsRecord(String id, Client client, Currency currencyFrom, Currency currencyTo,
      Date validFrom, Date validTo) {
    //@formatter:off
    final String hql = 
                  "select t.id" +
                  "  from CurrencyConversionRate as t" +
                  " where t.id != :currencyConversionRateId" +
                  "   and t.client.id = :clientId" +
                  "   and t.currency.id = :currencyFromId" +
                  "   and t.toCurrency.id = :currencyToId" +
                  "   and" +
                  "     (" +
                  "       (" +
                  "         :validFrom between t.validFromDate and t.validToDate" +
                  "         or :validTo between t.validFromDate and t.validToDate" +
                  "       )" +
                  "       or" +
                  "         (" +
                  "           :validFrom < t.validFromDate" +
                  "           and :validTo > t.validToDate" +
                  "         )" +
                  "     )";
    //@formatter:on

    return !OBDal.getInstance()
        .getSession()
        .createQuery(hql, String.class)
        .setParameter("currencyConversionRateId", id)
        .setParameter("clientId", client.getId())
        .setParameter("currencyFromId", currencyFrom.getId())
        .setParameter("currencyToId", currencyTo.getId())
        .setParameter("validFrom", validFrom)
        .setParameter("validTo", validTo)
        .setMaxResults(1)
        .list()
        .isEmpty();
  }

}
