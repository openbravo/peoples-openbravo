/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
  i18nHeader: 'OBPOS_LblAssignReceipt',
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
    if (this.model.get('bp')) {
      this.$.bp.setContent(this.model.get('bp').get('_identifier'));
    }
    this.$.total.setContent(this.model.printTotal());
  }
});

/*delete confirmation modal*/

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalDeleteReceipt',
  bodyContent: {
    i18nContent: 'OBPOS_MsgConfirmDelete' // TODO: add this as part of the message + '\n' + OB.I18N.getLabel('OBPOS_cannotBeUndone')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.btnModalApplyDelete'
    }, {
      kind: 'OB.UI.btnModalCancelDelete'
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBPOS_ConfirmDeletion');
    this.inherited(arguments);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnModalApplyDelete',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblYesDelete',
  events: {
    onDeleteOrder: ''
  },
  tap: function () {
    this.doHideThisPopup();
    this.doDeleteOrder();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnModalCancelDelete',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});