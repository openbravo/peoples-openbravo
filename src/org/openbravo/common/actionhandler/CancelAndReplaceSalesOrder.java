package org.openbravo.common.actionhandler;

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
      // FIN_Utility.getDocumentNo();

      // Get new Order
      Order oldOrder = OBDal.getInstance().get(Order.class, oldOrderId);

      // Create new Order header
      Order newOrder = (Order) DalUtil.copy(oldOrder, false, true);
      // TODO Change order values
      newOrder.setProcessed(false);
      newOrder.setPosted("N");
      newOrder.setDocumentStatus("TMP");
      String newDocumentNo = FIN_Utility.getDocumentNo(newOrder.getDocumentType(), "C_Order");
      newOrder.setDocumentNo(newDocumentNo);
      OBDal.getInstance().save(newOrder);

      // Create new Order lines
      List<OrderLine> orderLineList = oldOrder.getOrderLineList();
      for (OrderLine oldOrderLine : orderLineList) {
        OrderLine newOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        newOrderLine.setSalesOrder(newOrder);
        OBDal.getInstance().save(newOrderLine);
      }

      // Get new Order id
      String newOrderId = newOrder.get(Order.PROPERTY_ID).toString();

      // Return result
      JSONObject result = new JSONObject();
      // Execute process and prepare an array with actions to be executed after execution
      JSONArray actions = new JSONArray();

      // Old record message
      // JSONObject oldWindowMessage = new JSONObject();
      // oldWindowMessage.put("msgType", "info");
      // oldWindowMessage.put("msgTitle", "Old record");
      // oldWindowMessage.put("msgText", "Old record message");
      // JSONObject oldWindowMessageAction = new JSONObject();
      // oldWindowMessageAction.put("showMsgInProcessView", oldWindowMessage);
      // actions.put(oldWindowMessageAction);

      // New record info
      JSONObject recordInfo = new JSONObject();
      recordInfo.put("tabId", tabId);
      recordInfo.put("recordId", newOrderId);
      recordInfo.put("wait", true);
      JSONObject recordInfoAction = new JSONObject();
      recordInfoAction.put("openDirectTab", recordInfo);
      actions.put(recordInfoAction);

      // New record message
      // JSONObject newWindowMessage = new JSONObject();
      // newWindowMessage.put("msgType", "success");
      // newWindowMessage.put("msgTitle", "Update Sales Order");
      // newWindowMessage.put("msgText", "This record was opened from process execution");
      // JSONObject newWindowMessageAction = new JSONObject();
      // newWindowMessageAction.put("showMsgInProcessView", newWindowMessage);
      // actions.put(newWindowMessageAction);

      result.put("responseActions", actions);

      return result;
    } catch (JSONException e) {
      log.error("Error in process", e);
      return new JSONObject();
    }
  }
}
