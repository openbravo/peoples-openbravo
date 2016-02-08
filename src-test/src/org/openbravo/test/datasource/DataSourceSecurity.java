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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.json.JsonConstants;

/**
 * Test cases to ensure that mechanism of security DataSource access is working properly.
 *
 * @author inigo.sanchez
 *
 */
@RunWith(Parameterized.class)
public class DataSourceSecurity extends BaseDataSourceTestDal {
  private static final String ASTERISK_ORG_ID = "0";
  private static final String CONTEXT_USER = "100";
  private static final String LANGUAGE_ID = "192";
  private static final String WAREHOUSE_ID = "B2D40D8A5D644DD89E329DC297309055";
  private static final String ROLE_INTERNATIONAL_ADMIN = "42D0EEB1C66F497A90DD526DC597E6F0";
  private static final String ROLE_NO_ACCESS = "1";
  private static final String ROLE_SYSTEM_ADMIN = "0";
  private static final String ESP_ORG = "E443A31992CB4635AFCAEABE7183CE85";

  private static final String TABLE_WINDOWS_TABS_FIELDS_ID = "105";
  private static final String RECORD_OF_WINDOWS_TABS_FIELDS_ID = "283";
  private static final String PRODUCT_TEST = "11";

  private RoleType role;
  private DataSource dataSource;
  private int expectedResponseStatus;

  private enum RoleType {
    ADMIN_ROLE(ROLE_INTERNATIONAL_ADMIN, ESP_ORG), //
    NO_ACCESS_ROLE(ROLE_NO_ACCESS, ESP_ORG), //
    SYSTEM_ROLE(ROLE_SYSTEM_ADMIN, ASTERISK_ORG_ID);

    private String roleId;
    private String orgId;

    private RoleType(String roleId, String orgId) {
      this.roleId = roleId;
      this.orgId = orgId;
    }
  }

  @SuppressWarnings("serial")
  private enum DataSource {
    Order("Order"), //
    Alert("DB9F062472294F12A0291A7BD203F922"), //
    ProductByPriceAndWarehouse("ProductByPriceAndWarehouse", new HashMap<String, String>() {
      {
        try {
          put("_selectorDefinitionId", "2E64F551C7C4470C80C29DBA24B34A5F");
          put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
          put("_where", "e.active='Y'");
          put("_sortBy", "_identifier");
          put("_requestType", "Window");
          put("_distinct", "productPrice");

          // To reproduce this problem is important not to add the targetProperty parameter. For
          // this reason targetProperty=null.
          put("_inpTableId", "293");
          put("_textMatchStyle", "substring");

          // Filter selector
          JSONObject criteria = new JSONObject();
          criteria.put("fieldName", "productPrice$priceListVersion$_identifier");
          criteria.put("operator", "iContains");
          criteria.put("value", "Tarifa");
          put("criteria", criteria.toString());
        } catch (Exception ignore) {
        }
      }
    }), //
    PropertySelector("83B60C4C19AE4A9EBA947B948C5BA04D", new HashMap<String, String>() {
      {
        // Property selector invocation from Windows > Tab > Field > Property field
        put("_selectorDefinitionId", "387D9FFC48A74054835C5DF6E6FD08F7");
        put("inpadTableId", "259");
        put("inpTabId", "107");
        put("targetProperty", "property");
      }
    }), //
    ManageVariants("6654D607F650425A9DFF7B6961D54920", new HashMap<String, String>() {
      {
        put("@Product.id@", PRODUCT_TEST);
      }
    }), //
    Note("090A37D22E61FE94012E621729090048", new HashMap<String, String>() {
      {
        // Note of a record in Windows, Tabs and Fields.
        String criteria = "{\"fieldName\":\"table\",\"operator\":\"equals\",\"value\":\""
            + TABLE_WINDOWS_TABS_FIELDS_ID
            + "\"}__;__{\"fieldName\":\"record\",\"operator\":\"equals\",\"value\":\""
            + RECORD_OF_WINDOWS_TABS_FIELDS_ID + "\"}";
        String entityName = "OBUIAPP_Note";
        put("criteria", criteria);
        put("_entityName", entityName);
      }
    }), //
    ProductCharacteristics("BE2735798ECC4EF88D131F16F1C4EC72"), //
    Combo("ComboTableDatasourceService", new HashMap<String, String>() {
      {
        // Sales Order > Payment Terms
        put("fieldId", "1099");
      }
    }), //
    CustomQuerySelectorDatasource("F8DD408F2F3A414188668836F84C21AF",
        new HashMap<String, String>() {
          {
            // Sales Invoice > Selector Business Partner
            put("_selectorDefinitionId", "862F54CB1B074513BD791C6789F4AA42");
            put("inpTableId", "318");
            put("targetProperty", "businessPartner");
          }
        }), //
    CustomQuerySelectorDatasourceProcess("ADList", new HashMap<String, String>() {
      {
        // Sales Order > Add Payment process > Selector Action Regarding Document
        put("_selectorDefinitionId", "41B3A5EA61AB46FBAF4567E3755BA190");
        put("_processDefinitionId", "9BED7889E1034FE68BD85D5D16857320");
        put("targetProperty", "businessPartner");
      }
    }), //
    QuickLaunch("99B9CC42FDEA4CA7A4EE35BC49D61E0E"), //
    QuickCreate("C17951F970E942FD9F3771B7BE91D049"), //
    HQLDataSource("3C1148C0AB604DE1B51B7EA4112C325F", new HashMap<String, String>() {
      {
        // Invocation from Sales Order > Add Payment process > Credit to Use.
        put("tableId", "58AF4D3E594B421A9A7307480736F03E");
      }
    }), //
    ADTree("90034CAE96E847D78FBEF6D38CB1930D", new HashMap<String, String>() {
      {
        // Organization tree view.
        put("referencedTableId", "155");
        put("tabId", "143");
        String selectedPro = "[\"searchKey\",\"name\",\"description\",\"active\",\"summaryLevel\",\"socialName\",\"organizationType\",\"currency\",\"allowPeriodControl\",\"calendar\"]";
        put("_selectedProperties", selectedPro);
      }
    }), //
    AccountTree("D2F94DC86DEC48D69E4BFCE59DC670CF", new HashMap<String, String>() {
      {
        // Account tree value > Entity FinancialMgmtElementValue.
        put("referencedTableId", "188");
        put("tabId", "132");
        String selectedPro = "[\"searchKey\",\"name\",\"elementLevel\",\"accountType\",\"showValueCondition\",\"summaryLevel\"]";
        put("_selectedProperties", selectedPro);
        put("@FinancialMgmtElement.id@", "CCC5ACF18A114F3E9630EE321E6063BF");
      }
    }), //
    StockReservations("2F5B70D7F12E4F5C8FE20D6F17D69ECF", new HashMap<String, String>() {
      {
        // Manage Stock from Stock Reservations
        put("@MaterialMgmtReservation.id@", "848E85D3020245888B9579FEA9A1799B");
      }
    }), //
    QueryList("DD17275427E94026AD721067C3C91C18", new HashMap<String, String>() {
      {
        // Query List Widget > Best Sellers
        put("widgetId", "CD1B06C4ED974B5F905A5A01B097DF4E");
      }
    });

