/*
 ************************************************************************************
 * Copyright (C) 2019-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutsContainer',
  classes: 'obObposPointOfSaleUiBottomRightGridLayoutsContainer',
  tabsToCheck: ['scan', 'edit']
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayout',
  classes: 'obObposPointOfSaleUiBottomRightGridLayout',
  applyClassesAppliedToChildAbas: true
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutScan',
  kind: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayout',
  activeTab: 'scan',
  classes: 'obObposPointOfSaleUiBottomRightGridLayoutScan',
  components: [
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'bottomRightScanAba1',
      abaIdentifier: 'obpos_pointofsale_scan_bottomrightaba1',
      classes:
        'obObposPointOfSaleUiBottomRightGridLayout-obUiActionButtonArea-generic obObposPointOfSaleUiBottomRightGridLayoutScan-bottomRightScanAba1'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.GridKeyboardScan',
      name: 'bottomRightScanKeyboard',
      classes:
        'obObposPointOfSaleUiBottomRightGridLayoutScan-bottomRightScanKeyboard'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutEdit',
  kind: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayout',
  activeTab: 'edit',
  classes: 'obObposPointOfSaleUiBottomRightGridLayoutEdit',
  components: [
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'bottomRightEditAba1',
      abaIdentifier: 'obpos_pointofsale_edit_bottomrightaba1',
      classes:
        'obObposPointOfSaleUiBottomRightGridLayout-obUiActionButtonArea-generic obObposPointOfSaleUiBottomRightGridLayoutEdit-bottomRightEditAba1'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.GridKeyboardEdit',
      name: 'bottomRightEditKeyboard',
      classes:
        'obObposPointOfSaleUiBottomRightGridLayoutEdit-bottomRightEditKeyboard'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightBottomPanelWrapper',
  classes: 'obObposPointOfSaleUiRightBottomPanelWrapper',
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutsContainer',
      name: 'bottomRightGridLayoutsContainer',
      showing: false,
      classes:
        'obObposPointOfSaleUiRightBottomPanelWrapper-bottomRightGridLayout',
      components: [
        {
          kind: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutScan',
          name: 'bottomRightScan',
          classes:
            'obObposPointOfSaleUiRightBottomPanelWrapper-bottomRightGridLayout-bottomRightScan'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.BottomRightGridLayoutEdit',
          name: 'bottomRightEdit',
          classes:
            'obObposPointOfSaleUiRightBottomPanelWrapper-bottomRightGridLayout-bottomRightEdit'
        }
      ]
    },
    {
      name: 'keyboardWrapper',
      showing: true,
      classes: 'obObposPointOfSaleUiRightBottomPanelWrapper-keyboardWrapper',
      components: [
        {
          kind: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
          name: 'keyboard',
          classes:
            'obObposPointOfSaleUiRightBottomPanelWrapper-keyboardWrapper-keyboard'
        }
      ]
    }
  ],
  tabChanged: function(newTab) {
    if (
      this.$.bottomRightGridLayoutsContainer.tabsToCheck.indexOf(newTab) !== -1
    ) {
      this.showGridLayout(newTab);
      return;
    }
    this.showLegacyKeyboard();
  },
  showLegacyKeyboard: function(newTab) {
    this.$.keyboardWrapper.setShowing(true);
    this.$.bottomRightGridLayoutsContainer.setShowing(false);
  },
  showGridLayout: function(newTab) {
    var showGridLayout = false;
    this.$.keyboardWrapper.setShowing(false);
    this.$.bottomRightGridLayoutsContainer.setShowing(true);
    enyo.forEach(
      this.getComponents(),
      function(comp) {
        if (comp.activeTab) {
          comp.setShowing(false);
        }
        if (comp.activeTab === newTab) {
          enyo.forEach(
            comp.getComponents(),
            function(subComp) {
              if (subComp.configuredToBeVisible) {
                comp.setShowing(true);
                showGridLayout = true;
              }
            },
            this
          );
        }
      },
      this
    );
    this.$.keyboardWrapper.setShowing(!showGridLayout);
    this.$.bottomRightGridLayoutsContainer.setShowing(showGridLayout);
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  classes: 'obObposPointOfSaleUiPointOfSale',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  allowedIncrementalRefresh: true,
  incrementalRefreshOnNavigate: true,
  handlers: {
    onAddProduct: 'addProductToOrder',
    onViewProductDetails: 'viewProductDetails',
    onCloseProductDetailsView: 'showOrder',
    onCancelReceiptToInvoice: 'cancelReceiptToInvoice',
    onReceiptToInvoice: 'receiptToInvoice',
    onCreateQuotation: 'createQuotation',
    onCreateOrderFromQuotation: 'createOrderFromQuotation',
    onShowCreateOrderPopup: 'showCreateOrderPopup',
    onReactivateQuotation: 'reactivateQuotation',
    onShowReactivateQuotation: 'showReactivateQuotation',
    onShowRejectQuotation: 'showRejectQuotation',
    onRejectQuotation: 'rejectQuotation',
    onQuotations: 'quotations',
    onShowDivText: 'showDivText',
    onAddNewOrder: 'addNewOrder',
    onDeleteOrder: 'deleteCurrentOrder',
    onTabChange: 'tabChange',
    onDeleteLine: 'deleteLine',
    onEditLine: 'editLine',
    onReturnLine: 'returnLine',
    onDeliveryPayment: 'deliveryPayment',
    onExactPayment: 'exactPayment',
    onRemovePayment: 'removePayment',
    onReversePayment: 'reversePayment',
    onChangeCurrentOrder: 'changeCurrentOrder',
    onChangeBusinessPartner: 'changeBusinessPartner',
    onPrintReceipt: 'printReceipt',
    onPrintSingleReceipt: 'printSingleReceipt',
    onBackOffice: 'backOffice',
    onVerifiedReturns: 'verifiedReturns',
    onChangeSubWindow: 'changeSubWindow',
    onShowLeftSubWindow: 'showLeftSubWindow',
    onCloseLeftSubWindow: 'showOrder',
    onSetProperty: 'setProperty',
    onSetLineProperty: 'setLineProperty',
    onSetReceiptsList: 'setReceiptsList',
    onShowReceiptProperties: 'showModalReceiptProperties',
    onDiscountsMode: 'discountsMode',
    onDiscountsModeFinished: 'discountsModeFinished',
    onDisableLeftToolbar: 'leftToolbarDisabled',
    onDisableBPSelection: 'BPSelectionDisabled',
    onDisableBPLocSelection: 'BPLocSelectionDisabled',
    onDisableNewBP: 'newBPDisabled',
    onDisableNewBPLoc: 'newBPLocDisabled',
    onDisableOrderSelection: 'orderSelectionDisabled',
    onDisableKeyboard: 'keyboardDisabled',
    onDiscountsModeKeyboard: 'keyboardOnDiscountsMode',
    onCheckAllTicketLines: 'allTicketLinesChecked',
    onSetDiscountQty: 'discountQtyChanged',
    onLineChecked: 'checkedLine',
    onStatusChanged: 'statusChanged',
    onPaymentChanged: 'paymentChanged',
    onPaymentChangedCancelled: 'paymentChangedCancelled',
    onPaymentActionPay: 'paymentActionPay',
    onClearPaymentSelect: 'clearPaymentSelect',
    onLayaways: 'layaways',
    onChangeSalesRepresentative: 'changeSalesRepresentative',
    onMaxLimitAmountError: 'maxLimitAmountError',
    onMultiOrders: 'multiOrders',
    onSelectMultiOrders: 'selectMultiOrders',
    onRemoveMultiOrders: 'removeMultiOrders',
    onRightToolDisabled: 'rightToolbarDisabled',
    onSelectCharacteristicValue: 'selectCharacteristicValue',
    onSelectFilter: 'selectFilter',
    onSelectCategoryTreeItem: 'selectCategoryTreeItem',
    onShowLeftHeader: 'doShowLeftHeader',
    onWarehouseSelected: 'warehouseSelected',
    onClearUserInput: 'clearUserInput',
    onPricelistChanged: 'pricelistChanged',
    onChangeDiscount: 'changeDiscount',
    onReceiptLineSelected: 'receiptLineSelected',
    onManageServiceProposal: 'manageServiceProposal',
    onDisableUserInterface: 'disableUserInterface',
    onEnableUserInterface: 'enableUserInterface',
    onShowActionIcons: 'showActionIcons',
    onSetMultiSelection: 'setMultiSelection',
    onShowMultiSelection: 'showMultiSelection',
    onSetMultiSelectionItems: 'setMultiSelectionItems',
    onToggleLineSelection: 'toggleLineSelection',
    onFinishServiceProposal: 'finishServiceProposal',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget',
    onPreSetCustomer: 'preSetCustomer',
    onPreSaveCustomer: 'preSaveCustomer',
    onkeydown: 'keyDownHandler',
    onkeyup: 'keyUpHandler',
    onRearrangeEditButtonBar: 'rearrangeEditButtonBar',
    onModalSelectPrinters: 'modalSelectPrinters',
    onModalSelectPDFPrinters: 'modalSelectPDFPrinters',
    onChangeFilterSelector: 'changeFilterSelector',
    onResetAdvancedFilters: 'resetAdvancedFilters',
    onChangeInitFilters: 'changeInitFilters',
    onClearAllFilterSelector: 'clearAllFilterSelector',
    onCheckPresetFilterSelector: 'checkPresetFilterSelector',
    onAdvancedFilterSelector: 'advancedFilterSelector',
    onSetSelectorAdvancedSearch: 'setSelectorAdvancedSearch',
    onCloseSelector: 'closeSelector',
    onErrorCalcLineTax: 'errorCalcLineTax'
  },
  events: {
    onShowPopup: '',
    onHidePopup: '',
    onButtonStatusChanged: ''
  },
  components: [
    {
      name: 'other_SubWindows_Container',
      classes: 'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer',
      components: [
        {
          kind:
            'OB.OBPOSPointOfSale.UI.customers.ModalConfigurationRequiredForCreateCustomers',
          name: 'modalConfigurationRequiredForCreateNewCustomers',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalConfigurationRequiredForCreateNewCustomers'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
          name: 'customerCreateAndEdit',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-customerCreateAndEdit'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.customers.editcustomer',
          name: 'customerView',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-customerView'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddr',
          name: 'customerAddrCreateAndEdit',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-customerAddrCreateAndEdit'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.customeraddr.editcustomeraddr',
          name: 'customerAddressView',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-customerAddressView'
        },
        {
          kind: 'OB.UI.ModalSelectorBusinessPartners',
          name: 'modalcustomer',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalcustomer'
        },
        {
          kind: 'OB.UI.ModalSelectorExternalBusinessPartners',
          name: 'modalExternalBusinessPartner',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalExternalBusinessPartner'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEdit',
          name: 'modalExternalBusinessPartnerViewEdit',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalExternalBusinessPartnerViewEdit'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterBP',
          name: 'modalAdvancedFilterBP',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalAdvancedFilterBP'
        },
        {
          kind: 'OB.UI.ModalAdvancedFiltersExternalBp',
          name: 'modalAdvancedFiltersExternalBp',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalAdvancedFiltersExternalBp'
        },
        {
          kind: 'OB.UI.ModalBPLocation',
          name: 'modalcustomeraddress',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalcustomeraddress'
        },
        {
          kind: 'OB.UI.ModalBPLocationShip',
          name: 'modalcustomershipaddress',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalcustomershipaddress'
        },
        {
          kind: 'OB.UI.ModalDeleteReceipt',
          name: 'modalConfirmReceiptDelete',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalConfirmReceiptDelete'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalProductCannotBeGroup',
          name: 'modalProductCannotBeGroup',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalProductCannotBeGroup'
        },
        {
          kind: 'OB.UI.Modalnoteditableorder',
          name: 'modalNotEditableOrder',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalNotEditableOrder'
        },
        {
          kind: 'OB.UI.ModalNotEditableLine',
          name: 'modalNotEditableLine',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalNotEditableLine'
        },
        {
          kind: 'OB.UI.ModalReceipts',
          name: 'modalreceipts',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalreceipts'
        },
        {
          kind: 'OB.UI.ModalVerifiedReturns',
          name: 'modalVerifiedReturns',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalVerifiedReturns'
        },
        {
          kind: 'OBPOS.UI.ReceiptSelector',
          name: 'modalReceiptSelector',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalReceiptSelector'
        },
        {
          kind: 'OBPOS.UI.ReceiptSelectorCustomerView',
          name: 'modalReceiptSelectorCustomerView',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalReceiptSelectorCustomerView'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterReceipts',
          name: 'OB_UI_ModalAdvancedFilterReceipts',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-ObUiModalAdvancedFilterReceipts'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterPayOpenTickets',
          name: 'OB_UI_ModalAdvancedFilterPayOpenTickets',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-ObUiModalAdvancedFilterReceipts'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterVerifiedReturns',
          name: 'modalAdvancedFilterVerifiedReturns',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalAdvancedFilterVerifiedReturns'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterSelectStore',
          name: 'modalAdvancedFilterSelectStore'
        },
        {
          kind: 'OB.UI.ModalMultiOrdersPayOpenTickets',
          name: 'modalMultiOrders',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalMultiOrders'
        },
        {
          kind: 'OB.UI.ModalInvoices',
          name: 'modalInvoices',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalInvoices'
        },
        {
          kind: 'OB.UI.ModalCreateOrderFromQuotation',
          name: 'modalCreateOrderFromQuotation',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalCreateOrderFromQuotation'
        },
        {
          kind: 'OB.UI.ModalReactivateQuotation',
          name: 'modalReactivateQuotation',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalReactivateQuotation'
        },
        {
          kind: 'OB.UI.ModalRejectQuotation',
          name: 'modalRejectQuotation',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalRejectQuotation'
        },
        {
          kind: 'OB.UI.ModalPriceModification',
          name: 'modalPriceModification',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalPriceModification'
        },
        {
          kind: 'OB.UI.ModalReceiptPropertiesImpl',
          name: 'receiptPropertiesDialog',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-receiptPropertiesDialog'
        },
        {
          kind: 'OB.UI.ModalReceiptLinesPropertiesImpl',
          name: 'receiptLinesPropertiesDialog',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-receiptLinesPropertiesDialog'
        },
        {
          kind: 'OB.UI.ModalDeliveryChange',
          name: 'modalDeliveryChange',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalDeliveryChange'
        },
        {
          kind: 'OB.UI.ModalPayment',
          name: 'modalpayment',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalpayment'
        },
        {
          kind: 'OB.UI.ModalPaymentVoid',
          name: 'modalpaymentvoid',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalpaymentvoid'
        },
        {
          kind: 'OB.UI.ModalProviderGroup',
          name: 'modalprovidergroup',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalprovidergroup'
        },
        {
          kind: 'OB.UI.ModalProviderGroupVoid',
          name: 'modalprovidergroupvoid',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalprovidergroupvoid'
        },
        {
          kind: 'OB.UI.ModalChange',
          name: 'modalchange',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalchange'
        },
        {
          kind:
            'OB.OBPOSPointOfSale.UI.Modals.ModalConfigurationRequiredForCrossStore',
          name: 'modalConfigurationRequiredForCrossStore',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalConfigurationRequiredForCrossStore'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
          name: 'modalLocalStock',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalLocalStock'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStoreClickable',
          name: 'modalLocalStockClickable',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalLocalStockClickable'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores',
          name: 'modalStockInOtherStores',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalStockInOtherStores'
        },
        {
          kind: 'OB.UI.ValidateAction',
          name: 'modalValidateAction',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalValidateAction'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.modalDiscountNeedQty',
          name: 'modalDiscountNeedQty',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalDiscountNeedQty'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.modalNotValidValueForDiscount',
          name: 'modalNotValidValueForDiscount',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalNotValidValueForDiscount'
        },
        {
          kind: 'OB.UI.ModalSalesRepresentative',
          name: 'modalsalesrepresentative',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalsalesrepresentative'
        },
        {
          kind: 'OB.UI.ModalMultiOrdersLayaway',
          name: 'modalmultiorderslayaway',
          classes: 'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-'
        },
        {
          kind: 'OB.UI.ModalProductCharacteristic',
          name: 'modalproductcharacteristic',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalproductcharacteristic'
        },
        {
          kind: 'OB.UI.ModalCategoryTree',
          name: 'modalcategorytree',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalcategorytree'
        },
        {
          kind: 'OB.UI.ModalSearchFilterBuilder',
          name: 'modalsearchfilterbuilder',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalsearchfilterbuilder'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalPaymentsSelect',
          name: 'modalPaymentsSelect',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalPaymentsSelect'
        },
        {
          kind: 'OB.UI.ModalSelectPrinters',
          name: 'modalSelectPrinters',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalSelectPrinters'
        },
        {
          kind: 'OB.UI.ModalSelectPDFPrinters',
          name: 'modalSelectPDFPrinters',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalSelectPDFPrinters'
        },
        {
          kind: 'OB.UI.ModalModulesInDev',
          name: 'modalModulesInDev',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalModulesInDev'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
          name: 'OBPOS_UI_PaymentMethods',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSUIPaymentMethods'
        },
        {
          kind: 'OB.UI.ModalSelectOpenedReceipt',
          name: 'OBPOS_modalSelectOpenedReceipt',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSmodalSelectOpenedReceipt'
        },
        {
          kind: 'OB.UI.ModalSplitLine',
          name: 'OBPOS_modalSplitLine',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalSplitLine'
        },
        {
          kind: 'OB.UI.ModalDeleteDiscount',
          name: 'modalDeleteDiscount',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalDeleteDiscount'
        },
        {
          kind: 'OB.UI.ModalProductAttributes',
          name: 'modalProductAttribute',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalProductAttribute'
        },
        {
          kind: 'OB.UI.ModalQuotationProductAttributes',
          name: 'modalQuotationProductAttributes',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalQuotationProductAttributes'
        },
        {
          kind: 'OB.UI.ModalOpenRelatedReceipts',
          name: 'modalOpenRelatedReceipts',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-modalOpenRelatedReceipts'
        },
        {
          kind: 'OB.UI.ModalAssociateTickets',
          name: 'OBPOS_modalAssociateTickets',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalAssociateTickets'
        },
        {
          kind: 'OB.UI.ModalRemoveAssociatedTickets',
          name: 'OBPOS_modalRemoveAssociatedTickets',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalRemoveAssociatedTickets'
        },
        {
          kind: 'OB.UI.ModalAdvancedFilterOrders',
          name: 'OBPOS_modalAdvancedFilterOrders',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalAdvancedFilterOrders'
        },
        {
          kind: 'OB.UI.ModalSafeBox',
          name: 'OBPOS_modalSafeBox',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalSafeBox'
        },
        {
          kind: 'OBPOS.UI.CrossStoreSelector',
          name: 'OBPOS_modalCrossStoreSelector',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSModalCrossStoreSelector'
        },
        {
          kind: 'OBPOS.UI.StoreInformation',
          name: 'OBPOS_storeInformation',
          classes:
            'obObposPointOfSaleUiPointOfSale-otherSubWindowsContainer-OBPOSStoreInformation'
        }
      ]
    },
    {
      name: 'mainSubWindow',
      classes: 'obObposPointOfSaleUiPointOfSale-mainSubWindow',
      isMainSubWindow: true,
      components: [
        {
          kind: 'OB.UI.MultiColumn',
          name: 'multiColumn',
          classes: 'obObposPointOfSaleUiPointOfSale-mainSubWindow-multiColumn',
          handlers: {
            onChangeTotal: 'processChangeTotal'
          },
          leftToolbar: {
            kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
            name: 'leftToolbar',
            classes: 'obObposPointOfSaleUiPointOfSale-multiColumn-leftToolbar',
            showMenu: true,
            showWindowsMenu: true
          },
          leftPanel: {
            name: 'leftPanel',
            classes: 'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel',
            components: [
              {
                classes:
                  'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel-divHeader',
                kind: 'OB.OBPOSPointOfSale.UI.LeftHeader',
                name: 'divHeader'
              },
              {
                classes:
                  'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel-receiptview',
                kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
                name: 'receiptview',
                init: function(model) {
                  this.model = model;
                  this.model.get('leftColumnViewManager').on(
                    'change:currentView',
                    function(changedModel) {
                      this.setShowing(changedModel.isOrder());
                    },
                    this
                  );
                }
              },
              {
                classes:
                  'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel-multireceiptview',
                kind: 'OB.OBPOSPointOfSale.UI.MultiReceiptView',
                name: 'multireceiptview',
                showing: false,
                init: function(model) {
                  this.model = model;
                  this.model.get('leftColumnViewManager').on(
                    'change:currentView',
                    function(changedModel) {
                      this.setShowing(changedModel.isMultiOrder());
                    },
                    this
                  );
                }
              },
              {
                name: 'leftSubWindowsContainer',
                classes:
                  'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel-leftSubWindowsContainer',
                components: [
                  {
                    classes:
                      'obObposPointOfSaleUiPointOfSale-multiColumn-leftPanel-leftSubWindowsContainer-productdetailsview',
                    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView',
                    name: 'productdetailsview'
                  }
                ]
              }
            ]
          },
          rightToolbar: {
            kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
            name: 'rightToolbar',
            classes: 'obObposPointOfSaleUiPointOfSale-multiColumn-rightToolbar',
            showMenu: true,
            showWindowsMenu: true
          },
          rightPanel: {
            name: 'rightPanel',
            classes: 'obObposPointOfSaleUiPointOfSale-multiColumn-rightPanel',
            components: [
              {
                classes:
                  'obObposPointOfSaleUiPointOfSale-multiColumn-rightPanel-wrapper',
                components: [
                  {
                    kind: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
                    name: 'toolbarpane',
                    classes:
                      'obObposPointOfSaleUiPointOfSale-multiColumn-rightPanel-wrapper-toolbarpane'
                  },
                  {
                    kind: 'OB.OBPOSPointOfSale.UI.RightBottomPanelWrapper',
                    name: 'rightBottomPanel',
                    classes:
                      'obObposPointOfSaleUiPointOfSale-multiColumn-rightPanel-wrapper-rightBottomPanel'
                  }
                ]
              }
            ]
          },
          processChangeTotal: function(inSender, inEvent) {
            if (
              this.$.leftPanel.$.receiptview.model
                .get('leftColumnViewManager')
                .isMultiOrder() &&
              inEvent.normalOrder
            ) {
              //Do not update total button as we are in multicolumn mode, and the event is for a single receipt
              return true;
            } else {
              this.waterfall('onChangedTotal', {
                newTotal: inEvent.newTotal
              });
            }
          }
        }
      ]
    }
  ],
  classModel: new Backbone.Model(),
  printReceipt: function(inSender, inEvent) {
    var receipt = this.model.get('order');
    if (OB.MobileApp.model.hasPermission('OBPOS_print.receipt')) {
      if (receipt.get('isPaid') && !receipt.get('isQuotation')) {
        this.doShowPopup({
          popup: 'modalInvoices'
        });
      } else {
        this.printSingleReceipt(inSender, inEvent);
      }
    }
  },
  printSingleReceipt: function(inSender, inEvent) {
    if (OB.MobileApp.model.hasPermission('OBPOS_print.receipt')) {
      if (this.model.get('leftColumnViewManager').isOrder()) {
        var receipt = this.model.get('order');
        if (receipt.get('isPaid')) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PrePrintPaidReceipt',
            {
              context: this,
              receipt: this.model.get('order')
            },
            function(args) {
              if (
                args &&
                args.cancelOperation &&
                args.cancelOperation === true
              ) {
                return;
              }
              receipt.trigger('print', receipt, {
                forcePrint: true
              });
              if (inEvent.callback && inEvent.callback instanceof Function) {
                inEvent.callback();
              }
            }
          );

          return;
        }
        receipt.trigger('print', receipt, {
          forcePrint: true
        });
        if (inEvent.callback && inEvent.callback instanceof Function) {
          inEvent.callback();
        }
        return;
      }
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        _.each(
          this.model.get('multiOrders').get('multiOrdersList').models,
          function(order) {
            this.model.get('multiOrders').trigger('print', order, {
              forcePrint: true
            });
            if (inEvent.callback && inEvent.callback instanceof Function) {
              inEvent.callback();
            }
          },
          this
        );
      }
    }
  },
  keyDownHandler: function(inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    OB.MobileApp.model.ctrlPressed = keyCode === 17;
    OB.MobileApp.model.shiftPressed = keyCode === 16;
  },
  keyUpHandler: function(inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 17) {
      OB.MobileApp.model.ctrlPressed = false;
    }
    if (keyCode === 16) {
      OB.MobileApp.model.shiftPressed = false;
    }
  },
  verifiedReturns: function(inSender, inEvent) {
    var receipt = this.model.get('order');
    if (inEvent && inEvent.isReturn) {
      if (
        receipt &&
        receipt.get('bp') &&
        receipt.get('bp').get('id') !==
          OB.MobileApp.model.get('businessPartner').get('id')
      ) {
        inEvent.bpartner = receipt.get('bp');
      } else if (receipt && receipt.get('lines').length > 0) {
        inEvent.bpartner = receipt.get('bp');
        inEvent.defaultBP = true;
      }
    }
    this.$.modalVerifiedReturns.setParams(inEvent);
    this.$.modalVerifiedReturns.waterfall('onClearAction');
    this.doShowPopup({
      popup: 'modalVerifiedReturns'
    });
    return true;
  },

  backOffice: function(inSender, inEvent) {
    if (inEvent.url) {
      window.open(inEvent.url, '_blank');
    }
  },
  addNewOrder: function(inSender, inEvent) {
    this.$.receiptPropertiesDialog.resetProperties();
    OB.App.State.Global.addNewTicket(
      OB.UTIL.TicketUtils.addTicketCreationDataToPayload()
    ).then(async () => {
      OB.MobileApp.model.receipt.setIsCalculateGrossLockState(false);
      OB.MobileApp.model.receipt.setIsCalculateReceiptLockState(false);
      OB.MobileApp.model.receipt.trigger('forceRenderCurrentCustomer');
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        const bp = OB.MobileApp.model.receipt.get('bp');
        await OB.App.State.Global.saveBusinessPartner(bp.serializeToJSON());
        await OB.App.State.Global.saveBusinessPartnerLocation(
          bp.get('locationModel').serializeToJSON()
        );
      }

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_NewReceipt',
        {
          newOrder: this.model.get('order')
        },
        function(args) {
          OB.UTIL.TicketListUtils.triggerTicketLoadEvents();
        }
      );
    });
    return true;
  },
  deleteCurrentOrder: function(inSender, inEvent) {
    var me = this,
      receipt = this.model.get('order'),
      execution = OB.UTIL.ProcessController.start('deleteCurrentOrder');

    OB.UTIL.setScanningFocus(false);
    inEvent.status = true;
    this.leftToolbarDisabled(inSender, inEvent);
    receipt.deleteOrder(this, function() {
      inEvent.status = false;
      me.leftToolbarDisabled(inSender, inEvent);
      OB.UTIL.setScanningFocus(true);
      OB.UTIL.ProcessController.finish('deleteCurrentOrder', execution);
    });
  },
  addProductToOrder: function(inSender, inEvent) {
    var targetOrder, attrs, finalCallback, negativeLines;
    let me = this;
    if (inEvent && inEvent.targetOrder) {
      targetOrder = inEvent.targetOrder;
    } else {
      targetOrder = this.model.get('order');
    }
    finalCallback = function(success, orderline) {
      if (inEvent.callback) {
        inEvent.callback.call(inEvent.context, success || false, orderline);
      }
      if (
        !success ||
        (orderline && orderline.get('hasMandatoryServices') === true)
      ) {
        targetOrder.addProcess = {};
      }
      if (
        targetOrder.addProcess.products &&
        targetOrder.addProcess.products.length > 0
      ) {
        const pendingProductToAdd = targetOrder.addProcess.products.shift();
        me.addProductToOrder(pendingProductToAdd.inSender, {
          ...pendingProductToAdd.inEvent,
          skipPendingProductToAdd: true
        });
      } else {
        targetOrder.addProcess.pending = false;
      }
    };
    if (inEvent.product.get('ignoreAddProduct')) {
      inEvent.product.unset('ignoreAddProduct');
      finalCallback(false);
      return;
    }
    targetOrder.addProcess = targetOrder.addProcess || {};
    targetOrder.addProcess.products = targetOrder.addProcess.products || [];
    if (
      targetOrder.addProcess.pending === true &&
      !inEvent.skipPendingProductToAdd
    ) {
      targetOrder.addProcess.products.push({
        inSender: inSender,
        inEvent: inEvent
      });
      return false;
    }
    targetOrder.addProcess.pending = true;
    negativeLines = _.filter(targetOrder.get('lines').models, function(line) {
      return line.get('qty') < 0;
    }).length;
    if (
      targetOrder.get('isEditable') === false ||
      (OB.UTIL.isCrossStoreReceipt(targetOrder) && negativeLines !== 0)
    ) {
      targetOrder.canAddAsServices(
        this.model,
        inEvent.product,
        function(addAsServices) {
          if (addAsServices !== 'ABORT') {
            if (addAsServices === 'OK') {
              // Get approval
              var deferedSellApproval = _.find(
                targetOrder.get('approvals'),
                function(approval) {
                  return (
                    approval.approvalType.approval ===
                    'OBPOS_approval.deferred_sell_max_days'
                  );
                }
              );
              if (deferedSellApproval) {
                deferedSellApproval.approvalType.message =
                  'OBPOS_approval.deferred_sell_max_days_erp';
                deferedSellApproval.approvalType.params.push(
                  inEvent.attrs.relatedLines[0].productName
                );
                deferedSellApproval.approvalType.params.push(
                  targetOrder.get('documentNo')
                );
              }
              _.each(inEvent.attrs.relatedLines, function(relatedLine) {
                relatedLine.orderDocumentNo = targetOrder.get('documentNo');
                relatedLine.otherTicket = OB.UTIL.isNullOrUndefined(
                  inEvent.targetOrder
                );
                relatedLine.deferred = true;
                var currentLine = targetOrder
                  .get('lines')
                  .models.find(function getCurrentLine(line) {
                    return line.id === relatedLine.orderlineId;
                  });
                relatedLine.qty = currentLine.get('qty');
                relatedLine.deliveredQuantity = currentLine.getDeliveredQuantity();
                relatedLine.gross = currentLine.get('gross');
                relatedLine.net = currentLine.get('net');
                if (currentLine.get('promotions')) {
                  relatedLine.promotions = currentLine
                    .get('promotions')
                    .slice();
                }
                relatedLine.obposCanbedelivered = currentLine.get(
                  'obposCanbedelivered'
                );
                relatedLine.obposIspaid = currentLine.get('obposIspaid');
              });

              // Select open ticket or create a new one
              this.doShowPopup({
                popup: 'OBPOS_modalSelectOpenedReceipt',
                args: {
                  product: inEvent.product,
                  approval: deferedSellApproval,
                  attrs: inEvent.attrs,
                  context: inEvent.context,
                  callback: inEvent.callback
                }
              });
              // Remove approval from not editable ticket
              if (deferedSellApproval) {
                var index = _.indexOf(
                  targetOrder.get('approvals'),
                  deferedSellApproval
                );
                if (index >= 0) {
                  targetOrder.get('approvals').splice(index, 1);
                }
              }
            }
            finalCallback(false);
          } else {
            this.doShowPopup({
              popup: 'modalNotEditableOrder'
            });
            finalCallback(false);
          }
        },
        this
      );
      return true;
    }

    // If a deferred service has 'As per product' quantity rule, the product quantity must be set to the quantity of the line
    if (
      inEvent.attrs &&
      inEvent.attrs.relatedLines &&
      inEvent.attrs.relatedLines[0].deferred &&
      inEvent.product.get('quantityRule') === 'PP'
    ) {
      inEvent.qty = inEvent.attrs.relatedLines[0].qty;
    }

    if (inEvent.ignoreStockTab) {
      this.showOrder(inSender, inEvent);
    } else {
      if (
        !targetOrder.get('lines').isProductPresent(inEvent.product) &&
        ((inEvent.product.get('showstock') && !inEvent.product.get('ispack')) ||
          OB.UTIL.isCrossStoreProduct(inEvent.product))
      ) {
        inEvent.leftSubWindow =
          OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
        this.showLeftSubWindow(inSender, inEvent);
        if (OB.UI.MultiColumn.isSingleColumn()) {
          this.$.multiColumn.$.rightToolbar.$.rightToolbar.$.toolbar.$.toolbarBtnCart.tap();
        }
        finalCallback(false);
        return true;
      } else {
        if (
          OB.UTIL.isCrossStoreProduct(inEvent.product) &&
          OB.UTIL.isNullOrUndefined(inEvent.product.get('listPrice')) &&
          !inEvent.product.get('ispack') &&
          targetOrder.get('lines').isProductPresent(inEvent.product)
        ) {
          var product = null;
          _.find(
            targetOrder.get('lines').models,
            function(line) {
              if (line.get('product').get('id') === inEvent.product.get('id')) {
                product = line.get('product');
                return;
              }
            },
            this
          );
          inEvent.product.set('listPrice', product.get('listPrice'));
          inEvent.product.set('standardPrice', product.get('standardPrice'));
          inEvent.product.set('currentPrice', product.get('currentPrice'));
          inEvent.product.set('productPrices', product.get('productPrices'));
        }
        this.showOrder(inSender, inEvent);
      }
    }

    attrs = inEvent.attrs || {};
    attrs.kindOriginator = inEvent.originator && inEvent.originator.kind;
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreAddProductToOrder',
      {
        context: this,
        receipt: targetOrder,
        productToAdd: inEvent.product,
        qtyToAdd: inEvent.qty ? inEvent.qty : 1,
        options: inEvent.options,
        attrs: attrs
      },
      function(args) {
        if (args.cancelOperation && args.cancelOperation === true) {
          finalCallback(false);
          return true;
        }
        args.receipt.addProductToOrder(
          args.productToAdd,
          args.qtyToAdd,
          args.options,
          args.attrs,
          finalCallback,
          function() {
            finalCallback(false);
          }
        );
      }
    );
    return true;
  },
  showOrder: function(inSender, inEvent) {
    var allHidden = true;
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      return false;
    }
    enyo.forEach(
      this.$.multiColumn.$.leftPanel.$.leftSubWindowsContainer.getControls(),
      function(component) {
        if (component.showing === true) {
          if (component.mainBeforeSetHidden) {
            if (!component.mainBeforeSetHidden(inEvent)) {
              allHidden = false;
              return false;
            } else {
              component.setShowing(false);
            }
          }
        }
      },
      this
    );
    if (allHidden) {
      this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(true);
      return true;
    }
  },
  showLeftSubWindow: function(inSender, inEvent) {
    var me = this;
    if (this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow]) {
      if (
        this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow]
          .mainBeforeSetShowing
      ) {
        var allHidden = true;
        enyo.forEach(
          this.$.multiColumn.$.leftPanel.getControls(),
          function(component) {
            if (component.showing === true) {
              if (component.mainBeforeSetHidden) {
                if (!component.mainBeforeSetHidden(inEvent)) {
                  allHidden = false;
                  return false;
                }
              }
            }
          },
          this
        );
        if (allHidden) {
          inEvent.checkStockCallback = function() {
            OB.UTIL.HookManager.executeHooks(
              'OBPOS_LeftSubWindow_beforeSetShowing',
              {
                context: me.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow],
                params: inEvent
              },
              function(args) {
                if (args && !args.cancelOperation) {
                  if (OB.UI.MultiColumn.isSingleColumn()) {
                    me.$.multiColumn.$.rightToolbar.$.rightToolbar.$.toolbar.$.toolbarBtnCart.tap();
                  }
                  me.$.multiColumn.$.leftPanel.$.receiptview.setShowing(false);
                  me.$.multiColumn.$.leftPanel.$[
                    inEvent.leftSubWindow
                  ].setShowing(true);
                  me.$.multiColumn.$.leftPanel.$[
                    inEvent.leftSubWindow
                  ].inEvent = inEvent;
                }
              }
            );
          };

          this.$.multiColumn.$.leftPanel.$[
            inEvent.leftSubWindow
          ].mainBeforeSetShowing(inEvent);
        }
      }
    }
  },
  viewProductDetails: function(inSender, inEvent) {
    this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(false);
    this.$.multiColumn.$.leftPanel.$.productdetailsview.updateProduct(
      inEvent.product
    );
    this.$.multiColumn.$.leftPanel.$.productdetailsview.setShowing(true);
    return true;
  },
  hideProductDetails: function(inSender, inEvent) {
    if (this.$.multiColumn.$.leftPanel.$.productdetailsview.showing) {
      this.$.multiColumn.$.leftPanel.$.productdetailsview.setShowing(false);
    }
    if (
      !this.model.get('leftColumnViewManager').isMultiOrder() &&
      !this.$.multiColumn.$.leftPanel.$.receiptview.showing
    ) {
      this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(true);
    }
    return true;
  },
  changeBusinessPartner: function(inSender, inEvent) {
    var component = this,
      isBPChange =
        component.model
          .get('order')
          .get('bp')
          .get('id') !== inEvent.businessPartner.get('id'),
      isShippingChange =
        component.model
          .get('order')
          .get('bp')
          .get('shipLocId') !== inEvent.businessPartner.get('shipLocId'),
      isInvoicingChange =
        component.model
          .get('order')
          .get('bp')
          .get('locId') !== inEvent.businessPartner.get('locId'),
      eventBP = inEvent.businessPartner;
    if (
      inEvent.businessPartner.get('customerBlocking') &&
      inEvent.businessPartner.get('salesOrderBlocking')
    ) {
      OB.UTIL.showError(
        OB.I18N.getLabel('OBPOS_BPartnerOnHold', [
          inEvent.businessPartner.get('_identifier')
        ])
      );
      return;
    }
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_preChangeBusinessPartner',
      {
        bp: inEvent.businessPartner,
        isBPChange: isBPChange,
        isShippingChange: isShippingChange,
        isInvoicingChange: isInvoicingChange,
        target: inEvent.target
      },
      function() {
        if (
          inEvent.target === 'order' ||
          inEvent.target === 'filterSelectorButton_receiptProperties' ||
          inEvent.target === undefined
        ) {
          if (
            component.model.get('order').get('isEditable') === false &&
            (isBPChange || isInvoicingChange || isShippingChange)
          ) {
            component.doShowPopup({
              popup: 'modalNotEditableOrder'
            });
            return true;
          }
          component.model.get('order').setBPandBPLoc(eventBP, false, true);
          component.model.get('order').trigger('updateView');
        } else {
          component.waterfall('onChangeBPartner', inEvent);
        }
      }
    );
    return true;
  },
  receiptToInvoice: function() {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('invoiceCreated') === true) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }
      this.model.get('order').setOrderInvoice();
    } else {
      this.model.get('multiOrders').toInvoice(true);
    }
  },
  createQuotation: function() {
    OB.UTIL.TicketListUtils.addNewQuotation(
      OB.App.TerminalProperty.get('terminal').terminalType
        .documentTypeForQuotations
    );
    return true;
  },

  createOrderFromQuotation: function() {
    var me = this;
    this.model.get('order').createOrderFromQuotation(false, function(success) {
      if (success) {
        me.model.get('order').trigger('updateView');
      }
    });
    return true;
  },

  showReactivateQuotation: function() {
    this.doShowPopup({
      popup: 'modalReactivateQuotation'
    });
  },

  showRejectQuotation: function() {
    this.doShowPopup({
      popup: 'modalRejectQuotation'
    });
  },

  reactivateQuotation: function() {
    this.model
      .get('order')
      .reactivateQuotation()
      .then(() => {
        this.model.get('order').trigger('updateView');
        if (
          this.$.multiColumn.$.rightPanel.$.toolbarpane.$.edit.$.editTabContent
            .$.actionButtonsContainer.$.descriptionButton
        ) {
          if (
            this.model.get('order').get('isEditable') &&
            this.model.get('order').get('isQuotation')
          ) {
            this.$.multiColumn.$.rightPanel.$.toolbarpane.$.edit.$.editTabContent.$.actionButtonsContainer.$.descriptionButton.show();
          }
        }
      });
    return true;
  },
  rejectQuotation: function(inSender, inEvent) {
    this.model.get('order').rejectQuotation(inEvent.rejectReason);
    return true;
  },
  showDivText: function(inSender, inEvent) {
    if (!this.model.get('order').get('isEditable')) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.model
      .get('order')
      .setOrderType(inEvent.permission, inEvent.orderType, {
        applyPromotions: false
      });
    this.model.get('order').trigger('updateView');
    return true;
  },

  cancelReceiptToInvoice: function(inSender, inEvent) {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('invoiceCreated') === true) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }
      this.model.get('order').resetOrderInvoice();
    } else {
      this.model.get('multiOrders').toInvoice(false);
    }
  },
  checkedLine: function(inSender, inEvent) {
    if (inEvent.originator.kind === 'OB.UI.RenderOrderLine') {
      this.waterfall('onCheckedTicketLine', inEvent);
      return true;
    }
  },
  changeDiscount: function(inSender, inEvent) {
    this.waterfall('onDiscountChanged', inEvent);
  },
  discountQtyChanged: function(inSender, inEvent) {
    this.waterfall('onDiscountQtyChanged', inEvent);
  },
  keyboardOnDiscountsMode: function(inSender, inEvent) {
    this.waterfall('onKeyboardOnDiscountsMode', inEvent);
  },
  keyboardDisabled: function(inSender, inEvent) {
    this.waterfall('onKeyboardDisabled', inEvent);
  },
  allTicketLinesChecked: function(inSender, inEvent) {
    this.waterfall('onAllTicketLinesChecked', inEvent);
  },
  leftToolbarDisabled: function(inSender, inEvent) {
    this.waterfall('onLeftToolbarDisabled', inEvent);
  },
  rightToolbarDisabled: function(inSender, inEvent) {
    this.waterfall('onRightToolbarDisabled', inEvent);
  },
  BPSelectionDisabled: function(inSender, inEvent) {
    this.waterfall('onBPSelectionDisabled', inEvent);
  },
  BPLocSelectionDisabled: function(inSender, inEvent) {
    this.waterfall('onBPLocSelectionDisabled', inEvent);
  },
  newBPDisabled: function(inSender, inEvent) {
    this.waterfall('onNewBPDisabled', inEvent);
  },
  newBPLocDisabled: function(inSender, inEvent) {
    this.waterfall('onNewBPLocDisabled', inEvent);
  },
  orderSelectionDisabled: function(inSender, inEvent) {
    this.waterfall('onOrderSelectionDisabled', inEvent);
  },
  discountsMode: function(inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: true
    });
    this.rightToolbarDisabled(inSender, {
      status: true,
      tab: 'edit',
      subtab: 'discount'
    });
    this.BPSelectionDisabled(inSender, {
      status: true
    });
    this.BPLocSelectionDisabled(inSender, {
      status: true
    });
    this.orderSelectionDisabled(inSender, {
      status: true
    });
    this.keyboardOnDiscountsMode(inSender, {
      status: true
    });
    this.waterfall('onCheckBoxBehaviorForTicketLine', {
      status: true
    });
    this.tabChange(inSender, inEvent);
  },
  disableUserInterface: function(inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: true
    });
    this.rightToolbarDisabled(inSender, {
      status: true
    });
    this.BPSelectionDisabled(inSender, {
      status: true
    });
    this.BPLocSelectionDisabled(inSender, {
      status: true
    });
    this.orderSelectionDisabled(inSender, {
      status: true
    });
  },
  enableUserInterface: function(inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: false
    });
    this.rightToolbarDisabled(inSender, {
      status: false
    });
    this.BPSelectionDisabled(inSender, {
      status: false
    });
    this.BPLocSelectionDisabled(inSender, {
      status: false
    });
    this.orderSelectionDisabled(inSender, {
      status: false
    });
  },
  showActionIcons: function(inSender, inEvent) {
    this.waterfall('onShowingActionIcons', inEvent);
  },
  tabChange: function(inSender, inEvent) {
    this.switchBottomRightLayout(inEvent.tabPanel);
    OB.POS.terminal.$.containerWindow
      .getRoot()
      .$.multiColumn.$.panels.removeClass('obUiMultiColumn-panels-showReceipt');
    this.leftToolbarDisabled(inSender, {
      status: inEvent.status || false,
      disableMenu:
        inEvent.keyboard === 'toolbardiscounts' ||
        this.model.get('leftColumnViewManager').isMultiOrder()
          ? true
          : false,
      disableButtonNew:
        inEvent.keyboard === 'toolbardiscounts' ||
        this.model.get('leftColumnViewManager').isMultiOrder()
          ? true
          : false
    });
    this.waterfall('onTabButtonTap', {
      tabPanel: inEvent.tabPanel,
      options: inEvent.options
    });
    this.waterfall('onChangeEditMode', {
      edit: inEvent.edit
    });
    if (inEvent.keyboard) {
      this.$.multiColumn.$.rightPanel.$.keyboard.showToolbar(inEvent.keyboard);
    } else {
      this.$.multiColumn.$.rightPanel.$.keyboard.hide();
    }
    if (!_.isUndefined(inEvent.status)) {
      this.$.multiColumn.$.rightPanel.$.keyboard.lastStatus = '';
      this.$.multiColumn.$.rightPanel.$.keyboard.setStatus(inEvent.status);
    }
    if (inEvent.tabPanel === 'payment') {
      OB.MobileApp.model.set('inPaymentTab', true);
    } else {
      OB.MobileApp.model.set('inPaymentTab', false);
    }
    this.hideProductDetails();
  },
  discountsModeFinished: function(inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: false
    });
    this.keyboardOnDiscountsMode(inSender, {
      status: false
    });
    this.rightToolbarDisabled(inSender, {
      status: false
    });

    this.keyboardDisabled(inSender, {
      status: false
    });

    this.BPSelectionDisabled(inSender, {
      status: false
    });

    this.BPLocSelectionDisabled(inSender, {
      status: false
    });

    this.orderSelectionDisabled(inSender, {
      status: false
    });

    this.waterfall('onCheckBoxBehaviorForTicketLine', {
      status: false
    });

    this.allTicketLinesChecked(inSender, {
      status: false
    });

    this.tabChange(inSender, inEvent);
  },
  deleteLine: function(inSender, inEvent) {
    const selectedModels = inEvent.selectedReceiptLines;
    const receipt = this.model.get('order');
    receipt.deleteLinesFromOrder(selectedModels, inEvent.callback);
  },
  editLine: function(inSender, inEvent) {
    var receipt = this.model.get('order');
    if (receipt.get('isQuotation') && receipt.get('hasbeenpaid') === 'Y') {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    if (receipt.get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.doShowPopup({
      popup: 'receiptLinesPropertiesDialog',
      args: inEvent ? inEvent.args : null
    });
  },
  returnLine: function(inSender, inEvent) {
    var me = this;
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    if (
      this.model.get('order').get('replacedorder') &&
      inEvent.line.get('remainingQuantity')
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        OB.I18N.getLabel('OBPOS_CancelReplaceReturnLines')
      );
      return;
    }
    if (
      !this.model.get('order').get('isQuotation') &&
      (OB.MobileApp.model.hasPermission(
        'OBPOS_CheckStockForNotSaleWithoutStock',
        true
      ) ||
        OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true))
    ) {
      var product = inEvent.line.get('product'),
        negativeQty = OB.DEC.compare(inEvent.line.get('qty')) < 0,
        productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product),
        checkStockActions = [];

      var getLineStock = function() {
        if (checkStockActions.length) {
          var qtyAdded = OB.DEC.mul(inEvent.line.get('qty'), -2);
          me.model
            .get('order')
            .getStoreStock(
              inEvent.line.get('product'),
              qtyAdded,
              inEvent,
              null,
              checkStockActions,
              function(hasStock) {
                if (hasStock) {
                  me.model.get('order').returnLine(inEvent.line);
                }
              }
            );
        } else {
          me.model.get('order').returnLine(inEvent.line);
        }
      };

      if (
        negativeQty &&
        OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true)
      ) {
        checkStockActions.push('stockValidation');
      }

      if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_CheckStockForNotSaleWithoutStock',
          true
        )
      ) {
        if (negativeQty && productStatus.restrictsaleoutofstock) {
          checkStockActions.push('discontinued');
        }

        if (negativeQty && OB.UTIL.isCrossStoreProduct(product)) {
          checkStockActions.push('crossStore');
        }

        OB.UTIL.HookManager.executeHooks(
          'OBPOS_CheckStockReturnLine',
          {
            order: this.model.get('order'),
            line: inEvent.line,
            checkStockActions: checkStockActions
          },
          function(args) {
            if (args.cancelOperation) {
              return;
            }
            getLineStock();
          }
        );
      } else {
        getLineStock();
      }
    } else {
      this.model.get('order').returnLine(inEvent.line);
    }
  },
  deliveryPayment: function(inSender, inEvent) {
    this.$.multiColumn.$.rightPanel.$.keyboard.execStatelessCommand(
      'cashdelivery'
    );
  },
  exactPayment: function(inSender, inEvent) {
    this.$.multiColumn.$.rightPanel.$.keyboard.execStatelessCommand(
      'cashexact'
    );
  },
  changeCurrentOrder: function(inSender, inEvent) {
    OB.MobileApp.model.receipt.set('preventServicesUpdate', true);
    OB.UTIL.TicketListUtils.loadLocalTicket(inEvent.newCurrentOrder.id)
      .then(() => {
        if (inEvent.callback) {
          inEvent.callback();
        }
      })
      .finally(() => OB.MobileApp.model.receipt.unset('preventServicesUpdate'));
    return true;
  },
  removePayment: function(inSender, inEvent) {
    var me = this,
      voidTransaction,
      voidConfirmation,
      paymentProvider;

    var removeTransaction = function() {
      if (me.model.get('leftColumnViewManager').isOrder()) {
        me.model
          .get('order')
          .removePayment(
            inEvent.payment,
            inEvent.cancellationCallback,
            function() {
              if (inEvent.removeCallback) {
                inEvent.removeCallback();
              }
              return;
            }
          );
      }
      if (me.model.get('leftColumnViewManager').isMultiOrder()) {
        me.model
          .get('multiOrders')
          .removePayment(inEvent.payment, inEvent.removeCallback);
        OB.App.State.Global.displayTotal({
          ticket: OB.UTIL.TicketUtils.toMultiTicket(me.model.get('multiOrders'))
        });
        return;
      }
    };

    var callVoidTransaction = function() {
      //To remove this payment we've to connect with server
      //a callback is defined to receive the confirmation
      me.doShowPopup({
        popup: 'modalpaymentvoid',
        args: {
          amount: inEvent.payment.get('amount')
        }
      });

      voidTransaction(
        function(hasError, error) {
          me.doHidePopup({
            popup: 'modalpaymentvoid'
          });

          if (inEvent.removeCallback) {
            inEvent.removeCallback();
          }
          if (hasError) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_LblPaymentMethod'),
              error,
              [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true
                }
              ],
              {
                autoDismiss: false
              }
            );
          } else {
            removeTransaction();
          }
        },
        me.model.get('order'),
        inEvent.payment
      );
    };

    var callRemoveCallback = function() {
      if (inEvent.removeCallback) {
        inEvent.removeCallback();
      }
    };

    var showProviderVoidInstance = function(providerinstance) {
      me.doShowPopup({
        popup: 'modalprovidergroupvoid',
        args: {
          removeTransaction: removeTransaction,
          onhide: callRemoveCallback,
          receipt: me.model.get('order'),
          payment: inEvent.payment,
          providerinstance: providerinstance
        }
      });
    };

    var callProviderVoidInstance = function() {
      var providerinstance = enyo.createFromKind(
        inEvent.payment.get('paymentData').provider.provider + 'Void'
      );
      if (providerinstance.voidConfirmation) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_LblPaymentMethod'),
          OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'),
          [
            {
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true,
              action: function() {
                showProviderVoidInstance(providerinstance);
              }
            },
            {
              label: OB.I18N.getLabel('OBMOBC_LblCancel'),
              action: callRemoveCallback
            }
          ],
          {
            autoDismiss: false,
            onHideFunction: callRemoveCallback
          }
        );
      } else {
        showProviderVoidInstance(providerinstance);
      }
    };

    if (inEvent.payment.get('paymentData')) {
      paymentProvider = eval(
        OB.MobileApp.model.paymentnames[inEvent.payment.get('kind')]
          .paymentMethod.paymentProvider
      );
      if (
        paymentProvider &&
        paymentProvider.prototype.voidTransaction &&
        paymentProvider.prototype.voidTransaction instanceof Function
      ) {
        voidTransaction = paymentProvider.prototype.voidTransaction;
      }
      if (!voidTransaction) {
        voidTransaction = inEvent.payment.get('paymentData').voidTransaction;
      }
      voidConfirmation = inEvent.payment.get('paymentData').voidConfirmation;

      if (voidConfirmation === false) {
        if (inEvent.payment.get('paymentData').provider) {
          callProviderVoidInstance();
        } else if (voidTransaction !== undefined) {
          callVoidTransaction();
        } else {
          removeTransaction();
        }
        return;
      }

      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_LblPaymentMethod'),
        OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isConfirmButton: true,
            action: function() {
              if (inEvent.payment.get('paymentData').provider) {
                callProviderVoidInstance();
              } else if (voidTransaction !== undefined) {
                callVoidTransaction();
              } else {
                removeTransaction();
              }
              return true;
            }
          },
          {
            label: OB.I18N.getLabel('OBMOBC_LblCancel'),
            action: function() {
              if (inEvent.removeCallback) {
                inEvent.removeCallback();
              }
              return true;
            }
          }
        ],
        {
          autoDismiss: false,
          onHideFunction: function() {
            if (inEvent.removeCallback) {
              inEvent.removeCallback();
            }
          }
        }
      );
    } else {
      removeTransaction();
    }
  },
  reversePayment: function(inSender, inEvent) {
    var me = this;
    if (me.model.get('leftColumnViewManager').isOrder()) {
      me.model
        .get('order')
        .reversePayment(
          inEvent.payment,
          inEvent.sender,
          inEvent.reverseCallback
        );
      return;
    }
  },
  changeSubWindow: function(inSender, inEvent) {
    this.model.get('subWindowManager').set('currentWindow', inEvent.newWindow);
  },
  setReceiptsList: function(inSender, inEvent) {
    this.$.modalreceipts.setReceiptsList(inEvent.orderList);
    this.$.OBPOS_modalSelectOpenedReceipt.setReceiptsList(inEvent.orderList);
  },
  showModalReceiptProperties: function(inSender, inEvent) {
    this.doShowPopup({
      popup: 'receiptPropertiesDialog',
      args: {
        model: inEvent.model
      }
    });
    return true;
  },
  modalSelectPrinters: function(inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalSelectPrinters'
    });
    return true;
  },
  modalSelectPDFPrinters: function(inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalSelectPDFPrinters'
    });
    return true;
  },
  setProperty: function(inSender, inEvent) {
    var i;
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    if (inEvent.extraProperties) {
      for (i = 0; i < inEvent.extraProperties.length; i++) {
        this.model
          .get('order')
          .setProperty(inEvent.extraProperties[i], inEvent.value);
      }
    }
    this.model.get('order').setProperty(inEvent.property, inEvent.value);
    this.model.get('order').trigger('updateView');
    return true;
  },
  setLineProperty: function(inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var line = inEvent.line,
      receipt = this.model.get('order');
    if (line && receipt) {
      receipt.setLineProperty(line, inEvent.property, inEvent.value);
    }
    this.model.get('order').trigger('updateView');
    return true;
  },
  statusChanged: function(inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonStatusChanged', {
      value: inEvent
    });
  },
  paymentChanged: function(inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonPaymentChanged', inEvent);
  },
  paymentChangedCancelled: function(inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonPaymentChangedCancelled', inEvent);
  },
  paymentActionPay: function(inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onActionPay', inEvent);
  },
  clearPaymentSelect: function(inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onClearPaymentMethodSelect', inEvent);
  },
  changeSalesRepresentative: function(inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.model
      .get('order')
      .set('salesRepresentative', inEvent.salesRepresentative.get('id'));
    this.model
      .get('order')
      .set(
        'salesRepresentative$_identifier',
        inEvent.salesRepresentative.get('_identifier')
      );
    this.model.get('order').trigger('updateView');
    return true;
  },
  selectCharacteristicValue: function(inSender, inEvent) {
    this.waterfall('onUpdateFilter', {
      value: inEvent
    });
  },
  multiOrders: function(inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalMultiOrders'
    });
    return true;
  },
  maxLimitAmountError: function(inSender, inEvent) {
    this.waterfallDown('onMaxLimitAmountError', inEvent);
    return true;
  },
  selectCategoryTreeItem: function(inSender, inEvent) {
    this.waterfall('onSelectCategoryItem', inEvent);
  },
  selectFilter: function(inSender, inEvent) {
    this.waterfall('onCustomFilterUpdate', inEvent);
  },
  warehouseSelected: function(inSender, inEvent) {
    this.waterfall('onModifyWarehouse', inEvent);
  },
  selectMultiOrders: function(inSender, inEvent) {
    var me = this;
    me.model
      .get('multiOrders')
      .get('multiOrdersList')
      .reset(inEvent.value);
    me.model.get('leftColumnViewManager').setMultiOrderMode();
    me.model
      .get('multiOrders')
      .get('multiOrdersList')
      .trigger('loadedMultiOrder', function() {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_hookPostMultiOrder',
          {
            context: me,
            multiOrdersList: me.model.get('multiOrders').get('multiOrdersList')
          },
          function(args) {
            if (args.cancellation) {
              me.removeOrderAndExitMultiOrder(me.model);
              OB.UTIL.showLoading(false);
              return;
            }
            if (inEvent.callback && inEvent.callback instanceof Function) {
              inEvent.callback();
            }
          }
        );
      });
  },
  removeOrderAndExitMultiOrder: function(model) {
    model.deleteMultiOrderList();
    model.get('multiOrders').resetValues();
    model.get('leftColumnViewManager').setOrderMode();
  },
  cancelRemoveMultiOrders: function(inSender, originator) {
    if (originator.kind === 'OB.UI.RemoveMultiOrders') {
      originator.deleting = false;
      originator.removeClass('btn-icon-loading');
      originator.addClass('btn-icon-clearPayment');
    }
  },
  removeMultiOrders: async function(inSender, inEvent) {
    var me = this,
      originator = inEvent.originator;
    if (me.model.get('multiOrders').checkMultiOrderPayment()) {
      me.cancelRemoveMultiOrders(inSender, originator);
      return true;
    }
    // If there are more than 1 order, do as usual
    if (
      me.model.get('multiOrders').get('multiOrdersList').length > 1 &&
      inEvent.order
    ) {
      me.model
        .get('multiOrders')
        .get('multiOrdersList')
        .remove(inEvent.order);
      if (inEvent && inEvent.order) {
        if (inEvent.order.get('loadedFromServer')) {
          me.model.get('orderList').remove(inEvent.order);
          inEvent.order.deleteOrder();
        } else {
          await OB.App.State.Global.checkTicketForPayOpenTickets({
            ticketId: inEvent.order.get('id'),
            checked: false
          });
        }
      }
      return true;
    } else {
      // Delete and exit the multiorder
      me.removeOrderAndExitMultiOrder(me.model);
      return true;
    }
  },
  doShowLeftHeader: function(inSender, inEvent) {
    this.waterfall('onLeftHeaderShow', inEvent);
  },
  clearUserInput: function(inSender, inEvent) {
    this.waterfall('onClearEditBox', inEvent);
  },
  pricelistChanged: function(inSender, inEvent) {
    this.waterfall('onChangePricelist', inEvent);
  },
  resetAdvancedFilters: function(inSender, inEvent) {
    this.waterfallDown('onResetAdvancedFilters', inEvent);
  },
  changeFilterSelector: function(inSender, inEvent) {
    this.waterfall('onUpdateFilterSelector', inEvent);
  },
  changeInitFilters: function(inSender, inEvent) {
    this.waterfall('onInitFilters', inEvent);
  },
  clearAllFilterSelector: function(inSender, inEvent) {
    this.waterfall('onClearFilterSelector', inEvent);
  },
  checkPresetFilterSelector: function(inSender, inEvent) {
    this.waterfall('onHasPresetFilterSelector', inEvent);
  },
  advancedFilterSelector: function(inSender, inEvent) {
    this.waterfall('onGetAdvancedFilterSelector', inEvent);
  },
  setSelectorAdvancedSearch: function(inSender, inEvent) {
    this.waterfall('onSetAdvancedSearchMode', inEvent);
  },
  receiptLineSelected: function(inSender, inEvent) {
    var product,
      i,
      enableButton = true,
      selectedLines = this.$.multiColumn.$.rightPanel.$.keyboard.selectedModels,
      selectedLinesLength = selectedLines
        ? this.$.multiColumn.$.rightPanel.$.keyboard.selectedModels.length
        : 0;

    if (selectedLinesLength > 0) {
      for (i = 0; i < selectedLinesLength; i++) {
        product = selectedLines[i].get('product');
        if (
          !product.get('groupProduct') ||
          (product.get('productType') === 'S' &&
            product.get('isLinkedToProduct')) ||
          !selectedLines[i].get('isEditable') ||
          product.get('isSerialNo')
        ) {
          enableButton = false;
          break;
        }
      }
    } else {
      enableButton = false;
    }
    this.enableKeyboardButton(enableButton);
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_LineSelected',
      {
        line: inEvent.line,
        selectedLines: selectedLines,
        context: this
      },
      function(args) {}
    );
  },
  enableKeyboardButton: function(enableButton) {
    if (enableButton && !this.model.get('order').get('isEditable')) {
      enableButton = false;
    }
    this.waterfall('onEnableQtyButton', {
      enable: enableButton
    });
    this.waterfall('onEnablePlusButton', {
      enable: enableButton
    });
    this.waterfall('onEnableMinusButton', {
      enable: enableButton
    });
  },
  manageServiceProposal: function(inSender, inEvent) {
    this.waterfallDown('onManageServiceProposal', inEvent);
  },
  toggleLineSelection: function(inSender, inEvent) {
    this.waterfall('onToggledLineSelection', inEvent);
  },
  finishServiceProposal: function(inSender, inEvent) {
    this.waterfallDown('onFinishServiceProposal', inEvent);
  },
  setBusinessPartnerTarget: function(inSender, inEvent) {
    this.waterfallDown('onSetBPartnerTarget', inEvent);
  },
  preSetCustomer: function(inSender, inEvent) {
    this.waterfallDown('onSetCustomer', inEvent);
  },
  preSaveCustomer: function(inSender, inEvent) {
    this.waterfallDown('onSaveCustomer', inEvent);
  },
  setMultiSelection: function(inSender, inEvent) {
    this.waterfall('onSetMultiSelected', inEvent);
  },
  showMultiSelection: function(inSender, inEvent) {
    this.waterfall('onShowMultiSelected', inEvent);
  },
  setMultiSelectionItems: function(inSender, inEvent) {
    this.waterfall('onTableMultiSelectedItems', inEvent);
  },
  closeSelector: function(inSender, inEvent) {
    this.waterfall('onCloseCancelSelector', inEvent);
  },
  errorCalcLineTax: function(inSender, inEvent) {
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      OB.MobileApp.view.$.containerWindow
        .getRoot()
        .$.multiColumn.$.rightToolbar.$.rightToolbar.manualTap('edit');
    }
  },
  rearrangeEditButtonBar: function(inSender, inEvent) {
    this.waterfall('onRearrangedEditButtonBar', inEvent);
  },
  init: function() {
    var receipt, receiptList, LeftColumnCurrentView;
    this.inherited(arguments);
    receipt = this.model.get('order');
    const session = OB.MobileApp.model.get('session');
    receiptList = new Backbone.Collection(
      OB.App.State.TicketList.Utils.getSessionTickets(session).map(ticket => {
        return OB.App.StateBackwardCompatibility.getInstance(
          'Ticket'
        ).toBackboneObject(ticket);
      })
    );

    OB.MobileApp.view.scanningFocus(true);

    this.waterfall('onPointOfSaleLoad');

    // Try to print the pending receipts.
    OB.OBPOSPointOfSale.OfflinePrinter.printPendingJobs();

    OB.App.PersistenceChangeListenerManager.addListener(
      state => {
        const session = OB.MobileApp.model.get('session');
        const ticketList = new Backbone.Collection(
          OB.App.State.TicketList.Utils.getSessionTickets(session).map(
            ticket => {
              return OB.App.StateBackwardCompatibility.getInstance(
                'Ticket'
              ).toBackboneObject(ticket);
            }
          )
        );

        if (this.$.multiColumn) {
          this.$.multiColumn.$.leftPanel.$.receiptview.setOrderList(ticketList);
        }

        OB.MobileApp.model.orderList.reset(ticketList.models);
      },
      ['TicketList']
    );

    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function(changedModel) {
        this.$.multiColumn.$.rightPanel.$.keyboard.clearInput();
        if (changedModel.isMultiOrder()) {
          this.rightToolbarDisabled(
            {},
            {
              status: true,
              exceptionPanel: 'payment'
            }
          );
          this.tabChange(
            {},
            {
              tabPanel: 'payment',
              keyboard: 'toolbarpayment'
            }
          );
          OB.POS.terminal.$.containerWindow
            .getRoot()
            .$.multiColumn.$.panels.addClass(
              'obUiMultiColumn-panels-showReceipt'
            );
          return;
        }
        if (changedModel.isOrder()) {
          this.rightToolbarDisabled(
            {},
            {
              status: false
            }
          );
          this.tabChange(
            {},
            {
              tabPanel: 'scan',
              keyboard: 'toolbarscan'
            }
          );
          return;
        }
      },
      this
    );

    LeftColumnCurrentView = enyo.json.parse(
      OB.UTIL.localStorage.getItem('leftColumnCurrentView')
    );
    if (LeftColumnCurrentView === null) {
      LeftColumnCurrentView = {
        name: 'order',
        params: []
      };
    }
    this.model
      .get('leftColumnViewManager')
      .set('currentView', LeftColumnCurrentView);

    this.model.get('subWindowManager').on(
      'change:currentWindow',
      function(changedModel) {
        function restorePreviousState(swManager, changedModel) {
          swManager.set(
            'currentWindow',
            changedModel.previousAttributes().currentWindow,
            {
              silent: true
            }
          );
        }

        var showNewSubWindow = false,
          currentWindowClosed = true;
        if (this.$[changedModel.get('currentWindow').name]) {
          if (!changedModel.get('currentWindow').params) {
            changedModel.get('currentWindow').params = {};
          }
          changedModel.get(
            'currentWindow'
          ).params.caller = changedModel.previousAttributes().currentWindow.name;
          if (
            this.$[changedModel.previousAttributes().currentWindow.name]
              .mainBeforeClose
          ) {
            currentWindowClosed = this.$[
              changedModel.previousAttributes().currentWindow.name
            ].mainBeforeClose(changedModel.get('currentWindow').name);
          }
          if (currentWindowClosed) {
            if (
              this.$[changedModel.get('currentWindow').name]
                .mainBeforeSetShowing
            ) {
              showNewSubWindow = this.$[
                changedModel.get('currentWindow').name
              ].mainBeforeSetShowing(changedModel.get('currentWindow').params);
              if (showNewSubWindow) {
                this.$[
                  changedModel.previousAttributes().currentWindow.name
                ].setShowing(false);
                this.$[changedModel.get('currentWindow').name].setShowing(true);
                if (
                  this.$[changedModel.get('currentWindow').name].mainAfterShow
                ) {
                  this.$[
                    changedModel.get('currentWindow').name
                  ].mainAfterShow();
                }
              } else {
                restorePreviousState(
                  this.model.get('subWindowManager'),
                  changedModel
                );
              }
            } else {
              if (
                this.$[changedModel.get('currentWindow').name].isMainSubWindow
              ) {
                this.$[
                  changedModel.previousAttributes().currentWindow.name
                ].setShowing(false);
                this.$[changedModel.get('currentWindow').name].setShowing(true);
                OB.MobileApp.view.scanningFocus(true);
              } else {
                //developers helps
                //OB.info("Error! A subwindow must inherits from OB.UI.subwindow -> restore previous state");
                restorePreviousState(
                  this.model.get('subWindowManager'),
                  changedModel
                );
              }
            }
          } else {
            restorePreviousState(
              this.model.get('subWindowManager'),
              changedModel
            );
          }
        } else {
          //developers helps
          //OB.info("The subwindow to navigate doesn't exists -> restore previous state");
          restorePreviousState(
            this.model.get('subWindowManager'),
            changedModel
          );
        }
      },
      this
    );

    // show properties when needed...
    receipt.get('lines').on(
      'created',
      function(line) {
        this.classModel.trigger('createdLine', this, line);
      },
      this
    );
    receipt.get('lines').on(
      'removed',
      function(line) {
        this.classModel.trigger('removedLine', this, line);
      },
      this
    );

    receipt.on(
      'change:isEditable',
      function(model) {
        this.enableKeyboardButton(true);
      },
      this
    );

    this.$.multiColumn.$.leftPanel.$.receiptview.setOrder(receipt);
    this.$.multiColumn.$.leftPanel.$.receiptview.setOrderList(receiptList);
    this.$.multiColumn.$.rightPanel.$.toolbarpane.setModel(this.model);
    this.$.multiColumn.$.rightPanel.$.keyboard.setReceipt(receipt);
    this.$.multiColumn.$.rightToolbar.$.rightToolbar.setReceipt(receipt);
  },
  switchBottomRightLayout: function(newTab) {
    this.$.multiColumn.$.rightPanel.$.rightBottomPanel.tabChanged(newTab);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.multiColumn.$.rightPanel.$.keyboard = this.$.multiColumn.$.rightPanel.$.rightBottomPanel.$.keyboard;
    if (OB.UTIL.Debug.isDebug()) {
      enyo.$.terminal.addClass(
        OB.UTIL.Debug.getDebugCauses().isTestEnvironment
          ? 'obUiTerminal_isWarn'
          : 'obUiTerminal_isError'
      );
      this.waterfall('onInDevHeaderShow');
    }

    // Create test components if defined
    if (OB.UI.TestComponent) {
      Object.keys(OB.UI.TestComponent).forEach(function(item) {
        this.createComponent({
          kind: OB.UI.TestComponent[item]
        });
      }, this);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftHeader',
  classes: 'obObPosPointOfSaleUiLeftHeader',
  published: {
    text: null
  },
  handlers: {
    onLeftHeaderShow: 'doShowHeader'
  },
  doShowHeader: function(inSender, inEvent) {
    this.setText(inEvent.text);
    if (inEvent.class) {
      this.$.innerDiv.addClass(inEvent.class);
    }
    this.$.innerDiv.show();
  },

  components: [
    {
      name: 'innerDiv',
      classes: 'obObPosPointOfSaleUiLeftHeader-innerDiv',
      showing: false,
      components: [
        {
          name: 'headerText',
          classes: 'obObPosPointOfSaleUiLeftHeader-innerDiv-headerText',
          content: ''
        }
      ]
    }
  ],
  textChanged: function() {
    this.$.headerText.setContent(this.text);
  }
});

OB.OBPOSPointOfSale.UICustomization = OB.OBPOSPointOfSale.UICustomization || {};
OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow = 'productdetailsview';

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  permission: 'OBPOS_retail.pointofsale',
  // Not to display it in the menu
  menuLabel: 'POS',
  defaultWindow: true
});
