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
package org.openbravo.role.inheritance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.FieldAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.access.TabAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.role.inheritance.access.AccessTypeInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains all the methods required to manage the Role Inheritance functionality
 */
@ApplicationScoped
public class RoleInheritanceManager {

  private static final Logger log = LoggerFactory.getLogger(RoleInheritanceManager.class);
  private static final Set<String> propertyBlackList = new HashSet<String>(Arrays.asList(
      "OBUIAPP_RecentDocumentsList", "OBUIAPP_RecentViewList", "OBUIAPP_GridConfiguration",
      "OBUIAPP_DefaultSavedView", "UINAVBA_RecentLaunchList"));

  private static final int ACCESS_NOT_CHANGED = 0;
  private static final int ACCESS_UPDATED = 1;
  private static final int ACCESS_CREATED = 2;

  @Inject
  @Any
  private Instance<AccessTypeInjector> accessTypeInjectors;

  /**
   * Retrieves the properties of the entity related to the entered class name that will not be
   * copied.
   * 
   * @param className
   *          the name of the class
   */
  private List<String> getSkippedProperties(String className) {
    List<String> skippedProperties = new ArrayList<String>(Arrays.asList("creationDate",
        "createdBy"));
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(className)) {
      skippedProperties.add("role");
    } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
      skippedProperties.add("visibleAtRole");
    }
    return skippedProperties;
  }

  /**
   * Returns the id of the secured object for the given inheritable access.
   * 
   * @param access
   *          An object of an inheritable class,i.e., a class that implements
   *          InheritedAccessEnabled.
   * @param injector
   *          An AccessTypeInjector used to retrieve the access elements
   * 
   * @return A String with the id of the secured object
   */
  String getSecuredElementIdentifier(InheritedAccessEnabled access, AccessTypeInjector injector) {
    try {
      Class<?> myClass = Class.forName(injector.getClassName());
      if ("org.openbravo.model.ad.domain.Preference".equals(injector.getClassName())) {
        // Preference requires a special identifier management, because it is possible to define the
        // same preference with different visibility settings
        String identifier = (String) myClass.getMethod(injector.getSecuredElement()).invoke(access);
        Preference preference = (Preference) access;
        String visibleAtClient = preference.getVisibleAtClient() != null ? (String) DalUtil
            .getId(preference.getVisibleAtClient()) : "";
        String visibleAtOrg = preference.getVisibleAtOrganization() != null ? (String) DalUtil
            .getId(preference.getVisibleAtOrganization()) : "";
        String visibleAtUser = preference.getUserContact() != null ? (String) DalUtil
            .getId(preference.getUserContact()) : "";
        String visibleAtWindow = preference.getWindow() != null ? (String) DalUtil.getId(preference
            .getWindow()) : "";
        return identifier + "_" + visibleAtClient + "_" + visibleAtOrg + "_" + visibleAtUser + "_"
            + visibleAtWindow;
      }
      BaseOBObject bob = (BaseOBObject) myClass.getMethod(injector.getSecuredElement()).invoke(
          access);
      String securedElementIndentifier = (String) DalUtil.getId(bob);
      return securedElementIndentifier;
    } catch (Exception ex) {
      log.error("Error getting secured element identifier", ex);
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
   * @param className
   *          the name of the class
   */
  private void setParent(InheritedAccessEnabled newAccess, InheritedAccessEnabled parentAccess,
      Role role, String className) {
    try {
      // TabAccess, FieldAccess and Preference do not have role property as parent
      if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
        TabAccess newTabAccess = (TabAccess) newAccess;
        TabAccess parentTabAccess = (TabAccess) parentAccess;
        setParentWindow(newTabAccess, parentTabAccess, role);
        // We need to have the new tab access in memory for the case where we are
        // adding field accesses also (when adding a new inheritance)
        newTabAccess.getWindowAccess().getADTabAccessList().add(newTabAccess);
      } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
        setParentTab((FieldAccess) newAccess, (FieldAccess) parentAccess, role);
      } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
        ((Preference) (newAccess)).setVisibleAtRole(role);
      } else {
        setParentRole(newAccess, role, className);
        if ("org.openbravo.model.ad.access.WindowAccess".equals(className)) {
          // We need to have the new window access in memory for the case where we are
          // adding tab accesses also (when adding a new inheritance)
          role.getADWindowAccessList().add((WindowAccess) newAccess);
        }
      }
    } catch (Exception ex) {
      log.error("Error setting parent ", ex);
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
   * @param className
   *          the name of the class
   */
  private void setParentRole(InheritedAccessEnabled access, Role role, String className) {
    try {
      Class<?> myClass = Class.forName(className);
      myClass.getMethod("setRole", new Class[] { Role.class })
          .invoke(access, new Object[] { role });
    } catch (Exception ex) {
      log.error("Error setting parent role ", ex);
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
    String parentWindowId = (String) DalUtil.getId(parentTabAccess.getWindowAccess().getWindow());
    for (WindowAccess wa : role.getADWindowAccessList()) {
      String currentWindowId = (String) DalUtil.getId(wa.getWindow());
      if (currentWindowId.equals(parentWindowId)) {
        newTabAccess.setWindowAccess(wa);
        break;
      }
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
    String parentTabId = (String) DalUtil.getId(parentFieldAccess.getTabAccess().getTab());
    for (WindowAccess wa : role.getADWindowAccessList()) {
      for (TabAccess ta : wa.getADTabAccessList()) {
        String currentTabId = (String) DalUtil.getId(ta.getTab());
        if (currentTabId.equals(parentTabId)) {
          newFieldAccess.setTabAccess(ta);
          break;
        }
      }
    }
  }

  /**
   * Returns the role which the access given as parameter is assigned to.
   * 
   * @param access
   *          An inheritable access
   * @param className
   *          the name of the class
   * 
   * @return the Role owner of the access
   */
  Role getRole(InheritedAccessEnabled access, String className) {
    // TabAccess, FieldAccess and Preference do not have role property as parent
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
      return getParentRole(access, className);
    }
  }

  /**
   * Returns the role which the access given as parameter is assigned to. This method is used for
   * those inheritable accesses which Role is their parent entity.
   * 
   * @param access
   *          An inheritable access
   * @param className
   *          the name of the class
   * 
   * @return the parent Role of the access
   */
  private Role getParentRole(InheritedAccessEnabled access, String className) {
    try {
      Class<?> myClass = Class.forName(className);
      Role role = (Role) myClass.getMethod("getRole").invoke(access);
      return role;
    } catch (Exception ex) {
      log.error("Error getting role ", ex);
      throw new OBException("Error getting role");
    }
  }

  /**
   * Returns the list of accesses of a particular type for the Role given as parameter.
   * 
   * @param role
   *          The role whose list of accesses of a particular type will be retrieved
   * @param className
   *          the name of the class
   * 
   * @return a list of accesses
   */
  @SuppressWarnings("unchecked")
  private <T extends BaseOBObject> List<? extends InheritedAccessEnabled> getAccessList(Role role,
      String className) {
    try {
      String roleProperty = getRoleProperty(className);
      Class<T> clazz = (Class<T>) Class.forName(className);
      final StringBuilder whereClause = new StringBuilder();
      whereClause.append(" as p ");
      whereClause.append(" where p.").append(roleProperty).append(" = :roleId");
      addEntityWhereClause(whereClause, className);
      final OBQuery<T> query = OBDal.getInstance().createQuery(clazz, whereClause.toString());
      query.setNamedParameter("roleId", (String) DalUtil.getId(role));
      doEntityParameterReplacement(query, className);
      query.setFilterOnActive(false);
      return (List<? extends InheritedAccessEnabled>) query.list();
    } catch (Exception ex) {
      log.error("Error getting access list of class " + className, ex);
      throw new OBException("Error getting access list of class " + className);
    }
  }

  /**
   * Returns the role property retrieved from the class name.
   * 
   * @param className
   *          the name of the class
   * 
   * @return the role property that can be retrieved according to the input class name.
   */
  private String getRoleProperty(String className) {
    // TabAccess, FieldAccess and Preference do not have role property as parent
    if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
      return "windowAccess.role.id";
    } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
      return "tabAccess.windowAccess.role.id";
    } else if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
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
   * @param className
   *          The class name used to identify which filtering must be returned
   */
  private void addEntityWhereClause(StringBuilder whereClause, String className) {
    if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
      // Inheritable preferences are those that are not in the black list and also has a value in
      // the Visible At Role field
      whereClause.append(" and p.visibleAtRole is not null");
      whereClause.append(" and p.property not in (:blackList)");
    } else if ("org.openbravo.model.ad.alert.AlertRecipient".equals(className)) {
      // Inheritable alert recipients are those with empty User/Contact field
      whereClause.append(" and p.userContact is null");
    }
  }

  /**
   * Performs the needed parameter substitution according to the input class name.
   * 
   * @param query
   *          The query where to perform the parameter substitution
   * @param className
   *          The class name used to identify if the parameter substitution is needed
   */
  private <T extends BaseOBObject> void doEntityParameterReplacement(OBQuery<T> query,
      String className) {
    if ("org.openbravo.model.ad.domain.Preference".equals(className)) {
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
   * @param className
   *          the name of the class
   */
  private void copyRoleAccess(InheritedAccessEnabled parentAccess, Role role, String className) {
    // copy the new access
    final InheritedAccessEnabled newAccess = (InheritedAccessEnabled) DalUtil.copy(
        (BaseOBObject) parentAccess, false);
    setParent(newAccess, parentAccess, role, className);
    newAccess.setInheritedFrom(getRole(parentAccess, className));
    OBDal.getInstance().save(newAccess);
  }

  /**
   * Deletes all accesses which are inheriting from a particular role.
   * 
   * @param inheritFromToDelete
   *          The role which the accesses about to delete are inherited from
   * @param roleAccessList
   *          The list of accesses to remove from
   * @param className
   *          the name of the class
   */
  private void deleteRoleAccess(Role inheritFromToDelete,
      List<? extends InheritedAccessEnabled> roleAccessList, String className) {
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
      Role owner = getRole(iae, className);
      if (!owner.isTemplate()) {
        // Perform this operation for not template roles, because for template roles is already done
        // in the event handler
        removeReferenceInParentList(iae, className);
      }
      OBDal.getInstance().remove(iae);
    }
  }

  /**
   * Sets to null the Inherit From field to child elements (TabAccess and FieldAccess). This allows
   * the cascade deletion of these elements when removing an inherited Window Access or Tab Access.
   * 
   * @param access
   *          The access to be removed from the parent list
   * @param className
   *          the name of the class
   */
  void clearInheritFromFieldInChilds(InheritedAccessEnabled access, String className) {
    String inheritedFromId = (String) DalUtil.getId(access.getInheritedFrom());
    if ("org.openbravo.model.ad.access.WindowAccess".equals(className)) {
      WindowAccess wa = (WindowAccess) access;
      for (TabAccess ta : wa.getADTabAccessList()) {
        clearInheritedFromField(ta, inheritedFromId);
        for (FieldAccess fa : ta.getADFieldAccessList()) {
          clearInheritedFromField(fa, inheritedFromId);
        }
      }
    } else if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
      TabAccess ta = (TabAccess) access;
      for (FieldAccess fa : ta.getADFieldAccessList()) {
        clearInheritedFromField(fa, inheritedFromId);
      }
    }
  }

  /**
   * Sets to null the Inherited From field of an access whenever the value of the field is equal to
   * the entered role id.
   * 
   * @param access
   *          The access with the Inherit From field to be nullified
   * @param roleId
   *          The id of the role used to decide whether the field should be nullified or not
   */
  private void clearInheritedFromField(InheritedAccessEnabled access, String roleId) {
    String inheritedFromId = access.getInheritedFrom() != null ? (String) DalUtil.getId(access
        .getInheritedFrom()) : "";
    if (!StringUtils.isEmpty(inheritedFromId) && roleId.equals(inheritedFromId)) {
      access.setInheritedFrom(null);
    }
  }

  /**
   * Removes references to child elements (TabAccess and FieldAccess) from the parent list. Using
   * this method prevents the "deleted object would be re-saved by cascade" error after deleting an
   * inherited TabAccess or FieldAccess.
   * 
   * @param access
   *          The access to be removed from the parent list
   * @param className
   *          the name of the class
   */
  void removeReferenceInParentList(InheritedAccessEnabled access, String className) {
    boolean accessExists;
    if ("org.openbravo.model.ad.access.TabAccess".equals(className)) {
      TabAccess ta = (TabAccess) access;
      accessExists = OBDal.getInstance().exists(WindowAccess.ENTITY_NAME,
          (String) DalUtil.getId(ta.getWindowAccess()));
      if (accessExists) {
        ta.getWindowAccess().getADTabAccessList().remove(ta);
      }
    } else if ("org.openbravo.model.ad.access.FieldAccess".equals(className)) {
      FieldAccess fa = (FieldAccess) access;
      accessExists = OBDal.getInstance().exists(TabAccess.ENTITY_NAME,
          (String) DalUtil.getId(fa.getTabAccess()));
      if (accessExists) {
        fa.getTabAccess().getADFieldAccessList().remove(fa);
      }
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
   * @param className
   *          the name of the class
   */
  private void updateRoleAccess(InheritedAccessEnabled access, InheritedAccessEnabled inherited,
      String className) {
    final InheritedAccessEnabled updatedAccess = (InheritedAccessEnabled) DalUtil.copyToTarget(
        (BaseOBObject) inherited, (BaseOBObject) access, false, getSkippedProperties(className));
    // update the inherit from field, to indicate from which role we are inheriting now
    updatedAccess.setInheritedFrom(getRole(inherited, className));
  }

  /**
   * Applies all type of accesses based on the inheritance passed as parameter
   * 
   * @param inheritance
   *          The inheritance used to calculate the possible new accesses
   */
  void applyNewInheritance(RoleInheritance inheritance) {
    long t = System.currentTimeMillis();
    List<RoleInheritance> inheritanceList = getUpdatedRoleInheritancesList(inheritance, false);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    List<RoleInheritance> newInheritanceList = new ArrayList<RoleInheritance>();
    newInheritanceList.add(inheritance);
    for (AccessTypeInjector accessType : getAccessTypeOrderByPriority(true)) {
      calculateAccesses(newInheritanceList, inheritanceRoleIdList, accessType);
    }
    log.debug("add new inheritance time: " + (System.currentTimeMillis() - t));
  }

  /**
   * Calculates all type of accesses after the removal of the inheritance passed as parameter
   * 
   * @param inheritance
   *          The inheritance being removed
   */
  void applyRemoveInheritance(RoleInheritance inheritance) {
    long t = System.currentTimeMillis();
    List<RoleInheritance> inheritanceList = getUpdatedRoleInheritancesList(inheritance, true);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    for (AccessTypeInjector accessType : getAccessTypeOrderByPriority(false)) {
      // We need to retrieve the access types ordered descending by their priority, to force to
      // handle first 'child' accesses like TabAccess or ChildAccess which have a
      // priority number higher than their parent, WindowAccess. This way, child instances will be
      // deleted first when it applies.
      calculateAccesses(inheritanceList, inheritanceRoleIdList, inheritance, accessType);
    }
    log.debug("remove inheritance time: " + (System.currentTimeMillis() - t));
  }

  /**
   * Recalculates all accesses for those roles using as template the role passed as parameter
   * 
   * @param template
   *          The template role used by the roles whose accesses will be recalculated
   * @return a set of the child roles which have accesses that have been updated or created
   */
  public Set<Role> recalculateAllAccessesFromTemplate(Role template) {
    long t = System.currentTimeMillis();
    Set<Role> updatedRoles = new HashSet<Role>();
    for (RoleInheritance ri : template.getADRoleInheritanceInheritFromList()) {
      if (ri.isActive()) {
        Map<String, CalculationResult> result = recalculateAllAccessesForRole(ri.getRole());
        for (String accessClassName : result.keySet()) {
          CalculationResult counters = (CalculationResult) result.get(accessClassName);
          if (counters.getUpdated() > 0 || counters.getCreated() > 0) {
            updatedRoles.add(ri.getRole());
          }
        }
      }
    }
    log.debug("recalculate all accesses from template " + template.getName() + " time: "
        + (System.currentTimeMillis() - t));
    return updatedRoles;
  }

  /**
   * Recalculates all accesses for a given role
   * 
   * @param role
   *          The role whose accesses will be recalculated
   * @return a map with the number of accesses updated and created for every access type
   */
  public Map<String, CalculationResult> recalculateAllAccessesForRole(Role role) {
    long t = System.currentTimeMillis();
    Map<String, CalculationResult> result = new HashMap<String, CalculationResult>();
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(role);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    for (AccessTypeInjector accessType : getAccessTypeOrderByPriority(true)) {
      CalculationResult counters = calculateAccesses(inheritanceList, inheritanceRoleIdList,
          accessType);
      result.put(accessType.getClassName(), counters);
    }
    log.debug("recalculate all accesses for role " + role.getName() + " time: "
        + (System.currentTimeMillis() - t));
    return result;
  }

  /**
   * Recalculates the accesses whose type is assigned to the manager, for those roles using as
   * template the role passed as parameter
   * 
   * @param template
   *          The template role used by the roles whose accesses will be recalculated * @param
   * @param classCanonicalName
   *          the name of the class
   */
  public void recalculateAccessFromTemplate(Role template, String classCanonicalName) {
    long t = System.currentTimeMillis();
    AccessTypeInjector injector = getInjector(classCanonicalName);
    if (injector == null) {
      return;
    }
    for (RoleInheritance ri : template.getADRoleInheritanceInheritFromList()) {
      if (ri.isActive()) {
        recalculateAccessForRole(ri.getRole(), injector);
      }
    }
    log.debug("recalculate access for " + classCanonicalName + " from template "
        + template.getName() + " time: " + (System.currentTimeMillis() - t));
  }

  /**
   * Recalculates the accesses whose type is assigned to the manager for a given role
   * 
   * @param role
   *          The role whose accesses will be recalculated
   * @param injector
   *          An AccessTypeInjector used to retrieve the access elements
   * 
   */
  public void recalculateAccessForRole(Role role, AccessTypeInjector injector) {
    long t = System.currentTimeMillis();
    List<RoleInheritance> inheritanceList = getRoleInheritancesList(role);
    List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
    calculateAccesses(inheritanceList, inheritanceRoleIdList, injector);
    log.debug("recalculate access for " + injector.getClassName() + " for role " + role.getName()
        + " time: " + (System.currentTimeMillis() - t));
  }

  /**
   * Propagates a new access assigned to a template role
   * 
   * @param role
   *          The template role whose new access will be propagated
   * @param access
   *          The new access to be propagated
   * @param classCanonicalName
   *          the name of the class
   */
  void propagateNewAccess(Role role, InheritedAccessEnabled access, String classCanonicalName) {
    long t = System.currentTimeMillis();
    AccessTypeInjector injector = getInjector(classCanonicalName);
    if (injector == null) {
      return;
    }
    if ("org.openbravo.model.ad.domain.Preference".equals(injector.getClassName())) {
      Preference preference = (Preference) access;
      if (Preferences.existsPreference(preference)) {
        Utility.throwErrorMessage("DuplicatedPreferenceForTemplate");
      }
      if (!isInheritablePreference(preference)) {
        return;
      }
    }
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(injector.getClassName())) {
      AlertRecipient alertRecipient = (AlertRecipient) access;
      if (existsAlertRecipient(alertRecipient)) {
        Utility.throwErrorMessage("DuplicatedAlertRecipientForTemplate");
      }
      if (!isInheritableAlertRecipient(alertRecipient)) {
        return;
      }
    }
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      if (ri.isActive()) {
        List<RoleInheritance> inheritanceList = getRoleInheritancesList(ri.getRole());
        List<String> inheritanceRoleIdList = getRoleInheritancesInheritFromIdList(inheritanceList);
        handleAccess(ri, access, inheritanceRoleIdList, injector);
      }
    }
    log.debug("propagate new access from template " + role.getName() + " time: "
        + (System.currentTimeMillis() - t));
  }

  /**
   * Propagates an updated access of a template role
   * 
   * @param role
   *          The template role whose updated access will be propagated
   * @param access
   *          The updated access with the changes to propagate
   * @param classCanonicalName
   *          the name of the class
   */
  void propagateUpdatedAccess(Role role, InheritedAccessEnabled access, String classCanonicalName) {
    long t = System.currentTimeMillis();
    AccessTypeInjector injector = getInjector(classCanonicalName);
    if (injector == null) {
      return;
    }
    if ("org.openbravo.model.ad.domain.Preference".equals(injector.getClassName())
        && !isInheritablePreference((Preference) access)) {
      return;
    }
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(injector.getClassName())
        && !isInheritableAlertRecipient((AlertRecipient) access)) {
      return;
    }
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      if (ri.isActive()) {
        List<? extends InheritedAccessEnabled> roleAccessList = getAccessList(ri.getRole(),
            injector.getClassName());
        InheritedAccessEnabled childAccess = findInheritedAccess(roleAccessList, access, injector);
        if (childAccess != null) {
          updateRoleAccess(childAccess, access, injector.getClassName());
        }
      }
    }
    log.debug("propagate updated access from template " + role.getName() + " time: "
        + (System.currentTimeMillis() - t));
  }

  /**
   * Propagates a deleted access of a template role
   * 
   * @param role
   *          The template role whose deleted access will be propagated
   * @param access
   *          The removed access to be propagated
   * @param classCanonicalName
   *          the name of the class
   */
  void propagateDeletedAccess(Role role, InheritedAccessEnabled access, String classCanonicalName) {
    long t = System.currentTimeMillis();
    AccessTypeInjector injector = getInjector(classCanonicalName);
    if (injector == null) {
      return;
    }
    if ("org.openbravo.model.ad.domain.Preference".equals(injector.getClassName())
        && !isInheritablePreference((Preference) access)) {
      return;
    }
    if ("org.openbravo.model.ad.alert.AlertRecipient".equals(injector.getClassName())
        && !isInheritableAlertRecipient((AlertRecipient) access)) {
      return;
    }
    for (RoleInheritance ri : role.getADRoleInheritanceInheritFromList()) {
      if (ri.isActive()) {
        Role childRole = ri.getRole();
        List<? extends InheritedAccessEnabled> roleAccessList = getAccessList(childRole,
            injector.getClassName());
        InheritedAccessEnabled iaeToDelete = findInheritedAccess(roleAccessList, access, injector);
        if (iaeToDelete != null) {
          // need to recalculate, look for this access in other inheritances
          String iaeToDeleteElementId = getSecuredElementIdentifier(iaeToDelete, injector);
          boolean updated = false;
          // retrieve the list of templates, ordered by sequence number descending, to update the
          // access with the first one available (highest sequence number)
          List<Role> inheritFromList = getRoleInheritancesInheritFromList(childRole, role, false);
          for (Role inheritFrom : inheritFromList) {
            for (InheritedAccessEnabled inheritFromAccess : getAccessList(inheritFrom,
                injector.getClassName())) {
              String accessElementId = getSecuredElementIdentifier(inheritFromAccess, injector);
              if (accessElementId.equals(iaeToDeleteElementId)) {
                updateRoleAccess(iaeToDelete, inheritFromAccess, injector.getClassName());
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
            clearInheritFromFieldInChilds(iaeToDelete, injector.getClassName());
            iaeToDelete.setInheritedFrom(null);
            roleAccessList.remove(iaeToDelete);
            Role owner = getRole(iaeToDelete, injector.getClassName());
            if (!owner.isTemplate()) {
              // Perform this operation for not template roles, because for template roles is
              // already done in the event handler
              removeReferenceInParentList(iaeToDelete, injector.getClassName());
            }
            OBDal.getInstance().remove(iaeToDelete);
          }
        }
      }
    }
    log.debug("propagate deleted access from template " + role.getName() + " time: "
        + (System.currentTimeMillis() - t));
  }

  /**
   * Looks for a particular access into an accessList
   * 
   * @param accessList
   *          The accessList to look for
   * @param access
   *          The access to be found
   * @param injector
   *          An AccessTypeInjector used to retrieve the access elements
   * @return the access being searched or null if not found
   */
  private InheritedAccessEnabled findInheritedAccess(
      List<? extends InheritedAccessEnabled> accessList, InheritedAccessEnabled access,
      AccessTypeInjector injector) {
    String accessElementId = getSecuredElementIdentifier(access, injector);
    String accessRole = (String) DalUtil.getId(getRole(access, injector.getClassName()));
    for (InheritedAccessEnabled iae : accessList) {
      String listElementId = getSecuredElementIdentifier(iae, injector);
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
    if (preference.getVisibleAtRole() != null) {
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
   * Utility method to determine if already exists an alert recipient with the same settings (alert
   * rule, role and user) as the alertRecipient passed as parameter
   * 
   * @param alertRecipient
   *          The alert recipient with the settings to find
   * @return true if already exists an alert recipient with the same settings as the entered alert
   *         recipient, false otherwise
   */
  private boolean existsAlertRecipient(AlertRecipient alertRecipient) {
    final OBCriteria<AlertRecipient> obCriteria = OBDal.getInstance().createCriteria(
        AlertRecipient.class);
    obCriteria
        .add(Restrictions.eq(AlertRecipient.PROPERTY_ALERTRULE, alertRecipient.getAlertRule()));
    obCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_ROLE, alertRecipient.getRole()));
    if (alertRecipient.getUserContact() == null) {
      obCriteria.add(Restrictions.isNull(AlertRecipient.PROPERTY_USERCONTACT));
    } else {
      obCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_USERCONTACT,
          alertRecipient.getUserContact()));
    }
    obCriteria.setMaxResults(1);
    return (obCriteria.list().size() > 0);
  }

  /**
   * @see RoleInheritanceManager#calculateAccesses(List, List, RoleInheritance, AccessTypeInjector)
   */
  private CalculationResult calculateAccesses(List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList, AccessTypeInjector injector) {
    return calculateAccesses(inheritanceList, inheritanceInheritFromIdList, null, injector);
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
   * @param injector
   *          An AccessTypeInjector used to retrieve the access elements
   * @return a list with two Integers containing the number of accesses updated and created
   *         respectively.
   */
  private CalculationResult calculateAccesses(List<RoleInheritance> inheritanceList,
      List<String> inheritanceInheritFromIdList, RoleInheritance roleInheritanceToDelete,
      AccessTypeInjector injector) {
    int[] counters = new int[] { 0, 0, 0 };
    for (RoleInheritance roleInheritance : inheritanceList) {
      for (InheritedAccessEnabled inheritedAccess : getAccessList(roleInheritance.getInheritFrom(),
          injector.getClassName())) {
        if ("org.openbravo.model.ad.domain.Preference".equals(injector.getClassName())
            && !isInheritablePreference((Preference) inheritedAccess)) {
          continue;
        }
        if ("org.openbravo.model.ad.alert.AlertRecipient".equals(injector.getClassName())
            && !isInheritableAlertRecipient((AlertRecipient) inheritedAccess)) {
          continue;
        }
        int res = handleAccess(roleInheritance, inheritedAccess, inheritanceInheritFromIdList,
            injector);
        counters[res]++;
      }
    }
    if (roleInheritanceToDelete != null) {
      // delete accesses not inherited anymore
      deleteRoleAccess(roleInheritanceToDelete.getInheritFrom(),
          getAccessList(roleInheritanceToDelete.getRole(), injector.getClassName()),
          injector.getClassName());
    }
    CalculationResult result = new CalculationResult(counters[ACCESS_UPDATED],
        counters[ACCESS_CREATED]);
    return result;
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
   * @param injector
   *          An AccessTypeInjector used to retrieve the access elements
   * @return an integer that indicates the final action done with the access: not changed
   *         (ACCESS_NOT_CHANGED), updated (ACCESS_UPDATED) or created (ACCESS_CREATED).
   */
  private int handleAccess(RoleInheritance roleInheritance, InheritedAccessEnabled inheritedAccess,
      List<String> inheritanceInheritFromIdList, AccessTypeInjector injector) {
    String inheritedAccessElementId = getSecuredElementIdentifier(inheritedAccess, injector);
    String newInheritedFromId = (String) DalUtil.getId(roleInheritance.getInheritFrom());
    Role role = roleInheritance.getRole();
    for (InheritedAccessEnabled access : getAccessList(role, injector.getClassName())) {
      String accessElementId = getSecuredElementIdentifier(access, injector);
      String currentInheritedFromId = access.getInheritedFrom() != null ? (String) DalUtil
          .getId(access.getInheritedFrom()) : "";
      if (accessElementId.equals(inheritedAccessElementId)) {
        if (!StringUtils.isEmpty(currentInheritedFromId)
            && isPrecedent(inheritanceInheritFromIdList, currentInheritedFromId, newInheritedFromId)) {
          updateRoleAccess(access, inheritedAccess, injector.getClassName());
          log.debug("Updated access for role " + role.getName() + ": class = "
              + injector.getClassName() + " secured element id = " + inheritedAccessElementId);
          return ACCESS_UPDATED;
        }
        return ACCESS_NOT_CHANGED;
      }
    }
    copyRoleAccess(inheritedAccess, roleInheritance.getRole(), injector.getClassName());
    log.debug("Created access for role " + role.getName() + ": class = " + injector.getClassName()
        + " secured element id = " + inheritedAccessElementId);
    return ACCESS_CREATED;
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
  List<RoleInheritance> getRoleInheritancesList(Role role) {
    return getRoleInheritancesList(role, true);
  }

  /**
   * @see RoleInheritanceManager#getRoleInheritancesList(Role, Role, boolean)
   */
  List<RoleInheritance> getRoleInheritancesList(Role role, boolean seqNoAscending) {
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
  List<RoleInheritance> getRoleInheritancesList(Role role, Role excludedInheritFrom,
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
  List<Role> getRoleInheritancesInheritFromList(Role role, Role excludedInheritFrom,
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
   *          in the returned list or not.
   * @return the list of role inheritances
   */
  private List<RoleInheritance> getUpdatedRoleInheritancesList(RoleInheritance inheritance,
      boolean deleting) {
    final ArrayList<RoleInheritance> roleInheritancesList = new ArrayList<RoleInheritance>();
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, inheritance.getRole()));
    obCriteria
        .add(Restrictions.ne(RoleInheritance.PROPERTY_ID, (String) DalUtil.getId(inheritance)));
    obCriteria.addOrderBy(RoleInheritance.PROPERTY_SEQUENCENUMBER, true);
    boolean added = false;
    for (RoleInheritance rh : obCriteria.list()) {
      String inheritFromId = (String) DalUtil.getId(rh.getInheritFrom());
      String inheritanceInheritFromId = (String) DalUtil.getId(inheritance.getInheritFrom());
      if (inheritFromId.equals(inheritanceInheritFromId)) {
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
  private List<String> getRoleInheritancesInheritFromIdList(
      List<RoleInheritance> roleInheritanceList) {
    final ArrayList<String> roleIdsList = new ArrayList<String>();
    for (RoleInheritance roleInheritance : roleInheritanceList) {
      roleIdsList.add((String) DalUtil.getId(roleInheritance.getInheritFrom()));
    }
    return roleIdsList;
  }

  /**
   * Returns the list of access types ordered by their priority value
   * 
   * @param ascending
   *          determines the sorting of the list, ascending (true) or descending (false)
   * 
   * @return the list of template access types
   */
  private List<AccessTypeInjector> getAccessTypeOrderByPriority(boolean ascending) {
    List<AccessTypeInjector> list = new ArrayList<AccessTypeInjector>();
    for (AccessTypeInjector injector : accessTypeInjectors) {
      list.add(injector);
    }
    Collections.sort(list);
    if (!ascending) {
      Collections.reverse(list);
    }
    return list;
  }

  /**
   * Returns the injector for the access type related to the canonical name of the class entered as
   * parameter
   * 
   * @param classCanonicalName
   *          the name of the class to identify the injector
   * 
   * @return the AccessTypeInjector used to retrieve the access type to be handled by the manager
   */
  private AccessTypeInjector getInjector(String classCanonicalName) {
    try {
      for (AccessTypeInjector injector : accessTypeInjectors
          .select(new AccessTypeInjector.Selector(classCanonicalName))) {
        return injector;
      }
    } catch (Exception e) {
      log.error("No access type injector found for class name: " + classCanonicalName, e);
    }
    return null;
  }

  /**
   * Returns true if there exists an injector for the access type related to the canonical name of
   * the class entered as parameter
   * 
   * @param classCanonicalName
   *          the name of the class to identify the injector
   * 
   * @return true if exists an injector for the entered class name, false otherwise
   */
  public boolean existsInjector(String classCanonicalName) {
    if (getInjector(classCanonicalName) == null) {
      return false;
    }
    return true;
  }

  /**
   * A class used to hold the results of the recalculation done for a particular access of a role
   */
  class CalculationResult {
    private int updated;
    private int created;

    /**
     * Basic constructor
     * 
     * @param updated
     *          An integer that represents the number of accesses updated during an access
     *          recalculation
     * @param created
     *          An integer that represents the number of new accesses created during an access
     *          recalculation
     */
    public CalculationResult(int updated, int created) {
      this.updated = updated;
      this.created = created;
    }

    /**
     * Returns the updated number
     * 
     * @return the value of the updated field
     */
    public int getUpdated() {
      return updated;
    }

    /**
     * Returns the created number
     * 
     * @return the value of the created field
     */
    public int getCreated() {
      return created;
    }
  }
}
