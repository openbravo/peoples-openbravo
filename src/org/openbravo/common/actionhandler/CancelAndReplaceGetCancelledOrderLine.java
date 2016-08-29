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

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLine;

public class CancelAndReplaceGetCancelledOrderLine extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(CancelAndReplaceGetCancelledOrderLine.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    String orderLineId = "";
    try {
      final JSONObject jsonData = new JSONObject(data);
      orderLineId = jsonData.getString("orderLineId");
      OrderLine orderLine = OBDal.getInstance().get(OrderLine.class, orderLineId);
      result.put("deliveredQuantity", orderLine.getDeliveredQuantity());
    } catch (Exception e) {
      log.error("Error retrieving OrderLine with id {}", e);
    }
    return result;
  }
}
