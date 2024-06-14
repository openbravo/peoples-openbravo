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
 * All portions are Copyright (C) 2001-2023 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;

public class NavigationBar {

  public NavigationBar(ConnectionProvider _conn, String _language, String _action,
      String _windowName, String _windowType, String _baseDirection, String _breadcrumb) {
  }

  public NavigationBar(ConnectionProvider _conn, String _language, String _action,
      String _windowName, String _windowType, String _baseDirection, String _breadcrumb,
      boolean _hideBack) {
  }

  public NavigationBar(ConnectionProvider _conn, String _language, String _action,
      String _windowName, String _windowType, String _baseDirection, String _breadcrumb,
      boolean hideBack, boolean _validateChangesOnRefresh) {
    this(_conn, _language, _action, _windowName, _windowType, _baseDirection, _breadcrumb,
        hideBack);
  }

  @Override
  public String toString() {

    final StringBuffer toolbar = new StringBuffer();
    toolbar.append("<TABLE class=\"Main_ContentPane_NavBar\" id=\"tdtopNavButtons\">\n");
    toolbar.append("</TABLE>");
    return toolbar.toString();
  }
}
