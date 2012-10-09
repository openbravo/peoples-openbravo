/*global OB, enyo, $, confirm */

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
    onChangeSubWindow: 'changeSubWindow',
    onSetProperty: 'setProperty',
    onShowReceiptProperties: 'showModalReceiptProperties'
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
      kind: 'OB.UI.ModalReceiptPropertiesImpl'
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
  changeSubWindow: function(sender, event) {
    this.model.get('subWindowManager').set('currentWindow', event.newWindow);
  },
  showModalReceiptProperties: function(inSender, inEvent) {
    $('#receiptPropertiesDialog').modal('show');
    return true;
  },
  setProperty: function(inSender, inEvent) {
    this.model.get('order').setProperty(inEvent.property, inEvent.value);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  beforeSetShowing: function(value, params){
    this.setShowing(value);
  },
  init: function() {
    var receipt, receiptList;
    this.inherited(arguments);
    receipt = this.model.get('order');
    receiptList = this.model.get('orderList');
    this.model.get('subWindowManager').on('change:currentWindow', function(changedModel) {
      //TODO backbone route
      if (this.$[changedModel.get('currentWindow').name]) {
        this.$[changedModel.previousAttributes().currentWindow.name].setShowing(false);
        if (this.$[changedModel.get('currentWindow').name].beforeSetShowing) {
          this.$[changedModel.get('currentWindow').name].beforeSetShowing(true, changedModel.get('currentWindow').params);
        } else {
          this.$[changedModel.get('currentWindow').name].setShowing(true);
        }
      } else {
        this.model.get('subWindowManager').set('currentWindow', changedModel.previousAttributes().currentWindow, {
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