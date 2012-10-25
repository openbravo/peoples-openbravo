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
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    Organization org = POSUtils.getOrganization(jsonsent.getString("organization"));
    return "SELECT bpl.businessPartner.id as id, bpl.businessPartner.name as name, bpl.businessPartner.name as _identifier, bpl.businessPartner.searchKey as searchKey, bpl.businessPartner.description as description, bpl.businessPartner.taxID as taxID, bpl.businessPartner.sOBPTaxCategory.id as taxCategory, bpl.businessPartner.paymentMethod.id as paymentMethod, bpl.businessPartner.paymentTerms.id as paymentTerms, bpl.businessPartner.invoiceTerms as invoiceTerms, bpl.id as locId, max(bpl.name) as locName, ulist.email as email, ulist.phone as phone, bpl.locationAddress.cityName as cityName, bpl.locationAddress.postalCode as postalCode "
        + "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
        + "WHERE (bpl.$incrementalUpdateCriteria) AND ("
        + "(bpl.id = '"
        + org.getObretcoCBpLocation().getId()
        + "')"
        + " OR "
        + "(bpl.businessPartner.id <> '"
        + org.getObretcoCBpartner().getId()
        + "' AND "
        + "bpl.invoiceToAddress = true AND "
        + "bpl.businessPartner.customer = true AND "
        + "bpl.$readableClientCriteria AND "
        + "bpl.$naturalOrgCriteria "
        + "))"
        + "GROUP BY bpl.businessPartner.id, bpl.businessPartner.name, bpl.businessPartner.name, bpl.businessPartner.searchKey, bpl.businessPartner.description, bpl.businessPartner.taxID, bpl.businessPartner.sOBPTaxCategory.id, bpl.businessPartner.paymentMethod.id, bpl.businessPartner.paymentTerms.id, bpl.businessPartner.invoiceTerms, bpl.id, ulist.email, ulist.phone,bpl.locationAddress.cityName, bpl.locationAddress.postalCode "
        + "ORDER BY bpl.businessPartner.name";
    // probar los casos con varias loc para un mismo BP
    // return "select bp as BusinessPartner, loc as BusinessPartnerLocation "
    // + "from BusinessPartner bp, BusinessPartnerLocation loc "
    // +
    // "where bp.id = loc.businessPartner.id and bp.customer = true and bp.$readableClientCriteria and bp.$naturalOrgCriteria";
  }
}
