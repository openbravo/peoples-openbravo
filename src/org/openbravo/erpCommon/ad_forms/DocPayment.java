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
 * Contributor(s): Openbravo SL
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.
 ******************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.base.secureApp.VariablesSecureApp;
import java.math.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.Logger ;
// imports for transactions
import org.openbravo.database.ConnectionProvider;
import java.sql.Connection;
import org.openbravo.data.FieldProvider;



public class DocPayment extends AcctServer {
    private static final long serialVersionUID = 1L;
    static Logger log4j = Logger.getLogger(DocPayment.class);

    private String        SeqNo = "0";
    private String        SettlementType = "";
	public final BigDecimal ZERO = new BigDecimal("0");
/**
 *  Constructor
 *  @param AD_Client_ID AD_Client_ID
 */
public DocPayment(String AD_Client_ID, ConnectionProvider connectionProvider){
    super(AD_Client_ID, connectionProvider);
}

public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id) throws ServletException{
    setObjectFieldProvider(DocPaymentData.selectRegistro(conn, AD_Client_ID, Id));
}

/**
 *  Load Specific Document Details
 *  @return true if loadDocumentType was set
 */
public boolean loadDocumentDetails (FieldProvider [] data,ConnectionProvider conn){
    DateDoc = data[0].getField("DateTrx");
    ChargeAmt = data[0].getField("ChargedAmt");
    SettlementType = data[0].getField("settlementtype");
    //  Contained Objects
    p_lines = loadLines(conn);
    if (log4j.isDebugEnabled()) log4j.debug("DocPayment - loadDocumentDetails - Lines=" + p_lines.length);
    return false;
}   //  loadDocumentDetails

/**
 *  Load Payment Line. Settlement Cancel
 *  @return DocLine Array
 */
