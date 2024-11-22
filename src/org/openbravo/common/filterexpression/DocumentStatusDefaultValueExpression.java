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
 * All portions are Copyright (C) 2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.filterexpression;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;

/**
 * This class returns the default value for the Document Status Parameter of the Purchase Order
 * Process in Purchase Order Window
 */

public class DocumentStatusDefaultValueExpression implements FilterExpression {
  private static final Logger log = LogManager.getLogger();

  @Override
  public String getExpression(Map<String, String> requestMap) {
    try {

      final JSONObject context = new JSONObject(requestMap.get("context"));
      final String strOrderId = OrderDefaultValuesUtility.getOrderIdFromContext(context);
      if (strOrderId == null || StringUtils.isBlank(strOrderId)) {
        return null;
      } else {
        return OBDal.getInstance().get(Order.class, strOrderId).getDocumentStatus();
      }
    } catch (Exception e) {
      log.error(
          "Error trying to get default value of the Default Document Status: " + e.getMessage(), e);
      return null;
    }
  }
}
