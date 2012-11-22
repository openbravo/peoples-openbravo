/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.payment.DoubtfulDebt;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;

public class DocDoubtfulDebt extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(DocDoubtfulDebt.class);
  private String SeqNo = "0";

  public DocDoubtfulDebt() {
  }

  public DocDoubtfulDebt(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    DoubtfulDebt dd = OBDal.getInstance().get(DoubtfulDebt.class, Id);
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    data[0] = new FieldProviderFactory(null);
    FieldProviderFactory.setField(data[0], "AD_Client_ID", dd.getClient().getId());
    FieldProviderFactory.setField(data[0], "AD_Org_ID", dd.getOrganization().getId());
    FieldProviderFactory.setField(data[0], "DocumentNo", dd.getDocumentNo());
    String strAcctDate = OBDateUtils.formatDate(dd.getAccountingDate());
    FieldProviderFactory.setField(data[0], "DateAcct", strAcctDate);
    FieldProviderFactory.setField(data[0], "DateDoc", strAcctDate);
    FieldProviderFactory.setField(data[0], "C_DocType_ID", dd.getDocumentType().getId());
    FieldProviderFactory.setField(data[0], "C_Currency_ID", dd.getCurrency().getId());
    FieldProviderFactory.setField(data[0], "Description", dd.getDescription());
    FieldProviderFactory.setField(data[0], "Posted", dd.getPosted());
    FieldProviderFactory.setField(data[0], "Processed", dd.isProcessed() ? "Y" : "N");
    FieldProviderFactory.setField(data[0], "Processing", dd.isProcessNow() ? "Y" : "N");

    // HEADER DIMENSIONS
    // Business Partner - NO
    // Product - NO
    // Project
    // FieldProviderFactory.setField(data[0], "C_Project_ID", dd.getContract() != null ? dd
    // .getContract().getId() : "");
    // FieldProviderFactory.setField(data[0], "C_Campaign_ID", "");
    // FieldProviderFactory.setField(data[0], "C_Activity_ID", "");
    // FieldProviderFactory.setField(data[0], "C_SalesRegion_ID", "");
    // FieldProviderFactory.setField(data[0], "User1_ID", "");
    // FieldProviderFactory.setField(data[0], "User2_ID", "");
    // TODO User1_ID and User2_ID

    setObjectFieldProvider(data);

  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  @Override
  public BigDecimal getBalance() {
    return BigDecimal.ZERO;
  }

  @Override
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    final Fact fact = new Fact(this, as, Fact.POST_Actual);
    final String Fact_Acct_Group_ID = SequenceIdData.getUUID();

    try {
      OBContext.setAdminMode();
      whereClause.append(" as astdt ");
      whereClause.append(" where astdt.acctschemaTable.accountingSchema.id = '"
          + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and astdt.acctschemaTable.table.id = '" + AD_Table_ID + "'");
      whereClause.append(" and astdt.documentCategory = '" + DocumentType + "'");

      final OBQuery<AcctSchemaTableDocType> obqParameters = OBDal.getInstance().createQuery(
          AcctSchemaTableDocType.class, whereClause.toString());
      final List<AcctSchemaTableDocType> acctSchemaTableDocTypes = obqParameters.list();

      if (acctSchemaTableDocTypes != null && acctSchemaTableDocTypes.size() > 0) {
        strClassname = acctSchemaTableDocTypes.get(0).getCreatefactTemplate().getClassname();
      }

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
          DocDoubtfulDebtTemplate newTemplate = (DocDoubtfulDebtTemplate) Class.forName(
              strClassname).newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocUnbilledRevenueTemplate - ", e);
        }
      }

      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        // DocLine_UnbilledRevenue line = (DocLine_UnbilledRevenue) p_lines[i];
        // Account pendingServiceRevenue = getAccountProduct(conn, as, (Product) OBDal.getInstance()
        // .getProxy(Product.ENTITY_NAME, line.m_M_Product_ID));
        // Account nonInvoicedServices = getAccountBussinesPartner(conn, as, (BusinessPartner) OBDal
        // .getInstance().getProxy(BusinessPartner.ENTITY_NAME, line.m_C_BPartner_ID));

        // if (SCConstants.UNBILLED_REVENUE_CURRENT.equals(line.getType())) {
        // fact.createLine(line, pendingServiceRevenue, C_Currency_ID,
        // line.getBaseCurrencyLineNetAmount(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
        // DocumentType, conn);
        // fact.createLine(line, nonInvoicedServices, C_Currency_ID, "",
        // line.getBaseCurrencyLineNetAmount(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
        // DocumentType, conn);
        // } else if (SCConstants.UNBILLED_REVENUE_PREVIOUS.equals(line.getType())) {
        // UnbilledRevenueLine prevURLine = line.getPreviousUnbilledRevenueLine();

        // BigDecimal convertedAmount = new BigDecimal(line.getBaseCurrencyLineNetAmount());
        boolean isMulticurrency = false;
        if (!C_Currency_ID.equals(as.m_C_Currency_ID)) {
          // convertedAmount = new BigDecimal(getConvertedAmt(line.getBaseCurrencyLineNetAmount()
          // .toString(), C_Currency_ID, as.m_C_Currency_ID, OBDateUtils.formatDate(prevURLine
          // .getOBSCNTRUnBillRevenue().getAccountingDate()), "", AD_Client_ID, AD_Org_ID, conn));
          // isMulticurrency = true;
          // }

          // If it is a multicurrency transaction use the accounting schema currency to avoid the
          // amount conversion inside the createLine method. The amount has been converted
          // previously.
          // fact.createLine(line, pendingServiceRevenue, isMulticurrency ? as.m_C_Currency_ID
          // : C_Currency_ID, "", convertedAmount.toString(), Fact_Acct_Group_ID,
          // nextSeqNo(SeqNo), DocumentType, conn);
          // fact.createLine(line, nonInvoicedServices, isMulticurrency ? as.m_C_Currency_ID
          // : C_Currency_ID, convertedAmount.toString(), "", Fact_Acct_Group_ID,
          // nextSeqNo(SeqNo), DocumentType, conn);
        } else {
          // log4j.error("Unknown type of unbilled revenue line: " + line.getType());
        }

      }

    } finally {
      OBContext.restorePreviousMode();
    }

    SeqNo = "0";

    return fact;
  }

  @Override
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  private FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FIN_PaymentScheduleDetail psd = null;
    OrderLine ol = null;
    DoubtfulDebt doubtfulDebt = OBDal.getInstance().get(DoubtfulDebt.class, Id);
    List<FIN_PaymentScheduleDetail> lines = doubtfulDebt.getFINPaymentSchedule()
        .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
    if (lines == null) {
      return null;
    }

    FieldProviderFactory[] data = new FieldProviderFactory[lines.size()];
    try {
      OBContext.setAdminMode(true);
      for (int i = 0; i < data.length; i++) {
        data[i] = new FieldProviderFactory(null);
        psd = lines.get(i);

        // Common attributes
        FieldProviderFactory.setField(data[i], "adOrgId", psd.getOrganization().getId());
        // FieldProviderFactory.setField(data[i], "description", psd.getDescription());
        // FieldProviderFactory.setField(data[i], "cCurrencyId", psd.getCurrency().getId());

        // Special attributes for unbilled revenue lines
        // DocLine_UnbilledRevenueLine object will be populated using this info on loadLines()
        // method
        FieldProviderFactory.setField(data[i], "OBCNTR_Unbilled_RevenueLine_ID", psd.getId());
        // FieldProviderFactory.setField(data[i], "LineNetAmt", psd.getLineNetAmount().toString());

        // ACCOUNTING DIMENSIONS

      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    FieldProviderFactory[] data = loadLinesFieldProvider(Record_ID);
    if (data == null || data.length == 0) {
      return null;
    }
    for (int i = 0; i < data.length; i++) {
      if (data[i] == null) {
        continue;
      }
      OBContext.setAdminMode();
      try {
        String lineID = data[i].getField("OBCNTR_Unbilled_RevenueLine_ID");
        // DocLine_UnbilledRevenue docLine = new DocLine_UnbilledRevenue(DocumentType, Record_ID,
        // lineID);
        // String invoicePlanId = data[i].getField("OBSCNTR_InvoicePlan_ID");
        // String orderLineId = data[i].getField("C_OrderLine_ID");
        // String previousUnbilledRevenueLine = data[i].getField("Previous_RevenueLine_ID");
        // OrderLine ol = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
        // orderLineId);
        // InvoicePlan ip = (InvoicePlan) OBDal.getInstance().getProxy(InvoicePlan.ENTITY_NAME,
        // invoicePlanId);
        // UnbilledRevenueLine urline = (UnbilledRevenueLine) OBDal.getInstance().getProxy(
        // UnbilledRevenueLine.ENTITY_NAME, previousUnbilledRevenueLine);
        // docLine.loadAttributes(data[i], this);
        // docLine.setType(data[i].getField("Type"));
        // docLine.setLineNetAmount(data[i].getField("LineNetAmt"));
        // docLine.setBaseCurrencyLineNetAmount(data[i].getField("BaseCurrencyLineNetAmount"));
        // docLine.setInvoicePlan(ip);
        // docLine.setOrderLine(ol);
        // docLine.setPreviousUnbilledRevenueLine(urline);
        // list.add(docLine);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    // DocLine_UnbilledRevenue[] dl = new DocLine_UnbilledRevenue[list.size()];
    // list.toArray(dl);
    // return dl;
    return null;// FIXME
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }
}