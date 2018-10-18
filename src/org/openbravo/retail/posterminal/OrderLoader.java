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
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.common.order.OrderlineServiceRelation;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.retail.posterminal.utility.AttributesUtils;
import org.openbravo.retail.posterminal.utility.DocumentNoHandler;
import org.openbravo.retail.posterminal.utility.ShipmentInOut_Utils;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.importprocess.ImportEntryManager;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "Order")
public class OrderLoader extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  private static final Logger log = Logger.getLogger(OrderLoader.class);

  // DocumentNo Handler are used to collect all needed document numbers and create and set
  // them as late in the process as possible
  private static ThreadLocal<List<DocumentNoHandler>> documentNoHandlers = new ThreadLocal<List<DocumentNoHandler>>();

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> invoiceDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<String, DocumentType>();
  HashMap<String, JSONArray> orderLineServiceList;
  String paymentDescription = null;
  private boolean newLayaway = false;
  private boolean notpaidLayaway = false;
  private boolean creditpaidLayaway = false;
  private boolean partialpaidLayaway = false;
  private boolean fullypaidLayaway = false;
  private boolean createShipment = true;
  private boolean isQuotation = false;
  private boolean isDeleted = false;
  private boolean isModified = false;
  private boolean doCancelAndReplace = false;
  private boolean doCancelLayaway = false;
  private boolean paidReceipt = false;
  private boolean deliver = false;
  private boolean hasPrepayment = false;
  private boolean donePressed = false;
  private boolean paidOnCredit = false;
  private boolean isNegative = false;

  @Inject
  @Any
  private Instance<OrderLoaderHook> orderProcesses;

  @Inject
  @Any
  private Instance<OrderLoaderModifiedHook> orderModifiedProcesses;

  @Inject
  @Any
  private Instance<OrderLoaderPreProcessHook> orderPreProcesses;

  @Inject
  @Any
  private Instance<OrderLoaderModifiedPreProcessHook> orderModifiedPreProcesses;

  @Inject
  @Any
  private Instance<OrderLoaderHookForQuotations> quotationProcesses;

  @Inject
  @Any
  private Instance<OrderLoaderPreProcessPaymentHook> preProcessPayment;

  @Inject
  @Any
  private Instance<OrderLoaderPreAddShipmentLineHook> preAddShipmentLine;

  private boolean useOrderDocumentNoForRelatedDocs = false;
  private int paymentCount = 0;

  protected String getImportQualifier() {
    return "Order";
  }

  /**
   * Method to initialize the global variables needed during the synchronization process
   * 
   * @param jsonorder
   *          JSONObject which contains the order to be synchronized. This object is generated in
   *          Web POS
   */
  public void initializeVariables(JSONObject jsonorder) throws JSONException {
    try {
      useOrderDocumentNoForRelatedDocs = "Y".equals(Preferences.getPreferenceValue(
          "OBPOS_UseOrderDocumentNoForRelatedDocs", true, OBContext.getOBContext()
              .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
              .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error(
          "Error getting OBPOS_UseOrderDocumentNoForRelatedDocs preference: " + e1.getMessage(), e1);
    }

    isNegative = jsonorder.optBoolean("isNegative", false);

    boolean fullyPaid = isNegative ? jsonorder.getDouble("payment") <= Math.abs(jsonorder
        .getDouble("gross")) : jsonorder.getDouble("payment") >= Math.abs(jsonorder
        .getDouble("gross"));
    boolean isLayaway = jsonorder.optBoolean("isLayaway", false)
        || jsonorder.optLong("orderType") == 2;

    documentNoHandlers.set(new ArrayList<DocumentNoHandler>());

    isQuotation = jsonorder.optBoolean("isQuotation", false);

    paidReceipt = jsonorder.optBoolean("isPaid", false);

    donePressed = jsonorder.optBoolean("donePressed", false);
    hasPrepayment = donePressed && !fullyPaid;
    paidOnCredit = !paidReceipt && jsonorder.optBoolean("paidOnCredit", false);

    newLayaway = jsonorder.getLong("orderType") == 2;
    notpaidLayaway = isLayaway && !paidOnCredit && !donePressed && !fullyPaid;
    creditpaidLayaway = isLayaway && (paidOnCredit || (donePressed && !fullyPaid));
    partialpaidLayaway = jsonorder.optBoolean("isLayaway", false) && !fullyPaid;
    fullypaidLayaway = isLayaway && fullyPaid;

    isDeleted = jsonorder.optBoolean("obposIsDeleted", false);
    isModified = jsonorder.has("isModified") && jsonorder.getBoolean("isModified");

    createShipment = !isQuotation && !isDeleted && !paidReceipt
        && jsonorder.optBoolean("generateShipment", true);

    doCancelAndReplace = jsonorder.optBoolean("doCancelAndReplace", false);
    doCancelLayaway = jsonorder.optBoolean("cancelLayaway", false);

    deliver = jsonorder.optBoolean("deliver", true);
  }

  @Override
  public JSONObject saveRecord(JSONObject jsonorder) throws Exception {
    long t0 = 0, t1 = 0, t11 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0, t111 = 0, t112 = 0, t113 = 0, t115 = 0;

    JSONObject jsoncashup = null;
    if (jsonorder.has("cashUpReportInformation")) {
      // Update CashUp Report
      jsoncashup = jsonorder.getJSONObject("cashUpReportInformation");
      Date cashUpDate = new Date();

      UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);
    }

    orderLineServiceList = new HashMap<String, JSONArray>();
    try {

      initializeVariables(jsonorder);
      Order order = null;
      OrderLine orderLine = null;
      ShipmentInOut shipment = null;
      OBPOSApplications posTerminal = null;
      ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      JSONArray orderlines = jsonorder.getJSONArray("lines");

      if (jsonorder.getLong("orderType") != 2 && !jsonorder.getBoolean("isLayaway") && !isQuotation
          && validateOrder(jsonorder)
          && (!jsonorder.has("preserveId") || jsonorder.getBoolean("preserveId")) && !paidReceipt) {
        return successMessage(jsonorder);
      }

      if (jsonorder.getString("posTerminal") != null) {
        posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
            jsonorder.getString("posTerminal"));
      }

      order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));

      if (order != null) {
        final String loaded = jsonorder.has("loaded") ? jsonorder.getString("loaded") : null, updated = OBMOBCUtils
            .convertToUTCDateComingFromServer(order.getUpdated());
        if (loaded == null || loaded.compareTo(updated) != 0) {
          throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
              "OBPOS_outdatedLayaway", OBContext.getOBContext().getLanguage().getLanguage()));
        }
      }

      if (!isQuotation && !jsonorder.getBoolean("isLayaway")) {
        verifyCashupStatus(jsonorder);
      }

      if (!isModified) {
        executeOrderLoaderPreProcessHook(orderPreProcesses, jsonorder);
      } else {
        executeOrderLoaderModifiedPreProcessHook(orderModifiedPreProcesses, jsonorder);
      }

      // Set the 'deliver' property again because it can be changed during the previous hook
      deliver = jsonorder.optBoolean("deliver", true);

      if (jsonorder.has("deletedLines")) {
        mergeDeletedLines(jsonorder);
      }

      t0 = System.currentTimeMillis();
      TriggerHandler.getInstance().disable();
      try {
        if (jsonorder.has("oldId") && !jsonorder.getString("oldId").equals("null") && isQuotation) {
          try {
            deleteOldDocument(jsonorder);
          } catch (Exception e) {
            log.warn("Error to delete old quotation with id: " + jsonorder.getString("oldId"));
          }
        }

        if (log.isDebugEnabled()) {
          t1 = System.currentTimeMillis();
        }

        // Order header
        if (log.isDebugEnabled()) {
          t111 = System.currentTimeMillis();
        }

        if (paidReceipt) {
          order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
          order.setObposAppCashup(jsonorder.getString("obposAppCashup"));
          for (OrderLine line : order.getOrderLineList()) {
            lineReferences.add(line);
          }
          if (orderlines.length() > 0) {
            List<OrderLine> lstResultOL = getOrderLineList(order);
            for (int i = 0; i < lstResultOL.size(); i++) {
              JSONObject jsonOrderLine = orderlines.getJSONObject(i);
              OrderLine ol = lstResultOL.get(i);
              ol.setObposCanbedelivered(jsonOrderLine.optBoolean("obposCanbedelivered", false));
              ol.setObposIspaid(jsonOrderLine.optBoolean("obposIspaid", false));
            }
          }
        } else if (!newLayaway && notpaidLayaway) {
          order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
          order.setObposAppCashup(jsonorder.getString("obposAppCashup"));
          if (orderlines.length() > 0) {
            List<OrderLine> lstResultOL = getOrderLineList(order);
            for (int i = 0; i < lstResultOL.size(); i++) {
              OrderLine ol = lstResultOL.get(i);
              JSONObject jsonOrderLine = orderlines.getJSONObject(i);
              ol.setObposCanbedelivered(jsonOrderLine.optBoolean("obposCanbedelivered", false));
              lineReferences.add(ol);
            }
          }
        } else if (!newLayaway && (creditpaidLayaway || fullypaidLayaway)) {
          order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
          order.setObposAppCashup(jsonorder.getString("obposAppCashup"));
          order.setDelivered(deliver);
          if (jsonorder.has("oBPOSNotInvoiceOnCashUp")) {
            order.setOBPOSNotInvoiceOnCashUp(jsonorder.getBoolean("oBPOSNotInvoiceOnCashUp"));
          }
          if (orderlines.length() > 0) {
            List<OrderLine> lstResultOL = getOrderLineList(order);

            for (int i = 0; i < lstResultOL.size(); i++) {
              orderLine = lstResultOL.get(i);
              JSONObject jsonOrderLine = orderlines.getJSONObject(i);
              orderLine.setObposCanbedelivered(jsonOrderLine.optBoolean("obposCanbedelivered",
                  false));
              orderLine.setObposIspaid(jsonOrderLine.optBoolean("obposIspaid", false));
              BigDecimal qtyToDeliver = jsonOrderLine.has("obposQtytodeliver") ? BigDecimal
                  .valueOf(jsonOrderLine.getDouble("obposQtytodeliver")) : orderLine
                  .getOrderedQuantity();
              orderLine.setObposQtytodeliver(qtyToDeliver);
              orderLine.setDeliveredQuantity(qtyToDeliver);
              lineReferences.add(orderLine);
            }
          }
        } else if (partialpaidLayaway) {
          order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
          if (!jsonorder.has("channel")) {
            order.setObposAppCashup(jsonorder.getString("obposAppCashup"));
          }
          if (jsonorder.has("oBPOSNotInvoiceOnCashUp")) {
            order.setOBPOSNotInvoiceOnCashUp(jsonorder.getBoolean("oBPOSNotInvoiceOnCashUp"));
          }
        } else {
          verifyOrderLineTax(jsonorder);

          if (isModified) {
            order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
            if (order != null) {
              // If the order exists, delete all service lines relationships to regenerate them
              // with the information of the modified ticket
              deleteOrderlineServiceRelations(order);
            } else {
              order = OBProvider.getInstance().get(Order.class);
            }
          } else {
            order = OBProvider.getInstance().get(Order.class);
          }
          createOrder(order, jsonorder);
          OBDal.getInstance().save(order);
          lineReferences = new ArrayList<OrderLine>();
          createOrderLines(order, jsonorder, orderlines, lineReferences);

        }

        // 37240: done outside of createOrderLines, since needs to be done in all order loaders, not
        // only in new ones
        updateLinesWithAttributes(order, orderlines, lineReferences);

        if (log.isDebugEnabled()) {
          t112 = System.currentTimeMillis();
        }

        order.setObposIslayaway(notpaidLayaway);

        // Order lines
        if (jsonorder.has("oldId") && !jsonorder.getString("oldId").equals("null")
            && (!jsonorder.has("isQuotation") || !jsonorder.getBoolean("isQuotation"))) {
          try {
            // This order comes from a quotation, we need to associate both
            associateOrderToQuotation(jsonorder, order);
          } catch (Exception e) {
            log.warn("Error to associate order to quotation with id: "
                + jsonorder.getString("oldId"));
          }
        }

        if (log.isDebugEnabled()) {
          t113 = System.currentTimeMillis();
        }

        createShipment = createShipment && goodsToDeliver(order);
        final ShipmentInOut_Utils su = new ShipmentInOut_Utils();
        if (createShipment) {

          BigDecimal pendingQtyToDeliver = BigDecimal.ZERO;
          for (int i = 0; i < order.getOrderLineList().size(); i++) {
            OrderLine ol = order.getOrderLineList().get(i);
            if (!ol.isObposIsDeleted()) {
              JSONObject jsonOrderLine = orderlines.getJSONObject(i);
              BigDecimal pendingLineQty = jsonOrderLine.has("obposQtytodeliver") ? BigDecimal
                  .valueOf(jsonOrderLine.getDouble("obposQtytodeliver")) : ol.getOrderedQuantity();
              ol.setDeliveredQuantity(pendingLineQty);
              OBDal.getInstance().save(ol);
              if (pendingLineQty != null) {
                pendingLineQty = pendingLineQty.abs();
                if (orderlines.getJSONObject(i).has("deliveredQuantity")
                    && orderlines.getJSONObject(i).get("deliveredQuantity") != JSONObject.NULL) {
                  pendingLineQty = pendingLineQty.subtract(new BigDecimal(orderlines.getJSONObject(
                      i).getLong("deliveredQuantity")));
                }
                pendingQtyToDeliver = pendingQtyToDeliver.add(pendingLineQty);
              }
            }
          }

          if (pendingQtyToDeliver.compareTo(BigDecimal.ZERO) != 0) {
            OBCriteria<Locator> locators = OBDal.getInstance().createCriteria(Locator.class);
            locators.add(Restrictions.eq(Locator.PROPERTY_ACTIVE, true));
            locators.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, order.getWarehouse()));
            locators.addOrderBy(Locator.PROPERTY_RELATIVEPRIORITY, true);
            locators.setMaxResults(2);
            List<Locator> locatorList = locators.list();

            if (locatorList.isEmpty()) {
              throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
                  "OBPOS_WarehouseNotStorageBin", OBContext.getOBContext().getLanguage()
                      .getLanguage()));
            }

            shipment = OBProvider.getInstance().get(ShipmentInOut.class);
            su.createShipment(shipment, order, jsonorder, useOrderDocumentNoForRelatedDocs);
            OBDal.getInstance().save(shipment);
            su.createShipmentLines(shipment, order, jsonorder, orderlines, lineReferences,
                locatorList);
          } else {
            createShipment = false;
          }
        }
        if (log.isDebugEnabled()) {
          t115 = System.currentTimeMillis();
        }
        if (doCancelAndReplace) {
          final String canceledOrderId = jsonorder.getJSONObject("canceledorder").getString("id");
          final Order canceledOrder = OBDal.getInstance().get(Order.class, canceledOrderId);
          canceledOrder.setObposAppCashup(jsoncashup.getString("id"));
          if (canceledOrder.isObposIslayaway()) {
            canceledOrder.setObposIslayaway(false);
          }
          OBDal.getInstance().save(canceledOrder);
        }

        createApprovals(order, jsonorder);

        if (log.isDebugEnabled()) {
          t11 = System.currentTimeMillis();
          t2 = System.currentTimeMillis();
        }

        if (log.isDebugEnabled()) {
          t3 = System.currentTimeMillis();
        }

        if (createShipment) {
          // Stock manipulation
          org.openbravo.database.ConnectionProvider cp = new DalConnectionProvider(false);
          CallableStatement updateStockStatement = cp.getConnection().prepareCall(
              "{call M_UPDATE_INVENTORY (?,?,?,?,?,?,?,?,?,?,?,?,?)}");
          try {
            // Stock manipulation
            su.handleStock(shipment, updateStockStatement);
          } finally {
            updateStockStatement.close();
          }
        }

        if (log.isDebugEnabled()) {
          t4 = System.currentTimeMillis();

          log.debug("Creation of bobs. Order: " + (t112 - t111) + "; Orderlines: " + (t113 - t112)
              + "; Shipment: " + (t115 - t113) + "; Approvals" + (t11 - t115) + "; stock"
              + (t4 - t3));
        }

        if (!paidReceipt) {
          // do the docnumbers at the end
          OBContext.setAdminMode(false);
          try {
            for (DocumentNoHandler documentNoHandler : documentNoHandlers.get()) {
              documentNoHandler.setDocumentNoAndSave();
            }
            OBDal.getInstance().flush();
          } finally {
            // set to null, should not be used anymore after this.
            documentNoHandlers.set(null);
            OBContext.restorePreviousMode();
          }
        }

      } catch (Exception ex) {
        throw new OBException("Error in OrderLoader: " + ex.getMessage(), ex);
      } finally {
        // flush and enable triggers, the rest of this method needs enabled
        // triggers
        try {
          // enable triggers contains a flush in getConnection method
          TriggerHandler.getInstance().enable();
        } catch (Throwable ignored) {
        }
      }
      if (useOrderDocumentNoForRelatedDocs) {
        paymentCount = countPayments(order);
      }
      if (log.isDebugEnabled()) {
        t5 = System.currentTimeMillis();
      }
      if (!isQuotation && !isDeleted) {
        // Payment
        JSONObject paymentResponse = handlePayments(jsonorder, order);
        if (paymentResponse.getInt(JsonConstants.RESPONSE_STATUS) == JsonConstants.RPCREQUEST_STATUS_FAILURE) {
          return paymentResponse;
        }

        if (doCancelAndReplace && order.getReplacedorder() != null) {
          TriggerHandler.getInstance().disable();
          try {
            // Set default payment type to order in case there is no payment on the order
            POSUtils.setDefaultPaymentType(jsonorder, order);
            // Cancel and Replace the order
            CancelAndReplaceUtils.cancelAndReplaceOrder(order.getId(), jsonorder,
                useOrderDocumentNoForRelatedDocs);
          } catch (Exception ex) {
            OBDal.getInstance().rollbackAndClose();
            throw new OBException("CancelAndReplaceUtils.cancelAndReplaceOrder: ", ex);
          } finally {
            TriggerHandler.getInstance().enable();
          }
        }

        for (OrderLoaderHook hook : orderProcesses) {
          if (hook instanceof OrderLoaderPaymentHook) {
            ((OrderLoaderPaymentHook) hook).setPaymentSchedule(paymentResponse
                .has("paymentSchedule") ? (FIN_PaymentSchedule) paymentResponse
                .get("paymentSchedule") : null);
            ((OrderLoaderPaymentHook) hook).setPaymentScheduleInvoice(paymentResponse
                .has("paymentScheduleInvoice") ? (FIN_PaymentSchedule) paymentResponse
                .get("paymentScheduleInvoice") : null);
          }
        }

        // Call all OrderProcess injected.
        if (!isModified) {
          executeHooks(orderProcesses, jsonorder, order, shipment, null);
        } else {
          executeModifiedHooks(orderModifiedProcesses, jsonorder, order, shipment, null);
        }
      } else {
        // Call all OrderProcess injected when order is a quotation
        executeHooks(quotationProcesses, jsonorder, order, shipment, null);
      }

      if (log.isDebugEnabled()) {
        t6 = System.currentTimeMillis();
      }

      // Save the last order synchronized in obposApplication object
      if (posTerminal != null) {
        posTerminal.setTerminalLastordersinchronized(order.getUpdated());
      }

      OBDal.getInstance().flush();

      if (log.isDebugEnabled()) {
        log.debug("Order with docno: " + order.getDocumentNo() + " (uuid: " + order.getId()
            + ") saved correctly. Initial flush: " + (t1 - t0) + "; Generate bobs:" + (t11 - t1)
            + "; Save bobs:" + (t2 - t11) + "; First flush:" + (t3 - t2) + "; Process Payments:"
            + (t6 - t5) + " Final flush: " + (System.currentTimeMillis() - t6));
      }

      ImportEntryManager.getInstance()
          .reportStats("orderLoader", (System.currentTimeMillis() - t0));

      return successMessage(jsonorder);
    } finally {
      documentNoHandlers.set(null);
    }
  }

  private void updateLinesWithAttributes(Order order, JSONArray orderlines,
      ArrayList<OrderLine> lineReferences) throws JSONException {
    for (int i = 0; i < orderlines.length(); i++) {
      OrderLine orderline = order.getOrderLineList().get(i);

      if (orderline.getProduct().getAttributeSet() == null) {
        continue;
      }
      JSONObject jsonOrderLine = orderlines.getJSONObject(i);
      String attr = null;
      if (jsonOrderLine.has("attSetInstanceDesc")) {
        attr = jsonOrderLine.get("attSetInstanceDesc").toString();
      } else if (jsonOrderLine.has("attributeValue")) {
        attr = jsonOrderLine.get("attributeValue").toString();
      }
      if (attr.equals("null")) {
        attr = null;
      }
      orderline.setAttributeSetValue(AttributesUtils.fetchAttributeSetValue(attr, jsonOrderLine
          .getJSONObject("product").get("id").toString(), order.getOrganization().getId()));

    }
  }

  private List<OrderLine> getOrderLineList(Order order) {
    String olsHqlWhereClause = " ol where ol.salesOrder.id = :orderId and ol.obposIsDeleted = false order by lineNo";
    OBQuery<OrderLine> queryOls = OBDal.getInstance().createQuery(OrderLine.class,
        olsHqlWhereClause);
    queryOls.setNamedParameter("orderId", order.getId());
    List<OrderLine> lstResultOL = queryOls.list();
    return lstResultOL;
  }

  private void mergeDeletedLines(JSONObject jsonorder) {
    try {
      JSONArray deletedLines = jsonorder.getJSONArray("deletedLines");
      JSONArray lines = jsonorder.getJSONArray("lines");
      for (int i = 0; i < deletedLines.length(); i++) {
        lines.put(deletedLines.get(i));
      }
      jsonorder.put("lines", lines);
    } catch (JSONException e) {
      log.error("JSON information couldn't be read when merging deleted lines", e);
      return;
    }
  }

  private void executeHooks(Instance<? extends Object> hooks, JSONObject jsonorder, Order order,
      ShipmentInOut shipment, Invoice invoice) throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof OrderLoaderHook) {
        ((OrderLoaderHook) proc).exec(jsonorder, order, shipment, invoice);
      } else if (proc instanceof OrderLoaderHookForQuotations) {
        ((OrderLoaderHookForQuotations) proc).exec(jsonorder, order, shipment, invoice);
      }
    }
  }

  private void executeModifiedHooks(Instance<? extends Object> hooks, JSONObject jsonorder,
      Order order, ShipmentInOut shipment, Invoice invoice) throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof OrderLoaderModifiedHook) {
        ((OrderLoaderModifiedHook) proc).exec(jsonorder, order, shipment, invoice);
      }
    }
  }

  protected List<Object> sortHooksByPriority(Instance<? extends Object> hooks) throws Exception {

    List<Object> hookList = new ArrayList<Object>();
    for (Object hookToAdd : hooks) {
      hookList.add(hookToAdd);
    }

    Collections.sort(hookList, new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        int o1Priority = (o1 instanceof PreOrderLoaderPrioritizedHook) ? ((PreOrderLoaderPrioritizedHook) o1)
            .getPriority() : 100;
        int o2Priority = (o2 instanceof PreOrderLoaderPrioritizedHook) ? ((PreOrderLoaderPrioritizedHook) o2)
            .getPriority() : 100;

        return (int) Math.signum(o2Priority - o1Priority);
      }
    });

    return hookList;
  }

  protected void executeOrderLoaderPreProcessHook(Instance<? extends Object> hooks,
      JSONObject jsonorder) throws Exception {

    List<Object> hookList = sortHooksByPriority(hooks);

    for (Object proc : hookList) {
      if (proc instanceof OrderLoaderPreProcessHook) {
        ((OrderLoaderPreProcessHook) proc).exec(jsonorder);
      }
    }
  }

  protected void executeOrderLoaderModifiedPreProcessHook(Instance<? extends Object> hooks,
      JSONObject jsonorder) throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof OrderLoaderModifiedPreProcessHook) {
        ((OrderLoaderModifiedPreProcessHook) proc).exec(jsonorder);
      }
    }
  }

  protected void executeOrderLoaderPreProcessPaymentHook(Instance<? extends Object> hooks,
      JSONObject jsonorder, Order order, JSONObject jsonpayment, FIN_Payment payment)
      throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof OrderLoaderPreProcessPaymentHook) {
        ((OrderLoaderPreProcessPaymentHook) proc).exec(jsonorder, order, jsonpayment, payment);
      }
    }
  }

  private void associateOrderToQuotation(JSONObject jsonorder, Order order) throws JSONException {
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

  private void deleteOldDocument(JSONObject jsonorder) throws JSONException {
    /*
     * Issue 0029953 Instead of remove old order, we set it as rejected (CJ). The new quotation will
     * be linked to the rejected one
     */
    Order oldOrder = OBDal.getInstance().get(Order.class, jsonorder.getString("oldId"));
    oldOrder.setDocumentStatus("CJ");
    // Order Loader will automatically store this field into c_order table based on Json
    jsonorder.put("obposRejectedQuotation", jsonorder.getString("oldId"));
  }

  private boolean validateOrder(JSONObject jsonorder) throws Exception {
    OBContext.setAdminMode(false);
    try {
      if ((!jsonorder.has("obposIsDeleted") || !jsonorder.getBoolean("obposIsDeleted"))
          && (!jsonorder.has("gross") || jsonorder.getString("gross").equals("0"))
          && (jsonorder.isNull("lines") || (jsonorder.getJSONArray("lines") != null && jsonorder
              .getJSONArray("lines").length() == 0))) {
        log.error("Detected order without lines and total amount zero. Document number "
            + jsonorder.getString("documentNo"));
        return true;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return false;
  }

  private DocumentType getPaymentDocumentType(Organization org) {
    if (paymentDocTypes.get(org.getId()) != null) {
      return paymentDocTypes.get(org.getId());
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARReceipt);
    paymentDocTypes.put(org.getId(), docType);
    return docType;

  }

  protected void createOrderLines(Order order, JSONObject jsonorder, JSONArray orderlines,
      ArrayList<OrderLine> lineReferences) throws JSONException {
    Entity orderLineEntity = ModelProvider.getInstance().getEntity(OrderLine.class);
    Entity promotionLineEntity = ModelProvider.getInstance().getEntity(OrderLineOffer.class);
    int pricePrecision = order.getCurrency().getObposPosprecision() == null ? order.getCurrency()
        .getPricePrecision().intValue() : order.getCurrency().getObposPosprecision().intValue();

    order.getOrderLineList().clear();
    for (int i = 0; i < orderlines.length(); i++) {

      JSONObject jsonOrderLine = orderlines.getJSONObject(i);
      OrderLine orderline = null;
      if (isModified) {
        orderline = OBDal.getInstance().get(OrderLine.class, jsonOrderLine.getString("id"));
      }
      if (orderline == null) {
        orderline = OBProvider.getInstance().get(OrderLine.class);
        if (jsonOrderLine.has("id")) {
          orderline.setId(jsonOrderLine.getString("id"));
          orderline.setNewOBObject(true);
        }
      }
      if (jsonOrderLine.has("description")
          && StringUtils.length(jsonOrderLine.getString("description")) > 255) {
        jsonOrderLine.put("description",
            StringUtils.substring(jsonOrderLine.getString("description"), 0, 255));
      }

      JSONPropertyToEntity.fillBobFromJSON(ModelProvider.getInstance().getEntity(OrderLine.class),
          orderline, jsonorder, jsonorder.getLong("timezoneOffset"));
      JSONPropertyToEntity.fillBobFromJSON(orderLineEntity, orderline, jsonOrderLine,
          jsonorder.getLong("timezoneOffset"));

      orderline.setActive(true);
      orderline.setSalesOrder(order);
      orderline.setLineNetAmount(BigDecimal.valueOf(jsonOrderLine.getDouble("net")).setScale(
          pricePrecision, RoundingMode.HALF_UP));

      if ((createShipment && orderline.isObposIspaid())
          || (doCancelAndReplace && !newLayaway && !notpaidLayaway && !partialpaidLayaway)) {
        // shipment is created or is a C&R and is not a layaway, so all is delivered
        orderline
            .setDeliveredQuantity(jsonOrderLine.has("obposQtytodeliver") ? BigDecimal
                .valueOf(jsonOrderLine.getDouble("obposQtytodeliver")) : orderline
                .getOrderedQuantity());
      }

      if (jsonOrderLine.has("obposIsDeleted") && jsonOrderLine.getBoolean("obposIsDeleted")) {
        orderline.setObposQtytodeliver(BigDecimal.ZERO);
      } else {
        orderline
            .setObposQtytodeliver(jsonOrderLine.has("obposQtytodeliver") ? BigDecimal
                .valueOf(jsonOrderLine.getDouble("obposQtytodeliver")) : orderline
                .getOrderedQuantity());
      }

      lineReferences.add(orderline);
      orderline.setLineNo((long) ((i + 1) * 10));
      order.getOrderLineList().add(orderline);
      OBDal.getInstance().save(orderline);

      if (jsonOrderLine.has("relatedLines")) {
        if (jsonOrderLine.has("product")
            && jsonOrderLine.getJSONObject("product").has("productType")
            && "S".equals(jsonOrderLine.getJSONObject("product").get("productType"))) {
          orderLineServiceList.put(orderline.getId(), jsonOrderLine.getJSONArray("relatedLines"));
        }
      }

      if (!orderline.isNewOBObject()) {
        // updating order - delete old taxes to create again
        String deleteStr = "delete " + OrderLineTax.ENTITY_NAME //
            + " where " + OrderLineTax.PROPERTY_SALESORDERLINE + ".id = :id";
        @SuppressWarnings("rawtypes")
        Query deleteQuery = OBDal.getInstance().getSession().createQuery(deleteStr);
        deleteQuery.setParameter("id", orderline.getId());
        deleteQuery.executeUpdate();
      }
      JSONObject taxes = jsonOrderLine.getJSONObject("taxLines");
      @SuppressWarnings("unchecked")
      Iterator<String> itKeys = taxes.keys();
      int ind = 0;
      while (itKeys.hasNext()) {
        String taxId = itKeys.next();
        JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
        OrderLineTax orderlinetax = OBProvider.getInstance().get(OrderLineTax.class);
        TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
            ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
        orderlinetax.setTax(tax);
        orderlinetax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
            pricePrecision, RoundingMode.HALF_UP));
        orderlinetax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
            pricePrecision, RoundingMode.HALF_UP));
        orderlinetax.setSalesOrder(order);
        orderlinetax.setSalesOrderLine(orderline);
        orderlinetax.setLineNo((long) ((ind + 1) * 10));
        ind++;
        orderline.getOrderLineTaxList().add(orderlinetax);
        order.getOrderLineTaxList().add(orderlinetax);
        orderlinetax.setId(OBMOBCUtils.getUUIDbyString(orderlinetax.getSalesOrderLine().getId()
            + orderlinetax.getLineNo()));
        orderlinetax.setNewOBObject(true);
        OBDal.getInstance().save(orderlinetax);
      }

      // Discounts & Promotions
      if (jsonOrderLine.has("promotions") && !jsonOrderLine.isNull("promotions")
          && !jsonOrderLine.getString("promotions").equals("null")) {

        if (!orderline.isNewOBObject()) {
          // updating order - delete old promotions to create again
          String deleteStr = "delete " + OrderLineOffer.ENTITY_NAME //
              + " where " + OrderLineOffer.PROPERTY_SALESORDERLINE + ".id = :id";
          @SuppressWarnings("rawtypes")
          Query deleteQuery = OBDal.getInstance().getSession().createQuery(deleteStr);
          deleteQuery.setParameter("id", orderline.getId());
          deleteQuery.executeUpdate();
        }

        JSONArray jsonPromotions = jsonOrderLine.getJSONArray("promotions");
        for (int p = 0; p < jsonPromotions.length(); p++) {
          JSONObject jsonPromotion = jsonPromotions.getJSONObject(p);
          boolean hasActualAmt = jsonPromotion.has("actualAmt");
          if ((hasActualAmt && jsonPromotion.getDouble("actualAmt") == 0)
              || (!hasActualAmt && jsonPromotion.getDouble("amt") == 0)) {
            continue;
          }

          OrderLineOffer promotion = OBProvider.getInstance().get(OrderLineOffer.class);
          JSONPropertyToEntity.fillBobFromJSON(promotionLineEntity, promotion, jsonPromotion,
              jsonorder.getLong("timezoneOffset"));

          if (hasActualAmt) {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("actualAmt"))
                .setScale(pricePrecision, RoundingMode.HALF_UP));
          } else {
            promotion.setTotalAmount(BigDecimal.valueOf(jsonPromotion.getDouble("amt")).setScale(
                pricePrecision, RoundingMode.HALF_UP));
          }
          promotion.setLineNo((long) ((p + 1) * 10));
          promotion.setSalesOrderLine(orderline);
          if (jsonPromotion.has("identifier") && !jsonPromotion.isNull("identifier")) {
            String identifier = jsonPromotion.getString("identifier");
            if (identifier.length() > 100) {
              identifier = identifier.substring(identifier.length() - 100);
            }
            promotion.setObdiscIdentifier(identifier);
          }
          if (jsonPromotion.has("discountinstance") && !jsonPromotion.isNull("discountinstance")) {
            String discountinstance = jsonPromotion.getString("discountinstance");
            promotion.setObposDiscountinstance(discountinstance);
          }
          promotion.setId(OBMOBCUtils.getUUIDbyString(orderline.getId() + p));
          promotion.setNewOBObject(true);
          orderline.getOrderLineOfferList().add(promotion);
        }
      }
    }
    if (orderLineServiceList.size() > 0) {
      createLinesForServiceProduct(lineReferences);
    }
  }

  protected void deleteOrderlineServiceRelations(Order order) {
    String deleteStr = "delete " + OrderlineServiceRelation.ENTITY_NAME //
        + " where " + OrderlineServiceRelation.PROPERTY_ID + " in (" //
        + " select rel." + OrderlineServiceRelation.PROPERTY_ID + " from " //
        + OrderlineServiceRelation.ENTITY_NAME + " as rel join rel." //
        + OrderlineServiceRelation.PROPERTY_SALESORDERLINE + " as ol join ol." //
        + OrderLine.PROPERTY_SALESORDER + " as order where order." //
        + Order.PROPERTY_ID + " = :id)";
    @SuppressWarnings("rawtypes")
    Query deleteQuery = OBDal.getInstance().getSession().createQuery(deleteStr);
    deleteQuery.setParameter("id", order.getId());
    deleteQuery.executeUpdate();
  }

  private void createLinesForServiceProduct(ArrayList<OrderLine> lineReferences)
      throws JSONException {
    Iterator<Entry<String, JSONArray>> orderLineIterator = orderLineServiceList.entrySet()
        .iterator();

    while (orderLineIterator.hasNext()) {
      Entry<String, JSONArray> olservice = orderLineIterator.next();
      for (OrderLine orderLine : lineReferences) {
        if (orderLine.getId().equals(olservice.getKey())) {
          JSONArray relatedLines = olservice.getValue();
          for (int i = 0; i < relatedLines.length(); i++) {
            OrderlineServiceRelation olServiceRelation = OBProvider.getInstance().get(
                OrderlineServiceRelation.class);
            JSONObject relatedJsonOrderLine = relatedLines.getJSONObject(i);
            OrderLine rol = OBDal.getInstance().get(OrderLine.class,
                relatedJsonOrderLine.get("orderlineId"));
            if (rol != null) {
              olServiceRelation.setActive(true);
              olServiceRelation.setOrganization(orderLine.getOrganization());
              olServiceRelation.setCreatedBy(orderLine.getCreatedBy());
              olServiceRelation.setCreationDate(orderLine.getCreationDate());
              if ("UQ".equals(orderLine.getProduct().getQuantityRule())) {
                if (orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                  olServiceRelation.setQuantity(BigDecimal.ONE);
                } else {
                  olServiceRelation.setQuantity(new BigDecimal(-1));
                }
              } else {
                if (rol.getOrderedQuantity().signum() != orderLine.getOrderedQuantity().signum()) {
                  olServiceRelation.setQuantity(rol.getOrderedQuantity().negate());
                } else {
                  olServiceRelation.setQuantity(rol.getOrderedQuantity());
                }
              }
              olServiceRelation.setAmount(rol.getBaseGrossUnitPrice().multiply(
                  olServiceRelation.getQuantity()));
              olServiceRelation.setUpdated(orderLine.getUpdated());
              olServiceRelation.setUpdatedBy(orderLine.getUpdatedBy());
              olServiceRelation.setSalesOrderLine(orderLine);
              olServiceRelation.setOrderlineRelated(rol);
              olServiceRelation.setId(OBMOBCUtils.getUUIDbyString(orderLine.getId() + i));
              olServiceRelation.setNewOBObject(true);
              OBDal.getInstance().save(olServiceRelation);
            }
          }
        }
      }
    }
  }

  protected void createOrder(Order order, JSONObject jsonorder) throws JSONException {
    Entity orderEntity = ModelProvider.getInstance().getEntity(Order.class);
    if (jsonorder.has("description")
        && StringUtils.length(jsonorder.getString("description")) > 255) {
      jsonorder.put("description",
          StringUtils.substring(jsonorder.getString("description"), 0, 255));
    }
    JSONPropertyToEntity.fillBobFromJSON(orderEntity, order, jsonorder,
        jsonorder.getLong("timezoneOffset"));

    if (jsonorder.has("id") && order.getId() == null) {
      order.setId(jsonorder.getString("id"));
      order.setNewOBObject(true);

      Long value = jsonorder.getLong("created");
      order.set("creationDate", new Date(value));
    }

    int pricePrecision = order.getCurrency().getObposPosprecision() == null ? order.getCurrency()
        .getPricePrecision().intValue() : order.getCurrency().getObposPosprecision().intValue();
    BusinessPartner bp = order.getBusinessPartner();
    order.setTransactionDocument((DocumentType) OBDal.getInstance().getProxy("DocumentType",
        jsonorder.getString("documentType")));
    order.setAccountingDate(order.getOrderDate());
    order.setScheduledDeliveryDate(order.getOrderDate());
    order.setPartnerAddress(OBDal.getInstance().getProxy(Location.class,
        jsonorder.getJSONObject("bp").getString("shipLocId")));
    order.setInvoiceAddress(OBDal.getInstance().getProxy(Location.class,
        jsonorder.getJSONObject("bp").getString("locId")));

    Boolean paymenthMethod = false;
    if (!jsonorder.isNull("paymentMethodKind")
        && !jsonorder.getString("paymentMethodKind").equals("null")) {
      String posTerminalId = jsonorder.getString("posTerminal");
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          posTerminalId);
      if (posTerminal != null) {
        String paymentTypeName = jsonorder.getString("paymentMethodKind");
        OBPOSAppPayment paymentType = null;
        for (OBPOSAppPayment type : posTerminal.getOBPOSAppPaymentList()) {
          if (type.getSearchKey().equals(paymentTypeName)) {
            paymentType = type;
          }
        }
        if (paymentType != null) {
          order.setPaymentMethod(paymentType.getPaymentMethod().getPaymentMethod());
          paymenthMethod = true;
        }
      }
    }

    if (!paymenthMethod) {
      if (!jsonorder.getJSONObject("bp").isNull("paymentMethod")
          && !jsonorder.getJSONObject("bp").getString("paymentMethod").equals("null")) {
        order.setPaymentMethod((FIN_PaymentMethod) OBDal.getInstance().getProxy(
            "FIN_PaymentMethod", jsonorder.getJSONObject("bp").getString("paymentMethod")));
      } else if (bp.getPaymentMethod() != null) {
        order.setPaymentMethod((FIN_PaymentMethod) bp.getPaymentMethod());
      } else if (order.getOrganization().getObretcoDbpPmethodid() != null) {
        order
            .setPaymentMethod((FIN_PaymentMethod) order.getOrganization().getObretcoDbpPmethodid());
      } else {
        String paymentMethodHqlWhereClause = " pmethod where EXISTS (SELECT 1 FROM FinancialMgmtFinAccPaymentMethod fapm "
            + "WHERE pmethod.id = fapm.paymentMethod.id AND fapm.payinAllow = 'Y')";
        OBQuery<FIN_PaymentMethod> queryPaymentMethod = OBDal.getInstance().createQuery(
            FIN_PaymentMethod.class, paymentMethodHqlWhereClause);
        queryPaymentMethod.setFilterOnReadableOrganization(true);
        queryPaymentMethod.setMaxResult(1);
        List<FIN_PaymentMethod> lstPaymentMethod = queryPaymentMethod.list();
        if (lstPaymentMethod != null && lstPaymentMethod.size() > 0) {
          order.setPaymentMethod(lstPaymentMethod.get(0));
        }
      }
    }
    if (!jsonorder.getJSONObject("bp").isNull("paymentTerms")
        && !jsonorder.getJSONObject("bp").getString("paymentTerms").equals("null")) {
      order.setPaymentTerms((PaymentTerm) OBDal.getInstance().getProxy("FinancialMgmtPaymentTerm",
          jsonorder.getJSONObject("bp").getString("paymentTerms")));
    } else if (bp.getPaymentTerms() != null) {
      order.setPaymentTerms((PaymentTerm) bp.getPaymentTerms());
    } else if (order.getOrganization().getObretcoDbpPmethodid() != null) {
      order.setPaymentTerms((PaymentTerm) order.getOrganization().getObretcoDbpPtermid());
    } else {
      OBCriteria<PaymentTerm> paymentTerms = OBDal.getInstance().createCriteria(PaymentTerm.class);
      paymentTerms.add(Restrictions.eq(Locator.PROPERTY_ACTIVE, true));
      paymentTerms.addOrderBy(PaymentTerm.PROPERTY_NAME, true);
      paymentTerms.setMaxResults(1);
      List<PaymentTerm> lstPaymentTerm = paymentTerms.list();
      if (lstPaymentTerm != null && lstPaymentTerm.size() > 0) {
        order.setPaymentTerms(lstPaymentTerm.get(0));
      }
    }

    if (!jsonorder.getJSONObject("bp").isNull("invoiceTerms")
        && !jsonorder.getJSONObject("bp").getString("invoiceTerms").equals("null")) {
      order.setInvoiceTerms(jsonorder.getJSONObject("bp").getString("invoiceTerms"));
    } else if (bp.getInvoiceTerms() != null) {
      order.setInvoiceTerms(bp.getInvoiceTerms());
    } else if (order.getOrganization().getObretcoDbpIrulesid() != null) {
      order.setInvoiceTerms(order.getOrganization().getObretcoDbpIrulesid());
    } else {
      OBCriteria<org.openbravo.model.ad.domain.List> invoiceRules = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.domain.List.class);
      invoiceRules.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE
          + ".id", "150"));
      invoiceRules.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_ACTIVE, true));
      invoiceRules.addOrderBy(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, true);
      invoiceRules.setMaxResults(1);
      List<org.openbravo.model.ad.domain.List> lstInvoiceRule = invoiceRules.list();
      if (lstInvoiceRule != null && lstInvoiceRule.size() > 0) {
        order.setInvoiceTerms(lstInvoiceRule.get(0).getSearchKey());
      }
    }

    order.setGrandTotalAmount(BigDecimal.valueOf(jsonorder.getDouble("gross")).setScale(
        pricePrecision, RoundingMode.HALF_UP));
    order.setSummedLineAmount(BigDecimal.valueOf(jsonorder.getDouble("net")).setScale(
        pricePrecision, RoundingMode.HALF_UP));

    order.setSalesTransaction(true);
    if (jsonorder.has("obposIsDeleted") && jsonorder.has("isQuotation")
        && jsonorder.getBoolean("obposIsDeleted")) {
      order.setDocumentStatus("CL");
    } else if (isQuotation) {
      order.setDocumentStatus("UE");
    } else {
      order.setDocumentStatus("CO");
    }

    order.setDocumentAction("--");
    order.setProcessed(true);
    order.setProcessNow(false);
    order.setObposSendemail((jsonorder.has("sendEmail") && jsonorder.getBoolean("sendEmail")));
    if (!newLayaway && !isQuotation && !isDeleted) {
      order.setDelivered(deliver);
    }

    if (!doCancelAndReplace && !doCancelLayaway) {
      if (order.getDocumentNo().indexOf("/") > -1) {
        long documentno = Long.parseLong(order.getDocumentNo().substring(
            order.getDocumentNo().lastIndexOf("/") + 1));

        if (isQuotation) {
          if (order.getObposApplications().getQuotationslastassignednum() == null
              || documentno > order.getObposApplications().getQuotationslastassignednum()) {
            OBPOSApplications terminal = order.getObposApplications();
            terminal.setQuotationslastassignednum(documentno);
            OBDal.getInstance().save(terminal);
          }
        } else if (jsonorder.optLong("returnnoSuffix", -1L) > -1L) {
          if (order.getObposApplications().getReturnslastassignednum() == null
              || documentno > order.getObposApplications().getReturnslastassignednum()) {
            OBPOSApplications terminal = order.getObposApplications();
            terminal.setReturnslastassignednum(documentno);
            OBDal.getInstance().save(terminal);
          }
        } else {
          if (order.getObposApplications().getLastassignednum() == null
              || documentno > order.getObposApplications().getLastassignednum()) {
            OBPOSApplications terminal = order.getObposApplications();
            terminal.setLastassignednum(documentno);
            OBDal.getInstance().save(terminal);
          }
        }
      } else {
        long documentno;
        if (isQuotation) {
          if (jsonorder.has("quotationnoPrefix")) {
            documentno = Long.parseLong(order.getDocumentNo().replace(
                jsonorder.getString("quotationnoPrefix"), ""));

            if (order.getObposApplications().getQuotationslastassignednum() == null
                || documentno > order.getObposApplications().getQuotationslastassignednum()) {
              OBPOSApplications terminal = order.getObposApplications();
              terminal.setQuotationslastassignednum(documentno);
              OBDal.getInstance().save(terminal);
            }
          }
        } else if (jsonorder.optLong("returnnoSuffix", -1L) > -1L) {
          if (jsonorder.has("returnnoPrefix")) {
            documentno = Long.parseLong(order.getDocumentNo().replace(
                jsonorder.getString("returnnoPrefix"), ""));

            if (order.getObposApplications().getReturnslastassignednum() == null
                || documentno > order.getObposApplications().getReturnslastassignednum()) {
              OBPOSApplications terminal = order.getObposApplications();
              terminal.setReturnslastassignednum(documentno);
              OBDal.getInstance().save(terminal);
            }
          }
        } else {
          if (jsonorder.has("documentnoPrefix")) {
            documentno = Long.parseLong(order.getDocumentNo().replace(
                jsonorder.getString("documentnoPrefix"), ""));

            if (order.getObposApplications().getLastassignednum() == null
                || documentno > order.getObposApplications().getLastassignednum()) {
              OBPOSApplications terminal = order.getObposApplications();
              terminal.setLastassignednum(documentno);
              OBDal.getInstance().save(terminal);
            }
          }
        }
      }
    }

    String userHqlWhereClause = " usr where usr.businessPartner = :bp and usr.organization.id in (:orgs) order by username";
    OBQuery<User> queryUser = OBDal.getInstance().createQuery(User.class, userHqlWhereClause);
    queryUser.setNamedParameter("bp", bp);
    queryUser.setNamedParameter("orgs", OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(order.getOrganization().getId()));
    // already filtered
    queryUser.setFilterOnReadableOrganization(false);
    queryUser.setMaxResult(1);
    List<User> lstResultUsers = queryUser.list();
    if (lstResultUsers != null && lstResultUsers.size() > 0) {
      order.setUserContact(lstResultUsers.get(0));
    }

    if (!order.isNewOBObject()) {
      // updating order - delete old taxes to create again
      String deleteStr = "delete " + OrderTax.ENTITY_NAME //
          + " where " + OrderTax.PROPERTY_SALESORDER + ".id = :id";
      @SuppressWarnings("rawtypes")
      Query deleteQuery = OBDal.getInstance().getSession().createQuery(deleteStr);
      deleteQuery.setParameter("id", order.getId());
      deleteQuery.executeUpdate();
    }
    JSONObject taxes = jsonorder.getJSONObject("taxes");
    @SuppressWarnings("unchecked")
    Iterator<String> itKeys = taxes.keys();
    int i = 0;
    while (itKeys.hasNext()) {
      String taxId = itKeys.next();
      JSONObject jsonOrderTax = taxes.getJSONObject(taxId);
      OrderTax orderTax = OBProvider.getInstance().get(OrderTax.class);
      TaxRate tax = (TaxRate) OBDal.getInstance().getProxy(
          ModelProvider.getInstance().getEntity(TaxRate.class).getName(), taxId);
      orderTax.setTax(tax);
      orderTax.setTaxableAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("net")).setScale(
          pricePrecision, RoundingMode.HALF_UP));
      orderTax.setTaxAmount(BigDecimal.valueOf(jsonOrderTax.getDouble("amount")).setScale(
          pricePrecision, RoundingMode.HALF_UP));
      orderTax.setSalesOrder(order);
      orderTax.setLineNo((long) ((i + 1) * 10));
      orderTax.setId(OBMOBCUtils.getUUIDbyString(orderTax.getSalesOrder().getId()
          + orderTax.getLineNo()));
      orderTax.setNewOBObject(true);
      i++;
      order.getOrderTaxList().add(orderTax);
    }
  }

  public static BigDecimal convertCurrencyOrder(Order order, BigDecimal amt) {
    int pricePrecision = order.getCurrency().getObposPosprecision() == null ? order.getCurrency()
        .getPricePrecision().intValue() : order.getCurrency().getObposPosprecision().intValue();
    List<Object> parameters = new ArrayList<Object>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(amt.setScale(pricePrecision, RoundingMode.HALF_UP));
    types.add(BigDecimal.class);
    parameters.add(order.getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(order.getBusinessPartner().getPriceList().getCurrency());
    types.add(BaseOBObject.class);
    parameters.add(order.getOrderDate());
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

  @Deprecated
  protected JSONObject handlePayments(JSONObject jsonorder, Order order, Invoice invoice,
      boolean wasPaidOnCredit, boolean createInvoice) throws Exception {
    return handlePayments(jsonorder, order);
  }

  protected JSONObject handlePayments(JSONObject jsonorder, Order order) throws Exception {
    final JSONObject jsonResponse = new JSONObject();
    String posTerminalId = jsonorder.getString("posTerminal");
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class, posTerminalId);
    if (posTerminal == null) {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      jsonResponse.put(JsonConstants.RESPONSE_ERRORMESSAGE, "The POS terminal with id "
          + posTerminalId + " couldn't be found");
      return jsonResponse;
    }

    JSONArray payments = jsonorder.getJSONArray("payments");

    // Create a unique payment schedule for all payments
    BigDecimal paymentAmt = BigDecimal.valueOf(jsonorder.optDouble("nettingPayment", 0));
    for (int i = 0; i < payments.length(); i++) {
      paymentAmt = paymentAmt
          .add(BigDecimal.valueOf(payments.getJSONObject(i).getDouble("amount")));
    }
    final BigDecimal gross = BigDecimal.valueOf(jsonorder.getDouble("gross"));

    if (payments.length() == 0 && gross.compareTo(BigDecimal.ZERO) == 0) {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      return jsonResponse;
    }

    FIN_PaymentSchedule paymentSchedule;
    int pricePrecision = order.getCurrency().getObposPosprecision() == null ? order.getCurrency()
        .getPricePrecision().intValue() : order.getCurrency().getObposPosprecision().intValue();

    if (!order.getFINPaymentScheduleList().isEmpty()) {
      paymentSchedule = order.getFINPaymentScheduleList().get(0);
    } else {
      paymentSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
      paymentSchedule.setId(order.getId());
      paymentSchedule.setNewOBObject(true);
      paymentSchedule.setCurrency(order.getCurrency());
      paymentSchedule.setOrder(order);
    }
    if (order.getFINPaymentScheduleList().isEmpty() || isModified) {
      order.getFINPaymentScheduleList().add(paymentSchedule);
      paymentSchedule.setFinPaymentmethod(order.getPaymentMethod());
      // paymentSchedule.setPaidAmount(new BigDecimal(0));
      paymentSchedule.setAmount(gross.setScale(pricePrecision, RoundingMode.HALF_UP));
      // Sept 2012 -> gross because outstanding is not allowed in Openbravo Web POS
      paymentSchedule.setOutstandingAmount(gross.setScale(pricePrecision, RoundingMode.HALF_UP));
      paymentSchedule.setDueDate(order.getOrderDate());
      paymentSchedule.setExpectedDate(order.getOrderDate());
      if (ModelProvider.getInstance().getEntity(FIN_PaymentSchedule.class)
          .hasProperty("origDueDate")) {
        // This property is checked and set this way to force compatibility with both MP13, MP14
        // and
        // later releases of Openbravo. This property is mandatory and must be set. Check issue
        paymentSchedule.set("origDueDate", paymentSchedule.getDueDate());
      }
      paymentSchedule.setFINPaymentPriority(order.getFINPaymentPriority());
      OBDal.getInstance().save(paymentSchedule);
      OBDal.getInstance().save(order);
    }

    BigDecimal writeoffAmt = paymentAmt.subtract(gross);
    // If there's not a real over payment, the writeoff is set to zero
    if (!(writeoffAmt.signum() == 1 && !isNegative) && !(writeoffAmt.signum() == -1 && isNegative)) {
      writeoffAmt = BigDecimal.ZERO;
    }

    // When a replaced ticket or a new one is synchronized without any payment, a new payment
    // schedule detail must be created with the remaining amount
    if ((!jsonorder.optBoolean("isLayaway", false) && !jsonorder.optBoolean("isPaid", false))
        || doCancelAndReplace || doCancelLayaway) {
      final FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
      paymentScheduleDetail.setAmount(gross);
      paymentScheduleDetail.setBusinessPartner(order.getBusinessPartner());
      paymentScheduleDetail.setNewOBObject(true);
      OBDal.getInstance().save(paymentScheduleDetail);
      paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
          paymentScheduleDetail);
    }

    for (int i = 0; i < payments.length(); i++) {
      JSONObject payment = payments.getJSONObject(i);
      OBPOSAppPayment paymentType = null;
      if (payment.optBoolean("isPrePayment", false)) {
        continue;
      }

      // When doing a reverse payment, normally the reversal payment has the 'paid' property to 0,
      // because this 'paid' property is the sum of the total amount paid by this payment method
      // (normally a payment is reversed to set the total quantity of that payment method to 0).
      // Because of that, the next condition must be ignored to reversal payments
      BigDecimal paid = BigDecimal.valueOf(payment.getDouble("paid"));
      boolean isReversalPayment = payment.has("reversedPaymentId");
      if (paid.compareTo(BigDecimal.ZERO) == 0 && !isReversalPayment) {
        continue;
      }
      String paymentTypeName = payment.getString("kind");
      OBCriteria<OBPOSAppPayment> type = OBDal.getInstance().createCriteria(OBPOSAppPayment.class);
      type.add(Restrictions.eq(OBPOSAppPayment.PROPERTY_SEARCHKEY, paymentTypeName));
      type.add(Restrictions.eq(OBPOSAppPayment.PROPERTY_OBPOSAPPLICATIONS + ".id", posTerminalId));
      type.setMaxResults(1);
      paymentType = (OBPOSAppPayment) type.uniqueResult();

      if (paymentType == null) {
        @SuppressWarnings("unchecked")
        Class<PaymentProcessor> paymentclazz = (Class<PaymentProcessor>) Class
            .forName(paymentTypeName);
        PaymentProcessor paymentinst = paymentclazz.getDeclaredConstructor().newInstance();
        paymentinst.process(payment, order, null, writeoffAmt);
      } else {
        FIN_FinancialAccount account = null;
        if (payment.has("account") && payment.get("account") != JSONObject.NULL) {
          account = OBDal.getInstance().get(FIN_FinancialAccount.class,
              payment.getString("account"));
        }
        if (paymentType.getFinancialAccount() == null && account == null) {
          continue;
        }
        BigDecimal amount = BigDecimal.valueOf(payment.getDouble("origAmount")).setScale(
            pricePrecision, RoundingMode.HALF_UP);
        BigDecimal tempWriteoffAmt = writeoffAmt;
        if (writeoffAmt.compareTo(BigDecimal.ZERO) != 0
            && writeoffAmt.abs().compareTo(amount.abs()) == 1) {
          // In case writeoff is higher than amount, we put 1 as payment and rest as overpayment
          // because the payment cannot be 0 (It wouldn't be created)
          tempWriteoffAmt = amount.abs().subtract(BigDecimal.ONE);
        }
        if (useOrderDocumentNoForRelatedDocs) {
          paymentCount++;
        }
        if (paidReceipt && donePressed) {
          // Update customer credit. Subtract the amount paid to the existing credit.
          OBContext.setAdminMode(false);
          try {
            BigDecimal creditPaid = BigDecimal.valueOf(payment.getDouble("amount")).setScale(
                pricePrecision, RoundingMode.HALF_UP);
            if (!order.getCurrency()
                .equals(order.getBusinessPartner().getPriceList().getCurrency())) {
              creditPaid = convertCurrencyOrder(order,
                  BigDecimal.valueOf(payment.getDouble("amount")));
            }
            order.getBusinessPartner().setCreditUsed(
                order.getBusinessPartner().getCreditUsed().subtract(creditPaid));
          } finally {
            OBContext.restorePreviousMode();
          }
        }
        processPayments(paymentSchedule, order, paymentType, payment, tempWriteoffAmt, jsonorder,
            account);
        writeoffAmt = writeoffAmt.subtract(tempWriteoffAmt);
      }
    }
    if (paidOnCredit || (!paidReceipt && hasPrepayment)) {
      // Update customer credit. Credit generated.
      OBContext.setAdminMode(false);
      try {
        BigDecimal creditGenerated = gross.subtract(paymentAmt).setScale(pricePrecision,
            RoundingMode.HALF_UP);

        if (!order.getCurrency().equals(order.getBusinessPartner().getPriceList().getCurrency())) {
          creditGenerated = convertCurrencyOrder(order, gross.subtract(paymentAmt));
        }
        // Same currency, no conversion required
        order.getBusinessPartner().setCreditUsed(
            order.getBusinessPartner().getCreditUsed().add(creditGenerated));
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("paymentSchedule", paymentSchedule);

    return jsonResponse;
  }

  protected void processPayments(FIN_PaymentSchedule paymentSchedule, Order order,
      OBPOSAppPayment paymentType, JSONObject payment, BigDecimal writeoffAmt,
      JSONObject jsonorder, FIN_FinancialAccount account) throws Exception {
    OBContext.setAdminMode(false);
    try {
      int pricePrecision = order.getCurrency().getObposPosprecision() == null ? order.getCurrency()
          .getPricePrecision().intValue() : order.getCurrency().getObposPosprecision().intValue();
      BigDecimal amount = BigDecimal.valueOf(payment.getDouble("origAmount")).setScale(
          pricePrecision, RoundingMode.HALF_UP);
      BigDecimal origAmount = amount;
      BigDecimal mulrate = new BigDecimal(1);
      // FIXME: Coversion should be only in one direction: (USD-->EUR)
      if (payment.has("mulrate") && payment.getDouble("mulrate") != 1) {
        mulrate = BigDecimal.valueOf(payment.getDouble("mulrate"));
        if (payment.has("amount")) {
          origAmount = BigDecimal.valueOf(payment.getDouble("amount")).setScale(pricePrecision,
              RoundingMode.HALF_UP);
        } else {
          origAmount = amount.multiply(mulrate).setScale(pricePrecision, RoundingMode.HALF_UP);
        }
      }

      // writeoffAmt.divide(BigDecimal.valueOf(payment.getDouble("rate")));
      if (amount.signum() == 0) {
        return;
      }
      if (writeoffAmt.compareTo(BigDecimal.ZERO) != 0) {
        // there was an overpayment, we need to take into account the writeoffamt
        amount = amount.subtract(writeoffAmt).setScale(pricePrecision, RoundingMode.HALF_UP);
      }

      final List<FIN_PaymentScheduleDetail> paymentScheduleDetailList = new ArrayList<FIN_PaymentScheduleDetail>();
      final HashMap<String, BigDecimal> paymentAmountMap = new HashMap<String, BigDecimal>();
      if (payment.has("isReversePayment")) {
        // If the current payment is a reversal payment, a new PSD must be added for each PSD in the
        // reversed payment
        final StringBuffer reversedPSDHQL = new StringBuffer();
        reversedPSDHQL.append("SELECT psd ");
        reversedPSDHQL.append("FROM FIN_Payment_ScheduleDetail psd ");
        reversedPSDHQL.append("JOIN psd.orderPaymentSchedule ps ");
        reversedPSDHQL.append("JOIN psd.paymentDetails pd ");
        reversedPSDHQL.append("JOIN pd.finPayment p ");
        reversedPSDHQL.append("WHERE p.id = :paymentId ");
        reversedPSDHQL.append("AND ps.id = :paymentSchId");
        final Query<FIN_PaymentScheduleDetail> reversedPSDQuery = OBDal.getInstance().getSession()
            .createQuery(reversedPSDHQL.toString(), FIN_PaymentScheduleDetail.class);
        reversedPSDQuery.setParameter("paymentId", payment.getString("reversedPaymentId"));
        reversedPSDQuery.setParameter("paymentSchId", paymentSchedule.getId());
        final List<FIN_PaymentScheduleDetail> reversedPSDList = reversedPSDQuery.list();
        for (final FIN_PaymentScheduleDetail reversedPSD : reversedPSDList) {
          // Create the new paymentScheduleDetail for the reversal payment
          final FIN_PaymentScheduleDetail newPSD = OBProvider.getInstance().get(
              FIN_PaymentScheduleDetail.class);
          newPSD.setNewOBObject(true);
          newPSD.setOrderPaymentSchedule(paymentSchedule);
          newPSD.setInvoicePaymentSchedule(reversedPSD.getInvoicePaymentSchedule());
          newPSD.setAmount(reversedPSD.getAmount().negate());
          newPSD.setBusinessPartner(order.getBusinessPartner());
          paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(newPSD);
          OBDal.getInstance().save(newPSD);
          paymentScheduleDetailList.add(newPSD);
          paymentAmountMap.put(newPSD.getId(), newPSD.getAmount());
          // Created the paymentScheduleDetail with the remaining after adding the reversal payment.
          // If there is an existing PSD with the remaining amount add the reversed quantity to that
          // PSD instead of creating a new one
          final OBCriteria<FIN_PaymentScheduleDetail> remainingPSDCriteria = OBDal.getInstance()
              .createCriteria(FIN_PaymentScheduleDetail.class);
          remainingPSDCriteria.add(Restrictions.eq(
              FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
          if (reversedPSD.getInvoicePaymentSchedule() != null) {
            remainingPSDCriteria.add(Restrictions.eq(
                FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
                reversedPSD.getInvoicePaymentSchedule()));
          } else {
            remainingPSDCriteria.add(Restrictions
                .isNull(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE));
          }
          remainingPSDCriteria.add(Restrictions
              .isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
          remainingPSDCriteria.setMaxResults(1);
          final FIN_PaymentScheduleDetail remainingPSD = (FIN_PaymentScheduleDetail) remainingPSDCriteria
              .uniqueResult();
          if (remainingPSD == null) {
            final FIN_PaymentScheduleDetail newRemainingPSD = OBProvider.getInstance().get(
                FIN_PaymentScheduleDetail.class);
            newRemainingPSD.setNewOBObject(true);
            newRemainingPSD.setOrderPaymentSchedule(paymentSchedule);
            newRemainingPSD.setInvoicePaymentSchedule(reversedPSD.getInvoicePaymentSchedule());
            newRemainingPSD.setAmount(reversedPSD.getAmount());
            newRemainingPSD.setBusinessPartner(order.getBusinessPartner());
            paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
                newRemainingPSD);
            OBDal.getInstance().save(newRemainingPSD);
          } else {
            remainingPSD.setAmount(remainingPSD.getAmount().add(reversedPSD.getAmount()));
            OBDal.getInstance().save(remainingPSD);
          }
        }
      } else {
        BigDecimal remainingAmount = amount;
        boolean isNegativePayment = amount.compareTo(BigDecimal.ZERO) == -1 ? true : false;
        // Get the remaining PSD and sort it by the ones that are related to an invoice
        final List<FIN_PaymentScheduleDetail> remainingPSDList = new ArrayList<>();
        BigDecimal paymentsRemainingAmt = BigDecimal.ZERO;
        for (final FIN_PaymentScheduleDetail currentDetail : paymentSchedule
            .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
          if (currentDetail.getPaymentDetails() == null) {
            remainingPSDList.add(currentDetail);
            paymentsRemainingAmt = paymentsRemainingAmt.add(currentDetail.getAmount());
          }
        }
        if (paymentsRemainingAmt.signum() == remainingAmount.signum()) {
          sortPSDByInvoice(remainingPSDList);
          for (final FIN_PaymentScheduleDetail currentDetail : remainingPSDList) {
            if ((!isNegativePayment && remainingAmount.compareTo(BigDecimal.ZERO) == 1)
                || (isNegativePayment && remainingAmount.compareTo(BigDecimal.ZERO) == -1)) {
              if ((!isNegativePayment && remainingAmount.compareTo(currentDetail.getAmount()) != -1)
                  || (isNegativePayment && remainingAmount.compareTo(currentDetail.getAmount()) != 1)) {
                remainingAmount = remainingAmount.subtract(currentDetail.getAmount());
              } else {
                // Create a new paymentScheduleDetail for pending amount to be paid and add it to
                // the paymentScheduleDetailList and to the paymentAmountList
                final FIN_PaymentScheduleDetail newPSD = OBProvider.getInstance().get(
                    FIN_PaymentScheduleDetail.class);
                newPSD.setNewOBObject(true);
                newPSD.setOrderPaymentSchedule(paymentSchedule);
                newPSD.setInvoicePaymentSchedule(currentDetail.getInvoicePaymentSchedule());
                newPSD.setAmount(currentDetail.getAmount().subtract(remainingAmount));
                newPSD.setBusinessPartner(order.getBusinessPartner());
                paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(newPSD);
                OBDal.getInstance().save(newPSD);

                // Modify the existing paymentScheduleDetail to match the remaining to pay
                currentDetail.setAmount(remainingAmount);
                OBDal.getInstance().save(currentDetail);

                remainingAmount = BigDecimal.ZERO;
              }
              paymentScheduleDetailList.add(currentDetail);
              paymentAmountMap.put(currentDetail.getId(), currentDetail.getAmount());
            } else {
              break;
            }
          }
          if ((!isNegativePayment && remainingAmount.compareTo(BigDecimal.ZERO) == 1)
              || (isNegativePayment && remainingAmount.compareTo(BigDecimal.ZERO) == -1)) {
            // There can be the possibility that the user is paying more than the remaining amount.
            // In this case, the previously created PSD must be updated to add this amount. This
            // occurs only in a special case, that is when paying more than the remaining to pay
            // using a cash payment method and there's no enough change to return in that payment
            // method. That payment is not set as over payment, even when the amount is higher than
            // the expected amount. After this payment, another one will come but in negative to set
            // the paid and outstanding amounts to 0.
            FIN_PaymentSchedule newPSInvoice = null;
            if (paymentScheduleDetailList.size() != 0) {
              final FIN_PaymentScheduleDetail newPSD = paymentScheduleDetailList
                  .get(paymentScheduleDetailList.size() - 1);
              newPSD.setAmount(newPSD.getAmount().add(remainingAmount));
              OBDal.getInstance().save(newPSD);
              paymentAmountMap.put(newPSD.getId(), newPSD.getAmount());
              // If the PSD has an invoice, the new remaining PSD must have the invoice set
              if (newPSD.getInvoicePaymentSchedule() != null) {
                newPSInvoice = newPSD.getInvoicePaymentSchedule();
              }
            }
            final FIN_PaymentScheduleDetail newRemainingPSD = OBProvider.getInstance().get(
                FIN_PaymentScheduleDetail.class);
            newRemainingPSD.setNewOBObject(true);
            newRemainingPSD.setOrderPaymentSchedule(paymentSchedule);
            newRemainingPSD.setInvoicePaymentSchedule(newPSInvoice);
            newRemainingPSD.setAmount(remainingAmount.negate());
            newRemainingPSD.setBusinessPartner(order.getBusinessPartner());
            paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
                newRemainingPSD);
            OBDal.getInstance().save(newRemainingPSD);
          }
        } else {
          // The quantity that is being introduced has a different sign to the pending quantity.
          // This means that a negative payment is being introduced in a positive ticket, or that a
          // positive payment is being introduced in a negative ticket. Instead of consuming the
          // remaining payment, it is increased.
          // This only occurs in a C&R or a CL processes.
          final FIN_PaymentScheduleDetail newPSD;
          if (remainingPSDList.size() > 0) {
            newPSD = remainingPSDList.get(remainingPSDList.size() - 1);
          } else {
            // Create a new PSD if there was not a remaining amount
            newPSD = OBProvider.getInstance().get(FIN_PaymentScheduleDetail.class);
            newPSD.setNewOBObject(true);
            newPSD.setOrderPaymentSchedule(paymentSchedule);
            newPSD.setBusinessPartner(order.getBusinessPartner());
            paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(newPSD);
          }
          // Create the new PSD for the remaining amount
          final FIN_PaymentScheduleDetail newRemainingPSD = OBProvider.getInstance().get(
              FIN_PaymentScheduleDetail.class);
          newRemainingPSD.setNewOBObject(true);
          newRemainingPSD.setOrderPaymentSchedule(paymentSchedule);
          newRemainingPSD.setInvoicePaymentSchedule(newPSD.getInvoicePaymentSchedule());
          newRemainingPSD.setAmount(newPSD.getAmount().subtract(remainingAmount));
          newRemainingPSD.setBusinessPartner(order.getBusinessPartner());
          paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList()
              .add(newRemainingPSD);
          OBDal.getInstance().save(newRemainingPSD);
          // Set the quantity to the payment that is being created
          newPSD.setAmount(remainingAmount);
          OBDal.getInstance().save(newPSD);

          paymentScheduleDetailList.add(newPSD);
          paymentAmountMap.put(newPSD.getId(), remainingAmount);
        }
      }

      DocumentType paymentDocType = getPaymentDocumentType(order.getOrganization());
      Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.class);

      String paymentDocNo;
      if (useOrderDocumentNoForRelatedDocs) {
        paymentDocNo = order.getDocumentNo();
        if (paymentCount > 0) {
          paymentDocNo = paymentDocNo + "-" + paymentCount;
        }
      } else {
        paymentDocNo = getDocumentNo(paymentEntity, null, paymentDocType);
      }
      if (payment.has("reversedPaymentId") && payment.getString("reversedPaymentId") != null) {
        paymentDocNo = "*R*" + paymentDocNo;
      }

      // get date
      Date calculatedDate = (payment.has("date") && !payment.isNull("date")) ? OBMOBCUtils
          .calculateServerDate((String) payment.get("date"), jsonorder.getLong("timezoneOffset"))
          : OBMOBCUtils.stripTime(new Date());

      // insert the payment
      FIN_Payment finPayment = FIN_AddPayment.savePayment(null, true, paymentDocType, paymentDocNo,
          order.getBusinessPartner(), paymentType.getPaymentMethod().getPaymentMethod(),
          account == null ? paymentType.getFinancialAccount() : account, amount.toString(),
          calculatedDate, order.getOrganization(), null, paymentScheduleDetailList,
          paymentAmountMap, false, false, order.getCurrency(), mulrate, origAmount, true,
          payment.has("id") ? payment.getString("id") : null);

      // Associate a GLItem with the overpayment amount to the payment which generates the
      // overpayment
      if (writeoffAmt.compareTo(BigDecimal.ZERO) != 0) {
        FIN_AddPayment.saveGLItem(finPayment, writeoffAmt, paymentType.getPaymentMethod()
            .getGlitemWriteoff(),
            payment.has("id") ? OBMOBCUtils.getUUIDbyString(payment.getString("id")) : null);
        // Update Payment In amount after adding GLItem
        finPayment.setAmount(origAmount.setScale(pricePrecision, RoundingMode.HALF_UP));
      }

      if (paidOnCredit) {
        List<FIN_PaymentDetail> paymentDetailList = finPayment.getFINPaymentDetailList();
        if (paymentDetailList.size() > 0) {
          for (FIN_PaymentDetail paymentDetail : paymentDetailList) {
            paymentDetail.setPrepayment(true);
          }
          OBDal.getInstance().flush();
        }
      }

      if (payment.has("paymentData") && payment.getString("paymentData").length() > 0
          && !("null".equals(payment.getString("paymentData")))) {
        // ensure that it is a valid JSON Object prior to save it
        try {
          JSONObject jsonPaymentData = payment.getJSONObject("paymentData");
          finPayment.setObposPaymentdata(jsonPaymentData.toString());
        } catch (Exception e) {
          throw new OBException("paymentData attached to payment " + finPayment.getIdentifier()
              + " is not a valid JSON.");
        }
      }

      if (payment.has("reversedPaymentId") && payment.getString("reversedPaymentId") != null) {
        FIN_Payment reversedPayment = OBDal.getInstance().get(FIN_Payment.class,
            payment.getString("reversedPaymentId"));
        reversedPayment.setReversedPayment(finPayment);
        OBDal.getInstance().save(reversedPayment);
      }
      finPayment.setObposAppCashup(jsonorder.has("obposAppCashup") ? OBDal.getInstance().get(
          OBPOSAppCashup.class, jsonorder.getString("obposAppCashup")) : null);
      finPayment.setOBPOSPOSTerminal(payment.has("oBPOSPOSTerminal") ? OBDal.getInstance().get(
          OBPOSApplications.class, payment.getString("oBPOSPOSTerminal")) : null);

      OBDal.getInstance().save(finPayment);

      long t1 = System.currentTimeMillis();
      // Call all OrderProcess injected.
      executeOrderLoaderPreProcessPaymentHook(preProcessPayment, jsonorder, order, payment,
          finPayment);
      FIN_PaymentProcess.doProcessPayment(finPayment, "P", null, null);
      ImportEntryManager.getInstance().reportStats("processPayments",
          (System.currentTimeMillis() - t1));

      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      vars.setSessionValue("POSOrder", "Y");

      // retrieve the transactions of this payment and set the cashupId to those transactions
      if (!jsonorder.has("channel")) {
        OBDal.getInstance().refresh(finPayment);
        final List<FIN_FinaccTransaction> transactions = finPayment.getFINFinaccTransactionList();
        final String cashupId = jsonorder.getString("obposAppCashup");
        if (Utility.isUUIDString(cashupId)) {
          final OBPOSAppCashup cashup = OBDal.getInstance()
              .getProxy(OBPOSAppCashup.class, cashupId);
          for (FIN_FinaccTransaction transaction : transactions) {
            transaction.setObposAppCashup(cashup);
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void sortPSDByInvoice(List<FIN_PaymentScheduleDetail> psdList) {
    Collections.sort(psdList, new Comparator<FIN_PaymentScheduleDetail>() {
      @Override
      public int compare(FIN_PaymentScheduleDetail psd1, FIN_PaymentScheduleDetail psd2) {
        boolean isNullPSD1 = psd1.getInvoicePaymentSchedule() == null;
        boolean isNullPSD2 = psd2.getInvoicePaymentSchedule() == null;
        if (isNullPSD1 == isNullPSD2) {
          return 0;
        } else if (isNullPSD1) {
          return -1;
        } else {
          return 1;
        }
      }
    });
  }

  private int countPayments(Order order) {
    final String countHql = "select count(*) from FIN_Payment_ScheduleDetail where "
        + FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE + "."
        + FIN_PaymentSchedule.PROPERTY_ORDER + "=:order" + " and "
        + FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS + " is not null ";
    final Query<Number> qry = OBDal.getInstance().getSession().createQuery(countHql, Number.class);
    qry.setParameter("order", order);
    return qry.uniqueResult().intValue();
  }

  private void verifyCashupStatus(JSONObject jsonorder) throws JSONException, OBException {
    OBContext.setAdminMode(false);
    try {
      if (jsonorder.has("obposAppCashup") && jsonorder.getString("obposAppCashup") != null
          && !jsonorder.getString("obposAppCashup").equals("")) {
        OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class,
            jsonorder.getString("obposAppCashup"));
        if (cashUp != null && cashUp.isProcessedbo()) {
          // Additional check to verify that the cashup related to the order has not been processed
          throw new OBException("The cashup related to this order has been processed");
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void verifyOrderLineTax(JSONObject jsonorder) throws JSONException, OBException {
    JSONArray orderlines = jsonorder.getJSONArray("lines");
    for (int i = 0; i < orderlines.length(); i++) {
      JSONObject jsonOrderLine = orderlines.getJSONObject(i);
      if (!jsonOrderLine.has(OrderLine.PROPERTY_TAX)
          || StringUtils.length(jsonOrderLine.getString(OrderLine.PROPERTY_TAX)) != 32) {
        throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_OrderProductWithoutTax", OBContext.getOBContext().getLanguage().getLanguage()));
      }
    }
  }

  private void createApprovals(Order order, JSONObject jsonorder) {
    if (!jsonorder.has("approvals")) {
      return;
    }
    Entity approvalEntity = ModelProvider.getInstance().getEntity(OrderApproval.class);
    try {
      JSONArray approvals = jsonorder.getJSONArray("approvals");
      for (int i = 0; i < approvals.length(); i++) {
        JSONObject jsonApproval = approvals.getJSONObject(i);

        OrderApproval approval = OBProvider.getInstance().get(OrderApproval.class);

        JSONPropertyToEntity.fillBobFromJSON(approvalEntity, approval, jsonApproval,
            jsonorder.getLong("timezoneOffset"));

        approval.setSalesOrder(order);

        Long value = jsonorder.getLong("created");
        Date creationDate = new Date(value);
        approval.setCreationDate(creationDate);
        approval.setUpdated(creationDate);

        OBDal.getInstance().save(approval);
      }

    } catch (JSONException e) {
      log.error("Error creating approvals for order" + order, e);
    }
  }

  protected String getDocumentNo(Entity entity, DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "", entity
            .getTableName(), doctypeTarget == null ? "" : doctypeTarget.getId(),
        doctype == null ? "" : doctype.getId(), false, true);
  }

  protected String getDummyDocumentNo() {
    return "DOCNO" + System.currentTimeMillis();
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  protected boolean goodsToDeliver(Order order) {
    for (OrderLine line : order.getOrderLineList()) {
      if (line.getObposQtytodeliver().compareTo(BigDecimal.ZERO) == 0 || !line.isObposIspaid()) {
        continue;
      } else {
        return true;
      }
    }
    return false;
  }

}
