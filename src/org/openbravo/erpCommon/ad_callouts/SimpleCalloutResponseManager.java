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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * SimpleCalloutResponseManager provides the information that is used to populate the messages,
 * comboEntries,etc updated by a SimpleCallout.
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
      log.error("Error: position > 1 is not implemented yet.");
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
  public void manageComboData() {

  }
}
