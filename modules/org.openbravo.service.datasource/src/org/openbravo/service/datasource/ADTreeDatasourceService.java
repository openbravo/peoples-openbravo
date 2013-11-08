package org.openbravo.service.datasource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ADTreeType;
import org.openbravo.model.ad.utility.TableTree;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADTreeDatasourceService extends TreeDatasourceService {
  final static Logger log = LoggerFactory.getLogger(ADTreeDatasourceService.class);

  @Override
  protected void addNewNode(JSONObject bobProperties) {
    try {
      Client client = OBContext.getOBContext().getCurrentClient();
      Organization org = OBContext.getOBContext().getCurrentOrganization();
      String bobId = bobProperties.getString("id");
      String entityName = bobProperties.getString("_entity");
      Entity entity = ModelProvider.getInstance().getEntity(entityName);
      Table table = OBDal.getInstance().get(Table.class, entity.getTableId());

      Tree adTree = getTree(table, bobProperties);
      if (adTree == null) {
        // The adTree does not exists, create it
        adTree = createTree(table, bobProperties);
      }
      // Adds the node to the adTree
      TreeNode adTreeNode = OBProvider.getInstance().get(TreeNode.class);
      adTreeNode.setClient(client);
      adTreeNode.setOrganization(org);
      adTreeNode.setTree(adTree);
      adTreeNode.setNode(bobId);
      adTreeNode.setSequenceNumber(100L);
      adTreeNode.setReportSet("0");
      OBDal.getInstance().save(adTreeNode);
    } catch (Exception e) {
      log.error("Error while adding the tree node", e);
    }
  }

  @Override
  protected void deleteNode(JSONObject bobProperties) {
    try {
      String bobId = bobProperties.getString("id");
      String entityName = bobProperties.getString("_entity");
      Entity entity = ModelProvider.getInstance().getEntity(entityName);
      Table table = OBDal.getInstance().get(Table.class, entity.getTableId());

      Tree tree = getTree(table, bobProperties);
      OBCriteria<TreeNode> adTreeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
      adTreeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      adTreeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, bobId));
      TreeNode treeNode = (TreeNode) adTreeNodeCriteria.uniqueResult();
      // Hay que:
      // - Borrar el treeNode
      // - Hacer un reparent de los nodos hijos de este treeNode
      int nChildrenMoved = reparentChildrenOfDeletedNode(tree, treeNode.getReportSet(),
          treeNode.getNode());
      log.info(nChildrenMoved + " children have been moved to another parent");
      OBDal.getInstance().remove(treeNode);
    } catch (Exception e) {
      log.error("Error while deleting tree node: ", e);
      throw new OBException("The treenode could not be created");
    }
  }

  public int reparentChildrenOfDeletedNode(Tree tree, String newParentId, String deletedNodeId) {
    int nChildrenMoved = -1;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append(" UPDATE AD_TREENODE set parent_id = ? ");
      sql.append(" WHERE ad_tree_id = ? ");
      sql.append(" AND parent_id= ? ");
      PreparedStatement ps = OBDal.getInstance().getConnection(false)
          .prepareStatement(sql.toString());
      ps.setString(1, newParentId);
      ps.setString(2, tree.getId());
      ps.setString(3, deletedNodeId);
      nChildrenMoved = ps.executeUpdate();
    } catch (SQLException e) {
      log.error("Error while deleting tree node: ", e);
    }
    return nChildrenMoved;
  }

  @Override
  protected JSONArray fetchNodeChildren(Map<String, String> parameters, String parentId,
      String hqlWhereClause) throws JSONException, TooManyTreeNodesException {

    String referencedTableId = parameters.get("referencedTableId");
    String parentRecordId = parameters.get("parentRecordId");
    Tree tree = this.getTree(referencedTableId, parentRecordId);

    JSONArray responseData = new JSONArray();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tree.getTable().getId());

    String selectedPropertiesStr = parameters.get("_selectedProperties");
    JSONArray selectedProperties = new JSONArray(selectedPropertiesStr);

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    StringBuilder joinClause = new StringBuilder();
    joinClause.append(" as tn ");
    joinClause.append(" , " + entity.getName() + " as e ");
    joinClause.append(" where tn.node = e.id ");
    joinClause.append(" and tn.tree.id = '" + tree.getId() + "' ");
    joinClause.append(" and tn.reportSet = '" + parentId + "' order by tn.sequenceNumber ");

    String selectClause = " tn.id as treeNodeId, tn.reportSet as parentId, tn.sequenceNumber as seqNo, tn.node as nodeId, e as entity";
    OBQuery<BaseOBObject> obq = OBDal.getInstance()
        .createQuery("ADTreeNode", joinClause.toString());
    obq.setSelectClause(selectClause);

    boolean fetchRoot = ROOT_NODE.equals(parentId);

    int TREE_NODE_ID = 0;
    int PARENT_ID = 1;
    int SEQNO = 2;
    int NODE_ID = 3;
    int ENTITY = 4;

    for (Object rawNode : obq.createQuery().list()) {
      Object[] node = (Object[]) rawNode;
      JSONObject value = null;
      BaseOBObject bob = (BaseOBObject) node[ENTITY];
      try {
        value = toJsonConverter.toJsonObject((BaseOBObject) bob, DataResolvingMode.FULL);
        value.put("nodeId", bob.getId().toString());
        if (fetchRoot) {
          value.put("parentId", ROOT_NODE);
        } else {
          value.put("parentId", node[PARENT_ID]);
        }
        value.put("seqno", node[SEQNO]);
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
    List<TableTree> tableTreeList = table.getADTableTreeList();
    if (tableTreeList.size() != 1) {
      return false;
    } else {
      TableTree tableTree = tableTreeList.get(0);
      ADTreeType treeType = tableTree.getTreeCategory();
      return treeType.isOrdered();
    }
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

  private Tree getTree(Table table, JSONObject bobProperties) {
    Tree tree = null;
    OBCriteria<Tree> adTreeCriteria = OBDal.getInstance().createCriteria(Tree.class);
    adTreeCriteria.add(Restrictions.eq(Tree.PROPERTY_TABLE, table));

    List<Column> parentColumns = getParentColumns(table);
    // If it is a subtab, the tree must be associated to the id of its parent tab
    if (parentColumns != null && !parentColumns.isEmpty()) {
      // TODO: Support tables with multple parent columns
      String referencedColumnValue = getReferencedColumnValue(bobProperties, parentColumns);
      if (referencedColumnValue != null && !referencedColumnValue.isEmpty()) {
        adTreeCriteria.add(Restrictions.eq(Tree.PROPERTY_PARENTRECORDID, referencedColumnValue));
      }
    }
    tree = (Tree) adTreeCriteria.uniqueResult();
    return tree;
  }

  private String getReferencedColumnValue(JSONObject bobProperties, List<Column> parentColumns) {
    Column parentColumn = parentColumns.get(0);
    Property property = getPropertyFromColumn(parentColumn);
    String referencedBobId = null;
    try {
      referencedBobId = (String) bobProperties.get(property.getName());
    } catch (JSONException e) {
      log.error("Error on tree datasource", e);
    }
    return referencedBobId;
  }

  private List<Column> getParentColumns(Table table) {
    OBCriteria<Column> isParentColumnsCriteria = OBDal.getInstance().createCriteria(Column.class);
    isParentColumnsCriteria.add(Restrictions.eq(Column.PROPERTY_TABLE, table));
    isParentColumnsCriteria.add(Restrictions.eq(Column.PROPERTY_LINKTOPARENTCOLUMN, true));
    return isParentColumnsCriteria.list();
  }

  private Property getPropertyFromColumn(Column column) {
    Entity entity = ModelProvider.getInstance().getEntityByTableId(column.getTable().getId());
    return entity.getPropertyByColumnName(column.getDBColumnName());
  }

  private Tree createTree(Table table, JSONObject bobProperties) {
    Client client = OBContext.getOBContext().getCurrentClient();
    Organization org = OBContext.getOBContext().getCurrentOrganization();

    Tree adTree = OBProvider.getInstance().get(Tree.class);
    adTree.setClient(client);
    adTree.setOrganization(org);
    adTree.setAllNodes(true);
    // TODO: Change this
    adTree.setTypeArea("NEW");
    adTree.setTable(table);
    String name = table.getName();
    List<Column> parentColumns = getParentColumns(table);
    if (parentColumns != null && !parentColumns.isEmpty()) {
      String referencedColumnValue = getReferencedColumnValue(bobProperties, parentColumns);
      adTree.setParentRecordID(referencedColumnValue);
      name = name + referencedColumnValue;
      // TODO: Fix this!
      name = name.substring(0, 59);
    }
    adTree.setName(name);
    OBDal.getInstance().save(adTree);
    return adTree;
  }

  protected JSONObject moveNode(Map<String, String> parameters, String nodeId, String newParentId,
      String prevNodeId, String nextNodeId) throws Exception {

    String referencedTableId = parameters.get("referencedTableId");
    String parentRecordId = parameters.get("parentRecordId");
    Tree tree = this.getTree(referencedTableId, parentRecordId);

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

    return null;
  }

  @Override
  protected JSONObject getJSONObjectByNodeId(Map<String, String> parameters, String nodeId)
      throws MultipleParentsException {
    // In the ADTree structure, nodeId = recordId
    return this.getJSONObjectByRecordId(parameters, nodeId);
  }

  @Override
  protected JSONObject getJSONObjectByRecordId(Map<String, String> parameters, String bobId) {
    String referencedTableId = parameters.get("referencedTableId");
    String parentRecordId = parameters.get("parentRecordId");
    Tree tree = this.getTree(referencedTableId, parentRecordId);
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tree.getTable().getId());

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    JSONObject json = null;
    try {
      OBCriteria<TreeNode> treeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
      treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      treeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, bobId));
      TreeNode treeNode = (TreeNode) treeNodeCriteria.uniqueResult();
      BaseOBObject bob = OBDal.getInstance().get(entity.getName(), treeNode.getNode());
      json = toJsonConverter.toJsonObject((BaseOBObject) bob, DataResolvingMode.FULL);
      json.put("nodeId", bobId);
      json.put("parentId", treeNode.getReportSet());
      json.put("_hasChildren", this.nodeHasChildren(treeNode.getNode()));
    } catch (Exception e) {
      log.error("Error on tree datasource", e);
    }
    return json;
  }

  protected boolean nodeConformsToWhereClause(Table table, TableTree tableTree, String nodeId,
      String hqlWhereClause) {
    // TODO: Implementar
    return true;
  }

}
