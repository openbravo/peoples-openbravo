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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.common.actionhandler.createlinesfromorderprocess.CreateLinesFromOrderProcess;
import org.openbravo.common.actionhandler.createlinesfromprocess.util.CreateLinesFromMessageUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLinesFromOrder extends BaseProcessActionHandler {

  private static final Logger log = LoggerFactory.getLogger(CreateLinesFromOrder.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    try {
      // Request Parameters
      jsonRequest = new JSONObject(content);
      final String requestedAction = getRequestedAction(jsonRequest);
      JSONArray selectedOrders = getSelectedOrderLines(jsonRequest);
      Invoice currentInvoice = getCurrentInvoice(jsonRequest);

      if (requestedActionIsDoneAndThereAreSelectedOrderLines(requestedAction, selectedOrders)) {
        // CreateLinesFromOrderProcess is instantiated using Weld so it can use Dependency Injection
        CreateLinesFromOrderProcess createLinesFromOrderProcess = WeldUtils
            .getInstanceFromStaticBeanManager(CreateLinesFromOrderProcess.class);
        int createdOrderLinesCount = createLinesFromOrderProcess.createOrderLines(selectedOrders,
            currentInvoice);
        jsonRequest.put(CreateLinesFromMessageUtil.MESSAGE,
            CreateLinesFromMessageUtil.getSuccessMessage(createdOrderLinesCount));
      }
    } catch (Exception e) {
      log.error("Error in CreateLinesFromOrder Action Handler", e);

      try {
        if (jsonRequest != null) {
          jsonRequest.put(CreateLinesFromMessageUtil.MESSAGE,
              CreateLinesFromMessageUtil.getErrorMessage(e));
        }
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    }

    return jsonRequest;
  }

  private Invoice getCurrentInvoice(JSONObject jsonRequest) {
    String invoiceId;
    try {
      invoiceId = jsonRequest.getString("inpcInvoiceId");
    } catch (JSONException e) {
      log.error("Error getting the current invoice id.", e.getMessage());
      throw new OBException(e);
    }
    return OBDal.getInstance().get(Invoice.class, invoiceId);
  }

  private String getRequestedAction(final JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getString(ApplicationConstants.BUTTON_VALUE);
  }

  private JSONArray getSelectedOrderLines(final JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getJSONObject("_params").getJSONObject("window").getJSONArray("_selection");
  }

  private boolean requestedActionIsDoneAndThereAreSelectedOrderLines(final String requestedAction,
      final JSONArray selectedOrderLines) {
    return StringUtils.equals(requestedAction, "DONE") && selectedOrderLines.length() > 0;
  }

}
