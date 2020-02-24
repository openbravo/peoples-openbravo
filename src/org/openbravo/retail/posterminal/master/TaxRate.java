/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("TaxRate")
public class TaxRate extends MasterDataProcessHQLQuery {
  public static final String taxRatePropertyExtension = "OBPOS_TaxRateExtension";

  @Inject
  @Any
  @Qualifier(taxRatePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      OBPOSApplications posDetail = POSUtils.getTerminalById(jsonsent.getString("pos"));
      final OrganizationInformation storeInfo = posDetail.getOrganization()
          .getOrganizationInformationList()
          .get(0);
      final Country fromCountry = storeInfo.getLocationAddress().getCountry();
      final Region fromRegion = storeInfo.getLocationAddress().getRegion();
      Map<String, Object> paramValues = new HashMap<>();
      if (fromCountry != null) {
        paramValues.put("fromCountryId", fromCountry.getId());
      }
      if (fromRegion != null) {
        paramValues.put("fromRegionId", fromRegion.getId());
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    OBPOSApplications posDetail;

    posDetail = POSUtils.getTerminalById(jsonsent.getString("pos"));

    if (posDetail == null) {
      throw new OBException("terminal id is not present in session ");
    }

    // FROM
    final OrganizationInformation storeInfo = posDetail.getOrganization()
        .getOrganizationInformationList()
        .get(0);

    final Country fromCountry = storeInfo.getLocationAddress().getCountry();
    final Region fromRegion = storeInfo.getLocationAddress().getRegion();

    //@formatter:off
    String hql = " select " + ModelExtensionUtils.getPropertyExtensions(extensions).getHqlSelect()
        + " from FinancialMgmtTaxRate as tr"
        + " left join tr.parentTaxRate as ptr"
        + " where tr.$readableSimpleCriteria"
        + " and tr.$readableSimpleClientCriteria"
        + " and tr.$naturalOrgCriteria"
        + " and tr.$incrementalUpdateCriteria"
        + " and tr.salesPurchaseType in ('S', 'B')"
        + " and (tr.summaryLevel = false"
        + " or exists (select 1 from FinancialMgmtTaxCategory as tc where tr.taxCategory.id = tc.id and tc.asbom = true))";
    //@formatter:on

    if (fromCountry != null) {
      hql = hql + " and (tr.country.id = :fromCountryId"
          + " or (tr.country is null and (not exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id))"
          + " or exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id and tz.fromCountry.id = :fromCountryId)"
          + " or exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id and tz.fromCountry is null)))";
    } else {
      hql = hql + " and tr.country is null";
    }
    if (fromRegion != null) {
      hql = hql + " and (tr.region.id = :fromRegionId"
          + " or (tr.region is null and (not exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id))"
          + " or exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id and tz.fromRegion.id = :fromRegionId)"
          + " or exists (select 1 from FinancialMgmtTaxZone as tz where tz.tax.id = tr.id and tz.fromRegion is null)))";

    } else {
      hql = hql + " and tr.region is null";
    }
    hql = hql + " order by tr.validFromDate desc, tr.id";

    return Arrays.asList(hql);
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return ModelExtensionUtils.getPropertyExtensions(extensions)
        .getProperties()
        .stream()
        .map(HQLProperty::getHqlProperty)
        .collect(Collectors.toList());
  }
}
