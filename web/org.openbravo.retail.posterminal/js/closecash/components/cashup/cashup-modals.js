/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
  classes: 'obObposCashupUiModalPendingToProcess',
  i18nHeader: 'OBPOS_LblReceiptsToProcess',
  i18nBody: 'OBPOS_MsgReceiptsProcess',
  footer: {
    classes: 'obObposCashupUiModalPendingToProcess-footer',
    components: [
      {
        //Cancel button
        kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess_CancelButton',
        classes:
          'obObposCashupUiModalPendingToProcess-footer-obObposCashupUiModalPendingToProcessCancelButton'
      },
      {
        //OK button
        kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess_OkButton',
        classes:
          'obObposCashupUiModalPendingToProcess-footer-obObposCashupUiModalPendingToProcessOkButton'
      }
    ]
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess_OkButton',
  classes: 'obObposCashupUiModalPendingToProcessOkButton',
  i18nContent: 'OBMOBC_LblOk',
  isDefaultAction: true,
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess_CancelButton',
  classes: 'obObposCashupUiModalPendingToProcessCancelButton',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.doHideThisPopup();
    OB.POS.navigate('retail.pointofsale');
  }
});
