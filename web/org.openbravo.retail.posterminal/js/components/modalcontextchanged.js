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
  classes: 'obObpospointofsaleUiModalsBtnModaContextChangedAccept',
  i18nContent: 'OBMOBC_LblOk',
  events: {
    onHideThisPopup: ''
  },
  tap: function() {
    OB.MobileApp.model.logout();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalContextChanged',
  classes: 'obUiModalContextChanged',
  bodyContent: {},
  closeOnEscKey: false,
  autoDismiss: false,
  i18nHeader: 'OBPOS_ContextChanged',
  bodyButtons: {
    classes: 'obUiModalContextChanged-bodyButtons',
    components: [
      {
        classes: 'obUiModalContextChanged-bodyButtons-element1',
        initComponents: function() {
          this.setContent(OB.I18N.getLabel('OBPOS_ContextChangedMessage'));
        }
      },
      {
        classes:
          'obUiModalContextChanged-bodyButtons-obObpospointofsaleUiModalsBtnModaContextChangedAccept',
        kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModaContextChangedAccept'
      }
    ]
  }
});
