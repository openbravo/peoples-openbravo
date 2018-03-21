/*
 ************************************************************************************
 * Copyright (C) 2017-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerRequestExecutor;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;

public class PaidReceiptsFilter extends ProcessHQLQueryValidated {
  public static final Logger log = Logger.getLogger(PaidReceiptsFilter.class);

  public static final String paidReceiptsFilterPropertyExtension = "PaidReceiptsFilter_Extension";

  @Inject
  @Any
  @Qualifier(paidReceiptsFilterPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList receiptsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        jsonsent);
    propertiesList.add(receiptsHQLProperties);

    return propertiesList;
  }

  @Override
  protected String getFilterEntity() {
    return "OrderFilter";
  }

  @Override
  protected List<String> getQueryValidated(JSONObject jsonsent) throws JSONException {

    HQLPropertyList receiptsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        jsonsent);

    String orderTypeFilter = getOrderTypeFilter(jsonsent);
    String orderTypeHql;

    switch (orderTypeFilter) {
    case "RET":
      orderTypeHql = "and ord.documentType.return = true";
      break;
    case "LAY":
      orderTypeHql = "and ord.obposIslayaway = true";
      break;
    case "ORD":
      orderTypeHql = "and ord.documentType.return = false and ord.documentType.sOSubType <> 'OB' and ord.obposIslayaway = false";
      break;
    default:
      orderTypeHql = "";
    }

    final StringBuilder hqlPaidReceipts = new StringBuilder();
    hqlPaidReceipts.append("select");
    hqlPaidReceipts.append(receiptsHQLProperties.getHqlSelect());
    hqlPaidReceipts.append("from Order as ord ");
    hqlPaidReceipts.append("where $filtersCriteria and $hqlCriteria ");
    hqlPaidReceipts.append(orderTypeHql);
    hqlPaidReceipts.append(" and ord.client.id =  $clientId and ord.$orgId");
    hqlPaidReceipts
        .append(" and ord.obposIsDeleted = false and ord.obposApplications is not null and ord.documentStatus <> 'CJ' ");
    hqlPaidReceipts
        .append(" and ord.documentStatus <> 'CA' and (ord.documentStatus <> 'CL' or ord.iscancelled = true)");
    if (jsonsent.has("orderByClause") && jsonsent.get("orderByClause") != JSONObject.NULL) {
      hqlPaidReceipts.append(" $orderByCriteria");
    }

    return Arrays.asList(new String[] { hqlPaidReceipts.toString() });
  }

  protected static String getOrderTypeFilter(JSONObject jsonsent) {
    String orderType = "";
    try {
      if (jsonsent.has("remoteFilters")) {
        JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
        for (int i = 0; i < remoteFilters.length(); i++) {
          JSONObject filter = remoteFilters.getJSONObject(i);
          JSONArray columns = filter.getJSONArray("columns");
          for (int j = 0; j < columns.length(); j++) {
            String column = columns.getString(j);
            if ("orderType".equals(column)) {
              orderType = filter.getString("value");
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      // Ignored
    }
    return orderType;
  }

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    Writer temporal = new StringWriter();
    super.exec(temporal, jsonsent);
    String data = temporal.toString();
    try {
      JSONObject result = new JSONObject("{" + w.toString() + "}");
      if (MobileServerController.getInstance().isThisAStoreServer() && isScanning(jsonsent)
          && result.optLong("totalRows") == 0) {
        JSONObject centralResult = MobileServerRequestExecutor.getInstance().executeCentralRequest(
            MobileServerUtils.OBWSPATH + PaidReceiptsFilter.class.getName(), jsonsent);
        data = centralResult.toString().substring(1, centralResult.toString().length() - 1);
      }
    } catch (JSONException e) {
      // Do nothing
    }
    w.write(data);
    return;

  }

  private boolean isScanning(JSONObject jsonsent) {
    try {
      if ("documentNo".equals(jsonsent.getJSONArray("remoteFilters").getJSONObject(0)
          .getJSONArray("columns").get(0))
          && "=".equals(jsonsent.getJSONArray("remoteFilters").getJSONObject(0)
              .getString("operator"))) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}