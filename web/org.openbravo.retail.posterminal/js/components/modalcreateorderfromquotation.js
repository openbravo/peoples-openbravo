/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderCancel',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderAccept',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  content: OB.I18N.getLabel('OBPOS_CreateOrderFromQuotation'),
  events: {
    onCreateOrderFromQuotation: '',
    onHideThisPopup: ''
  },
  tap: function () {
    var checked = !this.parent.children[1].children[0].checked;
    this.parent.parent.parent.parent.theQuotation.createOrderFromQuotation(checked);
    this.doHideThisPopup();
  }
});


enyo.kind({
  name: 'OB.UI.updateprices',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  checked: false,
  content: OB.I18N.getLabel('OBPOS_QuotationUpdatePrices'),
  init: function () {
    this.checked = true;
    this.addRemoveClass('active', this.checked);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalCreateOrderFromQuotation',
  myId: 'modalCreateOrderFromQuotation',
  bodyContent: {},
  bodyButtons: {
    components: [{
      style: 'height: 40px; width: 127px; float:left;'
    }, {
      style: 'height: 40px; width: 50px; background-color: #ffffff; float:left',
      components: [{
        kind: 'OB.UI.updateprices',
        myId: 'updatePricesCheck'
      }]
    }, {
      style: 'clear: both;'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderCancel'
    }]
  },
  init: function (model) {
    var receipt = model.get('order');
    this.theQuotation = receipt;
  }
});