private DocLine[] loadLines(ConnectionProvider conn){
    ArrayList<Object> list = new ArrayList<Object>();
    DocLinePaymentData[] data = null;
    try{
        data = DocLinePaymentData.select(connectionProvider, Record_ID);
    }
    catch (ServletException e){
        log4j.warn(e);
    }
    for (int i=0;i<data.length;i++){
        String Line_ID = data[i].cDebtPaymentId;
        DocLine_Payment docLine = new DocLine_Payment (DocumentType, Record_ID, Line_ID);
        docLine.Amount = data[i].getField("amount");
        docLine.WriteOffAmt = data[i].getField("writeoffamt");
        docLine.isReceipt = data[i].getField("isreceipt");
        docLine.isManual = data[i].getField("ismanual");
        docLine.isPaid = data[i].getField("ispaid");
        docLine.loadAttributes(data[i], this);
        docLine.m_Record_Id2 = data[i].cDebtPaymentId;
        docLine.C_Settlement_Generate_ID = data[i].getField("cSettlementGenerateId");
        docLine.C_Settlement_Cancel_ID = data[i].getField("cSettlementCancelId");
        docLine.C_GLItem_ID = data[i].getField("cGlitemId");
        docLine.IsDirectPosting = data[i].getField("isdirectposting");
        docLine.C_Currency_ID_From = data[i].getField("cCurrencyId");
        docLine.conversionDate = data[i].getField("conversiondate");
        try{
          docLine.dpStatus = DocLinePaymentData.getDPStatus(connectionProvider, Record_ID, data[i].getField("cDebtPaymentId"));
        } catch(ServletException e) {
          log4j.error(e);
          docLine.dpStatus = "";
        }
        if (log4j.isDebugEnabled()) log4j.debug("DocPayment - loadLines - docLine.IsDirectPosting - " + docLine.IsDirectPosting);
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
 *  STT, APP.
 *  <pre>
 *  
 *  Flow:
 *    1. Currency conversion variations 
 *    2. Non manual DPs in settlement
 *       2.1 Cancelled
 *       2.2 Generated
 *    3. Manual DPs in settlement
 *       3.1 Transitory account
 *    4. Conceptos contables (manual sett and cancelation DP)
 *    5. Writeoff
 *    6. Bank in transit
 *
 *  </pre>
 *  @param as accounting schema
 *  @return Fact
 */
  public Fact createFact (AcctSchema as,ConnectionProvider conn,Connection con,VariablesSecureApp vars) throws ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("DocPayment - createFact - p_lines.length - " + p_lines.length);
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getSequence(conn, "Fact_Acct_Group", vars.getClient());

    //Loop to cover C_Debt_Payment in settlement (SttType != 'I' || directPosting=Y)
    for (int i = 0;p_lines!=null && i < p_lines.length; i++) {
      DocLine_Payment line = (DocLine_Payment)p_lines[i];
      
      if (log4j.isDebugEnabled()) log4j.debug("DocPayment - createFact - line.conversionDate - " + line.conversionDate);
      //1* Amount is calculated and if there is currency conversion variations between dates this change is accounted
      String convertedAmt = convertAmount(line.Amount,line.isReceipt.equals("Y"),DateAcct, line.conversionDate, line.C_Currency_ID_From, C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, conn);
           
      if(line.isManual.equals("N")) { //2* Normal debt-payments
        if (!line.C_Settlement_Generate_ID.equals(Record_ID)) { //2.1* Cancelled DP
          fact.createLine(line, getAccountBPartner(line.m_C_BPartner_ID, as, line.isReceipt.equals("Y"), line.dpStatus, conn),C_Currency_ID,
                                   (line.isReceipt.equals("Y")?"":convertedAmt), 
                                   (line.isReceipt.equals("Y")?convertedAmt:""), 
                                   Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,conn);
        } else { //2.2* Generated DP
          if (log4j.isDebugEnabled()) log4j.debug("Genenarted DP");
          if(!line.isPaid.equals("Y") || !(line.C_Settlement_Cancel_ID==null || line.C_Settlement_Cancel_ID.equals(""))) { 
            if (log4j.isDebugEnabled()) log4j.debug("Not paid");
            fact.createLine(line, getAccountBPartner(line.m_C_BPartner_ID, as, line.isReceipt.equals("Y"), line.dpStatus, conn),C_Currency_ID, 
                            (line.isReceipt.equals("Y")?convertedAmt:""), 
                            (line.isReceipt.equals("Y")?"":convertedAmt), 
                            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,conn);
          }
        }
        
        if (log4j.isDebugEnabled()) log4j.debug("DocPayment - createFact - No manual  - isReceipt: " + line.isReceipt);
      } else {//3* MANUAL debt-payments (generated in a Manual stt)
        if (log4j.isDebugEnabled()) log4j.debug("Manual DP - DirectPosting: "+line.IsDirectPosting+" - SettType:"+SettlementType);
        if (line.IsDirectPosting.equals("Y")) { //Direct posting: transitory Account
          BigDecimal amount = ZERO;
          DocPaymentData [] data = DocPaymentData.selectDirectManual(conn, as.m_C_AcctSchema_ID, line.Line_ID);
          if (log4j.isDebugEnabled()) log4j.debug("data[0].amount:"+data[0].amount+" - convertedAmt:"+convertedAmt);
          
          if(convertedAmt!=null && !convertedAmt.equals("")) amount = new BigDecimal(convertedAmt);
          boolean changeGenerate = (!SettlementType.equals("I"));
          if(changeGenerate) amount = amount.negate();
          BigDecimal transitoryAmount = new BigDecimal(convertedAmt);
          if (log4j.isDebugEnabled()) log4j.debug("Manual DP - amount:"+amount+" - transitoryAmount:"+transitoryAmount+" - Receipt:"+line.isReceipt);
          //Depending on the stt type and the signum of DP it will be posted on credit or debit
          if(amount.signum() == 1) {
            fact.createLine(line, new Account(conn, (line.isReceipt.equals("Y")?data[0].creditAcct:data[0].debitAcct)), C_Currency_ID,
                            (line.isReceipt.equals("Y")?transitoryAmount.abs().toString():"0"), 
                            (line.isReceipt.equals("Y")?"0":transitoryAmount.abs().toString()), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if ((!changeGenerate && line.isReceipt.equals("N")) || (changeGenerate && line.isReceipt.equals("Y"))) amount = amount.negate();
          } else {
            fact.createLine(line, new Account(conn, (line.isReceipt.equals("Y")?data[0].creditAcct:data[0].debitAcct)), C_Currency_ID,
                            (line.isReceipt.equals("Y")?"0":transitoryAmount.abs().toString()), 
                            (line.isReceipt.equals("Y")?transitoryAmount.abs().toString():"0"), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if ((!changeGenerate && line.isReceipt.equals("Y")) || (changeGenerate && line.isReceipt.equals("N"))) amount = amount.negate();
          }
        }
        //4 Manual Sett + Cancelation Sett (no direct posting) conceptos contables
        if (SettlementType.equals("I") || line.IsDirectPosting.equals("N")) {
          DocPaymentData [] data = DocPaymentData.selectManual(conn, as.m_C_AcctSchema_ID, line.Line_ID);
          for(int j = 0;data!=null && j < data.length;j++){
            String amountdebit = getConvertedAmt (data[j].amountdebit, line.C_Currency_ID_From, C_Currency_ID, DateAcct, "", AD_Client_ID,AD_Org_ID,conn);
            String amountcredit = getConvertedAmt (data[j].amountcredit, line.C_Currency_ID_From, C_Currency_ID, DateAcct, "", AD_Client_ID,AD_Org_ID,conn);
            if (log4j.isDebugEnabled()) log4j.debug("DocPayment - createFact - Conceptos - AmountDebit: " + amountdebit + " - AmountCredit: " + amountcredit);
            fact.createLine(line, new Account(conn, (line.isReceipt.equals("Y")?data[j].creditAcct:data[j].debitAcct)), C_Currency_ID, (amountdebit.equals("0")?"":amountdebit), (amountcredit.equals("0")?"":amountcredit), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          }
        }
      } //END debt-payment conditions

      //5* WRITEOFF calculations
      if (line.C_Settlement_Cancel_ID.equals(Record_ID)) { //Cancelled debt-payments
        if (line.WriteOffAmt!=null && !line.WriteOffAmt.equals("") && !line.WriteOffAmt.equals("0")) {
          fact.createLine(line, getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as,conn),C_Currency_ID, 
                          (line.isReceipt.equals("Y")?line.WriteOffAmt:""), 
                          (line.isReceipt.equals("Y")?"":line.WriteOffAmt), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,conn);
        }
      }

      //6* PPA - Bank in transit default, paid DPs, (non manual and manual non direct posting) 
      if(line.isPaid.equals("Y")&&((line.C_Settlement_Cancel_ID == null || line.C_Settlement_Cancel_ID.equals(""))||(line.C_Settlement_Cancel_ID.equals(Record_ID)))){
        BigDecimal finalLineAmt = new BigDecimal(line.Amount);
        if (line.WriteOffAmt!=null && !line.WriteOffAmt.equals("") && !line.WriteOffAmt.equals("0")) finalLineAmt = finalLineAmt.subtract(new BigDecimal(line.WriteOffAmt));
        String finalAmtTo = getConvertedAmt (finalLineAmt.toString(), line.C_Currency_ID_From, C_Currency_ID, DateAcct, "", AD_Client_ID,AD_Org_ID,conn);  
        finalLineAmt = new BigDecimal(finalAmtTo);
        if(finalLineAmt.compareTo(new BigDecimal("0.00"))!=0) fact.createLine(line, getAccount(AcctServer.ACCTTYPE_BankInTransitDefault, as,conn),C_Currency_ID, 
                          (line.isReceipt.equals("Y")?finalAmtTo:""), 
                          (line.isReceipt.equals("Y")?"":finalAmtTo), Fact_Acct_Group_ID, "999999", DocumentType,conn);
      }
    } //END of the C_Debt_Payment loop
    SeqNo = "0";
    if (log4j.isDebugEnabled()) log4j.debug("DocPayment - createFact - finish");
    return fact;
  }

  public String convertAmount(String Amount,boolean isReceipt, String DateAcct, String conversionDate, String C_Currency_ID_From, String C_Currency_ID, DocLine line, AcctSchema as, Fact fact, String Fact_Acct_Group_ID, ConnectionProvider conn) throws ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Amount:"+Amount+" curr from:"+C_Currency_ID_From+" Curr to:"+C_Currency_ID+" convDate:"+conversionDate+" DateAcct:"+DateAcct);
    
    String Amt = getConvertedAmt (Amount, C_Currency_ID_From, C_Currency_ID,conversionDate, "", AD_Client_ID,AD_Org_ID,conn);
    if (log4j.isDebugEnabled()) log4j.debug("Amt:"+Amt);
    
    String AmtTo = getConvertedAmt (Amount, C_Currency_ID_From, C_Currency_ID, DateAcct, "", AD_Client_ID,AD_Org_ID,conn);
    if (log4j.isDebugEnabled()) log4j.debug("AmtTo:"+AmtTo);
    
    BigDecimal AmtDiff = (new BigDecimal(AmtTo)).subtract(new BigDecimal(Amt));
    if (log4j.isDebugEnabled()) log4j.debug("AmtDiff:"+AmtDiff);
    
    if (log4j.isDebugEnabled()){ 
      log4j.debug("curr from:"+C_Currency_ID_From+" Curr to:"+C_Currency_ID+" convDate:"+conversionDate+" DateAcct:"+DateAcct);
      log4j.debug("Amt:"+Amt+" AmtTo:"+AmtTo+" Diff:"+AmtDiff.toString());
    }

    if ((isReceipt && AmtDiff.compareTo(new BigDecimal("0.00"))==1) || (!isReceipt && AmtDiff.compareTo(new BigDecimal("0.00"))==-1)) {
      fact.createLine(line,getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),C_Currency_ID,"", AmtDiff.abs().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    } else {
      fact.createLine(line,getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),C_Currency_ID, AmtDiff.abs().toString(),"", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    }
    
    return Amt;
  }


    /**
     *  Get the account for Accounting Schema
     *  @param AcctType see ACCTTYPE_*
     *  @param as accounting schema
     *  @return Account
     */
    public final Account getAccountBPartner(String cBPartnerId, AcctSchema as,boolean isReceipt, String dpStatus, ConnectionProvider conn){
        DocPaymentData [] data=null;
        try{
            if (log4j.isDebugEnabled()) log4j.debug("DocPayment - getAccountBPartner - DocumentType = " + DocumentType);
            if (isReceipt){
                data = DocPaymentData.selectBPartnerCustomerAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(), dpStatus);
            }else{
                data = DocPaymentData.selectBPartnerVendorAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(), dpStatus);
            }
        }catch(ServletException e){
            log4j.warn(e);
        }
        //  Get Acct
        String Account_ID = "";
        if (data != null && data.length!=0){
            Account_ID = data[0].accountId;
        }else   return null;
        //  No account
        if (Account_ID.equals("")){
            log4j.warn("DocPayment - getAccountBPartner - NO account BPartner="
                + cBPartnerId + ", Record=" + Record_ID+", status "+dpStatus);
            return null;
        }
        //  Return Account
        Account acct = null;
        try{
            acct = Account.getAccount(conn, Account_ID);
        }catch(ServletException e){
            log4j.warn(e);
        }
        return acct;
    }   //  getAccount

    public String nextSeqNo(String oldSeqNo){
      if (log4j.isDebugEnabled()) log4j.debug("DocPayment - oldSeqNo = " + oldSeqNo);
      BigDecimal seqNo = new BigDecimal(oldSeqNo);
      SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
      if (log4j.isDebugEnabled()) log4j.debug("DocPayment - nextSeqNo = " + SeqNo);
      return SeqNo;
    }

  /**
   *  Get Document Confirmation
   *  @not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

    public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
