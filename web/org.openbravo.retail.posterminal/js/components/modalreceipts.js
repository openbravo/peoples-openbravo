/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, $, B, Backbone */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

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
    receiptsListChanged: function(oldValue){
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
    receiptsListChanged: function(oldValue){
      this.$.receiptslistitemprinter.setCollection(this.receiptsList);
      this.receiptsList.on('click', function(model){
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
  
  
  
//  OB.UI.ModalReceipts = OB.COMP.Modal.extend({
//    id: 'modalreceipts',
//    header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
//    getContentView: function() {
//      return OB.COMP.ListReceipts;
//    },
//    showEvent: function(e) {
//      // custom bootstrap event, no need to prevent default
//      this.options.modelorderlist.saveCurrent();
//    }
//  });

  OB.COMP.ModalDeleteReceipt = OB.COMP.ModalAction.extend({
    id: 'modalDeleteReceipt',
    header: OB.I18N.getLabel('OBPOS_ConfirmDeletion'),

    setBodyContent: function() {
      return ({
        kind: B.KindJQuery('div'),
        content: [
          OB.I18N.getLabel('OBPOS_MsgConfirmDelete'),{kind: B.KindJQuery('br')},OB.I18N.getLabel('OBPOS_cannotBeUndone')]
      });
    },

    setBodyButtons: function() {
      return ({
        kind: B.KindJQuery('div'),
        content: [{
          kind: OB.COMP.DeleteReceiptDialogApply
        }, {
          kind: OB.COMP.DeleteReceiptDialogCancel
        }]
      });
    }
  });

  // Apply the changes
  OB.COMP.DeleteReceiptDialogApply = OB.COMP.Button.extend({
    isActive: true,
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function() {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblYesDelete'));
      return this;
    },
    clickEvent: function(e) {
      // If the model order does not have an id, it has not been
      // saved in the database yet, so there is no need to remove it
      if (this.options.modelorder.get('id')) {
        // makes sure that the current order has the id
        this.options.modelorderlist.saveCurrent();
        // removes the current order from the database
        OB.Dal.remove(this.options.modelorderlist.current, null, null);
      }
      this.options.modelorderlist.deleteCurrent();
      $('#modalDeleteReceipt').modal('hide');
    }
  });

  // Cancel
  OB.COMP.DeleteReceiptDialogCancel = OB.COMP.Button.extend({
    attributes: {
      'data-dismiss': 'modal'
    },
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function() {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      return this;
    },
    clickEvent: function(e) {
    }
  });

}());