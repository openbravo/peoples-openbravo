/*
 ************************************************************************************
 * Copyright (C) 2013-2022 Openbravo S.L.U.
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
    // Set dependency on Business logic for POS
    BaseComponentProvider.setAppDependencies(POSUtils.BUSINESSLOGIC_NAME, Arrays.asList( //
        MobileCoreConstants.BUSINESSLOGIC_NAME, //
        DiscountsEngineAppComponentProvider.DISCOUNTS_APP, //
        TaxesEngineAppComponentProvider.TAXES_APP));
    // Set dependency on Mobile Core app
    BaseComponentProvider.setAppDependencies(POSUtils.APP_NAME, Arrays.asList( //
        MobileCoreConstants.RETAIL_CORE, //
        POSUtils.BUSINESSLOGIC_NAME));
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
        "model/bplocation", "model/productcharacteristicvalue", "model/characteristicvalue",
        "model/characteristic", "model/terminal-model", "model/paymentmethodcashup",
        "model/taxcashup", "model/orderassociationsfilter", "model/product-servicelinked",
        "model/prepaymentrules",

        "components/modalcancel", "components/subwindow", "components/leftsubwindow",
        "components/modalreceiptproperties", "components/modalreceiptlineproperties",
        "components/modalnoteditableorder", "components/modalnoteditableline",
        "components/modalmodulesindev", "components/modalselectprinters",
        "components/modalmorepaymentmethods", "components/modalDeliveryChange",
        "components/obpos-gridkeyboard",

        // WebPrinter test menu entry
        "webprinter/testprinter",

        // Web POS window
        "utils/eventbus", "utils/attributeUtils", "utils/cashUpReportUtils",
        "utils/cashManagementUtils", "utils/ticketUtils", "utils/prepaymentUtils",
        "utils/servicesUtils", "utils/remoteUtils", "components/keypadcoins", "data/dataordertaxes",
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
        "model/discounts", "model/discountsbusinesspartner", "model/pricelist",
        "model/product-category-tree", "model/product", "model/productprice",
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

        "pointofsale/view/subwindows/dqm/posterminalValidations-dqm",

        // Point of sale modals
        "pointofsale/view/modals/modalstockdiscontinued",
        "pointofsale/view/modals/modalstockinstore",
        "pointofsale/view/modals/modalstockinstoreclickable",
        "pointofsale/view/modals/modalstockinotherstore", "pointofsale/view/modals/modalpayments",
        "pointofsale/view/modals/modalproductcannotbegroup",
        "pointofsale/view/modals/modalwarehousesrequired",
        "pointofsale/view/modals/modaldiscountneedqty", "pointofsale/view/modals/modalmessage",
        "pointofsale/view/modals/modalDeleteDiscounts",
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

        // Hook
        "components/menuhook",

        // Websockets
        "utils/rfidWebsocket",

        // Action Preparation
        "actionpreparation/AddPaymentCheck", "actionpreparation/AddProductApproval",
        "actionpreparation/CompleteLayawayApproval", "actionpreparation/CompleteTicketApproval",
        "actionpreparation/CompleteTicketCheck", "actionpreparation/DeleteLineApproval",
        "actionpreparation/SetLinePriceApproval", "actionpreparation/ReturnLineApproval",
        "actionpreparation/ReturnBlindTicketApproval", "actionpreparation/ReversePaymentApproval",

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
        "utils/deliveryModeUtils", "receiptEdit", "receiptMultiLineEdit", "hookRenderOrderLine",
        "hookPrePayment", "hookPreSetPaymentsToReceipt", "components/menuissue",
        "model/dmorderfilter", "utils/utils",

        "model/ordertoselectorissue", "components/orderselector",
        "components/modalwarehouseselector", "components/orderlineinfopopup",
        "components/orderlineinfostandardpopup",

        // Taxes
        "taxesengine/pos/pos-taxes",

        // Terminal Log
        "utils/terminal-log-context",

        // Discounts
        "discountsengine/pos/pos-discounts",

        // Ticket List
        "utils/ticketListUtils",

        // Loss sales
        "utils/lossSalesUtils", "components/modallosssales" };

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

    // BusinessLogicEngine
    globalResources.addAll(Arrays.asList( //

        // Masterdata
        businesslogic("app/model/masterdata/BPCategoryModel"), //
        businesslogic("app/model/masterdata/BPSetLineModel"), //
        businesslogic("app/model/masterdata/BusinessPartnerModel"), //
        businesslogic("app/model/masterdata/BusinessPartnerLocationModel"), //
        businesslogic("app/model/masterdata/CountryModel"), //
        businesslogic("app/model/masterdata/PriceListModel"), //
        businesslogic("app/model/masterdata/ProductModel"), //
        businesslogic("app/model/masterdata/ProductPriceModel"), //
        businesslogic("app/model/masterdata/ProductCategoryTreeModel"), //
        businesslogic("app/model/masterdata/CharacteristicModel"), //
        businesslogic("app/model/masterdata/CharacteristicValueModel"), //
        businesslogic("app/model/masterdata/ProductCharacteristicValueModel"), //
        businesslogic("app/model/masterdata/ProductBOMModel"), //
        businesslogic("app/model/masterdata/ProductServiceLinkedModel"), //
        businesslogic("app/model/masterdata/SalesRepresentativeModel"), //
        businesslogic("app/model/masterdata/ServicePriceRuleModel"), //
        businesslogic("app/model/masterdata/ServicePriceRuleVersionModel"), //
        businesslogic("app/model/masterdata/ServicePriceRuleRangeModel"), //
        businesslogic("app/model/masterdata/ServicePriceRuleRangePricesModel"), //
        businesslogic("app/model/masterdata/ServiceProductModel"), //
        businesslogic("app/model/masterdata/ServiceProductCategoryModel"), //
        businesslogic("app/model/masterdata/TaxCategoryModel"), //
        businesslogic("app/model/masterdata/TaxCategoryBOMModel"), //
        businesslogic("app/model/masterdata/TaxRateModel"), //
        businesslogic("app/model/masterdata/TaxZoneModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterBusinessPartnerGroupModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterBusinessPartnerModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterBusinessPartnerSetModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterCharacteristicModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterPriceListModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterProductCategoryModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterProductModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterIncompatibilityModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterAvailabilityModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterRoleModel"), //
        businesslogic("app/model/masterdata/discount/DiscountFilterBusinessPartnerExtRef"), //
        businesslogic("app/model/masterdata/discount/DiscountModel"), //

        // WebPrinter. WebUSB and WebBluetooth
        businesslogic("app/webprinter/bluetooth"), //
        businesslogic("app/webprinter/usb"), //
        businesslogic("app/webprinter/webprinter"), //
        businesslogic("app/webprinter/escpos"), //
        businesslogic("app/webprinter/standardprinters"), //
        businesslogic("app/webprinter/typedarrays"), //
        businesslogic("app/webprinter/usbprinters/epsontmt20"), //
        businesslogic("app/webprinter/usbprinters/epsontmt20ii"), //
        businesslogic("app/webprinter/usbprinters/epsontmt88v"), //
        businesslogic("app/webprinter/usbprinters/ncr7197"), //
        businesslogic("app/webprinter/usbprinters/startsp100"), //
        businesslogic("app/webprinter/usbprinters/wincorth230"), //
        businesslogic("app/webprinter/usbprinters/hpa799"), //
        businesslogic("app/webprinter/btprinters/genericBTPrinter"), //
        businesslogic("app/webprinter/btprinters/zebraZQ320"), //

        // Business-Logic
        businesslogic("app/model/business-logic/currency/CurrencyConversion"), //
        businesslogic("app/model/business-logic/stock/StockChecker"), //
        businesslogic("app/model/business-logic/pack/ProductPack"), //
        businesslogic("app/model/business-logic/pack/ProductPackProvider"), //
        businesslogic("app/model/business-logic/pack/Pack"), //
        businesslogic("app/model/business-logic/service/ServicesFilter"), //
        businesslogic("app/model/business-logic/utils/Util"), //
        // Business-Object
        businesslogic("app/model/business-object/document-sequence/DocumentSequence"), //
        businesslogic("app/model/business-object/document-sequence/DocumentSequenceUtils"), //
        businesslogic("app/model/business-object/document-sequence/actions/InitializeSequence"), //
        businesslogic("app/model/business-object/document-sequence/actions/IncreaseSequence"), //
        businesslogic("app/model/business-object/document-sequence/actions/DecreaseSequence"), //

        businesslogic(
            "app/model/business-object/business-partner/actions/SynchronizeBusinessPartner"), //
        businesslogic(
            "app/model/business-object/business-partner/actions/SynchronizeBusinessPartnerLocation"), //

        businesslogic("app/model/business-object/safebox/SynchronizeCountSafeBox"), //

        // ticket model
        businesslogic("app/model/business-object/ticket/Ticket"), //
        businesslogic("app/model/business-object/ticket/TicketUtils"), //
        businesslogic("app/model/business-object/ticket/CompleteTicketUtils"), //
        businesslogic("app/model/business-object/ticket/AddProductUtils"), //
        businesslogic("app/model/business-object/ticket/LoadTicketUtils"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteCreditTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CancelTicket"), //
        businesslogic("app/model/business-object/ticket/actions/ReplaceTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteQuotation"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteLayaway"), //
        businesslogic("app/model/business-object/ticket/actions/VoidLayaway"), //
        businesslogic("app/model/business-object/ticket/actions/DeleteTicket"), //
        businesslogic("app/model/business-object/ticket/actions/ReturnBlindTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteMultiTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CompleteMultiCreditTicket"), //
        businesslogic("app/model/business-object/ticket/CompleteMultiTicketUtils"), //
        businesslogic("app/model/business-object/ticket/AddApprovalsModelHook"), //
        businesslogic("app/model/business-object/ticket/CalculateTotalsModelHook"), //
        businesslogic("app/model/business-object/ticket/actions/AddProduct"), //
        businesslogic("app/model/business-object/ticket/actions/CreateEmptyTicket"), //
        businesslogic("app/model/business-object/ticket/actions/DisplayTotal"), //
        businesslogic("app/model/business-object/ticket/actions/PrintDocument"), //
        businesslogic("app/model/business-object/ticket/actions/SplitLine"), //
        businesslogic("app/model/business-object/ticket/actions/SetLinePrice"), //
        businesslogic("app/model/business-object/ticket/actions/AddByTotalPromotion"), //
        businesslogic("app/model/business-object/ticket/actions/RemovePromotion"), //
        businesslogic("app/model/business-object/ticket/actions/ReactivateQuotation"), //
        businesslogic("app/model/business-object/ticket/actions/RejectQuotation"), //
        businesslogic("app/model/business-object/ticket/actions/CreateTicketFromQuotation"), //
        businesslogic("app/model/business-object/ticket/actions/DeleteLine"), //
        businesslogic("app/model/business-object/ticket/actions/ReturnLine"), //
        businesslogic("app/model/business-object/ticket/actions/AssignExternalBusinessPartner"), //
        businesslogic("app/model/business-object/ticket/actions/ConvertTicketIntoQuotation"), //
        businesslogic("app/model/business-object/ticket/actions/CreateCancelTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CreateReplaceTicket"), //
        businesslogic("app/model/business-object/ticket/actions/CheckTicketForPayOpenTickets"), //
        businesslogic("app/model/business-object/ticket/actions/DeletePayment"), //
        businesslogic("app/model/business-object/ticket/AddPaymentUtils"), //
        businesslogic("app/model/business-object/ticket/actions/AddPayment"), //
        businesslogic("app/model/business-object/ticket/actions/AddPaymentRounding"), //
        businesslogic("app/model/business-object/ticket/ReversePaymentUtils"), //
        businesslogic("app/model/business-object/ticket/actions/ReversePayment"), //
        businesslogic("app/model/business-object/ticket/actions/SetDeliveryMode"), //

        // Cashup
        businesslogic("app/model/business-object/cashup/Cashup"), //
        businesslogic("app/model/business-object/cashup/CashupUtils"), //
        businesslogic("app/model/business-object/cashup/PaymentMethodUtils"), //
        businesslogic("app/model/business-object/cashup/CashManagementUtils"), //
        businesslogic("app/model/business-object/cashup/actions/CancelCashManagements"), //
        businesslogic("app/model/business-object/cashup/actions/CompleteCashupAndCreateNew"), //
        businesslogic("app/model/business-object/cashup/actions/CreateCashManagement"), //
        businesslogic("app/model/business-object/cashup/actions/InitCashup"), //
        businesslogic("app/model/business-object/cashup/actions/ProcessCashManagements"), //

        // Messages
        businesslogic("app/model/business-object/messages/MessagesUtils"), //

        // ticket list model
        businesslogic("app/model/business-object/ticket-list/TicketList"), //
        businesslogic("app/model/business-object/ticket-list/TicketListUtils"), //
        businesslogic("app/model/business-object/ticket-list/actions/AddNewTicket"), //
        businesslogic("app/model/business-object/ticket-list/actions/AddNewQuotation"), //
        businesslogic("app/model/business-object/ticket-list/actions/BringTicketToSession"), //
        businesslogic("app/model/business-object/ticket-list/actions/LoadLocalTicket"), //
        businesslogic("app/model/business-object/ticket-list/actions/LoadRemoteTicket"), //
        businesslogic(
            "app/model/business-object/ticket-list/actions/MarkIgnoreCheckIfIsActiveToPendingTickets"), //
        businesslogic("app/model/business-object/ticket-list/actions/UpdateBPInAllTickets"), //
        businesslogic("app/model/business-object/ticket-list/actions/SaveTicket"), //

        // Remote Server
        businesslogic("app/integration/remote-server/HardwareManagerServer"), //
        // Synchronization Buffer
        businesslogic("app/model/synchronization-buffer/HardwareManagerEndpoint"), //

        // External Device
        businesslogic("app/external-device/ExternalDeviceController"), //
        businesslogic("app/external-device/actions/InitHardwareManager"), //
        businesslogic("app/external-device/actions/PrintLine"), //
        businesslogic("app/external-device/actions/PrintTicket"), //
        businesslogic("app/external-device/actions/PrintWelcome"), //
        businesslogic("app/external-device/printing/PrintTemplate"), //
        businesslogic("app/external-device/printing/PrintTemplateStore"), //
        businesslogic("app/external-device/printing/PrintUtils"), //
        businesslogic("app/external-device/printing/TicketPrinter"), //
        businesslogic("app/external-device/printing/CashupPrinter"), //
        businesslogic("app/external-device/printing/CashupKeptCashPrinter")));

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

  private ComponentResource js(String resource, String app) {
    return createComponentResource(ComponentResourceType.Static,
        "web/" + POSUtils.MODULE_JAVA_PACKAGE + "/" + resource + ".js", app);
  }

  private ComponentResource businesslogic(String resource) {
    return js(resource, POSUtils.BUSINESSLOGIC_NAME);
  }
}
