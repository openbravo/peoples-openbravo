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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
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
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class ConfirmCancelAndReplaceSalesOrder extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) throws OBException {
    try {
      // Get request parameters
      JSONObject request = new JSONObject(content);
      String newOrderId = request.getString("inpcOrderId");

      // Get new Order
      Order newOrder = OBDal.getInstance().get(Order.class, newOrderId);

      // Get old Order
      Order oldOrder = newOrder.getReplacedorder();

      // Create inverse Order header
      Order inverseOrder = (Order) DalUtil.copy(oldOrder, false, true);
      // Change order values
      inverseOrder.setPosted("N");
      inverseOrder.setProcessed(false);
      inverseOrder.setDocumentStatus("DR");
      inverseOrder.setDocumentAction("CO");
      inverseOrder.setGrandTotalAmount(BigDecimal.ZERO);
      inverseOrder.setSummedLineAmount(BigDecimal.ZERO);
      Date today = new Date();
      inverseOrder.setOrderDate(today);
      inverseOrder.setScheduledDeliveryDate(today);
      String newDocumentNo = FIN_Utility
          .getDocumentNo(oldOrder.getDocumentType(), Order.TABLE_NAME);
      inverseOrder.setDocumentNo(newDocumentNo);
      inverseOrder.setCancelledorder(oldOrder);
      OBDal.getInstance().save(inverseOrder);

      // Define netting goods shipment and its lines
      ShipmentInOut nettingGoodsShipment = null;
      ShipmentInOutLine newGoodsShipmentLine1 = null;
      ShipmentInOutLine newGoodsShipmentLine2 = null;

      // Iterate old order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
      long lineNoCounter = 1;
      for (OrderLine oldOrderLine : oldOrderLineList) {
        // Set old order delivered quantity zero
        BigDecimal orderedQuantity = oldOrderLine.getOrderedQuantity();
        oldOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        OBDal.getInstance().save(oldOrderLine);

        // Create inverse Order lines
        OrderLine inverseOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        inverseOrderLine.setSalesOrder(inverseOrder);
        BigDecimal inverseOrderedQuantity = orderedQuantity.negate();
        inverseOrderLine.setOrderedQuantity(inverseOrderedQuantity);

        // Set inverse order delivered quantity zero
        inverseOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        OBDal.getInstance().save(inverseOrderLine);
        OBDal.getInstance().flush();

        // Create new lines on the nettingGoodsShipment
        OBCriteria<ShipmentInOutLine> goodsShipmentLineCriteria = OBDal.getInstance()
            .createCriteria(ShipmentInOutLine.class);
        goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
            oldOrderLine));
        goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_LINENO,
            oldOrderLine.getLineNo()));
        List<ShipmentInOutLine> goodsShipmentLineList = goodsShipmentLineCriteria.list();
        if (goodsShipmentLineList.size() == 0) {
          // Line without shipment
          // If nettingGoodsShipment has not been initialized, initialize it
          if (nettingGoodsShipment == null) {
            // Create new Shipment
            nettingGoodsShipment = OBProvider.getInstance().get(ShipmentInOut.class);
            nettingGoodsShipment.setOrganization(oldOrder.getOrganization());
            // Set Document Type
            OBCriteria<DocumentType> goodsShipmentDocumentTypeCriteria = OBDal.getInstance()
                .createCriteria(DocumentType.class);
            OBCriteria<Table> goodsShipmentTableCriteria = OBDal.getInstance().createCriteria(
                Table.class);
            goodsShipmentTableCriteria.add(Restrictions.eq(Table.PROPERTY_DBTABLENAME,
                ShipmentInOut.TABLE_NAME));
            List<Table> goodsShipmentTableList = goodsShipmentTableCriteria.list();
            if (goodsShipmentTableList.size() != 1) {
              throw new OBException("Only one table named M_InOut can exists");
            }
            Table goodsShipmentTable = goodsShipmentTableList.get(0);
            goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_TABLE,
                goodsShipmentTable));
            goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(
                DocumentType.PROPERTY_SALESTRANSACTION, true));
            goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(
                DocumentType.PROPERTY_ORGANIZATION, oldOrder.getOrganization()));
            goodsShipmentDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_ACTIVE,
                true));
            goodsShipmentDocumentTypeCriteria.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
            List<DocumentType> goodsShipmentDocumentTypeList = goodsShipmentDocumentTypeCriteria
                .list();
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
            nettingGoodsShipment.setMovementDate(today);
            nettingGoodsShipment.setAccountingDate(today);
            nettingGoodsShipment.setSalesOrder(null);
            nettingGoodsShipment.setPosted("N");
            nettingGoodsShipment.setProcessed(false);
            nettingGoodsShipment.setDocumentStatus("DR");
            nettingGoodsShipment.setDocumentAction("CO");
            OBDal.getInstance().flush();
            String nettingGoodsShipmentDocumentNo = FIN_Utility.getDocumentNo(
                nettingGoodsShipment.getDocumentType(), ShipmentInOut.TABLE_NAME);
            nettingGoodsShipment.setDocumentNo(nettingGoodsShipmentDocumentNo);
            OBDal.getInstance().save(nettingGoodsShipment);
          }

          // For the oldOrderLine
          newGoodsShipmentLine1 = OBProvider.getInstance().get(ShipmentInOutLine.class);
          newGoodsShipmentLine1.setOrganization(oldOrderLine.getOrganization());
          newGoodsShipmentLine1.setProduct(oldOrderLine.getProduct());
          newGoodsShipmentLine1.setUOM(oldOrderLine.getUOM());
          // Get first storage bin
          Locator locator1 = nettingGoodsShipment.getWarehouse().getLocatorList().get(0);
          newGoodsShipmentLine1.setStorageBin(locator1);

          // For the inverseOrderLine
          newGoodsShipmentLine2 = (ShipmentInOutLine) DalUtil.copy(newGoodsShipmentLine1, false,
              true);
        } else if (goodsShipmentLineList.size() != 1) {
          throw new OBException("More than one goods shipment lines associated to a order line");
        } else {
          ShipmentInOutLine goodsShipmentLine = goodsShipmentLineList.get(0);

          // If nettingGoodsShipment has not been initialized, initialize it
          if (nettingGoodsShipment == null) {
            // Copy existing shipment
            nettingGoodsShipment = (ShipmentInOut) DalUtil.copy(
                goodsShipmentLine.getShipmentReceipt(), false, true);
            nettingGoodsShipment.setMovementDate(today);
            nettingGoodsShipment.setAccountingDate(today);
            nettingGoodsShipment.setSalesOrder(null);
            nettingGoodsShipment.setPosted("N");
            nettingGoodsShipment.setProcessed(false);
            nettingGoodsShipment.setDocumentStatus("DR");
            nettingGoodsShipment.setDocumentAction("CO");
            OBDal.getInstance().flush();
            String nettingGoodsShipmentDocumentNo = FIN_Utility.getDocumentNo(
                nettingGoodsShipment.getDocumentType(), ShipmentInOut.TABLE_NAME);
            nettingGoodsShipment.setDocumentNo(nettingGoodsShipmentDocumentNo);
            OBDal.getInstance().save(nettingGoodsShipment);
          }

          // For the oldOrderLine
          newGoodsShipmentLine1 = (ShipmentInOutLine) DalUtil.copy(goodsShipmentLine, false, true);

          // For the inverseOrderLine
          newGoodsShipmentLine2 = (ShipmentInOutLine) DalUtil.copy(goodsShipmentLine, false, true);

        }
        // For the oldOrderLine
        newGoodsShipmentLine1.setLineNo(10 * lineNoCounter + 1);
        newGoodsShipmentLine1.setSalesOrderLine(oldOrderLine);
        newGoodsShipmentLine1.setShipmentReceipt(nettingGoodsShipment);
        OBDal.getInstance().save(newGoodsShipmentLine1);
        OBDal.getInstance().flush();
        newGoodsShipmentLine1.setMovementQuantity(orderedQuantity);
        OBDal.getInstance().save(newGoodsShipmentLine1);
        OBDal.getInstance().flush();

        // Set old order delivered quantity to the ordered quantity
        oldOrderLine.setDeliveredQuantity(orderedQuantity);
        OBDal.getInstance().save(oldOrderLine);
        OBDal.getInstance().flush();

        // For the inverseOrderLine
        newGoodsShipmentLine2.setLineNo(10 * lineNoCounter);
        newGoodsShipmentLine2.setSalesOrderLine(inverseOrderLine);
        newGoodsShipmentLine2.setShipmentReceipt(nettingGoodsShipment);
        OBDal.getInstance().save(newGoodsShipmentLine2);
        OBDal.getInstance().flush();
        newGoodsShipmentLine2.setMovementQuantity(inverseOrderedQuantity);
        OBDal.getInstance().save(newGoodsShipmentLine2);
        OBDal.getInstance().flush();

        // Set inverse order delivered quantity to zero
        inverseOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        OBDal.getInstance().save(inverseOrderLine);
        OBDal.getInstance().flush();

        // Increase counter
        lineNoCounter++;
      }

      // Assign the original goods shipment lines to the new order lines
      List<OrderLine> newOrderLineList = newOrder.getOrderLineList();
      for (OrderLine newOrderLine : newOrderLineList) {
        OrderLine replacedOrderLine = newOrderLine.getReplacedorderline();
        if (replacedOrderLine != null) {
          // Manage reservations
          OBCriteria<Reservation> reservationCriteria = OBDal.getInstance().createCriteria(
              Reservation.class);
          reservationCriteria.add(Restrictions.eq(Reservation.PROPERTY_SALESORDERLINE,
              replacedOrderLine));
          reservationCriteria.setMaxResults(1);
          Reservation reservation = (Reservation) reservationCriteria.uniqueResult();
          if (reservation != null) {
            Reservation newReservation = (Reservation) DalUtil.copy(reservation, true, true);
            newReservation.setSalesOrderLine(newOrderLine);
            newReservation.setReservedQty(BigDecimal.ZERO);
            newReservation.setReleased(BigDecimal.ZERO);
            OBDal.getInstance().save(newReservation);
          }

          newOrderLine.setDeliveredQuantity(replacedOrderLine.getDeliveredQuantity());

          // Set old order delivered quantity zero
          replacedOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
          OBDal.getInstance().save(replacedOrderLine);
          OBDal.getInstance().flush();

          newOrderLine.setInvoicedQuantity(replacedOrderLine.getInvoicedQuantity());
          OBCriteria<ShipmentInOutLine> goodsShipmentLineCriteria = OBDal.getInstance()
              .createCriteria(ShipmentInOutLine.class);
          goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
              replacedOrderLine));
          goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_LINENO,
              replacedOrderLine.getLineNo()));
          goodsShipmentLineCriteria.addOrderBy(ShipmentInOutLine.PROPERTY_UPDATED, true);
          List<ShipmentInOutLine> goodsShipmentLineList = goodsShipmentLineCriteria.list();
          if (goodsShipmentLineList.size() < 1) {
            // Line without shipment
          } else if (goodsShipmentLineList.size() > 1) {
            throw new OBException("More than two goods shipment lines associated to a order line");
          } else {
            ShipmentInOutLine goodsShipmentLine = goodsShipmentLineList.get(0);
            ShipmentInOut goodsShipment = goodsShipmentLine.getShipmentReceipt();
            goodsShipment.setPosted("N");
            goodsShipment.setProcessed(false);
            OBDal.getInstance().save(goodsShipment);
            OBDal.getInstance().flush();
            // Assign old shipment header to the new order
            goodsShipment.setSalesOrder(newOrder);
            // Assign old shipment line to the new order line
            goodsShipmentLine.setSalesOrderLine(newOrderLine);
            OBDal.getInstance().save(goodsShipmentLine);
            OBDal.getInstance().flush();
            // Restore flags
            goodsShipment.setProcessed(true);
            goodsShipment.setPosted("Y");
            OBDal.getInstance().save(goodsShipment);
            OBDal.getInstance().flush();
          }
        }
      }

      // Get accountPaymentMethod in order to avoid automatic payment creation during c_order_post
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
        accountPaymentMethodCriteria.add(Restrictions.eq(
            FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
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

      // Complete inverse order
      callCOrderPost(inverseOrder);

      // Complete nettingGoodsShipment
      callMInoutPostPost(nettingGoodsShipment);

      // Close inverse order
      inverseOrder.setDocumentAction("CL");
      OBDal.getInstance().save(inverseOrder);
      callCOrderPost(inverseOrder);

      // Restore document type of the inverse order
      // inverseOrder.setDocumentType(oldOrder.getDocumentType());

      // Close original order
      oldOrder.setDocumentAction("CL");
      OBDal.getInstance().save(oldOrder);
      callCOrderPost(oldOrder);

      // Set Stardard Order to new order document type

      // Complete new order and generate good shipment and sales invoice
      newOrder.setDocumentStatus("DR");
      OBDal.getInstance().save(newOrder);
      callCOrderPost(newOrder);

      // Restore document type of the new order
      // newOrder.setDocumentType(oldOrder.getDocumentType());

      if (businessPartnerAccount != null && paymentMethod != null) {
        // Restore Automatic Receipt check
        accountPaymentMethod.setAutomaticReceipt(originalAutomaticReceipt);
      }

      // Payment Creation
      // Get the payment schedule detail of the oldOrder
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
          FIN_PaymentDetail paymentDetail = paymentScheduleDetail.getPaymentDetails();
          FIN_Payment payment = paymentDetail.getFinPayment();
          FIN_PaymentMethod paymentPaymentMethod = payment.getPaymentMethod();
          BigDecimal amount = payment.getAmount();
          BigDecimal negativeAmount = amount.negate();
          DocumentType paymentDocumentType = payment.getDocumentType();
          FIN_FinancialAccount financialAccount = payment.getAccount();

          // Duplicate payment with positive amount
          newPayment = createPayment(newPayment, newOrder, paymentPaymentMethod, amount,
              paymentDocumentType, financialAccount);

          // Duplicate payment with negative amount
          newPayment = createPayment(newPayment, inverseOrder, paymentPaymentMethod,
              negativeAmount, paymentDocumentType, financialAccount);

          // Set amount and used credit to zero
          newPayment.setAmount(BigDecimal.ZERO);
          newPayment.setUsedCredit(BigDecimal.ZERO);
          OBDal.getInstance().save(newPayment);

          // Call to processPayment in order to process it
          OBError error = FIN_AddPayment.processPayment(vars, conn, "P", newPayment);
          if (error.getType().equals("Error")) {
            throw new OBException(error.getMessage());
          }
        }

        // Create if needed a second payment for the partially paid
        BigDecimal outstandingAmount = paymentSchedule.getOutstandingAmount();
        if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {
          BigDecimal negativeOutstandingAmount = outstandingAmount.negate();

          OBCriteria<DocumentType> arReceiptDocumentTypeCriteria = OBDal.getInstance()
              .createCriteria(DocumentType.class);
          OBCriteria<Table> paymentTableCriteria = OBDal.getInstance().createCriteria(Table.class);
          paymentTableCriteria.add(Restrictions.eq(Table.PROPERTY_DBTABLENAME,
              FIN_Payment.TABLE_NAME));
          List<Table> paymentTableList = paymentTableCriteria.list();
          if (paymentTableList.size() != 1) {
            throw new OBException("Only one table named FIN_Payment can exists");
          }
          Table paymentTable = paymentTableList.get(0);
          arReceiptDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_TABLE,
              paymentTable));
          arReceiptDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_SALESTRANSACTION,
              true));
          // Parent organization list
          OrganizationStructureProvider osp = OBContext.getOBContext()
              .getOrganizationStructureProvider(oldOrder.getOrganization().getClient().getId());
          ;
          List<String> parentOrganizationIdList = osp.getParentList(oldOrder.getOrganization()
              .getId(), true);

          arReceiptDocumentTypeCriteria.add(Restrictions.in(DocumentType.PROPERTY_ORGANIZATION
              + ".id", parentOrganizationIdList));
          arReceiptDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_ACTIVE, true));
          arReceiptDocumentTypeCriteria.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
          List<DocumentType> arReceiptDocumentTypeList = arReceiptDocumentTypeCriteria.list();
          if (arReceiptDocumentTypeList.size() == 0) {
            throw new OBException("No document type found for the new payment");
          }
          DocumentType paymentDocumentType = arReceiptDocumentTypeList.get(0);
          FIN_FinancialAccount financialAccount = null;
          FIN_PaymentMethod paymentPaymentMethod = null;
          if (newPayment != null) {
            financialAccount = newPayment.getAccount();
            paymentPaymentMethod = newPayment.getPaymentMethod();
          } else {
            if (oldOrder.getBusinessPartner().getAccount() != null) {
              financialAccount = oldOrder.getBusinessPartner().getAccount();
            } else {
              throw new OBException("The business partner has not a finnancial account defined");
            }
            if (oldOrder.getBusinessPartner().getPaymentMethod() != null) {
              paymentPaymentMethod = oldOrder.getBusinessPartner().getPaymentMethod();
            } else {
              throw new OBException("The business partner has not a payment method defined");
            }
          }
          FIN_Payment newPayment2 = null;

          // Duplicate payment with negative amount
          newPayment2 = createPayment(newPayment2, inverseOrder, paymentPaymentMethod,
              negativeOutstandingAmount, paymentDocumentType, financialAccount);

          // Duplicate payment with positive amount
          newPayment2 = createPayment(newPayment2, oldOrder, paymentPaymentMethod,
              outstandingAmount, paymentDocumentType, financialAccount);

          // Set amount and used credit to zero
          newPayment2.setAmount(BigDecimal.ZERO);
          newPayment2.setUsedCredit(BigDecimal.ZERO);
          OBDal.getInstance().save(newPayment2);

          // Call to processPayment in order to process it
          if (newPayment2 != null) {
            OBError error2 = FIN_AddPayment.processPayment(vars, conn, "P", newPayment2);
            if (error2.getType().equals("Error")) {
              throw new OBException(error2.getMessage());
            }
          }
        }

      } else {
        throw new OBException("There is no payment plan for the order: " + oldOrder.getId());
      }

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
    }
  }

  private static void callCOrderPost(Order order) throws OBException {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(order.getId());
    final String procedureName = "c_order_post1";
    CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
  }

  private static void callMInoutPostPost(ShipmentInOut shipment) throws OBException {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(shipment.getId());
    final String procedureName = "m_inout_post";
    CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
  }

  private static FIN_Payment createPayment(FIN_Payment payment, Order order,
      FIN_PaymentMethod paymentPaymentMethod, BigDecimal amount, DocumentType paymentDocumentType,
      FIN_FinancialAccount financialAccount) throws Exception {
    String paymentDocumentNo = null;

    // Get the payment schedule of the order
    FIN_PaymentSchedule paymentSchedule;
    OBCriteria<FIN_PaymentSchedule> paymentScheduleCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    paymentScheduleCriteria.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_ORDER, order));
    List<FIN_PaymentSchedule> paymentScheduleList = paymentScheduleCriteria.list();
    if (paymentScheduleList.size() != 0) {
      paymentSchedule = paymentScheduleList.get(0);

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
          FIN_Payment returnPayment = FIN_AddPayment.savePayment(payment, true,
              paymentDocumentType, paymentDocumentNo, order.getBusinessPartner(),
              paymentPaymentMethod, financialAccount, amount.toPlainString(), order.getOrderDate(),
              order.getOrganization(), null, paymentScheduleDetailList,
              paymentScheduleDetailAmount, false, false, order.getCurrency(), BigDecimal.ZERO,
              BigDecimal.ZERO);
          return returnPayment;
        }
        // Create a new line
        else {
          FIN_AddPayment.updatePaymentDetail(paymentScheduleDetailList.get(0), payment, amount,
              false);
          return payment;
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

        // Continue with the payment
        HashMap<String, BigDecimal> paymentScheduleDetailAmount = new HashMap<String, BigDecimal>();
        String paymentScheduleDetailId = paymentScheduleDetail.getId();
        paymentScheduleDetailAmount.put(paymentScheduleDetailId, amount);
        if (payment == null) {
          // Call to savePayment in order to create a new payment in
          FIN_Payment returnPayment = FIN_AddPayment.savePayment(payment, true,
              paymentDocumentType, paymentDocumentNo, order.getBusinessPartner(),
              paymentPaymentMethod, financialAccount, amount.toPlainString(), order.getOrderDate(),
              order.getOrganization(), null, paymentScheduleDetailList,
              paymentScheduleDetailAmount, false, false, order.getCurrency(), BigDecimal.ZERO,
              BigDecimal.ZERO);
          return returnPayment;
        }
        // Create a new line
        else {
          FIN_AddPayment.updatePaymentDetail(paymentScheduleDetail, payment, amount, false);
          return payment;
        }
      }
    } else {
      throw new OBException("There is no payment plan for the order: " + order.getId());
    }
  }
}
