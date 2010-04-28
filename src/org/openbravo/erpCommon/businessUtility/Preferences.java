/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.businessUtility;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Handles preferences, resolving priorities in case there are values for a same property at
 * different visibility levels
 * 
 */
public class Preferences {
  private static final Logger log4j = Logger.getLogger(Preferences.class);

  /**
   * Obtains a list of all preferences that are applicable at the given visibility level (client,
   * org, user, role)
   */
  public static List<Preference> getAllPreferences(String client, String org, String user,
      String role) {
    OBContext.enableAsAdminContext();
    try {
      StringBuilder hql = new StringBuilder();
      hql.append(" as p ");
      hql.append(" where (p.visibleAtClient.id = :client ");
      hql.append("        or coalesce(p.visibleAtClient, '0')='0') ");
      hql.append("   and (p.visibleAtRole.id = :role ");
      hql.append("        or p.visibleAtRole is null) ");
      hql.append("   and (coalesce(p.visibleAtOrganization, '0')='0'");
      hql.append("        or (ad_isorgincluded(:org, p.visibleAtOrganization, :client) != -1))");
      hql.append("   and (p.userContact.id = :user ");
      hql.append("        or p.userContact.id is null) ");
      OBQuery<Preference> qPref = OBDal.getInstance().createQuery(Preference.class, hql.toString());
      qPref.setNamedParameter("client", client);
      qPref.setNamedParameter("org", org);
      qPref.setNamedParameter("role", role);
      qPref.setNamedParameter("user", user);

      List<String> parentTree = OBContext.getOBContext().getOrganizationStructureProvider()
          .getParentList(org, true);

      ArrayList<Preference> preferences = new ArrayList<Preference>();
      for (Preference pref : qPref.list()) {
        Preference existentPreference = getPreferenceFromList(pref, preferences);
        if (existentPreference == null) {
          // There is not a preference for the current property, add it to the list
          preferences.add(pref);
        } else {
          // There is a preference for the current property, check whether it is higher priority by
          // and if so replace it
          if (isHigherPriority(pref, existentPreference, parentTree)) {
            preferences.remove(existentPreference);
            preferences.add(pref);
          }
        }

      }
      return preferences;
    } finally {
      OBContext.resetAsAdminContext();
    }
  }

  /**
   * Saves the property/value as a preference. If a preference with exactly the same visualization
   * priority already exists, it is overwritten; if not, a new one is created.
   * <p>
   * It also saves the new preference in session, in case the vars parameter is not null. If it is
   * null, the preference is not stored in session.
   * 
   * @param property
   *          Name of the property or attribute for the preference.
   * @param value
   *          New value to set.
   * @param isListProperty
   *          Determines whether list of properties or attribute should be used.
   * @param client
   *          Client visibility.
   * @param org
   *          Organization visibility.
   * @param user
   *          User visibility.
   * @param role
   *          Role visibility.
   * @param window
   *          Window visibility.
   * @param vars
   *          VariablesSecureApp to store new property value.
   */
  public static void setPreferenceValue(String property, String value, boolean isListProperty,
      Client client, Organization org, User user, Role role, Window window, VariablesSecureApp vars) {
    boolean adminModule = OBContext.getOBContext().setInAdministratorMode(true);
    try {
      List<Object> parameters = new ArrayList<Object>();
      StringBuilder hql = new StringBuilder();
      hql.append(" as p ");
      hql.append(" where ");

      hql.append(" p.propertyList = " + (isListProperty ? "Y" : "N"));
      if (isListProperty) {
        hql.append(" and p.property = ? ");
      } else {
        hql.append(" and p.attribute = ? ");
      }
      parameters.add(property);

      if (client != null) {
        hql.append(" and p.visibleAtClient = ? ");
        parameters.add(client);
      } else {
        hql.append(" p.visibleAtClient is null");
      }

      if (org != null) {
        hql.append(" and p.visibleAtOrganization = ? ");
        parameters.add(org);
      } else {
        hql.append(" and p.visibleAtOrganization is null ");
      }

      if (user != null) {
        hql.append(" and p.userContact = ? ");
        parameters.add(user);
      } else {
        hql.append(" and p.userContact is null ");
      }

      if (role != null) {
        hql.append(" and p.visibleAtRole = ? ");
        parameters.add(role);
      } else {
        hql.append(" and p.visibleAtRole is null");
      }

      if (window != null) {
        hql.append(" and p.window = ? ");
        parameters.add(window);
      } else {
        hql.append(" and p.window is null");
      }

      OBQuery<Preference> qPref = OBDal.getInstance().createQuery(Preference.class, hql.toString());
      qPref.setParameters(parameters);

      Preference preference;
      if (qPref.list().size() == 0) {
        // New preference
        preference = OBProvider.getInstance().get(Preference.class);
        preference.setPropertyList(isListProperty);
        if (isListProperty) {
          preference.setProperty(property);
        } else {
          preference.setAttribute(property);
        }
        preference.setVisibleAtClient(client);
        preference.setVisibleAtOrganization(org);
        preference.setVisibleAtRole(role);
        preference.setUserContact(user);
        preference.setWindow(window);
      } else {
        // Rewrite value (assume there's no conflicting properties
        preference = qPref.list().get(0);
      }
      preference.setSearchKey(value);
      OBDal.getInstance().save(preference);

      if (vars != null) {
        savePreferenceInSession(vars, preference);
      }

    } finally {
      OBContext.getOBContext().setInAdministratorMode(adminModule);
    }

  }

