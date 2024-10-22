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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  
 *************************************************************************
 */
package org.openbravo.advpaymentmngt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class ReactivateFundTransferRecord extends DalBaseProcess {

  private static final Logger log = LogManager.getLogger();

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    VariablesSecureApp varsAux = bundle.getContext().toVars();
    OBContext.setOBContext(varsAux.getUser(), varsAux.getRole(), varsAux.getClient(),
        varsAux.getOrg());
    try {

      final String fundTransferRecordId = (String) bundle.getParams()
          .get("Aprm_Fund_Transfer_Rec_ID");
      APRM_FundTransferRec fundTransferRecord = OBDal.getInstance()
          .get(APRM_FundTransferRec.class, fundTransferRecordId);
      fundTransferRecord.setStatus("DR");

      // Reactivate transactions before delete them, in this way we check that are not posted or
      // reconciliated
      FIN_TransactionProcess.doTransactionProcess("R", fundTransferRecord.getFINAccTranFrom());
      FIN_TransactionProcess.doTransactionProcess("R", fundTransferRecord.getFINAccTranTo());

      OBDal.getInstance().getSession().delete(fundTransferRecord.getFINAccTranFrom());
      OBDal.getInstance().getSession().delete(fundTransferRecord.getFINAccTranTo());

      fundTransferRecord.setFINAccTranFrom(null);
      fundTransferRecord.setFINAccTranTo(null);

      // OBError is also used for successful results
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
}
