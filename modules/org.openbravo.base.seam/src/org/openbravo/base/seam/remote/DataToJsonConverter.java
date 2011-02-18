/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

package org.openbravo.base.seam.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.ActiveEnabled;
import org.openbravo.base.structure.BaseOBObject;

/**
 * Is responsible for converting data to a json representation. The data can be a primitive type or
 * a business objects or lists of this same information.
 * 
 * @author mtaal
 */
@Name("dataToJsonConverter")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Install(precedence = Install.FRAMEWORK)
public class DataToJsonConverter {

  public static final String REF_SEPARATOR = "/";

  /**
   * Converts any type of object to a json representation. {@link BaseOBObject} instances are
   * converted to JSONObject, collections to JSONArray instances. Uses DataResolvingMode:
   * {@link DataResolvingMode#FULL}. Calls
   * {@link #convertToJsonObject(BaseOBObject, DataResolvingMode)}.
   * 
   * @param object
   *          the object to convert.
   * @return a json string
   */
  public String convertToJsonString(Object object) {
    return convertToJsonString(object, DataResolvingMode.FULL);
  }

  /**
   * Converts any type of object to a json representation. {@link BaseOBObject} instances are
   * converted to JSONObject, collections to JSONArray instances.
   * 
   * @param object
   *          the object to convert.
   * @param dataResolvingMode
   *          {@link DataResolvingMode#FULL} means that business objects are fully converted (all
   *          the properties), {@link DataResolvingMode#SHORT} means that only id and identifier
   *          properties are returned.
   * @return a json string
   */
  public String convertToJsonString(Object object, DataResolvingMode dataResolvingMode) {
    try {
      if (object == null) {
        return JSONObject.NULL.toString();
      }
      if (object instanceof Collection<?>) {
        return new JSONArray((Collection<?>) convertToJsonValue(object)).toString();
      } else if (object instanceof Map<?, ?>) {
        return new JSONObject((Map<?, ?>) convertToJsonValue(object)).toString();
      } else if (object instanceof BaseOBObject) {
        return convertToJsonObject((BaseOBObject) object, dataResolvingMode).toString();
      } else {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(convertPrimitiveValue(object));
        return jsonArray.toString();
      }
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  protected Object convertToJsonValue(Object object) throws JSONException {
    if (object == null) {
      return JSONObject.NULL;
    }
    if (object instanceof Collection<?>) {
      final Collection<?> collection = (Collection<?>) object;
      final List<Object> jsonValues = new ArrayList<Object>();
      for (Object o : collection) {
        jsonValues.add(convertToJsonValue(o));
      }
      return jsonValues;
    } else if (object instanceof Map<?, ?>) {
      final Map<?, ?> map = (Map<?, ?>) object;
      final Map<Object, Object> jsonValues = new HashMap<Object, Object>();
      for (Object key : map.keySet()) {
        jsonValues.put(key, convertToJsonValue(map.get(key)));
      }
      return jsonValues;
    } else if (object instanceof BaseOBObject) {
      return convertToJsonObject((BaseOBObject) object, DataResolvingMode.FULL);
    } else {
      return object;
    }
  }

  public JSONObject convertToJsonObject(BaseOBObject bob, DataResolvingMode dataResolvingMode)
      throws JSONException {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("identifier", bob.getIdentifier());
    jsonObject.put("entityName", bob.getEntityName());
    jsonObject.put("$ref", encodeReference(bob));
    if (dataResolvingMode == DataResolvingMode.SHORT) {
      jsonObject.put("id", bob.getId());
      if (bob instanceof ActiveEnabled) {
        jsonObject.put("active", ((ActiveEnabled) bob).isActive());
      }
      return jsonObject;
    }

    for (Property p : bob.getEntity().getProperties()) {
      if (p.isOneToMany()) {
        // ignore these for now....
        continue;
      }
      final Object value = bob.get(p.getName());
      if (value != null) {
        if (p.isPrimitive()) {
          // TODO: format!
          jsonObject.put(p.getName(), convertPrimitiveValue(value));
        } else {
          if (value == null) {
            jsonObject.put(p.getName(), JSONObject.NULL);
          } else {
            jsonObject.put(p.getName(), convertToJsonObject((BaseOBObject) value,
                DataResolvingMode.SHORT));
          }
        }
      }
    }
    return jsonObject;
  }

  // TODO: do some form of formatting here?
  protected Object convertPrimitiveValue(Object value) {
    return value;
  }

  protected String encodeReference(BaseOBObject bob) {
    return bob.getEntityName() + REF_SEPARATOR + bob.getId();
  }
}
