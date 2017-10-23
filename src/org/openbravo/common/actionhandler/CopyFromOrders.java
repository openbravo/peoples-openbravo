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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.UOMUtil;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action Handler to manage the Copy From Orders process
 * 
 * @author Mark
 *
 */
public class CopyFromOrders extends BaseProcessActionHandler {

  private static final Logger log = LoggerFactory.getLogger(CopyFromOrders.class);
  private static final String REQUEST_ORDER_ID = "C_Order_ID";
  private static final String REQUEST_SELECTED_ORDER_ID_FIELD = "id";
  private static final String REQUEST_ACTION_DONE = "DONE";
  private static final String REQUEST_PARAMS = "_params";
  private static final String REQUEST_GRID = "grid";
  private static final String REQUEST_SELECTION = "_selection";
  private static final String PRODUCT_ID_PARAM = "PRODUCT_ID_PARAM";
  private static final String PRICE_LIST_ID_PARAM = "PRICE_LIST_ID_PARAM";
  private static final String PRICE_LIST_VERSION_VALID_FROM_PARAM = "PRICE_LIST_VERSION_VALID_FROM_PARAM";
  private static final String DATE_FORMAT_JAVA = "dateFormat.java";

  private static final String MESSAGE = "message";
  private static final String MESSAGE_SEVERITY = "severity";
  private static final String MESSAGE_TEXT = "text";
  private static final String MESSAGE_RECORDS_COPIED = "RecordsCopied";
  private static final String MESSAGE_SUCCESS = "success";
  private static final String MESSAGE_ERROR = "error";

