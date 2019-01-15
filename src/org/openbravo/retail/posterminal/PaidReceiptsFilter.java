/*
 ************************************************************************************
 * Copyright (C) 2017-2019 Openbravo S.L.U.
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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerRequestExecutor;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.model.common.enterprise.Organization;

public class PaidReceiptsFilter extends ProcessHQLQueryValidated {
  public static final Logger log = LogManager.getLogger();

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
    boolean isVerifiedReturns = false;
    boolean isPayOpenTicket = false;

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
    case "verifiedReturns":
      orderTypeHql = "and ord.documentType.return = false and ord.documentType.sOSubType <> 'OB' and ord.obposIslayaway = false and cancelledorder is null";
      isVerifiedReturns = true;
      break;
    case "payOpenTickets":
      orderTypeHql = "and ord.grandtotal>0 and ord.documentType.sOSubType <> 'OB' and ord.documentStatus <> 'CL'";
    default:
      orderTypeHql = "";
    }

    final StringBuilder hqlPaidReceipts = new StringBuilder();
    hqlPaidReceipts.append("select");
    hqlPaidReceipts.append(receiptsHQLProperties.getHqlSelect());
    hqlPaidReceipts.append("from Order as ord ");
    hqlPaidReceipts.append("where $filtersCriteria and $hqlCriteria ");
    hqlPaidReceipts.append(orderTypeHql);
    hqlPaidReceipts.append(" and ord.client.id =  $clientId");
    hqlPaidReceipts.append(getOganizationFilter(jsonsent));
    hqlPaidReceipts
        .append(" and ord.obposIsDeleted = false and ord.obposApplications is not null and ord.documentStatus <> 'CJ' ");
    hqlPaidReceipts.append(" and ord.documentStatus <> 'CA' ");
    if (!isVerifiedReturns && !isPayOpenTicket) {
      // verified returns is already filtering by delivered = true
      hqlPaidReceipts.append(" and (ord.documentStatus <> 'CL' or ord.delivered = true) ");
    }
    if ((jsonsent.has("orderByClause") && jsonsent.get("orderByClause") != JSONObject.NULL)
        || (jsonsent.has("orderByProperties") && jsonsent.get("orderByProperties") != JSONObject.NULL)) {
      hqlPaidReceipts.append(" $orderByCriteria");
    }

    return Arrays.asList(new String[] { hqlPaidReceipts.toString() });
  }

  protected static String getOrderTypeFilter(JSONObject jsonsent) {
    return getColumnFilterValue(jsonsent, "orderType");
  }

  protected static String getOganizationFilter(JSONObject jsonsent) throws JSONException {

    String documentNo = getColumnFilterValue(jsonsent, "documentNo");
    String crossStoreFilter = getColumnFilterValue(jsonsent, "store");

    if (!StringUtils.isEmpty(crossStoreFilter)) {
      return StringUtils.EMPTY;
    }

    OBPOSApplications pOSTerminal = POSUtils.getTerminal(jsonsent.optString("terminalName"));
    Organization myOrg = pOSTerminal.getOrganization().getOrganizationInformationList().get(0)
        .getOrganization();
    String crossStoreOrgId = myOrg.getOBPOSCrossStoreOrganization() != null ? myOrg
        .getOBPOSCrossStoreOrganization().getId() : "";

    final StringBuilder orgFilter = new StringBuilder();

    if (!StringUtils.isEmpty(documentNo) && !StringUtils.isEmpty(crossStoreOrgId)) {

      String crossStoreOrg = StringCollectionUtils.commaSeparated(
          POSUtils.getOrgListByCrossStoreId(crossStoreOrgId), true);

      orgFilter.append(" and ord.organization.id in (");
      orgFilter.append(crossStoreOrg);
      orgFilter.append(")");
    } else {
      orgFilter.append(" and ord.$orgId");
    }

    return orgFilter.toString();
  }

  protected static String getColumnFilterValue(JSONObject jsonsent, final String columnFilter) {
    String columnValue = "";
    try {
      if (jsonsent.has("remoteFilters")) {
        JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
        for (int i = 0; i < remoteFilters.length(); i++) {
          JSONObject filter = remoteFilters.getJSONObject(i);
          JSONArray columns = filter.getJSONArray("columns");
          for (int j = 0; j < columns.length(); j++) {
            String column = columns.getString(j);
            if (columnFilter.equals(column)) {
              columnValue = filter.getString("value");
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      // Ignored
    }
    return columnValue;
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