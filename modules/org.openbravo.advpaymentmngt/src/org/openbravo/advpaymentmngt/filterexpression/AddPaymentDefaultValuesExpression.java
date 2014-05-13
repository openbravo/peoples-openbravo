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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.advpaymentmngt.filterexpression;

import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.client.application.OBBindingsConstants;
import org.openbravo.client.kernel.ComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPaymentDefaultValuesExpression implements FilterExpression {
  private static final Logger log = LoggerFactory
      .getLogger(AddPaymentDefaultValuesExpression.class);
  @Inject
  @Any
  private Instance<AddPaymentDefaultValuesHandler> addPaymentFilterExpressionHandlers;

  @Override
  public String getExpression(Map<String, String> requestMap) {
    final String strWindowId = getWindowId(requestMap);

    AddPaymentDefaultValuesHandler handler = getHandler(strWindowId,
        addPaymentFilterExpressionHandlers);
    if (handler == null) {
      throw new OBException("No handler found");
    }
    final String strCurrentParam = getCurrentParam(requestMap);
    Parameters param = getParameter(strCurrentParam);
    switch (param) {
    case ExpectedPayment:
      return handler.getDefaultExpectedAmount(requestMap);
    case ActualPayment:
      return handler.getDefaultActualPaymentAmount(requestMap);
    }
    throw new OBException("Unsupported columnname");
  }

  private String getWindowId(Map<String, String> requestMap) {
    final String strContext = requestMap.get("context");
    try {
      JSONObject context = new JSONObject(strContext);
      return context.getString(OBBindingsConstants.WINDOW_ID_PARAM);
    } catch (JSONException ignore) {
    }
    return null;
  }

  private String getCurrentParam(Map<String, String> requestMap) {
    final String strContext = requestMap.get("context");
    try {
      JSONObject context = new JSONObject(strContext);
      return context.getString("currentParam");
    } catch (JSONException ignore) {
    }
    return null;
  }

  private static AddPaymentDefaultValuesHandler getHandler(String strWindowId,
      Instance<AddPaymentDefaultValuesHandler> addPaymentFilterExpressionHandlers) {
    AddPaymentDefaultValuesHandler handler = null;
    for (AddPaymentDefaultValuesHandler nextHandler : addPaymentFilterExpressionHandlers
        .select(new ComponentProvider.Selector(strWindowId))) {
      if (handler == null) {
        handler = nextHandler;
      } else {
        log.warn("Trying to get handler for window with id {}, there are more than one instance",
            strWindowId);
      }
    }
    return handler;
  }

  private Parameters getParameter(String columnname) {
    if ("actual_payment".equals(columnname)) {
      return Parameters.ActualPayment;
    } else if ("expected_payment".equals(columnname)) {
      return Parameters.ExpectedPayment;
    }
    return null;
  }

  private enum Parameters {
    ActualPayment, ExpectedPayment
  }

}
