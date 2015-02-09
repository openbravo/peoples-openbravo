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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
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

      // Define netting goods shipment
      ShipmentInOut nettingGoodsShipment = null;

      // Iterate old order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
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
        } else if (goodsShipmentLineList.size() != 1) {
          throw new OBException("More than one goods shipment lines associated to a order line");
        } else {
          ShipmentInOutLine goodsShipmentLine = goodsShipmentLineList.get(0);

          // If nettingGoodsShipment has not been initialized, initialize it
          if (nettingGoodsShipment == null) {
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
          ShipmentInOutLine newGoodsShipmentLine1 = (ShipmentInOutLine) DalUtil.copy(
              goodsShipmentLine, false, true);
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
          ShipmentInOutLine newGoodsShipmentLine2 = (ShipmentInOutLine) DalUtil.copy(
              goodsShipmentLine, false, true);
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
        }
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
          if (goodsShipmentLineList.size() == 0) {
            // Line without shipment
          } else if (goodsShipmentLineList.size() != 2) {
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

      // Complete nettingGoodsShipment

      // Complete inverse order
      callCOrderPost(inverseOrder);
      // Close inverse order
      inverseOrder.setDocumentAction("CL");
      OBDal.getInstance().save(inverseOrder);
      callCOrderPost(inverseOrder);

      // TODO

      // Complete new order and generate good shipment and sales invoice
      newOrder.setDocumentStatus("DR");
      OBDal.getInstance().save(newOrder);
      callCOrderPost(newOrder);

      // Restore Automatic Receipt check
      accountPaymentMethod.setAutomaticReceipt(originalAutomaticReceipt);

      // TODO Payments

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
}
