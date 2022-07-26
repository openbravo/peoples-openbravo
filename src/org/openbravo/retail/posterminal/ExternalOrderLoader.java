/*
 ************************************************************************************
 * Copyright (C) 2015-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.LockOptions;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryArchive;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * Order loader which translates a json which is more externally friendly to a json which can be
 * processed by the order loader. It also will make sure that any errors are returned to the caller
 * in the expected json format.
 */
@AuthenticationManager.Stateless
public class ExternalOrderLoader extends OrderLoader {

  private static final String ORDERLOADER_QUALIFIER = "OBPOS_ExternalOrder";

  public static final String APP_NAME = "External";

  private static final Logger log = LogManager.getLogger();

  public static final String CREATE = "create";
  public static final String PAY = "pay";
  public static final String SHIP = "ship";
  public static final String CANCEL = "cancel";
  public static final String CANCEL_REPLACE = "cancel_replace";
  public static final String ALL = "all";
  HashMap<String, BigDecimal> consumedOriginalOrderLineQtyInSameReturnOrder = new HashMap<String, BigDecimal>();

  private static ThreadLocal<JSONArray> processedOrders = new ThreadLocal<JSONArray>();
  private static ThreadLocal<Throwable> exception = new ThreadLocal<Throwable>();
  private static ThreadLocal<JSONObject> transformedMessage = new ThreadLocal<JSONObject>();

  protected static Throwable getCurrentException() {
    return exception.get();
  }

  protected static void setCurrentException(Throwable t) {
    exception.set(t);
  }

