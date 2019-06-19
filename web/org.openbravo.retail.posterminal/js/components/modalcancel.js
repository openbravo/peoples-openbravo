/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalCancel',
  popup: 'modalCancel',
  classes: 'obUiModalCancel',
  i18nHeader: 'OBMOBC_LblCancel',
  bodyContent: {
    classes: 'obUiModalCancel-bodyContent',
    i18nContent: 'OBPOS_ProcessCancelDialog'
  },
  bodyButtons: {
    classes: 'obUiModalCancel-bodyButtons',
    components: [
      {
        //OK button
        classes: 'obUiModalCancel-bodyButtons-obUiModalCancelOkButton',
        kind: 'OB.UI.ModalCancel_OkButton'
      },
      {
        //Cancel button
        classes: 'obUiModalCancel-bodyButtons-obUiModalCancelCancelButton',
        kind: 'OB.UI.ModalCancel_CancelButton'
      }
    ]
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalCancel_OkButton',
  classes: 'obUiModalCancelOkButton',
  i18nContent: 'OBMOBC_LblOk',
  isDefaultAction: true,
  popup: 'modalCancel',
  tap: function() {
    this.doHideThisPopup();
    OB.POS.navigate('retail.pointofsale');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalCancel_CancelButton',
  classes: 'obUiModalCancelCancelButton',
  i18nContent: 'OBMOBC_LblCancel',
  popup: 'modalCancel',
  tap: function() {
    this.doHideThisPopup();
  }
});
