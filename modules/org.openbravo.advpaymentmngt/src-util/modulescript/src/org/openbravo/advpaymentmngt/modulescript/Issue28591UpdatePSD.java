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
package org.openbravo.advpaymentmngt.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import java.math.BigDecimal;

public class Issue28591UpdatePSD extends ModuleScript {
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean issue28591UpdatePSD =  Issue28591UpdatePSDData.updateWrongPSD(cp);
      if(!issue28591UpdatePSD) {
        Issue28591UpdatePSDData[] data = Issue28591UpdatePSDData.selectPSD(cp);
        for (Issue28591UpdatePSDData upsd : data) {
          Issue28591UpdatePSDData.updatePSDAmount(cp, upsd.outstandingamt, upsd.finPaymentScheduledetailId);
        }
        Issue28591UpdatePSDData[] wrongdata = Issue28591UpdatePSDData.selectWrongPSD(cp);
        for (Issue28591UpdatePSDData wpsd : wrongdata) {
          Issue28591UpdatePSDData.updateWrongInvoiceAmt(cp, wpsd.wrongamt, wpsd.cInvoiceId);
          Issue28591UpdatePSDData.updateWrongPSAmt(cp, wpsd.wrongamt, wpsd.finPaymentScheduleId);
          Issue28591UpdatePSDData.updateWrongPSDAmt(cp, wpsd.wrongamt, wpsd.finPaymentScheduledetailId);
          String finPaymentDetailId = Issue28591UpdatePSDData.selectFinPaymentDetailId(cp);
          Issue28591UpdatePSDData.createCredit(cp, finPaymentDetailId, wpsd.finPaymentId, wpsd.wrongamt);
          Issue28591UpdatePSDData.createCreditScheduledetail(cp, finPaymentDetailId, wpsd.wrongamt);
          Issue28591UpdatePSDData.updateCreditGenerated(cp, wpsd.wrongamt, wpsd.finPaymentId);
          Issue28591UpdatePSDData.updateWrongPDAmt(cp, wpsd.wrongamt, wpsd.finPaymentDetailId);          
        }
        Issue28591UpdatePSDData.createPreference(cp);
     }
    } catch (Exception e) {
      handleError(e);
    }
 }
}