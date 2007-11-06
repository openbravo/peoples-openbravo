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
      System.out.println("Field does not exist: " + fieldName);
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
      System.out.println("SQL error in query: " + strSql + "Exception:"+ ex2);
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
  /*
     public static String getSequence(ConnectionProvider connectionProvider, String table, String client)
     throws ServletException {
   String strSql = "";
   strSql = strSql + "";
   strSql = strSql + "      SELECT CURRENTNEXT, CURRENTNEXTSYS FROM AD_SEQUENCE WHERE NAME=? AND IsActive = 'Y' AND IsTableID = 'Y' AND IsAutoSequence = 'Y'";
   strSql = strSql + "    ";

   PreparedStatement st = connectionProvider.getPreparedStatement(strSql);
   ResultSet result;
   Vector vector = new Vector(0);
   String resultado = "", v_NextNoSys = "", v_NextNo = "";

   int iParameter = 0;
   try {
   iParameter++; UtilSql.setValue(st, iParameter, 12, "Test", table);
  //iParameter++; UtilSql.setValue(st, iParameter, 12, "Test", client);

  result = st.executeQuery();
  long countRecord = 0;
  while(result.next()) {
  countRecord++;
  v_NextNoSys = UtilSql.getValue(result, "CURRENTNEXTSYS");
  v_NextNo = UtilSql.getValue(result, "CURRENTNEXT");
  }
  result.close();
  } catch(SQLException e){
  System.out.println("Error of SQL in query: " + strSql + "Exception:"+ e);
  throw new ServletException(Integer.toString(e.getErrorCode()));
  } finally {
  connectionProvider.releasePreparedStatement(st);
  }

  if (v_NextNoSys!=null && !v_NextNoSys.equals("") && Integer.valueOf(v_NextNoSys).intValue() != -1 && Integer.valueOf(client).intValue() < 1000000) {
  if (updateSequence(connectionProvider, table, "CURRENTNEXTSYS")<=0) throw new ServletException("0 lines updated at AD_Sequence");
  resultado = v_NextNoSys;
  } else {
  if (updateSequence(connectionProvider, table, "CURRENTNEXT")<=0) throw new ServletException("0 lines updated at AD_Sequence");
  resultado = v_NextNo;
  }
  return(resultado);
     }

     public static int updateSequence(ConnectionProvider connectionProvider, String table, String column)
     throws ServletException {
   String strSql = "";
   strSql = strSql + "";
   strSql = strSql + "        UPDATE AD_SEQUENCE SET UPDATED=now(), " + column + "=" + column + " + IncrementNo ";
   strSql = strSql + "        WHERE NAME = ?";
   strSql = strSql + "      ";

   PreparedStatement st = connectionProvider.getPreparedStatement(strSql);
   int updateCount = 0;

   int iParameter = 0;
   try {
   iParameter++; UtilSql.setValue(st, iParameter, 12, null, table);

   updateCount = st.executeUpdate();
   } catch(SQLException e){
   System.out.println("Error of SQL in query: " + strSql + "Exception:"+ e);
   throw new ServletException(Integer.toString(e.getErrorCode()));
   } finally {
   connectionProvider.releasePreparedStatement(st);
   }
   return(updateCount);
   }*/

  /**Get the sequence for the specified table 
  */
  public static String getSequence(ConnectionProvider conn, String table, String client)
    throws ServletException {
    /*String strSql = "";
      strSql = strSql + "";
      strSql = strSql + "        CALL AD_Sequence_Next(?,?,?)";
      strSql = strSql + "      ";

      CallableStatement st = conn.getCallableStatement(strSql);
      String object;

      int iParameter = 0;*/
    String object;
    CSResponse response = SequenceData.getSequence(conn, table, client);
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
