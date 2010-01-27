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
import org.openbravo.reference.Reference;

public class UITableDir extends UIReference {
  public UITableDir(String reference, String subreference) {
    super(reference, subreference);
  }

  public void generateSQL(TableSQLData table, Properties prop) throws Exception {
    table.addSelectField(table.getTableName() + "." + prop.getProperty("ColumnName"), prop
        .getProperty("ColumnName"));
    identifier(table, table.getTableName(), prop, prop.getProperty("ColumnName") + "_R", table
        .getTableName()
        + "." + prop.getProperty("ColumnName"), false);
  }

  public void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    int myIndex = tableSql.index++;
    String name = field.getProperty("ColumnNameSearch");
    String tableDirName = name.substring(0, name.length() - 3);
    if (subReference != null && !subReference.equals("")) {
      TableSQLQueryData[] search = TableSQLQueryData.searchInfo(tableSql.getPool(), subReference);
      if (search != null && search.length != 0) {
        name = search[0].columnname;
        tableDirName = search[0].tablename;
      }
    } else {
      if (name.equalsIgnoreCase("CreatedBy") || name.equalsIgnoreCase("UpdatedBy")) {
        tableDirName = "AD_User";
        name = "AD_User_ID";
      }
    }
    ComboTableQueryData trd[] = ComboTableQueryData.identifierColumns(tableSql.getPool(),
        tableDirName);
    String tables = "(SELECT " + name;
    for (int i = 0; i < trd.length; i++) {
      // exclude tabledir pk-column as it has already been added in the line above
      if (!trd[i].name.equals(name)) {
        tables += ", " + trd[i].name;
      }
    }
    tables += " FROM ";
    tables += tableDirName + ") td" + myIndex;
    tables += " on " + parentTableName + "." + field.getProperty("ColumnName") + " = td" + myIndex
        + "." + name + "\n";
    tableSql.addFromField(tables, "td" + myIndex, realName);
    for (int i = 0; i < trd.length; i++) {
      Properties linkedRefProp = UIReferenceUtility.fieldToProperties(trd[i]);
      UIReference linkedReference = Reference.getUIReference(linkedRefProp
          .getProperty("AD_Reference_ID"), linkedRefProp.getProperty("AD_Reference_Value_ID"));
      linkedReference.identifier(tableSql, "td" + myIndex, linkedRefProp, identifierName, realName,
          false);
    }
  }

  public String getGridType() {
    return "dynamicEnum";
  }
}
