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
package org.openbravo.test.datasource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.service.json.JsonConstants;

/**
 * This test evaluates if the datasources are correctly fetched and if the filter is correctly
 * applied.
 * 
 * @author Naroa Iriarte
 * 
 */
@RunWith(Parameterized.class)
public class DataSourceWhereParameterTest extends BaseDataSourceTestDal {
  // Expected

  private static final String USER_EXPECTED_VALUE = "A530AAE22C864702B7E1C22D58E7B17B";
  private static final String ALERT_EXPECTED_VALUE = "D0CB68A7ADDD462E8B46438E2B9F58F6";
  private static final String CUSTOM_QUERY_SELECTOR_EXPECTED_VALUE = "C0D9FAD1047343BAA53AF6F60D572DD0";
  private static final String PRODUCT_SELECTOR_DATASOURCE_EXPECTED_VALUE = "B2D40D8A5D644DD89E329DC29730905541732EFCA6374148BFD8B08C8B12DB73";
  private static final String RETURN_FROM_CUSTOMER_EXPECTED_VALUE = "279997DFE0D74C72AC983E1F346CE6B9";

  // Unexpected

  private static final String USER_UNEXPECTED_VALUE = "6A3D3D6A808C455EAF1DAB48058FDBF4";
  private static final String ALERT_UNEXPECTED_VALUE = "D938304218B6405F8B2665D5E77A3EE4";
  private static final String CUSTOM_QUERY_SELECTOR_UNEXPECTED_VALUE = "369"; // The "<" symbol
  private static final String PRODUCT_SELECTOR_DATASOURCE_UNEXPECTED_VALUE = "3DBB480253094C99A4408923F69806D7"; // Electricity
  private static final String RETURN_FROM_CUSTOMER_UNEXPECTED_VALUE = "8F71AF46474240E6915F7F0458DE2318"; // Naranja
                                                                                                          // BIO
  private static final String TABLE_ID = "105";
  private static final String RECORD_ID = "283";
  private static final String MANUAL_WHERE = "1=1) or 2=2";

  private DataSource datasource;
  private String expectedRecords;
  private String notExpectedRecords;

  @SuppressWarnings("serial")
  private enum DataSource {
    User("ADUser", USER_EXPECTED_VALUE, USER_UNEXPECTED_VALUE, false,
        new HashMap<String, String>() {
          {
            put("_noCount", "true");
            put("isFilterApplied", "true");
            put("isSorting", "true");
            put("_selectedProperties",
                "id,client,organization,updatedBy,updated,creationDate,createdBy,name,processNow,id,client,organization,processNow,name,username,firstName,lastName,password,description,active,businessPartner,partnerAddress,email,locked,position,birthday,phone,alternativePhone,fax,emailServerUsername,emailServerPassword,supervisor,defaultRole,defaultLanguage,defaultClient,defaultOrganization,defaultWarehouse");
            put("windowId", "108");
            put("tabId", "118");
            put("moduleId", "0");
            put("_noActiveFilter", "true");
            put("sendOriginalIDBack", "true");
            put("_className", "OBViewDataSource");
            put("Constants_FIELDSEPARATOR", "$");
            put("Constants_IDENTIFIER", "_identifier");
            put("_startRow", "0");
            put("_endRow", "100");
            put("_sortBy", "name");
            put("_textMatchStyle", "substring");
            put("_componentId", "isc_OBViewGrid_0");
            put("_dataSource", "isc_OBViewDataSource_0");
            put("isc_metaDataPrefix", "_");
            put("isc_dataFormat", "json");
          }
        }), //
    QuickLaunch("99B9CC42FDEA4CA7A4EE35BC49D61E0E", null, null, true,
        new HashMap<String, String>() {
          {
            put("_startRow", "0");
            put("_endRow", "50");
          }
        }), //

    QuickCreate("C17951F970E942FD9F3771B7BE91D049", null, null, true,
        new HashMap<String, String>() {
          {
            put("_startRow", "0");
            put("_endRow", "50");
          }
        }), //

