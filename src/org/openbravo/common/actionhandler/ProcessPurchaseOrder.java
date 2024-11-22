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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;

public class ProcessPurchaseOrder extends BaseProcessActionHandler {

  private static final Logger log = Logger.getLogger(ProcessPurchaseOrder.class);
  private static final String BOOK_ACTION_ID = "137FAE2E0B2E41CB901067BC77E37EE7";
  private static final String APPROVE_ACTION_ID = "83E7ED16C71F43558DA8404D21D0B63B";
  private static final String REJECT_ACTION_ID = "B06D9380FA25434EB5EDCAF44368F347";
  private static final String REACTIVATE_ACTION_ID = "C102A96C20C44BA6BAAD322E999633A3";
  private static final String CLOSE_ACTION_ID = "FA50DEFEDC4E4EFE82BA5BF71C55E53F";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject result = new JSONObject();
    try {
      final JSONObject jsonData = new JSONObject(content);
      final JSONObject jsonParams = jsonData.getJSONObject("_params");
      final String strOrderId = jsonData.getString("inpcOrderId");
      String selectedActionId = jsonParams.getString("docAction");
      if (StringUtils.isEmpty(selectedActionId) || "null".equals(selectedActionId)) {
        selectedActionId = CLOSE_ACTION_ID;
      }
      final String docAction = DocAction.get(selectedActionId).getsearchKey();
      final Order order = OBDal.getInstance().get(Order.class, strOrderId);

      result = ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(order, docAction,
          OBContext.getOBContext().getRole().getId());
      if (result != null) {
        return result;
      }
    } catch (JSONException e) {

      log.error(e.getMessage(), e);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      try {

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", OBMessageUtils.messageBD("Error"));
        errorMessage.put("text", e.getMessage());
        result.put("message", errorMessage);
      } catch (JSONException e2) {

        log.error(e2.getMessage(), e2);
      }
    }
    return result;
  }

  /**
   * The available document actions used to change the status of a delivery note
   */
  public enum DocAction {

    BOOK(BOOK_ACTION_ID, "CO"),
    APPROVE(APPROVE_ACTION_ID, "AP"),
    REJECT(REJECT_ACTION_ID, "RJ"),
    CLOSE(CLOSE_ACTION_ID, "CL"),
    REACTIVATE(REACTIVATE_ACTION_ID, "RE");

    private String searchKey;
    private String id;

    private DocAction(String id, String searchKey) {
      this.id = id;
      this.searchKey = searchKey;
    }

    /**
     * The search key of the document action
     */
    public String getsearchKey() {
      return searchKey;
    }

    /**
     * The search key of the document action
     */
    public String getId() {
      return id;
    }

    /**
     * Gets the document action with the given search key and type
     *
     * @param id
     *          The search key that identifies the document action
     *
     * @return the document action with the given search key and type
     */
    public static DocAction get(String id) {
      return Arrays.stream(DocAction.values())
          .filter(docAction -> (docAction.getId().equalsIgnoreCase(id)))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Unknown order document action: " + id));
    }
  }

}
