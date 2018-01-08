/*
 ************************************************************************************
 * Copyright (C) 2016-2018 Openbravo S.L.U.
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
      paramValues.put("bplocId",
          jsonsent.getJSONObject("parameters").getJSONObject("bpLocationId").get("value"));
      if (jsonsent.getJSONObject("parameters").has("bpBillLocationId")) {
        paramValues.put("bpbilllocId",
            jsonsent.getJSONObject("parameters").getJSONObject("bpBillLocationId").get("value"));
      }

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
    String hql = "SELECT " + bpartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartnerLocation AS bpl " + "join bpl.businessPartner as bp "
        + "left outer join bp.aDUserList AS ulist " + "left outer join bp.priceList AS plist "
        + "left outer join bp.businessPartnerLocationList AS bpsl " //
        + "Where bp.id= :businessPartnerId "
        + " and bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bp.id and bpls.invoiceToAddress = true "
        + " and bpls.$readableSimpleClientCriteria AND "
        + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)"
        + " and (ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner=bp  group by ulist2.businessPartner))"
        + " and (bpsl is null or bpsl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.active=true AND bpls.shipToAddress = true and bpls.businessPartner.id=bp.id and bpls.$readableSimpleClientCriteria AND "
        + " bpls.$naturalOrgCriteria)) " //
        + " ORDER BY bp.name";
    customers.add(hql);
    hql = " select" + bpartnerLocHQLProperties.getHqlSelect()
        + " from BusinessPartnerLocation AS bploc join bploc.businessPartner AS bp "
        + "Where bploc.id= :bplocId" + " ORDER BY bploc.locationAddress.addressLine1";
    customers.add(hql);
    if (jsonsent.getJSONObject("parameters").has("bpBillLocationId")) {
      hql = " select" + bpartnerLocHQLProperties.getHqlSelect()
          + " from BusinessPartnerLocation AS bploc join bploc.businessPartner AS bp "
          + "Where bploc.id= :bpbilllocId" + " ORDER BY bploc.locationAddress.addressLine1";
      customers.add(hql);
    }
    return customers;
  }
}