  protected static SimpleDateFormat createOrderLoaderDateTimeFormat() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZZZZZ");
    dateFormat.setLenient(true);
    return dateFormat;
  }

  protected static boolean isSynchronizedRequest() {
    final String param = (String) RequestContext.get()
        .getRequest()
        .getParameter("synchronizedProcessing");
    return param != null && param.toLowerCase().equals("true");
  }

  @Inject
  @Any
  private Instance<DataResolver> dataResolvers;

  private List<DataResolver> dataResolversList = null;

  @Override
  public String getAppName() {
    return "External";
  }

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntity(Order.ENTITY_NAME);
  }

  @Override
  protected boolean bypassSecurity() {
    return true;
  }

  @Override
  public void executeCreateImportEntry(Writer w, JSONObject jsonObject) {
    JSONObject message = null;
    try {
      // start with a fresh set
      exception.set(null);
      processedOrders.set(new JSONArray());

      // check if the message was already sent, if so return previous responses
      final String messageId = jsonObject.getString("messageId");
      String currentImportState = getCurrentImportEntryState(messageId);
      if ("Processed".equals(currentImportState) || "Error".equals(currentImportState)) {
        final String currentResponse = getCurrentResponse(messageId);
        if (currentResponse == null) {
          if ("Processed".equals(currentImportState)) {
            writeSuccessMessage(w);
          } else if ("Error".equals(currentImportState)) {
            writeJson(w, createErrorJSON(jsonObject, new OBException()));
          } else {
            throw new OBException("Import state not supported " + currentImportState, true);
          }
        }
        writeCurrentResponse(currentResponse, w);
        log.debug("Found already processed message, returning its response " + currentResponse);
        return;
      } else if ("Initial".equals(currentImportState)) {
        log.debug("Found message being processed, waiting for result");

        // is being processed, wait a while for the response to happen
        final long waitUntil = System.currentTimeMillis() + getProcessingWaitTime();
        String currentResponse = null;
        while (waitUntil > System.currentTimeMillis()) {
          Thread.sleep(500); // try every 0.5 secs
          currentResponse = getCurrentResponse(messageId);
          if (currentResponse != null) {
            break;
          }
        }
        if (currentResponse == null) {
          // try one more time
          currentResponse = getCurrentResponse(messageId);
          if (currentResponse == null) {
            throw new OBException(
                "Message is being processed, but processing takes too long " + jsonObject, true);
          }
        }
        writeCurrentResponse(currentResponse, w);
        log.debug("Message finished processing, returning its response " + currentResponse);
        return;
      } else if (currentImportState != null) {
        throw new OBException(
            "Can not handle current state " + currentImportState + " " + jsonObject);
      }

      // note to prevent dead locking this call needs to be done after the check if the
      // import entry was already processed
      // deadlocking happens on the document no of the pos terminal
      message = transformMessage(jsonObject);
      transformedMessage.set(message);

      if (isSynchronizedRequest()) {
        // also for synchronized requests create an import entry for syncing
        // and tracking
        try {
          OBContext.setAdminMode(false);

          String checkImportState = getCurrentImportEntryState(messageId);
          if (checkImportState != null) {
            throw new OBException("Duplicate message found, but not captured on time " + message);
          }
          // create the entry in initial state, will be set to processed by the parent class
          final String id = jsonObject.getString("messageId");
          getImportEntryManager().createImportEntry(id, ORDERLOADER_QUALIFIER, message.toString(),
              false);

          // save it so that double requests can be detected right away
          OBDal.getInstance().commitAndClose();

          setImportEntryId(id);
        } finally {
          OBContext.restorePreviousMode();
        }

        // Now execute the orderloader directly
        super.exec(w, message);
      } else {
        super.executeCreateImportEntry(w, message);
      }
    } catch (Throwable t) {
      Throwable cause = DbUtility.getUnderlyingSQLException(t);
      log.error(t.getMessage() + " --> " + (message != null ? message : jsonObject), cause);
      writeJson(w, createErrorJSON(jsonObject, cause));
    } finally {
      exception.set(null);
      processedOrders.set(null);
      transformedMessage.set(null);
    }
  }

  private void writeCurrentResponse(String currentResponse, Writer w) throws JSONException {
    final JSONObject responseJson = new JSONObject(currentResponse);

    JSONObject contextInfo = getContextInformation();
    if (contextInfo != null) {
      responseJson.put("contextInfo", contextInfo);
    }
    writeJson(w, responseJson);

    if (RequestContext.get().getResponse() != null) {
      RequestContext.get().getResponse().setStatus(HttpServletResponse.SC_OK);
    }
  }

  private long getProcessingWaitTime() {
    try {
      OBContext.setAdminMode(false);
      final String value = Preferences.getPreferenceValue(
          "OBPOS_ExternalOrderLoaderWaitForProcessingTime", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
      return 1000 * Long.parseLong(value);
    } catch (Exception e) {
      // default wait 10 seconds
      return 10000;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public JSONObject exec(JSONObject json) throws JSONException, ServletException {
    JSONObject jsonIn = json;
    try {
      return super.exec(jsonIn);
    } catch (Throwable t) {
      return createErrorJSON(json, t);
    }
  }

  @Override
  public JSONObject exec(JSONObject json, boolean shouldFailWithError)
      throws JSONException, ServletException {
    JSONObject jsonIn = json;
    try {
      return super.exec(jsonIn, shouldFailWithError);
    } catch (Throwable t) {
      return createErrorJSON(json, t);
    }
  }

  protected void writeJson(Writer w, JSONObject j) {
    try {
      final String jStr = j.toString();
      w.write(jStr.substring(1, jStr.length() - 1));
    } catch (IOException e) {
      throw new OBException("Exception when writing: " + j.toString(), e);
    }
  }

  /**
   * @deprecated method is not being used anymore
   */
  @Deprecated
  protected boolean messageAlreadyReceived(String id) {
    // check if it is not there already or already archived
    {
      final Query<Number> qry = SessionHandler.getInstance()
          .getSession()
          .createQuery("select count(*) from " + ImportEntry.ENTITY_NAME + " where id=:id",
              Number.class);
      qry.setParameter("id", id);
      if (qry.uniqueResult().intValue() > 0) {
        return true;
      }
    }
    {
      final Query<Number> qry = SessionHandler.getInstance()
          .getSession()
          .createQuery("select count(*) from " + ImportEntryArchive.ENTITY_NAME + " where id=:id",
              Number.class);
      qry.setParameter("id", id);
      if (qry.uniqueResult().intValue() > 0) {
        return true;
      }
    }
    return false;
  }

  protected String getCurrentImportEntryState(String id) {
    try {
      OBContext.setAdminMode();
      // check if it is not there already or already archived
      {
        final Query<String> qry = SessionHandler.getInstance()
            .getSession()
            .createQuery("select " + ImportEntry.PROPERTY_IMPORTSTATUS + " from "
                + ImportEntry.ENTITY_NAME + " where id=:id", String.class);
        qry.setParameter("id", id);
        final List<String> result = qry.list();
        if (!result.isEmpty()) {
          return result.get(0);
        }
      }
      {
        final Query<String> qry = SessionHandler.getInstance()
            .getSession()
            .createQuery("select " + ImportEntry.PROPERTY_IMPORTSTATUS + " from "
                + ImportEntryArchive.ENTITY_NAME + " where id=:id", String.class);
        qry.setParameter("id", id);
        final List<String> result = qry.list();
        if (!result.isEmpty()) {
          return (String) result.get(0);
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected String getCurrentResponse(String id) {
    // check if it is not there already or already archived
    {
      final Query<String> qry = SessionHandler.getInstance()
          .getSession()
          .createQuery("select " + ImportEntry.PROPERTY_RESPONSEINFO + " from "
              + ImportEntry.ENTITY_NAME + " where id=:id", String.class);
      qry.setParameter("id", id);
      final List<String> result = qry.list();
      if (!result.isEmpty()) {
        return result.get(0);
      }
    }
    {
      final Query<String> qry = SessionHandler.getInstance()
          .getSession()
          .createQuery("select " + ImportEntry.PROPERTY_RESPONSEINFO + " from "
              + ImportEntryArchive.ENTITY_NAME + " where id=:id", String.class);
      qry.setParameter("id", id);
      final List<String> result = qry.list();
      if (!result.isEmpty()) {
        return result.get(0);
      }
    }
    return null;
  }

  @Override
  protected JSONObject createErrorResponse(JSONArray incomingJson, List<String> errorIds,
      List<String> errorMsgs) throws JSONException {
    if (exception.get() != null) {
      Throwable cause = DbUtility.getUnderlyingSQLException(exception.get());
      return createErrorJSON(transformedMessage.get(), cause);
    } else {
      return createSuccessResponse(incomingJson);
    }
  }

  @Override
  protected JSONObject createSuccessResponse(JSONArray incomingJson) throws JSONException {

    try {
      // be backward compatible
      final StringWriter w = new StringWriter();
      writeSuccessMessage(w);
      if (RequestContext.get().getResponse() != null) {
        RequestContext.get().getResponse().setStatus(HttpServletResponse.SC_OK);
      }

      return new JSONObject("{" + w.toString() + "}");
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  protected void writeSuccessMessage(Writer w) {
    try {
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
      jsonResponse.put("orders", processedOrders.get());
      if (RequestContext.get().getResponse() != null) {
        RequestContext.get().getResponse().setStatus(HttpServletResponse.SC_OK);
      }
      writeJson(w, jsonResponse);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  // collect all the successfully created orders
  @Override
  protected JSONObject successMessage(JSONObject jsonOrder) throws Exception {
    if (processedOrders.get() != null) {
      processedOrders.get().put(processedOrders.get().length(), jsonOrder);
    }
    return super.successMessage(jsonOrder);
  }

  protected JSONObject transformMessage(JSONObject messageIn) throws JSONException {
    if (!messageIn.has("channel") || !"External".equals(messageIn.get("channel"))) {
      return messageIn;
    }
    OBContext.setAdminMode(false);
    try {
      if (log.isDebugEnabled()) {
        log.debug("Transforming message from " + messageIn);
      }
      final JSONObject messageOut = new JSONObject(messageIn.toString());

      messageOut.put("channel", "FromExternal");
      messageOut.put("appName", APP_NAME);

      final OBPOSApplications posTerminal = getPOSTerminal(messageOut);

      if (!OBContext.getOBContext()
          .getWritableOrganizations()
          .contains(posTerminal.getOrganization().getId())) {
        throw new OBException("Actual user " + OBContext.getOBContext().getUser().getIdentifier()
            + " doesn't have access to the organization "
            + posTerminal.getOrganization().getIdentifier());
      }

      messageOut.put("posTerminal", posTerminal.getId());
      messageOut.put("pos", posTerminal.getId());
      setClientOrg(messageOut);

      if (!messageOut.has("messageId")) {
        messageOut.put("messageId", SequenceIdData.getUUID());
      }

      final JSONArray data = messageOut.getJSONArray("data");
      for (int i = 0; i < data.length(); i++) {
        // set the pos terminal to be sure
        final JSONObject order = data.getJSONObject(i);
        if (!order.has("posTerminal")) {
          if (order.has("terminal")) {
            order.put("posTerminal", order.getString("terminal"));
          } else {
            order.put("posTerminal", posTerminal.getId());
          }
        }

        // In case of cancel layaway, validate and transform canceledorder
        if (order.has("step") && CANCEL.equals(order.getString("step"))) {
          if (order.has("canceledorder")) {
            JSONObject cancelledOrder = order.getJSONObject("canceledorder");
            validateCancelHeader(cancelledOrder);
            final Order ord = OBDal.getInstance().get(Order.class, cancelledOrder.get("id"));
            if (ord == null) {
              throw new OBException(
                  "Cancelled order with id " + cancelledOrder.get("id") + " does not exists");
            }
            cancelledOrder.put("isCanceledOrder", true);
            cancelledOrder.put("posTerminal", order.getString("posTerminal"));
            cancelledOrder.put("step", order.getString("step"));
            transformOrder(cancelledOrder);
          } else {
            throw new OBException(
                "Step " + order.getString("step") + " must have property canceledorder");
          }
        }
        if (order.has("step") && CANCEL_REPLACE.equals(order.getString("step"))) {
          order.put("doCancelAndReplace", true);
          order.put("bypassConcurrentModificationCheck", true);
        }

        order.put("orderChannel", "FromExternal");

        transformOrder(data.getJSONObject(i));
      }
      if (log.isDebugEnabled()) {
        log.debug("Transformed message from " + messageIn + " ----------> " + messageOut);
      }
      return messageOut;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected void copyPropertyValue(JSONObject json, String from, String to) throws JSONException {
    if (json.has(to)) {
      return;
    }
    json.put(to, json.get(from));
  }

  protected void writePropertyValue(JSONObject json, String property, Object value)
      throws JSONException {
    if (json.has(property)) {
      return;
    }
    json.put(property, value);
  }

  protected JSONObject createErrorJSON(JSONObject jsonOrder, Throwable t) {
    try {
      Throwable cause = DbUtility.getUnderlyingSQLException(t);
      HttpServletResponse response = RequestContext.get().getResponse();
      if (response != null) {
        response.setStatus(HttpServletResponse.SC_OK);
      }
      // TODO: we give the stacktrace in the json maybe not good to do
      // it like that...
      log.error("Error transforming/handling order " + jsonOrder, cause);
      JSONObject respJson = new JSONObject(JsonUtils.convertExceptionToJson(cause));
      // skip the response part, will be wrapped in a response anyway in the MobileService class
      return respJson.getJSONObject("response");
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  protected void transformOrder(JSONObject orderJson) throws JSONException {

    handleOrderSteps(orderJson);

    setDefaults(orderJson);

    setClientOrg(orderJson);

    disableConcurrencyCheck(orderJson);

    final OBPOSApplications posTerminal = getPOSTerminal(orderJson);

    orderJson.put("obposApplications", posTerminal.getId());

    if (!orderJson.has("id")) {
      orderJson.put("id", SequenceIdData.getUUID());
    }

    setDocumentType(orderJson, posTerminal);

    orderJson.put("currency", resolveJsonValue(Currency.ENTITY_NAME,
        orderJson.getString("currency"), new String[] { "id", "iSOCode" }));

    if (orderJson.has("salesRepresentative")) {
      orderJson.put("salesRepresentative",
          resolveJsonValue(User.ENTITY_NAME, orderJson.getString("salesRepresentative"),
              new String[] { "id", "name", "email", "userName" }));
    }

    setWarehouse(orderJson);

    if (!orderJson.has("priceList")) {
      orderJson.put("priceList", posTerminal.getOrganization().getObretcoPricelist().getId());
      orderJson.put("priceIncludesTax",
          posTerminal.getOrganization().getObretcoPricelist().isPriceIncludesTax());
    }

    setDocumentNo(orderJson);

    setOrderType(orderJson);

    copyPropertyValue(orderJson, "grossAmount", "gross");
    copyPropertyValue(orderJson, "netAmount", "net");

    if (CREATE.equals(orderJson.getString("step")) || SHIP.equals(orderJson.getString("step"))
        || ALL.equals(orderJson.getString("step")) || CANCEL.equals(orderJson.getString("step"))
        || CANCEL_REPLACE.equals(orderJson.getString("step"))) {
      setBusinessPartnerInformation(orderJson);
      // Transform order array to object
      JSONArray orderTaxes = orderJson.optJSONArray("taxes");
      if (orderTaxes != null) {
        JSONObject orderTax = new JSONObject();
        for (int i = 0; i < orderTaxes.length(); i++) {
          JSONObject ordTax = orderTaxes.getJSONObject(i);
          orderTax.put(ordTax.getString("tax"), ordTax);
        }
        orderJson.put("taxes", orderTax);
      }
      transformTaxes(orderJson.getJSONObject("taxes"));
      transformLines(orderJson);
    }

    transformPayments(orderJson);

    setQuantitiesToDeliver(orderJson);
  }

  private void disableConcurrencyCheck(JSONObject orderJson) throws JSONException {

    if (orderJson.has("id")) {
      final Order order = OBDal.getInstance().get(Order.class, orderJson.get("id"));
      if (order != null) {
        orderJson.put("loaded", OBMOBCUtils.convertToUTCDateComingFromServer(order.getUpdated()));
      }
    }
    for (int i = 0; i < orderJson.getJSONArray("lines").length(); i++) {
      JSONObject jsonOrderLine = orderJson.getJSONArray("lines").getJSONObject(i);
      if (jsonOrderLine.has("id")) {
        final OrderLine orderLine = OBDal.getInstance()
            .get(OrderLine.class, jsonOrderLine.optString("id"));
        if (orderLine != null) {
          jsonOrderLine.put("loaded",
              OBMOBCUtils.convertToUTCDateComingFromServer(orderLine.getUpdated()));
        }
      }
    }
  }

  // set the special attributes which are needed to cover/handle create, pay and ship
  // separately, this needs to be improved.
  protected void handleOrderSteps(JSONObject orderJson) throws JSONException {
    if (!orderJson.has("step")) {
      orderJson.put("step", ALL);
    }
    final String step = orderJson.getString("step");
    if (CREATE.equals(step) || CANCEL_REPLACE.equals(step)) {
      orderJson.put("payment", -1);
      orderJson.put("isLayaway", false);
    } else if (PAY.equals(step)) {
      orderJson.put("payment", -1);
      orderJson.put("isLayaway", true);
    } else if (SHIP.equals(step)) {
      orderJson.put("payment", orderJson.getDouble("grossAmount"));
      orderJson.put("generateExternalInvoice", true);
      orderJson.put("generateShipment", true);
      orderJson.put("deliver", true);
      orderJson.put("isLayaway", true);
    } else if (CANCEL.equals(step)) {
      // In case of cancel, set gross amount as payment
      if (orderJson.has("cancelLayaway") && orderJson.getBoolean("cancelLayaway")) {
        orderJson.put("paymentWithSign", orderJson.getDouble("grossAmount"));
        orderJson.put("payment", -orderJson.getDouble("grossAmount"));
      }
    } else if (ALL.equals(step)) {
      copyPropertyValue(orderJson, "grossAmount", "payment");
      orderJson.put("generateExternalInvoice", true);
      orderJson.put("generateShipment", true);
      orderJson.put("deliver", true);
      // do nothing
    } else {
      log.warn("Step value " + step + " not recognized, order " + orderJson + " assuming all");
      copyPropertyValue(orderJson, "grossAmount", "payment");
    }
  }

  protected void transformLines(JSONObject orderJson) throws JSONException {

    for (int i = 0; i < orderJson.getJSONArray("lines").length(); i++) {
      transformLine(orderJson, orderJson.getJSONArray("lines").getJSONObject(i));
    }

    // link any related lines, this needs to be done after the product and line id
    // of the lines has been set
    handleRelatedLines(orderJson);
  }

  protected void transformLine(JSONObject orderJson, JSONObject lineJson) throws JSONException {
    // verify cancelled line exists in db
    if (orderJson.has("isCanceledOrder") && orderJson.getBoolean("isCanceledOrder")) {
      validateCancelLine(lineJson);
      final OrderLine orderLine = OBDal.getInstance().get(OrderLine.class, lineJson.get("id"));
      if (orderLine == null) {
        throw new OBException(
            "Cancelled order line with id " + lineJson.get("id") + " does not exists");
      }
    }

    setProduct(lineJson);

    if (!lineJson.has("id")) {
      lineJson.put("id", SequenceIdData.getUUID());
    }
    lineJson.put("orderStep", orderJson.get("step"));

    if (lineJson.has("orderedQuantity")) {
      copyPropertyValue(lineJson, "orderedQuantity", "qty");
    } else if (lineJson.has("quantity")) {
      copyPropertyValue(lineJson, "quantity", "qty");
    }

    if (lineJson.has("warehouse")) {
      final String warehouseId = resolveJsonValue(Warehouse.ENTITY_NAME,
          lineJson.getString("warehouse"), new String[] { "id", "name", "searchKey" });
      final JSONObject whJson = new JSONObject();
      whJson.put("id", warehouseId);
      lineJson.put("warehouse", whJson);
    }

    final Boolean isReturn = orderJson.optBoolean("isReturn", false);
    final Boolean isLineQtyNegative = lineJson.getDouble("qty") < 0;
    if ((isReturn || isLineQtyNegative) && !lineJson.has("originalOrderLineId")
        && lineJson.has("originalSalesOrderDocumentNumber")) {
      final BigDecimal qtyReturned = BigDecimal.valueOf(lineJson.getDouble("qty")).negate();
      // getOriginalOrderLineId based on originalSalesOrderDocumentNumber and productId set by
      // setProduct using getProductIdFromJson
      String strOriginalSalesOrderDocumentNumber = lineJson
          .getString("originalSalesOrderDocumentNumber");
      String strProductId = lineJson.getJSONObject("product").getString("id");

      //@formatter:off
      final String hql =
          " select ol.id as originalOrderLineId "
        + " from OrderLine as ol join ol.salesOrder as o "
        + " where o.documentNo = :documentNo "
        + " and ol.product.id = :productId "
        + " and o.processed = true "
        + " and o.documentStatus <> 'VO'"
        + " order by ol.orderedQuantity asc, ol.id ";
      //@formatter:on

      final Query<String> originalOrderLineQuery = OBDal.getInstance()
          .getSession()
          .createQuery(hql, String.class)
          .setParameter("documentNo", strOriginalSalesOrderDocumentNumber)
          .setParameter("productId", strProductId);

      for (String originalOrderLineId : originalOrderLineQuery.list()) {
        if (isOriginalOrderLineValidForReturn(originalOrderLineId, qtyReturned)) {
          lineJson.put("originalOrderLineId", originalOrderLineId);
          break;
        }
      }
    }

    if (lineJson.has("returnReason")) {
      final String returnReasonId = resolveJsonValue(ReturnReason.ENTITY_NAME,
          lineJson.getString("returnReason"), new String[] { "id", "name", "searchKey" });
      lineJson.put("returnReason", returnReasonId);
    }

    if (!lineJson.has("promotions")) {
      lineJson.put("promotions", new JSONArray());
    } else {
      transformPromotions(lineJson.getJSONArray("promotions"));
    }

    writePropertyValue(lineJson, "promotionMessages", new JSONArray());
    writePropertyValue(lineJson, "promotionCandidates", new JSONArray());

    if (lineJson.has("taxLines")) {
      transformTaxes(lineJson.getJSONObject("taxLines"));
    } else if (lineJson.has("taxes")) {
      // Transform order array to object
      JSONArray lineTaxes = lineJson.optJSONArray("taxes");
      if (lineTaxes != null) {
        JSONObject taxLines = new JSONObject();
        for (int i = 0; i < lineTaxes.length(); i++) {
          JSONObject lineTax = lineTaxes.getJSONObject(i);
          taxLines.put(lineTax.getString("tax"), lineTax);
        }
        lineJson.put("taxLines", taxLines);
      } else {
        lineJson.put("taxLines", lineJson.getJSONObject("taxes"));
      }
      transformTaxes(lineJson.getJSONObject("taxLines"));
    } else {
      lineJson.put("taxLines", new JSONObject());
    }

    setLineTaxInformation(lineJson);
    transformPriceInformation(lineJson);
  }

  private void setQuantitiesToDeliver(JSONObject orderJson) throws JSONException {
    final String step = orderJson.getString("step");
    for (int i = 0; i < orderJson.getJSONArray("lines").length(); i++) {
      setQuantityToDeliver(orderJson, orderJson.getJSONArray("lines").getJSONObject(i), step);
    }
  }

  private void setQuantityToDeliver(JSONObject orderJson, JSONObject lineJson, String step)
      throws JSONException {
    if (CREATE.equals(step) || PAY.equals(step) || CANCEL_REPLACE.equals(step)) {
      if (lineJson.has("shippedQuantity")) {
        copyPropertyValue(lineJson, "shippedQuantity", "deliveredQuantity");
      }
      if (lineJson.has("deliveredQuantity")) {
        copyPropertyValue(lineJson, "deliveredQuantity", "obposQtytodeliver");
      } else if (!lineJson.has("obposQtytodeliver")) {
        lineJson.put("obposQtytodeliver", 0);
      }
    } else {
      copyPropertyValue(lineJson, "qty", "obposQtytodeliver");
    }
    if (orderJson.optBoolean("completeTicket") || orderJson.optBoolean("payOnCredit")) {
      lineJson.put("obposIspaid", true);
    }
  }

  protected void handleRelatedLines(JSONObject orderJson) throws JSONException {
    for (int i = 0; i < orderJson.getJSONArray("lines").length(); i++) {
      final JSONObject lineJson = orderJson.getJSONArray("lines").getJSONObject(i);
      if (!lineJson.has("relatedLines")) {
        continue;
      }
      final JSONArray relatedLines = lineJson.getJSONArray("relatedLines");
      for (int j = 0; j < relatedLines.length(); j++) {
        final JSONObject relatedLine = relatedLines.getJSONObject(j);
        // if no orderLineId then hopefully there is a product..
        if (relatedLine.has("orderlineId")) {
          // no need, already pointing to an orderline
          continue;
        }
        final String productId = getProductIdFromJson(relatedLine);
        // now search any of the lines if they have a product with the same id
        String relatedOrderLineId = null;
        for (int k = 0; k < orderJson.getJSONArray("lines").length(); k++) {
          final JSONObject checkLineJson = orderJson.getJSONArray("lines").getJSONObject(k);
          final JSONObject product = checkLineJson.getJSONObject("product");
          if (productId.equals(product.getString("id"))) {
            relatedOrderLineId = checkLineJson.getString("id");
            break;
          }
        }
        if (relatedOrderLineId == null) {
          throw new OBException("Related line information can't be resolved, line " + relatedLine
              + " of order line " + lineJson + " of order " + orderJson);
        }
        relatedLine.put("orderlineId", relatedOrderLineId);
      }
    }
  }

  protected void transformPriceInformation(JSONObject lineJson) throws JSONException {

    if (!lineJson.has("priceIncludesTax")) {
      lineJson.put("priceIncludesTax", false);
    }
    writePropertyValue(lineJson, "netListPrice",
        lineJson.getDouble("netAmount") / lineJson.getDouble("qty"));
    writePropertyValue(lineJson, "grossListPrice",
        lineJson.getDouble("grossAmount") / lineJson.getDouble("qty"));
    writePropertyValue(lineJson, "netPrice",
        lineJson.getDouble("netAmount") / lineJson.getDouble("qty"));
    writePropertyValue(lineJson, "grossUnitPrice",
        lineJson.getDouble("grossAmount") / lineJson.getDouble("qty"));
    writePropertyValue(lineJson, "baseGrossUnitPrice",
        lineJson.getDouble("grossAmount") / lineJson.getDouble("qty"));
    copyPropertyValue(lineJson, "netAmount", "discountedNet");
    copyPropertyValue(lineJson, "netPrice", "discountedLinePrice");
    writePropertyValue(lineJson, "discountPercentage",
        lineJson.has("discount") ? lineJson.getDouble("discount") : 0);
    copyPropertyValue(lineJson, "netListPrice", "listPrice");
    copyPropertyValue(lineJson, "grossListPrice", "priceList");
    copyPropertyValue(lineJson, "listPrice", "standardPrice");
    copyPropertyValue(lineJson, "netPrice", "unitPrice");
    copyPropertyValue(lineJson, "netAmount", "net");
    copyPropertyValue(lineJson, "grossAmount", "gross");
    if (CANCEL.equals(lineJson.getString("orderStep"))) {
      lineJson.put("lineGrossAmount", BigDecimal.valueOf(lineJson.getDouble("grossAmount")).abs());
    } else {
      copyPropertyValue(lineJson, "grossAmount", "lineGrossAmount");
    }
  }

  protected String convertToUTCDate(String dateStr, int timezoneOffset) {
    final SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final SimpleDateFormat dateFormatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    try {
      Date parsedDate = new Timestamp(dateFormatWithTime.parse(dateStr).getTime());
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(parsedDate);
      calendar.add(Calendar.MINUTE, timezoneOffset);
      return dateFormatISO.format(calendar.getTime());
    } catch (ParseException e) {
      log.error("Error parsing Date", e);
      return null;
    }
  }

  protected void transformPayments(JSONObject orderJson) throws JSONException {
    final JSONArray payments = orderJson.getJSONArray("payments");
    final OBPOSApplications posTerminal = getPOSTerminal(orderJson);
    for (int i = 0; i < payments.length(); i++) {
      final JSONObject payment = payments.getJSONObject(i);
      if (posTerminal != null) {
        payment.put("oBPOSPOSTerminal", posTerminal.getId());
        if (orderJson.has("obposAppCashup")) {
          payment.put("obposAppCashup", orderJson.get("obposAppCashup"));
        }
      }
      if (payment.has("paymentDate")) {
        String paymentDateStr = convertToUTCDate(payment.getString("paymentDate"),
            orderJson.getInt("timezoneOffset"));
        if (paymentDateStr != null) {
          payment.put("paymentDate", paymentDateStr);
        }
      }
      if (!payment.has("currency")) {
        payment.put("currency", orderJson.get("currency"));
      }
      if (!payment.has("rate")) {
        payment.put("rate", 1.0);
      }

      // check payment kind
      boolean found = false;

      if (!payment.has("kind")) {
        for (OBPOSAppPayment paymentType : posTerminal.getOBPOSAppPaymentList()) {
          if (paymentType.getPaymentMethod()
              .getPaymentMethod()
              .getName()
              .equals(payment.getString("name"))) {
            payment.put("kind", paymentType.getSearchKey());
            found = true;
            break;
          }
        }
      } else {
        for (OBPOSAppPayment paymentType : posTerminal.getOBPOSAppPaymentList()) {
          if (paymentType.getSearchKey().equals(payment.getString("kind"))) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        throw new OBException("Value " + payment.get("kind")
            + " does not resolve to a payment type for terminal " + posTerminal.getSearchKey());
      }
      transformPayment(payment);
    }

    // now use the data to fill the payment field correctly in the json
    if (!orderJson.has("payment") || -1 == orderJson.getInt("payment")) {
      final Currency currency = getCurrency(orderJson.getString("currency"));
      int pricePrecision = currency.getObposPosprecision() == null
          ? currency.getPricePrecision().intValue()
          : currency.getObposPosprecision().intValue();

      BigDecimal paid = BigDecimal.ZERO;
      if (orderJson.has("id")) {
        final Order order = OBDal.getInstance().get(Order.class, orderJson.get("id"));
        if (order != null) {
          for (FIN_PaymentSchedule paymentSchedule : order.getFINPaymentScheduleList()) {
            paid = paid.add(paymentSchedule.getPaidAmount());
          }
        }
      }

      // Skip new payment for cancelled order
      if (!orderJson.has("isCanceledOrder") || !orderJson.getBoolean("isCanceledOrder")) {
        for (int i = 0; i < payments.length(); i++) {
          final JSONObject payment = payments.getJSONObject(i);

          BigDecimal amount = BigDecimal.valueOf(payment.getDouble("origAmount"))
              .setScale(pricePrecision, RoundingMode.HALF_UP);
          BigDecimal origAmount = amount;
          BigDecimal mulrate = new BigDecimal(1);
          // FIXME: Coversion should be only in one direction: (USD-->EUR)
          if (payment.has("mulrate") && payment.getDouble("mulrate") != 1) {
            mulrate = BigDecimal.valueOf(payment.getDouble("mulrate"));
            if (payment.has("amount")) {
              origAmount = BigDecimal.valueOf(payment.getDouble("amount"))
                  .setScale(pricePrecision, RoundingMode.HALF_UP);
            } else {
              origAmount = amount.multiply(mulrate).setScale(pricePrecision, RoundingMode.HALF_UP);
            }
          }
          paid = paid.add(origAmount);
        }
      }
      orderJson.put("payment", paid.doubleValue());
      BigDecimal gross = BigDecimal.valueOf(orderJson.getDouble("gross")).abs();
      BigDecimal paidAmt = BigDecimal.valueOf(orderJson.getDouble("payment")).abs();
      // Add nettingPayment for cancelLayaway
      if (orderJson.has("cancelLayaway") || orderJson.getBoolean("cancelLayaway")) {
        orderJson.put("nettingPayment", (gross.subtract(paidAmt)).negate());
      } else {
        boolean fullyPaid = BigDecimal.ZERO.compareTo(gross) != 0 && gross.compareTo(paidAmt) != 1;
        if (fullyPaid) {
          orderJson.put("completeTicket", true);
        }
      }
    } else {
      orderJson.put("completeTicket", true);
    }
  }

  protected Currency getCurrency(String isoCode) {
    final OBQuery<Currency> qry = OBDal.getInstance()
        .createQuery(Currency.class, Currency.PROPERTY_ISOCODE + "=:isoCode or id=:id");
    qry.setNamedParameter("isoCode", isoCode);
    qry.setNamedParameter("id", isoCode);
    // copy the list to only execute the query once
    final ArrayList<Currency> result = new ArrayList<Currency>(qry.list());
    if (result.isEmpty()) {
      throw new OBException("No currency found using isocode " + isoCode);
    }
    return result.get(0);
  }

  protected void transformPayment(JSONObject payment) throws JSONException {

    validatePayment(payment);

    copyPropertyValue(payment, "paidAmount", "amount");
    final boolean hasOriginalAmount = payment.has("origAmount");
    copyPropertyValue(payment, "paidAmount", "origAmount");
    copyPropertyValue(payment, "paidAmount", "paid");
    final Object rate = payment.get("rate");
    if (!(rate instanceof String)) {
      payment.put("rate", rate.toString());
    }
    if (!payment.has("mulrate")) {
      final BigDecimal rateBD = new BigDecimal(payment.getString("rate"));
      payment.put("mulrate", BigDecimal.ONE.divide(rateBD).toPlainString());
    } else {
      final Object mulRate = payment.get("mulrate");
      if (!(mulRate instanceof String)) {
        payment.put("mulrate", mulRate.toString());
      }
    }
    if (!hasOriginalAmount) {
      payment.put("origAmount",
          new BigDecimal(payment.getString("mulrate"))
              .multiply(new BigDecimal(payment.getDouble("origAmount")))
              .doubleValue());
    }
    if (!payment.has("date")) {
      if (payment.has("paymentDate")) {
        payment.put("date", payment.getString("paymentDate"));
      } else {
        payment.put("date", JsonUtils.createDateTimeFormat().format(new Date()));
      }
    }
    payment.put("isocode", payment.getString("currency"));
  }

  protected void validateHeader(JSONObject json) {
    final String msg = "Checking order line: ";
    check(json, "netAmount", msg);
    check(json, "grossAmount", msg);
    check(json, "payment", msg);
    check(json, "currency", msg);
  }

  protected void validateLine(JSONObject json) {
    final String msg = "Checking order line: ";
    check(json, "qty", msg);
    check(json, "product", msg);
    check(json, "netAmount", msg);
    check(json, "grossAmount", msg);
  }

  protected void validateCancelHeader(JSONObject json) {
    final String msg = "Checking cancelled order header: ";
    check(json, "id", msg);
    check(json, "netAmount", msg);
    check(json, "grossAmount", msg);
    check(json, "currency", msg);
  }

  protected void validateCancelLine(JSONObject json) {
    final String msg = "Checking cancelled order line: ";
    check(json, "id", msg);
    check(json, "qty", msg);
    check(json, "product", msg);
    check(json, "netAmount", msg);
    check(json, "grossAmount", msg);
  }

  protected void validatePayment(JSONObject json) throws JSONException {
    final String msg = "Checking payment: ";
    check(json, "paidAmount", msg);
    check(json, "kind", msg);
    check(json, "rate", msg);
    check(json, "currency", msg);
  }

  protected void validateTax(JSONObject json) {
    final String msg = "Checking tax: ";
    check(json, "taxAmount", msg);
    check(json, "netAmount", msg);
  }

  protected void validatePromotion(JSONObject json) {
    final String msg = "Checking promotion: ";
    check(json, "discountRule", msg);
    check(json, "quantity", msg);
    check(json, "amount", msg);
    check(json, "unitDiscount", msg);
  }

  protected void check(JSONObject json, String property, String message) {
    if (!json.has(property)) {
      throw new OBException(message + " property not found " + property + " on json " + json);
    }
  }

  protected void setLineTaxInformation(JSONObject lineJson) throws JSONException {
    // "tax": "5235D8E99A2749EFA17A5C92A52AEFC6",
    // "taxAmount": 6.33,
    // "lineRate": 1.21
    final JSONObject taxes = lineJson.getJSONObject("taxLines");
    final JSONArray names = taxes.names();
    BigDecimal totalTax = BigDecimal.ZERO;
    for (int i = 0; i < names.length(); i++) {
      final String name = names.getString(i);
      final JSONObject taxInfo = taxes.getJSONObject(name);
      if (i == 0) {
        lineJson.put("tax", name);
      }
      if (!lineJson.has("lineRate") && taxInfo.has("rate")) {
        lineJson.put("lineRate", (double) ((100.0 + taxInfo.getInt("rate")) / 100.0));
      }
      totalTax = totalTax.add(new BigDecimal(taxInfo.getDouble("amount")));
    }
    if (!lineJson.has("taxAmount")) {
      lineJson.put("taxAmount", totalTax.doubleValue());
    }
  }

  protected void transformPromotions(JSONArray promotionsJson) throws JSONException {
    for (int i = 0; i < promotionsJson.length(); i++) {
      transformPromotion(promotionsJson.getJSONObject(i));
    }
  }

  protected void transformPromotion(JSONObject promotionJson) throws JSONException {
    if (promotionJson.has("amountPerUnit")) {
      copyPropertyValue(promotionJson, "amountPerUnit", "unitDiscount");
    }
    if (promotionJson.has("totalAmount")) {
      copyPropertyValue(promotionJson, "totalAmount", "amount");
    }
    validatePromotion(promotionJson);

    if (promotionJson.has("name")) {
      copyPropertyValue(promotionJson, "name", "identifier");
    }
    copyPropertyValue(promotionJson, "amount", "amt");
    copyPropertyValue(promotionJson, "amount", "fullAmt");
    copyPropertyValue(promotionJson, "amount", "displayedTotalAmount");
    promotionJson.put("ruleId", resolveJsonValue(PriceAdjustment.ENTITY_NAME,
        promotionJson.getString("discountRule"), new String[] { "id", "name", "printName" }));
    copyPropertyValue(promotionJson, "quantity", "obdiscQtyoffer");
    copyPropertyValue(promotionJson, "quantity", "qtyOffer");
    copyPropertyValue(promotionJson, "quantity", "qtyOfferReserved");
    copyPropertyValue(promotionJson, "quantity", "pendingQtyOffer");
    copyPropertyValue(promotionJson, "quantity", "obdiscQtyoffer");
  }

  protected void setProduct(JSONObject lineJson) throws JSONException {
    final Product product = OBDal.getInstance().get(Product.class, getProductIdFromJson(lineJson));
    final DataToJsonConverter jsonConverter = new DataToJsonConverter();
    final JSONObject productJson = jsonConverter.toJsonObject(product, DataResolvingMode.FULL);
    lineJson.put("product", productJson);
    if (lineJson.has("uOM")) {
      lineJson.put("uom", lineJson.getString("uOM"));
    }
    if (lineJson.has("uom")) {
      lineJson.put("uOM", resolveJsonValue(UOM.ENTITY_NAME, lineJson.getString("uom"),
          new String[] { "id", "name", "eDICode", "symbol" }));
    } else {
      lineJson.put("uOM", product.getUOM().getId());
    }
  }

  protected String getProductIdFromJson(JSONObject json) throws JSONException {
    String productId = resolveJsonValueNoException(Product.ENTITY_NAME, json.getString("product"),
        true, new String[] { "id", "searchKey", "name", "uPCEAN" });
    if (productId == null) {
      productId = findProductIdForLine(json);
    }
    if (productId == null) {
      throw new OBException("No product could be found for the orderline " + json);
    }
    return productId;
  }

  /**
   * Can be overridden by subclass to add other specific ways of finding a product. By default
   * returns null. Is only called if a product could not be found using the standard approach of
   * searching by id, searchKey, name, etc.
   * 
   * @param lineJson
   *          the orderline json
   * @return the product id
   */
  protected String findProductIdForLine(JSONObject lineJson) {
    return null;
  }

  protected void transformTaxes(JSONObject taxes) throws JSONException {
    final JSONArray names = taxes.names();
    for (int i = 0; i < names.length(); i++) {
      final String name = names.getString(i);
      final JSONObject taxValue = taxes.getJSONObject(name);

      if (taxValue.has("taxableAmount")) {
        copyPropertyValue(taxValue, "taxableAmount", "netAmount");
      }

      validateTax(taxValue);
      if (!taxValue.has("rate") && taxValue.getDouble("net") > 0) {
        taxValue.put("rate", taxValue.getDouble("amount") / taxValue.getDouble("net"));
      }
      copyPropertyValue(taxValue, "taxAmount", "amount");
      copyPropertyValue(taxValue, "netAmount", "net");

      taxes.remove(name);
      final String taxId = resolveJsonValue(TaxRate.ENTITY_NAME, name,
          new String[] { "id", "name", "taxSearchKey" });
      if (taxId == null) {
        throw new OBException("Tax " + name + " can not be translated to a tax " + taxes);
      }
      taxes.put(taxId, taxValue);
    }
  }

  protected void setOrderType(JSONObject orderJson) throws JSONException {
    if (orderJson.getBoolean("isReturn")) {
      orderJson.put("orderType", 1l);
    } else if (orderJson.getBoolean("isLayaway")) {
      // orderJson.put("orderType", 2l);
      orderJson.put("orderType", 0l);
    } else if (orderJson.getBoolean("cancelLayaway")) {
      orderJson.put("orderType", 3l);
    } else {
      orderJson.put("orderType", 0l);
    }
  }

  protected void setDefaults(JSONObject orderJson) throws JSONException {
    final JSONObject defaults = createOrderDefaults(orderJson);
    final JSONArray names = defaults.names();
    for (int i = 0; i < names.length(); i++) {
      final String name = names.getString(i);
      if (!orderJson.has(name)) {
        orderJson.put(name, defaults.get(name));
      }
    }

    // Update IDs to Upper case
    if (orderJson.has("id")) {
      orderJson.put("id", orderJson.getString("id").toUpperCase());
    }
    for (int i = 0; i < orderJson.getJSONArray("lines").length(); i++) {
      JSONObject jsonOrderLine = orderJson.getJSONArray("lines").getJSONObject(i);
      if (jsonOrderLine.has("id")) {
        jsonOrderLine.put("id", jsonOrderLine.getString("id").toUpperCase());
      }
    }
  }

  protected JSONObject createOrderDefaults(JSONObject orderJson) throws JSONException {
    final JSONObject defaultJson = new JSONObject();
    defaultJson.put("isQuotation", false);
    defaultJson.put("isLayaway", false);
    defaultJson.put("isReturn", false);
    defaultJson.put("cancelLayaway", false);
    defaultJson.put("obposAppCashup", "-1");
    defaultJson.put("created", new Date().getTime());
    defaultJson.put("approvals", new JSONArray());
    defaultJson.put("lines", new JSONArray());
    defaultJson.put("payments", new JSONArray());
    defaultJson.put("taxes", new JSONObject());
    defaultJson.put("change", 0);
    defaultJson.put("timezoneOffset", 0);
    defaultJson.put("generateInvoice", false);

    final SimpleDateFormat dtFormat = createOrderLoaderDateTimeFormat();
    if (!orderJson.has("orderDate")) {
      defaultJson.put("orderDate", dtFormat.format(new Date()));
    }
    defaultJson.put("creationDate", dtFormat.format(new Date()));
    defaultJson.put("obposCreatedabsolute", dtFormat.format(new Date()));

    return defaultJson;
  }

  protected void setDocumentNo(JSONObject orderJson) throws JSONException {
    if (!orderJson.has("documentNo")) {
      if (orderJson.has("cancelLayaway") && orderJson.getBoolean("cancelLayaway")) {
        // set documentNo from cancelledOrder
        if (orderJson.has("orderid")) {
          final Order order = OBDal.getInstance().get(Order.class, orderJson.get("orderid"));
          if (order != null) {
            orderJson.put("documentNo", order.getDocumentNo() + "*R*");
          } else {
            throw new OBException(
                "orderid in cancelLayaway does not exists for order json " + orderJson);
          }
        } else {
          throw new OBException(
              "orderid attribute is missing for the cancelLayaway for order json " + orderJson);
        }
      } else {
        OBPOSApplications posTerminal = getPOSTerminal(orderJson);
        if (posTerminal != null) {
          Long currentNo = (long) 0;
          // The record will be locked to this process until it ends.
          Query<OBPOSApplications> terminalQuery = OBDal.getInstance()
              .getSession()
              .createQuery("from OBPOS_Applications where id=:terminalId", OBPOSApplications.class);
          terminalQuery.setParameter("terminalId", posTerminal.getId());
          terminalQuery.setLockOptions(LockOptions.UPGRADE);
          OBPOSApplications lockedTerminal = terminalQuery.uniqueResult();
          OBDal.getInstance().getSession().evict(lockedTerminal);
          lockedTerminal = OBDal.getInstance().get(OBPOSApplications.class, lockedTerminal.getId());
          if (lockedTerminal.getLastassignednum() != null) {
            currentNo = lockedTerminal.getLastassignednum() + 1;
          } else {
            currentNo++;
          }
          lockedTerminal.setLastassignednum(currentNo);
          orderJson.put("documentNo",
              lockedTerminal.getOrderdocnoPrefix() + "/" + String.format("%07d", currentNo));
        } else {
          final String documentNo = getDocumentNo(
              ModelProvider.getInstance().getEntity(Order.ENTITY_NAME), null,
              OBDal.getInstance().get(DocumentType.class, orderJson.getString("documentType")));
          orderJson.put("documentNo", documentNo);
        }
      }
    }
  }

  protected void setBusinessPartnerInformation(JSONObject orderJson) throws JSONException {

    final OBPOSApplications posTerminal = getPOSTerminal(orderJson);

    String bpId = resolveJsonValue(BusinessPartner.ENTITY_NAME,
        orderJson.getString("businessPartner"), new String[] { "id", "searchKey", "name" });

    if (bpId == null) {
      if (posTerminal.getDefaultCustomer() != null) {
        bpId = posTerminal.getDefaultCustomer().getId();
      }
    }
    if (bpId == null && posTerminal.getOrganization().getObretcoCBpartner() != null) {
      bpId = posTerminal.getOrganization().getObretcoCBpartner().getId();
    }

    if (bpId == null) {
      throw new OBException("No customer info can be determined " + orderJson);
    }

    final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
    final DataToJsonConverter jsonConverter = new DataToJsonConverter();
    final JSONObject bpJson = jsonConverter.toJsonObject(bp, DataResolvingMode.FULL);
    String addressId = null;
    String shipAddressId = null;
    JSONObject locationDetails;
    String locationId = null;
    if (orderJson.has("invoiceAddress")) {
      locationDetails = orderJson.optJSONObject("invoiceAddress");
      if (locationDetails != null && locationDetails.has("name")) {
        locationId = getAddressIdFromAddressName(bpId, locationDetails.getString("name"));
        if (locationId != null) {
          orderJson.put("locId", locationId);
        }
      } else {
        copyPropertyValue(orderJson, "invoiceAddress", "locId");
      }
      // Removing invoiceAddress as in OrderLoader error occurs in fillBobFromJSON as id property is
      // not available to getReferencedProperty
      orderJson.remove("invoiceAddress");
    }
    if (orderJson.has("shipmentAddress")) {
      locationDetails = orderJson.optJSONObject("shipmentAddress");
      if (locationDetails != null && locationDetails.has("name")) {
        locationId = getAddressIdFromAddressName(bpId, locationDetails.getString("name"));
        if (locationId != null) {
          orderJson.put("shipLocId", locationId);
        }
      } else {
        copyPropertyValue(orderJson, "shipmentAddress", "shipLocId");
      }
      orderJson.remove("shipmentAddress");
    }

    if (orderJson.has("locId") && orderJson.has("shipLocId")) {
      addressId = orderJson.getString("locId");
      shipAddressId = orderJson.getString("shipLocId");
    } else if (orderJson.has("locId") && !orderJson.has("shipLocId")) {
      addressId = orderJson.getString("locId");
      shipAddressId = addressId;
    } else if (!orderJson.has("locId") && orderJson.has("shipLocId")) {
      throw new OBException("Invoice location is missing for bp " + bpId + " for order json "
          + orderJson + " while shipping location is defined");
    } else if (orderJson.has("address")) {
      addressId = resolveJsonValue(Location.ENTITY_NAME, orderJson.getString("address"),
          new String[] { "id", "name" });

      if (addressId == null) {
        addressId = getAddressIdFromBP(bpId);
      }
      shipAddressId = addressId;
    } else {
      addressId = getAddressIdFromBP(bpId);
      shipAddressId = addressId;
    }
    if (addressId == null && posTerminal.getObposCBpartnerLoc() != null) {
      addressId = posTerminal.getObposCBpartnerLoc().getId();
    }
    if (addressId == null && posTerminal.getOrganization().getObretcoCBpLocation() != null) {
      addressId = posTerminal.getOrganization().getObretcoCBpLocation().getId();
    }
    if (addressId == null) {
      throw new OBException(
          "No address information found for bp " + bpId + " for order json " + orderJson);
    }
    bpJson.put("locId", addressId);
    bpJson.put("shipLocId", shipAddressId);
    orderJson.put("bp", bpJson);

  }

  protected void setWarehouse(JSONObject orderJson) throws JSONException {

    final OBPOSApplications posTerminal = getPOSTerminal(orderJson);

    if (orderJson.has("warehouse")) {
      orderJson.put("warehouse", resolveJsonValue(Warehouse.ENTITY_NAME,
          orderJson.getString("warehouse"), new String[] { "id", "name", "searchKey" }));
    } else {
      Warehouse wh = posTerminal.getOrganization().getObretcoMWarehouse();
      wh = null;
      if (wh == null) {
        OBQuery<OrgWarehouse> orgWarehouses = OBDal.getInstance()
            .createQuery(OrgWarehouse.class, " e where e.organization=:org and "
                + "e.active=true and e.warehouse.active=true order by priority, e.warehouse.name");
        orgWarehouses.setNamedParameter("org", posTerminal.getOrganization());
        orgWarehouses.setMaxResult(1);
        OrgWarehouse orgWarehouse = orgWarehouses.uniqueResult();
        if (orgWarehouse != null) {
          wh = orgWarehouse.getWarehouse();
        }
      }
      if (wh != null) {
        orderJson.put("warehouse", wh.getId());
      } else {
        throw new OBException(posTerminal.getOrganization().getName()
            + " organization does not have a defined warehouse", true);
      }
    }

  }

  protected void setClientOrg(JSONObject orderJson) throws JSONException {
    final OBPOSApplications posTerminal = getPOSTerminal(orderJson);
    if (!orderJson.has("organization")) {
      orderJson.put("trxOrganization", posTerminal.getOrganization().getId());
      orderJson.put("organization", posTerminal.getOrganization().getId());
      orderJson.put("client", posTerminal.getClient().getId());
    }

  }

  protected String getAddressIdFromAddressName(String bpId, String locName) {
    String bpLocationId = null;
   // @formatter:off
    String qryStr =
        "select bpl.id"
      + "  from BusinessPartnerLocation bpl"
      + "  where bpl.businessPartner.id = :bpId"
      + "  and bpl.name = :bpName" 
      + "  order by bpl.creationDate desc";
    // @formatter:on

    final Query<String> qry = OBDal.getInstance()
        .getSession()
        .createQuery(qryStr, String.class)
        .setParameter("bpId", bpId)
        .setParameter("bpName", locName);

    final List<String> values = qry.list();
    if (values.size() > 1) {
      bpLocationId = values.get(0);
    }
    return bpLocationId;
  }

  protected String getAddressIdFromBP(String bpId) {
    final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
    for (Location location : bp.getBusinessPartnerLocationList()) {
      if (location.isActive() && location.isInvoiceToAddress()) {
        return location.getId();
      }
    }
    return null;
  }

  protected void setDocumentType(JSONObject orderJson, OBPOSApplications posTerminal)
      throws JSONException {
    if (orderJson.has("documentType")) {
      orderJson.put("documentType", resolveJsonValue(DocumentType.ENTITY_NAME,
          orderJson.getString("documentType"), new String[] { "id", "name" }));
      return;
    }
    Organization organization = posTerminal.getOrganization();
    if (orderJson.has("isQuotation") && orderJson.getBoolean("isQuotation")) {
      orderJson.put("documentType", organization.getObposCDoctypequot().getId());
    } else if (orderJson.has("isReturn") && orderJson.getBoolean("isReturn")) {
      orderJson.put("documentType", organization.getObposCDoctyperet().getId());
    } else {
      orderJson.put("documentType", organization.getObposCDoctype().getId());
    }
  }

  protected OBPOSApplications getPOSTerminal(JSONObject jsonObject) throws JSONException {
    if (getImportEntryId() != null) {
      ImportEntry entry = OBDal.getInstance().get(ImportEntry.class, getImportEntryId());
      if (entry != null && entry.getOBPOSPOSTerminal() != null) {
        return entry.getOBPOSPOSTerminal();
      }
    }
    if (jsonObject.has("terminal")) {
      copyPropertyValue(jsonObject, "terminal", "posTerminal");
    }
    if (!jsonObject.has("posTerminal")) {
      new OBException("Property posTerminal not found in json " + jsonObject);
    }
    final String posId = resolveJsonValueWithoutOrgFilter(OBPOSApplications.ENTITY_NAME,
        jsonObject.getString("posTerminal"), new String[] { "id", "name", "searchKey" });
    final OBPOSApplications result = OBDal.getInstance().get(OBPOSApplications.class, posId);

    if (result == null) {
      throw new OBException("No pos terminal found using id " + posId + " json " + jsonObject);
    }

    String orgId = result.getOrganization().getId();
    try {
      if ("Y"
          .equals(Preferences.getPreferenceValue("OBPOS_ExternalOrderLoaderCrossStoreOrg", true,
              null, null, OBContext.getOBContext().getUser(), null, null))
          && result.getOrganization().getOBRETCOCrossStoreOrganization() != null) {
        orgId = result.getOrganization().getOBRETCOCrossStoreOrganization().getId();
      }
    } catch (PropertyException e) {
      log.error("Error while reading OBPOS_ExternalOrderLoaderCrossStoreOrg preference");
    }
    OBContext.setOBContext(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getRole().getId(), result.getClient().getId(), orgId);

    final Role role = OBDal.getInstance()
        .get(Role.class, OBContext.getOBContext().getRole().getId());
    if (!role.isWebServiceEnabled()) {
      throw new OBException(
          "Webservice is not enabled for the user " + OBContext.getOBContext().getUser().getName());
    }
    return result;
  }

  /**
   * Search for the id of referenced entity by querying the entities for each of its properties
   * (passed as a parameter) using the searchValue
   * 
   * @param entityName
   *          the entity to search for
   * @param searchValue
   *          the value to search with
   * @param properties
   *          the properties of the entity which are one-by-one used to query
   */
  protected String resolveJsonValue(String entityName, String searchValue, String... properties) {
    final String id = resolveJsonValueNoException(entityName, searchValue, true, properties);
    if (id == null) {
      throw new OBException(
          "Value " + searchValue + " does not resolve to an instance of " + entityName);
    }
    return id;
  }

  protected String resolveJsonValueWithoutOrgFilter(String entityName, String searchValue,
      String... properties) {
    final String id = resolveJsonValueNoException(entityName, searchValue, false, properties);
    if (id == null) {
      throw new OBException(
          "Value " + searchValue + " does not resolve to an instance of " + entityName);
    }
    return id;
  }

  /**
   * Same as {@link #resolveJsonValue(String, String, String...)}, except will return null if the
   * value does not resolve to an instance and will not throw an exception in that case.
   */
  private String resolveJsonValueNoException(String entityName, String searchValue,
      boolean addOrgFilter, String... properties) {

    // the deprecated resolve will not resolve the value by default,
    // only an overriding class may do something
    for (String property : properties) {
      String id = resolve(entityName, property, searchValue);
      if (id != null) {
        return id;
      }
    }

    // not found in normal way, use the dataresolvers
    for (DataResolver dataResolver : getDataResolvers()) {
      dataResolver.setOrgFilter(addOrgFilter);
      String id = dataResolver.resolveJsonValue(entityName, searchValue, properties);
      if (id != null) {
        return id;
      }
    }
    return null;
  }

  private List<DataResolver> getDataResolvers() {
    if (dataResolversList == null) {
      dataResolversList = new ArrayList<DataResolver>();
      for (Iterator<DataResolver> procIter = dataResolvers.iterator(); procIter.hasNext();) {
        final DataResolver dataResolver = procIter.next();
        dataResolversList.add(dataResolver);
      }
      Collections.sort(dataResolversList, new Comparator<DataResolver>() {
        @Override
        public int compare(DataResolver o1, DataResolver o2) {
          return o1.getOrder() - o2.getOrder();
        }
      });
    }
    return dataResolversList;
  }

  /**
   * @deprecated by default this one now returns null. It is still called to provide backward
   *             compatibility for overriding classes. If not overridden then the new logic in the
   *             {@link DefaultDataResolver} is used.
   */
  @Deprecated
  protected String resolve(String entityName, String property, String value) {
    return null;
  }

  public JSONObject importOrder(JSONObject messageIn) throws ServletException {
    JSONObject transformedMns;
    JSONObject ret = new JSONObject();
    this.setRunInSynchronizedMode(true);
    try {
      transformedMns = this.transformMessage(messageIn);
      ret = this.exec(transformedMns);
    } catch (Exception e) {
      try {
        ret.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
        ret.put("result", "-1");
        ret.put("message", e.getMessage());
      } catch (JSONException e1) {
        // wont happen
      }
    }
    return ret;
  }

  public class SetOrderIDHook implements OrderLoaderHook {
    @Override
    public void exec(JSONObject jsonorder, Order order, ShipmentInOut shipment, Invoice invoice)
        throws Exception {
      jsonorder.put("_createdOrderId", order.getId());
    }
  }

  /**
   * Interface which can be implemented by modules to resolving data in different ways. Is only
   * called if the ExternalOrderLoader can not resolve the reference using its own internal logic.
   * The order by which data resolvers are called is undetermined.
   * 
   * A data resolver will be called for all types of entities. So when implementing you should only
   * handle the ones you can handle and return null in other cases.
   * 
   * @author mtaal
   */
  public static abstract class DataResolver {
    boolean addOrgFilter = true;

    public abstract String resolveJsonValue(String entityName, String searchValue,
        String... properties);

    public int getOrder() {
      return 110;
    }

    public void setOrgFilter(boolean addOrgFilter) {
      this.addOrgFilter = addOrgFilter;
    }
  }

  public static class DefaultDataResolver extends DataResolver {
    @Override
    public String resolveJsonValue(String entityName, String searchValue, String... properties) {
      for (String property : properties) {
        String id = resolve(entityName, property, searchValue);
        if (id != null) {
          return id;
        }
      }
      return null;
    }

    protected String resolve(String entityName, String property, String value) {
      try {
        // @formatter:off
        String qryStr =
            "select e.id"
          + "  from " + entityName
          + " e where e." + property + " = :value"
          + "   and e.client.id in :clients";
        // @formatter:on

        if (addOrgFilter) {
          qryStr += " and e.organization.id in (:orgs)";

          qryStr += " order by ";
          qryStr += "to_number(ad_isorgincluded(:orgId, e.organization.id, e.client.id)) asc, ";
          qryStr += "e.organization.name asc";
        }

        final Query<String> qry = OBDal.getInstance()
            .getSession()
            .createQuery(qryStr, String.class)
            .setParameter("value", value)
            .setParameterList("clients", OBContext.getOBContext().getReadableClients());
        if (addOrgFilter) {
          OrganizationStructureProvider osp = OBContext.getOBContext()
              .getOrganizationStructureProvider();
          qry.setParameterList("orgs",
              osp.getNaturalTree(OBContext.getOBContext().getCurrentOrganization().getId()));
          qry.setParameter("orgId", OBContext.getOBContext().getCurrentOrganization().getId());
        }
        final List<String> values = qry.list();
        if (values.isEmpty()) {
          return null;
        }
        return values.get(0);
      } catch (Throwable t) {
        final Throwable cause = DbUtility.getUnderlyingSQLException(t);
        log.error(cause.getMessage(), cause);
        return null;
      }
    }

    @Override
    public int getOrder() {
      return 100;
    }
  }

  /**
   * Is default handler of external orders. Is not processing the external order.
   * 
   * @author mtaal
   */
  @ImportEntryQualifier(entity = "OBPOS_ExternalOrder")
  @ApplicationScoped
  public static class ExternalOrderImportEntryProcessor extends ImportEntryProcessor {

    @Override
    protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
      return WeldUtils.getInstanceFromStaticBeanManager(ExternalOrderRunnable.class);
    }

    @Override
    protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
      return ORDERLOADER_QUALIFIER.equals(importEntryInformation.getTypeofdata());
    }

    @Override
    protected String getProcessSelectionKey(ImportEntry importEntry) {
      return importEntry.getOrganization().getId();
    }

    private static class ExternalOrderRunnable extends ImportEntryProcessRunnable {
      @Override
      public void run() {
      }

      @Override
      protected void processEntry(ImportEntry importEntry) throws Exception {
      }
    }
  }

  private Boolean isOriginalOrderLineValidForReturn(String originalOrderLineId,
      BigDecimal qtyReturned) {
    // validate Whether originalOrderLine has completely returned in others or in the current Order
    OrderLine originalOrderLine = OBDal.getInstance().get(OrderLine.class, originalOrderLineId);

    OBCriteria<ShipmentInOutLine> inOutCriteria = OBDal.getInstance()
        .createCriteria(ShipmentInOutLine.class);
    inOutCriteria
        .add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, originalOrderLine));
    Set<String> shipmentLines = new HashSet<String>();
    for (ShipmentInOutLine inOutLine : inOutCriteria.list()) {
      shipmentLines.add(inOutLine.getId());
    }

    //@formatter:off
    final String hql =
        " select coalesce(sum(ol.orderedQuantity), 0)*-1 as returnedQty "
      + " from OrderLine as ol join ol.salesOrder as o "
      + " where ol.goodsShipmentLine.id in :inOutLineIds "
      + " and o.processed = true "
      + " and o.documentStatus <> 'VO' ";
    //@formatter:on

    final Query<BigDecimal> returnedQtyInOthersQry = OBDal.getInstance()
        .getSession()
        .createQuery(hql, BigDecimal.class)
        .setParameterList("inOutLineIds", shipmentLines);
    BigDecimal returnedQuantityInOthers = returnedQtyInOthersQry.uniqueResult();
    returnedQuantityInOthers = returnedQuantityInOthers.add(qtyReturned);
    // In case same original Order line Id is to be returned in the multiple lines
    // compute the returnQuantity in same order as well as those in the Other orders

    // Sum of these both quantity should not exceed than original ordered quantity

    // Sum could be even equal to original ordered quantity taking into consideration the quantity
    // from other orders (from DB)
    // and the current order (it is not from DB but computed in this scenario )

    if (consumedOriginalOrderLineQtyInSameReturnOrder.containsKey(originalOrderLineId)) {
      BigDecimal returnQuantityInSameOrder = consumedOriginalOrderLineQtyInSameReturnOrder
          .get(originalOrderLineId);
      returnedQuantityInOthers = returnedQuantityInOthers.add(returnQuantityInSameOrder);
    }

    if (originalOrderLine.getDeliveredQuantity().compareTo(returnedQuantityInOthers) >= 0) {
      if (consumedOriginalOrderLineQtyInSameReturnOrder.containsKey(originalOrderLineId)) {
        consumedOriginalOrderLineQtyInSameReturnOrder.put(originalOrderLineId,
            consumedOriginalOrderLineQtyInSameReturnOrder.get(originalOrderLineId)
                .add(qtyReturned));
      } else {
        consumedOriginalOrderLineQtyInSameReturnOrder.put(originalOrderLineId, qtyReturned);
      }
      return true;
    }
    return returnedQuantityInOthers.compareTo(originalOrderLine.getDeliveredQuantity()) <= 0;
  }
}
