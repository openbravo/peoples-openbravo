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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;

/**
 * Test cases covering special cases for cross organization references, where they are allowed based
 * on ad_column.allowed_cross_org_link setting.
 * 
 * @author alostale
 *
 */
public class ExplicitCrossOrganizationReference extends CrossOrganizationReference {
  private static final String CORE = "0";
  private static final String ORDER_WAREHOUSE = "2202";
  private static boolean wasCoreInDev;

  /** References from org Spain to USA should not be allowed on insertion */
  @Test
  @Ignore("Expected exception is not thrown on isert, see issue #32063")
  public void crossOrgRefShouldBeIllegalOnInsert() {
    setTestAdminContext();
    createOrder(SPAIN, USA_WAREHOUSE);

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  /** References from org Spain to USA should not be allowed on update */
  @Test
  public void crossOrgRefShouldBeIllegalOnUpdate() {
    setTestAdminContext();
    Order order = createOrder(SPAIN, SPAIN_WAREHOUSE);
    order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  /** References from org Spain to USA should be allowed on insertion if in cross org admin mode */
  @Test
  public void crossOrgRefShouldBeAllowedOnInsertInCrossOrgAdminMode() {
    setTestAdminContext();
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      createOrder(SPAIN, USA_WAREHOUSE);

      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousCrossOrgReferenceMode();
    }
  }

  /** References from org Spain to USA should not be allowed on update if in cross org admin mode */
  @Test
  public void crossOrgRefShouldBeAllowedOnUpdateInCrossOrgAdminMode() {
    setTestAdminContext();
    Order order = createOrder(SPAIN, SPAIN_WAREHOUSE);
    OBContext.setCrossOrgReferenceAdminMode();
    try {
      order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

      exception.expect(OBSecurityException.class);

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
    if (wasCoreInDev) {
      core.setInDevelopment(true);
    }

    Column orderWarehouse = OBDal.getInstance().get(Column.class, ORDER_WAREHOUSE);
    orderWarehouse.setAllowedCrossOrganizationReference(true);

    OBDal.getInstance().commitAndClose();

    DalLayerInitializer.getInstance().setInitialized(false);
    setDalUp();
  }

}
