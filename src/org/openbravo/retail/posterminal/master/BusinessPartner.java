/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
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
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularBusinessPartnerHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularBusinessPartnerHQLProperties);
    propertiesList.add(regularBusinessPartnerHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    // if it is a total refresh we need to ensure that all(AND) entities are active. In a
    // incremental refresh, we need to retrieve it if some (OR) ot the entities have changed
    String operator = lastUpdated == null ? " AND " : " OR ";
    HQLPropertyList regularBusinessPartnerHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "SELECT "
        + regularBusinessPartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartnerLocation AS bpl " //
        + "join bpl.businessPartner AS bp " //
        + "join bp.priceList AS plist " //
        + "left outer join bp.aDUserList AS ulist "
        + "left outer join bp.businessPartnerLocationList AS bpsl " //
        + "WHERE $filtersCriteria AND " //
        + "bp.customer = true AND "
        + "(bpsl is null or bpsl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.active=true AND bpls.shipToAddress = true and bpls.businessPartner.id=bp.id and bpls.$readableSimpleClientCriteria AND "
        + "bpls.$naturalOrgCriteria)) AND " //
        + "bpl.$readableSimpleClientCriteria AND "
        + "bpl.$naturalOrgCriteria AND "
        + "(bpl.$incrementalUpdateCriteria"
        + operator
        + "bp.$incrementalUpdateCriteria) AND "
        + "bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.active=true and bpls.invoiceToAddress = true and bpls.businessPartner.id=bp.id and bpls.$readableSimpleClientCriteria AND "
        + "bpls.$naturalOrgCriteria) AND "
        + "(not exists (select 1 from ADUser usr where usr.businessPartner = bp)) "
        + "ORDER BY bp.name, bp.id";
    String hql2 = "SELECT"
        + regularBusinessPartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartnerLocation AS bpl " //
        + "join bpl.businessPartner AS bp " //
        + "join bp.priceList AS plist " //
        + "left outer join bp.aDUserList AS ulist "
        + "left outer join bp.businessPartnerLocationList AS bpsl " //
        + "WHERE $filtersCriteria AND " //
        + "bp.customer = true AND "
        + "(bpsl is null or bpsl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.active=true AND bpls.shipToAddress = true and bpls.businessPartner.id=bp.id and bpls.$readableSimpleClientCriteria AND "
        + "bpls.$naturalOrgCriteria)) AND " //
        + "bpl.$readableSimpleClientCriteria AND "
        + "bpl.$naturalOrgCriteria AND " //
        + "(bpl.$incrementalUpdateCriteria"
        + operator
        + "bp.$incrementalUpdateCriteria) AND "
        + "bpl.id in (select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.active=true and bpls.invoiceToAddress = true and bpls.businessPartner.id=bp.id and bpls.$readableSimpleClientCriteria AND "
        + "bpls.$naturalOrgCriteria) AND "
        + "(ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner=bp)) "
        + "ORDER BY bp.name, bp.id";
    return Arrays.asList(new String[] { hql, hql2 });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
