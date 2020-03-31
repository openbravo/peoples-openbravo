/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.payment.PaymentTermLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.db.CallStoredProcedure;

public class InvoiceUtils {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<>();
  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<>();
  HashMap<String, JSONArray> invoicelineserviceList;

  public Invoice createNewInvoice(JSONObject jsoninvoice, Order order) {
    final Invoice invoice = OBProvider.getInstance().get(Invoice.class);
    try {

      final ArrayList<OrderLine> invoicelineReferences = new ArrayList<>();
      JSONArray invoicelines = jsoninvoice.getJSONArray("lines");

      for (int i = 0; i < invoicelines.length(); i++) {
        String invoiceLineId = null;
        if (invoicelines.getJSONObject(i).has("orderLineId")) {
          invoiceLineId = invoicelines.getJSONObject(i).getString("orderLineId");
        } else {
          invoiceLineId = invoicelines.getJSONObject(i).getString("id");
        }
        invoicelineReferences.add(OBDal.getInstance().get(OrderLine.class, invoiceLineId));
      }

      createInvoiceAndLines(jsoninvoice, invoice, order, invoicelines, invoicelineReferences);
    } catch (JSONException e) {
      // won't happen
    }

    return invoice;
  }

  private void updateAuditInfo(Invoice invoice, JSONObject jsoninvoice) throws JSONException {
    Long value = jsoninvoice.getLong("created");
    invoice.set("creationDate", new Date(value));
  }

  private DocumentType getInvoiceDocumentType(String orderDocTypeId, boolean isFullInvoice) {
    final DocumentType orderDocType = OBDal.getInstance().get(DocumentType.class, orderDocTypeId);
    final DocumentType invoiceDocType = isFullInvoice ? orderDocType.getDocumentTypeForInvoice()
        : orderDocType.getObposDoctypesimpinvoice();

    if (invoiceDocType == null) {
      throw new OBException(
          String.format(OBMessageUtils.messageBD(isFullInvoice ? "OBPOS_DocTypeInvoiceNotConfigured"
              : "OBPOS_DocTypeSimplifiedInvoiceNotConfigured"), orderDocType.getName()));
    }

    return invoiceDocType;
  }

  private void createInvoiceAndLines(final JSONObject jsoninvoice, final Invoice invoice,
      final Order order, final JSONArray invoicelines,
      final ArrayList<OrderLine> invoicelineReferences) throws JSONException {
    createInvoice(invoice, order, jsoninvoice);
    OBDal.getInstance().save(invoice);
    createInvoiceLines(invoice, order, jsoninvoice, invoicelines, invoicelineReferences);
    updateAuditInfo(invoice, jsoninvoice);

    if (POSUtils.isCrossStore(order, order.getObposApplications())) {
      OBContext.setCrossOrgReferenceAdminMode();
      try {
        OBDal.getInstance().flush();
      } finally {
        OBContext.restorePreviousCrossOrgReferenceMode();
      }
    }
  }

