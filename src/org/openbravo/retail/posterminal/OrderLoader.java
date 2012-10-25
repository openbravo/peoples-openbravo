/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_OrigPaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.Fin_OrigPaymentSchedule;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class OrderLoader extends JSONProcessSimple {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> invoiceDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  private static final Logger log = Logger.getLogger(OrderLoader.class);

  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    Object jsonorder = jsonsent.get("order");

    JSONArray array = null;
    if (jsonorder instanceof JSONObject) {
      array = new JSONArray();
      array.put(jsonorder);
    } else if (jsonorder instanceof String) {
      JSONObject obj = new JSONObject((String) jsonorder);
      array = new JSONArray();
      array.put(obj);
    } else if (jsonorder instanceof JSONArray) {
      array = (JSONArray) jsonorder;
    }

    long t1 = System.currentTimeMillis();
    JSONObject result = this.saveOrder(array);
    log.info("Final total time: " + (System.currentTimeMillis() - t1));
    return result;
  }

  public JSONObject saveOrder(JSONArray jsonarray) throws JSONException {
    boolean error = false;
    OBContext.setAdminMode(true);
    try {
      for (int i = 0; i < jsonarray.length(); i++) {
        long t1 = System.currentTimeMillis();
        JSONObject jsonorder = jsonarray.getJSONObject(i);
        try {
          JSONObject result = saveOrder(jsonorder);
          if (!result.get(JsonConstants.RESPONSE_STATUS).equals(
              JsonConstants.RPCREQUEST_STATUS_SUCCESS)) {
            log.error("There was an error importing order: " + jsonorder.toString());
            error = true;
          }
          if (i % 1 == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getConnection().commit();
            OBDal.getInstance().getSession().clear();
          }
          log.info("Total order time: " + (System.currentTimeMillis() - t1));
        } catch (Exception e) {
          // Creation of the order failed. We will now store the order in the import errors table
          OBDal.getInstance().rollbackAndClose();
          if (TriggerHandler.getInstance().isDisabled()) {
            TriggerHandler.getInstance().enable();
          }
          OBPOSErrors errorEntry = OBProvider.getInstance().get(OBPOSErrors.class);
          errorEntry.setError(getErrorMessage(e));
          errorEntry.setOrderstatus("N");
          errorEntry.setJsoninfo(jsonorder.toString());
          OBDal.getInstance().save(errorEntry);
          OBDal.getInstance().flush();

          log.error("Error while loading order", e);
          try {
            OBDal.getInstance().getConnection().commit();
          } catch (SQLException e1) {
            // this won't happen
          }

        }
      }

    } finally {
      OBContext.restorePreviousMode();
    }
    JSONObject jsonResponse = new JSONObject();
    if (!error) {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      jsonResponse.put("result", "0");
    }
    return jsonResponse;
  }

  public JSONObject saveOrder(JSONObject jsonorder) throws Exception {

    if (verifyOrderExistance(jsonorder)) {
      return successMessage(jsonorder);
    }

    long t0 = System.currentTimeMillis();
    long t1, t11, t2, t3;
    Order order = null;
    ShipmentInOut shipment = null;
    Invoice invoice = null;
    boolean sendEmail = false;
    TriggerHandler.getInstance().disable();
    try {
      t1 = System.currentTimeMillis();
      boolean createInvoice = (jsonorder.has("generateInvoice") && jsonorder
          .getBoolean("generateInvoice"));
      sendEmail = (jsonorder.has("sendEmail") && jsonorder.getBoolean("sendEmail"));
      // Order header
      order = OBProvider.getInstance().get(Order.class);
      long t111 = System.currentTimeMillis();
      createOrder(order, jsonorder);
      long t112 = System.currentTimeMillis();
      // Order lines
      ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      JSONArray orderlines = jsonorder.getJSONArray("lines");
      createOrderLines(order, jsonorder, orderlines, lineReferences);

      long t113 = System.currentTimeMillis();
      // Shipment header
      shipment = OBProvider.getInstance().get(ShipmentInOut.class);
      createShipment(shipment, order, jsonorder);

      long t114 = System.currentTimeMillis();
      // Shipment lines
      createShipmentLines(shipment, order, jsonorder, orderlines, lineReferences);
      long t115 = System.currentTimeMillis();
      if (createInvoice) {
        // Invoice header
        invoice = OBProvider.getInstance().get(Invoice.class);
        createInvoice(invoice, order, jsonorder);

        // Invoice lines
        createInvoiceLines(invoice, order, jsonorder, orderlines, lineReferences);
      }
      t11 = System.currentTimeMillis();
      OBDal.getInstance().save(order);
      OBDal.getInstance().save(shipment);
      if (invoice != null) {
        OBDal.getInstance().save(invoice);
      }
      t2 = System.currentTimeMillis();
      OBDal.getInstance().flush();
      t3 = System.currentTimeMillis();
      log.debug("Creation of bobs. Order: " + (t112 - t111) + "; Orderlines: " + (t113 - t112)
          + "; Shipment: " + (t114 - t113) + "; Shipmentlines: " + (t115 - t114) + "; Invoice: "
          + (t11 - t115));

    } finally {
      TriggerHandler.getInstance().enable();
    }

    long t4 = System.currentTimeMillis();

    // Payment
    JSONObject paymentResponse = handlePayments(jsonorder, order, invoice);
    if (paymentResponse != null) {
      return paymentResponse;
    }

    // Stock manipulation
    handleStock(shipment);

    // Send email
    if (sendEmail) {
      EmailSender emailSender = new EmailSender(order.getId(), jsonorder);
    }

    log.info("Initial flush: " + (t1 - t0) + "; Generate bobs:" + (t11 - t1) + "; Save bobs:"
        + (t2 - t11) + "; First flush:" + (t3 - t2) + "; Second flush: " + (t4 - t3)
        + "; Process Payments:" + (System.currentTimeMillis() - t4));

    return successMessage(jsonorder);
  }

  protected JSONObject successMessage(JSONObject jsonorder) throws Exception {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    jsonResponse.put("data", jsonorder);

    return jsonResponse;
  }

  protected boolean verifyOrderExistance(JSONObject jsonorder) throws Exception {
    if (jsonorder.has("isbeingretriggered")
        && jsonorder.getString("isbeingretriggered").equals("Y")) {
      // This order has been sent previously. We need to verify if it was saved before, or not.
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(jsonorder.getString("documentNo"));
      parameters.add(jsonorder.getString("posTerminal"));
      parameters.add(jsonorder.getJSONObject("bp").getString("id"));
      OBQuery<Order> orders = OBDal.getInstance().createQuery(Order.class,
          "documentNo=? and obposApplications.id=? and businessPartner.id=?");
      orders.setParameters(parameters);
      return orders.count() > 0;
    }
    return false;
  }

  protected String getPaymentDescription() {
    if (paymentDescription == null) {
      String language = RequestContext.get().getVariablesSecureApp().getLanguage();
      paymentDescription = Utility.messageBD(new DalConnectionProvider(false), "OrderDocumentno",
          language);
    }
    return paymentDescription;
  }

  protected DocumentType getPaymentDocumentType(Organization org) {
    if (paymentDocTypes.get(DalUtil.getId(org)) != null) {
      return paymentDocTypes.get(DalUtil.getId(org));
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARReceipt);
    paymentDocTypes.put((String) DalUtil.getId(org), docType);
    return docType;

  }

  protected DocumentType getInvoiceDocumentType(String documentTypeId) {
    if (invoiceDocTypes.get(documentTypeId) != null) {
      return invoiceDocTypes.get(documentTypeId);
    }
    DocumentType orderDocType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    final DocumentType docType = orderDocType.getDocumentTypeForInvoice();
    invoiceDocTypes.put(documentTypeId, docType);
    if (docType == null) {
      throw new OBException(
          "There is no 'Document type for Invoice' defined for the specified Document Type. The document type for invoices can be configured in the Document Type window, and it should be configured for the document type: "
              + orderDocType.getName());
    }
    return docType;
  }

  protected DocumentType getShipmentDocumentType(String documentTypeId) {
    if (shipmentDocTypes.get(documentTypeId) != null) {
      return shipmentDocTypes.get(documentTypeId);
    }
    DocumentType orderDocType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    final DocumentType docType = orderDocType.getDocumentTypeForShipment();
    shipmentDocTypes.put(documentTypeId, docType);
    if (docType == null) {
      throw new OBException(
          "There is no 'Document type for Shipment' defined for the specified Document Type. The document type for shipments can be configured in the Document Type window, and it should be configured for the document type: "
              + orderDocType.getName());
    }
    return docType;
  }

  protected void createInvoiceLines(Invoice invoice, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences) throws JSONException {
    Entity promotionLineEntity = ModelProvider.getInstance().getEntity(OrderLineOffer.class);

    for (int i = 0; i < orderlines.length(); i++) {
      InvoiceLine line = OBProvider.getInstance().get(InvoiceLine.class);
      Entity inlineEntity = ModelProvider.getInstance().getEntity(InvoiceLine.class);
      JSONPropertyToEntity.fillBobFromJSON(inlineEntity, line, orderlines.getJSONObject(i));
      JSONPropertyToEntity.fillBobFromJSON(
          ModelProvider.getInstance().getEntity(InvoiceLine.class), line, jsonorder);
      line.setLineNo((long) ((i + 1) * 10));
      line.setLineNetAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("net")));
      BigDecimal qty = lineReferences.get(i).getOrderedQuantity();
      line.setInvoicedQuantity(qty);
      lineReferences.get(i).setInvoicedQuantity(qty);
      line.setInvoice(invoice);
      line.setSalesOrderLine(lineReferences.get(i));
      line.setGrossAmount(lineReferences.get(i).getLineGrossAmount());
      invoice.getInvoiceLineList().add(line);

      InvoiceLineTax tax = OBProvider.getInstance().get(InvoiceLineTax.class);
      tax.setLineNo((long) ((i + 1) * 10));
      tax.setTax(line.getTax());
      tax.setTaxableAmount(line.getLineNetAmount());
      tax.setTaxAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("taxAmount")));
      tax.setInvoice(invoice);
      tax.setInvoiceLine(line);
      line.getInvoiceLineTaxList().add(tax);
      invoice.getInvoiceLineTaxList().add(tax);

      // Discounts & Promotions
      if (orderlines.getJSONObject(i).has("promotions")
          && orderlines.getJSONObject(i).get("promotions") != null) {
        JSONArray jsonPromotions = orderlines.getJSONObject(i).getJSONArray("promotions");
        for (int p = 0; p < jsonPromotions.length(); p++) {
          JSONObject jsonPromotion = jsonPromotions.getJSONObject(p);
          boolean hasActualAmt = jsonPromotion.has("actualAmt");
          if (hasActualAmt && jsonPromotion.getDouble("actualAmt") == 0) {
            continue;
          }

          InvoiceLineOffer promotion = OBProvider.getInstance().get(InvoiceLineOffer.class);
          JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion);

          if (hasActualAmt) {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt")));
          } else {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt")));
          }
          promotion.setLineNo((long) ((p + 1) * 10));
          promotion.setInvoiceLine(line);
          line.getInvoiceLineOfferList().add(promotion);
        }
      }
    }
  }

  protected void createInvoice(Invoice invoice, Order order, JSONObject jsonorder)
      throws JSONException {
    Entity invoiceEntity = ModelProvider.getInstance().getEntity(Invoice.class);
    JSONPropertyToEntity.fillBobFromJSON(invoiceEntity, invoice, jsonorder);

    invoice.setDocumentNo(null);
    invoice
        .setDocumentType(getInvoiceDocumentType((String) DalUtil.getId(order.getDocumentType())));
    invoice.setTransactionDocument(getInvoiceDocumentType((String) DalUtil.getId(order
        .getDocumentType())));
    invoice.setAccountingDate(order.getOrderDate());
    invoice.setInvoiceDate(order.getOrderDate());
    invoice.setSalesTransaction(true);
    invoice.setDocumentStatus("CO");
    invoice.setDocumentAction("RE");
    invoice.setAPRMProcessinvoice("RE");
    invoice.setSalesOrder(order);
    invoice.setPartnerAddress(OBDal.getInstance().get(Location.class,
        jsonorder.getJSONObject("bp").getString("locId")));
    invoice.setProcessed(true);
    invoice.setPaymentMethod((FIN_PaymentMethod) OBDal.getInstance().getProxy("FIN_PaymentMethod",
        jsonorder.getJSONObject("bp").getString("paymentMethod")));
    invoice.setPaymentTerms((PaymentTerm) OBDal.getInstance().getProxy("FinancialMgmtPaymentTerm",
        jsonorder.getJSONObject("bp").getString("paymentTerms")));
    invoice.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
    invoice.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("net")));
    invoice.setTotalPaid(BigDecimal.ZERO);
    invoice.setOutstandingAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))));
    invoice.setDueAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))));

    // Create invoice tax lines
    JSONObject taxes = jsonorder.getJSONObject("taxes");
    @SuppressWarnings("unchecked")
    Iterator<String> itKeys = taxes.keys();
    int i = 0;
    while (itKeys.hasNext()) {
      String taxId = (String) itKeys.next();
      JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
      InvoiceTax invoiceTax = OBProvider.getInstance().get(InvoiceTax.class);
      TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
          ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
      invoiceTax.setTax(tax);
      invoiceTax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")));
      invoiceTax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")));
      invoiceTax.setInvoice(invoice);
      invoiceTax.setLineNo((long) ((i + 1) * 10));
      i++;
      invoice.getInvoiceTaxList().add(invoiceTax);
    }

  }

  protected void createShipmentLines(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences) throws JSONException {
    int lineNo = 0;
    Entity shplineentity = ModelProvider.getInstance().getEntity(ShipmentInOutLine.class);
    for (int i = 0; i < orderlines.length(); i++) {
      String hqlWhereClause;

      OrderLine orderLine = lineReferences.get(i);
      BigDecimal pendingQty = orderLine.getOrderedQuantity();

      if (pendingQty.compareTo(BigDecimal.ZERO) > 0) {
        // Returns have qty<0
        // In case of standard sales (no return), take bins with product ordered by prio. Using same
        // logic as in M_InOut_Create PL code
        hqlWhereClause = " as t, Locator as l" + " where t.product = :product"
            + "   and t.uOM = :uom" + "   and l.warehouse = :warehouse" + "   and t.storageBin = l"
            + "   and l.active = true" + "   and coalesce(t.quantityOnHand,0)>0"
            + " order by l.relativePriority, t.creationDate";

        OBQuery<StorageDetail> query = OBDal.getInstance().createQuery(StorageDetail.class,
            hqlWhereClause);
        query.setNamedParameter("product", orderLine.getProduct());
        query.setNamedParameter("uom", orderLine.getUOM());
        query.setNamedParameter("warehouse", order.getWarehouse());

        ScrollableResults bins = query.scroll(ScrollMode.FORWARD_ONLY);
        while (pendingQty.compareTo(BigDecimal.ZERO) > 0 && bins.next()) {
          // TODO: Can we safely clear session here?
          StorageDetail storage = (StorageDetail) bins.get(0);
          BigDecimal qty;

          if (pendingQty.compareTo(storage.getQuantityOnHand()) > 0) {
            qty = storage.getQuantityOnHand();
            pendingQty = pendingQty.subtract(qty);
          } else {
            qty = pendingQty;
            pendingQty = BigDecimal.ZERO;
          }
          lineNo += 10;
          addShipemntline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
              jsonorder, lineNo, qty, storage.getStorageBin());
        }
      }

      if (pendingQty.compareTo(BigDecimal.ZERO) != 0) {
        // still qty to ship or return: let's use the bin with highest prio
        hqlWhereClause = " l where l.warehouse = :warehouse order by l.relativePriority, l.id";
        OBQuery<Locator> queryLoc = OBDal.getInstance().createQuery(Locator.class, hqlWhereClause);
        queryLoc.setNamedParameter("warehouse", order.getWarehouse());
        queryLoc.setMaxResult(1);
        lineNo += 10;
        addShipemntline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine, jsonorder,
            lineNo, pendingQty, queryLoc.list().get(0));
      }
    }
  }

  private void addShipemntline(ShipmentInOut shipment, Entity shplineentity,
      JSONObject jsonOrderLine, OrderLine orderLine, JSONObject jsonorder, long lineNo,
      BigDecimal qty, Locator bin) throws JSONException {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);

    JSONPropertyToEntity.fillBobFromJSON(shplineentity, line, jsonOrderLine);
    JSONPropertyToEntity.fillBobFromJSON(
        ModelProvider.getInstance().getEntity(ShipmentInOutLine.class), line, jsonorder);
    line.setLineNo(lineNo);
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);

    line.setMovementQuantity(qty);
    line.setStorageBin(bin);
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
  }

  protected void createShipment(ShipmentInOut shipment, Order order, JSONObject jsonorder)
      throws JSONException {
    Entity shpEntity = ModelProvider.getInstance().getEntity(ShipmentInOut.class);
    JSONPropertyToEntity.fillBobFromJSON(shpEntity, shipment, jsonorder);
    shipment.setDocumentNo(null);
    shipment
        .setDocumentType(getShipmentDocumentType((String) DalUtil.getId(order.getDocumentType())));
    shipment.setAccountingDate(order.getOrderDate());
    shipment.setMovementDate(order.getOrderDate());
    shipment.setPartnerAddress(OBDal.getInstance().get(Location.class,
        jsonorder.getJSONObject("bp").getString("locId")));
    shipment.setSalesTransaction(true);
    shipment.setDocumentStatus("CO");
    shipment.setDocumentAction("--");
    shipment.setMovementType("C-");
    shipment.setProcessNow(false);
    shipment.setProcessed(true);
    shipment.setSalesOrder(order);

  }

  protected void createOrderLines(Order order, JSONObject jsonorder, JSONArray orderlines,
      ArrayList<OrderLine> lineReferences) throws JSONException {
    Entity orderLineEntity = ModelProvider.getInstance().getEntity(OrderLine.class);
    Entity promotionLineEntity = ModelProvider.getInstance().getEntity(OrderLineOffer.class);

    for (int i = 0; i < orderlines.length(); i++) {
      OrderLine orderline = OBProvider.getInstance().get(OrderLine.class);

      JSONObject jsonOrderLine = orderlines.getJSONObject(i);

      JSONPropertyToEntity.fillBobFromJSON(ModelProvider.getInstance().getEntity(OrderLine.class),
          orderline, jsonorder);
      JSONPropertyToEntity.fillBobFromJSON(orderLineEntity, orderline, jsonOrderLine);

      orderline.setActive(true);
      orderline.setSalesOrder(order);
      orderline.setLineNetAmount(BigDecimal.valueOf(jsonOrderLine.getDouble("net")));
      orderline.setListPrice(orderline.getUnitPrice());

      // shipment is created, so all is delivered
      orderline.setDeliveredQuantity(orderline.getOrderedQuantity());

      lineReferences.add(orderline);
      orderline.setLineNo((long) ((i + 1) * 10));
      order.getOrderLineList().add(orderline);

      OrderLineTax tax = OBProvider.getInstance().get(OrderLineTax.class);
      tax.setLineNo((long) ((i + 1) * 10));
      tax.setTax(orderline.getTax());
      tax.setTaxableAmount(orderline.getLineNetAmount());
      tax.setTaxAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("taxAmount")));
      tax.setSalesOrder(order);
      tax.setSalesOrderLine(orderline);
      orderline.getOrderLineTaxList().add(tax);
      order.getOrderLineTaxList().add(tax);

      // Discounts & Promotions
      if (jsonOrderLine.has("promotions") && !jsonOrderLine.isNull("promotions")
          && !jsonOrderLine.getString("promotions").equals("null")) {
        JSONArray jsonPromotions = jsonOrderLine.getJSONArray("promotions");
        for (int p = 0; p < jsonPromotions.length(); p++) {
          JSONObject jsonPromotion = jsonPromotions.getJSONObject(p);
          boolean hasActualAmt = jsonPromotion.has("actualAmt");
          if (hasActualAmt && jsonPromotion.getDouble("actualAmt") == 0) {
            continue;
          }

          OrderLineOffer promotion = OBProvider.getInstance().get(OrderLineOffer.class);
          JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion);

          if (hasActualAmt) {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt")));
          } else {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt")));
          }
          promotion.setLineNo((long) ((p + 1) * 10));
          promotion.setSalesOrderLine(orderline);
          orderline.getOrderLineOfferList().add(promotion);
        }
      }
    }
  }

  protected void createOrder(Order order, JSONObject jsonorder) throws JSONException {
    Entity orderEntity = ModelProvider.getInstance().getEntity(Order.class);
    JSONPropertyToEntity.fillBobFromJSON(orderEntity, order, jsonorder);

    order.setTransactionDocument((DocumentType) OBDal.getInstance().getProxy("DocumentType",
        jsonorder.getString("documentType")));
    order.setAccountingDate(order.getOrderDate());
    order.setScheduledDeliveryDate(order.getOrderDate());
    order.setPartnerAddress(OBDal.getInstance().get(Location.class,
        jsonorder.getJSONObject("bp").getString("locId")));
    order.setInvoiceAddress(order.getPartnerAddress());
    order.setPaymentMethod((FIN_PaymentMethod) OBDal.getInstance().getProxy("FIN_PaymentMethod",
        jsonorder.getJSONObject("bp").getString("paymentMethod")));
    order.setPaymentTerms((PaymentTerm) OBDal.getInstance().getProxy("FinancialMgmtPaymentTerm",
        jsonorder.getJSONObject("bp").getString("paymentTerms")));
    order.setInvoiceTerms(jsonorder.getJSONObject("bp").getString("invoiceTerms"));
    order.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
    order.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("net")));

    order.setSalesTransaction(true);
    order.setDocumentStatus("CO");
    order.setDocumentAction("--");
    order.setProcessed(true);
    order.setProcessNow(false);
    order.setObposSendemail((jsonorder.has("sendEmail") && jsonorder.getBoolean("sendEmail")));

    JSONObject taxes = jsonorder.getJSONObject("taxes");
    @SuppressWarnings("unchecked")
    Iterator<String> itKeys = taxes.keys();
    int i = 0;
    while (itKeys.hasNext()) {
      String taxId = (String) itKeys.next();
      JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
      OrderTax orderTax = OBProvider.getInstance().get(OrderTax.class);
      TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
          ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
      orderTax.setTax(tax);
      orderTax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")));
      orderTax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")));
      orderTax.setSalesOrder(order);
      orderTax.setLineNo((long) ((i + 1) * 10));
      i++;
      order.getOrderTaxList().add(orderTax);
    }
  }

  protected void handleStock(ShipmentInOut shipment) {
    for (ShipmentInOutLine line : shipment.getMaterialMgmtShipmentInOutLineList()) {
      MaterialTransaction transaction = OBProvider.getInstance().get(MaterialTransaction.class);
      transaction.setOrganization(line.getOrganization());
      transaction.setMovementType(shipment.getMovementType());
      transaction.setProduct(line.getProduct());
      transaction.setStorageBin(line.getStorageBin());
      transaction.setOrderUOM(line.getOrderUOM());
      transaction.setUOM(line.getUOM());
      transaction.setOrderQuantity(line.getOrderQuantity());
      transaction.setMovementQuantity(line.getMovementQuantity().multiply(NEGATIVE_ONE));
      transaction.setMovementDate(shipment.getMovementDate());
      transaction.setGoodsShipmentLine(line);

      OBDal.getInstance().save(transaction);
    }
  }

  protected JSONObject handlePayments(JSONObject jsonorder, Order order, Invoice invoice)
      throws Exception {
    String posTerminalId = jsonorder.getString("posTerminal");
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class, posTerminalId);
    if (posTerminal == null) {
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      jsonResponse.put(JsonConstants.RESPONSE_ERRORMESSAGE, "The POS terminal with id "
          + posTerminalId + " couldn't be found");
      return jsonResponse;
    } else {
      JSONArray payments = jsonorder.getJSONArray("payments");

      // Create a unique payment schedule for all payments
      BigDecimal amt = BigDecimal.valueOf(jsonorder.getDouble("payment"));
      FIN_PaymentSchedule paymentSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
      paymentSchedule.setCurrency(order.getCurrency());
      paymentSchedule.setOrder(order);
      paymentSchedule.setFinPaymentmethod(order.getBusinessPartner().getPaymentMethod());
      // paymentSchedule.setPaidAmount(new BigDecimal(0));
      paymentSchedule.setAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
      // Sept 2012 -> gross because outstanding is not allowed in Openbravo Web POS
      paymentSchedule.setOutstandingAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
      paymentSchedule.setDueDate(order.getOrderDate());
      if (ModelProvider.getInstance().getEntity(FIN_PaymentSchedule.class)
          .hasProperty("origDueDate")) {
        // This property is checked and set this way to force compatibility with both MP13, MP14 and
        // later releases of Openbravo. This property is mandatory and must be set. Check issue
        paymentSchedule.set("origDueDate", paymentSchedule.getDueDate());
      }
      paymentSchedule.setFINPaymentPriority(order.getFINPaymentPriority());
      OBDal.getInstance().save(paymentSchedule);

      FIN_PaymentSchedule paymentScheduleInvoice = null;
      if (invoice != null) {
        paymentScheduleInvoice = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
        paymentScheduleInvoice.setCurrency(order.getCurrency());
        paymentScheduleInvoice.setInvoice(invoice);
        paymentScheduleInvoice.setFinPaymentmethod(order.getBusinessPartner().getPaymentMethod());
        paymentScheduleInvoice.setAmount(amt);
        paymentScheduleInvoice.setOutstandingAmount(amt);
        paymentScheduleInvoice.setDueDate(order.getOrderDate());
        if (ModelProvider.getInstance().getEntity(FIN_PaymentSchedule.class)
            .hasProperty("origDueDate")) {
          // This property is checked and set this way to force compatibility with both MP13, MP14
          // and
          // later releases of Openbravo. This property is mandatory and must be set. Check issue
          paymentScheduleInvoice.set("origDueDate", paymentScheduleInvoice.getDueDate());
        }
        paymentScheduleInvoice.setFINPaymentPriority(order.getFINPaymentPriority());

        OBDal.getInstance().save(paymentScheduleInvoice);
      }

      BigDecimal gross = BigDecimal.valueOf(jsonorder.getDouble("gross"));
      BigDecimal writeoffAmt = amt.subtract(gross);

      for (int i = 0; i < payments.length(); i++) {
        JSONObject payment = payments.getJSONObject(i);
        OBPOSAppPayment paymentType = null;
        String paymentTypeName = payment.getString("kind");
        for (OBPOSAppPayment type : posTerminal.getOBPOSAppPaymentList()) {
          if (type.getSearchKey().equals(paymentTypeName)) {
            paymentType = type;
          }
        }
        if (paymentType == null) {
          final JSONObject jsonResponse = new JSONObject();
          jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
          jsonResponse.put(JsonConstants.RESPONSE_ERRORMESSAGE, "The POS terminal with id "
              + posTerminalId + " didn't have a payment defined with name: " + paymentTypeName);
          return jsonResponse;
        } else {
          processPayments(paymentSchedule, paymentScheduleInvoice, order, invoice, paymentType,
              payment, i == (payments.length() - 1) ? writeoffAmt : BigDecimal.ZERO);
        }
      }

      return null;
    }

  }

  protected void processPayments(FIN_PaymentSchedule paymentSchedule,
      FIN_PaymentSchedule paymentScheduleInvoice, Order order, Invoice invoice,
      OBPOSAppPayment paymentType, JSONObject payment, BigDecimal writeoffAmt) throws Exception {
    long t1 = System.currentTimeMillis();
    OBContext.setAdminMode(true);
    try {
      BigDecimal amount = BigDecimal.valueOf(payment.getDouble("paid"));
      BigDecimal origAmount = amount;
      if (payment.has("rate")) {
        origAmount = BigDecimal.valueOf(payment.getDouble("amount"));
      }
      BigDecimal mulrate = new BigDecimal(1);
      if (payment.has("mulrate")) {
        mulrate = BigDecimal.valueOf(payment.getDouble("mulrate"));
      }

      // writeoffAmt.divide(BigDecimal.valueOf(payment.getDouble("rate")));
      if (amount.signum() == 0) {
        return;
      }
      if (writeoffAmt.signum() != 0) {
        amount = amount.subtract(writeoffAmt);
      }

      FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
      paymentScheduleDetail.setAmount(amount);
      paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
          paymentScheduleDetail);

      OBDal.getInstance().save(paymentScheduleDetail);
      if (paymentScheduleInvoice != null) {
        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(
            paymentScheduleDetail);
        paymentScheduleDetail.setInvoicePaymentSchedule(paymentScheduleInvoice);

        Fin_OrigPaymentSchedule origPaymentSchedule = OBProvider.getInstance().get(
            Fin_OrigPaymentSchedule.class);
        origPaymentSchedule.setCurrency(order.getCurrency());
        origPaymentSchedule.setInvoice(invoice);
        origPaymentSchedule.setPaymentMethod(paymentSchedule.getFinPaymentmethod());
        origPaymentSchedule.setAmount(amount);
        origPaymentSchedule.setDueDate(order.getOrderDate());
        origPaymentSchedule.setPaymentPriority(paymentScheduleInvoice.getFINPaymentPriority());

        OBDal.getInstance().save(origPaymentSchedule);

        FIN_OrigPaymentScheduleDetail origDetail = OBProvider.getInstance().get(
            FIN_OrigPaymentScheduleDetail.class);
        origDetail.setArchivedPaymentPlan(origPaymentSchedule);
        origDetail.setPaymentScheduleDetail(paymentScheduleDetail);
        origDetail.setAmount(amount);
        origDetail.setWriteoffAmount(paymentScheduleDetail.getWriteoffAmount());

        OBDal.getInstance().save(origDetail);

      }

      HashMap<String, BigDecimal> paymentAmount = new HashMap<String, BigDecimal>();
      paymentAmount.put(paymentScheduleDetail.getId(), amount);

      FIN_FinancialAccount account = paymentType.getFinancialAccount();

      long t2 = System.currentTimeMillis();
      // Save Payment

      List<FIN_PaymentScheduleDetail> detail = new ArrayList<FIN_PaymentScheduleDetail>();
      detail.add(paymentScheduleDetail);

      FIN_Payment finPayment = FIN_AddPayment.savePayment(null, true,
          getPaymentDocumentType(order.getOrganization()), order.getDocumentNo(),
          order.getBusinessPartner(), paymentType.getPaymentMethod().getPaymentMethod(), account,
          amount.toString(), order.getOrderDate(), order.getOrganization(), null, detail,
          paymentAmount, false, false, order.getCurrency(), mulrate, origAmount);
      if (writeoffAmt.signum() != 0) {
        FIN_AddPayment.saveGLItem(finPayment, writeoffAmt, paymentType.getPaymentMethod()
            .getGlitemWriteoff());
      }
      // Update Payment In amount after adding GLItem
      finPayment.setAmount(BigDecimal.valueOf(payment.getDouble("paid")));
      OBDal.getInstance().save(finPayment);

      String description = getPaymentDescription();
      description += ": " + order.getDocumentNo().substring(1, order.getDocumentNo().length() - 1)
          + "\n";
      finPayment.setDescription(description);

      long t3 = System.currentTimeMillis();
      // Process Payment

      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      ProcessBundle pb = new ProcessBundle("6255BE488882480599C81284B70CD9B3", vars)
          .init(new DalConnectionProvider(false));
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("action", "D");
      parameters.put("Fin_Payment_ID", finPayment.getId());
      parameters.put("isPOSOrder", "Y");
      pb.setParams(parameters);
      FIN_PaymentProcess process = new FIN_PaymentProcess();
      process.execute(pb);
      OBError result = (OBError) pb.getResult();
      if (result.getType().equalsIgnoreCase("Error")) {
        throw new OBException(result.getMessage());
      }
      vars.setSessionValue("POSOrder", "Y");
      log.debug("Payment. Create entities: " + (t2 - t1) + "; Save payment: " + (t3 - t2)
          + "; Process payment: " + (System.currentTimeMillis() - t3));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public static String getErrorMessage(Exception e) {
    StringWriter sb = new StringWriter();
    e.printStackTrace(new PrintWriter(sb));
    return sb.toString();
  }
}
