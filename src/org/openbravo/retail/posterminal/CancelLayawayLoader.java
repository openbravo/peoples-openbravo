/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Date;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "OBPOS_CancelLayaway")
public class CancelLayawayLoader extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  private static final Logger log = Logger.getLogger(CancelLayawayLoader.class);

  public JSONObject saveRecord(JSONObject json) throws Exception {

    boolean useOrderDocumentNoForRelatedDocs = false;

    try {
      useOrderDocumentNoForRelatedDocs = "Y".equals(Preferences.getPreferenceValue(
          "OBPOS_UseOrderDocumentNoForRelatedDocs", true, OBContext.getOBContext()
              .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
              .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error(
          "Error getting OBPOS_UseOrderDocumentNoForRelatedDocs preference: " + e1.getMessage(), e1);
    }

    try {
      if (json.has("cashUpReportInformation")) {
        // Update CashUp Report
        JSONObject jsoncashup = json.getJSONObject("cashUpReportInformation");
        Date cashUpDate = new Date();

        UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);
      }

      Order order = OBDal.getInstance().get(Order.class, json.getString("orderid"));
      POSUtils.setDefaultPaymentType(json, order);
      CancelAndReplaceUtils.cancelOrder(json.getString("orderid"), json,
          useOrderDocumentNoForRelatedDocs);
    } catch (Exception ex) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException("CancelLayawayLoader.cancelOrder: ", ex);
    }

    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    return jsonResponse;
  }

  protected String getImportQualifier() {
    return "OBPOS_CancelLayaway";
  }
}
