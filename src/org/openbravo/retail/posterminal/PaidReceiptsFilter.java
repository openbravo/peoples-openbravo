/*
 ************************************************************************************
 * Copyright (C) 2017-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
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
        break;
      case "QT":
        orderTypeHql = "and ord.documentType.return = false and ord.documentType.sOSubType = 'OB' and ord.obposIslayaway = false";
        break;
      case "payOpenTickets":
        orderTypeHql = "and ord.grandTotalAmount>0 and ord.documentType.sOSubType <> 'OB' and ord.documentStatus <> 'CL'";
        isPayOpenTicket = true;
        break;
      default:
        orderTypeHql = "";
    }
    orderTypeHql += getOrderDateFilter(jsonsent, orderTypeFilter);

    String deliveryModeFilter = getDeliveryModeFilter(jsonsent);
    String deliveryModeHql;
    switch (deliveryModeFilter) {
      case "PickupInStore":
        deliveryModeHql = " and exists (select 1 from OrderLine ol where ord.id = ol.salesOrder.id and ol.obrdmDeliveryMode = 'PickupInStore') ";
        break;
      case "PickupInStoreWithDate":
        deliveryModeHql = " and exists (select 1 from OrderLine ol where ord.id = ol.salesOrder.id and ol.obrdmDeliveryMode = 'PickupInStoreWithDate') ";
        break;
      case "HomeDelivery":
        deliveryModeHql = " and exists (select 1 from OrderLine ol where ord.id = ol.salesOrder.id and ol.obrdmDeliveryMode = 'HomeDelivery') ";
        break;
      case "PickAndCarry":
        deliveryModeHql = " and exists (select 1 from OrderLine ol where ord.id = ol.salesOrder.id and coalesce(ol.obrdmDeliveryMode, 'PickAndCarry') = 'PickAndCarry') ";
        break;
      default:
        deliveryModeHql = "";
    }
    String hqlPaidReceipts = "select " //
        + receiptsHQLProperties.getHqlSelect() //
        + " from Order as ord" //
        + " where $filtersCriteria and $hqlCriteria" //
        + orderTypeHql //
        + deliveryModeHql //
        + " and ord.client.id = $clientId" //
        + getOganizationFilter(jsonsent) //
        + " and ord.obposIsDeleted = false" //
        + " and ord.obposApplications is not null" //
        + " and ord.documentStatus not in ('CJ', 'CA', 'NC', 'AE', 'ME')";
    if (!isPayOpenTicket) {
      hqlPaidReceipts += " and (ord.documentStatus <> 'CL' or ord.iscancelled = true) ";
    }
    if ((jsonsent.has("orderByClause") && jsonsent.get("orderByClause") != JSONObject.NULL)
        || (jsonsent.has("orderByProperties")
            && jsonsent.get("orderByProperties") != JSONObject.NULL)) {
      hqlPaidReceipts += " $orderByCriteria";
    }

    return Arrays.asList(hqlPaidReceipts);
  }

  protected static String getInvoiceDocumentNo(JSONObject jsonsent) {
    return getColumnFilterValue(jsonsent, "invoiceDocumentNo");
  }

  protected static String getOrderTypeFilter(JSONObject jsonsent) {
    return getColumnFilterValue(jsonsent, "orderType");
  }

  protected static String getDeliveryModeFilter(JSONObject jsonsent) {
    return getColumnFilterValue(jsonsent, "deliveryMode");
  }

  private static String getOrderDateFilter(final JSONObject jsonsent, final String orderTypeFilter)
      throws JSONException {
    if (jsonsent.optJSONArray("remoteFilters") != null
        && ((jsonsent.optJSONArray("remoteFilters").length() == 0
            && StringUtils.isEmpty(orderTypeFilter))
            || (jsonsent.optJSONArray("remoteFilters").length() == 1
                && !StringUtils.isEmpty(orderTypeFilter)))) {
      int orderDaysFilter = 30;
      try {
        orderDaysFilter = Integer
            .parseInt(Preferences.getPreferenceValue("OBPOS_PaidReceiptsOrderDateFilterDays", true,
                OBContext.getOBContext().getCurrentClient(),
                OBContext.getOBContext().getCurrentOrganization(),
                OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
      } catch (PropertyException e) {

      }

      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(terminalDate);
      calendar.add(Calendar.DAY_OF_MONTH, -orderDaysFilter);
      final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return String.format(" and ord.orderDate >= to_date('%s', 'yyyy-MM-dd') ",
          dateFormat.format(calendar.getTime()));
    }
    return "";
  }

  private static String getOganizationFilter(final JSONObject jsonsent) throws JSONException {
    final String crossStoreFilter = getColumnFilterValue(jsonsent, "store");
    if (StringUtils.isNotEmpty(crossStoreFilter)) {
      return StringUtils.EMPTY;
    }

    final String documentNoFilter = getColumnFilterValue(jsonsent, "documentNo");
    final OBPOSApplications pOSTerminal = POSUtils.getTerminalById(jsonsent.getString("pos"));
    final Organization org = pOSTerminal.getOrganization();
    final String crossStoreId = org.getOBRETCOCrossStoreOrganization() != null
        ? org.getOBRETCOCrossStoreOrganization().getId()
        : "";

    return StringUtils.isNotEmpty(documentNoFilter) && StringUtils.isNotEmpty(crossStoreId) //
        ? " and ord.organization.id in (" //
            + StringCollectionUtils.commaSeparated(POSUtils.getOrgListByCrossStoreId(crossStoreId),
                true) //
            + " )" //
        : " and ord.$orgId";
  }

  private static String getColumnFilterValue(JSONObject jsonsent, final String columnFilter) {
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
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
