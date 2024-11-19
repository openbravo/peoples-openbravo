/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.CallStoredProcedure;

public class ProcessPurchaseOrderUtility {

  private static final Logger log = Logger.getLogger(ProcessPurchaseOrderUtility.class);
  public static final String PURCHASE_ORDER_PROCESS_ACCESS_ID = "074F7113FD4546EFB27928103383E8CD";
  public static final String DOCACTION_CLOSE = "CL";
  public static final String DOCACTION_BOOK = "CO";
  public static final String DOCACTION_APPROVE = "AP";
  public static final String DOCACTION_REJECT = "RJ";
  public static final String DOCACTION_REACTIVATE = "RE";

  public static final String DOCSTATUS_DRAFT = "DR";
  public static final String DOCSTATUS_BOOKED = "CO";
  public static final String DOCSTATUS_CLOSE = "CL";
  public static final String DOCSTATUS_PENDINGAPPROVAL = "PA";
  public static final String DOCSTATUS_REJECTED = "RJ";

  /**
   * Manage the document status based on action for a given purchase order.
   */
  public static JSONObject manageDocumentStatusBasedOnAction(Order order, String docAction,
      String roleId) {

    JSONObject msg = new JSONObject();
    try {
      switch (docAction) {
        case DOCACTION_BOOK:
          if (!roleHasPrivilegesToProcessPurchaseOrder(roleId)) {
            // Set processed = Y, in order make document read only in Pending Approval Status
            order.setProcessed(true);
            updateDocumentStatus(order, DOCSTATUS_PENDINGAPPROVAL, DOCACTION_APPROVE,
                DOCACTION_APPROVE);
          } else {
            // In order to use existing BOOK action from c_order_post1
            // set DocStatus = DR and DocAction = CO.
            updateDocumentStatus(order, DOCSTATUS_DRAFT, DOCACTION_BOOK, DOCACTION_CLOSE);
            processOrder(order);
          }
          msg = createReturnMessage("success");
          break;
        case DOCACTION_APPROVE:
          if (!roleHasPrivilegesToProcessPurchaseOrder(roleId)) {
            msg = createErrorMessage(
                "The user does not have enough privileges to Approve this purchase order.");
            break;
          } else {
            // In order to use existing BOOK action from c_order_post1
            // set DocStatus = DR and DocAction = CO.
            order.setProcessed(false);
            updateDocumentStatus(order, DOCSTATUS_DRAFT, DOCACTION_BOOK, DOCACTION_CLOSE);
            processOrder(order);
            msg = createReturnMessage("success");
          }
          break;
        case DOCACTION_REJECT:
          if (!roleHasPrivilegesToProcessPurchaseOrder(roleId)) {
            msg = createErrorMessage(
                "The user does not have enough privileges to Reject this purchase order.");
            break;
          }
          // Set processed = Y, in order make document read only in Rejected Status
          order.setProcessed(true);
          updateDocumentStatus(order, DOCSTATUS_REJECTED, DOCACTION_CLOSE, DOCACTION_CLOSE);
          msg = createReturnMessage("success");
          break;
        case DOCACTION_REACTIVATE:
          // In order to use existing REACTIVATE action from c_order_post1
          // set Processed = Y, DocStatus = CO and DocAction = RE.
          order.setProcessed(true);
          updateDocumentStatus(order, DOCSTATUS_BOOKED, DOCACTION_REACTIVATE, DOCACTION_BOOK);
          processOrder(order);
          msg = createReturnMessage("success");
          break;
        case DOCACTION_CLOSE:
          // In order to use existing CLOSE action from c_order_post1
          // set DocStatus = CO and DocAction = CL.
          updateDocumentStatus(order, DOCSTATUS_BOOKED, DOCACTION_CLOSE, "--");
          processOrder(order);
          msg = createReturnMessage("success");
          break;
        default:
          break;
      }
      return msg;
    } catch (JSONException e) {
      log.error("Error while updating purchase order status.." + e.getMessage());
    }
    return null;
  }

  /**
   * Process Order
   */
  private static void processOrder(Order order) {
    try {
      final List<Object> params = new ArrayList<>();
      params.add(null);
      params.add(order.getId());
      CallStoredProcedure.getInstance().call("c_order_post1", params, null, true, false);
    } catch (Exception e) {
      throw new OBException(e.getMessage());
    }
  }

  /**
   * Create a JSON error message with the given text.
   */
  private static JSONObject createErrorMessage(String messageText) throws JSONException {
    JSONObject errorMessage = new JSONObject();
    errorMessage.put("severity", "error");
    errorMessage.put("title", "Process Order");
    errorMessage.put("text", messageText);

    JSONObject jsonMessage = new JSONObject();
    jsonMessage.put("message", errorMessage);
    return jsonMessage;
  }

  /**
   * Check whether Role has access to Process Definition to Process Purchase Order
   */
  public static boolean roleHasPrivilegesToProcessPurchaseOrder(String roleId) {
    Role role = OBDal.getInstance().get(Role.class, roleId);
    OBDal.getInstance().refresh(role);
    return !role.getOBUIAPPProcessAccessList()
        .stream()
        .filter(pda -> StringUtils.equals(pda.getObuiappProcess().getId(),
            PURCHASE_ORDER_PROCESS_ACCESS_ID))
        .collect(Collectors.toList())
        .isEmpty();
  }

  /**
   * Updates the document status and action for a given purchase order.
   */
  private static void updateDocumentStatus(Order order, String newStatus, String newDocAction,
      String newProcessAction) {
    if (newStatus != null) {
      order.setDocumentStatus(newStatus);
    }
    if (newDocAction != null) {
      order.setDocumentAction(newDocAction);
    }
    if (newProcessAction != null) {
      order.setProcessPo(newProcessAction);
    }
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    OBDal.getInstance().commitAndClose();
  }

  /**
   * Return message on successful process
   */
  private static JSONObject createReturnMessage(String msgType) {
    JSONObject jsonRequest = new JSONObject();
    try {
      JSONObject successMessage = new JSONObject();
      successMessage.put("severity", msgType);
      successMessage.put("title", "Process Order");
      successMessage.put("text", "Process completed successfully");
      jsonRequest.put("message", successMessage);

    } catch (JSONException e) {
      log.error("Error in createReturnMessage", e);
    }
    return jsonRequest;
  }
}
