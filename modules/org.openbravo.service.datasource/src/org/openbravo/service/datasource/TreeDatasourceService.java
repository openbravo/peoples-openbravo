package org.openbravo.service.datasource;

import java.sql.PreparedStatement;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.ADTreeType;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeDatasourceService extends DefaultDataSourceService {
  final static Logger log = LoggerFactory.getLogger(TreeDatasourceService.class);

  @Override
  public String fetch(Map<String, String> parameters) {
    OBContext.setAdminMode(true);
    try {
      String referencedTableId = parameters.get("referencedTableId");
      String parentRecordId = parameters.get("parentRecordId");
      Tree tree = this.getTree(referencedTableId, parentRecordId);
      String parentId = parameters.get("parentId");
      // boolean rootNode = (parentId == null || "null".equals(parentId) || "0".equals(parentId) ||
      // parentId
      // .isEmpty());
      JSONArray responseData = fetchNodeChildren(tree, parentId, parameters);

      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();

      jsonResponse.put(JsonConstants.RESPONSE_DATA, responseData);
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, responseData.length());
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, responseData.length() - 1);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

      return jsonResult.toString();
    } catch (Throwable t) {
      log.error("Error on tree datasource", t);
      return JsonUtils.convertExceptionToJson(t);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private JSONArray fetchNodeChildren(Tree tree, String parentId, Map<String, String> parameters)
      throws JSONException {
    JSONArray responseData = new JSONArray();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tree.getTable().getId());

    String selectedPropertiesStr = parameters.get("_selectedProperties");
    JSONArray selectedProperties = new JSONArray(selectedPropertiesStr);

    StringBuilder joinClause = new StringBuilder();
    joinClause.append(" as tn ");
    joinClause.append(" , " + entity.getName() + " as t ");
    joinClause.append(" where tn.node = t.id ");
    joinClause.append(" and tn.tree.id = '" + tree.getId() + "' ");
    joinClause.append(" and tn.reportSet = '" + parentId + "' order by tn.sequenceNumber ");

    String selectClause = " tn.id as treeNodeId, tn.reportSet as parentId, tn.sequenceNumber as seqNo, tn.node as nodeId, t as entity";
    OBQuery<BaseOBObject> obq = OBDal.getInstance()
        .createQuery("ADTreeNode", joinClause.toString());
    obq.setSelectClause(selectClause);

    int TREE_NODE_ID = 0;
    int PARENT_ID = 1;
    int SEQNO = 2;
    int NODE_ID = 3;
    int ENTITY = 4;

    for (Object rawNode : obq.createQuery().list()) {
      Object[] node = (Object[]) rawNode;
      JSONObject value = new JSONObject();
      BaseOBObject bob = (BaseOBObject) node[ENTITY];
      try {
        value.put("id", node[NODE_ID]);
        value.put("parentId", node[PARENT_ID]);
        value.put("seqno", node[SEQNO]);
        value.put("canAcceptDroppedRecords", false);
        value.put("_hasChildren", (this.nodeHasChildren((String) node[NODE_ID])) ? true : false);
        for (int i = 0; i < selectedProperties.length(); i++) {
          value.put(selectedProperties.getString(i), bob.get(selectedProperties.getString(i)));
        }
      } catch (JSONException e) {
        log.error("Error while constructing JSON reponse", e);
      }
      responseData.put(value);
    }
    return responseData;
  }

  private boolean nodeHasChildren(String treeNodeId) {
    OBCriteria<TreeNode> nodeChildrenCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
    nodeChildrenCriteria.add(Restrictions.eq(TreeNode.PROPERTY_REPORTSET, treeNodeId));
    return nodeChildrenCriteria.count() > 0;
  }

  @Override
  public String update(Map<String, String> parameters, String content) {

    final String JSON_PREFIX = "<SCRIPT>//'\"]]>>isc_JSONResponseStart>>";
    final String JSON_SUFFIX = "//isc_JSONResponseEnd";
    OBContext.setAdminMode(true);
    String response = null;
    try {
      final JSONObject jsonObject = new JSONObject(content);
      if (content == null) {
        return "";
      }

      String referencedTableId = parameters.get("referencedTableId");
      String parentRecordId = parameters.get("parentRecordId");
      String prevNodeId = parameters.get("prevNodeId");
      String nextNodeId = parameters.get("nextNodeId");
      Tree tree = this.getTree(referencedTableId, parentRecordId);

      if (jsonObject.has("data")) {
        response = processNodeMovement(tree, jsonObject.getJSONObject("data"), prevNodeId,
            nextNodeId);
      } else if (jsonObject.has("transaction")) {
        JSONArray jsonResultArray = new JSONArray();
        JSONObject transaction = jsonObject.getJSONObject("transaction");
        JSONArray operations = transaction.getJSONArray("operations");
        for (int i = 0; i < operations.length(); i++) {
          JSONObject operation = operations.getJSONObject(i);
          jsonResultArray.put(processNodeMovement(tree, operation.getJSONObject("data"),
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

  private String processNodeMovement(Tree tree, JSONObject data, String prevNodeId,
      String nextNodeId) throws Exception {
    String nodeId = data.getString("id");
    String newParentId = data.getString("parentId");

    JSONObject jsonResult = new JSONObject();
    JSONObject jsonResponse = new JSONObject();
    JSONArray dataResponse = new JSONArray();

    moveNode(tree, nodeId, newParentId, prevNodeId, nextNodeId);

    dataResponse.put(data);
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put(JsonConstants.RESPONSE_DATA, dataResponse);
    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 0);
    jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 1);
    jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    return jsonResult.toString();
  }

  private void moveNode(Tree tree, String nodeId, String newParentId, String prevNodeId,
      String nextNodeId) throws Exception {

    boolean isOrdered = this.isOrdered(tree);

    Long seqNo = null;
    if (isOrdered) {
      seqNo = this.calculateSequenceNumberAndRecompute(tree, prevNodeId, nextNodeId, newParentId);
    }

    OBCriteria<TreeNode> treeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
    treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
    treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, nodeId));
    TreeNode treeNode = (TreeNode) treeNodeCriteria.uniqueResult();

    treeNode.setReportSet(newParentId);
    if (isOrdered) {
      treeNode.setSequenceNumber(seqNo);
    }
    OBDal.getInstance().flush();
  }

  private Long calculateSequenceNumberAndRecompute(Tree tree, String prevNodeId, String nextNodeId,
      String newParentId) throws Exception {
    Long seqNo = null;
    if (prevNodeId == null && nextNodeId == null) {
      // Only child, no need to recompute sequence numbers
      seqNo = 10L;
    } else if (nextNodeId == null) {
      // Last positioned child. Pick the highest sequence number of its brothers and add 10
      // No need to recompute sequence numbers
      OBCriteria<TreeNode> maxSeqNoCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
      maxSeqNoCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      maxSeqNoCriteria.add(Restrictions.eq(TreeNode.PROPERTY_REPORTSET, newParentId));
      maxSeqNoCriteria.setProjection(Projections.max(TreeNode.PROPERTY_SEQUENCENUMBER));
      Long maxSeqNo = (Long) maxSeqNoCriteria.uniqueResult();
      seqNo = maxSeqNo + 10;
    } else {
      // Sequence numbers of the nodes that are positioned after the new one needs to be recomputed
      OBCriteria<TreeNode> nextNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
      nextNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      nextNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, nextNodeId));
      TreeNode nextNode = (TreeNode) nextNodeCriteria.uniqueResult();
      seqNo = nextNode.getSequenceNumber();
      recomputeSequenceNumbers(tree, newParentId, seqNo);
    }
    return seqNo;
  }

  private void recomputeSequenceNumbers(Tree tree, String newParentId, Long seqNo) {
    StringBuilder queryStr = new StringBuilder();
    queryStr.append(" UPDATE ad_treenode ");
    queryStr.append(" SET seqno = (seqno + 10) ");
    queryStr.append(" WHERE ad_tree_id = ? ");
    queryStr.append(" AND parent_id = ? ");
    queryStr.append(" AND seqno >= ? ");

    ConnectionProvider conn = new DalConnectionProvider(false);
    PreparedStatement st;
    try {
      st = conn.getPreparedStatement(queryStr.toString());
      st.setString(1, tree.getId());
      st.setString(2, newParentId);
      st.setLong(3, seqNo);
      int nUpdated = st.executeUpdate();
      log.debug("Recomputing sequence numbers: " + nUpdated + " nodes updated");
      conn.releasePreparedStatement(st);
    } catch (Exception e) {
      log.error("Exception while recomputing sequence numbers: ", e);
    }
  }

  private boolean isOrdered(Tree tree) {
    Table table = tree.getTable();
    ADTreeType treeType = table.getTreetype34();
    return treeType.isOrdered();
  }

  private Tree getTree(String referencedTableId, String parentRecordId) {
    Table referencedTable = OBDal.getInstance().get(Table.class, referencedTableId);

    OBCriteria<Tree> treeCriteria = OBDal.getInstance().createCriteria(Tree.class);
    treeCriteria.add(Restrictions.eq(Tree.PROPERTY_TABLE, referencedTable));
    if (parentRecordId != null && !parentRecordId.isEmpty() && !"null".equals(parentRecordId)) {
      treeCriteria.add(Restrictions.eq(Tree.PROPERTY_PARENTRECORDID, parentRecordId));
    }
    return (Tree) treeCriteria.uniqueResult();
  }
}
