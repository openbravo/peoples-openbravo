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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSApplications;

public class TaxRate extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    final OBQuery<OBPOSApplications> posAppQuery = OBDal.getInstance().createQuery(
        OBPOSApplications.class, "where searchKey = 'POS-1'"); // FIXME: value is not unique
    final OBPOSApplications posDetail = posAppQuery.list().get(0); // FIXME: could throw
                                                                   // IndexOutOfBoundsException
    OBContext.setAdminMode();

    // FROM
    final OrganizationInformation orgInfo = posDetail.getOrganization()
        .getOrganizationInformationList().get(0); // FIXME: expected org info?
                                                  // IndexOutOfBoundsException?

    final Country fromCountry = orgInfo.getLocationAddress().getCountry();
    final Region fromRegion = orgInfo.getLocationAddress().getRegion();

    // TO
    final Country toCountry = null; // posDetail.getPartnerAddress().getLocationAddress().getCountry();
    final Region toRegion = null; // posDetail.getPartnerAddress().getLocationAddress().getRegion();

    return "from FinancialMgmtTaxRate where active = true "
        + "and parentTaxRate is null and salesPurchaseType in ('S', 'B') and (country.id = '"
        + fromCountry.getId() + "' or country is null) and (region.id = '" + fromRegion.getId()
        + "' or region is null) and (destinationCountry.id = '" + toCountry.getId()
        + "' or destinationCountry is null) and (destinationRegion.id = '" + toRegion.getId()
        + "' or destinationRegion is null) and $readableCriteria order by validFromDate desc ";
  }
}
