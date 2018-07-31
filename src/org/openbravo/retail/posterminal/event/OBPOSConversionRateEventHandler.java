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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSConversionRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBPOSConversionRateEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OBPOSConversionRate.ENTITY_NAME) };

  protected Logger logger = LoggerFactory.getLogger(OBPOSConversionRateEventHandler.class);

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    throwIfExistsRecord((OBPOSConversionRate) event.getTargetInstance());
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    throwIfExistsRecord((OBPOSConversionRate) event.getTargetInstance());
  }

  private void throwIfExistsRecord(final OBPOSConversionRate conversionRate) {
    // Check if exists another record using this currencyFrom - currencyTo in the same dates
    if (existsRecord(conversionRate.getId(), conversionRate.getClient(),
        conversionRate.getOrganization(), conversionRate.getCurrency(),
        conversionRate.getToCurrency(), conversionRate.getValidFromDate(),
        conversionRate.getValidToDate())) {
      throw new OBException(OBMessageUtils.messageBD("20504"));
    }
  }

  // Check if exists another record using this currencyFrom - currencyTo in the same dates
  private boolean existsRecord(String id, Client client, Organization organization,
      Currency currencyFrom, Currency currencyTo, Date validFrom, Date validTo) {
    StringBuilder hql = new StringBuilder();
    hql.append(" SELECT t." + OBPOSConversionRate.PROPERTY_ID);
    hql.append(" FROM " + OBPOSConversionRate.ENTITY_NAME + " as t");
    hql.append(" WHERE :id != t. " + OBPOSConversionRate.PROPERTY_ID);
    hql.append(" AND :client = t. " + OBPOSConversionRate.PROPERTY_CLIENT);
    hql.append(" AND :organization = t. " + OBPOSConversionRate.PROPERTY_ORGANIZATION);
    hql.append(" AND :currencyFrom = t. " + OBPOSConversionRate.PROPERTY_CURRENCY);
    hql.append(" AND :currencyTo = t. " + OBPOSConversionRate.PROPERTY_TOCURRENCY);
    hql.append(" AND ((:validFrom between t." + OBPOSConversionRate.PROPERTY_VALIDFROMDATE
        + " AND t." + OBPOSConversionRate.PROPERTY_VALIDTODATE);
    hql.append(" OR :validTo between t." + OBPOSConversionRate.PROPERTY_VALIDFROMDATE + " AND t."
        + OBPOSConversionRate.PROPERTY_VALIDTODATE + ")");
    hql.append(" OR (:validFrom < t." + OBPOSConversionRate.PROPERTY_VALIDFROMDATE
        + " AND :validTo > t." + OBPOSConversionRate.PROPERTY_VALIDTODATE + "))");

    final Query<Object> query = OBDal.getInstance().getSession()
        .createQuery(hql.toString(), Object.class);
    query.setParameter("id", id);
    query.setParameter("client", client);
    query.setParameter("organization", organization);
    query.setParameter("currencyFrom", currencyFrom);
    query.setParameter("currencyTo", currencyTo);
    query.setParameter("validFrom", validFrom);
    query.setParameter("validTo", validTo);
    query.setMaxResults(1);

    return (query.uniqueResult() != null);
  }

}