    Alert("DB9F062472294F12A0291A7BD203F922", ALERT_EXPECTED_VALUE, ALERT_UNEXPECTED_VALUE, false,
        new HashMap<String, String>() {
          {
            put("_alertStatus", "New");
            put("_startRow", "0");
            put("_endRow", "50");
          }
        }), //
    ActionRegardingSelector("ADList", CUSTOM_QUERY_SELECTOR_EXPECTED_VALUE,
        CUSTOM_QUERY_SELECTOR_UNEXPECTED_VALUE, false, new HashMap<String, String>() {
          {
            put("inpemAprmAddScheduledpayments", "false");
            put("inpposted", "N");
            put("inpemAprmProcessPayment", "P");
            put("inpemAprmExecutepayment", "false");
            put("inpwriteoffamt", "0");
            put("inpemAprmReversepayment", "false");
            put("inpcreatedbyalgorithm", "false");
            put("inpcActivityId", "null");
            put("inpcCampaignId", "null");
            put("inpadClientId", "23C59575B9CF467C9620760EB255B389");
            put("inpfinPaymentId", "E800127A25734B0AAB7AB5624F5EC0FF");
            put("inpisactive", "true");
            put("inpisreceipt", "true");
            put("inpprocessed", "false");
            put("inpprocessing", "false");
            put("inpemAprmReconcilePayment", "false");
            put("Fin_Payment_ID", "E800127A25734B0AAB7AB5624F5EC0FF");
            put("inpadOrgId", "E443A31992CB4635AFCAEABE7183CE85");
            put("inpcDoctypeId", "3BA2EAD55A214AE68B7710C1E48C9F9C");
            put("inpdocumentno", "1000163");
            put("inpreferenceno", "null");
            put("inppaymentdate", "2016-02-22");
            put("inpcBpartnerId", "null");
            put("inpdescription", "null");
            put("inpfinPaymentmethodId", "47506D4260BA4996B92768FF609E6665");
            put("inpamount", "0");
            put("inpfinFinancialAccountId", "C2AA9C0AFB434FD4B827BE58DC52C1E2");
            put("inpcCurrencyId", "102");
            put("inpfinaccTxnAmount", "0");
            put("inpfinaccTxnConvertRate", "1");
            put("inpfinRevPaymentId", "");
            put("inpcProjectId", "");
            put("inpcCostcenterId", "");
            put("inpuser1Id", "");
            put("inpuser2Id", "");
            put("inpstatus", "RPAP");
            put("inpgeneratedCredit", "0");
            put("inpusedCredit", "0");
            put("inpTabId", "C4B6506838E14A349D6717D6856F1B56");
            put("inpwindowId", "E547CE89D4C04429B6340FFA44E70716");
            put("inpTableId", "D1A97202E832470285C9B1EB026D54E2");
            put("inpkeyColumnId", "Fin_Payment_ID");
            put("keyProperty", "id");
            put("inpKeyName", "inpfinPaymentId");
            put("keyColumnName", "Fin_Payment_ID");
            put("keyPropertyType", "_id_13");
            put("payment_documentno", "1000163");
            put("reference_no", "null");
            put("c_currency_id", "102");
            put("c_currency_to_id", "102");
            put("received_from", "null");
            put("fin_paymentmethod_id", "47506D4260BA4996B92768FF609E6665");
            put("actual_payment", "0");
            put("converted_amount", "0");
            put("payment_date", "2016-02-22");
            put("fin_financial_account_id", "C2AA9C0AFB434FD4B827BE58DC52C1E2");
            put("expected_payment", "0");
            put("conversion_rate", "1");
            put("0C672A3B7CDF416F9522DF3FA5AE4022", "Order/Invoice");
            put("transaction_type", "I");
            put("order_invoice", "{\"_selection\":[],\"_allRows\":[]}");
            put("7B6B5F5475634E35A85CF7023165E50B", "GL Items");
            put("glitem", "{\"_selection\":[],\"_allRows\":[]}");
            put("CB265F2D7ACF439F9FB5EFBFA0B50363", "Credit To Use");
            put("credit_to_use", "{\"_selection\":[],\"_allRows\":[]}");
            put("BFFF70E721654110AD5BACF3D4216D3A", "Totals");
            put("amount_gl_items", "0");
            put("amount_inv_ords", "0");
            put("total", "0");
            put("difference", "0");
            put("document_action", "null");
            put("overpayment_action", "null");
            put("customer_credit", "null");
            put("issotrx", "true");
            put("fin_payment_id", "E800127A25734B0AAB7AB5624F5EC0FF");
            put("c_invoice_id", "null");
            put("c_order_id", "null");
            put("used_credit", "0");
            put("StdPrecision", "2");
            put("generateCredit", "0");
            put("DOCBASETYPE", "ARR");
            put("expectedDifference", "0");
            put("overpayment_action_display_logic", "N");
            put("trxtype_display_logic", "N");
            put("credit_to_use_display_logic", "N");
            put("payment_documentno_readonly_logic", "Y");
            put("payment_method_readonly_logic", "Y");
            put("actual_payment_readonly_logic", "N");
            put("converted_amount_readonly_logic", "N");
            put("payment_date_readonly_logic", "Y");
            put("fin_financial_account_id_readonly_logic", "Y");
            put("conversion_rate_readonly_logic", "N");
            put("received_from_readonly_logic", "Y");
            put("c_currency_id_readonly_logic", "Y");
            put("outad_org_id_display_logic", "N");
            put("bslamount_display_logic", "N");
            put("trxtype", "");
            put("ad_org_id", "E443A31992CB4635AFCAEABE7183CE85");
            put("bslamount", "null");
            put("_org", "E443A31992CB4635AFCAEABE7183CE85");
            put("_selectorDefinitionId", "41B3A5EA61AB46FBAF4567E3755BA190");
            put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
            put("_sortBy", "_identifier");
            put("_processDefinitionId", "9BED7889E1034FE68BD85D5D16857320");
            put("_selectorFieldId", "52BD390363394BE980D0A55AFC4CDBB9");
            put("_noCount", "true");
            put("IsSelectorItem", "true");
            put("Constants_FIELDSEPARATOR", "$");
            put("columnName", "document_action");
            put("Constants_IDENTIFIER", "_identifier");
            put("operator", "or");
            put("_constructor", "AdvancedCriteria");
            put("criteria",
                "{\"fieldName\":\"_dummy\",\"operator\":\"equals\",\"value\":1456742508197,\"_constructor\":\"AdvancedCriteria\"}");
            put("_operationType", "fetch");
            put("_startRow", "0");
            put("_endRow", "75");
            put("_textMatchStyle", "startsWith");
            put("_componentId", "isc_PickListMenu_0");
            put("_dataSource", "isc_OBRestDataSource_36");
            put("isc_metaDataPrefix", "_");
            put("isc_dataFormat", "json");
          }
        }), //

