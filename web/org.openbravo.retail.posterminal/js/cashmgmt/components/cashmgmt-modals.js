/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSCashMgmt.UI.modalFinished',
  classes: 'obObposCashMgmtUiModalFinished',
  i18nHeader: 'OBPOS_LblDone',
  i18nBody: 'OBPOS_FinishCashMgmtDialog',
  footer: {
    classes: 'obObposCashMgmtUiModalFinished-footer',
    components: [
      {
        //OK button
        kind: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton',
        classes:
          'obObposCashMgmtUiModalFinished-footer-obObposCashMgmtUiModalFinishedOkButton'
      }
    ]
  },
  executeOnHide: function() {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinished_OkButton',
  classes: 'obObposCashMgmtUiModalFinishedOkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly',
  classes: 'obObposCashMgmtUiModalFinishedWrongly',
  i18nHeader: 'OBPOS_CashMgmtWronglyHeader',
  i18nBody: 'OBPOS_CashMgmtWrongly',
  footer: {
    classes: 'obObposCashMgmtUiModalFinishedWrongly-footer',
    components: [
      {
        //OK button
        kind: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton',
        classes:
          'obObposCashMgmtUiModalFinishedWrongly-footer-obObposCashMgmtUiModalFinishedWronglyOkButton'
      }
    ]
  },
  executeOnHide: function() {
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashMgmt.UI.modalFinishedWrongly_OkButton',
  classes: 'obObposCashMgmtUiModalFinishedWronglyOkButton',
  i18nContent: 'OBMOBC_LblOk',
  tap: function() {
    this.doHideThisPopup();
  }
});
