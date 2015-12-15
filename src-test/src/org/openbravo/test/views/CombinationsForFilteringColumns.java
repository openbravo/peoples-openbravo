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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.GCField;
import org.openbravo.client.application.GCSystem;
import org.openbravo.client.application.GCTab;
import org.openbravo.client.application.window.OBViewUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test cases to check the correct behavior of the canFilter property , with the different grid
 * configurations in tab, field and in system level. And with different configurations at column
 * level.
 * 
 * @author NaroaIriarte
 *
 */
@RunWith(Parameterized.class)
public class CombinationsForFilteringColumns extends OBBaseTest {
  private static final String CLIENT_FOR_GC_SYSTEM_FIELD_TAB = "0";
  private static final String ZERO_ORGANIZATION = "0";
  private static final String BUSINESS_PARTNER_TAB_ID = "220";
  private static final String BUSINESS_PARTNER_CATEGORY_FIELD_ID = "3955";
  private static final String CAN_FILTER_FALSE = "\"canFilter\":false";
  private static final String CAN_FILTER_TRUE = "\"canFilter\":true";

  private String gcFieldFilteringConfig;
  private String gcTabFilteringConfig;
  private Boolean gcSystemFilteringConfig;;
  private boolean columnFilteringConfig;
  private String expectedResult;

