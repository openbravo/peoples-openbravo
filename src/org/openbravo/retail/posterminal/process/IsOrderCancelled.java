/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.servercontroller.MultiServerJSONProcess;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.json.JsonConstants;

public class IsOrderCancelled extends MultiServerJSONProcess {
  @Override
  public JSONObject execute(JSONObject jsonData) {
    JSONObject result = new JSONObject();

    OBContext.setAdminMode(true);
    try {
      String orderId = jsonData.getString("orderId");
      String documentNo = jsonData.getString("documentNo");
      Boolean cancelOrder = jsonData.getBoolean("setCancelled");
      Order order = OBDal.getInstance().get(Order.class, orderId);

      if (order != null) {
        if (order.isCancelled()) {
          result.put("orderCancelled", true);
        } else {
          result.put("orderCancelled", false);
          if (cancelOrder) {
            order.setCancelled(true);
            OBDal.getInstance().save(order);
          }
        }
      } else {
        // The layaway was not found in the database.
        throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_OrderNotFound",
            new String[] { documentNo }));
      }
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } catch (JSONException e) {
      throw new OBException("Error while canceling and order", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  @Override
  protected String getImportEntryDataType() {
    return null;
  }

  @Override
  protected void createImportEntry(String messageId, JSONObject sentIn, JSONObject processResult,
      Organization organization) throws JSONException {
    // We don't want to create any import entry in these transactions.
  }

  @Override
  protected void createArchiveEntry(String id, JSONObject json) throws JSONException {
    // We don't want to create any import entry in these transactions.
  }

  @Override
  protected boolean executeInOneServer(JSONObject json) throws JSONException {
    return true;
  }
}