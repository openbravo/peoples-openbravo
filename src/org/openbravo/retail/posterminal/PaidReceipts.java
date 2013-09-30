/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.service.json.JsonConstants;

public class PaidReceipts extends JSONProcessSimple {
  public static final String paidReceiptsPropertyExtension = "PRExtension";

  @Inject
  @Any
  @Qualifier(paidReceiptsPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONArray respArray = new JSONArray();
    OBContext.setAdminMode(true);
    String orderid = jsonsent.getString("orderid");

    HQLPropertyList hqlProperties = ModelExtensionUtils.getPropertyExtensions(extensions);

    String hqlPaidReceipts = "select " + hqlProperties.getHqlSelect() + //
        " from Order as ord " + //
        "where ord.id = :orderId " + //
        "  and ord.obposApplications is not null";

    Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
    paidReceiptsQuery.setString("orderId", orderid);

    for (Object obj : paidReceiptsQuery.list()) {
      Object[] objpaidReceipts = (Object[]) obj;
      JSONObject paidReceipt = hqlProperties.getJSONObjectRow(objpaidReceipts);
      paidReceipt.put("orderid", orderid);

      JSONArray listpaidReceiptsLines = new JSONArray();

      // TODO: make this extensible
      String hqlPaidReceiptsLines = "select ordLine.product.id as id, ordLine.product.name as name, ordLine.product.uOM.id as uOM, ordLine.orderedQuantity as quantity, "
          + "ordLine.baseGrossUnitPrice as unitPrice, ordLine.lineGrossAmount as linegrossamount, ordLine.id as lineId, ordLine.unitPrice as netPrice , ordLine.salesOrder.currency.pricePrecision as pricePrecision "
    	  + "from OrderLine as ordLine where ordLine.salesOrder.id=?";
      Query paidReceiptsLinesQuery = OBDal.getInstance().getSession()
          .createQuery(hqlPaidReceiptsLines);
      paidReceiptsLinesQuery.setString(0, orderid);
      for (Object objLine : paidReceiptsLinesQuery.list()) {
        Object[] objpaidReceiptsLines = (Object[]) objLine;
        JSONObject paidReceiptLine = new JSONObject();

        paidReceiptLine.put("id", objpaidReceiptsLines[0]);
        paidReceiptLine.put("name", objpaidReceiptsLines[1]);
        paidReceiptLine.put("uOM", objpaidReceiptsLines[2]);
        paidReceiptLine.put("quantity", objpaidReceiptsLines[3]);
        paidReceiptLine.put("unitPrice", objpaidReceiptsLines[4]);
        paidReceiptLine.put("netPrice", objpaidReceiptsLines[7]);

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
          if (!paidReceipt.getBoolean("priceIncludesTax")) {
            paidReceiptLine.put("netPrice", ((BigDecimal) objpaidReceiptsLines[7])
                .add(displayedAmount.divide((BigDecimal) objpaidReceiptsLines[3], new Integer(String.valueOf(objpaidReceiptsLines[8])).intValue(), BigDecimal.ROUND_HALF_UP)));
          }
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
      String hqlPaymentsIn = "select scheduleDetail.paymentDetails.finPayment.amount, scheduleDetail.paymentDetails.finPayment.account.id, scheduleDetail.paymentDetails.finPayment.paymentDate "
          + "from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=?";
      Query paymentsInQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsIn);
      // paidReceiptsQuery.setString(0, id);
      paymentsInQuery.setString(0, orderid);
      for (Object objPaymentIn : paymentsInQuery.list()) {
        Object[] objPaymentsIn = (Object[]) objPaymentIn;
        JSONObject paymentsIn = new JSONObject();
        paymentsIn.put("amount", objPaymentsIn[0]);
        paymentsIn.put("account", objPaymentsIn[1]);
        String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("dateFormat.java");
        SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
        paymentsIn.put("paymentDate", outputFormat.format(((Date) objPaymentsIn[2])));
        listPaymentsIn.put(paymentsIn);
      }

      JSONArray listpaidReceiptsPayments = new JSONArray();

      JSONArray listPaymentsType = new JSONArray();
      String hqlPaymentsType = "select p.commercialName as name, p.financialAccount.id as account, p.searchKey as searchKey, "
          + "c_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as rate, "
          + "c_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate, "
          + "p.financialAccount.currency.iSOCode as isocode, "
          + "p.paymentMethod.openDrawer as openDrawer "
          + " from OBPOS_App_Payment as p where p.obposApplications.id=? ";
      Query paymentsTypeQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsType);
      // paidReceiptsQuery.setString(0, id);
      paymentsTypeQuery.setString(0, paidReceipt.getString("posTerminal"));
      for (Object objPaymentType : paymentsTypeQuery.list()) {
        Object[] objPaymentsType = (Object[]) objPaymentType;
        JSONObject paymentsType = new JSONObject();
        paymentsType.put("name", objPaymentsType[0]);
        paymentsType.put("account", objPaymentsType[1]);
        paymentsType.put("kind", objPaymentsType[2]);
        paymentsType.put("rate", objPaymentsType[3]);
        paymentsType.put("mulrate", objPaymentsType[4]);
        paymentsType.put("isocode", objPaymentsType[5]);
        paymentsType.put("openDrawer", objPaymentsType[6]);
        listPaymentsType.put(paymentsType);
      }
      for (int i = 0; i < listPaymentsIn.length(); i++) {
        JSONObject objectIn = (JSONObject) listPaymentsIn.get(i);
        for (int j = 0; j < listPaymentsType.length(); j++) {
          JSONObject objectType = (JSONObject) listPaymentsType.get(j);
          if (objectIn.get("account").equals(objectType.get("account"))) {
            JSONObject paidReceiptPayment = new JSONObject();
            // FIXME: Multicurrency problem, amount always in terminal currency
            paidReceiptPayment.put("amount", new BigDecimal((String) objectIn.get("amount")
                .toString())
                .multiply(new BigDecimal((String) objectType.get("mulrate").toString())));
            paidReceiptPayment.put("paymentDate", objectIn.get("paymentDate"));
            paidReceiptPayment.put("name", objectType.get("name"));
            paidReceiptPayment.put("kind", objectType.get("kind"));
            paidReceiptPayment.put("rate", objectType.get("rate"));
            paidReceiptPayment.put("mulrate", objectType.get("mulrate"));
            paidReceiptPayment.put("isocode", objectType.get("isocode"));
            paidReceiptPayment.put("openDrawer", objectType.get("openDrawer"));
            paidReceiptPayment.put("isPrePayment", true);
            listpaidReceiptsPayments.put(paidReceiptPayment);
          }
        }
      }

      paidReceipt.put("receiptPayments", listpaidReceiptsPayments);

      String hqlReceiptTaxes = "select orderTax.tax.id as taxId, orderTax.tax.rate as rate, orderTax.taxableAmount as taxableamount, orderTax.taxAmount as taxamount, orderTax.tax.name as name from OrderTax as orderTax where orderTax.salesOrder.id=?";
      Query ReceiptTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlReceiptTaxes);
      ReceiptTaxesQuery.setString(0, orderid);
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

  @Override
  protected String getProperty() {
    return "OBPOS_retail.paidReceipts";
  }
}