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

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class CompleteFundTransferRecord extends DalBaseProcess {

  private static final Logger log = LogManager.getLogger();

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    // Recover context and variables
    ConnectionProvider conn = bundle.getConnection();
    VariablesSecureApp varsAux = bundle.getContext().toVars();
    HttpServletRequest request = RequestContext.get().getRequest();

    OBContext.setOBContext(varsAux.getUser(), varsAux.getRole(), varsAux.getClient(),
        varsAux.getOrg());
    VariablesSecureApp vars = new VariablesSecureApp(request);

    try {

      final String fundTransferRecordId = (String) bundle.getParams()
          .get("Aprm_Fund_Transfer_Rec_ID");
      APRM_FundTransferRec fundTransferRecord = OBDal.getInstance()
          .get(APRM_FundTransferRec.class, fundTransferRecordId);
      fundTransferRecord.setStatus("CO");

      // OBDal.getInstance().flush();
      // OBDal.getInstance().refresh(fundTransferRecord);

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
