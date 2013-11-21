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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BPLocation extends ProcessHQLQuery {
  public static final String bpLocationPropertyExtension = "OBPOS_BPLocationExtension";

  @Inject
  @Any
  @Qualifier(bpLocationPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();
    Organization org = POSUtils.getOrganization(OBContext.getOBContext().getCurrentOrganization()
        .getId());

    HQLPropertyList regularBPLocationHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries
        .add("select"
            + regularBPLocationHQLProperties.getHqlSelect()
            + "from BusinessPartnerLocation bploc "
            + "where bploc.businessPartner.id in ("
            + "SELECT"
            + " bpl.businessPartner.id "
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
            + ") ORDER BY bploc.locationAddress.addressLine1");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
