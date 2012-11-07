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
  popup: 'modalCancel',

  header: OB.I18N.getLabel('OBPOS_LblCancel'),
  bodyContent: {
    tag: 'div',
    content: OB.I18N.getLabel('OBPOS_ProcessCancelDialog')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.UI.ModalCancel_OkButton'
    }, {
      //Cancel button	
      kind: 'OB.UI.ModalCancel_CancelButton'
    }]
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.ModalCancel_OkButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  isApplyButton: true,
  popup: 'modalCancel',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.ModalCancel_CancelButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  attributes: {
    'onEnterTap': 'hide'
  },
  popup: 'modalCancel',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});