/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2008 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.modules;

import javax.servlet.ServletException;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.GenericTree;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Manages the tree of installed modules.
 * 
 * It implements GenericTree, detailed description is in that API doc.
 */
public class ModuleTree extends GenericTree {

  /**
   * Constructor to generate a root tree
   * 
   * @param base
   */
  public ModuleTree(HttpBaseServlet base) {
    super(base);
    setRootTree();
  }

  /**
   * Constructor to generate a root tree
   * 
   * @param base
   * @param bSmall
   *          Normal size or small size (true)
   */
  public ModuleTree(HttpBaseServlet base, boolean bSmall) {
    super(base, bSmall);
    // setRootTree();
  }

  /**
   * Default constructor, needed by GenericTreeServlet
   */
  public ModuleTree() {
  }

  /**
   * sets to data the root tree
   */
  public void setRootTree() {
    try {
      data = ModuleTreeData.select(conn, (lang.equals("") ? "en_US" : lang));
      addLinks();
      setLevel(0);
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }

  /**
   * Generates a subtree with nodeId as root node
   * 
   * @param nodeId
   */
  public void setSubTree(String nodeId, String level) {
    setIsSubTree(true);
    try {
      data = ModuleTreeData.selectSubTree(conn, (lang.equals("") ? "en_US" : lang), nodeId);
      addLinks();
      setLevel(new Integer(level).intValue());
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }

  /**
   * Returns a HTML with the description for the given node
   * 
   * @param node
   * @return a HTML String with the description for the given node
   */
  public String getHTMLDescription(String node) {
    try {

      ModuleTreeData[] data = ModuleTreeData.selectDescription(conn, lang, node);
      addLinks(data, true);
      String discard[] = { "" };
      if (data != null && data.length > 0 && data[0].linkname != null
          && !data[0].linkname.equals(""))
        data[0].statusName = "";
      if (data != null && data.length > 0
          && (data[0].updateAvailable == null || data[0].updateAvailable.equals("")))
        discard[0] = "update";

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/modules/ModuleTreeDescription", discard).createXmlDocument();
      xmlDocument.setData("structureDesc", data);
      return xmlDocument.print();

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Adds links to the current sets of nodes, these links can be Update or Apply
   */
  private void addLinks() {
    addLinks((ModuleTreeData[]) data, false);
  }

  private void addLinks(ModuleTreeData[] modules, boolean showApplied) {
    if (modules == null || modules.length == 0)
      return;
    for (int i = 0; i < modules.length; i++) {
      if (!modules[i].updateAvailable.equals("")) {
        modules[i].linkname = Utility.messageBD(conn, "UpdateAvailable", lang);
        modules[i].linkclick = "gt_getUpdateDescription('" + modules[i].nodeId
            + "'); return false;";
      }
      if (modules[i].status.equals("I") || modules[i].status.equals("P")) {
        modules[i].linkname = Utility.messageBD(conn, "ApplyModules", lang) + ", "
            + Utility.messageBD(conn, "RebuildNow", lang);
        modules[i].linkclick = "openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 600, 900);return false;";
      }
      if (modules[i].status.equals("U")) {
        modules[i].linkname = Utility.messageBD(conn, "UninstalledModule", lang);
        modules[i].linkclick = "openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 600, 900);return false;";
      }
    }
  }

  /**
   * Selects the correct icons for the current set of nodes depending on the module type (Module,
   * Pack or Template), it also sets the secondary icon in case the node have available updates.
   */
  protected void setIcons() {
    setIcons(data);
  }

  /**
   * Set the icons (module type) and subicons (update available) for each node
   * 
   * @param modules
   */
  private void setIcons(FieldProvider[] modules) {
    if (modules == null || modules.length == 0)
      return;
    for (int i = 0; i < modules.length; i++) {
      if (modules[i].getField("type").equals("M"))
        FieldProviderFactory.setField(modules[i], "icon", "Tree_Icon_Module");
      if (modules[i].getField("type").equals("P"))
        FieldProviderFactory.setField(modules[i], "icon", "Tree_Icon_Pack");
      if (modules[i].getField("type").equals("T"))
        FieldProviderFactory.setField(modules[i], "icon", "Tree_Icon_Template");

      boolean updateAvailable = modules[i].getField("updateAvailable") != null
          && !modules[i].getField("updateAvailable").equals("");
      boolean updateAvailableInChildNode = !updateAvailable
          && hasChildUpdate(modules[i].getField("nodeId"));

      if (updateAvailable || updateAvailableInChildNode)
        FieldProviderFactory.setField(modules[i], "icon2", "Tree_Icon_Update");
    }
  }

  /**
   * Returns true in case one of the descendant of the current node has an update available
   * 
   * @param node
   * @return
   */
  private boolean hasChildUpdate(String node) {
    try {
      ModuleTreeData data[] = ModuleTreeData.selectSubTree(conn, "", node);
      if (data == null || data.length == 0)
        return false;
      for (int i = 0; i < data.length; i++) {
        if (data[i].updateAvailable != null && !data[i].updateAvailable.equals(""))
          return true;
        if (hasChildUpdate(data[i].nodeId))
          return true;
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  protected String getNodePosition(String nodeID) {
    try {
      String parentNodeID = getParent(nodeID);
      ModuleTreeData[] tree;
      if (parentNodeID.equals(""))
        tree = ModuleTreeData.select(conn, (lang.equals("") ? "en_US" : lang)); // Root
      else
        tree = ModuleTreeData.selectSubTree(conn, (lang.equals("") ? "en_US" : lang), parentNodeID); // Subtree

      if (tree == null || tree.length == 0)
        return "0";
      for (int i = 0; i < tree.length; i++) {
        if (tree[i].nodeId.equals(nodeID)) {
          return new Integer(i + 1).toString();
        }
      }
      return "0";
    } catch (Exception e) {
      e.printStackTrace();
      return "0";
    }
  }

  /**
   * Returns the node id for the parent of the passed node
   * 
   * @param node
   * @return the node id for the parent of the passed node
   */
  protected String getParent(String node) {
    try {
      return ModuleTreeData.selectParent(conn, node);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  protected boolean isLastLevelNode(String nodeID) {
    try {
      String parentNodeID = getParent(nodeID);
      ModuleTreeData[] tree;
      if (parentNodeID.equals(""))
        tree = ModuleTreeData.select(conn, (lang.equals("") ? "en_US" : lang)); // Root
      else
        tree = ModuleTreeData.selectSubTree(conn, (lang.equals("") ? "en_US" : lang), parentNodeID); // Subtree

      if (tree == null || tree.length == 0)
        return true;
      for (int i = 0; i < tree.length; i++) {
        if (tree[i].nodeId.equals(nodeID)) {
          return i == (tree.length - 1);
        }
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return true;
    }
  }

}