  public CombinationsForFilteringColumns(Boolean gcSystemFilteringConfig,
      String gcTabFilteringConfig, String gcFieldFilteringConfig, boolean columnFilteringConfig,
      String expectedResult) {
    this.gcSystemFilteringConfig = gcSystemFilteringConfig;
    this.gcTabFilteringConfig = gcTabFilteringConfig;
    this.gcFieldFilteringConfig = gcFieldFilteringConfig;
    this.columnFilteringConfig = columnFilteringConfig;
    this.expectedResult = expectedResult;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { { false, "N", "N", false, CAN_FILTER_FALSE },
        { false, "N", "N", true, CAN_FILTER_FALSE }, { false, "N", "Y", false, CAN_FILTER_TRUE },
        { false, "N", "Y", true, CAN_FILTER_TRUE }, { false, "Y", "N", false, CAN_FILTER_FALSE },
        { false, "Y", "N", true, CAN_FILTER_FALSE }, { false, "Y", "Y", false, CAN_FILTER_TRUE },
        { false, "Y", "Y", true, CAN_FILTER_TRUE }, { true, "N", "N", false, CAN_FILTER_FALSE },
        { true, "N", "N", true, CAN_FILTER_FALSE }, { true, "N", "Y", false, CAN_FILTER_TRUE },
        { true, "N", "Y", true, CAN_FILTER_TRUE }, { true, "Y", "N", false, CAN_FILTER_FALSE },
        { true, "Y", "N", true, CAN_FILTER_FALSE }, { true, "Y", "Y", false, CAN_FILTER_TRUE },
        { true, "Y", "Y", true, CAN_FILTER_TRUE }, { false, "N", "D", false, CAN_FILTER_FALSE },
        { false, "N", "D", true, CAN_FILTER_FALSE }, { false, "Y", "D", true, CAN_FILTER_TRUE },
        { false, "Y", "D", false, CAN_FILTER_FALSE }, { true, "N", "D", true, CAN_FILTER_FALSE },
        { true, "N", "D", false, CAN_FILTER_FALSE }, { true, "Y", "D", true, CAN_FILTER_TRUE },
        { true, "Y", "D", false, CAN_FILTER_FALSE }, { false, "D", "D", true, CAN_FILTER_FALSE },
        { false, "D", "D", false, CAN_FILTER_FALSE }, { true, "D", "D", false, CAN_FILTER_FALSE },
        { true, "D", "D", true, CAN_FILTER_TRUE }, { null, null, null, true, CAN_FILTER_TRUE },
        { null, null, null, false, CAN_FILTER_FALSE }, { null, "D", "Y", true, CAN_FILTER_TRUE },
        { null, "D", "Y", false, CAN_FILTER_TRUE }, { null, "D", "N", true, CAN_FILTER_FALSE },
        { null, "D", "N", false, CAN_FILTER_FALSE }, { null, "Y", "N", true, CAN_FILTER_FALSE },
        { null, "Y", "N", false, CAN_FILTER_FALSE }, { null, "Y", "Y", true, CAN_FILTER_TRUE },
        { null, "Y", "Y", false, CAN_FILTER_TRUE }, { null, "N", "N", true, CAN_FILTER_FALSE },
        { null, "N", "N", false, CAN_FILTER_FALSE }, { null, "N", "Y", true, CAN_FILTER_TRUE },
        { null, "N", "Y", false, CAN_FILTER_TRUE }, { null, "Y", "D", true, CAN_FILTER_TRUE },
        { null, "Y", "D", false, CAN_FILTER_FALSE }, { null, "N", "D", true, CAN_FILTER_FALSE },
        { null, "N", "D", false, CAN_FILTER_FALSE }, { true, null, null, true, CAN_FILTER_TRUE },
        { true, null, null, false, CAN_FILTER_FALSE },
        { false, null, null, true, CAN_FILTER_FALSE },
        { false, null, null, false, CAN_FILTER_FALSE },
        { false, "D", null, true, CAN_FILTER_FALSE },
        { false, "D", null, false, CAN_FILTER_FALSE }, { false, "Y", null, true, CAN_FILTER_TRUE },
        { false, "Y", null, false, CAN_FILTER_FALSE },
        { false, "N", null, true, CAN_FILTER_FALSE },
        { false, "N", null, false, CAN_FILTER_FALSE }, { true, "D", null, true, CAN_FILTER_TRUE },
        { true, "D", null, false, CAN_FILTER_FALSE }, { true, "Y", null, true, CAN_FILTER_TRUE },
        { true, "Y", null, false, CAN_FILTER_FALSE }, { true, "N", null, true, CAN_FILTER_FALSE },
        { true, "N", null, false, CAN_FILTER_FALSE }, { null, "D", null, true, CAN_FILTER_TRUE },
        { null, "D", null, false, CAN_FILTER_FALSE }, { null, "Y", null, true, CAN_FILTER_TRUE },
        { null, "Y", null, false, CAN_FILTER_FALSE }, { null, "N", null, true, CAN_FILTER_FALSE },
        { null, "N", null, false, CAN_FILTER_FALSE } });
  }

  @Test
  public void testIsFieldFilterable() {
    assertThat("Grid configuration at field level:",
        CombinationsForFilteringColumns.computeResultForField(gcSystemFilteringConfig,
            gcTabFilteringConfig, gcFieldFilteringConfig, columnFilteringConfig),
        containsString(expectedResult));
  }

  private static String computeResultForField(Boolean gcSystemConfig, String gcTabConfig,
      String gcFieldConfig, Boolean columnConfig) {
    JSONObject fieldConfig;
    GCSystem gcsystem = null;
    GCTab gctab = null;
    GCField gcfield = null;
    OBContext.setAdminMode(false);
    try {
      Field field = OBDal.getInstance().get(Field.class, BUSINESS_PARTNER_CATEGORY_FIELD_ID);
      field.getColumn().setAllowfiltering(columnConfig);
      if (gcSystemConfig != null) {
        gcsystem = OBProvider.getInstance().get(GCSystem.class);
        gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gcsystem.setFilterable(gcSystemConfig);
        OBDal.getInstance().save(gcsystem);
        OBDal.getInstance().flush();
      }
      if (gcTabConfig != null) {
        gctab = OBProvider.getInstance().get(GCTab.class);
        gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gctab.setFilterable(gcTabConfig);
        Tab tab = OBDal.getInstance().get(Tab.class, BUSINESS_PARTNER_TAB_ID);
        tab.getOBUIAPPGCTabList().add(gctab);
        OBDal.getInstance().save(gctab);
      }
      if (gcFieldConfig != null) {
        field = OBDal.getInstance().get(Field.class, BUSINESS_PARTNER_CATEGORY_FIELD_ID);
        gcfield = OBProvider.getInstance().get(GCField.class);
        gcfield.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gcfield.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gcfield.setField(field);
        gcfield.setFilterable(gcFieldConfig);
        gctab.getOBUIAPPGCFieldList().add(gcfield);
        OBDal.getInstance().save(gcfield);
      }
      fieldConfig = OBViewUtil.getGridConfigurationSettings(OBDal.getInstance().get(Field.class,
          BUSINESS_PARTNER_CATEGORY_FIELD_ID));
      return fieldConfig.toString();
    } finally {
      OBDal.getInstance().rollbackAndClose();
      OBContext.restorePreviousMode();
    }
  }
}