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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.PaymentPriority;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.scheduling.ProcessBundle;

public class UpdatePaymentPlanProcess implements org.openbravo.scheduling.Process {

  private static final Logger log = Logger.getLogger(UpdatePaymentPlanProcess.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBContext.setAdminMode(true);
    try {
      final String strPaymentScheduleInvId = (String) bundle.getParams().get(
          "Fin_Payment_Sched_Inv_V_ID");
      final String strPaymentScheduleOrdId = (String) bundle.getParams().get(
          "Fin_Payment_Sched_Ord_V_ID");
      final String strPaymentPriority = (String) bundle.getParams().get("emAprmPaymentPriority");
      final String strDueDate = (String) bundle.getParams().get("duedate");

      final String strPaymentScheduleId = (strPaymentScheduleOrdId == null) ? strPaymentScheduleInvId
          : strPaymentScheduleOrdId;

      FIN_PaymentSchedule ps = OBDal.getInstance().get(FIN_PaymentSchedule.class,
          strPaymentScheduleId);
      ps.setDueDate(FIN_Utility.getDate(strDueDate));
      ps.setAPRMPaymentPriority(OBDal.getInstance().get(PaymentPriority.class, strPaymentPriority));
      OBDal.getInstance().save(ps);
      OBDal.getInstance().flush();

      final OBError msg = new OBError();
      msg.setTitle(FIN_Utility.messageBD("Success"));
      msg.setType("Success");
      bundle.setResult(msg);

    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Done with Errors");
      bundle.setResult(msg);
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
