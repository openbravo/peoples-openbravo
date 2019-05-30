/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

(function () {

  enyo.kind({
    kind: 'OB.UI.ModalAction',
    name: 'OB.UI.MessageDialog',
    classes: 'obUiMessageDialogGeneric',
    header: '',
    bodyContent: {
      name: 'bodymessage',
      classes: 'obUiMessageDialogGeneric-bodymessage',
      content: ''
    },
    bodyButtons: {
      classes: 'obUiMessageDialogGeneric-bodyButtons',
      components: [{
        kind: 'OB.UI.MessageDialogOK',
        classes: 'obUiMessageDialogGeneric-bodyButtons-obUiMessageDialogOK'
      }]
    },
    executeOnShow: function () {
      this.$.header.setContent(this.args.header);
      this.$.bodyContent.$.bodymessage.setContent(this.args.message);
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.MessageDialogOK',
    classes: 'obUiMessageDialogOK',
    i18nContent: 'OBMOBC_LblOk',
    isDefaultAction: true,
    tap: function () {
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
}());