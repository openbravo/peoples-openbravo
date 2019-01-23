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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationErrorHandler;
import org.openbravo.model.common.order.OrderLine;

@ApplicationScoped
@Qualifier(POSConstants.APP_NAME)
public class POSDataSynchronizationErrorHandler extends DataSynchronizationErrorHandler {

  private static final Logger log = LogManager.getLogger();

  @Override
  public void handleError(Throwable t, Entity entity, JSONObject result, JSONObject jsonRecord) {

    OBPOSAppTermStatHist terminalStatusHistory = null;

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
    String cashupId = null;
    try {
      if (entity.getName().equals("OBPOS_App_Cashup")) {
        if (jsonRecord.has("id")) {
          cashupId = jsonRecord.getString("id");
        }
      } else {
        if (jsonRecord.has("cashUpReportInformation")) {
          JSONObject cashUpReportInformation = jsonRecord.getJSONObject("cashUpReportInformation");
          if (cashUpReportInformation.has("id")) {
            cashupId = cashUpReportInformation.getString("obposAppCashup");
          }
        }
      }
    } catch (JSONException e1) {
      // TODO: won't happen
    }

    if (cashupId != null) {
      OBCriteria<OBPOSAppTermStatHist> termStatHistCriteria = OBDal.getInstance().createCriteria(
          OBPOSAppTermStatHist.class);
      termStatHistCriteria.add(Restrictions.eq(OBPOSAppTermStatHist.PROPERTY_CASHUP,
          OBDal.getInstance().get(OBPOSAppCashup.class, cashupId)));
      terminalStatusHistory = (OBPOSAppTermStatHist) termStatHistCriteria.uniqueResult();
    } else {
      if (posTerminalId != null) {
        OBCriteria<OBPOSAppTermStatHist> termStatHistCriteria = OBDal.getInstance().createCriteria(
            OBPOSAppTermStatHist.class);
        termStatHistCriteria.add(Restrictions.eq(OBPOSAppTermStatHist.PROPERTY_POSTERMINAL,
            OBDal.getInstance().get(OBPOSApplications.class, posTerminalId)));
        termStatHistCriteria.addOrderBy(OBPOSAppTermStatHist.PROPERTY_CREATIONDATE, false);
        termStatHistCriteria.setMaxResults(1);
        terminalStatusHistory = (OBPOSAppTermStatHist) termStatHistCriteria.uniqueResult();
      } else {
        log.debug("Unable to get posterminal id or cashup id to update terminal status history");
      }
    }

    if (terminalStatusHistory != null) {
      terminalStatusHistory.setErrorswhileimporting(terminalStatusHistory.getErrorswhileimporting() + 1L);
    } else {
      log.debug("There is no record for Terminal Status History.");
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
    errorEntry.setObposApplications(OBDal.getInstance().get(OBPOSApplications.class, posTerminalId));
    OBDal.getInstance().save(errorEntry);

    // save order_id, order_id from verified return in error line
    HashSet<String> recordIdList = new HashSet<String>();
    recordIdList.add(jsonRecord.optString("id", null));

    if (jsonRecord.has("lines")) {
      JSONArray orderlines = jsonRecord.optJSONArray("lines");
      if (orderlines != null) {
        for (int i = 0; i < orderlines.length(); i++) {
          JSONObject jsonOrderLine = orderlines.optJSONObject(i);
          if (jsonOrderLine != null && jsonOrderLine.has("originalOrderLineId")) {
            OrderLine orderLine = OBDal.getInstance().get(OrderLine.class,
                jsonOrderLine.optString("originalOrderLineId"));
            recordIdList.add(orderLine.getSalesOrder().getId());
          }
        }
      }
    }
    recordIdList.remove(null);

    for (String recordId : recordIdList) {
      OBPOSErrorsLine errorLineEntry = OBProvider.getInstance().get(OBPOSErrorsLine.class);
      errorLineEntry.setObposErrors(errorEntry);
      errorLineEntry.setRecordID(recordId);
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
  @Override
  public boolean setImportEntryStatusToError() {
    return false;
  }

}
