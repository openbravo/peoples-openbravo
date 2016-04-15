/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _, enyo */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.Modals.btnModalApprovalReasonAccept',
  i18nContent: 'OBMOBC_LblOk',
  tap: function () {
    this.model.set('approvals', {
      supervisor: this.owner.owner.args.supervisor,
      message: this.owner.owner.args.message,
      approvalReason: this.owner.owner.$.bodyContent.$.approvalReason.getValue()
    });
    this.owner.owner.args.callback();
    this.doHideThisPopup();
  },
  init: function (model) {
    this.model = model;
  }
});

enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.ModalApprovalReasonList',
  classes: 'combo',
  style: 'width: 80%',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('name'));
    }
  }),
  renderEmpty: 'enyo.Control',
  initComponents: function () {
    this.setCollection(new Backbone.Collection());
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalApprovalReason',
  closeOnEscKey: false,
  autoDismiss: false,
  bodyContent: {
    components: [{
      name: 'labelApprovalReason',
      style: 'text-align: right; padding-right:10px; width: 50%; height: 40px; float: left;'
    }, {
      style: 'width: 45%; float: left;',
      kind: 'OB.UI.ModalApprovalReasonList',
      name: 'approvalReason'
    }]
  },
  i18nHeader: 'OBPOS_ApprovalReason',
  bodyButtons: {
    components: [{
      kind: 'OB.UI.Modals.btnModalApprovalReasonAccept'
    }]
  },
  events: {
    onApprovalReason: ''
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.headerCloseButton.hide();
    this.$.bodyContent.$.labelApprovalReason.setContent(OB.I18N.getLabel('OBPOS_lblApprovalReason'));
    var me = this,
        approvalReasonCollection = [];
    _.each(OB.POS.modelterminal.get('approvalReason'), function (reason) {
      approvalReasonCollection.push(new Backbone.Model({
        id: reason.id,
        name: reason.name
      }));
    });
    me.$.bodyContent.$.approvalReason.getCollection().reset(approvalReasonCollection);
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSCashUp.UI.CashUp', {
  kind: 'OB.UI.ModalApprovalReason',
  name: 'OBPOS_modalApprovalReason'
});