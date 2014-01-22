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
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class TaxZone extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    OBPOSApplications posDetail;

    posDetail = POSUtils.getTerminalById(RequestContext.get().getSessionAttribute("POSTerminal")
        .toString());

    if (posDetail == null) {
      throw new OBException("terminal id is not present in session ");
    }

    // FROM
    final OrganizationInformation storeInfo = posDetail.getOrganization()
        .getOrganizationInformationList().get(0); // FIXME: expected org info?
                                                  // IndexOutOfBoundsException?

    final Country fromCountry = storeInfo.getLocationAddress().getCountry();
    final Region fromRegion = storeInfo.getLocationAddress().getRegion();

    String hqlTax = "select id from FinancialMgmtTaxRate as financialMgmtTaxRate where "
        + "financialMgmtTaxRate.$readableClientCriteria AND "
        + "financialMgmtTaxRate.$naturalOrgCriteria AND "
        + "(financialMgmtTaxRate.$incrementalUpdateCriteria) AND active = true "
        + "and salesPurchaseType in ('S', 'B') ";

    if (fromCountry != null) {
      hqlTax = hqlTax
          + "and (financialMgmtTaxRate.country.id = '"
          + fromCountry.getId()
          + "' or (financialMgmtTaxRate.country is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry.id = '"
          + fromCountry.getId()
          + "')"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromCountry is null)))";
    } else {
      hqlTax = hqlTax + "and financialMgmtTaxRate.country is null ";
    }
    if (fromRegion != null) {
      hqlTax = hqlTax
          + "and (financialMgmtTaxRate.region.id = '"
          + fromRegion.getId()
          + "' or (financialMgmtTaxRate.region is null and (not exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate))"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion.id = '"
          + fromRegion.getId()
          + "')"
          + "  or exists (select z from FinancialMgmtTaxZone as z where z.tax = financialMgmtTaxRate and z.fromRegion is null))))";

    } else {
      hqlTax = hqlTax + "and financialMgmtTaxRate.region is null ";
    }
    hqlTax = hqlTax + "and $readableCriteria";

    String hql = "from FinancialMgmtTaxZone as financialMgmtTaxZone where "
        + "financialMgmtTaxZone.$readableClientCriteria AND "
        + "financialMgmtTaxZone.$naturalOrgCriteria AND "
        + "(financialMgmtTaxZone.$incrementalUpdateCriteria) AND active = true " + "and tax in ("
        + hqlTax + ")";

    if (fromCountry != null) {
      hql = hql + "and (fromCountry.id = '" + fromCountry.getId() + "' or fromCountry is null) ";
    } else {
      hql = hql + "and fromCountry is null ";
    }
    if (fromRegion != null) {
      hql = hql + "and (fromRegion.id = '" + fromRegion.getId() + "' or fromRegion is null) ";
    } else {
      hql = hql + "and fromRegion is null ";
    }
    hql = hql + "and $readableCriteria";

    return Arrays.asList(new String[] { hql });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}