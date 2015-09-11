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

import java.util.List;

import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.access.WindowAccess;

public class WindowAccessManager extends AccessManager {

  public WindowAccessManager(RoleInheritance inheritance, List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList) {
    super(inheritance, inheritanceList, inheritanceInheritFromIdList);
  }

  @Override
  public void updateRoleAccess(InheritedAccessEnabled access, InheritedAccessEnabled inherited) {
    WindowAccess windowAccess = (WindowAccess) access;
    WindowAccess inheritedWindowAccess = (WindowAccess) inherited;
    // update the window access
    windowAccess.setActive(inheritedWindowAccess.isActive());
    windowAccess.setEditableField(inheritedWindowAccess.isEditableField());
    windowAccess.setInheritedFrom(inheritedWindowAccess.getRole());
    windowAccess.getRole().getADWindowAccessList();
  }

  @Override
  public String getSecuredElementIdentifier(InheritedAccessEnabled access) {
    // Return the window id
    WindowAccess windowAccess = (WindowAccess) access;
    String securedElementIndentifier = (String) DalUtil.getId(windowAccess.getWindow());
    return securedElementIndentifier;
  }

  @Override
  public List<? extends InheritedAccessEnabled> getAccessList(RoleInheritance roleInheritance) {
    return roleInheritance.getRole().getADWindowAccessList();
  }

  @Override
  public List<? extends InheritedAccessEnabled> getInheritedAccessList(
      RoleInheritance roleInheritance) {
    return roleInheritance.getInheritFrom().getADWindowAccessList();
  }
}
