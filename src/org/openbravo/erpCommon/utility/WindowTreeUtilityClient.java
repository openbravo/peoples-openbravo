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
import org.openbravo.data.FieldProvider;
import org.apache.log4j.Logger ;


public class WindowTreeUtilityClient {
  static Logger log4j = Logger.getLogger(WindowTreeUtilityClient.class);
  
  public static String getTreeType(String keyColumnName) {
    if (log4j.isDebugEnabled()) log4j.debug("WindowTreeUtilityClient.getTreeID() - key Column: " + keyColumnName);
    String TreeType = "";
    if(keyColumnName.equals("AT_Test_ID")) TreeType = "II";
    else if(keyColumnName == null || keyColumnName.length() == 0) return "";
    return TreeType;
  }

  public static FieldProvider[] getTree(ConnectionProvider conn, VariablesSecureApp vars, String TreeType, String TreeID, boolean editable, String strParentID, String strNodeId, String strTabID) throws ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("WindowTreeUtilityClient.getTree() - TreeID: " + TreeID);
    FieldProvider[] data = null;
    String strEditable = (editable?"editable":"");
    if (TreeType.equals("II")) data = WindowTreeUtilityClientData.selectInstructions(conn, strEditable, strParentID, strNodeId, vars.getUser(), TreeID);
    if (data==null) throw new ServletException("WindowTreeUtilityClient.getTree - Unknown TreeType=" + TreeType);
    return data;
  }


  public static String windowType(String type) {
    if (log4j.isDebugEnabled()) log4j.debug("WindowTreeUtilityClient.windowType() - type: " + type);
    if (type==null || type.equals("")) return "";
    else return "";
  }

  public static String windowTypeNico(String tipo) {
    if (log4j.isDebugEnabled()) log4j.debug("WindowTreeUtilityClient.windowTypeNico() - type: " + tipo);
    if (tipo==null) return "";
    else return "";
  }
}
