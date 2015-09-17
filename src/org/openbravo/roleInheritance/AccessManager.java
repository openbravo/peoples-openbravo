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
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.client.application.ViewRoleAccess;
import org.openbravo.client.myob.WidgetClassAccess;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.FieldAccess;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.TabAccess;
import org.openbravo.model.ad.access.TableAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.alert.AlertRecipient;

public class AccessManager {

  private static final Logger log4j = Logger.getLogger(AccessManager.class);
  private RoleInheritance inheritance;
  private List<String> inheritanceInheritFromIdList;

  public AccessManager(RoleInheritance inheritance, List<String> inheritanceInheritFromIdList) {
    this.inheritance = inheritance;
    this.inheritanceInheritFromIdList = inheritanceInheritFromIdList;
  }

  public void calculateAccesses(List<RoleInheritance> inheritanceList, AccessType accessType,
      boolean delete) {
    for (RoleInheritance roleInheritance : inheritanceList) {
      for (InheritedAccessEnabled inheritedAccess : accessType.getAccessList(roleInheritance
          .getInheritFrom())) {
        handleAccess(roleInheritance, inheritedAccess, accessType);
      }
    }
    if (delete) {
      // delete accesses not inherited anymore
      accessType.deleteRoleAccess(inheritance.getInheritFrom(),
          accessType.getAccessList(inheritance.getRole()));
    }
    // OBDal.getInstance().getSession().clear();
  }

  public void handleAccess(RoleInheritance roleInheritance, InheritedAccessEnabled inheritedAccess,
      AccessType accessType) {
    String inheritedAccessElementId = accessType.getSecuredElementIdentifier(inheritedAccess);
    String newInheritedFromId = (String) DalUtil.getId(roleInheritance.getInheritFrom());
    boolean found = false;
    for (InheritedAccessEnabled access : accessType.getAccessList(roleInheritance.getRole())) {
      String accessElementId = accessType.getSecuredElementIdentifier(access);
      String currentInheritedFromId = access.getInheritedFrom() != null ? (String) DalUtil
          .getId(access.getInheritedFrom()) : "";
      if (accessElementId.equals(inheritedAccessElementId)) {
        if (!StringUtils.isEmpty(currentInheritedFromId)
            && isPrecedent(currentInheritedFromId, newInheritedFromId)) {
          accessType.updateRoleAccess(access, inheritedAccess);
        }
        found = true;
        break;
      }
    }
    if (!found) {
      accessType.copyRoleAccess(inheritedAccess, roleInheritance);
    }
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

  public enum AccessType {
    /**
     * Organization Access
     */
    ORG_ACCESS("org.openbravo.model.ad.access.RoleOrganization", "getOrganization"),
    /**
     * Window Access
     */
    WINDOW_ACCESS("org.openbravo.model.ad.access.WindowAccess", "getWindow"),
    /**
     * Tab Access
     */
    TAB_ACCESS("org.openbravo.model.ad.access.TabAccess", "getTab"),
    /**
     * Field Access
     */
    FIELD_ACCESS("org.openbravo.model.ad.access.FieldAccess", "getField"),
    /**
     * Process Access
     */
    PROCESS_ACCESS("org.openbravo.model.ad.access.ProcessAccess", "getProcess"),
    /**
     * Form Access
     */
    FORM_ACCESS("org.openbravo.model.ad.access.FormAccess", "getSpecialForm"),
    /**
     * Widget Class Access
     */
    WIDGET_ACCESS("org.openbravo.client.myob.WidgetClassAccess", "getWidgetClass"),
    /**
     * View Implementation Access
     */
    VIEW_ACCESS("org.openbravo.client.application.ViewRoleAccess", "getViewImplementation"),
    /**
     * Process Definition Access
     */
    PROCESS_DEF_ACCESS("org.openbravo.client.application.ProcessAccess", "getObuiappProcess"),
    /**
     * Table Access
     */
    TABLE_ACCESS("org.openbravo.model.ad.access.TableAccess", "getTable"),
    /**
     * Alert Recipient Access
     */
    ALERT_RECIPIENT("org.openbravo.model.ad.alert.AlertRecipient", "getAlertRule");

    private final String className;
    private final String securedElement;

    AccessType(String className, String securedElement) {
      this.className = className;
      this.securedElement = securedElement;
    }

    public String getClassName() {
      return this.className;
    }

    public String getSecuredElement() {
      return this.securedElement;
    }

    public String getSecuredElementIdentifier(InheritedAccessEnabled access) {
      try {
        Class<?> myClass = Class.forName(className);
        BaseOBObject bob = (BaseOBObject) myClass.getMethod(securedElement).invoke(access);
        String securedElementIndentifier = (String) DalUtil.getId(bob);
        return securedElementIndentifier;
      } catch (Exception ex) {
        log4j.error("Error getting secured element identifier", ex);
        throw new OBException("Error getting secured element identifier");
      }
    }

    public void setParent(InheritedAccessEnabled newAccess, InheritedAccessEnabled parentAccess,
        Role role) {
      try {
        // TabAccess and Field Access does not have role property as parent
        if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
          setParentWindow(newAccess, parentAccess, role);
        } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
          setParentTab(newAccess, parentAccess, role);
        } else {
          setParentRole(newAccess, role);
        }
      } catch (Exception ex) {
        log4j.error("Error setting parent ", ex);
        throw new OBException("Error setting parent");
      }
    }

