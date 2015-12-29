/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.DateFormat;
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
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.service.json.JsonConstants;

public class PaidReceipts extends JSONProcessSimple {
  public static final String paidReceiptsPropertyExtension = "PRExtension";
  public static final String paidReceiptsLinesPropertyExtension = "PRExtensionLines";
  public static final String paidReceiptsShipLinesPropertyExtension = "PRExtensionShipLines";

  @Inject
  @Any
  @Qualifier(paidReceiptsPropertyExtension)
  private Instance<ModelExtension> extensions;
  @Inject
  @Any
  @Qualifier(paidReceiptsLinesPropertyExtension)
  private Instance<ModelExtension> extensionsLines;
  @Inject
  @Any
  @Qualifier(paidReceiptsShipLinesPropertyExtension)
  private Instance<ModelExtension> extensionsShipLines;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      JSONArray respArray = new JSONArray();

      String orderid = jsonsent.getString("orderid");

      // get the orderId
      HQLPropertyList hqlProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlPaidReceipts = "select "
          + hqlProperties.getHqlSelect()
          + //
          " from Order as ord LEFT OUTER JOIN ord.obposApplications AS pos LEFT OUTER JOIN ord.salesRepresentative as salesRepresentative"
          + //
          " where ord.id = :orderId";
      Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
      paidReceiptsQuery.setString("orderId", orderid);

      // cycle through the lines of the selected order
      for (Object obj : paidReceiptsQuery.list()) {
        Object[] objpaidReceipts = (Object[]) obj;
        JSONObject paidReceipt = hqlProperties.getJSONObjectRow(objpaidReceipts);
        paidReceipt.put("orderid", orderid);
        // orderDate is a date so we don't need to transform the time information
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        int orderDatePropertyIndex = hqlProperties.getHqlPropertyIndex("orderDate");
        if (orderDatePropertyIndex != -1) {
          String nowAsISO = df.format(objpaidReceipts[orderDatePropertyIndex]);
          paidReceipt.put("orderDate", nowAsISO);
        }
        // get the Invoice for the Order
        String hqlPaidReceiptsInvoice = "select inv.id from Invoice as inv where inv.salesOrder.id = :orderId";
        Query PaidReceiptsInvoiceQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaidReceiptsInvoice);
        PaidReceiptsInvoiceQuery.setString("orderId", orderid);
        if (!PaidReceiptsInvoiceQuery.list().isEmpty())
          paidReceipt.put("generateInvoice", true);

        JSONArray listpaidReceiptsLines = new JSONArray();

        // get the details of each line
        HQLPropertyList hqlPropertiesLines = ModelExtensionUtils
            .getPropertyExtensions(extensionsLines);
        String hqlPaidReceiptsLines = "select " + hqlPropertiesLines.getHqlSelect() + //
            "  from OrderLine as ordLine " + //
            " where ordLine.salesOrder.id=? "; //

