/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
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
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

@MasterDataModel("SalesRepresentative")
public class SalesRepresentative extends MasterDataProcessHQLQuery {
  public static final String salesRepresentativePropertyExtension = "OBPOS_SalesRepresentativeExtension";

  @Inject
  @Any
  @Qualifier(salesRepresentativePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Sales Representative Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    Map<String, Object> args = new HashMap<String, Object>();
    HQLPropertyList salesRepHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        args);
    propertiesList.add(salesRepHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    List<String> hqlQueries = new ArrayList<String>();
    HQLPropertyList regularSalesRepresentativeHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String operator = lastUpdated == null ? " AND " : " OR ";

    String hqlQuery = "SELECT " + regularSalesRepresentativeHQLProperties.getHqlSelect() //
        + "FROM ADUser user " + "WHERE $filtersCriteria AND "
        + "EXISTS (SELECT 1 FROM BusinessPartner bp WHERE user.businessPartner = bp AND bp.isSalesRepresentative = true AND (bp.$naturalOrgCriteria)) ";

    try {
      if (Preferences
          .getPreferenceValue("OBPOS_OrgAccessInSalesRepresentative", true,
              OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              OBContext.getOBContext().getRole(), null)
          .equals("Y")) {
        hqlQuery += "AND EXISTS (SELECT 1 FROM ADUserRoles userRoles, ADRoleOrganization orgAccess "
            + "WHERE userRoles.role = orgAccess.role AND  userRoles.userContact.id = user.id AND orgAccess.organization.id = :orgId AND orgAccess.active = true) ";
      }
    } catch (Exception e) {
    }

    hqlQuery += "AND ((user.$incrementalUpdateCriteria) " + operator
        + " (user.businessPartner.$incrementalUpdateCriteria)) AND (user.$naturalOrgCriteria) AND (user.$readableSimpleClientCriteria) order by user.name asc, user.id";

    hqlQueries.add(hqlQuery);

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> parameters = new HashMap<String, Object>();
    try {
      if (Preferences
          .getPreferenceValue("OBPOS_OrgAccessInSalesRepresentative", true,
              OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              OBContext.getOBContext().getRole(), null)
          .equals("Y")) {
        parameters.put("orgId", jsonsent.getString("organization"));
      }
    } catch (Exception e) {
    }
    return parameters;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }
}