  private void createInvoiceLine(Invoice invoice, Order order, JSONObject jsoninvoice,
      JSONArray invoicelines, ArrayList<OrderLine> lineReferences, int numIter, int pricePrecision,
      ShipmentInOutLine inOutLine, int lineNo, int numLines, int actualLine,
      BigDecimal movementQtyTotal) throws JSONException {
    final OrderLine orderLine = lineReferences.get(numIter);
    final JSONObject jsonInvoiceLine = invoicelines.getJSONObject(numIter);
    final BigDecimal qtyToInvoice = BigDecimal.valueOf(jsonInvoiceLine.getDouble("qty"));
    final BigDecimal lineGrossAmount = BigDecimal
        .valueOf(jsonInvoiceLine.getDouble("lineGrossAmount"));
    final BigDecimal lineNetAmount = BigDecimal.valueOf(jsonInvoiceLine.getDouble("net"));

    if (orderLine.getObposQtyDeleted() != null
        && orderLine.getObposQtyDeleted().compareTo(BigDecimal.ZERO) != 0) {
      return;
    }

    boolean deliveredQtyEqualsToMovementQty = movementQtyTotal
        .compareTo(orderLine.getOrderedQuantity()) == 0;

    if (jsonInvoiceLine.has("description")
        && StringUtils.length(jsonInvoiceLine.getString("description")) > 255) {
      jsonInvoiceLine.put("description",
          StringUtils.substring(jsonInvoiceLine.getString("description"), 0, 255));
    }

    Entity promotionLineEntity = ModelProvider.getInstance().getEntity(InvoiceLineOffer.class);
    final InvoiceLine invoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
    Entity inlineEntity = ModelProvider.getInstance().getEntity(InvoiceLine.class);
    JSONPropertyToEntity.fillBobFromJSON(inlineEntity, invoiceLine, jsonInvoiceLine,
        jsoninvoice.getLong("timezoneOffset"));
    JSONPropertyToEntity.fillBobFromJSON(ModelProvider.getInstance().getEntity(InvoiceLine.class),
        invoiceLine, jsoninvoice, jsoninvoice.getLong("timezoneOffset"));
    invoiceLine.set("creationDate", invoice.getCreationDate());
    invoiceLine.setLineNo((long) lineNo);
    invoiceLine.setDescription(
        jsonInvoiceLine.has("description") ? jsonInvoiceLine.getString("description") : "");
    invoiceLine.setOrganization(invoice.getOrganization());

    BigDecimal movQty = null;
    if (inOutLine != null && inOutLine.getMovementQuantity() != null) {
      movQty = inOutLine.getMovementQuantity();
    } else if (inOutLine == null && movementQtyTotal.compareTo(BigDecimal.ZERO) != 0) {
      movQty = qtyToInvoice.subtract(movementQtyTotal);
    } else {
      movQty = qtyToInvoice;
    }

    BigDecimal ratio = movQty.divide(qtyToInvoice, 32, RoundingMode.HALF_UP);

    BigDecimal qty = movQty;

    // if ratio equals to one, then only one shipment line is related to orderline, then lineNetAmt
    // and gross is populated from JSON
    if (ratio.compareTo(BigDecimal.ONE) != 0) {
      // if there are several shipments line to the same orderline, in the last line of the invoice
      // of this sales order line, the line net amt will be the pending line net amount
      if (numLines > actualLine || (numLines == actualLine && !deliveredQtyEqualsToMovementQty)) {
        invoiceLine.setLineNetAmount(
            orderLine.getUnitPrice().multiply(qty).setScale(pricePrecision, RoundingMode.HALF_UP));
        invoiceLine.setGrossAmount(orderLine.getGrossUnitPrice()
            .multiply(qty)
            .setScale(pricePrecision, RoundingMode.HALF_UP));
      } else {
        BigDecimal partialGrossAmount = BigDecimal.ZERO;
        BigDecimal partialLineNetAmount = BigDecimal.ZERO;
        for (InvoiceLine il : invoice.getInvoiceLineList()) {
          if (il.getSalesOrderLine() != null
              && il.getSalesOrderLine().getId() == orderLine.getId()) {
            partialGrossAmount = partialGrossAmount.add(il.getGrossAmount());
            partialLineNetAmount = partialLineNetAmount.add(il.getLineNetAmount());
          }
        }
        invoiceLine.setLineNetAmount(lineNetAmount.subtract(partialLineNetAmount)
            .setScale(pricePrecision, RoundingMode.HALF_UP));
        invoiceLine.setGrossAmount(lineGrossAmount.subtract(partialGrossAmount)
            .setScale(pricePrecision, RoundingMode.HALF_UP));
      }
    } else {
      invoiceLine.setLineNetAmount(lineNetAmount.setScale(pricePrecision, RoundingMode.HALF_UP));
      invoiceLine.setGrossAmount(lineGrossAmount.setScale(pricePrecision, RoundingMode.HALF_UP));
    }
    invoiceLine.setInvoicedQuantity(qty);
    orderLine.setInvoicedQuantity(
        (orderLine.getInvoicedQuantity() != null ? orderLine.getInvoicedQuantity().add(qty) : qty));
    invoiceLine.setInvoice(invoice);
    invoiceLine.setSalesOrderLine(orderLine);
    invoiceLine.setGoodsShipmentLine(inOutLine);
    invoiceLine.setAttributeSetValue(orderLine.getAttributeSetValue());
    invoice.getInvoiceLineList().add(invoiceLine);
    OBDal.getInstance().save(invoiceLine);

    JSONObject taxes = jsonInvoiceLine.getJSONObject("taxLines");
    @SuppressWarnings("unchecked")
    Iterator<String> itKeys = taxes.keys();
    BigDecimal totalTaxAmount = BigDecimal.ZERO;
    int ind = 0;
    while (itKeys.hasNext()) {
      String taxId = itKeys.next();
      JSONObject jsoninvoiceTax = taxes.getJSONObject(taxId);
      InvoiceLineTax invoicelinetax = OBProvider.getInstance().get(InvoiceLineTax.class);
      TaxRate tax = (TaxRate) OBDal.getInstance()
          .getProxy(ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
      invoicelinetax.setTax(tax);
      invoicelinetax.setOrganization(invoiceLine.getOrganization());

      final BigDecimal taxNetAmt = BigDecimal.valueOf(jsoninvoiceTax.getDouble("net"));
      final BigDecimal taxAmt = BigDecimal.valueOf(jsoninvoiceTax.getDouble("amount"));
      // if ratio equals to one, then only one shipment line is related to orderline, then
      // lineNetAmt and gross is populated from JSON
      if (ratio.compareTo(BigDecimal.ONE) != 0) {
        // if there are several shipments line to the same orderline, in the last line of the
        // splited lines, the tax amount will be calculated as the pending tax amount
        if (numLines > actualLine || (numLines == actualLine && !deliveredQtyEqualsToMovementQty)) {
          invoicelinetax.setTaxableAmount(
              taxNetAmt.multiply(ratio).setScale(pricePrecision, RoundingMode.HALF_UP));
          invoicelinetax
              .setTaxAmount(taxAmt.multiply(ratio).setScale(pricePrecision, RoundingMode.HALF_UP));
          totalTaxAmount = totalTaxAmount.add(invoicelinetax.getTaxAmount());
        } else {
          BigDecimal partialTaxableAmount = BigDecimal.ZERO;
          BigDecimal partialTaxAmount = BigDecimal.ZERO;
          for (InvoiceLineTax ilt : invoice.getInvoiceLineTaxList()) {
            if (ilt.getInvoiceLine().getSalesOrderLine() != null
                && ilt.getInvoiceLine().getSalesOrderLine().getId() == orderLine.getId()
                && ilt.getTax() != null && ilt.getTax().getId() == tax.getId()) {
              partialTaxableAmount = partialTaxableAmount.add(ilt.getTaxableAmount());
              partialTaxAmount = partialTaxAmount.add(ilt.getTaxAmount());
            }
          }
          invoicelinetax.setTaxableAmount(taxNetAmt.subtract(partialTaxableAmount)
              .setScale(pricePrecision, RoundingMode.HALF_UP));
          invoicelinetax.setTaxAmount(
              taxAmt.subtract(partialTaxAmount).setScale(pricePrecision, RoundingMode.HALF_UP));
        }
      } else {
        invoicelinetax.setTaxableAmount(taxNetAmt.setScale(pricePrecision, RoundingMode.HALF_UP));
        invoicelinetax.setTaxAmount(taxAmt.setScale(pricePrecision, RoundingMode.HALF_UP));
      }
      invoicelinetax.setInvoice(invoice);
      invoicelinetax.setInvoiceLine(invoiceLine);
      invoicelinetax.setRecalculate(true);
      invoicelinetax.setLineNo((long) ((ind + 1) * 10));
      ind++;
      invoice.getInvoiceLineTaxList().add(invoicelinetax);
      invoiceLine.getInvoiceLineTaxList().add(invoicelinetax);
      OBDal.getInstance().save(invoicelinetax);
    }

    // Discounts & Promotions
    if (jsonInvoiceLine.has("promotions") && !jsonInvoiceLine.isNull("promotions")
        && !jsonInvoiceLine.getString("promotions").equals("null")) {
      JSONArray jsonPromotions = jsonInvoiceLine.getJSONArray("promotions");
      for (int p = 0; p < jsonPromotions.length(); p++) {
        JSONObject jsonPromotion = jsonPromotions.getJSONObject(p);
        boolean hasActualAmt = jsonPromotion.has("actualAmt");
        if (hasActualAmt && jsonPromotion.getDouble("actualAmt") == 0) {
          continue;
        }

        InvoiceLineOffer promotion = OBProvider.getInstance().get(InvoiceLineOffer.class);
        JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion,
            jsoninvoice.getLong("timezoneOffset"));

        if (hasActualAmt) {
          promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt"))
              .multiply(ratio)
              .setScale(pricePrecision, RoundingMode.HALF_UP));
        } else {
          promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt"))
              .multiply(ratio)
              .setScale(pricePrecision, RoundingMode.HALF_UP));
        }
        promotion.setLineNo((long) ((p + 1) * 10));
        promotion.setId(OBMOBCUtils.getUUIDbyString(invoiceLine.getId() + p));
        promotion.setNewOBObject(true);
        promotion.setInvoiceLine(invoiceLine);
        promotion.setOrganization(invoiceLine.getOrganization());
        invoiceLine.getInvoiceLineOfferList().add(promotion);
      }
    }

  }

  private void createInvoiceLines(Invoice invoice, Order order, JSONObject jsoninvoice,
      JSONArray invoicelines, ArrayList<OrderLine> lineReferences) throws JSONException {
    int pricePrecision = order.getCurrency().getObposPosprecision() == null
        ? order.getCurrency().getPricePrecision().intValue()
        : order.getCurrency().getObposPosprecision().intValue();

    int lineNo = 0;
    for (int i = 0; i < invoicelines.length(); i++) {
      final List<ShipmentInOutLine> iolList = lineReferences.get(i)
          .getMaterialMgmtShipmentInOutLineList();
      BigDecimal movementQtyTotal = BigDecimal.ZERO;
      final List<ShipmentInOutLine> iolNotInvoicedList = new ArrayList<>();
      for (final ShipmentInOutLine iol : iolList) {
        final OBCriteria<InvoiceLine> invoiceLineCriteria = OBDal.getInstance()
            .createCriteria(InvoiceLine.class);
        invoiceLineCriteria.add(Restrictions.eq(
            InvoiceLine.PROPERTY_GOODSSHIPMENTLINE + "." + ShipmentInOutLine.PROPERTY_ID,
            iol.getId()));
        final List<InvoiceLine> invoiceLineList = invoiceLineCriteria.list();
        if (invoiceLineList.size() == 0) {
          iolNotInvoicedList.add(iol);
          movementQtyTotal = movementQtyTotal.add(iol.getMovementQuantity());
        } else {
          BigDecimal invoicedQty = BigDecimal.ZERO;
          for (final InvoiceLine invoiceLine : invoiceLineList) {
            invoicedQty = invoicedQty.add(invoiceLine.getInvoicedQuantity());
          }
          if (invoicedQty.compareTo(iol.getMovementQuantity()) == -1) {
            iolNotInvoicedList.add(iol);
            movementQtyTotal = movementQtyTotal
                .add(iol.getMovementQuantity().subtract(invoicedQty));
          }
        }
      }
      if (iolNotInvoicedList.size() == 0) {
        lineNo = lineNo + 10;
        createInvoiceLine(invoice, order, jsoninvoice, invoicelines, lineReferences, i,
            pricePrecision, null, lineNo, iolNotInvoicedList.size(), 1, BigDecimal.ZERO);
      } else {
        int numIter = 0;
        for (final ShipmentInOutLine iol : iolNotInvoicedList) {
          numIter++;
          lineNo = lineNo + 10;
          createInvoiceLine(invoice, order, jsoninvoice, invoicelines, lineReferences, i,
              pricePrecision, iol, lineNo, iolNotInvoicedList.size(), numIter, movementQtyTotal);
        }
        final BigDecimal qtyToInvoice = BigDecimal
            .valueOf(invoicelines.getJSONObject(i).getDouble("qty"));
        final BigDecimal orderedQty = lineReferences.get(i).getOrderedQuantity();
        if (qtyToInvoice.compareTo(orderedQty) == 0
            && movementQtyTotal.compareTo(orderedQty) == -1) {
          lineNo = lineNo + 10;
          createInvoiceLine(invoice, order, jsoninvoice, invoicelines, lineReferences, i,
              pricePrecision, null, lineNo, iolNotInvoicedList.size(),
              iolNotInvoicedList.size() + 1, movementQtyTotal);
        }
      }
    }
  }

  private void createInvoice(Invoice invoice, Order order, JSONObject jsoninvoice)
      throws JSONException {
    Entity invoiceEntity = ModelProvider.getInstance().getEntity(Invoice.class);
    JSONPropertyToEntity.fillBobFromJSON(invoiceEntity, invoice, jsoninvoice,
        jsoninvoice.getLong("timezoneOffset"));
    if (jsoninvoice.has("id")) {
      invoice.setId(jsoninvoice.getString("id"));
      invoice.setNewOBObject(true);
    }
    int pricePrecision = order.getCurrency().getObposPosprecision() == null
        ? order.getCurrency().getPricePrecision().intValue()
        : order.getCurrency().getObposPosprecision().intValue();

    String description = null;
    if (jsoninvoice.has("Invoice.description")) {
      // in case the description is directly set to Invoice entity, preserve it
      description = jsoninvoice.getString("Invoice.description");
    } else {
      // other case use generic description if present and add relationship to order
      description = OBMessageUtils.getI18NMessage("OBPOS_InvoiceRelatedToOrder", null)
          + jsoninvoice.getString("documentNo");
      if (jsoninvoice.has("description")
          && !StringUtils.isEmpty(jsoninvoice.getString("description"))) {
        description = StringUtils.substring(jsoninvoice.getString("description"), 0,
            255 - description.length() - 1) + "\n" + description;
      }
    }

    invoice.setOrganization(order.getOrganization());
    invoice.setTrxOrganization(order.getTrxOrganization());
    invoice.setDescription(description);
    final DocumentType docType = getInvoiceDocumentType(order.getDocumentType().getId(),
        jsoninvoice.getBoolean("fullInvoice"));
    invoice.setDocumentType(docType);
    invoice.setTransactionDocument(docType);

    final Date orderDate = OBMOBCUtils.calculateServerDatetime(jsoninvoice.getString("orderDate"),
        Long.parseLong(jsoninvoice.getString("timezoneOffset")));
    Date now = new Date();
    invoice.set("creationDate", orderDate.after(now) ? now : orderDate);
    final Date invoiceDate = OBMOBCUtils.stripTime(orderDate);
    invoice.setAccountingDate(invoiceDate);
    invoice.setInvoiceDate(invoiceDate);
    invoice.setSalesTransaction(true);
    invoice.setDocumentStatus("CO");
    invoice.setDocumentAction("RE");
    invoice.setAPRMProcessinvoice("RE");
    invoice.setSalesOrder(order);
    invoice.setPartnerAddress(OBDal.getInstance()
        .getProxy(Location.class, jsoninvoice.getJSONObject("bp").getString("locId")));
    invoice.setProcessed(true);
    invoice.setPaymentMethod(order.getPaymentMethod());
    invoice.setPaymentTerms(order.getPaymentTerms());
    invoice.setGrandTotalAmount(BigDecimal.valueOf(jsoninvoice.getDouble("gross"))
        .setScale(pricePrecision, RoundingMode.HALF_UP));
    invoice.setSummedLineAmount(BigDecimal.valueOf(jsoninvoice.getDouble("net"))
        .setScale(pricePrecision, RoundingMode.HALF_UP));
    invoice.setTotalPaid(BigDecimal.ZERO);
    invoice.setOutstandingAmount((BigDecimal.valueOf(jsoninvoice.getDouble("gross")))
        .setScale(pricePrecision, RoundingMode.HALF_UP));
    invoice.setDueAmount((BigDecimal.valueOf(jsoninvoice.getDouble("gross")))
        .setScale(pricePrecision, RoundingMode.HALF_UP));
    invoice.setUserContact(order.getUserContact());

    // Create invoice tax lines
    JSONObject taxes = jsoninvoice.getJSONObject("taxes");
    @SuppressWarnings("unchecked")
    Iterator<String> itKeys = taxes.keys();
    int i = 0;
    while (itKeys.hasNext()) {
      String taxId = itKeys.next();
      JSONObject jsoninvoiceTax = taxes.getJSONObject(taxId);
      InvoiceTax invoiceTax = OBProvider.getInstance().get(InvoiceTax.class);
      TaxRate tax = (TaxRate) OBDal.getInstance()
          .getProxy(ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
      invoiceTax.setTax(tax);
      invoiceTax.setOrganization(invoice.getOrganization());
      invoiceTax.setTaxableAmount(BigDecimal.valueOf(jsoninvoiceTax.getDouble("net"))
          .setScale(pricePrecision, RoundingMode.HALF_UP));
      invoiceTax.setTaxAmount(BigDecimal.valueOf(jsoninvoiceTax.getDouble("amount"))
          .setScale(pricePrecision, RoundingMode.HALF_UP));
      invoiceTax.setInvoice(invoice);
      invoiceTax.setLineNo((long) ((i + 1) * 10));
      invoiceTax.setRecalculate(true);
      invoiceTax.setId(
          OBMOBCUtils.getUUIDbyString(invoiceTax.getInvoice().getId() + invoiceTax.getLineNo()));
      invoiceTax.setNewOBObject(true);
      i++;
      invoice.getInvoiceTaxList().add(invoiceTax);
    }

  }

  public FIN_PaymentSchedule createPSInvoice(Order order, Invoice invoice) {
    if (invoice.getGrandTotalAmount().compareTo(BigDecimal.ZERO) == 0
        || order.getFINPaymentScheduleList().size() == 0) {
      return null;
    }

    final BigDecimal gross = invoice.getGrandTotalAmount();
    final FIN_PaymentSchedule paymentSchedule = order.getFINPaymentScheduleList().get(0);
    final FIN_PaymentSchedule paymentScheduleInvoice = OBProvider.getInstance()
        .get(FIN_PaymentSchedule.class);
    paymentScheduleInvoice.setOrganization(invoice.getOrganization());
    paymentScheduleInvoice.setCurrency(order.getCurrency());
    paymentScheduleInvoice.setInvoice(invoice);
    paymentScheduleInvoice.setFinPaymentmethod(order.getPaymentMethod());
    paymentScheduleInvoice.setAmount(gross);
    paymentScheduleInvoice.setOutstandingAmount(gross);
    paymentScheduleInvoice.setDueDate(order.getOrderDate());
    paymentScheduleInvoice.setExpectedDate(order.getOrderDate());
    paymentScheduleInvoice.setFINPaymentPriority(order.getFINPaymentPriority());
    invoice.getFINPaymentScheduleList().add(paymentScheduleInvoice);
    OBDal.getInstance().save(paymentScheduleInvoice);

    Date finalSettlementDate = null;

    final OBCriteria<FIN_PaymentScheduleDetail> paymentScheduleCriteria = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    paymentScheduleCriteria.add(
        Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
    paymentScheduleCriteria
        .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE));
    paymentScheduleCriteria
        .add(Restrictions.isNotNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
    paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORGANIZATION,
        paymentSchedule.getOrganization()));
    paymentScheduleCriteria.setFilterOnReadableOrganization(false);
    if (gross.compareTo(BigDecimal.ZERO) != -1) {
      paymentScheduleCriteria
          .addOrder(org.hibernate.criterion.Order.asc(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT));
    } else {
      paymentScheduleCriteria
          .addOrder(org.hibernate.criterion.Order.desc(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT));
    }
    final List<FIN_PaymentScheduleDetail> paymentScheduleList = paymentScheduleCriteria.list();
    BigDecimal paidAmt = BigDecimal.ZERO;
    BigDecimal paymentsAmt = BigDecimal.ZERO;
    for (final FIN_PaymentScheduleDetail remainingPSD : paymentScheduleList) {
      paymentsAmt = paymentsAmt.add(remainingPSD.getAmount());
    }
    // The reversal payments must be added to the same invoice to which are assigned the reversed
    // payment, so is necessary to put out of the list and add to the invoice each time a reversed
    // payment is added
    final List<FIN_PaymentScheduleDetail> reversalPSDList = new ArrayList<>();
    final Map<String, String> reverseRelation = new HashMap<>();
    for (final FIN_PaymentScheduleDetail psd : paymentScheduleList) {
      if (psd.getPaymentDetails().getFinPayment().getReversedPayment() != null) {
        for (final FIN_PaymentScheduleDetail psd2 : paymentScheduleList) {
          if (psd2.getPaymentDetails()
              .getFinPayment()
              .getId()
              .equals(psd.getPaymentDetails().getFinPayment().getReversedPayment().getId())
              && psd2.getPaymentDetails()
                  .getFinPayment()
                  .getAmount()
                  .negate()
                  .compareTo(psd.getPaymentDetails().getFinPayment().getAmount()) == 0) {
            reversalPSDList.add(psd2);
            reverseRelation.put(
                psd.getPaymentDetails().getFinPayment().getReversedPayment().getId(), psd2.getId());
            break;
          }
        }
      }
    }
    for (final FIN_PaymentScheduleDetail reversalPSD : reversalPSDList) {
      paymentScheduleList.remove(reversalPSD);
    }

    BigDecimal amtToDistribute = gross.compareTo(paymentsAmt) == 1 ? paymentsAmt : gross;
    // Create invoice payment schedule details:
    for (int i = 0; i < paymentScheduleList.size(); i++) {
      FIN_PaymentScheduleDetail psd = paymentScheduleList.get(i);
      if (amtToDistribute.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }

      if (finalSettlementDate == null || (psd.getPaymentDetails() != null
          && psd.getPaymentDetails().getFinPayment().getPaymentDate() != null
          && psd.getPaymentDetails()
              .getFinPayment()
              .getPaymentDate()
              .compareTo(finalSettlementDate) > 0)) {
        finalSettlementDate = psd.getPaymentDetails().getFinPayment().getPaymentDate();
      }
      // Do not consider as paid amount if the payment is in a not valid status
      boolean invoicePaidAmounts = POSUtils.isPaidStatus(psd.getPaymentDetails().getFinPayment());

      int amtSign = amtToDistribute.signum();
      FIN_PaymentScheduleDetail reversalPSD = null;
      if (psd.getPaymentDetails().getFinPayment().getReversedPayment() != null) {
        reversalPSD = OBDal.getInstance()
            .get(FIN_PaymentScheduleDetail.class, reverseRelation
                .get(psd.getPaymentDetails().getFinPayment().getReversedPayment().getId()));
      }
      if ((amtSign >= 0 && psd.getAmount().compareTo(amtToDistribute) <= 0)
          || (amtSign == -1 && psd.getAmount().compareTo(amtToDistribute) >= 0)) {
        psd.setInvoicePaymentSchedule(paymentScheduleInvoice);
        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(psd);
        if (reversalPSD == null) {
          amtToDistribute = amtToDistribute.subtract(psd.getAmount());
          paidAmt = paidAmt.add(invoicePaidAmounts ? psd.getAmount() : BigDecimal.ZERO);
        } else {
          reversalPSD.setInvoicePaymentSchedule(paymentScheduleInvoice);
          paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList()
              .add(reversalPSD);
        }
      } else {
        // Create new paymentScheduleDetail:
        FIN_AddPayment.createPSD(amtToDistribute, paymentSchedule, paymentScheduleInvoice,
            psd.getPaymentDetails(), order.getOrganization(), order.getBusinessPartner());

        // Adjust the original payment schedule detail to match the new amount
        psd.setAmount(psd.getAmount().subtract(amtToDistribute));
        OBDal.getInstance().save(psd);

        if (reversalPSD == null) {
          paidAmt = paidAmt.add(invoicePaidAmounts ? amtToDistribute : BigDecimal.ZERO);
          amtToDistribute = BigDecimal.ZERO;
        } else {
          // Create new paymentScheduleDetail for the reverse payment:
          FIN_AddPayment.createPSD(amtToDistribute, paymentSchedule, paymentScheduleInvoice,
              psd.getPaymentDetails(), order.getOrganization(), order.getBusinessPartner());

          // Adjust the original payment schedule detail to match the new amount
          reversalPSD.setAmount(reversalPSD.getAmount().add(amtToDistribute));
          OBDal.getInstance().save(reversalPSD);
        }
      }
    }

    // If the invoice haven't been completely paid, add the remaining payment
    final BigDecimal remainingAmt = gross.subtract(paidAmt);
    if (remainingAmt.compareTo(BigDecimal.ZERO) != 0) {
      final OBCriteria<FIN_PaymentScheduleDetail> remainingPSDCriteria = OBDal.getInstance()
          .createCriteria(FIN_PaymentScheduleDetail.class);
      remainingPSDCriteria.add(Restrictions
          .eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
      remainingPSDCriteria
          .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE));
      remainingPSDCriteria
          .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
      remainingPSDCriteria.setFilterOnReadableOrganization(false);
      remainingPSDCriteria.setMaxResults(1);
      final FIN_PaymentScheduleDetail remainingPSD = (FIN_PaymentScheduleDetail) remainingPSDCriteria
          .uniqueResult();
      if (remainingPSD != null) {
        if (remainingPSD.getAmount().compareTo(remainingAmt) != 0) {
          // The PSD must be splitted in two PSD, one that belongs to the invoice with the
          // remaining amount for the invoice and the other only to the order with the remaining
          // amount for the order that not belongs to an invoice
          FIN_AddPayment.createPSD(remainingPSD.getAmount().subtract(remainingAmt), paymentSchedule,
              null, order.getOrganization(), order.getBusinessPartner());
          remainingPSD.setAmount(remainingAmt);
        }
        remainingPSD.setInvoicePaymentSchedule(paymentScheduleInvoice);
        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList()
            .add(remainingPSD);
        OBDal.getInstance().save(remainingPSD);

        // Generate credit for the remaining to pay in the invoice
        BigDecimal creditGenerated = remainingAmt;
        if (!invoice.getCurrency()
            .equals(invoice.getBusinessPartner().getPriceList().getCurrency())) {
          creditGenerated = convertCurrencyInvoice(invoice, remainingAmt);
        }
        if (creditGenerated.compareTo(BigDecimal.ZERO) != 0) {
          OBDal.getInstance().flush();
          OBContext.setAdminMode(false);
          try {
            order.getBusinessPartner()
                .setCreditUsed(order.getBusinessPartner().getCreditUsed().add(creditGenerated));
            OBDal.getInstance().flush();
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }
    }

    paymentScheduleInvoice.setOutstandingAmount(gross.subtract(paidAmt));
    paymentScheduleInvoice.setPaidAmount(paidAmt);

    if (ModelProvider.getInstance()
        .getEntity(FIN_PaymentSchedule.class)
        .hasProperty("origDueDate")) {
      // This property is checked and set this way to force compatibility with both MP13, MP14
      // and later releases of Openbravo. This property is mandatory and must be set. Check
      // issue
      paymentScheduleInvoice.set("origDueDate", paymentScheduleInvoice.getDueDate());
    }

    if (paidAmt.compareTo(invoice.getGrandTotalAmount()) == 0) {
      invoice.setFinalSettlementDate(finalSettlementDate);
    }

    invoice.setPrepaymentamt(paidAmt);
    invoice.setTotalPaid(paidAmt);
    invoice.setOutstandingAmount(remainingAmt);
    invoice.setDueAmount(remainingAmt);
    invoice.setDaysTillDue(FIN_Utility.getDaysToDue(paymentScheduleInvoice.getDueDate()));
    invoice.setPaymentComplete(paidAmt.compareTo(invoice.getGrandTotalAmount()) == 0);
    invoice.setLastCalculatedOnDate(new Date());
    OBDal.getInstance().save(paymentScheduleInvoice);
    OBDal.getInstance().save(invoice);

    if (invoice.isCashVAT()) {
      createCashVat(invoice);
    }

    OBDal.getInstance().flush();

    return paymentScheduleInvoice;
  }

  public void createCashVat(Invoice invoiceObj) {
    for (FIN_PaymentSchedule scheduleObj : invoiceObj.getFINPaymentScheduleList()) {
      for (FIN_PaymentScheduleDetail schDetailObj : scheduleObj
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        FIN_PaymentDetail paymentDetail = schDetailObj.getPaymentDetails();
        if (paymentDetail != null) {
          CashVATUtil.createInvoiceTaxCashVAT(paymentDetail, scheduleObj,
              paymentDetail.getAmount().add(paymentDetail.getWriteoffAmount()));
        }
      }
    }
  }

  private BigDecimal convertCurrencyInvoice(Invoice invoice, BigDecimal amt) {
    int pricePrecision = invoice.getCurrency().getObposPosprecision() == null
        ? invoice.getCurrency().getPricePrecision().intValue()
        : invoice.getCurrency().getObposPosprecision().intValue();
    List<Object> parameters = new ArrayList<Object>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(amt.setScale(pricePrecision, RoundingMode.HALF_UP));
    types.add(BigDecimal.class);
    parameters.add(invoice.getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(invoice.getBusinessPartner().getPriceList().getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(invoice.getOrderDate());
    types.add(Timestamp.class);
    parameters.add("S");
    types.add(String.class);
    parameters.add(OBContext.getOBContext().getCurrentClient());
    types.add(BaseOBObject.class);
    parameters.add(OBContext.getOBContext().getCurrentOrganization());
    types.add(BaseOBObject.class);
    parameters.add('A');
    types.add(Character.class);

    return (BigDecimal) CallStoredProcedure.getInstance()
        .call("c_currency_convert_precision", parameters, types);
  }

  public void createPaymentTerms(Order order, Invoice invoice) {
    if (invoice.getFINPaymentScheduleList() == null
        || invoice.getFINPaymentScheduleList().isEmpty()) {
      return;
    }
    final FIN_PaymentSchedule paymentSchedule = order.getFINPaymentScheduleList().get(0);
    final FIN_PaymentSchedule paymentScheduleInvoice = invoice.getFINPaymentScheduleList().get(0);
    final OBCriteria<FIN_PaymentScheduleDetail> remainingPSDCriteria = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    remainingPSDCriteria.add(
        Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
    remainingPSDCriteria.add(Restrictions
        .eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE, paymentScheduleInvoice));
    remainingPSDCriteria
        .add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
    remainingPSDCriteria.setMaxResults(1);
    final FIN_PaymentScheduleDetail remainingPSD = (FIN_PaymentScheduleDetail) remainingPSDCriteria
        .uniqueResult();

    if (remainingPSD != null) {
      // There's something remaining to pay, so is necessary to check the payment terms. In case
      // that the remaining must be paid in different terms, the different PS must be created
      // for each term
      final OBCriteria<PaymentTermLine> lineCriteria = OBDal.getInstance()
          .createCriteria(PaymentTermLine.class);
      lineCriteria
          .add(Restrictions.eq(PaymentTermLine.PROPERTY_PAYMENTTERMS, order.getPaymentTerms()));
      lineCriteria.add(Restrictions.eq(PaymentTermLine.PROPERTY_ACTIVE, true));
      lineCriteria.addOrderBy(PaymentTermLine.PROPERTY_LINENO, true);
      List<PaymentTermLine> termLineList = lineCriteria.list();
      if (termLineList.size() > 0) {
        final int pricePrecision = order.getCurrency().getObposPosprecision() == null
            ? order.getCurrency().getPricePrecision().intValue()
            : order.getCurrency().getObposPosprecision().intValue();
        final BigDecimal gross = invoice.getGrandTotalAmount();
        final BigDecimal remainingAmt = remainingPSD.getAmount();
        BigDecimal pendingGrossAmount = remainingAmt;
        int i = 0;
        for (PaymentTermLine paymentTermLine : termLineList) {
          if (pendingGrossAmount.compareTo(BigDecimal.ZERO) == 0) {
            break;
          }
          BigDecimal amount = BigDecimal.ZERO;
          if (paymentTermLine.isExcludeTax()) {
            amount = (invoice.getSummedLineAmount()
                .multiply(paymentTermLine.getPercentageDue().divide(BigDecimal.valueOf(100))))
                    .setScale(pricePrecision, RoundingMode.HALF_UP);
          } else if (!paymentTermLine.isRest()) {
            amount = (remainingAmt
                .multiply(paymentTermLine.getPercentageDue().divide(BigDecimal.valueOf(100))))
                    .setScale(pricePrecision, RoundingMode.HALF_UP);
          } else {
            amount = (pendingGrossAmount
                .multiply(paymentTermLine.getPercentageDue().divide(BigDecimal.valueOf(100))))
                    .setScale(pricePrecision, RoundingMode.HALF_UP);
            pendingGrossAmount = BigDecimal.ZERO;
          }

          if (amount.compareTo(BigDecimal.ZERO) == 0) {
            continue;
          }

          if (pendingGrossAmount.compareTo(BigDecimal.ZERO) != 0) {
            pendingGrossAmount = pendingGrossAmount.subtract(amount);
          }

          Date dueDate = getCalculatedDueDateBasedOnPaymentTerms(order.getOrderDate(), null,
              paymentTermLine);

          if (i == 0 && remainingAmt.compareTo(gross) == 0) {
            paymentScheduleInvoice.setAmount(amount);
            paymentScheduleInvoice.setOutstandingAmount(amount);
            paymentScheduleInvoice.setPaidAmount(BigDecimal.ZERO);
            paymentScheduleInvoice.setDueDate(dueDate);
            paymentScheduleInvoice.setExpectedDate(dueDate);
            i++;
          } else {
            addPaymentSchedule(order, invoice, amount, amount, dueDate);
            i++;
          }
          if (termLineList.size() == i) {
            if (pendingGrossAmount.compareTo(BigDecimal.ZERO) != 0) {
              dueDate = getCalculatedDueDateBasedOnPaymentTerms(order.getOrderDate(),
                  order.getPaymentTerms(), null);

              addPaymentSchedule(order, invoice, pendingGrossAmount, pendingGrossAmount, dueDate);
              i++;
            }
          }
        }
        // Now the PSD with the remaining quantity must be divided between the different PS
        // Invoices
        for (FIN_PaymentSchedule invoicePS : invoice.getFINPaymentScheduleList()) {
          if (invoicePS.getId().equals(paymentScheduleInvoice.getId())) {
            if (remainingAmt.compareTo(gross) != 0) {
              // The PS is paid, so is not taken into account by the payment terms
              // Set the PS as fully paid (the remaining amount is now in the other PS)
              final BigDecimal amountToInvoice = paymentScheduleInvoice.getAmount()
                  .subtract(remainingAmt);
              paymentScheduleInvoice.setAmount(amountToInvoice);
              paymentScheduleInvoice.setOutstandingAmount(
                  paymentScheduleInvoice.getOutstandingAmount().subtract(remainingAmt));
            }
            continue;
          }
          int pendingSign = remainingAmt.signum();
          if ((pendingSign >= 0 && remainingPSD.getAmount().compareTo(invoicePS.getAmount()) <= 0)
              || (pendingSign == -1
                  && remainingPSD.getAmount().compareTo(invoicePS.getAmount()) >= 0)) {
            remainingPSD.setInvoicePaymentSchedule(invoicePS);
          } else {
            // The remaining PSD must be splitted
            FIN_AddPayment.createPSD(invoicePS.getAmount(), paymentSchedule, invoicePS,
                order.getOrganization(), order.getBusinessPartner());
            remainingPSD.setAmount(remainingPSD.getAmount().subtract(invoicePS.getAmount()));
          }
        }
      } else {
        Date dueDate = getCalculatedDueDateBasedOnPaymentTerms(order.getOrderDate(),
            order.getPaymentTerms(), null);
        paymentScheduleInvoice.setDueDate(dueDate);
        paymentScheduleInvoice.setExpectedDate(dueDate);
      }

      // Set the days till the next due
      if (invoice.getFINPaymentScheduleList().size() == 1) {
        invoice.setDaysTillDue(FIN_Utility.getDaysToDue(paymentScheduleInvoice.getDueDate()));
      } else {
        // Set the lower due date of the PS that is not paid
        long daysTillDue = -1;
        for (final FIN_PaymentSchedule currentPSInvoice : invoice.getFINPaymentScheduleList()) {
          if (currentPSInvoice.getId().equals(paymentScheduleInvoice.getId())) {
            continue;
          }
          final long newDaysTillDue = FIN_Utility.getDaysToDue(currentPSInvoice.getDueDate());
          if (daysTillDue == -1) {
            daysTillDue = newDaysTillDue;
          } else {
            if (newDaysTillDue < daysTillDue) {
              daysTillDue = newDaysTillDue;
            }
          }
        }
        invoice.setDaysTillDue(daysTillDue);
      }

      OBDal.getInstance().save(paymentScheduleInvoice);
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();
    }
  }

  private void addPaymentSchedule(Order order, Invoice invoice, BigDecimal amount,
      BigDecimal outstandingAmount, Date dueDate) {
    FIN_PaymentSchedule pymtSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
    pymtSchedule.setOrganization(invoice.getOrganization());
    pymtSchedule.setCurrency(order.getCurrency());
    pymtSchedule.setInvoice(invoice);
    pymtSchedule.setFinPaymentmethod(order.getPaymentMethod());
    pymtSchedule.setFINPaymentPriority(order.getFINPaymentPriority());
    pymtSchedule.setAmount(amount);
    pymtSchedule.setOutstandingAmount(outstandingAmount);
    pymtSchedule.setPaidAmount(amount.subtract(outstandingAmount));
    pymtSchedule.setDueDate(dueDate);
    pymtSchedule.setExpectedDate(dueDate);
    if (ModelProvider.getInstance()
        .getEntity(FIN_PaymentSchedule.class)
        .hasProperty("origDueDate")) {
      pymtSchedule.set("origDueDate", dueDate);
    }
    invoice.getFINPaymentScheduleList().add(pymtSchedule);
    OBDal.getInstance().save(pymtSchedule);
  }

  private Date getCalculatedDueDateBasedOnPaymentTerms(Date startingDate, PaymentTerm paymentTerm,
      PaymentTermLine paymentTermLine) {
    // TODO Take into account the flag "Next business date"
    // TODO Take into account the flag "Fixed due date"
    Calendar calculatedDueDate = new GregorianCalendar();
    calculatedDueDate.setTime(startingDate);
    calculatedDueDate.set(Calendar.HOUR_OF_DAY, 0);
    calculatedDueDate.set(Calendar.MINUTE, 0);
    calculatedDueDate.set(Calendar.SECOND, 0);
    calculatedDueDate.set(Calendar.MILLISECOND, 0);
    long daysToAdd, monthOffset, maturityDate1 = 0, maturityDate2 = 0, maturityDate3 = 0;
    String dayToPay;

    if (paymentTerm != null) {
      daysToAdd = paymentTerm.getOverduePaymentDaysRule();
      monthOffset = paymentTerm.getOffsetMonthDue();
      dayToPay = paymentTerm.getOverduePaymentDayRule();
      if (paymentTerm.isFixedDueDate()) {
        maturityDate1 = paymentTerm.getMaturityDate1() == null ? 0 : paymentTerm.getMaturityDate1();
        maturityDate2 = paymentTerm.getMaturityDate2() == null ? 0 : paymentTerm.getMaturityDate2();
        maturityDate3 = paymentTerm.getMaturityDate3() == null ? 0 : paymentTerm.getMaturityDate3();
      }
    } else if (paymentTermLine != null) {
      daysToAdd = paymentTermLine.getOverduePaymentDaysRule();
      monthOffset = paymentTermLine.getOffsetMonthDue() == null ? 0
          : paymentTermLine.getOffsetMonthDue();
      dayToPay = paymentTermLine.getOverduePaymentDayRule();
      if (paymentTermLine.isFixedDueDate()) {
        maturityDate1 = paymentTermLine.getMaturityDate1() == null ? 0
            : paymentTermLine.getMaturityDate1();
        maturityDate2 = paymentTermLine.getMaturityDate2() == null ? 0
            : paymentTermLine.getMaturityDate2();
        maturityDate3 = paymentTermLine.getMaturityDate3() == null ? 0
            : paymentTermLine.getMaturityDate3();
      }
    } else {
      return calculatedDueDate.getTime();
    }
    if (monthOffset > 0) {
      calculatedDueDate.add(Calendar.MONTH, (int) monthOffset);
    }
    if (daysToAdd > 0) {
      calculatedDueDate.add(Calendar.DATE, (int) daysToAdd);
    }
    // Calculating due date based on "Fixed due date"
    if ((paymentTerm != null && paymentTerm.isFixedDueDate())
        || (paymentTermLine != null && paymentTermLine.isFixedDueDate())) {
      long dueDateDay = calculatedDueDate.get(Calendar.DAY_OF_MONTH), finalDueDateDay = 0;
      if (maturityDate3 > 0 && maturityDate2 > 0 && maturityDate2 < dueDateDay
          && maturityDate3 >= dueDateDay) {
        finalDueDateDay = maturityDate3;
      } else if (maturityDate2 > 0 && maturityDate1 > 0 && maturityDate1 < dueDateDay
          && maturityDate2 >= dueDateDay) {
        finalDueDateDay = maturityDate2;
      } else if (maturityDate1 > 0) {
        finalDueDateDay = maturityDate1;
      } else {
        // Due Date day should be maximum of Month's Last day
        finalDueDateDay = 1;
      }

      if ((int) finalDueDateDay > calculatedDueDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        finalDueDateDay = calculatedDueDate.getActualMaximum(Calendar.DAY_OF_MONTH);
      }
      calculatedDueDate.set(Calendar.DAY_OF_MONTH, (int) finalDueDateDay);
      if (finalDueDateDay < dueDateDay) {
        calculatedDueDate.add(Calendar.MONTH, 1);
      }
    }
    if (!StringUtils.isEmpty(dayToPay)) {
      // for us: 1 -> Monday
      // for Calendar: 1 -> Sunday
      int dayOfTheWeekToPay = Integer.parseInt(dayToPay);
      dayOfTheWeekToPay += 1;
      if (dayOfTheWeekToPay == 8) {
        dayOfTheWeekToPay = 1;
      }
      if (calculatedDueDate.get(Calendar.DAY_OF_WEEK) == dayOfTheWeekToPay) {
        return calculatedDueDate.getTime();
      } else {
        Boolean dayFound = false;
        while (dayFound == false) {
          calculatedDueDate.add(Calendar.DATE, 1);
          if (calculatedDueDate.get(Calendar.DAY_OF_WEEK) == dayOfTheWeekToPay) {
            dayFound = true;
          }
        }
      }
    }
    return calculatedDueDate.getTime();
  }

  /**
   * Method to set the 'Paid Amount At invoicing' field, can only be called after the TotalPaid has
   * been updated the the sum of all payments amounts.
   * 
   * This field is used for the cash VAT functionality
   * 
   * This field is filled with all the payments amounts done at the moment of creating the invoice,
   * previous payments are prepayment and payments done after are normal payments
   * 
   * @param invoice
   *          the method will update the passed invoice object, setting the field PaidAmtAtInvoice
   *          with the current value of TotalPaid
   */
  public void setPaidAmountAtInvoicing(Invoice invoice) {
    invoice.setPaidAmountAtInvoicing(invoice.getTotalPaid().subtract(invoice.getPrepaymentamt()));
  }
}
