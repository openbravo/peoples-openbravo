/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ServicePriceRuleRangePrices extends ProcessHQLQuery {
  public static final String servicePriceRuleRangePricesPropertyExtension = "OBPOS_ServicePriceRuleRangePricesExtension";

  @Inject
  @Any
  @Qualifier(servicePriceRuleRangePricesPropertyExtension)
  private Instance<ModelExtension> extensions;

  SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularPriceRuleRangePricesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    propertiesList.add(regularPriceRuleRangePricesHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      final Date terminalDate = OBMOBCUtils
          .calculateServerDate(
              jsonsent.getJSONObject("parameters").getString("terminalTime"),
              jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset")
                  .getLong("value"));
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("terminalDate", format.format(terminalDate));

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();
    HQLPropertyList regularPriceRuleRangePricesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    hqlQueries
        .add("select"
            + regularPriceRuleRangePricesHQLProperties.getHqlSelect()
            + "from PricingPriceList ppl join ppl.pricingPriceListVersionList pplv "
            + "join ppl.servicePriceRuleRangeList sppr join pplv.pricingProductPriceList ppp "
            + "where $filtersCriteria and $hqlCriteria and pplv.id = get_pricelist_version(pplv.priceList.id, :terminalDate ) and ppp.product.productType = 'S' "
            + "and sppr.$naturalOrgCriteria and sppr.$incrementalUpdateCriteria "
            + "order by ppl.id asc");

    return hqlQueries;
  }
}