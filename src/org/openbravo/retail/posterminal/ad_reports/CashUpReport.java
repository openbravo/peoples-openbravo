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
 * All portions are Copyright (C) 2013-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal.ad_reports;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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
import org.codehaus.jettison.json.JSONArray;
import org.hibernate.Query;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSAppCashReconcil;
import org.openbravo.retail.posterminal.OBPOSAppCashup;
import org.openbravo.retail.posterminal.OBPOSAppPayment;

public class CashUpReport extends HttpSecureAppServlet {
  @Inject
  @Any
  private Instance<CashupReportHook> cashupReportHooks;

  private static final long serialVersionUID = 1L;
  FieldProvider[] data;
  VariablesSecureApp vars;
  HashMap<String, String> psData;
  String reconIds;
  String cashupId;

  private static final Logger log = Logger.getLogger(CashUpReport.class);

  OBPOSAppCashup cashup;
  BigDecimal cashToDeposit;
  BigDecimal conversionRate;
  String isoCode;
  BigDecimal totalDrops;
  BigDecimal totalDeposits;
  BigDecimal expected;
  BigDecimal taxAmount;
  String hqlWhere;

  List<HashMap<String, String>> hashMapList;
  List<HashMap<String, String>> hashMapStartingsList;
  List<HashMap<String, String>> hashMapSalesList;
  List<HashMap<String, String>> hashMapWithdrawalsList;
  List<HashMap<String, String>> hashMapCountedList;
  List<HashMap<String, String>> hashMapExpectedList;
  List<HashMap<String, String>> hashMapDifferenceList;
  List<HashMap<String, String>> hashMapCashToKeepList;
  List<HashMap<String, String>> hashMapCashToDepositList;

