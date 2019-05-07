/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQueryValidated;

public class CrossStoreFilter extends ProcessHQLQueryValidated {

  public static final String crossStorePropertyExtension = "OBPOS_CrossStoreExtension";
  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(crossStorePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
      final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
      final boolean showProductsWithCurrentPrice = isMultipricelist && isMultiPriceListSearch;
      final String posId = jsonsent.getString("pos");
      final String productId = jsonsent.getString("product");
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));

      boolean filterByStock = true;
      if (jsonsent.has("remoteFilters")) {
        final JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
        for (int i = 0; i < remoteFilters.length(); i++) {
          final JSONObject filter = remoteFilters.getJSONObject(i);
          final JSONArray columns = filter.getJSONArray("columns");
          for (int j = 0; j < columns.length(); j++) {
            final String column = columns.getString(j);
            if (column.equals("stock")) {
              filterByStock = filter.getBoolean("value");
              filter.put("value", "");
              break;
            }
          }
        }
      }
      jsonsent.put("filterByStock", filterByStock);

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
      paramValues.put("productId", productId);
      paramValues.put("terminalDate", terminalDate);
      if (showProductsWithCurrentPrice) {
        paramValues.put("multipriceListVersionId",
            POSUtils.getPriceListVersionForPriceList(
                jsonsent.getJSONObject("remoteParams").getString("currentPriceList"), terminalDate)
                .getId());
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQueryValidated(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
      final boolean allowNoPriceInMainPriceList = getPreference(
          "OBPOS_allowProductsNoPriceInMainPricelist");
      final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
      final boolean showProductsWithCurrentPrice = isMultipricelist && isMultiPriceListSearch;
      final boolean showOnlyProductsWithPrice = !showProductsWithCurrentPrice
          || !allowNoPriceInMainPriceList;
      final Map<String, Boolean> args = new HashMap<>();
      args.put("showProductsWithCurrentPrice", showProductsWithCurrentPrice);
      args.put("showOnlyProductsWithPrice", showOnlyProductsWithPrice);
      final HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions, args);

      final StringBuilder hql = new StringBuilder();
      hql.append(" select" + regularProductStockHQLProperties.getHqlSelect());
      hql.append(" from Organization o");
      hql.append(" join o.organizationWarehouseList ow");
      hql.append(" join ow.warehouse w");
      hql.append(" join o.organizationWarehouseList owh");
      hql.append(" join owh.warehouse wh");
      hql.append(" join wh.locatorList l");
      hql.append(" join l.inventoryStatus ls");
      hql.append(" left join l.materialMgmtStorageDetailList sd");
      hql.append(" with sd.product.id = :productId");
      if (showOnlyProductsWithPrice) {
        hql.append(" join o.obretcoPricelist pl");
        hql.append(" join pl.pricingPriceListVersionList plv");
        hql.append(" join plv.pricingProductPriceList pp");
      }
      if (showProductsWithCurrentPrice) {
        hql.append(" , PricingProductPrice bppp");
      }
      hql.append(" where o.id in :crossStoreOrgIds");
      hql.append(" and $filtersCriteria");
      hql.append(" and ls.oBRETCOAvailableCrossStore = true");
      if (showOnlyProductsWithPrice) {
        hql.append(" and pp.product.id = :productId");
        hql.append(" and plv.id = (");
        hql.append("   select min(plv2.id)");
        hql.append("   from PricingPriceListVersion plv2");
        hql.append("   where plv2.priceList.id = pl.id");
        hql.append("   and plv2.active = true");
        hql.append("   and plv2.validFromDate = (");
        hql.append("     select max(plv3.validFromDate)");
        hql.append("     from PricingPriceListVersion plv3");
        hql.append("     where plv3.priceList.id = pl.id");
        hql.append("     and plv3.active = true");
        hql.append("     and plv3.validFromDate <= :terminalDate");
        hql.append("   )");
        hql.append(" )");
      }
      if (showProductsWithCurrentPrice) {
        hql.append(" and bppp.product.id = :productId");
        hql.append(" and bppp.priceListVersion.id = :multipriceListVersionId");
      }
      hql.append(" and o.active = true");
      hql.append(" and ow.active = true");
      hql.append(" and w.active = true");
      hql.append(" and owh.active = true");
      hql.append(" and wh.active = true");
      hql.append(" and l.active = true");
      hql.append(" and pl.active = true");
      hql.append(" and plv.active = true");
      hql.append(" group by o.id, o.name, w.id, w.name");
      if (showOnlyProductsWithPrice) {
        hql.append(" , pp.standardPrice");
      }
      if (showProductsWithCurrentPrice) {
        hql.append(" , bppp.standardPrice");
      }
      hql.append(" having w.id = min(wh.id)");
      if (jsonsent.getBoolean("filterByStock")) {
        hql.append(" and coalesce(sum(sd.quantityOnHand - sd.reservedQty), 0) > 0");
      }

      return Collections.singletonList(hql.toString());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean getPreference(final String preference) {
    OBContext.setAdminMode(false);
    boolean value;
    try {
      value = StringUtils.equals(Preferences.getPreferenceValue(preference, true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null), "Y");
    } catch (PropertyException e) {
      value = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return value;
  }

  private static boolean isMultiPriceListSearch(final JSONObject jsonsent) {
    boolean multiPriceListSearch = false;
    try {
      multiPriceListSearch = jsonsent.has("remoteParams") && StringUtils
          .isNotEmpty(jsonsent.getJSONObject("remoteParams").optString("currentPriceList"));
    } catch (JSONException e) {
      log.error("Error while getting currentPriceList " + e.getMessage(), e);
    }
    return multiPriceListSearch;
  }

  @Override
  protected String getFilterEntity() {
    return "CrossStoreFilter";
  }

}
