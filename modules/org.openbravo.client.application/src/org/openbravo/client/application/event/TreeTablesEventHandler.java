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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.jfree.util.Log;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;

public class TreeTablesEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = getTreeTables();

  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onNew(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Client client = OBContext.getOBContext().getCurrentClient();
    Organization org = OBContext.getOBContext().getCurrentOrganization();
    BaseOBObject bob = event.getTargetInstance();
    Table table = OBDal.getInstance().get(Table.class,
        event.getTargetInstance().getEntity().getTableId());
    Tree adTree = getTree(table);
    if (adTree == null) {
      // The adTree does not exists, create it
      adTree = OBProvider.getInstance().get(Tree.class);
      adTree.setClient(client);
      adTree.setOrganization(org);
      adTree.setName(bob.getClass().getName());
      adTree.setAllNodes(true);
      // TODO: Change this
      adTree.setTypeArea("NEW");
      adTree.setTable(table);
      OBDal.getInstance().save(adTree);
    }
    // Adds the node to the adTree
    TreeNode adTreeNode = OBProvider.getInstance().get(TreeNode.class);
    adTreeNode.setClient(client);
    adTreeNode.setOrganization(org);
    adTreeNode.setTree(adTree);
    adTreeNode.setNode(bob.getId().toString());
    adTreeNode.setSequenceNumber(100L);
    adTreeNode.setReportSet("0");
    OBDal.getInstance().save(adTreeNode);
  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    BaseOBObject bob = event.getTargetInstance();
    Table table = OBDal.getInstance().get(Table.class,
        event.getTargetInstance().getEntity().getTableId());
    Tree adTree = getTree(table);
    OBCriteria<TreeNode> adTreeNodeCriteria = OBDal.getInstance().createCriteria(TreeNode.class);
    adTreeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_TREE, adTree));
    adTreeNodeCriteria.add(Restrictions.eq(TreeNode.PROPERTY_NODE, bob.getId().toString()));
    TreeNode treeNode = (TreeNode) adTreeNodeCriteria.uniqueResult();
    // Hay que:
    // - Borrar el treeNode
    // - Hacer un reparent de los nodos hijos de este treeNode
    String newParentId = treeNode.getReportSet();
    StringBuilder sql = new StringBuilder();
    sql.append(" UPDATE AD_TREENODE set parent_id = ? ");
    sql.append(" WHERE ad_tree_id = ? ");
    sql.append(" AND parent_id= ? ");
    try {
      PreparedStatement ps = OBDal.getInstance().getConnection(false)
          .prepareStatement(sql.toString());
      ps.setString(1, newParentId);
      ps.setString(2, adTree.getId());
      ps.setString(3, treeNode.getNode());
      int nChildrenMoved = ps.executeUpdate();
      Log.info(nChildrenMoved + " children have been moved to another parent");
    } catch (SQLException e) {
      Log.error("Error while deleting tree node: ", e);
      throw new OBException("NO GUARDAR!");
    }

  }

  private Tree getTree(Table table) {
    OBCriteria<Tree> adTreeCriteria = OBDal.getInstance().createCriteria(Tree.class);
    adTreeCriteria.add(Restrictions.eq(Tree.PROPERTY_TABLE, table));
    // TODO: Assuming one tree per table
    // TODO: Assuming ADTreeNode tree structure
    return (Tree) adTreeCriteria.uniqueResult();
  }

  private static Entity[] getTreeTables() {
    OBCriteria<Table> treeTablesCriteria = OBDal.getInstance().createCriteria(Table.class);
    treeTablesCriteria.add(Restrictions.eq(Table.PROPERTY_ISTREE, true));
    List<Table> treeTableList = treeTablesCriteria.list();
    ArrayList<Entity> entityArray = new ArrayList<Entity>();
    for (Table treeTable : treeTableList) {
      entityArray.add(ModelProvider.getInstance().getEntityByTableId(treeTable.getId()));
    }
    return (Entity[]) entityArray.toArray(new Entity[entityArray.size()]);
  }
}