    private String ds;
    private Map<String, String> params;

    private DataSource(String ds) {
      this.ds = ds;
      params = new HashMap<String, String>();
      params.put("_operationType", "fetch");
      params.put("_startRow", "0");
      params.put("_endRow", "1");
    }

    private DataSource(String ds, Map<String, String> extraParams) {
      this(ds);
      params.putAll(extraParams);
    }
  }

  public DataSourceSecurity(RoleType role, DataSource dataSource, int expectedResponseStatus) {
    this.role = role;
    this.dataSource = dataSource;
    this.expectedResponseStatus = expectedResponseStatus;
  }

  @Parameters(name = "{0} - dataSource: {1}")
  public static Collection<Object[]> parameters() {
    List<Object[]> testCases = new ArrayList<Object[]>();
    for (RoleType type : RoleType.values()) {
      int accessForAdminOnly = type == RoleType.ADMIN_ROLE ? JsonConstants.RPCREQUEST_STATUS_SUCCESS
          : JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR;
      int accessForAdminAndSystemOnly = type == RoleType.NO_ACCESS_ROLE ? JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR
          : JsonConstants.RPCREQUEST_STATUS_SUCCESS;

      testCases.add(new Object[] { type, DataSource.Order, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.ManageVariants, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.ProductCharacteristics, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.Combo, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.CustomQuerySelectorDatasource,
          accessForAdminAndSystemOnly });
      testCases.add(new Object[] { type, DataSource.CustomQuerySelectorDatasourceProcess,
          accessForAdminAndSystemOnly });

      // QuickLaunch ds should be always accessible
      testCases.add(new Object[] { type, DataSource.QuickLaunch,
          JsonConstants.RPCREQUEST_STATUS_SUCCESS });

      // QuickCreate ds should be always accessible
      testCases.add(new Object[] { type, DataSource.QuickCreate,
          JsonConstants.RPCREQUEST_STATUS_SUCCESS });
      testCases.add(new Object[] { type, DataSource.HQLDataSource, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.ADTree, accessForAdminAndSystemOnly });
      testCases.add(new Object[] { type, DataSource.AccountTree, accessForAdminOnly });
      testCases.add(new Object[] { type, DataSource.StockReservations, accessForAdminOnly });

      // QueryList ds is accessible if current role has access to widgetId
      testCases.add(new Object[] { type, DataSource.QueryList,
          JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR });
      testCases.add(new Object[] {
          type,
          DataSource.PropertySelector,
          type == RoleType.SYSTEM_ROLE ? JsonConstants.RPCREQUEST_STATUS_SUCCESS
              : JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR });

      // Alert ds should be always accessible
      testCases
          .add(new Object[] { type, DataSource.Alert, JsonConstants.RPCREQUEST_STATUS_SUCCESS });

      // Note ds is accessible if current role has access to entity of the notes. This note is
      // invocated from a record in Windows, Tabs and Fields.
      testCases.add(new Object[] {
          type,
          DataSource.Note,
          type == RoleType.NO_ACCESS_ROLE ? JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR
              : JsonConstants.RPCREQUEST_STATUS_SUCCESS });
    }
    // testing a problem detected in how properties are initialized.
    testCases.add(new Object[] { RoleType.ADMIN_ROLE, DataSource.ProductByPriceAndWarehouse,
        JsonConstants.RPCREQUEST_STATUS_SUCCESS });

    return testCases;
  }

