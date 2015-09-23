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
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ProductCharacteristicValue extends ProcessHQLQuery {
  public static final String productCharacteristicValuePropertyExtension = "OBPOS_ProductCharacteristicValueExtension";

  @Inject
  @Any
  @Qualifier(productCharacteristicValuePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    
    final Date terminalDate = OBMOBCUtils.calculateServerDate(jsonsent.getJSONObject("parameters")
            .getString("terminalTime"),
            jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));
    
    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
        terminalDate);
    
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsCharacteristicHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries
        .add("select "
            + regularProductsCharacteristicHQLProperties.getHqlSelect()
            + "from ProductCharacteristicValue pcv "
            + "where pcv.product.id in (select product.id from OBRETCO_Prol_Product assort where obretcoProductlist.id= '"
            + productList.getId()
            + "') "
            + "AND exists (select 1 from PricingProductPrice ppp,PricingPriceListVersion pplv WHERE (pplv.id='"+ priceListVersion.getId() 
            +"') AND (ppp.priceListVersion.id = pplv.id) AND (ppp.product.id=pcv.product.id) ) "
            + "and $naturalOrgCriteria and $readableSimpleClientCriteria and (pcv.$incrementalUpdateCriteria"
            + "OR pcv.characteristic.$incrementalUpdateCriteria OR pcv.characteristicValue.$incrementalUpdateCriteria)");

    return hqlQueries;
  }
}
