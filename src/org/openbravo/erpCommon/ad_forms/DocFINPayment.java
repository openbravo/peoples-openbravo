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
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;

public class DocFINPayment extends AcctServer {

  private static final long serialVersionUID = 1L;
  static Logger log4jDocFINPayment = Logger.getLogger(DocFINPayment.class);

  String SeqNo = "0";

  public DocFINPayment() {
  }

  public DocFINPayment(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("PaymentDate");
    Amounts[0] = data[0].getField("AMOUNT");
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  public FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Id);
    List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
    if (paymentDetails == null)
      return null;

    FieldProviderFactory[] data = new FieldProviderFactory[paymentDetails.size()];
    boolean wasAdministrator = OBContext.getOBContext().setInAdministratorMode(true);
    for (int i = 0; i < data.length; i++) {
      data[i] = new FieldProviderFactory(new HashMap());
      FieldProviderFactory.setField(data[i], "AD_Client_ID", paymentDetails.get(i).getClient()
          .getId());
      FieldProviderFactory.setField(data[i], "AD_Org_ID", paymentDetails.get(i).getOrganization()
          .getId());
      FieldProviderFactory
          .setField(data[i], "FIN_Payment_Detail_ID", paymentDetails.get(i).getId());
      FieldProviderFactory
          .setField(data[i], "Amount", paymentDetails.get(i).getAmount().toString());
      FieldProviderFactory.setField(data[i], "isprepayment",
          paymentDetails.get(i).isPrepayment() ? "Y" : "N");
      FieldProviderFactory.setField(data[i], "WriteOffAmt", paymentDetails.get(i)
          .getWriteoffAmount().toString());
      FieldProviderFactory.setField(data[i], "C_GLItem_ID",
          paymentDetails.get(i).getGLItem() != null ? paymentDetails.get(i).getGLItem().getId()
              : "");
      FieldProviderFactory
          .setField(data[i], "Refund", paymentDetails.get(i).isRefund() ? "Y" : "N");
      FieldProviderFactory.setField(data[i], "isprepayment",
          paymentDetails.get(i).isPrepayment() ? "Y" : "N");
    }
    OBContext.getOBContext().setInAdministratorMode(wasAdministrator);
    return data;
  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    FieldProviderFactory[] data = loadLinesFieldProvider(Record_ID);
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].getField("FIN_Payment_Detail_ID");
      DocLine_FINPayment docLine = new DocLine_FINPayment(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setAmount(data[i].getField("Amount"));
      docLine.setIsPrepayment(data[i].getField("isprepayment"));
      list.add(docLine);
    }
    // Return Array
    DocLine_FINPayment[] dl = new DocLine_FINPayment[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    boolean wasAdministrator = OBContext.getOBContext().setInAdministratorMode(true);
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
        DocFINPaymentTemplate newTemplate = (DocFINPaymentTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4jDocFINPayment.error("Error while creating new instance for DocFINPaymentTemplate - "
            + e);
      }
    }
    OBContext.getOBContext().setInAdministratorMode(wasAdministrator);
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_FINPayment line = (DocLine_FINPayment) p_lines[i];

      boolean isReceipt = DocumentType.equals("ARR");
      boolean isPrepayment = line.getIsPrepayment().equals("Y");

      fact.createLine(line, getAccountBPartner(
          (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals("")) ? this.C_BPartner_ID
              : line.m_C_BPartner_ID, as, isReceipt, isPrepayment, conn), C_Currency_ID,
          (isReceipt ? "" : line.getAmount()), (isReceipt ? line.getAmount() : ""),
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Record_ID);
      fact.createLine(line, getAccount(conn, payment.getAccount(), as, isReceipt), C_Currency_ID,
          (isReceipt ? line.getAmount() : ""), (isReceipt ? "" : line.getAmount()),
          Fact_Acct_Group_ID, "999999", DocumentType, conn);

      if (line.WriteOffAmt != null && !line.WriteOffAmt.equals("") && !line.WriteOffAmt.equals("0")) {
        fact.createLine(line, getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn),
            C_Currency_ID, (isReceipt ? line.WriteOffAmt : ""),
            (isReceipt ? "" : line.WriteOffAmt), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      }

    }

    SeqNo = "0";
    return fact;
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  public BigDecimal getBalance() {
    return null;
  }

  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    // Checks if this step is configured to generate accounting for the selected financial account
    boolean confirmation = false;
    boolean wasAdministrator = OBContext.getOBContext().setInAdministratorMode(true);
    try {
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strRecordId);
      List<FIN_FinancialAccountAccounting> accounts = payment.getAccount()
          .getFINFinancialAccountAcctList();
      for (FIN_FinancialAccountAccounting account : accounts) {
        if ((payment.isReceipt() && account.getReceivePaymentAccount() != null)
            || (!payment.isReceipt() && account.getMakePaymentAccount() != null))
          confirmation = true;
      }
    } finally {
      OBContext.getOBContext().setInAdministratorMode(wasAdministrator);
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Id);
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    data[0] = new FieldProviderFactory(new HashMap());
    FieldProviderFactory.setField(data[0], "AD_Client_ID", payment.getClient().getId());
    FieldProviderFactory.setField(data[0], "AD_Org_ID", payment.getOrganization().getId());
    FieldProviderFactory.setField(data[0], "C_BPartner_ID",
        payment.getBusinessPartner() != null ? payment.getBusinessPartner().getId() : "");
    FieldProviderFactory.setField(data[0], "DocumentNo", payment.getDocumentNo());
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    FieldProviderFactory.setField(data[0], "PaymentDate", outputFormat.format(payment
        .getPaymentDate()));
    FieldProviderFactory.setField(data[0], "C_DocType_ID", payment.getDocumentType().getId());
    FieldProviderFactory.setField(data[0], "C_Currency_ID", payment.getCurrency().getId());
    FieldProviderFactory.setField(data[0], "Amount", payment.getAmount().toString());
    FieldProviderFactory.setField(data[0], "WriteOffAmt", payment.getWriteoffAmount().toString());
    FieldProviderFactory.setField(data[0], "Description", payment.getDescription());
    FieldProviderFactory.setField(data[0], "Posted", payment.getPosted());
    FieldProviderFactory.setField(data[0], "Processed", payment.isProcessed() ? "Y" : "N");
    FieldProviderFactory.setField(data[0], "Processing", payment.isProcessNow() ? "Y" : "N");
    setObjectFieldProvider(data);
  }

  public Account getAccount(ConnectionProvider conn, FIN_FinancialAccount finAccount,
      AcctSchema as, boolean bIsReceipt) throws ServletException {
    boolean wasAdministrator = OBContext.getOBContext().setInAdministratorMode(true);
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
      OBContext.getOBContext().setInAdministratorMode(wasAdministrator);
    }
    return account;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public static Logger getLog4jDocAccDefPlan() {
    return log4jDocFINPayment;
  }

  public static void setLog4jDocAccDefPlan(Logger log4jDocAccDefPlan) {
    DocFINPayment.log4jDocFINPayment = log4jDocAccDefPlan;
  }

}