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
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  published: {
    receiptsList: null
  },
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
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px; width: 100%;',
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
    if (this.model.get('isPaid')) {
      this.$.time.setContent(OB.I18N.formatDate(this.model.get('orderDate')));
    } else {
      this.$.time.setContent(OB.I18N.formatHour(this.model.get('orderDate')));
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
  isApplyButton: true,
  classes: 'btnlink btnlink-gray modal-dialog-button',
  content: OB.I18N.getLabel('OBPOS_LblYesDelete'),
  events: {
    onDeleteOrder: '',
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
    this.doDeleteOrder();
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.btnModalCancelDelete',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  attributes: {
    'onEnterTap': 'hide'
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});