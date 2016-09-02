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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.service.json.JsonConstants;

/**
 * SimpleCalloutInformationProvider provides the information that is used to populate the messages,
 * comboEntries,etc in the FIC. These information are updated by a SimpleCallout.
 * 
 * @author inigo.sanchez
 *
 */
public class SimpleCalloutInformationProvider implements CalloutInformationProvider {

  private JSONObject calloutResult;
  private Iterator<String> keys;
  private String elementName;

  @SuppressWarnings("unchecked")
  public SimpleCalloutInformationProvider(JSONObject calloutResult) {
    this.calloutResult = calloutResult;
    this.keys = this.calloutResult.keys();
    this.elementName = "";
  }

  @Override
  public Object getElementName(Object values) {
    return elementName;
  }

  @Override
  public Object getValue(Object element) {
    JSONObject json = (JSONObject) element;
    String value = null;
    try {
      value = json.getString(CalloutConstants.CLASSIC_VALUE);
    } catch (JSONException e) {
      log.error("Error retrieving value from json {}", json);
    }
    return value;
  }

  @Override
  public Object getNextElement() {
    try {
      if (keys.hasNext()) {
        elementName = keys.next();
        return calloutResult.getJSONObject(elementName);
      }
    } catch (JSONException e) {
      log.error("Error retrieving next element with key {}", elementName);
    }
    return null;
  }

  @Override
  public Boolean isComboData(Object element) {
    if (element instanceof JSONObject) {
      JSONObject json = (JSONObject) element;
      return json.has(CalloutConstants.ENTRIES);
    }
    return false;
  }

  @Override
  public boolean manageComboData(Map<String, JSONObject> columnValues, List<String> dynamicCols,
      List<String> changedCols, RequestContext request, Object element, Column col, String colIdent)
      throws JSONException {
    boolean changed = false;
    JSONObject temporalyElement = new JSONObject();
    JSONObject elem = (JSONObject) element;

    // if value is not selected
    if (!elem.has(CalloutConstants.CLASSIC_VALUE)) {
      JSONArray jsonArr = elem.getJSONArray(CalloutConstants.ENTRIES);
      ArrayList<JSONObject> newJsonArr = new ArrayList<JSONObject>();
      JSONObject temporaly = null;

      // If it is not mandatory and first value is not empty, we add an initial blank element
      if (!col.isMandatory() && !jsonArr.getJSONObject(0).isNull(JsonConstants.ID)) {
        temporaly = new JSONObject();
        temporaly.put(JsonConstants.ID, (String) null);
        temporaly.put(JsonConstants.IDENTIFIER, (String) null);
        newJsonArr.add(temporaly);
      }

      for (int i = 0; i < jsonArr.length(); i++) {
        temporaly = jsonArr.getJSONObject(i);
        newJsonArr.add(temporaly);
      }

      if (newJsonArr.get(0).has(JsonConstants.ID)) {
        // create element with selected value
        String valueSelected = newJsonArr.get(0).getString(JsonConstants.ID);
        temporalyElement.put(CalloutConstants.VALUE, valueSelected);
        temporalyElement.put(CalloutConstants.CLASSIC_VALUE, valueSelected);
      }

    } else {
      // value is selected before this parsing
      temporalyElement.put(CalloutConstants.VALUE, elem.getString(CalloutConstants.VALUE));
      temporalyElement.put(CalloutConstants.CLASSIC_VALUE,
          elem.getString(CalloutConstants.CLASSIC_VALUE));
    }

    // added this new value and set parameter into request
    if (temporalyElement.has(CalloutConstants.CLASSIC_VALUE)) {
      request.setRequestParameter((String) this.getElementName(null),
          temporalyElement.getString(CalloutConstants.CLASSIC_VALUE));
    }

    columnValues.put(colIdent, temporalyElement);
    changed = true;
    if (dynamicCols.contains((String) this.getElementName(null))) {
      changedCols.add(col.getDBColumnName());
    }

    if (elem.has(CalloutConstants.ENTRIES)) {
      temporalyElement.put(CalloutConstants.ENTRIES, elem.getJSONArray(CalloutConstants.ENTRIES));
    }
    return changed;
  }
}
