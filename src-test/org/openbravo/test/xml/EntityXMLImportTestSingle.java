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
 * All portions are Copyright (C) 2008 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.xml;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.Greeting;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data, different scenarios in which data is re-imported (no update should occur),
 * or small changes are made and an update should occur.
 * 
 * @author mtaal
 */

public class EntityXMLImportTestSingle extends XMLBaseTest {

  private static final Logger log = Logger.getLogger(EntityXMLImportTestSingle.class);

  // non-final on purpose
  private static int DATA_SET_SIZE = 20;

  /**
   * Test an import of data in its own organization/client. This should not result in an update or
   * insert.
   */
  public void testImportNoUpdate() {
  return;
  }

  private String exportTax() {
  return "";
  }

  /**
   * Export {@link Greeting} from one org and import in the other
   */
  public void test1Greeting() {
  return;
  }

  /**
   * Test that a repeat of the action of @ #test1Greeting()} is done without updating/inserting an
   * object.
   */
  public void test2Greeting() {
  return;
  }

  /**
   * Tests reads the {@link Greeting} objects from the QA_TEST_ORG_ID, changes something and then
   * imports again. The result should be twenty updates.
   */
  public void test3Greeting() {
  return;
  }

  /**
   * Remove the test data from QA_TEST_ORG_ID.
   */
  public void test4Greeting() {
  return;
  }

  /**
   * Checks that the testdata was indeed removed.
   */
  public void test5Greeting() {
  return;
  }

  /**
   * Same test as before exporting and then importing in same organization.
   */
  public void test6Greeting() {
  return;
  }

  // do it again, no change!
  private <T extends BaseOBObject> void doTestNoChange(Class<T> clz) {
  return;
  }

  private void createTestData() {
  return;
  }
}
