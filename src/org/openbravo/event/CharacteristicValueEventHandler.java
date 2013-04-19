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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.client.kernel.event.TransactionBeginEvent;
import org.openbravo.client.kernel.event.TransactionCompletedEvent;
import org.openbravo.materialmgmt.VariantChDescUpdateProcess;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class CharacteristicValueEventHandler extends EntityPersistenceEventObserver {
  protected Logger logger = Logger.getLogger(this.getClass());
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      CharacteristicValue.ENTITY_NAME) };
  private static ThreadLocal<String> chvalueUpdated = new ThreadLocal<String>();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  @SuppressWarnings("unused")
  public void onTransactionBegin(@Observes TransactionBeginEvent event) {
    chvalueUpdated.set(null);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final CharacteristicValue chv = (CharacteristicValue) event.getTargetInstance();
    chvalueUpdated.set(chv.getId());
  }

  public void onTransactionCompleted(@Observes TransactionCompletedEvent event) {
    String strChValueId = chvalueUpdated.get();
    chvalueUpdated.set(null);
    if (StringUtils.isBlank(strChValueId) || event.getTransaction().wasRolledBack()) {
      return;
    }
    try {
      ProcessBundle pb = new ProcessBundle(VariantChDescUpdateProcess.AD_PROCESS_ID, RequestContext
          .get().getVariablesSecureApp()).init(new DalConnectionProvider(false));
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("mProductId", null);
      parameters.put("mChValueId", strChValueId);
      pb.setParams(parameters);
      OBScheduler.getInstance().schedule(pb);
    } catch (Exception e) {
      logger.error("Error executing process", e);
    }
  }
}