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

  /**
   * Test an import of data in its own organization/client. This should not result in an update or
   * insert.
   */
  public void testImportNoUpdate() {
    setBigBazaarAdminContext();

    final String xml = exportTax();
    final Client c = OBDal.getInstance().get(Client.class, "1000000");
    final Organization o = OBDal.getInstance().get(Organization.class, "1000000");
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(c, o, xml);

    log.debug("WARNING>>>>");
    assertTrue(ir.getWarningMessages(), ir.getWarningMessages() == null);
    assertEquals(0, ir.getUpdatedObjects().size());
    assertEquals(0, ir.getInsertedObjects().size());
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  private String exportTax() {
    final OBCriteria<?> obc = OBDal.getInstance().createCriteria(TaxRate.class);

    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeChildren(true);
    exc.setOptionIncludeReferenced(true);
    exc.setAddSystemAttributes(false);

    @SuppressWarnings("unchecked")
    final List<BaseOBObject> list = (List<BaseOBObject>) obc.list();
    final String xml = exc.toXML(list);
    log.debug(xml);
    return xml;
  }

  /**
   * Export {@link Greeting} from 1000000 and import in 1000001.
   */
  public void test1Greeting() {
    cleanRefDataLoaded();
    setBigBazaarUserContext();
    addReadWriteAccess(Greeting.class);
    final int cnt = count(Greeting.class);
    addReadWriteAccess(Greeting.class);
    final String xml = getXML(Greeting.class);
    // insert in org 1000001
    setUserContext("1000019");
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, "1000001"),
        OBDal.getInstance().get(Organization.class, "1000001"), xml);
    assertEquals(cnt, ir.getInsertedObjects().size());
    assertEquals(0, ir.getUpdatedObjects().size());
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  /**
   * Test that a repeat of the action of @ #test1Greeting()} is done without updating/inserting an
   * object.
   */
  public void test2Greeting() {
    setBigBazaarUserContext();
    addReadWriteAccess(Greeting.class);
    final String xml = getXML(Greeting.class);
    setUserContext("1000019");
    // insert in org 1000002
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, "1000001"),
        OBDal.getInstance().get(Organization.class, "1000001"), xml);
    assertEquals(0, ir.getInsertedObjects().size());
    assertEquals(0, ir.getUpdatedObjects().size());
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  /**
   * Tests reads the {@link Greeting} objects from 1000001, changes something and then imports
   * again. The result should be two updates.
   */
  public void test3Greeting() {
    setUserContext("1000019");
    String xml = getXML(Greeting.class);
    xml = xml.replaceAll("Mrs", "Mrsses");
    xml = xml.replaceAll("Herr", "Heer");
    xml = xml.replaceAll("Heer", "Her");
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, "1000001"),
        OBDal.getInstance().get(Organization.class, "1000001"), xml);
    assertEquals(0, ir.getInsertedObjects().size());
    assertEquals(2, ir.getUpdatedObjects().size());
    if (ir.hasErrorOccured()) {
      fail(ir.getErrorMessages());
    }
  }

  /**
   * Remove the test data from 1000001.
   */
  public void test4Greeting() {
    setUserContext("1000019");
    final Organization org = OBDal.getInstance().get(Organization.class, "1000001");
    final OBCriteria<Greeting> obc = OBDal.getInstance().createCriteria(Greeting.class);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Expression.eq(PROPERTY_ORGANIZATION, org));
    // assertEquals(7, obc.list().size());
    for (final Greeting g : obc.list()) {
      OBDal.getInstance().remove(g);
    }
  }

  /**
   * Checks that the testdata was indeed removed.
   */
  public void test5Greeting() {
    setUserContext("1000019");
    final Organization org = OBDal.getInstance().get(Organization.class, "1000000");
    final OBCriteria<Greeting> obc = OBDal.getInstance().createCriteria(Greeting.class);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableClients(false);
    obc.add(Expression.eq(PROPERTY_ORGANIZATION, org));
    assertEquals(0, obc.list().size());
  }

  /**
   * Same test as before exporting and then importing in same organization.
   */
  public void test6Greeting() {
    doTestNoChange(Greeting.class);
  }

  // do it again, no change!
  private <T extends BaseOBObject> void doTestNoChange(Class<T> clz) {
    setBigBazaarUserContext();
    addReadWriteAccess(Greeting.class);
    final String xml = getXML(clz);
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), xml);
    assertTrue(ir.getInsertedObjects().size() == 0);
    assertTrue(ir.getUpdatedObjects().size() == 0);
  }
}