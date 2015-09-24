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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.client.application.ViewRoleAccess;
import org.openbravo.client.myob.WidgetClassAccess;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.FieldAccess;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.TabAccess;
import org.openbravo.model.ad.access.TableAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.domain.Preference;

public class RoleInheritanceManager {

  private static final Logger log4j = Logger.getLogger(RoleInheritanceManager.class);
  private static final Set<String> propertyBlackList = new HashSet<String>(Arrays.asList(
      "OBUIAPP_RecentDocumentsList", "OBUIAPP_RecentViewList", "OBUIAPP_GridConfiguration",
      "OBUIAPP_DefaultSavedView", "UINAVBA_RecentLaunchList"));

  private final String className;
  private final String securedElement;
  private final List<String> skippedProperties;

  /**
   * Basic constructor of the class.
   * 
   * @param accessType
   *          AccessType type which define the inheritable access that will be handled by the
   *          manager
   */
  public RoleInheritanceManager(AccessType accessType) {
    this.className = accessType.getClassName();
    this.securedElement = accessType.getSecuredElement();
    this.skippedProperties = new ArrayList<String>();
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(className)) {
      skippedProperties.add("role");
    } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
      skippedProperties.add("visibleAtRole");
    }
  }

  /**
   * Returns the name of the inheritable class.
   * 
   * @return A String with the class name
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Returns the secured object.
   * 
   * @return a String with the name of the method to retrieve the secured element
   */
  public String getSecuredElement() {
    return this.securedElement;
  }

  /**
   * Returns the id of the secured object by the given inheritable class.
   * 
   * @param access
   *          An object of an inheritable class,i.e., a class that implements
   *          InheritedAccessEnabled.
   * 
   * @return A String with the id of the secured object
   */
  public String getSecuredElementIdentifier(InheritedAccessEnabled access) {
    try {
      Class<?> myClass = Class.forName(className);
      if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
        return (String) myClass.getMethod(securedElement).invoke(access);
      }
      BaseOBObject bob = (BaseOBObject) myClass.getMethod(securedElement).invoke(access);
      String securedElementIndentifier = (String) DalUtil.getId(bob);
      return securedElementIndentifier;
    } catch (Exception ex) {
      log4j.error("Error getting secured element identifier", ex);
      throw new OBException("Error getting secured element identifier");
    }
  }

  /**
   * Sets the parent for an inheritable access object.
   * 
   * @param newAccess
   *          Access whose parent object will be set
   * @param parentAccess
   *          Access that is used in some cases to find the correct parent
   * @param role
   *          Parent role to set directly when applies
   */
  private void setParent(InheritedAccessEnabled newAccess, InheritedAccessEnabled parentAccess,
      Role role) {
    try {
      // TabAccess and Field Access does not have role property as parent
      if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
        setParentWindow((TabAccess) newAccess, (TabAccess) parentAccess, role);
      } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
        setParentTab((FieldAccess) newAccess, (FieldAccess) parentAccess, role);
      } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
        ((Preference) (newAccess)).setVisibleAtRole(role);
      } else {
        setParentRole(newAccess, role);
      }
    } catch (Exception ex) {
      log4j.error("Error setting parent ", ex);
      throw new OBException("Error setting parent");
    }
  }

  /**
   * Sets the parent role for an inheritable access object.
   * 
   * @param access
   *          Access whose parent role will be set
   * @param role
   *          Parent role
   */
  private void setParentRole(InheritedAccessEnabled access, Role role) {
    try {
      Class<?> myClass = Class.forName(className);
      myClass.getMethod("setRole", new Class[] { Role.class })
          .invoke(access, new Object[] { role });
    } catch (Exception ex) {
      log4j.error("Error setting parent role ", ex);
      throw new OBException("Error setting parent role");
    }
  }

  /**
   * Sets the parent window for a TabAccess.
   * 
   * @param newTabAccess
   *          TabAccess whose parent window will be set
   * @param parentTabAccess
   *          TabAccess used to retrieve the parent window
   * @param role
   *          Parent role
   */
  private void setParentWindow(TabAccess newTabAccess, TabAccess parentTabAccess, Role role) {
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
    if (parent != null) {
      newTabAccess.setWindowAccess(parent);
    }
  }

  /**
   * Sets the parent tab for a FieldAccess.
   * 
   * @param newFieldAccess
   *          FieldAccess whose parent tab will be set
   * @param parentFieldAccess
   *          FieldAccess used to retrieve the parent tab
   * @param role
   *          Parent role
   */
  private void setParentTab(FieldAccess newFieldAccess, FieldAccess parentFieldAccess, Role role) {
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
    if (parent != null) {
      newFieldAccess.setTabAccess(parent);
    }
  }

  /**
   * Returns the role which the access given as parameter is assigned to.
   * 
   * @param access
   *          An inheritable access
   * 
   * @return the Role owner of the access
   */
  public Role getRole(InheritedAccessEnabled access) {
    // TabAccess and Field Access does not have role property as parent
    if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
      TabAccess tabAccess = (TabAccess) access;
      return tabAccess.getWindowAccess().getRole();
    } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
      FieldAccess fieldAccess = (FieldAccess) access;
      return fieldAccess.getTabAccess().getWindowAccess().getRole();
    } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
      Preference preference = (Preference) access;
      return preference.getVisibleAtRole();
    } else {
      return getParentRole(access);
    }
  }

  /**
   * Returns the role which the access given as parameter is assigned to. This method is used for
   * those inheritable accesses which Role is their parent entity.
   * 
   * @param access
   *          An inheritable access
   * 
   * @return the parent Role of the access
   */
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

  /**
   * Returns the list of accesses of a particular type for the Role given as parameter.
   * 
   * @param role
   *          The role whose list of accesses of a particular type will be retrieved
   * 
   * @return a list of accesses
   */
  @SuppressWarnings("unchecked")
  private <T extends BaseOBObject> List<? extends InheritedAccessEnabled> getAccessList(Role role) {
    try {
      String roleProperty = getRoleProperty(className);
      Class<T> clazz = (Class<T>) Class.forName(className);
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as p ");
      whereClause.append(" where p.").append(roleProperty).append(" = :roleId");
      addEntityWhereClause(whereClause, className);
      final OBQuery<T> query = OBDal.getInstance().createQuery(clazz, whereClause.toString());
      query.setNamedParameter("roleId", role.getId());
      doEntityParameterReplacement(query, className);
      query.setFilterOnActive(false);
      return (List<? extends InheritedAccessEnabled>) query.list();
    } catch (Exception ex) {
      log4j.error("Error getting access list of class " + className, ex);
      throw new OBException("Error getting access list of class " + className);
    }
  }

  /**
   * Returns the role property retrieved from the class name.
   * 
   * @return the role property that can be retrieved according to the input class name.
   */
  private String getRoleProperty(String clazzName) {
    // TabAccess and Field Access does not have role property as parent
    if ("org.openbravo.model.ad.access.TabAccess".equals(clazzName)) {
      return "windowAccess.role.id";
    } else if ("org.openbravo.model.ad.access.FieldAccess".equals(clazzName)) {
      return "tabAccess.windowAccess.role.id";
    } else if ("org.openbravo.model.ad.domain.Preference".equals(clazzName)) {
      return "visibleAtRole.id";
    } else {
      return "role.id";
    }
  }

  /**
   * Includes in the where clause some filtering needed for same cases.
   * 
   * @param whereClause
   *          The where clause where the particular filtering will be included
   * @param clazzName
   *          The class name used to identify which filtering must be returned
   */
  private void addEntityWhereClause(StringBuilder whereClause, String clazzName) {
    if ("org.openbravo.model.ad.domain.Preference".equals(clazzName)) {
      // Inheritable preferences are those that only define the visibility at role level
      whereClause.append(" and p.visibleAtClient = null and p.visibleAtOrganization = null"
          + " and p.userContact = null and p.window = null");
      whereClause.append(" and p.property not in (:blackList)");
    } else if ("org.openbravo.model.ad.alert.AlertRecipient".equals(clazzName)) {
      whereClause.append(" and p.userContact is null");
    }
  }

  /**
   * Performs the needed parameter substitution according to the input class name.
   * 
   * @param query
   *          The query where to perform the parameter substitution
   * @param clazzName
   *          The class name used to identify if the parameter substitution is needed
   */
  private <T extends BaseOBObject> void doEntityParameterReplacement(OBQuery<T> query,
      String clazzName) {
    if ("org.openbravo.model.ad.domain.Preference".equals(clazzName)) {
      query.setNamedParameter("blackList", propertyBlackList);
    }
  }

  /**
   * Creates a new access by copying from the one introduced as parameter. In addition, it sets the
   * Inherit From field with the corresponding role.
   * 
   * @param parentAccess
   *          The access to be copied
   * @param role
   *          The role used to set the parent of the new access
   */
  private void copyRoleAccess(InheritedAccessEnabled parentAccess, Role role) {
    // copy the new access
    final InheritedAccessEnabled newAccess = (InheritedAccessEnabled) DalUtil.copy(
        (BaseOBObject) parentAccess, false);
    setParent(newAccess, parentAccess, role);
    newAccess.setInheritedFrom(getRole(parentAccess));
    OBDal.getInstance().save(newAccess);
  }

  /**
   * Deletes all accesses which are inheriting from a particular role.
   * 
   * @param inheritFromToDelete
   *          The role whose inherited accesses will be removed from the list
   * @param roleAccessList
   *          The list of accesses to remove from
   */
  private void deleteRoleAccess(Role inheritFromToDelete,
      List<? extends InheritedAccessEnabled> roleAccessList) {
    String inheritFromId = (String) DalUtil.getId(inheritFromToDelete);
    List<InheritedAccessEnabled> iaeToDelete = new ArrayList<InheritedAccessEnabled>();
    for (InheritedAccessEnabled ih : roleAccessList) {
      String inheritedFromId = ih.getInheritedFrom() != null ? (String) DalUtil.getId(ih
          .getInheritedFrom()) : "";
      if (!StringUtils.isEmpty(inheritedFromId) && inheritFromId.equals(inheritedFromId)) {
        iaeToDelete.add(ih);
      }
    }
    for (InheritedAccessEnabled iae : iaeToDelete) {
      iae.setInheritedFrom(null);
      roleAccessList.remove(iae);
      OBDal.getInstance().remove(iae);
    }
  }

  /**
   * Updates the fields of an access with the values of the access introduced as parameter. In
   * addition, it sets the Inherit From field with the corresponding role.
   * 
   * @param access
   *          The access to be updated
   * @param inherited
   *          The access with the values to update
   */
  private void updateRoleAccess(InheritedAccessEnabled access, InheritedAccessEnabled inherited) {
    final InheritedAccessEnabled updatedAccess = (InheritedAccessEnabled) DalUtil.copyToTarget(
        (BaseOBObject) inherited, (BaseOBObject) access, false, skippedProperties);
    // update the inherit from field, to indicate from which role we are inheriting now
    updatedAccess.setInheritedFrom(getRole(inherited));
  }

  /**
   * Applies all type of accesses based on the inheritance passed as parameter
   * 
   * @param inheritance
   *          The inheritance used to calculate the possible new accesses
   */
  public static void applyNewInheritance(RoleInheritance inheritance) {
    List<RoleInheritance> inheritanceList = getUpdatedRoleInheritancesList(inheritance, false);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    List<RoleInheritance> newInheritanceList = new ArrayList<RoleInheritance>();
    newInheritanceList.add(inheritance);
    for (AccessType accessType : AccessType.values()) {
      RoleInheritanceManager manager = new RoleInheritanceManager(accessType);
      manager.calculateAccesses(newInheritanceList, inheritanceRoleIdList);
    }
  }

  /**
   * Calculates all type of accesses after the removal of the inheritance passed as parameter
   * 
   * @param inheritance
   *          The inheritance being removed
   */
  public static void applyRemoveInheritance(RoleInheritance inheritance) {
    List<RoleInheritance> inheritanceList = getUpdatedRoleInheritancesList(inheritance, true);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    for (AccessType accessType : AccessType.values()) {
      RoleInheritanceManager manager = new RoleInheritanceManager(accessType);
      manager.calculateAccesses(inheritanceList, inheritanceRoleIdList, inheritance);
    }
  }

  /**
   * Recalculates all accesses for those roles using as template the role passed as parameter
   * 
   * @param template
   *          The template role used by the roles whose accesses will be recalculated
   */
  public static void recalculateAllAccessesFromTemplate(Role template) {
    for (RoleInheritance ri : template.getADRoleInheritanceInheritFromList()) {
      recalculateAllAccessesForRole(ri.getRole());
    }
  }

  /**
   * Recalculates all accesses for a given role
   * 
   * @param role
   *          The role whose accesses will be recalculated
   */
  public static void recalculateAllAccessesForRole(Role role) {
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(role);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    for (AccessType accessType : AccessType.values()) {
      RoleInheritanceManager manager = new RoleInheritanceManager(accessType);
      manager.calculateAccesses(inheritanceList, inheritanceRoleIdList);
    }
  }

  /**
   * Recalculates the accesses whose type is assigned to the manager, for those roles using as
   * template the role passed as parameter
   * 
   * @param template
   *          The template role used by the roles whose accesses will be recalculated
   */
  public void recalculateAccessFromTemplate(Role template) {
    for (RoleInheritance ri : template.getADRoleInheritanceInheritFromList()) {
      recalculateAccessForRole(ri.getRole());
    }
  }

  /**
   * Recalculates the accesses whose type is assigned to the manager for a given role
   * 
   * @param role
   *          The role whose accesses will be recalculated
   */
  public void recalculateAccessForRole(Role role) {
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(role);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    calculateAccesses(inheritanceList, inheritanceRoleIdList);
  }

  /**
   * Propagates a new access assigned to a template role
   * 
   * @param role
   *          The template role whose new access will be propagated
   * @param access
   *          The new access to be propagated
   */
  public void propagateNewAccess(Role role, InheritedAccessEnabled access) {
    if ("org.openbravo.model.ad.domain.Preference".equals(className)
        && !isInheritablePreference((Preference) access)) {
      return;
    }
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(className)
        && !isInheritableAlertRecipient((AlertRecipient) access)) {
      return;
    }
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      List<RoleInheritance> inheritanceList = getRoleInheritancesList(ri.getRole());
      List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
      handleAccess(ri, access, inheritanceRoleIdList);
    }
  }

  /**
   * Propagates an updated access of a template role
   * 
   * @param role
   *          The template role whose updated access will be propagated
   * @param access
   *          The updated access with the changes to propagate
   */
  public void propagateUpdatedAccess(Role role, InheritedAccessEnabled access) {
    if ("org.openbravo.model.ad.domain.Preference".equals(className)
        && !isInheritablePreference((Preference) access)) {
      return;
    }
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(className)
        && !isInheritableAlertRecipient((AlertRecipient) access)) {
      return;
    }
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      List<? extends InheritedAccessEnabled> roleAccessList = getAccessList(ri.getRole());
      InheritedAccessEnabled childAccess = findInheritedAccess(roleAccessList, access);
      if (childAccess != null) {
        updateRoleAccess(childAccess, access);
      }
    }
  }

  /**
   * Propagates a deleted access of a template role
   * 
   * @param role
   *          The template role whose deleted access will be propagated
   * @param access
   *          The removed access to be propagated
   */
  public void propagateDeletedAccess(Role role, InheritedAccessEnabled access) {
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      Role childRole = ri.getRole();
      List<? extends InheritedAccessEnabled> roleAccessList = getAccessList(childRole);
      InheritedAccessEnabled iaeToDelete = findInheritedAccess(roleAccessList, access);
      if (iaeToDelete != null) {
        // need to recalculate, look for this access in other inheritances
        String iaeToDeleteElementId = getSecuredElementIdentifier(iaeToDelete);
        boolean updated = false;
        // retrieve the list of templates, ordered by sequence number descending, to update the
        // access with the first one available (highest sequence number)
        List<Role> inheritFromList = getRoleInheritancesInheritFromList(childRole, role, false);
        for (Role inheritFrom : inheritFromList) {
          for (InheritedAccessEnabled inheritFromAccess : getAccessList(inheritFrom)) {
            String accessElementId = getSecuredElementIdentifier(inheritFromAccess);
            if (accessElementId.equals(iaeToDeleteElementId)) {
              updateRoleAccess(iaeToDelete, inheritFromAccess);
              updated = true;
              break;
            }
          }
          if (updated) {
            break;
          }
        }
        if (!updated) {
          // access not present in other inheritances, remove it
          iaeToDelete.setInheritedFrom(null);
          roleAccessList.remove(iaeToDelete);
          OBDal.getInstance().remove(iaeToDelete);
        }
      }
    }
  }

  /**
   * Looks for a particular access into an accessList
   * 
   * @param accessList
   *          The accessList to look for
   * @param access
   *          The access to be found
   * @return the access being searched or null if not found
   */
  private InheritedAccessEnabled findInheritedAccess(
      List<? extends InheritedAccessEnabled> accessList, InheritedAccessEnabled access) {
    String accessElementId = getSecuredElementIdentifier(access);
    String accessRole = (String) DalUtil.getId(getRole(access));
    for (InheritedAccessEnabled iae : accessList) {
      String listElementId = getSecuredElementIdentifier(iae);
      String inheritFromRole = iae.getInheritedFrom() != null ? (String) DalUtil.getId(iae
          .getInheritedFrom()) : "";
      if (accessElementId.equals(listElementId) && accessRole.equals(inheritFromRole)) {
        return iae;
      }
    }
    return null;
  }

  /**
   * Utility method to determine if a preference is inheritable. An inheritable preference should
   * only define the role on its visibility settings and it must not be present in the black list.
   * 
   * @param preference
   *          The preference
   * @return true if the Preference is inheritable, false otherwise
   */
  private boolean isInheritablePreference(Preference preference) {
    if (preference.getVisibleAtClient() == null && preference.getVisibleAtOrganization() == null
        && preference.getUserContact() == null && preference.getWindow() == null
        && preference.getVisibleAtRole() != null) {
      return true;
    }
    if (preference.isPropertyList()) {
      return !propertyBlackList.contains(preference.getProperty());
    } else {
      return true;
    }
  }

  /**
   * Utility method to determine if an alert recipient is inheritable. An inheritable alert
   * recipient should have the User/Contact field empty.
   * 
   * @param alertRecipient
   *          The alert recipient instance
   * @return true if the AlertRecipient is inheritable, false otherwise
   */
  private boolean isInheritableAlertRecipient(AlertRecipient alertRecipient) {
    if (alertRecipient.getUserContact() != null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * @see RoleInheritanceManager#calculateAccesses(List<RoleInheritance>, List<String>,
   *      RoleInheritance)
   */
  private void calculateAccesses(List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList) {
    calculateAccesses(inheritanceList, inheritanceInheritFromIdList, null);
  }

  /**
   * Calculate the inheritable accesses according to the inheritance list passed as parameter.
   * 
   * @param inheritanceList
   *          The list of inheritances used to calculate the accesses
   * @param inheritanceInheritFromIdList
   *          A list of template role ids. The position of the ids in this list determines the
   *          priority when applying their related inheritances.
   * @param roleInheritanceToDelete
   *          If not null, the accesses introduced by this inheritance will be removed
   */
  private void calculateAccesses(List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList, RoleInheritance roleInheritanceToDelete) {
    for (RoleInheritance roleInheritance : inheritanceList) {
      for (InheritedAccessEnabled inheritedAccess : getAccessList(roleInheritance.getInheritFrom())) {
        handleAccess(roleInheritance, inheritedAccess, inheritanceInheritFromIdList);
      }
    }
    if (roleInheritanceToDelete != null) {
      // delete accesses not inherited anymore
      deleteRoleAccess(roleInheritanceToDelete.getInheritFrom(),
          getAccessList(roleInheritanceToDelete.getRole()));
    }
    // OBDal.getInstance().getSession().clear();
  }

  /**
   * Determines if a access candidate to be inherited should be created, not created or updated.
   * 
   * @param roleInheritance
   *          Inheritance with the role information
   * @param inheritedAccess
   *          An existing access candidate to be overridden
   * @param inheritanceInheritFromIdList
   *          A list of template role ids which determines the priority of the template roles
   */
  private void handleAccess(RoleInheritance roleInheritance,
      InheritedAccessEnabled inheritedAccess, List<String> inheritanceInheritFromIdList) {
    String inheritedAccessElementId = getSecuredElementIdentifier(inheritedAccess);
    String newInheritedFromId = (String) DalUtil.getId(roleInheritance.getInheritFrom());
    boolean found = false;
    for (InheritedAccessEnabled access : getAccessList(roleInheritance.getRole())) {
      String accessElementId = getSecuredElementIdentifier(access);
      String currentInheritedFromId = access.getInheritedFrom() != null ? (String) DalUtil
          .getId(access.getInheritedFrom()) : "";
      if (accessElementId.equals(inheritedAccessElementId)) {
        if (!StringUtils.isEmpty(currentInheritedFromId)
            && isPrecedent(inheritanceInheritFromIdList, currentInheritedFromId, newInheritedFromId)) {
          updateRoleAccess(access, inheritedAccess);
        }
        found = true;
        break;
      }
    }
    if (!found) {
      copyRoleAccess(inheritedAccess, roleInheritance.getRole());
    }
  }

  /**
   * Utility method used to determine the precedence between two roles according to the given
   * priority list.
   * 
   * @param inheritanceInheritFromIdList
   *          A list of template role ids which determines the priority of the template roles
   * @param role1
   *          The first role to check its priority
   * @param role2
   *          The second role to check its priority
   * @return true if the first role is precedent to the second role, false otherwise
   */
  private boolean isPrecedent(List<String> inheritanceInheritFromIdList, String role1, String role2) {
    if (inheritanceInheritFromIdList.indexOf(role1) == -1) {
      // Not found, need to override (this can happen on delete or on update)
      return true;
    }
    if (inheritanceInheritFromIdList.indexOf(role1) < inheritanceInheritFromIdList.indexOf(role2)) {
      return true;
    }
    return false;
  }

  /**
   * @see RoleInheritanceManager#getRoleInheritancesList(Role, boolean)
   */
  public static List<RoleInheritance> getRoleInheritancesList(Role role) {
    return getRoleInheritancesList(role, true);
  }

  /**
   * @see RoleInheritanceManager#getRoleInheritancesList(Role, Role, boolean)
   */
  public static List<RoleInheritance> getRoleInheritancesList(Role role, boolean seqNoAscending) {
    return getRoleInheritancesList(role, null, true);
  }

  /**
   * Returns the list of inheritances of a role
   * 
   * @param role
   *          The role whose inheritance list will be retrieved
   * @param excludedInheritFrom
   *          A template role whose inheritance will be excluded from the returned list
   * @param seqNoAscending
   *          Determines of the list is returned by sequence number ascending (true) or descending
   * @return the list of inheritances of the role
   */
  public static List<RoleInheritance> getRoleInheritancesList(Role role, Role excludedInheritFrom,
      boolean seqNoAscending) {
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, role));
    if (excludedInheritFrom != null) {
      obCriteria.add(Restrictions.ne(RoleInheritance.PROPERTY_INHERITFROM, excludedInheritFrom));
    }
    obCriteria.addOrderBy(RoleInheritance.PROPERTY_SEQUENCENUMBER, seqNoAscending);
    return obCriteria.list();
  }

  /**
   * Returns the list of template roles which a particular role is using.
   * 
   * @param role
   *          The role whose parent template role list will be retrieved
   * @param excludedInheritFrom
   *          A template role that can be excluded from the list
   * @param seqNoAscending
   *          Determines of the list is returned by sequence number ascending (true) or descending
   * @return the list of template roles used by role
   */
  public static List<Role> getRoleInheritancesInheritFromList(Role role, Role excludedInheritFrom,
      boolean seqNoAscending) {
    List<RoleInheritance> inheritancesList = getRoleInheritancesList(role, excludedInheritFrom,
        seqNoAscending);
    final List<Role> inheritFromList = new ArrayList<Role>();
    for (RoleInheritance ri : inheritancesList) {
      inheritFromList.add(ri.getInheritFrom());
    }
    return inheritFromList;
  }

  /**
   * Returns the list of inheritances of the role owner of the inheritance passed as parameter. It
   * also verifies if this inheritance fulfills the unique constraints, before adding it to the
   * list.
   * 
   * @param inheritance
   *          inheritance that contains the role information
   * @param deleting
   *          a flag which determines whether the inheritance passed as parameter should be included
   *          in the returned list.
   * @return the list of role inheritances
   */
  private static List<RoleInheritance> getUpdatedRoleInheritancesList(RoleInheritance inheritance,
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
        Utility.throwErrorMessage("RoleInheritanceInheritFromDuplicated");
      } else if (rh.getSequenceNumber().equals(inheritance.getSequenceNumber())) {
        Utility.throwErrorMessage("RoleInheritanceSequenceNumberDuplicated");
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

  /**
   * Returns the list of role template ids from an inheritance list.
   * 
   * @param roleInheritanceList
   *          a list of inheritances
   * @return the list of template role ids
   */
  private static List<String> getRoleInheritancesInheritFromIdList(
      List<RoleInheritance> roleInheritanceList) {
    final ArrayList<String> roleIdsList = new ArrayList<String>();
    for (RoleInheritance roleInheritance : roleInheritanceList) {
      roleIdsList.add((String) DalUtil.getId(roleInheritance.getInheritFrom()));
    }
    return roleIdsList;
  }

  /**
   * Enumeration type which defines all inheritable accesses.
   * 
   */
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
    ALERT_RECIPIENT("org.openbravo.model.ad.alert.AlertRecipient", "getAlertRule"),
    /**
     * Preference
     */
    PREFERENCE("org.openbravo.model.ad.domain.Preference", "getIdentifier");

    private final String className;
    private final String securedElement;

    /**
     * Basi constructor.
     * 
     * @param className
     *          a String with the name of the class
     * @param securedElement
     *          a String with the name of the method to retrieve the secured element
     */
    AccessType(String className, String securedElement) {
      this.className = className;
      this.securedElement = securedElement;
    }

    /**
     * Returns the name of the inheritable class.
     * 
     * @return A String with the class name
     */
    public String getClassName() {
      return this.className;
    }

    /**
     * Returns the secured object.
     * 
     * @return a String with the name of the method to retrieve the secured element
     */
    public String getSecuredElement() {
      return this.securedElement;
    }

    /**
     * Returns the corresponding AccessType based on the entity name.
     * 
     * @param entityName
     *          a String with the entity name.
     * @return the AccessType associated to the input entity.
     * @throws OBException
     *           In case the input String parameter does not correspond with any valid AccessType,
     *           an exception is thrown with the error message.
     */
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
      } else if (Preference.ENTITY_NAME.equals(entityName)) {
        return AccessType.PREFERENCE;
      } else {
        throw new OBException(OBMessageUtils.getI18NMessage("UnsupportedAccessType",
            new String[] { entityName }));
      }
    }
  }
}
