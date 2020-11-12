/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.UI.MenuReturn',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuReturn',
  action: {
    window: 'retail.pointofsale',
    name: 'returnReceipt'
  }
});

enyo.kind({
  name: 'OB.UI.MenuVoidLayaway',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuVoidLayaway',
  permission: 'OBPOS_receipt.voidLayaway',
  events: {
    onShowDivText: '',
    onTabChange: ''
  },
  i18nLabel: 'OBPOS_VoidLayaway',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    OB.MobileApp.model.receipt.runCompleteTicket(
      OB.App.State.Global.voidLayaway,
      'voidLayaway'
    );
    return;
  },
  displayLogic: function() {
    var me = this,
      i;
    this.hideVoidLayaway = [];

    this.show();
    this.adjustVisibilityBasedOnPermissions();
    if (
      this.model.get('order').get('isLayaway') &&
      this.model.get('order').get('payments').length === 0
    ) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreDisplayVoidLayaway',
        {
          context: this
        },
        function(args) {
          for (i = 0; i < me.hideVoidLayaway.length; i++) {
            if (me.hideVoidLayaway[i]) {
              me.hide();
              break;
            }
          }
        }
      );
    } else {
      this.hide();
    }
  },
  init: function(model) {
    this.model = model;
    var receipt = model.get('order');
    this.setShowing(false);
    receipt.on(
      'change:isLayaway change:receiptLines change:orderType',
      function(model) {
        this.displayLogic();
      },
      this
    );

    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function(changedModel) {
        if (changedModel.isOrder()) {
          this.displayLogic();
          return;
        }
        if (changedModel.isMultiOrder()) {
          this.setShowing(false);
          return;
        }
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuReceiptLayaway',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuReceiptLayaway',
  permission: 'OBPOS_receipt.receiptLayaway',
  events: {
    onShowDivText: '',
    onRearrangeEditButtonBar: ''
  },
  i18nLabel: 'OBPOS_LblReceiptLayaway',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    // check if this order has been voided previously
    if (this.model.get('order').get('orderType') === 3) {
      return;
    }

    var errorsConvertingLayawayToReceipt = [];

    if (!this.model.get('order').checkAllAttributesHasValue()) {
      errorsConvertingLayawayToReceipt.push('OBPOS_AllAttributesNeedValue');
    }

    enyo.forEach(
      this.model.get('order').get('payments').models,
      function(curPayment) {
        errorsConvertingLayawayToReceipt.push('OBPOS_LayawayHasPayment');
        return;
      },
      this
    );

    if (errorsConvertingLayawayToReceipt.length === 0) {
      this.doShowDivText({
        permission: this.permission,
        orderType: 0
      });
    } else {
      errorsConvertingLayawayToReceipt.forEach(function(error) {
        OB.UTIL.showWarning(OB.I18N.getLabel(error));
      });
    }
    this.doRearrangeEditButtonBar();
  },
  displayLogic: function() {
    if (this.model.get('order').get('orderType') === 2) {
      this.show();
      this.adjustVisibilityBasedOnPermissions();
    } else {
      this.hide();
    }
  },
  init: function(model) {
    this.model = model;
    var receipt = model.get('order');
    this.setShowing(false);
    receipt.on(
      'change:orderType',
      function(model) {
        this.displayLogic();
      },
      this
    );
    receipt.on(
      'change:isLayaway',
      function(model) {
        this.displayLogic();
      },
      this
    );

    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function(changedModel) {
        if (changedModel.isOrder()) {
          this.displayLogic();
          return;
        }
        if (changedModel.isMultiOrder()) {
          this.setShowing(false);
          return;
        }
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuCancelLayaway',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuCancelLayaway',
  permission: 'OBPOS_receipt.cancelLayaway',
  events: {
    onShowDivText: '',
    onTabChange: ''
  },
  i18nLabel: 'OBPOS_CancelLayaway',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    if (this.model.get('order').get('iscancelled')) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_AlreadyCancelledHeader'),
        OB.I18N.getLabel('OBPOS_AlreadyCancelled')
      );
      return;
    }
    if (this.model.get('order').get('isFullyDelivered')) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_FullyDeliveredHeader'),
        OB.I18N.getLabel('OBPOS_FullyDelivered')
      );
      return;
    }
    var negativeLines = _.find(
      this.model.get('order').get('lines').models,
      function(line) {
        return line.get('qty') < 0;
      }
    );
    if (negativeLines) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_cannotCancelLayawayHeader'),
        OB.I18N.getLabel('OBPOS_cancelLayawayWithNegativeLines')
      );
      return;
    }
    var reservationLines = _.find(
      this.model.get('order').get('lines').models,
      function(line) {
        return line.get('hasStockReservation');
      }
    );
    if (reservationLines) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_cannotCancelOrderHeader'),
        OB.I18N.getLabel('OBPOS_cancelOrderWithReservation')
      );
      return;
    }

    this.model.get('order').cancelLayaway(this);
  },
  displayLogic: function() {
    var me = this,
      isPaidReceipt,
      isReturn,
      receiptLines,
      receipt;

    receipt = this.model.get('order');

    isPaidReceipt = receipt.get('isPaid') && !receipt.get('isQuotation');
    isReturn =
      receipt.get('orderType') === 1 ||
      receipt.get('documentType') ===
        OB.MobileApp.model.get('terminal').terminalType
          .documentTypeForReturns ||
      receipt.get('documentType') === 'VBS RFC Order';
    receiptLines = receipt.get('receiptLines');

    // Function to know the delivered status of the current order

    function delivered() {
      var shipqty = OB.DEC.Zero;
      var qty = OB.DEC.Zero;
      _.each(receipt.get('lines').models, function(line) {
        qty += line.get('qty');
      });
      if (receiptLines) {
        _.each(receiptLines, function(line) {
          _.each(line.shipmentlines, function(shipline) {
            shipqty += shipline.qty;
          });
        });
      } else {
        return 'udf';
      }
      if (shipqty === qty) {
        //totally delivered
        return 'TD';
      }
      if (shipqty === 0) {
        //no deliveries
        return 'ND';
      }
      return 'DN';
    }

    function hasPayments() {
      if (
        me.model.get('orderList').current &&
        me.model.get('orderList').current.get('payments').length &&
        _.find(
          me.model.get('orderList').current.get('payments').models,
          function(payment) {
            return payment.get('isPrePayment');
          }
        )
      ) {
        return true;
      }
      return false;
    }

    if (
      !isReturn &&
      delivered() !== 'TD' &&
      (isPaidReceipt ||
        (receipt.get('isLayaway') &&
          (!OB.MobileApp.model.hasPermission(
            'OBPOS_payments.cancelLayaway',
            true
          ) ||
            hasPayments())))
    ) {
      this.show();
      this.adjustVisibilityBasedOnPermissions();
    } else {
      this.hide();
    }
  },
  updateLabel: function(model) {
    if (
      model.get('isPaid') &&
      this.$.lbl.getContent() === OB.I18N.getLabel('OBPOS_CancelLayaway')
    ) {
      this.$.lbl.setContent(OB.I18N.getLabel('OBPOS_CancelOrder'));
    } else if (
      model.get('isLayaway') &&
      this.$.lbl.getContent() === OB.I18N.getLabel('OBPOS_CancelOrder')
    ) {
      this.$.lbl.setContent(OB.I18N.getLabel('OBPOS_CancelLayaway'));
    }
  },
  init: function(model) {
    this.model = model;
    var receipt = model.get('order');

    receipt.on(
      'change:isLayaway change:isPaid change:orderType change:documentType',
      function(model) {
        this.updateLabel(model);
        this.displayLogic();
      },
      this
    );

    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function(changedModel) {
        if (changedModel.isOrder()) {
          this.displayLogic(changedModel);
          return;
        }
        if (changedModel.isMultiOrder()) {
          this.hide();
          return;
        }
      },
      this
    );

    this.displayLogic();
  }
});

