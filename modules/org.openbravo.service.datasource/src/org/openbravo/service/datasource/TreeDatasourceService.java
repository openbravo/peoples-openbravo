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
      if (tabId != null) {
        tab = OBDal.getInstance().get(Tab.class, tabId);
        table = tab.getTable();
      } else if (treeReferenceId != null) {
        ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class,
            treeReferenceId);
        table = treeReference.getTable();
      } else {
        // TODO: Throw proper exception
      }

      String hqlTreeWhereClause = null;

      if (parameters.containsKey(JsonConstants.DISTINCT_PARAMETER)) {
        Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
        DataSourceService dataSource = dataSourceServiceProvider.getDataSource(entity.getName());
        return dataSource.fetch(parameters);
      }

      if (parentId.equals(ROOT_NODE)) {
        // Now the HQL tree where clause is only beig applied to fetch the root nodes, to tell them
        // from the non root nodes
        // TODO
        // The HQL where clause of the tab should be applied for all records
        if (tab != null && tab.getHqlTreeWhereClause() != null) {
          hqlTreeWhereClause = this.substituteParameters(tab.getHqlTreeWhereClause(), parameters);
        }
      }
      JSONArray responseData = null;
      JSONArray selectedNodes = null;
      if (parameters.containsKey("selectedRecords")) {
        selectedNodes = new JSONArray(parameters.get("selectedRecords"));
      }

      if (parameters.containsKey("criteria") && parentId.equals(ROOT_NODE)) {
        List<String> filteredNodes = getFilteredNodes(table, parameters);
        if (!filteredNodes.isEmpty()) {
          // Fetch only the filtered nodes and its parents (filtered tree)
          responseData = fetchFilteredNodes(parameters, filteredNodes);
        } else {
          responseData = new JSONArray();
        }
      } else if (selectedNodes != null && selectedNodes.length() > 0) {
        // Fetch only the selected nodes and its parents (full tree)
        try {
          responseData = fetchSelectedNodes(parameters, selectedNodes);
        } catch (MultipleParentsException e) {
          // If a node has multiple parents, we can't select them
          log.warn("Node found with multiple parents. It can't be selected, displaying root nodes closed");
          responseData = fetchNodeChildren(parameters, parentId, hqlTreeWhereClause);
        }
      } else {
        // Fetch the node children of a given parent
        responseData = fetchNodeChildren(parameters, parentId, hqlTreeWhereClause);
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

  private List<String> getFilteredNodes(Table table, Map<String, String> parameters) {
    List<String> filteredNodes = new ArrayList<String>();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    DataSourceService dataSource = dataSourceServiceProvider.getDataSource(entity.getName());
    String dsResult = dataSource.fetch(parameters);
    try {
      JSONObject jsonDsResult = new JSONObject(dsResult);
      JSONObject jsonResponse = jsonDsResult.getJSONObject(JsonConstants.RESPONSE_RESPONSE);
      JSONArray dataArray = jsonResponse.getJSONArray(JsonConstants.RESPONSE_DATA);
      int nRecords = dataArray.length();
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
    Table table = null;
    TableTree tableTree = null;
    if (tabId != null) {
      tab = OBDal.getInstance().get(Tab.class, tabId);
      table = tab.getTable();
      tableTree = tab.getTableTree();
    } else if (treeReferenceId != null) {
      ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class, treeReferenceId);
      table = treeReference.getTable();
      tableTree = treeReference.getTableTreeCategory();
    } else {
      // TODO: Throw proper exception
    }
    try {
      Map<String, JSONObject> addedNodesMap = new HashMap<String, JSONObject>();
      for (String nodeId : filteredNodes) {
        JSONObject node = getJSONObjectByRecordId(parameters, nodeId);

        JSONObject savedNode = addedNodesMap.get(node.getString("id"));
        if (savedNode == null) {
          node.put("filterHit", true);
          addedNodesMap.put(node.getString("id"), node);
        } else {
          savedNode.put("filterHit", true);
        }

        // TODO: whereclause para selectores
        String hqlTreeWhereClause = null;
        if (tab != null && tab.getHqlTreeWhereClause() != null) {
          hqlTreeWhereClause = this.substituteParameters(tab.getHqlTreeWhereClause(), parameters);
        }

        while (node.has("parentId")
            && !"0".equals(node.getString("parentId"))
            && this.nodeConformsToWhereClause(table, tableTree, node.getString("parentId"),
                hqlTreeWhereClause)) {
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
        if (this.nodeConformsToWhereClause(table, tableTree, node.getString("parentId"),
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

  private JSONArray fetchSelectedNodes(Map<String, String> parameters, JSONArray selectedNodes)
      throws MultipleParentsException {
    JSONArray responseData = new JSONArray();
    String tabId = parameters.get("tabId");
    String treeReferenceId = parameters.get("treeReferenceId");
    Tab tab = null;
    Table table = null;
    TableTree tableTree = null;
    if (tabId != null) {
      tab = OBDal.getInstance().get(Tab.class, tabId);
      table = tab.getTable();
      tableTree = tab.getTableTree();
    } else if (treeReferenceId != null) {
      ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class, treeReferenceId);
      table = treeReference.getTable();
      tableTree = treeReference.getTableTreeCategory();
    } else {
      // TODO: Throw proper exception
    }
    try {
      ArrayList<String> addedNodes = new ArrayList<String>();
      ArrayList<String> parentsToExpand = new ArrayList<String>();
      int maxLevel = -1;
      for (int i = 0; i < selectedNodes.length(); i++) {
        String nodeId = selectedNodes.getString(i);
        JSONObject node = getJSONObjectByRecordId(parameters, nodeId);
        if (!addedNodes.contains(node.getString("id"))) {
          addedNodes.add(node.getString("id"));
          responseData.put(node);
        }
        int level = 0;

        String hqlTreeWhereClause = null;
        // TODO: hql clause for selectors
        if (tab != null && tab.getHqlTreeWhereClause() != null) {
          hqlTreeWhereClause = this.substituteParameters(tab.getHqlTreeWhereClause(), parameters);
        }

        while (node.has("parentId")
            && !"0".equals(node.getString("parentId"))
            && this.nodeConformsToWhereClause(table, tableTree, node.getString("parentId"),
                hqlTreeWhereClause)) {
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
        if (this.nodeConformsToWhereClause(table, tableTree, node.getString("parentId"),
            hqlTreeWhereClause)) {
          node.put("parentId", ROOT_NODE);
        }
        if (level > maxLevel) {
          maxLevel = level;
        }
      }
      // Expand all the parents
      for (String parentId : parentsToExpand) {
        JSONArray nodeChildren = this.fetchNodeChildren(parameters, parentId, null);
        for (int i = 0; i < nodeChildren.length(); i++) {
          JSONObject node = nodeChildren.getJSONObject(i);
          if (!addedNodes.contains(node.getString("id"))) {
            addedNodes.add(node.getString("id"));
            responseData.put(node);
          }
        }
      }
      // Include all the first level nodes
      JSONArray firstLevelNodes = this.fetchFirstLevelNodes(parameters);
      for (int i = 0; i < firstLevelNodes.length(); i++) {
        JSONObject node = firstLevelNodes.getJSONObject(i);
        if (!addedNodes.contains(node.getString("id"))) {
          addedNodes.add(node.getString("id"));
          responseData.put(node);
        }
      }
    } catch (JSONException e) {
      log.error("Error on tree datasource", e);
    }
    return responseData;
  }

  protected abstract boolean nodeConformsToWhereClause(Table table, TableTree tableTree,
      String nodeId, String hqlWhereClause);

  protected JSONArray fetchFirstLevelNodes(Map<String, String> parameters) throws JSONException {
    String tabId = parameters.get("tabId");
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    String hqlTreeWhereClause = null;
    if (tab.getHqlTreeWhereClause() != null) {
      hqlTreeWhereClause = this.substituteParameters(tab.getHqlTreeWhereClause(), parameters);
    }
    return this.fetchNodeChildren(parameters, ROOT_NODE, hqlTreeWhereClause);
  }

  private String substituteParameters(String hqlTreeWhereClause, Map<String, String> parameters) {
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
      String hqlWhereClause) throws JSONException;

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

  }
}
