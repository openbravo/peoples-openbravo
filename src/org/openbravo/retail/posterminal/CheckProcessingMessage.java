/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.importprocess.ImportEntryArchive;
import org.openbravo.service.json.JsonConstants;

public class CheckProcessingMessage extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(CheckProcessingMessage.class);

  @Override
  public JSONObject exec(final JSONObject jsonsent) throws JSONException, ServletException {
    final JSONObject respArray = new JSONObject();
    final JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    OBContext.setAdminMode(true);
    try {
      final String messageId = jsonsent.getString("messageId");

      final ImportEntryArchive importEntry = OBDal.getInstance().get(ImportEntryArchive.class,
          messageId);
      if (importEntry != null) {
        respArray.put("status", importEntry.getImportStatus());
        respArray.put("json", importEntry.getJsonInfo());
        respArray.put("errorMessage", importEntry.getErrorinfo());
      }
    } catch (final Exception e) {
      log.error("There was an error Checking Processin Message: ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    return result;
  }

}
