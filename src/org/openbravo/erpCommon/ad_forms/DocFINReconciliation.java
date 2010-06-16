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
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class DocFINReconciliation extends AcctServer {
  /** Transaction type - Financial Account */
  public static final String TRXTYPE_BPDeposit = "BPD";
  public static final String TRXTYPE_BPWithdrawal = "BPW";
  public static final String TRXTYPE_BankFee = "BF";

  private static final long serialVersionUID = 1L;
  private static final Logger log4j = Logger.getLogger(DocFINReconciliation.class);

  String SeqNo = "0";

  public DocFINReconciliation() {
  }

  public DocFINReconciliation(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_Reconciliation;
    DateDoc = data[0].getField("statementDate");
    C_DocType_ID = data[0].getField("C_Doctype_ID");
    DocumentNo = data[0].getField("DocumentNo");
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          Record_ID);
      Amounts[0] = reconciliation.getEndingBalance().subtract(reconciliation.getStartingbalance())
          .toString();
    } finally {
      OBContext.restorePreviousMode();
    }
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  public FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FieldProviderFactory[] linesInfo = null;
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, Id);
      List<FIN_FinaccTransaction> transactions = getTransactionList(reconciliation);
      for (FIN_FinaccTransaction transaction : transactions) {
        FIN_Payment payment = transaction.getFinPayment();
        // If payment exists the payment details are loaded, if not the GLItem info is loaded,
        // finally fee is loaded
        if (payment != null)
          linesInfo = add(linesInfo, loadLinesPaymentDetailsFieldProvider(transaction));
        else if (transaction.getGLItem() != null)
          linesInfo = add(linesInfo, loadLinesGLItemFieldProvider(transaction));
        else
          linesInfo = add(linesInfo, loadLinesFeeFieldProvider(transaction));
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return linesInfo;
  }

  FieldProviderFactory[] add(FieldProviderFactory[] one, FieldProviderFactory[] two) {
    if (one == null)
      return two;
    if (two == null)
      return one;
    FieldProviderFactory[] result = new FieldProviderFactory[one.length + two.length];
    for (int i = 0; i < one.length; i++) {
      if (one[i] != null)
        result[i] = one[i];
    }
    for (int i = 0; i < two.length; i++) {
      if (two[i] != null)
        result[i + one.length] = two[i];
    }
    return result;
  }

  public List<FIN_FinaccTransaction> getTransactionList(FIN_Reconciliation reconciliation) {
    OBContext.setAdminMode();
    List<FIN_FinaccTransaction> transactions = null;
    try {
      OBCriteria<FIN_FinaccTransaction> trans = OBDal.getInstance().createCriteria(
          FIN_FinaccTransaction.class);
      trans.add(Expression.eq(FIN_FinaccTransaction.PROPERTY_RECONCILIATION, reconciliation));
      trans.setFilterOnReadableClients(false);
      trans.setFilterOnReadableOrganization(false);
      transactions = trans.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return transactions;
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
        data[i] = new FieldProviderFactory(new HashMap());
        FieldProviderFactory.setField(data[i], "FIN_Reconciliation_ID", transaction
            .getReconciliation().getId());
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
      FieldProviderFactory.setField(data[0], "FIN_Reconciliation_ID", transaction
          .getReconciliation().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "adOrgId", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "cGlItemId", transaction.getGLItem().getId());
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

  public FieldProviderFactory[] loadLinesFeeFieldProvider(FIN_FinaccTransaction transaction) {
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(new HashMap());
      FieldProviderFactory.setField(data[0], "FIN_Reconciliation_ID", transaction
          .getReconciliation().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", transaction.getId());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", transaction.getClient().getId());
      FieldProviderFactory.setField(data[0], "adOrgId", transaction.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "DepositAmount", transaction.getDepositAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "PaymentAmount", transaction.getPaymentAmount()
          .toString());
      FieldProviderFactory.setField(data[0], "description", transaction.getDescription());
      FieldProviderFactory.setField(data[0], "cCurrencyId", transaction.getCurrency().getId());
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
    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].getField("FIN_Finacc_Transaction_ID");
        DocLine_FINReconciliation docLine = new DocLine_FINReconciliation(DocumentType, Record_ID,
            Line_ID);
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
        docLine.setFinFinAccTransactionId(data[i].getField("FIN_Finacc_Transaction_ID"));
        docLine.loadAttributes(data[i], this);
        list.add(docLine);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    // Return Array
    DocLine_FINReconciliation[] dl = new DocLine_FINReconciliation[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  @Override
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    Fact fact = new Fact(this, as, Fact.POST_Actual);
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
          DocFINReconciliationTemplate newTemplate = (DocFINReconciliationTemplate) Class.forName(
              strClassname).newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocFINReconciliationTemplate - " + e);
        }
      }
      String Fact_Acct_Group_ID = SequenceIdData.getUUID();
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_FINReconciliation line = (DocLine_FINReconciliation) p_lines[i];
        FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
            line.getFinFinAccTransactionId());
        // 3 Scenarios: 1st Bank fee 2nd payment related transaction 3rd GL item transaction
        if (transaction.getTransactionType().equals(TRXTYPE_BankFee))
          fact = createFactFee(line, as, conn, fact, Fact_Acct_Group_ID);
        else if (!"".equals(line.getFinPaymentId()))
          fact = createFactPaymentDetails(line, as, conn, fact, Fact_Acct_Group_ID);
        else
          fact = createFactGLItem(line, as, conn, fact, Fact_Acct_Group_ID);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return fact;
  }

  /*
   * Creates the accounting for a bank Fee
   */
  public Fact createFactFee(DocLine_FINReconciliation line, AcctSchema as, ConnectionProvider conn,
      Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction))
      fact.createLine(line, getWithdrawalAccount(as, transaction.getAccount(), conn),
          C_Currency_ID, line.getPaymentAmount(), line.getDepositAmount(), Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    else
      fact.createLine(line, getAccountFee(as, transaction.getAccount(), conn), C_Currency_ID, line
          .getPaymentAmount(), line.getDepositAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    fact.createLine(line, getClearOutAccount(as, transaction.getAccount(), conn), C_Currency_ID,
        line.getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
        DocumentType, conn);
    SeqNo = "0";
    return fact;
  }

  public Fact createFactPaymentDetails(DocLine_FINReconciliation line, AcctSchema as,
      ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    boolean isPrepayment = "Y".equals(line.getIsPrepayment());
    BigDecimal paymentAmount = new BigDecimal(line.getPaymentAmount());
    BigDecimal depositAmount = new BigDecimal(line.getDepositAmount());
    boolean isReceipt = paymentAmount.compareTo(depositAmount) < 0;
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, line.getFinPaymentId());
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction))
      fact.createLine(line, getAccountTransactionPayment(conn, payment, as), C_Currency_ID,
          !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    else if (!getDocumentPaymentConfirmation(payment))
      fact.createLine(line, getAccountBPartner(
          (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals("")) ? this.C_BPartner_ID
              : line.m_C_BPartner_ID, as, isReceipt, isPrepayment, conn), C_Currency_ID,
          !isReceipt ? line.getAmount() : "", isReceipt ? line.getAmount() : "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    else
      fact.createLine(line, getAccountPayment(conn, payment, as), C_Currency_ID, !isReceipt ? line
          .getAmount() : "", isReceipt ? line.getAmount() : "", Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    fact.createLine(line, getAccount(conn, payment.getAccount(), as, isReceipt), C_Currency_ID,
        isReceipt ? line.getAmount() : "", !isReceipt ? line.getAmount() : "", Fact_Acct_Group_ID,
        "999999", DocumentType, conn);

    if (payment.getWriteoffAmount() != null
        && payment.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn),
          C_Currency_ID, (isReceipt ? line.getWriteOffAmt() : ""), (isReceipt ? "" : line
              .getWriteOffAmt()), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    }
    SeqNo = "0";
    return fact;
  }

  /*
   * Creates the accounting for a transaction related directly with a GLItem
   */
  public Fact createFactGLItem(DocLine_FINReconciliation line, AcctSchema as,
      ConnectionProvider conn, Fact fact, String Fact_Acct_Group_ID) throws ServletException {
    BigDecimal paymentAmount = new BigDecimal(line.getPaymentAmount());
    BigDecimal depositAmount = new BigDecimal(line.getDepositAmount());
    boolean isReceipt = paymentAmount.compareTo(depositAmount) < 0;
    FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
        line.getFinFinAccTransactionId());
    if (getDocumentTransactionConfirmation(transaction))
      fact.createLine(line, getAccountTransaction(conn, transaction.getAccount(), as, isReceipt),
          C_Currency_ID, line.getPaymentAmount(), line.getDepositAmount(), Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
    else if (!"".equals(line.getCGlItemId()))
      fact.createLine(line, getAccountGLItem(OBDal.getInstance().get(GLItem.class,
          line.getCGlItemId()), as, isReceipt, conn), C_Currency_ID, line.getPaymentAmount(), line
          .getDepositAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
    fact.createLine(line, getAccount(conn, transaction.getAccount(), as, isReceipt), C_Currency_ID,
        line.getDepositAmount(), line.getPaymentAmount(), Fact_Acct_Group_ID, "999999",
        DocumentType, conn);
    SeqNo = "0";
    return fact;
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  @Override
  public BigDecimal getBalance() {
    return null;
  }

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
          if (lines.get(0).getUponReceiptUse().equals("INT")
              && account.getInTransitPaymentAccountIN() != null)
            confirmation = true;
          else if (lines.get(0).getUponReceiptUse().equals("DEP")
              && account.getDepositAccount() != null)
            confirmation = true;
          else if (lines.get(0).getUponReceiptUse().equals("CLE")
              && account.getClearedPaymentAccount() != null)
            confirmation = true;
        } else {
          if (lines.get(0).getUponPaymentUse().equals("INT")
              && account.getFINOutIntransitAcct() != null)
            confirmation = true;
          else if (lines.get(0).getUponPaymentUse().equals("WIT")
              && account.getWithdrawalAccount() != null)
            confirmation = true;
          else if (lines.get(0).getUponPaymentUse().equals("CLE")
              && account.getClearedPaymentAccountOUT() != null)
            confirmation = true;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

  public boolean getDocumentTransactionConfirmation(FIN_FinaccTransaction transaction) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
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
          if (payment.isReceipt()) {
            if (lines.get(0).getUponDepositUse().equals("INT")
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (lines.get(0).getUponDepositUse().equals("DEP")
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (lines.get(0).getUponDepositUse().equals("CLE")
                && account.getClearedPaymentAccount() != null)
              confirmation = true;
          } else {
            if (lines.get(0).getUponWithdrawalUse().equals("INT")
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (lines.get(0).getUponWithdrawalUse().equals("WIT")
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (lines.get(0).getUponWithdrawalUse().equals("CLE")
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
        }
      } else {
        for (FIN_FinancialAccountAccounting account : accounts) {
          if ((transaction.getTransactionType().equals(TRXTYPE_BPDeposit) && account
              .getDepositAccount() != null)
              || (transaction.getTransactionType().equals(TRXTYPE_BPWithdrawal) && account
                  .getWithdrawalAccount() != null)
              || (transaction.getTransactionType().equals(TRXTYPE_BankFee) && account
                  .getWithdrawalAccount() != null))
            confirmation = true;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  /*
   * Checks if this step (Reconciliation) is configured to generate accounting for the selected
   * financial account
   */
  @Override
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          strRecordId);
      List<FIN_FinaccTransaction> transactions = getTransactionList(reconciliation);
      List<FIN_FinancialAccountAccounting> accounts = reconciliation.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinaccTransaction transaction : transactions) {
        if (confirmation)
          break;
        FIN_Payment payment = transaction.getFinPayment();
        // If payment exists, check Payment Method + financial Account Configuration
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
              break;
            if (payment.isReceipt()) {
              if (lines.get(0).getINUponClearingUse().equals("INT")
                  && account.getInTransitPaymentAccountIN() != null)
                confirmation = true;
              else if (lines.get(0).getINUponClearingUse().equals("DEP")
                  && account.getDepositAccount() != null)
                confirmation = true;
              else if (lines.get(0).getINUponClearingUse().equals("CLE")
                  && account.getClearedPaymentAccount() != null)
                confirmation = true;
            } else {
              if (lines.get(0).getOUTUponClearingUse().equals("INT")
                  && account.getFINOutIntransitAcct() != null)
                confirmation = true;
              else if (lines.get(0).getOUTUponClearingUse().equals("WIT")
                  && account.getWithdrawalAccount() != null)
                confirmation = true;
              else if (lines.get(0).getOUTUponClearingUse().equals("CLE")
                  && account.getClearedPaymentAccountOUT() != null)
                confirmation = true;
            }
          }
        } else if (transaction.getGLItem() != null) {
          for (FIN_FinancialAccountAccounting account : accounts) {
            if (confirmation)
              break;
            if ("BPD".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccount() != null) {
              confirmation = true;
            } else if ("BPW".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              confirmation = true;
            } else if ("BF".equals(transaction.getTransactionType())
                && account.getClearedPaymentAccountOUT() != null) {
              confirmation = true;
            }
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class, Id);

    FieldProviderFactory[] data = new FieldProviderFactory[1];
    OBContext.setAdminMode();
    try {
      data[0] = new FieldProviderFactory(new HashMap());
      FieldProviderFactory.setField(data[0], "AD_Client_ID", reconciliation.getClient().getId());
      FieldProviderFactory.setField(data[0], "AD_Org_ID", reconciliation.getOrganization().getId());
      FieldProviderFactory.setField(data[0], "FIN_Finacc_Transaction_ID", reconciliation.getId());
      FieldProviderFactory.setField(data[0], "C_Currency_ID", reconciliation.getAccount()
          .getCurrency().getId());
      FieldProviderFactory.setField(data[0], "C_Doctype_ID", reconciliation.getDocumentType()
          .getId());
      FieldProviderFactory.setField(data[0], "DocumentNo", reconciliation.getDocumentNo());
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
          "dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      FieldProviderFactory.setField(data[0], "statementDate", outputFormat.format(reconciliation
          .getTransactionDate()));
      FieldProviderFactory.setField(data[0], "Posted", reconciliation.getPosted());
      FieldProviderFactory.setField(data[0], "Processed", reconciliation.isProcessed() ? "Y" : "N");
      FieldProviderFactory.setField(data[0], "Processing", reconciliation.isProcessNow() ? "Y"
          : "N");
    } finally {
      OBContext.restorePreviousMode();
    }
    setObjectFieldProvider(data);
  }

  public Account getWithdrawalAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccountTransaction(conn, finAccount, as, false);
  }

  public Account getDepositAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccountTransaction(conn, finAccount, as, true);
  }

  public Account getAccountTransaction(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
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
      if (bIsReceipt)
        account = new Account(conn, accountList.get(0).getDepositAccount().getId());
      else
        account = new Account(conn, accountList.get(0).getWithdrawalAccount().getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getAccountTransactionPayment(ConnectionProvider conn, FIN_Payment payment,
      AcctSchema as) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, payment
          .getAccount()));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment
          .getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      AccountingCombination result = null;
      if (payment.isReceipt()) {
        if (lines.get(0).getUponDepositUse().equals("INT"))
          result = accountList.get(0).getInTransitPaymentAccountIN();
        else if (lines.get(0).getUponDepositUse().equals("DEP"))
          result = accountList.get(0).getDepositAccount();
        else if (lines.get(0).getUponDepositUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccount();
      } else {
        if (lines.get(0).getUponWithdrawalUse().equals("INT"))
          result = accountList.get(0).getFINOutIntransitAcct();
        else if (lines.get(0).getUponWithdrawalUse().equals("WIT"))
          result = accountList.get(0).getWithdrawalAccount();
        else if (lines.get(0).getUponWithdrawalUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccountOUT();
      }
      if (result != null)
        account = new Account(conn, result.getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getClearOutAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccount(conn, finAccount, as, false);
  }

  public Account getClearInAccount(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    return getAccount(conn, finAccount, as, true);
  }

  public Account getAccount(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
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
      if (bIsReceipt)
        account = new Account(conn, accountList.get(0).getClearedPaymentAccount().getId());
      else
        account = new Account(conn, accountList.get(0).getClearedPaymentAccountOUT().getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getAccountPayment(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
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
      if (bIsReceipt)
        account = new Account(conn, accountList.get(0).getReceivePaymentAccount().getId());
      else
        account = new Account(conn, accountList.get(0).getMakePaymentAccount().getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  public Account getAccountPayment(ConnectionProvider conn, FIN_Payment payment, AcctSchema as)
      throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    AccountingCombination result = null;
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
      obCriteria.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, payment
          .getPaymentMethod()));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, payment
          .getAccount()));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA, OBDal
          .getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Expression.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (payment.isReceipt()) {
        if (lines.get(0).getUponReceiptUse().equals("INT"))
          result = accountList.get(0).getInTransitPaymentAccountIN();
        else if (lines.get(0).getUponReceiptUse().equals("DEP"))
          result = accountList.get(0).getDepositAccount();
        else if (lines.get(0).getUponReceiptUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccount();
      } else {
        if (lines.get(0).getUponPaymentUse().equals("INT"))
          result = accountList.get(0).getFINOutIntransitAcct();
        else if (lines.get(0).getUponPaymentUse().equals("WIT"))
          result = accountList.get(0).getWithdrawalAccount();
        else if (lines.get(0).getUponPaymentUse().equals("CLE"))
          result = accountList.get(0).getClearedPaymentAccountOUT();
      }
      if (result != null)
        account = new Account(conn, result.getId());
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

}