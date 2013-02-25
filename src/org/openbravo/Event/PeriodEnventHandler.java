package org.openbravo.Event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.service.db.DalConnectionProvider;

public class PeriodEnventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Period.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    if (!adjustmentPeriod((Period) event.getTargetInstance())) {
      checkPeriod((Period) event.getTargetInstance());
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    if (!adjustmentPeriod((Period) event.getTargetInstance())) {
      checkPeriod((Period) event.getTargetInstance());
    }
  }

  private boolean adjustmentPeriod(Period period) {
    if ("A".equals(period.getPeriodType())) {
      return true;
    } else {
      return false;
    }
  }

  private void checkPeriod(Period period) {
    ConnectionProvider conn = new DalConnectionProvider(false);
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    OBCriteria<Period> criteria = OBDal.getInstance().createCriteria(Period.class);
    criteria.add(Restrictions.eq(Period.PROPERTY_ORGANIZATION, period.getOrganization()));
    criteria.add(Restrictions.eq(Period.PROPERTY_CLIENT, period.getClient()));
    criteria.add(Restrictions.ne(Period.PROPERTY_ID, period.getId()));
    criteria.add(Restrictions.ge(Period.PROPERTY_ENDINGDATE, period.getStartingDate()));
    criteria.add(Restrictions.le(Period.PROPERTY_STARTINGDATE, period.getEndingDate()));
    criteria.add(Restrictions.eq(Period.PROPERTY_PERIODTYPE, "S"));
    criteria.setMaxResults(1);

    if (criteria.uniqueResult() != null) {
      logger
          .error("Period " + period.getId() + " is being saved and is overlapping another period");
      throw new OBException(Utility.messageBD(conn, "DatesOverlappedParams", language)
          .replace("%1", ((Period) criteria.uniqueResult()).getName())
          .replace("%2", ((Period) criteria.uniqueResult()).getYear().getFiscalYear()));
    }
  }
}