    ProductSelectorDataSource("ProductByPriceAndWarehouse",
        PRODUCT_SELECTOR_DATASOURCE_EXPECTED_VALUE, PRODUCT_SELECTOR_DATASOURCE_UNEXPECTED_VALUE,
        false, new HashMap<String, String>() {
          {
            put("inpcDoctypeId", "0");
            put("inpemAprmAddpayment", "false");
            put("inpdocaction", "CO");
            put("inpcopyfrom", "false");
            put("inpcopyfrompo", "false");
            put("inpdeliveryviarule", "P");
            put("inpfreightcostrule", "I");
            put("inpfreightamt", "0");
            put("inpchargeamt", "0");
            put("inpcalculatePromotions", "false");
            put("inpcOrderId", "05D75EC4235A43819F58BD56E625B75F");
            put("inpadClientId", "23C59575B9CF467C9620760EB255B389");
            put("inpisactive", "true");
            put("inpprocessing", "false");
            put("inpprocessed", "false");
            put("inpissotrx", "true");
            put("inpposted", "N");
            put("inpgeneratetemplate", "false");
            put("C_Order_ID", "05D75EC4235A43819F58BD56E625B75F");
            put("inpadOrgId", "E443A31992CB4635AFCAEABE7183CE85");
            put("inpcDoctypetargetId", "466AF4B0136A4A3F9F84129711DA8BD3");
            put("inpdocumentno", "1000164");
            put("inpcBpartnerId", "D6C43D502F6C492AB185411CC071E169");
            put("inpcBpartnerLocationId", "4663C528F3094DB0977A8D274FC880F8");
            put("inpmPricelistId", "AEE66281A08F42B6BC509B8A80A33C29");
            put("inpmWarehouseId", "B2D40D8A5D644DD89E329DC297309055");
            put("inpdocstatus", "DR");
            put("inpgrandtotal", "0");
            put("inpcCurrencyId", "102");
            put("inpdeliverystatus", "0");
            put("inpinvoicestatus", "0");
            put("inpTabId", "187");
            put("inpwindowId", "143");
            put("inpTableId", "260");
            put("inpkeyColumnId", "C_OrderLine_ID");
            put("keyProperty", "id");
            put("inpKeyName", "inpcOrderlineId");
            put("keyColumnName", "C_OrderLine_ID");
            put("keyPropertyType", "_id_13");
            put("inpqtyinvoiced", "0");
            put("inpqtydelivered", "0");
            put("inpqtyreserved", "0");
            put("inpdirectship", "false");
            put("inpcancelpricead", "false");
            put("inpgrosspricestd", "0");
            put("inppricestd", "0");
            put("inpiseditlinenetamt", "false");
            put("inpmanageReservation", "false");
            put("inpexplode", "false");
            put("inprelateOrderline", "false");
            put("inppricelimit", "0");
            put("inpisdescription", "false");
            put("inpmanagePrereservation", "false");
            put("inpline", "10");
            put("inpqtyordered", "1");
            put("inpcUomId", "100");
            put("inppriceactual", "0");
            put("inpgrossUnitPrice", "0");
            put("inplinenetamt", "0");
            put("inplineGrossAmount", "0");
            put("inpcTaxId", "C976D95942B54A2F9BB7B59863917035");
            put("inppricelist", "0");
            put("inpdiscount", "0");
            put("inptaxbaseamt", "0");
            put("inpprintDescription", "false");
            put("HASSECONDUOM", "0");
            put("Parent_AD_Org", "E443A31992CB4635AFCAEABE7183CE85");
            put("DOCBASETYPE", "SOO");
            put("ATTRIBUTESETINSTANCIABLE", "N");
            put("C_BPARTNER_ID", "D6C43D502F6C492AB185411CC071E169");
            put("Processed", "N");
            put("HASRELATEDSERVICE", "N");
            put("UsesAlternate", "N");
            put("GROSSPRICE", "N");
            put("Posted", "N");
            put("$Element_U2_POO_L", "N");
            put("IsSOTrx", "Y");
            put("$Element_PJ_POO_L", "Y");
            put("$Element_PJ_SOO_L", "Y");
            put("$Element_OO", "Y");
            put("$IsAcctDimCentrally", "Y");
            put("$Element_OO_SOO_L", "N");
            put("$Element_PJ", "Y");
            put("$Element_U1_SOO_L", "N");
            put("$Element_U1_POO_L", "N");
            put("$Element_U2_SOO_L", "N");
            put("windowId", "143");
            put("tabId", "187");
            put("moduleId", "0");
            put("_org", "E443A31992CB4635AFCAEABE7183CE85");
            put("_selectorDefinitionId", "2E64F551C7C4470C80C29DBA24B34A5F");
            put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
            put("_sortBy", "_identifier");
            put("_noCount", "true");
            put("recordIdInForm", "_1456744949359");
            put("targetProperty", "product");
            put("adTabId", "187");
            put("IsSelectorItem", "true");
            put("Constants_FIELDSEPARATOR", "$");
            put("columnName", "M_Product_ID");
            put("Constants_IDENTIFIER", "_identifier");
            put("_extraProperties",
                "product$searchKey,product$id,productPrice$priceListVersion$_identifier,available,product$genericProduct$_identifier,warehouse$_identifier,productPrice$priceListVersion$priceList$currency$id,priceLimit,product$name,qtyOnHand,product$uOM$id,product$_identifier,product$characteristicDescription,qtyOrdered,standardPrice,netListPrice");
            put("operator", "or");
            put("_constructor", "AdvancedCriteria");
            put("criteria",
                "{\"fieldName\":\"_dummy\",\"operator\":\"equals\",\"value\":1456744954173}");
            put("_startRow", "0");
            put("_endRow", "75");
            put("_textMatchStyle", "substring");
            put("_componentId", "isc_PickListMenu_4");
            put("_dataSource", "isc_OBRestDataSource_67");
            put("isc_metaDataPrefix", "_");
            put("isc_dataFormat", "json");
          }
        }), //

