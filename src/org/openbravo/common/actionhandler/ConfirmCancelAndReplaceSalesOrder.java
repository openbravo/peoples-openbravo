package org.openbravo.common.actionhandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;

public class ConfirmCancelAndReplaceSalesOrder extends BaseProcessActionHandler {
  private static final Logger log = Logger.getLogger(ConfirmCancelAndReplaceSalesOrder.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {

      // Get request parameters
      JSONObject request = new JSONObject(content);
      String newOrderId = request.getString("inpcOrderId");

      // Get new Order
      Order newOrder = OBDal.getInstance().get(Order.class, newOrderId);

      // Get old Order
      Order oldOrder = null;

      return null;
    } catch (JSONException e) {
      log.error("Error in process", e);
      return new JSONObject();
    }
  }
}
