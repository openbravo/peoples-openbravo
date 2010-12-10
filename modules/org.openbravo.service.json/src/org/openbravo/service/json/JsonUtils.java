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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.sql.BatchUpdateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jfree.util.Log;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;

/**
 * Contains utility methods used in this module.
 * 
 * @author mtaal
 */
public class JsonUtils {

  /**
   * @return a new instance of the {@link SimpleDateFormat} using a format of yyyy-MM-dd (xml schema
   *         date format). The date format has lenient set to true.
   */
  public static SimpleDateFormat createDateFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * @return a new instance of the {@link SimpleDateFormat} using a format of yyyy-MM-dd'T'HH:mm:ss
   *         (xml schema date time format). The date format has lenient set to true.
   * 
   *         TODO: also encode time zone somehow
   */
  public static SimpleDateFormat createDateTimeFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  /**
   * Gets the value of the {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} in the parameters
   * map and returns it as a list of String, if no parameter is set an empty list is returned.
   * 
   * @param parameters
   *          the parameter map to search for the
   *          {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} parameter
   * @return the values in the {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} parameter
   */
  public static List<String> getAdditionalProperties(Map<String, String> parameters) {
    final String additionalPropertiesString = parameters
        .get(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER);
    if (additionalPropertiesString == null) {
      return Collections.emptyList();
    }
    final List<String> result = new ArrayList<String>();
    for (String additionalProperty : additionalPropertiesString.split(",")) {
      result.add(additionalProperty.trim());
    }
    return result;
  }

  /**
   * Converts an exception to its json represention. Uses the Smartclient format for the json
   * string, see here: <a
   * href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
   * RestDataSource</a>
   * 
   * @param throwable
   *          the exception to convert to json
   * @return the resulting json string
   */
  public static String convertExceptionToJson(Throwable throwable) {
    Throwable localThrowable = throwable;
    if (throwable.getCause() instanceof BatchUpdateException) {
      final BatchUpdateException batchException = (BatchUpdateException) throwable.getCause();
      localThrowable = batchException.getNextException();
    }

    try {
      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, localThrowable.getMessage());
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
      return jsonResult.toString();

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns an empty result for a fetch call
   * 
   * @return the JSON representation of an empty result
   */
  public static String getEmptyResult() {
    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();

    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROWS, "0");
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, "0");
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, "0");
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray());
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return jsonResult.toString();
  }

  /**
   * Determines the list of properties based on the property path (for example
   * bankAccount.bank.name).
   * 
   * @param entity
   *          the entity to start from, the first property in the property path is a property of
   *          this entity
   * @param propertyPath
   *          the property path, i.e. property names separated by dots
   * @return the list of properties determined from the property path
   */
  public static List<Property> getPropertiesOnPath(Entity entity, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    Entity currentEntity = entity;
    Property result = null;
    final List<Property> properties = new ArrayList<Property>();

    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      if (part.equals(JsonConstants.IDENTIFIER)) {
        // pick the first identifier property
        if (currentEntity.getIdentifierProperties().isEmpty()) {
          properties.add(currentEntity.getIdProperties().get(0));
        } else {
          properties.add(currentEntity.getIdentifierProperties().get(0));
        }
        return properties;
      }
      if (!currentEntity.hasProperty(part)) {
        Log.warn("Property " + part + " not found in entity " + currentEntity);
        return properties;
      }
      result = currentEntity.getProperty(part);
      properties.add(result);
      if (result.getTargetEntity() != null) {
        currentEntity = result.getTargetEntity();
      }
    }
    return properties;
  }

}
