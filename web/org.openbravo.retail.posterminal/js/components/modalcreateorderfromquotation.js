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
    $('#modalCreateOrderFromQuotation').modal('hide');
    this.doSomething();
  }
});



enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalCreateOrderFromQuotation',
  myId: 'modalCreateOrderFromQuotation',
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_QuotationUpdatePrices')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalCreateOrderCancel'
    }]
  }
});