/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ProductPrice extends ProcessHQLQuery {
  public static final String productPricePropertyExtension = "OBPOS_ProductPriceExtension";

  @Inject
  @Any
  @Qualifier(productPricePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    Date terminalDate = OBMOBCUtils.calculateServerDate(jsonsent.getJSONObject("parameters")
        .getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));
    String plvIds = PriceList.getPriceListVersionIds(orgId, terminalDate);
    if (plvIds.equals("")) {
      plvIds = "'-1'";
    }

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList priceListHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);

    hqlQueries
        .add("select "
            + priceListHQLProperties.getHqlSelect()
            + "from OBRETCO_Prol_Product as pli, PricingProductPrice ppp "
            + "where pli.product.id = ppp.product.id and pli.obretcoProductlist = '"
            + productList.getId()
            + "' and ppp.priceListVersion.id in ("
            + plvIds
            + ") "
            + "and pli.$naturalOrgCriteria and pli.$readableClientCriteria and (ppp.$incrementalUpdateCriteria)");

    return hqlQueries;
  }
}