        hqlPaidReceiptsLines += " order by ordLine.lineNo";
        Query paidReceiptsLinesQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaidReceiptsLines);
        paidReceiptsLinesQuery.setString(0, orderid);

        for (Object objLine : paidReceiptsLinesQuery.list()) {
          Object[] objpaidReceiptsLines = (Object[]) objLine;
          JSONObject paidReceiptLine = new JSONObject();

          // add the received database information into the working object (paidReceiptLine)
          int i = 0;
          for (HQLProperty property : hqlPropertiesLines.getProperties()) {
            paidReceiptLine.put(property.getJsonName(), objpaidReceiptsLines[i]);
            i++;
          }
          paidReceiptLine.put("priceIncludesTax", paidReceipt.getBoolean("priceIncludesTax"));

          // get shipmentLines for returns

          HQLPropertyList hqlPropertiesShipLines = ModelExtensionUtils
              .getPropertyExtensions(extensionsShipLines);
          String hqlPaidReceiptsShipLines = "select " + hqlPropertiesShipLines.getHqlSelect() //
              + " from MaterialMgmtShipmentInOutLine as m where salesOrderLine.id= ? ";
          OBDal.getInstance().getSession().createQuery(hqlPaidReceiptsShipLines);
          Query paidReceiptsShipLinesQuery = OBDal.getInstance().getSession()
              .createQuery(hqlPaidReceiptsShipLines);
          paidReceiptsShipLinesQuery.setString(0, (String) objpaidReceiptsLines[6]);

          // cycle through the lines of the selected order
          JSONArray shipmentlines = new JSONArray();
          for (Object objShipLines : paidReceiptsShipLinesQuery.list()) {

            JSONObject jsonShipline = new JSONObject();
            Object[] objpaidReceiptsShipLines = (Object[]) objShipLines;
            jsonShipline.put("shipLineId", objpaidReceiptsShipLines[0]);
            jsonShipline.put("shipment", objpaidReceiptsShipLines[1]);
            jsonShipline.put("shipmentlineNo", objpaidReceiptsShipLines[2]);
            jsonShipline.put("qty", objpaidReceiptsShipLines[3]);
            jsonShipline.put("remainingQty", objpaidReceiptsShipLines[4]);
            shipmentlines.put(jsonShipline);
          }
          paidReceiptLine.put("shipmentlines", shipmentlines);

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
            if (promotion.getObdiscIdentifier() != null) {
              jsonPromo.put("identifier", promotion.getObdiscIdentifier());
            }
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

        // TODO: make this extensible
        String hqlPaymentsIn = "select scheduleDetail.paymentDetails.finPayment.amount, scheduleDetail.paymentDetails.finPayment.account.id, scheduleDetail.paymentDetails.finPayment.paymentDate, scheduleDetail.paymentDetails.finPayment.id "
            + "from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=? "
            + "order by scheduleDetail.paymentDetails.finPayment.documentNo";
        Query paymentsInQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsIn);
        // paidReceiptsQuery.setString(0, id);
        paymentsInQuery.setString(0, orderid);
        for (Object objPaymentIn : paymentsInQuery.list()) {
          Object[] objPaymentsIn = (Object[]) objPaymentIn;
          JSONObject paymentsIn = new JSONObject();
          paymentsIn.put("amount", objPaymentsIn[0]);
          paymentsIn.put("account", objPaymentsIn[1]);
          String dateFormat = "yyyy-MM-dd";
          SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
          paymentsIn.put("paymentDate", outputFormat.format(((Date) objPaymentsIn[2])));
          paymentsIn.put("paymentId", objPaymentsIn[3]);
          listPaymentsIn.put(paymentsIn);
        }

        JSONArray listpaidReceiptsPayments = new JSONArray();
        JSONArray listPaymentsType = new JSONArray();

        // TODO: make this extensible
        String hqlPaymentsType = "select p.commercialName as name, p.financialAccount.id as account, p.searchKey as searchKey, "
            + "c_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as rate, "
            + "c_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate, "
            + "p.financialAccount.currency.iSOCode as isocode, "
            + "p.paymentMethod.openDrawer as openDrawer "
            + " from OBPOS_App_Payment as p where p.financialAccount.id in (select scheduleDetail.paymentDetails.finPayment.account.id from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=?)"
            + "group by  p.financialAccount.id, p.commercialName ,p.searchKey,"
            + "c_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id),"
            + "c_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id),"
            + "p.financialAccount.currency.iSOCode ,p.paymentMethod.openDrawer";
        Query paymentsTypeQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsType);
        // paidReceiptsQuery.setString(0, id);
        paymentsTypeQuery.setString(0, orderid);
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
          boolean added = false;
          for (int j = 0; j < listPaymentsType.length(); j++) {
            JSONObject objectType = (JSONObject) listPaymentsType.get(j);
            if (objectIn.get("account").equals(objectType.get("account"))) {
              JSONObject paidReceiptPayment = new JSONObject();
              // FIXME: Multicurrency problem, amount always in terminal currency
              paidReceiptPayment.put("amount", new BigDecimal((String) objectIn.get("amount")
                  .toString()).multiply(new BigDecimal((String) objectType.get("mulrate")
                  .toString())));
              paidReceiptPayment.put("paymentDate", objectIn.get("paymentDate"));
              paidReceiptPayment.put("name", objectType.get("name"));
              paidReceiptPayment.put("kind", objectType.get("kind"));
              paidReceiptPayment.put("rate", objectType.get("rate"));
              paidReceiptPayment.put("mulrate", objectType.get("mulrate"));
              paidReceiptPayment.put("isocode", objectType.get("isocode"));
              paidReceiptPayment.put("openDrawer", objectType.get("openDrawer"));
              paidReceiptPayment.put("isPrePayment", true);
              added = true;
              listpaidReceiptsPayments.put(paidReceiptPayment);
            }
          }
          if (!added) {
            // The payment type of the current payment is not configured for the webpos

            String hqlPaymentType = "select p.paymentMethod.name as name, p.account.id as account, "
                + "c_currency_rate(p.account.currency, p.organization.currency, null, null, p.client.id, p.organization.id) as rate, "
                + "c_currency_rate(p.organization.currency, p.account.currency, null, null, p.client.id, p.organization.id) as mulrate, "
                + "p.account.currency.iSOCode as isocode " + " from FIN_Payment as p where p.id=?)";
            Query paymentTypeQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentType);
            // paidReceiptsQuery.setString(0, id);
            paymentTypeQuery.setString(0, objectIn.getString("paymentId"));

            if (paymentTypeQuery.list().size() > 0) {

              Object objPaymentType = paymentTypeQuery.list().get(0);
              Object[] objPaymentsType = (Object[]) objPaymentType;
              JSONObject paymentsType = new JSONObject();
              paymentsType.put("name", objPaymentsType[0]);
              paymentsType.put("account", objPaymentsType[1]);
              paymentsType.put("kind", "");
              paymentsType.put("rate", objPaymentsType[2]);
              paymentsType.put("mulrate", objPaymentsType[3]);
              paymentsType.put("isocode", objPaymentsType[4]);
              paymentsType.put("openDrawer", "N");

              JSONObject paidReceiptPayment = new JSONObject();
              // FIXME: Multicurrency problem, amount always in terminal currency
              paidReceiptPayment.put("amount", new BigDecimal((String) objectIn.get("amount")
                  .toString()).multiply(new BigDecimal((String) paymentsType.get("mulrate")
                  .toString())));
              paidReceiptPayment.put("paymentDate", objectIn.get("paymentDate"));
              paidReceiptPayment.put("name", paymentsType.get("name"));
              paidReceiptPayment.put("kind", paymentsType.get("kind"));
              paidReceiptPayment.put("rate", paymentsType.get("rate"));
              paidReceiptPayment.put("mulrate", paymentsType.get("mulrate"));
              paidReceiptPayment.put("isocode", paymentsType.get("isocode"));
              paidReceiptPayment.put("openDrawer", paymentsType.get("openDrawer"));
              paidReceiptPayment.put("isPrePayment", true);
              added = true;
              listpaidReceiptsPayments.put(paidReceiptPayment);
            }

          }
        }

        paidReceipt.put("receiptPayments", listpaidReceiptsPayments);

        // TODO: make this extensible
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

        result.put(JsonConstants.RESPONSE_DATA, respArray);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
    } finally {

      OBContext.restorePreviousMode();
    }
    return result;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}