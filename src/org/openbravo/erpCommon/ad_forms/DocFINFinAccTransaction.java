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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class DocFINFinAccTransaction extends AcctServer {
  /** Transaction type - Financial Account */
  public static final String TRXTYPE_BPDeposit = "BPD";
  public static final String TRXTYPE_BPWithdrawal = "BPW";
  public static final String TRXTYPE_BankFee = "BF";
  BigDecimal usedCredit = ZERO;
  BigDecimal generatedCredit = ZERO;

  private static final long serialVersionUID = 1L;
  private static final Logger log4j = Logger.getLogger(DocFINFinAccTransaction.class);

  String SeqNo = "0";

  public DocFINFinAccTransaction() {
  }

  public DocFINFinAccTransaction(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_FinAccTransaction;
    DateDoc = data[0].getField("trxdate");
    BigDecimal paymentAmount = "".equals(data[0].getField("PaymentAmount")) ? ZERO
        : new BigDecimal(data[0].getField("PaymentAmount"));
    BigDecimal depositAmount = "".equals(data[0].getField("DepositAmount")) ? ZERO
        : new BigDecimal(data[0].getField("DepositAmount"));
    usedCredit = "".equals(data[0].getField("UsedCredit")) ? ZERO : new BigDecimal(data[0]
        .getField("UsedCredit"));
    generatedCredit = "".equals(data[0].getField("GeneratedCredit")) ? ZERO : new BigDecimal(
        data[0].getField("GeneratedCredit"));
    Amounts[AMTTYPE_Gross] = depositAmount.subtract(paymentAmount).toString();
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  public FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class, Id);
    FIN_Payment payment = transaction.getFinPayment();
    // If payment exists the payment details are loaded, if not the GLItem info is loaded
    if (payment != null)
      return loadLinesPaymentDetailsFieldProvider(transaction);
    else
      return loadLinesGLItemFieldProvider(transaction);
  }

  public FieldProviderFactory[] loadLinesPaymentDetailsFieldProvider(
      FIN_FinaccTransaction transaction) {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class,
        transaction.getFinPayment().getId());
    List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
    FieldProviderFactory[] data = new FieldProviderFactory[paymentDetails.size()];
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        if (!getPaymentConfirmation(payment))
          continue;
        // Details refunded used credit are excluded as the entry will be created using the credit
        // used
        if (paymentDetails.get(i).isRefund() && paymentDetails.get(i).isPrepayment())
          continue;
        data[i] = new FieldProviderFactory(new HashMap());
        FieldProviderFactory.setField(data[i], "FIN_Finacc_Transaction_ID", transaction.getId());
        FieldProviderFactory.setField(data[i], "AD_Client_ID", paymentDetails.get(i).getClient()
            .getId());
        FieldProviderFactory.setField(data[i], "AD_Org_ID", paymentDetails.get(i).getOrganization()
            .getId());
        FieldProviderFactory.setField(data[i], "FIN_Payment_Detail_ID", paymentDetails.get(i)
            .getId());
        FieldProviderFactory.setField(data[i], "FIN_Payment_ID", payment.getId());
        FieldProviderFactory.setField(data[i], "DepositAmount", transaction.getDepositAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "PaymentAmount", transaction.getPaymentAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "Amount", paymentDetails.get(i).getAmount()
            .toString());
        FieldProviderFactory.setField(data[i], "isprepayment",
            paymentDetails.get(i).isPrepayment() ? "Y" : "N");
        FieldProviderFactory.setField(data[i], "WriteOffAmt", paymentDetails.get(i)
            .getWriteoffAmount().toString());
        FieldProviderFactory.setField(data[i], "cGlItemId",
            paymentDetails.get(i).getGLItem() != null ? paymentDetails.get(i).getGLItem().getId()
                : "");
        FieldProviderFactory.setField(data[i], "cBpartnerId", payment.getBusinessPartner().getId());
        FieldProviderFactory.setField(data[i], "Refund", paymentDetails.get(i).isRefund() ? "Y"
            : "N");
        FieldProviderFactory.setField(data[i], "adOrgId", transaction.getOrganization().getId());
        FieldProviderFactory.setField(data[i], "cGlItemId",
            transaction.getGLItem() != null ? transaction.getGLItem().getId() : data[i]
                .getField("cGlItemId"));
        FieldProviderFactory.setField(data[i], "description", transaction.getDescription());
        FieldProviderFactory.setField(data[i], "cCurrencyId", transaction.getCurrency().getId());
        if (transaction.getActivity() != null)
          FieldProviderFactory.setField(data[i], "cActivityId", transaction.getActivity().getId());
        if (transaction.getProject() != null)
          FieldProviderFactory.setField(data[i], "cProjectId", transaction.getProject().getId());
        if (transaction.getSalesCampaign() != null)
          FieldProviderFactory.setField(data[i], "cCampaignId", transaction.getSalesCampaign()
              .getId());
        FieldProviderFactory.setField(data[i], "lineno", transaction.getLineNo().toString());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  public FieldProviderFactory[] loadLinesGLItemFieldProvider(FIN_FinaccTransaction transaction) {
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(new HashMap());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "adOrgId", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "cGlItemId",
          transaction.getGLItem() != null ? transaction.getGLItem().getId() : "");
      FieldProviderFactory.setField(data[0], "DepositAmount", transaction.getDepositAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "PaymentAmount", transaction.getPaymentAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "description", transaction.getDescription());
      FieldProviderFactory.setField(data[0], "cCurrencyId", transaction.getCurrency().getId());
      FieldProviderFactory
          .setField(data[0], "cBpartnerId", (transaction.getFinPayment() == null || transaction
              .getFinPayment().getBusinessPartner() == null) ? "" : transaction.getFinPayment()
              .getBusinessPartner().getId());
      if (transaction.getActivity() != null)
        FieldProviderFactory.setField(data[0], "cActivityId", transaction.getActivity().getId());
      if (transaction.getProject() != null)
        FieldProviderFactory.setField(data[0], "cProjectId", transaction.getProject().getId());
      if (transaction.getSalesCampaign() != null)
        FieldProviderFactory.setField(data[0], "cCampaignId", transaction.getSalesCampaign()
            .getId());
      FieldProviderFactory.setField(data[0], "lineno", transaction.getLineNo().toString());
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    FieldProviderFactory[] data = loadLinesFieldProvider(Record_ID);
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      if (data[i] == null)
        continue;
      String Line_ID = data[i].getField("FIN_Finacc_Transaction_ID");
      DocLine_FINFinAccTransaction docLine = new DocLine_FINFinAccTransaction(DocumentType,
          Record_ID, Line_ID);
      String strPaymentId = data[i].getField("FIN_Payment_ID");
      if (strPaymentId != null && !strPaymentId.equals(""))
        docLine.setFinPaymentId(strPaymentId);
      docLine.m_Record_Id2 = strPaymentId;
      docLine.setIsPrepayment(data[i].getField("isprepayment"));
      docLine.setCGlItemId(data[i].getField("cGlItemId"));
      docLine.setPaymentAmount(data[i].getField("PaymentAmount"));
      docLine.setDepositAmount(data[i].getField("DepositAmount"));
      docLine.setWriteOffAmt(data[i].getField("WriteOffAmt"));
      docLine.setAmount(data[i].getField("Amount"));
      docLine.loadAttributes(data[i], this);
      list.add(docLine);
    }
    // Return Array
    DocLine_FINFinAccTransaction[] dl = new DocLine_FINFinAccTransaction[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  @Override
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    OBContext.setAdminMode();
    try {
      whereClause.append(" as astdt ");
      whereClause.append(" where astdt.acctschemaTable.accountingSchema.id = '"
          + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and astdt.acctschemaTable.table.id = '" + AD_Table_ID + "'");
      whereClause.append(" and astdt.documentCategory = '" + DocumentType + "'");

      final OBQuery<AcctSchemaTableDocType> obqParameters = OBDal.getInstance().createQuery(
          AcctSchemaTableDocType.class, whereClause.toString());
      final List<AcctSchemaTableDocType> acctSchemaTableDocTypes = obqParameters.list();

      if (acctSchemaTableDocTypes != null && acctSchemaTableDocTypes.size() > 0)
        strClassname = acctSchemaTableDocTypes.get(0).getCreatefactTemplate().getClassname();

      if (strClassname.equals("")) {
        final StringBuilder whereClause2 = new StringBuilder();

        whereClause2.append(" as ast ");
        whereClause2.append(" where ast.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause2.append(" and ast.table.id = '" + AD_Table_ID + "'");

        final OBQuery<AcctSchemaTable> obqParameters2 = OBDal.getInstance().createQuery(
            AcctSchemaTable.class, whereClause2.toString());
        final List<AcctSchemaTable> acctSchemaTables = obqParameters2.list();
        if (acctSchemaTables != null && acctSchemaTables.size() > 0
            && acctSchemaTables.get(0).getCreatefactTemplate() != null)
          strClassname = acctSchemaTables.get(0).getCreatefactTemplate().getClassname();
      }
      if (!strClassname.equals("")) {
        try {
          DocFINFinAccTransactionTemplate newTemplate = (DocFINFinAccTransactionTemplate) Class
              .forName(strClassname).newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocFINFinAccTransactionTemplate - "
              + e);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        Record_ID);
    // 3 Scenarios: 1st Bank fee 2nd payment related transaction 3rd glitem transaction
    if (TRXTYPE_BankFee.equals(transaction.getTransactionType()))
      fact = createFactFee(transaction, as, conn, fact);
    else if (transaction.getFinPayment() != null)
      fact = createFactPaymentDetails(as, conn, fact);
    else
      fact = createFactGLItem(as, conn, fact);
    return fact;
  }

  /*
   * Creates accounting related to a bank fee transaction
   */
  public Fact createFactFee(FIN_FinaccTransaction transaction, AcctSchema as,
      ConnectionProvider conn, Fact fact) throws ServletException {
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_FINFinAccTransaction line = (DocLine_FINFinAccTransaction) p_lines[i];
      fact.createLine(line, getAccountFee(as, transaction.getAccount(), conn), C_Currency_ID, line
          .getPaymentAmount(), line.getDepositAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
      fact.createLine(line, getWithdrawalAccount(as, null, transaction.getAccount(), conn),
          C_Currency_ID, line.getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    }
    SeqNo = "0";
    return fact;
  }

  /*
   * Creates accounting related to a bank fee transaction
   */
  @Deprecated
  public Fact createFactFee(DocLine_FINFinAccTransaction docline,
      FIN_FinaccTransaction transaction, AcctSchema as, ConnectionProvider conn, Fact fact)
      throws ServletException {
    return createFactFee(transaction, as, conn, fact);
  }

  public Fact createFactPaymentDetails(AcctSchema as, ConnectionProvider conn, Fact fact)
      throws ServletException {
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        Record_ID);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_FINFinAccTransaction line = (DocLine_FINFinAccTransaction) p_lines[i];
      boolean isPrepayment = "Y".equals(line.getIsPrepayment());
      boolean isReceipt = transaction.getFinPayment().isReceipt();
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, line.getFinPaymentId());
      if (!getDocumentPaymentConfirmation(payment)) {
        fact.createLine(line, getAccountBPartner(
            (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals("")) ? this.C_BPartner_ID
                : line.m_C_BPartner_ID, as, isReceipt, isPrepayment, conn), C_Currency_ID,
            !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (payment.getWriteoffAmount() != null
            && payment.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
          fact.createLine(line, getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn),
              C_Currency_ID, (isReceipt ? line.getWriteOffAmt() : ""), (isReceipt ? "" : line
                  .getWriteOffAmt()), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        }
      } else
        fact.createLine(line, getAccountPayment(conn, payment.getPaymentMethod(), payment
            .getAccount(), as, isReceipt), C_Currency_ID, !isReceipt ? line.getAmount() : "",
            isReceipt ? line.getAmount() : "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
            conn);
    }
    if (!getDocumentPaymentConfirmation(transaction.getFinPayment())) {
      // Pre-payment is consumed when Used Credit Amount not equals Zero. When consuming Credit no
      // credit is generated
      if (transaction.getFinPayment().getUsedCredit().compareTo(ZERO) != 0
          && transaction.getFinPayment().getGeneratedCredit().compareTo(ZERO) == 0) {
        fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, transaction.getFinPayment()
            .isReceipt(), true, conn), C_Currency_ID,
            (transaction.getFinPayment().isReceipt() ? transaction.getFinPayment().getUsedCredit()
                .toString() : ""), (transaction.getFinPayment().isReceipt() ? "" : transaction
                .getFinPayment().getUsedCredit().toString()), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }
    }
    fact.createLine(null, getAccountUponDepositWithdrawal(conn, transaction.getFinPayment()
        .getPaymentMethod(), transaction.getFinPayment().getAccount(), as, transaction
        .getFinPayment().isReceipt()), C_Currency_ID,
        transaction.getFinPayment().isReceipt() ? Amounts[AMTTYPE_Gross].toString() : "",
        !transaction.getFinPayment().isReceipt() ? Amounts[AMTTYPE_Gross].toString() : "",
        Fact_Acct_Group_ID, "999999", DocumentType, conn);

    SeqNo = "0";
    return fact;
  }

  @Deprecated
  public Fact createFactPaymentDetails(DocLine_FINFinAccTransaction docline, AcctSchema as,
      ConnectionProvider conn, Fact fact) throws ServletException {
    return createFactPaymentDetails(as, conn, fact);
  }

  /*
   * Creates the accounting for a transaction related directly with a GLItem
   */
  public Fact createFactGLItem(AcctSchema as, ConnectionProvider conn, Fact fact)
      throws ServletException {
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_FINFinAccTransaction line = (DocLine_FINFinAccTransaction) p_lines[i];
      BigDecimal paymentAmount = new BigDecimal(line.getPaymentAmount());
      BigDecimal depositAmount = new BigDecimal(line.getDepositAmount());
      boolean isReceipt = paymentAmount.compareTo(depositAmount) < 0;
      String Fact_Acct_Group_ID = SequenceIdData.getUUID();
      if (!"".equals(line.getCGlItemId()))
        fact.createLine(line, getAccountGLItem(OBDal.getInstance().get(GLItem.class,
            line.getCGlItemId()), as, isReceipt, conn), C_Currency_ID, line.getPaymentAmount(),
            line.getDepositAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          Record_ID);
      fact.createLine(line, getAccountUponDepositWithdrawal(conn,
          transaction.getFinPayment() != null ? transaction.getFinPayment().getPaymentMethod()
              : null, transaction.getAccount(), as, isReceipt), C_Currency_ID, line
          .getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID, "999999", DocumentType,
          conn);
    }
    SeqNo = "0";
    return fact;
  }

  /*
   * Creates the accounting for a transaction related directly with a GLItem
   */
  @Deprecated
  public Fact createFactGLItem(DocLine_FINFinAccTransaction docline, AcctSchema as,
      ConnectionProvider conn, Fact fact) throws ServletException {
    return createFactGLItem(as, conn, fact);
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  /**
   * Get Source Currency Balance - subtracts line amounts from total + usedCredit - no rounding
   * 
   * @return positive amount, if total is bigger than lines
   */
  @Override
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    if (usedCredit.compareTo(ZERO) != 0 && generatedCredit.compareTo(ZERO) == 0)
      retValue.add(usedCredit);
    sb.append(retValue);
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      BigDecimal lineBalance = new BigDecimal(
          ((DocLine_FINFinAccTransaction) p_lines[i]).DepositAmount);
      lineBalance = lineBalance.subtract(new BigDecimal(
          ((DocLine_FINFinAccTransaction) p_lines[i]).PaymentAmount));
      retValue = retValue.subtract(lineBalance);
      sb.append("-").append(lineBalance);
    }
    sb.append("]");
    //
    log4j.debug(" Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  /*
   * Checks if Accounting for payments are enabled for the given payment
   */
  public boolean getDocumentPaymentConfirmation(FIN_Payment payment) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment
          .getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      List<FIN_FinancialAccountAccounting> accounts = payment.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinancialAccountAccounting account : accounts) {
        if (payment.isReceipt()) {
          if (("INT").equals(lines.get(0).getUponReceiptUse())
              && account.getInTransitPaymentAccountIN() != null)
            confirmation = true;
          else if (("DEP").equals(lines.get(0).getUponReceiptUse())
              && account.getDepositAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponReceiptUse())
              && account.getClearedPaymentAccount() != null)
            confirmation = true;
        } else {
          if (("INT").equals(lines.get(0).getUponPaymentUse())
              && account.getFINOutIntransitAcct() != null)
            confirmation = true;
          else if (("WIT").equals(lines.get(0).getUponPaymentUse())
              && account.getWithdrawalAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponPaymentUse())
              && account.getClearedPaymentAccountOUT() != null)
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

  /*
   * Checks if Accounting for payments in transactions are enabled for the given paymentis
   */
  public boolean getPaymentConfirmation(FIN_Payment payment) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment
          .getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      List<FIN_FinancialAccountAccounting> accounts = payment.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinancialAccountAccounting account : accounts) {
        if (confirmation)
          return confirmation;
        if (payment.isReceipt()) {
          if (("INT").equals(lines.get(0).getUponDepositUse())
              && account.getInTransitPaymentAccountIN() != null)
            confirmation = true;
          else if (("DEP").equals(lines.get(0).getUponDepositUse())
              && account.getDepositAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponDepositUse())
              && account.getClearedPaymentAccount() != null)
            confirmation = true;
        } else {
          if (("INT").equals(lines.get(0).getUponWithdrawalUse())
              && account.getFINOutIntransitAcct() != null)
            confirmation = true;
          else if (("WIT").equals(lines.get(0).getUponWithdrawalUse())
              && account.getWithdrawalAccount() != null)
            confirmation = true;
          else if (("CLE").equals(lines.get(0).getUponWithdrawalUse())
              && account.getClearedPaymentAccountOUT() != null)
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

  /*
   * Checks if this step is configured to generate accounting for the selected financial account
   */
  @Override
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          strRecordId);
      List<FIN_FinancialAccountAccounting> accounts = transaction.getAccount()
          .getFINFinancialAccountAcctList();
      FIN_Payment payment = transaction.getFinPayment();
      if (payment != null) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
        obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment
            .getPaymentMethod()));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if (payment.isReceipt()) {
            if (("INT").equals(lines.get(0).getUponDepositUse())
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (("DEP").equals(lines.get(0).getUponDepositUse())
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponDepositUse())
                && account.getClearedPaymentAccount() != null)
              confirmation = true;
          } else {
            if (("INT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (("WIT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponWithdrawalUse())
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
        }
      } else {
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if ((TRXTYPE_BPDeposit.equals(transaction.getTransactionType()) && account
              .getDepositAccount() != null)
              || (TRXTYPE_BPWithdrawal.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null)
              || (TRXTYPE_BankFee.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null))
            confirmation = true;
        }
      }
    } catch (Exception e) {
      setStatus(STATUS_DocumentDisabled);
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  /*
   * Retrieves Account for deposit / Withdrawal for the given payment method + Financial Account
   */
  public Account getAccount(ConnectionProvider conn, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, AcctSchema as, boolean bIsReceipt) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      if (paymentMethod != null) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAccount));
        obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        if (bIsReceipt) {
          account = getAccount(conn, lines.get(0).getUponDepositUse(), accountList.get(0),
              bIsReceipt);
        } else {
          account = getAccount(conn, lines.get(0).getUponWithdrawalUse(), accountList.get(0),
              bIsReceipt);
        }
      } else {
        if (bIsReceipt) {
          account = new Account(conn, accountList.get(0).getDepositAccount().getId());
        } else {
          account = new Account(conn, accountList.get(0).getWithdrawalAccount().getId());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class, Id);

    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(new HashMap());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "AD_Org_ID", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "DepositAmount", transaction.getDepositAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "PaymentAmount", transaction.getPaymentAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "description", transaction.getDescription());
      FieldProviderFactory.setField(data[0], "C_Currency_ID", transaction.getCurrency().getId());
      FieldProviderFactory.setField(data[0], "C_GLItem_ID",
          transaction.getGLItem() != null ? transaction.getGLItem().getId() : "");
      FieldProviderFactory.setField(data[0], "C_Activity_ID",
          transaction.getActivity() != null ? transaction.getActivity().getId() : "");
      FieldProviderFactory.setField(data[0], "C_Project_ID",
          transaction.getProject() != null ? transaction.getProject().getId() : "");
      FieldProviderFactory.setField(data[0], "C_Campaign_ID",
          transaction.getSalesCampaign() != null ? transaction.getSalesCampaign().getId() : "");
      FieldProviderFactory.setField(data[0], "lineno", transaction.getLineNo().toString());
      // This lines can be uncommented when User1 and User2 are implemented
      // FieldProviderFactory.setField(data[0], "User1_ID", transaction.getNdDimension().getId());
      // FieldProviderFactory.setField(data[0], "User2_ID", transaction.getNdDimension().getId());
      FieldProviderFactory.setField(data[0], "FIN_Payment_ID",
          transaction.getFinPayment() != null ? transaction.getFinPayment().getId() : "");
      FieldProviderFactory.setField(data[0], "C_BPartner_ID",
          transaction.getFinPayment() != null ? transaction.getFinPayment().getBusinessPartner()
              .getId() : "");
      FieldProviderFactory.setField(data[0], "UsedCredit",
          transaction.getFinPayment() != null ? transaction.getFinPayment().getUsedCredit()
              .toString() : "");
      FieldProviderFactory.setField(data[0], "GeneratedCredit",
          transaction.getFinPayment() != null ? transaction.getFinPayment().getGeneratedCredit()
              .toString() : "");
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
          "dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "DateAcct", outputFormat.format(transaction
          .getDateAcct()));
      FieldProviderFactory.setField(data[0], "trxdate", outputFormat.format(transaction
          .getTransactionDate()));
      FieldProviderFactory.setField(data[0], "Posted", transaction.getPosted());
      FieldProviderFactory.setField(data[0], "Processed", transaction.isProcessed() ? "Y" : "N");
      FieldProviderFactory.setField(data[0], "Processing", transaction.isProcessNow() ? "Y" : "N");
    } finally {
      OBContext.restorePreviousMode();
    }
    setObjectFieldProvider(data);
  }

  public Account getWithdrawalAccount(AcctSchema as, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, ConnectionProvider conn) throws ServletException {
    return getAccountUponDepositWithdrawal(conn, paymentMethod, finAccount, as, false);
  }

  public Account getDepositAccount(AcctSchema as, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, ConnectionProvider conn) throws ServletException {
    return getAccountUponDepositWithdrawal(conn, paymentMethod, finAccount, as, true);
  }

  public Account getAccountUponDepositWithdrawal(ConnectionProvider conn,
      FIN_PaymentMethod paymentMethod, FIN_FinancialAccount finAccount, AcctSchema as,
      boolean bIsReceipt) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      if (paymentMethod != null) {
        OBCriteria<FinAccPaymentMethod> accPaymentMethod = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        accPaymentMethod.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAccount));
        accPaymentMethod.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
            paymentMethod));
        accPaymentMethod.setFilterOnReadableClients(false);
        accPaymentMethod.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = accPaymentMethod.list();
        if (bIsReceipt)
          account = getAccount(conn, lines.get(0).getUponDepositUse(), accountList.get(0),
              bIsReceipt);
        else
          account = getAccount(conn, lines.get(0).getUponWithdrawalUse(), accountList.get(0),
              bIsReceipt);
      } else {
        if (bIsReceipt)
          account = new Account(conn, accountList.get(0).getDepositAccount().getId());
        else
          account = new Account(conn, accountList.get(0).getWithdrawalAccount().getId());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getAccountPayment(ConnectionProvider conn, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, AcctSchema as, boolean bIsReceipt) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      OBCriteria<FinAccPaymentMethod> accPaymentMethod = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      accPaymentMethod.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAccount));
      accPaymentMethod
          .add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
      accPaymentMethod.setFilterOnReadableClients(false);
      accPaymentMethod.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = accPaymentMethod.list();
      if (bIsReceipt)
        account = getAccount(conn, lines.get(0).getUponReceiptUse(), accountList.get(0), bIsReceipt);
      else
        account = getAccount(conn, lines.get(0).getUponPaymentUse(), accountList.get(0), bIsReceipt);
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getAccountReconciliation(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      account = new Account(conn, accountList.get(0).getDebitAccount().getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

}