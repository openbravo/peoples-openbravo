/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.text.SimpleDateFormat;
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
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class PriceList extends ProcessHQLQuery {
  public static final String priceListPropertyExtension = "OBPOS_PriceListExtension";

  @Inject
  @Any
  @Qualifier(priceListPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList priceListHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);

    hqlQueries
        .add("select "
            + priceListHQLProperties.getHqlSelect()
            + " from PricingPriceList pl "
            + "where pl.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
            + "and pl.id <> (select obretcoPricelist.id from Organization where id = '"
            + orgId
            + "') "
            + "and $naturalOrgCriteria and $readableClientCriteria and ($incrementalUpdateCriteria)");

    return hqlQueries;
  }

  public static String getSelectPriceListVersionIds(String orgId, Date terminalDate) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    return "select plv.id from PricingPriceListVersion AS plv "
        + "where plv.active = true"
        + " and plv.validFromDate = ("
        + "  select max(pplv.validFromDate) from PricingPriceListVersion as pplv "
        + "  where pplv.active=true and pplv.priceList.id = plv.priceList.id "
        + "    and to_char(pplv.validFromDate, 'yyyy-mm-dd') <= '"
        + format.format(terminalDate)
        + " ') and (plv.priceList.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
        + " and plv.priceList.id <> (select obretcoPricelist.id from Organization where id = '"
        + orgId + "'))";
  }

}