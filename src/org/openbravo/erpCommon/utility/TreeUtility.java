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
 * All portions are Copyright (C) 2012-2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;

public class TreeUtility {
  private static final Logger log4j = Logger.getLogger(TreeUtility.class);

  private Map<String, Set<String>> childTrees = new HashMap<>();
  private Map<String, Set<String>> naturalTrees = new HashMap<>();

  /**
   * Gets Natural tree for the given node
   */
  @Deprecated
  public Set<String> getNaturalTree(String nodeId, String treeType) {
    initialize(treeType);
    Set<String> result;
    if (naturalTrees.get(nodeId) == null) {
      result = new HashSet<>();
      result.add(nodeId);
    } else {
      result = new HashSet<>(naturalTrees.get(nodeId));
    }
    log4j.debug("Natural Tree(" + treeType + ") for the node" + nodeId + ":" + result.toString());
    return result;
  }

  /**
   * Gets the Child tree for the given node, including optionally given node
   */
  @Deprecated
  public Set<String> getChildTree(String nodeId, String treeType, boolean includeNode) {
    initialize(treeType);
    Set<String> childNode = this.getChildNode(nodeId, treeType);
    Set<String> result = new HashSet<>();

    if (includeNode)
      result.add(nodeId);

    while (!childNode.isEmpty()) {
      for (String co : childNode) {
        result.add(co);
        childNode = this.getChildTree(co, treeType, false);
        result.addAll(childNode);
      }
    }
    return result;
  }

  public Set<String> getChildTree(String nodeId, String treeType) {
    Set<String> result = new HashSet<>();

    Deque<String> pendingNodes = new ArrayDeque<>();
    pendingNodes.push(nodeId);

    while (!pendingNodes.isEmpty()) {
      String nextNodeId = pendingNodes.pop();
      result.add(nextNodeId);
      pendingNodes.addAll(getChildrenOf(nextNodeId, treeType));
    }
    return result;
  }

  private List<String> getChildrenOf(String nodeId, String treeType) {
    List<Tree> treeIds = getTreeIdsFromTreeType(treeType);
    List<String> treeNodeIds = new ArrayList<>();
    for (Tree tree : treeIds) {
      treeNodeIds.addAll(getChildrenOfTreeNode(tree, nodeId));
    }
    return treeNodeIds;
  }

  private List<String> getChildrenOfTreeNode(final Tree t, String nodeId) {
    final String nodeQryStr = "select tn.node from " + TreeNode.class.getName()
        + " tn where tn.tree.id='" + t.getId() + "' and tn.reportSet = '" + nodeId + "'";
    final Query<String> nodeQry = SessionHandler.getInstance()
        .createQuery(nodeQryStr, String.class);
    return nodeQry.list();
  }

  /**
   * Gets Child node in the tree
   */
  @Deprecated
  public Set<String> getChildNode(String nodeId, String treeType) {
    initialize(treeType);
    if (childTrees.get(nodeId) == null) {
      return new HashSet<>();
    } else {
      return new HashSet<>(childTrees.get(nodeId));
    }
  }

  @Deprecated
  private void initialize(String treeType) {

    final List<Tree> ts = getTreeIdsFromTreeType(treeType);

    final List<TreeNode> treeNodes = new ArrayList<>();
    for (final Tree t : ts) {
      final List<TreeNode> tns = getTreeNodesOfTree(t);
      treeNodes.addAll(tns);
    }

    final List<Node> nodes = new ArrayList<>(treeNodes.size());
    for (final TreeNode tn : treeNodes) {
      final Node on = new Node();
      on.setTreeNode(tn);
      nodes.add(on);
    }

    for (final Node on : nodes) {
      on.resolve(nodes);
    }

    for (final Node on : nodes) {
      naturalTrees.put(on.getTreeNode().getNode(), on.getNaturalTree());
      if (on.getChildren() != null) {
        Set<String> os = new HashSet<>();
        for (Node o : on.getChildren())
          os.add(o.getTreeNode().getNode());
        childTrees.put(on.getTreeNode().getNode(), os);
      }
    }
  }

  private List<TreeNode> getTreeNodesOfTree(final Tree t) {
    final String nodeQryStr = "select tn from " + TreeNode.class.getName()
        + " tn where tn.tree.id='" + t.getId() + "'";
    final Query<TreeNode> nodeQry = SessionHandler.getInstance().createQuery(nodeQryStr,
        TreeNode.class);
    return nodeQry.list();
  }

  private List<Tree> getTreeIdsFromTreeType(String treeType) {
    final String clientId = OBContext.getOBContext().getCurrentClient().getId();
    final String qryStr = "select t from " + Tree.class.getName() + " t where treetype='"
        + treeType + "' and client.id='" + clientId + "'";
    final Query<Tree> qry = SessionHandler.getInstance().createQuery(qryStr, Tree.class);
    return qry.list();
  }
}

class Node {

  private TreeNode treeNode;
  private Node parent;
  private List<Node> children = new ArrayList<>();

  private Set<String> naturalTreeParent = null;
  private Set<String> naturalTreeChildren = null;
  private Set<String> naturalTree = null;

  void addChild(Node child) {
    children.add(child);
  }

  public void resolve(List<Node> nodes) {
    if (treeNode.getReportSet() == null) {
      return;
    }
    for (final Node on : nodes) {
      if (on.getTreeNode().getNode().equals(treeNode.getReportSet())) {
        on.addChild(this);
        setParent(on);
        break;
      }
    }
  }

  public Set<String> getNaturalTree() {
    if (naturalTree == null) {
      naturalTree = new HashSet<>();
      naturalTree.add(getTreeNode().getNode());
      if (getParent() != null) {
        getParent().getParentPath(naturalTree);
      }
      for (final Node child : getChildren()) {
        child.getChildPath(naturalTree);
      }
    }
    return naturalTree;
  }

  public void getParentPath(Set<String> theNaturalTree) {
    if (naturalTreeParent == null) {
      naturalTreeParent = new HashSet<>();
      naturalTreeParent.add(getTreeNode().getNode());
      if (getParent() != null) {
        getParent().getParentPath(naturalTreeParent);
      }
    }
    theNaturalTree.addAll(naturalTreeParent);
  }

  public void getChildPath(Set<String> theNaturalTree) {
    if (naturalTreeChildren == null) {
      naturalTreeChildren = new HashSet<>();
      naturalTreeChildren.add(getTreeNode().getNode());
      for (final Node child : getChildren()) {
        child.getChildPath(naturalTreeChildren);
      }
    }
    theNaturalTree.addAll(naturalTreeChildren);
  }

  public TreeNode getTreeNode() {
    return treeNode;
  }

  public void setTreeNode(TreeNode treeNode) {
    this.treeNode = treeNode;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public List<Node> getChildren() {
    return children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }
}
