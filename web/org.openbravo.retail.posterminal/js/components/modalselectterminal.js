/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalSelectTerminal',
  closeOnEscKey: false,
  autoDismiss: false,
  executeOnShow: function () {
    this.$.bodyButtons.$.terminalKeyIdentifier.attributes.placeholder = OB.I18N.getLabel('OBPOS_TerminalKeyIdentifier');
    this.$.bodyButtons.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyButtons.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');
  },
  bodyContent: {
    i18nContent: 'OBPOS_SelectTerminalMsg'
  },
  bodyButtons: {
    components: [{
      kind: 'enyo.Input',
      type: 'text',
      name: 'terminalKeyIdentifier',
      classes: 'input-login',
      style: 'display: block; margin-left: auto; margin-right: auto;',
      onkeydown: 'inputKeydownHandler'
    }, {
      kind: 'enyo.Input',
      type: 'text',
      name: 'username',
      classes: 'input-login',
      style: 'display: block; margin-left: auto; margin-right: auto;',
      onkeydown: 'inputKeydownHandler'
    }, {
      kind: 'enyo.Input',
      type: 'password',
      name: 'password',
      classes: 'input-login',
      style: 'display: block; margin-left: auto; margin-right: auto;',
      onkeydown: 'inputKeydownHandler'
    }, {
      kind: 'OB.UI.btnApplyTerminal'
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBPOS_SelectTerminalHeader');
    this.inherited(arguments);
    this.$.headerCloseButton.hide();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnApplyTerminal',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function () {
    var terminalData = {
      terminalKeyIdentifier: this.owner.$.terminalKeyIdentifier.getValue(),
      username: this.owner.$.username.getValue(),
      password: this.owner.$.password.getValue()
    };
    this.doHideThisPopup();
    this.owner.owner.context.linkTerminal(JSON.stringify(terminalData), this.owner.owner.callback);
  }
});