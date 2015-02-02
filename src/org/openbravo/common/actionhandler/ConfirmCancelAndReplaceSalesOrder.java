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
      inverseOrder.setProcessed(false);
      inverseOrder.setPosted("N");
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

      // Create inverse Order lines
      List<OrderLine> oldOrderLineList = oldOrder.getOrderLineList();
      for (OrderLine oldOrderLine : oldOrderLineList) {
        OrderLine inverseOrderLine = (OrderLine) DalUtil.copy(oldOrderLine, false, true);
        inverseOrderLine.setDeliveredQuantity(BigDecimal.ZERO);
        inverseOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
        inverseOrderLine.setSalesOrder(inverseOrder);
        BigDecimal orderedQuantity = inverseOrderLine.getOrderedQuantity();
        BigDecimal inverseOrderedQuantity = orderedQuantity.multiply(new BigDecimal(-1));
        inverseOrderLine.setOrderedQuantity(inverseOrderedQuantity);
        OBDal.getInstance().save(inverseOrderLine);
      }

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
      } else if (accountPaymentMethodCriteria.list().size() > 1) {
        throw new OBException("The payment method " + paymentMethod.getName()
            + " appears more than once in the financial account "
            + businessPartnerAccount.getName());
      }
      FinAccPaymentMethod accountPaymentMethod = accountPaymentMethodList.get(0);

      // Disable Automatic Receipt check
      boolean originalAutomaticReceipt = accountPaymentMethod.isAutomaticReceipt();
      accountPaymentMethod.setAutomaticReceipt(false);

      // Complete inverse order and generate good shipment and sales invoice
      callCOrderPost(inverseOrder);

      // Complete new order and generate good shipment and sales invoice
      newOrder.setDocumentStatus("DR");
      callCOrderPost(newOrder);

      // Restore Automatic Receipt check
      accountPaymentMethod.setAutomaticReceipt(originalAutomaticReceipt);

      // Add inverse order payments
      // List<FIN_PaymentSchedule> paymentScheduleList = oldOrder.getFINPaymentScheduleList();
      // if (paymentScheduleList.size() != 0) {
      // FIN_PaymentSchedule paymentSchedule = paymentScheduleList.get(0);
      //
      // // Get the payment schedule detail of the order
      // OBCriteria<FIN_PaymentScheduleDetail> paymentScheduleDetailCriteria = OBDal.getInstance()
      // .createCriteria(FIN_PaymentScheduleDetail.class);
      // paymentScheduleDetailCriteria.add(Restrictions.eq(
      // FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE, paymentSchedule));
      // // There sould be only one with null paymentdetails
      // paymentScheduleDetailCriteria.add(Restrictions
      // .isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
      // List<FIN_PaymentScheduleDetail> paymentScheduleDetailList = paymentScheduleDetailCriteria
      // .list();
      // if (paymentScheduleDetailList.size() != 0) {
      // HashMap<String, BigDecimal> paymentScheduleDetailAmount = new HashMap<String,
      // BigDecimal>();
      // String paymentScheduleDetailId = paymentScheduleDetailList.get(0).getId();
      // BigDecimal paymentAmount = (BigDecimal) jsonPayment.get("amount");
      // paymentScheduleDetailAmount.put(paymentScheduleDetailId, paymentAmount);
      //
      // BigDecimal zeroBigDecimal = new BigDecimal(0);
      //
      // // Call to savePayment in order to create a new payment in
      // FIN_Payment payment = FIN_AddPayment.savePayment(null, true, documentType,
      // paymentDocumentNo, order.getBusinessPartner(), paymentMethod, financialAccount,
      // paymentAmount.toPlainString(), order.getOrderDate(), order.getOrganization(), null,
      // paymentScheduleDetailList, paymentScheduleDetailAmount, false, false,
      // order.getCurrency(), zeroBigDecimal, zeroBigDecimal);
      //
      // // Call to processPayment in order to process it
      // ConnectionProvider conn = new DalConnectionProvider();
      // VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      // OBError error = FIN_AddPayment.processPayment(vars, conn, "P", payment);
      // if (error.getType().equals("Error")) {
      // errorMessage = errorMessage + "\nError durante el procesado del pago: "
      // + error.getMessage();
      // }
      // } else {
      // BusinessPartner businessPatner = (BusinessPartner) matchedJsonOrder
      // .get("businessPartner");
      // FIN_FinancialAccount businessPartnerFinancialAccount = businessPatner.getAccount();
      // if (businessPartnerFinancialAccount != null) {
      // errorMessage = errorMessage + "\nEl check \"Cobro automático\" del método de pago \""
      // + paymentMethod.getName() + "\" de la cuenta financiera \""
      // + businessPatner.getAccount().getName() + "\" debe estar desactivado";
      // } else {
      // errorMessage = errorMessage + "\nEl tercero " + businessPatner.getName()
      // + " no tiene una cuenta financiera asociada";
      // }
      // }
      // } else {
      // errorMessage = errorMessage + "\nNo existe ningún plan de cobro para el pedido: "
      // + order.getId();
      // }

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
