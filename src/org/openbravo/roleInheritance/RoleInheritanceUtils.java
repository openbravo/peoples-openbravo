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

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.service.db.DalConnectionProvider;

public class RoleInheritanceUtils {

  public static List<RoleInheritance> getRoleInheritancesList(Role role) {
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(RoleInheritance.PROPERTY_SEQUENCENUMBER, true);
    return obCriteria.list();
  }

  public static List<RoleInheritance> getUpdatedRoleInheritancesList(RoleInheritance inheritance,
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

  public static List<String> getRoleInheritancesRoleIdList(List<RoleInheritance> roleInheritanceList) {
    final ArrayList<String> roleIdsList = new ArrayList<String>();
    for (RoleInheritance roleInheritance : roleInheritanceList) {
      roleIdsList.add((String) DalUtil.getId(roleInheritance.getInheritFrom()));
    }
    return roleIdsList;
  }

  public static void showErrorMessage(String message) {
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    ConnectionProvider conn = new DalConnectionProvider(false);
    throw new OBException(Utility.messageBD(conn, message, language));
  }
}
