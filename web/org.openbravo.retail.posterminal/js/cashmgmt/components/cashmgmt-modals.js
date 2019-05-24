/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashMgmt.UI.modalFinished',
  classes: 'obObposCashMgmtUiModalFinished',
  i18nHeader: 'OBPOS_LblDone',
  bodyContent: {
    i18nContent: 'OBPOS_FinishCashMgmtDialog',
    classes: 'obObposCashMgmtUiModalFinished-bodyContent'
  },
  bodyButtons: {
    classes: 'obObposCashMgmtUiModalFinished-bodyButtons',
    components: [{
      //OK button
      kind: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton',
      classes: 'obObposCashMgmtUiModalFinished-bodyButtons-obObposCashMgmtUiModalFinishedOkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton',
  classes: 'obObposCashMgmtUiModalFinishedOkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function () {
    this.doHideThisPopup();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly',
  classes: 'obObposCashMgmtUiModalFinishedWrongly',
  i18nHeader: 'OBPOS_CashMgmtWronglyHeader',
  bodyContent: {
    i18nContent: 'OBPOS_CashMgmtWrongly',
    classes: 'obObposCashMgmtUiModalFinishedWrongly-bodyContent'
  },
  bodyButtons: {
    classes: 'obObposCashMgmtUiModalFinishedWrongly-bodyButtons',
    components: [{
      //OK button
      kind: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton',
      classes: 'obObposCashMgmtUiModalFinishedWrongly-bodyButtons-obObposCashMgmtUiModalFinishedWronglyOkButton'
    }]
  },
  executeOnHide: function () {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton',
  classes: 'obObposCashMgmtUiModalFinishedWronglyOkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function () {
    this.doHideThisPopup();
  }
});