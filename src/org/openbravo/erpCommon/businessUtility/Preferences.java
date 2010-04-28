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

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.common.enterprise.Organization;

public class Preferences {

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
          if (isHigherPriority(pref, existentPreference, client, parentTree)) {
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

  private static boolean isHigherPriority(Preference pref1, Preference pref2, String client,
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
