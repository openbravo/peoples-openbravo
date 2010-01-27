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
 * All portions are Copyright (C) 2010 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.util.Properties;

import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.TableSQLData;

/**
 * Utility methods used bu UIRefernce classes
 * 
 */
class UIReferenceUtility {

  /**
   * Checks whether there is trl for the table and creates the query if needed.
   * 
   */
  static public boolean checkTableTranslation(TableSQLData tableSql, String tableName,
      Properties field, String ref, String identifierName, String realName, boolean tableRef)
      throws Exception {
    if (tableName == null || tableName.equals("") || field == null)
      return false;

    ComboTableQueryData[] data = ComboTableQueryData.selectTranslatedColumn(tableSql.getPool(),
        field.getProperty("TableName"), field.getProperty("ColumnName"));
    if (data == null || data.length == 0)
      return false;
    int myIndex = tableSql.index++;
    tableSql.addSelectField("(CASE WHEN td_trl" + myIndex + "." + data[0].columnname
        + " IS NULL THEN "
        + formatField(ref, tableSql, (tableName + "." + field.getProperty("ColumnName")))
        + " ELSE " + formatField(ref, tableSql, ("td_trl" + myIndex + "." + data[0].columnname))
        + " END)", identifierName);

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
  static String formatField(String reference, TableSQLData tableSql, String field) {
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

  /**
   * Transform a fieldprovider into a Properties object.
   * 
   * @param field
   *          FieldProvider object.
   * @return Properties with the FieldProvider information.
   * @throws Exception
   */
  static public Properties fieldToProperties(FieldProvider field) throws Exception {
    Properties aux = new Properties();
    if (field != null) {
      aux.setProperty("ColumnName", field.getField("name"));
      aux.setProperty("TableName", field.getField("tablename"));
      aux.setProperty("AD_Reference_ID", field.getField("reference"));
      aux.setProperty("AD_Reference_Value_ID", field.getField("referencevalue"));
      aux.setProperty("IsMandatory", field.getField("required"));
      aux.setProperty("ColumnNameSearch", field.getField("columnname"));
    }
    return aux;
  }

}
