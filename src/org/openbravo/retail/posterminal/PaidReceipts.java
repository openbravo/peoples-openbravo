/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.service.json.JsonConstants;

public class PaidReceipts extends JSONProcessSimple {
  public static final String paidReceiptsPropertyExtension = "PRExtension";
  public static final String paidReceiptsLinesPropertyExtension = "PRExtensionLines";
  public static final String paidReceiptsShipLinesPropertyExtension = "PRExtensionShipLines";
  public static final String paidReceiptsRelatedLinesPropertyExtension = "PRExtensionRelatedLines";
  public static final String paidReceiptsPaymentsPropertyExtension = "PRExtensionPayments";

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
  @Inject
  @Any
  @Qualifier(paidReceiptsRelatedLinesPropertyExtension)
  private Instance<ModelExtension> extensionsRelatedLines;
  @Inject
  @Any
  @Qualifier(paidReceiptsPaymentsPropertyExtension)
  private Instance<ModelExtension> extensionsPayments;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      JSONArray respArray = new JSONArray();

      String orderid = jsonsent.getString("orderid");

      // get the orderId
      HQLPropertyList hqlPropertiesReceipts = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlPaidReceipts = "select " + hqlPropertiesReceipts.getHqlSelect()
          + " from Order as ord LEFT OUTER JOIN ord.obposApplications AS pos "
          + " LEFT OUTER JOIN ord.salesRepresentative as salesRepresentative "
          + " LEFT OUTER JOIN ord.replacedorder AS replacedOrder where ord.id = :orderId";
      Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
      paidReceiptsQuery.setString("orderId", orderid);

      // get the timezoneOffset
      final long timezoneOffset = ((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar
          .getInstance().get(Calendar.DST_OFFSET)) / (60 * 1000));
      final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

      // cycle through the lines of the selected order
      JSONArray paidReceipts = hqlPropertiesReceipts.getJSONArray(paidReceiptsQuery);

      for (int receipt = 0; receipt < paidReceipts.length(); receipt++) {
        JSONObject paidReceipt = paidReceipts.getJSONObject(receipt);

        paidReceipt.put("orderid", orderid);

        Date creationDate = OBMOBCUtils.calculateClientDatetime(
            paidReceipt.getString("creationDate"), timezoneOffset);
        paidReceipt.put("creationDate", df.format(creationDate));

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
            " where ordLine.salesOrder.id=? and ordLine.obposIsDeleted = false"; //
        hqlPaidReceiptsLines += " order by ordLine.lineNo";
        Query paidReceiptsLinesQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaidReceiptsLines);
        paidReceiptsLinesQuery.setString(0, orderid);

        JSONArray paidReceiptsLines = hqlPropertiesLines.getJSONArray(paidReceiptsLinesQuery);

        for (int receiptLine = 0; receiptLine < paidReceiptsLines.length(); receiptLine++) {
          JSONObject paidReceiptLine = paidReceiptsLines.getJSONObject(receiptLine);

          paidReceiptLine.put("priceIncludesTax", paidReceipt.getBoolean("priceIncludesTax"));

          // get shipmentLines for returns
          HQLPropertyList hqlPropertiesShipLines = ModelExtensionUtils
              .getPropertyExtensions(extensionsShipLines);
          String hqlPaidReceiptsShipLines = "select " + hqlPropertiesShipLines.getHqlSelect() //
              + " from MaterialMgmtShipmentInOutLine as m where salesOrderLine.id= ? ";
          OBDal.getInstance().getSession().createQuery(hqlPaidReceiptsShipLines);
          Query paidReceiptsShipLinesQuery = OBDal.getInstance().getSession()
              .createQuery(hqlPaidReceiptsShipLines);
          paidReceiptsShipLinesQuery.setString(0, paidReceiptLine.getString("lineId"));
          paidReceiptLine.put("shipmentlines",
              hqlPropertiesShipLines.getJSONArray(paidReceiptsShipLinesQuery));

          // promotions per line
          OBCriteria<OrderLineOffer> qPromotions = OBDal.getInstance().createCriteria(
              OrderLineOffer.class);
          qPromotions.add(Restrictions.eq(OrderLineOffer.PROPERTY_SALESORDERLINE + ".id",
              (String) paidReceiptLine.getString("lineId")));
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
            lineAmount = (new BigDecimal(paidReceiptLine.optString("quantity"))
                .multiply(new BigDecimal(paidReceiptLine.optString("unitPrice"))));
          } else {
            lineAmount = new BigDecimal(paidReceiptLine.optString("linegrossamount"));
          }
          paidReceiptLine.put("linegrossamount", lineAmount);

