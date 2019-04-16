/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQueryValidated;

public class CrossStoreFilter extends ProcessHQLQueryValidated {

  public static final String crossStorePropertyExtension = "OBPOS_CrossStoreExtension";

  @Inject
  @Any
  @Qualifier(crossStorePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList crossStoreFilterHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, jsonsent);
    propertiesList.add(crossStoreFilterHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final String posId = jsonsent.getString("pos");
      final String productId = jsonsent.getString("product");
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));

      final Map<String, Object> paramValues = new HashMap<>();
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
      final HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);

      final StringBuilder hql = new StringBuilder();
      hql.append(" select" + regularProductStockHQLProperties.getHqlSelect());
      hql.append(" from Organization o");
      hql.append(" join o.organizationWarehouseList ow");
      hql.append(" join ow.warehouse w");
      hql.append(" join w.locatorList l");
      hql.append(" join l.materialMgmtStorageDetailList sd");
      hql.append(" join o.obretcoPricelist pl");
      hql.append(" join pl.pricingPriceListVersionList plv");
      hql.append(" join plv.pricingProductPriceList pp");
      hql.append(" where o.id in :crossStoreOrgIds");
      hql.append(" and $filtersCriteria");
      hql.append(" and sd.product.id = :productId");
      hql.append(" and pp.product.id = :productId");
      hql.append(" and plv.validFromDate = (");
      hql.append("   select max(plv2.validFromDate)");
      hql.append("   from PricingPriceListVersion plv2");
      hql.append("   where plv2.priceList.id = pl.id");
      hql.append("   and plv2.validFromDate <= :terminalDate");
      hql.append("   and plv2.active = true");
      hql.append(" )");
      hql.append(" and ow.obretcoIsCrossStore = true");
      hql.append(" and o.active = true");
      hql.append(" and ow.active = true");
      hql.append(" and w.active = true");
      hql.append(" and l.active = true");
      hql.append(" and sd.active = true");
      hql.append(" and pl.active = true");
      hql.append(" and plv.active = true");
      hql.append(" and pp.active = true");
      hql.append(" group by o.id, o.name, pp.standardPrice");
      hql.append(" having sum(sd.quantityOnHand - sd.reservedQty) <> 0");

      return Collections.singletonList(hql.toString());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected String getFilterEntity() {
    return "CrossStoreFilter";
  }

}