    private void setParentRole(InheritedAccessEnabled access, Role role) {
      try {
        Class<?> myClass = Class.forName(className);
        myClass.getMethod("setRole", new Class[] { Role.class }).invoke(access,
            new Object[] { role });
      } catch (Exception ex) {
        log4j.error("Error setting parent role ", ex);
        throw new OBException("Error setting parent role");
      }
    }

    private void setParentWindow(InheritedAccessEnabled newAccess,
        InheritedAccessEnabled parentAccess, Role role) {
      TabAccess parentTabAccess = (TabAccess) parentAccess;
      TabAccess newTabAccess = (TabAccess) newAccess;
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as wa ");
      whereClause.append(" where wa.role.id = :roleId");
      whereClause.append(" and wa.window.id = :windowId");
      final OBQuery<WindowAccess> query = OBDal.getInstance().createQuery(WindowAccess.class,
          whereClause.toString());
      query.setNamedParameter("roleId", role.getId());
      query.setNamedParameter("windowId", parentTabAccess.getWindowAccess().getWindow().getId());
      query.setMaxResult(1);
      WindowAccess parent = (WindowAccess) query.uniqueResult();
      newTabAccess.setWindowAccess(parent);
    }

    private void setParentTab(InheritedAccessEnabled newAccess,
        InheritedAccessEnabled parentAccess, Role role) {
      FieldAccess parentFieldAccess = (FieldAccess) parentAccess;
      FieldAccess newFieldAccess = (FieldAccess) newAccess;
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as ta ");
      whereClause.append(" where ta.windowAccess.role.id = :roleId");
      whereClause.append(" and ta.tab.id = :tabId");
      final OBQuery<TabAccess> query = OBDal.getInstance().createQuery(TabAccess.class,
          whereClause.toString());
      query.setNamedParameter("roleId", role.getId());
      query.setNamedParameter("tabId", parentFieldAccess.getTabAccess().getTab().getId());
      query.setMaxResult(1);
      TabAccess parent = (TabAccess) query.uniqueResult();
      newFieldAccess.setTabAccess(parent);
    }

