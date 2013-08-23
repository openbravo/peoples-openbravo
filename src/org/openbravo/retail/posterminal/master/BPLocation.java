/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 * 
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BPLocation extends ProcessHQLQuery {

    @Override
    protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

	List<String> hqlQueries = new ArrayList<String>();
	Organization org = POSUtils.getOrganization(OBContext.getOBContext()
		.getCurrentOrganization().getId());
	hqlQueries
		.add("select bploc.id as id, bploc.businessPartner.id as bpartner, bploc.locationAddress.addressLine1 as name, bploc.locationAddress.postalCode as postalCode, bploc.locationAddress.cityName as cityName, bploc.locationAddress.addressLine1 as _identifier "
			+ "from BusinessPartnerLocation bploc "
			+ "where bploc.businessPartner.id in ("
			+ "SELECT bpl.businessPartner.id "
			+ "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
			+ "WHERE (bpl.$incrementalUpdateCriteria or bpl.businessPartner.$incrementalUpdateCriteria or bpl.locationAddress.$incrementalUpdateCriteria or ulist.$incrementalUpdateCriteria) AND bpl.businessPartner.active=true AND ("
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
			// This section is added to prevent more than one row
			// for each business partner from
			// being
			// selected (check issues 22249 and 22256)
			+ " AND bpl.id in (SELECT max(bpl2.id)"
			+ "FROM BusinessPartnerLocation AS bpl2 "
			+ "WHERE (bpl2.$incrementalUpdateCriteria or bpl2.businessPartner.$incrementalUpdateCriteria or bpl2.locationAddress.$incrementalUpdateCriteria or ulist.$incrementalUpdateCriteria) AND ("
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
			// Here the section to prevent the same business partner
			// from being selected more than
			// once
			// ends
			+ "GROUP BY bpl.businessPartner.id, bpl.businessPartner.organization.id, bpl.businessPartner.name, bpl.businessPartner.name, bpl.businessPartner.searchKey, bpl.businessPartner.description, bpl.businessPartner.taxID, bpl.businessPartner.sOBPTaxCategory.id, bpl.businessPartner.priceList.id, bpl.businessPartner.paymentMethod.id, bpl.businessPartner.paymentTerms.id, bpl.businessPartner.invoiceTerms, bpl.id, ulist.email, ulist.id, ulist.phone,bpl.locationAddress.cityName, bpl.locationAddress.postalCode, bpl.businessPartner.businessPartnerCategory.id, bpl.businessPartner.businessPartnerCategory.name, bpl.businessPartner.creditLimit, bpl.businessPartner.creditUsed "
			+ "ORDER BY bpl.businessPartner.name"
			+ ") ORDER BY bploc.locationAddress.addressLine1");
	return hqlQueries;
    }

    @Override
    protected boolean bypassPreferenceCheck() {
	return true;
    }
}
