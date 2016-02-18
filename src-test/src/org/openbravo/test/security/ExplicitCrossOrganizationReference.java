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

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
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
    setTestAdminContext();
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
    setTestAdminContext();
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
    setTestAdminContext();
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
    setTestAdminContext();
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
    setTestAdminContext();
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
    setTestAdminContext();
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

  @SuppressWarnings("serial")
  @Test
  @Ignore("Expected exception is not thrown on insert, see issue #32063")
  public void shouldBeIllegalOnChildInsert() {
    setTestAdminContext();
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    createOrderLine(order, new HashMap<String, Object>() {
      {
        put(OrderLine.PROPERTY_ORGANIZATION, OBDal.getInstance().get(Organization.class, USA_ORG));
      }
    });

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  @Test
  public void shouldBeIllegalOnChildUpdate() {
    setTestAdminContext();
    Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
    OrderLine ol = createOrderLine(order);

    ol.setOrganization(OBDal.getInstance().getProxy(Organization.class, USA_ORG));

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  @SuppressWarnings("serial")
  @Test
  public void shouldBeAllowedOnChildInsertInOrgAdminMode() {
    setTestAdminContext();
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      Order order = createOrder(SPAIN_ORG, SPAIN_WAREHOUSE);
      createOrderLine(order, new HashMap<String, Object>() {
        {
          put(OrderLine.PROPERTY_ORGANIZATION, OBDal.getInstance().get(Organization.class, USA_ORG));
        }
      });

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  @Test
  public void shouldBeAllowedOnChildUpdateInOrgAdminMode() {
    setTestAdminContext();
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

  @BeforeClass
  public static void setUpAllowedCrossOrg() throws Exception {
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

    DalLayerInitializer.getInstance().setInitialized(false);
    setDalUp();
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
