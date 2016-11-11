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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.materialmgmt.CentralBroker;

public class GetConvertedQtyActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(GetConvertedQtyActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();

    try {
      final JSONObject jsonData = new JSONObject(data);
      final String mProductId = jsonData.getString("mProductId");
      final BigDecimal qty = new BigDecimal(jsonData.getString("qty"));
      final String toUOM = jsonData.getString("toUOM");
      final Boolean reverse = jsonData.getBoolean("reverse");

      try {
        result.put("qty", CentralBroker.getInstance().getConvertedQty(mProductId, qty, toUOM, reverse));
      } catch (Exception e) {
        result.put("qty", qty);
        log4j.error("Error while converting UOM, exception e", e);
      }
    } catch (Exception e1) {
      try {
        result.put("qty", "0");
      } catch (JSONException e) {
        log4j.error("Error while converting UOM, exception e1", e);
      }
    }
    return result;
  }
}