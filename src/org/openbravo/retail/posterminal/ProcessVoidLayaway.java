/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Date;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  @Override
  public JSONObject saveRecord(JSONObject jsonRecord) throws Exception {

    JSONArray respArray = new JSONArray();
    JSONObject jsonorder = (JSONObject) jsonRecord.get("order");
    Order order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));

    if (order != null) {
      final String loaded = jsonorder.getString("loaded"), updated = OBMOBCUtils.convertToUTCDateComingFromServer(order.getUpdated());
      if (!(loaded.compareTo(updated) >= 0)) {
        throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_outdatedLayaway", OBContext.getOBContext().getLanguage().getLanguage()));
      }
    }

    // Update CashUp Report
    JSONObject jsoncashup = jsonorder.getJSONObject("cashUpReportInformation");
    Date cashUpDate = new Date();

    UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);

    VoidLayaway proc = WeldUtils.getInstanceFromStaticBeanManager(VoidLayaway.class);
    proc.voidLayaway(jsonorder, order);

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }

  protected String getImportQualifier() {
    return "OBPOS_VoidLayaway";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.voidLayaway";
  }
}