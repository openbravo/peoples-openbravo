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
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal.ad_reports;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.ListOfArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSAppCashReconcil;
import org.openbravo.retail.posterminal.OBPOSAppCashup;

public class CashUpReport extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  HashMap<String, Object> parameters;
  FieldProvider[] data;
  VariablesSecureApp vars;
  JRDataSource dataSource;
  HashMap<String, String> psData;
  String reconIds;
  String cashupId;

  private static final Logger log = Logger.getLogger(CashUpReport.class);

  OBPOSAppCashup cashup;
  List<?> salesTaxList;
  List<?> returnsTaxList;
  BigDecimal totalNetSalesAmount;
  BigDecimal totalGrossSalesAmount;
  BigDecimal totalNetReturnsAmount;
  BigDecimal totalGrossReturnsAmount;
  BigDecimal cashToDeposit;
  BigDecimal conversionRate;
  String isoCode;
  BigDecimal totalRetailTransactions;
  BigDecimal totalDrops;
  BigDecimal totalDeposits;
  BigDecimal expected;

  List<HashMap<String, String>> hashMapList;
  List<HashMap<String, String>> hashMapStartingsList;
  List<HashMap<String, String>> hashMapSalesList;
  List<HashMap<String, String>> hashMapWithdrawalsList;
  List<HashMap<String, String>> hashMapCountedList;
  List<HashMap<String, String>> hashMapExpectedList;
  List<HashMap<String, String>> hashMapDifferenceList;
  List<HashMap<String, String>> hashMapCashToKeepList;
  List<HashMap<String, String>> hashMapCashToDepositList;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    totalNetSalesAmount = BigDecimal.ZERO;
    totalGrossSalesAmount = BigDecimal.ZERO;
    totalNetReturnsAmount = BigDecimal.ZERO;
    totalGrossReturnsAmount = BigDecimal.ZERO;
    cashToDeposit = BigDecimal.ZERO;
    conversionRate = BigDecimal.ONE;
    isoCode = new String();
    totalRetailTransactions = BigDecimal.ZERO;
    totalDrops = BigDecimal.ZERO;
    totalDeposits = BigDecimal.ZERO;

    salesTaxList = new ArrayList<Object[]>();
    returnsTaxList = new ArrayList<Object[]>();
    hashMapList = new ArrayList<HashMap<String, String>>();
    hashMapStartingsList = new ArrayList<HashMap<String, String>>();
    hashMapSalesList = new ArrayList<HashMap<String, String>>();
    hashMapWithdrawalsList = new ArrayList<HashMap<String, String>>();
    hashMapCountedList = new ArrayList<HashMap<String, String>>();
    hashMapExpectedList = new ArrayList<HashMap<String, String>>();
    hashMapDifferenceList = new ArrayList<HashMap<String, String>>();
    hashMapCashToKeepList = new ArrayList<HashMap<String, String>>();
    hashMapCashToDepositList = new ArrayList<HashMap<String, String>>();

    reconIds = new String();
    vars = new VariablesSecureApp(request);
    cashupId = vars.getStringParameter("inpobposAppCashupId");
    parameters = new HashMap<String, Object>();

    OBContext.setAdminMode(true);
    try {
      cashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);

      for (int i = 0; i < cashup.getOBPOSAppCashReconcilList().size(); i++) {

        expected = BigDecimal.ZERO;

        if (i != 0)
          reconIds = reconIds + ",";
        reconIds = reconIds
            + "'"
            + (((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i))
                .getReconciliation().getId().toString()) + "'";

        String hqlConversionRate = "select c_currency_rate(payment.financialAccount.currency, payment.obposApplications.organization.currency, null, null, payment.obposApplications.client.id, payment.obposApplications.organization.id) as rate, payment.financialAccount.currency.iSOCode as isocode "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.reconciliation.id=? and trans.account=payment.financialAccount ";
        Query conversionRateQuery = OBDal.getInstance().getSession().createQuery(hqlConversionRate);
        conversionRateQuery.setString(0, ((OBPOSAppCashReconcil) cashup
            .getOBPOSAppCashReconcilList().get(i)).getReconciliation().getId());
        List<?> conversionRateList = conversionRateQuery.list();
        if (!conversionRateList.isEmpty()) {
          conversionRate = new BigDecimal(((Object[]) conversionRateList.get(0))[0].toString());
          isoCode = ((Object[]) conversionRateList.get(0))[1].toString();
        } else {
          conversionRate = BigDecimal.ONE;
        }

        /******************************* STARTING CASH ***************************************************************/
        String hqlStartingCash = "select startingbalance " + "from FIN_Reconciliation recon "
            + "where recon.id = ?";
        Query startingCashQuery = OBDal.getInstance().getSession().createQuery(hqlStartingCash);
        startingCashQuery.setString(0, ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList()
            .get(i)).getReconciliation().getId());
        BigDecimal startingbalance = (BigDecimal) startingCashQuery.uniqueResult();
        expected = expected.add(startingbalance);

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "STARTING");
        psData.put("LABEL", OBMessageUtils.getI18NMessage("OBPOS_LblStarting", new String[] {})
            + " "
            + ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i)).getPaymentType()
                .getCommercialName());
        psData.put("VALUE",
            startingbalance.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", startingbalance.toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalStarting", new String[] {}));
        hashMapStartingsList.add(psData);

        /******************************* DROPS DEPOSIT ***************************************************************/
        // Total drops and deposits computation

        String hqlDropsDeposits = "select trans.description, trans.paymentAmount, trans.depositAmount , c_currency_rate(payment.financialAccount.currency, payment.obposApplications.organization.currency, null, null, payment.obposApplications.client.id, payment.obposApplications.organization.id) as rate, payment.financialAccount.currency.iSOCode as isocode "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where (trans.gLItem=payment.paymentMethod.gLItemForDrops or trans.gLItem=payment.paymentMethod.gLItemForDeposits) and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount order by payment.commercialName";
        Query dropsDepositsQuery = OBDal.getInstance().getSession().createQuery(hqlDropsDeposits);
        dropsDepositsQuery.setString(0, ((OBPOSAppCashReconcil) cashup
            .getOBPOSAppCashReconcilList().get(i)).getReconciliation().getId());
        List<?> dropsDepositList = dropsDepositsQuery.list();

        for (Object obj : dropsDepositList) {

          Object[] objdropdeposit = (Object[]) obj;
          BigDecimal drop = (BigDecimal) objdropdeposit[1];
          BigDecimal deposit = (BigDecimal) objdropdeposit[2];
          if (drop.compareTo(deposit) > 0) {
            psData = new HashMap<String, String>();
            psData.put("GROUPFIELD", "WITHDRAWAL");
            psData.put("LABEL", objdropdeposit[0].toString());
            psData.put("VALUE", drop.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
                .toString());
            if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
              psData.put("FOREIGN_VALUE", drop.toString());
              psData.put("ISOCODE", isoCode);
            } else {
              psData.put("FOREIGN_VALUE", null);
              psData.put("ISOCODE", null);
            }
            psData.put("TOTAL_LABEL",
                OBMessageUtils.getI18NMessage("OBPOS_LblTotalWithdrawals", new String[] {}));
            hashMapWithdrawalsList.add(psData);
            expected = expected.subtract(drop);
            totalDrops = totalDrops.add(drop.multiply(conversionRate).setScale(2,
                BigDecimal.ROUND_UP));
          } else {
            psData = new HashMap<String, String>();
            psData.put("GROUPFIELD", "SALE");
            psData.put("LABEL", objdropdeposit[0].toString());
            psData.put("VALUE", deposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
                .toString());
            if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
              psData.put("FOREIGN_VALUE", deposit.toString());
              psData.put("ISOCODE", isoCode);
            } else {
              psData.put("FOREIGN_VALUE", null);
              psData.put("ISOCODE", null);
            }
            psData.put("TOTAL_LABEL",
                OBMessageUtils.getI18NMessage("OBPOS_LblTotalWithdrawals", new String[] {}));
            hashMapSalesList.add(psData);
            expected = expected.add(deposit);
            totalDeposits = totalDeposits.add(deposit.multiply(conversionRate).setScale(2,
                BigDecimal.ROUND_UP));
          }
        }

        String hqlSalesDeposits = "select obpay.commercialName, sum(trans.paymentAmount), sum(trans.depositAmount),  c_currency_rate(obpay.financialAccount.currency, obpay.obposApplications.organization.currency, null, null, obpay.obposApplications.client.id, obpay.obposApplications.organization.id) as rate, obpay.financialAccount.currency.iSOCode as isocode "
            + " from org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "inner join trans.finPayment as pay, "
            + "org.openbravo.retail.posterminal.OBPOSAppPayment as obpay "
            + "where pay.account=obpay.financialAccount and trans.gLItem is null "
            + "and trans.reconciliation=? "
            + "group by obpay.commercialName, obpay.financialAccount.currency, obpay.obposApplications.organization.currency, obpay.financialAccount.currency.iSOCode, obpay.obposApplications.client.id, obpay.obposApplications.organization.id "
            + " order by obpay.commercialName";

        Query salesDepositsQuery = OBDal.getInstance().getSession().createQuery(hqlSalesDeposits);
        salesDepositsQuery.setString(0, ((OBPOSAppCashReconcil) cashup
            .getOBPOSAppCashReconcilList().get(i)).getReconciliation().getId());
        for (Object obj : salesDepositsQuery.list()) {
          Object[] obja = (Object[]) obj;

          BigDecimal drop = (BigDecimal) obja[1];
          BigDecimal deposit = (BigDecimal) obja[2];
          if (drop.compareTo(BigDecimal.ZERO) != 0) {
            expected = expected.subtract(drop);
            totalDrops = totalDrops.add(drop.multiply(conversionRate).setScale(2,
                BigDecimal.ROUND_UP));
            psData = new HashMap<String, String>();
            psData.put("GROUPFIELD", "WITHDRAWAL");
            psData
                .put(
                    "LABEL",
                    OBMessageUtils.getI18NMessage("OBPOS_Returns",
                        new String[] { obja[0].toString() }));
            psData.put("VALUE", drop.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
                .toString());
            if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
              psData.put("FOREIGN_VALUE", drop.toString());
              psData.put("ISOCODE", isoCode);
            } else {
              psData.put("FOREIGN_VALUE", null);
              psData.put("ISOCODE", null);
            }
            psData.put("TOTAL_LABEL",
                OBMessageUtils.getI18NMessage("OBPOS_LblTotalWithdrawals", new String[] {}));
            hashMapWithdrawalsList.add(psData);

          }

          if (deposit.compareTo(BigDecimal.ZERO) != 0) {
            {
              totalDeposits = totalDeposits.add(deposit.multiply(new BigDecimal((String) obja[3]))
                  .setScale(2, BigDecimal.ROUND_UP));
              expected = expected.add(deposit);
              psData = new HashMap<String, String>();
              psData.put("GROUPFIELD", "SALE");
              psData
                  .put(
                      "LABEL",
                      OBMessageUtils.getI18NMessage("OBPOS_Sales",
                          new String[] { obja[0].toString() }));
              psData.put("VALUE", deposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
                  .toString());
              if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
                psData.put("FOREIGN_VALUE", deposit.toString());
                psData.put("ISOCODE", isoCode);
              } else {
                psData.put("FOREIGN_VALUE", null);
                psData.put("ISOCODE", null);
              }
              psData.put("TOTAL_LABEL",
                  OBMessageUtils.getI18NMessage("OBPOS_LblTotalDeposits", new String[] {}));
              hashMapSalesList.add(psData);
            }
          }

        }
        /******************************* EXPECTED, COUNTED, DIFFERENCE ***************************************************************/
        String hqlDifferenceDeposit = "select trans.paymentAmount  "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.gLItem=payment.paymentMethod.cashDifferences and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount";
        Query differenceDepositQuery = OBDal.getInstance().getSession()
            .createQuery(hqlDifferenceDeposit);
        differenceDepositQuery.setString(0, ((OBPOSAppCashReconcil) cashup
            .getOBPOSAppCashReconcilList().get(i)).getReconciliation().getId());
        BigDecimal differenceDeposit = (BigDecimal) differenceDepositQuery.uniqueResult();
        if (differenceDeposit == null)
          differenceDeposit = BigDecimal.ZERO;

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "COUNTED");
        psData.put("LABEL", OBMessageUtils.getI18NMessage("OBPOS_LblCounted", new String[] {})
            + " "
            + ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i)).getPaymentType()
                .getCommercialName());
        psData.put("VALUE", (expected.subtract(differenceDeposit)).multiply(conversionRate)
            .setScale(2, BigDecimal.ROUND_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", expected.subtract(differenceDeposit).toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalCounted", new String[] {}));
        hashMapCountedList.add(psData);

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "DIFFERENCE");
        psData.put("LABEL", OBMessageUtils.getI18NMessage("OBPOS_LblDifference", new String[] {})
            + " "
            + ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i)).getPaymentType()
                .getCommercialName());
        psData.put("VALUE",
            differenceDeposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", differenceDeposit.toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalDifference", new String[] {}));
        hashMapDifferenceList.add(psData);

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "EXPECTED");
        psData.put("LABEL", OBMessageUtils.getI18NMessage("OBPOS_LblExpected", new String[] {})
            + " "
            + ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i)).getPaymentType()
                .getCommercialName());
        psData.put("VALUE", expected.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
            .toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", expected.toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalExpected", new String[] {}));
        hashMapExpectedList.add(psData);

        /******************************* CASH TO KEEP,CASH TO DEPOSIT ***************************************************************/
        String hqlCashToDeposit = "select trans.paymentAmount  "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.gLItem=payment.paymentMethod.glitemDropdep and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount";
        Query cashToDepositQuery = OBDal.getInstance().getSession().createQuery(hqlCashToDeposit);
        cashToDepositQuery.setString(0, ((OBPOSAppCashReconcil) cashup
            .getOBPOSAppCashReconcilList().get(i)).getReconciliation().getId());
        List<BigDecimal> lstCashToDeposit = cashToDepositQuery.list();
        cashToDeposit = BigDecimal.ZERO;
        if (!lstCashToDeposit.isEmpty()) {
          if (lstCashToDeposit.size() > 1) {
            log.warn("Configuration error: It seems to be more than one events configured with the same GL Item. "
                + lstCashToDeposit.size()
                + " Transactions with the same GLItem have been found for the reconciliation "
                + ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i))
                    .getReconciliation().getIdentifier()
                + ". This situation could cause wrong results");
          }
          for (BigDecimal itemCashToDeposit : lstCashToDeposit) {
            cashToDeposit = cashToDeposit.add((BigDecimal) itemCashToDeposit);
          }
        } else {
          cashToDeposit = BigDecimal.ZERO;
        }

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "TOKEEP");
        psData.put("LABEL", ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i))
            .getPaymentType().getCommercialName());
        psData.put("VALUE", (expected.subtract(differenceDeposit).subtract(cashToDeposit))
            .multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", expected.subtract(differenceDeposit).subtract(cashToDeposit)
              .toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalQtyToKeep", new String[] {}));
        hashMapCashToKeepList.add(psData);

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "TODEPOSIT");
        psData.put("LABEL", ((OBPOSAppCashReconcil) cashup.getOBPOSAppCashReconcilList().get(i))
            .getPaymentType().getCommercialName());
        psData.put("VALUE", cashToDeposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_UP)
            .toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", cashToDeposit.toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalQtyToDepo", new String[] {}));
        hashMapCashToDepositList.add(psData);
      }

      /******************************* SALES ***************************************************************/
      String hqlSales = "select abs(sum(ord.summedLineAmount)), abs(sum(ord.grandTotalAmount)) from Order as ord"
          + " where exists (select 1 from FIN_Payment_ScheduleDetail d"
          + "              where d.orderPaymentSchedule.order = ord"
          + "                 and exists (select 1 "
          + "                               from FIN_Finacc_Transaction t"
          + "                              where t.reconciliation.id in ("
          + reconIds
          + ")"
          + "                                and t.finPayment = d.paymentDetails.finPayment))"
          + "and ord.documentType.id=? ";

      Query salesQuery = OBDal.getInstance().getSession().createQuery(hqlSales);
      salesQuery.setString(0, cashup.getPOSTerminal().getObposTerminaltype().getDocumentType()
          .getId());
      Object[] totalSalesAmount = (Object[]) salesQuery.list().get(0);
      if (totalSalesAmount[0] != null)
        totalNetSalesAmount = totalNetSalesAmount.add((BigDecimal) totalSalesAmount[0]);
      if (totalSalesAmount[1] != null)
        totalGrossSalesAmount = totalGrossSalesAmount.add((BigDecimal) totalSalesAmount[1]);

      /******************************* RETURNS ***************************************************************/
      Query returnsQuery = OBDal.getInstance().getSession().createQuery(hqlSales);
      returnsQuery.setString(0, cashup.getPOSTerminal().getObposTerminaltype()
          .getDocumentTypeForReturns().getId());
      Object[] totalReturnsAmount = (Object[]) returnsQuery.list().get(0);
      if (totalReturnsAmount[0] != null)
        totalNetReturnsAmount = totalNetReturnsAmount.add((BigDecimal) totalReturnsAmount[0]);
      if (totalReturnsAmount[1] != null)
        totalGrossReturnsAmount = totalGrossReturnsAmount.add((BigDecimal) totalReturnsAmount[1]);

      totalRetailTransactions = totalGrossSalesAmount.subtract(totalGrossReturnsAmount);

      try {
        JasperReport subReportSalesTaxes;
        String strLanguage = vars.getLanguage();
        String strBaseDesign = getBaseDesignPath(strLanguage);
        JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
            + "/org/openbravo/retail/posterminal/ad_reports/CashUpSubreport.jrxml");
        subReportSalesTaxes = JasperCompileManager.compileReport(jasperDesignLines);
        parameters.put("SUBREP_CASHUP", subReportSalesTaxes);

      } catch (JRException e) {
        throw new ServletException(e.getMessage());
      }

      // SALES TAXES
      String hqlTaxes = "select ordertax.tax.name ,str(abs(sum(ordertax.taxAmount))) from OrderTax as ordertax "
          + " where exists (select 1 "
          + "                 from FIN_Payment_ScheduleDetail d"
          + "              where d.orderPaymentSchedule.order = ordertax.salesOrder"
          + "                 and exists (select 1 "
          + "                               from FIN_Finacc_Transaction t"
          + "                              where t.reconciliation.id in ("
          + reconIds
          + ") "
          + "                                and t.finPayment = d.paymentDetails.finPayment)) "
          + "and ordertax.salesOrder.documentType.id=? "
          + "group by ordertax.tax.id, ordertax.tax.name";

      Query salesTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
      salesTaxesQuery.setString(0, cashup.getPOSTerminal().getObposTerminaltype().getDocumentType()
          .getId());
      salesTaxList = salesTaxesQuery.list();
      dataSource = new ListOfArrayDataSource(salesTaxList, new String[] { "LABEL", "VALUE" });
      parameters.put("SALES_TAXES", dataSource);

      // RETURNS TAXES
      Query returnsTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
      returnsTaxesQuery.setString(0, cashup.getPOSTerminal().getObposTerminaltype()
          .getDocumentTypeForReturns().getId());
      returnsTaxList = returnsTaxesQuery.list();
      dataSource = new ListOfArrayDataSource(returnsTaxList, new String[] { "LABEL", "VALUE" });
      parameters.put("RETURNS_TAXES", dataSource);

      parameters.put("USER", OBMessageUtils.getI18NMessage("OBPOS_LblUser", new String[] {}) + ": "
          + cashup.getUserContact().getName());
      parameters.put("TERMINAL_ORGANIZATION", cashup.getPOSTerminal().getOrganization().getId());
      parameters.put("TIME", OBMessageUtils.getI18NMessage("OBPOS_LblTime", new String[] {}) + ": "
          + cashup.getCashUpDate());
      parameters.put("NET_SALES_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblNetSales", new String[] {}));
      parameters.put("NET_SALES_VALUE", totalNetSalesAmount.toString());
      parameters.put("GROSS_SALES_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblGrossSales", new String[] {}));
      parameters.put("GROSS_SALES_VALUE", totalGrossSalesAmount.toString());
      parameters.put("NET_RETURNS_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblNetReturns", new String[] {}));
      parameters.put("NET_RETURNS_VALUE", totalNetReturnsAmount.toString());
      parameters.put("GROSS_RETURNS_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblGrossReturns", new String[] {}));
      parameters.put("GROSS_RETURNS_VALUE", totalGrossReturnsAmount.toString());
      parameters.put("TOTAL_RETAIL_TRANS_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblTotalRetailTrans", new String[] {}));
      parameters.put("TOTAL_RETAIL_TRANS_VALUE", totalRetailTransactions.toString());
      parameters.put("TOTAL_DROPS", totalDrops.toString());
      parameters.put("TOTAL_DEPOSITS", totalDeposits.toString());

    } finally {
      OBContext.restorePreviousMode();
    }
    String strReportName = "@basedesign@/org/openbravo/retail/posterminal/ad_reports/CashUpReport.jrxml";
    response.setContentType("text/html; charset=UTF-8");
    hashMapList.addAll(hashMapStartingsList);
    hashMapList.addAll(hashMapWithdrawalsList);
    hashMapList.addAll(hashMapSalesList);
    hashMapList.addAll(hashMapExpectedList);
    hashMapList.addAll(hashMapCountedList);
    hashMapList.addAll(hashMapDifferenceList);
    hashMapList.addAll(hashMapCashToKeepList);
    hashMapList.addAll(hashMapCashToDepositList);
    data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    renderJR(vars, response, strReportName, "pdf", parameters, data, null);

  }
}