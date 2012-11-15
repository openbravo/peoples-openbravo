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
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit',
  header: OB.I18N.getLabel('OBPOS_enoughCreditHeader'),
  bodyContent: {
    name: 'popupmessage',
    content: ''
  },
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.apply_button'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.cancel_button'
    }]
  },
  executeOnShow: function () {
    var pendingQty = this.args.order.getPending();
    var bpName = this.args.order.get('bp').get('_identifier');
    this.$.bodyContent.$.popupmessage.setContent(OB.I18N.getLabel('OBPOS_enoughCreditBody', [pendingQty, bpName]));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.apply_button',
  content: OB.I18N.getLabel('OBPOS_LblUseCredit'),
  isApplyButton: true,
  init: function (model) {
    this.model = model;
  },
  tap: function () {
    function error(tx) {
      OB.UTIL.showError("OBDAL error: " + tx);
    }

    this.doHidePopup({
      popup: 'modalEnoughCredit'
    });
    this.model.get('order').trigger('paymentDone');
    this.model.get('order').trigger('openDrawer');
    if (!OB.POS.modelterminal.get('connectedToERP')) {
      var bp = this.model.get('order').get('bp');
      var bpCreditUsed = this.model.get('order').get('bp').get('creditUsed');
      var totalPending = this.model.get('order').getPending();
      bp.set('creditUsed', bpCreditUsed - totalPending);
      OB.Dal.save(bp, null, error);
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.cancel_button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalNotEnoughCredit',
  style: 'background-color: #EBA001;',
  header: OB.I18N.getLabel('OBPOS_notEnoughCreditHeader'),
  isApplyButton: true,
  executeOnShow: function () {
    if (this.args) {
      this.$.bodyContent.$.popupmessage.setContent(OB.I18N.getLabel('OBPOS_notEnoughCreditBody', [this.args.bpName, this.args.actualCredit]));
    }
  },
  bodyContent: {
    name: 'popupmessage',
    content: ''
  }
});