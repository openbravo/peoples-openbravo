/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.mobile.core.MobileCoreConstants;
import org.openbravo.retail.posterminal.locale.POSApplicationFormatComponent;

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
  public static final String APP_FORMAT = "ApplicationFormats";

  private List<ComponentResource> globalResources = null;

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    final BaseComponent component = createComponent(componentId, parameters);
    component.setId(componentId);
    component.setParameters(parameters);
    return component;
  }

  public BaseComponent createComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(MobileCoreConstants.APP_CACHE_COMPONENT)) {
      return getComponent(ApplicationCacheComponent.class);
    } else if (componentId.equals(APP_FORMAT)) {
      return getComponent(POSApplicationFormatComponent.class);
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public synchronized List<ComponentResource> getGlobalComponentResources() {
    if (globalResources != null) {
      return globalResources;
    }
    globalResources = new ArrayList<ComponentResource>();

    final String prefix = "web/" + POSUtils.MODULE_JAVA_PACKAGE + "/js/";

    final String[] resourceDependency = {
        "main",
        "model/changedbusinesspartners",
        "model/changedbplocation",
        "model/order",
        "model/cashup",
        "model/cashmanagement",
        "model/cancelLayaway",
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
        "model/bplocation", // Model for Customer
                            // Address
        "model/currencypanel",
        "model/salesrepresentative",
        "model/productcharacteristicvalue",
        "model/characteristicvalue",
        "model/characteristic",
        "model/brand",
        "model/returnreasons",
        "model/executor",
        "model/terminal-model",
        "model/paymentmethodcashup",
        "model/taxcashup",
        "model/returnreasons",
        "model/offlineprinter",
        "model/productbom",
        "model/taxcategorybom",
        "components/modalcancel",
        "components/subwindow",
        "components/leftsubwindow",
        "components/modalreceiptproperties",
        "components/modalreceiptlineproperties",
        "components/modalnoteditableorder",
        "components/modalnoteditableline",
        "components/modalmodulesindev",
        // Web POS window
        "utils/eventbus",
        "utils/cashUpReportUtils",
        "utils/cashManagementUtils",
        "components/keypadcoins",
        "data/dataordersave",
        "data/dataordertaxes",
        "data/datacustomersave",
        "data/datacustomeraddrsave",
        "data/dataorderdiscount",
        "components/modalreceipts",
        "components/modalmultiorderslayaway",
        "components/modalcategorytree",
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
        "components/bplocationship",
        "components/customershipbill",
        "components/salesrepresentative",
        "components/listreceiptscounter",
        "components/menu",
        "components/modalselectterminal",
        "components/popupdraweropened",
        "components/servicesfilter",
        "components/modalselectopenreceipts",
        "components/modalsplitlines",

        // Old Tickets
        "components/modalpaidreceipts",
        "components/modalmultiorders",
        // Quotations
        "components/modalcreateorderfromquotation",
        "components/modalreactivatequotation",
        "components/modalrejectquotation",
        // Detection of change in context
        "components/modalcontextchanged",
        "components/modalproductcharacteristic",
        "components/modalproductbrand",

        // Point of sale models
        "model/pricelist",
        "model/product-category",
        "model/product-category-tree",
        "model/product",
        "model/productprice",
        "model/offerpricelist",
        "model/service-product",
        "model/service-category",
        "model/businesspartner",
        "model/document-sequence",
        "model/taxRate",
        "model/taxZone",
        "model/promotions",

        "model/servicepricerule",
        "model/servicepricerulerange",
        "model/servicepricerulerangeprices",
        "model/servicepriceruleversion",

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
        "pointofsale/view/subwindows/customeraddress/customeraddrshipsearch",
        "pointofsale/view/subwindows/customeraddress/customeraddrdetailview",
        // Point of sale modals
        "pointofsale/view/modals/modalstockinstore",
        "pointofsale/view/modals/modalstockinstoreclickable",
        "pointofsale/view/modals/modalstockinotherstore",
        "pointofsale/view/modals/modalpayments",
        "pointofsale/view/modals/modalproductcannotbegroup",
        "pointofsale/view/modals/modalwarehousesrequired",
        "pointofsale/view/modals/modalcreditsales",
        "pointofsale/view/modals/modaldiscountneedqty",
        "pointofsale/view/modals/modalmessage",

        // Cash Management window
        "cashmgmt/model/cashmgmt-print",
        "cashmgmt/model/cashmgmt-model",

        "cashmgmt/view/cashmgmtkeyboard",
        "cashmgmt/view/listevents",
        "cashmgmt/view/cashmgmtinfo",
        "cashmgmt/view/listdepositsdrops",
        "cashmgmt/view/cashmgmt",

        "cashmgmt/components/cashmgmt-modals",

        // Cash Up window
        "closecash/model/cashup-steps", "closecash/model/cashup-print",
        "closecash/model/cashup-model", "closecash/view/closecash", "closecash/view/closekeyboard",
        "closecash/view/closeinfo", "closecash/view/tabpendingreceipts",
        "closecash/view/tabcashmaster", "closecash/view/tabcashpayments",
        "closecash/view/tabcountcash", "closecash/view/tabcashtokeep",
        "closecash/view/tabpostprintclose", "closecash/components/cashup-modals",
        "closecash/components/approvalreason-modal",

        "closecash/model/daycash",
        // Core resources
        "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",

        // Payment providers
        "components/mockpayments",

        // Discounts
        "model/discounts",

        // Hook
        "components/menuhook", "components/hookPreDeleteLine",

        // Websockets
        "utils/rfidWebsocket",

        "utils/preScanningFocusHook" };

    // Unlink onLoad in the ERP
    globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
        + "utils/unlinkDeviceOnLoad.js", ComponentResource.APP_OB3));

    final String[] cssDependency = { "pos-login", "obpos-main" };

    for (final String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", POSUtils.APP_NAME));
    }

    globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
        + "components/errors.js", ComponentResource.APP_OB3));

    for (final String resource : cssDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Stylesheet, prefix
          + "../css/" + resource + ".css", POSUtils.APP_NAME));
    }

    return globalResources;
  }
}
