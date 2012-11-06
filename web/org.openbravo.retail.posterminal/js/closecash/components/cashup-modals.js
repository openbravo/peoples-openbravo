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
  name: 'OB.OBPOSCashUp.UI.modalFinished',
  header: OB.I18N.getLabel('OBPOS_LblGoodjob'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_FinishCloseDialog')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.OBPOSCashUp.UI.modalFinished_OkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSCashUp.UI.modalFinished_OkButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashUp.UI.modalFinishedWrongly',
  header: OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_CashUpWrongly')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.OBPOSCashUp.UI.modalFinishedWrongly_OkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSCashUp.UI.modalFinishedWrongly_OkButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
  header: OB.I18N.getLabel('OBPOS_LblReceiptsToProcess'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_MsgReceiptsProcess')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess_OkButton'
    }, {
      //Cancel button
      kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess_CancelButton'
    }]
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess_OkButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess_CancelButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
    OB.POS.navigate('retail.pointofsale');
  }
});