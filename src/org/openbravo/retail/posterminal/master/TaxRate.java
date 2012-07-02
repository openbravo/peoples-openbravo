/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
  protected String getQuery(JSONObject jsonsent) throws JSONException {

    final OBPOSApplications posDetail = POSUtils.getTerminal("POS-1"); // FIXME: use parameters

    // FROM
    final OrganizationInformation storeInfo = posDetail.getOrganization()
        .getOrganizationInformationList().get(0); // FIXME: expected org info?
                                                  // IndexOutOfBoundsException?

    final Country fromCountry = storeInfo.getLocationAddress().getCountry();
    final Region fromRegion = storeInfo.getLocationAddress().getRegion();

    // TO
    final Country toCountry = posDetail.getOrganization().getObretcoCBpLocation()
        .getLocationAddress().getCountry();
    final Region toRegion = posDetail.getOrganization().getObretcoCBpLocation()
        .getLocationAddress().getRegion();

    return "from FinancialMgmtTaxRate where active = true "
        + "and parentTaxRate is null and salesPurchaseType in ('S', 'B') and (country.id = '"
        + fromCountry.getId() + "' or country is null) and (region.id = '" + fromRegion.getId()
        + "' or region is null) and (destinationCountry.id = '" + toCountry.getId()
        + "' or destinationCountry is null) and (destinationRegion.id = '" + toRegion.getId()
        + "' or destinationRegion is null) and $readableCriteria order by validFromDate desc ";
  }
}
