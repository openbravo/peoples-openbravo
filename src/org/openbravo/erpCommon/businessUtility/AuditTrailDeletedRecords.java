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
package org.openbravo.erpCommon.businessUtility;

import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ExecuteQuery;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Tab;

public class AuditTrailDeletedRecords {

  public static FieldProvider[] getDeletedRecords(ConnectionProvider conn, String tabId) {

    OBContext.enableAsAdminContext();
    StringBuffer sql = new StringBuffer();
    try {
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String tableName = tab.getTable().getDBTableName();
      sql.append("SELECT * FROM (\n");
      sql.append("SELECT \n");
      boolean firstItem = true;
      for (Column col : tab.getTable().getADColumnList()) {
        // obtain

        if (!firstItem) {
          sql.append(", ");
        } else {
          firstItem = false;
        }
        sql
            .append("(SELECT COALESCE(OLD_CHAR, TO_CHAR(OLD_NCHAR), TO_CHAR(OLD_NUMBER), TO_CHAR(OLD_DATE)) AS OLD\n");
        sql.append("  FROM AD_AUDIT_TRAIL\n");
        sql.append(" WHERE AD_TABLE_ID='").append(tab.getTable().getId()).append("'\n");
        sql.append("   AND AD_COLUMN_ID='").append(col.getId()).append("'\n");
        sql.append("   AND ACTION='D'\n");
        sql.append("   AND RECORD_ID = T.RECORD_ID\n");
        sql.append(" ) ").append(col.getDBColumnName()).append("\n");
      }

      sql.append(" FROM AD_AUDIT_TRAIL T\n");
      sql.append("WHERE ACTION='D'\n");
      sql.append("  AND AD_TABLE_ID = '").append(tab.getTable().getId()).append("'\n");
      sql.append("  AND AD_COLUMN_ID = '").append(
          ModelProvider.getInstance().getEntityByTableName(tableName).getIdProperties().get(0)
              .getColumnId()).append("'\n");
      sql.append(") ").append(tableName);

      // apply where clause if exists
      if (tab.getSQLWhereClause() != null) {
        sql.append(" where ").append(tab.getSQLWhereClause());
      }
      System.out.println(sql);

      ExecuteQuery q = new ExecuteQuery(conn, sql.toString(), null);
      return q.select();
    } catch (Exception e) {
      // TODO: handle exception

    } finally {
      OBContext.resetAsAdminContext();

    }
    return null;

  }
}
