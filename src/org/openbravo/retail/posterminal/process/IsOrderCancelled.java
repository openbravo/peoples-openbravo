/*
 ************************************************************************************
 * Copyright (C) 2015-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.servercontroller.MultiServerJSONProcess;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.json.JsonConstants;

public class IsOrderCancelled extends MultiServerJSONProcess {
  @Override
  public JSONObject execute(JSONObject jsonData) {
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
          if (jsonData.has("checkNotEditableLines") && jsonData.getBoolean("checkNotEditableLines")) {
            // Find the deferred services or the products that have related deferred services in the
            // order that is being canceled
            final StringBuffer hql = new StringBuffer();
            hql.append("SELECT DISTINCT sol.lineNo ");
            hql.append("FROM OrderlineServiceRelation AS olsr ");
            hql.append("JOIN olsr.salesOrderLine AS sol ");
            hql.append("JOIN olsr.orderlineRelated AS pol ");
            hql.append("JOIN sol.salesOrder AS so ");
            hql.append("WHERE so.id <> :orderId ");
            hql.append("AND pol.salesOrder.id = :orderId ");
            hql.append("AND so.iscancelled = false");
            final Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
            query.setParameter("orderId", orderId);
            result.put("deferredLines", query.list());
          }
          if (jsonData.has("checkNotDeliveredDeferredServices")
              && jsonData.getBoolean("checkNotDeliveredDeferredServices")) {
            // Find if there's any line in the ticket which is not delivered and has deferred
            // services
            final StringBuffer hql = new StringBuffer();
            hql.append("SELECT DISTINCT po.id ");
            hql.append("FROM OrderlineServiceRelation AS olsr ");
            hql.append("JOIN olsr.orderlineRelated AS pol ");
            hql.append("JOIN olsr.salesOrderLine AS sol ");
            hql.append("JOIN pol.salesOrder AS po ");
            hql.append("JOIN sol.salesOrder AS so ");
            hql.append("WHERE po.id = :orderId ");
            hql.append("AND so.id <> :orderId ");
            hql.append("AND so.iscancelled = false ");
            hql.append("AND pol.orderedQuantity > pol.deliveredQuantity");
            final Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
            query.setParameter("orderId", orderId);
            query.setMaxResults(1);
            result.put("hasDeferredServices", query.uniqueResult() != null);
          }
        }
      } else {
        // The layaway was not found in the database.
        throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_OrderNotFound",
            new String[] { documentNo }));
      }
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } catch (JSONException e) {
      throw new OBException("Error while canceling and order", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  @Override
  protected String getImportEntryDataType() {
    return null;
  }

  @Override
  protected void createImportEntry(String messageId, JSONObject sentIn, JSONObject processResult,
      Organization organization) throws JSONException {
    // We don't want to create any import entry in these transactions.
  }

  @Override
  protected void createArchiveEntry(String id, JSONObject json) throws JSONException {
    // We don't want to create any import entry in these transactions.
  }

  @Override
  protected boolean executeInOneServer(JSONObject json) throws JSONException {
    return true;
  }
}