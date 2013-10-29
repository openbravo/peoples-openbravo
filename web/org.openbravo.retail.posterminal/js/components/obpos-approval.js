/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

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
    onCheckCredentials: 'checkCredentials',
    onUserImgClick: 'handleUserImgClick'
  },

  i18nHeader: 'OBPOS_ApprovalRequiredTitle',
  bodyContent: {
    components: [{
      name: 'explainApprovalTxt'
    }, {
      classes: 'login-header-row',
      style: 'color:black; line-height: 20px;',
      components: [{
        classes: 'span6',
        components: [{
          kind: 'Scroller',
          thumb: true,
          horizontal: 'hidden',
          name: 'loginUserContainer',
          classes: 'login-user-container',
          style: 'background-color:#5A5A5A; margin: 5px;',
          content: ['.']
        }]
      }, {
        classes: 'span6',
        components: [{
          classes: 'login-inputs-container',
          components: [{
            name: 'loginInputs',
            classes: 'login-inputs-browser-compatible',
            components: [{
              components: [{
                kind: 'OB.UTIL.Approval.Input',
                name: 'username'
              }]
            }, {
              components: [{
                kind: 'OB.UTIL.Approval.Input',
                name: 'password',
                type: 'password'
              }]
            }]
          }]
        }]
      }]
    }]
  },

  bodyButtons: {
    components: [{
      kind: 'OB.UTIL.Approval.ApproveButton'
    }, {
      kind: 'OB.UI.ModalDialogButton',
      i18nLabel: 'OBPOS_Cancel',
      tap: function () {
        this.bubble('onHideThisPopup');
      }
    }]
  },

  handleUserImgClick: function (inSender, inEvent) {
    var u = inEvent.originator.user;
    this.$.bodyContent.$.username.setValue(u);
    this.$.bodyContent.$.password.setValue('');
    this.$.bodyContent.$.password.focus();
    return true;
  },

  initComponents: function () {
    var msg = '';
    this.inherited(arguments);
    this.$.bodyContent.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyContent.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');
    if (!Array.isArray(this.approvalType)) {
      this.approvalType = [this.approvalType];
    }
    _.each(this.approvalType, function (approval) {
      msg = msg + ' ' + (OB.I18N.labels[this.approvalType] || OB.I18N.getLabel('OBPOS_ApprovalTextHeader'));
    });
    this.$.bodyContent.$.explainApprovalTxt.setContent(msg);

    this.postRenderActions();
  },

  setUserImages: function (inSender, inResponse) {
    var name = [],
        userName = [],
        image = [],
        connected = [],
        me = this,
        jsonImgData, i;


    if (!inResponse.data) {
      OB.Dal.find(OB.Model.User, {}, function (users) {
        var i, user, session;
        for (i = 0; i < users.models.length; i++) {
          user = users.models[i];
          name.push(user.get('name'));
          userName.push(user.get('name'));
          connected.push(false);
        }
        me.renderUserButtons(name, userName, image, connected);
      }, function () {
        window.console.error(arguments);
      });
      return true;
    }
    jsonImgData = inResponse.data;
    enyo.forEach(jsonImgData, function (v) {
      name.push(v.name);
      userName.push(v.userName);
      image.push(v.image);
    });
    this.renderUserButtons(name, userName, image, connected);
  },

  renderUserButtons: function (name, userName, image) {
    var i, target = this.$.bodyContent.$.loginUserContainer;
    for (i = 0; i < name.length; i++) {
      target.createComponent({
        kind: 'OB.OBPOSLogin.UI.UserButton',
        user: userName[i],
        userImage: image[i],
        showConnectionStatus: false
      });
    }
    target.render();
    return true;
  },

  postRenderActions: function () {
    var params = OB.MobileApp.model.get('loginUtilsParams') || {};
    params.appName = OB.MobileApp.model.get('appName');

    params.command = 'userImages';
    params.approvalType = JSON.stringify(this.approvalType);
    new OB.OBPOSLogin.UI.LoginRequest({
      url: OB.MobileApp.model.get('loginUtilsUrl')
    }).response(this, 'setUserImages').go(params);
  },

  checkCredentials: function () {
    var u = this.$.bodyContent.$.username.getValue(),
        p = this.$.bodyContent.$.password.getValue();

    if (!u || !p) {
      alert(OB.I18N.getLabel('OBPOS_EmptyUserPassword'));
    } else {
      this.model.checkApproval(this.approvalType, u, p);
      this.waterfall('onHideThisPopup', {});
    }
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

enyo.kind({
  name: 'OB.UTIL.Approval.Input',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input-login',
  handlers: {
    onkeydown: 'inputKeydownHandler'
  },
  events: {
    onCheckCredentials: ''
  },
  inputKeydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13) { //Handle ENTER key
      this.doCheckCredentials();
      return true;
    }
    return false;
  }
});