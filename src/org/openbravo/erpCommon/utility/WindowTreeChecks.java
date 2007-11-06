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

import org.openbravo.database.ConnectionProvider;
import org.openbravo.base.secureApp.*;
import javax.servlet.*;
import org.apache.log4j.Logger ;


public class WindowTreeChecks {
  static Logger log4j = Logger.getLogger(WindowTreeChecks.class);
  
  public static String checkChanges(ConnectionProvider conn, VariablesSecureApp vars, String tabId, String topNodeId, String nodeId, boolean isChild) throws ServletException {
    String result = "";
    if (topNodeId.equals(nodeId)) return Utility.messageBD(conn, "SameElement", vars.getLanguage());
    try {
      String table = WindowTreeData.selectTableName(conn, tabId);
      String key = WindowTreeData.selectKey(conn, tabId);
      String TreeType = WindowTreeUtility.getTreeType(key);
      if (isChild && !topNodeId.equals("0") && WindowTreeChecksData.selectIsSummary(conn, table, key, topNodeId).equals("N")) return Utility.messageBD(conn, "NotIsSummary", vars.getLanguage());
      if (log4j.isDebugEnabled()) log4j.debug("key:"+key+", nodeId:"+nodeId+",topNodeId:"+topNodeId);
      String treeID;
      WindowTreeData[] data = WindowTreeData.selectTreeID(conn, vars.getClient(), TreeType);
    
      if (!(data==null || data.length==0)) {
        treeID = data[0].id;
        if (!WindowTreeChecksData.isItsOwnChild(conn,treeID, topNodeId,nodeId).equals("0")) return Utility.messageBD(conn,"RecursiveTree",vars.getLanguage());
      }
      result = WindowTreeChecks.checkSpecificChanges(conn, vars, tabId, topNodeId, nodeId, isChild, TreeType, key);
    } catch (ServletException ex) {
      log4j.error(ex);
      return Utility.messageBD(conn, "Error", vars.getLanguage());
    }
    return result;
  }

  public static String checkSpecificChanges(ConnectionProvider conn, VariablesSecureApp vars, String tabId, String topNodeId, String nodeId, boolean isChild, String TreeType, String key) throws ServletException {
    String result = "";
    if(TreeType.equals("MM")) { //Menu
      result = "";
    } else if (TreeType.equals("OO")) { //Organization
      result = "";
    } else if (TreeType.equals("PR")) { //Product
      result = "";
    } else if (TreeType.equals("PC")) { //Product Category
      result = "";
    } else if (TreeType.equals("BB")) { //Product BOM
      result = "";
    } else if (TreeType.equals("EV")) { //Element Value
      result = "";
    } else if (TreeType.equals("BP")) { //BusinessPartner
      result = "";
    } else if (TreeType.equals("MC")) { //Campaign
      result = "";
    } else if (TreeType.equals("PJ")) { //Project
      result = "";
    } else if (TreeType.equals("AY")) { //Activity
      result = "";
    } else if (TreeType.equals("SR")) { //Sales Region
      result = "";
    } else if (TreeType.equals("AR")){ //Accounting report
      result = "";
    } else result = WindowTreeChecksClient.checkChanges(conn, vars, tabId, topNodeId, nodeId, isChild, TreeType, key);
    return result;
  }
}
