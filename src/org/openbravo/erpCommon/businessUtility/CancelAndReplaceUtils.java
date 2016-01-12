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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderTax;
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
import org.openbravo.retail.posterminal.OrderLoader;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class CancelAndReplaceUtils {
  private static Logger log4j = Logger.getLogger(CancelAndReplaceUtils.class);
  private static Date today = null;
  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);
  private static final String CREATE_NETTING_SHIPMENT = "CancelAndReplaceCreateNetShipment";
  private static OrganizationStructureProvider osp = null;

  public static void cancelOrder(String newOrderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) {
    cancelAndReplaceOrder(newOrderId, jsonorder, useOrderDocumentNoForRelatedDocs, false);
  }

  public static void cancelAndReplaceOrder(String newOrderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) {
    cancelAndReplaceOrder(newOrderId, jsonorder, useOrderDocumentNoForRelatedDocs, true);
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
  protected static void cancelAndReplaceOrder(String orderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs, boolean replaceOrder) {
    ScrollableResults orderLines = null;
    try {

      boolean triggersDisabled = false;
      if (jsonorder != null && replaceOrder) {
        triggersDisabled = true;
      }

      Order newOrder = null;
      Order oldOrder = null;

      // If replaceOrder == true, the original order will be cancelled and replaced with a new one,
      // if == false, it will only be cancelled
      if (replaceOrder) {
        // Get new Order
        newOrder = OBDal.getInstance().get(Order.class, orderId);
        // Get old Order
        oldOrder = newOrder.getReplacedorder();
      } else {
        // Get old Order
        oldOrder = OBDal.getInstance().get(Order.class, orderId);
      }

      // Added check in case Cancel and Replace button is hit more than once
      if (jsonorder == null && oldOrder.isCancelled()) {
        throw new OBException("@APRM_Order@ " + oldOrder.getDocumentNo() + " @IsCancelled@");
      }

      today = new Date();

      osp = OBContext.getOBContext().getOrganizationStructureProvider(
          oldOrder.getOrganization().getClient().getId());

      // Release old reservations
      if (!triggersDisabled) {
        releaseOldReservations(oldOrder);
      }

      // Get documentNo for the inverse Order Header coming from jsonorder, if exists
      JSONObject negativeDocumentNoJSON = jsonorder != null
          && jsonorder.has("negativeDocNo") ? jsonorder.getJSONObject("negativeDocNo") : null;
      String negativeDocNo = negativeDocumentNoJSON != null
          && negativeDocumentNoJSON.has("documentNo")
          && negativeDocumentNoJSON.get("documentNo") != JSONObject.NULL ? negativeDocumentNoJSON
          .getString("documentNo") : null;

      // Create inverse Order header
      Order inverseOrder = createOrder(oldOrder, negativeDocNo, triggersDisabled);

      // Define netting goods shipment and its lines
      ShipmentInOut nettingGoodsShipment = null;
      ShipmentInOutLine newGoodsShipmentLine1 = null;

      // Iterate old order lines
      orderLines = getOrderLineList(oldOrder);
      long lineNoCounter = 1;
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

        if (createNettingGoodsShipment && inverseOrderLine != null) {
          // Create Netting goods shipment Header
          if (nettingGoodsShipment == null) {
            nettingGoodsShipment = createShipment(oldOrder, goodsShipmentLineList);
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
        }

        // Set old order delivered quantity to the ordered quantity
        oldOrderLine.setDeliveredQuantity(oldOrderLine.getOrderedQuantity());
        OBDal.getInstance().save(oldOrderLine);

        // Set inverse order delivered quantity to ordered quantity
        if (inverseOrderLine != null) {
          inverseOrderLine.setDeliveredQuantity(inverseOrderLine.getOrderedQuantity());
          OBDal.getInstance().save(inverseOrderLine);
        }
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
      oldOrder.setCancelled(true);
      oldOrder.setProcessed(true);
      oldOrder.setProcessNow(false);
      OBDal.getInstance().save(oldOrder);

      // Set Stardard Order to new order document type

      // Complete new order and generate good shipment and sales invoice
      if (!triggersDisabled && replaceOrder) {
        newOrder.setDocumentStatus("DR");
        OBDal.getInstance().save(newOrder);
        callCOrderPost(newOrder);

        // Create new reservations
        createNewReservations(newOrder);

        OBDal.getInstance().flush();
      }

      // Payment Creation
      // Get the payment schedule detail of the oldOrder
      createPayments(oldOrder, newOrder, inverseOrder, jsonorder, useOrderDocumentNoForRelatedDocs,
          replaceOrder, triggersDisabled);

    } catch (Exception e1) {
      try {
        OBDal.getInstance().getConnection().rollback();
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      throw new OBException(e3.getMessage());
    } finally {
      if (orderLines != null) {
        orderLines.close();
      }
    }
  }

  protected static void callCOrderPost(Order order) throws OBException {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(order.getId());
    final String procedureName = "c_order_post1";
    CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
  }

  private static Order createOrder(Order oldOrder, String documentNo, boolean triggersDisabled) {
    Order inverseOrder = (Order) DalUtil.copy(oldOrder, false, true);
    // Change order values
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
    inverseOrder.setOrderDate(today);
    inverseOrder.setCreationDate(today);
    inverseOrder.setUpdated(today);
    inverseOrder.setScheduledDeliveryDate(today);
    String newDocumentNo = documentNo;
    if (newDocumentNo == null) {
      newDocumentNo = FIN_Utility.getDocumentNo(oldOrder.getDocumentType(), Order.TABLE_NAME);
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
    if (oldOrderLine.getDeliveredQuantity().compareTo(BigDecimal.ZERO) == 1) {
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

    // Copy old order taxes to inverse, it is done when is executed from Web POS because triggers
    // are disabled
    if (triggersDisabled) {
      createOrderLineTaxes(oldOrderLine, inverseOrderLine, inverseOrder);
    }

    return inverseOrderLine;
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
      while (newOrderLines.next()) {
        OrderLine newOrderLine = (OrderLine) newOrderLines.get(0);
        OBCriteria<Reservation> reservationCriteria = OBDal.getInstance().createCriteria(
            Reservation.class);
        reservationCriteria.add(Restrictions.eq(Reservation.PROPERTY_SALESORDERLINE,
            newOrderLine.getReplacedorderline()));
        reservationCriteria.setMaxResults(1);
        Reservation reservation = (Reservation) reservationCriteria.uniqueResult();
        if (reservation != null) {
          ReservationUtils.createReserveFromSalesOrderLine(newOrderLine, true);
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
    if (line.getProduct().getProductType().equals("I") && line.getProduct().isStocked()) {
      // Stock is changed only for stocked products of type "Item"
      MaterialTransaction transaction = OBProvider.getInstance().get(MaterialTransaction.class);
      transaction.setOrganization(line.getOrganization());
      transaction.setMovementType(line.getShipmentReceipt().getMovementType());
      transaction.setProduct(line.getProduct());
      transaction.setStorageBin(line.getStorageBin());
      transaction.setOrderUOM(line.getOrderUOM());
      transaction.setUOM(line.getUOM());
      transaction.setOrderQuantity(line.getOrderQuantity());
      transaction.setMovementQuantity(line.getMovementQuantity().multiply(NEGATIVE_ONE));
      transaction.setMovementDate(line.getShipmentReceipt().getMovementDate());
      transaction.setGoodsShipmentLine(line);
      transaction.setAttributeSetValue(line.getAttributeSetValue());

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
      shipment.setPosted("N");
      shipment.setProcessed(false);
      shipment.setDocumentStatus("DR");
      shipment.setDocumentAction("CO");
    } else {
      shipment.setPosted("Y");
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
      List<FIN_PaymentSchedule> paymentScheduleList = paymentScheduleCriteria.list();
      boolean inversePaymentCreated = false;
      if (paymentScheduleList.size() != 0) {
        paymentSchedule = paymentScheduleList.get(0);
        FIN_Payment newPayment = null;
        if (replaceOrder) {
          // Get the payment schedule detail of the order
          OBCriteria<FIN_PaymentScheduleDetail> paymentScheduleDetailCriteria = OBDal.getInstance()
              .createCriteria(FIN_PaymentScheduleDetail.class);
          paymentScheduleDetailCriteria.add(Restrictions.eq(
              FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
          // We are looking for the ones with a payment detail
          paymentScheduleDetailCriteria.add(Restrictions
              .isNotNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
          List<FIN_PaymentScheduleDetail> paymentScheduleDetailList = paymentScheduleDetailCriteria
              .list();
          // New payment definition
          for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentScheduleDetailList) {
            newPayment = null;
            String paymentDocumentNo = null;

            FIN_PaymentDetail paymentDetail = paymentScheduleDetail.getPaymentDetails();
            FIN_Payment payment = paymentDetail.getFinPayment();
            FIN_PaymentMethod paymentPaymentMethod = payment.getPaymentMethod();
            BigDecimal amount = payment.getAmount();
            BigDecimal negativeAmount = paymentSchedule.getAmount().negate();
            DocumentType paymentDocumentType = payment.getDocumentType();
            FIN_FinancialAccount financialAccount = payment.getAccount();
            BigDecimal paymentTotalAmount = BigDecimal.ZERO;

            paymentDocumentNo = getPaymentDocumentNo(useOrderDocumentNoForRelatedDocs, oldOrder,
                paymentDocumentType);

            // Get Payment Description
            String description = getPaymentDescription();
            description += ": " + inverseOrder.getDocumentNo();

            if (!triggersDisabled) {
              // Only for BackEnd WorkFlow
              // Get the payment schedule of the new order to check the outstanding amount, could
              // have been automatically paid on C_ORDER_POST if is automatically invoiced and the
              // payment method of the financial account is configured as 'Automatic Receipt'
              FIN_PaymentSchedule paymentScheduleNewOrder = null;
              OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteriaNewOrder = OBDal.getInstance()
                  .createCriteria(FIN_PaymentSchedule.class);
              paymentScheduleCriteriaNewOrder.add(Restrictions.eq(
                  FIN_PaymentSchedule.PROPERTY_ORDER, newOrder));
              paymentScheduleCriteriaNewOrder.setMaxResults(1);
              paymentScheduleNewOrder = (FIN_PaymentSchedule) paymentScheduleCriteriaNewOrder
                  .uniqueResult();
              amount = paymentScheduleNewOrder.getOutstandingAmount();
            }
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
              // Duplicate payment with positive amount
              newPayment = createPayment(newPayment, newOrder, paymentPaymentMethod, amount,
                  paymentDocumentType, financialAccount, paymentDocumentNo);
              description += ": " + newOrder.getDocumentNo();
              paymentTotalAmount = paymentTotalAmount.add(amount);
            }

            if (negativeAmount.compareTo(BigDecimal.ZERO) != 0 && !inversePaymentCreated) {
              // Duplicate payment with negative amount
              newPayment = createPayment(newPayment, inverseOrder, paymentPaymentMethod,
                  negativeAmount, paymentDocumentType, financialAccount, paymentDocumentNo);
              paymentTotalAmount = paymentTotalAmount.add(negativeAmount);
              inversePaymentCreated = true;
            }

            // Create if needed a second payment for the partially paid
            BigDecimal outstandingAmount = paymentSchedule.getOutstandingAmount();
            if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {

              // Duplicate payment with positive amount
              newPayment = createPayment(newPayment, oldOrder, paymentPaymentMethod,
                  outstandingAmount, paymentDocumentType, financialAccount, paymentDocumentNo);
              description += ": " + oldOrder.getDocumentNo() + "\n";
              paymentTotalAmount = paymentTotalAmount.add(outstandingAmount);
            }

            // Set amount and used credit to zero
            newPayment.setAmount(paymentTotalAmount);
            newPayment.setUsedCredit(BigDecimal.ZERO);
            newPayment.setDescription(description);
            OBDal.getInstance().save(newPayment);

            OBDal.getInstance().flush();

            // Call to processPayment in order to process it
            FIN_PaymentProcess.doProcessPayment(newPayment, "P", true, null, null);
          }
          // There aren't any payments on original order, pay original order and inverse order
          // completely.
          if (paymentScheduleDetailList.size() == 0) {
            finishOrderPayments(jsonorder, oldOrder, inverseOrder, paymentSchedule,
                useOrderDocumentNoForRelatedDocs, triggersDisabled);
          }
        } else {
          // To only cancel a layaway two payments must be added to fully pay the old order and add
          // the same quantity in negative to the inverse order
          OrderLoader orderLoader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
          orderLoader.initializeVariables(jsonorder);
          orderLoader.handlePayments(jsonorder, inverseOrder, null, false);

          finishOrderPayments(jsonorder, oldOrder, inverseOrder, paymentSchedule,
              useOrderDocumentNoForRelatedDocs, triggersDisabled);
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

  // Create payments to pay complete fully the order, in the inverse order and old order
  private static void finishOrderPayments(JSONObject jsonorder, Order oldOrder, Order inverseOrder,
      FIN_PaymentSchedule paymentSchedule, boolean useOrderDocumentNoForRelatedDocs,
      boolean triggersDisabled) throws Exception {
    FIN_Payment newPayment = null;
    String paymentDocumentNo = null;
    FIN_PaymentMethod paymentPaymentMethod = null;
    FIN_FinancialAccount financialAccount = null;
    if (jsonorder != null) {
      paymentPaymentMethod = (FIN_PaymentMethod) jsonorder.getJSONObject("defaultPaymentType").get(
          "paymentMethod");
      financialAccount = (FIN_FinancialAccount) jsonorder.getJSONObject("defaultPaymentType").get(
          "financialAccount");
    } else {
      paymentPaymentMethod = oldOrder.getPaymentMethod();
      financialAccount = oldOrder.getBusinessPartner().getAccount();
    }

    BigDecimal negativeAmount = paymentSchedule.getOutstandingAmount().negate();
    BigDecimal outstandingAmount = paymentSchedule.getOutstandingAmount();
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
    newPayment = createPayment(newPayment, inverseOrder, paymentPaymentMethod, negativeAmount,
        paymentDocumentType, financialAccount, paymentDocumentNo);

    // Create if needed a second payment for the partially paid
    if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {

      // Duplicate payment with positive amount
      newPayment = createPayment(newPayment, oldOrder, paymentPaymentMethod, outstandingAmount,
          paymentDocumentType, financialAccount, paymentDocumentNo);
      description += ": " + oldOrder.getDocumentNo() + "\n";
    }

    // Set amount and used credit to zero
    newPayment.setAmount(BigDecimal.ZERO);
    newPayment.setUsedCredit(BigDecimal.ZERO);
    newPayment.setDescription(description);
    OBDal.getInstance().save(newPayment);

    OBDal.getInstance().flush();

    // Call to processPayment in order to process it
    FIN_PaymentProcess.doProcessPayment(newPayment, "P", true, null, null);
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
      paymentSchedule.setFinPaymentmethod(order.getBusinessPartner().getPaymentMethod());
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
    String language = RequestContext.get().getVariablesSecureApp().getLanguage();
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
}
