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
import org.openbravo.base.*;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Manages the tree of installed modules. 
 *
 * It implements GenericTree, detailed description is in that API doc.
 */
public class ModuleReferenceDataOrgTree extends ModuleTree {
  
  /**
   * Constructor to generate a root tree 
   * @param base
   * @param bSmall: Normal size or small size (true)
   * @param strClient: Client ID
   */
  public ModuleReferenceDataOrgTree(HttpBaseServlet base, String strClient, boolean bAddLinks, boolean bSmall) {
    super(base, bSmall);
    setRootTree(strClient, bAddLinks);
  }
  
  /**
   * Default constructor without parameters.
   * It is needed to be able to create instances by GenericTreeServlet, it must be implemented also by subclases.
   */   
  public ModuleReferenceDataOrgTree(){}
  
  /**
   * Constructor to generate a root tree 
   * @param base
   * @param bSmall: Normal size or small size (true)
   * @param strClient: Client ID
   * @param strOrg: Org ID
   */
  public ModuleReferenceDataOrgTree(HttpBaseServlet base, String strClient, String strOrg, boolean bAddLinks) {
    super(base);
    setRootTree(strClient, strOrg, bAddLinks);
  }

  /**
   * sets to data the root tree
   */
  public void setRootTree(String strClient, boolean bAddLinks) {
    try {
      data = ModuleReferenceDataOrgTreeData.select(conn, (lang.equals("")?"en_US":lang), strClient);
      if(bAddLinks)addLinks();
      setLevel(0);
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }


  /**
   * sets to data the root tree
   */
  public void setRootTree(String strClient, String strOrg, boolean bAddLinks) {
    try {
      data = ModuleReferenceDataOrgTreeData.selectOrg(conn, (lang.equals("")?"en_US":lang), strClient, strOrg);
      if(bAddLinks) addLinks();
      setLevel(0);
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }

  /**
   * Adds links to the current sets of nodes, these links can be Update or Apply
   */
  private void addLinks() {
    addLinks((ModuleReferenceDataOrgTreeData[])data, false);
  }
  
  private void addLinks(ModuleReferenceDataOrgTreeData[] modules, boolean showApplied){
    if (modules==null || modules.length==0) return;
    for (int i=0;i<modules.length;i++){
      if (modules[i].updateAvailable.equals("Y")) {
        modules[i].linkname=Utility.messageBD(conn, "UpdateAvailable", lang);
        //modules[i].linkclick="submitCommandFormParameter('OK', frmMain.inpNodes,'"+ modules[i].nodeId + "',false); return false;";
        String moduleId = modules[i].nodeId;
        modules[i].linkclick="gt_submitUpdateData('OK','"+ modules[i].nodeId + "'); return false;";
        //modules[i].linkclick="gt_getUpdateDescription('"+modules[i].nodeId+"'); return false;";
      }
      /*if (modules[i].status.equals("I")) {
        modules[i].linkname=Utility.messageBD(conn, "ApplyModules", lang)+", "+Utility.messageBD(conn, "RebuildNow", lang);
        modules[i].linkclick="openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 600, 900);return false;";
      }   
      if (modules[i].status.equals("U")) {
        modules[i].linkname=Utility.messageBD(conn, "UninstalledModule", lang);
        modules[i].linkclick="openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 600, 900);return false;";
      }*/
    }
  }  

  /**
   * Returns a HTML with the description for the given node
   * @param node
   * @return
   */
  public String getHTMLDescription(String node) {
    try {
      
      ModuleReferenceDataOrgTreeData[] data=  ModuleReferenceDataOrgTreeData.selectDescription(conn, lang, node);
      //addLinks(data, true);
      String discard[]={""};
      if (data!=null && data.length>0 && data[0].linkname!=null && !data[0].linkname.equals("")) 
        data[0].statusName = "";
      if (data!=null && data.length>0 && (data[0].updateAvailable==null || data[0].updateAvailable.equals("") || data[0].updateAvailable.equals("N"))) 
        discard[0] = "update";

      XmlDocument xmlDocument=xmlEngine.readXmlTemplate("org/openbravo/erpCommon/modules/ModuleTreeDescription",discard).createXmlDocument();
      xmlDocument.setData("structureDesc", data);
      return xmlDocument.print();
      
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
  /**
   * Generates a subtree with nodeId as root node
   * @param nodeId
   */
  public void setSubTree(String nodeId, String level) {
    setIsSubTree(true);
    try {
      data = ModuleReferenceDataOrgTreeData.selectSubTree(conn, (lang.equals("")?"en_US":lang), nodeId); 
      //addLinks();
      setLevel(new Integer(level).intValue());
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }
}
