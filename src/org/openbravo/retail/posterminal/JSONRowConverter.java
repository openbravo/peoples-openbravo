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
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
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
  private DataResolvingMode mode;

  public JSONRowConverter(String[] fields, DataResolvingMode mode) {
    this.fields = fields;
    this.mode = mode;
  }

  public JSONRowConverter(String[] fields) {
    this.fields = fields;
    this.mode = DataResolvingMode.FULL;
  }

  public JSONRowConverter(DataResolvingMode mode) {
    this.fields = new String[0];
    this.mode = mode;
  }

  public JSONRowConverter() {
    this.fields = new String[0];
    this.mode = DataResolvingMode.FULL;
  }

  public Object convert(Object obj) throws JSONException {
    return convert(fields, obj);
  }

  private Object convert(String[] fi, Object obj) throws JSONException {

    if (obj instanceof BaseOBObject) {
      return toJsonConverter.toJsonObject((BaseOBObject) obj, mode);
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
      if (fi == null || fi.length == 0) {
        return convertPrimitiveValue(obj);
      } else {
        JSONObject item = new JSONObject();
        item.put(fi[0], convertPrimitiveValue(obj));
        return item;
      }
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

  public static JSONObject buildResponse(List<?> listdata, String[] aliases) throws JSONException {

    JSONRowConverter converter = new JSONRowConverter(aliases);

    final int startRow = 0;
    final JSONObject jsonResponse = new JSONObject();
    final JSONArray jsonData = new JSONArray();

    for (Object o : listdata) {
      jsonData.put(converter.convert(o));
    }

    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, (jsonData.length() > 0 ? jsonData.length()
        + startRow - 1 : 0));

    if (jsonData.length() == 0) {
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
    }

    jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return jsonResponse;
  }
}
