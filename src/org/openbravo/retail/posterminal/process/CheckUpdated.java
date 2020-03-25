/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.json.JsonConstants;

public class CheckUpdated extends JSONProcessSimple {

  private final String PENDING = "P";
  private final String ERROR = "E";
  private final String OUTDATED = "O";

  @Override
  public JSONObject exec(JSONObject jsonData) {
    final JSONObject result = new JSONObject();

    OBContext.setAdminMode(true);
    try {
      final JSONObject data = new JSONObject();
      final JSONObject jsonOrder = jsonData.getJSONObject("order");
      final Session session = OBDal.getInstance().getSession();

      if (hasImportEntries(session, jsonOrder.optString("id"))) {
        data.put("error", true);
        data.put("type", PENDING);
      } else if (hasErrosrWhileImporting(session, jsonOrder.optString("id"))) {
        data.put("error", true);
        data.put("type", ERROR);
      } else if (isOrderOutdated(jsonOrder)) {
        data.put("error", true);
        data.put("type", OUTDATED);
      }

      result.put(JsonConstants.RESPONSE_DATA, data);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    } catch (JSONException e) {
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private boolean hasImportEntries(final Session session, final String orderId) {
    final String whereClause = "%\"id\":\"" + orderId + "\"%";
    final String importEntryHQL = "" //
        + "SELECT id " //
        + "FROM C_IMPORT_ENTRY " //
        + "WHERE importStatus = 'Initial' " //
        + "AND (typeofdata = 'Order' OR typeofdata = 'OBPOS_CancelLayaway' OR typeofdata = 'OBPOS_VoidLayaway') " //
        + "AND jsonInfo LIKE :orderId";
    final Query<String> importEntryQuery = session.createQuery(importEntryHQL, String.class);
    importEntryQuery.setParameter("orderId", whereClause);
    importEntryQuery.setMaxResults(1);
    return importEntryQuery.uniqueResult() != null;
  }

  private boolean hasErrosrWhileImporting(final Session session, final String orderId) {
    final String whereClause = "%\"id\":\"" + orderId + "\"%";
    final String errorWhileImportingHQL = "" //
        + "SELECT id " //
        + "FROM OBPOS_Errors " //
        + "WHERE orderstatus = 'N' " //
        + "AND (typeofdata = 'Order' OR typeofdata = 'OBPOS_CancelLayaway' OR typeofdata = 'OBPOS_VoidLayaway') " //
        + "AND jsonInfo LIKE :orderId";
    final Query<String> errorWhileImportingQuery = session.createQuery(errorWhileImportingHQL,
        String.class);
    errorWhileImportingQuery.setParameter("orderId", whereClause);
    errorWhileImportingQuery.setMaxResults(1);
    return errorWhileImportingQuery.uniqueResult() != null;
  }

  private boolean isOrderOutdated(final JSONObject jsonOrder) throws JSONException {
    final Order order = OBDal.getInstance().get(Order.class, jsonOrder.optString("id"));
    String loaded = jsonOrder.optString("loaded", null),
        updated = OBMOBCUtils.convertToUTCDateComingFromServer(order.getUpdated());
    if (loaded == null || loaded.compareTo(updated) != 0) {
      return true;
    } else {
      final JSONArray jsonOrderlines = jsonOrder.optJSONArray("lines");
      if (jsonOrderlines != null) {
        for (int i = 0; i < jsonOrderlines.length(); i++) {
          final JSONObject jsonOrderLine = jsonOrderlines.getJSONObject(i);
          final OrderLine orderLine = OBDal.getInstance()
              .get(OrderLine.class, jsonOrderLine.optString("id"));
          if (orderLine != null) {
            loaded = jsonOrderLine.optString("loaded");
            updated = OBMOBCUtils.convertToUTCDateComingFromServer(orderLine.getUpdated());
            if (loaded == null || loaded.compareTo(updated) != 0) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

}
