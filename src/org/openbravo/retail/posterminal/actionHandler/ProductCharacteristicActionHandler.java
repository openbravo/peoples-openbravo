/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.actionHandler;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.POSConstants;
import org.openbravo.retail.posterminal.POSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCharacteristicActionHandler extends BaseActionHandler {
  private static final Logger log = LoggerFactory
      .getLogger(ProductCharacteristicActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    boolean isDelete = false;
    JSONArray deletedRecords = null;
    long increment = 0;
    try {
      final JSONObject jsonData = new JSONObject(data);
      isDelete = jsonData.getBoolean("isDelete");

      // delete (deleted rows are in the database yet, so the query is not real. We need to remove
      // those which are being removed)
      if (isDelete) {
        deletedRecords = jsonData.getJSONArray("recordsToDelete");
        for (int i = 0; i < deletedRecords.length(); i++) {
          JSONObject curDeletedRecord = deletedRecords.getJSONObject(i);
          if (curDeletedRecord.getBoolean("obposFilteronwebpos")) {
            increment -= 1;
          }
        }
      } else {
        // save & update (when query is executed data is already in the DB, so result is real)
        increment = 0;
      }

      long limit = this.readPreference();
      // Query to get the current number of ch marked as "filter on web pos"
      long numberOfChToUseAsFilterInPOS = POSUtils.getNumberOfCharacteristicsToFilterInWebPos();
      numberOfChToUseAsFilterInPOS = numberOfChToUseAsFilterInPOS + increment;

      // To be used for msgs
      final String[] arrEmpty = {};
      String[] arrLimit = new String[2];
      arrLimit[0] = Long.toString(numberOfChToUseAsFilterInPOS);
      arrLimit[1] = Long.toString(limit);

      if (numberOfChToUseAsFilterInPOS > limit) {
        result.put("warn", true);
        result.put("warnMessageBody", OBMessageUtils.getI18NMessage(
            "OBPOS_nrOfChToUseAsFilterInWebPOSExceedTheLimit_body", arrLimit));
        result.put("warnMessageTitle", OBMessageUtils.getI18NMessage(
            "OBPOS_nrOfChToUseAsFilterInWebPOSExceedTheLimit_title", arrEmpty));
      }
    } catch (Exception e) {
      log.error(
          "Error calculating the number of characteristics which are marked for filtering in Web POS",
          e);
    }
    return result;
  }

  private long readPreference() {
    return POSConstants.MAX_CHARACTERISTICS_TO_BE_FILTERED_IN_WEB_POS;
  }
}