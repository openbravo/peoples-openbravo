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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.views;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.GCField;
import org.openbravo.client.application.GCSystem;
import org.openbravo.client.application.GCTab;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.datasource.BaseDataSourceTestDal;

/**
 * Test cases for checking the correct generation of the views, with the different configurations in
 * tab, field and in system level.
 * 
 * @author NaroaIriarte
 *
 */
public class ViewGenerationWithDifferentConfigLevelTest extends BaseDataSourceTestDal {
  private static final String BUSINESS_PARTNER_WINDOW_ID = "_123";
  private static final String CLIENT_FOR_GC_SYSTEM_FIELD_TAB = "0";
  private static final String ZERO_ORGANIZATION = "0";
  private static final String BUSINESS_PARTNER_TAB_ID = "220";
  private static final String BUSINESS_PARTNER_CATEGORY_FIELD_ID = "3955";

  /**
   * Test to ensure that the the view retrieves the expected values, having only grid configuration
   * in System level. In the configuration, the "by default allow filtering" checkbox is checked,
   * so, the expression "canFilter: true" must be found in the view.
   * 
   * @throws Exception
   */
  @Test
  public void gridConfigurationSystemLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCSystem gcsystem = OBProvider.getInstance().get(GCSystem.class);
      gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcsystem.setFilterable(true);
      OBDal.getInstance().save(gcsystem);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gcsystem);
      OBDal.getInstance().commitAndClose();
      boolean existsCanFilter;
      existsCanFilter = theResponse.contains("canFilter: true");
      assertThat(existsCanFilter, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test to ensure that the the view retrieves the expected values, having only grid configuration
   * at tab level. The "allow filtering" property in the grid configuration at tab level, in the
   * Businsess Partner tab has been set to "No". So this test checks that the "canFiler: false"
   * expression is present in the view.
   */
  @Test
  public void gridConfigurationTabLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCTab gctab = OBProvider.getInstance().get(GCTab.class);
      gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gctab.setFilterable("N");
      gctab.setTab(OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID));
      OBDal.getInstance().save(gctab);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gctab);
      OBDal.getInstance().commitAndClose();
      boolean existsCanFilter;
      existsCanFilter = theResponse.contains("canFilter: false");
      assertThat(existsCanFilter, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test to ensure that the the view retrieves the expected values, having grid configuration at
   * System and tab level. The "allow filtering" property in the grid configuration at tab level, in
   * the Business Partner tab has been set to "No". So this test checks that the "canFiler: false"
   * expression is present in the view. Also, the "allow sorting" property has been set to default,
   * so, the taken value is going to be the one set in the grid configuration at system level, which
   * is true. The test checks that the "canSort: true" expression is present in the view.
   */
  @Test
  public void gridConfigurationTabAndSystemLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCSystem gcsystem = OBProvider.getInstance().get(GCSystem.class);
      gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcsystem.setSortable(true);
      OBDal.getInstance().save(gcsystem);
      OBDal.getInstance().commitAndClose();
      GCTab gctab = OBProvider.getInstance().get(GCTab.class);
      gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gctab.setFilterable("N");
      gctab.setTab(OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID));
      OBDal.getInstance().save(gctab);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gctab);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().remove(gcsystem);
      OBDal.getInstance().commitAndClose();
      boolean existsCanFilter, existsCanSort;
      existsCanFilter = theResponse.contains("canFilter: false");
      existsCanSort = theResponse.contains("canSort: true");
      assertThat(existsCanFilter, is(true));
      assertThat(existsCanSort, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test to ensure that the the view retrieves the expected values, having only grid configuration
   * at field level. The Business Partner category field of business Partner has the property allow
   * sorting set to "Yes", so the view must have "canSort: true" expression.
   */
  @Test
  public void gridConfigurationFieldLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCTab gctab = OBProvider.getInstance().get(GCTab.class);
      gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gctab.setTab(OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID));
      OBDal.getInstance().save(gctab);
      OBDal.getInstance().commitAndClose();
      GCField gcfield = OBProvider.getInstance().get(GCField.class);
      gcfield.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcfield.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcfield.setField(OBDal.getInstance().get(Field.class, BUSINESS_PARTNER_CATEGORY_FIELD_ID));
      gcfield.setSortable("Y");
      gcfield.setObuiappGcTab(gctab);
      OBDal.getInstance().save(gcfield);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gcfield);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().remove(gctab);
      OBDal.getInstance().commitAndClose();
      boolean existsCanSort;
      existsCanSort = theResponse.contains("canSort: true");
      assertThat(existsCanSort, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Test to ensure that the the view retrieves the expected values, having grid configuration at
   * field and System level. In the grid configuration at system level, the "by default allow
   * sorting" checkbox is checked. The tests ensures that the expression "canSort: true" is in the
   * view. The business partner category of the business partner tab has the allow filtering
   * property set to "No". The test ensures that the "canFilter: false" expression is in the view.
   */
  @Test
  public void gridConfigurationFieldAndSystemLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCSystem gcsystem = OBProvider.getInstance().get(GCSystem.class);
      gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcsystem.setSortable(true);
      OBDal.getInstance().save(gcsystem);
      OBDal.getInstance().commitAndClose();
      GCTab gctab = OBProvider.getInstance().get(GCTab.class);
      gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gctab.setTab(OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID));
      OBDal.getInstance().save(gctab);
      OBDal.getInstance().commitAndClose();
      GCField gcfield = OBProvider.getInstance().get(GCField.class);
      gcfield.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcfield.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcfield.setField(OBDal.getInstance().get(Field.class, BUSINESS_PARTNER_CATEGORY_FIELD_ID));
      gcfield.setFilterable("N");
      gcfield.setObuiappGcTab(gctab);
      OBDal.getInstance().save(gcfield);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gcfield);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().remove(gctab);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().remove(gcsystem);
      OBDal.getInstance().commitAndClose();
      boolean existsCanFilter, existsCanSort;
      existsCanFilter = theResponse.contains("canFilter: false");
      existsCanSort = theResponse.contains("canSort: true");
      assertThat(existsCanFilter, is(true));
      assertThat(existsCanSort, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Test to ensure that the the view retrieves the expected values, having grid configuration at
   * field and tab level. The field Business Partner category of Business Parter has the property
   * "allow sorting" set to "Yes". The test checks if the "canSort: true" exists in the view. In the
   * tab configuration the allow filtering property is set to no, so the "canFilter: false" must
   * exist in the view.
   */
  @Test
  public void gridConfigurationFieldAndTabLevel() throws Exception {
    OBContext.setAdminMode(false);
    try {
      GCTab gctab = OBProvider.getInstance().get(GCTab.class);
      gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gctab.setFilterable("N");
      gctab.setTab(OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID));
      OBDal.getInstance().save(gctab);
      OBDal.getInstance().commitAndClose();
      GCField gcfield = OBProvider.getInstance().get(GCField.class);
      gcfield.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
      gcfield.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
      gcfield.setField(OBDal.getInstance().get(Field.class, BUSINESS_PARTNER_CATEGORY_FIELD_ID));
      gcfield.setSortable("Y");
      gcfield.setObuiappGcTab(gctab);
      OBDal.getInstance().save(gcfield);
      OBDal.getInstance().commitAndClose();
      String theResponse = getViewResponse();
      OBDal.getInstance().remove(gcfield);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().remove(gctab);
      OBDal.getInstance().commitAndClose();
      boolean existsCanFilter, existsCanSort;
      existsCanFilter = theResponse.contains("canFilter: false");
      existsCanSort = theResponse.contains("canSort: true");
      assertThat(existsCanFilter, is(true));
      assertThat(existsCanSort, is(true));
    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  private String getViewResponse() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    String businessPartnerViewResponse = doRequest(
        "/org.openbravo.client.kernel/OBUIAPP_MainLayout/View?viewId=" + BUSINESS_PARTNER_WINDOW_ID,
        params, 200, "GET");
    return businessPartnerViewResponse;
  }
}