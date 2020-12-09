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
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalContextChanged',
  classes: 'obUiModalContextChanged',
  closeOnEscKey: false,
  autoDismiss: false,
  i18nHeader: 'OBPOS_ContextChanged',
  footer: {
    classes: 'obUiModalContextChanged-footer',
    components: [
      {
        classes: 'obUiModalContextChanged-footer-element1',
        initComponents: function() {
          this.setContent(OB.I18N.getLabel('OBPOS_ContextChangedMessage'));
        }
      },
      {
        classes:
          'obUiModalContextChanged-footer-obObpospointofsaleUiModalsBtnModaContextChangedAccept',
        kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModaContextChangedAccept'
      }
    ]
  }
});