  @Override
  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
  ServletException {
    final HashMap<String, Object> parameters = new HashMap<String, Object>();
    cashToDeposit = BigDecimal.ZERO;
    conversionRate = BigDecimal.ONE;
    isoCode = new String();
    totalDrops = BigDecimal.ZERO;
    totalDeposits = BigDecimal.ZERO;
    taxAmount = BigDecimal.ZERO;
    hqlWhere = new String();

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

    OBContext.setAdminMode(false);
    try {
      cashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);
      final boolean isMaster = cashup.getPOSTerminal().isMaster();
      final boolean isSlave = cashup.getPOSTerminal().getMasterterminal() != null;
      // Check for slave
      if (isSlave) {
        // Check if master cashup is closed
        if (cashup.getObposParentCashup() == null || !cashup.getObposParentCashup().isProcessedbo()) {
          throw new ServletException(
              OBMessageUtils.messageBD("OBPOS_ErrCashupReportMasterNotFinish"));
        }
        // Check if all payment are shared
        final List<OBPOSAppPayment> paymentMethodList = cashup.getPOSTerminal()
            .getOBPOSAppPaymentList();
        boolean allShared = true;
        for (final OBPOSAppPayment payment : paymentMethodList) {
          if (!payment.getPaymentMethod().isShared()) {
            allShared = false;
            break;
          }
        }
        if (allShared) {
          throw new ServletException(OBMessageUtils.messageBD("OBPOS_ErrCashupReportSeeMaster"));
        }
      }
      final String hqlRecons = " rec where cashUp.id=:cashUpId order by rec.paymentType.commercialName ";
      final OBQuery<OBPOSAppCashReconcil> reconsQuery = OBDal.getInstance().createQuery(
          OBPOSAppCashReconcil.class, hqlRecons);
      reconsQuery.setNamedParameter("cashUpId", cashup.getId());
      final List<OBPOSAppCashReconcil> recons = reconsQuery.list();
      // Check for slave terminal and CashUp with all share payment
      if (isSlave && recons.size() == 0) {
        throw new ServletException(OBMessageUtils.messageBD("OBPOS_ErrCashupReportSeeMaster"));
      }
      final Date cashUpDate = cashup.getCashUpDate();
      for (int i = 0; i < recons.size(); i++) {
        if (recons.get(i).getReconciliation().getAccount().getOBPOSAppPaymentList().get(0)
            .getFinancialAccount() == null) {
          continue;
        }
        expected = BigDecimal.ZERO;

        if (i != 0)
          reconIds = reconIds + ",";
        reconIds = reconIds + "'" + recons.get(i).getReconciliation().getId().toString() + "'";

        final String hqlConversionRate = "select c_currency_rate(payment.financialAccount.currency, payment.obposApplications.organization.currency, ?, null, payment.obposApplications.client.id, payment.obposApplications.organization.id) as rate, payment.financialAccount.currency.iSOCode as isocode "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.reconciliation.id=? and trans.account=payment.financialAccount ";
        final Query conversionRateQuery = OBDal.getInstance().getSession()
            .createQuery(hqlConversionRate);
        conversionRateQuery.setDate(0, cashUpDate);
        conversionRateQuery.setString(1, recons.get(i).getReconciliation().getId());
        final List<?> conversionRateList = conversionRateQuery.list();
        if (!conversionRateList.isEmpty()) {
          conversionRate = new BigDecimal(((Object[]) conversionRateList.get(0))[0].toString());
          isoCode = ((Object[]) conversionRateList.get(0))[1].toString();
        } else {
          conversionRate = BigDecimal.ONE;
        }

        /******************************* STARTING CASH ***************************************************************/
        final String hqlStartingCash = "select startingbalance " + "from FIN_Reconciliation recon "
            + "where recon.id = ?";
        final Query startingCashQuery = OBDal.getInstance().getSession()
            .createQuery(hqlStartingCash);
        startingCashQuery.setString(0, recons.get(i).getReconciliation().getId());
        final BigDecimal startingbalance = (BigDecimal) startingCashQuery.uniqueResult();
        expected = expected.add(startingbalance);

        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "STARTING");
        psData.put("SEARCHKEY", "STARTING_" + recons.get(i).getPaymentType().getSearchKey());

        psData.put(
            "LABEL",
            getPaymentNameLabel(OBMessageUtils.getI18NMessage("OBPOS_LblStarting", new String[] {})
                + " " + recons.get(i).getPaymentType().getCommercialName(), isMaster, recons.get(i)
                .getPaymentType().getPaymentMethod().isShared()));
        psData.put("VALUE",
            startingbalance.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
            .toString());
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

        final String hqlDropsDeposits = "select trans.description, trans.paymentAmount, trans.depositAmount , c_currency_rate(payment.financialAccount.currency, "
            + "payment.obposApplications.organization.currency, ?, null, payment.obposApplications.client.id, payment.obposApplications.organization.id) as rate, "
            + "payment.financialAccount.currency.iSOCode as isocode, payment.paymentMethod.isshared "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where (trans.gLItem=payment.paymentMethod.gLItemForDrops or trans.gLItem=payment.paymentMethod.gLItemForDeposits) and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount and (payment.paymentMethod.isshared = 'N' or payment.obposApplications.masterterminal is null) order by payment.commercialName";
        final Query dropsDepositsQuery = OBDal.getInstance().getSession()
            .createQuery(hqlDropsDeposits);

        dropsDepositsQuery.setDate(0, cashUpDate);
        dropsDepositsQuery.setString(1, recons.get(i).getReconciliation().getId());
        final List<?> dropsDepositList = dropsDepositsQuery.list();

        for (final Object obj : dropsDepositList) {

          final Object[] objdropdeposit = (Object[]) obj;
          final BigDecimal drop = (BigDecimal) objdropdeposit[1];
          final BigDecimal deposit = (BigDecimal) objdropdeposit[2];
          if (drop.compareTo(deposit) > 0) {
            psData = new HashMap<String, String>();
            psData.put("GROUPFIELD", "WITHDRAWAL");
            psData.put("SEARCHKEY", "WITHDRAWAL_" + recons.get(i).getPaymentType().getSearchKey());
            psData.put(
                "LABEL",
                getPaymentNameLabel(objdropdeposit[0].toString(), isMaster,
                    (Boolean) objdropdeposit[5]));
            psData.put("VALUE", drop.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
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
                BigDecimal.ROUND_HALF_UP));
          } else {
            psData = new HashMap<String, String>();
            psData.put("GROUPFIELD", "SALE");
            psData.put("SEARCHKEY", "SALE_" + recons.get(i).getPaymentType().getSearchKey());
            psData.put(
                "LABEL",
                getPaymentNameLabel(objdropdeposit[0].toString(), isMaster,
                    (Boolean) objdropdeposit[5]));
            psData.put("VALUE",
                deposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
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
            expected = expected.add(deposit);
            totalDeposits = totalDeposits.add(deposit.multiply(conversionRate).setScale(2,
                BigDecimal.ROUND_HALF_UP));
          }
        }

        final String hqlSalesDeposits = "select obpay.commercialName, sum(trans.paymentAmount), sum(trans.depositAmount),  c_currency_rate(obpay.financialAccount.currency, obpay.obposApplications.organization.currency, ?, null, obpay.obposApplications.client.id, obpay.obposApplications.organization.id) as rate, obpay.financialAccount.currency.iSOCode as isocode, obpay.paymentMethod.isshared "
            + " from org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "inner join trans.finPayment as pay, "
            + "org.openbravo.retail.posterminal.OBPOSAppPayment as obpay "
            + "where pay.account=obpay.financialAccount and trans.gLItem is null and (obpay.paymentMethod.isshared = 'N' or obpay.obposApplications.masterterminal is null) "
            + "and trans.reconciliation=? "
            + "group by obpay.commercialName, obpay.financialAccount.currency, obpay.obposApplications.organization.currency, obpay.financialAccount.currency.iSOCode, obpay.obposApplications.client.id, obpay.obposApplications.organization.id, obpay.paymentMethod.isshared "
            + " order by obpay.commercialName";

        final Query salesDepositsQuery = OBDal.getInstance().getSession()
            .createQuery(hqlSalesDeposits);
        salesDepositsQuery.setDate(0, cashUpDate);
        salesDepositsQuery.setString(1, recons.get(i).getReconciliation().getId());
        final List<Object> sales = salesDepositsQuery.list();
        if (sales.size() > 0) {
          for (final Object obj : sales) {
            final Object[] obja = (Object[]) obj;

            final BigDecimal drop = (BigDecimal) obja[1];
            final BigDecimal deposit = (BigDecimal) obja[2];
            if (drop.compareTo(BigDecimal.ZERO) != 0) {
              expected = expected.subtract(drop);
              totalDrops = totalDrops.add(drop.multiply(conversionRate).setScale(2,
                  BigDecimal.ROUND_HALF_UP));
              psData = new HashMap<String, String>();
              psData.put("GROUPFIELD", "WITHDRAWAL");
              psData
              .put("SEARCHKEY", "WITHDRAWAL_" + recons.get(i).getPaymentType().getSearchKey());
              psData.put("LABEL",
                  getPaymentNameLabel(obja[0].toString(), isMaster, (Boolean) obja[5]));
              psData.put("VALUE",
                  drop.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
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
            } else {
              psData = new HashMap<String, String>();
              psData.put("GROUPFIELD", "WITHDRAWAL");
              psData
              .put("SEARCHKEY", "WITHDRAWAL_" + recons.get(i).getPaymentType().getSearchKey());
              psData.put("LABEL",
                  getPaymentNameLabel(obja[0].toString(), isMaster, (Boolean) obja[5]));
              psData.put("VALUE", BigDecimal.ZERO.toString());
              if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
                psData.put("FOREIGN_VALUE", BigDecimal.ZERO.toString());
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
              psData = new HashMap<String, String>();
              psData.put("GROUPFIELD", "SALE");
              psData.put("SEARCHKEY", "SALE_" + recons.get(i).getPaymentType().getSearchKey());
              psData.put("LABEL",
                  getPaymentNameLabel(obja[0].toString(), isMaster, (Boolean) obja[5]));
              psData
              .put("VALUE",
                  deposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
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
              totalDeposits = totalDeposits.add(deposit.multiply(new BigDecimal((String) obja[3]))
                  .setScale(2, BigDecimal.ROUND_HALF_UP));
              expected = expected.add(deposit);
              hashMapSalesList.add(psData);
            } else {
              psData = new HashMap<String, String>();
              psData.put("GROUPFIELD", "SALE");
              psData.put("SEARCHKEY", "SALE_" + recons.get(i).getPaymentType().getSearchKey());
              psData.put("LABEL",
                  getPaymentNameLabel(obja[0].toString(), isMaster, (Boolean) obja[5]));
              psData
              .put("VALUE",
                  deposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
                  .toString());
              if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
                psData.put("FOREIGN_VALUE", BigDecimal.ZERO.toString());
                psData.put("ISOCODE", isoCode);
              } else {
                psData.put("FOREIGN_VALUE", null);
                psData.put("ISOCODE", null);
              }
              psData.put("TOTAL_LABEL",
                  OBMessageUtils.getI18NMessage("OBPOS_LblTotalDeposits", new String[] {}));
              hashMapSalesList.add(psData);
              totalDeposits = totalDeposits.add(deposit.multiply(conversionRate).setScale(2,
                  BigDecimal.ROUND_HALF_UP));
            }

          }
        } else {
          psData = new HashMap<String, String>();
          psData.put("GROUPFIELD", "WITHDRAWAL");
          psData.put("SEARCHKEY", "WITHDRAWAL_" + recons.get(i).getPaymentType().getSearchKey());
          psData.put(
              "LABEL",
              getPaymentNameLabel(recons.get(i).getPaymentType().getCommercialName(), isMaster,
                  recons.get(i).getPaymentType().getPaymentMethod().isShared()));
          psData.put("VALUE", BigDecimal.ZERO.toString());
          if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
            psData.put("FOREIGN_VALUE", BigDecimal.ZERO.toString());
            psData.put("ISOCODE", isoCode);
          } else {
            psData.put("FOREIGN_VALUE", null);
            psData.put("ISOCODE", null);
          }
          psData.put("TOTAL_LABEL",
              OBMessageUtils.getI18NMessage("OBPOS_LblTotalWithdrawals", new String[] {}));
          hashMapWithdrawalsList.add(psData);

          psData = new HashMap<String, String>();
          psData.put("GROUPFIELD", "SALE");
          psData.put("SEARCHKEY", "SALE_" + (recons.get(i).getPaymentType().getSearchKey()));
          psData.put(
              "LABEL",
              getPaymentNameLabel(recons.get(i).getPaymentType().getCommercialName(), isMaster,
                  recons.get(i).getPaymentType().getPaymentMethod().isShared()));
          psData.put("VALUE", BigDecimal.ZERO.toString());
          if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
            psData.put("FOREIGN_VALUE", BigDecimal.ZERO.toString());
            psData.put("ISOCODE", isoCode);
          } else {
            psData.put("FOREIGN_VALUE", null);
            psData.put("ISOCODE", null);
          }
          psData.put("TOTAL_LABEL",
              OBMessageUtils.getI18NMessage("OBPOS_LblTotalDeposits", new String[] {}));
          hashMapSalesList.add(psData);
        }

