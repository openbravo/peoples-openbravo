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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.TableSQLData;

public class UIList extends UIReference {

  public UIList(String reference, String subreference) {
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

    // Check whether value must
    boolean showValue = false;
    boolean adminMode = OBContext.getOBContext().setInAdministratorMode(true);
    try {
      org.openbravo.model.ad.domain.Reference ref = OBDal.getInstance().get(
          org.openbravo.model.ad.domain.Reference.class, subReference);
      if (ref != null) {
        showValue = ref.isDisplayedValue();
      }
    } finally {
      OBContext.getOBContext().setInAdministratorMode(adminMode);
    }

    String fieldName = field.getProperty("ColumnName");
    int myIndex = tableSql.index++;

    StringBuffer name = new StringBuffer();
    // add inactive info
    name.append("((CASE td").append(myIndex).append(".isActive WHEN 'N' THEN '").append(
        TableSQLData.INACTIVE_DATA).append("' ELSE '' END)");
    // add value
    if (showValue) {
      name.append("|| td").append(myIndex).append(".value ||' - '");
    }
    // add name
    name.append("|| (CASE WHEN td_trl").append(myIndex).append(".name IS NULL THEN td").append(
        myIndex).append(".name ELSE td_trl").append(myIndex).append(".name END))");

    tableSql.addSelectField(name.toString(), identifierName);
    String tables = "(select IsActive, ad_ref_list_id, ad_reference_id, value, name from ad_ref_list) td"
        + myIndex;
    tables += " on ";
    if (fieldName.equalsIgnoreCase("DocAction")) {
      tables += "(CASE " + parentTableName + "." + fieldName + " WHEN '--' THEN 'CL' ELSE TO_CHAR("
          + parentTableName + "." + fieldName + ") END)";
    } else {
      tables += parentTableName + "." + fieldName;
    }
    tables += " = td" + myIndex + ".value AND td" + myIndex + ".ad_reference_id = ?";
    tableSql.addFromField(tables, "td" + myIndex, realName);
    tableSql.addFromParameter("TD" + myIndex + ".AD_REFERENCE_ID", "KEY", realName);
    tableSql.setParameter("TD" + myIndex + ".AD_REFERENCE_ID", subReference);
    tableSql
        .addFromField("(SELECT ad_language, name, ad_ref_list_id from ad_ref_list_trl) td_trl"
            + myIndex + " on td" + myIndex + ".ad_ref_list_id = td_trl" + myIndex
            + ".ad_ref_list_id AND td_trl" + myIndex + ".ad_language = ?", "td_trl" + myIndex,
            realName);
    tableSql.addFromParameter("#AD_LANGUAGE", "LANGUAGE", realName);
  }

  public String getGridType() {
    return "dynamicEnum";
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {
    UITableDir tableDir = new UITableDir(reference, subReference);
    tableDir.generateFilterHtml(strHtml, vars, fields, strTab, strWindow, vecScript, null);
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    UITableDir tableDir = new UITableDir(reference, subReference);
    tableDir.generateFilterAcceptScript(field, params, paramsData);
  }

}
