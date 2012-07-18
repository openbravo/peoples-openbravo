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

  OB.UI.ModalReceipts = OB.COMP.Modal.extend({

    id: 'modalreceipts',
    header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
    getContentView: function() {
      return OB.COMP.ListReceipts;
    },
    showEvent: function(e) {
      // custom bootstrap event, no need to prevent default
      this.options.modelorderlist.saveCurrent();
    }
  });

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