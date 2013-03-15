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
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalApplyClosePaidTicket',
  i18nContent: 'OBPOS_acceptClosePaidTicket',
  isDefaultAction: true,
  events: {
    onDeleteOrder: ''
  },
  tap: function () {
    this.doHideThisPopup();
    this.doDeleteOrder();
  }
});



enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalClosePaidReceipt',
  i18nHeader: 'OBPOS_confirmClosePaidTicketHeader',
  bodyContent: {
    i18nContent: 'OBPOS_closePaidTicketMessage'
  },
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalApplyClosePaidTicket'
    }, {
      kind: 'OB.UI.CancelDialogButton'
    }]
  }
});