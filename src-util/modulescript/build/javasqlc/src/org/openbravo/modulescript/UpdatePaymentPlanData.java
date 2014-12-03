//Sqlc generated V1.O00-1
package org.openbravo.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class UpdatePaymentPlanData implements FieldProvider {
static Logger log4j = Logger.getLogger(UpdatePaymentPlanData.class);
  private String InitRecordNumber="0";
  public String finpaymentscheduleid;
  public String paidamt;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("FINPAYMENTSCHEDULEID"))
      return finpaymentscheduleid;
    else if (fieldName.equalsIgnoreCase("PAIDAMT"))
      return paidamt;
    else if (fieldName.equals("rownum"))
      return rownum;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static UpdatePaymentPlanData[] dummy(ConnectionProvider connectionProvider)    throws ServletException {
    return dummy(connectionProvider, 0, 0);
  }

  public static UpdatePaymentPlanData[] dummy(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT '' AS finpaymentscheduleid, '' AS paidamt FROM DUAL";

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
        UpdatePaymentPlanData objectUpdatePaymentPlanData = new UpdatePaymentPlanData();
        objectUpdatePaymentPlanData.finpaymentscheduleid = UtilSql.getValue(result, "FINPAYMENTSCHEDULEID");
        objectUpdatePaymentPlanData.paidamt = UtilSql.getValue(result, "PAIDAMT");
        objectUpdatePaymentPlanData.rownum = Long.toString(countRecord);
        objectUpdatePaymentPlanData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUpdatePaymentPlanData);
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
    UpdatePaymentPlanData objectUpdatePaymentPlanData[] = new UpdatePaymentPlanData[vector.size()];
    vector.copyInto(objectUpdatePaymentPlanData);
    return(objectUpdatePaymentPlanData);
  }

  public static String selectAmount(ConnectionProvider connectionProvider, String finpaymentscheduleid)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT SUM(psd.amount) AS paidamt" +
      "      FROM fin_payment p" +
      "      LEFT JOIN fin_financial_account fa ON p.fin_financial_account_id = fa.fin_financial_account_id, fin_payment_detail pd" +
      "      JOIN fin_payment_scheduledetail psd ON pd.fin_payment_detail_id = psd.fin_payment_detail_id" +
      "      LEFT JOIN fin_payment_schedule psi ON psd.fin_payment_schedule_invoice = psi.fin_payment_schedule_id" +
      "      LEFT JOIN c_invoice i ON psi.c_invoice_id = i.c_invoice_id" +
      "      LEFT JOIN fin_payment_schedule pso ON psd.fin_payment_schedule_order = pso.fin_payment_schedule_id" +
      "      LEFT JOIN c_order o ON pso.c_order_id = o.c_order_id" +
      "      LEFT JOIN c_glitem gli ON pd.c_glitem_id = gli.c_glitem_id" +
      "      WHERE p.fin_payment_id = pd.fin_payment_id" +
      "      AND pso.fin_payment_schedule_id = ?";

    ResultSet result;
    String strReturn = null;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, finpaymentscheduleid);

      result = st.executeQuery();
      if(result.next()) {
        strReturn = UtilSql.getValue(result, "PAIDAMT");
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

  public static UpdatePaymentPlanData[] select(ConnectionProvider connectionProvider)    throws ServletException {
    return select(connectionProvider, 0, 0);
  }

  public static UpdatePaymentPlanData[] select(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      SELECT fin.fin_payment_schedule_id AS finpaymentscheduleid" +
      "      FROM fin_payment_schedule fin" +
      "      WHERE paidamt <> (SELECT SUM(psd.amount)" +
      "                           FROM fin_payment p" +
      "                           LEFT JOIN fin_financial_account fa ON p.fin_financial_account_id = fa.fin_financial_account_id, fin_payment_detail pd" +
      "                           JOIN fin_payment_scheduledetail psd ON pd.fin_payment_detail_id = psd.fin_payment_detail_id" +
      "                           LEFT JOIN fin_payment_schedule psi ON psd.fin_payment_schedule_invoice = psi.fin_payment_schedule_id" +
      "                           LEFT JOIN c_invoice i ON psi.c_invoice_id = i.c_invoice_id" +
      "                           LEFT JOIN fin_payment_schedule pso ON psd.fin_payment_schedule_order = pso.fin_payment_schedule_id" +
      "                           LEFT JOIN c_order o ON pso.c_order_id = o.c_order_id" +
      "                           LEFT JOIN c_glitem gli ON pd.c_glitem_id = gli.c_glitem_id" +
      "                          WHERE p.fin_payment_id = pd.fin_payment_id" +
      "                          and pso.fin_payment_schedule_id = fin.fin_payment_schedule_id)";

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
        UpdatePaymentPlanData objectUpdatePaymentPlanData = new UpdatePaymentPlanData();
        objectUpdatePaymentPlanData.finpaymentscheduleid = UtilSql.getValue(result, "FINPAYMENTSCHEDULEID");
        objectUpdatePaymentPlanData.rownum = Long.toString(countRecord);
        objectUpdatePaymentPlanData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectUpdatePaymentPlanData);
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
    UpdatePaymentPlanData objectUpdatePaymentPlanData[] = new UpdatePaymentPlanData[vector.size()];
    vector.copyInto(objectUpdatePaymentPlanData);
    return(objectUpdatePaymentPlanData);
  }

  public static int update(ConnectionProvider connectionProvider, String totalAmount, String finpaymentscheduleid)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      UPDATE fin_payment_schedule fin" +
      "      SET outstandingamt = amount - TO_NUMBER(?)," +
      "      paidamt = TO_NUMBER(?)" +
      "      WHERE fin.fin_payment_schedule_id = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, totalAmount);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, totalAmount);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, finpaymentscheduleid);

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

  public static boolean isExecuted(ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT count(*) as exist" +
      "        FROM DUAL" +
      "        WHERE EXISTS (SELECT 1 FROM ad_preference" +
      "                      WHERE attribute = 'PaymentPlanUpdated')";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "EXIST").equals("0");
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
      "           INSERT INTO ad_preference (" +
      "           ad_preference_id, ad_client_id, ad_org_id, isactive," +
      "           createdby, created, updatedby, updated,attribute" +
      "           ) VALUES (" +
      "           get_uuid(), '0', '0', 'Y', '0', NOW(), '0', NOW(),'PaymentPlanUpdated')";

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
