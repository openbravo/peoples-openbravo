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
 * All portions are Copyright (C) 2001-2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

/**
 * @author Fernando Iriazabal
 * 
 *         Class in charge of the keymap building for each window type.
 */
public class KeyMap {
  static Logger log4j = Logger.getLogger(KeyMap.class);

  /**
   * Constructor
   * 
   * @param _conn
   *          Handler for the database connection.
   * @param _tabId
   *          String with the tab's id.
   * @param _windowId
   *          String with the window's id.
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, String _tabId, String _windowId) throws Exception {
    if (_conn == null || _tabId == null || _tabId.equals("") || _windowId == null
        || _windowId.equals(""))
      throw new Exception("Missing parameters");
  }

  /**
   * Constructor
   * 
   * @param _conn
   *          Handler for the database connection.
   * @param _action
   *          String with the window type (form, report, process...)
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, String _action) throws Exception {
    if (_conn == null || _action == null || _action.equals(""))
      throw new Exception("Missing parameters");
  }

  /**
   * Gets the keymap for the Report window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getReportKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

}
