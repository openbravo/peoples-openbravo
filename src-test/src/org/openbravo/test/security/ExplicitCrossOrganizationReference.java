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
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.security;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;

/**
 * Test cases covering special cases for cross organization references, where they are allowed based
 * on ad_column.allowed_cross_org_link setting.
 * 
 * @author alostale
 *
 */
public class ExplicitCrossOrganizationReference extends CrossOrganizationReference {
  private static String QA_ONLY_SPAIN_ROLE;
  private static final String CORE = "0";
  private static final String ORDER_WAREHOUSE_COLUMN = "2202";
  private static final String ORDERLINE_ORDER_COLUMN = "2213";
  private static boolean wasCoreInDev;

  /**
   * References from org Spain to USA should not be allowed on insertion even in a column allowing
   * it if not in admin mode
   */
  @Test
  @Ignore("Expected exception is not thrown on insert, see issue #32063")
  public void shouldBeIllegalOnInsert() {
    createOrder(SPAIN_ORG, USA_WAREHOUSE);

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  /**
   * References from org Spain to USA should not be allowed on update even in a column allowing it
   * if not in admin mode
   */
  @Test
  public void shouldBeIllegalOnUpdate() {
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  /**
   * References from org Spain to USA should be allowed on insertion if in cross org admin mode for
   * columns that allow it
   */
  @Test
  public void shouldBeAllowedOnInsertInCrossOrgAdminMode() {
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      createOrder(SPAIN_ORG, USA_WAREHOUSE);

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  /**
   * References from org Spain to USA should not be allowed on update if in cross org admin mode for
   * columns that allow it
   */
  @Test
  public void shouldBeAllowedOnUpdateInCrossOrgAdminMode() {
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  /**
   * For columns not flagged to allow cross org refs, it should not be possible to do it even in
   * cross org admin mode
   */
  @SuppressWarnings("serial")
  @Test
  @Ignore("Expected exception is not thrown on insert, see issue #32063")
  public void shouldBeIllegalOnInsertAdminModeIfColumnNotSet() {
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      createOrder(SPAIN_ORG, new HashMap<String, Object>() {
        {
          put(Order.PROPERTY_BUSINESSPARTNER,
              OBDal.getInstance().getProxy(BusinessPartner.class, USA_BP));
        }
      });

      exception.expect(OBSecurityException.class);

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  /**
   * For columns not flagged to allow cross org refs, it should not be possible to do it even in
   * cross org admin mode
   */
  @Test
  public void shouldBeIllegalOnUpdateAdminModeIfColumnNotSet() {
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      exception.expect(OBSecurityException.class);

      order.setBusinessPartner(OBDal.getInstance().getProxy(BusinessPartner.class, USA_BP));
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  @Test
  @Ignore("Expected exception is not thrown on insert, see issue #32063")
  public void shouldBeIllegalOnChildInsert() {
    createCrossOrgOrderOrderLine();

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  @Test
  public void shouldBeIllegalOnChildUpdate() {
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    OrderLine ol = createOrderLine(order);

    ol.setOrganization(OBDal.getInstance().getProxy(Organization.class, USA_ORG));

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  @Test
  public void shouldBeAllowedOnChildInsertInOrgAdminMode() {
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      createCrossOrgOrderOrderLine();

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  @Test
  public void shouldBeAllowedOnChildUpdateInOrgAdminMode() {
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
      OrderLine ol = createOrderLine(order);

      ol.setOrganization(OBDal.getInstance().getProxy(Organization.class, USA_ORG));

      // warehouse needs to be modified as line-warehouse is not cross-org
      ol.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  /**
   * Fetching children (order.getOrderLineList) should retrieve cross-org elements if role has
   * access to children org
   */
  @Test
  public void childListShouldBeRetrivedIfRoleHasAccess() {
    OBContext.setCrossOrgReferenceAdminMode();
    Order order;
    try {
      order = createCrossOrgOrderOrderLine();
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }

    OBDal.getInstance().refresh(order); // force fetch
    assertThat("Number of lines in order", order.getOrderLineList(), hasSize(1));
  }

  /**
   * When fetching children elements (order.getOrderLineList), children's organization is not
   * checked, so even they are cross-org and the role has no access to them, they are present in the
   * bag.
   */
  @Test
  public void childListShouldBeRetrivedEvenIfRoleHasNoAccess() {
    OBContext.setCrossOrgReferenceAdminMode();
    Order order;
    try {
      order = createCrossOrgOrderOrderLine();
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }

    setSpainQARole();

    OBDal.getInstance().refresh(order); // force fetch
    assertThat("Number of lines in order", order.getOrderLineList(), hasSize(1));
  }

  @Test
  public void byDefaultCrossOrgAdminShouldBeDisabled() {
    assertThat("isCrossOrgAdministratorMode", OBContext.getOBContext()
        .isCrossOrgAdministratorMode(), is(false));
  }

  @Test
  public void crossOrgAdminModeShoudBePossibleToEnable() {
    OBContext.setCrossOrgReferenceAdminMode();

    assertThat("isCrossOrgAdministratorMode", OBContext.getOBContext()
        .isCrossOrgAdministratorMode(), is(true));

    OBContext.restorePreviousCrossOrgReferenceMode();
  }

  @Test
  public void adminAndCrossOrgAdminAreIndpendent() {
    setTestLogAppenderLevel(Level.WARN);
    OBContext.setAdminMode();
    assertThat("admin mode", OBContext.getOBContext().isInAdministratorMode(), is(true));
    assertThat("cross org admin mode", OBContext.getOBContext().isCrossOrgAdministratorMode(),
        is(false));

    OBContext.setCrossOrgReferenceAdminMode();
    assertThat("admin mode", OBContext.getOBContext().isInAdministratorMode(), is(true));
    assertThat("cross org admin mode", OBContext.getOBContext().isCrossOrgAdministratorMode(),
        is(true));

    OBContext.restorePreviousMode();
    assertThat("admin mode", OBContext.getOBContext().isInAdministratorMode(), is(false));
    assertThat("cross org admin mode", OBContext.getOBContext().isCrossOrgAdministratorMode(),
        is(true));

    OBContext.restorePreviousCrossOrgReferenceMode();
    assertThat("admin mode", OBContext.getOBContext().isInAdministratorMode(), is(false));
    assertThat("cross org admin mode", OBContext.getOBContext().isCrossOrgAdministratorMode(),
        is(false));
    assertThat(getTestLogAppender().getMessages(Level.WARN), hasSize(0));
  }

  @Test
  public void unbalancedRestorePreviousCrossOrgAdminShouldLogWarn() {
    setTestLogAppenderLevel(Level.WARN);

    OBContext.setCrossOrgReferenceAdminMode();
    OBContext.restorePreviousCrossOrgReferenceMode();
    assertThat(getTestLogAppender().getMessages(Level.WARN), hasSize(0));

    OBContext.restorePreviousCrossOrgReferenceMode();
    assertThat(getTestLogAppender().getMessages(Level.WARN),
        hasItem(containsString("Unbalanced calls to setCrossOrgReferenceAdminMode")));
  }

  @Test
  public void unbalancedOrgAdminThreadFinalizationShouldLogWarn() throws NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    setTestLogAppenderLevel(Level.WARN);
    OBContext.setCrossOrgReferenceAdminMode();

    // OBContext.clearAdminModeStack is invoked on request thread finalization, invoke it here
    // directly making it accessible first
    Method clearAdminModeStack = OBContext.class.getDeclaredMethod("clearAdminModeStack");
    clearAdminModeStack.setAccessible(true);
    clearAdminModeStack.invoke(null);

    assertThat(getTestLogAppender().getMessages(Level.WARN),
        hasItem(containsString("Unbalanced calls to setCrossOrgReferenceAdminMode")));
  }

  @SuppressWarnings("serial")
  private Order createCrossOrgOrderOrderLine() {
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    createOrderLine(order, new HashMap<String, Object>() {
      {
        put(OrderLine.PROPERTY_ORGANIZATION, OBDal.getInstance().get(Organization.class, USA_ORG));
      }
    });
    return order;
  }

  private void setSpainQARole() {
    OBContext.setOBContext("100", QA_ONLY_SPAIN_ROLE, QA_TEST_CLIENT_ID, SPAIN_ORG);
  }

  @BeforeClass
  public static void setUpAllowedCrossOrg() throws Exception {
    // allow cross org references in order.warehouse and in orderline.order
    OBContext.setOBContext("0");
    Module core = OBDal.getInstance().get(Module.class, CORE);
    wasCoreInDev = core.isInDevelopment();
    if (!wasCoreInDev) {
      core.setInDevelopment(true);
    }

    Column orderWarehouse = OBDal.getInstance().get(Column.class, ORDER_WAREHOUSE_COLUMN);
    orderWarehouse.setAllowedCrossOrganizationReference(true);

    Column orderLineOrder = OBDal.getInstance().get(Column.class, ORDERLINE_ORDER_COLUMN);
    orderLineOrder.setAllowedCrossOrganizationReference(true);

    OBDal.getInstance().commitAndClose();

    // reload in memory model with these new settings
    DalLayerInitializer.getInstance().setInitialized(false);
    setDalUp();

    // crate a role with access only to Spain org
    setQAAdminRole();
    Role spainRole = OBProvider.getInstance().get(Role.class);
    spainRole.setName("QA Only Spain - " + System.currentTimeMillis()); // some randomness
    spainRole.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
    spainRole.setManual(true);
    spainRole.setClientList("-");
    spainRole.setOrganizationList("-");
    spainRole.setUserLevel("  O");
    OBDal.getInstance().save(spainRole);
    createdObjects.add(spainRole);
    QA_ONLY_SPAIN_ROLE = spainRole.getId();

    RoleOrganization orgAccess = OBProvider.getInstance().get(RoleOrganization.class);
    orgAccess.setOrganization(OBDal.getInstance().getProxy(Organization.class, SPAIN_ORG));
    orgAccess.setRole(spainRole);
    OBDal.getInstance().save(orgAccess);
    createdObjects.add(orgAccess);

    UserRoles userRole = OBProvider.getInstance().get(UserRoles.class);
    userRole.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
    userRole.setRole(spainRole);
    userRole.setUserContact(OBDal.getInstance().getProxy(User.class, "100"));
    OBDal.getInstance().save(userRole);
    createdObjects.add(userRole);

    OBDal.getInstance().commitAndClose();
  }

  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext("0");
    if (!wasCoreInDev) {
      Module core = OBDal.getInstance().get(Module.class, CORE);
      core.setInDevelopment(false);
    }

    Column orderWarehouse = OBDal.getInstance().get(Column.class, ORDER_WAREHOUSE_COLUMN);
    orderWarehouse.setAllowedCrossOrganizationReference(false);

    Column orderLineOrder = OBDal.getInstance().get(Column.class, ORDERLINE_ORDER_COLUMN);
    orderLineOrder.setAllowedCrossOrganizationReference(false);

    OBDal.getInstance().commitAndClose();
  }

}
