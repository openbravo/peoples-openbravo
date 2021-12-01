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
 * All portions are Copyright (C) 2021 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;

/**
 * This event handler ensures that a Process definition can only have one file upload reference
 */
public class FileUploadProcessDefinitionEventHandler extends EntityPersistenceEventObserver {
  private static final String FILE_UPLOAD_REFERENCE_ID = "715C53D4FEA74B28B74F14AE65BC5C16";
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(Parameter.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  /**
   * Check that after the update the related ProcessDefinition only have one single
   */
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    long fileUploadParameters = this.getNumberOfFileUploadParametersInProcess(event);

    if (fileUploadParameters > 1) {
      throw new OBException(
          OBMessageUtils.getI18NMessage("OBUIAPP_FileUploadReferenceAlreadyPresent"));
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    long fileUploadParameters = this.getNumberOfFileUploadParametersInProcess(event);
    Parameter instanceParameter = (Parameter) event.getTargetInstance();

    if (fileUploadParameters == 1
        && instanceParameter.getReference().getId().equals(FILE_UPLOAD_REFERENCE_ID)) {
      throw new OBException(
          OBMessageUtils.getI18NMessage("OBUIAPP_FileUploadReferenceAlreadyPresent"));
    }
  }

  private long getNumberOfFileUploadParametersInProcess(EntityPersistenceEvent event) {
    Parameter instanceParameter = (Parameter) event.getTargetInstance();

    return instanceParameter.getObuiappProcess()
        .getOBUIAPPParameterList()
        .stream()
        .filter(parameter -> parameter.getReference().getId().equals(FILE_UPLOAD_REFERENCE_ID))
        .count();
  }
}
