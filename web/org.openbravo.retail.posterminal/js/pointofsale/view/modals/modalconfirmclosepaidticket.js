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
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalApplyClosePaidTicket',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_acceptClosePaidTicket'),
  events: {
    onDeleteOrder: ''
  },
  tap: function () {
    $('#modalConfirmClosePaidTicket').modal('hide');
    this.doDeleteOrder();
  }
});



enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalClosePaidReceipt',
  myId: 'modalConfirmClosePaidTicket',
  header: OB.I18N.getLabel('OBPOS_confirmClosePaidTicketHeader'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_closePaidTicketMessage')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalApplyClosePaidTicket'
    }, {
      kind: 'OB.UI.CancelDialogButton'
    }]
  }
});