/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2014 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;

public class DocCostAdjustment extends AcctServer {

  private static final long serialVersionUID = 1L;
  static Logger log4jDocCostAdjustment = Logger.getLogger(DocCostAdjustment.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

  public DocCostAdjustment() {
  }

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocCostAdjustment(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, @SuppressWarnings("hiding")
  String AD_Client_ID, String Id) throws ServletException {
    setObjectFieldProvider(DocCostAdjustmentData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    C_Currency_ID = NO_CURRENCY;

    // TODO: SACAR CURRENCY
    DocumentType = AcctServer.DOCTYPE_CostAdjustment;
    log4jDocCostAdjustment.debug("loadDocumentDetails - C_Currency_ID : " + C_Currency_ID);
    DateDoc = data[0].getField("DateTrx");
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();

    DocLineCostAdjustmentData[] data = null;
    try {
      data = DocLineCostAdjustmentData.select(conn, Record_ID);
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].mCostadjustmentlineId;
        DocLine_CostAdjustment docLine = new DocLine_CostAdjustment(DocumentType, Record_ID,
            Line_ID);
        docLine.loadAttributes(data[i], this);
        docLine.setTrxType(data[i].mTransactionId);
        docLine.m_C_Currency_ID = data[i].cCurrencyId;
        docLine.setWarehouseId(data[i].mWarehouseId);
        docLine.m_DateAcct = data[i].dateacct;
        // -- Source Amounts
        String amt = data[i].adjustmentAmount;
        docLine.setAmount(amt);
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4jDocCostAdjustment.warn(e);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Balance
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for MMS, MMR.
   * 
   * <pre>
   *  Shipment
   *      CoGS            DR
   *      Inventory               CR
   *  Shipment of Project Issue
   *      CoGS            DR
   *      Project                 CR
   *  Receipt
   *      Inventory       DR
   *      NotInvoicedReceipt      CR
   * </pre>
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = AcctServerData
        .selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocCostAdjustmentTemplate newTemplate = (DocCostAdjustmentTemplate) Class.forName(
            strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocCostAdjustmentTemplate - " + e);
      }
    }
    C_Currency_ID = as.getC_Currency_ID();
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    boolean isNegativeAmount;
    String amtDebit = "0";
    String amtCredit = "0";
    // Lines
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_CostAdjustment line = (DocLine_CostAdjustment) p_lines[i];
      String transactionType = line.getTransactionType();

      BigDecimal amount = new BigDecimal(line.getAmount());
      isNegativeAmount = false;
      if (amount.signum() < 0) {
        isNegativeAmount = true;
      }

      ProductInfo p = new ProductInfo(line.m_M_Product_ID, conn);

      log4jDocCostAdjustment.debug("antes del creteline, line.getAmount(): " + line.getAmount()
          + " - TransactionType: " + transactionType);
      if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_SHIPMENT)) {
        // Cogs DR
        // Inventory Asset CR
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + p.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {

        }
        if (line.isTransactionNegative()) {
          amtDebit = "";
          amtCredit = amount.toString();
        } else {
          amtDebit = amount.toString();
          amtCredit = "";
        }
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_RECEIPT)) {
        // Inventory Asset DR
        // Product Exp CR
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + p.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INVENTORY)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INTERNALMOVEMENTFROM)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);

        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INTERNALMOVEMENTTO)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INTERNALCONSUMPTION)) {
        // TODO: review if the accounting generated by internalconsumption is similar
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);

        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_BOM)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);

        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_MANUFACTURING)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn).C_ValidCombination_ID);
        if (line.isTransactionNegative()) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(
            line,
            getAccountByWarehouse(AcctServer.ACCTTYPE_InvDifferences, as, line.getWarehouseId(),
                conn), line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, line.m_DateAcct, null, conn);

        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      }
    } // lines

    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocCostAdjustment
   */
  public static Logger getlog4jDocCostAdjustment() {
    return log4jDocCostAdjustment;
  }

  /**
   * @param log4jDocCostAdjustment
   *          the log4jDocCostAdjustment to set
   */
  public static void setlog4jDocCostAdjustment(Logger log4jDocCostAdjustment) {
    DocCostAdjustment.log4jDocCostAdjustment = log4jDocCostAdjustment;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String nextSeqNo(String oldSeqNo) {
    log4jDocCostAdjustment.debug("DocMatchInv - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocCostAdjustment.debug("DocMatchInv - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get the account for Accounting Schema
   * 
   * @param AcctType
   *          see ACCTTYPE_*
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountByWarehouse(String AcctType, AcctSchema as, String WarehouseId,
      ConnectionProvider conn) {
    AcctServerData[] data = null;

    try {
      /** Account Type - Invoice */
      if (AcctType.equals(ACCTTYPE_InvDifferences)) {
        /** Inventory Accounts */
        data = AcctServerData.selectWDifferencesAcct(conn, WarehouseId, as.getC_AcctSchema_ID());
      } else {
        log4jDocCostAdjustment.warn("AcctServer - getAccount - Not found AcctType=" + AcctType);
        return null;
      }
    } catch (ServletException e) {
      log4jDocCostAdjustment.warn(e);
      e.printStackTrace();
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4jDocCostAdjustment.warn("AcctServer - getAccount - NO account Type=" + AcctType
          + ", Record=" + Record_ID);
      return null;
    }
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - *******************************getAccount 4");
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4jDocCostAdjustment.warn(e);
      e.printStackTrace();
    }
    return acct;
  } // getAccount

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
