/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _ */

enyo.kind({
  name: 'OB.UI.PopupDrawerOpened',
  kind: 'OB.UI.Popup',
  classes: 'modal-dialog',
  bodyContentClass: 'modal-dialog-header-text',
  showing: false,
  closeOnEscKey: false,
  autoDismiss: false,
  keydownHandler: '',
  i18nHeader: 'OBPOS_closeDrawerContinue',
  components: [{
    classes: 'modal-dialog-header',
    name: 'table',
    components: [{
      name: 'header',
      classes: 'modal-dialog-header-text'
    }]
  }, {
    classes: 'modal-dialog-body',
    name: 'bodyParent',
    components: [{
      name: 'bodyContent'
    }]
  }],
  bodyContent: {
    kind: 'enyo.Control',
    name: 'label'
  },
  initComponents: function () {
    this.inherited(arguments);

    this.setStyle('min-height: 180px;');

    if (this.i18nHeader) {
      this.$.header.setContent(OB.I18N.getLabel(this.i18nHeader));
    } else {
      this.$.header.setContent(this.header);
    }

    this.$.bodyContent.setClasses(this.bodyContentClass);
    if (this.bodyContent && this.bodyContent.i18nContent) {
      this.$.bodyContent.setContent(OB.I18N.getLabel(this.bodyContent.i18nContent));
    } else {
      this.$.bodyContent.createComponent(this.bodyContent);
    }

  }
});

OB.MobileApp.model.hookManager.registerHook('OBPOS_LoadPOSWindow', function (args, callbacks) {
  if (OB.MobileApp.model.get('permissions').OBPOS_closeDrawerBeforeContinue) {
    OB.POS.hwserver.isDrawerClosed({
      openFirst: false
    }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
  }
  OB.MobileApp.model.hookManager.callbackExecutor(args, callbacks);
});