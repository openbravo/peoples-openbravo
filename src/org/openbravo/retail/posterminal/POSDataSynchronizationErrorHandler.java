/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationErrorHandler;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.model.common.order.OrderLine;

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
      try {
        posTerminalId = jsonRecord.getString("posterminal");
      } catch (JSONException e2) {
        // won't happen
      }
    }
    log.error("An error happened when processing a record: ", t);
    OBPOSErrors errorEntry = null;
    if (jsonRecord.has("posErrorId")) {
      // note: error entry may have been removed, in that case the errorEntry remains null
      // and a new one will be created
      try {
        errorEntry = OBDal.getInstance().get(OBPOSErrors.class, jsonRecord.getString("posErrorId"));
        errorEntry.getOBPOSErrorsLineList().clear();
      } catch (JSONException e1) {
        // won't happen
      }
    }
    if (errorEntry == null) {
      errorEntry = OBProvider.getInstance().get(OBPOSErrors.class);
    }
    errorEntry.setError(getErrorMessage(t));
    errorEntry.setOrderstatus("N");
    errorEntry.setJsoninfo(jsonRecord.toString());
    errorEntry.setTypeofdata(entity.getName());
    errorEntry
        .setObposApplications(OBDal.getInstance().get(OBPOSApplications.class, posTerminalId));
    OBDal.getInstance().save(errorEntry);

    // save order_id, order_id from verified return in error line
    HashSet<String> orderIdList = new HashSet<String>();
    try {
      orderIdList.add(jsonRecord.optString("id", null));

      JSONArray orderlines = jsonRecord.getJSONArray("lines");
      for (int i = 0; i < orderlines.length(); i++) {
        JSONObject jsonOrderLine = orderlines.getJSONObject(i);
        if (jsonOrderLine.has("originalOrderLineId")) {
          OrderLine orderLine = OBDal.getInstance().get(OrderLine.class,
              jsonOrderLine.optString("originalOrderLineId"));
          orderIdList.add(orderLine.getSalesOrder().getId());
        }
      }
      orderIdList.remove(null);
    } catch (JSONException e) {
      log.error("Error while getting orderid", e);
    }

    for (String orderId : orderIdList) {
      OBPOSErrorsLine errorLineEntry = OBProvider.getInstance().get(OBPOSErrorsLine.class);
      errorLineEntry.setObposErrors(errorEntry);
      errorLineEntry.setRecordID(orderId);
      OBDal.getInstance().save(errorLineEntry);
    }

    OBDal.getInstance().flush();
    log.error("Error while loading order", t);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.mobile.core.process.DataSynchronizationErrorHandler#setImportStatusToError()
   */
  public boolean setImportEntryStatusToError() {
    return false;
  }

}
