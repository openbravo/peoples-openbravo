/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonarray) throws JSONException, ServletException {

    JSONArray respArray = new JSONArray();
    OBContext.setAdminMode(true);
    final String orderId = jsonarray.getString("orderId");
    try {
      TriggerHandler.getInstance().disable();
      Order order = OBDal.getInstance().get(Order.class, orderId);
      order.setObposIslayaway(false);
      order.setDocumentStatus("CL");
      order.setGrandTotalAmount(BigDecimal.ZERO);
      order.setSummedLineAmount(BigDecimal.ZERO);
      for (int i = 0; i < order.getOrderLineList().size(); i++) {
        ((OrderLine) order.getOrderLineList().get(i)).setOrderedQuantity(BigDecimal.ZERO);
        ((OrderLine) order.getOrderLineList().get(i)).setLineNetAmount(BigDecimal.ZERO);
        ((OrderLine) order.getOrderLineList().get(i)).setLineGrossAmount(BigDecimal.ZERO);
      }
      FIN_PaymentSchedule paymentSchedule = (FIN_PaymentSchedule) order.getFINPaymentScheduleList()
          .get(0);
      paymentSchedule.setAmount(BigDecimal.ZERO);
      paymentSchedule.setPaidAmount(BigDecimal.ZERO);
      paymentSchedule.setOutstandingAmount(BigDecimal.ZERO);
      List<FIN_PaymentScheduleDetail> negativePayments = new ArrayList<FIN_PaymentScheduleDetail>();

      for (int i = 0; i < paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList()
          .size(); i++) {

        FIN_PaymentScheduleDetail paymentScheduleDetail = paymentSchedule
            .getFINPaymentScheduleDetailOrderPaymentScheduleList().get(i);

        FIN_PaymentScheduleDetail newPaymentScheduleDetail = OBProvider.getInstance().get(
            FIN_PaymentScheduleDetail.class);
        newPaymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
        newPaymentScheduleDetail.setAmount(paymentScheduleDetail.getAmount().negate());
        negativePayments.add(newPaymentScheduleDetail);
        // AddPayment per each
        if (paymentScheduleDetail.getPaymentDetails() != null) {
          FIN_Payment payment = paymentScheduleDetail.getPaymentDetails().getFinPayment();
          OBDal.getInstance().save(newPaymentScheduleDetail);
          for (int j = 0; j < payment.getFINPaymentDetailList().size(); j++) {
            ((FIN_PaymentDetail) payment.getFINPaymentDetailList().get(j))
                .setAmount(BigDecimal.ZERO);
          }
          List<FIN_PaymentScheduleDetail> detail = new ArrayList<FIN_PaymentScheduleDetail>();
          detail.add(newPaymentScheduleDetail);

          HashMap<String, BigDecimal> paymentAmount = new HashMap<String, BigDecimal>();
          paymentAmount.put(newPaymentScheduleDetail.getId(), payment.getAmount().negate());

          FIN_Payment finPayment = FIN_AddPayment.savePayment(null, true,
              payment.getDocumentType(), payment.getDocumentNo(), payment.getBusinessPartner(),
              payment.getPaymentMethod(), payment.getAccount(), payment.getAmount().negate()
                  .toString(), payment.getPaymentDate(), payment.getOrganization(), null, detail,
              paymentAmount, false, false, payment.getCurrency(), BigDecimal.ONE, payment
                  .getAmount().negate());
          finPayment.setDescription(payment.getDescription());
          finPayment.setStatus("RDNC");
          finPayment.setProcessed(true);
          finPayment.setAPRMProcessPayment("RE");
          for (int j = 0; j < finPayment.getFINPaymentDetailList().size(); j++) {
            ((FIN_PaymentDetail) finPayment.getFINPaymentDetailList().get(j))
                .setAmount(BigDecimal.ZERO);
          }
          OBDal.getInstance().save(finPayment);
        }

      }
      for (int i = 0; i < negativePayments.size(); i++) {
        paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList().add(
            negativePayments.get(i));
      }
    } finally {
      OBDal.getInstance().flush();
      OBContext.restorePreviousMode();
      TriggerHandler.getInstance().enable();
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}
