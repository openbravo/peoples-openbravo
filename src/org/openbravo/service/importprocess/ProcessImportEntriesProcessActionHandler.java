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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.service.importprocess;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.process.BaseProcessActionHandler;

/**
 * Will trigger the import process.
 * 
 * @author mtaal
 */
public class ProcessImportEntriesProcessActionHandler extends BaseProcessActionHandler {
  private static final Logger log = Logger
      .getLogger(ProcessImportEntriesProcessActionHandler.class);

  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      ImportEntryManager entryManager = WeldUtils
          .getInstanceFromStaticBeanManager(ImportEntryManager.class);

      entryManager.notifyNewImportEntryCreated();

      JSONObject result = new JSONObject();
      JSONObject msgTotal = new JSONObject();
      JSONArray actions = new JSONArray();
      msgTotal.put("msgType", "info");
      msgTotal.put("msgTitle", "Import Process");
      msgTotal.put("msgText", "Import process has been triggered");

      JSONObject msgTotalAction = new JSONObject();
      msgTotalAction.put("showMsgInProcessView", msgTotal);
      actions.put(msgTotalAction);
      result.put("responseActions", actions);

      return result;
    } catch (JSONException e) {
      log.error("Error in process", e);
      return new JSONObject();
    }
  }

}
