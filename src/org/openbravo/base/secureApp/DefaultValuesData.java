/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base.secureApp;

import java.sql.*;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.*;
import org.openbravo.data.UtilSql;

/**Clase SqlStandardData
 */
public class DefaultValuesData implements FieldProvider {
  public String columnname;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("columnname"))
      return columnname;
    else {      
      return null;
    }
  }

/**Select for relation
 */
  public static String select(ConnectionProvider connectionProvider, String param1, String param2, String param3, String param4) throws ServletException {
    String strSql = "SELECT " + param1 + " AS COLUMNNAME";
    strSql = strSql + " FROM " + param2 + " ";
    strSql = strSql + " WHERE isActive = 'Y' ";
    strSql = strSql + " AND isDefault = 'Y' ";
    strSql = strSql + " AND AD_Client_ID IN (" + param3 + ") ";
    strSql = strSql + " AND AD_Org_ID IN (" + param4 + ") ";
    strSql = strSql + " ORDER BY AD_Client_ID";

    Statement st = null;
    ResultSet result;
    String resultado="";

    try {
      st = connectionProvider.getStatement();
      result = st.executeQuery(strSql);

      if (result.next()) {
        resultado = UtilSql.getValue(result, "COLUMNNAME");
      }
      result.close();
    } catch(SQLException e){
      System.out.println("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(NoConnectionAvailableException ec){
      System.out.println("Connection error in query: " + strSql + "Exception:"+ ec);
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch(PoolNotFoundException ep){
      System.out.println("Pool error in query: " + strSql + "Exception:"+ ep);
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch(Exception ex){
      System.out.println("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(resultado);
  }
}
