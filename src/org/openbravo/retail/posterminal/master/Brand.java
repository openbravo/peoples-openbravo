/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Brand extends ProcessHQLQuery {
  public static final String brandPropertyExtension = "OBPOS_BrandExtension";
  public static final Logger log = Logger.getLogger(Brand.class);

  @Inject
  @Any
  @Qualifier(brandPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    boolean forceRemote = false;
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    Map<String, Object> args = new HashMap<String, Object>();

    if (!isRemote && jsonsent.has("remoteFilters")) {
      forceRemote = true;
      args.put("forceRemote", forceRemote);
    }
    HQLPropertyList brandHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    propertiesList.add(brandHQLProperties);
    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
      boolean forceRemote = false;
      boolean isRemote = false;
      try {
        OBContext.setAdminMode(false);
        isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
            OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                .getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }

      if (!isRemote && jsonsent.has("remoteFilters")) {
        forceRemote = true;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("forceRemote", forceRemote);
      }
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("productListId", productList.getId());

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();
    boolean forceRemote = false;
    HQLPropertyList regularBrandsHQLProperties;
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    if (!isRemote && jsonsent.has("remoteFilters")) {
      forceRemote = true;
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("forceRemote", forceRemote);
      regularBrandsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions, args);
    } else {
      regularBrandsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    }
    if (isRemote || forceRemote) {
      hqlQueries
          .add("select"
              + regularBrandsHQLProperties.getHqlSelect() //
              + "from Brand brand " //
              + "where "
              + " exists (select 1 from  Product p, OBRETCO_Prol_Product assort "
              + " where brand.id=p.brand.id "
              + " and p.id= assort.product.id "
              + " and assort.obretcoProductlist.id= :productListId)"
              + " AND $filtersCriteria AND $hqlCriteria and  $naturalOrgCriteria and $incrementalUpdateCriteria and brand.active = true "
              + "order by brand.name, brand.id");
    } else {
      hqlQueries
          .add("select"
              + regularBrandsHQLProperties.getHqlSelect() //
              + "from Product product " //
              + "where exists (select 1 from OBRETCO_Prol_Product assort where obretcoProductlist.id= :productListId "
              + "and assort.product = product) "
              + "and $naturalOrgCriteria and $incrementalUpdateCriteria and product.active = true "
              + "order by product.brand.name, product.brand.id");
    }

    return hqlQueries;
  }
}