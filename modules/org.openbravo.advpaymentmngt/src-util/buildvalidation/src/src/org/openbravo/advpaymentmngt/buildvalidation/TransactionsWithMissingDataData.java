//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.buildvalidation;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class TransactionsWithMissingDataData implements FieldProvider {
static Logger log4j = Logger.getLogger(TransactionsWithMissingDataData.class);
  private String InitRecordNumber="0";
  public String documentno;
  public String finFinaccTransactionId;
  public String adClientId;
  public String adOrgId;
  public String adRoleId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("documentno"))
      return documentno;
    else if (fieldName.equalsIgnoreCase("fin_finacc_transaction_id") || fieldName.equals("finFinaccTransactionId"))
      return finFinaccTransactionId;
    else if (fieldName.equalsIgnoreCase("ad_client_id") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("ad_org_id") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("ad_role_id") || fieldName.equals("adRoleId"))
      return adRoleId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static TransactionsWithMissingDataData[] dummy(ConnectionProvider connectionProvider)    throws ServletException {
    return dummy(connectionProvider, 0, 0);
  }

  public static TransactionsWithMissingDataData[] dummy(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT '' AS documentno, '' AS fin_finacc_transaction_id, '' AS ad_client_id," +
      "             '' AS ad_org_id, '' AS ad_role_id" +
      "      FROM DUAL";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
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
        TransactionsWithMissingDataData objectTransactionsWithMissingDataData = new TransactionsWithMissingDataData();
        objectTransactionsWithMissingDataData.documentno = UtilSql.getValue(result, "documentno");
        objectTransactionsWithMissingDataData.finFinaccTransactionId = UtilSql.getValue(result, "fin_finacc_transaction_id");
        objectTransactionsWithMissingDataData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectTransactionsWithMissingDataData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectTransactionsWithMissingDataData.adRoleId = UtilSql.getValue(result, "ad_role_id");
        objectTransactionsWithMissingDataData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectTransactionsWithMissingDataData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    TransactionsWithMissingDataData objectTransactionsWithMissingDataData[] = new TransactionsWithMissingDataData[vector.size()];
    vector.copyInto(objectTransactionsWithMissingDataData);
    return(objectTransactionsWithMissingDataData);
  }

/**
This query returns transactions with deposit and payment amount equal to Zero and/or Transaction Date null and/or Accounting Date null
 */
  public static TransactionsWithMissingDataData[] selectTransactionsWithMissingData(ConnectionProvider connectionProvider)    throws ServletException {
    return selectTransactionsWithMissingData(connectionProvider, 0, 0);
  }

