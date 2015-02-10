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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
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
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class ConfirmCancelAndReplaceSalesOrder extends BaseProcessActionHandler {
  private static final Logger log4j = Logger.getLogger(ConfirmCancelAndReplaceSalesOrder.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) throws OBException {
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
      String newDocumentNo = FIN_Utility.getDocumentNo(oldOrder.getDocumentType(), "C_Order");
      inverseOrder.setDocumentNo(newDocumentNo);
      inverseOrder.setCancelledorder(oldOrder);
      OBDal.getInstance().save(inverseOrder);

      // Define netting goods shipment and its lines
      ShipmentInOut nettingGoodsShipment = null;
      ShipmentInOutLine newGoodsShipmentLine1 = null;
      ShipmentInOutLine newGoodsShipmentLine2 = null;

      // Iterate old order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
      int lineNoCounter = 1;
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
            nettingGoodsShipment.setOrganization(oldOrderLine.getOrganization());
            // ¿Set Document Type?
            // TODO
            nettingGoodsShipment.setWarehouse(oldOrderLine.getWarehouse());
            nettingGoodsShipment.setBusinessPartner(oldOrderLine.getBusinessPartner());
            nettingGoodsShipment.setPartnerAddress(oldOrderLine.getPartnerAddress());
            nettingGoodsShipment.setMovementDate(today);
            nettingGoodsShipment.setAccountingDate(today);
            nettingGoodsShipment.setSalesOrder(null);
            nettingGoodsShipment.setPosted("N");
            nettingGoodsShipment.setProcessed(false);
            OBDal.getInstance().flush();
            String nettingGoodsShipmentDocumentNo = FIN_Utility.getDocumentNo(
                nettingGoodsShipment.getDocumentType(), "M_InOut");
            nettingGoodsShipment.setDocumentNo(nettingGoodsShipmentDocumentNo);
            OBDal.getInstance().save(nettingGoodsShipment);
          }

          // For the oldOrderLine
          newGoodsShipmentLine1 = OBProvider.getInstance().get(ShipmentInOutLine.class);
          // TODO
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
            OBDal.getInstance().flush();
            String nettingGoodsShipmentDocumentNo = FIN_Utility.getDocumentNo(
                nettingGoodsShipment.getDocumentType(), "M_InOut");
            nettingGoodsShipment.setDocumentNo(nettingGoodsShipmentDocumentNo);
            OBDal.getInstance().save(nettingGoodsShipment);
          }

          // For the oldOrderLine
          newGoodsShipmentLine1 = (ShipmentInOutLine) DalUtil.copy(goodsShipmentLine, false, true);

          // For the inverseOrderLine
          newGoodsShipmentLine2 = (ShipmentInOutLine) DalUtil.copy(goodsShipmentLine, false, true);

        }
        // For the oldOrderLine
        newGoodsShipmentLine1.setLineNo(new Long(10 * lineNoCounter));
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

        // For the inverseOrderLine
        newGoodsShipmentLine2.setLineNo(new Long(10 * lineNoCounter));
        newGoodsShipmentLine2.setSalesOrderLine(inverseOrderLine);
        newGoodsShipmentLine2.setShipmentReceipt(nettingGoodsShipment);
        OBDal.getInstance().save(newGoodsShipmentLine2);
        OBDal.getInstance().flush();
        newGoodsShipmentLine2.setMovementQuantity(inverseOrderedQuantity);
        OBDal.getInstance().save(newGoodsShipmentLine2);
        OBDal.getInstance().flush();

        // Set inverse order delivered quantity to the ordered quantity
        inverseOrderLine.setDeliveredQuantity(inverseOrderedQuantity);
        OBDal.getInstance().save(inverseOrderLine);

        // Increase counter
        lineNoCounter++;
      }

      // Assign the original goods shipment lines to the new order lines
      List<OrderLine> newOrderLineList = newOrder.getOrderLineList();
      for (OrderLine newOrderLine : newOrderLineList) {
        OrderLine replacedOrderLine = newOrderLine.getReplacedorderline();
        if (replacedOrderLine != null) {
          newOrderLine.setDeliveredQuantity(replacedOrderLine.getDeliveredQuantity());
          newOrderLine.setInvoicedQuantity(replacedOrderLine.getInvoicedQuantity());
          OBCriteria<ShipmentInOutLine> goodsShipmentLineCriteria = OBDal.getInstance()
              .createCriteria(ShipmentInOutLine.class);
          goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
              replacedOrderLine));
          goodsShipmentLineCriteria.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_LINENO,
              replacedOrderLine.getLineNo()));
          goodsShipmentLineCriteria.addOrderBy(ShipmentInOutLine.PROPERTY_UPDATED, true);
          List<ShipmentInOutLine> goodsShipmentLineList = goodsShipmentLineCriteria.list();
          if (goodsShipmentLineList.size() < 2) {
            // Line without shipment
          } else if (goodsShipmentLineList.size() > 2) {
            throw new OBException("More than two goods shipment lines associated to a order line");
          } else {
            ShipmentInOutLine goodsShipmentLine = goodsShipmentLineList.get(0);
            ShipmentInOut goodsShipment = goodsShipmentLine.getShipmentReceipt();
            goodsShipment.setPosted("N");
            goodsShipment.setProcessed(false);
            OBDal.getInstance().save(goodsShipment);
            OBDal.getInstance().flush();
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

      // Create if needed a new goods shipment
      // TODO?

      // Get accountPaymentMethod in order to avoid automatic payment creation during c_order_post
      FIN_PaymentMethod paymentMethod = inverseOrder.getPaymentMethod();
      if (paymentMethod == null) {
        throw new OBException("The business partner has not a payment method defined");
      }
      BusinessPartner businessPartner = inverseOrder.getBusinessPartner();
      FIN_FinancialAccount businessPartnerAccount = businessPartner.getAccount();
      if (businessPartnerAccount == null) {
        throw new OBException("The business partner has not a finnancial account defined");
      }
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
      FinAccPaymentMethod accountPaymentMethod = accountPaymentMethodList.get(0);

      // Disable Automatic Receipt check
      boolean originalAutomaticReceipt = accountPaymentMethod.isAutomaticReceipt();
      accountPaymentMethod.setAutomaticReceipt(false);

      // Change inverse order document type to Standard Order in order to avoid shipment and
      // invoice creation during c_order_post call
      OBCriteria<DocumentType> standardOrderDocumentTypeCriteria = OBDal.getInstance()
          .createCriteria(DocumentType.class);
      standardOrderDocumentTypeCriteria.add(Restrictions.eq(DocumentType.PROPERTY_NAME,
          "Standard Order"));
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(inverseOrder.getOrganization().getClient().getId());
      ;
      List<String> parentOrganizationIdList = osp.getParentList(inverseOrder.getOrganization()
          .getId(), true);
      // TODO Ya tenemos el listado de ids organizaciones

      standardOrderDocumentTypeCriteria.add(Restrictions.in(DocumentType.PROPERTY_ORGANIZATION
          + ".id", parentOrganizationIdList));
      List<DocumentType> standardOrderDocumentTypeList = standardOrderDocumentTypeCriteria.list();
      if (standardOrderDocumentTypeList.size() != 1) {
        throw new OBException(
            "Only one Standar Order named document can exist for the organization "
                + inverseOrder.getOrganization().getName());
      }
      inverseOrder.setDocumentType(standardOrderDocumentTypeList.get(0));

      // Complete inverse order
      callCOrderPost(inverseOrder);
      // Close inverse order
      inverseOrder.setDocumentAction("CL");
      OBDal.getInstance().save(inverseOrder);
      callCOrderPost(inverseOrder);

      // Restore document type of the inverse order
      inverseOrder.setDocumentType(oldOrder.getDocumentType());

      // Complete new order and generate good shipment and sales invoice
      newOrder.setDocumentStatus("DR");
      OBDal.getInstance().save(newOrder);
      callCOrderPost(newOrder);

      // Restore Automatic Receipt check
      accountPaymentMethod.setAutomaticReceipt(originalAutomaticReceipt);

      // TODO Payment Creation
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
        // There should be only one with null paymentDetails
        paymentScheduleDetailCriteria.add(Restrictions
            .isNotNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
        List<FIN_PaymentScheduleDetail> paymentScheduleDetailList = paymentScheduleDetailCriteria
            .list();
        // New payment definition
        FIN_Payment newPayment = null;
        for (FIN_PaymentScheduleDetail paymentScheduleDetail : paymentScheduleDetailList) {
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
        }
        // Call to processPayment in order to process it
        ConnectionProvider conn = new DalConnectionProvider();
        VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
        OBError error = FIN_AddPayment.processPayment(vars, conn, "P", newPayment);
        if (error.getType().equals("Error")) {
          throw new OBException(error.getMessage());
        }

        // Create if needed a second payment for the partially paid
        // TODO
        // Si ya hay un pago pillar el del pago, de lo contrario pillar el del tercero
        BigDecimal outstandingAmount = paymentSchedule.getOutstandingAmount();
        if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {
          OBCriteria<FIN_PaymentScheduleDetail> paymentScheduleDetailCriteria2 = OBDal
              .getInstance().createCriteria(FIN_PaymentScheduleDetail.class);
          paymentScheduleDetailCriteria2.add(Restrictions.eq(
              FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
          // There should be only one with null paymentDetails
          paymentScheduleDetailCriteria.add(Restrictions
              .isNotNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
          List<FIN_PaymentScheduleDetail> paymentScheduleDetailList2 = paymentScheduleDetailCriteria2
              .list();
          FIN_Payment newPayment2 = null;
        }

      } else {
        throw new OBException("There is no payment plan for the order: " + oldOrder.getId());
      }

      // Return result
      JSONObject result = new JSONObject();

      return result;
    } catch (JSONException e) {
      log4j.error("Error in process", e);
      return new JSONObject();
    } catch (Exception e1) {
      try {
        OBDal.getInstance().getConnection().rollback();
      } catch (Exception e2) {
        throw new OBException(e2);
      }
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      log4j.error(e3);
      return new JSONObject();
    }
  }

  private static void callCOrderPost(Order order) throws OBException {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(order.getId());
    final String procedureName = "c_order_post1";
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
        BigDecimal paymentAmount = amount;
        paymentScheduleDetailAmount.put(paymentScheduleDetailId, paymentAmount);

        if (payment == null) {
          // Call to savePayment in order to create a new payment in
          FIN_Payment returnPayment = FIN_AddPayment.savePayment(payment, true,
              paymentDocumentType, paymentDocumentNo, order.getBusinessPartner(),
              paymentPaymentMethod, financialAccount, paymentAmount.toPlainString(),
              order.getOrderDate(), order.getOrganization(), null, paymentScheduleDetailList,
              paymentScheduleDetailAmount, false, false, order.getCurrency(), BigDecimal.ZERO,
              BigDecimal.ZERO);
          return returnPayment;
        }
        // Create a new line
        else {
          FIN_AddPayment.updatePaymentDetail(paymentScheduleDetailList.get(0), payment,
              paymentAmount, false);
          return payment;
        }

      } else {
        BusinessPartner businessPatner = order.getBusinessPartner();
        FIN_FinancialAccount businessPartnerFinancialAccount = businessPatner.getAccount();
        if (businessPartnerFinancialAccount != null) {
          throw new OBException("The payments have been already created");
        } else {
          throw new OBException("The business partner has not a financial account asociated");
        }
      }
    } else {
      throw new OBException("There is no payment plan for the order: " + order.getId());
    }
  }
}
