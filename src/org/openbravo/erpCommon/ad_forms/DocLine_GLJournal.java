package org.openbravo.erpCommon.ad_forms;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class DocLine_GLJournal extends DocLine {
  static Logger log4jDocLine_GLJournal = Logger.getLogger(DocLine_GLJournal.class);

  public DocLine_GLJournal(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  /**
   * Line Account from Product (or Charge).
   * 
   * @param AcctType
   *          see ProoductInfo.ACCTTYPE_* (0..3)
   * @param as
   *          Accounting schema
   * @return Requested Product Account
   */
  public Account getAccount(String AcctType, AcctSchema as, ConnectionProvider conn) {

    // GL Item directly from GLJournal Line
    if (m_C_Glitem_ID != null && !m_C_Glitem_ID.equals("")) {
      try {
        DocLineGLJournalData[] data = null;
        data = DocLineGLJournalData.selectGlitem(conn, m_C_Glitem_ID, as.getC_AcctSchema_ID());
        String Account_ID = "";
        if (data == null || data.length == 0)
          return null;
        if (data.length > 0) {
          switch (Integer.parseInt(AcctType)) {
          case 1:
            // It is similar to ProductInfo.ACCTTYPE_P_Revenue
            Account_ID = data[0].glitemCreditAcct;
            break;
          case 2:
            // It is similar to ProductInfo.ACCTTYPE_P_Expense
            Account_ID = data[0].glitemDebitAcct;
            break;
          }
        }
        // No account
        if (Account_ID.equals("")) {
          log4jDocLine_GLJournal.warn("getAccount - NO account for m_C_Glitem_ID=" + m_C_Glitem_ID);
          return null;
        }
        // Return Account
        return Account.getAccount(conn, Account_ID);

      } catch (ServletException e) {
        log4jDocLine_GLJournal.warn(e);
      }
    } else {
      return this.m_account;
    }
    return null;
  } // getAccount
}
