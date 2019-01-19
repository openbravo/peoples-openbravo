/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
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
      paramValues.put("bplocId", jsonsent.getJSONObject("parameters").getJSONObject("bpLocationId")
          .get("value"));
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

    StringBuilder bpartnerHQLQuery = new StringBuilder();
    bpartnerHQLQuery.append("select ");
    bpartnerHQLQuery.append(bpartnerHQLProperties.getHqlSelect());
    bpartnerHQLQuery.append("from BusinessPartnerLocation AS bpl ");
    bpartnerHQLQuery.append(" join bpl.businessPartner as bp ");
    bpartnerHQLQuery.append(" left outer join bp.aDUserList AS ulist ");
    bpartnerHQLQuery.append(" left outer join bp.priceList AS plist ");
    bpartnerHQLQuery.append(" left outer join bp.businessPartnerLocationList AS bpsl ");
    bpartnerHQLQuery.append("where bp.id= :businessPartnerId ");
    bpartnerHQLQuery.append(" and bpl.id in (select max(bpls.id) as bpLocId ");
    bpartnerHQLQuery.append("  from BusinessPartnerLocation AS bpls ");
    bpartnerHQLQuery.append("  where bpls.businessPartner.id=bp.id ");
    bpartnerHQLQuery.append("   and bpls.invoiceToAddress = true ");
    bpartnerHQLQuery.append("   and bpls.$readableSimpleClientCriteria ");
    bpartnerHQLQuery.append("   and bpls.$naturalOrgCriteria");
    bpartnerHQLQuery.append("  group by bpls.businessPartner.id)");
    bpartnerHQLQuery.append(" and (ulist.id in (select max(ulist2.id) ");
    bpartnerHQLQuery.append("  from ADUser as ulist2 ");
    bpartnerHQLQuery.append("  where ulist2.businessPartner=bp ");
    bpartnerHQLQuery.append("  group by ulist2.businessPartner))");
    bpartnerHQLQuery.append(" and (bpsl is null or bpsl.id in ");
    bpartnerHQLQuery.append("  (select max(bpls.id) as bpLocId ");
    bpartnerHQLQuery.append("  from BusinessPartnerLocation AS bpls ");
    bpartnerHQLQuery.append("  where bpls.active=true ");
    bpartnerHQLQuery.append("  and bpls.shipToAddress = true ");
    bpartnerHQLQuery.append("  and bpls.businessPartner.id=bp.id ");
    bpartnerHQLQuery.append("  and bpls.$readableSimpleClientCriteria ");
    bpartnerHQLQuery.append("  and bpls.$naturalOrgCriteria)) ");
    bpartnerHQLQuery.append(" order by bp.name");
    customers.add(bpartnerHQLQuery.toString());

    StringBuilder bpartnerLocQuery = new StringBuilder();
    bpartnerLocQuery.append("select");
    bpartnerLocQuery.append(bpartnerLocHQLProperties.getHqlSelect());
    bpartnerLocQuery.append("from BusinessPartnerLocation AS bploc ");
    bpartnerLocQuery.append(" join bploc.businessPartner AS bp ");
    bpartnerLocQuery.append(" left join bploc.locationAddress AS bplocAddress ");
    bpartnerLocQuery.append(" left join bplocAddress.region AS bplocRegion ");
    bpartnerLocQuery.append("where bploc.id= :bplocId ");
    bpartnerLocQuery.append("order by bploc.locationAddress.addressLine1");
    customers.add(bpartnerLocQuery.toString());

    if (jsonsent.getJSONObject("parameters").has("bpBillLocationId")) {
      StringBuilder bpartnerLocationQuery = new StringBuilder();
      bpartnerLocationQuery.append("select");
      bpartnerLocationQuery.append(bpartnerLocHQLProperties.getHqlSelect());
      bpartnerLocationQuery.append("from BusinessPartnerLocation AS bploc ");
      bpartnerLocationQuery.append(" join bploc.businessPartner AS bp ");
      bpartnerLocationQuery.append(" left join bploc.locationAddress AS bplocAddress ");
      bpartnerLocationQuery.append(" left join bplocAddress.region AS bplocRegion ");
      bpartnerLocationQuery.append("where bploc.id= :bpbilllocId");
      bpartnerLocationQuery.append("order by bploc.locationAddress.addressLine1");
      customers.add(bpartnerLocationQuery.toString());
    }
    return customers;
  }
}
