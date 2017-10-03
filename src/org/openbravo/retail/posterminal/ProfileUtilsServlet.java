/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.MobileDefaults;
import org.openbravo.mobile.core.login.ProfileUtils;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;

public class ProfileUtilsServlet extends ProfileUtils {

  @Override
  protected JSONArray getWarehouses(String clientId, List<Organization> orgs) throws JSONException {
    // Web POS filters those warehouses which are defined in the organization window as store
    // warehouses
    List<JSONObject> orgWarehouseArray = new ArrayList<JSONObject>();
    final OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(clientId);
    for (Organization org : orgs) {
      JSONObject orgWarehouse = new JSONObject();
      orgWarehouse.put("orgId", org.getId());
      StringBuffer hqlQuery = new StringBuffer();
      hqlQuery.append("organization.id in (:orgList) AND ");
      hqlQuery.append("client.id=:clientId AND ");
      hqlQuery.append("id in (");
      hqlQuery.append("  select owar.warehouse.id from OrganizationWarehouse owar ");
      hqlQuery.append("  where owar.organization.id = :orgId");
      hqlQuery.append(") AND ");
      hqlQuery.append("organization.active=true ");
      hqlQuery.append("order by name");
      final OBQuery<Warehouse> warehouses = OBDal.getInstance().createQuery(Warehouse.class,
          hqlQuery.toString());
      warehouses.setNamedParameter("orgList", osp.getNaturalTree(org.getId()));
      warehouses.setNamedParameter("orgId", org.getId());
      warehouses.setNamedParameter("clientId", clientId);
      warehouses.setFilterOnReadableClients(false);
      warehouses.setFilterOnReadableOrganization(false);
      orgWarehouse.put("warehouseMap", createValueMapObject(warehouses.list(), null, null));
      orgWarehouseArray.add(orgWarehouse);
    }
    return new JSONArray(orgWarehouseArray);
  }

  @Override
  protected List<Role> getRoles(MobileDefaults defaults) {
    // Web pos filters by roles of the current organization
    String formId = defaults.getFormId();
    String clientId = OBContext.getOBContext().getCurrentClient().getId();
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    String whereClause = "as r where userContact.id=:user and role.active=true ";
    whereClause += "and exists (select 1 from ADFormAccess a "//
        + " where a.active = true" //
        + " and a.role.id = r.role.id "//
        + " and a.role.client.id = :clientId "//
        + " and a.specialForm.id = :formId) "//
        + " and exists (select 1 from ADRoleOrganization o "//
        + " where o.active = true and o.role.id = r.role.id "//
        + " and o.organization.id = :orgId)"//
        + " order by role.name ASC";

    final OBQuery<UserRoles> rolesQuery = OBDal.getInstance().createQuery(UserRoles.class,
        whereClause);
    rolesQuery.setFilterOnReadableClients(false);
    rolesQuery.setFilterOnReadableOrganization(false);
    rolesQuery.setNamedParameter("user", OBContext.getOBContext().getUser().getId());
    rolesQuery.setNamedParameter("formId", formId);
    rolesQuery.setNamedParameter("clientId", clientId);
    rolesQuery.setNamedParameter("orgId", orgId);

    final List<Role> result = new ArrayList<Role>();
    for (UserRoles userRole : rolesQuery.list()) {
      if (!result.contains(userRole.getRole())) {
        result.add(userRole.getRole());
      }
    }
    return result;
  }

}