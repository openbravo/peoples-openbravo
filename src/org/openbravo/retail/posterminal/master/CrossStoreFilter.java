/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
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
  public static final String crossStoreMultiPricePropertyExtension = "OBPOS_CrossStoreMultiPriceExtension";

  @Inject
  @Any
  @Qualifier(crossStorePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(crossStoreMultiPricePropertyExtension)
  private Instance<ModelExtension> multiPriceExtensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(final JSONObject jsonsent) {
    final List<HQLPropertyList> propertiesList = new ArrayList<>();
    final boolean isMultiPriceListEnabled = getPreference("OBPOS_EnableMultiPriceList");

    final HQLPropertyList crossStoreFilterHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    propertiesList.add(crossStoreFilterHQLProperties);
    if (isMultiPriceListEnabled) {
      final HQLPropertyList crossStoreMultiPriceHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(multiPriceExtensions);
      propertiesList.add(crossStoreMultiPriceHQLProperties);
    }

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final String orgId = jsonsent.getString("organization");
      final String posId = jsonsent.getString("pos");
      final String productId = jsonsent.getString("product");
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId, true);
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
      paramValues.put("orgId", orgId);
      paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
      paramValues.put("productId", productId);
      paramValues.put("terminalDate", terminalDate);

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQueryValidated(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final List<String> hqlList = new ArrayList<>();
      final boolean isMultiPriceListEnabled = getPreference("OBPOS_EnableMultiPriceList");
      final boolean allowNoPriceInMainPriceList = getPreference(
          "OBPOS_allowProductsNoPriceInMainPricelist");
      final boolean showProductsWithoutPrice = isMultiPriceListEnabled
          && allowNoPriceInMainPriceList;
      final boolean filterByStock = jsonsent.getBoolean("filterByStock");

      final HQLPropertyList crossStoreHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);
      hqlList.add(getCrossStoreStockAndPrice(crossStoreHQLProperties, filterByStock,
          showProductsWithoutPrice));
      if (isMultiPriceListEnabled) {
        final HQLPropertyList crossStoreMultiPriceHQLProperties = ModelExtensionUtils
            .getPropertyExtensions(multiPriceExtensions);
        hqlList.add(getCrossStoreMultiPrice(crossStoreMultiPriceHQLProperties));
      }

      return hqlList;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getCrossStoreStockAndPrice(final HQLPropertyList crossStoreHQLProperties,
      final boolean filterByStock, final boolean showProductsWithoutPrice) {
    final StringBuilder hql = new StringBuilder();
    hql.append(" select" + crossStoreHQLProperties.getHqlSelect());
    hql.append(" from Organization o");
    hql.append(" join o.organizationInformationList oi");
    hql.append(" left join oi.locationAddress la");
    hql.append(" join o.organizationWarehouseList ow");
    hql.append(" join ow.warehouse w");
    hql.append(" join o.organizationWarehouseList owh");
    hql.append(" join owh.warehouse wh");
    hql.append(" join wh.locatorList l");
    hql.append(" join l.inventoryStatus ls");
    hql.append(" left join l.materialMgmtStorageDetailList sd");
    hql.append(" with sd.product.id = :productId");
    hql.append(" join o.obretcoPricelist pl");
    if (showProductsWithoutPrice) {
      hql.append(" left join pl.pricingPriceListVersionList plv");
      hql.append(" left join plv.pricingProductPriceList pp");
      hql.append(" left join pp.product p");
    } else {
      hql.append(" join pl.pricingPriceListVersionList plv");
      hql.append(" join plv.pricingProductPriceList pp");
      hql.append(" join pp.product p");
    }
    hql.append(" with p.id = :productId");
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
    hql.append(" where o.id in :crossStoreOrgIds");
    hql.append(" and $filtersCriteria");
    hql.append(" and ls.oBRETCOAvailableCrossStore = true");
    hql.append(" and pl.priceIncludesTax = (");
    hql.append("   select pl2.priceIncludesTax");
    hql.append("   from Organization o2");
    hql.append("   join o2.obretcoPricelist pl2");
    hql.append("   where o2.id = :orgId");
    hql.append(" )");
    hql.append(" and (exists (");
    hql.append("   select 1");
    hql.append("   from OBRETCO_Prol_Product pli");
    hql.append("   where pli.product.id = :productId");
    hql.append("   and pli.obretcoProductlist.id = o.obretcoProductlist.id");
    hql.append(" )");
    hql.append(" or o.id = :orgId)");
    hql.append(" and o.active = true");
    hql.append(" and owh.active = true");
    hql.append(" and wh.active = true");
    hql.append(" and l.active = true");
    hql.append(" group by " + crossStoreHQLProperties.getHqlGroupBy());
    hql.append(" having w.id = min(wh.id)");
    if (filterByStock) {
      hql.append(" and coalesce(sum(sd.quantityOnHand - sd.reservedQty), 0) > 0");
    }
    hql.append(" order by case when o.id = :orgId then 0 else 1 end, o.name");
    return hql.toString();
  }

  private String getCrossStoreMultiPrice(final HQLPropertyList crossStoreMultiPriceHQLProperties) {
    final StringBuilder hql = new StringBuilder();
    hql.append(" select" + crossStoreMultiPriceHQLProperties.getHqlSelect());
    hql.append(" from PricingPriceList bppl");
    hql.append(" join bppl.pricingPriceListVersionList bpplv");
    hql.append(" join bpplv.pricingProductPriceList bppp");
    hql.append(" where bppp.product.id = :productId");
    hql.append(" and bpplv.id = (");
    hql.append("   select min(bpplv2.id)");
    hql.append("   from PricingPriceListVersion bpplv2");
    hql.append("   where bpplv2.priceList.id = bppl.id");
    hql.append("   and bpplv2.active = true");
    hql.append("   and bpplv2.validFromDate = (");
    hql.append("     select max(bpplv3.validFromDate)");
    hql.append("     from PricingPriceListVersion bpplv3");
    hql.append("     where bpplv3.priceList.id = bppl.id");
    hql.append("     and bpplv3.active = true");
    hql.append("     and bpplv3.validFromDate <= :terminalDate");
    hql.append("   )");
    hql.append(" )");
    hql.append(" and exists (");
    hql.append("   select 1");
    hql.append("   from BusinessPartner bp");
    hql.append("   where bp.priceList.id = bppl.id");
    hql.append("   and bp.customer = true");
    hql.append(" )");
    return hql.toString();
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

  @Override
  protected String getFilterEntity() {
    return "CrossStoreFilter";
  }

}
