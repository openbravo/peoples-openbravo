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
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays
        .asList(new String[] {
            "SELECT bpl.businessPartner.id as id, bpl.businessPartner.organization.id as organization, bpl.businessPartner.name as name, bpl.businessPartner.name as _identifier, "
                + "bpl.businessPartner.searchKey as searchKey, bpl.businessPartner.description as description, bpl.businessPartner.taxID as taxID, "
                + "bpl.businessPartner.sOBPTaxCategory.id as taxCategory, bpl.businessPartner.priceList.id as priceList, "
                + "bpl.businessPartner.paymentMethod.id as paymentMethod, bpl.businessPartner.paymentTerms.id as paymentTerms, "
                + "bpl.businessPartner.invoiceTerms as invoiceTerms, bpl.id as locId, max(bpl.locationAddress.addressLine1) as locName, ulist.email as email, "
                + "ulist.id as contactId, "
                + "ulist.phone as phone, bpl.locationAddress.cityName as cityName, bpl.locationAddress.postalCode as postalCode, "
                + "bpl.businessPartner.businessPartnerCategory.id as businessPartnerCategory, "
                + "bpl.businessPartner.businessPartnerCategory.name as businessPartnerCategory_name, "
                + "bpl.businessPartner.creditLimit as creditLimit, "
                + "bpl.businessPartner.creditUsed as creditUsed "
                + "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
                + "WHERE "
                + "bpl.invoiceToAddress = true AND "
                + "bpl.businessPartner.customer = true AND "
                + "bpl.businessPartner.priceList IS NOT NULL AND "
                + "bpl.$readableClientCriteria AND "
                + "bpl.$naturalOrgCriteria AND"
                + "(bpl.$incrementalUpdateCriteria or bpl.businessPartner.$incrementalUpdateCriteria or bpl.locationAddress.$incrementalUpdateCriteria or ulist.$incrementalUpdateCriteria) AND bpl.businessPartner.active=true"
                + " and bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.$readableClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)"
                + " and ulist.id is null "
                + " GROUP BY bpl.businessPartner.id, bpl.businessPartner.organization.id, bpl.businessPartner.name, bpl.businessPartner.name, bpl.businessPartner.searchKey, bpl.businessPartner.description, bpl.businessPartner.taxID, bpl.businessPartner.sOBPTaxCategory.id, bpl.businessPartner.priceList.id, bpl.businessPartner.paymentMethod.id, bpl.businessPartner.paymentTerms.id, bpl.businessPartner.invoiceTerms, ulist.email, ulist.phone,bpl.locationAddress.cityName, bpl.locationAddress.postalCode, bpl.businessPartner.businessPartnerCategory.id, bpl.businessPartner.businessPartnerCategory.name, bpl.businessPartner.creditLimit, bpl.businessPartner.creditUsed, bpl.id , ulist.id  "
                + " ORDER BY bpl.businessPartner.name",
            "SELECT bpl.businessPartner.id as id, bpl.businessPartner.organization.id as organization, bpl.businessPartner.name as name, bpl.businessPartner.name as _identifier, "
                + "bpl.businessPartner.searchKey as searchKey, bpl.businessPartner.description as description, bpl.businessPartner.taxID as taxID, "
                + "bpl.businessPartner.sOBPTaxCategory.id as taxCategory, bpl.businessPartner.priceList.id as priceList, "
                + "bpl.businessPartner.paymentMethod.id as paymentMethod, bpl.businessPartner.paymentTerms.id as paymentTerms, "
                + "bpl.businessPartner.invoiceTerms as invoiceTerms, bpl.id as locId, max(bpl.locationAddress.addressLine1) as locName, ulist.email as email, "
                + "ulist.id as contactId, "
                + "ulist.phone as phone, bpl.locationAddress.cityName as cityName, bpl.locationAddress.postalCode as postalCode, "
                + "bpl.businessPartner.businessPartnerCategory.id as businessPartnerCategory, "
                + "bpl.businessPartner.businessPartnerCategory.name as businessPartnerCategory_name, "
                + "bpl.businessPartner.creditLimit as creditLimit, "
                + "bpl.businessPartner.creditUsed as creditUsed "
                + "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
                + "WHERE "
                + "bpl.invoiceToAddress = true AND "
                + "bpl.businessPartner.customer = true AND "
                + "bpl.businessPartner.priceList IS NOT NULL AND "
                + "bpl.$readableClientCriteria AND "
                + "bpl.$naturalOrgCriteria AND"
                + " (bpl.$incrementalUpdateCriteria or bpl.businessPartner.$incrementalUpdateCriteria or bpl.locationAddress.$incrementalUpdateCriteria or ulist.$incrementalUpdateCriteria) AND bpl.businessPartner.active=true"
                + " and bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.$readableClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)"
                + " and (ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner is not null group by ulist2.businessPartner))"
                + " GROUP BY bpl.businessPartner.id, bpl.businessPartner.organization.id, bpl.businessPartner.name, bpl.businessPartner.name, bpl.businessPartner.searchKey, bpl.businessPartner.description, bpl.businessPartner.taxID, bpl.businessPartner.sOBPTaxCategory.id, bpl.businessPartner.priceList.id, bpl.businessPartner.paymentMethod.id, bpl.businessPartner.paymentTerms.id, bpl.businessPartner.invoiceTerms, ulist.email, ulist.phone,bpl.locationAddress.cityName, bpl.locationAddress.postalCode, bpl.businessPartner.businessPartnerCategory.id, bpl.businessPartner.businessPartnerCategory.name, bpl.businessPartner.creditLimit, bpl.businessPartner.creditUsed, bpl.id , ulist.id  "
                + " ORDER BY bpl.businessPartner.name" });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
