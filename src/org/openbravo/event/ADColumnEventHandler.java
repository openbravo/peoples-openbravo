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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.domain.Preference;

/**
 * The purpose of the event handler is to ensure that only computed {@link Column} can use the
 * Organization DateTime {@link Preference}, i.e. only those columns that use
 * {@link Column#PROPERTY_SQLLOGIC}.
 * 
 * @author Eugen Hamuraru
 *
 */
public class ADColumnEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Column.ENTITY_NAME) };
  private static final String ORG_DATE_TIME_REFERENCE = "F8428F177B6146D3A13C4830FB87DE49";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  /**
   * Check when a new Column is saved, if the {@link Column#PROPERTY_SQLLOGIC} is not defined then
   * the Organization DateTime could not be used
   *
   * @param event
   *          the event for a entity saved for first time.
   */
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Column column = (Column) event.getTargetInstance();
    checkReference(column);
  }

  /**
   * Check when a new Column is updated, if the {@link Column#PROPERTY_SQLLOGIC} is not defined then
   * the Organization DateTime could not be used
   *
   * @param event
   *          the event for a updated entity.
   */
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Column column = (Column) event.getTargetInstance();
    checkReference(column);
  }

  /**
   * @param event
   *          the event for a updated entity.
   * 
   * @throws OBException
   *           exception if the given Column {@link Column#PROPERTY_SQLLOGIC} property is not
   *           defined and the Organization DateTime preference is set.
   */
  private void checkReference(Column column) {
    if (column.getSqllogic() == null
        && ORG_DATE_TIME_REFERENCE.equals(column.getReference().getId())) {
      String[] messageParam = { column.getReference().getName() };
      throw new OBException(OBMessageUtils.getI18NMessage("ComputedColumnRequired", messageParam));
    }
  }

}
