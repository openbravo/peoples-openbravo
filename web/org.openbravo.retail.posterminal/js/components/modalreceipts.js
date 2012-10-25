/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.UI.ModalReceipts',
  myId: 'modalreceipts',
  published: {
    receiptsList: null
  },
  kind: 'OB.UI.Modal',
  modalClass: 'modal',
  headerClass: 'modal-header',
  bodyClass: 'modal-header',
  header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
  body: {
    kind: 'OB.UI.ListReceipts',
    name: 'listreceipts'
  },
  receiptsListChanged: function (oldValue) {
    this.$.body.$.listreceipts.setReceiptsList(this.receiptsList);
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
      style: 'border-bottom: 1px solid #cccccc;'
    }, {
      components: [{
        name: 'receiptslistitemprinter',
        kind: 'OB.UI.ScrollableTable',
        scrollAreaMaxHeight: '400px',
        renderLine: 'OB.UI.ListReceiptLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],
  receiptsListChanged: function (oldValue) {
    this.$.receiptslistitemprinter.setCollection(this.receiptsList);
    this.receiptsList.on('click', function (model) {
      this.doChangeCurrentOrder({
        newCurrentOrder: model
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.ListReceiptLine',
  kind: 'OB.UI.SelectButton',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      components: [{
        style: 'float: left; width: 15%',
        name: 'time'
      }, {
        style: 'float: left; width: 25%',
        name: 'orderNo'
      }, {
        style: 'float: left; width: 60%',
        name: 'bp'
      }, {
        style: 'clear: both;'
      }]
    }, {
      components: [{
        style: 'float: left; width: 15%; font-weight: bold;'
      }, {
        style: 'float: left; width: 25%; font-weight: bold;'
      }, {
        style: 'float: right; text-align: right; width: 25%; font-weight: bold;',
        name: 'total'
      }, {
        style: 'clear: both;'
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    if (this.model.get('orderDate') instanceof Date) {
      this.$.time.setContent(OB.I18N.formatHour(this.model.get('orderDate')));
    } else {
      this.$.time.setContent(this.model.get('orderDate'));
    }
    this.$.orderNo.setContent(this.model.get('documentNo'));
    this.$.bp.setContent(this.model.get('bp').get('_identifier'));
    this.$.total.setContent(this.model.printTotal());
  }
});

/*delete confirmation modal*/

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalDeleteReceipt',
  myId: 'modalConfirmReceiptDelete',
  header: OB.I18N.getLabel('OBPOS_ConfirmDeletion'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_MsgConfirmDelete') + '\n' + OB.I18N.getLabel('OBPOS_cannotBeUndone')
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
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblYesDelete'),
  events: {
    onDeleteOrder: ''
  },
  tap: function () {
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