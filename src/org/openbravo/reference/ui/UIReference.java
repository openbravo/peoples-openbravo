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
import java.util.Vector;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;

/**
 * Base implementation for UI objects
 * 
 */
public class UIReference {

  protected String reference;
  protected String subReference;
  // addSecondaryFilter is used to add a "to" filter in the standard getFilter method
  protected boolean addSecondaryFilter;

  public UIReference(String reference, String subreference) {
    this.reference = reference;
    this.subReference = subreference;
    this.addSecondaryFilter = false;
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

  /**
   * Obtains filter for TableSQLData
   */
  public void getFilter(SQLReturnObject result, boolean isNewFilter, VariablesSecureApp vars,
      TableSQLData tableSQL, Vector<String> filter, Vector<String> filterParams, Properties prop)
      throws Exception {
    String aux;
    if (isNewFilter) {
      aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName"), tableSQL
          .getTabID()
          + "|param" + prop.getProperty("ColumnName"));
    } else {
      aux = vars.getSessionValue(tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName"));
    }
    // The filter is not applied if the parameter value is null or
    // parameter value is '%' for string references.
    if (!aux.equals("")) {
      UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL, prop
          .getProperty("ColumnName"), reference, true, aux);
    }
    if (addSecondaryFilter) {
      aux = vars.getRequestGlobalVariable("inpParam" + prop.getProperty("ColumnName") + "_f",
          tableSQL.getTabID() + "|param" + prop.getProperty("ColumnName") + "_f");
      if (!aux.equals("")) {
        UIReferenceUtility.addFilter(filter, filterParams, result, tableSQL, prop
            .getProperty("ColumnName")
            + "_f", reference, false, aux);
      }
    }
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
