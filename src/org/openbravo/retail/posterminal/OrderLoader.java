/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSAppPayment;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;

public class OrderLoader {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> invoiceDocTypes = new HashMap<String, DocumentType>();

  private static final Logger log = Logger.getLogger(OrderLoader.class);

  public JSONObject saveOrder(JSONArray jsonarray) throws JSONException {
    boolean error = false;
    for (int i = 0; i < jsonarray.length(); i++) {
      if (i % 1 == 0) {
        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      }
      long t1 = System.currentTimeMillis();
      JSONObject jsonorder = jsonarray.getJSONObject(i);
      JSONObject result = saveOrder(jsonorder);
      if (!result.get(JsonConstants.RESPONSE_STATUS)
          .equals(JsonConstants.RPCREQUEST_STATUS_SUCCESS)) {
        log.error("There was an error importing order: " + jsonorder.toString());
        error = true;
      }
      log.info("Total order time: " + (System.currentTimeMillis() - t1));
    }
    OBDal.getInstance().flush();
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

  public JSONObject saveOrder(JSONObject jsonorder) throws JSONException {

    long t1 = System.currentTimeMillis();
    long t11;
    long t2;
    Order order = null;
    ShipmentInOut shipment = null;
    Invoice invoice = null;
    TriggerHandler.getInstance().disable();
    try {

      boolean createInvoice = true;
      // Order header
      order = OBProvider.getInstance().get(Order.class);
      createOrder(order, jsonorder);

      // Order lines
      ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      JSONArray orderlines = jsonorder.getJSONArray("lines");
      createOrderLines(order, jsonorder, orderlines, lineReferences);

      // Shipment header
      shipment = OBProvider.getInstance().get(ShipmentInOut.class);
      createShipment(shipment, order, jsonorder);

      // Shipment lines
      createShipmentLines(shipment, order, jsonorder, orderlines, lineReferences);

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
      OBDal.getInstance().save(invoice);
      t2 = System.currentTimeMillis();

    } finally {
      TriggerHandler.getInstance().enable();
    }

    long t3 = System.currentTimeMillis();

    // Payment
    JSONObject paymentResponse = handlePayments(jsonorder, order, invoice);
    if (paymentResponse != null) {
      return paymentResponse;
    }

    // Stock manipulation
    handleStock(shipment);

    log.info("Generate bobs:" + (t11 - t1) + "; Save bobs:" + (t2 - t11) + "; Flush:" + (t3 - t2)
        + "; Process Payments:" + (System.currentTimeMillis() - t3));

    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    jsonResponse.put("data", jsonorder);

    return jsonResponse;
  }

  protected DocumentType getPaymentDocumentType(Organization org) {
    if (paymentDocTypes.get(DalUtil.getId(org)) != null) {
      return paymentDocTypes.get(DalUtil.getId(org));
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARReceipt);
    paymentDocTypes.put((String) DalUtil.getId(org), docType);
    return docType;

  }

  protected DocumentType getInvoiceDocumentType(Organization org) {
    if (invoiceDocTypes.get(DalUtil.getId(org)) != null) {
      return invoiceDocTypes.get(DalUtil.getId(org));
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARInvoice);
    invoiceDocTypes.put((String) DalUtil.getId(org), docType);
    return docType;
  }

  protected void createInvoiceLines(Invoice invoice, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences) throws JSONException {

    for (int i = 0; i < orderlines.length(); i++) {
      InvoiceLine line = OBProvider.getInstance().get(InvoiceLine.class);
      Entity inlineEntity = ModelProvider.getInstance().getEntity(InvoiceLine.class);
      fillBobFromJSON(inlineEntity, line, orderlines.getJSONObject(i));
      fillBobFromJSON(ModelProvider.getInstance().getEntity(InvoiceLine.class), line, jsonorder);
      line.setLineNo((long) ((i + 1) * 10));
      line.setLineNetAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("net")));
      line.setInvoicedQuantity(lineReferences.get(i).getOrderedQuantity());
      line.setInvoice(invoice);
      line.setSalesOrderLine(lineReferences.get(i));
      invoice.getInvoiceLineList().add(line);
    }

  }

  protected void createInvoice(Invoice invoice, Order order, JSONObject jsonorder)
      throws JSONException {
    Entity invoiceEntity = ModelProvider.getInstance().getEntity(Invoice.class);
    fillBobFromJSON(invoiceEntity, invoice, jsonorder);

    invoice.setDocumentType(getInvoiceDocumentType(order.getOrganization()));
    invoice.setTransactionDocument(getInvoiceDocumentType(order.getOrganization()));
    invoice.setAccountingDate(order.getOrderDate());
    invoice.setInvoiceDate(order.getOrderDate());
    invoice.setSalesTransaction(true);
    invoice.setDocumentStatus("CO");
    invoice.setDocumentAction("RE");
    invoice.setAPRMProcessinvoice("RE");
    invoice.setSalesOrder(order);
    invoice.setProcessed(true);
    invoice.setPaymentMethod(order.getBusinessPartner().getPaymentMethod());
    invoice.setPaymentTerms(order.getBusinessPartner().getPaymentTerms());
    invoice.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
    invoice.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
    invoice.setTotalPaid(BigDecimal.ZERO);
    invoice.setOutstandingAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))));
    invoice.setDueAmount((BigDecimal.valueOf(jsonorder.getDouble("gross"))));

    // Create invoice tax lines
    JSONObject taxes = jsonorder.getJSONObject("taxes");
    Iterator<String> itKeys = taxes.keys();
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
      invoice.getInvoiceTaxList().add(invoiceTax);
    }

  }

  protected void createShipmentLines(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences) throws JSONException {
    for (int i = 0; i < orderlines.length(); i++) {

      ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
      Entity shplineentity = ModelProvider.getInstance().getEntity(ShipmentInOutLine.class);
      fillBobFromJSON(shplineentity, line, orderlines.getJSONObject(i));
      fillBobFromJSON(ModelProvider.getInstance().getEntity(ShipmentInOutLine.class), line,
          jsonorder);
      line.setLineNo((long) ((i + 1) * 10));
      line.setMovementQuantity(lineReferences.get(i).getOrderedQuantity());
      line.setShipmentReceipt(shipment);
      line.setSalesOrderLine(lineReferences.get(i));
      line.setStorageBin(order.getWarehouse().getLocatorList().get(0));
      shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    }

  }

  protected void createShipment(ShipmentInOut shipment, Order order, JSONObject jsonorder)
      throws JSONException {
    Entity shpEntity = ModelProvider.getInstance().getEntity(ShipmentInOut.class);
    fillBobFromJSON(shpEntity, shipment, jsonorder);

    shipment.setAccountingDate(order.getOrderDate());
    shipment.setMovementDate(order.getOrderDate());
    shipment.setSalesTransaction(true);
    shipment.setDocumentStatus("CO");
    shipment.setDocumentAction("--");
    shipment.setMovementType("C-");
    shipment.setProcessNow(false);
    shipment.setSalesOrder(order);

  }

  protected void createOrderLines(Order order, JSONObject jsonorder, JSONArray orderlines,
      ArrayList<OrderLine> lineReferences) throws JSONException {
    for (int i = 0; i < orderlines.length(); i++) {

      OrderLine orderline = OBProvider.getInstance().get(OrderLine.class);
      Entity orderLineEntity = ModelProvider.getInstance().getEntity(OrderLine.class);
      fillBobFromJSON(orderLineEntity, orderline, orderlines.getJSONObject(i));
      fillBobFromJSON(ModelProvider.getInstance().getEntity(OrderLine.class), orderline, jsonorder);
      orderline.setActive(true);
      orderline.setSalesOrder(order);
      orderline.setLineNetAmount(BigDecimal.valueOf(orderlines.getJSONObject(i).getDouble("net")));

      lineReferences.add(orderline);
      orderline.setLineNo((long) ((i + 1) * 10));

      order.getOrderLineList().add(orderline);
    }
  }

  protected void createOrder(Order order, JSONObject jsonorder) throws JSONException {
    Entity orderEntity = ModelProvider.getInstance().getEntity(Order.class);
    fillBobFromJSON(orderEntity, order, jsonorder);

    order.setTransactionDocument(OBDal.getInstance().get(DocumentType.class,
        jsonorder.getString("documentType")));
    order.setAccountingDate(order.getOrderDate());
    order.setScheduledDeliveryDate(order.getOrderDate());

    order.setInvoiceAddress(order.getPartnerAddress());
    order.setPaymentMethod(order.getBusinessPartner().getPaymentMethod());
    order.setPaymentTerms(order.getBusinessPartner().getPaymentTerms());
    order.setInvoiceTerms(order.getBusinessPartner().getInvoiceTerms());
    order.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));
    order.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")));

    order.setSalesTransaction(true);
    order.setDocumentStatus("CO");
    order.setDocumentAction("--");
    order.setProcessed(true);
    order.setProcessNow(false);

    JSONObject taxes = jsonorder.getJSONObject("taxes");
    Iterator<String> itKeys = taxes.keys();
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
      order.getOrderTaxList().add(orderTax);
    }
  }

  protected void handleStock(ShipmentInOut shipment) {
    for (ShipmentInOutLine line : shipment.getMaterialMgmtShipmentInOutLineList()) {
      MaterialTransaction transaction = OBProvider.getInstance().get(MaterialTransaction.class);
      transaction.setMovementType(shipment.getMovementType());
      transaction.setProduct(line.getProduct());
      transaction.setStorageBin(line.getStorageBin());
      transaction.setOrderUOM(line.getOrderUOM());
      transaction.setUOM(line.getUOM());
      transaction.setOrderQuantity(line.getOrderQuantity());
      transaction.setMovementQuantity(line.getMovementQuantity());
      transaction.setMovementDate(shipment.getMovementDate());
      transaction.setGoodsShipmentLine(line);

      OBDal.getInstance().save(transaction);
    }
  }

  protected JSONObject handlePayments(JSONObject jsonorder, Order order, Invoice invoice)
      throws JSONException {
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
          processPayments(order, invoice, paymentType, payment);
        }
      }
      return null;
    }

  }

  protected void processPayments(Order order, Invoice invoice, OBPOSAppPayment paymentType,
      JSONObject payment) throws JSONException {
    long t1 = System.currentTimeMillis();
    OBContext.setAdminMode(true);
    try {
      BigDecimal amount = BigDecimal.valueOf(payment.getDouble("paid"));
      FIN_PaymentSchedule paymentSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
      paymentSchedule.setCurrency(order.getCurrency());
      paymentSchedule.setOrder(order);
      paymentSchedule.setFinPaymentmethod(paymentType.getPaymentMethod());
      paymentSchedule.setAmount(amount);
      paymentSchedule.setOutstandingAmount(amount);
      paymentSchedule.setDueDate(order.getOrderDate());
      paymentSchedule.setFINPaymentPriority(order.getFINPaymentPriority());

      FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
      paymentScheduleDetail.setAmount(amount);
      paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
          paymentScheduleDetail);

      OBDal.getInstance().save(paymentSchedule);
      OBDal.getInstance().save(paymentScheduleDetail);
      if (invoice != null) {
        FIN_PaymentSchedule paymentScheduleInvoice = OBProvider.getInstance().get(
            FIN_PaymentSchedule.class);
        paymentScheduleInvoice.setCurrency(order.getCurrency());
        paymentScheduleInvoice.setInvoice(invoice);
        paymentScheduleInvoice.setFinPaymentmethod(paymentType.getPaymentMethod());
        paymentScheduleInvoice.setAmount(amount);
        paymentScheduleInvoice.setOutstandingAmount(amount);
        paymentScheduleInvoice.setDueDate(order.getOrderDate());
        paymentScheduleInvoice.setFINPaymentPriority(order.getFINPaymentPriority());
        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(
            paymentScheduleDetail);
        paymentScheduleDetail.setInvoicePaymentSchedule(paymentScheduleInvoice);
        OBDal.getInstance().save(paymentScheduleInvoice);
      }

      HashMap<String, BigDecimal> paymentAmount = new HashMap<String, BigDecimal>();
      paymentAmount.put(paymentScheduleDetail.getId(), amount);

      FIN_FinancialAccount account = paymentType.getFinancialAccount();

      long t2 = System.currentTimeMillis();
      // Save Payment
      FIN_Payment finPayment = FIN_AddPayment.savePayment(null, true, getPaymentDocumentType(order
          .getOrganization()), order.getDocumentNo(), order.getBusinessPartner(), paymentType
          .getPaymentMethod(), account, paymentSchedule.getAmount().toString(), order
          .getOrderDate(), order.getOrganization(), null, paymentSchedule
          .getFINPaymentScheduleDetailOrderPaymentScheduleList(), paymentAmount, false, false);

      long t3 = System.currentTimeMillis();
      // Process Payment
      FIN_AddPayment.processPayment(RequestContext.get().getVariablesSecureApp(),
          new DalConnectionProvider(false), "D", finPayment);
      log.debug("Payment. Create entities: " + (t2 - t1) + "; Save payment: " + (t3 - t2)
          + "; Process payment: " + (System.currentTimeMillis() - t3));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  protected void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json)
      throws JSONException {
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
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
          if (key.equals("product")) {
            value = ((JSONObject) value).get("product");
          }
          value = ((JSONObject) value).getString("id");
        }
        BaseOBObject refBob = OBDal.getInstance().getProxy(refEntity.getName(), value.toString());
        // BaseOBObject refBob = OBDal.getInstance().get(refEntity.getName(), value.toString());
        bob.set(p.getName(), refBob);
      }

    }
  }

  private static String getEquivalentKey(String key) {
    if (key.equals("bp")) {
      return "businessPartner";
    } else if (key.equals("bploc")) {
      return "partnerAddress";
    } else if (key.equals("qty")) {
      return "orderedQuantity";
    } else if (key.equals("price")) {
      return "unitPrice";
    } else if (key.equals("posTerminal")) {
      return "obposApplications";
    } else if (key.equals("taxId")) {
      return "tax";
    }
    return null;
  }

}
