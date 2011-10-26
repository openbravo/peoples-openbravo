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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonUtils;

/**
 * A JSON objects converter.
 * 
 * @author adrianromero
 */
public class JSONRowConverter {

  private final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
      DataToJsonConverter.class);
  private final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
  private final static SimpleDateFormat xmlTimeFormat = JsonUtils.createTimeFormat();
  private String[] fields;

  public JSONRowConverter(String[] fields) {
    this.fields = fields;
  }

  public Object convert(Object obj) throws JSONException {
    return convert(fields, obj);
  }

  private Object convert(String[] fi, Object obj) throws JSONException {

    if (obj instanceof BaseOBObject) {
      return toJsonConverter.toJsonObject((BaseOBObject) obj, DataResolvingMode.FULL);
    } else if (obj instanceof Object[]) {
      if (fi == null) {
        JSONArray row = new JSONArray();
        for (Object o : (Object[]) obj) {
          row.put(convert(null, o));
        }
        return row;
      } else {
        JSONObject item = new JSONObject();
        for (int i = 0; i < fields.length; i++) {
          item.put(fields[i], convert(null, ((Object[]) obj)[i]));
        }
        return item;
      }
    } else {
      return convertPrimitiveValue(obj);
    }
  }

  private Object convertPrimitiveValue(Object value) {
    if (value == null) {
      return JSONObject.NULL;
    } else if (Date.class.isAssignableFrom(value.getClass())) {
      if (value instanceof java.sql.Timestamp) {
        final String formattedValue = xmlDateTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      } else if (value instanceof java.sql.Time) {
        final String formattedValue = xmlTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);        
      } else if (value instanceof java.sql.Date) {
        return xmlDateFormat.format(value);
      } else {
        // Timestamp formating by default
        final String formattedValue = xmlDateTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      }
    } else if (value instanceof byte[]) {
      return Base64.encodeBase64String((byte[]) value);
    } else {
      return value;
    }
  }
}
