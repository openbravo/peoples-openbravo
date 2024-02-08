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
package org.openbravo.materialmgmt;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.service.db.CallProcess;
import org.openbravo.synchronization.event.SynchronizationEvent;

/*
 * Process the  transaction document Status of the Requisition.
 */
public class RequisitionProcessor {
  private static final String PROCESS_M_REQUISITION_POST = "1004400003";

  public ProcessInstance processRequisition(String strM_Requisition_ID, String strdocaction)
      throws OBException {
    try {

      final Requisition requisition = (Requisition) OBDal.getInstance()
          .getProxy(Requisition.ENTITY_NAME, strM_Requisition_ID);
      requisition.setDocumentAction(strdocaction);
      OBDal.getInstance().save(requisition);
      OBDal.getInstance().flush();

      final ProcessInstance pinstance = CallProcess.getInstance()
          .call(getProcessRequisition(), strM_Requisition_ID, null);

      OBDal.getInstance().commitAndClose();

      triggerPushEvent(strM_Requisition_ID);
      return pinstance;

    } catch (Exception e) {
      throw new OBException("Error processing requisition", e);
    }

  }

  private Process getProcessRequisition() {
    Process process = null;
    try {
      OBContext.setAdminMode(true);
      process = (Process) OBDal.getInstance()
          .getProxy(Process.ENTITY_NAME, PROCESS_M_REQUISITION_POST);
    } finally {
      OBContext.restorePreviousMode();
    }
    return process;
  }

  public void triggerPushEvent(String requisitionId) throws OBException {
    try {
      SynchronizationEvent.getInstance().triggerEvent("API_Process_Requisition", requisitionId);
    } catch (Exception e) {
      throw new OBException("Error triggering Push API event for requisition", e);
    }
  }
}
