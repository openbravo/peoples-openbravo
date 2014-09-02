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

public class DocLandedCost extends AcctServer {

  private static final long serialVersionUID = 1L;
  static Logger log4jDocLandedCost = Logger.getLogger(DocLandedCost.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

  public DocLandedCost() {
  }

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocLandedCost(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, @SuppressWarnings("hiding")
  String AD_Client_ID, String Id) throws ServletException {
    setObjectFieldProvider(DocLandedCostData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    C_Currency_ID = NO_CURRENCY;

    // TODO: SACAR CURRENCY
    DocumentType = AcctServer.DOCTYPE_LandedCost;
    log4jDocLandedCost.debug("loadDocumentDetails - C_Currency_ID : " + C_Currency_ID);
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

    DocLineLandedCostData[] data = null;
    try {
      data = DocLineLandedCostData.select(conn, Record_ID);
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].mLcReceiptlineAmtId;
        DocLine_LandedCost docLine = new DocLine_LandedCost(DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);
        docLine.m_C_Currency_ID = data[i].cCurrencyId;
        docLine.setWarehouseId(data[i].mWarehouseId);
        docLine.m_C_BPartner_ID = data[i].cBpartnerId;
        docLine.m_M_Product_ID = data[i].mProductId;
        docLine.m_DateAcct = DateDoc;
        docLine.setLandedCostTypeId(data[i].mLcTypeId);
        // -- Source Amounts
        String amt = data[i].amount;
        docLine.setAmount(amt);
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4jDocLandedCost.warn(e);
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
        DocLandedCostTemplate newTemplate = (DocLandedCostTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocLandedCostTemplate - " + e);
      }
    }
    C_Currency_ID = as.getC_Currency_ID();
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    String amtDebit = "0";
    String amtCredit = "0";
    // Lines
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_LandedCost line = (DocLine_LandedCost) p_lines[i];

      BigDecimal amount = new BigDecimal(line.getAmount());
      ProductInfo p = new ProductInfo(line.m_M_Product_ID, conn);

      log4jDocLandedCost.debug("previous to creteline, line.getAmount(): " + line.getAmount());

      amtDebit = amount.toString();
      amtCredit = "";

      fact.createLine(line, getLandedCostAccount(line.getLandedCostTypeId(), amount, as, conn),
          line.m_C_Currency_ID, amtDebit, amtCredit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, line.m_DateAcct, null, conn);

      fact.createLine(line, p.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn),
          line.m_C_Currency_ID, amtCredit, amtDebit, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, line.m_DateAcct, null, conn);
    }

    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocLandedCost
   */
  public static Logger getlog4jDocLandedCost() {
    return log4jDocLandedCost;
  }

  /**
   * @param log4jDocLandedCost
   *          the log4jDocLandedCost to set
   */
  public static void setlog4jDocLandedCost(Logger log4jDocLandedCost) {
    DocLandedCost.log4jDocLandedCost = log4jDocLandedCost;
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
    log4jDocLandedCost.debug("DocLandedCost - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocLandedCost.debug("DocLandedCost - nextSeqNo = " + SeqNo);
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
  public final Account getLandedCostAccount(String lcTypeId, BigDecimal amount, AcctSchema as,
      ConnectionProvider conn) {
    String Account_ID = "";
    DocLineLandedCostData[] data = null;
    Account acct = null;
    try {
      DocLineLandedCostData[] dataAcctType = DocLineLandedCostData.selectLCAccount(conn, lcTypeId);
      if (!"".equals(dataAcctType[0].accountId)) {
        data = DocLineLandedCostData.selectGlitem(conn, dataAcctType[0].accountId,
            as.getC_AcctSchema_ID());
        if (data.length > 0) {
          Account_ID = data[0].glitemDebitAcct;
          if (amount != null && amount.signum() < 0) {
            Account_ID = data[0].glitemCreditAcct;
          }
        }
      } else if (!"".equals(dataAcctType[0].mProductId)) {
        data = DocLineLandedCostData.selectLCProduct(conn, dataAcctType[0].mProductId,
            as.getC_AcctSchema_ID());
        if (data.length > 0) {
          Account_ID = data[0].accountId;
        }
      } else {
        log4jDocLandedCost.warn("getLandedCostAccount - NO account for landed cost type "
            + dataAcctType[0].name);
        return null;
      }

      // No account
      if (Account_ID.equals("")) {
        log4jDocLandedCost.warn("getLandedCostAccount - NO account for landed cost type ="
            + dataAcctType[0].name);
        return null;
      }
      // Return Account
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4jDocLandedCost.warn(e);
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