    Note("090A37D22E61FE94012E621729090048", null, null, true, new HashMap<String, String>() {
      {
        // Note of a record in Windows, Tabs and Fields.
        String criteria = "{\"fieldName\":\"table\",\"operator\":\"equals\",\"value\":\""
            + TABLE_ID + "\"}__;__{\"fieldName\":\"record\",\"operator\":\"equals\",\"value\":\""
            + RECORD_ID + "\"}";
        String entityName = "OBUIAPP_Note";
        put("criteria", criteria);
        put("_entityName", entityName);
        put("_startRow", "0");
        put("_endRow", "50");
      }
    }), //

    ReturnFromCustomersPickAndExecute("3C1148C0AB604DE1B51B7EA4112C325F",
        RETURN_FROM_CUSTOMER_EXPECTED_VALUE, RETURN_FROM_CUSTOMER_UNEXPECTED_VALUE, false,
        new HashMap<String, String>() {
          {
            put("@Order.id@", "D6446F22C5E24EBB95F232302AEABBBE");
            put("@Order.client@", "23C59575B9CF467C9620760EB255B389");
            put("@Order.documentType@", "0");
            put("@Order.deliveryMethod@", "P");
            put("@Order.processed@", "false");
            put("@Order.freightCostRule@", "I");
            put("@Order.salesTransaction@", "true");
            put("@Order.posted@", "N");
            put("@Order.organization@", "E443A31992CB4635AFCAEABE7183CE85");
            put("@Order.businessPartner@", "A6750F0D15334FB890C254369AC750A8");
            put("@Order.partnerAddress@", "6518D3040ED54008A1FC0C09ED140D66");
            put("@Order.warehouse@", "B2D40D8A5D644DD89E329DC297309055");
            put("@Order.priceList@", "AEE66281A08F42B6BC509B8A80A33C29");
            put("@Order.documentStatus@", "DR");
            put("@Order.currency@", "102");
            put("_org", "E443A31992CB4635AFCAEABE7183CE85");
            put("_orderBy", "obSelected desc, io.movementDate desc, io.documentNo desc, iol.lineNo");
            put("_noCount", "true");
            put("isFilterApplied", "true");
            put("tableId", "CDB9DC9655F24DF8AB41AA0ADBD04390");
            put("_className", "OBPickAndExecuteDataSource");
            put("Constants_FIELDSEPARATOR", "$");
            put("Constants_IDENTIFIER", "_identifier");
            put("tabId", "B01BFDF1E6B24CF4941807CA7F77A073");
            put("buttonOwnerViewTabId", "AF4090093CFF1431E040007F010048A5");
            put("_isPickAndEdit", "true");
            put("_startRow", "0");
            put("_endRow", "100");
            put("_textMatchStyle", "substring");
            put("_componentId", "isc_OBPickAndExecuteGrid_0");
            put("_dataSource", "isc_OBPickAndExecuteDataSource_0");
            put("isc_metaDataPrefix", "_");
            put("isc_dataFormat", "json");
          }
        });

