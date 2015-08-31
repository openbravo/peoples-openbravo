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
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class POSProdFiles extends ProcessHQLQuery {
  public static final String posProdFilesPropertyExtension = "OBPOS_ProdFilesExtension";

  @Inject
  @Any
  @Qualifier(posProdFilesPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties() {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList posProdFilesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    propertiesList.add(posProdFilesHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);

    final Date terminalDate = OBMOBCUtils.calculateServerDate(jsonsent.getJSONObject("parameters")
        .getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));

    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
        terminalDate);

    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularOBPOSFilesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries
        .add("select "
            + regularOBPOSFilesHQLProperties.getHqlSelect()
            + "from OBPOS_Prod_Files pf "
            + "where $filtersCriteria AND exists (select 1 from OBRETCO_Prol_Product opp where opp.product.id = pf.product.id AND opp.obretcoProductlist.id = '"
            + productList.getId()
            + "')"
            + " AND exists (select 1 from PricingProductPrice ppp where ppp.product.id = pf.product.id AND ppp.priceListVersion.id = '"
            + priceListVersion.getId() + "')" + " AND " + "pf.$readableSimpleClientCriteria AND "
            + "pf.$naturalOrgCriteria AND " + "(pf.$incrementalUpdateCriteria) "
            + " order by pf.product.id");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
