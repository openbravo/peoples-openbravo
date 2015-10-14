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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class CancelAndReplaceUtils {
  private static Logger log4j = Logger.getLogger(CancelAndReplaceUtils.class);
  private static Date today = null;
  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);
  private static final String CREATE_NETTING_SHIPMENT = "CancelAndReplaceCreateNetShipment";
  private static OrganizationStructureProvider osp = null;

  public static JSONObject cancelOrder(String newOrderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) {
    return cancelAndReplaceOrder(newOrderId, jsonorder, useOrderDocumentNoForRelatedDocs, false);
  }

  public static JSONObject cancelAndReplaceOrder(String newOrderId, JSONObject jsonorder,
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
  protected static JSONObject cancelAndReplaceOrder(String orderId, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs, boolean replaceOrder) {
    ScrollableResults orderLines = null;
    try {

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

      today = new Date();

      osp = OBContext.getOBContext().getOrganizationStructureProvider(
          oldOrder.getOrganization().getClient().getId());

      // Get documentNo for the inverse Order Header coming from jsonorder, if exists
      JSONObject negativeDocumentNoJSON = jsonorder != JSONObject.NULL
          && jsonorder.has("negativeDocNo") ? jsonorder.getJSONObject("negativeDocNo") : null;
      String negativeDocNo = negativeDocumentNoJSON != null
          && negativeDocumentNoJSON.has("documentNo")
          && negativeDocumentNoJSON.get("documentNo") != JSONObject.NULL ? negativeDocumentNoJSON
          .getString("documentNo") : null;

      // Create inverse Order header
      Order inverseOrder = createOrder(oldOrder, negativeDocNo);

      // Define netting goods shipment and its lines
      ShipmentInOut nettingGoodsShipment = null;
      ShipmentInOutLine newGoodsShipmentLine1 = null;

      // TODO, Replace Reservations or Create New Reservations and Release old ones??
      // createReservations();

      // Iterate old order lines
      orderLines = getOrderLineList(oldOrder);
      long lineNoCounter = 1;
      while (orderLines.next()) {
        OrderLine oldOrderLine = (OrderLine) orderLines.get(0);

        // Create inverse Order line
        OrderLine inverseOrderLine = createOrderLine(oldOrderLine, inverseOrder);

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

        if (createNettingGoodsShipment) {
          // Create Netting goods shipment Header
          if (nettingGoodsShipment == null) {
            nettingGoodsShipment = createShipment(oldOrder, goodsShipmentLineList);
          }
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
                oldOrderLine, lineNoCounter++, movementQty);
          }
          // Create Netting goods shipment Line for the inverse order line
          movementQty = inverseOrderLine.getOrderedQuantity().subtract(
              inverseOrderLine.getDeliveredQuantity());
          if (movementQty.compareTo(BigDecimal.ZERO) != 0) {
            createShipmentLine(nettingGoodsShipment, newGoodsShipmentLine1, inverseOrderLine,
                lineNoCounter++, movementQty);
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
                    lineNoCounter++, movementQty);
              }
              if (newOrderLineDeliveredQty.compareTo(BigDecimal.ZERO) == 0) {
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
        inverseOrderLine.setDeliveredQuantity(inverseOrderLine.getOrderedQuantity());
        OBDal.getInstance().save(inverseOrderLine);
      }
      if (nettingGoodsShipment != null) {
        processShipment(nettingGoodsShipment);
      }

      // TODO
      // Get accountPaymentMethod in order to avoid automatic payment creation during c_order_post
      // beforePosting(inverseOrder);

      // Close inverse order
      inverseOrder.setDocumentStatus("CL");
      inverseOrder.setDocumentAction("--");
      inverseOrder.setProcessed(true);
      inverseOrder.setProcessNow(false);
      OBDal.getInstance().save(inverseOrder);

      // Restore document type of the inverse order
      // inverseOrder.setDocumentType(oldOrder.getDocumentType());

      // Close original order
      oldOrder.setDocumentStatus("CL");
      oldOrder.setDocumentAction("--");
      oldOrder.setProcessed(true);
      oldOrder.setProcessNow(false);
      OBDal.getInstance().save(oldOrder);

      // Set Stardard Order to new order document type

      // Complete new order and generate good shipment and sales invoice
      if (jsonorder == null && replaceOrder) {
        newOrder.setDocumentStatus("DR");
        OBDal.getInstance().save(newOrder);
        callCOrderPost(newOrder);
      }

      // Restore document type of the new order
      // newOrder.setDocumentType(oldOrder.getDocumentType());

      // if (businessPartnerAccount != null && paymentMethod != null) {
      // // Restore Automatic Receipt check
      // accountPaymentMethod.setAutomaticReceipt(originalAutomaticReceipt);
      // }

      // Payment Creation
      // Get the payment schedule detail of the oldOrder
      createPayments(oldOrder, newOrder, inverseOrder, jsonorder, useOrderDocumentNoForRelatedDocs,
          replaceOrder);

      // Return result
      JSONObject result = new JSONObject();

      return result;
    } catch (Exception e1) {
      try {
        OBDal.getInstance().getConnection().rollback();
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      throw new OBException(e3);
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

  protected static Order createOrder(Order oldOrder, String documentNo) {
    Order inverseOrder = (Order) DalUtil.copy(oldOrder, false, true);
    // Change order values
    inverseOrder.setPosted("N");
    inverseOrder.setProcessed(false);
    inverseOrder.setDocumentStatus("DR");
    inverseOrder.setDocumentAction("CO");
    inverseOrder.setGrandTotalAmount(BigDecimal.ZERO);
    inverseOrder.setSummedLineAmount(BigDecimal.ZERO);
    inverseOrder.setOrderDate(today);
    inverseOrder.setScheduledDeliveryDate(today);
    String newDocumentNo = documentNo;
    if (newDocumentNo == null) {
      newDocumentNo = FIN_Utility.getDocumentNo(oldOrder.getDocumentType(), Order.TABLE_NAME);
    }
    inverseOrder.setDocumentNo(newDocumentNo);
    inverseOrder.setCancelledorder(oldOrder);
    OBDal.getInstance().save(inverseOrder);

    return inverseOrder;
  }

  protected static OrderLine createOrderLine(OrderLine oldOrderLine, Order inverseOrder) {
    OrderLine inverseOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
    inverseOrderLine.setSalesOrder(inverseOrder);
    BigDecimal inverseOrderedQuantity = oldOrderLine.getOrderedQuantity().negate();
    inverseOrderLine.setOrderedQuantity(inverseOrderedQuantity);

    // Set inverse order delivered quantity zero
    inverseOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
    inverseOrderLine.setReservedQuantity(BigDecimal.ZERO);
    inverseOrder.getOrderLineList().add(inverseOrderLine);
    OBDal.getInstance().save(inverseOrderLine);
    return inverseOrderLine;
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
      BigDecimal movementQty) {
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
    createMTransaction(newGoodsShipmentLine);

    OBDal.getInstance().save(newGoodsShipmentLine);
    return newGoodsShipmentLine;
  }

  protected static void createReservations(OrderLine replacedOrderLine, OrderLine newOrderLine) {
    OBCriteria<Reservation> reservationCriteria = OBDal.getInstance().createCriteria(
        Reservation.class);
    reservationCriteria
        .add(Restrictions.eq(Reservation.PROPERTY_SALESORDERLINE, replacedOrderLine));
    reservationCriteria.setMaxResults(1);
    Reservation reservation = (Reservation) reservationCriteria.uniqueResult();
    if (reservation != null) {
      Reservation newReservation = (Reservation) DalUtil.copy(reservation, true, true);
      newReservation.setSalesOrderLine(newOrderLine);
      newReservation.setReservedQty(BigDecimal.ZERO);
      newReservation.setReleased(BigDecimal.ZERO);
      OBDal.getInstance().save(newReservation);
    }
  }

  protected static ScrollableResults getOrderLineList(Order order) {
    OBCriteria<OrderLine> orderLinesCriteria = OBDal.getInstance().createCriteria(OrderLine.class);
    orderLinesCriteria.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, order));

    ScrollableResults orderLines = orderLinesCriteria.scroll(ScrollMode.FORWARD_ONLY);
    return orderLines;
  }

  protected static void beforePosting(Order inverseOrder) {
    FIN_PaymentMethod paymentMethod = inverseOrder.getPaymentMethod();
    BusinessPartner businessPartner = inverseOrder.getBusinessPartner();
    FIN_FinancialAccount businessPartnerAccount = businessPartner.getAccount();
    FinAccPaymentMethod accountPaymentMethod = null;
    boolean originalAutomaticReceipt = false;

    // Only disable Automatic Receipt Check if the bussinessPartner has a financial account and
    // the order a paymentMethod
    if (businessPartnerAccount != null && paymentMethod != null) {
      OBCriteria<FinAccPaymentMethod> accountPaymentMethodCriteria = OBDal.getInstance()
          .createCriteria(FinAccPaymentMethod.class);
      accountPaymentMethodCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
          paymentMethod));
      accountPaymentMethodCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT,
          businessPartnerAccount));
      List<FinAccPaymentMethod> accountPaymentMethodList = accountPaymentMethodCriteria.list();
      if (accountPaymentMethodList.size() == 0) {
        throw new OBException("The payment method " + paymentMethod.getName()
            + " is not in the financial account " + businessPartnerAccount.getName());
      } else if (accountPaymentMethodList.size() > 1) {
        throw new OBException("The payment method " + paymentMethod.getName()
            + " appears more than once in the financial account "
            + businessPartnerAccount.getName());
      }
      accountPaymentMethod = accountPaymentMethodList.get(0);

      // Save originalAutomaticReceipt status
      originalAutomaticReceipt = accountPaymentMethod.isAutomaticReceipt();

      // Disable Automatic Receipt check
      accountPaymentMethod.setAutomaticReceipt(false);

      // Change inverse order document type to Standard Order in order to avoid shipment and
      // invoice creation during c_order_post call
      // OBCriteria<DocumentType> standardOrderDocumentTypeCriteria = OBDal.getInstance()
      // .createCriteria(DocumentType.class);
      // standardOrderDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_NAME,
      // "Standard Order"));
      // OrganizationStructureProvider osp = OBContext.getOBContext()
      // .getOrganizationStructureProvider(inverseOrder.getOrganization().getClient().getId());
      // ;
      // List<String> parentOrganizationIdList = osp.getParentList(inverseOrder.getOrganization()
      // .getId(), true);
      //
      // standardOrderDocumentTypeCriteria.add(Restrictions.in(DocumentType.PROPERTY_ORGANIZATION
      // + ".id", parentOrganizationIdList));
      // List<DocumentType> standardOrderDocumentTypeList =
      // standardOrderDocumentTypeCriteria.list();
      // if (standardOrderDocumentTypeList.size() != 1) {
      // throw new OBException(
      // "Only one Standard Order named document can exist for the organization "
      // + inverseOrder.getOrganization().getName());
      // }

      // Set Standard Order to inverse order document type
      // inverseOrder.setDocumentType(standardOrderDocumentTypeList.get(0));
    }
  }

  protected static void createPayments(Order oldOrder, Order newOrder, Order inverseOrder,
      JSONObject jsonorder, boolean useOrderDocumentNoForRelatedDocs, boolean replaceOrder) {
    try {
      FIN_PaymentSchedule paymentSchedule;
      OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteria = OBDal.getInstance().createCriteria(
          FIN_PaymentSchedule.class);
      paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, oldOrder));
      List<FIN_PaymentSchedule> paymentScheduleList = paymentScheduleCriteria.list();
      if (paymentScheduleList.size() != 0) {
        paymentSchedule = paymentScheduleList.get(0);
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
        FIN_Payment newPayment = null;
        ConnectionProvider conn = new DalConnectionProvider();
        VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
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

          if (jsonorder != JSONObject.NULL) {
            // Get Payment DocumentNo
            Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.class);

            if (useOrderDocumentNoForRelatedDocs) {
              paymentDocumentNo = oldOrder.getDocumentNo();
            } else {
              paymentDocumentNo = getDocumentNo(paymentEntity, null, paymentDocumentType);
            }
          }

          // Get Payment Description
          String description = getPaymentDescription();
          description += ": " + inverseOrder.getDocumentNo();

          if (replaceOrder) {
            // Duplicate payment with positive amount
            newPayment = createPayment(newPayment, newOrder, paymentPaymentMethod, amount,
                paymentDocumentType, financialAccount, paymentDocumentNo);
            description += ": " + newOrder.getDocumentNo();
            paymentTotalAmount = paymentTotalAmount.add(amount);
          }

          // Duplicate payment with negative amount
          newPayment = createPayment(newPayment, inverseOrder, paymentPaymentMethod,
              negativeAmount, paymentDocumentType, financialAccount, paymentDocumentNo);
          paymentTotalAmount = paymentTotalAmount.add(negativeAmount);

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
          OBError error = new OBError();
          if (jsonorder != null) {
            FIN_PaymentProcess.doProcessPayment(newPayment, "P", true, null, null);
          } else {
            error = FIN_AddPayment.processPayment(vars, conn, "P", newPayment);
          }
          if (error.getType().equals("Error")) {
            throw new OBException(error.getMessage());
          }
        }

      } else {
        throw new OBException("There is no payment plan for the order: " + oldOrder.getId());
      }

    } catch (Exception e1) {
      try {
        OBDal.getInstance().getConnection().rollback();
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      throw new OBException(e3);
    }
  }

  protected static void createMTransaction(ShipmentInOutLine line) {
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

      OBDal.getInstance().save(transaction);
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

  protected static FIN_Payment createPayment(FIN_Payment payment, Order order,
      FIN_PaymentMethod paymentPaymentMethod, BigDecimal amount, DocumentType paymentDocumentType,
      FIN_FinancialAccount financialAccount, String paymentDocumentNo) throws Exception {
    FIN_Payment newPayment = payment;

    // Get the payment schedule of the order
    FIN_PaymentSchedule paymentSchedule = null;
    OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, order));
    List<FIN_PaymentSchedule> paymentScheduleList = paymentScheduleCriteria.list();
    if (paymentScheduleList.size() == 0) {
      // Create a Payment Schedule if the order hasn't got
      paymentSchedule = OBProvider.getInstance().get(FIN_PaymentSchedule.class);
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
    } else {
      paymentSchedule = paymentScheduleList.get(0);
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
}
