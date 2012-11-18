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
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalCancel_OkButton',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  isDefaultAction: true,
  popup: 'modalCancel',
  tap: function () {
    this.doHideThisPopup();
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalCancel_CancelButton',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  popup: 'modalCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});