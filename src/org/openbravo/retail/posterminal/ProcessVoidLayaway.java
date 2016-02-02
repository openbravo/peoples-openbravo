/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  @Inject
  @Any
  private Instance<VoidLayawayHook> layawayhooks;

  @Override
  public JSONObject saveRecord(JSONObject jsonRecord) throws Exception {

    JSONArray respArray = new JSONArray();
    JSONObject jsonorder = (JSONObject) jsonRecord.get("order");
    try {

      Order order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));

      for (Iterator<VoidLayawayHook> layawayhookiter = layawayhooks.iterator(); layawayhookiter
          .hasNext();) {
        VoidLayawayHook layawayhook = layawayhookiter.next();
        layawayhook.exec(jsonorder, order);
      }

      TriggerHandler.getInstance().disable();
      OBContext.setAdminMode(true);

      order.setDocumentStatus("CL");
      order.setGrandTotalAmount(BigDecimal.ZERO);
      order.setSummedLineAmount(BigDecimal.ZERO);
      for (int i = 0; i < order.getOrderLineList().size(); i++) {
        OrderLine orderLine = (order.getOrderLineList().get(i));
        orderLine.setOrderedQuantity(BigDecimal.ZERO);
        orderLine.setLineNetAmount(BigDecimal.ZERO);
        orderLine.setLineGrossAmount(BigDecimal.ZERO);
        for (int j = 0; j < orderLine.getOrderLineOfferList().size(); j++) {
          OrderLineOffer offer = (orderLine.getOrderLineOfferList().get(j));
          offer.setTotalAmount(BigDecimal.ZERO);
          offer.setDisplayedTotalAmount(BigDecimal.ZERO);
          offer.setPriceAdjustmentAmt(BigDecimal.ZERO);
        }
      }
      for (int i = 0; i < order.getOrderLineTaxList().size(); i++) {
        OrderLineTax orderLineTax = (order.getOrderLineTaxList().get(i));
        orderLineTax.setTaxableAmount(BigDecimal.ZERO);
        orderLineTax.setTaxAmount(BigDecimal.ZERO);
      }

      OrderLoader orderLoader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
      orderLoader.initializeVariables(jsonorder);
      orderLoader.handlePayments(jsonorder, order, null, false);

      for (int i = 0; i < order.getOrderTaxList().size(); i++) {
        OrderTax orderLineTax = (order.getOrderTaxList().get(i));
        orderLineTax.setTaxableAmount(BigDecimal.ZERO);
        orderLineTax.setTaxAmount(BigDecimal.ZERO);
      }
      FIN_PaymentSchedule paymentSchedule = order.getFINPaymentScheduleList().get(0);
      paymentSchedule.setAmount(BigDecimal.ZERO);
      paymentSchedule.setPaidAmount(BigDecimal.ZERO);
      paymentSchedule.setOutstandingAmount(BigDecimal.ZERO);

      OBDal.getInstance().getConnection(true).commit();
    } catch (Exception e) {
      throw new OBException("There was an error voiding the orde Layaway: ", e);
    } finally {
      OBDal.getInstance().rollbackAndClose();
      OBContext.restorePreviousMode();
      TriggerHandler.getInstance().enable();
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }

  protected String getImportQualifier() {
    return "OBPOS_VoidLayaway";
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

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.voidLayaway";
  }
}