  /**
   * Stores the preference as a session value
   * 
   * @param vars
   *          VariablesSecureApp of the current session to store preference in
   * @param preference
   *          Preference to save in session
   */
  public static void savePreferenceInSession(VariablesSecureApp vars, Preference preference) {
    String prefName = "P|"
        + (preference.getWindow() == null ? "" : (preference.getWindow().getId() + "|"))
        + (preference.isPropertyList() ? preference.getProperty() : preference.getAttribute());
    vars.setSessionValue(prefName, preference.getSearchKey());
    log4j.info("Set preference " + prefName + " - " + preference.getSearchKey());
  }

  /**
   * Determines which of the 2 preferences has higher visibility priority.
   * 
   * @param pref1
   *          First preference to compare
   * @param pref2
   *          Second preference to compare
   * @param parentTree
   *          Parent tree of organizations including the current one, used to assign more priority
   *          to organizations nearer in the tree.
   * @return true in case pref1 is more visible than pref2
   */
  private static boolean isHigherPriority(Preference pref1, Preference pref2,
      List<String> parentTree) {
    // Check priority by client
    if ((pref2.getVisibleAtClient() == null || pref2.getVisibleAtClient().getId().equals("0"))
        && pref1.getVisibleAtClient() != null && !pref1.getVisibleAtClient().getId().equals("0")) {
      return true;
    }

    // Check priority by organization
    Organization org1 = pref1.getVisibleAtOrganization();
    Organization org2 = pref2.getVisibleAtOrganization();
    if (org1 != null && org2 == null) {
      return true;
    }

    if ((org1 == null && org2 != null) || (org1 == null || org2 == null)) {
      return false;
    }

    if (org1 != null && org2 != null
        && depthInTree(org1, parentTree) < depthInTree(org2, parentTree)) {
      return true;
    }

    // Check priority by user
    if (pref1.getUserContact() != null && pref2.getUserContact() == null) {
      return true;
    }

    // Check priority by role
    if (pref1.getVisibleAtRole() != null && pref2.getVisibleAtRole() == null) {
      return true;
    }

    return false;
  }

  /**
   * Returns the position of a given organization in a tree, being 0 the nearest.
   * 
   * @param org
   *          Organization to check.
   * @param tree
   *          Tree of organizations to look in.
   * @return The position if the organization is in the tree, -1 if it is not.
   */
  private static int depthInTree(Organization org, List<String> tree) {
    int i = 0;
    for (String orgId : tree) {
      if (orgId.equals(org.getId())) {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Checks whether a there is a preference for the same property in a List. If so, it is returned,
   * other case null is returned.
   * 
   * @param pref
   *          Preference to look for.
   * @param preferences
   *          List of preferences to look in.
   * @return The preference if it exists in the list, null if not.
   */
  private static Preference getPreferenceFromList(Preference pref, List<Preference> preferences) {
    for (Preference listPref : preferences) {
      if (((listPref.isPropertyList() && pref.isPropertyList() && pref.getProperty().equals(
          listPref.getProperty())) || (!listPref.isPropertyList() && !pref.isPropertyList() && pref
          .getAttribute().equals(listPref.getAttribute())))
          && pref.getWindow().equals(listPref.getWindow())) {
        return listPref;
      }
    }
    return null;
  }
}
