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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.Greeting;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test various issues reported with XML import/export in mantis.
 * 
 * @author mtaal
 */

public class EntityXMLIssues extends XMLBaseTest {

  /**
   * Checks mantis issue 6212, issue text: When inserting reference data using DAL into ad_client 0
   * it should not generate new uuids but maintain the current ids but it is doing so.
   */
  public void _testMantis6212() {
    cleanRefDataLoaded();
    setErrorOccured(true);
    setUserContext("1000001");

    final List<Greeting> gs = getList(Greeting.class);

    // only do one greeting
    final Greeting greeting = (Greeting) DalUtil.copy(gs.get(0));
    final String id = "" + System.currentTimeMillis();
    greeting.setId(id);
    final List<Greeting> newGs = new ArrayList<Greeting>();
    newGs.add(greeting);
    final String xml = getXML(newGs);

    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, "1000000"),
        OBDal.getInstance().get(Organization.class, "1000000"), xml);

    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    }

    assertEquals(1, ir.getInsertedObjects().size());
    assertTrue(ir.getWarningMessages() == null);
    final BaseOBObject bob = ir.getInsertedObjects().get(0);
    assertEquals(id, bob.getId());
    System.err.println(id);
    setErrorOccured(false);
  }

  /**
   * Checks mantis issue 6213, issue text: When exporting/importing reference data for char columns
   * dal trims the blank spaces so in case the column contains only blank spaces it is treated as
   * null.
   */
  public void testMantis6213() {
    final String spaces = "   ";
    cleanRefDataLoaded();
    setErrorOccured(true);
    setUserContext("1000001");

    // update all greetings to have a name with spaces
    {
      final List<Greeting> gs = getList(Greeting.class);
      for (final Greeting g : gs) {
        g.setGreetingName(spaces);
      }
      OBDal.getInstance().commitAndClose();
    }

    final List<Greeting> gs = getList(Greeting.class);
    for (final Greeting g : gs) {
      assertEquals(spaces, g.getGreetingName());
    }

    // only do one greeting
    final Greeting greeting = (Greeting) DalUtil.copy(gs.get(0));
    final List<Greeting> newGs = new ArrayList<Greeting>();
    final String id = "" + System.currentTimeMillis();
    greeting.setId(id);
    newGs.add(greeting);
    final String xml = getXML(newGs);
    assertTrue(xml.indexOf("<greetingName>   </greetingName>") != -1);

    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, "1000000"),
        OBDal.getInstance().get(Organization.class, "1000000"), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    }

    assertEquals(1, ir.getInsertedObjects().size());
    assertTrue(ir.getWarningMessages() == null);
    final BaseOBObject bob = ir.getInsertedObjects().get(0);
    assertEquals(id, bob.getId());

    OBDal.getInstance().commitAndClose();

    // now reread the greeting and check that the space is still there
    final Greeting newGreeting = OBDal.getInstance().get(Greeting.class, id);
    assertTrue(greeting != newGreeting);
    assertEquals(spaces, newGreeting.getGreetingName());
    setErrorOccured(false);
  }

  public <T extends BaseOBObject> List<T> getList(Class<T> clz) {
    setErrorOccured(true);
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    return obc.list();
  }

  public <T extends BaseOBObject> String getXML(List<T> objs) {
    setErrorOccured(true);
    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeReferenced(true);
    // exc.setOptionEmbedChildren(true);
    // exc.setOptionIncludeChildren(true);
    exc.setAddSystemAttributes(false);
    return exc.toXML(new ArrayList<BaseOBObject>(objs));
  }

  public <T extends BaseOBObject> String getXML(Class<T> clz) {
    setErrorOccured(true);
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeReferenced(true);
    // exc.setOptionEmbedChildren(true);
    // exc.setOptionIncludeChildren(true);
    exc.setAddSystemAttributes(false);
    return exc.toXML(new ArrayList<BaseOBObject>(obc.list()));
  }
}