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
import org.mozilla.javascript.NativeArray;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.service.json.JsonConstants;

/**
 * HttpServletCalloutInformationProvider provides the information that is used to populate the
 * messages, comboEntries,etc in the FIC. These information are updated by a HttpServlet Callout.
 * 
 * @author inigo.sanchez
 *
 */
public class HttpServletCalloutInformationProvider implements CalloutInformationProvider {

  private ArrayList<NativeArray> returnedArray;

  public HttpServletCalloutInformationProvider(ArrayList<NativeArray> nativeArray) {
    returnedArray = nativeArray;
  }

  public ArrayList<NativeArray> getNativeArray() {
    return returnedArray;
  }

  @Override
  public Object getElementName(Object values) {
    NativeArray element = (NativeArray) values;
    return element.get(0, null);
  }

  @Override
  public Object getValue(Object values) {
    return getValue(values, 1);
  }

  private Object getValue(Object values, int position) {
    NativeArray element = (NativeArray) values;
    return element.get(position, null);
  }

  @Override
  public Boolean isComboData(Object values) {
    if (values instanceof NativeArray) {
      NativeArray element = (NativeArray) values;
      return element.get(1, null) instanceof NativeArray;
    }
    return false;
  }

  @Override
  public boolean manageComboData(Map<String, JSONObject> columnValues, List<String> dynamicCols,
      List<String> changedCols, RequestContext request, Object element, Column col, String colIdent)
      throws JSONException {
    boolean changed = false;
    NativeArray subelements = (NativeArray) this.getValue(element);
    JSONObject jsonobject = new JSONObject();
    ArrayList<JSONObject> comboEntries = new ArrayList<JSONObject>();
    // If column is not mandatory, we add an initial blank element
    if (!col.isMandatory()) {
      JSONObject entry = new JSONObject();
      entry.put(JsonConstants.ID, (String) null);
      entry.put(JsonConstants.IDENTIFIER, (String) null);
      comboEntries.add(entry);
    }
    for (int j = 0; j < subelements.getLength(); j++) {
      NativeArray subelement = (NativeArray) getValue(subelements, j);
      if (subelement != null && getValue(subelement, 2) != null) {
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, this.getElementName(subelement));
        entry.put(JsonConstants.IDENTIFIER, this.getValue(subelement));
        comboEntries.add(entry);
        if ((j == 0 && col.isMandatory())
            || getValue(subelement, 2).toString().equalsIgnoreCase("True")) {
          // If the column is mandatory, we choose the first value as selected
          // In any case, we select the one which is marked as selected "true"
          UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(col.getId());
          String newValue = this.getElementName(subelement).toString();

          jsonobject.put("value", newValue);
          jsonobject.put("classicValue", uiDef.convertToClassicString(newValue));
          request.setRequestParameter(colIdent, uiDef.convertToClassicString(newValue));
          log.debug("Column: " + col.getDBColumnName() + "  Value: " + newValue);
        }
      }
    }
    // If the callout returns a combo, we in any case set the new value with what
    // the callout returned
    columnValues.put(colIdent, jsonobject);
    changed = true;
    if (dynamicCols.contains(colIdent)) {
      changedCols.add(col.getDBColumnName());
    }
    jsonobject.put("entries", new JSONArray(comboEntries));

    return changed;
  }
}
