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
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalReactivateQuotationCancel',
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
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalReactivateQuotationAccept',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  events: {
    onReactivateQuotation: '',
    onHideThisPopup: ''
  },
  tap: function () {
    this.doReactivateQuotation();
    this.doHideThisPopup();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalReactivateQuotation',
  myId: 'modalReactivateQuotation',
  bodyContent: {},
  header: OB.I18N.getLabel('OBPOS_ReactivateQuotation'),
  bodyButtons: {
    components: [{
      content: OB.I18N.getLabel('OBPOS_ReactivateQuotationMessage')
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalReactivateQuotationAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalReactivateQuotationCancel'
    }]
  }
});