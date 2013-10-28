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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.mobile.core.MobileCoreConstants;

/**
 * @author iperdomo
 * 
 */
@ApplicationScoped
@ComponentProvider.Qualifier(OBPOSComponentProvider.QUALIFIER)
public class OBPOSComponentProvider extends BaseComponentProvider {

  static {
    // Set dependency on Mobile Core app
    BaseComponentProvider.setAppDependencies(POSUtils.APP_NAME,
        Arrays.asList(MobileCoreConstants.RETAIL_CORE));
  }

  public static final String QUALIFIER = "OBPOS_Main";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(MobileCoreConstants.APP_CACHE_COMPONENT)) {
      final ApplicationCacheComponent component = getComponent(ApplicationCacheComponent.class);
      component.setId(componentId);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/" + POSUtils.MODULE_JAVA_PACKAGE + "/js/";

    final String[] resourceDependency = {
        "login/model/login-model",

        // Common components
        "datasource",
        // "data/dal",
        // "utilities",
        // "utilitiesui",
        // "arithmetic",
        // "i18n",
        // "components/clock",
        // "model/order",
        // "model/terminal",
        // "model/user",
        "utils/ob-utilitiesuipos",
        "model/bpcategory",
        "model/bplocation", // Model for Customer Address
        "model/currencypanel",
        "model/salesrepresentative",
        "model/productcharacteristic",
        "model/productchvalue",
        "model/brand",
        "model/changedbusinesspartners",
        "model/changedbplocation",
        "model/executor",
        "model/terminal-model",
        "model/returnreasons",
        "components/modalcancel",
        "components/subwindow",
        "components/leftsubwindow",
        "components/modalreceiptproperties",
        "components/modalreceiptlineproperties",
        "components/modalnoteditableorder",
        "components/modalnoteditableline",
        "components/modalmultiorders",
        "components/obpos-approval",
        "main",
        // Web POS window
        "utils/ordersSyncUtils",
        "utils/customersSyncUtils",
        "utils/customerAddrSyncUtils",
        "components/keypadcoins",
        "data/dataordersave",
        "data/dataordertaxes",
        "data/datacustomersave",
        "data/datacustomeraddrsave",
        "data/dataorderdiscount",
        "components/modalreceipts",
        "components/modalmultiorderslayaway",
        "components/rendercategory",
        "components/renderproduct",
        "components/renderproductch",
        "components/total",
        "components/modalpayment",
        "components/renderorderline",
        "components/rendermultiorderline",
        "components/order",
        "components/orderdetails",
        "components/businesspartner",
        "components/bplocation", // Button for selecting customer address
        "components/salesrepresentative",
        "components/listreceiptscounter",
        "components/menu",
        // Old Tickets
        "components/modalpaidreceipts",
        // Quotations
        "components/modalcreateorderfromquotation",
        "components/modalreactivatequotation",
        // Detection of change in context
        "components/modalcontextchanged",
        "components/modalproductcharacteristic",
        "components/modalproductbrand",

        // Point of sale models
        "model/order",
        "model/product-category",
        "model/product",
        "model/businesspartner",
        "model/document-sequence",
        "model/taxRate",
        "model/promotions",

        "model/obpos-supervisor-model",

        // Point of sale
        "pointofsale/model/pointofsale-print",
        "pointofsale/model/pointofsale-model",
        "pointofsale/model/localstock",
        "pointofsale/model/otherstoresstock",
        "pointofsale/view/pointofsale",
        "pointofsale/view/ps-receiptview",
        "pointofsale/view/ps-multireceiptview",
        "pointofsale/view/ps-productdetailsview",
        "pointofsale/view/toolbar-left",
        "pointofsale/view/toolbar-right",
        "pointofsale/view/scan",
        "pointofsale/view/editline",
        "pointofsale/view/payment",
        "pointofsale/view/ticketdiscount",
        "pointofsale/view/keyboard-toolbars",
        "pointofsale/view/keyboardorder",
        // Point of sale subwindows
        "pointofsale/view/subwindows/customers/components/sharedcomponents",
        "pointofsale/view/subwindows/customers/customersadvancedsearch",
        "pointofsale/view/subwindows/customers/editcreatecustomerform",
        "pointofsale/view/subwindows/customers/customerdetailview",

        "pointofsale/view/subwindows/customeraddress/components/sharedcomponents",
        "pointofsale/view/subwindows/customeraddress/editcreatecustomeraddress",
        "pointofsale/view/subwindows/customeraddress/customeraddrsearch",
        "pointofsale/view/subwindows/customeraddress/customeraddrdetailview",
        // Point of sale modals
        "pointofsale/view/modals/modalstockinstore",
        "pointofsale/view/modals/modalstockinotherstore",
        "pointofsale/view/modals/modalproductcannotbegroup",
        "pointofsale/view/modals/modalwarehousesrequired",
        "pointofsale/view/modals/modalcreditsales",
        "pointofsale/view/modals/modaldiscountneedqty",

        // Cash Management window
        "cashmgmt/model/cashmgmt-print", "cashmgmt/model/cashmgmt-model",

        "cashmgmt/view/cashmgmtkeyboard",
        "cashmgmt/view/listevents",
        "cashmgmt/view/cashmgmtinfo",
        "cashmgmt/view/listdepositsdrops",
        "cashmgmt/view/cashmgmt",

        "cashmgmt/components/cashmgmt-modals",

        // Cash Up window
        "closecash/model/cashup-steps", "closecash/model/cashup-print",
        "closecash/model/cashup-model", "closecash/view/closecash",
        "closecash/view/closekeyboard", "closecash/view/closeinfo",
        "closecash/view/tabpendingreceipts", "closecash/view/tabcountcash",
        "closecash/view/tabcashtokeep", "closecash/view/tabpostprintclose",
        "closecash/components/cashup-modals",

        "closecash/model/daycash",
        // Core resources
        "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",

        // Payment providers
        "components/mockpayments",

        // Discounts
        "model/discounts",

    };

    for (String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", POSUtils.APP_NAME));
    }

    globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
        + "components/errors.js", ComponentResource.APP_OB3));

    return globalResources;
  }
}
