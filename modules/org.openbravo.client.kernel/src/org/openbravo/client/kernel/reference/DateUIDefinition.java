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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

/**
 * Implementation of the date ui definition.
 * 
 * @author mtaal
 */
public class DateUIDefinition extends UIDefinition {

  @Override
  public String getParentType() {
    return "date";
  }

  @Override
  public String getFormEditorType() {
    return "OBDateItem";
  }

  @Override
  public String getTypeProperties() {
    final StringBuilder sb = new StringBuilder();
    sb.append("shortDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.Date.JSToOB(value, OB.Format.date);" + "},"
        + "normalDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.Date.JSToOB(value, OB.Format.date);" + "},");
    return sb.toString();
  }
}
