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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.service.db.DalConnectionProvider;

/*
 * This class validates that Sequence cannot be null in the Handling Unit Type if the 
 * sequence type is set as Global and if sequence type is not Global then Sequence is automatically as null.
 */

class HUTypeEventHandler extends EntityPersistenceEventObserver {
  private static final String GLOBAL_SEQUENCE_TYPE = "Global";
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ReferencedInventoryType.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateSequenceType(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateSequenceType(event);
  }

  private void validateSequenceType(EntityPersistenceEvent event) {
    ConnectionProvider conn = new DalConnectionProvider(false);
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    final Entity hUType = ModelProvider.getInstance()
        .getEntity(ReferencedInventoryType.ENTITY_NAME);
    final Property sequenceType = hUType.getProperty(ReferencedInventoryType.PROPERTY_SEQUENCETYPE);
    final Property sequence = hUType.getProperty(ReferencedInventoryType.PROPERTY_SEQUENCE);

    String newSequenceType = (String) event.getCurrentState(sequenceType);
    Sequence newSequence = (Sequence) event.getCurrentState(sequence);
    if (StringUtils.equals(newSequenceType, GLOBAL_SEQUENCE_TYPE) && newSequence == null) {
      throw new OBException(Utility.messageBD(conn, "SequenceMandatoryForGlobalSeqType", language));
    }
    if (!StringUtils.equals(newSequenceType, "Global") && newSequence != null) {
      event.setCurrentState(sequence, null);
    }
  }

}
