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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link CallStoredProcedure} class.
 * 
 * @author mtaal
 */

public class DalStoredProcedureTest extends BaseTest {
  /**
   * Tests calling database procedures using the dal connection provider
   */
  public void testCallGetTax() throws Exception {
    setUserContext("0");

    final List<Object> parameters = new ArrayList<Object>();
    final List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(OBDal.getInstance().get(Product.class, "1000004"));
    types.add(BaseOBObject.class);
    parameters.add(new Date());
    types.add(Date.class);
    parameters.add(OBDal.getInstance().get(Organization.class, "1000002"));
    types.add(BaseOBObject.class);
    parameters.add(OBDal.getInstance().get(Warehouse.class, "1000000"));
    types.add(BaseOBObject.class);
    parameters.add(OBDal.getInstance().get(Location.class, "1000001"));
    types.add(BaseOBObject.class);
    parameters.add(OBDal.getInstance().get(Location.class, "1000001"));
    types.add(BaseOBObject.class);
    parameters.add(null);
    types.add(BaseOBObject.class);
    parameters.add(true);
    types.add(Boolean.class);

    final String procedureName = "C_GetTax";

    CallStoredProcedure.getInstance().call(procedureName, parameters, types);
  }

  /**
   * Tests calling database procedures using the dal connection provider
   */
  public void testCallDivide() throws Exception {
    setUserContext("0");

    final List<Object> parameters = new ArrayList<Object>();
    final List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(new BigDecimal("10.1"));
    types.add(BigDecimal.class);
    parameters.add(new BigDecimal("2.0"));
    types.add(BigDecimal.class);

    final String procedureName = "C_Divide";

    final Object result = CallStoredProcedure.getInstance().call(procedureName, parameters, types);
    assertTrue(result instanceof BigDecimal);
    final BigDecimal bd = (BigDecimal) result;
    // not a precise check but okay
    assertTrue(bd.toString().startsWith("5.05"));
  }

  /**
   * Tests calling database procedures using the dal connection provider
   */
  public void testCallLastDay() throws Exception {
    setUserContext("0");

    final List<Object> parameters = new ArrayList<Object>();
    final List<Class<?>> types = new ArrayList<Class<?>>();
    parameters.add(new Date());
    types.add(Date.class);

    final String procedureName = "last_day";

    final Object result = CallStoredProcedure.getInstance().call(procedureName, parameters, types);
    assertTrue(result instanceof Date);
  }
}