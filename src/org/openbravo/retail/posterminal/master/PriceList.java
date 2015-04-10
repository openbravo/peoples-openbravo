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
import org.hibernate.Query;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.posterminal.POSUtils;
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

    hqlQueries.add("select " + priceListHQLProperties.getHqlSelect() + getPriceListSelect(orgId));

    return hqlQueries;
  }

  public static String getPriceListSelect(String orgId) {
    return " from PricingPriceList pl "
        + "where pl.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
        + "and pl.id <> (select obretcoPricelist.id from Organization where id = '" + orgId + "') "
        + "and $naturalOrgCriteria and $readableClientCriteria and ($incrementalUpdateCriteria)";
  }

  public static String getPriceListVersionIds(String orgId, Date terminalDate) {
    Query priceListQuery = OBDal
        .getInstance()
        .getSession()
        .createQuery(
            "from PricingPriceList pl "
                + "where pl.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
                + "and pl.id <> (select obretcoPricelist.id from Organization where id = '" + orgId
                + "')");
    String plvIds = "";
    for (Object pl : priceListQuery.list()) {
      PriceListVersion plv = POSUtils.getPriceListVersionForPriceList(
          ((org.openbravo.model.pricing.pricelist.PriceList) pl).getId(), terminalDate);
      if (plv != null) {
        if (!plvIds.equals("")) {
          plvIds += ", ";
        }
        plvIds += "'" + plv.getId() + "'";
      }
    }
    return plvIds;
  }

  public static String getPriceListIds(String orgId) {
    Query priceListQuery = OBDal
        .getInstance()
        .getSession()
        .createQuery(
            "from PricingPriceList pl "
                + "where pl.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
                + "and pl.id <> (select obretcoPricelist.id from Organization where id = '" + orgId
                + "')");
    String plIds = "";
    for (Object pl : priceListQuery.list()) {
      if (!plIds.equals("")) {
        plIds += ", ";
      }
      plIds += "'" + ((org.openbravo.model.pricing.pricelist.PriceList) pl).getId() + "'";
    }
    return plIds;
  }
}