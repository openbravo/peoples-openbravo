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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.test.base.BaseTest;

/**
 * Tests registered classes in Application Dictionary
 * 
 * @author iperdomo
 */
public class ClassLoaderTest extends BaseTest {

  private static final Logger log = Logger.getLogger(ClassLoaderTest.class);

  /**
   * Test if all registered classes in Application Dictionary can be loaded. Consistency test to
   * have a clean web.xml
   */
  public void testModelObject() {

    final List<String> notFoundClasses = new ArrayList<String>();

    setSystemAdministratorContext();

    OBCriteria<ModelImplementation> obc = OBDal.getInstance().createCriteria(
        ModelImplementation.class);

    // "S" - "Servlet"
    // "C" - "ContextParam"
    // "L" - "Listener"
    // "ST" - "Session timeout"
    // "F" - "Filter"
    // "R" - "Resource"

    final String[] in = { "S", "L", "F" };

    obc.add(Expression.in(ModelImplementation.PROPERTY_OBJECTTYPE, in));

    for (ModelImplementation mi : obc.list()) {
      try {

        if (mi.getId().equals("801180")) {
          // Ugly hack!!!
          // Check issue
          // https://issues.openbravo.com/view.php?id=12429
          continue;
        }

        // Testing if the defined class can be loaded
        ClassLoader.getSystemClassLoader().loadClass(mi.getJavaClassName());

      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }
    if (notFoundClasses.size() > 0) {
      for (String nf : notFoundClasses) {
        log.error(nf);
      }
    }
    assertEquals(0, notFoundClasses.size());
  }
}
