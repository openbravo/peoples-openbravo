package org.openbravo.service.datasource;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
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
      JSONArray responseData = fetchNodeChildren(tree, parentId);

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

  private JSONArray fetchNodeChildren(Tree tree, String parentId) {
    JSONArray responseData = new JSONArray();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tree.getTable().getId());

    StringBuilder joinClause = new StringBuilder();
    joinClause.append(" as tn ");
    joinClause.append(" , " + entity.getName() + " as t ");
    joinClause.append(" where tn.node = t.id ");
    joinClause.append(" and tn.tree.id = '" + tree.getId() + "' ");
    joinClause.append(" and tn.reportSet = '" + parentId + "' ");

    String selectClause = " tn.id as treeNodeId, tn.reportSet as parentId, tn.sequenceNumber as seqNo, tn.node as nodeId, t.name as name";
    OBQuery<BaseOBObject> obq = OBDal.getInstance()
        .createQuery("ADTreeNode", joinClause.toString());
    obq.setSelectClause(selectClause);
    obq.setFilterOnReadableClients(false);
    obq.setFilterOnReadableOrganization(false);
    obq.getWhereAndOrderBy();

    int TREE_NODE_ID = 0;
    int PARENT_ID = 1;
    int SEQNO = 2;
    int NODE_ID = 3;
    int NODE_NAME = 4;

    // OBCriteria<TreeNode> treeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
    // treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
    // treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_REPORTSET, parentId));
    // treeNodeCriteria.addOrder(Order.desc(TreeNode.PROPERTY_SEQUENCENUMBER));
    // List<TreeNode> treeNodeList = treeNodeCriteria.list();

    for (Object rawNode : obq.createQuery().list()) {
      Object[] node = (Object[]) rawNode;
      JSONObject value = new JSONObject();
      try {
        value.put("id", node[NODE_ID]);
        value.put("_identifier", node[NODE_NAME]);
        value.put("parentId", node[PARENT_ID]);
        value.put("seqno", node[SEQNO]);
      } catch (JSONException e) {
        log.error("Error while constructing JSON reponse", e);
      }
      responseData.put(value);
    }
    return responseData;
  }

  @Override
  public String update(Map<String, String> parameters, String content) {
    OBContext.setAdminMode(true);
    final JSONObject jsonResult = new JSONObject();

    try {
      final JSONObject jsonObject = new JSONObject(content);
      if (content == null) {
        return "";
      }

      String referencedTableId = parameters.get("referencedTableId");
      String parentRecordId = parameters.get("parentRecordId");
      Tree tree = this.getTree(referencedTableId, parentRecordId);

      final JSONObject data = jsonObject.getJSONObject("data");
      // final JSONObject oldValues = jsonObject.getJSONObject("oldValues");

      String nodeId = data.getString("id");
      String newParentId = data.getString("parentId");

      OBCriteria<TreeNode> treeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
      treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, nodeId));
      TreeNode treeNode = (TreeNode) treeNodeCriteria.uniqueResult();
      treeNode.setReportSet(newParentId);
      OBDal.getInstance().flush();

      JSONArray dataResponse = new JSONArray();
      dataResponse.put(data);

      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, dataResponse);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 0);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 1);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

    } catch (Exception e) {
      log.error("Error while moving tree node", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonResult.toString();
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
