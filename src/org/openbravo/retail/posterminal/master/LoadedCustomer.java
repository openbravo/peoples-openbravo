/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class LoadedCustomer extends ProcessHQLQuery {
  public static final String businessPartnerPropertyExtension = "OBPOS_BusinessPartnerExtension";
  public static final String bpLocationPropertyExtension = "OBPOS_BPLocationExtension";
  public static final Logger log = Logger.getLogger(BusinessPartner.class);

  @Inject
  @Any
  @Qualifier(businessPartnerPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(bpLocationPropertyExtension)
  private Instance<ModelExtension> extensionsLoc;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> customers = new ArrayList<String>();
    HQLPropertyList bpartnerHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    HQLPropertyList bpartnerLocHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsLoc);
    String hql = "SELECT "
        + bpartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartnerLocation AS bpl left outer join bpl.businessPartner.aDUserList AS ulist "
        + "left outer join bpl.businessPartner.priceList AS plist "
        + "Where bpl.businessPartner.id='"
        + jsonsent.getJSONObject("parameters").getJSONObject("bpartnerId").get("value")
        + "'"
        + " and bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true and bpls.$readableSimpleClientCriteria AND "
        + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)"
        + " and (ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner=bpl.businessPartner  group by ulist2.businessPartner))"
        + " ORDER BY bpl.businessPartner.name";
    customers.add(hql);
    hql = "select" + bpartnerLocHQLProperties.getHqlSelect()
        + "from BusinessPartnerLocation AS bploc " + "Where bploc.id='"
        + jsonsent.getJSONObject("parameters").getJSONObject("bpLocationId").get("value") + "'"
        + "ORDER BY bploc.locationAddress.addressLine1";
    customers.add(hql);
    return customers;
  }
}
