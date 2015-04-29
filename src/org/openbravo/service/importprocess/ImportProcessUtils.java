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
package org.openbravo.service.importprocess;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.BatchUpdateException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

/**
 * Utility methods used in the import process.
 * 
 * @author mtaal
 */
public class ImportProcessUtils {

  public static void logError(Logger log, Throwable t) {
    Throwable toReport = t;
    if (t.getCause() instanceof BatchUpdateException) {
      toReport = ((BatchUpdateException) t.getCause()).getNextException();
    }
    log.error(toReport.getMessage(), toReport);
  }

  public static String getErrorMessage(Throwable e) {
    StringWriter sb = new StringWriter();

    PrintWriter pw = new PrintWriter(sb);

    e.printStackTrace(pw);

    if (e.getCause() instanceof BatchUpdateException) {
      final BatchUpdateException batchException = (BatchUpdateException) e.getCause();
      if (batchException.getNextException() != null) {
        pw.write("\n >>>> Next Exception:\n");
        batchException.getNextException().printStackTrace(pw);
      }
    }

    return sb.toString();
  }

  /**
   * Data send from clients can contain a single data element or be an array. If it is an array then
   * the first entry in the array is used to find the value of the property.
   * 
   * If the property can not be found then null is returned.
   */
  public static String getJSONProperty(JSONObject jsonObject, String property) {
    try {
      if (jsonObject.has(property)) {
        return jsonObject.getString(property);
      }
      if (!jsonObject.has("data")) {
        return null;
      }

      Object jsonData = jsonObject.get("data");
      JSONObject jsonContent = null;
      if (jsonData instanceof JSONObject) {
        jsonContent = (JSONObject) jsonData;
      } else if (jsonData instanceof String) {
        jsonContent = new JSONObject((String) jsonData);
      } else if (jsonData instanceof JSONArray) {
        final JSONArray jsonArray = (JSONArray) jsonData;
        if (jsonArray.length() > 0) {
          jsonContent = (JSONObject) jsonArray.getJSONObject(0);
        }
      }
      if (jsonContent != null && jsonContent.has(property)) {
        return jsonContent.getString(property);
      }
      return null;
    } catch (JSONException e) {
      throw new OBException(e);
    }

  }
}
