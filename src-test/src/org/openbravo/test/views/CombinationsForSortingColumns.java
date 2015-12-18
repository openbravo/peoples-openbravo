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
 * Test cases to check the correct behavior of the canSort property , with the different grid
 * configurations in tab, field and in system level. And with different configurations at column
 * level.
 * 
 * @author NaroaIriarte
 *
 */
@RunWith(Parameterized.class)
public class CombinationsForSortingColumns extends OBBaseTest {
  private static final String CLIENT_FOR_GC_SYSTEM_FIELD_TAB = "0";
  private static final String ZERO_ORGANIZATION = "0";
  private static final String BUSINESS_PARTNER_TAB_ID = "220";
  private static final String BUSINESS_PARTNER_CATEGORY_FIELD_ID = "3955";
  private static final String CAN_SORT_FALSE = "\"canSort\":false";
  private static final String CAN_SORT_TRUE = "\"canSort\":true";
  private static final String YES = "Y";
  private static final String NO = "N";
  private static final String DEFAULT = "D";
  private static final long SEQUENCE = 10;

  private String gcFieldSortingConfig;
  private String gcTabSortingConfig;
  private Boolean gcSystemSortingConfig;;
  private boolean columnSortingConfig;
  private String expectedResult;

  public CombinationsForSortingColumns(Boolean gcSystemSortingConfig, String gcTabSortingConfig,
      String gcFieldSortingConfig, boolean columnSortingConfig, String expectedResult) {

    this.gcSystemSortingConfig = gcSystemSortingConfig;
    this.gcTabSortingConfig = gcTabSortingConfig;
    this.gcFieldSortingConfig = gcFieldSortingConfig;
    this.columnSortingConfig = columnSortingConfig;
    this.expectedResult = expectedResult;
  }

  @Parameters
  public static Collection<Object[]> data() {
    HashMap<String, Object> valuesMap = new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;

      {
        put("SystemCanSort", true);
        put("TabCanSort", YES);
        put("FieldCanSort", YES);
        put("ColumnCanSort", true);
        put("SystemCannotSort", false);
        put("TabCannotSort", NO);
        put("FieldCannotSort", NO);
        put("ColumnCannotSort", false);
        put("SystemConfigNull", null);
        put("TabConfigNull", null);
        put("FieldConfigNull", null);
        put("TabConfigDefault", DEFAULT);
        put("FieldConfigDefault", DEFAULT);
      }
    };

    return Arrays
        .asList(new Object[][] {
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCannotSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldCanSort"), valuesMap.get("ColumnCannotSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigDefault"), valuesMap.get("ColumnCannotSort"),
                CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigNull"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCannotSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemCanSort"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabConfigDefault"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_TRUE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCanSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCanSort"), CAN_SORT_FALSE },
            { valuesMap.get("SystemConfigNull"), valuesMap.get("TabCannotSort"),
                valuesMap.get("FieldConfigNull"), valuesMap.get("ColumnCannotSort"), CAN_SORT_FALSE } });
  }

  @Test
  public void testIsFieldSortable() {
    assertThat("Grid configuration at field level:",
        CombinationsForSortingColumns.computeResultForField(gcSystemSortingConfig,
            gcTabSortingConfig, gcFieldSortingConfig, columnSortingConfig),
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
      field.getColumn().setAllowsorting(columnConfig);
      if (gcSystemConfig != null) {
        gcsystem = OBProvider.getInstance().get(GCSystem.class);
        gcsystem.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gcsystem.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gcsystem.setSortable(gcSystemConfig);
        gcsystem.setSeqno(SEQUENCE);
        OBDal.getInstance().save(gcsystem);
        OBDal.getInstance().flush();
      }
      if (gcTabConfig != null) {
        gctab = OBProvider.getInstance().get(GCTab.class);
        gctab.setClient(OBDal.getInstance().get(Client.class, CLIENT_FOR_GC_SYSTEM_FIELD_TAB));
        gctab.setOrganization(OBDal.getInstance().get(Organization.class, ZERO_ORGANIZATION));
        gctab.setSortable(gcTabConfig);
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
        gcfield.setSortable(gcFieldConfig);
        gctab.getOBUIAPPGCFieldList().add(gcfield);
        OBDal.getInstance().save(gcfield);
        field.getColumn().setAllowsorting(columnConfig);
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