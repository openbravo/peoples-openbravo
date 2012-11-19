/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.service.json.JsonConstants;

public class PaidReceipts extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    JSONArray respArray = new JSONArray();
    OBContext.setAdminMode(true);
    JSONObject json = jsonsent.getJSONObject("filters");
    String pos = json.getString("pos");
    String client = json.getString("client");
    String organization = json.getString("organization");

    String hqlPaidReceipts = "select ord.id as id, ord.documentNo as documentNo, ord.orderDate as orderDate, "
        + "ord.businessPartner.id as businessPartner, ord.grandTotalAmount as totalamount,  ord.salesRepresentative.name as salesRepresentative_identifier,  ord.documentType.name as documenttype, "
        + "ord.id as orderid, ord.warehouse.id as warehouse, ord.currency.iSOCode as currency, ord.obposApplications.name as posterminalidentifier, ord.businessPartner.name as businessPartner_identifier, ord.currency.id as currency, ord.priceList.id as priceList, ord.salesRepresentative.id as salesRepresentative from Order as ord where ord.client=? and ord.organization=? and ord.obposApplications is not null";
    if (!json.getString("filterText").isEmpty()) {
      hqlPaidReceipts += " and (ord.documentNo like '%" + json.getString("filterText")
          + "%' or upper(ord.businessPartner.name) like upper('%" + json.getString("filterText")
          + "%')) ";
    }
    if (!json.getString("documentType").isEmpty()) {
      hqlPaidReceipts += " and ord.documentType.id='" + json.getString("documentType") + "'";
    }
    if (!json.getString("docstatus").isEmpty() && !json.getString("docstatus").equals("null")) {
      hqlPaidReceipts += " and ord.documentStatus='" + json.getString("docstatus") + "'";
    }
    if (!json.getString("startDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate >='" + json.getString("startDate") + "'";
    }
    if (!json.getString("endDate").isEmpty()) {
      hqlPaidReceipts += " and ord.orderDate <='" + json.getString("endDate") + "'";
    }
    Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
    paidReceiptsQuery.setString(0, client);
    paidReceiptsQuery.setString(1, organization);

    for (Object obj : paidReceiptsQuery.list()) {
      Object[] objpaidReceipts = (Object[]) obj;
      JSONObject paidReceipt = new JSONObject();
      paidReceipt.put("documentNo", objpaidReceipts[1]);
      paidReceipt.put("orderDate", (objpaidReceipts[2]));
      paidReceipt.put("businessPartner", objpaidReceipts[3]);
      paidReceipt.put("totalamount", objpaidReceipts[4]);
      paidReceipt.put("salesrepresentative_identifier", objpaidReceipts[5]);
      paidReceipt.put("documenttype", objpaidReceipts[6]);
      paidReceipt.put("orderid", objpaidReceipts[7]);
      paidReceipt.put("warehouse", objpaidReceipts[8]);
      paidReceipt.put("currency_identifier", objpaidReceipts[9]);
      paidReceipt.put("posterminalidentifier", objpaidReceipts[10]);
      paidReceipt.put("businessPartner_identifier", objpaidReceipts[11]);
      paidReceipt.put("currency", objpaidReceipts[12]);
      paidReceipt.put("priceList", objpaidReceipts[13]);
      paidReceipt.put("salesRepresentative", objpaidReceipts[14]);
      paidReceipt.put("organization", json.getString("organization"));
      paidReceipt.put("posterminal", json.getString("pos"));
      paidReceipt.put("client", json.getString("client"));
      paidReceipt.put("isQuotation", json.getBoolean("isQuotation"));

      JSONArray listpaidReceiptsLines = new JSONArray();
      String hqlPaidReceiptsLines = "select ordLine.product.id as id, ordLine.product.name as name, ordLine.product.uOM.id as uOM, ordLine.orderedQuantity as quantity, "
          + "ordLine.baseGrossUnitPrice as unitPrice, ordLine.lineGrossAmount as linegrossamount, ordLine.id as lineId from OrderLine as ordLine where ordLine.salesOrder.id=?";
      Query paidReceiptsLinesQuery = OBDal.getInstance().getSession()
          .createQuery(hqlPaidReceiptsLines);
      // // paidReceiptsQuery.setString(0, id);
      paidReceiptsLinesQuery.setString(0, (String) objpaidReceipts[0]);
      for (Object objLine : paidReceiptsLinesQuery.list()) {
        Object[] objpaidReceiptsLines = (Object[]) objLine;
        JSONObject paidReceiptLine = new JSONObject();

        paidReceiptLine.put("id", objpaidReceiptsLines[0]);
        paidReceiptLine.put("name", objpaidReceiptsLines[1]);
        paidReceiptLine.put("uOM", objpaidReceiptsLines[2]);
        paidReceiptLine.put("quantity", objpaidReceiptsLines[3]);
        paidReceiptLine.put("unitPrice", objpaidReceiptsLines[4]);

        // promotions per line
        OBCriteria<OrderLineOffer> qPromotions = OBDal.getInstance().createCriteria(
            OrderLineOffer.class);
        qPromotions.add(Restrictions.eq(OrderLineOffer.PROPERTY_SALESORDERLINE + ".id",
            (String) objpaidReceiptsLines[6]));
        qPromotions.addOrder(Order.asc(OrderLineOffer.PROPERTY_LINENO));
        JSONArray promotions = new JSONArray();
        boolean hasPromotions = false;
        for (OrderLineOffer promotion : qPromotions.list()) {
          BigDecimal displayedAmount = promotion.getDisplayedTotalAmount();
          if (displayedAmount == null) {
            displayedAmount = promotion.getTotalAmount();
          }

          JSONObject jsonPromo = new JSONObject();
          String name = promotion.getPriceAdjustment().getPrintName() != null ? promotion
              .getPriceAdjustment().getPrintName() : promotion.getPriceAdjustment().getName();
          jsonPromo.put("ruleId", promotion.getPriceAdjustment().getId());
          jsonPromo.put("name", name);
          jsonPromo.put("amt", displayedAmount);
          jsonPromo.put("actualAmt", promotion.getTotalAmount());
          jsonPromo.put("hidden", BigDecimal.ZERO.equals(displayedAmount));
          promotions.put(jsonPromo);
          hasPromotions = true;
        }

        BigDecimal lineAmount;
        if (hasPromotions) {
          // When it has promotions, show line amount without them as they are shown after it
          lineAmount = ((BigDecimal) objpaidReceiptsLines[4])
              .multiply((BigDecimal) objpaidReceiptsLines[3]);
        } else {
          lineAmount = (BigDecimal) objpaidReceiptsLines[5];
        }
        paidReceiptLine.put("linegrossamount", lineAmount);

        paidReceiptLine.put("promotions", promotions);

        listpaidReceiptsLines.put(paidReceiptLine);
      }
      paidReceipt.put("receiptLines", listpaidReceiptsLines);

      JSONArray listPaymentsIn = new JSONArray();
      String hqlPaymentsIn = "select scheduleDetail.paymentDetails.finPayment.amount, scheduleDetail.paymentDetails.finPayment.account.id "
          + "from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=?";
      Query paymentsInQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsIn);
      // paidReceiptsQuery.setString(0, id);
      paymentsInQuery.setString(0, (String) objpaidReceipts[0]);
      for (Object objPaymentIn : paymentsInQuery.list()) {
        Object[] objPaymentsIn = (Object[]) objPaymentIn;
        JSONObject paymentsIn = new JSONObject();
        paymentsIn.put("amount", objPaymentsIn[0]);
        paymentsIn.put("account", objPaymentsIn[1]);
        listPaymentsIn.put(paymentsIn);
      }

      JSONArray listpaidReceiptsPayments = new JSONArray();

      JSONArray listPaymentsType = new JSONArray();
      String hqlPaymentsType = "select p.commercialName as name, p.financialAccount.id as account"
          + " from OBPOS_App_Payment as p where p.obposApplications.id=? ";
      Query paymentsTypeQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsType);
      // paidReceiptsQuery.setString(0, id);
      paymentsTypeQuery.setString(0, pos);
      for (Object objPaymentType : paymentsTypeQuery.list()) {
        Object[] objPaymentsType = (Object[]) objPaymentType;
        JSONObject paymentsType = new JSONObject();
        paymentsType.put("name", objPaymentsType[0]);
        paymentsType.put("account", objPaymentsType[1]);
        listPaymentsType.put(paymentsType);
      }
      for (int i = 0; i < listPaymentsIn.length(); i++) {
        JSONObject objectIn = (JSONObject) listPaymentsIn.get(i);
        for (int j = 0; j < listPaymentsType.length(); j++) {
          JSONObject objectType = (JSONObject) listPaymentsType.get(j);
          if (objectIn.get("account").equals(objectType.get("account"))) {
            JSONObject paidReceiptPayment = new JSONObject();
            // FIXME: Multicurrency problem, amount always in terminal currency
            paidReceiptPayment.put("amount", objectIn.get("amount"));
            paidReceiptPayment.put("name", objectType.get("name"));
            listpaidReceiptsPayments.put(paidReceiptPayment);
          }
        }
      }

      paidReceipt.put("receiptPayments", listpaidReceiptsPayments);

      String hqlReceiptTaxes = "select orderTax.tax.id as taxId, orderTax.tax.rate as rate, orderTax.taxableAmount as taxableamount, orderTax.taxAmount as taxamount, orderTax.tax.name as name from OrderTax as orderTax where orderTax.salesOrder.id=?";
      Query ReceiptTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlReceiptTaxes);
      ReceiptTaxesQuery.setString(0, (String) objpaidReceipts[0]);
      JSONArray jsonListTaxes = new JSONArray();
      for (Object objTax : ReceiptTaxesQuery.list()) {
        Object[] objTaxInfo = (Object[]) objTax;
        JSONObject jsonObjTaxes = new JSONObject();
        jsonObjTaxes.put("taxid", objTaxInfo[0]);
        jsonObjTaxes.put("rate", objTaxInfo[1]);
        jsonObjTaxes.put("net", objTaxInfo[2]);
        jsonObjTaxes.put("amount", objTaxInfo[3]);
        jsonObjTaxes.put("name", objTaxInfo[4]);
        jsonObjTaxes.put("gross", new BigDecimal((String) objTaxInfo[2].toString())
            .add(new BigDecimal((String) objTaxInfo[3].toString())));
        jsonListTaxes.put(jsonObjTaxes);
      }

      paidReceipt.put("receiptTaxes", jsonListTaxes);

      respArray.put(paidReceipt);
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}