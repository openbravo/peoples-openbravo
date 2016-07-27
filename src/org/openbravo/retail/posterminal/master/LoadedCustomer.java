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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class LoadedCustomer extends ProcessHQLQuery {
  public static final String businessPartnerPropertyExtension = "OBPOS_BusinessPartnerExtension";
  public static final String bpLocationPropertyExtension = "OBPOS_BPLocationExtension";

  @Inject
  @Any
  @Qualifier(businessPartnerPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(bpLocationPropertyExtension)
  private Instance<ModelExtension> extensionsLoc;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("businessPartnerId",
          jsonsent.getJSONObject("parameters").getJSONObject("bpartnerId").get("value"));
      paramValues.put("bplocId", jsonsent.getJSONObject("parameters").getJSONObject("bpLocationId")
          .get("value"));

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

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
        + "Where bpl.businessPartner.id= :businessPartnerId "
        + " and bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true "
        + " and bpls.$readableSimpleClientCriteria AND "
        + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)"
        + " and (ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner=bpl.businessPartner  group by ulist2.businessPartner))"
        + " ORDER BY bpl.businessPartner.name";
    customers.add(hql);
    hql = " select" + bpartnerLocHQLProperties.getHqlSelect()
        + " from BusinessPartnerLocation AS bploc " + "Where bploc.id= :bplocId"
        + " ORDER BY bploc.locationAddress.addressLine1";
    customers.add(hql);
    return customers;
  }
}
