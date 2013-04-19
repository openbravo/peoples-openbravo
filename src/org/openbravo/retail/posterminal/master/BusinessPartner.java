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
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    Organization org = POSUtils.getOrganization(OBContext.getOBContext().getCurrentOrganization()
        .getId());
    return Arrays
        .asList(new String[] { "SELECT bpl.businessPartner.id as id, bpl.businessPartner.organization.id as organization, bpl.businessPartner.name as name, bpl.businessPartner.name as _identifier, "
            + "bpl.businessPartner.searchKey as searchKey, bpl.businessPartner.description as description, bpl.businessPartner.taxID as taxID, "
            + "bpl.businessPartner.sOBPTaxCategory.id as taxCategory, bpl.businessPartner.priceList.id as priceList, "
            + "bpl.businessPartner.paymentMethod.id as paymentMethod, bpl.businessPartner.paymentTerms.id as paymentTerms, "
            + "bpl.businessPartner.invoiceTerms as invoiceTerms, bpl.id as locId, max(bpl.name) as locName, ulist.email as email, "
            + "ulist.id as contactId, "
            + "ulist.phone as phone, bpl.locationAddress.cityName as cityName, bpl.locationAddress.postalCode as postalCode, "
            + "bpl.businessPartner.businessPartnerCategory.id as businessPartnerCategory, "
            + "bpl.businessPartner.businessPartnerCategory.name as businessPartnerCategory_name, "
            + "bpl.businessPartner.creditLimit as creditLimit, "
            + "bpl.businessPartner.creditUsed as creditUsed "
            + "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
            + "WHERE (bpl.$incrementalUpdateCriteria or bpl.businessPartner.$incrementalUpdateCriteria) AND bpl.businessPartner.active=true AND ("
            + "(bpl.id = '"
            + org.getObretcoCBpLocation().getId()
            + "')"
            + " OR "
            + "(bpl.businessPartner.id <> '"
            + org.getObretcoCBpartner().getId()
            + "' AND "
            + "bpl.invoiceToAddress = true AND "
            + "bpl.businessPartner.customer = true AND "
            + "bpl.businessPartner.priceList IS NOT NULL AND "
            + "bpl.$readableClientCriteria AND "
            + "bpl.$naturalOrgCriteria "
            + "))"
            // This section is added to prevent more than one row for each business partner from
            // being
            // selected (check issues 22249 and 22256)
            + " AND bpl.id in (SELECT max(bpl2.id)"
            + "FROM BusinessPartnerLocation AS bpl2 "
            + "WHERE (bpl2.businessPartner.$incrementalUpdateCriteria) AND ("
            + "(bpl2.id = '"
            + org.getObretcoCBpLocation().getId()
            + "')"
            + " OR "
            + "(bpl2.businessPartner.id <> '"
            + org.getObretcoCBpartner().getId()
            + "' AND "
            + "bpl2.invoiceToAddress = true AND "
            + "bpl2.businessPartner.customer = true AND "
            + "bpl2.$readableClientCriteria AND "
            + "bpl2.$naturalOrgCriteria "
            + ")) GROUP BY bpl2.businessPartner.id)"
            + " AND (ulist is null or ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner is not null group by ulist2.businessPartner))"
            // Here the section to prevent the same business partner from being selected more than
            // once
            // ends
            + "GROUP BY bpl.businessPartner.id, bpl.businessPartner.organization.id, bpl.businessPartner.name, bpl.businessPartner.name, bpl.businessPartner.searchKey, bpl.businessPartner.description, bpl.businessPartner.taxID, bpl.businessPartner.sOBPTaxCategory.id, bpl.businessPartner.priceList.id, bpl.businessPartner.paymentMethod.id, bpl.businessPartner.paymentTerms.id, bpl.businessPartner.invoiceTerms, bpl.id, ulist.email, ulist.id, ulist.phone,bpl.locationAddress.cityName, bpl.locationAddress.postalCode, bpl.businessPartner.businessPartnerCategory.id, bpl.businessPartner.businessPartnerCategory.name, bpl.businessPartner.creditLimit, bpl.businessPartner.creditUsed "
            + "ORDER BY bpl.businessPartner.name" });
    // probar los casos con varias loc para un mismo BP
    // return "select bp as BusinessPartner, loc as BusinessPartnerLocation "
    // + "from BusinessPartner bp, BusinessPartnerLocation loc "
    // +
    // "where bp.id = loc.businessPartner.id and bp.customer = true and bp.$readableClientCriteria and bp.$naturalOrgCriteria";
  }
}
