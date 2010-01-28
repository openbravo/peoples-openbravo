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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.reference.Reference;

public class UITable extends UIReference {
  public UITable(String reference, String subreference) {
    super(reference, subreference);
  }

  public void generateSQL(TableSQLData table, Properties prop) throws Exception {
    table.addSelectField(table.getTableName() + "." + prop.getProperty("ColumnName"), prop
        .getProperty("ColumnName"));
    identifier(table, table.getTableName(), prop, prop.getProperty("ColumnName") + "_R", table
        .getTableName()
        + "." + prop.getProperty("ColumnName"), false);
  }

  protected void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null)
      return;

    String fieldName = field.getProperty("ColumnName");

    int myIndex = tableSql.index++;
    ComboTableQueryData trd[] = ComboTableQueryData
        .selectRefTable(tableSql.getPool(), subReference);
    if (trd == null || trd.length == 0)
      return;
    String tables = "(SELECT ";
    if (trd[0].isvaluedisplayed.equals("Y")) {
      tableSql.addSelectField("td" + myIndex + ".VALUE", identifierName);
      tables += "value, ";
    }
    tables += trd[0].keyname + " AS tableID, " + trd[0].name + " FROM ";
    Properties fieldsAux = UIReferenceUtility.fieldToProperties(trd[0]);
    tables += trd[0].tablename + ") td" + myIndex;
    tables += " on " + parentTableName + "." + fieldName + " = td" + myIndex + ".tableID";
    tableSql.addFromField(tables, "td" + myIndex, realName);

    UIReference linkedReference = Reference.getUIReference(
        fieldsAux.getProperty("AD_Reference_ID"), fieldsAux.getProperty("AD_Reference_Value_ID"));
    linkedReference.identifier(tableSql, "td" + myIndex, fieldsAux, identifierName, realName, true);
  }

  public String getGridType() {
    return "dynamicEnum";
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript, Vector<Object> vecKeys) throws IOException, ServletException {
    UITableDir tableDir = new UITableDir(reference, subReference);
    tableDir.generateFilterHtml(strHtml, vars, fields, strTab, strWindow, vecScript, null);
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    UITableDir tableDir = new UITableDir(reference, subReference);
    tableDir.generateFilterAcceptScript(field, params, paramsData);
  }
}
