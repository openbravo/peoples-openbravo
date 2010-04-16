/*
 ************************************************************************************
 * Copyright (C) 2010 Openbravo S.L.U.

 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at
  http://www.openbravo.com/legal/obcl.html
  <http://www.openbravo.com/legal/obcl.html>
 ************************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;

public class DocLine_FINPayment extends DocLine {
  static Logger log4jDocLine_Payment = Logger.getLogger(DocLine_FINPayment.class);

  String Line_ID = "";
  String Amount = "";
  String WriteOffAmt = "";
  String isReceipt = "";
  String C_GLItem_ID = "";
  String isPrepayment = "";

  /**
   * @param isReceipt
   *          the isReceipt to set
   */
  public void setIsReceipt(String isReceipt) {
    this.isReceipt = isReceipt;
  }

  /**
   * @return the isReceipt
   */
  public String getIsReceipt() {
    return isReceipt;
  }

  /**
   * @param isPrepayment
   *          the isPrepayment to set
   */
  public void setIsPrepayment(String isPrepayment) {
    this.isPrepayment = isPrepayment;
  }

  /**
   * @return the isPrepayment
   */
  public String getIsPrepayment() {
    return isPrepayment;
  }

  /**
   * @return the amount
   */
  public String getAmount() {
    return Amount;
  }

  /**
   * @return the log4jDocLine_Payment
   */
  public static Logger getLog4jDocLine_Payment() {
    return log4jDocLine_Payment;
  }

  /**
   * @param log4jDocLine_Payment
   *          the log4jDocLine_Payment to set
   */
  public static void setLog4jDocLine_Payment(Logger log4jDocLine_Payment) {
    DocLine_FINPayment.log4jDocLine_Payment = log4jDocLine_Payment;
  }

  /**
   * @return the line_ID
   */
  public String getLine_ID() {
    return Line_ID;
  }

  /**
   * @param line_ID
   *          the line_ID to set
   */
  public void setLine_ID(String line_ID) {
    Line_ID = line_ID;
  }

  /**
   * @return the writeOffAmt
   */
  public String getWriteOffAmt() {
    return WriteOffAmt;
  }

  /**
   * @param writeOffAmt
   *          the writeOffAmt to set
   */
  public void setWriteOffAmt(String writeOffAmt) {
    WriteOffAmt = writeOffAmt;
  }

  /**
   * @return the c_GLItem_ID
   */
  public String getC_GLItem_ID() {
    return C_GLItem_ID;
  }

  /**
   * @param item_ID
   *          the c_GLItem_ID to set
   */
  public void setC_GLItem_ID(String item_ID) {
    C_GLItem_ID = item_ID;
  }

  /**
   * @param amount
   *          the amount to set
   */
  public void setAmount(String amount) {
    Amount = amount;
  }

  public DocLine_FINPayment(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
    Line_ID = TrxLine_ID;
    m_Record_Id2 = Line_ID;
  }

  public String getServletInfo() {
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
