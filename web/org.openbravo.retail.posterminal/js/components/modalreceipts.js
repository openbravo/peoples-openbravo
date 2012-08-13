/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, $, B, Backbone */

enyo.kind({
  name: 'OB.UI.ModalReceipts',
  myId: 'modalreceipts',
  published: {
    receiptsList: null
  },
  kind: 'OB.UI.Modal',
  modalClass: 'modal-dialog',
  bodyClass: 'modal-dialog-body',
  header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
  body: {
    kind: 'OB.UI.ListReceipts',
    name: 'listreceipts'
  },
  receiptsListChanged: function(oldValue) {
    this.$.body.$.listreceipts.setReceiptsList(this.receiptsList);
  },
  init: function() {
    //this.$.body.$.listEvents.init();
  }
});

enyo.kind({
  name: 'OB.UI.ListReceipts',
  classes: 'row-fluid',
  published: {
    receiptsList: null
  },
  events: {
    onChangeCurrentOrder: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      name: 'receiptslistitemprinter',
      kind: 'OB.UI.Table',
      renderLine: 'OB.UI.ListReceiptLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    }]
  }],
  receiptsListChanged: function(oldValue) {
    this.$.receiptslistitemprinter.setCollection(this.receiptsList);
    this.receiptsList.on('click', function(model) {
      this.clickedOrder = model;
      this.doChangeCurrentOrder();
    }, this)
  }
});

enyo.kind({
  name: 'OB.UI.ListReceiptLine',
  kind: 'OB.UI.SelectButton',
  style: 'background-color:#dddddd;  border: 1px solid #ffffff;',
  components: [{
    name: 'line',
    style: 'padding: 1px 0px 1px 5px;'
  }],
  create: function() {
    this.inherited(arguments);
    this.$.line.setContent(this.model.get('documentNo'));
  }
});

/*delete confirmation modal*/

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalDeleteReceipt',
  myId: 'modalConfirmReceiptDelete',
  header: OB.I18N.getLabel('OBPOS_ConfirmDeletion'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_MsgConfirmDelete') + '\n' + OB.I18N.getLabel('OBPOS_cannotBeUndone'),
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.btnModalApplyDelete'
    }, {
      kind: 'OB.UI.btnModalCancelDelete'
    }]
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.btnModalApplyDelete',
  isActive: true,
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblYesDelete'),
  events: {
    onDeleteOrder: ''
  },
  tap: function() {
    $('#modalConfirmReceiptDelete').modal('hide');
    this.doDeleteOrder();
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.btnModalCancelDelete',
  attributes: {
    'data-dismiss': 'modal'
  },
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel')
});