/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $, enyo */

(function () {

  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};
  OB.OBPOSLogin = window.OB.OBPOSLogin || {};
  OB.OBPOSLogin.UI = window.OB.OBPOSLogin.UI || {};

  OB.OBPOSLogin.loginButtonAction = function () {
    var u = $('#username').val();
    var p = $('#password').val();
    if (!u || !p) {
      alert('Please enter your username and password');
    } else {
      OB.POS.modelterminal.login(u, p);
    }
  };

  enyo.kind({
    name: 'OB.OBPOSLogin.UI.UserButton',
    classes: 'login-user-button',
    user: null,
    userImage: null,
    userConnected: null,
    components: [{
      classes: 'login-user-button-bottom',
      components: [{
        name: 'bottomIcon',
        classes: 'login-user-button-bottom-icon',
        content: ['.']
      }, {
        name: 'bottomText',
        classes: 'login-user-button-bottom-text'
      }]
    }],
    tap: function () {
      var u = this.user;
      $('#username').val(u);
      $('#password').val('');
      $('#password').focus();
      //OB.POS.modelterminal.login(u, p, 'userImgPress'); // Disable 'openbravo' autologin feature
    },
    create: function () {
      this.inherited(arguments);

      if (this.userImage && this.userImage !== 'none') {
        this.applyStyle('background-image', 'url(' + this.userImage + ')');
      }
      if (this.userConnected === 'true') {
        this.$.bottomIcon.applyStyle('background-image', 'url(img/iconOnlineUser.png)');
      }
      if (this.user) {
        this.$.bottomText.setContent(this.user);
      }
    }
  });

  enyo.kind({
    name: 'OB.OBPOSLogin.UI.LoginButton',
    kind: 'OB.UI.ModalDialogButton',
    style: 'min-width: 115px;',
    id: 'loginaction',
    tap: function () {
      OB.OBPOSLogin.loginButtonAction();
    },
    initComponents: function () {
      this.inherited(arguments);
      this.content = OB.I18N.getLabel('OBPOS_LoginButton');
    }
  });

  enyo.kind({
    name: 'OB.OBPOSLogin.UI.Login',
    tag: 'section',
    components: [{
      classes: 'row login-header-row',
      components: [{
        name: 'loginHeaderCompany',
        classes: 'login-header-company'
      }, {
        classes: 'login-header-ob'
      }]
    }, {
      classes: 'row',
      components: [{
        classes: 'span6',
        components: [{
          name: 'loginUserContainer',
          classes: 'row login-user-container',
          content: ['.']
        }]
      }, {
        classes: 'span6',
        components: [{
          classes: 'row-fluid login-inputs-container',
          components: [{
            name: 'loginInputs',
            components: [{
              classes: 'row',
              components: [{
                classes: 'span6 login-inputs-screenlocked',
                name: 'screenLockedLbl'
              }]
            }, {
              classes: 'row',
              components: [{
                classes: 'span6 login-inputs-userpassword',
                components: [{
                  tag: 'input',
                  name: 'username',
                  id: 'username',
                  classes: 'login-inputs-username',
                  attributes: {
                    type: 'text',
                    onkeydown: 'if(event && event.keyCode == 13) { OB.OBPOSLogin.loginButtonAction(); }; return true;'
                  }
                }]
              }]
            }, {
              classes: 'row',
              components: [{
                classes: 'span6 login-inputs-userpassword',
                components: [{
                  tag: 'input',
                  name: 'password',
                  id: 'password',
                  classes: 'login-inputs-password',
                  attributes: {
                    type: 'password',
                    onkeydown: 'if(event && event.keyCode == 13) { OB.OBPOSLogin.loginButtonAction(); }; return true;'
                  }
                }]
              }]
            }, {
              classes: 'row',
              components: [{
                classes: 'span1',
                style: 'color: transparent;',
                content: ['.']
              }, {
                classes: 'span1',
                style: 'color: transparent;',
                content: ['.']
              }, {
                classes: 'span2',
                style: 'margin: 20px 0px 0px 0px; text-align: center;',
                components: [{
                  kind: 'OB.OBPOSLogin.UI.LoginButton'
                }]
              }, {
                classes: 'span2',
                components: [{
                  kind: 'OB.UI.Clock',
                  classes: 'login-clock'
                }]
              }]
            }]
          }, {
            name: 'loginBrowserNotSupported',
            style: 'display: none;',
            components: [{
              classes: 'row',
              components: [{
                name: 'LoginBrowserNotSupported_Title_Lbl',
                classes: 'span6 login-browsernotsupported-title'
              }]
            }, {
              classes: 'row',
              components: [{
                classes: 'span4',
                components: [{
                  name: 'LoginBrowserNotSupported_P1_Lbl',
                  classes: 'login-browsernotsupported-content'
                }, {
                  name: 'LoginBrowserNotSupported_P2_Lbl',
                  classes: 'login-browsernotsupported-content'
                }, {
                  name: 'LoginBrowserNotSupported_P3_Lbl',
                  classes: 'login-browsernotsupported-content'
                }]
              }, {
                classes: 'span2',
                components: [{
                  kind: 'OB.UI.Clock',
                  classes: 'login-clock'
                }]
              }]
            }]
          }]
        }]
      }

      ]
    }

    ],
    initComponents: function () {
      this.inherited(arguments);

      this.$.screenLockedLbl.setContent(OB.I18N.getLabel('OBPOS_LoginScreenLocked'));
      this.$.username.attributes.placeholder = OB.I18N.getLabel('OBPOS_LoginUserInput');
      this.$.password.attributes.placeholder = OB.I18N.getLabel('OBPOS_LoginPasswordInput');

      this.$.LoginBrowserNotSupported_Title_Lbl.setContent(OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported'));
      this.$.LoginBrowserNotSupported_P1_Lbl.setContent(OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P1'));
      this.$.LoginBrowserNotSupported_P2_Lbl.setContent(OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P2', ['Chrome, Safari, Safari (iOS)', 'Android']));
      this.$.LoginBrowserNotSupported_P3_Lbl.setContent(OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P3'));

      this.postRenderActions();
    },

    postRenderActions: function () {
      var me = this,
          requestA, requestB;
      OB.POS.modelterminal.on('loginfail', function (status, data) {
        var msg;
        if (data && data.messageTitle) {
          msg = data.messageTitle;
        }

        if (data && data.messageText) {
          msg += (msg ? '\n' : '') + data.messageText;
        }
        msg = msg || 'Invalid user name or password.\nPlease try again.';
        alert(msg);
        $('#password').val('');
        $('#username').focus();
      });
      OB.POS.modelterminal.on('loginUserImgPressfail', function (status) {
        //If the user image press (try to login with default password) fails, then no alert is shown and the focus goes directly to the password input
        $('#password').val('');
        $('#password').focus();
      });

      function setUserImages(jsonImgData) {
        var name = [],
            userName = [],
            image = [],
            connected = [],
            target = me.$.loginUserContainer,
            i;
        jsonImgData = jsonImgData.response[0].data;
        enyo.forEach(jsonImgData, function (v) {
          name.push(v.name);
          userName.push(v.userName);
          image.push(v.image);
          connected.push(v.connected);
        });
        for (i = 0; i < name.length; i++) {
          target.createComponent({
            kind: 'OB.OBPOSLogin.UI.UserButton',
            user: userName[i],
            userImage: image[i],
            userConnected: connected[i]
          });
        }
        target.render();
        OB.UTIL.showLoading(false);
        return true;
      }

      function setCompanyLogo(jsonCompanyLogo) {
        var logoUrl = [];
        jsonCompanyLogo = jsonCompanyLogo.response[0].data;
        enyo.forEach(jsonCompanyLogo, function (v) {
          logoUrl.push(v.logoUrl);
        });
        me.$.loginHeaderCompany.applyStyle('background-image', 'url("' + logoUrl[0] + '")');
        return true;
      }

      requestA = new enyo.Ajax({
        url: "../../org.openbravo.retail.posterminal.service.loginutils",
        method: "GET",
        handleAs: "json",
        contentType: 'application/json;charset=utf-8',
        data: {
          command: 'companyLogo',
          terminalName: OB.POS.paramTerminal
        },
        success: function (inSender, inResponse) {
          setCompanyLogo(inResponse);
        }
      });
      requestA.response('success');
      requestA.go(requestA.data);

      if (!OB.UTIL.isSupportedBrowser()) { //If the browser is not supported, show message and finish.
        me.$.loginInputs.setStyle('display: none');
        me.$.loginBrowserNotSupported.setStyle('display: block');
        OB.UTIL.showLoading(false);
        return true;
      }

      requestB = new enyo.Ajax({
        url: "../../org.openbravo.retail.posterminal.service.loginutils",
        method: "GET",
        handleAs: "json",
        contentType: 'application/json;charset=utf-8',
        data: {
          command: 'userImages',
          terminalName: OB.POS.paramTerminal
        },
        success: function (inSender, inResponse) {
          setUserImages(inResponse);
        }
      });
      requestB.response('success');
      requestB.go(requestB.data);

      $('#username').focus();
    }

  });

}());