    private String ds;
    private String expected;
    private String unexpected;
    private boolean onlySuccessAssert;
    private Map<String, String> params;

    private DataSource(String ds, String expected, String unexpected, boolean onlySuccessAssert) {
      this.ds = ds;
      this.expected = expected;
      this.unexpected = unexpected;
      this.onlySuccessAssert = onlySuccessAssert;
      params = new HashMap<String, String>();
      params.put("_operationType", "fetch");
    }

    private DataSource(String ds, String expected, String unexpected, boolean onlySuccessAssert,
        Map<String, String> extraParams) {
      this(ds, expected, unexpected, onlySuccessAssert);
      params.putAll(extraParams);
    }
  }

  public DataSourceWhereParameterTest(DataSource datasource, String expectedRecords,
      String notExpectedRecords) {
    this.datasource = datasource;
    this.expectedRecords = expectedRecords;
    this.notExpectedRecords = notExpectedRecords;
  }

  @Parameters(name = "{0} datasource: {1}")
  public static Collection<Object[]> parameters() {
    List<Object[]> tests = new ArrayList<Object[]>();
    tests.add(new Object[] { DataSource.Alert, DataSource.Alert.expected,
        DataSource.Alert.unexpected });
    tests
        .add(new Object[] { DataSource.ActionRegardingSelector,
            DataSource.ActionRegardingSelector.expected,
            DataSource.ActionRegardingSelector.unexpected });
    tests.add(new Object[] { DataSource.ProductSelectorDataSource,
        DataSource.ProductSelectorDataSource.expected,
        DataSource.ProductSelectorDataSource.unexpected });
    tests.add(new Object[] { DataSource.ReturnFromCustomersPickAndExecute,
        DataSource.ReturnFromCustomersPickAndExecute.expected,
        DataSource.ReturnFromCustomersPickAndExecute.unexpected });
    tests
        .add(new Object[] { DataSource.User, DataSource.User.expected, DataSource.User.unexpected });
    tests
        .add(new Object[] { DataSource.Note, DataSource.Note.expected, DataSource.Note.unexpected });
    tests.add(new Object[] { DataSource.QuickCreate, DataSource.QuickCreate.expected,
        DataSource.QuickCreate.unexpected });
    tests.add(new Object[] { DataSource.QuickLaunch, DataSource.QuickLaunch.expected,
        DataSource.QuickLaunch.unexpected });

    return tests;
  }

