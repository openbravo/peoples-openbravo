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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.base.secureApp.VariablesSecureApp;
import java.math.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.Logger;
// imports for transactions
import org.openbravo.database.ConnectionProvider;
import java.sql.Connection;
import org.openbravo.data.FieldProvider;



public class DocDPManagement extends AcctServer {
    private static final long serialVersionUID = 1L;
    static Logger log4j = Logger.getLogger(DocDPManagement.class);

    private String        SeqNo = "0";


    /**
     *  Constructor
     *  @param AD_Client_ID AD_Client_ID
     */
    public DocDPManagement(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider){
        super(AD_Client_ID, AD_Org_ID, connectionProvider);
    }

public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id) throws ServletException{
    setObjectFieldProvider(DocDPManagementData.selectRegistro(conn, AD_Client_ID, Id));
}

/**
 *  Load Specific Document Details
 *  @param rs result set
 *  @return true if loadDocumentType was set
 */
public boolean loadDocumentDetails (FieldProvider [] data,ConnectionProvider conn){
    DocumentType = AcctServer.DOCTYPE_DPManagement;
    DateDoc = data[0].getField("Dateacct");
    loadDocumentType();     //  lines require doc type
    //  Contained Objects
    p_lines = loadLines(conn);
    log4j.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    return false;
}   //  loadDocumentDetails

/**
 *  Load AmortizationLine Line.
 *  @return DocLine Array
 */
private DocLine[] loadLines(ConnectionProvider conn){
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineDPManagementData [] data =  null;

    try{
        data = DocLineDPManagementData.select(conn, Record_ID);
    }catch(ServletException e){
        log4j.warn(e);
    }

    log4j.debug("LoadLines: data.len"+data.length+" record_ID "+Record_ID);
        //
    for (int i=0;data!=null && i<data.length;i++){
        String Line_ID = data[i].getField("C_DP_MANAGEMENTLINE_ID");
        DocLine_DPManagement docLine = new DocLine_DPManagement (DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);
        docLine.Amount = data[i].getField("AMOUNT");
        docLine.m_Record_Id2 = data[i].getField("C_DEBT_PAYMENT_ID");
        docLine.Isreceipt = data[i].getField("ISRECEIPT");
        docLine.StatusTo  = data[i].getField("STATUS_TO");
        docLine.StatusFrom = data[i].getField("STATUS_FROM");
        list.add(docLine);
    }

    //  Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
}   //  loadLines

/**
 *  Get Source Currency Balance - always zero
 *  @return Zero (always balanced)
 */
public BigDecimal getBalance(){
    BigDecimal retValue = ZERO;

    return retValue;
}   //  getBalance


/**
 *  Create Facts (the accounting logic) for
 *  @param as accounting schema
 *  @return Fact
 */
public Fact createFact (AcctSchema as,ConnectionProvider conn,Connection con,VariablesSecureApp vars) throws ServletException{
    log4j.debug("createFact - Inicio");
    //  create Fact Header
    Fact fact = null;
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    log4j.debug("createFact - object created");
    //  Lines
    fact = new Fact(this, as, Fact.POST_Actual);
    for (int i = 0;p_lines != null && i < p_lines.length; i++){
        DocLine_DPManagement line = (DocLine_DPManagement)p_lines[i];
//        ProductInfo product = new ProductInfo(line.m_M_Product_ID,conn);
      if (line.Isreceipt.equals("Y"))
      {
        fact.createLine(line,getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusTo, conn),line.m_C_Currency_ID, line.Amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        fact.createLine(line,getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusFrom, conn),line.m_C_Currency_ID, "", line.Amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
         
      } else {
        fact.createLine(line,getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusTo, conn),line.m_C_Currency_ID, "", line.Amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        fact.createLine(line,getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusFrom, conn),line.m_C_Currency_ID, line.Amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      }
    }
    SeqNo = "0";
    return fact;
}   //  createFact


    public String nextSeqNo(String oldSeqNo){
      log4j.debug("DocAmortization - oldSeqNo = " + oldSeqNo);
      BigDecimal seqNo = new BigDecimal(oldSeqNo);
      SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
      log4j.debug("DocAmortization - nextSeqNo = " + SeqNo);
      return SeqNo;
    }

  /**
   *  Get Document Confirmation
   *  @not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  /**
   *  Line Account from Asset
   *
   *  @param  AcctType see ACCTTYPE_* (1..8)
   *  @param as Accounting Schema
   *  @return Requested Asset Account
   */
  public Account getAccount(String Isreceipt, String partnerID, AcctSchema as, String status, ConnectionProvider conn){
    /*if (Integer.parseInt(AcctType) < 1 || Integer.parseInt(AcctType) > 2)
      return null;*/

    //  No Product - get Default from Product Category
    /*if (A_Asset_ID.equals(""))
      return getAccountDefault(AcctType, as, conn);*/
    DocDPManagementData [] data = null;
    Account acc =null;
    try{
      
      String validCombination_ID = "";

      if (Isreceipt.equals("Y"))
      {
           data = DocDPManagementData.selectReceiptAcct(conn, partnerID, as.getC_AcctSchema_ID(),status);
          validCombination_ID = data[0].acct;
      }else{
        data = DocDPManagementData.selectNoReceiptAcct(conn, partnerID, as.getC_AcctSchema_ID(),status);
            validCombination_ID = data[0].acct;
      }

      if(data==null || data.length == 0) return null;
      

      if (validCombination_ID.equals(""))
        return null;
      acc = Account.getAccount(conn,validCombination_ID);
    }catch(ServletException e){
      log4j.warn(e);
    }
    log4j.debug("DocAmortization - getAccount - " + acc.Account_ID);
    return acc;
  }   //  getAccount

    public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
