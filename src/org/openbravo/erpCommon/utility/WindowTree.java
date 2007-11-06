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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.base.secureApp.*;
import org.openbravo.data.Sqlc;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;
import org.openbravo.utils.FormatUtilities;



public class WindowTree extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final String CHILD_SHEETS="frameWindowTreeF3";

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }
  
  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT", "TAB")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "WindowTree|tabId");
      String strTreeID="";
      String key=WindowTreeData.selectKey(this, strTabId);
      {
        String TreeType = WindowTreeUtility.getTreeType(key);
        WindowTreeData[] data = WindowTreeData.selectTreeID(this, vars.getClient(), TreeType);
        if (data!=null && data.length>0) strTreeID = data[0].id;
      }
      WindowTreeData[] data = WindowTreeData.selectTabName(this, strTabId);
      String windowName = FormatUtilities.replace(data[0].description);
      String tabName = FormatUtilities.replace(data[0].name);
      String strHref = windowName + "/" + tabName + "_Relation.html?Command=" + vars.getCommand();
      {
        WindowTreeData[] parents = WindowTreeData.selectParents(this, strTabId);
        if (parents!=null && parents.length>0) {
          for (int i=0;i<parents.length;i++) {
            String strField = "inp" + Sqlc.TransformaNombreColumna(parents[i].name);
            String strData = vars.getGlobalVariable(strField, parents[i].nodeId + "|" + parents[i].name);
            strHref += "&" + strField + "=" + strData;
          }
        }
      }
      if (strTreeID.equals("")) advisePopUp(response, "Error", Utility.messageBD(this, "AccessTableNoView", vars.getLanguage()));//response.sendRedirect(strDireccion + "/" + strHref);
      else printPageDataSheet(response, vars, strTabId);
    } else if (vars.commandIn("ASSIGN")) {
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strTop = vars.getRequiredStringParameter("inpTop");
      String strLink = vars.getRequiredStringParameter("inpLink");
      String strChild = vars.getStringParameter("inpChild", "N");
      String strResult = WindowTreeChecks.checkChanges(this, vars, strTabId, strTop, strLink, strChild.equals("Y"));
      if (strResult.equals("")) changeNode(vars, strTabId, strTop, strLink, strChild);
      else {
        vars.setSessionValue("WindowTree|message", strResult);
      }
      vars.setSessionValue("WindowTree|tabId", strTabId);
      PrintWriter out = response.getWriter();
      out.print(strResult);
      out.close();
      //response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT&inpTabId=" + strTabId);
    } else throw new ServletException();
  }

  public String loadNodes(VariablesSecureApp vars, String key, boolean editable, String strTabId) throws ServletException {
    String TreeType = WindowTreeUtility.getTreeType(key);
    String TreeID="";
    String TreeName="";
    String TreeDescription="";
    WindowTreeData[] data = WindowTreeData.selectTreeID(this, vars.getClient(), TreeType);
    StringBuffer menu = new StringBuffer();
    if (data==null || data.length==0) {
      log4j.error("WindowTree.loadNodes() - Unknown TreeNode: TreeType " + TreeType + " - TreeKey " + key);
      throw new ServletException("WindowTree.loadNodes() - Unknown TreeNode");
    } else {
      TreeID = data[0].id;
      TreeName = data[0].name;
      TreeDescription = data[0].description;
    }
    if (log4j.isDebugEnabled()) log4j.debug("WindowTree.loadNodes() - TreeType: " + TreeType + " || TreeID: " + TreeID);
    menu.append("\n<ul class=\"dhtmlgoodies_tree\">\n");
    menu.append(WindowTreeUtility.addNodeElement(TreeName, TreeDescription, CHILD_SHEETS, true, "", strDireccion, "clickItem(0, '" + Replace.replace(TreeName, "'", "\\'") + "', 'N');", "dblClickItem(0);", true, "0", ""));
    //menu.append(WindowTreeUtility.addNodeElement(TreeName, TreeDescription, CHILD_SHEETS, true, "", strDireccion, "clickItem(0, '" + Replace.replace(TreeName, "'", "\\'") + "', 'N');", "", true, "0", ""));
    menu.append(generateTree(WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable, "", "", strTabId), strDireccion, "0", true));
    menu.append("\n</ul>\n");
    return menu.toString();
  }

  public String loadChildNodes(VariablesSecureApp vars, String key, String strTreeID, String strParentID, boolean editable, String strTabId) throws ServletException {
    String TreeType = WindowTreeUtility.getTreeType(key);
    StringBuffer menu = new StringBuffer();
    menu.append("<ul>\n");
    StringBuffer script = new StringBuffer();
    script.append(generateTree(WindowTreeUtility.getTree(this, vars, TreeType, strTreeID, editable, strParentID, "", strTabId), strDireccion, strParentID, true));
    if (!script.equals("")) {
      String TreeName="", TreeDescription="";
      WindowTreeData[] data=null;
      if (strParentID.equals("0")) data = WindowTreeData.selectTreeID(this, vars.getClient(), TreeType);
      else data = WindowTreeUtility.getTree(this, vars, TreeType, strTreeID, editable, "", strParentID, strTabId);
      if (data==null || data.length==0) {
        log4j.error("WindowTree.loadNodes() - Unknown TreeNode");
        throw new ServletException("WindowTree.loadNodes() - Unknown TreeNode");
      } else {
        TreeName = data[0].name;
        TreeDescription = data[0].description;
      }
      menu.append(WindowTreeUtility.addNodeElement(TreeName, TreeDescription, CHILD_SHEETS, true, "", strDireccion, "", "", false, "0", ""));
      menu.append(script);
    }
    menu.append("</ul>\n");
    return menu.toString();
  }

  public String generateTree(WindowTreeData[] data, String strDireccion, String indice, boolean isFirst) {
    if (data==null || data.length==0) return "";
    if (log4j.isDebugEnabled()) log4j.debug("WindowTree.generateTree() - data: " + data.length);
    if (indice == null) indice="0";
    boolean hayDatos=false;
    StringBuffer strCabecera = new StringBuffer();
    StringBuffer strResultado= new StringBuffer();
    strCabecera.append("<ul>");
    isFirst=false;
    for (int i=0;i<data.length;i++) {
      if (data[i].parentId.equals(indice)) {
        hayDatos=true;
        String strHijos = generateTree(data, strDireccion, data[i].nodeId, isFirst);
        strResultado.append(WindowTreeUtility.addNodeElement(data[i].name, data[i].description, CHILD_SHEETS, data[i].issummary.equals("Y"), WindowTreeUtility.windowType(data[i].action), strDireccion, "clickItem(" + data[i].nodeId + ", '" + Replace.replace(data[i].name, "'", "\\'") + "', '" + data[i].issummary + "');", "dblClickItem(" + data[i].nodeId + ");", !strHijos.equals(""), data[i].nodeId, data[i].action));
        //strResultado.append(WindowTreeUtility.addNodeElement(data[i].name, data[i].description, CHILD_SHEETS, data[i].issummary.equals("Y"), WindowTreeUtility.windowType(data[i].action), strDireccion, "clickItem(" + data[i].nodeId + ", '" + Replace.replace(data[i].name, "'", "\\'") + "', '" + data[i].issummary + "');", "", !strHijos.equals(""), data[i].nodeId, data[i].action));
        strResultado.append(strHijos);
      }
    }
    return (hayDatos?(strCabecera.toString() + strResultado.toString() + "</li></ul>"):"");
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String TabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Tree's screen for the tab: " + TabId);
    OBError defaultInfo = new OBError();
    defaultInfo.setType("INFO");
    defaultInfo.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
    defaultInfo.setMessage(Utility.messageBD(this, "TreeInfo", vars.getLanguage()));
    vars.setMessage("WindowTree", defaultInfo);
    
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/WindowTree").createXmlDocument();
    
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    String strTreeID="";
    String key=WindowTreeData.selectKey(this, TabId);
    {
      String TreeType = WindowTreeUtility.getTreeType(key);
      WindowTreeData[] data = WindowTreeData.selectTreeID(this, vars.getClient(), TreeType);
      if (data!=null && data.length>0) strTreeID = data[0].id;
    }
    WindowTreeData[] data = WindowTreeData.selectTabName(this, TabId);
    String windowName = FormatUtilities.replace(data[0].description);
    String tabName = FormatUtilities.replace(data[0].name);
    xmlDocument.setParameter("description", data[0].name);
    xmlDocument.setParameter("page", "../" + windowName + "/" + tabName + "_Edition.html");
    xmlDocument.setParameter("menu", loadNodes(vars, key, WindowTreeData.selectEditable(this, TabId).equals("Y"), TabId));
    xmlDocument.setParameter("treeID", strTreeID);
    xmlDocument.setParameter("tabID", TabId);
    key = "inp" + Sqlc.TransformaNombreColumna(key);
    xmlDocument.setParameter("keyField", key);
    xmlDocument.setParameter("keyFieldScript", "function getKeyField() {\n return document.frmMain." + key + ";\n}\n");
    
    try {
      OBError myMessage = vars.getMessage("WindowTree");
      vars.removeMessage("WindowTree");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void changeNode(VariablesSecureApp vars, String strTabId, String strTop, String strLink, String strChild) throws ServletException {
    String key = WindowTreeData.selectKey(this, strTabId);
    String TreeType = WindowTreeUtility.getTreeType(key);
    String TreeID="";
    String strParent=strTop;
    boolean editable = WindowTreeData.selectEditable(this, strTabId).equals("Y");
    //Calculating the TreeID
    {
      WindowTreeData[] data = WindowTreeData.selectTreeID(this, vars.getClient(), TreeType);
      if (data==null || data.length==0) {
        log4j.error("WindowTree.loadNodes() - Unknown TreeNode");
        throw new ServletException("WindowTree.loadNodes() - Unknown TreeNode");
      } else {
        TreeID = data[0].id;
      }
    }
    //Calculating the parent
    if (!strTop.equals("0")) {
      WindowTreeData[] data = WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable, "", strTop, strTabId);
      if (data==null || data.length==0) {
        log4j.error("WindowTree.loadNodes() - Unknown Top Node");
        throw new ServletException("WindowTree.loadNodes() - Unknown Top Node");
      }
      
      if (!data[0].issummary.equals("Y") || !strChild.equals("Y")) {
        strParent = data[0].parentId;
      }
    } else strParent = strTop;
    WindowTreeData[] data = WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable, strParent, "", strTabId);
    int seqNo=10;
    try {
      if (data==null || data.length==0) {
        WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink, Integer.toString(seqNo));
      } else {
        boolean updated=false;
        if (strParent.equals(strTop)) {
          WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink, Integer.toString(seqNo));
          seqNo += 10;
          updated=true;
        }
        for (int i=0;i<data.length;i++) {
          if (!data[i].nodeId.equals(strLink)) {
            WindowTreeUtility.setNode(this, vars, TreeType, TreeID, data[i].parentId, data[i].nodeId, Integer.toString(seqNo));
            seqNo += 10;
            if (!updated && data[i].nodeId.equals(strTop)) {
              WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink, Integer.toString(seqNo));
              seqNo += 10;
              updated=true;
            }
          }
        }
        if (!updated) WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink, Integer.toString(seqNo));
      }
    } catch (ServletException e) {
      log4j.error("WindowTree.changeNode() - Couldn't change the node: " + strLink);
      log4j.error("WindowTree.setNode() - error: " + e);
      throw new ServletException(e);
    }
  }

  public String getServletInfo() {
    return "Servlet that presents the tree of a TreeNode windo windoww";
  } // end of getServletInfo() method
}
