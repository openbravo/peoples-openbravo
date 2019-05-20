/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalSelectTerminal',
  classes: 'obUiModalSelectTerminal',
  closeOnEscKey: false,
  autoDismiss: false,
  executeOnShow: function () {
    this.$.bodyButtons.$.terminalKeyIdentifier.attributes.placeholder = OB.I18N.getLabel('OBPOS_TerminalKeyIdentifier');
    this.$.bodyButtons.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyButtons.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');
  },
  bodyContent: {
    classes: 'obUiModalSelectTerminal-bodyContent',
    i18nContent: 'OBPOS_SelectTerminalMsg'
  },
  bodyButtons: {
    classes: 'obUiModalSelectTerminal-bodyButtons',
    components: [{
      kind: 'enyo.Input',
      type: 'text',
      name: 'terminalKeyIdentifier',
      classes: 'obUiModalSelectTerminal-bodyButtons-terminalKeyIdentifier',
      onkeydown: 'inputKeydownHandler'
    }, {
      kind: 'enyo.Input',
      type: 'text',
      name: 'username',
      classes: 'obUiModalSelectTerminal-bodyButtons-username',
      onkeydown: 'inputKeydownHandler'
    }, {
      kind: 'enyo.Input',
      type: 'password',
      name: 'password',
      classes: 'obUiModalSelectTerminal-bodyButtons-password',
      onkeydown: 'inputKeydownHandler'
    }, {
      classes: 'obUiModalSelectTerminal-bodyButtons-element1',
      kind: 'OB.UI.btnApplyTerminal'
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBPOS_SelectTerminalHeader');
    this.inherited(arguments);
    this.$.headerCloseButton.hide();
    OB.MobileApp.view.currentWindow = 'terminalAuthentication';
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnApplyTerminal',
  classes: 'obUiBtnApplyTerminal',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function () {
    if (this.validateForm()) {
      var terminalData = {
        terminalKeyIdentifier: this.owner.$.terminalKeyIdentifier.getValue(),
        username: this.owner.$.username.getValue(),
        user: this.owner.$.username.getValue(),
        password: this.owner.$.password.getValue(),
        cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId')
      };
      this.doHideThisPopup();
      this.owner.owner.context.linkTerminal(JSON.stringify(terminalData), this.owner.owner.callback);
    }
  },
  validateForm: function () {
    if (_.isEmpty(this.owner.$.terminalKeyIdentifier.getValue()) || _.isEmpty(this.owner.$.username.getValue()) || _.isEmpty(this.owner.$.password.getValue())) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_EmptyTerminalAuthenticationValue'));
      return false;
    }
    return true;
  }
});