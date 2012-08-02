/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalCancel',
  attributes: {
    id: 'modalCancel'
  },
  header: OB.I18N.getLabel('OBPOS_LblCancel'),
  bodyContent: {
    tag: 'div',
    content: OB.I18N.getLabel('OBPOS_ProcessCancelDialog')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function() {
        $('#modalCancel').modal('hide');
        OB.POS.navigate('retail.pointofsale');
      }
    }, {
      //Cancel button	
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      attributes: {
        'data-dismiss': 'modal'
      },
      content: OB.I18N.getLabel('OBPOS_LblCancel')
    }]
  },

  makeId: function() {
    // ensure id is fixed
    return 'modalCancel'
  }
});

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // reimplemented with enyo: OB.UI.ModalAction
  OB.COMP.ModalCancel = OB.COMP.ModalAction.extend({
    id: 'modalCancel',
    header: OB.I18N.getLabel('OBPOS_LblCancel'),

    setBodyContent: function() {
      return ({
        kind: B.KindJQuery('div'),
        content: [
        OB.I18N.getLabel('OBPOS_ProcessCancelDialog')]
      });
    },

    setBodyButtons: function() {
      return ({
        kind: B.KindJQuery('div'),
        content: [{
          kind: OB.COMP.CancelDialogOk
        }, {
          kind: OB.COMP.CancelDialogCancel
        }]
      });
    }
  });

  // Exit
  OB.COMP.CancelDialogOk = OB.COMP.Button.extend({
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function() {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblOk'));
      return this;
    },
    clickEvent: function(e) {
      $('#modalCancel').modal('hide');
      OB.POS.navigate('retail.pointofsale');
    }
  });

  // Cancel
  OB.COMP.CancelDialogCancel = OB.COMP.Button.extend({
    attributes: {
      'data-dismiss': 'modal'
    },
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function() {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      return this;
    },
    clickEvent: function(e) {}
  });

}());