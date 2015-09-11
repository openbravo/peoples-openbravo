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

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.roleInheritance.WindowAccessManager;
import org.openbravo.service.db.DalConnectionProvider;

public class RoleInheritanceEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      RoleInheritance.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final RoleInheritance inheritance = (RoleInheritance) event.getTargetInstance();
    String inheritFromId = (String) DalUtil.getId(inheritance.getInheritFrom());
    if (existCycles(inheritance.getRole(), inheritFromId)) {
      showErrorMessage("CyclesInRoleInheritance");
    } else {
      doSaveAccesses(inheritance);
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    showErrorMessage("RoleInheritanceNotEdit");
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final RoleInheritance inheritance = (RoleInheritance) event.getTargetInstance();
    boolean notDeletingParent = OBDal.getInstance().exists(Role.ENTITY_NAME,
        (String) DalUtil.getId(inheritance.getRole()));
    if (notDeletingParent) {
      // checkings
      doDeleteAccesses(inheritance);
    }
  }

  private void doSaveAccesses(RoleInheritance inheritance) {
    // Window Access
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(inheritance, false);
    List<String> inheritanceRoleIdList = getRoleInheritancesRoleIdList(inheritanceList);
    List<RoleInheritance> newInheritanceList = new ArrayList<RoleInheritance>();
    newInheritanceList.add(inheritance);
    WindowAccessManager wam = new WindowAccessManager(inheritance, newInheritanceList,
        inheritanceRoleIdList);
    wam.calculateAccesses(false);
  }

  private void doDeleteAccesses(RoleInheritance inheritance) {
    // Window Access
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(inheritance, true);
    List<String> inheritanceRoleIdList = getRoleInheritancesRoleIdList(inheritanceList);
    WindowAccessManager wam = new WindowAccessManager(inheritance, inheritanceList,
        inheritanceRoleIdList);
    wam.calculateAccesses(true);
  }

  private List<RoleInheritance> getRoleInheritancesList(RoleInheritance inheritance,
      boolean deleting) {
    final ArrayList<RoleInheritance> roleInheritancesList = new ArrayList<RoleInheritance>();
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, inheritance.getRole()));
    obCriteria.add(Restrictions.ne(RoleInheritance.PROPERTY_ID, inheritance.getId()));
    obCriteria.addOrderBy(RoleInheritance.PROPERTY_SEQUENCENUMBER, true);
    boolean added = false;
    for (RoleInheritance rh : obCriteria.list()) {
      if (rh.getInheritFrom().getId().equals(inheritance.getInheritFrom().getId())) {
        showErrorMessage("RoleInheritanceInheritFromDuplicated");
      }
      if (!deleting && !added
          && rh.getSequenceNumber().longValue() > inheritance.getSequenceNumber().longValue()) {
        roleInheritancesList.add(inheritance);
        added = true;
      }
      roleInheritancesList.add(rh);
    }
    if (!deleting && !added) {
      roleInheritancesList.add(inheritance);
    }
    return roleInheritancesList;
  }

  private List<String> getRoleInheritancesRoleIdList(List<RoleInheritance> roleInheritanceList) {
    final ArrayList<String> roleIdsList = new ArrayList<String>();
    for (RoleInheritance roleInheritance : roleInheritanceList) {
      roleIdsList.add((String) DalUtil.getId(roleInheritance.getInheritFrom()));
    }
    return roleIdsList;
  }

  private boolean existCycles(Role role, String roleIdToFind) {
    boolean result = false;
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      if (roleIdToFind.equals(ri.getRole().getId())) {
        return true;
      }
      result = existCycles(ri.getRole(), roleIdToFind);
    }
    return result;
  }

  private void showErrorMessage(String message) {
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    ConnectionProvider conn = new DalConnectionProvider(false);
    throw new OBException(Utility.messageBD(conn, message, language));
  }
}
