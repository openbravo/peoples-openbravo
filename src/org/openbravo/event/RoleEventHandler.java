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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.common.enterprise.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Role.ENTITY_NAME) };

  protected Logger logger = LoggerFactory.getLogger(RoleEventHandler.class);

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onNew(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity roleEntity = ModelProvider.getInstance().getEntity(Role.class);
    final Property roleProperty = roleEntity.getProperty(Role.PROPERTY_ADROLEORGANIZATIONLIST);
    final Role role = (Role) event.getTargetInstance();

    // Create org access for new automatic role
    try {
      if (role.isManual().booleanValue() == false) {
        List<RoleOrganization> roleOrganizationList = getRoleOrganizationList(role);
        @SuppressWarnings("unchecked")
        final List<Object> roleOrganizations = (List<Object>) event.getCurrentState(roleProperty);
        roleOrganizations.addAll(roleOrganizationList);
      }
    } catch (Exception e) {
      logger
          .error("Error in RoleEventHandler while inserting Org Access to role " + role.getName());
    }
  }

  // Get org access list
  private List<RoleOrganization> getRoleOrganizationList(Role role) throws Exception {

    List<RoleOrganization> roleOrganizationList = new ArrayList<RoleOrganization>();

    // System level
    if (StringUtils.equals(role.getUserLevel(), "S")) {
      roleOrganizationList.add(getRoleOrganization(role,
          OBDal.getInstance().get(Organization.class, "0"), false));
    }

    // Client or Client/Organization level
    else if (StringUtils.equals(role.getUserLevel(), " C")
        || StringUtils.equals(role.getUserLevel(), " CO")) {
      roleOrganizationList.add(getRoleOrganization(role,
          OBDal.getInstance().get(Organization.class, "0"), false));
      OBCriteria<Organization> criteria = OBDal.getInstance().createCriteria(Organization.class);
      criteria.add(Restrictions.eq(Organization.PROPERTY_CLIENT, role.getClient()));
      criteria.add(Restrictions.ne(Organization.PROPERTY_ID, "0"));
      ScrollableResults scroll = criteria.scroll(ScrollMode.FORWARD_ONLY);
      try {
        int i = 0;
        while (scroll.next()) {
          final Organization organization = (Organization) scroll.get()[0];
          roleOrganizationList.add(getRoleOrganization(role, organization, true));
          i++;
          if (i % 100 == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getSession().clear();
          }
        }
      } finally {
        scroll.close();
      }
    }

    // Organization level
    else if (StringUtils.equals(role.getUserLevel(), "  O")) {
      OBCriteria<Organization> criteria = OBDal.getInstance().createCriteria(Organization.class);
      criteria.add(Restrictions.eq(Organization.PROPERTY_CLIENT, role.getClient()));
      ScrollableResults scroll = criteria.scroll(ScrollMode.FORWARD_ONLY);
      try {
        int i = 0;
        while (scroll.next()) {
          final Organization organization = (Organization) scroll.get()[0];
          roleOrganizationList.add(getRoleOrganization(role, organization, true));
          i++;
          if (i % 100 == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getSession().clear();
          }
        }
      } finally {
        scroll.close();
      }
    }

    return roleOrganizationList;
  }

  // Get org access
  private RoleOrganization getRoleOrganization(Role role, Organization orgProvided,
      boolean isOrgAdmin) throws Exception {
    OBContext.setAdminMode();
    try {
      final RoleOrganization newRoleOrganization = OBProvider.getInstance().get(
          RoleOrganization.class);
      newRoleOrganization.setClient(role.getClient());
      newRoleOrganization.setOrganization(orgProvided);
      newRoleOrganization.setRole(role);
      newRoleOrganization.setOrgAdmin(isOrgAdmin);
      return newRoleOrganization;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}