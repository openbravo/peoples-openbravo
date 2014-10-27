/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.text.ParseException;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.materialmgmt.cost.CostingRule;

public class CostingRuleEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      CostingRule.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity costingRule = ModelProvider.getInstance().getEntity(CostingRule.ENTITY_NAME);
    final Property genericProperty = costingRule
        .getProperty(CostingRule.PROPERTY_BACKDATEDTRANSACTIONSFIXED);
    Boolean isbackdatedtransaction = (Boolean) event.getCurrentState(genericProperty);
    CostingRule rule = (CostingRule) event.getTargetInstance();
    checkFixBackdatedFrom(isbackdatedtransaction, rule);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity costingRule = ModelProvider.getInstance().getEntity(CostingRule.ENTITY_NAME);
    final Property genericProperty = costingRule
        .getProperty(CostingRule.PROPERTY_BACKDATEDTRANSACTIONSFIXED);
    Boolean isbackdatedtransaction = (Boolean) event.getCurrentState(genericProperty);
    CostingRule rule = (CostingRule) event.getTargetInstance();
    checkFixBackdatedFrom(isbackdatedtransaction, rule);
  }

  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity costingRule = ModelProvider.getInstance().getEntity(CostingRule.ENTITY_NAME);
    final Property genericProperty = costingRule
        .getProperty(CostingRule.PROPERTY_BACKDATEDTRANSACTIONSFIXED);
    Boolean isbackdatedtransaction = (Boolean) event.getCurrentState(genericProperty);
    CostingRule rule = (CostingRule) event.getTargetInstance();
    checkFixBackdatedFrom(isbackdatedtransaction, rule);
  }

  private void checkFixBackdatedFrom(Boolean isbackdatedtransaction, CostingRule rule) {
    StringBuilder hql = new StringBuilder();
    final Session session = OBDal.getInstance().getSession();
    hql.append("select min(p.startingDate)  from FinancialMgmtPeriodControl pc"
        + " inner join  pc.period p" + " where  periodstatus='O' " + " and p.client= :client"
        + " and pc.organization= :org ");

    final Query query = session.createQuery(hql.toString());
    query.setParameter("client", rule.getClient());
    query.setParameter("org", rule.getOrganization());
    query.uniqueResult();

    try {
      if (isbackdatedtransaction) {
        if (rule.getFixbackdatedfrom() == null && !(rule.getStartingDate() == null)) {
          rule.setFixbackdatedfrom(rule.getStartingDate());
        }
      }
      if (rule.getFixbackdatedfrom() != null) {
        if (rule.getFixbackdatedfrom().before(
            OBDateUtils.getDate(OBDateUtils.formatDate((Date) query.uniqueResult())))) {
          throw new OBException(OBMessageUtils.messageBD("WrongFixBackdatedFrom"));
        }
      }

    } catch (HibernateException e) {
      logger.error("Error executing process", e);
    } catch (ParseException e) {
      logger.error("Error executing process", e);
    }
  }
}