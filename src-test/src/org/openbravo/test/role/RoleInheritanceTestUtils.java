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
package org.openbravo.test.role;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

public class RoleInheritanceTestUtils {
  public final static String CLIENT_ID = "23C59575B9CF467C9620760EB255B389";
  public final static String ASTERISK_ORG_ID = "0";

  public static Role createRole(String name, String clientId, String organizationId,
      String userLevel, boolean isManual, boolean isTemplate) {
    final Role role = OBProvider.getInstance().get(Role.class);
    Client client = OBDal.getInstance().get(Client.class, clientId);
    Organization org = OBDal.getInstance().get(Organization.class, organizationId);
    role.setClient(client);
    role.setClientList(clientId);
    role.setOrganization(org);
    role.setOrganizationList(organizationId);
    role.setTemplate(isTemplate);
    role.setManual(isManual);
    role.setName(name);
    role.setUserLevel(userLevel);
    OBDal.getInstance().save(role);
    return role;
  }

  public static void deleteRole(Role role) {
    OBDal.getInstance().remove(role);
  }

  public static void addInheritance(Role role, Role template, Long sequenceNumber) {
    final RoleInheritance inheritance = OBProvider.getInstance().get(RoleInheritance.class);
    inheritance.setClient(role.getClient());
    inheritance.setOrganization(role.getOrganization());
    inheritance.setRole(role);
    inheritance.setInheritFrom(template);
    inheritance.setSequenceNumber(sequenceNumber);
    OBDal.getInstance().save(inheritance);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  public static void removeInheritance(Role role, Role template) {
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, role));
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_INHERITFROM, template));
    obCriteria.setMaxResults(1);
    RoleInheritance roleInheritance = (RoleInheritance) obCriteria.uniqueResult();
    OBDal.getInstance().remove(roleInheritance);
  }
}
