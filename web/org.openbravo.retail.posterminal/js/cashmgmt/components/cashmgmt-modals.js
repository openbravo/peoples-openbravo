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
  i18nHeader: 'OBPOS_LblDone',
  bodyContent: {
    i18Content: 'OBPOS_FinishCashMgmtDialog'
  },
  bodyButtons: {
    components: [{
      //OK button
      kind: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function () {
    this.doHideThisPopup();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly',
  i18nHeader: 'OBPOS_CashMgmtWronglyHeader',
  bodyContent: {
    i18Content: 'OBPOS_CashMgmtWrongly'
  },
  bodyButtons: {
    components: [{
      //OK button
      kind: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function () {
    this.doHideThisPopup();
  }
});