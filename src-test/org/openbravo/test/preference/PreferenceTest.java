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
package org.openbravo.test.preference;

import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyConflictException;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.test.base.BaseTest;

public class PreferenceTest extends BaseTest {

  public void testCreatePreference() {
    setSystemAdministratorContext();

    Preferences.setPreferenceValue("testProperty", "testValue", false, null, null, null, null,
        null, null);
    OBDal.getInstance().flush();

    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));

    List<Preference> prefs = qPref.list();
    assertFalse("No property has been set", prefs.isEmpty());
    assertEquals("Property does not contain the expected value", "testValue", prefs.get(0)
        .getSearchKey());
  }

  public void testOverwritePreference() {
    setSystemAdministratorContext();

    Preferences.setPreferenceValue("testProperty", "newValue", false, null, null, null, null, null,
        null);
    OBDal.getInstance().flush();

    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));

    List<Preference> prefs = qPref.list();
    assertFalse("No property has been set", prefs.isEmpty());
    assertEquals("There should be only one property, found:" + prefs.size(), 1, prefs.size());
    assertEquals("Property does not contain the expected value", "newValue", prefs.get(0)
        .getSearchKey());
  }

  public void testSamePropertyDifferentVisibility() {
    setSystemAdministratorContext();

    Role role = OBDal.getInstance().get(Role.class, "1000001"); // Sales

    Preferences.setPreferenceValue("testProperty", "salesValue", false, null, null, null, role,
        null, null);
    OBDal.getInstance().flush();

    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));

    List<Preference> prefs = qPref.list();
    assertEquals("There should be only 2 properties, found:" + prefs.size(), 2, prefs.size());
  }

  public void testPropertyGet() throws PropertyException {
    setSystemAdministratorContext();
    String value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    Role role = OBDal.getInstance().get(Role.class, "1000001"); // Sales
    value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), role, null);
    assertEquals("Not found expected value.", "salesValue", value);
  }

  public void testWindowVisibility() throws PropertyException {
    setSystemAdministratorContext();
    Window window = OBDal.getInstance().get(Window.class, "276"); // Alert window
    Preferences.setPreferenceValue("testProperty", "alertGeneral", false, null, null, null, null,
        window, null);

    Role role = OBDal.getInstance().get(Role.class, "1000001"); // Sales
    Preferences.setPreferenceValue("testProperty", "alertSales", false, null, null, null, role,
        window, null);
    OBDal.getInstance().flush();

    String value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), role, null);
    assertEquals("Not found expected value.", "salesValue", value);

    value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), window);
    assertEquals("Not found expected value.", "alertGeneral", value);

    value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), role, window);
    assertEquals("Not found expected value.", "alertSales", value);
  }

  public void testExceptionNotFound() {
    PropertyException exception = null;
    try {
      Preferences.getPreferenceValue("testNotExists", false, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), null, null);
    } catch (PropertyException e) {
      exception = e;
    }
    assertNotNull("Expected exception PropertyNotFoundException", exception);
    assertTrue("Expected exception PropertyNotFoundException",
        exception instanceof org.openbravo.erpCommon.utility.PropertyNotFoundException);
  }

  public void testConflict() {
    setSystemAdministratorContext();
    Preference newPref = OBProvider.getInstance().get(Preference.class);
    newPref.setPropertyList(false);
    newPref.setAttribute("testProperty");
    newPref.setSearchKey("anotherValue");
    OBDal.getInstance().save(newPref);
    OBDal.getInstance().flush();

    PropertyException exception = null;
    try {
      Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), null, null);
    } catch (PropertyException e) {
      exception = e;
    }
    assertNotNull("Expected exception PropertyConflictException", exception);
    assertTrue("Expected exception PropertyConflictException",
        exception instanceof PropertyConflictException);
  }

  public void testSolvedConflict() throws PropertyException {
    setSystemAdministratorContext();

    // This piece of code doesn't work because of issue #13153
    // OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    // qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));
    // qPref.add(Expression.eq(Preference.PROPERTY_SEARCHKEY, "anotherValue"));
    //
    // Preference newPref = qPref.list().get(0);

    Preference newPref = null;
    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));
    for (Preference p : qPref.list()) {
      if (p.getSearchKey().equals("anotherValue")) {
        newPref = p;
      }
    }
    newPref.setSelected(true);
    OBDal.getInstance().flush();

    String value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "anotherValue", value);

  }

  public void testPreferenceClientOrgSetting() {
    setBigBazaarAdminContext();
    Preference p = Preferences.setPreferenceValue("testProperty2", "testValue", false, null, null,
        null, null, null, null);
    assertEquals("Incorrect Client ID", "0", p.getClient().getId());
    assertEquals("Incorrect Org ID", "0", p.getOrganization().getId());

  }

  public void testClean() {
    setSystemAdministratorContext();
    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.eq(Preference.PROPERTY_ATTRIBUTE, "testProperty"));
    for (Preference pref : qPref.list()) {
      OBDal.getInstance().remove(pref);
    }
  }

}
