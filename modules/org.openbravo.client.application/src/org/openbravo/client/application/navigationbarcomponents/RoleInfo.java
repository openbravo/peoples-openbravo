/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.navigationbarcomponents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;

/**
 * This class provides the organizations and warehouses that can be accessed for a particular role.
 * It is is used to populate the 'Profile' widget with the information related to the roles assigned
 * to the current user.
 */
public class RoleInfo {
  private Role role;
  private List<Organization> roleOrganizations;
  private Map<String, List<Warehouse>> organizationWarehouses;

  public RoleInfo(Role role) {
    this.role = role;
  }

  public String getRoleId() {
    return role.getId();
  }

  public String getClient() {
    return role.getClient().getIdentifier();
  }

  public List<Organization> getOrganizations() {
    if (roleOrganizations != null) {
      return roleOrganizations;
    }
    roleOrganizations = new ArrayList<Organization>();
    final OBQuery<RoleOrganization> roleOrgs = OBDal.getInstance().createQuery(
        RoleOrganization.class, "role.id=:roleId and organization.active=true");
    roleOrgs.setFilterOnReadableClients(false);
    roleOrgs.setFilterOnReadableOrganization(false);
    roleOrgs.setNamedParameter("roleId", role.getId());
    for (RoleOrganization roleOrg : roleOrgs.list()) {
      if (!roleOrganizations.contains(roleOrg.getOrganization())) {
        roleOrganizations.add(roleOrg.getOrganization());
      }
    }
    DalUtil.sortByIdentifier(roleOrganizations);
    return roleOrganizations;
  }

  public Map<String, List<Warehouse>> getOrganizationWarehouses() {
    if (organizationWarehouses != null) {
      return organizationWarehouses;
    }
    organizationWarehouses = new LinkedHashMap<>();
    for (Organization org : getOrganizations()) {
      final OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(role.getClient().getId());
      final OBQuery<Warehouse> warehouses = OBDal
          .getInstance()
          .createQuery(Warehouse.class,
              "organization.id in (:orgList) and client.id=:clientId and organization.active=true order by name");
      warehouses.setNamedParameter("orgList", osp.getNaturalTree(org.getId()));
      warehouses.setNamedParameter("clientId", role.getClient().getId());
      warehouses.setFilterOnReadableClients(false);
      warehouses.setFilterOnReadableOrganization(false);
      organizationWarehouses.put(org.getId(), warehouses.list());
    }
    return organizationWarehouses;
  }
}
