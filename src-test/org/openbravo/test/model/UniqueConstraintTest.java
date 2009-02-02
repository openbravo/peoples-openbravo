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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.model;

import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.UniqueConstraint;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.geography.Country;
import org.openbravo.test.base.BaseTest;

/**
 * Tests unique constraints
 * 
 * @author mtaal
 */

public class UniqueConstraintTest extends BaseTest {

  public void testUniqueConstraintLoad() {
    final Entity entity = ModelProvider.getInstance().getEntityByTableName("C_Country_Trl");
    assertEquals(1, entity.getUniqueConstraints().size());
    dumpUniqueConstraints();
  }

  public void testUniqueConstraintQuerying() {
    setUserContext("1000001");
    OBContext.getOBContext().setInAdministratorMode(true);
    final List<Country> countries = OBDal.getInstance().createCriteria(Country.class).list();
    assertTrue(countries.size() > 0);
    for (final Country c : countries) {
      // make copy to not interfere with hibernate's auto update mechanism
      final Country copy = (Country) DalUtil.copy(c);
      copy.setId("test");
      final List<BaseOBObject> queried = OBDal.getInstance().findUniqueConstrainedObjects(copy);
      assertEquals(1, queried.size());
      assertEquals(c.getId(), queried.get(0).getId());
    }
  }

  // dump uniqueconstraints
  private void dumpUniqueConstraints() {
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (e.getUniqueConstraints().size() > 0) {
        for (final UniqueConstraint uc : e.getUniqueConstraints()) {
          System.err.println(">>> Entity " + e);
          System.err.println("UniqueConstraint " + uc.getName());
          for (final Property p : uc.getProperties()) {
            System.err.print(p.getName() + " ");
          }
        }
        System.err.println("");
      }
    }
  }

}