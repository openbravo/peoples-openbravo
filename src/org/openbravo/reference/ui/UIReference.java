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
 * All portions are Copyright (C) 2009-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Base implementation for UI objects
 * 
 */
public class UIReference {

  protected String reference;
  protected String subReference;
  // addSecondaryFilter is used to add a "to" filter in the standard getFilter method
  protected boolean addSecondaryFilter;
  protected ConnectionProvider conn;
  protected boolean numeric;

  public UIReference(String reference, String subreference) {
    this.reference = reference;
    this.subReference = subreference;
    this.addSecondaryFilter = false;
    this.conn = new DalConnectionProvider();
    this.numeric = false;
  }

  /**
   * Generates the sql needed for TableSQLData class
   */
  public void generateSQL(TableSQLData table, Properties field) throws Exception {
    identifier(table, table.getTableName(), field, field.getProperty("ColumnName"),
        table.getTableName() + "." + field.getProperty("ColumnName"), false);
  }

  /**
   * Helper method called from generateSQL to create the SQL for the identifier
   */
  protected void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    if (!UIReferenceUtility.checkTableTranslation(tableSql, parentTableName, field, reference,
        identifierName, realName, tableRef)) {
      tableSql.addSelectField(UIReferenceUtility.formatField(tableSql.getVars(), reference,
          (parentTableName + "." + field.getProperty("ColumnName"))), identifierName);
    }
  }

  /**
   * Obtains the type of data to be shown in the grid mode
   * 
   */
  public String getGridType() {
    return "string";
  }

  /**
   * Obtains filter for TableSQLData
   */
  public void getFilter(SQLReturnObject result, boolean isNewFilter, VariablesSecureApp vars,
      TableSQLData tableSQL, Vector<String> filter, Vector<String> filterParams, Properties prop)
      throws Exception {
    String aux;
    if (isNewFilter) {
      aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName"),
          tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    } else {
      aux = vars.getSessionValue(tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    }
    // The filter is not applied if the parameter value is null or
    // parameter value is '%' for string references.
    if (!aux.equals("")) {
      UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL,
          prop.getProperty("ColumnName"), prop.getProperty("ColumnName"), reference, true, aux);
    }
    if (addSecondaryFilter) {
      if (isNewFilter) {
        aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName") + "_f",
            tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName") + "_f");
      } else {
        aux = vars.getSessionValue(tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName")
            + "_f");
      }
      if (!aux.equals("")) {
        UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL,
            prop.getProperty("ColumnName"), prop.getProperty("ColumnName") + "_f", reference,
            false, aux);
      }
    }
  }

  /**
   * This method is called to show the value in the grid, it is intended to format the value
   * properly
   * 
   * @param vars
   */
  public String formatGridValue(VariablesSecureApp vars, String value) {
    return StringEscapeUtils.escapeHtml(value);
  }

  public boolean isNumeric() {
    return numeric;
  }

  public void setComboTableDataIdentifier(ComboTableData comboTableData, String tableName,
      FieldProvider field) throws Exception {
    if (!UIReferenceUtility.checkTableTranslation(comboTableData, tableName, field, reference)) {
      comboTableData.addSelectField(UIReferenceUtility.formatField(comboTableData.getVars(),
          reference,
          (((tableName != null && tableName.length() != 0) ? (tableName + ".") : "") + field
              .getField("name"))), "NAME");
    }
  }

  /**
   * Indicates whether this reference is a cacheable combo Basically, this indicates whether the
   * ComboTableData instances related to this class will be // cached and reused by the
   * FormInitializationComponent or not. // For them to be cached, it's very important that the
   * Combo values themselves only depend on the // parameter values the combo uses; that is, that
   * they do not depend on things like session // variables, ...
   */
  public boolean canBeCached() {
    return false;
  }

}
