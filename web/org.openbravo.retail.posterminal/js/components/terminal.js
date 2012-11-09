/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.UI.onlineviewer',
  published: {
    onlineState: null
  },
  onlineStateChanged: function () {
    if (this.onlineState) {
      this.$.onlineLabel.setContent(OB.I18N.getLabel('OBPOS_Online'));
      this.$.onlineImg.setAttribute('style', 'display: inline-block; width: 20px; color: transparent; background-image: url(\'./img/iconOnline.png\'); background-repeat: no-repeat; background-position: 2px 3px;');
    } else {
      this.$.onlineLabel.setContent(OB.I18N.getLabel('OBPOS_Offline'));
      this.$.onlineImg.setAttribute('style', 'display: inline-block; width: 20px; color: transparent; background-image: url(\'./img/iconOffline.png\'); background-repeat: no-repeat; background-position: 2px 3px;');
    }
  },
  style: 'display: inline-block; margin-left: 15px;',
  components: [{
    name: 'onlineImg',
    tag: 'span',
    style: 'display: inline-block; width: 20px; color: transparent; background-image: url(\'./img/iconOnline.png\'); background-repeat: no-repeat; background-position: 2px 3px;',
    content: '.'
  }, {
    name: 'onlineLabel',
    tag: 'span',
    content: '' //is set later, in onlineStateChanged function.
  }]
});

