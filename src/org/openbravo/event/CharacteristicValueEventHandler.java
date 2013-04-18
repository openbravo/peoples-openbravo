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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.util.HashMap;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class CharacteristicValueEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      CharacteristicValue.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    System.out.println("entra al event handler");
    final CharacteristicValue chv = (CharacteristicValue) event.getTargetInstance();
    try {
      ProcessBundle pb = new ProcessBundle("58591E3E0F7648E4A09058E037CE49FC", RequestContext.get()
          .getVariablesSecureApp()).init(new DalConnectionProvider(false));
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("mProductId", null);
      parameters.put("mChValueId", chv.getId());
      parameters.put("chValueName", chv.getName());
      pb.setParams(parameters);
      OBScheduler.getInstance().schedule(pb);
    } catch (Exception e) {
      logger.error("Error executing process", e);
    }
  }

}