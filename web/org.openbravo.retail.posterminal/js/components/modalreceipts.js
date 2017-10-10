/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo*/

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
  }
});

enyo.kind({
  name: 'OB.UI.ListReceiptLine',
  kind: 'OB.UI.SelectButton',
  events: {
    onHideThisPopup: '',
    onChangeCurrentOrder: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doChangeCurrentOrder({
      newCurrentOrder: this.model
    });
    this.doHideThisPopup();
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px; width: 100%;',
    components: [{
      style: 'float: left; width: 95px;',
      components: [{
        style: 'float: left; width: 100%;',
        name: 'date'
      }, {
        style: 'clear: both;'
      }, {
        style: 'float: left; width: 100%;',
        name: 'time'
      }]
    }, {
      style: 'float: left; width: calc(100% - 185px);',
      components: [{
        style: 'float: left; width: 100%;',
        name: 'orderNo'
      }, {
        style: 'clear: both;'
      }, {
        style: 'float: left; width: 100%;',
        name: 'bp'
      }]
    }, {
      style: 'float: right; font-weight: bold; text-align: right;',
      name: 'total'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.date.setContent(OB.I18N.formatDate(new Date(this.model.get('orderDate'))));
    this.$.time.setContent(OB.I18N.formatHour(OB.I18N.normalizeDate(this.model.get('creationDate') || this.model.get('orderDate'))));
    this.$.orderNo.setContent(this.model.get('documentNo'));
    this.$.bp.setContent(this.model.get('bp').get('_identifier'));
    this.$.total.setContent(this.model.printTotal());
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderListReceiptLine', {
      listReceiptLine: this
    });
  }
});

/*delete confirmation modal*/

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalDeleteReceipt',
  events: {
    onDisableLeftToolbar: ''
  },
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
  executeOnHide: function () {
    this.doDisableLeftToolbar({
      status: false
    });
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
    this.doDeleteOrder({
      notSavedOrder: true
    });
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