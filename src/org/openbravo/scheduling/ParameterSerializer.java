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

import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A class that allows to serialize and deserialize the parameters of a {@link ProcessBundle}.
 */
class ParameterSerializer {

  private static ParameterSerializer instance = new ParameterSerializer();

  static ParameterSerializer getInstance() {
    return instance;
  }

  String serialize(Map<String, Object> parameters) {
    if (parameters.isEmpty()) {
      return "";
    }
    JSONObject json = new JSONObject();
    parameters.entrySet()
        .stream()
        .map(ProcessBundleParameter::new)
        .forEach(parameter -> parameter.serialize(json));
    return json.toString();
  }

  Map<String, Object> deserialize(String parameters) {
    JSONObject json;
    try {
      json = new JSONObject(parameters);
    } catch (JSONException e) {
      throw new ParameterSerializationException(
          "Could not deserialize map of parameters: " + parameters);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> pbParams = (Map<String, Object>) StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(json.keys(), 0), false)
        .map(key -> new ProcessBundleParameter((String) key, json))
        .collect(
            Collectors.toMap(ProcessBundleParameter::getName, ProcessBundleParameter::deserialize));

    return pbParams;
  }
}