  /** Creates dummy role without any access for testing purposes */
  @BeforeClass
  public static void createNoAccessRoleAndGenericProduct() {
    OBContext.setOBContext(CONTEXT_USER);

    Role noAccessRole = OBProvider.getInstance().get(Role.class);
    noAccessRole.setId("1");
    noAccessRole.setNewOBObject(true);
    noAccessRole.setOrganization(OBDal.getInstance().get(Organization.class, ASTERISK_ORG_ID));
    noAccessRole.setName("Test No Access");
    noAccessRole.setManual(true);
    noAccessRole.setUserLevel(" CO");
    noAccessRole.setClientList(OBContext.getOBContext().getCurrentClient().getId());
    noAccessRole.setOrganizationList(ASTERISK_ORG_ID);
    OBDal.getInstance().save(noAccessRole);

    RoleOrganization noAcessRoleOrg = OBProvider.getInstance().get(RoleOrganization.class);
    noAcessRoleOrg.setOrganization((Organization) OBDal.getInstance().getProxy(
        Organization.ENTITY_NAME, ESP_ORG));
    noAcessRoleOrg.setRole(noAccessRole);
    OBDal.getInstance().save(noAcessRoleOrg);

    UserRoles noAccessRoleUser = OBProvider.getInstance().get(UserRoles.class);
    noAccessRoleUser.setOrganization(noAccessRole.getOrganization());
    noAccessRoleUser.setUserContact(OBContext.getOBContext().getUser());
    noAccessRoleUser.setRole(noAccessRole);
    OBDal.getInstance().save(noAccessRoleUser);

    // Create product generic for manage variants
    Product productToClone = OBDal.getInstance().get(Product.class,
        "DA7FC1BB3BA44EC48EC1AB9C74168CED");
    Product product = (Product) DalUtil.copy(productToClone, false);
    product.setId(PRODUCT_TEST);
    product.setNewOBObject(true);
    product.setOrganization(OBDal.getInstance().get(Organization.class, ASTERISK_ORG_ID));
    product.setName("Generic Product Test");
    product.setSearchKey("GEN-1 ");
    product.setClient(OBDal.getInstance().get(Client.class, "23C59575B9CF467C9620760EB255B389"));
    product.setGeneric(true);
    OBDal.getInstance().save(product);

    OBDal.getInstance().commitAndClose();
  }

  /** Tests datasource allows or denies fetch action based on role access */
  @Test
  public void fetchShouldBeAllowedOnlyIfRoleIsGranted() throws Exception {
    OBContext.setOBContext(CONTEXT_USER);
    changeProfile(role.roleId, LANGUAGE_ID, role.orgId, WAREHOUSE_ID);
    JSONObject jsonResponse = null;
    jsonResponse = fetchDataSource();
    assertThat("Request status", jsonResponse.getInt("status"), is(expectedResponseStatus));

  }

  private JSONObject fetchDataSource() throws Exception {
    String response = doRequest("/org.openbravo.service.datasource/" + dataSource.ds,
        dataSource.params, 200, "POST");

    return new JSONObject(response).getJSONObject("response");
  }

  /** Deletes dummy testing role and product */
  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext(CONTEXT_USER);
    OBDal.getInstance().remove(OBDal.getInstance().get(Role.class, ROLE_NO_ACCESS));
    OBDal.getInstance().remove(OBDal.getInstance().get(Product.class, PRODUCT_TEST));
    OBDal.getInstance().commitAndClose();
  }
}
