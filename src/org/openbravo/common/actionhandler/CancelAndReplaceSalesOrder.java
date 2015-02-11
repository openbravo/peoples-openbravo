/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
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
 ************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;

public class CancelAndReplaceSalesOrder extends BaseProcessActionHandler {
  private static final Logger log = Logger.getLogger(CancelAndReplaceSalesOrder.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {

      // Get request parameters
      JSONObject request = new JSONObject(content);
      String oldOrderId = request.getString("inpcOrderId");
      String tabId = request.getString("inpTabId");

      // Get new Order
      Order oldOrder = OBDal.getInstance().get(Order.class, oldOrderId);

      // Create new Order header
      Order newOrder = (Order) DalUtil.copy(oldOrder, false, true);
      // Change order values
      newOrder.setProcessed(false);
      newOrder.setPosted("N");
      newOrder.setDocumentStatus("TMP");
      newOrder.setDocumentAction("CO");
      newOrder.setGrandTotalAmount(BigDecimal.ZERO);
      newOrder.setSummedLineAmount(BigDecimal.ZERO);
      Date today = new Date();
      newOrder.setOrderDate(today);
      newOrder.setScheduledDeliveryDate(today);
      String newDocumentNo = FIN_Utility
          .getDocumentNo(oldOrder.getDocumentType(), Order.TABLE_NAME);
      newOrder.setDocumentNo(newDocumentNo);
      newOrder.setReplacedorder(oldOrder);
      OBDal.getInstance().save(newOrder);

      // Create new Order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
      for (OrderLine oldOrderLine : oldOrderLineList) {
        OrderLine newOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        newOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        newOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
        newOrderLine.setSalesOrder(newOrder);
        newOrderLine.setReplacedorderline(oldOrderLine);
        OBDal.getInstance().save(newOrderLine);
      }

      // Get new Order id
      String newOrderId = newOrder.getId();

      // Return result
      JSONObject result = new JSONObject();

      // Execute process and prepare an array with actions to be executed after execution
      JSONArray actions = new JSONArray();

      // New record info
      JSONObject recordInfo = new JSONObject();
      recordInfo.put("tabId", tabId);
      recordInfo.put("recordId", newOrderId);
      recordInfo.put("wait", true);
      JSONObject recordInfoAction = new JSONObject();
      recordInfoAction.put("openDirectTab", recordInfo);
      actions.put(recordInfoAction);

      result.put("responseActions", actions);

      return result;
    } catch (JSONException e) {
      log.error("Error in process", e);
      return new JSONObject();
    }
  }
}