  private Long lastLineNo = 0L;
  private List<OrderLine> explodeOrderLines = new ArrayList<OrderLine>();
  private static final String EXPLODE_BOM_PROCESS = "DFC78024B1F54CBB95DC73425BA6687F";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    try {
      // Request Parameters
      jsonRequest = new JSONObject(content);
      final String requestedAction = getRequestedAction(jsonRequest);
      JSONArray selectedOrders = getSelectedOrders(jsonRequest);
      Order processingOrder = getProcessingOrder(jsonRequest);

      if (ifExecutedDoneActionAndThereAreSelectedOrders(requestedAction, selectedOrders)) {
        int createdOrderLinesCount = createOrderLinesFromSelectedOrders(processingOrder,
            selectedOrders);
        processExplodeOrderLines();
        jsonRequest.put(MESSAGE, getSuccessMessage(createdOrderLinesCount));
      }
    } catch (Exception e) {
      log.error("Error in CopyFromOrders Action Handler", e);

      try {
        jsonRequest.put(MESSAGE, getErrorMessage(e));
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return jsonRequest;
  }

  private String getRequestedAction(JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getString(ApplicationConstants.BUTTON_VALUE);
  }

  private boolean ifExecutedDoneActionAndThereAreSelectedOrders(final String requestedAction,
      JSONArray selectedOrders) {
    return StringUtils.equals(requestedAction, REQUEST_ACTION_DONE) && selectedOrders.length() > 0;
  }

  private JSONArray getSelectedOrders(JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getJSONObject(REQUEST_PARAMS).getJSONObject(REQUEST_GRID)
        .getJSONArray(REQUEST_SELECTION);
  }

  private Order getProcessingOrder(JSONObject jsonRequest) throws JSONException {
    return OBDal.getInstance().get(Order.class, jsonRequest.getString(REQUEST_ORDER_ID));
  }

  /**
   * Creates order lines from selected orders. Iterates all the selected orders and copies it lines
   * to processing order
   * 
   * @param order
   *          The order where the lines will be added
   * @param selectedOrders
   *          The selected orders from the lines will be copied.
   * @return The created order lines count
   * @throws JSONException
   * @throws OBException
   * @throws IOException
   * @throws ServletException
   */
  private int createOrderLinesFromSelectedOrders(Order order, JSONArray selectedOrders)
      throws JSONException, OBException, IOException, ServletException {
    // Initialize the line number with the last one in the order lines.
    lastLineNo = getMaxOrderLineNumber(order);
    int createdOrderLinesCount = 0;
    for (int index = 0; index < selectedOrders.length(); index++) {
      createdOrderLinesCount += createOrderLinesFromSelectedOrder(order,
          getSelectedOrderInPosition(selectedOrders, index));
    }
    return createdOrderLinesCount;
  }

  private Order getSelectedOrderInPosition(JSONArray selectedOrders, int index)
      throws JSONException {
    String selectedOrderId = selectedOrders.getJSONObject(index).getString(
        REQUEST_SELECTED_ORDER_ID_FIELD);
    return OBDal.getInstance().get(Order.class, selectedOrderId);
  }

  /**
   * Creates order lines from selected order. Get all the selected order lines and copies them to
   * the processing order.
   * 
   * @param order
   *          The order where the lines will be added
   * @param selectedOrder
   *          A selected order to be copied
   * @return The created order lines count
   * @throws JSONException
   * @throws OBException
   * @throws IOException
   * @throws ServletException
   */
  private int createOrderLinesFromSelectedOrder(Order order, Order selectedOrder)
      throws JSONException, OBException, IOException, ServletException {
    int createdOrderLinesCount = 0;
    for (OrderLine orderLine : selectedOrder.getOrderLineList()) {
      if (orderLine.getBOMParent() == null) {
        OrderLine newOrderLine = createLineFromSelectedOrderLine(order, orderLine);
        order.getOrderLineList().add(newOrderLine);
        OBDal.getInstance().save(newOrderLine);
        OBDal.getInstance().save(order);

        if (orderLine.isExplode()) {
          addOrderLineToExplodeList(newOrderLine);
        }

        createdOrderLinesCount++;
      }
    }
    OBDal.getInstance().flush();
    return createdOrderLinesCount;
  }

  /**
   * Creates a new order line from another one
   * 
   * @param order
   *          The order when the line will be added
   * @param orderLine
   *          The order line to be copied
   * @return The created order line
   * @throws IOException
   * @throws ServletException
   * @throws OBException
   */
  private OrderLine createLineFromSelectedOrderLine(Order order, OrderLine orderLine)
      throws IOException, ServletException, OBException {

    OrderLine newOrderLine = OBProvider.getInstance().get(OrderLine.class);

    // Always increment the lineNo when adding a new order line
    newOrderLine.setLineNo(nextLineNo());

    // Create to the new order line the reference to the order line from it is created
    updateOrderLineReference(orderLine, newOrderLine);

    // Information updated from order: org, order date, currency and warehouse
    updateInformationFromOrder(order, newOrderLine);

    // Product information and Attribute Set Instances
    updateProductAndAttributes(orderLine, newOrderLine);

    // Calculation of quantities and UOM-AUM Support
    updateQuantitiesAndUOMs(orderLine, newOrderLine);

    // Prices and amounts computation
    updatePricesAndAmounts(newOrderLine);

    // Tax computation
    updateTax(newOrderLine);

    return newOrderLine;
  }

  /**
   * Creates to the new order line the reference to the order line from it is created.
   * 
   * @param orderLine
   *          The order line to be referenced
   * @param newOrderLine
   *          The new order line
   */
  private void updateOrderLineReference(OrderLine orderLine, OrderLine newOrderLine) {
    newOrderLine.setSOPOReference(orderLine);
    newOrderLine.setSelectOrderLine(Boolean.TRUE);
  }

  /**
   * Update order line tax. Throws an exception if no taxes are found.
   * 
   * @param newOrderLine
   *          The order line where tax will be updated.
   * @throws IOException
   * @throws ServletException
   */
  private void updateTax(OrderLine newOrderLine) throws IOException, ServletException {
    TaxRate tax = OBDal.getInstance().get(TaxRate.class,
        getCurrentTaxId(newOrderLine.getSalesOrder(), newOrderLine.getProduct()));
    newOrderLine.setTax(tax);
  }

  /**
   * Updates prices and amounts. If the product has a product price in the order price list then all
   * prices and amounts will be recalculated using currency precisions and taking into account if
   * the price list includes taxes or not.
   * 
   * @param newOrderLine
   *          The order line where prices and amounts will be updated.
   */
  private void updatePricesAndAmounts(OrderLine newOrderLine) {
    BigDecimal qtyOrdered = newOrderLine.getOrderedQuantity();
    Order order = newOrderLine.getSalesOrder();
    ProductPrice productPrice = getProductPriceInPriceList(newOrderLine.getProduct(),
        order.getPriceList());

    if (productPrice != null) {
      // Standard and Price precision
      Currency orderCurrency = order.getCurrency();
      int stdPrecision = orderCurrency.getStandardPrecision().intValue();
      int pricePrecision = orderCurrency.getPricePrecision().intValue();

      // Price List, Price Standard and discount
      BigDecimal priceList = BigDecimal.ZERO;
      BigDecimal priceActual = BigDecimal.ZERO;
      BigDecimal priceLimit = productPrice.getPriceLimit().setScale(pricePrecision,
          BigDecimal.ROUND_HALF_UP);
      BigDecimal discount = BigDecimal.ZERO;

      if (productPrice.getListPrice().compareTo(BigDecimal.ZERO) != 0) {
        // Discount = ((PL-PA)/PL)*100
        priceList = productPrice.getListPrice();
        priceActual = productPrice.getStandardPrice().setScale(pricePrecision,
            BigDecimal.ROUND_HALF_UP);
        discount = priceList.subtract(priceActual).multiply(new BigDecimal("100"))
            .divide(priceList, stdPrecision, BigDecimal.ROUND_HALF_UP);
      }
      newOrderLine.setDiscount(discount);

      BigDecimal grossUnitPrice = BigDecimal.ZERO;
      BigDecimal grossAmount = BigDecimal.ZERO;

      // Processing for taxincluded
      if (order.getPriceList().isPriceIncludesTax()) {
        grossUnitPrice = priceActual;
        grossAmount = qtyOrdered.multiply(grossUnitPrice).setScale(stdPrecision,
            BigDecimal.ROUND_HALF_UP);
        priceActual = BigDecimal.ZERO;
        priceList = BigDecimal.ZERO;

        newOrderLine.setGrossUnitPrice(grossUnitPrice);
        newOrderLine.setGrossListPrice(priceList);
        newOrderLine.setBaseGrossUnitPrice(BigDecimal.ZERO);
        newOrderLine.setLineNetAmount(BigDecimal.ZERO);
        newOrderLine.setLineGrossAmount(grossAmount);
      }

      newOrderLine.setUnitPrice(priceActual);
      newOrderLine.setListPrice(priceList);
      newOrderLine.setPriceLimit(priceLimit);
      newOrderLine.setStandardPrice(priceActual);

    } else {
      newOrderLine.setUnitPrice(BigDecimal.ZERO);
      newOrderLine.setListPrice(BigDecimal.ZERO);
      newOrderLine.setPriceLimit(BigDecimal.ZERO);
      newOrderLine.setStandardPrice(BigDecimal.ZERO);
      newOrderLine.setGrossUnitPrice(BigDecimal.ZERO);
      newOrderLine.setGrossListPrice(BigDecimal.ZERO);
      newOrderLine.setBaseGrossUnitPrice(BigDecimal.ZERO);
      newOrderLine.setLineNetAmount(BigDecimal.ZERO);
      newOrderLine.setLineGrossAmount(BigDecimal.ZERO);
    }
  }

  /**
   * Updates some order line information from the order it is created. It links the order line to
   * the order and update the organization, order date, warehouse and currency from it.
   * 
   * @param order
   *          The order from it will be part
   * @param newOrderLine
   *          The order line to be updated
   */
  private void updateInformationFromOrder(Order order, OrderLine newOrderLine) {
    newOrderLine.setSalesOrder(order);
    newOrderLine.setOrganization(order.getOrganization());
    newOrderLine.setOrderDate(order.getOrderDate());
    newOrderLine.setWarehouse(order.getWarehouse());
    newOrderLine.setCurrency(order.getCurrency());
  }

  /**
   * Update the product and attribute set to the new order line
   * 
   * @param orderLine
   *          The order line to be copied
   * @param newOrderLine
   *          The order line to be updated
   */
  private void updateProductAndAttributes(OrderLine orderLine, OrderLine newOrderLine) {
    // Update the product
    newOrderLine.setProduct(orderLine.getProduct());

    // Update the attributes
    boolean isInstance = attributeSetIsInstance(orderLine.getAttributeSetValue());
    if (isInstance) {
      AttributeSetInstance newAttributeSetInstance = copyAttributeSetValue(orderLine
          .getAttributeSetValue());
      newOrderLine.setAttributeSetValue(newAttributeSetInstance);
    }
  }

  /**
   * Calculation of quantities and UOM-AUM Support
   * 
   * @param orderLine
   *          The order line to be copied
   * @param newOrderLine
   *          The order line to be updated
   */
  private void updateQuantitiesAndUOMs(OrderLine orderLine, OrderLine newOrderLine) {
    BigDecimal qtyOrdered = orderLine.getOrderedQuantity();
    BigDecimal operativeQty = orderLine.getOperativeQuantity();
    boolean isUomManagementEnabled = UOMUtil.isUomManagementEnabled();

    if (isUomManagementEnabled && orderLine.getUOM() != null && orderLine.getOperativeUOM() != null
        && orderLine.getOperativeQuantity() != null) {
      String defaultAum = UOMUtil.getDefaultAUMForDocument(orderLine.getProduct().getId(),
          newOrderLine.getSalesOrder().getTransactionDocument().getId());
      operativeQty = qtyOrdered;
      UOM aum = OBDal.getInstance().get(UOM.class, defaultAum);
      newOrderLine.setOperativeUOM(aum);

      if (!StringUtils.equals(defaultAum, orderLine.getUOM().getId())) {
        qtyOrdered = UOMUtil.getConvertedQty(orderLine.getProduct().getId(), operativeQty,
            defaultAum);
      }
    } else {
      newOrderLine.setOperativeUOM(orderLine.getOperativeUOM());
    }
    newOrderLine.setUOM(orderLine.getUOM());
    newOrderLine.setOrderedQuantity(qtyOrdered);
    newOrderLine.setOperativeQuantity(operativeQty);
  }

  private Long nextLineNo() {
    lastLineNo = lastLineNo + 10L;
    return lastLineNo;
  }

  private AttributeSetInstance copyAttributeSetValue(AttributeSetInstance attributeSetValue) {
    AttributeSetInstance newAttributeSetInstance = copyAttributeSetInstance(attributeSetValue);
    copyAttributes(attributeSetValue, newAttributeSetInstance);

    return newAttributeSetInstance;
  }

  private void copyAttributes(AttributeSetInstance attributeSetValueFrom,
      AttributeSetInstance attributeSetInstanceTo) {
    for (AttributeInstance attrInstance : attributeSetValueFrom.getAttributeInstanceList()) {
      AttributeInstance newAttributeInstance = OBProvider.getInstance()
          .get(AttributeInstance.class);
      newAttributeInstance.setAttributeSetValue(attributeSetInstanceTo);
      newAttributeInstance.setAttribute(attrInstance.getAttribute());
      attrInstance.setAttributeValue(attrInstance.getAttributeValue());

      attributeSetInstanceTo.getAttributeInstanceList().add(newAttributeInstance);
      OBDal.getInstance().save(newAttributeInstance);
      OBDal.getInstance().save(attributeSetInstanceTo);
    }
  }

  private AttributeSetInstance copyAttributeSetInstance(AttributeSetInstance attributeSetValue) {
    AttributeSetInstance newAttributeSetInstance = OBProvider.getInstance().get(
        AttributeSetInstance.class);
    newAttributeSetInstance.setAttributeSet(attributeSetValue.getAttributeSet());
    newAttributeSetInstance.setSerialNo(attributeSetValue.getSerialNo());
    newAttributeSetInstance.setLot(attributeSetValue.getLot());
    newAttributeSetInstance.setExpirationDate(attributeSetValue.getExpirationDate());
    newAttributeSetInstance.setDescription(attributeSetValue.getDescription());
    newAttributeSetInstance.setLotName(attributeSetValue.getLotName());
    newAttributeSetInstance.setLocked(attributeSetValue.isLocked());
    newAttributeSetInstance.setLockDescription(attributeSetValue.getLockDescription());
    OBDal.getInstance().save(newAttributeSetInstance);
    return newAttributeSetInstance;
  }

  /**
   * Return if an attribute set is instance. It returns TRUE if the attribute set is Lot, Serial No.
   * or Expiration Date or any of it attributes is an instance attribute
   * 
   * @param attributeSetInstance
   *          The attribute set instance to be validated
   * @return True if it is instance or False if not
   */
  private boolean attributeSetIsInstance(AttributeSetInstance attributeSetInstance) {
    if (attributeSetInstance == null) {
      return Boolean.FALSE;
    }
    AttributeSet attributeSet = attributeSetInstance.getAttributeSet();
    List<AttributeInstance> attributeInstances = attributeSetInstance.getAttributeInstanceList();
    boolean hasInstanceAttribute = false;
    for (AttributeInstance attrInstance : attributeInstances) {
      if (attrInstance.getAttribute().isInstanceAttribute()) {
        hasInstanceAttribute = Boolean.TRUE;
        break;
      }
    }
    return (attributeSet.isLot() || attributeSet.isSerialNo() || attributeSet.isExpirationDate() || hasInstanceAttribute);
  }

  /**
   * Gets the current tax according order information and selected product. If any tax is found an
   * exception is thrown.
   * 
   * @param order
   *          The order is processing
   * @param product
   *          The product where taxes are searching for
   * @return The Tax ID or an exception if it is not found
   * @throws IOException
   * @throws ServletException
   */
  private String getCurrentTaxId(Order order, Product product) throws IOException, ServletException {
    String bpLocationId = getMaxBusinessPartnerLocationId(order.getBusinessPartner());
    String orderWarehouseId = order.getWarehouse() != null ? order.getWarehouse().getId() : "";
    String orderProjectId = order.getProject() != null ? order.getProject().getId() : "";
    String strDatePromised = DateFormatUtils.format(order.getScheduledDeliveryDate(),
        OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(DATE_FORMAT_JAVA));

    String taxID = Tax.get(new DalConnectionProvider(), product.getId(), strDatePromised, order
        .getOrganization().getId(), orderWarehouseId, bpLocationId, bpLocationId, orderProjectId,
        order.isSalesTransaction());
    if (StringUtils.isEmpty(taxID)) {
      throw new OBException("@TaxNotFound@");
    }
    return taxID;
  }

  /**
   * Return the defined product price in a selected pricelist or null if the product doesn't has any
   * price defined on the price list
   * 
   * @param product
   *          The product where the price is searched.
   * @param priceList
   *          The price list where the product price is searched.
   * @return The product price defined for the product in the price list or NULL if any.
   */
  private ProductPrice getProductPriceInPriceList(Product product, PriceList priceList) {
    StringBuilder obq = new StringBuilder(" as pp ");
    obq.append("JOIN pp." + ProductPrice.PROPERTY_PRICELISTVERSION + " plv ");
    obq.append("WHERE pp." + ProductPrice.PROPERTY_PRODUCT + "." + Product.PROPERTY_ID + "= :"
        + PRODUCT_ID_PARAM);
    obq.append(" AND plv." + PriceListVersion.PROPERTY_PRICELIST + "." + PriceList.PROPERTY_ID
        + "= :" + PRICE_LIST_ID_PARAM);
    obq.append(" AND plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + "<= :"
        + PRICE_LIST_VERSION_VALID_FROM_PARAM);
    obq.append(" ORDER BY plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + " desc");

    OBQuery<ProductPrice> obQuery = OBDal.getInstance().createQuery(ProductPrice.class,
        obq.toString());
    obQuery.setNamedParameter(PRODUCT_ID_PARAM, product.getId());
    obQuery.setNamedParameter(PRICE_LIST_ID_PARAM, priceList.getId());
    obQuery.setNamedParameter(PRICE_LIST_VERSION_VALID_FROM_PARAM, new Date());
    obQuery.setMaxResult(1);
    ProductPrice productPrice = obQuery.uniqueResult();
    return productPrice;
  }

  /**
   * Returns the max order line number defined in the order passed as parameter
   * 
   * @param order
   *          The order is processing
   * @return The max order line number
   */
  private Long getMaxOrderLineNumber(Order order) {
    OBCriteria<OrderLine> obc = OBDal.getInstance().createCriteria(OrderLine.class);
    obc.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, order));
    obc.setProjection(Projections.max(OrderLine.PROPERTY_LINENO));
    Long lineNumber = 0L;
    obc.setMaxResults(1);
    Object o = obc.uniqueResult();
    if (o != null) {
      lineNumber = (Long) o;
    }
    return lineNumber;
  }

