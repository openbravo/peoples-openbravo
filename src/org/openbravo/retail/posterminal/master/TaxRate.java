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
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    OBPOSApplications posDetail;

    if (jsonsent.has("pos")) {
      posDetail = POSUtils.getTerminalById(jsonsent.getString("pos"));
    } else if (jsonsent.has("terminalName")) {
      posDetail = POSUtils.getTerminal(jsonsent.getString("terminalName"));
    } else {
      throw new OBException("terminal not sent as parameter");
    }

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

    String hql = "from FinancialMgmtTaxRate as financialMgmtTaxRate where "
        + "(financialMgmtTaxRate.$incrementalUpdateCriteria) AND active = true "
        + "and salesPurchaseType in ('S', 'B') ";

    if (fromCountry != null) {
      hql = hql + "and (country.id = '" + fromCountry.getId() + "' or country is null) ";
    } else {
      hql = hql + "and country is null ";
    }
    if (fromRegion != null) {
      hql = hql + "and (region.id = '" + fromRegion.getId() + "' or region is null) ";
    } else {
      hql = hql + "and region is null ";
    }
    if (toCountry != null) {
      hql = hql + "and (destinationCountry.id = '" + toCountry.getId()
          + "' or destinationCountry is null) ";
    } else {
      hql = hql + "and destinationCountry is null ";
    }
    if (toRegion != null) {
      hql = hql + "and (destinationRegion.id = '" + toRegion.getId()
          + "' or destinationRegion is null) ";
    } else {
      hql = hql + "and destinationRegion is null ";
    }
    hql = hql + "and $readableCriteria order by validFromDate desc ";

    return Arrays.asList(new String[] { hql });
  }
}