        /******************************* EXPECTED, COUNTED, DIFFERENCE ***************************************************************/
        final String hqlDifferenceDeposit = "select trans.paymentAmount, trans.depositAmount  "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.gLItem=payment.paymentMethod.cashDifferences and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount and (payment.paymentMethod.isshared = 'N' or payment.obposApplications.masterterminal is null) ";
        final Query differenceDepositQuery = OBDal.getInstance().getSession()
            .createQuery(hqlDifferenceDeposit);
        differenceDepositQuery.setString(0, recons.get(i).getReconciliation().getId());
        final Object[] differenceObj = (Object[]) differenceDepositQuery.uniqueResult();
        BigDecimal differenceDeposit = BigDecimal.ZERO;
        if (differenceObj != null) {
          differenceDeposit = (BigDecimal) differenceObj[0];
          if (differenceDeposit == null || differenceDeposit.equals(BigDecimal.ZERO)) {
            differenceDeposit = (BigDecimal) differenceObj[1];
            if (differenceDeposit == null) {
              differenceDeposit = BigDecimal.ZERO;
            }
          } else {
            differenceDeposit = differenceDeposit.negate();
          }
        }

        // -- EXPECTED --
        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "EXPECTED");
        psData.put("SEARCHKEY", "EXPECTED_" + recons.get(i).getPaymentType().getSearchKey());
        psData.put(
            "LABEL",
            getPaymentNameLabel(OBMessageUtils.getI18NMessage("OBPOS_LblExpected", new String[] {})
                + " " + recons.get(i).getPaymentType().getCommercialName(), isMaster, recons.get(i)
                .getPaymentType().getPaymentMethod().isShared()));
        psData.put("VALUE", expected.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
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

        // -- COUNTED --
        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "COUNTED");
        psData.put("SEARCHKEY", "COUNTED_" + recons.get(i).getPaymentType().getSearchKey());
        psData.put(
            "LABEL",
            getPaymentNameLabel(OBMessageUtils.getI18NMessage("OBPOS_LblCounted", new String[] {})
                + " " + recons.get(i).getPaymentType().getCommercialName(), isMaster, recons.get(i)
                .getPaymentType().getPaymentMethod().isShared()));
        psData.put(
            "VALUE",
            (expected.add(differenceDeposit)).multiply(conversionRate)
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", expected.add(differenceDeposit).toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalCounted", new String[] {}));
        hashMapCountedList.add(psData);

        // -- DIFFERENCE --
        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "DIFFERENCE");
        psData.put("SEARCHKEY", "DIFFERENCE_" + recons.get(i).getPaymentType().getSearchKey());
        psData.put(
            "LABEL",
            getPaymentNameLabel(
                OBMessageUtils.getI18NMessage("OBPOS_LblDifference", new String[] {}) + " "
                    + recons.get(i).getPaymentType().getCommercialName(), isMaster, recons.get(i)
                    .getPaymentType().getPaymentMethod().isShared()));
        psData.put("VALUE",
            differenceDeposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
            .toString());
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

        /******************************* CASH TO KEEP,CASH TO DEPOSIT ***************************************************************/
        final String hqlCashToDeposit = "select trans.paymentAmount  "
            + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
            + "where trans.gLItem=payment.paymentMethod.glitemDropdep and trans.reconciliation=? "
            + "and trans.account=payment.financialAccount and (payment.paymentMethod.isshared = 'N' or payment.obposApplications.masterterminal is null) ";
        final Query cashToDepositQuery = OBDal.getInstance().getSession()
            .createQuery(hqlCashToDeposit);
        cashToDepositQuery.setString(0, recons.get(i).getReconciliation().getId());
        final List<BigDecimal> lstCashToDeposit = cashToDepositQuery.list();
        cashToDeposit = BigDecimal.ZERO;
        if (!lstCashToDeposit.isEmpty()) {
          if (lstCashToDeposit.size() > 1) {
            log.warn("Configuration error: It seems to be more than one events configured with the same GL Item. "
                + lstCashToDeposit.size()
                + " Transactions with the same GLItem have been found for the reconciliation "
                + recons.get(i).getReconciliation().getIdentifier()
                + ". This situation could cause wrong results");
          }
          for (final BigDecimal itemCashToDeposit : lstCashToDeposit) {
            cashToDeposit = cashToDeposit.add(itemCashToDeposit);
          }
        } else {
          cashToDeposit = BigDecimal.ZERO;
        }

        // -- TODEPOSIT --
        final String searchKey = recons.get(i).getPaymentType().getSearchKey();
        psData = new HashMap<String, String>();
        psData.put("SEARCHKEY", "TODEPOSIT_" + searchKey);
        psData.put("GROUPFIELD", "TODEPOSIT");
        psData.put(
            "LABEL",
            getPaymentNameLabel(recons.get(i).getPaymentType().getCommercialName(), isMaster,
                recons.get(i).getPaymentType().getPaymentMethod().isShared()));
        psData
        .put("VALUE",
            cashToDeposit.multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP)
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

        // -- TOKEEP --
        psData = new HashMap<String, String>();
        psData.put("GROUPFIELD", "TOKEEP");
        psData.put("SEARCHKEY", "TOKEEP_" + recons.get(i).getPaymentType().getSearchKey());
        psData.put(
            "LABEL",
            getPaymentNameLabel(recons.get(i).getPaymentType().getCommercialName(), isMaster,
                recons.get(i).getPaymentType().getPaymentMethod().isShared()));
        psData.put("VALUE",
            (expected.add(differenceDeposit).subtract(cashToDeposit)).multiply(conversionRate)
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        if (conversionRate.compareTo(BigDecimal.ONE) != 0) {
          psData.put("FOREIGN_VALUE", expected.add(differenceDeposit).subtract(cashToDeposit)
              .toString());
          psData.put("ISOCODE", isoCode);
        } else {
          psData.put("FOREIGN_VALUE", null);
          psData.put("ISOCODE", null);
        }
        psData.put("TOTAL_LABEL",
            OBMessageUtils.getI18NMessage("OBPOS_LblTotalQtyToKeep", new String[] {}));
        hashMapCashToKeepList.add(psData);

      }

      /******************************* SALES AREA ***************************************************************/
      final String hqlCashup = "SELECT netsales, grosssales, netreturns, grossreturns, totalretailtransactions " //
          + " FROM OBPOS_App_Cashup " //
          + " WHERE id = '" + cashupId + "' "; //
      final Query cashupQuery = OBDal.getInstance().getSession().createQuery(hqlCashup);
      final Object[] arrayOfCashupResults = (Object[]) cashupQuery.list().get(0);
      final BigDecimal totalNetSalesAmount = (BigDecimal) arrayOfCashupResults[0];
      final BigDecimal totalGrossSalesAmount = (BigDecimal) arrayOfCashupResults[1];
      final BigDecimal totalNetReturnsAmount = (BigDecimal) arrayOfCashupResults[2];
      final BigDecimal totalGrossReturnsAmount = (BigDecimal) arrayOfCashupResults[3];
      final BigDecimal totalRetailTransactions = (BigDecimal) arrayOfCashupResults[4];

      // SALES TAXES
      final String hqlTaxes = String.format("SELECT name, STR(ABS(amount)) " //
          + " FROM OBPOS_Taxcashup " //
          + " WHERE obpos_app_cashup_id='%s' AND ordertype='0' " //
          + " ORDER BY name ", cashupId);
      final Query salesTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
      final JRDataSource salesTaxesDataSource = new ListOfArrayDataSource(salesTaxesQuery.list(),
          new String[] { "LABEL", "VALUE" });

      // RETURNS TAXES
      final String hqlReturnTaxes = String.format("SELECT name, STR(ABS(amount)) " //
          + " FROM OBPOS_Taxcashup " //
          + " WHERE obpos_app_cashup_id='%s' AND ordertype='1'  " //
          + " ORDER BY name ", cashupId);
      final Query returnsTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlReturnTaxes);
      final JRDataSource returnTaxesDatasource = new ListOfArrayDataSource(
          returnsTaxesQuery.list(), new String[] { "LABEL", "VALUE" });

      /******************************* BUILD REPORT ***************************************************************/

      try {
        JasperReport subReportSalesTaxes;
        final String strLanguage = vars.getLanguage();
        final String strBaseDesign = getBaseDesignPath(strLanguage);
        final JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
            + "/org/openbravo/retail/posterminal/ad_reports/CashUpSubreport.jrxml");
        subReportSalesTaxes = JasperCompileManager.compileReport(jasperDesignLines);
        parameters.put("SUBREP_CASHUP", subReportSalesTaxes);

      } catch (final JRException e) {
        throw new ServletException(e.getMessage());
      }

      parameters.put("STORE", OBMessageUtils.getI18NMessage("OBPOS_LblStore", new String[] {})
          + ": " + cashup.getPOSTerminal().getOrganization().getIdentifier());
      parameters.put("TERMINAL",
          OBMessageUtils.getI18NMessage("OBPOS_LblTerminal", new String[] {}) + ": "
              + cashup.getPOSTerminal().getIdentifier());
      parameters.put("USER", OBMessageUtils.getI18NMessage("OBPOS_LblUser", new String[] {}) + ": "
          + cashup.getUserContact().getName());
      parameters.put("TERMINAL_ORGANIZATION", cashup.getPOSTerminal().getOrganization().getId());
      parameters.put("TIME", OBMessageUtils.getI18NMessage("OBPOS_LblTime", new String[] {}) + ": "
          + cashup.getCashUpDate().toString().substring(0, 16));
      parameters.put("NET_SALES_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblNetSales", new String[] {}));
      parameters.put("NET_SALES_VALUE", totalNetSalesAmount.toString());
      parameters.put("SALES_TAXES", salesTaxesDataSource);
      parameters.put("GROSS_SALES_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblGrossSales", new String[] {}));
      parameters.put("GROSS_SALES_VALUE", totalGrossSalesAmount.toString());
      parameters.put("NET_RETURNS_LABEL",
          OBMessageUtils.getI18NMessage("OBPOS_LblNetReturns", new String[] {}));
      parameters.put("NET_RETURNS_VALUE", totalNetReturnsAmount.toString());
      parameters.put("RETURNS_TAXES", returnTaxesDatasource);
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

    final String strReportName = "@basedesign@/org/openbravo/retail/posterminal/ad_reports/CashUpReport.jrxml";
    response.setContentType("text/html; charset=UTF-8");
    hashMapList.addAll(hashMapStartingsList);
    hashMapList.addAll(hashMapWithdrawalsList);
    hashMapList.addAll(hashMapSalesList);
    hashMapList.addAll(hashMapExpectedList);
    hashMapList.addAll(hashMapCountedList);
    hashMapList.addAll(hashMapDifferenceList);
    hashMapList.addAll(hashMapCashToKeepList);
    hashMapList.addAll(hashMapCashToDepositList);

    // Hook for procesing cashups..
    final JSONArray messages = new JSONArray(); // all messages returned by hooks
    String next = null; // the first next action of all hooks wins
    for (final CashupReportHook hook : cashupReportHooks) {
      CashupReportHookResult result;
      try {
        result = hook.exec(cashup, hashMapList, parameters);

        if (result != null) {
          if (result.getMessage() != null && !result.getMessage().equals("")) {
            messages.put(result.getMessage());
          }
          if (next == null && result.getNextAction() != null && !result.getNextAction().equals("")) {
            next = result.getNextAction();
          }
        }
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    renderJR(vars, response, strReportName, "pdf", parameters, data, null);

  }

  private String getPaymentNameLabel(String name, boolean isMaster, boolean isShared) {
    String label = name;
    if (isMaster && isShared) {
      label += " " + OBMessageUtils.getI18NMessage("OBPOS_LblPaymentMethodShared", new String[] {});
    }
    return label;
  }

}