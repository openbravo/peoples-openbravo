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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.OBBindingsConstants;
import org.openbravo.client.kernel.ComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
abstract class AddOrderOrInvoiceFilterExpressionHandler {
  private static final Logger log = LoggerFactory
      .getLogger(AddOrderOrInvoiceFilterExpressionHandler.class);

  @Inject
  @Any
  private Instance<AddPaymentDefaultValuesHandler> addPaymentDefaultValuesHandlers;

  protected abstract long getSeq();

  String getFilterExpression(Map<String, String> requestMap) throws JSONException {
    return getDefaultPaymentMethod(requestMap);
  }

  protected AddPaymentDefaultValuesHandler getDefaultsHandler(String strWindowId) {
    AddPaymentDefaultValuesHandler handler = null;

    for (AddPaymentDefaultValuesHandler nextHandler : addPaymentDefaultValuesHandlers
        .select(new ComponentProvider.Selector(strWindowId))) {
      if (handler == null) {
        handler = nextHandler;
      } else if (nextHandler.getSeq() < handler.getSeq()) {
        handler = nextHandler;
      } else if (nextHandler.getSeq() == handler.getSeq()) {
        log.warn(
            "Trying to get handler for window with id {}, there are more than one instance with the same sequence",
            strWindowId);
      }
    }
    return handler;
  }

  protected String getDefaultPaymentMethod(Map<String, String> requestMap) throws JSONException {
    final String strContext = requestMap.get("context");
    JSONObject context = new JSONObject(strContext);
    final String strWindowId = context.getString(OBBindingsConstants.WINDOW_ID_PARAM);
    AddPaymentDefaultValuesHandler handler = getDefaultsHandler(strWindowId);
    return handler.getDefaultPaymentMethod(requestMap);
  }

}
