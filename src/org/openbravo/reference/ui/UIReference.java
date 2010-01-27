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

    if (!checkTableTranslation(tableSql, parentTableName, field, reference, identifierName,
        realName, tableRef)) {
      tableSql.addSelectField(formatField(tableSql, (parentTableName + "." + field
          .getProperty("ColumnName"))), identifierName);
    }
  }

  private boolean checkTableTranslation(TableSQLData tableSql, String tableName, Properties field,
      String ref, String identifierName, String realName, boolean tableRef) throws Exception {
    if (tableName == null || tableName.equals("") || field == null)
      return false;

    ComboTableQueryData[] data = ComboTableQueryData.selectTranslatedColumn(tableSql.getPool(),
        field.getProperty("TableName"), field.getProperty("ColumnName"));
    if (data == null || data.length == 0)
      return false;
    int myIndex = tableSql.index++;
    tableSql.addSelectField("(CASE WHEN td_trl" + myIndex + "." + data[0].columnname
        + " IS NULL THEN "
        + formatField(tableSql, (tableName + "." + field.getProperty("ColumnName"))) + " ELSE "
        + formatField(tableSql, ("td_trl" + myIndex + "." + data[0].columnname)) + " END)",
        identifierName);

    String columnName;
    if (tableRef) {
      columnName = "tableID";
    } else {
      columnName = data[0].reference;
    }
    tableSql.addFromField("(SELECT AD_Language, " + data[0].reference + ", " + data[0].columnname
        + " FROM " + data[0].tablename + ") td_trl" + myIndex + " on " + tableName + "."
        + columnName + " = td_trl" + myIndex + "." + data[0].reference + " AND td_trl" + myIndex
        + ".AD_Language = ?", "td_trl" + myIndex, realName);
    tableSql.addFromParameter("#AD_LANGUAGE", "LANGUAGE", realName);
    return true;
  }

  /**
   * Formats the fields to get a correct output.
   * 
   * @param field
   *          String with the field.
   * @return String with the formated field.
   */
  private String formatField(TableSQLData tableSql, String field) {
    String result = "";
    if (field == null)
      return "";
    else if (reference == null || reference.length() == 0)
      return field;

    if (reference.equals("11")) {
      // INTEGER
      result = "CAST(" + field + " AS INTEGER)";
    } else if (reference.equals("12")/* AMOUNT */
        || reference.equals("22") /* NUMBER */
        || reference.equals("23") /* ROWID */
        || reference.equals("29") /* QUANTITY */
        || reference.equals("800008") /* PRICE */
        || reference.equals("800019")/* GENERAL QUANTITY */) {
      result = "TO_NUMBER(" + field + ")";
    } else if (reference.equals("15")) {
      // DATE
      result = "TO_CHAR("
          + field
          + (tableSql.getVars() == null ? "" : (", '"
              + tableSql.getVars().getSessionValue("#AD_SqlDateFormat") + "'")) + ")";
    } else if (reference.equals("16")) {
      // DATE-TIME
      result = "TO_CHAR("
          + field
          + (tableSql.getVars() == null ? "" : (", '"
              + tableSql.getVars().getSessionValue("#AD_SqlDateTimeFormat") + "'")) + ")";
    } else if (reference.equals("24")) {
      // TIME
      result = "TO_CHAR(" + field + ", 'HH24:MI:SS')";
    } else if (reference.equals("20")) {
      // YESNO
      result = "COALESCE(" + field + ", 'N')";
    } else if (reference.equals("23")) {
      // Binary
      result = field;
    } else {
      result = "COALESCE(TO_CHAR(" + field + "),'')";
    }

    return result;
  }

}
