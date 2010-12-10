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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.myob.WidgetInstance;
import org.openbravo.client.myob.WidgetProvider;

/**
 * Responsible for creating the Query/List Widgets.
 * 
 * @author gorkaion
 */
public class QueryListWidgetProvider extends WidgetProvider {

  private static final Logger log = Logger.getLogger(QueryListWidgetProvider.class);

  @Override
  public String generate() {
    return "isc.defineClass('"
        + KernelConstants.ID_PREFIX
        + getWidgetClass().getId()
        + "', isc.OBQueryListWidget).addProperties({widgetId: '"
        + getWidgetClass().getId()
        + "', fields:"
        + QueryListUtils
            .getWidgetClassFields(getWidgetClass(), QueryListUtils.IncludeIn.WidgetView) + "});";
  }

  @Override
  public JSONObject getWidgetClassDefinition() {
    try {
      final JSONObject jsonObject = super.getWidgetClassDefinition();
      final JSONObject parameters = new JSONObject();
      jsonObject.put(WidgetProvider.PARAMETERS, parameters);
      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  public JSONObject getWidgetInstanceDefinition(WidgetInstance widgetInstance) {
    try {
      final JSONObject jsonObject = new JSONObject();
      addDefaultWidgetProperties(jsonObject, widgetInstance);
      final JSONObject parameters = jsonObject.getJSONObject(WidgetProvider.PARAMETERS);
      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}
