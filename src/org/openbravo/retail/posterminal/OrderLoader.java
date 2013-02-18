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
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
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
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_OrigPaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.Fin_OrigPaymentSchedule;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.onhandquantity.StockProposed;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;

public class OrderLoader extends JSONProcessSimple {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> invoiceDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  private static final Logger log = Logger.getLogger(OrderLoader.class);

  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);

  @Inject
  @Any
  private Instance<OrderLoaderHook> orderProcesses;

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
    String currentClient = OBContext.getOBContext().getCurrentClient().getId();
    String currentOrg = OBContext.getOBContext().getCurrentOrganization().getId();
    String currentUser = OBContext.getOBContext().getUser().getId();
    String currentRole = OBContext.getOBContext().getRole().getId();
    OBContext.setAdminMode(true);
    try {
      for (int i = 0; i < jsonarray.length(); i++) {
        long t1 = System.currentTimeMillis();
        JSONObject jsonorder = jsonarray.getJSONObject(i);
        String posTerminalId = jsonorder.getString("posTerminal");
        OBContext.setOBContext(jsonorder.getString("createdBy"), currentRole,
            jsonorder.getString("client"), jsonorder.getString("organization"));
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
          log.error("An error happened when processing an order: ", e);
          // Creation of the order failed. We will now store the order in the import errors table
          OBDal.getInstance().rollbackAndClose();
          if (TriggerHandler.getInstance().isDisabled()) {
            TriggerHandler.getInstance().enable();
          }
          OBPOSErrors errorEntry = OBProvider.getInstance().get(OBPOSErrors.class);
          errorEntry.setError(getErrorMessage(e));
          errorEntry.setOrderstatus("N");
          errorEntry.setJsoninfo(jsonorder.toString());
          errorEntry.setTypeofdata("order");
          errorEntry.setObposApplications(OBDal.getInstance().get(OBPOSApplications.class,
              posTerminalId));
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
      OBContext.setOBContext(currentUser, currentRole, currentClient, currentOrg);
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
    boolean isQuotation = jsonorder.has("isQuotation") && jsonorder.getBoolean("isQuotation");
    try {
      if (jsonorder.has("oldId") && !jsonorder.getString("oldId").equals("null")
          && jsonorder.has("isQuotation") && jsonorder.getBoolean("isQuotation")) {
        deleteOldDocument(jsonorder);
      }

      t1 = System.currentTimeMillis();
      boolean createInvoice = !isQuotation
          && (jsonorder.has("generateInvoice") && jsonorder.getBoolean("generateInvoice"));
      boolean createShipment = !isQuotation;
      sendEmail = (jsonorder.has("sendEmail") && jsonorder.getBoolean("sendEmail"));
      // Order header
      order = OBProvider.getInstance().get(Order.class);
      long t111 = System.currentTimeMillis();
      createOrder(order, jsonorder);
      OBDal.getInstance().save(order);
      long t112 = System.currentTimeMillis();
      // Order lines
      ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      JSONArray orderlines = jsonorder.getJSONArray("lines");
      createOrderLines(order, jsonorder, orderlines, lineReferences);

      if (jsonorder.has("oldId") && !jsonorder.getString("oldId").equals("null")
          && (!jsonorder.has("isQuotation") || !jsonorder.getBoolean("isQuotation"))) {
        // This order comes from a quotation, we need to associate both
        associateOrderToQuotation(jsonorder, order);
      }

      long t113 = System.currentTimeMillis();
      if (createShipment) {
        // Shipment header
        shipment = OBProvider.getInstance().get(ShipmentInOut.class);
        createShipment(shipment, order, jsonorder);

        // Shipment lines
        createShipmentLines(shipment, order, jsonorder, orderlines, lineReferences);
      }
      long t115 = System.currentTimeMillis();
      if (createInvoice) {
        // Invoice header
        invoice = OBProvider.getInstance().get(Invoice.class);
        createInvoice(invoice, order, jsonorder);
        OBDal.getInstance().save(invoice);

        // Invoice lines
        createInvoiceLines(invoice, order, jsonorder, orderlines, lineReferences);
      }
      t11 = System.currentTimeMillis();
      if (shipment != null) {
        OBDal.getInstance().save(shipment);
      }

      t2 = System.currentTimeMillis();
      OBDal.getInstance().flush();
      updateAuditInfo(order, invoice, jsonorder);
      t3 = System.currentTimeMillis();
      log.debug("Creation of bobs. Order: " + (t112 - t111) + "; Orderlines: " + (t113 - t112)
          + "; Shipment: " + (t115 - t113) + "; Invoice: " + (t11 - t115));
    } finally {
      TriggerHandler.getInstance().enable();
    }

    long t4 = System.currentTimeMillis();

    if (!isQuotation) {
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

      // Call all OrderProcess injected.
      for (Iterator<OrderLoaderHook> procIter = orderProcesses.iterator(); procIter.hasNext();) {
        OrderLoaderHook proc = procIter.next();
        proc.exec(jsonorder, order, shipment, invoice);
      }
    }

    log.info("Initial flush: " + (t1 - t0) + "; Generate bobs:" + (t11 - t1) + "; Save bobs:"
        + (t2 - t11) + "; First flush:" + (t3 - t2) + "; Second flush: " + (t4 - t3)
        + "; Process Payments:" + (System.currentTimeMillis() - t4));

    return successMessage(jsonorder);
  }

  private void updateAuditInfo(Order order, Invoice invoice, JSONObject jsonorder)
      throws JSONException {
    Long value = jsonorder.getLong("created");
    order.set("creationDate", new Date(value));
    if (invoice != null) {
      invoice.set("creationDate", new Date(value));
    }
  }

  protected void associateOrderToQuotation(JSONObject jsonorder, Order order) throws JSONException {
    String quotationId = jsonorder.getString("oldId");
    Order quotation = OBDal.getInstance().get(Order.class, quotationId);
    order.setQuotation(quotation);
    List<OrderLine> orderLines = order.getOrderLineList();
    List<OrderLine> quotationLines = quotation.getOrderLineList();
    for (int i = 0; (i < orderLines.size() && i < quotationLines.size()); i++) {
      orderLines.get(i).setQuotationLine(quotationLines.get(i));
    }
    quotation.setDocumentStatus("CA");

  }

  protected JSONObject successMessage(JSONObject jsonorder) throws Exception {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    return jsonResponse;
  }

  protected void deleteOldDocument(JSONObject jsonorder) throws JSONException {
    Order oldOrder = OBDal.getInstance().get(Order.class, jsonorder.getString("oldId"));
    OBDal.getInstance().remove(oldOrder);
    OBDal.getInstance().flush();
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
    int stdPrecision = order.getCurrency().getStandardPrecision().intValue();

    for (int i = 0; i < orderlines.length(); i++) {
      InvoiceLine line = OBProvider.getInstance().get(InvoiceLine.class);
      Entity inlineEntity = ModelProvider.getInstance().getEntity(InvoiceLine.class);
      JSONPropertyToEntity.fillBobFromJSON(inlineEntity, line, orderlines.getJSONObject(i),
          jsonorder.getLong("timezoneOffset"));
      JSONPropertyToEntity.fillBobFromJSON(
          ModelProvider.getInstance().getEntity(InvoiceLine.class), line, jsonorder,
          jsonorder.getLong("timezoneOffset"));
      line.setLineNo((long) ((i + 1) * 10));
      line.setLineNetAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("net"))
          .setScale(stdPrecision, RoundingMode.HALF_UP));
      BigDecimal qty = lineReferences.get(i).getOrderedQuantity();
      line.setInvoicedQuantity(qty);
      lineReferences.get(i).setInvoicedQuantity(qty);
      line.setInvoice(invoice);
      line.setSalesOrderLine(lineReferences.get(i));
      line.setGrossAmount(lineReferences.get(i).getLineGrossAmount()
          .setScale(stdPrecision, RoundingMode.HALF_UP));
      invoice.getInvoiceLineList().add(line);
      OBDal.getInstance().save(line);

      JSONObject taxes = orderlines.getJSONObject(i).getJSONObject("taxLines");
      @SuppressWarnings("unchecked")
      Iterator<String> itKeys = taxes.keys();
      int ind = 0;
      while (itKeys.hasNext()) {
        String taxId = (String) itKeys.next();
        JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
        InvoiceLineTax invoicelinetax = OBProvider.getInstance().get(InvoiceLineTax.class);
        TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
            ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
        invoicelinetax.setTax(tax);
        invoicelinetax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
            stdPrecision, RoundingMode.HALF_UP));
        invoicelinetax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
            stdPrecision, RoundingMode.HALF_UP));
        invoicelinetax.setInvoice(invoice);
        invoicelinetax.setInvoiceLine(line);
        invoicelinetax.setRecalculate(true);
        invoicelinetax.setLineNo((long) ((ind + 1) * 10));
        ind++;
        invoice.getInvoiceLineTaxList().add(invoicelinetax);
        line.getInvoiceLineTaxList().add(invoicelinetax);
        OBDal.getInstance().save(invoicelinetax);
      }

      // Discounts & Promotions
      if (orderlines.getJSONObject(i).has("promotions")
          && !orderlines.getJSONObject(i).isNull("promotions")
          && !orderlines.getJSONObject(i).getString("promotions").equals("null")) {
        JSONArray jsonPromotions = orderlines.getJSONObject(i).getJSONArray("promotions");
        for (int p = 0; p < jsonPromotions.length(); p++) {
          JSONObject jsonPromotion = jsonPromotions.getJSONObject(p);
          boolean hasActualAmt = jsonPromotion.has("actualAmt");
          if (hasActualAmt && jsonPromotion.getDouble("actualAmt") == 0) {
            continue;
          }

          InvoiceLineOffer promotion = OBProvider.getInstance().get(InvoiceLineOffer.class);
          JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion,
              jsonorder.getLong("timezoneOffset"));

          if (hasActualAmt) {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt"))
                .setScale(stdPrecision, RoundingMode.HALF_UP));
          } else {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt")).setScale(
                stdPrecision, RoundingMode.HALF_UP));
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
    JSONPropertyToEntity.fillBobFromJSON(invoiceEntity, invoice, jsonorder,
        jsonorder.getLong("timezoneOffset"));

    int stdPrecision = order.getCurrency().getStandardPrecision().intValue();

    String description = jsonorder.has("description") ? jsonorder.getString("description") + "\n"
        : "";
    description += OBMessageUtils.getI18NMessage("OBPOS_InvoiceRelatedToOrder", null)
        + jsonorder.getString("documentNo");
    invoice.setDescription(description);
    invoice
        .setDocumentType(getInvoiceDocumentType((String) DalUtil.getId(order.getDocumentType())));
    invoice.setTransactionDocument(getInvoiceDocumentType((String) DalUtil.getId(order
        .getDocumentType())));
    invoice.setDocumentNo(getDocumentNo(invoiceEntity, invoice.getTransactionDocument(),
        invoice.getDocumentType()));
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
    invoice.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")).setScale(
        stdPrecision, RoundingMode.HALF_UP));
    invoice.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("net")).setScale(
        stdPrecision, RoundingMode.HALF_UP));
    invoice.setTotalPaid(BigDecimal.ZERO);
    invoice.setOutstandingAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))).setScale(
        stdPrecision, RoundingMode.HALF_UP));
    invoice.setDueAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))).setScale(stdPrecision,
        RoundingMode.HALF_UP));

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
      invoiceTax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
          stdPrecision, RoundingMode.HALF_UP));
      invoiceTax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
          stdPrecision, RoundingMode.HALF_UP));
      invoiceTax.setInvoice(invoice);
      invoiceTax.setLineNo((long) ((i + 1) * 10));
      invoiceTax.setRecalculate(true);
      i++;
      invoice.getInvoiceTaxList().add(invoiceTax);
    }

    // Update customer credit
    BigDecimal total = invoice.getGrandTotalAmount().setScale(stdPrecision, RoundingMode.HALF_UP);

    if (!invoice.getCurrency().equals(invoice.getBusinessPartner().getPriceList().getCurrency())) {
      total = convertCurrencyInvoice(invoice);
    }
    OBContext.setAdminMode(false);
    try {
      // Same currency, no conversion required
      if (jsonorder.getLong("orderType") == 1) {
        invoice.getBusinessPartner().setCreditUsed(
            invoice.getBusinessPartner().getCreditUsed().subtract(total));
      } else {
        invoice.getBusinessPartner().setCreditUsed(
            invoice.getBusinessPartner().getCreditUsed().add(total));
      }
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public static BigDecimal convertCurrencyInvoice(Invoice invoice) {
    int stdPrecision = invoice.getCurrency().getStandardPrecision().intValue();
    List<Object> parameters = new ArrayList<Object>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(invoice.getGrandTotalAmount().setScale(stdPrecision, RoundingMode.HALF_UP));
    types.add(BigDecimal.class);
    parameters.add(invoice.getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(invoice.getBusinessPartner().getPriceList().getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(invoice.getInvoiceDate());
    types.add(Timestamp.class);
    parameters.add("S");
    types.add(String.class);
    parameters.add(OBContext.getOBContext().getCurrentClient());
    types.add(BaseOBObject.class);
    parameters.add(OBContext.getOBContext().getCurrentOrganization());
    types.add(BaseOBObject.class);
    parameters.add('A');
    types.add(Character.class);

    return (BigDecimal) CallStoredProcedure.getInstance().call("c_currency_convert_precision",
        parameters, types);
  }

  protected void createShipmentLines(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences) throws JSONException {
    int lineNo = 0;
    Entity shplineentity = ModelProvider.getInstance().getEntity(ShipmentInOutLine.class);
    for (int i = 0; i < orderlines.length(); i++) {
      String hqlWhereClause;

      OrderLine orderLine = lineReferences.get(i);
      BigDecimal pendingQty = orderLine.getOrderedQuantity();

      AttributeSetInstance oldAttributeSetValues = null;
      if (pendingQty.compareTo(BigDecimal.ZERO) > 0) {
        // The M_GetStock function is used
        Process process = (Process) OBDal.getInstance().get(Process.class,
            "FF80818132C964E30132C9747257002E");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("AD_Client_ID", OBContext.getOBContext().getCurrentClient().getId());
        parameters.put("AD_Org_ID", OBContext.getOBContext().getCurrentOrganization().getId());
        parameters.put("M_Product_ID",
            orderLine.getProduct() == null ? null : (String) DalUtil.getId(orderLine.getProduct()));
        parameters.put("C_Uom_ID",
            orderLine.getUOM() == null ? null : (String) DalUtil.getId(orderLine.getUOM()));
        parameters.put("M_Product_Uom_ID", orderLine.getOrderUOM() == null ? null
            : (String) DalUtil.getId(orderLine.getOrderUOM()));
        parameters.put("M_AttributesetInstance_ID", orderLine.getAttributeSetValue() == null ? null
            : (String) DalUtil.getId(orderLine.getAttributeSetValue()));
        parameters.put("Quantity", pendingQty);
        parameters.put("ProcessID", "118");
        if (orderLine.getEntity().hasProperty("warehouseRule")
            && orderLine.get("warehouseRule") != null) {
          parameters.put("M_Warehouse_Rule_ID",
              (String) DalUtil.getId(orderLine.get("warehouseRule")));
        }

        ProcessInstance pInstance = CallProcess.getInstance()
            .callProcess(process, null, parameters);

        OBCriteria<StockProposed> stockProposed = OBDal.getInstance().createCriteria(
            StockProposed.class);
        stockProposed.add(Restrictions.eq(StockProposed.PROPERTY_PROCESSINSTANCE, pInstance));
        stockProposed.addOrderBy(StockProposed.PROPERTY_PRIORITY, true);

        ScrollableResults bins = stockProposed.scroll(ScrollMode.FORWARD_ONLY);
        boolean foundStockProposed = false;
        while (pendingQty.compareTo(BigDecimal.ZERO) > 0 && bins.next()) {
          foundStockProposed = true;
          // TODO: Can we safely clear session here?
          StockProposed stock = (StockProposed) bins.get(0);
          BigDecimal qty;

          if (pendingQty.compareTo(new BigDecimal(stock.getQuantity())) > 0) {
            qty = new BigDecimal(stock.getQuantity());
            pendingQty = pendingQty.subtract(qty);
          } else {
            qty = pendingQty;
            pendingQty = BigDecimal.ZERO;
          }
          lineNo += 10;
          addShipemntline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
              jsonorder, lineNo, qty, stock.getStorageDetail().getStorageBin(), stock
                  .getStorageDetail().getAttributeSetValue());
        }
        if (!foundStockProposed && orderLine.getProduct().getAttributeSet() != null) {
          // M_GetStock couldn't find any valid stock, and the product has an attribute set. We will
          // attempt to find an old transaction for this product, and get the attribute values from
          // there
          OBCriteria<ShipmentInOutLine> oldLines = OBDal.getInstance().createCriteria(
              ShipmentInOutLine.class);
          oldLines.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_PRODUCT, orderLine.getProduct()));
          oldLines.setMaxResults(1);
          oldLines.addOrderBy(ShipmentInOutLine.PROPERTY_CREATIONDATE, false);
          List<ShipmentInOutLine> oldLine = oldLines.list();
          if (oldLine.size() > 0) {
            oldAttributeSetValues = oldLine.get(0).getAttributeSetValue();
          }

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
            lineNo, pendingQty, queryLoc.list().get(0), oldAttributeSetValues);
      }
    }
  }

  private void addShipemntline(ShipmentInOut shipment, Entity shplineentity,
      JSONObject jsonOrderLine, OrderLine orderLine, JSONObject jsonorder, long lineNo,
      BigDecimal qty, Locator bin, AttributeSetInstance attributeSetInstance) throws JSONException {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);

    JSONPropertyToEntity.fillBobFromJSON(shplineentity, line, jsonOrderLine,
        jsonorder.getLong("timezoneOffset"));
    JSONPropertyToEntity.fillBobFromJSON(
        ModelProvider.getInstance().getEntity(ShipmentInOutLine.class), line, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    line.setLineNo(lineNo);
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);

    line.setMovementQuantity(qty);
    line.setStorageBin(bin);
    if (attributeSetInstance != null) {
      line.setAttributeSetValue(attributeSetInstance);
    }
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
  }

  protected void createShipment(ShipmentInOut shipment, Order order, JSONObject jsonorder)
      throws JSONException {
    Entity shpEntity = ModelProvider.getInstance().getEntity(ShipmentInOut.class);
    JSONPropertyToEntity.fillBobFromJSON(shpEntity, shipment, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    shipment.setDocumentNo(null);
    shipment
        .setDocumentType(getShipmentDocumentType((String) DalUtil.getId(order.getDocumentType())));
    shipment.setDocumentNo(getDocumentNo(shpEntity, null, shipment.getDocumentType()));
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
    int stdPrecision = order.getCurrency().getStandardPrecision().intValue();

    for (int i = 0; i < orderlines.length(); i++) {

      OrderLine orderline = OBProvider.getInstance().get(OrderLine.class);
      JSONObject jsonOrderLine = orderlines.getJSONObject(i);

      JSONPropertyToEntity.fillBobFromJSON(ModelProvider.getInstance().getEntity(OrderLine.class),
          orderline, jsonorder, jsonorder.getLong("timezoneOffset"));
      JSONPropertyToEntity.fillBobFromJSON(orderLineEntity, orderline, jsonOrderLine,
          jsonorder.getLong("timezoneOffset"));

      orderline.setActive(true);
      orderline.setSalesOrder(order);
      orderline.setLineNetAmount(BigDecimal.valueOf(jsonOrderLine.getDouble("net")).setScale(
          stdPrecision, RoundingMode.HALF_UP));

      // shipment is created, so all is delivered
      orderline.setDeliveredQuantity(orderline.getOrderedQuantity());

      lineReferences.add(orderline);
      orderline.setLineNo((long) ((i + 1) * 10));
      order.getOrderLineList().add(orderline);
      OBDal.getInstance().save(orderline);

      JSONObject taxes = jsonOrderLine.getJSONObject("taxLines");
      @SuppressWarnings("unchecked")
      Iterator<String> itKeys = taxes.keys();
      int ind = 0;
      while (itKeys.hasNext()) {
        String taxId = (String) itKeys.next();
        JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
        OrderLineTax orderlinetax = OBProvider.getInstance().get(OrderLineTax.class);
        TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
            ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
        orderlinetax.setTax(tax);
        orderlinetax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
            stdPrecision, RoundingMode.HALF_UP));
        orderlinetax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
            stdPrecision, RoundingMode.HALF_UP));
        orderlinetax.setSalesOrder(order);
        orderlinetax.setSalesOrderLine(orderline);
        orderlinetax.setLineNo((long) ((ind + 1) * 10));
        ind++;
        orderline.getOrderLineTaxList().add(orderlinetax);
        order.getOrderLineTaxList().add(orderlinetax);
        OBDal.getInstance().save(orderlinetax);
      }

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
          JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion,
              jsonorder.getLong("timezoneOffset"));

          if (hasActualAmt) {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt"))
                .setScale(stdPrecision, RoundingMode.HALF_UP));
          } else {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt")).setScale(
                stdPrecision, RoundingMode.HALF_UP));
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
    JSONPropertyToEntity.fillBobFromJSON(orderEntity, order, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    order.setNewOBObject(true);
    order.setId(jsonorder.getString("id"));
    int stdPrecision = order.getCurrency().getStandardPrecision().intValue();

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
    order.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")).setScale(
        stdPrecision, RoundingMode.HALF_UP));
    order.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("net")).setScale(stdPrecision,
        RoundingMode.HALF_UP));

    order.setSalesTransaction(true);
    if (jsonorder.getBoolean("isQuotation")) {
      order.setDocumentStatus("UE");
    } else {
      order.setDocumentStatus("CO");
    }
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
      orderTax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
          stdPrecision, RoundingMode.HALF_UP));
      orderTax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
          stdPrecision, RoundingMode.HALF_UP));
      orderTax.setSalesOrder(order);
      orderTax.setLineNo((long) ((i + 1) * 10));
      i++;
      order.getOrderTaxList().add(orderTax);
    }
  }

  protected void handleStock(ShipmentInOut shipment) {
    for (ShipmentInOutLine line : shipment.getMaterialMgmtShipmentInOutLineList()) {
      if (line.getProduct().getProductType().equals("I") && line.getProduct().isStocked()) {
        // Stock is changed only for stocked products of type "Item"
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
        transaction.setAttributeSetValue(line.getAttributeSetValue());

        OBDal.getInstance().save(transaction);
      }
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
      int stdPrecision = order.getCurrency().getStandardPrecision().intValue();
      paymentSchedule.setAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")).setScale(
          stdPrecision, RoundingMode.HALF_UP));
      // Sept 2012 -> gross because outstanding is not allowed in Openbravo Web POS
      paymentSchedule.setOutstandingAmount(BigDecimal.valueOf(jsonorder.getDouble("gross"))
          .setScale(stdPrecision, RoundingMode.HALF_UP));
      paymentSchedule.setDueDate(order.getOrderDate());
      paymentSchedule.setExpectedDate(order.getOrderDate());
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
        paymentScheduleInvoice.setAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")).setScale(
            stdPrecision, RoundingMode.HALF_UP));
        paymentScheduleInvoice.setOutstandingAmount(BigDecimal
            .valueOf(jsonorder.getDouble("gross")).setScale(stdPrecision, RoundingMode.HALF_UP));
        paymentScheduleInvoice.setDueDate(order.getOrderDate());
        paymentScheduleInvoice.setExpectedDate(order.getOrderDate());
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
          @SuppressWarnings("unchecked")
          Class<PaymentProcessor> paymentclazz = (Class<PaymentProcessor>) Class
              .forName(paymentTypeName);
          PaymentProcessor paymentinst = paymentclazz.newInstance();
          paymentinst.process(payment, order, invoice, i == (payments.length() - 1) ? writeoffAmt
              : BigDecimal.ZERO);
        } else {
          processPayments(paymentSchedule, paymentScheduleInvoice, order, invoice, paymentType,
              payment, i == (payments.length() - 1) ? writeoffAmt : BigDecimal.ZERO);
        }
      }
      if (writeoffAmt.signum() == -1 && invoice != null) {
        FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
            FIN_PaymentScheduleDetail.class);
        paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
        paymentScheduleDetail.setAmount(writeoffAmt.abs());
        if (paymentScheduleInvoice != null) {
          paymentScheduleDetail.setInvoicePaymentSchedule(paymentScheduleInvoice);
        }
        OBDal.getInstance().save(paymentScheduleDetail);
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
      int stdPrecision = order.getCurrency().getStandardPrecision().intValue();
      BigDecimal amount = BigDecimal.valueOf(payment.getDouble("paid")).setScale(stdPrecision,
          RoundingMode.HALF_UP);
      BigDecimal origAmount = amount;
      BigDecimal mulrate = new BigDecimal(1);
      // FIXME: Conversion should be only in one direction: (USD-->EUR)
      if (payment.has("mulrate") && payment.getDouble("mulrate") != 1) {
        mulrate = BigDecimal.valueOf(payment.getDouble("mulrate"));
        origAmount = amount.multiply(BigDecimal.valueOf(payment.getDouble("mulrate"))).setScale(
            stdPrecision, RoundingMode.HALF_UP);
      }

      // writeoffAmt.divide(BigDecimal.valueOf(payment.getDouble("rate")));
      if (amount.signum() == 0) {
        return;
      }
      if (writeoffAmt.signum() == 1) {
        // there was an overpayment, we need to take into account the writeoffamt
        amount = amount.subtract(writeoffAmt).setScale(stdPrecision, RoundingMode.HALF_UP);
      }

      FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
      paymentScheduleDetail.setAmount(order.getGrandTotalAmount());
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
        origDetail.setWriteoffAmount(paymentScheduleDetail.getWriteoffAmount().setScale(
            stdPrecision, RoundingMode.HALF_UP));

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
      if (writeoffAmt.signum() == 1) {
        FIN_AddPayment.saveGLItem(finPayment, writeoffAmt, paymentType.getPaymentMethod()
            .getGlitemWriteoff());
      }
      // Update Payment In amount after adding GLItem
      finPayment.setAmount(BigDecimal.valueOf(payment.getDouble("paid")).setScale(stdPrecision,
          RoundingMode.HALF_UP));
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

  protected void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json)
      throws JSONException {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.equals("id")) {
        continue;
      }
      String oldKey = key;
      if (entity.hasProperty(key)) {
        log.debug("Found property: " + key + " in entity " + entity.getName());
      } else {
        key = getEquivalentKey(key);
        if (key == null) {
          log.debug("Did not find property: " + oldKey);
          continue;
        } else {
          if (entity.hasProperty(key)) {
            log.debug("Found equivalent key: " + key);
          } else {
            log.debug("Did not find property: " + oldKey);
            continue;
          }
        }
      }

      Property p = entity.getProperty(key);
      Object value = json.get(oldKey);
      if (p.isPrimitive()) {
        if (p.isDate()) {
          bob.set(p.getName(),
              (Date) JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.DATE, value));
        } else if (p.isNumericType()) {
          value = json.getString(oldKey);
          bob.set(key, new BigDecimal((String) value));
        } else {
          bob.set(p.getName(), value);
        }
      } else {
        Property refProp = p.getReferencedProperty();
        Entity refEntity = refProp.getEntity();
        if (value instanceof JSONObject) {
          value = ((JSONObject) value).getString("id");
        }
        BaseOBObject refBob = OBDal.getInstance().getProxy(refEntity.getName(), value.toString());
        bob.set(p.getName(), refBob);
      }

    }
  }

  public static String getErrorMessage(Exception e) {
    StringWriter sb = new StringWriter();
    e.printStackTrace(new PrintWriter(sb));
    return sb.toString();
  }

  private static String getEquivalentKey(String key) {
    if (key.equals("bp")) {
      return "businessPartner";
    } else if (key.equals("bploc")) {
      return "partnerAddress";
    } else if (key.equals("qty")) {
      return "orderedQuantity";
    } else if (key.equals("price")) {
      return "grossUnitPrice";
    } else if (key.equals("posTerminal")) {
      return "obposApplications";
    } else if (key.equals("pricenet")) {
      return "unitPrice";
    }
    return null;
  }

  protected String getDocumentNo(Entity entity, DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "", entity
            .getTableName(), doctypeTarget == null ? "" : doctypeTarget.getId(),
        doctype == null ? "" : doctype.getId(), false, true);
  }
}
