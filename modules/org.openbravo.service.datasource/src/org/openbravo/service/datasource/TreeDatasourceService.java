package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.ReferencedTree;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.TableTree;
import org.openbravo.service.datasource.CheckTreeOperationManager.ActionResponse;
import org.openbravo.service.json.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TreeDatasourceService extends DefaultDataSourceService {
  final static Logger log = LoggerFactory.getLogger(TreeDatasourceService.class);
  final static String JSON_PREFIX = "<SCRIPT>//'\"]]>>isc_JSONResponseStart>>";
  final static String JSON_SUFFIX = "//isc_JSONResponseEnd";

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  final String ROOT_NODE = "0";

  @Inject
  @Any
  private Instance<CheckTreeOperationManager> checkTreeOperationManagers;

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
      String parentId = parameters.get("parentId");
      String tabId = parameters.get("tabId");
      String treeReferenceId = parameters.get("treeReferenceId");
      Tab tab = null;
      Table table = null;
      String hqlTreeWhereClause = null;
      String hqlTreeWhereClauseRootNodes = null;
      if (tabId != null) {
        tab = OBDal.getInstance().get(Tab.class, tabId);
        table = tab.getTable();
        hqlTreeWhereClause = tab.getHqlwhereclause();
        hqlTreeWhereClauseRootNodes = tab.getHQLWhereClauseForRootNodes();
      } else if (treeReferenceId != null) {
        ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class,
            treeReferenceId);
        table = treeReference.getTable();
        hqlTreeWhereClause = treeReference.getHQLSQLWhereClause();
        hqlTreeWhereClauseRootNodes = treeReference.getHQLWhereClauseForRootNodes();
      } else {
        log.error("A request to the TreeDatasourceService must include the tabId or the treeReferenceId parameter");
        final JSONObject jsonResponse = new JSONObject();
        JSONArray responseData = new JSONArray();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
        jsonResponse.put(JsonConstants.RESPONSE_DATA, responseData);
        jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
        jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
        jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 0);
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
        return jsonResult.toString();
      }
      if (hqlTreeWhereClause != null) {
        hqlTreeWhereClause = this.substituteParameters(hqlTreeWhereClause, parameters);
      }

      if (hqlTreeWhereClauseRootNodes != null) {
        hqlTreeWhereClauseRootNodes = this.substituteParameters(hqlTreeWhereClauseRootNodes,
            parameters);
      }

      if (parameters.containsKey(JsonConstants.DISTINCT_PARAMETER)) {
        Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
        DataSourceService dataSource = dataSourceServiceProvider.getDataSource(entity.getName());
        return dataSource.fetch(parameters);
      }

      JSONArray responseData = null;

      boolean tooManyNodes = false;

      if (parameters.containsKey("criteria") && parentId.equals(ROOT_NODE)) {
        try {
          List<String> filteredNodes = getFilteredNodes(table, parameters);
          if (!filteredNodes.isEmpty()) {
            // Fetch only the filtered nodes and its parents (filtered tree)
            responseData = fetchFilteredNodes(parameters, filteredNodes);
          } else {
            responseData = new JSONArray();
          }
        } catch (TooManyTreeNodesException e) {
          tooManyNodes = true;
        }
      } else {
        // Fetch the node children of a given parent
        try {
          responseData = fetchNodeChildren(parameters, parentId, hqlTreeWhereClause,
              hqlTreeWhereClauseRootNodes);
        } catch (TooManyTreeNodesException e) {
          tooManyNodes = true;
        }
      }

      final JSONObject jsonResponse = new JSONObject();
      if (tooManyNodes) {
        responseData = new JSONArray();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
        final JSONObject error = new JSONObject();
        error.put("type", "tooManyNodes");
        jsonResponse.put(JsonConstants.RESPONSE_ERROR, error);
        jsonResponse.put(JsonConstants.RESPONSE_ERRORS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      } else {
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
      jsonResponse.put(JsonConstants.RESPONSE_DATA, responseData);
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

  private List<String> getFilteredNodes(Table table, Map<String, String> parameters)
      throws TooManyTreeNodesException {
    List<String> filteredNodes = new ArrayList<String>();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    DataSourceService dataSource = dataSourceServiceProvider.getDataSource(entity.getName());
    String dsResult = dataSource.fetch(parameters);
    try {
      JSONObject jsonDsResult = new JSONObject(dsResult);
      JSONObject jsonResponse = jsonDsResult.getJSONObject(JsonConstants.RESPONSE_RESPONSE);
      JSONArray dataArray = jsonResponse.getJSONArray(JsonConstants.RESPONSE_DATA);
      int nRecords = dataArray.length();

      OBContext context = OBContext.getOBContext();
      int nMaxResults = -1;
      try {
        nMaxResults = Integer.parseInt(Preferences.getPreferenceValue("TreeDatasourceFetchLimit",
            false, context.getCurrentClient(), context.getCurrentOrganization(), context.getUser(),
            context.getRole(), null));
      } catch (Exception e) {
        nMaxResults = 1000;
      }
      if (nRecords >= nMaxResults) {
        throw new TooManyTreeNodesException();
      }

      for (int i = 0; i < nRecords; i++) {
        JSONObject data = dataArray.getJSONObject(i);
        String ref = data.getString("$ref");
        String id = ref.substring(ref.lastIndexOf("/") + 1);
        filteredNodes.add(id);
      }
    } catch (JSONException e) {
      log.error("Error while getting the filtered nodes from the datasource", e);
    }
    return filteredNodes;
  }

  private JSONArray fetchFilteredNodes(Map<String, String> parameters, List<String> filteredNodes)
      throws MultipleParentsException {
    JSONArray responseData = new JSONArray();
    String tabId = parameters.get("tabId");
    String treeReferenceId = parameters.get("treeReferenceId");
    Tab tab = null;
    TableTree tableTree = null;
    String hqlTreeWhereClause = null;
    String hqlTreeWhereClauseRootNodes = null;
    if (tabId != null) {
      tab = OBDal.getInstance().get(Tab.class, tabId);
      tableTree = tab.getTableTree();
      hqlTreeWhereClause = tab.getHqlwhereclause();
      hqlTreeWhereClauseRootNodes = tab.getHQLWhereClauseForRootNodes();
    } else if (treeReferenceId != null) {
      ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class, treeReferenceId);
      tableTree = treeReference.getTableTreeCategory();
      hqlTreeWhereClause = treeReference.getHQLSQLWhereClause();
      hqlTreeWhereClauseRootNodes = treeReference.getHQLWhereClauseForRootNodes();
    } else {
      log.error("A request to the TreeDatasourceService must include the tabId or the treeReferenceId parameter");
      return responseData;
    }
    if (hqlTreeWhereClause != null) {
      hqlTreeWhereClause = this.substituteParameters(hqlTreeWhereClause, parameters);
    }
    if (hqlTreeWhereClauseRootNodes != null) {
      hqlTreeWhereClauseRootNodes = this.substituteParameters(hqlTreeWhereClauseRootNodes,
          parameters);
    }
    try {

      boolean allowNotApplyingWhereClauseToChildren = false;
      try {
        allowNotApplyingWhereClauseToChildren = "Y".equals(Preferences.getPreferenceValue(
            "AllowNotApplyingWhereClauseToChildNodes", true, "0", "0", null, null, null));
      } catch (PropertyException e) {
      }

      Map<String, JSONObject> addedNodesMap = new HashMap<String, JSONObject>();
      for (String nodeId : filteredNodes) {
        JSONObject node = getJSONObjectByRecordId(parameters, nodeId);

        if (!allowNotApplyingWhereClauseToChildren
            && !this.nodeConformsToWhereClause(tableTree, node.getString("id"), hqlTreeWhereClause)) {
          continue;
        }

        JSONObject savedNode = addedNodesMap.get(node.getString("id"));

        if (hqlTreeWhereClauseRootNodes != null) {
          Map<String, JSONObject> preAddedNodesMap = new HashMap<String, JSONObject>();
          if (savedNode == null) {
            node.put("filterHit", true);
            preAddedNodesMap.put(node.getString("id"), node);
          } else {
            savedNode.put("filterHit", true);
          }
          while (node.has("parentId")
              && !isRoot(node, hqlTreeWhereClauseRootNodes, tableTree)
              && (allowNotApplyingWhereClauseToChildren || this.nodeConformsToWhereClause(
                  tableTree, node.getString("parentId"), hqlTreeWhereClause))) {
            nodeId = node.getString("parentId");
            node = getJSONObjectByNodeId(parameters, nodeId);
            savedNode = addedNodesMap.get(node.getString("id"));
            if (savedNode == null) {
              node.put("isOpen", true);
              preAddedNodesMap.put(node.getString("id"), node);
            } else {
              savedNode.put("isOpen", true);
            }
          }
          // We have to make sure that the filtered node was not aboute the
          // root nodes as defined by the hqlTreeWhereClauseRootNodes
          if (this.nodeConformsToWhereClause(tableTree, node.getString("id"),
              hqlTreeWhereClauseRootNodes)) {
            addedNodesMap.putAll(preAddedNodesMap);
          }
        } else {
          if (savedNode == null) {
            node.put("filterHit", true);
            addedNodesMap.put(node.getString("id"), node);
          } else {
            savedNode.put("filterHit", true);
          }
          while (node.has("parentId")
              && !ROOT_NODE.equals(node.get("parentId"))
              && (allowNotApplyingWhereClauseToChildren || this.nodeConformsToWhereClause(
                  tableTree, node.getString("parentId"), hqlTreeWhereClause))) {
            nodeId = node.getString("parentId");
            node = getJSONObjectByNodeId(parameters, nodeId);
            savedNode = addedNodesMap.get(node.getString("id"));
            if (savedNode == null) {
              node.put("isOpen", true);
              addedNodesMap.put(node.getString("id"), node);
            } else {
              savedNode.put("isOpen", true);
            }
          }
        }

        if (allowNotApplyingWhereClauseToChildren
            || this.nodeConformsToWhereClause(tableTree, node.getString("parentId"),
                hqlTreeWhereClause)) {
          node.put("parentId", ROOT_NODE);
        }
      }

      // Add the values in the map to responsedata
      for (String key : addedNodesMap.keySet()) {
        if (addedNodesMap.get(key).has("filterHit")) {
          addedNodesMap.get(key).remove("filterHit");
        } else {
          addedNodesMap.get(key).put("notFilterHit", true);
        }
        responseData.put(addedNodesMap.get(key));
      }

    } catch (JSONException e) {
      log.error("Error on tree datasource", e);
    }
    return responseData;
  }

  private boolean isRoot(JSONObject node, String hqlTreeWhereClauseRootNodes, TableTree tableTree) {
    try {
      String nodeId = null;
      String parentId = null;
      nodeId = node.getString("id");
      parentId = node.getString("parentId");
      if (ROOT_NODE.equals(parentId)) {
        return true;
      }
      if (hqlTreeWhereClauseRootNodes != null) {
        return nodeConformsToWhereClause(tableTree, nodeId, hqlTreeWhereClauseRootNodes);
      } else {
        return false;
      }
    } catch (JSONException e) {
      return false;
    }
  }

  protected abstract boolean nodeConformsToWhereClause(TableTree tableTree, String nodeId,
      String hqlWhereClause);

  protected String substituteParameters(String hqlTreeWhereClause, Map<String, String> parameters) {
    Pattern pattern = Pattern.compile("@\\S*@");
    Matcher matcher = pattern.matcher(hqlTreeWhereClause);
    HashMap<String, String> replacements = new HashMap<String, String>();
    while (matcher.find()) {
      String contextPropertyName = hqlTreeWhereClause.substring(matcher.start(), matcher.end());
      String value = parameters.get(contextPropertyName);
      replacements.put(contextPropertyName, "'" + value + "'");
    }
    String hqlCopy = new String(hqlTreeWhereClause);
    for (String key : replacements.keySet()) {
      hqlCopy = hqlCopy.replaceAll(key, replacements.get(key));
    }
    return hqlCopy;
  }

  protected abstract JSONObject getJSONObjectByRecordId(Map<String, String> parameters,
      String nodeId);

  protected abstract JSONObject getJSONObjectByNodeId(Map<String, String> parameters, String nodeId)
      throws MultipleParentsException;

  protected abstract JSONArray fetchNodeChildren(Map<String, String> parameters, String parentId,
      String hqlWhereClause, String hqlWhereClauseRootNodes) throws JSONException,
      TooManyTreeNodesException;

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
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject oldValues = jsonObject.getJSONObject("oldValues");
        response = processNodeMovement(parameters, data, oldValues, prevNodeId, nextNodeId);
      } else if (jsonObject.has("transaction")) {
        JSONArray jsonResultArray = new JSONArray();
        JSONObject transaction = jsonObject.getJSONObject("transaction");
        JSONArray operations = transaction.getJSONArray("operations");
        for (int i = 0; i < operations.length(); i++) {
          JSONObject operation = operations.getJSONObject(i);
          JSONObject data = operation.getJSONObject("data");
          JSONObject oldValues = operation.getJSONObject("oldValues");
          jsonResultArray.put(processNodeMovement(parameters, data, oldValues, prevNodeId,
              nextNodeId));
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
      JSONObject oldValues, String prevNodeId, String nextNodeId) throws Exception {
    String nodeId = data.getString("id");
    String newParentId = data.getString("parentId");

    JSONObject jsonResult = new JSONObject();
    JSONObject jsonResponse = new JSONObject();
    JSONArray dataResponse = new JSONArray();

    String treeType = "EmployeeTree";

    CheckTreeOperationManager ctom = null;

    try {
      ctom = checkTreeOperationManagers.select(new ComponentProvider.Selector(treeType)).get();
    } catch (UnsatisfiedResolutionException e) {
      // Controlled exception, there aren't any CheckTreeOperationManager
    }

    boolean success = true;
    String messageType = null;
    String message = null;
    if (ctom != null) {
      ActionResponse actionResponse = ctom.checkNodeMovement(parameters, nodeId, newParentId,
          prevNodeId, nextNodeId);
      success = actionResponse.isSuccess();
      messageType = actionResponse.getMessageType();
      message = actionResponse.getMessage();
    }

    if (success) {
      JSONObject updatedData = moveNode(parameters, nodeId, newParentId, prevNodeId, nextNodeId);
      if (updatedData != null) {
        dataResponse.put(updatedData);
      } else {
        dataResponse.put(data);
      }
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } else {
      dataResponse.put(oldValues);
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
    }

    if (messageType != null && message != null) {
      JSONObject jsonMessage = new JSONObject();
      jsonMessage.put("messageType", messageType);
      jsonMessage.put("message", message);
      jsonResponse.put("message", jsonMessage);
    }

    jsonResponse.put(JsonConstants.RESPONSE_DATA, dataResponse);
    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 1);
    jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    return jsonResult.toString();
  }

  protected abstract JSONObject moveNode(Map<String, String> parameters, String nodeId,
      String newParentId, String prevNodeId, String nextNodeId) throws Exception;

  protected class MultipleParentsException extends Exception {
    private static final long serialVersionUID = 1L;

  }

  protected class TooManyTreeNodesException extends Exception {
    private static final long serialVersionUID = 1L;

  }

}
