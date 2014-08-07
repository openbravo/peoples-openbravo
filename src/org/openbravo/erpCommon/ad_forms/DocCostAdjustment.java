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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.procurement.ReceiptInvoiceMatch;

public class DocCostAdjustment extends AcctServer {

  private static final long serialVersionUID = 1L;
  static Logger log4jDocCostAdjustment = Logger.getLogger(DocCostAdjustment.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

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
        if (isNegativeAmount) {
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
        if (isNegativeAmount) {
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
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn).C_ValidCombination_ID);
        if (isNegativeAmount) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INTERNALMOVEMENTFROM)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn).C_ValidCombination_ID);
        if (isNegativeAmount) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);

        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      } else if (transactionType.equals(DocLine_CostAdjustment.TRXTYPE_INTERNALMOVEMENTTO)) {
        // Inventory Asset DR
        // Inventory Adjustment CR
        M_Warehouse_ID = line.getWarehouseId();
        log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account - "
            + getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn).C_ValidCombination_ID);
        if (isNegativeAmount) {
          amtDebit = amount.toString();
          amtCredit = "";
        } else {
          amtDebit = "";
          amtCredit = amount.toString();
        }
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_InvDifferences, as, conn),
            line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
        fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn),
            line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, line.m_DateAcct, null, conn);
      }
    } // lines

    // Cash Asset
    log4jDocCostAdjustment.debug("********** DocCostAdjustment - factAcct - account2 - "
        + getAccount(AcctServer.ACCTTYPE_CashAsset, as, conn).C_ValidCombination_ID);
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
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  /**
   * Update Product Info. - Costing (PriceLastInv) - PO (PriceLastInv)
   * 
   * @param C_AcctSchema_ID
   *          accounting schema
   */
  public void updateProductInfo(String C_AcctSchema_ID, ConnectionProvider conn, Connection con) {
    log4jDocCostAdjustment.debug("updateProductInfo - C_Invoice_ID=" + this.Record_ID);

    /**
     * @todo Last.. would need to compare document/last updated date would need to maintain
     *       LastPriceUpdateDate on _PO and _Costing
     */

    // update Product PO info
    // should only be once, but here for every AcctSchema
    // ignores multiple lines with same product - just uses first
    int no = 0;
    try {
      no = DocInvoiceData.updateProductPO(con, conn, Record_ID);
      log4jDocCostAdjustment.debug("M_Product_PO - Updated=" + no);

    } catch (ServletException e) {
      log4jDocCostAdjustment.warn(e);
    }
  } // updateProductInfo

  private MaterialTransaction getTransaction(String matchInvId) {
    OBContext.setAdminMode(false);
    MaterialTransaction transaction;
    if (OBDal.getInstance().get(ReceiptInvoiceMatch.class, matchInvId).getGoodsShipmentLine()
        .getMaterialMgmtMaterialTransactionList().size() == 0) {
      return null;
    }
    try {
      transaction = OBDal.getInstance().get(ReceiptInvoiceMatch.class, matchInvId)
          .getGoodsShipmentLine().getMaterialMgmtMaterialTransactionList().get(0);
    } finally {

      OBContext.restorePreviousMode();
    }
    return transaction;
  }

  private ShipmentInOutLine getShipmentLine(String matchInvId) {
    OBContext.setAdminMode(false);
    ShipmentInOutLine shipmentLine;
    try {
      shipmentLine = OBDal.getInstance().get(ReceiptInvoiceMatch.class, matchInvId)
          .getGoodsShipmentLine();
    } finally {
      OBContext.restorePreviousMode();
    }
    return shipmentLine;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
