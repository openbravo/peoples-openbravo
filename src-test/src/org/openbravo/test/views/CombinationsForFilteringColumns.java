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
import java.util.HashMap;

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
  private static final String YES = "Y";
  private static final String NO = "N";
  private static final String DEFAULT = "D";
  private static final long SEQUENCE = 10;

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
    HashMap<String, Object> valuesMap = new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;

      {
        put("SystemCanFilter", true);
        put("TabCanFilter", YES);
        put("FieldCanFilter", YES);
        put("ColumnCanFilter", true);
        put("SystemCannotFilter", false);
        put("TabCannotFilter", NO);
        put("FieldCannotFilter", NO);
        put("ColumnCannotFilter", false);
        put("SystemConfigNull", null);
        put("TabConfigNull", null);
        put("FieldConfigNull", null);
        put("TabConfigDefault", DEFAULT);
        put("FieldConfigDefault", DEFAULT);
      }
    };

    return Arrays
        .asList(new Object[][] {
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCannotFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldCanFilter"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCannotFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemCanFilter"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"), CAN_FILTER_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanFilter"),
                CAN_FILTER_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotFilter"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotFilter"),
                CAN_FILTER_FALSE } });
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
      field.getColumn().setAllowFiltering(columnConfig);
      if (gcSystemConfig != null) {
        gcsystem = OBProvider.getInstance().get(GCSystem.class);
        gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gcsystem.setFilterable(gcSystemConfig);
        gcsystem.setSeqno(SEQUENCE);
        OBDal.getInstance().save(gcsystem);
        OBDal.getInstance().flush();
      }
      if (gcTabConfig != null) {
        gctab = OBProvider.getInstance().get(GCTab.class);
        gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gctab.setFilterable(gcTabConfig);
        gctab.setSeqno(SEQUENCE);
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