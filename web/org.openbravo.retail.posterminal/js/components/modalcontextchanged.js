/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModaContextChangedAccept',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  i18nContent: 'OBMOBC_LblOk',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    OB.MobileApp.model.logout();
  }
});


enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalContextChanged',
  bodyContent: {},
  closeOnEscKey: false,
  autoDismiss: false,
  i18nHeader: 'OBPOS_ContextChanged',
  bodyButtons: {
    components: [{
      initComponents: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_ContextChangedMessage'));
      }
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModaContextChangedAccept'
    }]
  }
});