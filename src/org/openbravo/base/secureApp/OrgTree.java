/*
 ************************************************************************************
 * Copyright (C) 2008-2017 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.base.secureApp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.WindowTreeData;

public class OrgTree implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<OrgTreeNode> nodes;
  private static final String AD_ORG_TABLE_ID = "155";
  private static final char QUOUTE = '\'';
  private static final String QUOUTE_COMMA = "',";

  /**
   * Creates a new Organization tree with all the nodes
   * 
   * @param conn
   *          DB connection
   * @param strClient
   *          client to get the org tree from
   */
  public OrgTree(ConnectionProvider conn, String strClient) {
    try {
      String treeID = WindowTreeData.selectTreeIDWithTableId(conn, "'" + strClient + "'",
          AD_ORG_TABLE_ID)[0].id;
      WindowTreeData[] data = WindowTreeData.selectOrg(conn, "", "", "", treeID);
      this.nodes = OrgTreeNode.createTree(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates an empty Tree
   */
  private OrgTree() {
    nodes = new ArrayList<OrgTreeNode>();

  }

  /**
   * Creates a tree with the nodes in nodeList
   * 
   * @param nodeList
   */
  private OrgTree(List<OrgTreeNode> nodeList) {
    nodes = nodeList;
  }

  /**
   * Creates a tree with a colon-separated AD_Org_ID in strOrgs
   * 
   * @param strOrgs
   *          colon-separated AD_Org_ID. Example "'0','1000000'"
   */
  private OrgTree(String strOrgs) {
    OrgTreeNode orgTreeNode;
    List<OrgTreeNode> nodeList = new ArrayList<OrgTreeNode>();

    int i = 0;
    int charAt_Old = 0;
    while (i < strOrgs.length()) {
      char c = strOrgs.charAt(i++);
      if (c == ' ')
        charAt_Old = i;
      else {
        if (c == ',') {
          String AD_Org_ID = strOrgs.substring(charAt_Old + 1, i - 2);
          charAt_Old = i;
          orgTreeNode = new OrgTreeNode(AD_Org_ID);
          nodeList.add(orgTreeNode);
        }
        // Get the last org of the string
        if (i == strOrgs.length() - 1) {
          String AD_Org_ID = strOrgs.substring(charAt_Old + 1, i);
          orgTreeNode = new OrgTreeNode(AD_Org_ID);
          nodeList.add(orgTreeNode);
        }
      }
    }

    nodes = nodeList;
  }

  /**
   * Returns a String with the Ready Organizations which are able to manage transactions (like for
   * example Invoices)
   * 
   * @param strOrgs
   *          colon-separated AD_Org_ID. Example "'0','1000000'"
   * @return colon-separated AD_Org_ID of Ready Organizations which are able to manage transactions.
   *         Example: "'0','1000000'"
   */
  public static String getTransactionAllowedOrgs(String strOrgs) {
    StringBuilder sb = new StringBuilder();
    OrgTree orgTree = new OrgTree(strOrgs);
    for (OrgTreeNode n : orgTree.nodes) {
      if ("true".equals(n.getIsReady()) && n.getOrgType().isTransactionsAllowed()) {
        sb.append(QUOUTE).append(n.getId()).append(QUOUTE_COMMA);
      }
    }
    // Remove the last ','
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  /**
   * Returns a sub-tree of the original tree with those organizations accessible by the role
   * 
   * @param conn
   * @param strRole
   * @return the created {@link OrgTree}
   */
  public OrgTree getAccessibleTree(ConnectionProvider conn, String strRole) {
    try {
      OrgTreeData[] data = OrgTreeData.select(conn, strRole);
      OrgTree accessibleOrgTree = new OrgTree();
      for (int i = 0; i < data.length; i++) {
        accessibleOrgTree.addTree(this.getLogicPath(data[i].adOrgId));
      }
      return accessibleOrgTree;
    } catch (Exception e) {
      return new OrgTree();
    }
  }

  /**
   * Obtains all nodes descendant of the given parentNodeId
   * 
   * @param parentNodeId
   * @return the new tree with the desdentant elements
   */
  private OrgTree getDescendantTree(String parentNodeId) {
    try {
      List<OrgTreeNode> treeNodes = new ArrayList<OrgTreeNode>();
      getDescendantTreeList(parentNodeId, treeNodes);
      return new OrgTree(treeNodes);
    } catch (Exception e) {
      return new OrgTree();
    }
  }

  /**
   * Obtains all nodes ascendant of the given nodId
   * 
   * @param nodeId
   * @return the new tree with the ascendant elements
   */
  private OrgTree getAscendantTree(String nodeId) {
    try {
      List<OrgTreeNode> treeNodes = new ArrayList<OrgTreeNode>();
      OrgTreeNode parentNode = getNodeById(nodeId);
      while (parentNode != null) {
        treeNodes.add(parentNode);
        parentNode = getNodeById(parentNode.getParentId());
      }
      return new OrgTree(treeNodes);
    } catch (Exception e) {
      return new OrgTree();
    }
  }

  /**
   * Obtains the logic path for a node, this is the sum of all desdendat nodes and all ascendant
   * ones.
   * 
   * @param nodeId
   * @return the new tree with the Logic Path
   */
  public OrgTree getLogicPath(String nodeId) {
    try {
      return addTree(this.getDescendantTree(nodeId), this.getAscendantTree(nodeId));
    } catch (Exception e) {
      return new OrgTree();
    }
  }

  /**
   * Converts the tree into String. Nodes comma separated.
   */
  public String toString() {
    if (nodes == null) {
      return "";
    }

    StringBuilder s = new StringBuilder();
    int size = nodes.size();
    int i = 0;
    for (OrgTreeNode node : nodes) {
      if (node == null) {
        size -= 1;
        continue;
      }
      i++;
      s.append(QUOUTE).append(node.getId());
      if (i == size) {
        s.append(QUOUTE);
      } else {
        s.append(QUOUTE_COMMA);
      }
    }
    return s.toString();
  }

  /**
   * Converts the tree into String. Displaying the name in order to make it more understandable
   */
  public String toDebugString() {
    String s = "";
    if (nodes == null)
      return "";
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i) != null)
        s += nodes.get(i).getId() + " - " + nodes.get(i).getValue()
            + ((i < nodes.size() - 1) ? "\n" : "");
    }
    return s;
  }

  /**
   * Sums the nodes in t1 which do not exist in the current tree.
   * 
   * @param t1
   */
  private void addTree(OrgTree t1) {
    if ((t1 != null) && (t1.nodes != null)) {
      for (int i = 0; i < t1.nodes.size(); i++) {
        if (!this.isNodeInTree(t1.nodes.get(i).getId()))
          this.nodes.add(t1.nodes.get(i));
      }
    }
  }

  /**
   * Sums different nodes of two trees. And returns a new one.
   * 
   * @param t1
   *          tree1
   * @param t2
   *          tree2
   * @return new tree (t1+t2)
   */
  private static OrgTree addTree(OrgTree t1, OrgTree t2) {
    List<OrgTreeNode> treeNodes = new ArrayList<OrgTreeNode>();

    treeNodes.addAll(t1.nodes);

    OrgTree returnTree = new OrgTree(treeNodes);
    returnTree.addTree(t2);
    return returnTree;
  }

  /**
   * List param is modified with all the child nodes, repeatNodes decides what to do in case of
   * repeated nodes.
   * 
   * @param parentNodeId
   * @param list
   * @param repeatNodes
   */
  private void getDescendantTreeList(String parentNodeId, List<OrgTreeNode> list,
      boolean repeatNodes, boolean withZero) {
    List<OrgTreeNode> childNodes = getNodesWithParent(parentNodeId);
    if (repeatNodes) {
      if (withZero || !getNodeById(parentNodeId).getId().equals("0")) {
        OrgTreeNode node = getNodeById(parentNodeId);
        if (node != null) {
          list.add(getNodeById(parentNodeId));
        }
      }
    } else {
      if ((list.size() == 0) && (withZero || !getNodeById(parentNodeId).getId().equals("0")))
        list.add(getNodeById(parentNodeId));
      else {
        boolean exists = false;
        for (int i = 0; i < list.size(); i++)
          if (list.get(i).equals(parentNodeId))
            exists = true;
        if ((!exists) && (withZero || !getNodeById(parentNodeId).getId().equals("0")))
          list.add(getNodeById(parentNodeId));
      }
    }
    if (childNodes.size() != 0)
      for (int i = 0; i < childNodes.size(); i++)
        getDescendantTreeList(childNodes.get(i).getId(), list, repeatNodes, withZero);

  }

  /**
   * List param is modified with all the child nodes
   * 
   * @param parentNodeId
   * @param list
   */
  private void getDescendantTreeList(String parentNodeId, List<OrgTreeNode> list) {
    getDescendantTreeList(parentNodeId, list, true, true);
  }

  /**
   * Returns the node matching the id, in case it does not exists it returns null
   */
  private OrgTreeNode getNodeById(String id) {
    if (nodes == null)
      return null;
    for (int i = 0; i < nodes.size(); i++)
      if (nodes.get(i).equals(id))
        return nodes.get(i);
    return null;
  }

  /**
   * In case the node id is in the tree it returns true, if not false.
   */
  private boolean isNodeInTree(String id) {
    if (nodes == null)
      return false;
    for (int i = 0; i < nodes.size(); i++)
      if (nodes.get(i).equals(id))
        return true;
    return false;
  }

  /**
   * Returns the list of the nodes that have a determinate node.
   */
  private List<OrgTreeNode> getNodesWithParent(String parentId) {
    List<OrgTreeNode> vecNodes = new ArrayList<OrgTreeNode>();
    for (OrgTreeNode node : nodes) {
      if (node.getParentId().equals(parentId)) {
        vecNodes.add(node);
      }
    }
    return vecNodes;
  }

}
