/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ButtonNew',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-new',
  events: {
    onAddNewOrder: ''
  },
  tap: function() {
    this.doAddNewOrder();
  }
});


enyo.kind({
  name: 'OB.UI.ButtonDelete',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-delete',
  attributes: {
    href: '#modalConfirmReceiptDelete',
    'data-toggle': 'modal'
  }
});

enyo.kind({
  name: 'OB.UI.ButtonPrint',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-print',
  tap: function() {
    var receipt = this.options.modelorder;
    receipt.calculateTaxes(function() {
      receipt.trigger('print');
    });
  }
});

enyo.kind({
  name: 'OB.UI.MenuReturn',
  kind: 'OB.UI.MenuAction',
  events: {
    onShowReturnText: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblReturn'),
  tap: function() {
    this.doShowReturnText();
  }
});

enyo.kind({
  name: 'OB.UI.MenuInvoice',
  kind: 'OB.UI.MenuAction',
  events: {
    onShowInvoiceButton: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblInvoice'),
  tap: function() {
    this.doShowInvoiceButton();
  }
});