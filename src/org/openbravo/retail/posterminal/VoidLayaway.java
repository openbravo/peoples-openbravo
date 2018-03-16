/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.common.order.OrderTax;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;

public class VoidLayaway {

  @Inject
  @Any
  private Instance<VoidLayawayHook> layawayhooks;

  @Inject
  @Any
  private Instance<VoidLayawayPostHook> layawayPosthooks;

  public void voidLayaway(JSONObject jsonorder, Order order) throws Exception {

    executeHooks(layawayhooks, jsonorder, order);

    TriggerHandler.getInstance().disable();
    OBContext.setAdminMode(true);
    try {
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

      if (jsonorder != null) {
        OrderLoader orderLoader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
        orderLoader.initializeVariables(jsonorder);
        orderLoader.handlePayments(jsonorder, order);
      }

      for (int i = 0; i < order.getOrderTaxList().size(); i++) {
        OrderTax orderLineTax = (order.getOrderTaxList().get(i));
        orderLineTax.setTaxableAmount(BigDecimal.ZERO);
        orderLineTax.setTaxAmount(BigDecimal.ZERO);
      }
      FIN_PaymentSchedule paymentSchedule = order.getFINPaymentScheduleList().get(0);
      paymentSchedule.setAmount(BigDecimal.ZERO);
      paymentSchedule.setPaidAmount(BigDecimal.ZERO);
      paymentSchedule.setOutstandingAmount(BigDecimal.ZERO);

      OBDal.getInstance().flush();
      TriggerHandler.getInstance().enable();

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
      }
      throw new OBException("There was an error voiding the Layaway: ", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    executeHooks(layawayPosthooks, jsonorder, order);

  }

  private void executeHooks(Instance<? extends Object> hooks, JSONObject jsonorder, Order order) {
    try {
      for (Iterator<? extends Object> layawayhookiter = hooks.iterator(); layawayhookiter.hasNext();) {
        Object layawayhook = layawayhookiter.next();
        if (layawayhook instanceof VoidLayawayHook) {
          ((VoidLayawayHook) layawayhook).exec(jsonorder, order);
        } else if (layawayhook instanceof VoidLayawayPostHook) {
          ((VoidLayawayPostHook) layawayhook).exec(jsonorder, order);
        }
      }
    } catch (Exception e) {
      throw new OBException("There was an error voiding the Layaway: ", e);
    }
  }

}
