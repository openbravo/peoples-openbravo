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
    onCheckCredentials: 'checkCredentials',
    onUserImgClick: 'handleUserImgClick'
  },

  header: 'Approval required',
  //TODO: trl
  bodyContent: {
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
        style: 'background-color:#5A5A5A;',
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
        }]
      }]
    }]
  },

  bodyButtons: {
    components: [{
      kind: 'OB.UTIL.Approval.ApproveButton',
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
    this.inherited(arguments);
    this.$.bodyContent.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyContent.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');

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
    params.approvalType = this.approvalType;
    new OB.OBPOSLogin.UI.LoginRequest({
      url: OB.MobileApp.model.get('loginUtilsUrl')
    }).response(this, 'setUserImages').go(params);
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