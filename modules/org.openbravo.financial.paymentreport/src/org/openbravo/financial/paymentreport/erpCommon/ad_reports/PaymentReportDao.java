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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.financial.paymentreport.erpCommon.ad_reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.List;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.utils.Replace;

public class PaymentReportDao {

  public PaymentReportDao() {
  }

  public <T extends BaseOBObject> T getObject(Class<T> t, String strId) {
    return OBDal.getInstance().get(t, strId);
  }

  public FieldProvider[] getPaymentReport(VariablesSecureApp vars, String strOrg,
      String strInclSubOrg, String strDueDateFrom, String strDueDateTo, String strAmountFrom,
      String strAmountTo, String strDocumentDateFrom, String strDocumentDateTo,
      String strcBPartnerIdIN, String strcBPGroupIdIN, String strcProjectIdIN, String strfinPaymSt,
      String strPaymentMethodId, String strFinancialAccountId, String strcCurrency,
      String strConvertCurrency, String strConversionDate, String strPaymType, String strOverdue,
      String strGroupCrit, String strOrdCrit) {

    final StringBuilder hsqlScript = new StringBuilder();
    final java.util.List<Object> parameters = new ArrayList<Object>();

    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    FieldProvider[] data;
    Date invoicedDate;
    long plannedDSO = 0;
    long currentDSO = 0;
    long currentTime = 0;
    Currency transCurrency;
    BigDecimal transAmount = null;
    ConversionRate convRate = null;
    ArrayList<FieldProvider> groupedData = new ArrayList<FieldProvider>();

    OBContext.setAdminMode();
    try {

      hsqlScript.append(" as fpsd ");
      hsqlScript.append(" left outer join fpsd.paymentDetails.finPayment ");
      hsqlScript.append(" left outer join fpsd.invoicePaymentSchedule ");
      hsqlScript.append(" left outer join fpsd.invoicePaymentSchedule.invoice ");
      hsqlScript
          .append(" left outer join fpsd.paymentDetails.finPayment.businessPartner.businessPartnerCategory a");
      hsqlScript
          .append(" left outer join fpsd.invoicePaymentSchedule.invoice.businessPartner.businessPartnerCategory b");
      hsqlScript.append(" where (fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hsqlScript.append(" is not null or fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      hsqlScript.append(" is not null ");
      hsqlScript.append(") ");

      // organization + include sub-organization
      if (!strOrg.isEmpty()) {
        if (!strInclSubOrg.equalsIgnoreCase("include")) {
          hsqlScript.append(" and fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION);
          hsqlScript.append(".id = '");
          hsqlScript.append(strOrg);
          hsqlScript.append("'");
        } else {
          hsqlScript.append(" and fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION);
          hsqlScript.append(".id in ('");
          Set<String> orgChildTree = OBContext.getOBContext().getOrganizationStructureProvider()
              .getChildTree(strOrg, true);
          Iterator<String> orgChildTreeIter = orgChildTree.iterator();
          while (orgChildTreeIter.hasNext()) {
            hsqlScript.append(orgChildTreeIter.next());
            orgChildTreeIter.remove();
            hsqlScript.append("'");
            if (orgChildTreeIter.hasNext())
              hsqlScript.append(", '");
          }
          hsqlScript.append(")");
        }
      }

      // due date from - due date to
      if (!strDueDateFrom.isEmpty()) {
        hsqlScript.append(" and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" > ?");
        parameters.add(FIN_Utility.getDate(strDueDateFrom));
      }
      if (!strDueDateTo.isEmpty()) {
        hsqlScript.append(" and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" < ?");
        parameters.add(FIN_Utility.getDate(strDueDateTo));
      }

      // amount from - amount to
      if (!strAmountFrom.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_AMOUNT);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_AMOUNT);
        hsqlScript.append(") > '");
        hsqlScript.append(strAmountFrom);
        hsqlScript.append("'");
      }
      if (!strAmountTo.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_AMOUNT);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_AMOUNT);
        hsqlScript.append(") < '");
        hsqlScript.append(strAmountTo);
        hsqlScript.append("'");
      }

      // document date from - document date to
      if (!strDocumentDateFrom.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
        hsqlScript.append(") > ?");
        parameters.add(FIN_Utility.getDate(strDocumentDateFrom));
      }
      if (!strDocumentDateTo.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
        hsqlScript.append(") < ?");
        parameters.add(FIN_Utility.getDate(strDocumentDateTo));
      }

      // business partner
      if (!strcBPartnerIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(") in ");
        hsqlScript.append(strcBPartnerIdIN);
      }

      // business partner category
      if (!strcBPGroupIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(".");
        hsqlScript.append(BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(".");
        hsqlScript.append(BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY);
        hsqlScript.append(") = '");
        hsqlScript.append(strcBPGroupIdIN);
        hsqlScript.append("'");
      }

      // project
      if (!strcProjectIdIN.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_PROJECT);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_PROJECT);
        hsqlScript.append(") in ");
        hsqlScript.append(strcProjectIdIN);
      }

      // status
      if (!strfinPaymSt.isEmpty() && !strfinPaymSt.equalsIgnoreCase("('')")) {
        hsqlScript.append(" and (fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_STATUS);
        hsqlScript.append(" in ");
        hsqlScript.append(strfinPaymSt);
        if (strfinPaymSt.contains("RPAP")) {
          hsqlScript.append(" or fpsd.");
          hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
          hsqlScript.append(" is null)");
        } else {
          hsqlScript.append(" )");
        }
      }

      // payment method
      if (!strPaymentMethodId.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTMETHOD);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_PAYMENTMETHOD);
        hsqlScript.append(") = '");
        hsqlScript.append(strPaymentMethodId);
        hsqlScript.append("'");
      }

      // financial account
      if (!strFinancialAccountId.isEmpty()) {
        hsqlScript.append(" and (fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(" is not null and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_ACCOUNT);
        hsqlScript.append(" = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("' or ((fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'Y' and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(".");
        hsqlScript.append(BusinessPartner.PROPERTY_ACCOUNT);
        hsqlScript.append(" = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("') or (fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'N' and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(".");
        hsqlScript.append(BusinessPartner.PROPERTY_POFINANCIALACCOUNT);
        hsqlScript.append(" = '");
        hsqlScript.append(strFinancialAccountId);
        hsqlScript.append("')))");
      }

      // currency
      if (!strcCurrency.isEmpty()) {
        hsqlScript.append(" and coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_CURRENCY);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_CURRENCY);
        hsqlScript.append(") = '");
        hsqlScript.append(strcCurrency);
        hsqlScript.append("'");
      }

      // payment type
      if (strPaymType.equalsIgnoreCase("FINPR_Receivables")) {
        hsqlScript.append(" and (fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_RECEIPT);
        hsqlScript.append(" = 'Y'");
        hsqlScript.append(" or fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'Y')");
      } else if (strPaymType.equalsIgnoreCase("FINPR_Payables")) {
        hsqlScript.append(" and (fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_RECEIPT);
        hsqlScript.append(" = 'N'");
        hsqlScript.append(" or fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_SALESTRANSACTION);
        hsqlScript.append(" = 'N')");
      }

      // overdue
      if (!strOverdue.isEmpty()) {
        hsqlScript.append(" and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
        hsqlScript.append(" != '0'");
        hsqlScript.append(" and fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        hsqlScript.append(" <  ?");
        parameters.add(FIN_Utility.getDate(dateFormat.format(new Date())));
      }

      hsqlScript.append(" order by ");

      if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
        hsqlScript.append(" coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hsqlScript.append("), ");
      } else if (strGroupCrit.equalsIgnoreCase("Project")) {
        hsqlScript.append("  coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_PROJECT);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_PROJECT);
        hsqlScript.append("), ");
      } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
        hsqlScript.append("  coalesce(a, b), ");
      } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
        hsqlScript.append("  coalesce(fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
        hsqlScript.append(".");
        hsqlScript.append(FIN_Payment.PROPERTY_CURRENCY);
        hsqlScript.append(", fpsd.");
        hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        hsqlScript.append(".");
        hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hsqlScript.append(".");
        hsqlScript.append(Invoice.PROPERTY_CURRENCY);
        hsqlScript.append("), ");
      }

      hsqlScript.append(" coalesce(fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hsqlScript.append(".");
      hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
      hsqlScript.append(".");
      hsqlScript.append(FIN_Payment.PROPERTY_STATUS);
      hsqlScript.append(", 'RPAP')");

      if (!strOrdCrit.isEmpty()) {
        String[] strOrdCritList = strOrdCrit.substring(2, strOrdCrit.length() - 2).split("', '");

        for (int i = 0; i < strOrdCritList.length; i++) {
          if (strOrdCritList[i].contains("Date")) {
            hsqlScript.append(",  coalesce(fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
            hsqlScript.append(".");
            hsqlScript.append(FIN_Payment.PROPERTY_PAYMENTDATE);
            hsqlScript.append(", fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
            hsqlScript.append(".");
            hsqlScript.append(Invoice.PROPERTY_INVOICEDATE);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("Project")) {
            hsqlScript.append(",  coalesce(fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
            hsqlScript.append(".");
            hsqlScript.append(FIN_Payment.PROPERTY_PROJECT);
            hsqlScript.append(", fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
            hsqlScript.append(".");
            hsqlScript.append(Invoice.PROPERTY_PROJECT);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("FINPR_BPartner_Category")) {
            hsqlScript.append(",  coalesce(fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
            hsqlScript.append(".");
            hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
            hsqlScript.append(".");
            hsqlScript.append(BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY);
            hsqlScript.append(", fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
            hsqlScript.append(".");
            hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
            hsqlScript.append(".");
            hsqlScript.append(BusinessPartner.PROPERTY_BUSINESSPARTNERCATEGORY);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("APRM_FATS_BPARTNER")) {
            hsqlScript.append(",  coalesce(fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
            hsqlScript.append(".");
            hsqlScript.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
            hsqlScript.append(", fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
            hsqlScript.append(".");
            hsqlScript.append(Invoice.PROPERTY_BUSINESSPARTNER);
            hsqlScript.append(")");
          }
          if (strOrdCritList[i].contains("INS_CURRENCY")) {
            hsqlScript.append(",  coalesce(fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentDetail.PROPERTY_FINPAYMENT);
            hsqlScript.append(".");
            hsqlScript.append(FIN_Payment.PROPERTY_CURRENCY);
            hsqlScript.append(", fpsd.");
            hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
            hsqlScript.append(".");
            hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
            hsqlScript.append(".");
            hsqlScript.append(Invoice.PROPERTY_CURRENCY);
            hsqlScript.append(")");
          }
        }
      }

      hsqlScript.append(", fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hsqlScript.append(".");
      hsqlScript.append(FIN_PaymentDetail.PROPERTY_ID);
      hsqlScript.append(", fpsd.");
      hsqlScript.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      hsqlScript.append(".");
      hsqlScript.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      hsqlScript.append(".");
      hsqlScript.append(Invoice.PROPERTY_ID);

      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, hsqlScript.toString(), parameters);
      obqPSD.setFilterOnReadableOrganization(false);
      java.util.List<FIN_PaymentScheduleDetail> obqPSDList = obqPSD.list();
      data = FieldProviderFactory.getFieldProviderArray(obqPSDList);

      FIN_PaymentScheduleDetail[] FIN_PaymentScheduleDetail = new FIN_PaymentScheduleDetail[0];
      FIN_PaymentScheduleDetail = obqPSDList.toArray(FIN_PaymentScheduleDetail);

      FIN_PaymentDetail finPaymDetail;
      FIN_PaymentSchedule finPaymSchedule;
      Boolean mustGroup;
      String previousInvoiceId = null;
      String previousPaymentId = null;
      BigDecimal amountSum = BigDecimal.ZERO;
      FieldProvider previousRow = null;
      ConversionRate previousConvRate = null;

      for (int i = 0; i < data.length; i++) {
        if (FIN_PaymentScheduleDetail[i].getPaymentDetails() != null) {
          // bp_group -- bp_category
          FieldProviderFactory.setField(data[i], "BP_GROUP", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getBusinessPartner()
              .getBusinessPartnerCategory().getName());
          // bpartner
          FieldProviderFactory.setField(data[i], "BPARTNER", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getBusinessPartner().getName());

          // transCurrency
          transCurrency = FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
              .getCurrency();
          FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());
          // paymentMethod
          FieldProviderFactory.setField(data[i], "PAYMENT_METHOD", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getPaymentMethod().getIdentifier());

          // payment
          FieldProviderFactory.setField(data[i], "PAYMENT", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getIdentifier().toString());
          // payment_id
          FieldProviderFactory.setField(data[i], "PAYMENT_ID", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getId().toString());
          // payment yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_Y_N", "");
          // financialAccount
          FieldProviderFactory.setField(data[i], "FINANCIAL_ACCOUNT", FIN_PaymentScheduleDetail[i]
              .getPaymentDetails().getFinPayment().getAccount().getIdentifier());
          // status
          FieldProviderFactory.setField(data[i], "STATUS",
              translateRefList(FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment()
                  .getStatus()));
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getPaymentDetails().getFinPayment().isReceipt())
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
          else
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");
        } else {

          // bp_group -- bp_category
          FieldProviderFactory.setField(data[i], "BP_GROUP", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getInvoice().getBusinessPartner()
              .getBusinessPartnerCategory().getName());
          // bpartner
          FieldProviderFactory.setField(data[i], "BPARTNER", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getInvoice().getBusinessPartner().getName());
          // transCurrency
          transCurrency = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .getCurrency();
          FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());
          // paymentMethod
          FieldProviderFactory.setField(data[i], "PAYMENT_METHOD", "");
          // payment
          FieldProviderFactory.setField(data[i], "PAYMENT", "");
          // payment_id
          FieldProviderFactory.setField(data[i], "PAYMENT_ID", "");
          // payment yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_Y_N", "Display:None");
          // financialAccount
          FieldProviderFactory.setField(data[i], "FINANCIAL_ACCOUNT", "");
          // status
          FieldProviderFactory.setField(data[i], "STATUS", translateRefList("RPAP"));
          // is receipt
          if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .isSalesTransaction())
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "Y");
          else
            FieldProviderFactory.setField(data[i], "ISRECEIPT", "N");

        }

        if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule() != null) {
          // project
          if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice().getProject() != null)
            FieldProviderFactory.setField(data[i], "PROJECT", FIN_PaymentScheduleDetail[i]
                .getInvoicePaymentSchedule().getInvoice().getProject().getIdentifier());
          else
            FieldProviderFactory.setField(data[i], "PROJECT", "");
          // salesPerson
          if (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .getUserContact() != null) {
            FieldProviderFactory.setField(data[i], "SALES_PERSON", FIN_PaymentScheduleDetail[i]
                .getInvoicePaymentSchedule().getInvoice().getUserContact().getIdentifier());
          } else {
            FieldProviderFactory.setField(data[i], "SALES_PERSON", "");
          }
          // invoiceNumber
          FieldProviderFactory.setField(data[i], "INVOICE_NUMBER", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getInvoice().getDocumentNo());
          // payment plan id
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_ID", FIN_PaymentScheduleDetail[i]
              .getInvoicePaymentSchedule().getId());
          // payment plan yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_Y_N", "");
          // invoiceDate
          invoicedDate = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .getInvoiceDate();
          FieldProviderFactory.setField(data[i], "INVOICE_DATE", dateFormat.format(invoicedDate)
              .toString());
          // dueDate
          FieldProviderFactory.setField(data[i], "DUE_DATE", dateFormat.format(
              FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getDueDate()).toString());
          // plannedDSO
          plannedDSO = (FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getDueDate()
              .getTime() - FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
              .getInvoiceDate().getTime());
          FieldProviderFactory.setField(data[i], "PLANNED_DSO", String.valueOf(plannedDSO
              / (1000 * 60 * 60 * 24)));
          // currentDSO
          currentTime = System.currentTimeMillis();
          if (currentTime < invoicedDate.getTime())
            FieldProviderFactory.setField(data[i], "CURRENT_DSO", String.valueOf(plannedDSO
                / (1000 * 60 * 60 * 24)));
          else {
            currentDSO = currentTime
                - FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getInvoice()
                    .getInvoiceDate().getTime();
            FieldProviderFactory.setField(data[i], "CURRENT_DSO", String.valueOf((currentDSO)
                / (1000 * 60 * 60 * 24)));
          }
          // daysOverdue
          if (currentTime < FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule().getDueDate()
              .getTime())
            FieldProviderFactory.setField(data[i], "OVERDUE", "0");
          else
            FieldProviderFactory.setField(data[i], "OVERDUE", String
                .valueOf((currentTime - FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule()
                    .getDueDate().getTime())
                    / (1000 * 60 * 60 * 24)));
        } else {
          // project
          FieldProviderFactory.setField(data[i], "PROJECT", "");
          // salesPerson
          FieldProviderFactory.setField(data[i], "SALES_PERSON", "");
          // invoiceNumber.
          FieldProviderFactory.setField(data[i], "INVOICE_NUMBER", "");
          // payment plan id
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_ID", "");
          // payment plan yes / no
          FieldProviderFactory.setField(data[i], "PAYMENT_PLAN_Y_N", "Display:none");
          // invoiceDate
          FieldProviderFactory.setField(data[i], "INVOICE_DATE", "");
          // dueDate.
          FieldProviderFactory.setField(data[i], "DUE_DATE", "");
          // plannedDSO
          FieldProviderFactory.setField(data[i], "PLANNED_DSO", "0");
          // currentDSO
          FieldProviderFactory.setField(data[i], "CURRENT_DSO", "0");
          // daysOverdue
          FieldProviderFactory.setField(data[i], "OVERDUE", "0");

        }

        transAmount = FIN_PaymentScheduleDetail[i].getAmount();

        Currency baseCurrency = OBDal.getInstance().get(Currency.class, strConvertCurrency);

        boolean sameCurrency = baseCurrency.getISOCode().equalsIgnoreCase(
            transCurrency.getISOCode());
        if (!sameCurrency) {
          convRate = this.getConversionRate(transCurrency, baseCurrency, strConversionDate);

          if (convRate != null) {
            transAmount = FIN_PaymentScheduleDetail[i].getAmount();
            // baseAmount
            FieldProviderFactory.setField(data[i], "BASE_AMOUNT", transAmount.multiply(
                convRate.getMultipleRateBy()).toString());
          } else {
            FieldProvider[] fp = new FieldProvider[1];
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("transCurrency", transCurrency.getId());
            hm.put("baseCurrency", strConvertCurrency);
            hm.put("conversionDate", strConversionDate);

            fp[0] = new FieldProviderFactory(hm);
            data = fp;

            OBContext.restorePreviousMode();
            return data;
          }
        } else {
          convRate = null;
        }

        // currency
        FieldProviderFactory.setField(data[i], "BASE_CURRENCY", baseCurrency.getISOCode());
        // baseCurrency
        FieldProviderFactory.setField(data[i], "TRANS_CURRENCY", transCurrency.getISOCode());

        finPaymDetail = FIN_PaymentScheduleDetail[i].getPaymentDetails();
        finPaymSchedule = FIN_PaymentScheduleDetail[i].getInvoicePaymentSchedule();

        if (finPaymDetail != null && finPaymSchedule != null) {
          mustGroup = finPaymDetail.getFinPayment().getId().equalsIgnoreCase(previousPaymentId)
              && finPaymSchedule.getInvoice().getId().equalsIgnoreCase(previousInvoiceId);
          previousInvoiceId = finPaymSchedule.getInvoice().getId();
          previousPaymentId = finPaymDetail.getFinPayment().getId();
        } else if (finPaymDetail != null && finPaymSchedule == null) {
          mustGroup = finPaymDetail.getFinPayment().getId().equalsIgnoreCase(previousPaymentId)
              && previousInvoiceId == null;
          previousPaymentId = finPaymDetail.getFinPayment().getId();
          previousInvoiceId = null;
        } else if (finPaymDetail == null && finPaymSchedule != null) {
          mustGroup = previousPaymentId == null
              && finPaymSchedule.getInvoice().getId().equalsIgnoreCase(previousInvoiceId);
          previousPaymentId = null;
          previousInvoiceId = finPaymSchedule.getInvoice().getId();
        } else {
          mustGroup = false;
        }

        if (mustGroup) {
          amountSum = amountSum.add(transAmount);
        } else {
          if (previousRow != null) {
            FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.toString());
            if (previousConvRate == null) {
              FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.toString());
            } else {
              FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.multiply(
                  previousConvRate.getMultipleRateBy()).toString());
            }
            groupedData.add(previousRow);
          }
          previousRow = data[i];
          previousConvRate = convRate;
          amountSum = transAmount;
        }

        // group_crit_id this is the column that has the ids of the grouping criteria selected
        if (strGroupCrit.equalsIgnoreCase("APRM_FATS_BPARTNER")) {
          FieldProviderFactory.setField(data[i], "GROUP_CRIT_ID", data[i].getField("BPARTNER"));
          FieldProviderFactory.setField(data[i], "GROUP_CRIT", "Business Partner");
        } else if (strGroupCrit.equalsIgnoreCase("Project")) {
          FieldProviderFactory.setField(data[i], "GROUP_CRIT_ID", data[i].getField("PROJECT"));
          FieldProviderFactory.setField(data[i], "GROUP_CRIT", "Project");
        } else if (strGroupCrit.equalsIgnoreCase("FINPR_BPartner_Category")) {
          FieldProviderFactory.setField(data[i], "GROUP_CRIT_ID", data[i].getField("BP_GROUP"));
          FieldProviderFactory.setField(data[i], "GROUP_CRIT", "Business Partner Category");
        } else if (strGroupCrit.equalsIgnoreCase("INS_CURRENCY")) {
          FieldProviderFactory.setField(data[i], "GROUP_CRIT_ID", data[i]
              .getField("TRANS_CURRENCY"));
          FieldProviderFactory.setField(data[i], "GROUP_CRIT", "Currency");
        } else {
          FieldProviderFactory.setField(data[i], "GROUP_CRIT_ID", "");
        }

      }

      if (convRate != null) {
        FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.toString());
        FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.multiply(
            convRate.getMultipleRateBy()).toString());
        groupedData.add(previousRow);
      } else {
        FieldProviderFactory.setField(previousRow, "TRANS_AMOUNT", amountSum.toString());
        FieldProviderFactory.setField(previousRow, "BASE_AMOUNT", amountSum.toString());
        groupedData.add(previousRow);
      }

    } finally {
      OBContext.restorePreviousMode();
    }
    return (FieldProvider[]) groupedData.toArray(new FieldProvider[groupedData.size()]);
  }

  public ConversionRate getConversionRate(Currency transCurrency, Currency baseCurrency,
      String conversionDate) {

    java.util.List<ConversionRate> convRateList;
    ConversionRate convRate;
    Date conversionDateObj = FIN_Utility.getDate(conversionDate);

    OBContext.setAdminMode();
    try {

      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.add(Expression.eq(ConversionRate.PROPERTY_CURRENCY, transCurrency));
      obcConvRate.add(Expression.eq(ConversionRate.PROPERTY_TOCURRENCY, baseCurrency));
      obcConvRate.add(Expression.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDateObj));
      obcConvRate.add(Expression.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDateObj));

      convRateList = obcConvRate.list();

      if ((convRateList != null) && (convRateList.size() != 0))
        convRate = convRateList.get(0);
      else
        convRate = null;

    } finally {
      OBContext.restorePreviousMode();
    }

    return convRate;
  }

  public String[] getReferenceListValues(String refName, boolean inclEmtyValue) {
    OBContext.setAdminMode();
    String values[];
    try {
      final OBCriteria<Reference> obc = OBDal.getInstance().createCriteria(Reference.class);
      obc.add(Expression.eq(Reference.PROPERTY_NAME, refName));
      final OBCriteria<List> obcValue = OBDal.getInstance().createCriteria(List.class);
      obcValue.add(Expression.eq(List.PROPERTY_REFERENCE, obc.list().get(0)));
      java.util.List<List> v = obcValue.list();
      int n = v.size();

      if (inclEmtyValue)
        values = new String[n + 1];
      else
        values = new String[n];

      for (int i = 0; i < n; i++)
        values[i] = v.get(i).getSearchKey();

      if (inclEmtyValue)
        values[values.length - 1] = new String("");

    } finally {
      OBContext.restorePreviousMode();
    }

    return values;
  }

  public static String translateRefList(String strCode) {
    String strMessage = "";
    OBContext.setAdminMode();
    try {
      Language language = OBContext.getOBContext().getLanguage();

      if (!"en_US".equals(language.getLanguage())) {
        OBCriteria<ListTrl> obcTrl = OBDal.getInstance().createCriteria(ListTrl.class);
        obcTrl.add(Expression.eq(ListTrl.PROPERTY_LANGUAGE, language));
        obcTrl.createAlias(ListTrl.PROPERTY_LISTREFERENCE, "lr");
        obcTrl.add(Restrictions.eq("lr." + List.PROPERTY_SEARCHKEY, strCode));
        obcTrl.setFilterOnReadableClients(false);
        obcTrl.setFilterOnReadableOrganization(false);
        strMessage = (obcTrl.list() != null && obcTrl.list().size() > 0) ? obcTrl.list().get(0)
            .getName() : null;
      }
      if ("en_US".equals(language.getLanguage()) || strMessage == null) {
        OBCriteria<List> obc = OBDal.getInstance().createCriteria(List.class);
        obc.setFilterOnReadableClients(false);
        obc.setFilterOnReadableOrganization(false);
        obc.add(Expression.eq(List.PROPERTY_SEARCHKEY, strCode));
        strMessage = (obc.list() != null && obc.list().size() > 0) ? obc.list().get(0).getName()
            : null;
      }

      if (strMessage == null || strMessage.equals(""))
        strMessage = strCode;
    } finally {
      OBContext.restorePreviousMode();
    }
    return Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
  }

  public static HashMap<String, String> getLinkParameters(String adTableId, String isReceipt) {
    HashMap<String, String> hmValues = new HashMap<String, String>();

    OBContext.setAdminMode();
    try {
      Table adTable = OBDal.getInstance().get(Table.class, adTableId);

      Window adWindow = null;
      if (isReceipt.equalsIgnoreCase("Y")) {
        adWindow = adTable.getWindow();
      } else {
        adWindow = adTable.getPOWindow();
      }
      hmValues.put("adWindowName", adWindow.getName());

      java.util.List<Tab> adTabList = adWindow.getADTabList();
      for (int i = 0; i < adTabList.size(); i++) {
        if (adTabList.get(i).getTable().getId().equalsIgnoreCase(adTableId)) {
          hmValues.put("adTabName", adTabList.get(i).getName());
          hmValues.put("adTabId", adTabList.get(i).getId());
        }
      }

      java.util.List<Column> adColumnList = adTable.getADColumnList();
      for (int i = 0; i < adColumnList.size(); i++) {
        if (adColumnList.get(i).isKeyColumn()) {
          hmValues.put("adColumnName", adColumnList.get(i).getDBColumnName());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return hmValues;
  }
}