  /**
   * Returns the last business partner location ID
   * 
   * @param businessPartner
   *          The business partner where the location will be searched
   * @return the last business partner location ID
   */
  private String getMaxBusinessPartnerLocationId(BusinessPartner businessPartner) {
    OBCriteria<Location> obc = OBDal.getInstance().createCriteria(Location.class);
    obc.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, businessPartner));
    obc.setProjection(Projections.max(Location.PROPERTY_ID));
    obc.setMaxResults(1);
    String maxLocationId = (String) obc.uniqueResult();
    return maxLocationId;
  }

  private void addOrderLineToExplodeList(OrderLine newOrderLine) {
    explodeOrderLines.add(newOrderLine);
  }

  private void processExplodeOrderLines() {
    for (OrderLine orderLine : explodeOrderLines) {
      OBDal.getInstance().refresh(orderLine);
      org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, EXPLODE_BOM_PROCESS);

      final ProcessInstance pInstance = CallProcess.getInstance().call(process, orderLine.getId(),
          null);

      if (pInstance.getResult() == 0) {
        throw new OBException("Error executing Explode process");
      }
    }
  }

  private JSONObject getSuccessMessage(int recordsCopiedCount) throws JSONException {
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_SUCCESS);
    errorMessage.put(MESSAGE_TEXT, OBMessageUtils.messageBD(MESSAGE_SUCCESS) + "<br/>"
        + OBMessageUtils.messageBD(MESSAGE_RECORDS_COPIED) + recordsCopiedCount);
    return errorMessage;
  }

  private JSONObject getErrorMessage(Exception e) throws JSONException {
    Throwable ex = DbUtility.getUnderlyingSQLException(e);
    String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_ERROR);
    errorMessage.put(MESSAGE_TEXT, message);
    return errorMessage;
  }
}