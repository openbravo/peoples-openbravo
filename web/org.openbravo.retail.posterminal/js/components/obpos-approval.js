/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OB.UTIL.Approval',
  kind: 'OB.UI.ModalAction',
  statics: {
    /**
     * Static method to display the approval popup.
     * 
     * When the approval is requested and checked, 'approvalChecked' event is
     * triggered in model parameter. This event has a boolean parameter 'approved'
     * that determines whether the approval was accepted or rejected. 
     */
    requestApproval: function (model, approvalType) {
      var dialog;

      dialog = OB.MobileApp.view.$.confirmationContainer.createComponent({
        kind: 'OB.UTIL.Approval',
        model: model,
        approvalType: approvalType
      });

      dialog.show();
    }
  },
  handlers: {
    onCheckCredentials: 'checkCredentials'
  },

  header: 'Approval required',
  //TODO: trl
  bodyContent: {
    name: 'loginInputs',
    classes: 'login-inputs-browser-compatible',
    components: [{
      components: [{
        classes: 'login-status-info',
        style: 'float: left;',
        name: 'connectStatus'
      }, {
        classes: 'login-status-info',
        name: 'screenLockedLbl'
      }]
    }, {
      components: [{
        kind: 'enyo.Input',
        type: 'text',
        name: 'username',
        classes: 'input-login',
        onkeydown: 'inputKeydownHandler'
      }]
    }, {
      components: [{
        kind: 'enyo.Input',
        type: 'password',
        name: 'password',
        classes: 'input-login',
        onkeydown: 'inputKeydownHandler'
      }]
    }]
  },

  bodyButtons: {
    components: [{
      kind: 'OB.UTIL.Approval.ApproveButton',
    }]
  },

  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyContent.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyContent.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');
  },

  checkCredentials: function () {
    this.model.checkApproval(this.approvalType, this.$.bodyContent.$.username.getValue(), this.$.bodyContent.$.password.getValue());
    this.waterfall('onHideThisPopup', {});
  }
});

enyo.kind({
  name: 'OB.UTIL.Approval.ApproveButton',
  kind: 'OB.UI.ModalDialogButton',
  i18nLabel: 'OBPOS_Approve',
  events: {
    onCheckCredentials: ''
  },
  tap: function () {
    this.doCheckCredentials();
  }
});