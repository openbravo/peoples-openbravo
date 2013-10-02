package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.json.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TreeDatasourceService extends DefaultDataSourceService {
  final static Logger log = LoggerFactory.getLogger(TreeDatasourceService.class);
  final static String JSON_PREFIX = "<SCRIPT>//'\"]]>>isc_JSONResponseStart>>";
  final static String JSON_SUFFIX = "//isc_JSONResponseEnd";

  @Override
  public String add(Map<String, String> parameters, String content) {

    try {
      // We can't get the bob from DAL, it has not been saved yet
      JSONObject bobProperties = new JSONObject(parameters.get("jsonBob"));
      addNewNode(bobProperties);
    } catch (Exception e) {
      log.error("Error while adding the tree node", e);
    }

    // This is called from TreeTablesEventHandler, no need to return anything
    return "";
  }

  protected abstract void addNewNode(JSONObject bobProperties);

  @Override
  public String remove(Map<String, String> parameters) {

    try {
      // We can't get the bob from DAL, it has not been saved yet
      JSONObject bobProperties = new JSONObject(parameters.get("jsonBob"));
      this.deleteNode(bobProperties);
    } catch (Exception e) {
      log.error("Error while deleting tree node: ", e);
      throw new OBException("The treenode could not be created");
    }

    // This is called from TreeTablesEventHandler, no need to return anything
    return "";
  }

  protected abstract void deleteNode(JSONObject bobProperties);

  @Override
  public String fetch(Map<String, String> parameters) {
    OBContext.setAdminMode(true);
    final JSONObject jsonResult = new JSONObject();
    try {
      JSONArray responseData = null;
      JSONArray selectedNodes = null;
      if (parameters.containsKey("selectedRecords")) {
        selectedNodes = new JSONArray(parameters.get("selectedRecords"));
      }
      if (selectedNodes != null && selectedNodes.length() > 0) {
        responseData = fetchSelectedNodes(parameters, selectedNodes);
      } else {
        String parentId = parameters.get("parentId");
        responseData = fetchNodeChildren(parameters, parentId);
      }
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_DATA, responseData);
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, responseData.length());
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, responseData.length() - 1);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

    } catch (Throwable t) {
      log.error("Error on tree datasource", t);
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonResult.toString();
  }

  private JSONArray fetchSelectedNodes(Map<String, String> parameters, JSONArray selectedNodes) {
    JSONArray responseData = new JSONArray();
    try {
      ArrayList<String> addedNodes = new ArrayList<String>();
      ArrayList<String> parentsToExpand = new ArrayList<String>();
      int maxLevel = -1;
      for (int i = 0; i < selectedNodes.length(); i++) {
        String nodeId = selectedNodes.getString(i);
        JSONObject node = getJSONObjectByNodeId(parameters, nodeId);
        if (!addedNodes.contains(node.getString("id"))) {
          addedNodes.add(node.getString("id"));
          responseData.put(node);
        }
        int level = 0;
        while (node.has("parentId") && !"0".equals(node.getString("parentId"))) {
          nodeId = node.getString("parentId");
          node = getJSONObjectByNodeId(parameters, nodeId);
          node.put("isOpen", true);
          if (!parentsToExpand.contains(node.getString("id"))) {
            parentsToExpand.add(node.getString("id"));
          }
          if (!addedNodes.contains(node.getString("id"))) {
            addedNodes.add(node.getString("id"));
            responseData.put(node);
          }
          level++;
        }
        if (level > maxLevel) {
          maxLevel = level;
        }
      }
      // Expand all the parents
      for (String parentId : parentsToExpand) {
        JSONArray nodeChildren = this.fetchNodeChildren(parameters, parentId);
        for (int i = 0; i < nodeChildren.length(); i++) {
          JSONObject node = nodeChildren.getJSONObject(i);
          if (!addedNodes.contains(node.getString("id"))) {
            addedNodes.add(node.getString("id"));
            responseData.put(node);
          }
        }
      }
    } catch (JSONException e) {
      log.error("Error on tree datasource", e);
    }
    return responseData;
  }

  protected abstract JSONObject getJSONObjectByNodeId(Map<String, String> parameters, String nodeId);

  protected abstract JSONArray fetchNodeChildren(Map<String, String> parameters, String parentId)
      throws JSONException;

  @Override
  public String update(Map<String, String> parameters, String content) {

    OBContext.setAdminMode(true);
    String response = null;
    try {
      final JSONObject jsonObject = new JSONObject(content);
      if (content == null) {
        return "";
      }
      String prevNodeId = parameters.get("prevNodeId");
      String nextNodeId = parameters.get("nextNodeId");

      if (jsonObject.has("data")) {
        response = processNodeMovement(parameters, jsonObject.getJSONObject("data"), prevNodeId,
            nextNodeId);
      } else if (jsonObject.has("transaction")) {
        JSONArray jsonResultArray = new JSONArray();
        JSONObject transaction = jsonObject.getJSONObject("transaction");
        JSONArray operations = transaction.getJSONArray("operations");
        for (int i = 0; i < operations.length(); i++) {
          JSONObject operation = operations.getJSONObject(i);
          jsonResultArray.put(processNodeMovement(parameters, operation.getJSONObject("data"),
              prevNodeId, nextNodeId));
        }
        response = JSON_PREFIX + jsonResultArray.toString() + JSON_SUFFIX;
      }

    } catch (Exception e) {
      log.error("Error while moving tree node", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return response;
  }

  private String processNodeMovement(Map<String, String> parameters, JSONObject data,
      String prevNodeId, String nextNodeId) throws Exception {
    String nodeId = data.getString("id");
    String newParentId = data.getString("parentId");

    JSONObject jsonResult = new JSONObject();
    JSONObject jsonResponse = new JSONObject();
    JSONArray dataResponse = new JSONArray();

    moveNode(parameters, nodeId, newParentId, prevNodeId, nextNodeId);

    dataResponse.put(data);
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put(JsonConstants.RESPONSE_DATA, dataResponse);
    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 1);
    jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    return jsonResult.toString();
  }

  protected abstract void moveNode(Map<String, String> parameters, String nodeId,
      String newParentId, String prevNodeId, String nextNodeId) throws Exception;

}
