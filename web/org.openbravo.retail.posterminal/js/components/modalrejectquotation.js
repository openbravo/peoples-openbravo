/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalRejectQuotationCancel',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalRejectQuotationAccept',
  i18nContent: 'OBMOBC_LblOk',
  events: {
    onRejectQuotationDone: ''
  },
  tap: function () {
    this.doRejectQuotationDone();
    this.doHideThisPopup();
  }
});
enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.ModalRejectQuotationRejectReason',
  classes: 'combo',
  style: 'width: 80%',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('_identifier'));
    }
  }),
  renderEmpty: 'enyo.Control',
  initComponents: function () {
    this.setCollection(new Backbone.Collection());
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalRejectQuotation',
  myId: 'modalRejectQuotation',
  bodyContent: {
    components: [{
      name: 'labelRejectReason',
      style: 'text-align: right; padding-right:10px; width: 40%; height: 40px; float: left;'
    }, {
      style: 'width: 50%; float: left;',
      kind: 'OB.UI.ModalRejectQuotationRejectReason',
      name: 'rejectReason'
    }]
  },
  i18nHeader: 'OBPOS_RejectQuotation',
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalRejectQuotationAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalRejectQuotationCancel'
    }]
  },
  events: {
    onRejectQuotation: ''
  },
  handlers: {
    onRejectQuotationDone: 'doRejectQuotationDone'
  },
  doRejectQuotationDone: function () {
    this.doRejectQuotation({
      rejectReason: this.$.bodyContent.$.rejectReason.getValue()
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyContent.$.labelRejectReason.setContent(OB.I18N.getLabel('OBPOS_lblRejectReason'));
    var rejectReasonCollection = [];
    _.each(OB.MobileApp.model.get('rejectReasons'), function (reason) {
      rejectReasonCollection.push(new Backbone.Model({
        id: reason.id,
        _identifier: reason._identifier
      }));
    });
    this.$.bodyContent.$.rejectReason.getCollection().reset(rejectReasonCollection);
  }
});