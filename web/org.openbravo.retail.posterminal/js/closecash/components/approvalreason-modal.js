/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.Modals.btnModalApprovalReasonAccept',
  classes: 'obUiModalsBtnModalApprovalReasonAccept',
  i18nContent: 'OBMOBC_LblOk',
  tap: function() {
    this.model.set('approvals', {
      supervisor: this.owner.owner.args.supervisor,
      message: this.owner.owner.args.message,
      approvalReason: this.owner.owner.$.body.$.approvalReason.getValue()
    });
    this.owner.owner.approved = true;
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
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalApprovalReason',
  classes: 'obUiModalApprovalReason',
  closeOnEscKey: true,
  autoDismiss: true,
  body: {
    classes: 'obUiModalApprovalReason-body',
    components: [
      {
        name: 'labelApprovalReason',
        classes: 'obUiModalApprovalReason-body-labelApprovalReason'
      },
      {
        classes: 'obUiModalApprovalReason-body-approvalReason',
        kind: 'OB.UI.ModalApprovalReasonList',
        name: 'approvalReason'
      }
    ]
  },
  i18nHeader: 'OBPOS_ApprovalReason',
  footer: {
    classes: 'obUiModalApprovalReason-footer',
    components: [
      {
        classes:
          'obUiModalApprovalReason-footer-obUiModalsBtnModalApprovalReasonAccept',
        kind: 'OB.UI.Modals.btnModalApprovalReasonAccept'
      }
    ]
  },
  events: {
    onApprovalReason: ''
  },
  executeOnShow: function() {
    this.approved = false;
  },
  executeOnHide: function() {
    this.args.callback(this.approved);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.closebutton.show();
    this.$.body.$.labelApprovalReason.setContent(
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
    me.$.body.$.approvalReason.getCollection().reset(approvalReasonCollection);
  }
});
