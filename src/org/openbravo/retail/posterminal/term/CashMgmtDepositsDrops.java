/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;

public class CashMgmtDepositsDrops extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(CashCloseReport.class);
  Calendar now = Calendar.getInstance();
  // Obtains the UTC time zone offset of the server
  int serverMinutesTimezoneOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET))
      / (1000 * 60);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    String posTerminalId = jsonsent.getString("pos");

    OBPOSApplications terminal;
    OBContext.setAdminMode();
    try {
      terminal = OBDal.getInstance().get(OBPOSApplications.class, posTerminalId);
    } finally {
      OBContext.restorePreviousMode();
    }
    JSONArray result = new JSONArray();

    // Payment types
    String hqlPayments = "select p.id, p.searchKey, p.financialAccount.id , p.financialAccount.currentBalance, p.commercialName, p.paymentMethod.allowdeposits as allowdeposits, p.paymentMethod.allowdrops as allowdrops from OBPOS_App_Payment as p "
        + "where obposApplications.id = ? and (p.paymentMethod.allowdeposits=true or p.paymentMethod.allowdrops=true) order by p.commercialName";
    Query paymentsQuery = OBDal.getInstance().getSession().createQuery(hqlPayments);
    paymentsQuery.setString(0, posTerminalId);
    for (Object payObj : paymentsQuery.list()) {
      Object[] objpayments = (Object[]) payObj;

      JSONObject paymentResult = new JSONObject();
      paymentResult.put("paySearchKey", objpayments[1]);
      paymentResult.put("payName", objpayments[4]);

      String hqlStartingCash = "select sum(depositAmount) , sum(paymentAmount) "
          + "from org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
          + "where trans.account.id = ? and trans.reconciliation is null";
      Query startingCashQuery = OBDal.getInstance().getSession().createQuery(hqlStartingCash);
      startingCashQuery.setString(0, objpayments[2].toString());
      BigDecimal startingCash = BigDecimal.ZERO;
      for (Object obj : startingCashQuery.list()) {
        Object[] objstartingCash = (Object[]) obj;
        if (objstartingCash[0] == null) {
          objstartingCash[0] = new BigDecimal(0);
        }
        if (objstartingCash[1] == null) {
          objstartingCash[1] = new BigDecimal(0);
        }
        startingCash = ((BigDecimal) objpayments[3]).subtract(((BigDecimal) objstartingCash[0])
            .subtract((BigDecimal) objstartingCash[1]));
      }
      paymentResult.put("startingCash", startingCash);

      // Total sales computation
      String hqlTaxes = "select ordertax.tax.id, ordertax.tax.name, sum(ordertax.taxAmount) from OrderTax as ordertax "
          + "join ordertax.salesOrder as ord,  "
          + "org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail as det join det.orderPaymentSchedule as sched "
          + "inner join det.paymentDetails as sdet "
          + "inner join sdet.finPayment as pay, "
          + "org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
          + "where trans.reconciliation is null and ord.documentType.id=? "
          + "and trans.account.id=?"
          + " and sched.order=ord and trans.finPayment=pay "
          + "group by ordertax.tax.id, ordertax.tax.name";

      Query salesTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
      salesTaxesQuery.setString(0,
          (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentType()));
      salesTaxesQuery.setString(1, objpayments[2].toString());
      JSONArray salesTaxes = new JSONArray();
      BigDecimal totalSalesTax = BigDecimal.ZERO;
      for (Object obj : salesTaxesQuery.list()) {
        Object[] sales = (Object[]) obj;
        JSONObject salesTax = new JSONObject();
        salesTax.put("taxId", sales[0]);
        salesTax.put("taxName", sales[1]);
        salesTax.put("taxAmount", sales[2]);
        salesTaxes.put(salesTax);
        totalSalesTax = totalSalesTax.add((BigDecimal) sales[2]);
      }

      String hqlSales = "select sum(ord.summedLineAmount) from Order as ord,  "
          + "org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail as det join det.orderPaymentSchedule as sched "
          + "inner join det.paymentDetails as sdet inner join sdet.finPayment as pay, "
          + "org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
          + "where trans.reconciliation is null and ord.documentType.id=? "
          + "and trans.account.id=?" + " and sched.order=ord and trans.finPayment=pay ";

      Query salesQuery = OBDal.getInstance().getSession().createQuery(hqlSales);
      salesQuery.setString(0,
          (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentType()));
      salesQuery.setString(1, objpayments[2].toString());
      BigDecimal totalNetAmount = (BigDecimal) salesQuery.uniqueResult();
      if (totalNetAmount == null) {
        totalNetAmount = BigDecimal.ZERO;
      }

      Query returnTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
      returnTaxesQuery.setString(0,
          (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentTypeForReturns()));
      returnTaxesQuery.setString(1, objpayments[2].toString());
      JSONArray returnTaxes = new JSONArray();
      BigDecimal totalReturnsTax = BigDecimal.ZERO;
      for (Object obj : returnTaxesQuery.list()) {
        Object[] returns = (Object[]) obj;
        JSONObject returnTax = new JSONObject();
        returnTax.put("taxId", returns[0]);
        returnTax.put("taxName", returns[1]);
        returnTax.put("taxAmount", ((BigDecimal) returns[2]).abs());
        returnTaxes.put(returnTax);
        totalReturnsTax = totalReturnsTax.add(((BigDecimal) returns[2]).abs());
      }

      Query returnsQuery = OBDal.getInstance().getSession().createQuery(hqlSales);
      returnsQuery.setString(0,
          (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentTypeForReturns()));
      returnsQuery.setString(1, objpayments[2].toString());
      BigDecimal totalReturnsAmount = (BigDecimal) returnsQuery.uniqueResult();
      if (totalReturnsAmount == null) {
        totalReturnsAmount = BigDecimal.ZERO;
      } else {
        totalReturnsAmount = totalReturnsAmount.abs();
      }

      paymentResult
          .put(
              "totalTendered",
              totalNetAmount.add(totalSalesTax).subtract(
                  totalReturnsAmount.add(totalReturnsTax.abs())));

      String hqlDropsDeposits = "select trans.description, trans.paymentAmount, trans.depositAmount, trans.createdBy.name, trans.transactionDate as date, c_currency_rate(payment.financialAccount.currency, payment.obposApplications.organization.currency, null, null, payment.obposApplications.client.id, payment.obposApplications.organization.id) as rate, payment.financialAccount.currency.iSOCode as isocode "
          + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
          + "where (trans.gLItem=payment.paymentMethod.gLItemForDrops or trans.gLItem=payment.paymentMethod.gLItemForDeposits) and trans.reconciliation is null "
          + "and payment.id=? and trans.account=payment.financialAccount order by trans.transactionDate asc";

      JSONArray listdepositsdrops = new JSONArray();
      Query dropsDepositsQuery = OBDal.getInstance().getSession().createQuery(hqlDropsDeposits);
      dropsDepositsQuery.setString(0, objpayments[0].toString());
      for (Object obj : dropsDepositsQuery.list()) {
        Object[] objdropdeposit = (Object[]) obj;
        JSONObject dropDeposit = new JSONObject();
        dropDeposit.put("description", objdropdeposit[0]);
        dropDeposit.put("drop",
            ((BigDecimal) objdropdeposit[1]).multiply(new BigDecimal((String) objdropdeposit[5])));
        dropDeposit.put("deposit",
            ((BigDecimal) objdropdeposit[2]).multiply(new BigDecimal((String) objdropdeposit[5])));
        dropDeposit.put("user", objdropdeposit[3]);
        dropDeposit.put("time", objdropdeposit[4]);
        dropDeposit.put("timeOffset", serverMinutesTimezoneOffset);
        dropDeposit.put("rate", objdropdeposit[5]);
        dropDeposit.put("isocode", objdropdeposit[6]);
        listdepositsdrops.put(dropDeposit);
      }
      paymentResult.put("listdepositsdrops", listdepositsdrops);
      result.put(paymentResult);
    }
    JSONObject finalResult = new JSONObject();
    finalResult.put("data", result);
    finalResult.put("status", 0);
    log.debug(result.toString());
    return finalResult;
  }
}
