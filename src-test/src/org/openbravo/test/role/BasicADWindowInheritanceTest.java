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
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.ui.Window;

public class BasicADWindowInheritanceTest extends WeldBaseTest {
  private final static String BUSINESS_PARTNER_ID = "123";
  private final static String SALES_ORDER_ID = "143";
  private final static String SALES_INVOICE_ID = "167";
  private final static String PURCHASE_ORDER_ID = "181";
  private final static String PURCHASE_INVOICE_ID = "183";

  @Test
  public void testBasicHorizontalInheritance() {
    Role templateSalesRole = null;
    Role templatePurchaseRole = null;
    Role templateMixedRole = null;
    Role inheritedRole = null;
    try {
      OBContext.setAdminMode(true);
      // Create roles
      templateSalesRole = RoleInheritanceTestUtils.createRole("templateSalesRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateSalesRoleId = (String) DalUtil.getId(templateSalesRole);
      templatePurchaseRole = RoleInheritanceTestUtils.createRole("templatePurchaseRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templatePurchaseRoleId = (String) DalUtil.getId(templatePurchaseRole);
      templateMixedRole = RoleInheritanceTestUtils.createRole("templateMixedRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateMixedRoleId = (String) DalUtil.getId(templateMixedRole);
      inheritedRole = RoleInheritanceTestUtils.createRole("inheritedRole",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          false);
      String inheritedRoleId = (String) DalUtil.getId(inheritedRole);

      // Add window accesses for template roles
      addWindowAccess(templateSalesRole, "Sales Invoice", true);
      addWindowAccess(templateSalesRole, "Sales Order", true);
      addWindowAccess(templatePurchaseRole, "Purchase Invoice", true);
      addWindowAccess(templatePurchaseRole, "Purchase Order", true);
      addWindowAccess(templateMixedRole, "Sales Invoice", true);
      addWindowAccess(templateMixedRole, "Purchase Order", true);
      addWindowAccess(templateMixedRole, "Business Partner", true);

      // Save
      RoleInheritanceTestUtils.addInheritance(inheritedRole, templateSalesRole, new Long(10));
      RoleInheritanceTestUtils.addInheritance(inheritedRole, templatePurchaseRole, new Long(20));
      RoleInheritanceTestUtils.addInheritance(inheritedRole, templateMixedRole, new Long(30));
      OBDal.getInstance().commitAndClose();

      inheritedRole = OBDal.getInstance().get(Role.class, inheritedRoleId);
      templateMixedRole = OBDal.getInstance().get(Role.class, templateMixedRoleId);

      String[] expected = { BUSINESS_PARTNER_ID, templateMixedRoleId, PURCHASE_INVOICE_ID,
          templatePurchaseRoleId, PURCHASE_ORDER_ID, templateMixedRoleId, SALES_INVOICE_ID,
          templateMixedRoleId, SALES_ORDER_ID, templateSalesRoleId };
      String[] result = getWindowAccessesOrderedByWindowName(inheritedRole);
      assertThat("Window accesses have been inherited", result, equalTo(expected));

      // Delete
      RoleInheritanceTestUtils.removeInheritance(inheritedRole, templateMixedRole);
      OBDal.getInstance().commitAndClose();

      templateSalesRole = OBDal.getInstance().get(Role.class, templateSalesRoleId);
      templatePurchaseRole = OBDal.getInstance().get(Role.class, templatePurchaseRoleId);
      templateMixedRole = OBDal.getInstance().get(Role.class, templateMixedRoleId);
      inheritedRole = OBDal.getInstance().get(Role.class, inheritedRoleId);

      String[] expected2 = { PURCHASE_INVOICE_ID, templatePurchaseRoleId, PURCHASE_ORDER_ID,
          templatePurchaseRoleId, SALES_INVOICE_ID, templateSalesRoleId, SALES_ORDER_ID,
          templateSalesRoleId };
      result = getWindowAccessesOrderedByWindowName(inheritedRole);
      assertThat("Window accesses have been removed", result, equalTo(expected2));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(inheritedRole);
      RoleInheritanceTestUtils.deleteRole(templateSalesRole);
      RoleInheritanceTestUtils.deleteRole(templatePurchaseRole);
      RoleInheritanceTestUtils.deleteRole(templateMixedRole);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  @Test
  public void testBasicVerticalInheritance() {
    Role roleA = null;
    Role roleB = null;
    Role roleC = null;
    try {
      OBContext.setAdminMode(true);
      // Create roles
      roleA = RoleInheritanceTestUtils.createRole("roleA", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, true);
      String roleAId = (String) DalUtil.getId(roleA);
      roleB = RoleInheritanceTestUtils.createRole("roleB", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, true);
      String roleBId = (String) DalUtil.getId(roleB);
      roleC = RoleInheritanceTestUtils.createRole("roleC", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String roleCId = (String) DalUtil.getId(roleC);

      // Add window accesses for template roles
      addWindowAccess(roleA, "Sales Order", true);
      addWindowAccess(roleB, "Purchase Order", true);

      // Add inheritances
      RoleInheritanceTestUtils.addInheritance(roleB, roleA, new Long(10));
      RoleInheritanceTestUtils.addInheritance(roleC, roleB, new Long(20));

      OBDal.getInstance().commitAndClose();

      roleA = OBDal.getInstance().get(Role.class, roleAId);
      roleB = OBDal.getInstance().get(Role.class, roleBId);
      roleC = OBDal.getInstance().get(Role.class, roleCId);

      String[] expected = { PURCHASE_ORDER_ID, "", SALES_ORDER_ID, roleAId };
      String[] result = getWindowAccessesOrderedByWindowName(roleB);
      assertThat("Window accesses inherited for role B ", result, equalTo(expected));

      String[] expected2 = { PURCHASE_ORDER_ID, roleBId, SALES_ORDER_ID, roleBId };
      result = getWindowAccessesOrderedByWindowName(roleC);
      assertThat("Window accesses inherited for role C ", result, equalTo(expected2));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(roleC);
      RoleInheritanceTestUtils.deleteRole(roleB);
      RoleInheritanceTestUtils.deleteRole(roleA);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  @Test
  public void testBasicAccessPropagation() {
    Role role = null;
    Role template = null;
    try {
      OBContext.setAdminMode(true);
      // Create roles
      role = RoleInheritanceTestUtils.createRole("role", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String roleId = (String) DalUtil.getId(role);
      template = RoleInheritanceTestUtils.createRole("template",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String templateId = (String) DalUtil.getId(template);

      // Add inheritance
      RoleInheritanceTestUtils.addInheritance(role, template, new Long(10));

      OBDal.getInstance().commitAndClose();
      role = OBDal.getInstance().get(Role.class, roleId);
      template = OBDal.getInstance().get(Role.class, templateId);

      // Add window access
      addWindowAccess(template, "Sales Invoice", true);
      addWindowAccess(role, "Sales Order", true);

      String[] expected = { SALES_INVOICE_ID, templateId, SALES_ORDER_ID, "" };
      String[] result = getWindowAccessesOrderedByWindowName(role);
      assertThat("New window access has been propagated", result, equalTo(expected));

      // Perform an update in the window access of the parent
      updateWindowAccess(template, "Sales Invoice", false, false);
      OBDal.getInstance().commitAndClose();

      role = OBDal.getInstance().get(Role.class, roleId);
      template = OBDal.getInstance().get(Role.class, templateId);

      WindowAccess wa = getWindowAccessForWindowName(role.getADWindowAccessList(), "Sales Invoice");
      String[] expected2 = { "false", "false", templateId };
      String[] result2 = { wa.isEditableField().toString(), wa.isActive().toString(),
          wa.getInheritedFrom().getId() };
      assertThat("Updated window access has been propagated", result2, equalTo(expected2));

      WindowAccess wa2 = getWindowAccessForWindowName(role.getADWindowAccessList(), "Sales Order");
      String[] expected3 = { "true", "true", "" };
      String inheritedFromid = wa2.getInheritedFrom() != null ? wa2.getInheritedFrom().getId() : "";
      String[] result3 = { wa2.isEditableField().toString(), wa2.isActive().toString(),
          inheritedFromid };
      assertThat("Non inherited access remains unchanged after propagation", result3,
          equalTo(expected3));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(role);
      RoleInheritanceTestUtils.deleteRole(template);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  private void addWindowAccess(Role role, String windowName, boolean editableField) {
    final WindowAccess windowAccess = OBProvider.getInstance().get(WindowAccess.class);
    final OBCriteria<Window> obCriteria = OBDal.getInstance().createCriteria(Window.class);
    obCriteria.add(Restrictions.eq(Window.PROPERTY_NAME, windowName));
    obCriteria.setMaxResults(1);
    windowAccess.setClient(role.getClient());
    windowAccess.setOrganization(role.getOrganization());
    windowAccess.setRole(role);
    windowAccess.setWindow((Window) obCriteria.uniqueResult());
    windowAccess.setEditableField(editableField);
    OBDal.getInstance().save(windowAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private void updateWindowAccess(Role role, String windowName, boolean editableField,
      boolean isActive) {
    final OBCriteria<Window> windowCriteria = OBDal.getInstance().createCriteria(Window.class);
    windowCriteria.add(Restrictions.eq(Window.PROPERTY_NAME, windowName));
    windowCriteria.setMaxResults(1);
    final OBCriteria<WindowAccess> windowAccessCriteria = OBDal.getInstance().createCriteria(
        WindowAccess.class);
    windowAccessCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, role));
    windowAccessCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_WINDOW,
        (Window) windowCriteria.uniqueResult()));
    windowAccessCriteria.setMaxResults(1);
    WindowAccess wa = (WindowAccess) windowAccessCriteria.uniqueResult();
    wa.setEditableField(editableField);
    wa.setActive(isActive);
  }

  private String[] getWindowAccessesOrderedByWindowName(Role role) {
    final OBCriteria<WindowAccess> obCriteria = OBDal.getInstance().createCriteria(
        WindowAccess.class);
    obCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(WindowAccess.PROPERTY_WINDOW + "." + Window.PROPERTY_NAME, true);
    List<WindowAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (WindowAccess wa : list) {
      result[i] = (String) DalUtil.getId(wa.getWindow());
      result[i + 1] = wa.getInheritedFrom() != null ? (String) DalUtil.getId(wa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private WindowAccess getWindowAccessForWindowName(List<WindowAccess> list, String windowName) {
    for (WindowAccess wa : list) {
      if (windowName.equals(wa.getWindow().getName())) {
        return wa;
      }
    }
    return null;
  }
}
