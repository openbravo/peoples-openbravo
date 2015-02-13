//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class Issue28591UpdatePSDData implements FieldProvider {
static Logger log4j = Logger.getLogger(Issue28591UpdatePSDData.class);
  private String InitRecordNumber="0";
  public String finPaymentScheduledetailId;
  public String outstandingamt;
  public String wrongamt;
  public String finPaymentScheduleId;
  public String cInvoiceId;
  public String cCurrencyId;
  public String bpCurrencyId;
  public String finPaymentId;
  public String finPaymentDetailId;
  public String isreceipt;
  public String cBpartnerId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("fin_payment_scheduledetail_id") || fieldName.equals("finPaymentScheduledetailId"))
      return finPaymentScheduledetailId;
    else if (fieldName.equalsIgnoreCase("outstandingamt"))
      return outstandingamt;
    else if (fieldName.equalsIgnoreCase("wrongamt"))
      return wrongamt;
    else if (fieldName.equalsIgnoreCase("fin_payment_schedule_id") || fieldName.equals("finPaymentScheduleId"))
      return finPaymentScheduleId;
    else if (fieldName.equalsIgnoreCase("c_invoice_id") || fieldName.equals("cInvoiceId"))
      return cInvoiceId;
    else if (fieldName.equalsIgnoreCase("c_currency_id") || fieldName.equals("cCurrencyId"))
      return cCurrencyId;
    else if (fieldName.equalsIgnoreCase("bp_currency_id") || fieldName.equals("bpCurrencyId"))
      return bpCurrencyId;
    else if (fieldName.equalsIgnoreCase("fin_payment_id") || fieldName.equals("finPaymentId"))
      return finPaymentId;
    else if (fieldName.equalsIgnoreCase("fin_payment_detail_id") || fieldName.equals("finPaymentDetailId"))
      return finPaymentDetailId;
    else if (fieldName.equalsIgnoreCase("isreceipt"))
      return isreceipt;
    else if (fieldName.equalsIgnoreCase("c_bpartner_id") || fieldName.equals("cBpartnerId"))
      return cBpartnerId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static Issue28591UpdatePSDData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static Issue28591UpdatePSDData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT '' as fin_payment_scheduledetail_id, '' as outstandingamt, '' as wrongamt, " +
      "        '' as fin_payment_schedule_id, '' as c_invoice_id, '' as c_currency_id, '' as bp_currency_id," +
      "        '' as fin_payment_id, '' as fin_payment_detail_id, '' as isreceipt, '' as c_bpartner_id" +
      "        FROM DUAL";

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
        Issue28591UpdatePSDData objectIssue28591UpdatePSDData = new Issue28591UpdatePSDData();
        objectIssue28591UpdatePSDData.finPaymentScheduledetailId = UtilSql.getValue(result, "fin_payment_scheduledetail_id");
        objectIssue28591UpdatePSDData.outstandingamt = UtilSql.getValue(result, "outstandingamt");
        objectIssue28591UpdatePSDData.wrongamt = UtilSql.getValue(result, "wrongamt");
        objectIssue28591UpdatePSDData.finPaymentScheduleId = UtilSql.getValue(result, "fin_payment_schedule_id");
        objectIssue28591UpdatePSDData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectIssue28591UpdatePSDData.cCurrencyId = UtilSql.getValue(result, "c_currency_id");
        objectIssue28591UpdatePSDData.bpCurrencyId = UtilSql.getValue(result, "bp_currency_id");
        objectIssue28591UpdatePSDData.finPaymentId = UtilSql.getValue(result, "fin_payment_id");
        objectIssue28591UpdatePSDData.finPaymentDetailId = UtilSql.getValue(result, "fin_payment_detail_id");
        objectIssue28591UpdatePSDData.isreceipt = UtilSql.getValue(result, "isreceipt");
        objectIssue28591UpdatePSDData.cBpartnerId = UtilSql.getValue(result, "c_bpartner_id");
        objectIssue28591UpdatePSDData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectIssue28591UpdatePSDData);
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
    Issue28591UpdatePSDData objectIssue28591UpdatePSDData[] = new Issue28591UpdatePSDData[vector.size()];
    vector.copyInto(objectIssue28591UpdatePSDData);
    return(objectIssue28591UpdatePSDData);
  }

  public static Issue28591UpdatePSDData[] selectPSD(ConnectionProvider connectionProvider)    throws ServletException {
    return selectPSD(connectionProvider, 0, 0);
  }

  public static Issue28591UpdatePSDData[] selectPSD(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      select ps.outstandingamt as outstandingamt, max(psd.fin_payment_scheduledetail_id) as fin_payment_scheduledetail_id" +
      "      from fin_payment_scheduledetail psd" +
      "      LEFT JOIN fin_payment_schedule ps ON ps.fin_payment_schedule_id = COALESCE(psd.fin_payment_schedule_invoice,psd.fin_payment_schedule_order)" +
      "      where psd.fin_payment_detail_id is null and ps.outstandingamt > 0" +
      "      group by ps.outstandingamt, ps.fin_payment_schedule_id" +
      "      having sum(psd.amount) <> ps.outstandingamt";

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
        Issue28591UpdatePSDData objectIssue28591UpdatePSDData = new Issue28591UpdatePSDData();
        objectIssue28591UpdatePSDData.outstandingamt = UtilSql.getValue(result, "outstandingamt");
        objectIssue28591UpdatePSDData.finPaymentScheduledetailId = UtilSql.getValue(result, "fin_payment_scheduledetail_id");
        objectIssue28591UpdatePSDData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectIssue28591UpdatePSDData);
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
    Issue28591UpdatePSDData objectIssue28591UpdatePSDData[] = new Issue28591UpdatePSDData[vector.size()];
    vector.copyInto(objectIssue28591UpdatePSDData);
    return(objectIssue28591UpdatePSDData);
  }

  public static int updatePSDAmount(ConnectionProvider connectionProvider, String outStandingAmount, String finPaymentScheduledetailId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        UPDATE FIN_PAYMENT_SCHEDULEDETAIL SET AMOUNT=TO_NUMBER(?)," +
      "        updatedby='0', updated=now()" +
      "        WHERE FIN_PAYMENT_SCHEDULEDETAIL_ID = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, outStandingAmount);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, finPaymentScheduledetailId);

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

  public static boolean updateWrongPSD(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT count(*) as exist" +
      "        FROM DUAL" +
      "        WHERE EXISTS (SELECT 1 FROM ad_preference" +
      "                      WHERE attribute = 'Issue28591updateWrongPSD')";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "exist").equals("0");
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

  public static int createPreference(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        INSERT INTO ad_preference (" +
      "          ad_preference_id, ad_client_id, ad_org_id, isactive," +
      "          createdby, created, updatedby, updated," +
      "          attribute" +
      "        ) VALUES (" +
      "          get_uuid(), '0', '0', 'Y'," +
      "          '0', NOW(), '0', NOW()," +
      "          'Issue28591updateWrongPSD'" +
      "        )";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

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
}
