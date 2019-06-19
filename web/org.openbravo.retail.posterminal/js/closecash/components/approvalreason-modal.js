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
  classes: 'obUiModalsBtnModalApprovalReasonAccept',
  i18nContent: 'OBMOBC_LblOk',
  tap: function() {
    this.model.set('approvals', {
      supervisor: this.owner.owner.args.supervisor,
      message: this.owner.owner.args.message,
      approvalReason: this.owner.owner.$.bodyContent.$.approvalReason.getValue()
    });
    this.owner.owner.args.callback();
    this.doHideThisPopup();
  },
  init: function(model) {
    this.model = model;
  }
});

enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.ModalApprovalReasonList',
  classes: 'obUiModalApprovalReasonList',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function() {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('name'));
    }
  }),
  renderEmpty: 'enyo.Control',
  initComponents: function() {
    this.setCollection(new Backbone.Collection());
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalApprovalReason',
  classes: 'obUiModalApprovalReason',
  closeOnEscKey: true,
  autoDismiss: true,
  bodyContent: {
    classes: 'obUiModalApprovalReason-bodyContent',
    components: [
      {
        name: 'labelApprovalReason',
        classes: 'obUiModalApprovalReason-bodyContent-labelApprovalReason'
      },
      {
        classes: 'obUiModalApprovalReason-bodyContent-approvalReason',
        kind: 'OB.UI.ModalApprovalReasonList',
        name: 'approvalReason'
      }
    ]
  },
  i18nHeader: 'OBPOS_ApprovalReason',
  bodyButtons: {
    classes: 'obUiModalApprovalReason-bodyButtons',
    components: [
      {
        classes:
          'obUiModalApprovalReason-bodyButtons-obUiModalsBtnModalApprovalReasonAccept',
        kind: 'OB.UI.Modals.btnModalApprovalReasonAccept'
      }
    ]
  },
  events: {
    onApprovalReason: ''
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.headerCloseButton.show();
    this.$.bodyContent.$.labelApprovalReason.setContent(
      OB.I18N.getLabel('OBPOS_lblApprovalReason')
    );
    var me = this,
      approvalReasonCollection = [];
    _.each(OB.POS.modelterminal.get('approvalReason'), function(reason) {
      approvalReasonCollection.push(
        new Backbone.Model({
          id: reason.id,
          name: reason.name
        })
      );
    });
    me.$.bodyContent.$.approvalReason
      .getCollection()
      .reset(approvalReasonCollection);
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSCashUp.UI.CashUp', {
  kind: 'OB.UI.ModalApprovalReason',
  classes: 'obposModaApprovalReasonl',
  name: 'OBPOS_modalApprovalReason'
});
