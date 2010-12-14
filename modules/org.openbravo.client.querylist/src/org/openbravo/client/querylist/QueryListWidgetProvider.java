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
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.myob.WidgetInstance;
import org.openbravo.client.myob.WidgetProvider;

/**
 * Responsible for creating the Query/List Widgets.
 * 
 * @author gorkaion
 */
public class QueryListWidgetProvider extends WidgetProvider {

  private static String GRID_PROPERTIES_REFERENCE = "B36DF126DF5F4077A37F1E5B963AA636";
  private static final Logger log = Logger.getLogger(QueryListWidgetProvider.class);
  private static final String HEIGHT = "height";
  private static final Long ROW_HEIGHT = 23L;
  private static final Long STATIC_HEIGHT = 80L;

  @Override
  public String generate() {
    JSONObject gridPropertiesObject = null;
    for (Parameter parameter : getWidgetClass().getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
      // fixed parameters are not part of the fielddefinitions
      if (parameter.getReferenceSearchKey() != null
          && parameter.getReferenceSearchKey().getId().equals(GRID_PROPERTIES_REFERENCE)) {
        try {
          gridPropertiesObject = new JSONObject(parameter.getFixedValue());
        } catch (Exception e) {
          // ignore, invalid grid properties
          log.error("Grid properties parameter " + parameter + " has an illegal format "
              + e.getMessage(), e);
        }
      }
    }
    String gridProperties = (gridPropertiesObject == null ? "" : ", gridProperties: "
        + gridPropertiesObject.toString());
    return "isc.defineClass('"
        + KernelConstants.ID_PREFIX
        + getWidgetClass().getId()
        + "', isc.OBQueryListWidget).addProperties({widgetId: '"
        + getWidgetClass().getId()
        + "', fields:"
        + QueryListUtils
            .getWidgetClassFields(getWidgetClass(), QueryListUtils.IncludeIn.WidgetView)
        + gridProperties + "});";
  }

  public JSONObject getWidgetInstanceDefinition(WidgetInstance widgetInstance) {
    try {
      final JSONObject jsonObject = super.getWidgetInstanceDefinition(widgetInstance);
      jsonObject.put("widgetInstanceId", widgetInstance.getId());

      Long height = widgetInstance.getWidgetClass().getHeight();

      for (ParameterValue value : widgetInstance
          .getOBUIAPPParameterValueEMObkmoWidgetInstanceIDList()) {
        if ("RowsNumber".equals(value.getParameter().getDBColumnName())) {
          height = value.getValueNumber().longValue() * ROW_HEIGHT;
          height += STATIC_HEIGHT;
        }
      }

      jsonObject.put(HEIGHT, height);

      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}