// Container for the whole POS application
enyo.kind({
  name: 'OB.UI.Terminal',
  classes: 'pos-container',
  components: [{
    classes: 'section',
    name: 'topsection',
    components: [{
      classes: 'row',
      style: 'height: 50px; vertical-align: middle;',
      components: [{
        classes: 'span12',
        style: 'padding: 10px 0px 0px 0px; color: white; font-size: 16px;',
        components: [{
          name: 'onlineContainer',
          style: 'display: inline-block; vertical-align: middle; margin: 3px 0px 0px 0px;',
          components: [{
            kind: 'OB.UI.onlineviewer',
            name: 'onlineviewer'
          }, {
            name: 'terminal',
            style: 'display: inline-block; margin-left: 50px;'
          }, {
            kind: 'onyx.MenuDecorator',
            style: 'display: inline-block; margin-left: 50px;',
            components: [{
              name: 'yourcompany',
              classes: 'btn-dropdown'
            }, {
              kind: 'onyx.Menu',
              classes: 'dropdown dropdown-clientdata',
              maxHeight: 600,
              scrolling: false,
              floating: true,
              components: [{
                style: 'height: 60px; padding: 0px; background: no-repeat center center url(\'../../utility/ShowImageLogo?logo=yourcompanymenu\');'
              }, {
                name: 'yourcompanyproperties',
                style: 'width: 330px; display: block; padding: 10px; background-color: #FFF899; line-height: 23px;'
              }]
            }]
          }, {
            kind: 'onyx.MenuDecorator',
            style: 'display: inline-block; margin-left: 50px;',
            components: [{
              name: 'loggeduser',
              classes: 'btn-dropdown'
            }, {
              kind: 'onyx.Menu',
              classes: 'dropdown dropdown-clientdata',
              maxHeight: 600,
              scrolling: false,
              floating: true,
              components: [{
                name: 'loggeduserproperties',
                classes: 'dropdown',
                style: 'color: black; padding: 0px; width: 350px;'
              }]
            }]
          }]

        }, {
          style: 'display: inline-block; float: right;',
          components: [{
            style: 'display: inline-block; float: left; margin: 4px 10px 0px 0px;',
            content: 'Openbravo Web POS'
          }, {
            style: 'width: 30px; height: 30px; float: right; margin: 0px 12px 0px 0px;',
            components: [{
              classes: 'top-right-logo'
            }]
          }, {
            name: 'dialogsContainer'
          }]
        }]
      }]
    }, {
      components: [{
        name: 'containerLoading',
        components: [{
          classes: 'POSLoadingCenteredBox',
          components: [{
            classes: 'POSLoadingPromptLabel',
            content: 'Loading...'
          }, {
            classes: 'POSLoadingProgressBar',
            components: [{
              classes: 'POSLoadingProgressBarImg'
            }]
          }]
        }],
        makeId: function () {
          return 'containerLoading';
        }
      }, {
        name: 'containerWindow',
        makeId: function () {
          return 'containerWindow';
        },
        handlers: {
          onShowPopup: 'showPopupHandler',
          onHidePopup: 'hidePopupHandler'
        },
        showPopupHandler: function (inSender, inEvent) {
          if (inEvent.popup) {
            this.showPopup(inEvent.popup, inEvent.args);
          }
        },
        showPopup: function (popupName, args) {
          var componentsArray = this.getComponents(),
              i;
          for (i = 0; i < componentsArray.length; i++) {
            if (componentsArray[i].$[popupName]) {
              componentsArray[i].$[popupName].show(args);
              break;
            }
          }
          return true;
        },
        hidePopupHandler: function (inSender, inEvent) {
          if (inEvent.popup) {
            this.hidePopup(inEvent.popup, inEvent.args);
          }
        },
        hidePopup: function (popupName, args) {
          var componentsArray = this.getComponents(),
              i;
          for (i = 0; i < componentsArray.length; i++) {
            if (componentsArray[i].$[popupName]) {
              componentsArray[i].$[popupName].hide(args);
              break;
            }
          }
          return true;
        }
      }]
    }]
  }, {
    name: 'alertContainer'
  }],
  initComponents: function () {
    //this.terminal = terminal;
    this.inherited(arguments);

    this.terminal.on('change:context', function () {
      var ctx = this.terminal.get('context');
      if (ctx) {
        this.$.loggeduser.setContent(ctx.user._identifier);
        this.$.loggeduserproperties.destroyComponents();
        this.$.loggeduserproperties.createComponent({
          kind: 'OB.UI.Terminal.UserWidget',
          img: ctx.img,
          username: ctx.user._identifier,
          role: ctx.role._identifier
        }).render();
      } else {
        this.$.loggeduser.destroyComponents();
        this.$.loggeduserproperties.destroyComponents();
      }
    }, this);

    this.terminal.on('change:connectedToERP', function (model) {
      this.$.onlineviewer.setOnlineState(model.get('connectedToERP'));
      if (OB.POS.modelterminal.get('loggedOffline') !== undefined) {
        if (OB.POS.modelterminal.get('connectedToERP')) {
          if (!OB.POS.terminal.$.dialogsContainer.$.modalOnline) {
            OB.POS.terminal.$.dialogsContainer.createComponent({
              kind: 'OB.UI.ModalOnline',
              name: 'modalOnline'
            }).render();
          }
          OB.POS.terminal.$.dialogsContainer.$.modalOnline.show();
        } else {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineModeWarning'));
        }
      }
    }, this);

    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function () {
      var name = '';
      var clientname = '';
      var orgname = '';
      var pricelistname = '';
      var currencyname = '';
      var locationname = '';

      if (this.terminal.get('terminal')) {
        name = this.terminal.get('terminal')._identifier;
        clientname = this.terminal.get('terminal')['client' + OB.Constants.FIELDSEPARATOR + '_identifier'];
        orgname = this.terminal.get('terminal')['organization' + OB.Constants.FIELDSEPARATOR + '_identifier'];
      }
      if (this.terminal.get('pricelist')) {
        pricelistname = this.terminal.get('pricelist')._identifier;
        currencyname = this.terminal.get('pricelist')['currency' + OB.Constants.FIELDSEPARATOR + '_identifier'];
      }
      if (this.terminal.get('location')) {
        locationname = this.terminal.get('location')._identifier;
      }

      this.$.terminal.setContent(name);

      this.$.yourcompany.setContent(orgname);
      this.$.yourcompanyproperties.destroyComponents();
      this.$.yourcompanyproperties.createComponent({
        kind: 'OB.UI.Terminal.CompanyWidget',
        clientName: clientname,
        orgName: orgname,
        priceName: pricelistname,
        currencyName: currencyname,
        locationName: locationname
      }).render();

    }, this);
  },
  makeId: function () {
    return 'container';
  }
});

