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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;
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

  public void testPLPropertyGet() throws SQLException {
    setSystemAdministratorContext();
    String value = getPLPreference("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    Role role = OBDal.getInstance().get(Role.class, "1000001"); // Sales
    value = getPLPreference("testProperty", false, OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
        role, null);
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

  public void testPLWindowVisibility() throws SQLException {
    setSystemAdministratorContext();
    Window window = OBDal.getInstance().get(Window.class, "276"); // Alert window
    Preferences.setPreferenceValue("testProperty", "alertGeneral", false, null, null, null, null,
        window, null);

    Role role = OBDal.getInstance().get(Role.class, "1000001"); // Sales
    String value = getPLPreference("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    value = getPLPreference("testProperty", false, OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
        role, null);
    assertEquals("Not found expected value.", "salesValue", value);

    value = getPLPreference("testProperty", false, OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
        OBContext.getOBContext().getRole(), window);
    assertEquals("Not found expected value.", "alertGeneral", value);

    value = getPLPreference("testProperty", false, OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
        role, window);
    assertEquals("Not found expected value.", "alertSales", value);
  }

  public void testOrgVisibility() throws PropertyException {
    setSystemAdministratorContext();
    Client client = OBDal.getInstance().get(Client.class, "1000000");
    Organization orgB = OBDal.getInstance().get(Organization.class, "1000005");
    Organization orgB1 = OBDal.getInstance().get(Organization.class, "1000006");
    Organization orgB2 = OBDal.getInstance().get(Organization.class, "1000007");
    Organization orgB11 = OBDal.getInstance().get(Organization.class, "1000008");
    Organization orgB12 = OBDal.getInstance().get(Organization.class, "1000009");

    Preference p = Preferences.setPreferenceValue("testProperty", "B", false, null, orgB, null,
        null, null, null);
    Preferences
        .setPreferenceValue("testProperty", "B2", false, null, orgB2, null, null, null, null);
    Preferences.setPreferenceValue("testProperty", "B12", false, null, orgB12, null, null, null,
        null);
    OBDal.getInstance().flush();

    assertEquals("Preference not set in the expected visible org", orgB, p
        .getVisibleAtOrganization());

    String value = Preferences.getPreferenceValue("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    value = Preferences.getPreferenceValue("testProperty", false, client, orgB, OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = Preferences.getPreferenceValue("testProperty", false, client, orgB1, OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = Preferences.getPreferenceValue("testProperty", false, client, orgB2, OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B2", value);

    value = Preferences.getPreferenceValue("testProperty", false, client, orgB11, OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = Preferences.getPreferenceValue("testProperty", false, client, orgB12, OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B12", value);

  }

  public void testPLOrgVisibility() throws SQLException {

    setSystemAdministratorContext();
    Client client = OBDal.getInstance().get(Client.class, "1000000");
    Organization orgB = OBDal.getInstance().get(Organization.class, "1000005");
    Organization orgB1 = OBDal.getInstance().get(Organization.class, "1000006");
    Organization orgB2 = OBDal.getInstance().get(Organization.class, "1000007");
    Organization orgB11 = OBDal.getInstance().get(Organization.class, "1000008");
    Organization orgB12 = OBDal.getInstance().get(Organization.class, "1000009");

    String value = getPLPreference("testProperty", false, OBContext.getOBContext()
        .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
        .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "newValue", value);

    value = getPLPreference("testProperty", false, client, orgB,
        OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = getPLPreference("testProperty", false, client, orgB1, OBContext.getOBContext()
        .getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = getPLPreference("testProperty", false, client, orgB2, OBContext.getOBContext()
        .getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B2", value);

    value = getPLPreference("testProperty", false, client, orgB11, OBContext.getOBContext()
        .getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B", value);

    value = getPLPreference("testProperty", false, client, orgB12, OBContext.getOBContext()
        .getUser(), OBContext.getOBContext().getRole(), null);
    assertEquals("Not found expected value.", "B12", value);

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

  public void testPLExceptionNotFound() {
    SQLException exception = null;
    try {
      getPLPreference("testNotExists", false, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          null, null);
    } catch (SQLException e) {
      exception = e;
    }
    assertNotNull("Expected exception PropertyNotFoundException", exception);
    assertTrue("Expected exception PropertyNotFoundException, found: " + exception.getMessage(),
        exception.getMessage().contains("@PropertyNotFound@"));
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

  public void testPLConflict() {
    SQLException exception = null;
    try {
      getPLPreference("testProperty", false, OBContext.getOBContext().getCurrentClient(), OBContext
          .getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(), null, null);
    } catch (SQLException e) {
      exception = e;
    }
    assertNotNull("Expected exception PropertyConflictException", exception);
    assertTrue("Expected exception PropertyConflictException, found: " + exception.getMessage(),
        exception.getMessage().contains("@PropertyConflict@"));
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

  public void testPLSolvedConflict() throws SQLException {
    String value = getPLPreference("testProperty", false, OBContext.getOBContext()
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

  public void testPreferenceListSetAndGet() throws PropertyException {
    setSystemAdministratorContext();

    // Property configuration list
    Reference refProperties = OBDal.getInstance().get(Reference.class,
        "A26BA480E2014707B47257024C3CBFF7");
    org.openbravo.model.ad.domain.List listValue = OBProvider.getInstance().get(
        org.openbravo.model.ad.domain.List.class);
    listValue.setReference(refProperties);
    listValue.setName("test Property List");
    listValue.setSearchKey("testPropertyList");
    OBDal.getInstance().save(listValue);
    OBDal.getInstance().flush();

    Preference pref = Preferences.setPreferenceValue("testPropertyList", "testPropValue", true,
        null, null, null, null, null, null);
    OBDal.getInstance().flush();

    assertTrue("Pref type is not properly set", pref.isPropertyList());

    String value = Preferences.getPreferenceValue("testPropertyList", true, OBContext
        .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
        OBContext.getOBContext().getUser(), null, null);
    assertEquals("Not found expected value.", "testPropValue", value);
  }

  public void testClean() {
    setSystemAdministratorContext();
    OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);
    qPref.add(Expression.like(Preference.PROPERTY_ATTRIBUTE, "testProperty%"));
    for (Preference pref : qPref.list()) {
      OBDal.getInstance().remove(pref);
    }
  }

  private String getPLPreference(String property, boolean isListProperty, Client client,
      Organization org, User user, Role role, Window window) throws SQLException {
    ResultSet result = null;
    PreparedStatement st = null;
    try {
      Connection conn = OBDal.getInstance().getConnection();
      st = conn.prepareStatement("select ad_get_preference_value(?, ?, ?, ?, ?, ?, ?) from dual ");
      st.setString(1, property);
      st.setString(2, isListProperty ? "Y" : "N");
      st.setString(3, client == null ? null : client.getId());
      st.setString(4, org == null ? null : org.getId());
      st.setString(5, user == null ? null : user.getId());
      st.setString(6, role == null ? null : role.getId());
      st.setString(7, window == null ? null : window.getId());
      result = st.executeQuery();
      String rt = null;
      if (result.next()) {
        rt = result.getString(1);
      }
      return rt;
    } finally {
      if (result != null) {
        result.close();
      }
      if (st != null) {
        st.close();
      }
    }
  }

}
