/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalSelectOpenReceipts.btnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalSelectOpenReceipts.btnCancel',
  isDefaultAction: true,
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalSelectOpenReceipts',
  i18nHeader: 'OBPOS_modalNoEditableHeader',
  //body of the popup
  bodyContent: [{
    content: OB.I18N.getLabel('OBPOS_lblDeferredSellSelectTicket')
  }, {
    content: OB.I18N.getLabel('OBPOS_lblDeferredSellOpenAfterApply')
  }],
  //buttons of the popup
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalSelectOpenReceipts.btnApply'
    }, {
      kind: 'OB.UI.ModalSelectOpenReceipts.btnCancel'
    }]
  },
  executeOnHide: function () {
    //executed when popup is hiden.
    //to access to argumens -> this.args
  },
  executeOnShow: function () {
    //executed when popup is shown.
    //to access to argumens -> this.args
  },
  init: function (model) {
    // TODO: La aprobación (si fue necesaria debe ser copiada a la nueva orden o a la orden seleccionada)
  }
});