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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.json.JsonConstants;

public class CashMgmtDepositsDrops extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(CashCloseReport.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    String posTerminalId = jsonsent.getJSONObject("parameters").getJSONObject("pos")
        .getString("value");
    OBPOSApplications terminal = OBDal.getInstance().get(OBPOSApplications.class, posTerminalId);
    JSONObject result = new JSONObject();

    // Total sales computation

    String hqlTaxes = "select ordertax.tax.id, ordertax.tax.name, sum(ordertax.taxAmount) from OrderTax as ordertax "
        + "join ordertax.salesOrder as ord,  "
        + "org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail as det join det.orderPaymentSchedule as sched "
        + "inner join det.paymentDetails as sdet "
        + "inner join sdet.finPayment as pay, "
        + "org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
        + "where trans.reconciliation is null and ord.documentType.id=? "
        + "and ord.obposApplications.id=?"
        + " and sched.order=ord and trans.finPayment=pay "
        + "group by ordertax.tax.id, ordertax.tax.name";
    Query salesTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
    salesTaxesQuery.setString(0,
        (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentType()));
    salesTaxesQuery.setString(1, posTerminalId);
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
        + "and ord.obposApplications.id=?" + " and sched.order=ord and trans.finPayment=pay ";

    Query salesQuery = OBDal.getInstance().getSession().createQuery(hqlSales);
    salesQuery.setString(0,
        (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentType()));
    salesQuery.setString(1, posTerminalId);
    BigDecimal totalNetAmount = (BigDecimal) salesQuery.uniqueResult();
    if (totalNetAmount == null) {
      totalNetAmount = BigDecimal.ZERO;
    }

    // result.put("netSales", totalNetAmount);
    // result.put("grossSales", totalNetAmount.add(totalSalesTax));
    // result.put("salesTaxes", salesTaxes);
    // Total returns computation

    Query returnTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlTaxes);
    returnTaxesQuery.setString(0,
        (String) DalUtil.getId(terminal.getObposTerminaltype().getDocumentTypeForReturns()));
    returnTaxesQuery.setString(1, posTerminalId);
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
    returnsQuery.setString(1, posTerminalId);
    BigDecimal totalReturnsAmount = (BigDecimal) returnsQuery.uniqueResult();
    // if (totalReturnsAmount == null) {
    // totalReturnsAmount = BigDecimal.ZERO;
    // } else {
    // totalReturnsAmount = totalReturnsAmount.abs();
    // }

    // result.put("netReturns", totalReturnsAmount);
    // result.put("grossReturns", totalReturnsAmount.add(totalReturnsTax.abs()));
    // result.put("returnsTaxes", returnTaxes);
    result.put("totalTendered",
        totalNetAmount.add(totalSalesTax).subtract(totalReturnsAmount.add(totalReturnsTax.abs())));

    // result.put("totalRetailTransactions",
    // totalNetAmount.add(totalSalesTax).subtract(totalReturnsAmount.add(totalReturnsTax.abs())));
    // Total drops and deposits computation
    JSONArray listdepositsdrops = new JSONArray();
    // BigDecimal totalDrops = BigDecimal.ZERO;
    // BigDecimal totalDeposits = BigDecimal.ZERO;

    String hqlDropsDeposits = "select trans.description, trans.paymentAmount, trans.depositAmount, trans.transactionDate as date "
        + "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
        + "where (trans.gLItem=payment.paymentMethod.gLItemForDrops or trans.gLItem=payment.paymentMethod.gLItemForDeposits) and trans.reconciliation is null "
        + "and payment.obposApplications.id=? and trans.account=payment.financialAccount order by trans.transactionDate asc";

    // "select trans.description, trans.paymentAmount, trans.depositAmount "
    // +
    // "from org.openbravo.retail.posterminal.OBPOSAppPayment as payment, org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
    // +
    // "where (trans.gLItem=payment.paymentMethod.gLItemForDrops or trans.gLItem=payment.paymentMethod.gLItemForDeposits) and trans.reconciliation is null "
    // + "and payment.obposApplications=? and trans.account=payment.financialAccount";

    Query dropsDepositsQuery = OBDal.getInstance().getSession().createQuery(hqlDropsDeposits);
    dropsDepositsQuery.setString(0, posTerminalId);
    for (Object obj : dropsDepositsQuery.list()) {
      Object[] objdropdeposit = (Object[]) obj;
      JSONObject dropDeposit = new JSONObject();
      dropDeposit.put("description", objdropdeposit[0]);
      dropDeposit.put("drop", (BigDecimal) objdropdeposit[1]);
      dropDeposit.put("deposit", (BigDecimal) objdropdeposit[2]);
      listdepositsdrops.put(dropDeposit);
    }
    // }

    // String hqlSalesDeposits = "select obpay.commercialName, sum(trans.depositAmount)"
    // + " from org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
    // + "inner join trans.finPayment as pay, "
    // + "org.openbravo.retail.posterminal.OBPOSAppPayment as obpay "
    // + "where pay.account=obpay.financialAccount and trans.gLItem is null "
    // + "and trans.reconciliation is null and obpay.obposApplications.id=? "
    // + "group by obpay.commercialName";
    //
    // Query salesDepositsQuery = OBDal.getInstance().getSession().createQuery(hqlSalesDeposits);
    // salesDepositsQuery.setString(0, posTerminalId);
    // for (Object obj : salesDepositsQuery.list()) {
    // Object[] obja = (Object[]) obj;
    // JSONObject salesDep = new JSONObject();
    // salesDep.put("description", obja[0] + " sales");
    // salesDep.put("amount", obja[1]);
    // deposits.put(salesDep);
    // totalDeposits = totalDeposits.add((BigDecimal) obja[1]);
    // }

    // String hqlReturnsDrop = "select obpay.commercialName, sum(trans.paymentAmount)"
    // + " from org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction as trans "
    // + "inner join trans.finPayment as pay, "
    // + "org.openbravo.retail.posterminal.OBPOSAppPayment as obpay "
    // + "where pay.account=obpay.financialAccount and trans.gLItem is null "
    // + "and trans.reconciliation is null and obpay.obposApplications.id=? "
    // + "group by obpay.commercialName";
    //
    // Query returnDropsQuery = OBDal.getInstance().getSession().createQuery(hqlReturnsDrop);
    // returnDropsQuery.setString(0, posTerminalId);
    // for (Object obj : returnDropsQuery.list()) {
    // Object[] obja = (Object[]) obj;
    // JSONObject returnDrop = new JSONObject();
    // returnDrop.put("description", obja[0] + " returns");
    // returnDrop.put("amount", obja[1]);
    // drops.put(returnDrop);
    // totalDrops = totalDrops.add((BigDecimal) obja[1]);
    // }

    result.put("listdepositsdrops", listdepositsdrops);
    // result.put("totalDrops", totalDrops);
    // result.put("totalDeposits", totalDeposits);

    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    JSONObject finalResult = new JSONObject();
    finalResult.put("data", new JSONArray().put(result));
    finalResult.put("status", 0);
    log.debug(result.toString());
    return finalResult;
  }
}
