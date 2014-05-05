package org.openbravo.advpaymentmngt.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.service.datasource.DefaultDataSourceService;
import org.openbravo.service.json.JsonConstants;

public class GLItemDatasource extends DefaultDataSourceService {

  private static final String AD_TABLE_ID = "864A35C8FCD548B0AD1D69C89BBA6118";

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public String fetch(Map<String, String> parameters) {
    int startRow = 0;
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }

    final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() + startRow - 1);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (JSONException e) {
    }

    return jsonResult.toString();
  }

  @Override
  public String add(Map<String, String> parameters, String content) {
    return content;

  }
}
