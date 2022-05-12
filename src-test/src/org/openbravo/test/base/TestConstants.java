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
 * All portions are Copyright (C) 2018-2022 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.base;

import java.util.AbstractMap;
import java.util.Map;

/** Some constants to be used in tests */
public class TestConstants {

  public static class Orgs {
    public static final String MAIN = "0";
    public static final String FB_GROUP = "19404EAD144C49A0AF37D54377CF452D";

    public static final String USA = "5EFF95EB540740A3B10510D9814EFAD5";
    public static final String US = "2E60544D37534C0B89E765FE29BC0B43";
    public static final String US_EST = "7BABA5FF80494CAFA54DEBD22EC46F01";
    public static final String US_WEST = "BAE22373FEBE4CCCA24517E23F0C8A48";

    public static final String SPAIN = "357947E87C284935AD1D783CF6F099A1";
    public static final String ESP = "B843C30461EA4501935CB1D125C9C25A";
    public static final String ESP_SUR = "DC206C91AA6A4897B44DA897936E0EC3";
    public static final String ESP_NORTE = "E443A31992CB4635AFCAEABE7183CE85";

    public static final String QA_ORG = "43D590B4814049C6B85C6545E8264E37";
  }

  public static class Clients {
    public static final String SYSTEM = "0";
    public static final String FB_GRP = "23C59575B9CF467C9620760EB255B389";
    public static final String QA_CLIENT = "4028E6C72959682B01295A070852010D";
  }

  public static class Roles {
    public static final String FB_GRP_ADMIN = "42D0EEB1C66F497A90DD526DC597E6F0";
    public static final String ESP_ADMIN = "F3196A30B53A42778727B2852FF90C24";
    public static final String ESP_EMPLOYEE = "D615084948E046E3A439915008F464A6";
    public static final String QA_ADMIN_ROLE = "4028E6C72959682B01295A071429011E";
    public static final String SYS_ADMIN = "0";
  }

  public static class Tables {
    public static final String C_ORDER = "259";
  }

  public static class Windows {
    public static final String SALES_ORDER = "143";
    public static final String DISCOUNTS_AND_PROMOTIONS = "800028";
    public static final String SALES_INVOICE = "167";
    public static final String PURCHASE_INVOICE = "183";
  }

  public static class Tabs {
    public static final String SALES_INVOICE_HEADER = "263";
    public static final String PURCHASE_INVOICE_HEADER = "290";
  }

  public static class Entities {
    public static final String COUNTRY = "Country";
  }

  public static class Users {
    public static final String OPENBRAVO = "100";
    public static final String SYSTEM = "0";
    public static final String FB_ADMIN = "A530AAE22C864702B7E1C22D58E7B17B";
    /**
     * Record ID of User "F&amp;BESRNUser" - Any user with less privileges than {@link #FB_ADMIN}
     */
    public static final String FB_USER = "75449AFBAE7F46029F26C85C4CCF714B";
    public static final String QA_ADMIN = "4028E6C72959682B01295A0735CB0120";
  }

  public static class Languages {
    public static final String ES_ES_LANG_ID = "140";
    public static final String ES_ES_ISOCODE = "es_ES";
    public static final String SQ_AL_LANG_ID = "181";
    public static final String SQ_AL_ISOCODE = "sq_AL";
    public static final String EN_US_LANG_ID = "192";
    public static final String EN_US_ISOCODE = "en_US";
  }

  public static class Modules {
    public static final String ID_CORE = "0";
  }

  public static class WareHouses {
    public static final String SPAIN = "4028E6C72959682B01295ECFEF4502A0";
    public static final String ESP_NORTE = "B2D40D8A5D644DD89E329DC297309055";
    public static final String US_WEST = "4D45FE4C515041709047F51D139A21AC";
  }

  public static class Orders {
    public static final String DRAFT_ORDER = "F8492493E92C4EE5B5251AC4574778B7";
    public static final String FRESA_BIO_ORDER = "61047A6B06B3452B85260C7BCF08E78D";
  }

  public static class BusinessPartnerCategories {
    public static final String CUSTOMER = "4028E6C72959682B01295F40C38C02EB";
  }

  public static class Locations {
    /**
     * Record ID of the geographical location "c\ de la Costa 54, San Sebastián 12784"
     */
    public static final String COSTA_SS = "A21EF1AB822149BEB65D055CD91F261B";
  }

  public static class Currencies {
    public static final Map.Entry<String, String> EURO = new AbstractMap.SimpleEntry<String, String>(
        "EUR", "102");
    public static final Map.Entry<String, String> USD = new AbstractMap.SimpleEntry<String, String>(
        "USD", "100");
  }

  private TestConstants() {
  }
}
