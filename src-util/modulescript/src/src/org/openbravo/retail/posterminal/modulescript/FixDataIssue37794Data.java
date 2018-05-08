//Sqlc generated V1.O00-1
package org.openbravo.retail.posterminal.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.SessionInfo;
import java.util.*;

class FixDataIssue37794Data implements FieldProvider {
static Logger log4j = Logger.getLogger(FixDataIssue37794Data.class);
  private String InitRecordNumber="0";
  public String counter;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("counter"))
      return counter;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static FixDataIssue37794Data[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static FixDataIssue37794Data[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT '' AS counter" +
      "      FROM DUAL";

    ResultSet result;
    Vector<FixDataIssue37794Data> vector = new Vector<FixDataIssue37794Data>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        FixDataIssue37794Data objectFixDataIssue37794Data = new FixDataIssue37794Data();
        objectFixDataIssue37794Data.counter = UtilSql.getValue(result, "counter");
        objectFixDataIssue37794Data.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectFixDataIssue37794Data);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      if (log4j.isDebugEnabled()) {
        log4j.error("SQL error in query: " + strSql, e);
      } else {
        log4j.error("SQL error in query: " + strSql + " :" + e);
      }
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      if (log4j.isDebugEnabled()) {
        log4j.error("Exception in query: " + strSql, ex);
      } else {
        log4j.error("Exception in query: " + strSql + " :" + ex);
      }
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception e){
        log4j.error("Error during release*Statement of query: " + strSql, e);
      }
    }
    FixDataIssue37794Data objectFixDataIssue37794Data[] = new FixDataIssue37794Data[vector.size()];
    vector.copyInto(objectFixDataIssue37794Data);
    return(objectFixDataIssue37794Data);
  }

  public static int deleteDuplicatedOrganizationUserPosTerminal(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      DELETE FROM obpos_userterminal_access " +
      "      WHERE obpos_userterminal_access_id in " +
      "            (SELECT a.obpos_userterminal_access_id " +
      "             FROM obpos_userterminal_access a LEFT JOIN obpos_userterminal_access b " +
      "             ON a.ad_org_id = b.ad_org_id " +
      "             AND a.ad_user_id = b.ad_user_id " +
      "             AND a.obpos_applications_id = b.obpos_applications_id " +
      "             WHERE a.obpos_userterminal_access_id < b.obpos_userterminal_access_id)";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      SessionInfo.saveContextInfoIntoDB(conn);
      updateCount = st.executeUpdate();
    } catch(SQLException e){
      if (log4j.isDebugEnabled()) {
        log4j.error("SQL error in query: " + strSql, e);
      } else {
        log4j.error("SQL error in query: " + strSql + " :" + e);
      }
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      if (log4j.isDebugEnabled()) {
        log4j.error("Exception in query: " + strSql, ex);
      } else {
        log4j.error("Exception in query: " + strSql + " :" + ex);
      }
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception e){
        log4j.error("Error during release*Statement of query: " + strSql, e);
      }
    }
    return(updateCount);
  }
}
