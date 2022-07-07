/*
 ************************************************************************************
 * Copyright (C) 2015-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class IsOrderCancelled extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) {
    JSONObject result = new JSONObject();

    OBContext.setAdminMode(true);
    try {
      final String orderId = jsonData.getString("orderId");
      final String documentNo = jsonData.getString("documentNo");
      final Order order = OBDal.getInstance().get(Order.class, orderId);

      if (order != null) {
        if (order.isCancelled()) {
          result.put("orderCancelled", true);
        } else {
          result.put("orderCancelled", false);

          String loaded = jsonData.optString("orderLoaded", null),
              updated = OBMOBCUtils.convertToUTCDateComingFromServer(order.getUpdated());
          if (loaded == null || loaded.compareTo(updated) != 0) {
            throw new OutDatedDataChangeException(
                Utility.messageBD(new DalConnectionProvider(false), "OBPOS_outdatedLayaway",
                    OBContext.getOBContext().getLanguage().getLanguage()));
          }
          final JSONArray orderlines = jsonData.optJSONArray("orderLines");
          if (orderlines != null) {
            for (int i = 0; i < orderlines.length(); i++) {
              JSONObject jsonOrderLine = orderlines.getJSONObject(i);
              OrderLine orderLine = OBDal.getInstance()
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

          final Query<Integer> importErrorQuery = OBDal.getInstance()
              .getSession()
              .createQuery("select 1 from OBPOS_Errors where client.id = :clientId "
                  + "and typeofdata = 'Order' and orderstatus = 'N' "
                  + "and jsoninfo LIKE CONCAT ('%', :orderId, '%')", Integer.class);
          importErrorQuery.setParameter("clientId", order.getClient().getId());
          importErrorQuery.setParameter("orderId", orderId);
          importErrorQuery.setMaxResults(1);
          if (importErrorQuery.list().size() > 0) {
            throw new OBException(
                OBMessageUtils.getI18NMessage("OBPOS_OrderPresentInImportErrorList", null));
          }

          if (jsonData.has("checkNotEditableLines")
              && jsonData.getBoolean("checkNotEditableLines")) {
            // Find the deferred services or the products that have related deferred services in the
            // order that is being canceled
            final String hql = "SELECT DISTINCT sol.lineNo "
                + " FROM OrderlineServiceRelation AS olsr JOIN olsr.salesOrderLine AS sol "
                + " JOIN olsr.orderlineRelated AS pol JOIN sol.salesOrder AS so "
                + " WHERE so.id <> :orderId AND pol.salesOrder.id = :orderId "
                + " AND so.iscancelled = false";
            final Query<Long> query = OBDal.getInstance().getSession().createQuery(hql, Long.class);
            query.setParameter("orderId", orderId);
            result.put("deferredLines", query.list());
          }
          if (jsonData.has("checkNotDeliveredDeferredServices")
              && jsonData.getBoolean("checkNotDeliveredDeferredServices")) {
            // Find if there's any line in the ticket which is not delivered and has deferred
            // services
            final String hql = "SELECT DISTINCT so.documentNo "
                + " FROM OrderlineServiceRelation AS olsr JOIN olsr.orderlineRelated AS pol "
                + " JOIN olsr.salesOrderLine AS sol JOIN pol.salesOrder AS po "
                + " JOIN sol.salesOrder AS so WHERE po.id = :orderId AND so.id <> :orderId "
                + " AND pol.orderedQuantity <> pol.deliveredQuantity "
                + " AND sol.orderedQuantity <> sol.deliveredQuantity AND so.documentStatus not in ('CL','CJ') ";
            final Query<String> query = OBDal.getInstance()
                .getSession()
                .createQuery(hql, String.class);
            query.setParameter("orderId", orderId);
            result.put("notDeliveredDeferredServices", query.list());
          }
        }
      } else {
        // The layaway was not found in the database.
        throw new OBException(
            OBMessageUtils.getI18NMessage("OBPOS_OrderNotFound", new String[] { documentNo }));
      }
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } catch (JSONException e) {
      throw new OBException("Error while canceling and order", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return createSuccessResponse(jsonData, result);
  }

  @Override
  public String getImportEntryId() {
    return null;
  }

  @Override
  public void setImportEntryId(String importEntryId) {
    // We don't want to create any import entry in these transactions.
  }
}
