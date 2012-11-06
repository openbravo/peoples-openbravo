/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonConstants;

public class Terminal extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    String POSSearchKey = jsonsent.getJSONObject("parameters").getJSONObject("terminal")
        .getString("value");
    OBPOSApplications pOSTerminal = POSUtils.getTerminal(POSSearchKey);
    int lastDocumentNumber = POSUtils.getLastDocumentNumberForPOS(POSSearchKey, pOSTerminal
        .getObposTerminaltype().getDocumentType().getId(), pOSTerminal.getOrderdocnoPrefix());
    int lastQuotationDocumentNumber = 0;
    if (pOSTerminal.getObposTerminaltype().getDocumentTypeForQuotations() != null) {
      lastQuotationDocumentNumber = POSUtils.getLastDocumentNumberForPOS(POSSearchKey, pOSTerminal
          .getObposTerminaltype().getDocumentTypeForQuotations().getId(),
          pOSTerminal.getQuotationdocnoPrefix());
    }
    final org.openbravo.model.pricing.pricelist.PriceList pricesList = POSUtils
        .getPriceListByTerminal(POSSearchKey);

    return "select pos.id as id, pos.organization.obretcoCBpartner.id as businessPartner, pos.name as _identifier, pos.searchKey as searchKey, pos.organization.obretcoCBpLocation.id as partnerAddress, "
        + " pos.organization.id as organization, pos.organization.name as "
        + getIdentifierAlias("organization")
        + ", pos.client.id as client, pos.client.name as "
        + getIdentifierAlias("client")
        + ", pos.hardwareurl as hardwareurl, pos.scaleurl as scaleurl, pos.obposTerminaltype.openDrawer as drawerpreference, "
        + "'"
        + pricesList.getId()
        + "' as priceList, '"
        + pricesList.getCurrency().getId()
        + "' as currency, "
        + "'"
        + pricesList.getCurrency().getIdentifier()
        + "' as "
        + getIdentifierAlias("currency")
        + ", pos.obposTerminaltype.documentType.id as documentType, pos.obposTerminaltype.documentType.name as "
        + getIdentifierAlias("documentType")
        + ", pos.obposTerminaltype.documentTypeForReturns.id as documentTypeForReturns, pos.obposTerminaltype.documentTypeForReturns.name as "
        + getIdentifierAlias("documentTypeForReturns")
        + ", quot.id as documentTypeForQuotations, quot.name as "
        + getIdentifierAlias("documentTypeForQuotations")
        + ", pos.organization.obretcoDbpIrulesid as defaultbp_invoiceterm "
        + ", pos.organization.obretcoDbpPtermid.id as defaultbp_paymentterm "
        + ", pos.organization.obretcoDbpPmethodid.id as defaultbp_paymentmethod "
        + ", pos.organization.obretcoDbpBpcatid.id as defaultbp_bpcategory "
        + ", pos.organization.obretcoDbpCountryid.id as defaultbp_bpcountry "
        + ", pos.organization.obretcoDbpOrgid.id as defaultbp_bporg "
        + ", pos.organization.obretcoShowtaxid as bp_showtaxid "
        + ", pos.organization.obretcoMWarehouse.id as warehouse "
        + ", pos.orderdocnoPrefix as docNoPrefix "
        + ", pos.quotationdocnoPrefix as quotationDocNoPrefix "
        + ", "
        + lastDocumentNumber
        + " as lastDocumentNumber, "
        + lastQuotationDocumentNumber
        + " as lastQuotationDocumentNumber, pos.obposTerminaltype.minutestorefreshdatatotal as minutestorefreshdatatotal, "
        + " pos.obposTerminaltype.minutestorefreshdatainc as minutestorefreshdatainc"
        + " from OBPOS_Applications AS pos left join pos.obposTerminaltype.documentTypeForQuotations as quot where pos.$readableCriteria and pos.searchKey = :terminal";
  }

  private String getIdentifierAlias(String propertyName) {
    return propertyName + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
  }
}
