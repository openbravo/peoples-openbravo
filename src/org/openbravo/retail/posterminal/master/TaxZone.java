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
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("TaxZone")
public class TaxZone extends MasterDataProcessHQLQuery {
  public static final String taxZonePropertyExtension = "OBPOS_TaxZoneExtension";

  @Inject
  @Any
  @Qualifier(taxZonePropertyExtension)
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

    String hqlTax = " select financialMgmtTaxRate.id from FinancialMgmtTaxRate as financialMgmtTaxRate"
        + " where financialMgmtTaxRate.salesPurchaseType in ('S', 'B')"
        + " and (financialMgmtTaxRate.summaryLevel = false"
        + " or exists (select 1 from FinancialMgmtTaxCategory as financialMgmtTaxCategory where financialMgmtTaxRate.taxCategory.id = financialMgmtTaxCategory.id and financialMgmtTaxCategory.asbom = true))";

    if (fromCountry != null) {
      hqlTax = hqlTax + " and (financialMgmtTaxRate.country.id = :fromCountryId"
          + " or (financialMgmtTaxRate.country is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + " or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry.id = :fromCountryId)"
          + " or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry is null)))";
    } else {
      hqlTax = hqlTax + " and financialMgmtTaxRate.country is null";
    }
    if (fromRegion != null) {
      hqlTax = hqlTax + " and (financialMgmtTaxRate.region.id = :fromRegionId"
          + " or (financialMgmtTaxRate.region is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + " or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion.id =  :fromRegionId)"
          + " or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion is null)))";

    } else {
      hqlTax = hqlTax + " and financialMgmtTaxRate.region is null";
    }

    HQLPropertyList regularTaxZoneHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String hql = " select " + regularTaxZoneHQLProperties.getHqlSelect()
        + " from FinancialMgmtTaxZone as financialMgmtTaxZone"
        + " join financialMgmtTaxZone.tax as tax"
        + " where financialMgmtTaxZone.$readableSimpleClientCriteria"
        + " and financialMgmtTaxZone.$naturalOrgCriteria"
        + " and financialMgmtTaxZone.$incrementalUpdateCriteria"
        + " and financialMgmtTaxZone.tax in (" + hqlTax + ")";

    if (fromCountry != null) {
      hql = hql
          + " and (financialMgmtTaxZone.fromCountry.id = :fromCountryId or financialMgmtTaxZone.fromCountry is null)";
    } else {
      hql = hql + " and financialMgmtTaxZone.fromCountry is null";
    }
    if (fromRegion != null) {
      hql = hql
          + " and (financialMgmtTaxZone.fromRegion.id = :fromRegionId or financialMgmtTaxZone.fromRegion is null)";
    } else {
      hql = hql + " and financialMgmtTaxZone.fromRegion is null";
    }
    hql = hql + " order by financialMgmtTaxZone.id asc";

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