enyo.kind({
  name: 'OB.UI.MenuLayaway',
  kind: 'OB.UI.ActionMenuAction',
  action: {
    window: 'retail.pointofsale',
    name: 'layawayReceipt'
  }
});

enyo.kind({
  name: 'OB.UI.MenuProperties',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuProperties',
  action: {
    window: 'retail.pointofsale',
    name: 'showModalReceiptProperties'
  }
});

enyo.kind({
  name: 'OB.UI.MenuInvoice',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuInvoice',
  action: {
    window: 'retail.pointofsale',
    name: 'invoiceReceipt'
  }
});

enyo.kind({
  name: 'OB.UI.MenuOpenDrawer',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuOpenDrawer',
  action: {
    window: 'retail.pointofsale',
    name: 'openDrawer'
  }
});

enyo.kind({
  name: 'OB.UI.MenuCustomers',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuCustomers',
  permission: 'OBPOS_receipt.customers',
  events: {
    onShowPopup: ''
  },
  i18nLabel: 'OBPOS_LblCustomers',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    this.doShowPopup({
      popup: OB.UTIL.modalCustomer(),
      args: {
        target: 'order'
      }
    });
  },
  init: function(model) {
    this.model = model;
    model.get('leftColumnViewManager').on(
      'order',
      function() {
        this.setDisabled(false);
        this.adjustVisibilityBasedOnPermissions();
      },
      this
    );

    model.get('leftColumnViewManager').on(
      'multiorder',
      function() {
        this.setDisabled(true);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuPrint',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuPrint',
  action: {
    window: 'retail.pointofsale',
    name: 'printReceipt'
  }
});

enyo.kind({
  name: 'OB.UI.MenuQuotation',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuQuotation',
  action: {
    window: 'retail.pointofsale',
    name: 'createQuotation'
  }
});

enyo.kind({
  name: 'OB.UI.MenuDiscounts',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuDiscounts',
  i18nLabel: 'OBPOS_LblReceiptDiscounts',
  action: {
    window: 'retail.pointofsale',
    name: 'discount'
  }
});

enyo.kind({
  name: 'OB.UI.MenuCreateOrderFromQuotation',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuCreateOrderFromQuotation',
  action: {
    window: 'retail.pointofsale',
    name: 'convertQuotation'
  }
});

enyo.kind({
  name: 'OB.UI.MenuCreateQuotationFromOrder',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuCreateQuotationFromOrder',
  permission: 'OBPOS_receipt.createquotationfromorder',
  i18nLabel: 'OBPOS_CreateQuotationFromOrder',
  events: {
    onTabChange: ''
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    if (this.receipt.get('payments').length) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        OB.I18N.getLabel('OBPOS_ExistingPayments')
      );
      return;
    }
    if (
      _.find(this.receipt.get('lines').models, function(line) {
        return OB.DEC.compare(line.get('qty')) === -1;
      })
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        OB.I18N.getLabel('OBPOS_NegativeLinesQuotation')
      );
      return;
    }
    this.receipt.createQuotationFromOrder();
  },
  updateVisibility: function(model) {
    if (
      OB.MobileApp.model.hasPermission(this.permission) &&
      !model.get('isQuotation') &&
      model.get('isEditable') &&
      (model.get('orderType') === 0 || model.get('orderType') === 2)
    ) {
      this.show();
    } else {
      this.hide();
    }
  },
  updateLabel: function(model) {
    if (
      model.get('orderType') === 0 &&
      this.$.lbl.getContent() ===
        OB.I18N.getLabel('OBPOS_CreateQuotationFromLayaway')
    ) {
      this.$.lbl.setContent(OB.I18N.getLabel('OBPOS_CreateQuotationFromOrder'));
    } else if (
      model.get('orderType') === 2 &&
      this.$.lbl.getContent() ===
        OB.I18N.getLabel('OBPOS_CreateQuotationFromOrder')
    ) {
      this.$.lbl.setContent(
        OB.I18N.getLabel('OBPOS_CreateQuotationFromLayaway')
      );
    }
  },
  init: function(model) {
    this.receipt = model.get('order');

    this.updateVisibility(this.receipt);
    this.updateLabel(this.receipt);

    this.receipt.on(
      'change:isQuotation change:isEditable change:orderType',
      function(model) {
        this.updateVisibility(model);
      },
      this
    );

    this.receipt.on(
      'change:orderType',
      function(model) {
        this.updateLabel(model);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuReactivateQuotation',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuReactivateQuotation',
  permission: 'OBPOS_receipt.reactivatequotation',
  events: {
    onShowReactivateQuotation: ''
  },
  i18nLabel: 'OBPOS_ReactivateQuotation',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    if (OB.MobileApp.model.hasPermission(this.permission)) {
      this.doShowReactivateQuotation();
    }
  },
  updateVisibility: function(model) {
    if (
      OB.MobileApp.model.hasPermission(this.permission) &&
      model.get('isQuotation') &&
      model.get('hasbeenpaid') === 'Y'
    ) {
      this.show();
    } else {
      this.hide();
    }
  },
  init: function(model) {
    var receipt = model.get('order'),
      me = this;
    me.hide();

    model.get('leftColumnViewManager').on(
      'order',
      function() {
        this.updateVisibility(receipt);
        this.adjustVisibilityBasedOnPermissions();
      },
      this
    );

    model.get('leftColumnViewManager').on(
      'multiorder',
      function() {
        me.hide();
      },
      this
    );

    receipt.on(
      'change:isQuotation',
      function(model) {
        this.updateVisibility(model);
      },
      this
    );
    receipt.on(
      'change:hasbeenpaid',
      function(model) {
        this.updateVisibility(model);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuRejectQuotation',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuRejectQuotation',
  permission: 'OBPOS_quotation.rejections',
  events: {
    onShowRejectQuotation: ''
  },
  i18nLabel: 'OBPOS_RejectQuotation',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    if (OB.MobileApp.model.hasPermission(this.permission, true)) {
      this.doShowRejectQuotation();
    }
  },
  updateVisibility: function(model) {
    if (
      OB.MobileApp.model.hasPermission(this.permission, true) &&
      model.get('isQuotation') &&
      model.get('hasbeenpaid') === 'Y'
    ) {
      this.show();
    } else {
      this.hide();
    }
  },
  init: function(model) {
    var receipt = model.get('order'),
      me = this;
    me.hide();

    model.get('leftColumnViewManager').on(
      'order',
      function() {
        this.updateVisibility(receipt);
        this.adjustVisibilityBasedOnPermissions();
      },
      this
    );

    model.get('leftColumnViewManager').on(
      'multiorder',
      function() {
        me.hide();
      },
      this
    );

    receipt.on(
      'change:isQuotation',
      function(model) {
        this.updateVisibility(model);
      },
      this
    );
    receipt.on(
      'change:hasbeenpaid',
      function(model) {
        this.updateVisibility(model);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.MenuReceiptSelector',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuReceiptSelector',
  action: {
    window: 'retail.pointofsale',
    name: 'openReceipt'
  }
});

enyo.kind({
  name: 'OB.UI.MenuMultiOrders',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuMultiOrders',
  action: {
    window: 'retail.pointofsale',
    name: 'payOpenReceipts'
  }
});

enyo.kind({
  name: 'OB.UI.MenuBackOffice',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuBackOffice',
  permission: 'OBPOS_retail.backoffice',
  url: '../..',
  events: {
    onBackOffice: ''
  },
  i18nLabel: 'OBPOS_LblOpenbravoWorkspace',
  tap: function() {
    var useURL = this.url;
    if (this.disabled) {
      return true;
    }

    // use the central server url
    _.each(OB.RR.RequestRouter.servers.models, function(server) {
      if (server.get('mainServer') && server.get('address')) {
        useURL = server.get('address');
      }
    });

    this.inherited(arguments); // Manual dropdown menu closure
    if (OB.MobileApp.model.hasPermission(this.permission)) {
      this.doBackOffice({
        url: useURL
      });
    }
  }
});

enyo.kind({
  name: 'OB.UI.MenuDisableEnableRFIDReader',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_retail.disableEnableRFIDReader',
  i18nLabel: 'OBPOS_RFID',
  classes: 'obUiMenuDisableEnableRFIDReader',
  handlers: {
    onPointOfSaleLoad: 'pointOfSaleLoad'
  },
  components: [
    {
      name: 'lbl',
      classes: 'obUiMenuDisableEnableRFIDReader-lbl',
      allowHtml: true
    }
  ],
  tap: function() {
    this.inherited(arguments);
    if (this.disabled) {
      return true;
    }
    this.setDisabled(true);
    if (OB.MobileApp.model.hasPermission(this.permission)) {
      if (OB.UTIL.RfidController.get('isRFIDEnabled')) {
        OB.UTIL.RfidController.set('reconnectOnScanningFocus', false);
        OB.UTIL.RfidController.disconnectRFIDDevice();
        if (OB.UTIL.RfidController.get('rfidTimeout')) {
          clearTimeout(OB.UTIL.RfidController.get('rfidTimeout'));
        }
      } else {
        OB.UTIL.RfidController.set('reconnectOnScanningFocus', true);
        OB.UTIL.RfidController.connectRFIDDevice();
        if (OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout) {
          if (OB.UTIL.RfidController.get('rfidTimeout')) {
            clearTimeout(OB.UTIL.RfidController.get('rfidTimeout'));
          }
          OB.UTIL.RfidController.set(
            'rfidTimeout',
            setTimeout(function() {
              OB.UTIL.RfidController.unset('rfidTimeout');
              OB.UTIL.RfidController.set('reconnectOnScanningFocus', false);
              OB.UTIL.RfidController.disconnectRFIDDevice();
            }, OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout *
              1000 *
              60)
          );
        }
      }
    }
  },
  init: function(model) {
    if (!OB.UTIL.RfidController.isRfidConfigured()) {
      this.hide();
    }
    OB.UTIL.RfidController.off('change:connected change:connectionLost');
  },
  pointOfSaleLoad: function(inSender, inEvent) {
    OB.UTIL.RfidController.on(
      'change:connected change:connectionLost',
      function(model) {
        if (OB.UTIL.RfidController.get('connectionLost')) {
          this.removeClass('obUiMenuDisableEnableRFIDReader_switchOn');
          this.removeClass('obUiMenuDisableEnableRFIDReader_switchOff');
          this.addClass('obUiMenuDisableEnableRFIDReader_switchOffline');
          this.setDisabled(true);
        } else {
          this.removeClass('obUiMenuDisableEnableRFIDReader_switchOffline');
          if (
            OB.UTIL.RfidController.get('isRFIDEnabled') &&
            OB.UTIL.RfidController.get('connected')
          ) {
            this.addClass('obUiMenuDisableEnableRFIDReader_switchOn');
            this.removeClass('obUiMenuDisableEnableRFIDReader_switchOff');
          } else {
            OB.UTIL.RfidController.disconnectRFIDDevice();
            this.removeClass('obUiMenuDisableEnableRFIDReader_switchOn');
            this.addClass('obUiMenuDisableEnableRFIDReader_switchOff');
          }
          this.setDisabled(false);
        }
      },
      this
    );

    if (OB.UTIL.RfidController.isRfidConfigured()) {
      if (
        OB.UTIL.RfidController.get('connectionLost') ||
        !OB.UTIL.RfidController.get('connected')
      ) {
        this.addClass('obUiMenuDisableEnableRFIDReader_switchOffline');
        return;
      } else {
        this.removeClass('obUiMenuDisableEnableRFIDReader_switchOffline');
      }
      if (
        !OB.UTIL.RfidController.get('isRFIDEnabled') ||
        !OB.UTIL.RfidController.get('reconnectOnScanningFocus')
      ) {
        this.addClass('obUiMenuDisableEnableRFIDReader_switchOff');
        this.removeClass('obUiMenuDisableEnableRFIDReader_switchOn');
      } else {
        this.addClass('obUiMenuDisableEnableRFIDReader_switchOn');
        this.removeClass('obUiMenuDisableEnableRFIDReader_switchOffline');
      }
    } else {
      this.hide();
    }
  }
});

enyo.kind({
  name: 'OB.UI.MenuSelectPrinter',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuReturn',
  action: {
    window: 'retail.pointofsale',
    name: 'selectPrinter'
  }
});

enyo.kind({
  name: 'OB.UI.MenuSelectPDFPrinter',
  kind: 'OB.UI.ActionMenuAction',
  classes: 'obUiMenuReturn',
  action: {
    window: 'retail.pointofsale',
    name: 'selectPDFPrinter'
  }
});

enyo.kind({
  name: 'OB.UI.MenuCancelAndReplace',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuCancelAndReplace',
  permission: 'OBPOS_receipt.cancelreplace',
  i18nLabel: 'OBPOS_CancelReplace',
  events: {
    onRearrangeEditButtonBar: ''
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    this.model.get('order').verifyCancelAndReplace(this);
  },
  updateVisibility: function() {
    var isPaidReceipt, isLayaway, isReturn, haspayments, receiptLines, receipt;

    receipt = this.model.get('order');

    isPaidReceipt =
      receipt.get('isPaid') === true && !receipt.get('isQuotation');
    isLayaway = receipt.get('isLayaway');
    isReturn =
      receipt.get('orderType') === 1 ||
      receipt.get('documentType') ===
        OB.MobileApp.model.get('terminal').terminalType
          .documentTypeForReturns ||
      receipt.get('documentType') === 'VBS RFC Order';
    haspayments = receipt.get('payments').length > 0;
    receiptLines = OB.MobileApp.model.receipt.get('receiptLines');

    function delivered() {
      var shipqty = OB.DEC.Zero;
      var qty = OB.DEC.Zero;
      _.each(receipt.get('lines').models, function(line) {
        qty += line.get('qty');
      });
      if (receiptLines) {
        _.each(receiptLines, function(line) {
          _.each(line.shipmentlines, function(shipline) {
            shipqty += shipline.qty;
          });
        });
      } else {
        return 'udf';
      }
      if (shipqty === qty) {
        //totally delivered
        return 'TD';
      }
      if (shipqty === 0) {
        //no deliveries
        return 'ND';
      }
      return 'DN';
    }
    if (isPaidReceipt || isLayaway) {
      var deliveredresult = delivered();
      if (
        isPaidReceipt &&
        !OB.MobileApp.model.hasPermission(
          'OBPOS_receipt.CancelReplacePaidOrders',
          true
        ) &&
        deliveredresult === 'TD'
      ) {
        this.hide();
      } else if (
        !OB.MobileApp.model.hasPermission(
          'OBPOS_receipt.CancelReplaceLayaways',
          true
        ) &&
        deliveredresult === 'ND' &&
        !haspayments
      ) {
        this.hide();
      } else if (
        !OB.MobileApp.model.hasPermission(
          'OBPOS_receipt.CancelAndReplaceOrdersWithDeliveries',
          true
        ) &&
        (deliveredresult === 'TD' || deliveredresult === 'DN')
      ) {
        this.hide();
      } else if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_payments.hideCancelAndReplace',
          true
        ) &&
        haspayments
      ) {
        this.hide();
      } else {
        this.show();
      }
    } else {
      this.hide();
    }

    if (isReturn) {
      this.hide();
    }

    this.adjustVisibilityBasedOnPermissions();
  },
  init: function(model) {
    var receipt = model.get('order'),
      me = this;

    this.model = model;

    this.model.get('leftColumnViewManager').on(
      'order',
      function() {
        this.updateVisibility();
        this.adjustVisibilityBasedOnPermissions();
      },
      this
    );

    this.model.get('leftColumnViewManager').on(
      'multiorder',
      function() {
        me.hide();
      },
      this
    );

    receipt.on(
      'change:isLayaway change:isPaid change:isQuotation change:replacedorder change:orderType change:receiptLines',
      function() {
        this.updateVisibility();
      },
      this
    );

    this.updateVisibility();
  }
});

enyo.kind({
  name: 'OB.UI.MenuForceIncrementalRefresh',
  kind: 'OB.UI.MenuAction',
  classes: 'obUiMenuForceIncrementalRefresh',
  permission: 'OBMOBC_NotAutoLoadIncrementalAtLogin',
  i18nLabel: 'OBPOS_MenuForceIncrementalRefreshLabel',
  init: function(model) {
    this.displayLogic();
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.inherited(arguments); // Manual dropdown menu closure
    if (OB.MobileApp.model.hasPermission(this.permission, true)) {
      OB.UTIL.localStorage.setItem('POSForceIncrementalRefresh', true);
      window.location.reload();
    }
  },
  displayLogic: function() {
    if (OB.MobileApp.model.hasPermission(this.permission, true)) {
      this.show();
    } else {
      this.hide();
    }
  }
});
