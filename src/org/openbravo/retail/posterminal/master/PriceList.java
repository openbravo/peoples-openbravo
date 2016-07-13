/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
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
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class PriceList extends ProcessHQLQuery {
  public static final String priceListPropertyExtension = "OBPOS_PriceListExtension";
  private static final Logger log = Logger.getLogger(PriceList.class);

  @Inject
  @Any
  @Qualifier(priceListPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
      OBPOSApplications POSTerminal = POSUtils.getTerminalById(posId);
      String pricelist = POSUtils.getPriceListByTerminal(POSTerminal.getSearchKey()).getId();
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("priceList", pricelist);

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    boolean multiPrices = false;
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList priceListHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    try {
      multiPrices = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }

    if (multiPrices) {
      hqlQueries
          .add("select "
              + priceListHQLProperties.getHqlSelect()
              + " from PricingPriceList pl "
              + "where pl.id in (select distinct priceList.id from BusinessPartner where customer = 'Y') "
              + "and pl.id <> (:priceList) "
              + "and $naturalOrgCriteria and $readableClientCriteria and ($incrementalUpdateCriteria) "
              + "order by pl.id asc");
    }

    return hqlQueries;
  }

}