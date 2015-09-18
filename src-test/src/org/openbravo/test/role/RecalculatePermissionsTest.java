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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.roleInheritance.RoleInheritanceManager;
import org.openbravo.test.base.OBBaseTest;

public class RecalculatePermissionsTest extends OBBaseTest {
  // This test case is intended to simulate the "Recalculate Permissions" process
  // We make use a extension of the OBBaseTest to avoid the execution of the related event handlers
  // This way, we can simulate the process of adding of a permission without using DAL, for example,
  // like when using the Grant Access process which is based on a DB stored procedure

  @Test
  public void testRolePermissionRecalculate() {
    Role template = null;
    Role role = null;
    try {
      OBContext.setAdminMode(true);
      template = RoleInheritanceTestUtils.createRole("template",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateId = (String) DalUtil.getId(template);
      role = RoleInheritanceTestUtils.createRole("role", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, true);
      String roleId = (String) DalUtil.getId(role);

      // Add inheritance
      RoleInheritanceTestUtils.addInheritance(role, template, new Long(10));

      // Add permission (it will not be propagated as event handlers will not be fired)
      BasicADWindowInheritanceTest.addWindowAccess(template, "Sales Order", true);

      OBDal.getInstance().commitAndClose();
      role = OBDal.getInstance().get(Role.class, roleId);

      RoleInheritanceManager.recalculateAllAccessesForRole(role);
      OBDal.getInstance().commitAndClose();
      role = OBDal.getInstance().get(Role.class, roleId);
      template = OBDal.getInstance().get(Role.class, templateId);

      WindowAccess wa = BasicADWindowInheritanceTest.getWindowAccessForWindowName(
          role.getADWindowAccessList(), "Sales Order");

      assertThat("There is a new access created with the recalculation", wa, not(equalTo(null)));

      String[] expected = { "true", "true", templateId };
      String inheritedFromid = wa.getInheritedFrom() != null ? wa.getInheritedFrom().getId() : "";
      String[] result = { wa.isEditableField().toString(), wa.isActive().toString(),
          inheritedFromid };
      assertThat("New access recalculated properly", expected, equalTo(result));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(role);
      RoleInheritanceTestUtils.deleteRole(template);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  @Test
  public void testTemplatePermissionsRecalculate() {
    Role template = null;
    Role role1 = null;
    Role role2 = null;
    try {
      OBContext.setAdminMode(true);
      template = RoleInheritanceTestUtils.createRole("template",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateId = (String) DalUtil.getId(template);
      role1 = RoleInheritanceTestUtils.createRole("role1", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String role1Id = (String) DalUtil.getId(role1);
      role2 = RoleInheritanceTestUtils.createRole("role2", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String role2Id = (String) DalUtil.getId(role2);

      // Add inheritance
      RoleInheritanceTestUtils.addInheritance(role1, template, new Long(10));
      RoleInheritanceTestUtils.addInheritance(role2, template, new Long(20));

      // Add permission (it will not be propagated as event handlers will not be fired)
      BasicADWindowInheritanceTest.addWindowAccess(template, "Sales Order", true);

      OBDal.getInstance().commitAndClose();
      template = OBDal.getInstance().get(Role.class, templateId);

      RoleInheritanceManager.recalculateAllAccessesFromTemplate(template);
      OBDal.getInstance().commitAndClose();
      role1 = OBDal.getInstance().get(Role.class, role1Id);
      role2 = OBDal.getInstance().get(Role.class, role2Id);
      template = OBDal.getInstance().get(Role.class, templateId);

      WindowAccess wa = BasicADWindowInheritanceTest.getWindowAccessForWindowName(
          role1.getADWindowAccessList(), "Sales Order");
      assertThat("There is a new access created with the recalculation for role1", wa,
          not(equalTo(null)));

      WindowAccess wa2 = BasicADWindowInheritanceTest.getWindowAccessForWindowName(
          role1.getADWindowAccessList(), "Sales Order");
      assertThat("There is a new access created with the recalculation for role2", wa2,
          not(equalTo(null)));

      String[] expected = { "true", "true", templateId, "true", "true", templateId };
      String inheritedFromid1 = wa.getInheritedFrom() != null ? wa.getInheritedFrom().getId() : "";
      String inheritedFromid2 = wa2.getInheritedFrom() != null ? wa2.getInheritedFrom().getId()
          : "";
      String[] result = { wa.isEditableField().toString(), wa.isActive().toString(),
          inheritedFromid1, wa2.isEditableField().toString(), wa2.isActive().toString(),
          inheritedFromid2 };
      assertThat("New accesses recalculated properly", expected, equalTo(result));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(role1);
      RoleInheritanceTestUtils.deleteRole(role2);
      RoleInheritanceTestUtils.deleteRole(template);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }
}
