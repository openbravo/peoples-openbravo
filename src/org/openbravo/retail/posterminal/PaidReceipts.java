/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerRequestExecutor;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.service.json.JsonConstants;

public class PaidReceipts extends JSONProcessSimple {
  public static final Logger log = Logger.getLogger(PaidReceipts.class);

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
  @Inject
  @Any
  private Instance<PaidReceiptsPaymentsTypeHook> paymentsTypeInProcesses;

  @Inject
  @Any
  private Instance<PaidReceiptsPaymentsInHook> paymentsInProcesses;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    if (MobileServerController.getInstance().getCentralServer() != null) {
      final String ORIGIN_CENTRAL = MobileServerController.getInstance().getCentralServer()
          .getName();
      if (MobileServerController.getInstance().isThisAStoreServer()
          && ORIGIN_CENTRAL.equals(jsonsent.optString("originServer"))) {
        return MobileServerRequestExecutor.getInstance().executeCentralRequest(
            MobileServerUtils.OBWSPATH + PaidReceipts.class.getName(), jsonsent);
      }
    }
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      JSONArray respArray = new JSONArray();
      List<String> orderIds = new ArrayList<String>();

      final DateFormat parseDateFormat = (DateFormat) POSUtils.dateFormatUTC.clone();
      parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      final DateFormat paymentDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
      paymentDateFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone()
          .getID()));

      String orderid = jsonsent.getString("orderid");

      // get the orderId
      HQLPropertyList hqlPropertiesReceipts = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlPaidReceipts = "select " + hqlPropertiesReceipts.getHqlSelect()
          + " from Order as ord LEFT OUTER JOIN ord.obposApplications AS pos "
          + " LEFT OUTER JOIN ord.salesRepresentative as salesRepresentative "
          + " LEFT OUTER JOIN ord.replacedorder AS replacedOrder where ord.id = :orderId";
      Query paidReceiptsQuery = OBDal.getInstance().getSession().createQuery(hqlPaidReceipts);
      paidReceiptsQuery.setString("orderId", orderid);

      // cycle through the lines of the selected order
      JSONArray paidReceipts = hqlPropertiesReceipts.getJSONArray(paidReceiptsQuery);

      for (int receipt = 0; receipt < paidReceipts.length(); receipt++) {
        JSONObject paidReceipt = paidReceipts.getJSONObject(receipt);
        if (orderIds.indexOf(orderid) == -1) {
          orderIds.add(orderid);
        }

        paidReceipt.put("orderid", orderid);

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
            "  left join ordLine.returnReason as returnReason " + //
            " where ordLine.salesOrder.id=? and ordLine.obposIsDeleted = false"; //
        hqlPaidReceiptsLines += " order by ordLine.lineNo";
        Query paidReceiptsLinesQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaidReceiptsLines);
        paidReceiptsLinesQuery.setString(0, orderid);

        JSONArray paidReceiptsLines = hqlPropertiesLines.getJSONArray(paidReceiptsLinesQuery);

        for (int receiptLine = 0; receiptLine < paidReceiptsLines.length(); receiptLine++) {
          JSONObject paidReceiptLine = paidReceiptsLines.getJSONObject(receiptLine);
          if (orderIds.indexOf((String) paidReceiptLine.get("orderId")) == -1) {
            orderIds.add((String) paidReceiptLine.get("orderId"));
          }
          paidReceiptLine.put("priceIncludesTax", paidReceipt.getBoolean("priceIncludesTax"));

          // get shipmentLines for returns
          HQLPropertyList hqlPropertiesShipLines = ModelExtensionUtils
              .getPropertyExtensions(extensionsShipLines);
          String hqlPaidReceiptsShipLines = "select "
              + hqlPropertiesShipLines.getHqlSelect() //
              + " from MaterialMgmtShipmentInOutLine as m where salesOrderLine.id= ? "
              + " and m.shipmentReceipt.isnettingshipment = false";
          OBDal.getInstance().getSession().createQuery(hqlPaidReceiptsShipLines);
          Query paidReceiptsShipLinesQuery = OBDal.getInstance().getSession()
              .createQuery(hqlPaidReceiptsShipLines);
          paidReceiptsShipLinesQuery.setString(0, paidReceiptLine.getString("lineId"));
          paidReceiptLine.put("shipmentlines",
              hqlPropertiesShipLines.getJSONArray(paidReceiptsShipLinesQuery));

          if (paidReceiptLine.has("goodsShipmentLine")
              && !paidReceiptLine.getString("goodsShipmentLine").equals("null")) {
            String hqlShipLines = "select ordLine.goodsShipmentLine.salesOrderLine.salesOrder.documentNo, ordLine.goodsShipmentLine.salesOrderLine.id "
                + "from OrderLine as ordLine where ordLine.id = ? ";
            OBDal.getInstance().getSession().createQuery(hqlShipLines);
            Query shipLines = OBDal.getInstance().getSession().createQuery(hqlShipLines);
            shipLines.setString(0, paidReceiptLine.getString("lineId"));

            for (Object obj : shipLines.list()) {
              Object[] line = (Object[]) obj;
              paidReceiptLine.put("originalDocumentNo", line[0]);
              paidReceiptLine.put("originalOrderLineId", line[1]);
            }
          }

          // taxes per line
          OBCriteria<OrderLineTax> qTaxes = OBDal.getInstance().createCriteria(OrderLineTax.class);
          qTaxes.add(Restrictions.eq(OrderLineTax.PROPERTY_SALESORDERLINE + ".id",
              (String) paidReceiptLine.getString("lineId")));
          qTaxes.addOrder(Order.asc(OrderLineTax.PROPERTY_LINENO));
          JSONArray taxes = new JSONArray();
          for (OrderLineTax tax : qTaxes.list()) {
            JSONObject jsonTax = new JSONObject();
            jsonTax.put("taxId", tax.getTax().getId());
            jsonTax.put("identifier", tax.getTax().getName());
            jsonTax.put("taxAmount", tax.getTaxAmount());
            jsonTax.put("taxableAmount", tax.getTaxableAmount());
            jsonTax.put("taxRate", tax.getTax().getRate());
            jsonTax.put("docTaxAmount", tax.getTax().getDocTaxAmount());
            jsonTax.put("lineNo", tax.getTax().getLineNo());
            jsonTax.put("cascade", tax.getTax().isCascade());
            taxes.put(jsonTax);
          }

          paidReceiptLine.put("taxes", taxes);

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
            jsonPromo.put("discountType", promotion.getPriceAdjustment().getDiscountType().getId());
            jsonPromo.put("name", name);
            jsonPromo.put("amt", displayedAmount);
            jsonPromo.put("actualAmt", promotion.getTotalAmount());
            jsonPromo.put("hidden", BigDecimal.ZERO.equals(displayedAmount));
            jsonPromo.put("displayedTotalAmount", promotion.getDisplayedTotalAmount());
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
                jsonObject.put("otherTicket", true);
                jsonObject.put("deferred", true);
                if (orderIds.indexOf((String) jsonObject.getString("orderId")) == -1) {
                  orderIds.add((String) jsonObject.getString("orderId"));
                }
              } else {
                jsonObject.put("otherTicket", false);
                jsonObject.put("deferred", false);
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
        String hqlPaymentsIn = "select " + hqlPropertiesPayments.getHqlSelect()
            + "from FIN_Payment_ScheduleDetail as scheduleDetail "
            + "join scheduleDetail.paymentDetails as paymentDetail "
            + "join paymentDetail.finPayment as finPayment "
            + "join scheduleDetail.orderPaymentSchedule.order as order "
            + "left join finPayment.reversedPayment as reversedPayment "
            + "left join finPayment.obposAppCashup as obposAppCashup "
            + "left join finPayment.oBPOSPOSTerminal as oBPOSPOSTerminal "
            + "where order.id=? " //
            + "group by " + hqlPropertiesPayments.getHqlGroupBy()
            + " order by finPayment.documentNo";
        Query paidReceiptsPaymentsQuery = OBDal.getInstance().getSession()
            .createQuery(hqlPaymentsIn);
        paidReceiptsPaymentsQuery.setString(0, orderid);
        JSONArray listPaymentsIn = hqlPropertiesPayments.getJSONArray(paidReceiptsPaymentsQuery);

        JSONArray listpaidReceiptsPayments = new JSONArray();
        JSONArray listPaymentsType = new JSONArray();

        // TODO: make this extensible
        String hqlPaymentsType = "select p.commercialName as name, p.financialAccount.id as account, p.searchKey as searchKey, "
            + "obpos_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as rate, "
            + "obpos_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate, "
            + "p.financialAccount.currency.iSOCode as isocode, "
            + "p.paymentMethod.openDrawer as openDrawer "
            + " from OBPOS_App_Payment as p where p.financialAccount.id in (select scheduleDetail.paymentDetails.finPayment.account.id from FIN_Payment_ScheduleDetail as scheduleDetail where scheduleDetail.orderPaymentSchedule.order.id=?)"
            + "group by  p.financialAccount.id, p.commercialName ,p.searchKey,"
            + "obpos_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id),"
            + "obpos_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id),"
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
        executeHooks(paymentsTypeInProcesses, null, listPaymentsType, orderid);

        for (int i = 0; i < listPaymentsIn.length(); i++) {
          JSONObject objectIn = (JSONObject) listPaymentsIn.get(i);

          boolean added = false;
          for (int j = 0; j < listPaymentsType.length(); j++) {
            JSONObject objectType = (JSONObject) listPaymentsType.get(j);
            if (objectIn.get("account").equals(objectType.get("account"))) {
              JSONObject paidReceiptPayment = new JSONObject();
              // FIXME: Multicurrency problem, amount always in terminal currency
              BigDecimal objPaymentTrx = BigDecimal.ZERO;
              if (objectIn.getDouble("amount") == objectIn.getDouble("paymentAmount")) {
                objPaymentTrx = BigDecimal
                    .valueOf(objectIn.getDouble("financialTransactionAmount"));
              } else {
                objPaymentTrx = BigDecimal.valueOf(objectIn.getDouble("amount")).multiply(
                    BigDecimal.valueOf(objectType.getDouble("mulrate")));
              }
              paidReceiptPayment.put("amount", objPaymentTrx);
              paidReceiptPayment.put("paymentAmount",
                  BigDecimal.valueOf(objectIn.getDouble("financialTransactionAmount")));
              try {
                Date date = parseDateFormat.parse((String) objectIn.get("paymentDate"));
                paidReceiptPayment.put("paymentDate", paymentDateFormat.format(date));
              } catch (ParseException e) {
                log.error(e.getMessage(), e);
              }

              if (objectIn.has("paymentData")) {
                paidReceiptPayment.put("paymentData",
                    new JSONObject((String) objectIn.get("paymentData")));
              }
              paidReceiptPayment.put("name", objectType.get("name"));
              paidReceiptPayment.put("kind", objectType.get("kind"));
              paidReceiptPayment.put("rate", objectType.get("rate"));
              paidReceiptPayment.put("mulrate", objectType.get("mulrate"));
              paidReceiptPayment.put("isocode", objectType.get("isocode"));
              paidReceiptPayment.put("openDrawer", objectType.get("openDrawer"));
              paidReceiptPayment.put("isPrePayment", true);
              paidReceiptPayment.put("paymentId", objectIn.get("paymentId"));

              if (objectIn.has("reversedPaymentId")) {
                paidReceiptPayment.put("isReversed", true);
              }
              if (objectIn.has("reversedPaymentId")) {
                paidReceiptPayment.put("reversedPaymentId", objectIn.get("reversedPaymentId"));
              }
              paidReceiptPayment.put("obposAppCashup",
                  objectIn.has("cashup") ? objectIn.get("cashup") : null);
              paidReceiptPayment.put("oBPOSPOSTerminal",
                  objectIn.has("posTerminal") ? objectIn.get("posTerminal") : null);
              // Call all payments in processes injected.
              executeHooks(paymentsInProcesses, paidReceiptPayment, null,
                  (String) objectIn.get("paymentId"));
              added = true;
              listpaidReceiptsPayments.put(paidReceiptPayment);
            }
          }
          if (!added) {
            // The payment type of the current payment is not configured for the webpos

            String hqlPaymentType = "select p.paymentMethod.name as name, p.account.id as account, "
                + "obpos_currency_rate(p.account.currency, p.organization.currency, null, null, p.client.id, p.organization.id) as rate, "
                + "obpos_currency_rate(p.organization.currency, p.account.currency, null, null, p.client.id, p.organization.id) as mulrate, "
                + "p.account.currency.iSOCode as isocode " //
                + "from FIN_Payment as p where p.id=?)";
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
              BigDecimal objPaymentTrx = BigDecimal.ZERO;
              if (objectIn.getDouble("amount") == objectIn.getDouble("paymentAmount")) {
                objPaymentTrx = BigDecimal
                    .valueOf(objectIn.getDouble("financialTransactionAmount"));
              } else {
                objPaymentTrx = BigDecimal.valueOf(objectIn.getDouble("amount")).multiply(
                    BigDecimal.valueOf(paymentsType.getDouble("mulrate")));
              }
              paidReceiptPayment.put("amount", objPaymentTrx);
              paidReceiptPayment.put("paymentAmount",
                  BigDecimal.valueOf(objectIn.getDouble("financialTransactionAmount")));
              try {
                Date date = parseDateFormat.parse((String) objectIn.get("paymentDate"));
                paidReceiptPayment.put("paymentDate", paymentDateFormat.format(date));
              } catch (ParseException e) {
                log.error(e.getMessage(), e);
              }

              if (objectIn.has("paymentData")) {
                paidReceiptPayment.put("paymentData",
                    new JSONObject((String) objectIn.get("paymentData")));
              }
              paidReceiptPayment.put("name", paymentsType.get("name"));
              paidReceiptPayment.put("kind", paymentsType.get("kind"));
              paidReceiptPayment.put("rate", paymentsType.get("rate"));
              paidReceiptPayment.put("mulrate", paymentsType.get("mulrate"));
              paidReceiptPayment.put("isocode", paymentsType.get("isocode"));
              paidReceiptPayment.put("openDrawer", paymentsType.get("openDrawer"));
              paidReceiptPayment.put("isPrePayment", true);
              paidReceiptPayment.put("paymentId", objectIn.get("paymentId"));
              if (objectIn.has("reversedPaymentId")) {
                paidReceiptPayment.put("isReversed", true);
              }
              if (objectIn.has("reversedPaymentId")) {
                paidReceiptPayment.put("reversedPaymentId", objectIn.get("reversedPaymentId"));
              }
              paidReceiptPayment.put("obposAppCashup",
                  objectIn.has("cashup") ? objectIn.get("cashup") : null);
              paidReceiptPayment.put("oBPOSPOSTerminal",
                  objectIn.has("posTerminal") ? objectIn.get("posTerminal") : null);
              added = true;
              listpaidReceiptsPayments.put(paidReceiptPayment);
            }

          }
        }

        paidReceipt.put("receiptPayments", listpaidReceiptsPayments);

        // TODO: make this extensible
        String hqlReceiptTaxes = "select orderTax.tax.id as taxId, orderTax.tax.rate as rate, orderTax.taxableAmount as taxableamount, orderTax.taxAmount as taxamount, orderTax.tax.name as name, orderTax.tax.cascade as cascade, orderTax.tax.docTaxAmount as docTaxAmount, orderTax.tax.lineNo as lineNo, orderTax.tax.taxBase.id as taxBase from OrderTax as orderTax where orderTax.salesOrder.id=?";
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
          jsonObjTaxes.put("cascade", objTaxInfo[5]);
          jsonObjTaxes.put("docTaxAmount", objTaxInfo[6]);
          jsonObjTaxes.put("lineNo", objTaxInfo[7]);
          jsonObjTaxes.put("taxBase", objTaxInfo[8]);
          jsonListTaxes.put(jsonObjTaxes);
        }
        paidReceipt.put("receiptTaxes", jsonListTaxes);

        // Approvals
        if (paidReceipt.getBoolean("isLayaway")) {
          final String hqlApproval = "select a.approvalType, a.userContact.id "
              + "from OBPOS_Order_Approval a where a.salesOrder.id = ?";
          Query queryApprovals = OBDal.getInstance().getSession().createQuery(hqlApproval);
          queryApprovals.setString(0, orderid);
          JSONArray jsonListApproval = new JSONArray();
          for (Object objApproval : queryApprovals.list()) {
            Object[] objApprovalInfo = (Object[]) objApproval;
            JSONObject jsonObjApproval = new JSONObject();
            jsonObjApproval.put("approvalType", objApprovalInfo[0]);
            jsonObjApproval.put("userContact", objApprovalInfo[1]);
            jsonListApproval.put(jsonObjApproval);
          }
          paidReceipt.put("approvedList", jsonListApproval);
        }

        paidReceipt.put("recordInImportEntry", checkOrderInErrorEntry(orderIds));

        respArray.put(paidReceipt);

        result.put(JsonConstants.RESPONSE_DATA, respArray);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
    } catch (Exception e) {
      throw new OBException("Error in PaidReceips: ", e);
    } finally {

      OBContext.restorePreviousMode();
    }
    return result;
  }

  protected void executeHooks(Instance<? extends Object> hooks, JSONObject paymentIn,
      JSONArray paymentsTypes, String id) throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof PaidReceiptsPaymentsInHook) {
        ((PaidReceiptsPaymentsInHook) proc).exec(paymentIn, id);
      } else if (proc instanceof PaidReceiptsPaymentsTypeHook) {
        ((PaidReceiptsPaymentsTypeHook) proc).exec(paymentsTypes, id);
      }
    }
  }

  private boolean checkOrderInErrorEntry(List<String> orderIds) {
    boolean hasRecord = false;
    final String COMMA = ",";
    StringBuilder idsBuilder = new StringBuilder();
    final String OR = "OR";
    StringBuilder orBuilder = new StringBuilder();
    try {
      for (String id : orderIds) {
        idsBuilder.append(id);
        idsBuilder.append(COMMA);

        orBuilder.append(" imp.jsonInfo like '%" + id + "%' ");
        orBuilder.append(OR);
      }
      String ids = idsBuilder.toString();
      // Remove last comma
      ids = ids.substring(0, ids.length() - COMMA.length());

      String orIds = orBuilder.toString();
      // Remove last OR
      orIds = orIds.substring(0, orIds.length() - OR.length());

      // OBPOS Errors
      String hqlError = "select line.id from OBPOS_Errors_Line line inner join line.obposErrors error "
          + "where error.client.id = ? and line.recordID in (?) and error.typeofdata = 'Order' and error.orderstatus = 'N' ";
      Query errorQuery = OBDal.getInstance().getSession().createQuery(hqlError);
      errorQuery.setString(0, OBContext.getOBContext().getCurrentClient().getId());
      errorQuery.setString(1, ids);
      if (errorQuery.list().size() > 0) {
        return true;
      }

      String hqlError2 = "select imp.id from C_IMPORT_ENTRY imp "
          + "where imp.client.id = ? and imp.typeofdata = 'Order' and imp.importStatus = 'Error' "
          + "and (" + orIds + ")";
      Query errorQuery2 = OBDal.getInstance().getSession().createQuery(hqlError2);
      errorQuery2.setString(0, OBContext.getOBContext().getCurrentClient().getId());
      if (errorQuery2.list().size() > 0) {
        return true;
      }

    } catch (final Exception e) {
      log.error("Error while checking order in ErrorEntry", e);
    }
    return hasRecord;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

}
