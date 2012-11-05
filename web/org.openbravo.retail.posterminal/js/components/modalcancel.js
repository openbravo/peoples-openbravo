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
      tap: function () {
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

  makeId: function () {
    // ensure id is fixed
    return 'modalCancel';
  }
});