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
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.ReferencedTree;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.TableTree;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkToParentTreeDatasourceService extends TreeDatasourceService {
  final static Logger logger = LoggerFactory.getLogger(LinkToParentTreeDatasourceService.class);
  final static String ID_SEPARATOR = "-";

  @Override
  protected void addNewNode(JSONObject bobProperties) {
    // Nothing needs to be done
  }

  @Override
  protected void deleteNode(JSONObject bobProperties) {
    try {
      String entityName = bobProperties.getString("_entity");
      Entity entity = ModelProvider.getInstance().getEntity(entityName);
      Table table = OBDal.getInstance().get(Table.class, entity.getTableId());
      Property linkToParentProperty = getLinkToParentProperty(table);
      Property nodeIdProperty = getNodeIdProperty(table);
      String bobParentNode = null;
      String bobNodeId = null;
      if (bobProperties.has(linkToParentProperty.getName())) {
        bobParentNode = bobProperties.getString(linkToParentProperty.getName());
      }
      if (bobProperties.has(nodeIdProperty.getName())) {
        bobNodeId = bobProperties.getString(nodeIdProperty.getName());
      }

      int nChildrenMoved = reparentChildrenOfDeletedNode(entity, bobParentNode, bobNodeId);
      logger.info(nChildrenMoved + " children have been moved to another parent");
    } catch (Exception e) {
      logger.error("Error while deleting tree node: ", e);
      throw new OBException("The node could not be deleted");
    }
  }

  public int reparentChildrenOfDeletedNode(Entity entity, String newParentId, String oldParentId) {
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
        ps.setString(1, oldParentId);
      } else {
        ps.setString(1, newParentId);
        ps.setString(2, oldParentId);
      }
      nChildrenMoved = ps.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error while deleting tree node: ", e);
    }
    return nChildrenMoved;
  }

  private Property getLinkToParentProperty(TableTree tableTree) {
    Column linkToParentColumn = tableTree.getLinkToParentColumn();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tableTree.getTable().getId());
    return entity.getPropertyByColumnName(linkToParentColumn.getDBColumnName());
  }

  private Property getLinkToParentProperty(Table table) {
    // TODO: Terminar. Soportar tablas con varios árboles asociados
    List<TableTree> tableTreeList = table.getADTableTreeList();
    if (tableTreeList.size() != 1) {
      return null;
    }
    TableTree tableTree = tableTreeList.get(0);
    Column linkToParentColumn = tableTree.getLinkToParentColumn();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    return entity.getPropertyByColumnName(linkToParentColumn.getDBColumnName());
  }

  private Property getNodeIdProperty(Table table) {
    // TODO: Terminar. Soportar tablas con varios árboles asociados
    List<TableTree> tableTreeList = table.getADTableTreeList();
    if (tableTreeList.size() != 1) {
      return null;
    }
    TableTree tableTree = tableTreeList.get(0);
    Column nodeIdColumn = tableTree.getNodeIdColumn();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    return entity.getPropertyByColumnName(nodeIdColumn.getDBColumnName());
  }

  private Property getNodeIdProperty(TableTree tableTree) {
    Column nodeIdColumn = tableTree.getNodeIdColumn();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tableTree.getTable().getId());
    return entity.getPropertyByColumnName(nodeIdColumn.getDBColumnName());
  }

  @Override
  protected JSONArray fetchNodeChildren(Map<String, String> parameters, String parentId,
      String hqlWhereClause, String hqlWhereClauseRootNodes) throws JSONException,
      TooManyTreeNodesException {

    boolean fetchRoot = ROOT_NODE.equals(parentId);
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
      logger.error("Either tab id or tree reference id must be provided");
    }
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property linkToParentProperty = getLinkToParentProperty(tableTree);
    Property nodeIdProperty = getNodeIdProperty(tableTree);
    boolean isMultiParentTree = tableTree.isHasMultiparentNodes();

    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as e where ");

    String actualParentId = new String(parentId);
    if (isMultiParentTree) {
      if (parentId.contains(ID_SEPARATOR)) {
        actualParentId = parentId.substring(parentId.lastIndexOf(ID_SEPARATOR) + 1);
      }
    }

    // TODO: Do not apply always the whereclause on the children, make it configurable
    if (hqlWhereClause != null) {
      whereClause.append(hqlWhereClause + " and ");
    }

    if (hqlWhereClauseRootNodes != null && fetchRoot) {
      whereClause.append(" " + hqlWhereClauseRootNodes + " ");
    } else {
      whereClause.append(" e." + linkToParentProperty.getName());
      if (fetchRoot) {
        whereClause.append(" is null ");
      } else {
        whereClause.append(".id = '" + actualParentId + "' ");
      }
    }

    final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(entity.getName(),
        whereClause.toString());

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    JSONArray responseData = new JSONArray();

    int nResults = query.count();
    OBContext context = OBContext.getOBContext();
    int nMaxResults = -1;
    try {
      nMaxResults = Integer.parseInt(Preferences.getPreferenceValue("TreeDatasourceFetchLimit",
          false, context.getCurrentClient(), context.getCurrentOrganization(), context.getUser(),
          context.getRole(), null));
    } catch (Exception e) {
      nMaxResults = 1000;
    }
    if (nResults > nMaxResults) {
      throw new TooManyTreeNodesException();
    }
    int count = 0;
    final ScrollableResults scrollableResults = query.scroll(ScrollMode.FORWARD_ONLY);
    while (scrollableResults.next()) {
      BaseOBObject bob = (BaseOBObject) scrollableResults.get()[0];
      final JSONObject json = toJsonConverter.toJsonObject((BaseOBObject) bob,
          DataResolvingMode.FULL);
      if (fetchRoot) {
        json.put("parentId", ROOT_NODE);
      } else {
        json.put("parentId", parentId);
      }
      Object nodeId = bob.get(nodeIdProperty.getName());
      String nodeIdStr = null;
      if (nodeId instanceof String) {
        nodeIdStr = (String) nodeId;
      } else if (nodeId instanceof BaseOBObject) {
        nodeIdStr = ((BaseOBObject) nodeId).getId().toString();
      }

      Object parentNodeId = bob.get(linkToParentProperty.getName());
      String parentNodeIdStr = null;
      if (parentNodeId instanceof String) {
        parentNodeIdStr = (String) parentNodeId;
      } else if (parentNodeId instanceof BaseOBObject) {
        parentNodeIdStr = ((BaseOBObject) parentNodeId).getId().toString();
      }

      if (isMultiParentTree) {
        json.put("nodeId", parentNodeIdStr + ID_SEPARATOR + nodeIdStr);
      } else {
        json.put("nodeId", nodeIdStr);
      }
      json.put("_hasChildren", (this.nodeHasChildren(entity, linkToParentProperty, nodeIdProperty,
          bob, hqlWhereClause)) ? true : false);
      responseData.put(json);

      count++;
      if (count % 100 == 0) {
        OBDal.getInstance().getSession().clear();
      }

    }
    return responseData;
  }

  private boolean nodeHasChildren(Entity entity, Property linkToParentProperty,
      Property nodeIdProperty, BaseOBObject node, String hqlWhereClause) {

    Object nodeId = node.get(nodeIdProperty.getName());
    String nodeIdStr = null;
    if (nodeId instanceof String) {
      nodeIdStr = (String) nodeId;
    } else if (nodeId instanceof BaseOBObject) {
      nodeIdStr = ((BaseOBObject) nodeId).getId().toString();
    }
    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as e where e." + linkToParentProperty.getName());
    whereClause.append(".id = '" + nodeIdStr + "' ");
    if (hqlWhereClause != null) {
      whereClause.append(" and " + hqlWhereClause);
    }
    final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(entity.getName(),
        whereClause.toString());

    return query.count() > 0;
  }

  private void recomputeSequenceNumbers(Tree tree, String newParentId, Long seqNo) {
  }

  protected JSONObject moveNode(Map<String, String> parameters, String nodeId, String newParentId,
      String prevNodeId, String nextNodeId) throws Exception {

    String referencedTableId = parameters.get("referencedTableId");
    Table table = OBDal.getInstance().get(Table.class, referencedTableId);
    Entity referencedEntity = ModelProvider.getInstance().getEntityByTableId(table.getId());

    String tabId = parameters.get("tabId");
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    String hqlWhereClause = tab.getHqlwhereclause();
    TableTree tableTree = tab.getTableTree();
    Property linkToParentProperty = getLinkToParentProperty(tableTree);
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property nodeIdProperty = getNodeIdProperty(tableTree);

    boolean isOrdered = tableTree.getTreeCategory().isOrdered();

    BaseOBObject bob = OBDal.getInstance().get(referencedEntity.getName(), nodeId);
    BaseOBObject parentBob = OBDal.getInstance().get(referencedEntity.getName(), newParentId);
    bob.set(linkToParentProperty.getName(), parentBob);

    OBDal.getInstance().flush();

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    JSONObject updatedData = toJsonConverter.toJsonObject((BaseOBObject) bob,
        DataResolvingMode.FULL);
    updatedData.put("parentId", parentBob.getId().toString());
    updatedData.put("_hasChildren", (this.nodeHasChildren(entity, linkToParentProperty,
        nodeIdProperty, bob, hqlWhereClause)) ? true : false);

    return updatedData;
  }

  private Long calculateSequenceNumberAndRecompute(Tree tree, String prevNodeId, String nextNodeId,
      String newParentId) throws Exception {
    return 0L;
  }

  @Override
  protected JSONObject getJSONObjectByNodeId(Map<String, String> parameters, String nodeId)
      throws MultipleParentsException {
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
      logger.error("Either tab id or tree reference id must be provided");
    }
    // Obtain the recordId based on the nodeId
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property nodeIdProperty = getNodeIdProperty(tableTree);

    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" where " + nodeIdProperty.getName());
    whereClause.append(".id = '" + nodeId + "' ");
    final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(entity.getName(),
        whereClause.toString());
    if (query.count() != 1) {
      throw new MultipleParentsException();
    }
    BaseOBObject bob = query.uniqueResult();
    return this.getJSONObjectByRecordId(parameters, bob.getId().toString());
  }

  protected boolean nodeConformsToWhereClause(TableTree tableTree, String nodeId,
      String hqlWhereClause) {
    if (hqlWhereClause == null || hqlWhereClause.isEmpty()) {
      return true;
    }
    Table table = tableTree.getTable();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    Property nodeIdProperty = getNodeIdProperty(tableTree);

    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as e where e." + nodeIdProperty.getName());
    whereClause.append(".id = '" + nodeId + "' ");
    whereClause.append(" and " + hqlWhereClause);
    final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(entity.getName(),
        whereClause.toString());
    return (query.count() == 1);
  }

  @Override
  protected JSONObject getJSONObjectByRecordId(Map<String, String> parameters, String bobId) {
    String tabId = parameters.get("tabId");
    String treeReferenceId = parameters.get("treeReferenceId");
    Tab tab = null;
    Table table = null;
    TableTree tableTree = null;
    String hqlWhereClause = null;
    if (tabId != null) {
      tab = OBDal.getInstance().get(Tab.class, tabId);
      table = tab.getTable();
      tableTree = tab.getTableTree();
      hqlWhereClause = tab.getHqlwhereclause();
    } else if (treeReferenceId != null) {
      ReferencedTree treeReference = OBDal.getInstance().get(ReferencedTree.class, treeReferenceId);
      table = treeReference.getTable();
      tableTree = treeReference.getTableTreeCategory();
      hqlWhereClause = treeReference.getHQLSQLWhereClause();
    } else {
      // TODO: Throw proper exception
      logger.error("Either tab id or tree reference id must be provided");
    }
    Property linkToParentProperty = getLinkToParentProperty(tableTree);
    Property nodeIdProperty = getNodeIdProperty(tableTree);

    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    JSONObject json = null;

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);

    try {
      BaseOBObject bob = OBDal.getInstance().get(entity.getName(), bobId);
      json = toJsonConverter.toJsonObject((BaseOBObject) bob, DataResolvingMode.FULL);
      BaseOBObject parent = (BaseOBObject) bob.get(linkToParentProperty.getName());
      if (parent != null) {
        json.put("parentId", parent.getId().toString());
      } else {
        json.put("parentId", (String) ROOT_NODE);
      }
      Object nodeId = bob.get(nodeIdProperty.getName());
      String nodeIdStr = null;
      if (nodeId instanceof String) {
        nodeIdStr = (String) nodeId;
      } else if (nodeId instanceof BaseOBObject) {
        nodeIdStr = ((BaseOBObject) nodeId).getId().toString();
      }
      json.put("nodeId", nodeIdStr);
      json.put("_hasChildren", (this.nodeHasChildren(entity, linkToParentProperty, nodeIdProperty,
          bob, hqlWhereClause)) ? true : false);
    } catch (JSONException e) {
      logger.error("Error on tree datasource", e);
    }
    return json;
  }
}
