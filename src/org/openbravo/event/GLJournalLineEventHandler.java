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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;

public class GLJournalLineEventHandler extends EntityPersistenceEventObserver {
  protected Logger logger = Logger.getLogger(this.getClass());
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      GLJournalLine.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final GLJournalLine journalLine = (GLJournalLine) event.getTargetInstance();
    final Entity gljournalLine = ModelProvider.getInstance().getEntity(GLJournalLine.ENTITY_NAME);
    if (!journalLine.getJournalEntry().isProcessed() && journalLine.getRelatedPayment() != null) {
      // If you want to modify the Credit/Debit/Financial Account/Payment Method/GL Item/Payment
      // Date of any of these lines, you must first reactivate and delete its related payments.
      final Property credit = gljournalLine.getProperty(GLJournalLine.PROPERTY_CREDIT);
      final Property debit = gljournalLine.getProperty(GLJournalLine.PROPERTY_DEBIT);
      final Property openItems = gljournalLine.getProperty(GLJournalLine.PROPERTY_OPENITEMS);
      if (!event.getCurrentState(credit).equals(event.getPreviousState(credit))
          || !event.getCurrentState(debit).equals(event.getPreviousState(debit))
          || !event.getCurrentState(openItems).equals(event.getPreviousState(openItems))) {
        logger.info(event.getCurrentState(credit) + "...." + event.getPreviousState(credit));
        throw new OBException("@ModifyGLJournalLine@");
      }
    }
  }
}
