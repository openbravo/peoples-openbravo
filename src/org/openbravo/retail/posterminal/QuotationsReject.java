/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.RejectReason;
import org.openbravo.service.json.JsonConstants;

public class QuotationsReject extends POSDataSynchronizationProcess
    implements DataSynchronizationImportProcess {

  @Override
  public JSONObject saveRecord(JSONObject jsonRecord) throws Exception {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      String orderid = jsonRecord.getString("orderid");
      String rejectReasonId = jsonRecord.getString("rejectReasonId");
      Order order = OBDal.getInstance().get(Order.class, orderid);
      RejectReason reason = OBDal.getInstance().get(RejectReason.class, rejectReasonId);
      JSONArray respArray = new JSONArray();
      if (order != null && reason != null && !"CA".equals(order.getDocumentStatus())) {
        order.setDocumentStatus("CJ");
        order.setRejectReason(reason);
        OBDal.getInstance().save(order);
        respArray.put(order);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      } else {
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      }
      result.put(JsonConstants.RESPONSE_DATA, respArray);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  @Override
  protected String getImportQualifier() {
    return "OBPOS_RejectQuotation";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_quotation.rejections";
  }

}
