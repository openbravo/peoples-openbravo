package org.openbravo.service.datasource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkToParentTreeDatasourceService extends TreeDatasourceService {
  final static Logger logger = LoggerFactory.getLogger(LinkToParentTreeDatasourceService.class);

  @Override
  protected void addNewNode(JSONObject bobProperties) {
    // Nothing needs to be done
  }

  @Override
  protected void deleteNode(JSONObject bobProperties) {
    try {
      String bobId = bobProperties.getString("id");
      String entityName = bobProperties.getString("_entity");
      Entity entity = ModelProvider.getInstance().getEntity(entityName);
      Table table = OBDal.getInstance().get(Table.class, entity.getTableId());
      Property linkToParentProperty = getLinkToParentProperty(table);
      String bobParentNode = null;
      if (bobProperties.has(linkToParentProperty.getName())) {
        bobProperties.getString(linkToParentProperty.getName());
      }

      int nChildrenMoved = reparentChildrenOfDeletedNode(entity, bobParentNode, bobId);
      logger.info(nChildrenMoved + " children have been moved to another parent");
    } catch (Exception e) {
      logger.error("Error while deleting tree node: ", e);
      throw new OBException("The node could not be deleted");
    }
  }

  public int reparentChildrenOfDeletedNode(Entity entity, String newParentId, String deletedNodeId) {
    int nChildrenMoved = -1;
    Table table = OBDal.getInstance().get(Table.class, entity.getTableId());
    Property linkToParentProperty = getLinkToParentProperty(table);
    Column linkToParentColumn = OBDal.getInstance().get(Column.class,
        linkToParentProperty.getColumnId());
    try {
      StringBuilder sql = new StringBuilder();
      sql.append(" UPDATE " + table.getDBTableName() + " ");
      if (newParentId == null) {
        sql.append(" set " + linkToParentColumn.getDBColumnName() + " = null ");
      } else {
        sql.append(" set " + linkToParentColumn.getDBColumnName() + " = ? ");
      }
      sql.append(" WHERE " + linkToParentColumn.getDBColumnName() + " = ? ");
      PreparedStatement ps = OBDal.getInstance().getConnection(false)
          .prepareStatement(sql.toString());

      if (newParentId == null) {
        ps.setString(1, deletedNodeId);
      } else {
        ps.setString(1, newParentId);
        ps.setString(2, deletedNodeId);
      }
      nChildrenMoved = ps.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error while deleting tree node: ", e);
    }
    return nChildrenMoved;
  }

  private Property getLinkToParentProperty(Table table) {
    Column linkToParentColumn = table.getLinkToParentColumn();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    return entity.getPropertyByColumnName(linkToParentColumn.getDBColumnName());
  }

  @Override
  protected JSONArray fetchNodeChildren(Map<String, String> parameters, String parentId)
      throws JSONException {

    String tabId = parameters.get("tabId");

    boolean fetchRoot = ROOT_NODE.equals(parentId);
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    Table table = tab.getTable();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property linkToParentProperty = getLinkToParentProperty(table);

    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" where " + linkToParentProperty.getName());
    if (fetchRoot) {
      whereClause.append(" is null ");
    } else {
      BaseOBObject parentBob = OBDal.getInstance().get(entity.getName(), parentId);
      whereClause.append(".id = '" + parentBob.getId() + "' ");
    }

    final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(entity.getName(),
        whereClause.toString());

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    JSONArray responseData = new JSONArray();

    final ScrollableResults scrollableResults = query.scroll(ScrollMode.FORWARD_ONLY);
    while (scrollableResults.next()) {
      BaseOBObject bob = (BaseOBObject) scrollableResults.get()[0];
      final JSONObject json = toJsonConverter.toJsonObject((BaseOBObject) bob,
          DataResolvingMode.FULL);
      json.put("parentId", parentId);
      json.put("_hasChildren", (this.nodeHasChildren(entity, bob)) ? true : false);
      responseData.put(json);

    }
    return responseData;
  }

  private boolean nodeHasChildren(Entity entity, BaseOBObject node) {
    Table table = OBDal.getInstance().get(Table.class, entity.getTableId());
    Property linkToParentProperty = getLinkToParentProperty(table);
    OBCriteria<BaseOBObject> nodeChildrenCriteria = OBDal.getInstance().createCriteria(
        entity.getName());
    nodeChildrenCriteria.add(Restrictions.eq(linkToParentProperty.getName(), node));
    return nodeChildrenCriteria.count() > 0;
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
      logger.debug("Recomputing sequence numbers: " + nUpdated + " nodes updated");
      conn.releasePreparedStatement(st);
    } catch (Exception e) {
      logger.error("Exception while recomputing sequence numbers: ", e);
    }
  }

  private String getReferencedColumnValue(JSONObject bobProperties, List<Column> parentColumns) {
    Column parentColumn = parentColumns.get(0);
    Property property = getPropertyFromColumn(parentColumn);
    String referencedBobId = null;
    try {
      referencedBobId = (String) bobProperties.get(property.getName());
    } catch (JSONException e) {
      logger.error("Error on tree datasource", e);
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

  protected JSONObject moveNode(Map<String, String> parameters, String nodeId, String newParentId,
      String prevNodeId, String nextNodeId) throws Exception {

    String referencedTableId = parameters.get("referencedTableId");
    Table table = OBDal.getInstance().get(Table.class, referencedTableId);
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property linkToParentProperty = getLinkToParentProperty(table);
    boolean isOrdered = table.getTreeCategory().isOrdered();

    // TODO:Testing
    // Long seqNo = null;
    // if (isOrdered) {
    // seqNo = this.calculateSequenceNumberAndRecompute(tree, prevNodeId, nextNodeId,
    // newParentId);
    // }

    BaseOBObject bob = OBDal.getInstance().get(entity.getName(), nodeId);
    BaseOBObject parentBob = OBDal.getInstance().get(entity.getName(), newParentId);
    bob.set(linkToParentProperty.getName(), parentBob);

    // if (isOrdered) {
    // treeNode.setSequenceNumber(seqNo);
    // }
    OBDal.getInstance().flush();

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    JSONObject updatedData = toJsonConverter.toJsonObject((BaseOBObject) bob,
        DataResolvingMode.FULL);
    BaseOBObject parent = (BaseOBObject) bob.get(linkToParentProperty.getName());
    updatedData.put("parentId", parentBob.getId().toString());
    updatedData.put("_hasChildren", (this.nodeHasChildren(entity, bob)) ? true : false);

    return updatedData;
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

  @Override
  protected JSONObject getJSONObjectByNodeId(Map<String, String> parameters, String nodeId) {
    String referencedTableId = parameters.get("referencedTableId");
    Table table = OBDal.getInstance().get(Table.class, referencedTableId);
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property linkToParentProperty = getLinkToParentProperty(table);
    JSONObject json = null;

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    try {
      BaseOBObject bob = OBDal.getInstance().get(entity.getName(), nodeId);
      json = toJsonConverter.toJsonObject((BaseOBObject) bob, DataResolvingMode.FULL);
      BaseOBObject parent = (BaseOBObject) bob.get(linkToParentProperty.getName());
      if (parent != null) {
        json.put("parentId", parent.getId().toString());
      } else {
        json.put("parentId", (String) null);
      }
      json.put("_hasChildren", (this.nodeHasChildren(entity, bob)) ? true : false);
    } catch (JSONException e) {
      logger.error("Error on tree datasource", e);
    }

    return json;
  }

}