          paidReceiptLine.put("promotions", promotions);

          // Related lines
          HQLPropertyList hqlPropertiesRelatedLines = ModelExtensionUtils
              .getPropertyExtensions(extensionsRelatedLines);
          String hqlPaidReceiptsRelatedLines = "select " + hqlPropertiesRelatedLines.getHqlSelect() //
              + " from OrderlineServiceRelation as olsr where salesOrderLine.id = ? " //
              + " order by olsr.orderlineRelated.lineNo";
          OBDal.getInstance().getSession().createQuery(hqlPaidReceiptsShipLines);
          Query paidReceiptsRelatedLinesQuery = OBDal.getInstance().getSession()
              .createQuery(hqlPaidReceiptsRelatedLines);
          paidReceiptsRelatedLinesQuery.setString(0, paidReceiptLine.getString("lineId"));
          JSONArray relatedLines = hqlPropertiesRelatedLines
              .getJSONArray(paidReceiptsRelatedLinesQuery);

          if (relatedLines.length() > 0) {
            for (int r = 0; r < relatedLines.length(); r++) {
              JSONObject jsonObject = relatedLines.getJSONObject(r);
              if (!jsonObject.getString("orderId").equals(orderid)) {
                jsonObject.put("otherTicket", "true");
              }
              String hqlRelatedLinePromotions = "select olo.totalAmount from OrderLineOffer olo where olo.salesOrderLine.id = ?";
              Query relatedLinePromotionsQuery = OBDal.getInstance().getSession()
                  .createQuery(hqlRelatedLinePromotions);
              relatedLinePromotionsQuery.setString(0, jsonObject.getString("orderlineId"));
              JSONArray promos = new JSONArray();
              for (Object promotionAmt : relatedLinePromotionsQuery.list()) {
                JSONObject jsonPromo = new JSONObject();
                jsonPromo.put("amt", (BigDecimal) promotionAmt);
                promos.put(jsonPromo);
              }
              jsonObject.put("promotions", promos);
            }
            paidReceiptLine.put("relatedLines", relatedLines);
          }

          listpaidReceiptsLines.put(paidReceiptLine);
        }
        paidReceipt.put("receiptLines", listpaidReceiptsLines);

        HQLPropertyList hqlPropertiesPayments = ModelExtensionUtils
            .getPropertyExtensions(extensionsPayments);
        String hqlPaymentsIn = "select "
            + hqlPropertiesPayments.getHqlSelect()
            + "from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=? "
            + "order by scheduleDetail.paymentDetails.finPayment.documentNo";
        Query paidReceiptsPaymentsQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaymentsIn);
        paidReceiptsPaymentsQuery.setString(0, orderid);
        JSONArray listPaymentsIn = hqlPropertiesPayments.getJSONArray(paidReceiptsPaymentsQuery);

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
              if (objectIn.has("paymentData")) {
                paidReceiptPayment.put("paymentData", objectIn.get("paymentData"));
              }
              paidReceiptPayment.put("name", objectType.get("name"));
              paidReceiptPayment.put("kind", objectType.get("kind"));
              paidReceiptPayment.put("rate", objectType.get("rate"));
              paidReceiptPayment.put("mulrate", objectType.get("mulrate"));
              paidReceiptPayment.put("isocode", objectType.get("isocode"));
              paidReceiptPayment.put("openDrawer", objectType.get("openDrawer"));
              paidReceiptPayment.put("isPrePayment", true);
              paidReceiptPayment.put("paymentAmount", new BigDecimal(objectIn.get("paymentAmount")
                  .toString()).multiply(new BigDecimal((String) objectType.get("mulrate")
                  .toString())));
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
              if (objectIn.has("paymentData")) {
                paidReceiptPayment.put("paymentData", objectIn.get("paymentData"));
              }
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