/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends POSDataSynchronizationProcess
    implements DataSynchronizationImportProcess {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  @Override
  public JSONObject saveRecord(JSONObject jsonRecord) throws Exception {

    JSONArray respArray = new JSONArray();
    JSONObject jsonorder = (JSONObject) jsonRecord.get("order");
    Order order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));

    if (order != null) {
      String loaded = jsonorder.getString("loaded"),
          updated = OBMOBCUtils.convertToUTCDateComingFromServer(order.getUpdated());
      if (!(loaded.compareTo(updated) >= 0)) {
        throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_outdatedLayaway", OBContext.getOBContext().getLanguage().getLanguage()));
      }
      final JSONArray orderlines = jsonorder.optJSONArray("lines");
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

      final StringBuffer hql = new StringBuffer();
      hql.append("SELECT DISTINCT so.documentNo ");
      hql.append("FROM OrderlineServiceRelation AS olsr ");
      hql.append("JOIN olsr.orderlineRelated AS pol ");
      hql.append("JOIN olsr.salesOrderLine AS sol ");
      hql.append("JOIN pol.salesOrder AS po ");
      hql.append("JOIN sol.salesOrder AS so ");
      hql.append("WHERE po.id = :orderId ");
      hql.append("AND so.id <> :orderId ");
      hql.append("AND pol.orderedQuantity <> pol.deliveredQuantity ");
      hql.append("AND sol.orderedQuantity <> sol.deliveredQuantity ");
      hql.append("AND so.documentStatus <> 'CL' ");
      Query<String> query = OBDal.getInstance()
          .getSession()
          .createQuery(hql.toString(), String.class);
      query.setParameter("orderId", order.getId());
      List<String> documentNoList = query.list();
      if (documentNoList.size() > 0) {
        String errorMsg = OBMessageUtils.messageBD("OBPOS_CannotCancelLayWithDeferred") + " "
            + OBMessageUtils.messageBD("OBPOS_RelatedOrders") + " ";
        for (int i = 0; i < documentNoList.size(); i++) {
          errorMsg = errorMsg + documentNoList.get(i);
          if (i < documentNoList.size() - 1) {
            errorMsg = errorMsg + ", ";
          }
        }
        throw new OBException(errorMsg);
      }
    }

    // Update CashUp Report
    JSONObject jsoncashup = jsonorder.getJSONObject("cashUpReportInformation");
    Date cashUpDate = new Date();

    UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);

    VoidLayaway proc = WeldUtils.getInstanceFromStaticBeanManager(VoidLayaway.class);
    proc.voidLayaway(jsonorder, order);

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }

  @Override
  protected String getImportQualifier() {
    return "OBPOS_VoidLayaway";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.voidLayaway";
  }
}