enyo.kind({
  name: 'OB.UI.Terminal.UserWidget',
  style: 'width: 100%',
  components: [{
    style: 'height: 60px; background-color: #FFF899;',
    components: [{
      style: 'float: left; width: 55px; margin: 6px 0px 0px 6px;',
      components: [{
        kind: 'OB.UI.Thumbnail',
        'default': 'img/anonymous-icon.png'
      }]
    }, {
      style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;',
      components: [{
        components: [{
          components: [{
            tag: 'span',
            style: 'font-weight: 600; margin: 0px 0px 0px 5px;',
            name: 'username'
          }]
        }]
      }]
    }, {
      style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;',
      components: [{
        components: [{
          components: [{
            tag: 'span',
            style: 'font-weight: 600; margin: 0px 0px 0px 5px;',
            name: 'role'
          }]
        }]
      }]
    }]
  }, {
    components: [{
      style: 'height: 5px;'
    }, {
      kind: 'OB.UI.MenuAction',
      label: 'Profile',
      //TODO: OB.I18N.getLabel('OBPOS_LblProfile'),
      tap: function () {
        this.owner.owner.parent.hide(); // Manual dropdown menu closure
        OB.POS.terminal.$.dialogsContainer.$.profileDialog.show();
      }
    }, {
      style: 'height: 5px;'
    }, {
      kind: 'OB.UI.MenuAction',
      allowHtml: true,
      label: 'Lock screen <span style="padding-left:190px">0\u21B5</span>',
      tap: function () {
        this.owner.owner.parent.hide(); // Manual dropdown menu closure
        OB.POS.lock();
      }
    }, {
      style: 'height: 5px;'
    }, {
      kind: 'OB.UI.MenuAction',
      label: 'End session',
      //TODO: OB.I18N.getLabel('OBPOS_LblProfile'),
      tap: function () {
        this.owner.owner.parent.hide(); // Manual dropdown menu closure
        OB.POS.terminal.$.dialogsContainer.$.logoutDialog.show();
      }
    }, {
      style: 'height: 5px;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.username.setContent(this.username);
    this.$.role.setContent(this.role);
  }
});

enyo.kind({
  name: 'OB.UI.Terminal.CompanyWidget',
  components: [{
    components: [{
      tag: 'span',
      name: 'clientLbl'
    }, {
      tag: 'span',
      name: 'clientName',
      style: 'font-weight: bold; margin: 0px 0px 0px 5px;'
    }]
  }, {
    components: [{
      tag: 'span',
      name: 'orgLbl'
    }, {
      tag: 'span',
      name: 'orgName',
      style: 'font-weight: bold; margin: 0px 0px 0px 5px;'
    }]
  }, {
    components: [{
      tag: 'span',
      name: 'priceLbl'
    }, {
      tag: 'span',
      name: 'priceName',
      style: 'font-weight: bold; margin: 0px 0px 0px 5px;'
    }]
  }, {
    components: [{
      tag: 'span',
      name: 'currencyLbl'
    }, {
      tag: 'span',
      name: 'currencyName',
      style: 'font-weight: bold; margin: 0px 0px 0px 5px;'
    }]
  }, {
    components: [{
      tag: 'span',
      name: 'locationLbl'
    }, {
      tag: 'span',
      name: 'locationName',
      style: 'font-weight: bold; margin: 0px 0px 0px 5px;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.clientLbl.setContent(OB.I18N.getLabel('OBPOS_CompanyClient'));
    this.$.clientName.setContent(this.clientName);

    this.$.orgLbl.setContent(OB.I18N.getLabel('OBPOS_CompanyOrg'));
    this.$.orgName.setContent(this.orgName);

    this.$.priceLbl.setContent(OB.I18N.getLabel('OBPOS_CompanyPriceList'));
    this.$.priceName.setContent(this.priceName);

    this.$.currencyLbl.setContent(OB.I18N.getLabel('OBPOS_CompanyCurrency'));
    this.$.currencyName.setContent(this.currencyName);

    this.$.locationLbl.setContent(OB.I18N.getLabel('OBPOS_CompanyLocation'));
    this.$.locationName.setContent(this.locationName);
  }
});