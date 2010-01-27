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
 * All portions are Copyright (C) 2009-2010 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.util.Properties;

import org.openbravo.erpCommon.utility.TableSQLData;

/**
 * Base implementation for UI objects
 * 
 */
public class UIReference {

  protected String reference;
  protected String subReference;

  public UIReference(String reference, String subreference) {
    this.reference = reference;
    this.subReference = subreference;
  }

  /**
   * Generates the sql needed for TableSQLData class
   */
  public void generateSQL(TableSQLData table, Properties field) throws Exception {
    identifier(table, table.getTableName(), field, field.getProperty("ColumnName"), table
        .getTableName()
        + "." + field.getProperty("ColumnName"), false);
  }

  /**
   * Obtains the type of data to be shown in the grid mode
   * 
   */
  public String getGridType() {
    return "string";
  }

  /**
   * Includes the needed casting (TO_DATE...) to compose SQL
   */
  public String addSQLCasting(String column) {
    return column;
  }

  protected void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {

    if (field == null)
      return;

    if (!UIReferenceUtility.checkTableTranslation(tableSql, parentTableName, field, reference,
        identifierName, realName, tableRef)) {
      tableSql.addSelectField(UIReferenceUtility.formatField(reference, tableSql, (parentTableName
          + "." + field.getProperty("ColumnName"))), identifierName);
    }
  }

}
