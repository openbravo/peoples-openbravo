/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
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
    HQLPropertyList regularBusinessPartnerHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String hql = "SELECT "
        + regularBusinessPartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartner AS bp " //
        + "join bp.priceList AS plist " //
        + "left outer join bp.aDUserList AS ulist "
        + "WHERE $filtersCriteria AND " //
        + "bp.customer = true AND " + "(bp.$incrementalUpdateCriteria) AND "
        + "bp.$readableSimpleClientCriteria AND " + "bp.$naturalOrgCriteria AND "
        + "(not exists (select 1 from ADUser usr where usr.businessPartner = bp)) "
        + "ORDER BY bp.name, bp.id";
    String hql2 = "SELECT"
        + regularBusinessPartnerHQLProperties.getHqlSelect() //
        + "FROM BusinessPartner AS bp " //
        + "join bp.priceList AS plist " //
        + "left outer join bp.aDUserList AS ulist "
        + "WHERE $filtersCriteria AND " //
        + "bp.customer = true AND "
        + "bp.$readableSimpleClientCriteria AND "
        + "bp.$naturalOrgCriteria AND "
        + "(bp.$incrementalUpdateCriteria) AND "
        + "(ulist.id in (select max(ulist2.id) from ADUser as ulist2 where ulist2.businessPartner=bp)) "
        + "ORDER BY bp.name, bp.id";
    return Arrays.asList(new String[] { hql, hql2 });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
