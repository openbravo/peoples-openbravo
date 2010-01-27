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
import java.util.Vector;

import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.SQLReturnObject;
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

  /**
   * Formats the filter to get the correct output (adds TO_DATE, TO_NUMBER...).
   * 
   * @param tablename
   *          String with the table name.
   * @param columnname
   *          String with the column name.
   * @param reference
   *          String with the reference id.
   * @param first
   *          Boolean to know if is the first or not.
   * @return String with the formated field.
   */
  static String formatFilter(String tablename, String columnname, String reference, boolean first) {
    if (columnname == null || columnname.equals("") || tablename == null || tablename.equals("")
        || reference == null || reference.equals(""))
      return "";
    StringBuffer text = new StringBuffer();
    if (reference.equals("15") || reference.equals("16") || reference.equals("24")) {
      text.append("TO_DATE(").append(reference.equals("24") ? "TO_CHAR(" : "").append(tablename)
          .append(".").append(columnname).append(
              (reference.equals("24") ? ", 'HH24:MI:SS'), 'HH24:MI:SS'" : "")).append(") ");
      if (first)
        text.append(">= ");
      else {
        if (reference.equals("24"))
          text.append("<=");
        else
          text.append("< ");
      }
      text.append("TO_DATE(?").append((reference.equals("24") ? ", 'HH24:MI:SS'" : "")).append(")");
      if (!first && !reference.equals("24"))
        text.append("+1");
    } else if (reference.equals("11") || reference.equals("12") || reference.equals("13")
        || reference.equals("22") || reference.equals("29") || reference.equals("800008")
        || reference.equals("800019")) {
      text.append(tablename).append(".").append(columnname).append(" ");
      if (first)
        text.append(">= ");
      else
        text.append("<= ");
      text.append("TO_NUMBER(?)");
    } else if (reference.equals("10") || reference.equals("14") || reference.equals("34")) {
      String aux = "";
      if (!columnname.equalsIgnoreCase("Value") && !columnname.equalsIgnoreCase("DocumentNo"))
        aux = "C_IGNORE_ACCENT";
      text.append(aux).append("(");
      text.append(tablename).append(".").append(columnname).append(") LIKE ");
      text.append(aux).append("(?)");
    } else if (reference.equals("35")) {
      text
          .append(
              "(SELECT UPPER(DESCRIPTION) FROM M_ATTRIBUTESETINSTANCE WHERE M_ATTRIBUTESETINSTANCE.M_ATTRIBUTESETINSTANCE_ID = ")
          .append(tablename).append(".").append(columnname).append(") LIKE C_IGNORE_ACCENT(?)");
    } else {
      text.append(tablename).append(".").append(columnname).append(" = ?");
    }
    return text.toString();
  }

  public static void addFilter(Vector<String> filter, Vector<String> filterParams,
      SQLReturnObject result, TableSQLData tableSQL, String columnName, String reference,
      boolean first, String aux) {
    filter.addElement(UIReferenceUtility.formatFilter(tableSQL.getTableName(), columnName,
        reference, first));
    filterParams.addElement("Param" + columnName);
    result.setData("Param" + columnName, aux);

  }
}
