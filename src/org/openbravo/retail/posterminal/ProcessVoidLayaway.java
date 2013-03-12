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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends JSONProcessSimple {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;
  private static final Logger log = Logger.getLogger(ProcessVoidLayaway.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    JSONArray respArray = new JSONArray();
    JSONObject jsonorder = (JSONObject) jsonsent.get("order");
    try {
      TriggerHandler.getInstance().disable();
      OBContext.setAdminMode(true);
      Order order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));
      order.setDocumentStatus("CL");
      order.setGrandTotalAmount(BigDecimal.ZERO);
      order.setSummedLineAmount(BigDecimal.ZERO);
      for (int i = 0; i < order.getOrderLineList().size(); i++) {
        OrderLine orderLine = ((OrderLine) order.getOrderLineList().get(i));
        orderLine.setOrderedQuantity(BigDecimal.ZERO);
        orderLine.setLineNetAmount(BigDecimal.ZERO);
        orderLine.setLineGrossAmount(BigDecimal.ZERO);
        for (int j = 0; j < orderLine.getOrderLineOfferList().size(); j++) {
          OrderLineOffer offer = ((OrderLineOffer) orderLine.getOrderLineOfferList().get(j));
          offer.setTotalAmount(BigDecimal.ZERO);
          offer.setDisplayedTotalAmount(BigDecimal.ZERO);
          offer.setPriceAdjustmentAmt(BigDecimal.ZERO);
        }
      }
      for (int i = 0; i < order.getOrderLineTaxList().size(); i++) {
        OrderLineTax orderLineTax = ((OrderLineTax) order.getOrderLineTaxList().get(i));
        orderLineTax.setTaxableAmount(BigDecimal.ZERO);
        orderLineTax.setTaxAmount(BigDecimal.ZERO);
      }
      for (int i = 0; i < order.getOrderTaxList().size(); i++) {
        OrderTax orderLineTax = ((OrderTax) order.getOrderTaxList().get(i));
        orderLineTax.setTaxableAmount(BigDecimal.ZERO);
        orderLineTax.setTaxAmount(BigDecimal.ZERO);
      }
      FIN_PaymentSchedule paymentSchedule = (FIN_PaymentSchedule) order.getFINPaymentScheduleList()
          .get(0);
      paymentSchedule.setAmount(BigDecimal.ZERO);
      paymentSchedule.setPaidAmount(BigDecimal.ZERO);
      paymentSchedule.setOutstandingAmount(BigDecimal.ZERO);

      for (int i = 0; i < paymentSchedule.getFINPaymentScheduleDetailOrderPaymentScheduleList()
          .size(); i++) {
        FIN_PaymentScheduleDetail paymentScheduleDetail = paymentSchedule
            .getFINPaymentScheduleDetailOrderPaymentScheduleList().get(i);
        if (paymentScheduleDetail.getPaymentDetails() != null) {
          FIN_Payment payment = paymentScheduleDetail.getPaymentDetails().getFinPayment();
          for (int j = 0; j < payment.getFINPaymentDetailList().size(); j++) {
            ((FIN_PaymentDetail) payment.getFINPaymentDetailList().get(j))
                .setAmount(BigDecimal.ZERO);
          }
        }
      }
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonorder.getString("posTerminal"));
      int stdPrecision = order.getCurrency().getStandardPrecision().intValue();
      JSONArray payments = jsonorder.getJSONArray("payments");
      for (int i = 0; i < payments.length(); i++) {
        JSONObject payment = payments.getJSONObject(i);
        OBPOSAppPayment paymentType = null;
        String paymentTypeName = payment.getString("kind");
        BigDecimal mulrate = new BigDecimal(1);
        BigDecimal amount = BigDecimal.valueOf(payment.getDouble("paid"))
            .setScale(stdPrecision, RoundingMode.HALF_UP).negate();
        BigDecimal foreignAmount = amount;
        if (payment.has("mulrate") && payment.getDouble("mulrate") != 1) {
          mulrate = BigDecimal.valueOf(payment.getDouble("mulrate"));
          foreignAmount = amount.multiply(mulrate).setScale(stdPrecision, RoundingMode.HALF_UP);
        }

        for (OBPOSAppPayment type : posTerminal.getOBPOSAppPaymentList()) {
          if (type.getSearchKey().equals(paymentTypeName)) {
            paymentType = type;
          }
        }
        FIN_FinancialAccount account = paymentType.getFinancialAccount();

        FIN_PaymentScheduleDetail newPaymentScheduleDetail = OBProvider.getInstance().get(
            FIN_PaymentScheduleDetail.class);
        newPaymentScheduleDetail.setOrderPaymentSchedule(paymentSchedule);
        newPaymentScheduleDetail.setAmount(amount);
        OBDal.getInstance().save(newPaymentScheduleDetail);
        List<FIN_PaymentScheduleDetail> detail = new ArrayList<FIN_PaymentScheduleDetail>();
        detail.add(newPaymentScheduleDetail);

        HashMap<String, BigDecimal> paymentAmount = new HashMap<String, BigDecimal>();
        paymentAmount.put(newPaymentScheduleDetail.getId(), amount);

        DocumentType paymentDocType = getPaymentDocumentType(order.getOrganization());
        Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.class);
        String paymentDocNo = getDocumentNo(paymentEntity, null, paymentDocType);

        FIN_Payment finPayment = FIN_AddPayment.savePayment(null, true, paymentDocType,
            paymentDocNo, order.getBusinessPartner(), paymentType.getPaymentMethod()
                .getPaymentMethod(), account, amount.toString(), new Date(), order
                .getOrganization(), null, detail, paymentAmount, false, false, order.getCurrency(),
            mulrate.setScale(stdPrecision, RoundingMode.HALF_UP), foreignAmount);
        finPayment.setDescription(getPaymentDescription());
        finPayment.setStatus("RDNC");
        finPayment.setProcessed(true);
        finPayment.setAPRMProcessPayment("RE");
        for (int j = 0; j < finPayment.getFINPaymentDetailList().size(); j++) {
          ((FIN_PaymentDetail) finPayment.getFINPaymentDetailList().get(j))
              .setAmount(BigDecimal.ZERO);
        }
        OBDal.getInstance().save(finPayment);
      }
    } catch (Exception e) {
      log.error("There was an error voiding the orde Layaway: ", e);
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

  protected DocumentType getPaymentDocumentType(Organization org) {
    if (paymentDocTypes.get(DalUtil.getId(org)) != null) {
      return paymentDocTypes.get(DalUtil.getId(org));
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARReceipt);
    paymentDocTypes.put((String) DalUtil.getId(org), docType);
    return docType;

  }

  protected String getPaymentDescription() {
    if (paymentDescription == null) {
      String language = RequestContext.get().getVariablesSecureApp().getLanguage();
      paymentDescription = Utility.messageBD(new DalConnectionProvider(false), "OrderDocumentno",
          language);
    }
    return paymentDescription;
  }

  protected String getDocumentNo(Entity entity, DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "", entity
            .getTableName(), doctypeTarget == null ? "" : doctypeTarget.getId(),
        doctype == null ? "" : doctype.getId(), false, true);
  }
}
