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
 * All portions are Copyright (C) 2009-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.modularity;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDBPrefix;
import org.openbravo.test.base.BaseTest;

/**
 * This tests check that db prefixes are correctly checked when they are inserted in DB in order not
 * to follow modularity rules
 * 
 * IMPORTANT: Test cases are called by one of them called testContent(). The name of the rest of the
 * test cases NOT begin by "test...".
 */
public class DBPrefixTest extends BaseTest {

  /**
   * This test contains the invocations for the rest of the test cases. By this way, we preserve the
   * execution order of the test cases.
   */
  public void testContent() {
    createModule();
    addDBPrefixValid1();
    addDBPrefixValid2();
    addDBPrefixNotValid1();
    addDBPrefixNotValid2();
    addDBPrefixNotValid3();
    addDBPrefixNotValid4();
    deleteModule();

  }

  /**
   * Creates a new module to test with
   */
  public void createModule() {
    setSystemAdministratorContext();
    Module module = OBProvider.getInstance().get(Module.class);
    module.setName("Test-dbprefixes-names");
    module.setJavaPackage("org.openbravo.test.dbprefix");
    module.setVersion("1.0.0");
    module.setDescription("Testing dbprefixes");
    module.setInDevelopment(true);
    OBDal.getInstance().save(module);
    OBDal.getInstance().flush();
  }

  /**
   * Add a valid dbprefixes, everything should go ok only alphabetic upper chars
   */
  public void addDBPrefixValid1() {
    insertDBPrefix("OK", true);
  }

  /**
   * alpha numeric chars not starting with a numeric one
   */
  public void addDBPrefixValid2() {
    insertDBPrefix("OK12", true);
  }

  /**
   * Add not valid db prefixes starts with number
   */
  public void addDBPrefixNotValid1() {
    insertDBPrefix("1FAIL", false);
  }

  /**
   * contains lower case letters
   */
  public void addDBPrefixNotValid2() {
    insertDBPrefix("Fail", false);
  }

  /**
   * contains underscore
   */
  public void addDBPrefixNotValid3() {
    insertDBPrefix("FAIL_1", false);
  }

  /**
   * contains other non-alphabetic chars
   */
  public void addDBPrefixNotValid4() {
    insertDBPrefix("FAIL&/1", false);
  }

  /**
   * Deletes all the modules matching the name for the testing one
   */
  public void deleteModule() {
    setSystemAdministratorContext();
    final OBCriteria<Module> obCriteria = OBDal.getInstance().createCriteria(Module.class);
    obCriteria.add(Restrictions.eq(Module.PROPERTY_JAVAPACKAGE, "org.openbravo.test.dbprefix"));
    final List<Module> modules = obCriteria.list();
    for (Module mod : modules) {
      System.out.println("Removing module: " + mod.getName());
      OBDal.getInstance().remove(mod);
    }
  }

  // Obtains the module inserted for testing purposes
  private Module getModule() {
    setSystemAdministratorContext();
    final OBCriteria<Module> obCriteria = OBDal.getInstance().createCriteria(Module.class);
    obCriteria.add(Restrictions.eq(Module.PROPERTY_JAVAPACKAGE, "org.openbravo.test.dbprefix"));
    final List<Module> modules = obCriteria.list();
    assertEquals("Not a single module obtained", 1, modules.size());
    return modules.get(0);
  }

  // Tries to insert a valid or not valid and check it was inserted (if valid)
  // or not inserted (if not valid
  private void insertDBPrefix(String name, boolean isValid) {
    setSystemAdministratorContext();
    Module mod = getModule();
    ModuleDBPrefix dbPrefix = OBProvider.getInstance().get(ModuleDBPrefix.class);

    dbPrefix.setModule(mod);
    dbPrefix.setName(name);

    OBDal.getInstance().save(dbPrefix);

    boolean exception = false;
    try {
      // force dal commit to throw exception
      OBDal.getInstance().flush();
      OBDal.getInstance().remove(dbPrefix);
      OBDal.getInstance().commitAndClose();
      // thrown when using pgsql
    } catch (org.hibernate.exception.GenericJDBCException e) {
      exception = true;
      OBDal.getInstance().rollbackAndClose();
      // thrown when using oracle
    } catch (org.hibernate.QueryTimeoutException e) {
      exception = true;
      OBDal.getInstance().rollbackAndClose();
    }

    if (isValid)
      assertFalse("Not inserted a valid prefix:" + name, exception);
    else
      assertTrue("Inserted a non-valid prefix:" + name, exception);
    OBDal.getInstance().flush();
  }
}