    public Role getRole(InheritedAccessEnabled access) {
      // TabAccess and Field Access does not have role property as parent
      if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
        TabAccess tabAccess = (TabAccess) access;
        return tabAccess.getWindowAccess().getRole();
      } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
        FieldAccess fieldAccess = (FieldAccess) access;
        return fieldAccess.getTabAccess().getWindowAccess().getRole();
      } else {
        return getParentRole(access);
      }
    }

    private Role getParentRole(InheritedAccessEnabled access) {
      try {
        Class<?> myClass = Class.forName(className);
        Role role = (Role) myClass.getMethod("getRole").invoke(access);
        return role;
      } catch (Exception ex) {
        log4j.error("Error getting role ", ex);
        throw new OBException("Error getting role");
      }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseOBObject> List<? extends InheritedAccessEnabled> getAccessList(Role role) {
      try {
        String roleProperty;
        // TabAccess and Field Access does not have role property as parent
        if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
          roleProperty = "windowAccess.role.id";
        } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
          roleProperty = "tabAccess.windowAccess.role.id";
        } else {
          roleProperty = "role.id";
        }
        Class<T> clazz = (Class<T>) Class.forName(className);
        final StringBuilder whereClause = new StringBuilder();
        whereClause.append(" as p ");
        whereClause.append(" where p.").append(roleProperty).append(" = :roleId");
        final OBQuery<T> query = OBDal.getInstance().createQuery(clazz, whereClause.toString());
        query.setNamedParameter("roleId", role.getId());
        return (List<? extends InheritedAccessEnabled>) query.list();
      } catch (Exception ex) {
        log4j.error("Error getting access list of class " + className, ex);
        throw new OBException("Error getting access list of class " + className);
      }
    }

    public void copyRoleAccess(InheritedAccessEnabled parentAccess, RoleInheritance roleInheritance) {
      // copy the new access
      final InheritedAccessEnabled newAccess = (InheritedAccessEnabled) DalUtil.copy(
          (BaseOBObject) parentAccess, false);
      setParent(newAccess, parentAccess, roleInheritance.getRole());
      newAccess.setInheritedFrom(getRole(parentAccess));
      OBDal.getInstance().save(newAccess);
    }

    public void deleteRoleAccess(Role inheritFromToDelete,
        List<? extends InheritedAccessEnabled> roleAccessList) {
      String inheritFromId = inheritFromToDelete.getId();
      inheritFromToDelete.getADWindowAccessList();
      List<InheritedAccessEnabled> iaeToDelete = new ArrayList<InheritedAccessEnabled>();
      for (InheritedAccessEnabled ih : roleAccessList) {
        String inheritedFromId = (String) DalUtil.getId(ih.getInheritedFrom());
        if (inheritFromId.equals(inheritedFromId)) {
          iaeToDelete.add(ih);
        }
      }
      for (InheritedAccessEnabled ih : iaeToDelete) {
        roleAccessList.remove(ih);
        OBDal.getInstance().remove(ih);
      }
    }

    public void updateRoleAccess(InheritedAccessEnabled access, InheritedAccessEnabled inherited) {
      access.setInheritedFrom(getRole(inherited));
      if (className.equals("org.openbravo.model.ad.access.WindowAccess")) {
        WindowAccess windowAccess = (WindowAccess) access;
        WindowAccess inheritedWindowAccess = (WindowAccess) inherited;
        windowAccess.setActive(inheritedWindowAccess.isActive());
        windowAccess.setEditableField(inheritedWindowAccess.isEditableField());
      }
    }

    public static AccessType getAccessType(String entityName) throws OBException {
      if (RoleOrganization.ENTITY_NAME.equals(entityName)) {
        return AccessType.ORG_ACCESS;
      } else if (WindowAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.WINDOW_ACCESS;
      } else if (TabAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.TAB_ACCESS;
      } else if (FieldAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.FIELD_ACCESS;
      } else if (org.openbravo.model.ad.access.ProcessAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.PROCESS_ACCESS;
      } else if (FormAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.FORM_ACCESS;
      } else if (WidgetClassAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.WIDGET_ACCESS;
      } else if (ViewRoleAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.VIEW_ACCESS;
      } else if (org.openbravo.client.application.ProcessAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.PROCESS_DEF_ACCESS;
      } else if (TableAccess.ENTITY_NAME.equals(entityName)) {
        return AccessType.TABLE_ACCESS;
      } else if (AlertRecipient.ENTITY_NAME.equals(entityName)) {
        return AccessType.ALERT_RECIPIENT;
      } else {
        throw new OBException(OBMessageUtils.getI18NMessage("UnsupportedAccessType",
            new String[] { entityName }));
      }
    }
  }
}
