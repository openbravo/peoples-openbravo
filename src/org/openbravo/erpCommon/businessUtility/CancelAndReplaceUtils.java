/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class CancelAndReplaceUtils {
  private static Logger log4j = Logger.getLogger(CancelAndReplaceUtils.class);
  private static final BigDecimal NEGATIVE_ONE = new BigDecimal("-1");
  public static final String CREATE_NETTING_SHIPMENT = "CancelAndReplaceCreateNetShipment";
  public static final String ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET = "CancelAndReplaceAssociateShipmentToNewTicket";
  public static String REVERSE_PREFIX = "*R*";

  /**
   * Process that creates a replacement order in temporary status in order to Cancel and Replace an
   * original order
   * 
   * @param oldOrder
   *          Order that will be cancelled and replaced
   * @return
   */
  public static Order createReplacementOrder(Order oldOrder) {
    // Create new Order header
    Order newOrder = (Order) DalUtil.copy(oldOrder, false, true);
    // Change order values
    newOrder.setProcessed(false);
    newOrder.setPosted("N");
    newOrder.setDocumentStatus("TMP");
    newOrder.setDocumentAction("CO");
    newOrder.setGrandTotalAmount(BigDecimal.ZERO);
    newOrder.setSummedLineAmount(BigDecimal.ZERO);
    Date today = new Date();
    newOrder.setOrderDate(today);
    newOrder.setReplacedorder(oldOrder);
    String newDocumentNo = CancelAndReplaceUtils.getNextCancelDocNo(oldOrder.getDocumentNo());
    newOrder.setDocumentNo(newDocumentNo);
    OBDal.getInstance().save(newOrder);

    String newOrderId = newOrder.getId();

    // Create new Order lines
    ScrollableResults orderLines = null;
    long i = 0;
    try {
      orderLines = getOrderLineList(oldOrder);
      while (orderLines.next()) {
        OrderLine oldOrderLine = (OrderLine) orderLines.get(0);
        OrderLine newOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        newOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        newOrderLine.setReservedQuantity(BigDecimal.ZERO);
        newOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
        newOrderLine.setSalesOrder(newOrder);
        newOrderLine.setReplacedorderline(oldOrderLine);
        newOrder.getOrderLineList().add(newOrderLine);
        OBDal.getInstance().save(newOrderLine);
        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
          newOrder = OBDal.getInstance().get(Order.class, newOrderId);
        }
        i++;
      }
    } finally {
      if (orderLines != null) {
        orderLines.close();
      }
    }
    return newOrder;
  }

  public static Order cancelOrder(String newOrderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) {
    return cancelAndReplaceOrder(newOrderId, jsonorder, useOrderDocumentNoForRelatedDocs, false);
  }

  public static Order cancelAndReplaceOrder(String newOrderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) {
    return cancelAndReplaceOrder(newOrderId, jsonorder, useOrderDocumentNoForRelatedDocs, true);
  }

  /**
   * Process that cancels an existing order and creates another one inverse of the original. If it
   * is indicated a new order is created with received modifications that replaces the original one.
   * 
   * This process will create a netting goods shipment to leave the original order and the inverse
   * order completely delivered, and if anything is delivered was delivered in the original order it
   * will be delivered so in the new one.
   * 
   * The same behavior of shipments will be implemented with payments.
   * 
   * @param orderId
   *          Order Id of the new order or of the old order, depending on replaceOrder boolean.
   * @param jsonorder
   *          JSON Object of the order coming from Web POS
   * @param useOrderDocumentNoForRelatedDocs
   *          OBPOS_UseOrderDocumentNoForRelatedDocs preference from Web POS.
   * @param replaceOrder
   *          If replaceOrder == true, the original order will be cancelled and replaced with a new
   *          one, if == false, it will only be cancelled
   * @return
   */
  protected static Order cancelAndReplaceOrder(String orderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs, boolean replaceOrder) {
    ScrollableResults orderLines = null;
    Order newOrder = null;
    Order oldOrder = null;
    String newOrderId = null;
    String oldOrderId = null;
    String inverseOrderId = null;
    OBContext.setAdminMode(false);
    try {

      boolean triggersDisabled = false;
      if (jsonorder != null && replaceOrder) {
        triggersDisabled = true;
      }

      // If replaceOrder == true, the original order will be cancelled and replaced with a new one,
      // if == false, it will only be cancelled
      if (replaceOrder) {
        // Get new Order
        newOrder = OBDal.getInstance().get(Order.class, orderId);
        newOrderId = newOrder.getId();
        // Get old Order
        oldOrder = newOrder.getReplacedorder();
        oldOrderId = oldOrder.getId();
      } else {
        // Get old Order
        oldOrder = OBDal.getInstance().get(Order.class, orderId);
        oldOrderId = oldOrder.getId();
      }

      // Added check in case Cancel and Replace button is hit more than once
      if (jsonorder == null && oldOrder.isCancelled()) {
        throw new OBException("@APRM_Order@ " + oldOrder.getDocumentNo() + " @IsCancelled@");
      }

      // Release old reservations
      releaseOldReservations(oldOrder);

      // Refresh documents
      if (newOrderId != null) {
        newOrder = OBDal.getInstance().get(Order.class, newOrderId);
      }
      oldOrder = OBDal.getInstance().get(Order.class, oldOrderId);

      // Get documentNo for the inverse Order Header coming from jsonorder, if exists
      String negativeDocNo = jsonorder != null && jsonorder.has("negativeDocNo") ? jsonorder
          .getString("negativeDocNo") : null;

      // Create inverse Order header
      Order inverseOrder = createOrder(oldOrder, negativeDocNo, triggersDisabled);
      inverseOrderId = inverseOrder.getId();

      // Define netting goods shipment and its lines
      ShipmentInOut nettingGoodsShipment = null;
      String nettingGoodsShipmentId = null;
      ShipmentInOutLine newGoodsShipmentLine1 = null;

      // Iterate old order lines
      orderLines = getOrderLineList(oldOrder);
      long lineNoCounter = 1, i = 0;
      while (orderLines.next()) {
        OrderLine oldOrderLine = (OrderLine) orderLines.get(0);

        // Create inverse Order line
        OrderLine inverseOrderLine = createOrderLine(oldOrderLine, inverseOrder, replaceOrder,
            triggersDisabled);

        // Get Shipment lines of old order line
        OBCriteria<ShipmentInOutLine> goodsShipmentLineCriteria = OBDal.getInstance()
            .createCriteria(ShipmentInOutLine.class);
        goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
            oldOrderLine));
        goodsShipmentLineCriteria.addOrderBy(ShipmentInOutLine.PROPERTY_UPDATED, true);
        List<ShipmentInOutLine> goodsShipmentLineList = goodsShipmentLineCriteria.list();

        // check "Don't create netting shipment in Cancel and Replace" preference value
        boolean createNettingGoodsShipment = false;
        try {
          createNettingGoodsShipment = ("Y").equals(Preferences.getPreferenceValue(
              CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT, true, oldOrder.getClient(),
              oldOrder.getOrganization(), OBContext.getOBContext().getUser(), null, null));
        } catch (PropertyException e1) {
          createNettingGoodsShipment = false;
        }
        boolean associateShipmentToNewReceipt = false;
        try {
          associateShipmentToNewReceipt = ("Y").equals(Preferences.getPreferenceValue(
              CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET, true,
              oldOrder.getClient(), oldOrder.getOrganization(), OBContext.getOBContext().getUser(),
              null, null));
        } catch (PropertyException e1) {
          associateShipmentToNewReceipt = false;
        }

        if (createNettingGoodsShipment && inverseOrderLine != null) {
          // Create Netting goods shipment Header
          if (nettingGoodsShipment == null) {
            nettingGoodsShipment = createShipment(oldOrder, goodsShipmentLineList);
            nettingGoodsShipmentId = nettingGoodsShipment.getId();
          }

          // Stock manipulation
          org.openbravo.database.ConnectionProvider cp = new DalConnectionProvider(false);
          CallableStatement updateStockStatement = cp.getConnection().prepareCall(
              "{call M_UPDATE_INVENTORY (?,?,?,?,?,?,?,?,?,?,?,?,?)}");

          // Create Netting goods shipment Line for the old order line
          BigDecimal movementQty = oldOrderLine.getOrderedQuantity().subtract(
              oldOrderLine.getDeliveredQuantity());
          BigDecimal oldOrderLineDeliveredQty = oldOrderLine.getDeliveredQuantity();
          oldOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
          OBDal.getInstance().save(oldOrderLine);
          OBDal.getInstance().flush();
          if (movementQty.compareTo(BigDecimal.ZERO) != 0) {
            newGoodsShipmentLine1 = createShipmentLine(nettingGoodsShipment,
                goodsShipmentLineList.size() > 0 ? goodsShipmentLineList.get(0) : null,
                oldOrderLine, lineNoCounter++, movementQty, updateStockStatement, triggersDisabled);
          }
          // Create Netting goods shipment Line for the inverse order line
          movementQty = inverseOrderLine.getOrderedQuantity().subtract(
              inverseOrderLine.getDeliveredQuantity());
          if (movementQty.compareTo(BigDecimal.ZERO) != 0) {
            createShipmentLine(nettingGoodsShipment, newGoodsShipmentLine1, inverseOrderLine,
                lineNoCounter++, movementQty, updateStockStatement, triggersDisabled);
          }

          if (replaceOrder) {
            // Get the the new order line that replaces the old order line, should be only one
            OBCriteria<OrderLine> olc = OBDal.getInstance().createCriteria(OrderLine.class);
            olc.add(Restrictions.eq(OrderLine.PROPERTY_REPLACEDORDERLINE, oldOrderLine));
            olc.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, newOrder));
            olc.setMaxResults(1);
            OrderLine newOrderLine = (OrderLine) olc.uniqueResult();
            if (newOrderLine != null) {
              // Create Netting goods shipment Line for the new order line
              movementQty = oldOrderLineDeliveredQty;
              BigDecimal newOrderLineDeliveredQty = newOrderLine.getDeliveredQuantity();
              newOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
              OBDal.getInstance().save(newOrderLine);
              OBDal.getInstance().flush();
              if (movementQty.compareTo(BigDecimal.ZERO) != 0) {
                createShipmentLine(nettingGoodsShipment, newGoodsShipmentLine1, newOrderLine,
                    lineNoCounter++, movementQty, updateStockStatement, triggersDisabled);
              }
              if (newOrderLineDeliveredQty == null
                  || newOrderLineDeliveredQty.compareTo(BigDecimal.ZERO) == 0) {
                // Set new order line delivered quantity to old order line ordered quantity, this
                // case
                // coming from Backend (nothing is delivered)
                newOrderLine.setDeliveredQuantity(movementQty);
              } else {
                // Set new order line delivered quantity to previous delivery quantity, this case
                // coming from Web POS (everything is delivered)
                newOrderLine.setDeliveredQuantity(newOrderLineDeliveredQty);
              }
              OBDal.getInstance().save(newOrderLine);
            }
          }
        } else if (associateShipmentToNewReceipt) {
          OBDal.getInstance().flush();
          ShipmentInOut shipment = null;
          // Unprocess the shipment
          if (goodsShipmentLineList.size() > 0) {
            shipment = goodsShipmentLineList.get(0).getShipmentReceipt();
            processShipment(shipment);
          }
          for (ShipmentInOutLine shipmentLine : goodsShipmentLineList) {
            OBCriteria<OrderLine> olc = OBDal.getInstance().createCriteria(OrderLine.class);
            olc.add(Restrictions.eq(OrderLine.PROPERTY_REPLACEDORDERLINE, oldOrderLine));
            olc.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, newOrder));
            olc.setMaxResults(1);
            OrderLine newOrderLine = (OrderLine) olc.uniqueResult();
            if (newOrderLine != null) {
              shipmentLine.setSalesOrderLine(newOrderLine);
              if (jsonorder == null) {
                newOrderLine.setDeliveredQuantity(newOrderLine.getDeliveredQuantity().add(
                    shipmentLine.getMovementQuantity()));
                OBDal.getInstance().save(newOrderLine);
              }
              OBDal.getInstance().save(shipmentLine);
            }
          }
          OBDal.getInstance().flush();
          // Process the shipment
          if (shipment != null) {
            OBDal.getInstance().refresh(shipment);
            processShipment(shipment);
          }
        }

        // Set old order delivered quantity to the ordered quantity
        oldOrderLine.setDeliveredQuantity(oldOrderLine.getOrderedQuantity());
        OBDal.getInstance().save(oldOrderLine);

        // Set inverse order delivered quantity to ordered quantity
        if (inverseOrderLine != null) {
          inverseOrderLine.setDeliveredQuantity(inverseOrderLine.getOrderedQuantity());
          OBDal.getInstance().save(inverseOrderLine);
        }
        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();

          // Refresh documents
          if (nettingGoodsShipmentId != null) {
            nettingGoodsShipment = OBDal.getInstance().get(ShipmentInOut.class,
                nettingGoodsShipmentId);
          }
          if (replaceOrder) {
            newOrder = OBDal.getInstance().get(Order.class, newOrderId);
          }
          oldOrder = OBDal.getInstance().get(Order.class, oldOrderId);
          inverseOrder = OBDal.getInstance().get(Order.class, inverseOrderId);
        }
        i++;
      }
      if (nettingGoodsShipment != null) {
        processShipment(nettingGoodsShipment);
      }

      // Close inverse order
      inverseOrder.setDocumentStatus("CL");
      inverseOrder.setDocumentAction("--");
      inverseOrder.setProcessed(true);
      inverseOrder.setProcessNow(false);
      OBDal.getInstance().save(inverseOrder);

      // Close original order
      oldOrder.setDocumentStatus("CL");
      oldOrder.setDocumentAction("--");
      oldOrder.setReplacementorder(newOrder);
      oldOrder.setCancelled(true);
      oldOrder.setProcessed(true);
      oldOrder.setProcessNow(false);
      OBDal.getInstance().save(oldOrder);

      // Complete new order and generate good shipment and sales invoice
      if (!triggersDisabled && replaceOrder) {
        newOrder.setDocumentStatus("DR");
        OBDal.getInstance().save(newOrder);
        callCOrderPost(newOrder);
      }
      // Create new reservations
      createNewReservations(newOrder);

      OBDal.getInstance().flush();

      // Refresh documents
      if (nettingGoodsShipmentId != null) {
        nettingGoodsShipment = OBDal.getInstance().get(ShipmentInOut.class, nettingGoodsShipmentId);
      }
      if (replaceOrder) {
        newOrder = OBDal.getInstance().get(Order.class, newOrderId);
      }
      oldOrder = OBDal.getInstance().get(Order.class, oldOrderId);
      inverseOrder = OBDal.getInstance().get(Order.class, inverseOrderId);

      // Payment Creation
      // Get the payment schedule detail of the oldOrder
      createPayments(oldOrder, newOrder, inverseOrder, jsonorder, useOrderDocumentNoForRelatedDocs,
          replaceOrder, triggersDisabled);

      // Calling Cancelandreplaceorderhook
      WeldUtils.getInstanceFromStaticBeanManager(CancelAndReplaceOrderHookCaller.class)
          .executeHook(replaceOrder, triggersDisabled, oldOrder, newOrder, inverseOrder, jsonorder);

    } catch (Exception e1) {
      Throwable e2 = DbUtility.getUnderlyingSQLException(e1);
      log4j.error("Error executing Cancel and Replace", e1);
      throw new OBException(e2.getMessage());
    } finally {
      if (orderLines != null) {
        orderLines.close();
      }
      OBContext.restorePreviousMode();
    }
    return newOrder;
  }

  protected static void callCOrderPost(Order order) throws OBException {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(order.getId());
    final String procedureName = "c_order_post1";
    CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
  }

  private static Order createOrder(Order oldOrder, String documentNo, boolean triggersDisabled)
      throws JSONException, ParseException {
    Order inverseOrder = (Order) DalUtil.copy(oldOrder, false, true);
    // Change order values
    inverseOrder.setCreatedBy(OBContext.getOBContext().getUser());
    inverseOrder.setPosted("N");
    inverseOrder.setProcessed(false);
    inverseOrder.setDocumentStatus("DR");
    inverseOrder.setDocumentAction("CO");
    if (triggersDisabled) {
      inverseOrder.setGrandTotalAmount(oldOrder.getGrandTotalAmount().negate());
      inverseOrder.setSummedLineAmount(oldOrder.getSummedLineAmount().negate());
    } else {
      inverseOrder.setGrandTotalAmount(BigDecimal.ZERO);
      inverseOrder.setSummedLineAmount(BigDecimal.ZERO);
    }

    Date today = new Date();
    inverseOrder.setOrderDate(OBDateUtils.getDate(OBDateUtils.formatDate(today)));
    inverseOrder.setCreationDate(today);
    inverseOrder.setUpdated(today);
    inverseOrder.setScheduledDeliveryDate(today);
    String newDocumentNo = documentNo;
    if (newDocumentNo == null) {
      newDocumentNo = oldOrder.getDocumentNo() + REVERSE_PREFIX;
    }
    inverseOrder.setDocumentNo(newDocumentNo);
    inverseOrder.setCancelledorder(oldOrder);
    OBDal.getInstance().save(inverseOrder);

    // Copy old order taxes to inverse, it is done when is executed from Web POS because triggers
    // are disabled
    if (triggersDisabled) {
      createOrderTaxes(oldOrder, inverseOrder);
    }

    return inverseOrder;
  }

  protected static void createOrderTaxes(Order oldOrder, Order inverseOrder) {
    for (OrderTax orderTax : oldOrder.getOrderTaxList()) {
      OrderTax inverseOrderTax = (OrderTax) DalUtil.copy(orderTax, false, true);
      BigDecimal inverseTaxAmount = orderTax.getTaxAmount().negate();
      BigDecimal inverseTaxableAmount = orderTax.getTaxableAmount().negate();
      inverseOrderTax.setTaxAmount(inverseTaxAmount);
      inverseOrderTax.setTaxableAmount(inverseTaxableAmount);
      inverseOrderTax.setSalesOrder(inverseOrder);
      inverseOrder.getOrderTaxList().add(inverseOrderTax);
      OBDal.getInstance().save(inverseOrderTax);
    }
    OBDal.getInstance().flush();
  }

  protected static OrderLine createOrderLine(OrderLine oldOrderLine, Order inverseOrder,
      boolean replaceOrder, boolean triggersDisabled) {
    if (!replaceOrder
        && oldOrderLine.getDeliveredQuantity().compareTo(oldOrderLine.getOrderedQuantity()) == 0) {
      return null;
    }
    OrderLine inverseOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
    inverseOrderLine.setSalesOrder(inverseOrder);
    if (!replaceOrder && oldOrderLine.getDeliveredQuantity().compareTo(BigDecimal.ZERO) == 1) {
      BigDecimal inverseOrderedQuantity = oldOrderLine.getOrderedQuantity()
          .subtract(oldOrderLine.getDeliveredQuantity()).negate();
      inverseOrderLine.setOrderedQuantity(inverseOrderedQuantity);
    } else {
      inverseOrderLine.setOrderedQuantity(inverseOrderLine.getOrderedQuantity().negate());
    }
    if (triggersDisabled) {
      inverseOrderLine.setLineGrossAmount(oldOrderLine.getLineGrossAmount().negate());
      inverseOrderLine.setLineNetAmount(oldOrderLine.getLineNetAmount().negate());
    }
    // Set inverse order delivered quantity zero
    inverseOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
    inverseOrderLine.setReservedQuantity(BigDecimal.ZERO);

    inverseOrder.getOrderLineList().add(inverseOrderLine);
    OBDal.getInstance().save(inverseOrderLine);

    // Copy the discounts of the original line
    creteOrderLineDiscounts(oldOrderLine, inverseOrderLine, inverseOrder);
    // Copy old order taxes to inverse, it is done when is executed from Web POS because triggers
    // are disabled
    if (triggersDisabled) {
      createOrderLineTaxes(oldOrderLine, inverseOrderLine, inverseOrder);
    }

    return inverseOrderLine;
  }

  protected static void creteOrderLineDiscounts(OrderLine oldOrderLine, OrderLine inverseOrderLine,
      Order inverseOrder) {
    for (OrderLineOffer orderLineOffer : oldOrderLine.getOrderLineOfferList()) {
      final OrderLineOffer inverseOrderLineOffer = (OrderLineOffer) DalUtil.copy(orderLineOffer,
          false, true);
      inverseOrderLineOffer.setBaseGrossUnitPrice(inverseOrderLineOffer.getBaseGrossUnitPrice()
          .negate());
      inverseOrderLineOffer.setDisplayedTotalAmount(inverseOrderLineOffer.getDisplayedTotalAmount()
          .negate());
      inverseOrderLineOffer.setPriceAdjustmentAmt(inverseOrderLineOffer.getPriceAdjustmentAmt()
          .negate());
      inverseOrderLineOffer.setTotalAmount(inverseOrderLineOffer.getTotalAmount().negate());
      inverseOrderLineOffer.setSalesOrderLine(inverseOrderLine);
      OBDal.getInstance().save(inverseOrderLineOffer);
    }
    OBDal.getInstance().flush();
  }

  protected static void createOrderLineTaxes(OrderLine oldOrderLine, OrderLine inverseOrderLine,
      Order inverseOrder) {
    for (OrderLineTax orderLineTax : oldOrderLine.getOrderLineTaxList()) {
      final OrderLineTax inverseOrderLineTax = (OrderLineTax) DalUtil.copy(orderLineTax, false,
          true);
      BigDecimal inverseTaxAmount = orderLineTax.getTaxAmount().negate();
      BigDecimal inverseTaxableAmount = orderLineTax.getTaxableAmount().negate();
      inverseOrderLineTax.setTaxAmount(inverseTaxAmount);
      inverseOrderLineTax.setTaxableAmount(inverseTaxableAmount);
      inverseOrderLineTax.setSalesOrder(inverseOrder);
      inverseOrderLineTax.setSalesOrderLine(inverseOrderLine);
      inverseOrderLine.getOrderLineTaxList().add(inverseOrderLineTax);
      inverseOrder.getOrderLineTaxList().add(inverseOrderLineTax);
      OBDal.getInstance().save(inverseOrderLineTax);
    }
    OBDal.getInstance().flush();
  }

  protected static ShipmentInOut createShipment(Order oldOrder,
      List<ShipmentInOutLine> goodsShipmentLineList) {
    ShipmentInOut nettingGoodsShipment = null;
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        oldOrder.getOrganization().getClient().getId());
    // if (goodsShipmentLineList != null && goodsShipmentLineList.size() == 0) {
    if (goodsShipmentLineList.size() == 0) {
      // Create new Shipment
      nettingGoodsShipment = OBProvider.getInstance().get(ShipmentInOut.class);
      nettingGoodsShipment.setOrganization(oldOrder.getOrganization());
      // Set Document Type
      OBCriteria<DocumentType> goodsShipmentDocumentTypeCriteria = OBDal.getInstance()
          .createCriteria(DocumentType.class);
      OBCriteria<Table> goodsShipmentTableCriteria = OBDal.getInstance()
          .createCriteria(Table.class);
      goodsShipmentTableCriteria.add(Restrictions.eq(Table.PROPERTY_DBTABLENAME,
          ShipmentInOut.TABLE_NAME));
      List<Table> goodsShipmentTableList = goodsShipmentTableCriteria.list();
      if (goodsShipmentTableList.size() != 1) {
        throw new OBException("Only one table named M_InOut can exists");
      }
      Table goodsShipmentTable = goodsShipmentTableList.get(0);
      goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_TABLE,
          goodsShipmentTable));
      goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_SALESTRANSACTION,
          true));
      List<String> parentOrganizationIdList = osp.getParentList(oldOrder.getOrganization().getId(),
          true);
      goodsShipmentDocumentTypeCriteria.add(Restrictions.in(DocumentType.PROPERTY_ORGANIZATION
          + ".id", parentOrganizationIdList));
      goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_ACTIVE, true));
      goodsShipmentDocumentTypeCriteria.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
      List<DocumentType> goodsShipmentDocumentTypeList = goodsShipmentDocumentTypeCriteria.list();
      if (goodsShipmentDocumentTypeList.size() == 0) {
        throw new OBException("No document type found for the new shipment");
      }
      DocumentType goodsShipmentDocumentType = goodsShipmentDocumentTypeList.get(0);
      nettingGoodsShipment.setDocumentType(goodsShipmentDocumentType);
      nettingGoodsShipment.setWarehouse(oldOrder.getWarehouse());
      nettingGoodsShipment.setBusinessPartner(oldOrder.getBusinessPartner());
      if (oldOrder.getPartnerAddress() == null) {
        throw new OBException("The business partner location can not be null");
      }
      nettingGoodsShipment.setPartnerAddress(oldOrder.getPartnerAddress());
    } else {
      nettingGoodsShipment = (ShipmentInOut) DalUtil.copy(goodsShipmentLineList.get(0)
          .getShipmentReceipt(), false, true);
    }
    Date today = new Date();
    nettingGoodsShipment.setMovementDate(today);
    nettingGoodsShipment.setAccountingDate(today);
    nettingGoodsShipment.setSalesOrder(null);
    nettingGoodsShipment.setPosted("N");
    nettingGoodsShipment.setProcessed(false);
    nettingGoodsShipment.setDocumentStatus("DR");
    nettingGoodsShipment.setDocumentAction("CO");
    nettingGoodsShipment.setMovementType("C-");
    nettingGoodsShipment.setProcessGoodsJava("--");
    String nettingGoodsShipmentDocumentNo = FIN_Utility.getDocumentNo(
        nettingGoodsShipment.getDocumentType(), ShipmentInOut.TABLE_NAME);
    nettingGoodsShipment.setDocumentNo(nettingGoodsShipmentDocumentNo);
    OBDal.getInstance().save(nettingGoodsShipment);
    return nettingGoodsShipment;
  }

  protected static ShipmentInOutLine createShipmentLine(ShipmentInOut nettingGoodsShipment,
      ShipmentInOutLine nettingGoodsShipmentLine, OrderLine orderLine, long lineNoCounter,
      BigDecimal movementQty, CallableStatement updateStockStatement, boolean triggersDisabled) {
    ShipmentInOutLine newGoodsShipmentLine = null;
    if (nettingGoodsShipmentLine == null) {
      newGoodsShipmentLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
      newGoodsShipmentLine.setOrganization(orderLine.getOrganization());
      newGoodsShipmentLine.setProduct(orderLine.getProduct());
      newGoodsShipmentLine.setUOM(orderLine.getUOM());
      // Get first storage bin
      Locator locator1 = nettingGoodsShipment.getWarehouse().getLocatorList().get(0);
      newGoodsShipmentLine.setStorageBin(locator1);
    } else {
      newGoodsShipmentLine = (ShipmentInOutLine) DalUtil
          .copy(nettingGoodsShipmentLine, false, true);
    }
    newGoodsShipmentLine.setLineNo(10 * lineNoCounter);
    newGoodsShipmentLine.setSalesOrderLine(orderLine);
    newGoodsShipmentLine.setShipmentReceipt(nettingGoodsShipment);
    newGoodsShipmentLine.setMovementQuantity(movementQty);

    // Create Material Transaction record
    createMTransaction(newGoodsShipmentLine, updateStockStatement, triggersDisabled);

    OBDal.getInstance().save(newGoodsShipmentLine);
    return newGoodsShipmentLine;
  }

  protected static void releaseOldReservations(Order oldOrder) {
    ScrollableResults oldOrderLines = null;
    try {
      // Iterate old order lines
      oldOrderLines = getOrderLineList(oldOrder);
      int i = 0;
      while (oldOrderLines.next()) {
        OrderLine oldOrderLine = (OrderLine) oldOrderLines.get(0);
        OBCriteria<Reservation> reservationCriteria = OBDal.getInstance().createCriteria(
            Reservation.class);
        reservationCriteria.add(Restrictions.eq(Reservation.PROPERTY_SALESORDERLINE, oldOrderLine));
        reservationCriteria.setMaxResults(1);
        Reservation reservation = (Reservation) reservationCriteria.uniqueResult();
        if (reservation != null) {
          releaseReservation(reservation);
          OBDal.getInstance().save(reservation);
        }
        if ((i % 100) == 0) {
          OBDal.getInstance().getSession().clear();
        }
      }
    } catch (Exception e) {
      log4j.error("Error in CancelAndReplaceUtils.releaseOldReservations", e);
      throw new OBException(e.getMessage(), e);
    } finally {
      if (oldOrderLines != null) {
        oldOrderLines.close();
      }
    }
  }

  // Release a reservation
  protected static void releaseReservation(Reservation reservation) {
    final StringBuilder hqlReservations = new StringBuilder();
    hqlReservations.append(" update " + ReservationStock.ENTITY_NAME + " as rs set rs."
        + ReservationStock.PROPERTY_RELEASED + " = rs." + ReservationStock.PROPERTY_QUANTITY);
    hqlReservations.append(" where rs." + ReservationStock.PROPERTY_RESERVATION + "."
        + Reservation.PROPERTY_ID + " = :reservationId");
    Query updateTransactions = OBDal.getInstance().getSession()
        .createQuery(hqlReservations.toString());
    updateTransactions.setParameter("reservationId", reservation.getId());
    updateTransactions.executeUpdate();

    OBDal.getInstance().flush();
  }

  protected static void createNewReservations(Order newOrder) {
    ScrollableResults newOrderLines = null;
    try {
      // Iterate old order lines
      newOrderLines = getOrderLineList(newOrder);
      int i = 0;
      while (newOrderLines.next()) {
        OrderLine newOrderLine = (OrderLine) newOrderLines.get(0);
        if (newOrderLine.getDeliveredQuantity() != null) {
          if (newOrderLine.getOrderedQuantity().subtract(newOrderLine.getDeliveredQuantity())
              .compareTo(BigDecimal.ZERO) == 0) {
            continue;
          }
        }
        OBCriteria<Reservation> reservationCriteria = OBDal.getInstance().createCriteria(
            Reservation.class);
        reservationCriteria.add(Restrictions.eq(Reservation.PROPERTY_SALESORDERLINE,
            newOrderLine.getReplacedorderline()));
        reservationCriteria.setMaxResults(1);
        Reservation reservation = (Reservation) reservationCriteria.uniqueResult();
        if (reservation != null) {
          ReservationUtils.createReserveFromSalesOrderLine(newOrderLine, true);
        }
        if ((i % 100) == 0) {
          OBDal.getInstance().getSession().clear();
        }
      }
    } catch (Exception e) {
      log4j.error("Error in CancelAndReplaceUtils.createNewReservations", e);
      throw new OBException(e.getMessage(), e);
    } finally {
      if (newOrderLines != null) {
        newOrderLines.close();
      }
    }
  }

  protected static ScrollableResults getOrderLineList(Order order) {
    OBCriteria<OrderLine> orderLinesCriteria = OBDal.getInstance().createCriteria(OrderLine.class);
    orderLinesCriteria.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, order));

    ScrollableResults orderLines = orderLinesCriteria.scroll(ScrollMode.FORWARD_ONLY);
    return orderLines;
  }

  protected static void createMTransaction(ShipmentInOutLine line,
      CallableStatement updateStockStatement, boolean triggersDisabled) {
    Product prod = line.getProduct();
    if (prod.getProductType().equals("I") && line.getProduct().isStocked()) {
      // Stock is changed only for stocked products of type "Item"
      MaterialTransaction transaction = OBProvider.getInstance().get(MaterialTransaction.class);
      transaction.setOrganization(line.getOrganization());
      transaction.setMovementType(line.getShipmentReceipt().getMovementType());
      transaction.setProduct(prod);
      transaction.setStorageBin(line.getStorageBin());
      transaction.setOrderUOM(line.getOrderUOM());
      transaction.setUOM(line.getUOM());
      transaction.setOrderQuantity(line.getOrderQuantity());
      transaction.setMovementQuantity(line.getMovementQuantity().multiply(NEGATIVE_ONE));
      transaction.setMovementDate(line.getShipmentReceipt().getMovementDate());
      transaction.setGoodsShipmentLine(line);
      if (line.getAttributeSetValue() != null) {
        transaction.setAttributeSetValue(line.getAttributeSetValue());
      } else if (prod.getAttributeSet() != null
          && (prod.getUseAttributeSetValueAs() == null || !"F".equals(prod
              .getUseAttributeSetValueAs())) && prod.getAttributeSet().isRequireAtLeastOneValue()) {
        // Set fake AttributeSetInstance to transaction line for netting shipment as otherwise it
        // will return an error when the product has an attribute set and
        // "Is Required at Least One Value" property of the attribute set is "Y"
        AttributeSetInstance attr = OBProvider.getInstance().get(AttributeSetInstance.class);
        attr.setAttributeSet(prod.getAttributeSet());
        attr.setDescription("1");
        OBDal.getInstance().save(attr);
        transaction.setAttributeSetValue(attr);
      }

      // Execute M_UPDATE_INVENTORY stored procedure, it is done when is executed from Web POS
      // because triggers are disabled
      if (triggersDisabled) {
        updateInventory(transaction, updateStockStatement);
      }

      OBDal.getInstance().save(transaction);
    }
  }

  protected static void updateInventory(MaterialTransaction transaction,
      CallableStatement updateStockStatement) {
    try {
      // client
      updateStockStatement.setString(1, OBContext.getOBContext().getCurrentClient().getId());
      // org
      updateStockStatement.setString(2, OBContext.getOBContext().getCurrentOrganization().getId());
      // user
      updateStockStatement.setString(3, OBContext.getOBContext().getUser().getId());
      // product
      updateStockStatement.setString(4, transaction.getProduct().getId());
      // locator
      updateStockStatement.setString(5, transaction.getStorageBin().getId());
      // attributesetinstance
      updateStockStatement.setString(6, transaction.getAttributeSetValue() != null ? transaction
          .getAttributeSetValue().getId() : null);
      // uom
      updateStockStatement.setString(7, transaction.getUOM().getId());
      // product uom
      updateStockStatement.setString(8, null);
      // p_qty
      updateStockStatement.setBigDecimal(9,
          transaction.getMovementQuantity() != null ? transaction.getMovementQuantity() : null);
      // p_qtyorder
      updateStockStatement.setBigDecimal(10,
          transaction.getOrderQuantity() != null ? transaction.getOrderQuantity() : null);
      // p_dateLastInventory --- **
      updateStockStatement.setDate(11, null);
      // p_preqty
      updateStockStatement.setBigDecimal(12, BigDecimal.ZERO);
      // p_preqtyorder
      updateStockStatement.setBigDecimal(13, transaction.getOrderQuantity() != null ? transaction
          .getOrderQuantity().multiply(NEGATIVE_ONE) : null);

      updateStockStatement.execute();

    } catch (Exception e) {
      log4j.error("Error in CancelAndReplaceUtils.updateInventory", e);
      throw new OBException(e.getMessage(), e);
    }
  }

  protected static void processShipment(ShipmentInOut shipment) {
    if (shipment.isProcessed()) {
      shipment.setProcessed(false);
      shipment.setDocumentStatus("DR");
      shipment.setDocumentAction("CO");
    } else {
      shipment.setProcessed(true);
      shipment.setDocumentStatus("CO");
      shipment.setDocumentAction("--");
    }
    OBDal.getInstance().save(shipment);
    OBDal.getInstance().flush();
  }

  protected static void createPayments(Order oldOrder, Order newOrder, Order inverseOrder,
      JSONObject jsonorder, boolean useOrderDocumentNoForRelatedDocs, boolean replaceOrder,
      boolean triggersDisabled) {
    try {
      FIN_PaymentSchedule paymentSchedule;
      OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteria = OBDal.getInstance().createCriteria(
          FIN_PaymentSchedule.class);
      paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, oldOrder));
      paymentScheduleCriteria.setMaxResults(1);
      paymentSchedule = (FIN_PaymentSchedule) paymentScheduleCriteria.uniqueResult();
      if (paymentSchedule != null) {
        FIN_Payment newPayment = null;

        // Get paid amount on original order
        final String countHql = "select coalesce(sum(pd." + FIN_PaymentDetail.PROPERTY_AMOUNT
            + "), 0) as amount from " + FIN_PaymentScheduleDetail.ENTITY_NAME + " as psd join psd."
            + FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS + " as pd where psd."
            + FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE + ".id =:paymentScheduleId";
        final Query qry = OBDal.getInstance().getSession().createQuery(countHql);
        qry.setParameter("paymentScheduleId", paymentSchedule.getId());
        qry.setMaxResults(1);
        BigDecimal paidAmount = (BigDecimal) qry.uniqueResult();
        BigDecimal outstandingAmount = paymentSchedule.getAmount().subtract(paidAmount);
        BigDecimal negativeAmount = BigDecimal.ZERO;

        if (replaceOrder) {
          boolean createPayments = true;
          // Pay fully inverse order in C&R.
          negativeAmount = paymentSchedule.getAmount().negate();
          if (!triggersDisabled) {
            // Only for BackEnd WorkFlow
            // Get the payment schedule of the new order to check the outstanding amount, could
            // have been automatically paid on C_ORDER_POST if is automatically invoiced and the
            // payment method of the financial account is configured as 'Automatic Receipt'
            FIN_PaymentSchedule paymentScheduleNewOrder = null;
            OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteriaNewOrder = OBDal.getInstance()
                .createCriteria(FIN_PaymentSchedule.class);
            paymentScheduleCriteriaNewOrder.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER,
                newOrder));
            paymentScheduleCriteriaNewOrder.setMaxResults(1);
            paymentScheduleNewOrder = (FIN_PaymentSchedule) paymentScheduleCriteriaNewOrder
                .uniqueResult();
            if (paymentScheduleNewOrder.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
              createPayments = false;
            }
          }

          newPayment = payOriginalAndInverseOrder(jsonorder, oldOrder, inverseOrder, newPayment,
              outstandingAmount, negativeAmount, useOrderDocumentNoForRelatedDocs,
              triggersDisabled, replaceOrder);

          // Pay of the new order the amount already paid in original order
          if (createPayments && paidAmount.compareTo(BigDecimal.ZERO) != 0) {
            newPayment = createPayment(newPayment, newOrder, null, paidAmount, null, null, null);
            String description = newPayment.getDescription() + ": " + newOrder.getDocumentNo();
            newPayment.setDescription(description);
          }
        } else {
          // To only cancel a layaway two payments must be added to fully pay the old order and add
          // the same quantity in negative to the inverse order
          if (jsonorder.getJSONArray("payments").length() > 0) {
            WeldUtils.getInstanceFromStaticBeanManager(CancelLayawayPaymentsHookCaller.class)
                .executeHook(jsonorder, inverseOrder);

            // In a cancel layaway the gross value of the jsonorder was the amount to
            // return to the customer, not the amount of the ticket. In this case the amount and
            // outstanding needs to be fixed as are created in a wrong way.
            OBCriteria<FIN_PaymentSchedule> newPaymentScheduleList = OBDal.getInstance()
                .createCriteria(FIN_PaymentSchedule.class);
            newPaymentScheduleList.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER,
                inverseOrder));
            newPaymentScheduleList.setMaxResults(1);
            FIN_PaymentSchedule newPaymentSchedule = (FIN_PaymentSchedule) newPaymentScheduleList
                .uniqueResult();
            newPaymentSchedule.setAmount(paymentSchedule.getAmount().negate());
            newPaymentSchedule.setOutstandingAmount(newPaymentSchedule.getAmount().subtract(
                newPaymentSchedule.getPaidAmount()));
            OBDal.getInstance().save(newPaymentSchedule);
          }

          negativeAmount = outstandingAmount.negate();
          if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {
            newPayment = payOriginalAndInverseOrder(jsonorder, oldOrder, inverseOrder, newPayment,
                outstandingAmount, negativeAmount, useOrderDocumentNoForRelatedDocs,
                triggersDisabled, replaceOrder);
          }
        }

        // Call to processPayment in order to process it
        if (triggersDisabled && replaceOrder) {
          TriggerHandler.getInstance().enable();
        }
        if (newPayment != null) {
          FIN_PaymentProcess.doProcessPayment(newPayment, "P", true, null, null);
        }
        if (triggersDisabled && replaceOrder) {
          TriggerHandler.getInstance().disable();
        }

      } else {
        throw new OBException("There is no payment plan for the order: " + oldOrder.getId());
      }

    } catch (Exception e1) {
      log4j.error("Error in CancelAndReplaceUtils.createPayments", e1);
      try {
        OBDal.getInstance().getConnection().rollback();
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      throw new OBException(e3);
    }
  }

  // Pay original order and inverse order.
  private static FIN_Payment payOriginalAndInverseOrder(JSONObject jsonorder, Order oldOrder,
      Order inverseOrder, FIN_Payment newPayment, BigDecimal outstandingAmount,
      BigDecimal negativeAmount, boolean useOrderDocumentNoForRelatedDocs,
      boolean triggersDisabled, boolean replaceOrder) throws Exception {
    FIN_Payment _newPayment = newPayment;
    String paymentDocumentNo = null;
    FIN_PaymentMethod paymentPaymentMethod = null;
    FIN_FinancialAccount financialAccount = null;
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        oldOrder.getOrganization().getClient().getId());
    if (jsonorder != null) {
      paymentPaymentMethod = (FIN_PaymentMethod) jsonorder.getJSONObject("defaultPaymentType").get(
          "paymentMethod");
      financialAccount = (FIN_FinancialAccount) jsonorder.getJSONObject("defaultPaymentType").get(
          "financialAccount");
    } else {
      paymentPaymentMethod = oldOrder.getPaymentMethod();
      // Find a financial account belong the organization tree
      if (oldOrder.getBusinessPartner().getAccount() != null
          && FIN_Utility.getFinancialAccountPaymentMethod(paymentPaymentMethod.getId(), oldOrder
              .getBusinessPartner().getAccount().getId(), true, oldOrder.getCurrency().getId()) != null
          && osp.isInNaturalTree(oldOrder.getBusinessPartner().getAccount().getOrganization(),
              OBDal.getInstance().get(Organization.class, oldOrder.getOrganization().getId()))) {
        financialAccount = oldOrder.getBusinessPartner().getAccount();
      } else {
        financialAccount = FIN_Utility.getFinancialAccountPaymentMethod(
            paymentPaymentMethod.getId(), null, true, oldOrder.getCurrency().getId(),
            oldOrder.getOrganization().getId()).getAccount();
      }
    }

    final DocumentType paymentDocumentType = FIN_Utility.getDocumentType(
        oldOrder.getOrganization(), AcctServer.DOCTYPE_ARReceipt);
    if (paymentDocumentType == null) {
      throw new OBException("No document type found for the new payment");
    }

    paymentDocumentNo = getPaymentDocumentNo(useOrderDocumentNoForRelatedDocs, oldOrder,
        paymentDocumentType);

    // Get Payment Description
    String description = getPaymentDescription();
    description += ": " + inverseOrder.getDocumentNo();

    // Duplicate payment with negative amount
    _newPayment = createPayment(_newPayment, inverseOrder, paymentPaymentMethod, negativeAmount,
        paymentDocumentType, financialAccount, paymentDocumentNo);

    // Duplicate payment with positive amount
    _newPayment = createPayment(_newPayment, oldOrder, paymentPaymentMethod, outstandingAmount,
        paymentDocumentType, financialAccount, paymentDocumentNo);
    description += ": " + oldOrder.getDocumentNo() + "\n";

    // Set amount and used credit to zero
    _newPayment.setAmount(BigDecimal.ZERO);
    _newPayment.setUsedCredit(BigDecimal.ZERO);
    _newPayment.setDescription(description);
    return _newPayment;
  }

  protected static FIN_Payment createPayment(FIN_Payment payment, Order order,
      FIN_PaymentMethod paymentPaymentMethod, BigDecimal amount, DocumentType paymentDocumentType,
      FIN_FinancialAccount financialAccount, String paymentDocumentNo) throws Exception {
    FIN_Payment newPayment = payment;

    // Get the payment schedule of the order
    FIN_PaymentSchedule paymentSchedule = null;
    OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, order));
    paymentScheduleCriteria.setMaxResults(1);
    paymentSchedule = (FIN_PaymentSchedule) paymentScheduleCriteria.uniqueResult();
    if (paymentSchedule == null) {
      // Create a Payment Schedule if the order hasn't got
      paymentSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
      paymentSchedule.setClient(order.getClient());
      paymentSchedule.setOrganization(order.getOrganization());
      paymentSchedule.setCurrency(order.getCurrency());
      paymentSchedule.setOrder(order);
      paymentSchedule.setFinPaymentmethod(order.getPaymentMethod());
      paymentSchedule.setAmount(amount);
      paymentSchedule.setOutstandingAmount(amount);
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
    }

    // Get the payment schedule detail of the order
    OBCriteria<FIN_PaymentScheduleDetail> paymentScheduleDetailCriteria = OBDal.getInstance()
        .createCriteria(FIN_PaymentScheduleDetail.class);
    paymentScheduleDetailCriteria.add(Restrictions.eq(
        FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
    // There should be only one with null paymentDetails
    paymentScheduleDetailCriteria.add(Restrictions
        .isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
    List<FIN_PaymentScheduleDetail> paymentScheduleDetailList = paymentScheduleDetailCriteria
        .list();
    if (paymentScheduleDetailList.size() != 0) {
      HashMap<String, BigDecimal> paymentScheduleDetailAmount = new HashMap<String, BigDecimal>();
      String paymentScheduleDetailId = paymentScheduleDetailList.get(0).getId();
      paymentScheduleDetailAmount.put(paymentScheduleDetailId, amount);

      if (payment == null) {
        // Call to savePayment in order to create a new payment in
        newPayment = FIN_AddPayment.savePayment(payment, true, paymentDocumentType,
            paymentDocumentNo, order.getBusinessPartner(), paymentPaymentMethod, financialAccount,
            amount.toPlainString(), order.getOrderDate(), order.getOrganization(), null,
            paymentScheduleDetailList, paymentScheduleDetailAmount, false, false,
            order.getCurrency(), BigDecimal.ZERO, BigDecimal.ZERO);
      }
      // Create a new line
      else {
        FIN_AddPayment.updatePaymentDetail(paymentScheduleDetailList.get(0), newPayment, amount,
            false);
      }

    } else {
      // Two possibilities
      // 1.- All the payments have been created
      // 2.- The payment was created trough Web POS and therefore a payment schedule detail with
      // null payment detail is missing
      // Lets assume that in this point the payment was created trough Web POS
      // Create missing payment schedule detail
      FIN_PaymentScheduleDetail paymentScheduleDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      paymentScheduleDetail.setOrganization(order.getOrganization());
      paymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
      OBDal.getInstance().save(paymentScheduleDetail);
      paymentScheduleDetailList.add(paymentScheduleDetail);

      // Continue with the payment
      HashMap<String, BigDecimal> paymentScheduleDetailAmount = new HashMap<String, BigDecimal>();
      String paymentScheduleDetailId = paymentScheduleDetail.getId();
      paymentScheduleDetailAmount.put(paymentScheduleDetailId, amount);
      if (payment == null) {
        // Call to savePayment in order to create a new payment in
        newPayment = FIN_AddPayment.savePayment(payment, true, paymentDocumentType,
            paymentDocumentNo, order.getBusinessPartner(), paymentPaymentMethod, financialAccount,
            amount.toPlainString(), order.getOrderDate(), order.getOrganization(), null,
            paymentScheduleDetailList, paymentScheduleDetailAmount, false, false,
            order.getCurrency(), BigDecimal.ZERO, BigDecimal.ZERO);
      }
      // Create a new line
      else {
        FIN_AddPayment.updatePaymentDetail(paymentScheduleDetail, newPayment, amount, false);
      }
    }
    return newPayment;
  }

  protected static String getPaymentDescription() {
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    String paymentDescription = Utility.messageBD(new DalConnectionProvider(false),
        "OrderDocumentno", language);
    return paymentDescription;
  }

  protected static String getDocumentNo(Entity entity, DocumentType doctypeTarget,
      DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "", entity
            .getTableName(), doctypeTarget == null ? "" : doctypeTarget.getId(),
        doctype == null ? "" : doctype.getId(), false, true);
  }

  protected static String getPaymentDocumentNo(boolean useOrderDocumentNoForRelatedDocs,
      Order order, DocumentType paymentDocumentType) {
    String paymentDocumentNo = null;
    // Get Payment DocumentNo
    Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.class);

    if (useOrderDocumentNoForRelatedDocs) {
      paymentDocumentNo = order.getDocumentNo();
    } else {
      paymentDocumentNo = getDocumentNo(paymentEntity, null, paymentDocumentType);
    }
    return paymentDocumentNo;
  }

  /**
   * Process that generates a document number for an order which cancels another order.
   * 
   * @param documentNo
   *          Document number of the cancelled order.
   * @return The new document number for the order which cancels the old order.
   */
  public static String getNextCancelDocNo(String documentNo) {
    String newDocNo = "";
    String[] splittedDocNo = documentNo.split("-");
    if (splittedDocNo.length > 1) {
      int nextNumber;
      try {
        nextNumber = Integer.parseInt(splittedDocNo[splittedDocNo.length - 1]) + 1;
        for (int i = 0; i < splittedDocNo.length; i++) {
          if (i == 0) {
            newDocNo = splittedDocNo[i] + "-";
          } else if (i < splittedDocNo.length - 1) {
            newDocNo += splittedDocNo[i] + "-";
          } else {
            newDocNo += nextNumber;
          }
        }
      } catch (NumberFormatException nfe) {
        newDocNo = documentNo + "-1";
      }
    } else {
      newDocNo = documentNo + "-1";
    }
    return newDocNo;
  }

}
