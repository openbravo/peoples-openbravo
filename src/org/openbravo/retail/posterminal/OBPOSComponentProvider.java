/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
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
import org.openbravo.retail.discounts.DiscountsEngineAppComponentProvider;
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
        Arrays.asList(MobileCoreConstants.RETAIL_CORE,
            DiscountsEngineAppComponentProvider.DISCOUNTS_APP,
            TaxesEngineAppComponentProvider.TAXES_APP));
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
    globalResources = new ArrayList<>();

    final String prefix = "web/" + POSUtils.MODULE_JAVA_PACKAGE;

    final String[] resourceDependency = { "main", "model/order", "model/cashup",
        "model/cashmanagement", "login/model/login-model",
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
        "utils/ob-utilitiesuipos", "utils/externalBpUtils", "model/bpartnerfilter",
        "model/orderfilter", "model/crossstorefilter", "model/orders-for-verified-returns-filter",
        "model/bplocation", // Model
                            // for
                            // Customer
                            // Address

        "webprinter/bluetooth", "webprinter/usb", "webprinter/webprinter", "webprinter/escpos",
        "webprinter/standardprinters", "webprinter/typedarrays", "webprinter/testprinter",

        "webprinter/usbprinters/epsontmt20", "webprinter/usbprinters/epsontmt20ii",
        "webprinter/usbprinters/epsontmt88v", "webprinter/usbprinters/ncr7197",
        "webprinter/usbprinters/startsp100", "webprinter/usbprinters/wincorth230",
        "webprinter/btprinters/genericBTPrinter", "webprinter/btprinters/zebraZQ320",

        "model/productcharacteristicvalue", "model/characteristicvalue", "model/characteristic",
        "model/terminal-model", "model/paymentmethodcashup", "model/taxcashup",
        "model/orderassociationsfilter", "model/product-servicelinked", "model/prepaymentrules",

        "components/modalcancel", "components/subwindow", "components/leftsubwindow",
        "components/modalreceiptproperties", "components/modalreceiptlineproperties",
        "components/modalnoteditableorder", "components/modalnoteditableline",
        "components/modalmodulesindev", "components/modalselectprinters",
        "components/modalmorepaymentmethods", "components/modalDeliveryChange",
        "components/obpos-gridkeyboard",

        // Web POS window
        "utils/eventbus", "utils/attributeUtils", "utils/cashUpReportUtils",
        "utils/cashManagementUtils", "utils/ticketUtils", "utils/ticketCloseUtils",
        "utils/prepaymentUtils", "utils/servicesUtils", "utils/remoteUtils",
        "components/keypadcoins", "data/dataordersave", "data/dataordertaxes",
        "data/datacustomersave", "data/datacustomeraddrsave", "components/modalreceipts",
        "components/modalorderselector", "components/modalcrossstoreselector",
        "components/storeinformation", "components/modalmultiorderslayaway",
        "components/modalcategorytree", "components/rendercategory", "components/renderproduct",
        "components/renderproductch", "components/modalpayment", "components/modalprovidergroup",
        "components/standardprovider", "components/mockprovider",
        "components/modalprovidergroupvoid", "components/standardprovidervoid",
        "components/mockprovidervoid", "components/renderorderline",
        "components/rendermultiorderline", "components/order", "components/orderdetails",
        "components/businesspartner", "components/businesspartner_selector",
        "components/bplocation", // Button for selecting
                                 // customer address
        "components/bplocationship", "components/customershipbill",
        "components/listreceiptscounter", "components/menu", "components/salesrepresentative",
        "components/modalselectterminal", "components/popupdraweropened",
        "components/servicesfilter", "components/modalsafebox",
        "components/modalselectopenreceipts", "components/modalsplitlines",
        "components/modalassociatetickets", "components/modalremoveassociatedtickets",
        "components/openRelatedReceiptsModal",

        // externalBP UI components
        "components/externalbusinesspartner_viewedit",
        "components/externalbusinesspartner_selector",

        // Old Tickets
        "components/modalpaidreceipts", "components/modal-pay-open-tickets",
        "components/modalinvoices",
        // Quotations
        "components/modalcreateorderfromquotation", "components/modalreactivatequotation",
        "components/modalrejectquotation", "components/modalPriceModification",
        // Detection of change in context
        "components/modalcontextchanged", "components/modalproductcharacteristic",

        // Point of sale models

        "model/pricelist", "model/product-category-tree", "model/product", "model/productprice",
        "model/service-product", "model/service-category", "model/businesspartner",
        "model/servicepricerule", "model/servicepricerulerange",
        "model/servicepricerulerangeprices", "model/servicepriceruleversion",

        "model/obpos-supervisor-model",

        // Point of sale
        "pointofsale/view/ps-gridkeyboard", "pointofsale/view/ps-gridkeyboard-edit",
        "pointofsale/view/ps-gridkeyboard-scan", "pointofsale/model/pointofsale-print",
        "pointofsale/model/pointofsale-model", "pointofsale/model/localstock",
        "pointofsale/model/stock-checker", "pointofsale/model/otherstoresstock",
        "pointofsale/view/pointofsale", "pointofsale/view/ps-receiptview",
        "pointofsale/view/ps-multireceiptview", "pointofsale/view/ps-productdetailsview",
        "pointofsale/view/obpos-toolbar", "pointofsale/view/toolbar-left",
        "pointofsale/view/toolbar-right", "pointofsale/view/scan", "pointofsale/view/editline",
        "pointofsale/view/payment", "pointofsale/view/ticketdiscount",
        "pointofsale/view/keyboard-toolbars", "pointofsale/view/keyboardorder",

        // Point of sale subwindows
        "pointofsale/view/subwindows/customers/components/sharedcomponents",
        "pointofsale/view/subwindows/customers/editcreatecustomerform",
        "pointofsale/view/subwindows/customers/customerdetailview",

        "pointofsale/view/subwindows/customeraddress/components/sharedcomponents",
        "pointofsale/view/subwindows/customeraddress/editcreatecustomeraddress",
        "pointofsale/view/subwindows/customeraddress/customeraddrdetailview",

        "pointofsale/view/subwindows/dqm/controller-dqm",
        "pointofsale/view/subwindows/dqm/customerValidatorProvider",
        "pointofsale/view/subwindows/dqm/posterminalValidations-dqm",

        // Point of sale modals
        "pointofsale/view/modals/modalstockdiscontinued",
        "pointofsale/view/modals/modalstockinstore",
        "pointofsale/view/modals/modalstockinstoreclickable",
        "pointofsale/view/modals/modalstockinotherstore", "pointofsale/view/modals/modalpayments",
        "pointofsale/view/modals/modalproductcannotbegroup",
        "pointofsale/view/modals/modalwarehousesrequired",
        "pointofsale/view/modals/modalcreditsales", "pointofsale/view/modals/modaldiscountneedqty",
        "pointofsale/view/modals/modalmessage", "pointofsale/view/modals/modalDeleteDiscounts",
        "pointofsale/view/modals/modalproductattribute",
        "pointofsale/view/modals/modalquotationproductattribute",
        "pointofsale/view/modals/modalChange", "pointofsale/view/modals/modalChangeLine",

        // Cash Management window
        "cashmgmt/model/cashmgmt-print", "cashmgmt/model/cashmgmt-model",

        "cashmgmt/view/cashmgmtkeyboard", "cashmgmt/view/listevents", "cashmgmt/view/cashmgmtinfo",
        "cashmgmt/view/listdepositsdrops", "cashmgmt/view/cashmgmt",

        "cashmgmt/components/cashmgmt-modals",

        // Close Cash Common
        "closecash/model/closecash-steps", "closecash/model/closecash-print",
        "closecash/model/closecash-model", "closecash/view/closecash",
        "closecash/view/closekeyboard", "closecash/view/closeinfo",
        "closecash/view/tabcashpayments", "closecash/view/tabcountcash",
        "closecash/view/tabcashtokeep", "closecash/view/tabpostprintclose",
        "closecash/components/approvalreason-modal",

        // Cash Up window
        "closecash/model/cashup/cashup-steps", "closecash/model/cashup/cashup-model",
        "closecash/view/cashup/cashup", "closecash/view/cashup/tabpendingreceipts",
        "closecash/view/cashup/tabcashmaster", "closecash/view/cashup/cashuppostprintclose",
        "closecash/components/cashup/cashup-modals", "closecash/components/cashup/cashup-popups",

        // Safe Box window
        "closecash/model/countsafebox/countsafebox-steps",
        "closecash/model/countsafebox/countsafebox-model",
        "closecash/view/countsafebox/countsafebox", "closecash/view/countsafebox/tabsafeboxlist",
        "closecash/view/countsafebox/countsafeboxpostprintclose",
        "closecash/components/countsafebox/countsafebox-popups",

        // Core resources
        "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",

        // Payment providers
        "components/mockpayments",

        // Discounts
        "model/discounts",

        // Hook
        "components/menuhook",

        // Websockets
        "utils/rfidWebsocket",

        // States
        "actionstates/commonreceipt", "actionstates/commonwindow",

        // Actions
        "actions/changeprice", "actions/changequantity", "actions/convertquotation",
        "actions/createquotation", "actions/deleteline", "actions/discount", "actions/editline",
        "actions/invoicereceipt", "actions/keyboardevent", "actions/layawayreceipt",
        "actions/opendrawer", "actions/openreceipt", "actions/payopenreceipts",
        "actions/printreceipt", "actions/returnline", "actions/returnreceipt", "actions/scancode",
        "actions/selectpdfprinter", "actions/selectprinter", "actions/showreceiptproperties",
        "actions/showstockline", "actions/splitline",

        "utils/preScanningFocusHook", "utils/orderSelectorUtils", "utils/stockUtils",
        "utils/productStatusUtils",

        // Delivery Mode
        "utils/deliveryModeUtils", "hookPreDeleteLine", "hookPostUndo_DeleteLine", "receiptEdit",
        "receiptMultiLineEdit", "hookRenderOrderLine", "hookPrePayment",
        "hookPreSetPaymentsToReceipt", "components/menuissue", "model/dmorderfilter", "utils/utils",

        "model/ordertoselectorissue", "components/orderselector",
        "components/modalwarehouseselector", "components/orderlineinfopopup",
        "components/orderlineinfostandardpopup",

        // Taxes
        "taxesengine/pos/pos-taxes",

        // Discounts
        "discountsengine/pos/pos-discounts",

        // Ticket List
        "utils/ticketListUtils" };

    // Unlink onLoad in the ERP
    globalResources.add(createComponentResource(ComponentResourceType.Static,
        prefix + "/js/utils/unlinkDeviceOnLoad.js", ComponentResource.APP_OB3));
    globalResources.add(createComponentResource(ComponentResourceType.Static,
        prefix + "/js/utils/unlinkDeviceValidation.js", ComponentResource.APP_OB3));
    globalResources.add(createComponentResource(ComponentResourceType.Static,
        prefix + "/js/utils/checkChToFilterInWebPos.js", ComponentResource.APP_OB3));

    // Buttons to Open Hardware Manager
    globalResources.add(createComponentResource(ComponentResourceType.Static,
        prefix + "/js/utils/openhardwareurl.js", ComponentResource.APP_OB3));

    final String[] cssDependency = { "obpos-main" };

    final ArrayList<String> appDependency = new ArrayList<>();

    // Masterdata
    appDependency.add("model/masterdata/BPCategoryModel");
    appDependency.add("model/masterdata/BPSetLineModel");
    appDependency.add("model/masterdata/BusinessPartnerModel");
    appDependency.add("model/masterdata/BusinessPartnerLocationModel");
    appDependency.add("model/masterdata/CountryModel");
    appDependency.add("model/masterdata/PriceListModel");
    appDependency.add("model/masterdata/ProductModel");
    appDependency.add("model/masterdata/ProductPriceModel");
    appDependency.add("model/masterdata/ProductCategoryTreeModel");
    appDependency.add("model/masterdata/CharacteristicModel");
    appDependency.add("model/masterdata/CharacteristicValueModel");
    appDependency.add("model/masterdata/ProductCharacteristicValueModel");
    appDependency.add("model/masterdata/ProductBOMModel");
    appDependency.add("model/masterdata/ProductServiceLinkedModel");
    appDependency.add("model/masterdata/SalesRepresentativeModel");
    appDependency.add("model/masterdata/ServicePriceRuleModel");
    appDependency.add("model/masterdata/ServicePriceRuleVersionModel");
    appDependency.add("model/masterdata/ServicePriceRuleRangeModel");
    appDependency.add("model/masterdata/ServicePriceRuleRangePricesModel");
    appDependency.add("model/masterdata/ServiceProductModel");
    appDependency.add("model/masterdata/ServiceProductCategoryModel");
    appDependency.add("model/masterdata/TaxCategoryModel");
    appDependency.add("model/masterdata/TaxCategoryBOMModel");
    appDependency.add("model/masterdata/TaxRateModel");
    appDependency.add("model/masterdata/TaxZoneModel");
    appDependency.add("model/masterdata/discount/DiscountFilterBusinessPartnerGroupModel");
    appDependency.add("model/masterdata/discount/DiscountFilterBusinessPartnerModel");
    appDependency.add("model/masterdata/discount/DiscountFilterBusinessPartnerSetModel");
    appDependency.add("model/masterdata/discount/DiscountFilterCharacteristicModel");
    appDependency.add("model/masterdata/discount/DiscountFilterPriceListModel");
    appDependency.add("model/masterdata/discount/DiscountFilterProductCategoryModel");
    appDependency.add("model/masterdata/discount/DiscountFilterProductModel");
    appDependency.add("model/masterdata/discount/DiscountFilterRoleModel");
    appDependency.add("model/masterdata/discount/DiscountFilterBusinessPartnerExtRef");
    appDependency.add("model/masterdata/discount/DiscountModel");
    // Business-Logic
    appDependency.add("model/business-logic/stock/StockChecker");
    appDependency.add("model/business-logic/pack/ProductPack");
    appDependency.add("model/business-logic/pack/ProductPackProvider");
    appDependency.add("model/business-logic/pack/Pack");
    appDependency.add("model/business-logic/service/ServicesFilter");
    // Business-Object
    appDependency.add("model/business-object/document-sequence/DocumentSequence");
    appDependency.add("model/business-object/document-sequence/DocumentSequenceUtils");
    appDependency.add("model/business-object/document-sequence/actions/InitializeSequence");
    appDependency.add("model/business-object/document-sequence/actions/IncreaseSequence");
    appDependency.add("model/business-object/document-sequence/actions/DecreaseSequence");

    appDependency.add("model/business-object/business-partner/SynchronizeBusinessPartner");
    appDependency.add("model/business-object/business-partner/SynchronizeBusinessPartnerLocation");

    appDependency.add("model/business-object/safebox/SynchronizeCountSafeBox");

    // ticket model
    appDependency.add("model/business-object/ticket/Ticket");
    appDependency.add("model/business-object/ticket/TicketExtraProperties");
    appDependency.add("model/business-object/ticket/TicketUtils");
    appDependency.add("model/business-object/ticket/CompleteTicketUtils");
    appDependency.add("model/business-object/ticket/actions/CompleteTicket");
    appDependency.add("model/business-object/ticket/actions/CancelTicket");
    appDependency.add("model/business-object/ticket/actions/ReplaceTicket");
    appDependency.add("model/business-object/ticket/actions/CompleteQuotation");
    appDependency.add("model/business-object/ticket/actions/CompleteLayaway");
    appDependency.add("model/business-object/ticket/actions/VoidLayaway");
    appDependency.add("model/business-object/ticket/actions/DeleteTicket");
    appDependency.add("model/business-object/ticket/AddApprovalsModelHook");
    appDependency.add("model/business-object/ticket/CalculateTotalsModelHook");
    appDependency.add("model/business-object/ticket/actions/AddProduct");
    appDependency.add("model/business-object/ticket/actions/CreateEmptyTicket");
    appDependency.add("model/business-object/ticket/actions/DisplayTotal");
    appDependency.add("model/business-object/ticket/actions/SplitLine");
    appDependency.add("model/business-object/ticket/actions/SetLinePrice");
    appDependency.add("model/business-object/ticket/actions/AddByTotalPromotion");
    appDependency.add("model/business-object/ticket/actions/RemovePromotion");
    appDependency.add("model/business-object/ticket/actions/ReactivateQuotation");
    appDependency.add("model/business-object/ticket/actions/RejectQuotation");
    appDependency.add("model/business-object/ticket/actions/SyncTickets"); // TODO: Remove when
                                                                           // close ticket actions
                                                                           // are available
    appDependency.add("model/business-object/ticket/actions/RemovePayment");

    // Cashup
    appDependency.add("model/business-object/cashup/Cashup");
    appDependency.add("model/business-object/cashup/CashupUtils");
    appDependency.add("model/business-object/cashup/PaymentMethodUtils");
    appDependency.add("model/business-object/cashup/CashManagementUtils");
    appDependency.add("model/business-object/cashup/actions/CancelCashManagements");
    appDependency.add("model/business-object/cashup/actions/CompleteCashupAndCreateNew");
    appDependency.add("model/business-object/cashup/actions/CreateCashManagement");
    appDependency.add("model/business-object/cashup/actions/InitCashup");
    appDependency.add("model/business-object/cashup/actions/ProcessCashManagements");
    appDependency.add("model/business-object/cashup/actions/UpdateCashup");

    // ticket list model
    appDependency.add("model/business-object/ticket-list/TicketList");
    appDependency.add("model/business-object/ticket-list/TicketListUtils");
    appDependency.add("model/business-object/ticket-list/actions/AddNewTicket");
    appDependency.add("model/business-object/ticket-list/actions/AddNewQuotation");
    appDependency.add("model/business-object/ticket-list/actions/BringTicket");
    appDependency.add("model/business-object/ticket-list/actions/LoadTicket");
    appDependency
        .add("model/business-object/ticket-list/actions/MarkIgnoreCheckIfIsActiveToPendingTickets");
    appDependency.add("model/business-object/ticket-list/actions/UpdateBPInAllTickets");
    appDependency.add("model/business-object/ticket-list/actions/SaveTicket");

    // Utilities
    appDependency.add("util/OpenTicketList");

    // Synchronization Buffer
    appDependency.add("model/synchronization-buffer/HardwareManagerEndpoint");

    // External Device
    appDependency.add("external-device/actions/Print");
    appDependency.add("external-device/actions/PrintLine");
    appDependency.add("external-device/actions/PrintWelcome");

    for (final String resource : appDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static,
          prefix + "/app/" + resource + ".js", POSUtils.APP_NAME));
    }

    for (final String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static,
          prefix + "/js/" + resource + ".js", POSUtils.APP_NAME));
    }

    globalResources.add(createComponentResource(ComponentResourceType.Static,
        prefix + "/js/components/errors.js", ComponentResource.APP_OB3));

    for (final String resource : cssDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Stylesheet,
          prefix + "/css/" + resource + ".css", POSUtils.APP_NAME));
    }

    return globalResources;
  }
}
