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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleCalloutInformationProvider provides the information that is used to populate the messages,
 * comboEntries,etc in the FIC. This information is updated by a SimpleCallout.
 * 
 * @author inigo.sanchez
 *
 */
public class SimpleCalloutInformationProvider implements CalloutInformationProvider {

  private JSONObject calloutResult;
  private Iterator<String> keys;
  private String currentElementName;

  private final Logger log = LoggerFactory.getLogger(SimpleCalloutInformationProvider.class);

  @SuppressWarnings("unchecked")
  public SimpleCalloutInformationProvider(JSONObject calloutResult) {
    this.calloutResult = calloutResult;
    this.keys = this.calloutResult.keys();
    this.currentElementName = "";
  }

  @Override
  public String getCurrentElementName() {
    return currentElementName;
  }

  @Override
  public Object getCurrentElementValue(Object element) {
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
        currentElementName = keys.next();
        return calloutResult.getJSONObject(currentElementName);
      }
    } catch (JSONException e) {
      log.error("Error retrieving next element with key {}", currentElementName);
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
  public void manageComboData(Map<String, JSONObject> columnValues, List<String> dynamicCols,
      List<String> changedCols, RequestContext request, Object element, Column col, String colIdent)
      throws JSONException {
    JSONObject firstComboEntry = new JSONObject();
    JSONObject entry = (JSONObject) element;
    JSONArray entryValues = entry.getJSONArray(CalloutConstants.ENTRIES);
    ArrayList<JSONObject> newJsonArr = new ArrayList<JSONObject>();
    JSONObject comboEntry = null;

    if (!entry.has(CalloutConstants.COMBO_SELECTED_VALUE)) {
      // If it is not mandatory and first value is not empty, we add an initial blank element
      if (!col.isMandatory()
          && (entryValues.length() == 0 || !entryValues.getJSONObject(0).isNull(JsonConstants.ID))) {
        comboEntry = new JSONObject();
        comboEntry.put(JsonConstants.ID, (String) null);
        comboEntry.put(JsonConstants.IDENTIFIER, (String) null);
        newJsonArr.add(comboEntry);
      }

      // As we do not have selected value information in the entry, select the first element. For
      // not mandatory columns, this will be a blank element.
      if (newJsonArr.size() > 0 && newJsonArr.get(0).has(JsonConstants.ID)) {
        // create element with selected value
        String selectedValue = newJsonArr.get(0).getString(JsonConstants.ID);
        firstComboEntry.put(CalloutConstants.VALUE, selectedValue);
        firstComboEntry.put(CalloutConstants.CLASSIC_VALUE, selectedValue);
      }

    } else {
      // selected value is chosen
      firstComboEntry.put(CalloutConstants.VALUE, entry.getString(CalloutConstants.VALUE));
      firstComboEntry.put(CalloutConstants.CLASSIC_VALUE,
          entry.getString(CalloutConstants.CLASSIC_VALUE));
    }

    // Added all combo entries
    for (int i = 0; i < entryValues.length(); i++) {
      comboEntry = entryValues.getJSONObject(i);
      newJsonArr.add(comboEntry);
    }

    // added this new value and set parameter into request
    if (firstComboEntry.has(CalloutConstants.CLASSIC_VALUE)) {
      request.setRequestParameter(getCurrentElementName(),
          firstComboEntry.getString(CalloutConstants.CLASSIC_VALUE));
    }

    columnValues.put(colIdent, firstComboEntry);

    if (dynamicCols.contains(getCurrentElementName())) {
      changedCols.add(col.getDBColumnName());
    }

    if (entry.has(CalloutConstants.ENTRIES)) {
      firstComboEntry.put(CalloutConstants.ENTRIES, newJsonArr);
    }
  }
}
