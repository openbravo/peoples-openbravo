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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
//Sqlc generated VO.O11-2
// modified manually to return a String;

package org.openbravo.erpCommon.utility;

import java.sql.*;
import java.util.*;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.exception.*;
import org.openbravo.data.UtilSql;

public class SequenceIdData implements FieldProvider {
  public String dummy;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("dummy"))
      return dummy;
    else {
      return null;
    }
  }

  /**Select void
  */
  public static SequenceIdData[] select(ConnectionProvider connectionProvider)
    throws ServletException {
    String strSql = "";
    strSql = strSql + "";
    strSql = strSql + "      SELECT Dummy FROM DUAL";
    strSql = strSql + "    ";

    PreparedStatement st = null;
    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      result = st.executeQuery();
      long countRecord = 0;
      while(result.next()) {
        countRecord++;
        SequenceIdData objectSequenceIdData = new SequenceIdData();
        objectSequenceIdData.dummy = UtilSql.getValue(result, "DUMMY");
        vector.addElement(objectSequenceIdData);
      }
      result.close();
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      throw new ServletException("@CODE=@" + ex3.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignored) {}
    }
    SequenceIdData objectSequenceIdData[] = new SequenceIdData[vector.size()];
    vector.copyInto(objectSequenceIdData);
    return(objectSequenceIdData);
  }
  
  /**
   * Returns a new UUID
   * @return
   */
  public static String getUUID(){
	return UUID.randomUUID().toString().replace("-", "").toUpperCase();
  }
  
  /**Get the sequence for the specified table
   * this shouldn't be used anymore, use instead getUUID() 
  */
  public static String getSequence(ConnectionProvider conn, String table, String client) {
    return getUUID();
  }
  
  /**Get the sequence for the specified table 
   */
   public static String getSequenceConnection(Connection conn, ConnectionProvider con, String table, String client)
     throws ServletException {
     /*String strSql = "";
       strSql = strSql + "";
       strSql = strSql + "        CALL AD_Sequence_Next(?,?,?)";
       strSql = strSql + "      ";

       CallableStatement st = conn.getCallableStatement(strSql);
       String object;

       int iParameter = 0;*/
     String object;
     CSResponse response = SequenceData.getSequenceConnection(conn, con, table, client);
     object = response.razon;
     /*try {
       iParameter++; UtilSql.setValue(st, iParameter, 12, "Test", table);
       iParameter++; UtilSql.setValue(st, iParameter, 12, "Test", client);
       int iParametersequence = iParameter + 1;
       iParameter++; st.registerOutParameter(iParameter, 12);

       st.execute();
       object = UtilSql.getStringCallableStatement(st, iParametersequence);
       } catch(SQLException e){
       System.out.println("Error of SQL in query: getSequence Exception:"+ e);
       throw new ServletException(Integer.toString(e.getErrorCode()));
       } finally {
       conn.releasePreparedStatement(st);
       }*/
     return(object);
   }
}
