/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "OBPOS_CancelLayaway")
public class CancelLayawayLoader extends OrderLoader {

  private static final Logger log = Logger.getLogger(CancelLayawayLoader.class);

  public JSONObject saveRecord(JSONObject json) throws Exception {

    boolean useOrderDocumentNoForRelatedDocs = false;

    try {
      useOrderDocumentNoForRelatedDocs = "Y".equals(Preferences.getPreferenceValue(
          "OBPOS_UseOrderDocumentNoForRelatedDocs", true, OBContext.getOBContext()
              .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
              .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error(
          "Error getting OBPOS_UseOrderDocumentNoForRelatedDocs preference: " + e1.getMessage(), e1);
    }

    try {
      JSONObject jsoncashup = null;
      if (json.has("cashUpReportInformation")) {
        // Update CashUp Report
        jsoncashup = json.getJSONObject("cashUpReportInformation");
        Date cashUpDate = new Date();

        UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);
      }

      TriggerHandler.getInstance().disable();

      final ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      final JSONArray orderlines = json.getJSONArray("lines");

      initializeVariables(json);

      // Create new inverse order
      final Order inverseOrder = OBProvider.getInstance().get(Order.class);
      createOrder(inverseOrder, json);
      OBDal.getInstance().save(inverseOrder);

      // Create the lines
      createOrderLines(inverseOrder, json, orderlines, lineReferences);

      for (final OrderLine orderLine : inverseOrder.getOrderLineList()) {
        orderLine.setObposIspaid(true);
        orderLine.setDeliveredQuantity(orderLine.getOrderedQuantity());
        OBDal.getInstance().save(orderLine);
      }

      final OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          json.getString("posTerminal"));
      inverseOrder.setObposApplications(posTerminal);
      inverseOrder.setObposAppCashup(json.getString("obposAppCashup"));
      final JSONObject canceledOrder = json.getJSONObject("canceledorder");
      final Order oldOrder = OBDal.getInstance().get(Order.class, canceledOrder.getString("id"));
      inverseOrder.setCancelledorder(oldOrder);

      oldOrder.setObposIslayaway(false);
      inverseOrder.setObposIslayaway(false);

      final OBCriteria<OrderLine> orderLineCriteria = OBDal.getInstance().createCriteria(
          OrderLine.class);
      orderLineCriteria.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, oldOrder));
      orderLineCriteria.add(Restrictions.eq(OrderLine.PROPERTY_OBPOSISDELETED, false));
      for (final OrderLine orderLine : orderLineCriteria.list()) {
        orderLine.setObposIspaid(true);
        orderLine.setDeliveredQuantity(orderLine.getOrderedQuantity());
        OBDal.getInstance().save(orderLine);
      }

      OBDal.getInstance().save(inverseOrder);
      OBDal.getInstance().save(oldOrder);

      handlePayments(json, inverseOrder);

      POSUtils.setDefaultPaymentType(json, inverseOrder);

      OBDal.getInstance().flush();

      CancelAndReplaceUtils.cancelOrder(json.getString("orderid"), json,
          useOrderDocumentNoForRelatedDocs);
    } catch (Exception ex) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException("CancelLayawayLoader.cancelOrder: ", ex);
    } finally {
      TriggerHandler.getInstance().enable();
    }

    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    return jsonResponse;
  }

  protected String getImportQualifier() {
    return "OBPOS_CancelLayaway";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.cancelLayaway";
  }
}
