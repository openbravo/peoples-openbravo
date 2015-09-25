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
  public final static String BUSINESS_PARTNER_ID = "123";
  public final static String SALES_ORDER_ID = "143";
  public final static String SALES_INVOICE_ID = "167";
  public final static String PURCHASE_ORDER_ID = "181";
  public final static String PURCHASE_INVOICE_ID = "183";

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

  @Test
  public void testBasicAccessDeletePropagation() {
    Role role = null;
    Role template1 = null;
    Role template2 = null;
    Role template3 = null;
    try {
      OBContext.setAdminMode(true);
      // Create roles
      role = RoleInheritanceTestUtils.createRole("role", RoleInheritanceTestUtils.CLIENT_ID,
          RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true, false);
      String roleId = (String) DalUtil.getId(role);
      template1 = RoleInheritanceTestUtils.createRole("template1",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template1Id = (String) DalUtil.getId(template1);
      template2 = RoleInheritanceTestUtils.createRole("template2",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template2Id = (String) DalUtil.getId(template2);
      template3 = RoleInheritanceTestUtils.createRole("template3",
          RoleInheritanceTestUtils.CLIENT_ID, RoleInheritanceTestUtils.ASTERISK_ORG_ID, " C", true,
          true);
      String template3Id = (String) DalUtil.getId(template3);

      // Add window accesses
      addWindowAccess(template1, "Sales Invoice", true);
      addWindowAccess(template1, "Sales Order", true);
      addWindowAccess(template2, "Sales Invoice", true);
      addWindowAccess(template3, "Sales Invoice", true);

      // Add inheritances
      RoleInheritanceTestUtils.addInheritance(role, template1, new Long(10));
      RoleInheritanceTestUtils.addInheritance(role, template2, new Long(20));
      RoleInheritanceTestUtils.addInheritance(role, template3, new Long(30));
      OBDal.getInstance().commitAndClose();

      String[] expected = { SALES_INVOICE_ID, template3Id, SALES_ORDER_ID, template1Id };
      String[] result = getWindowAccessesOrderedByWindowName(role);
      assertThat("Inherited access created properly", result, equalTo(expected));

      // Remove window access for template 3
      template3 = OBDal.getInstance().get(Role.class, template3Id);
      removeWindowAccess(template3, "Sales Invoice");
      OBDal.getInstance().commitAndClose();

      String[] expected2 = { SALES_INVOICE_ID, template2Id, SALES_ORDER_ID, template1Id };
      String[] result2 = getWindowAccessesOrderedByWindowName(role);
      assertThat("Inherited access updated properly after first removal", result2,
          equalTo(expected2));

      // Remove window access for template 2
      template2 = OBDal.getInstance().get(Role.class, template2Id);
      removeWindowAccess(template2, "Sales Invoice");
      OBDal.getInstance().commitAndClose();

      String[] expected3 = { SALES_INVOICE_ID, template1Id, SALES_ORDER_ID, template1Id };
      String[] result3 = getWindowAccessesOrderedByWindowName(role);
      assertThat("Inherited access updated properly after second removal", result3,
          equalTo(expected3));
      OBDal.getInstance().commitAndClose();

      // Remove window access for template 1
      template1 = OBDal.getInstance().get(Role.class, template1Id);
      removeWindowAccess(template1, "Sales Invoice");
      OBDal.getInstance().commitAndClose();

      role = OBDal.getInstance().get(Role.class, roleId);
      template1 = OBDal.getInstance().get(Role.class, template1Id);
      template2 = OBDal.getInstance().get(Role.class, template2Id);
      template3 = OBDal.getInstance().get(Role.class, template3Id);

      String[] expected4 = { SALES_ORDER_ID, template1Id };
      String[] result4 = getWindowAccessesOrderedByWindowName(role);
      assertThat("Inherited access updated properly after third removal", result4,
          equalTo(expected4));

    } finally {
      // Delete roles
      RoleInheritanceTestUtils.deleteRole(role);
      RoleInheritanceTestUtils.deleteRole(template1);
      RoleInheritanceTestUtils.deleteRole(template2);
      RoleInheritanceTestUtils.deleteRole(template3);

      OBDal.getInstance().commitAndClose();

      OBContext.restorePreviousMode();
    }
  }

  public static void addWindowAccess(Role role, String windowName, boolean editableField) {
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

  public static void removeWindowAccess(Role role, String windowName) {
    WindowAccess wa = getWindowAccessForWindowName(role.getADWindowAccessList(), windowName);
    wa.setInheritedFrom(null);
    role.getADWindowAccessList().remove(wa);
    OBDal.getInstance().remove(wa);
  }

  public static void updateWindowAccess(Role role, String windowName, boolean editableField,
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

  public static WindowAccess getWindowAccessForWindowName(List<WindowAccess> list, String windowName) {
    for (WindowAccess wa : list) {
      if (windowName.equals(wa.getWindow().getName())) {
        return wa;
      }
    }
    return null;
  }
}
