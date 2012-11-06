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
  name: 'OB.OBPOSCashMgmt.UI.modalFinished',
  header: OB.I18N.getLabel('OBPOS_LblDone'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_FinishCashMgmtDialog')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function () {
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
      }
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly',
  header: OB.I18N.getLabel('OBPOS_CashMgmtWronglyHeader'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_CashMgmtWrongly')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function () {
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
      }
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});