package org.openbravo.base.secureApp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.erpCommon.utility.WindowTreeData;

public class OrgTreeNode implements Serializable {
  private static final long serialVersionUID=1L;
  private String id;
  private String parentId;
  private String value; 
  
  /**
   * Creates a node from data related to it
   * @param nodeData: info for the node
   */
  public OrgTreeNode(WindowTreeData nodeData){
    id = nodeData.id;
    parentId = nodeData.parentId;
    value = nodeData.name;
  }
  /**
   * Creates a tree from data and returns the root node
   * 
   * @param data: information to generete the tree
   * @return Node[]: Complete tree's nodes
   */
  public static List<OrgTreeNode> createTree(WindowTreeData[] data){
    List<OrgTreeNode> nodes = new ArrayList<OrgTreeNode>();
    
    for (int i=0; i<data.length; i++)
      nodes.add(new OrgTreeNode(data[i]));
    
    return nodes;
  }
  

  public String getParentId() {
    return parentId;
  }
  

  public String getId() {
    return id;
  }
  
  public String getValue(){
    return value;
  }
  
  public boolean equals(String s) {
    return id.equals(s);
  }
}
