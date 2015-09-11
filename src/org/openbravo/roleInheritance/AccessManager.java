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
package org.openbravo.roleInheritance;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;

public abstract class AccessManager {

  private RoleInheritance inheritance;
  private List<RoleInheritance> inheritanceList;
  private List<String> inheritanceInheritFromIdList;

  public AccessManager(RoleInheritance inheritance, List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList) {
    this.inheritance = inheritance;
    this.inheritanceList = inheritanceList;
    this.inheritanceInheritFromIdList = inheritanceInheritFromIdList;
  }

  public void calculateAccesses(boolean delete) {
    for (RoleInheritance roleInheritance : inheritanceList) {
      for (InheritedAccessEnabled inheritedAccess : getInheritedAccessList(roleInheritance)) {
        String inheritedAccessElementId = getSecuredElementIdentifier(inheritedAccess);
        String newInheritedFromId = (String) DalUtil.getId(roleInheritance.getInheritFrom());
        boolean found = false;
        for (InheritedAccessEnabled access : getAccessList(roleInheritance)) {
          String accessElementId = getSecuredElementIdentifier(access);
          String currentInheritedFromId = access.getInheritedFrom() != null ? (String) DalUtil
              .getId(access.getInheritedFrom()) : "";
          if (accessElementId.equals(inheritedAccessElementId)) {
            if (!StringUtils.isEmpty(currentInheritedFromId)
                && isPrecedent(currentInheritedFromId, newInheritedFromId)) {
              updateRoleAccess(access, inheritedAccess);
            }
            found = true;
            break;
          }
        }
        if (!found) {
          copyRoleAccess(inheritedAccess, roleInheritance);
        }
      }
    }
    if (delete) {
      // delete accesses not inherited anymore
      deleteRoleAccess(inheritance.getInheritFrom(), getAccessList(inheritance));
    }
    // OBDal.getInstance().getSession().clear();
  }

  private boolean isPrecedent(String role1, String role2) {
    if (inheritanceInheritFromIdList.indexOf(role1) == -1) {
      // Not found, need to override (this can happen on delete or on update)
      return true;
    }
    if (inheritanceInheritFromIdList.indexOf(role1) < inheritanceInheritFromIdList.indexOf(role2)) {
      return true;
    }
    return false;
  }

  private void copyRoleAccess(InheritedAccessEnabled inherited, RoleInheritance roleInheritance) {
    // copy the new access
    final InheritedAccessEnabled newAccess = (InheritedAccessEnabled) DalUtil.copy(
        (BaseOBObject) inherited, false);
    newAccess.setRole(roleInheritance.getRole());
    newAccess.setInheritedFrom(inherited.getRole());
    OBDal.getInstance().save(newAccess);
  }

  private void deleteRoleAccess(Role inheritFromToDelete,
      List<? extends InheritedAccessEnabled> accessList) {
    String inheritFromId = inheritFromToDelete.getId();
    inheritFromToDelete.getADWindowAccessList();
    List<InheritedAccessEnabled> iaeToDelete = new ArrayList<InheritedAccessEnabled>();
    for (InheritedAccessEnabled ih : accessList) {
      String inheritedFromId = (String) DalUtil.getId(ih.getInheritedFrom());
      if (inheritFromId.equals(inheritedFromId)) {
        iaeToDelete.add(ih);
      }
    }
    for (InheritedAccessEnabled ih : iaeToDelete) {
      accessList.remove(ih);
      OBDal.getInstance().remove(ih);
    }
  }

  protected abstract String getSecuredElementIdentifier(InheritedAccessEnabled access);

  protected abstract List<? extends InheritedAccessEnabled> getAccessList(
      RoleInheritance roleInheritance);

  protected abstract List<? extends InheritedAccessEnabled> getInheritedAccessList(
      RoleInheritance roleInheritance);

  protected abstract void updateRoleAccess(InheritedAccessEnabled access,
      InheritedAccessEnabled inherited);
}
