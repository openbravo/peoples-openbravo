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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {
  public static final String businessPartnerPropertyExtension = "OBPOS_BusinessPartnerExtension";

  @Inject
  @Any
  @Qualifier(businessPartnerPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList regularBusinessPartnerHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    return Arrays
        .asList(new String[] {
            "SELECT "
                + regularBusinessPartnerHQLProperties.getHqlSelect() //
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
                + " and ulist.id is null " + " GROUP BY "
                + regularBusinessPartnerHQLProperties.getHqlGroupBy()
                + " ORDER BY bpl.businessPartner.name",
            "SELECT"
                + regularBusinessPartnerHQLProperties.getHqlSelect() //
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
                + " GROUP BY " + regularBusinessPartnerHQLProperties.getHqlGroupBy()
                + " ORDER BY bpl.businessPartner.name" });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