/**
This query returns transactions with deposit and payment amount equal to Zero and/or Transaction Date null and/or Accounting Date null
 */
  public static TransactionsWithMissingDataData[] selectTransactionsWithMissingData(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT ad_column_identifier('fin_finacc_transaction_id', fin_finacc_transaction_id, 'en_US') as documentno, fin_finacc_transaction_id, ad_client_id, ad_org_id " +
      "      FROM fin_finacc_transaction" +
      "      WHERE (depositamt = 0 AND paymentamt = 0)" +
      "      OR statementdate IS NULL " +
      "      OR dateacct IS NULL";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
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
        TransactionsWithMissingDataData objectTransactionsWithMissingDataData = new TransactionsWithMissingDataData();
        objectTransactionsWithMissingDataData.documentno = UtilSql.getValue(result, "documentno");
        objectTransactionsWithMissingDataData.finFinaccTransactionId = UtilSql.getValue(result, "fin_finacc_transaction_id");
        objectTransactionsWithMissingDataData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectTransactionsWithMissingDataData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectTransactionsWithMissingDataData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectTransactionsWithMissingDataData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    TransactionsWithMissingDataData objectTransactionsWithMissingDataData[] = new TransactionsWithMissingDataData[vector.size()];
    vector.copyInto(objectTransactionsWithMissingDataData);
    return(objectTransactionsWithMissingDataData);
  }

  public static String getAlertRuleId(ConnectionProvider connectionProvider, String name, String client)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT MAX(ad_alertrule_id) AS name" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME = ?" +
      "         AND ISACTIVE = 'Y'" +
      "         AND AD_CLIENT_ID = ?";

    ResultSet result;
    String strReturn = null;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, name);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);

      result = st.executeQuery();
      if(result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(strReturn);
  }

  public static boolean existsAlert(ConnectionProvider connectionProvider, String alertRule, String payment)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERT" +
      "       WHERE AD_ALERTRULE_ID = ?" +
      "       AND REFERENCEKEY_ID = ?" +
      "       AND ISFIXED = 'N'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, payment);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static boolean existsAlertRule(ConnectionProvider connectionProvider, String alertRule, String client)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT COUNT(*) AS EXISTING" +
      "       FROM AD_ALERTRULE" +
      "       WHERE NAME = ?" +
      "         AND ISACTIVE = 'Y'" +
      "         AND AD_CLIENT_ID = ?";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, alertRule);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static int insertAlertRule(ConnectionProvider connectionProvider, String clientId, String orgId, String name, String tabId, String sql)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_ALERTRULE (" +
      "        AD_ALERTRULE_ID, AD_CLIENT_ID, AD_ORG_ID,ISACTIVE," +
      "        CREATED, CREATEDBY,  UPDATED, UPDATEDBY," +
      "        NAME, AD_TAB_ID, FILTERCLAUSE, TYPE," +
      "        SQL" +
      "      ) VALUES (" +
      "        get_uuid(), ?, ?, 'Y'," +
      "        now(), '100', now(), '100'," +
      "        ?, ?, '', 'D'," +
      "        ?" +
      "      )";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, orgId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, name);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tabId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, sql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int insertAlert(ConnectionProvider connectionProvider, String client, String description, String adAlertRuleId, String recordId, String referencekey_id)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO AD_Alert (" +
      "        AD_Alert_ID, AD_Client_ID, AD_Org_ID, IsActive," +
      "        Created, CreatedBy, Updated, UpdatedBy," +
      "        Description, AD_AlertRule_ID, Record_Id, Referencekey_ID" +
      "      ) VALUES (" +
      "        get_uuid(), ?, '0', 'Y'," +
      "        NOW(), '0', NOW(), '0'," +
      "        ?, ?, ?, ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, description);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adAlertRuleId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, recordId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, referencekey_id);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static TransactionsWithMissingDataData[] getRoleId(ConnectionProvider connectionProvider, String window, String clientId)    throws ServletException {
    return getRoleId(connectionProvider, window, clientId, 0, 0);
  }

  public static TransactionsWithMissingDataData[] getRoleId(ConnectionProvider connectionProvider, String window, String clientId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT distinct r.ad_role_id" +
      "       FROM ad_window_access wa join ad_role r on (wa.ad_role_id=r.ad_role_id)" +
      "       WHERE wa.ad_window_id = ?" +
      "             AND wa.ad_client_id = ?" +
      "             AND wa.isactive = 'Y'" +
      "             AND r.ismanual = 'Y'";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, window);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);

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
        TransactionsWithMissingDataData objectTransactionsWithMissingDataData = new TransactionsWithMissingDataData();
        objectTransactionsWithMissingDataData.adRoleId = UtilSql.getValue(result, "ad_role_id");
        objectTransactionsWithMissingDataData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectTransactionsWithMissingDataData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    TransactionsWithMissingDataData objectTransactionsWithMissingDataData[] = new TransactionsWithMissingDataData[vector.size()];
    vector.copyInto(objectTransactionsWithMissingDataData);
    return(objectTransactionsWithMissingDataData);
  }

  public static int insertAlertRecipient(ConnectionProvider connectionProvider, String client, String org, String adAlertRuleId, String role)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "    INSERT INTO ad_alertrecipient(" +
      "            ad_user_id, ad_client_id, ad_org_id, isactive, created, createdby, " +
      "            updated, updatedby, ad_alertrecipient_id, ad_alertrule_id, ad_role_id, " +
      "            sendemail)" +
      "    VALUES (null, ?, ?, 'Y', now(), '100', " +
      "            now(), '100', get_uuid(), ?, ?, " +
      "            'N')";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, client);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, org);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adAlertRuleId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, role);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Check if the FIN_Finacc_Transaction table exist
 */
  public static boolean existAPRMbasetables(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "       SELECT count(*) AS EXISTING" +
      "       FROM ad_table" +
      "       WHERE ad_table_id = '4D8C3B3C31D1410DA046140C9F024D17'";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "existing").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }
}
