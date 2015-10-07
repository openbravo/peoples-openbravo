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
package org.openbravo.test.role.inheritance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;

/**
 * This class contains some tests to check the restrictions of the Role Inheritance functionality
 */
public class RoleInheritanceRestrictionsTest extends WeldBaseTest {

  /**
   * Test case to check that is not possible to inherit directly for the same role more than once
   */
  @Test
  public void testUniqueRoleInheritance() {
    Role inherited = null;
    Role template = null;
    try {
      OBContext.setAdminMode(true);
      inherited = RoleInheritanceTestUtils.createRole("testRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          false);
      String inheritedId = (String) DalUtil.getId(inherited);
      template = RoleInheritanceTestUtils.createRole("testTemplateRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateId = (String) DalUtil.getId(template);

      // Add inheritances
      RoleInheritanceTestUtils.addInheritance(inherited, template, new Long(10));
      OBDal.getInstance().commitAndClose();
      inherited = OBDal.getInstance().get(Role.class, inheritedId);
      template = OBDal.getInstance().get(Role.class, templateId);
      try {
        RoleInheritanceTestUtils.addInheritance(inherited, template, new Long(20));
        OBDal.getInstance().commitAndClose();
      } catch (Exception ex) {
        OBDal.getInstance().rollbackAndClose();
      }
      inherited = OBDal.getInstance().get(Role.class, inheritedId);
      template = OBDal.getInstance().get(Role.class, templateId);
      assertThat("Inherit From not duplicated in Role Inheritance",
          inherited.getADRoleInheritanceList(), hasSize(1));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(inherited);
      RoleInheritanceTestUtils.deleteRole(template);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test case to check cycle definition on inheritance
   */
  @Test
  public void testCycles() {
    Role template1 = null;
    Role template2 = null;
    try {
      OBContext.setAdminMode(true);
      template1 = RoleInheritanceTestUtils.createRole("template1",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template1Id = (String) DalUtil.getId(template1);
      template2 = RoleInheritanceTestUtils.createRole("template2",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template2Id = (String) DalUtil.getId(template2);

      // Add inheritances
      RoleInheritanceTestUtils.addInheritance(template2, template1, new Long(10));
      OBDal.getInstance().commitAndClose();
      template1 = OBDal.getInstance().get(Role.class, template1Id);
      template2 = OBDal.getInstance().get(Role.class, template2Id);
      try {
        RoleInheritanceTestUtils.addInheritance(template1, template2, new Long(10));
        OBDal.getInstance().commitAndClose();
      } catch (Exception ex) {
        OBDal.getInstance().rollbackAndClose();
      }
      template1 = OBDal.getInstance().get(Role.class, template1Id);
      template2 = OBDal.getInstance().get(Role.class, template2Id);
      assertThat("Template 1 does not have inheritances", template1.getADRoleInheritanceList(),
          hasSize(0));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.removeInheritance(template2, template1);
      RoleInheritanceTestUtils.deleteRole(template1);
      RoleInheritanceTestUtils.deleteRole(template2);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test case to check that is not possible to deactivate the template flag for a template in use
   */
  @Test
  public void testUncheckTemplateInUse() {
    Role template = null;
    Role role = null;
    try {
      OBContext.setAdminMode(true);
      template = RoleInheritanceTestUtils.createRole("template",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateId = (String) DalUtil.getId(template);
      role = RoleInheritanceTestUtils.createRole("role", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String roleId = (String) DalUtil.getId(role);

      // Add inheritance
      RoleInheritanceTestUtils.addInheritance(role, template, new Long(10));
      OBDal.getInstance().commitAndClose();

      template = OBDal.getInstance().get(Role.class, templateId);
      role = OBDal.getInstance().get(Role.class, roleId);

      try {
        template.setTemplate(false);
        OBDal.getInstance().commitAndClose();
      } catch (Exception ex) {
        // Expected exception, the AD_ROLE_TRG avoids this save
      }

      template = OBDal.getInstance().get(Role.class, templateId);
      role = OBDal.getInstance().get(Role.class, roleId);

      assertThat("A template role in use can not be set as non template", template.isTemplate(),
          equalTo(true));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(role);
      RoleInheritanceTestUtils.deleteRole(template);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test case to check that is not possible to define a template role as automatic
   */
  @Test
  public void testTemplateRoleNotAutomatic() {
    Role template = null;
    Role template2 = null;
    try {
      OBContext.setAdminMode(true);
      // Try to create an automatic template
      template = RoleInheritanceTestUtils.createRole("template",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C",
          false, true);
      String templateId = (String) DalUtil.getId(template);
      try {
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      } catch (Exception ex) {
        // Expected exception, the AD_ROLE_TEMPLATE_ISMANUAL_CHK constraint avoids this save
        OBDal.getInstance().rollbackAndClose();
      }
      template = OBDal.getInstance().get(Role.class, templateId);
      assertThat("A template role can not be automatic", template, equalTo(null));

      // Create a manual template
      template2 = RoleInheritanceTestUtils.createRole("template2",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template2Id = (String) DalUtil.getId(template2);
      OBDal.getInstance().commitAndClose();
      try {
        template2 = OBDal.getInstance().get(Role.class, template2Id);
        template2.setManual(false);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      } catch (Exception ex) {
        // Expected exception, the AD_ROLE_TEMPLATE_ISMANUAL_CHK constraint avoids this update
        OBDal.getInstance().rollbackAndClose();
      }
      template2 = OBDal.getInstance().get(Role.class, template2Id);
      assertThat("Is not possible to uncheck the Manual flag for a template role",
          template2.isManual(), equalTo(true));

    } finally {
      // Delete roles (if exists)
      if (template != null) {
        RoleInheritanceTestUtils.deleteRole(template);
      }
      if (template2 != null) {
        RoleInheritanceTestUtils.deleteRole(template2);
      }
      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }
}
