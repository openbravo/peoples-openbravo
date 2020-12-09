/*
 ************************************************************************************
 * Copyright (C) 2014-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

(function() {
  enyo.kind({
    kind: 'OB.UI.Modal',
    name: 'OB.UI.MessageDialog',
    classes: 'obUiMessageDialogGeneric',
    header: '',
    body: '',
    footer: {
      classes: 'obUiMessageDialogGeneric-footer',
      components: [
        {
          kind: 'OB.UI.MessageDialogOK',
          classes: 'obUiMessageDialogGeneric-footer-obUiMessageDialogOK'
        }
      ]
    },
    executeOnShow: function() {
      this.setHeader(this.args.header ? this.args.header : this.header);
      this.$.body.show();
      this.setBody(this.args.message);
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.MessageDialogOK',
    classes: 'obUiMessageDialogOK',
    i18nContent: 'OBMOBC_LblOk',
    isDefaultAction: true,
    tap: function() {
      this.doHideThisPopup();
    }
  });

  OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
    kind: 'OB.UI.MessageDialog',
    name: 'OB_UI_MessageDialog',
    classes: 'obUiMessageDialog'
  });

  //  object.doShowPopup({   // OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
  //    popup: 'OB_UI_MessageDialog',
  //    args: {
  //      header: header,
  //      message: message
  //    }
  //  });
})();
