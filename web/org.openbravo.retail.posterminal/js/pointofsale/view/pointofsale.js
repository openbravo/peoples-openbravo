/*global OB, enyo, confirm */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  handlers: {
    onAddProduct: 'addProductToOrder',
    onCancelReceiptToInvoice: 'cancelReceiptToInvoice',
    onReceiptToInvoice: 'receiptToInvoice',
    onCreateQuotation: 'createQuotation',
    onCreateOrderFromQuotation: 'createOrderFromQuotation',
    onReactivateQuotation: 'reactivateQuotation',
    onRejectQuotation: 'rejectQuotation',
    onShowReturnText: 'showReturnText',
    onAddNewOrder: 'addNewOrder',
    onDeleteOrder: 'deleteCurrentOrder',
    onTabChange: 'tabChange',
    onDeleteLine: 'deleteLine',
    onExactPayment: 'exactPayment',
    onRemovePayment: 'removePayment',
    onChangeCurrentOrder: 'changeCurrentOrder',
    onChangeBusinessPartner: 'changeBusinessPartner',
    onPrintReceipt: 'printReceipt',
    onChangeWindow: 'changeWindow'
  },
  components: [{
    name: 'otherSubWindowsContainer',
  }, {
    name: 'mainSubWindow',
    components: [{
      kind: 'OB.UI.ModalDeleteReceipt'
    }, {
      kind: 'OB.UI.ModalBusinessPartners'
    }, {
      classes: 'row',
      style: 'margin-bottom: 5px;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl'
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
        name: 'rightToolbar'
      }]
    }, {
      classes: 'row',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
        name: 'receiptview'
      }, {
        classes: 'span6',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
          name: 'toolbarpane'
        }, {
          kind: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
          name: 'keyboard'
        }]
      }]
    }]
  }],
  printReceipt: function() {

    if (OB.POS.modelterminal.hasPermission('OBPOS_print.receipt')) {
      var receipt = this.model.get('order');
      receipt.calculateTaxes(function() {
        receipt.trigger('print');
      });
    }
  },
  addNewOrder: function(inSender, inEvent) {
    this.model.get('orderList').addNewOrder();
    return true;
  },
  deleteCurrentOrder: function() {
    if (this.model.get('order').get('id')) {
      this.model.get('orderList').saveCurrent();
      OB.Dal.remove(this.model.get('orderList').current, null, null);
    }
    this.model.get('orderList').deleteCurrent();
    return true;
  },
  addProductToOrder: function(inSender, inEvent) {
    this.model.get('order').addProduct(inEvent.product);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  changeBusinessPartner: function(inSender, inEvent) {
    this.model.get('order').setBPandBPLoc(inEvent.businessPartner, false, true);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  receiptToInvoice: function() {
    this.model.get('order').setOrderInvoice();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  createQuotation: function(){
    this.model.get('order').createQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  createOrderFromQuotation: function(){
    this.model.get('order').createOrderFromQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  reactivateQuotation: function(){
    this.model.get('order').reactivateQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  rejectQuotation: function(){
    this.model.get('order').rejectQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  showReturnText: function(inSender, inEvent) {
    this.model.get('order').setOrderTypeReturn();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  cancelReceiptToInvoice: function(inSender, inEvent) {
    this.model.get('order').resetOrderInvoice();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  tabChange: function(sender, event) {
    this.waterfall('onChangeEditMode', {
      edit: event.edit
    });
    if (event.keyboard) {
      this.$.keyboard.showToolbar(event.keyboard);
    } else {
      this.$.keyboard.hide();
    }
  },
  deleteLine: function(sender, event) {
    var line = event.line,
        receipt = this.model.get('order');
    if (line && receipt) {
      receipt.deleteLine(line);
      receipt.trigger('scan');
    }
  },
  exactPayment: function(sender, event) {
    this.$.keyboard.execStatelessCommand('cashexact');
  },
  changeCurrentOrder: function(inSender, inEvent) {
    this.model.get('orderList').load(inEvent.newCurrentOrder);
    return true;
  },
  removePayment: function(sender, event) {
    if (this.model.get('paymentData') && !confirm(OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'))) {
      return;
    }
    this.model.get('order').removePayment(event.payment);
  },
  changeWindow: function(sender, event) {
    this.model.get('windowManager').set('currentWindow', event.newWindow);
  },
  init: function() {
    var receipt, receiptList;
    this.inherited(arguments);
    receipt = this.model.get('order');
    receiptList = this.model.get('orderList');
    this.model.get('windowManager').on('change:currentWindow', function(changedModel) {
      //TODO backbone route
      if (this.$[changedModel.get('currentWindow')]) {
        this.$[changedModel.previousAttributes().currentWindow].setShowing(false);
        this.$[changedModel.get('currentWindow')].setShowing(true);
      } else {
        this.model.get('windowManager').set('currentWindow', changedModel.previousAttributes().currentWindow, {
          silent: true
        });
      }
    }, this);
    this.$.receiptview.setOrder(receipt);
    this.$.receiptview.setOrderList(receiptList);
    this.$.toolbarpane.setModel(this.model);
    this.$.keyboard.setReceipt(receipt);
    this.$.rightToolbar.setReceipt(receipt);
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  // Not to display it in the menu
  menuLabel: 'POS'
});
OB.POS.modelterminal.set('windowRegistered', true);
OB.POS.modelterminal.triggerReady();