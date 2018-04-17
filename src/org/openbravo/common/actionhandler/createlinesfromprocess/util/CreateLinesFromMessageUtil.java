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

package org.openbravo.common.actionhandler.createlinesfromprocess.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DbUtility;

public class CreateLinesFromMessageUtil {

  public static final String MESSAGE = "message";
  private static final String MESSAGE_SEVERITY = "severity";
  private static final String MESSAGE_TEXT = "text";
  private static final String MESSAGE_RECORDS_COPIED = "RecordsCopied";
  private static final String MESSAGE_SUCCESS = "success";
  private static final String MESSAGE_ERROR = "error";

  public static JSONObject getSuccessMessage(final int recordsCopiedCount) throws JSONException {
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_SUCCESS);
    errorMessage.put(MESSAGE_TEXT, OBMessageUtils.messageBD(MESSAGE_SUCCESS) + "<br/>"
        + OBMessageUtils.messageBD(MESSAGE_RECORDS_COPIED) + recordsCopiedCount);
    return errorMessage;
  }

  public static JSONObject getErrorMessage(final Exception e) throws JSONException {
    Throwable ex = DbUtility.getUnderlyingSQLException(e);
    String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_ERROR);
    errorMessage.put(MESSAGE_TEXT, message);
    return errorMessage;
  }
}
