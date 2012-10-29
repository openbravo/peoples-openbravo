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
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  tap: function () {
    $('#modalCreateOrderFromQuotation').modal('hide');
  }
});
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderAccept',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_CreateOrderFromQuotation'),
  events: {
    onCreateOrderFromQuotation: ''
  },
  tap: function () {
    var checked = this.parent.children[0].checked;
    this.doCreateOrderFromQuotation();
    $('#modalCreateOrderFromQuotation').modal('hide');
  }
});


enyo.kind({
  name: 'OB.UI.updateprices',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  checked: true,
  content: OB.I18N.getLabel('OBPOS_QuotationUpdatePrices')
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalCreateOrderFromQuotation',
  myId: 'modalCreateOrderFromQuotation',
  bodyContent: {
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.updateprices',
      myId: 'updatePricesCheck'
    },{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderCancel'
    }]
  },
  init: function(model){
    var receipt = model.get('order');
    this.theQuotation = receipt;
  }
});