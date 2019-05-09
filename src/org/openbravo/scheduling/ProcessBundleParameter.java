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
 * All portions are Copyright (C) 2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling;

import java.util.Map.Entry;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A class used by the {@link ParameterSerializer} to handle the serialization and deserialization
 * of the {@link ProcessBundle} parameters.
 */
class ProcessBundleParameter {

  private String name;
  private Object value;

  ProcessBundleParameter(Entry<String, Object> parameter) {
    this.name = parameter.getKey();
    this.value = parameter.getValue();
  }

  ProcessBundleParameter(String parameterName, JSONObject jsonObject) {
    this.name = parameterName;
    try {
      this.value = jsonObject.get(parameterName);
    } catch (JSONException ex) {
      // It should not happen as the key will always exists
    }
  }

  boolean isSupportedType() {
    return value instanceof String;
  }

  String getName() {
    return name;
  }

  void serialize(JSONObject jsonObject) {
    if (!isSupportedType()) {
      throw new ParameterSerializationException("Could not serialize parameter: " + this);
    }
    try {
      jsonObject.put(name, value);
    } catch (JSONException ex) {
      throw new ParameterSerializationException("Could not serialize parameter: " + this);
    }
  }

  Object deserialize() {
    if (!isSupportedType()) {
      throw new ParameterSerializationException("Could not deserialize parameter: " + this);
    }
    return value.toString();
  }

  @Override
  public String toString() {
    if (name != null) {
      return "[name = " + name + " value = " + value.toString() + " class = "
          + value.getClass().getName() + "]";
    } else {
      return "[name = " + name + " value = null]";
    }
  }

}
