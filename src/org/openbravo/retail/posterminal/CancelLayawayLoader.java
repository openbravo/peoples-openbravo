/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Date;

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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.retail.posterminal.utility.DocumentNoHandler;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "OBPOS_CancelLayaway")
public class CancelLayawayLoader extends OrderLoader {

  @Override
  public JSONObject saveRecord(JSONObject json) throws Exception {

    try {
      JSONObject jsoncashup = null;
      if (json.has("cashUpReportInformation")) {
        // Update CashUp Report
        jsoncashup = json.getJSONObject("cashUpReportInformation");
        Date cashUpDate = new Date();

        UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);
      }

      TriggerHandler.getInstance().disable();

      // Do not allow to do a CL in the case that the order was not updated
      final JSONObject canceledOrder = json.getJSONObject("canceledorder");
      final Order oldOrder = OBDal.getInstance().get(Order.class, canceledOrder.getString("id"));
      if (oldOrder != null) {
        String loaded = canceledOrder.optString("loaded"),
            updated = OBMOBCUtils.convertToUTCDateComingFromServer(oldOrder.getUpdated());
        if (loaded == null || loaded.compareTo(updated) != 0) {
          throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
              "OBPOS_outdatedLayaway", OBContext.getOBContext().getLanguage().getLanguage()));
        }

        final JSONArray orderlines = canceledOrder.optJSONArray("lines");
        for (int i = 0; i < orderlines.length(); i++) {
          final JSONObject jsonOrderLine = orderlines.getJSONObject(i);
          final OrderLine orderLine = OBDal.getInstance()
              .get(OrderLine.class, jsonOrderLine.optString("id"));
          if (orderLine != null) {
            loaded = jsonOrderLine.optString("loaded");
            updated = OBMOBCUtils.convertToUTCDateComingFromServer(orderLine.getUpdated());
            if (loaded == null || loaded.compareTo(updated) != 0) {
              throw new OutDatedDataChangeException(
                  Utility.messageBD(new DalConnectionProvider(false), "OBPOS_outdatedLayaway",
                      OBContext.getOBContext().getLanguage().getLanguage()));
            }
          }
        }
      }

      final ArrayList<OrderLine> lineReferences = new ArrayList<OrderLine>();
      final JSONArray orderlines = json.getJSONArray("lines");

      initializeVariables(json);

      // Create new inverse order
      final Order inverseOrder = OBProvider.getInstance().get(Order.class);
      createOrderAndLines(json, inverseOrder, orderlines, lineReferences);

      for (final OrderLine orderLine : inverseOrder.getOrderLineList()) {
        orderLine.setObposIspaid(true);
        OBDal.getInstance().save(orderLine);
      }

      inverseOrder.setCancelledorder(oldOrder);

      final OBCriteria<OrderLine> orderLineCriteria = OBDal.getInstance()
          .createCriteria(OrderLine.class);
      orderLineCriteria.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, oldOrder));
      orderLineCriteria.add(Restrictions.eq(OrderLine.PROPERTY_OBPOSISDELETED, false));
      for (final OrderLine orderLine : orderLineCriteria.list()) {
        orderLine.setObposIspaid(true);
        orderLine.setDeliveredQuantity(orderLine.getOrderedQuantity());
        OBDal.getInstance().save(orderLine);
      }

      OBDal.getInstance().save(oldOrder);

      OBDal.getInstance().flush();

      try {
        TriggerHandler.getInstance().enable();
        handlePayments(json, inverseOrder, null);
      } finally {
        TriggerHandler.getInstance().disable();
      }

      if (json.getJSONArray("payments").length() > 0) {
        OBContext.setAdminMode(false);
        try {
          for (DocumentNoHandler documentNoHandler : documentNoHandlers.get()) {
            documentNoHandler.setDocumentNoAndSave();
          }
          OBDal.getInstance().flush();
        } finally {
          documentNoHandlers.set(null);
          OBContext.restorePreviousMode();
        }
      }

      POSUtils.setDefaultPaymentType(json, inverseOrder);

      OBContext.setCrossOrgReferenceAdminMode();
      try {
        final Organization paymentOrganization = getPaymentOrganization(
            inverseOrder.getObposApplications(),
            POSUtils.isCrossStore(oldOrder, inverseOrder.getObposApplications()));
        CancelAndReplaceUtils.cancelOrder(json.getString("orderid"), paymentOrganization.getId(),
            json, false);
      } finally {
        OBContext.restorePreviousCrossOrgReferenceMode();
      }

    } catch (Exception ex) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException("Error in CancelLayawayLoader : " + ex.getMessage(), ex);
    } finally {
      TriggerHandler.getInstance().enable();
    }

    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    return jsonResponse;
  }

  @Override
  protected String getImportQualifier() {
    return "OBPOS_CancelLayaway";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.cancelLayaway";
  }
}
