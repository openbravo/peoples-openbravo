/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class TaxRate extends ProcessHQLQuery {

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
          .getOrganizationInformationList().get(0);
      final Country fromCountry = storeInfo.getLocationAddress().getCountry();
      final Region fromRegion = storeInfo.getLocationAddress().getRegion();
      Map<String, Object> paramValues = new HashMap<String, Object>();
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
        .getOrganizationInformationList().get(0); // FIXME: expected org info?
                                                  // IndexOutOfBoundsException?

    final Country fromCountry = storeInfo.getLocationAddress().getCountry();
    final Region fromRegion = storeInfo.getLocationAddress().getRegion();

    String hql = "from FinancialMgmtTaxRate as financialMgmtTaxRate where "
        + "financialMgmtTaxRate.$readableSimpleClientCriteria AND "
        + "financialMgmtTaxRate.$naturalOrgCriteria AND "
        + "(financialMgmtTaxRate.$incrementalUpdateCriteria) "
        + "and financialMgmtTaxRate.salesPurchaseType in ('S', 'B') ";

    if (fromCountry != null) {
      hql = hql
          + "and (financialMgmtTaxRate.country.id = :fromCountryId "
          + "  or (financialMgmtTaxRate.country is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry.id = :fromCountryId )"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry is null)))";
    } else {
      hql = hql + "and financialMgmtTaxRate.country is null ";
    }
    if (fromRegion != null) {
      hql = hql
          + "and (financialMgmtTaxRate.region.id = :fromRegionId  "
          + " or (financialMgmtTaxRate.region is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + " or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion.id = :fromRegionId )"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion is null)))";

    } else {
      hql = hql + "and financialMgmtTaxRate.region is null ";
    }
    hql = hql + "and $readableSimpleCriteria order by validFromDate desc, financialMgmtTaxRate.id";

    return Arrays.asList(new String[] { hql });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