  @Test
  public void datasourceWithNoManualWhereParameter() throws Exception {
    boolean expectedRecordId;
    boolean unexpectedRecordId;
    if (!datasource.onlySuccessAssert) {
      if ("3C1148C0AB604DE1B51B7EA4112C325F".equals(datasource.ds)
          || "ADUser".equals(datasource.ds)) {
        datasource.params.put("isFilterApplied", "true");
        String datasourceResponseFilterTrue = getDataSourceResponse();
        expectedRecordId = isValueInTheResponseData(datasource.expected,
            datasourceResponseFilterTrue);
        unexpectedRecordId = isValueInTheResponseData(datasource.unexpected,
            datasourceResponseFilterTrue);
        assertThat(expectedRecordId, is(true));
        assertThat(unexpectedRecordId, is(false));
        datasource.params.put("isFilterApplied", "false");
        String datasourceResponseFilterFalse = getDataSourceResponse();
        expectedRecordId = isValueInTheResponseData(datasource.expected,
            datasourceResponseFilterFalse);
        unexpectedRecordId = isValueInTheResponseData(datasource.unexpected,
            datasourceResponseFilterFalse);
        assertThat(expectedRecordId, is(true));
        assertThat(unexpectedRecordId, is(true));
      } else {
        String datasourceResponseNoFilter = getDataSourceResponse();
        expectedRecordId = isValueInTheResponseData(datasource.expected, datasourceResponseNoFilter);
        unexpectedRecordId = isValueInTheResponseData(datasource.unexpected,
            datasourceResponseNoFilter);
        assertThat(expectedRecordId, is(true));
        assertThat(unexpectedRecordId, is(false));
      }
    }
  }

  @Test
  public void datasourceWithManualWhereParameter() throws Exception {
    boolean expectedRecordId;
    boolean unexpectedRecordId;
    if (!datasource.onlySuccessAssert) {

      datasource.params.put("isFilterApplied", "true");
      datasource.params.put("_where", MANUAL_WHERE);
      String datasourceResponseFilterTrueWhereTrue = getDataSourceResponse();
      expectedRecordId = isValueInTheResponseData(datasource.expected,
          datasourceResponseFilterTrueWhereTrue);
      unexpectedRecordId = isValueInTheResponseData(datasource.unexpected,
          datasourceResponseFilterTrueWhereTrue);
      assertThat(expectedRecordId, is(true));
      assertThat(unexpectedRecordId, is(false));
      if ("3C1148C0AB604DE1B51B7EA4112C325F".equals(datasource.ds)
          || "ADUser".equals(datasource.ds)) {
        datasource.params.put("isFilterApplied", "false");
        String datasourceResponseFilterFalseWhereTrue = getDataSourceResponse();
        expectedRecordId = isValueInTheResponseData(datasource.expected,
            datasourceResponseFilterFalseWhereTrue);
        unexpectedRecordId = isValueInTheResponseData(datasource.unexpected,
            datasourceResponseFilterFalseWhereTrue);
        assertThat(expectedRecordId, is(true));
        assertThat(unexpectedRecordId, is(true));
        datasource.params.remove("_where");
      }
    }
  }

  @Test
  public void datasourceRequestStatusShouldBeSuccessful() throws Exception {
    String datasourceResponse = getDataSourceResponse();
    JSONObject jsonResponse = new JSONObject(datasourceResponse);
    assertThat(getStatus(jsonResponse), is(String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
  }

  private boolean isValueInTheResponseData(String valueId, String dataSourceResponse)
      throws Exception {
    JSONObject dataSourceResponseMid = new JSONObject();
    JSONArray dataSourceData = new JSONArray();
    boolean existsValue = false;
    JSONObject jsonResponse = new JSONObject(dataSourceResponse);
    dataSourceResponseMid = jsonResponse.getJSONObject("response");
    dataSourceData = dataSourceResponseMid.getJSONArray("data");
    String dataSourceDataString = dataSourceData.toString();
    if (dataSourceDataString.contains(valueId)) {
      existsValue = true;
    } else {
      existsValue = false;
    }
    return existsValue;
  }

  private String getDataSourceResponse() throws Exception {
    String response = doRequest("/org.openbravo.service.datasource/" + datasource.ds,
        datasource.params, 200, "POST");
    return response;
  }

  private String getStatus(JSONObject jsonResponse) throws JSONException {
    return jsonResponse.getJSONObject("response").get("status").toString();
  }
}
