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
package org.openbravo.erpCommon.ad_callouts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.service.json.JsonConstants;

/**
 * SimpleCalloutResponseManager provides the information that is used to populate the messages,
 * comboEntries,etc in the FIC. These information are updated by a SimpleCallout.
 * 
 * @author inigo.sanchez
 *
 */
public class SimpleCalloutResponseManager implements CalloutInformationProvider {

  JSONObject returnedJSONObject;
  String currentElement = null;

  public SimpleCalloutResponseManager(JSONObject json) {
    returnedJSONObject = json;
  }

  public JSONObject getJSONResult() {
    return returnedJSONObject;
  }

  public String getCurrentElement() {
    return currentElement;
  }

  public void setCurrentElement(String nameElement) {
    currentElement = nameElement;
  }

  @Override
  public Object getNameElement(Object values) {
    return currentElement;
  }

  public Object getValue(Object values, int position) {
    if (position == 1) {
      JSONObject json = (JSONObject) values;
      String value = null;
      try {
        value = json.getString(SimpleCalloutConstants.CLASSIC_VALUE);
      } catch (JSONException e) {
        log.error("Error parsing JSON Object.", e);
      }
      return value;
    } else {
      log.warn("Method getValue() can't manage position value bigger and lower than 1.");
      return null;
    }
  }

  @Override
  public Boolean isComboData(Object values) {
    Boolean isCombo = false;
    if (values instanceof org.codehaus.jettison.json.JSONObject) {
      JSONObject json = (JSONObject) values;
      if (json.has(SimpleCalloutConstants.ENTRIES)) {
        isCombo = true;
      }
    }
    return isCombo;
  }

  @Override
  public boolean manageComboData(Map<String, JSONObject> columnValues, List<String> dynamicCols,
      List<String> changedCols, RequestContext rq, Object element,
      CalloutInformationProvider calloutResponse, Column col, String colId) throws JSONException {

    boolean changed = false;
    JSONObject temporalyElement = new JSONObject();
    JSONObject elem = (JSONObject) element;

    // if value is not selected
    if (!elem.has(SimpleCalloutConstants.CLASSIC_VALUE)) {
      JSONArray jsonArr = elem.getJSONArray(SimpleCalloutConstants.ENTRIES);
      ArrayList<JSONObject> newJsonArr = new ArrayList<JSONObject>();
      JSONObject temporal = null;

      // If column is not mandatory and first value is not empty, we add an initial blank
      // element
      if (!col.isMandatory() && !jsonArr.getJSONObject(0).isNull(JsonConstants.ID)) {
        temporal = new JSONObject();
        temporal.put(JsonConstants.ID, (String) null);
        temporal.put(JsonConstants.IDENTIFIER, (String) null);
        newJsonArr.add(temporal);
      }

      for (int i = 0; i < jsonArr.length(); i++) {
        temporal = jsonArr.getJSONObject(i);
        newJsonArr.add(temporal);
      }

      if (newJsonArr.get(0).has(JsonConstants.ID)) {
        // create element with selected value
        String valueSelected = newJsonArr.get(0).getString(JsonConstants.ID);
        temporalyElement.put(SimpleCalloutConstants.VALUE, valueSelected);
        temporalyElement.put(SimpleCalloutConstants.CLASSIC_VALUE, valueSelected);
      }

    } else {
      // value is selected before this parsing
      temporalyElement.put(SimpleCalloutConstants.VALUE,
          elem.getString(SimpleCalloutConstants.VALUE));
      temporalyElement.put(SimpleCalloutConstants.CLASSIC_VALUE,
          elem.getString(SimpleCalloutConstants.CLASSIC_VALUE));
    }

    // added this new value and set parameter into request
    if (temporalyElement.has(SimpleCalloutConstants.CLASSIC_VALUE)) {
      rq.setRequestParameter((String) calloutResponse.getNameElement(null),
          temporalyElement.getString(SimpleCalloutConstants.CLASSIC_VALUE));
    }

    columnValues.put("inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName()), temporalyElement);
    changed = true;
    if (dynamicCols.contains((String) calloutResponse.getNameElement(null))) {
      changedCols.add(col.getDBColumnName());
    }

    if (elem.has(SimpleCalloutConstants.ENTRIES)) {
      temporalyElement.put(SimpleCalloutConstants.ENTRIES,
          elem.getJSONArray(SimpleCalloutConstants.ENTRIES));
    }

    return changed;
  }
}
