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

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.service.db.CallProcess;
import org.openbravo.synchronization.event.SynchronizationEvent;

public class RequisitionProcessor {
  private static final String PROCESS_M_REQUISITION_POST = "1004400003";

  public void processRequisition(String requisitionId) throws OBException {
    try {
      Process process = OBDal.getInstance().get(Process.class, PROCESS_M_REQUISITION_POST);
      final ProcessInstance pinstance = CallProcess.getInstance()
          .call(process, requisitionId, null);

      final OBError result = OBMessageUtils.getProcessInstanceMessage(pinstance);
      if (StringUtils.equals("Error", result.getType())) {
        throw new OBException(
            OBMessageUtils.messageBD("ErrorProcessingRequisition") + ": " + result.getMessage());
      } else {
        OBDal.getInstance().flush();
      }

      // Trigger Push API event for the requisition
      triggerPushEvent(requisitionId);
    } catch (Exception e) {
      throw new OBException("Error processing requisition", e);
    }
  }

  public void triggerPushEvent(String requisitionId) throws OBException {
    try {
      SynchronizationEvent.getInstance().triggerEvent("API_Process_Requisition", requisitionId);
    } catch (Exception e) {
      throw new OBException("Error triggering Push API event for requisition", e);
    }
  }
}
