/*global OB, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*left toolbar*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
  tag: 'li',
  classes: 'span3',
  components: [{
    name: 'theButton',
    attributes: {
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  classes: 'span4',
  components: [{
    tag: 'ul',
    classes: 'unstyled nav-pos row-fluid',
    name: 'toolbar'
  }],
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(this.buttons, function(btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
        button: btn
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
  kind: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  buttons: [{
    kind: 'OB.UI.ButtonNew'
  }, {
    kind: 'OB.UI.ButtonDelete'
  }, {
    kind: 'OB.UI.ButtonPrint'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.StandardMenu'
  }]
});

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  handlers: {
    onAddProduct: 'addProductToOrder',
    onInvoiceReceipt: 'receiptToInvoice',
    onShowInvoiceButton: 'showInvoiceButton',
    onShowReturnText: 'showReturnText',
    onAddNewOrder: 'addNewOrder',

    onTabChange: 'tabChange',
    onDeleteLine: 'deleteLine'
  },
  components: [{
    classes: 'row',
    style: 'margin-bottom: 5px;',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl'
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
        kind: 'OB.UI.KeyboardOrder',
        name: 'keyboard'
      }]

    }]
  }],
  addNewOrder: function(inSender, inEvent) {
    this.model.get('orderList').addNewOrder();
  },
  addProductToOrder: function(inSender, inEvent) {
    this.model.get('order').addProduct(inEvent.product);
    return true; // not continue
  },
  showInvoiceButton: function() {
    this.$.receiptview.$.orderview.$.btninvoice.show();
    return true;
  },
  showReturnText: function() {
    this.$.receiptview.$.orderview.$.
    return .show();
    return true;
  },
  receiptToInvoice: function(inSender, inEvent) {
    console.log('Invoice receipt handler');
    this.model.get('order').resetOrderInvoice();
    this.$.receiptview.$.orderview.$.btninvoice.hide();
    return true;
  },
  tabChange: function(sender, event) {
    OB.UTIL.setOrderLineInEditMode(event.edit);
    if (event.keyboard) {
      this.$.keyboard.showToolbar(event.keyboard);
    } else {
      this.$.keyboard.hide();
    }
    console.log('tab change', arguments);
  },
  deleteLine: function(seneder, event) {
    var line = event.line,
        receipt = this.model.get('order');
    if (line && receipt) {
      receipt.deleteLine(line)
      receipt.trigger('scan');
    }
  },
  init: function() {
    this.inherited(arguments);
    this.$.receiptview.setOrder(this.model.get('order'));
    this.$.toolbarpane.setModel(this.model);
    console.log('order: ' + this.model.get('order'));
    console.log('main init')
  }

});

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  // Not to display it in the menu
  menuLabel: 'POS'
});