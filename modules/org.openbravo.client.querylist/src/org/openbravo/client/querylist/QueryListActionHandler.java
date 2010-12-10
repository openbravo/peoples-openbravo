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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.querylist;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.client.querylist.QueryListUtils.IncludeIn;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class QueryListActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(QueryListActionHandler.class);
  private static final String GET_FIELDS = "GET_FIELDS";

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    OBContext.setAdminMode();
    try {
      // Retrieve content values
      JSONObject o = new JSONObject(content);
      final String strEventType = o.getString("eventType");
      final String strViewMode = o.getString("viewMode");
      final String strWidgetClass = o.getString("widgetId");
      WidgetClass widgetClass = OBDal.getInstance().get(WidgetClass.class, strWidgetClass);
      log.debug("=== New action, eventType: " + strEventType + " ===");

      if (GET_FIELDS.equals(strEventType)) {
        IncludeIn includeIn = IncludeIn.getIncludeIn(strViewMode);
        o.put("fields", QueryListUtils.getWidgetClassFields(widgetClass, includeIn));
      }
      return o;
    } catch (JSONException e) {
      log.error("Error executing action: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return new JSONObject();
  }
}
