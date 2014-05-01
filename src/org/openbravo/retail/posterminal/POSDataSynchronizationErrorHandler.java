/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationErrorHandler;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;

@ApplicationScoped
@Qualifier(POSConstants.APP_NAME)
public class POSDataSynchronizationErrorHandler extends DataSynchronizationErrorHandler {

  private static final Logger log = Logger.getLogger(DataSynchronizationProcess.class);

  @Override
  public void handleError(Throwable t, Entity entity, JSONObject result, JSONObject jsonRecord) {

    // Creation of the order failed. We will now store the order in the import errors table
    String posTerminalId = null;
    try {
      posTerminalId = jsonRecord.getString("posTerminal");
    } catch (JSONException e1) {
      // won't happen
    }
    log.error("An error happened when processing a record: ", t);
    OBPOSErrors errorEntry = null;
    if (jsonRecord.has("posErrorId")) {
      try {
        errorEntry = OBDal.getInstance().get(OBPOSErrors.class, jsonRecord.getString("posErrorId"));
      } catch (JSONException e1) {
        // won't happen
      }
    } else {
      errorEntry = OBProvider.getInstance().get(OBPOSErrors.class);
    }
    errorEntry.setError(getErrorMessage(t));
    errorEntry.setOrderstatus("N");
    errorEntry.setJsoninfo(jsonRecord.toString());
    errorEntry.setTypeofdata(entity.getName());
    errorEntry
        .setObposApplications(OBDal.getInstance().get(OBPOSApplications.class, posTerminalId));
    OBDal.getInstance().save(errorEntry);
    OBDal.getInstance().flush();
    log.error("Error while loading order", t);

  }

}
