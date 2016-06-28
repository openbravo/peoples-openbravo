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
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.db.DbUtility;

public class CancelAndReplaceSalesOrder extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(CancelAndReplaceSalesOrder.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {

    // Declare json to be returned
    JSONObject result = new JSONObject();
    JSONObject openDirectTab = new JSONObject();
    JSONObject showMsgInProcessView = new JSONObject();
    JSONObject showMsgInView = new JSONObject();
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
      String newDocumentNo = CancelAndReplaceUtils.getNextCancelDocNo(oldOrder.getDocumentNo());
      newOrder.setDocumentNo(newDocumentNo);
      newOrder.setReplacedorder(oldOrder);
      OBDal.getInstance().save(newOrder);

      // Create new Order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
      for (OrderLine oldOrderLine : oldOrderLineList) {
        OrderLine newOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        newOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        newOrderLine.setReservedQuantity(BigDecimal.ZERO);
        newOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
        newOrderLine.setSalesOrder(newOrder);
        newOrderLine.setReplacedorderline(oldOrderLine);
        OBDal.getInstance().save(newOrderLine);
      }

      // Get new Order id
      String newOrderId = newOrder.getId();

      // Execute process and prepare an array with actions to be executed after execution
      JSONArray actions = new JSONArray();

      // Message in tab from where the process is executed
      showMsgInProcessView.put("msgType", "success");
      showMsgInProcessView.put("msgTitle", OBMessageUtils.messageBD("Success"));
      showMsgInProcessView.put("msgText", OBMessageUtils.messageBD("OrderCreatedInTemporalStatus")
          + " " + newDocumentNo);
      showMsgInProcessView.put("wait", true);

      JSONObject showMsgInProcessViewAction = new JSONObject();
      showMsgInProcessViewAction.put("showMsgInProcessView", showMsgInProcessView);

      actions.put(showMsgInProcessViewAction);

      // New record info
      openDirectTab.put("tabId", tabId);
      openDirectTab.put("recordId", newOrderId);
      openDirectTab.put("wait", true);

      JSONObject openDirectTabAction = new JSONObject();
      openDirectTabAction.put("openDirectTab", openDirectTab);

      actions.put(openDirectTabAction);

      // result.put("openDirectTab", openDirectTab);

      // Message of the new opened tab
      showMsgInView.put("msgType", "success");
      showMsgInView.put("msgTitle", OBMessageUtils.messageBD("Success"));
      showMsgInView.put("msgText", OBMessageUtils.messageBD("OrderInTemporalStatus"));

      JSONObject showMsgInViewAction = new JSONObject();
      showMsgInViewAction.put("showMsgInView", showMsgInView);

      actions.put(showMsgInViewAction);

      result.put("responseActions", actions);

      // result.put("showMsgInView", showMsgInView);

    } catch (Exception e) {
      log.error("Error in process", e);
      try {
        OBDal.getInstance().getConnection().rollback();
        result = new JSONObject();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", OBMessageUtils.messageBD("Error"));
        errorMessage.put("text", e.getMessage());
        result.put("message", errorMessage);
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e);
      throw new OBException(e3);
    }
    return result;
  }
}
