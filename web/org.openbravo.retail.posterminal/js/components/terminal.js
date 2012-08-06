/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function() {
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Terminal = function(yourterminal, yourcompany, yourcompanyproperties, loggeduser, loggeduserproperties) {
    this.yourterminal = yourterminal;
    this.yourcompany = yourcompany;
    this.yourcompanyproperties = yourcompanyproperties;
    this.loggeduser = loggeduser;
    this.loggeduserproperties = loggeduserproperties;
  };

  OB.COMP.Terminal.prototype.setModel = function(terminal) {
    this.terminal = terminal;

    this.terminal.on('change:context', function() {
      var ctx = this.terminal.get('context');
      if (ctx) {
        this.loggeduser.text(ctx.user._identifier);
        this.loggeduserproperties.empty();
        this.loggeduserproperties.append(B({
          kind: B.KindJQuery('div'),
          attr: {
            style: 'height: 60px; background-color: #FFF899;'
          },
          content: [{
            kind: B.KindJQuery('div'),
            attr: {
              style: 'float: left; width: 55px; margin: 6px 0px 0px 6px;'
            },
            content: [{
              kind: OB.UTIL.Thumbnail,
              attr: {
                img: ctx.img,
                'default': 'img/anonymous-icon.png'
              }
            }]
          }, {
            kind: B.KindJQuery('div'),
            attr: {
              style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;'
            },
            content: [{
              kind: B.KindJQuery('div'),
              content: [{
                kind: B.KindJQuery('span'),
                attr: {
                  'style': 'font-weight: 600; margin: 0px 0px 0px 5px;'
                },
                content: [
                ctx.user._identifier]
              }]
            }, {
              kind: B.KindJQuery('div'),
              content: [{
                kind: B.KindJQuery('span'),
                attr: {
                  'style': 'font-weight: 600; margin: 0px 0px 0px 5px;'
                },
                content: [
                ctx.role._identifier]
              }]
            }]
          }]
        }).$el);
        this.loggeduserproperties.append(B({
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('div'),
            attr: {
              style: 'height: 5px;'
            }
          }, {
            kind: OB.COMP.MenuAction.extend({
              clickEvent: function() {
                $('#profileDialog').modal('show');
              },
              label: OB.I18N.getLabel('OBPOS_LblProfile')
            })
          }, {
            kind: B.KindJQuery('div'),
            attr: {
              style: 'height: 5px;'
            }
          },
/*{kind: OB.COMP.MenuAction.extend({clickEvent: function() { OB.POS.lock(); }, label: OB.I18N.getLabel('OBPOS_LogoutDialogLock')})},
            {kind: B.KindJQuery('div'), attr: {style: 'height: 5px;'}},*/
          //Disabled until feature be ready
          {
            kind: OB.COMP.MenuAction.extend({
              clickEvent: function() {
                $('#logoutDialog').modal('show');
              },
              label: OB.I18N.getLabel('OBPOS_LogoutDialogLogout')
            })
          }, {
            kind: B.KindJQuery('div'),
            attr: {
              style: 'height: 5px;'
            }
          }]
        }).$el);
      } else {
        this.loggeduser.text('');
        this.loggeduserproperties.empty();
      }

    }, this);

    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function() {
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

      this.yourterminal.text(name);
      this.yourcompany.text(orgname);
      this.yourcompanyproperties.empty().append(B({
        kind: B.KindJQuery('div'),
        content: [{
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('span'),
            content: [
            OB.I18N.getLabel('OBPOS_CompanyClient')]
          }, {
            kind: B.KindJQuery('span'),
            attr: {
              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
            },
            content: [
            clientname]
          }]
        }, {
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('span'),
            content: [
            OB.I18N.getLabel('OBPOS_CompanyOrg')]
          }, {
            kind: B.KindJQuery('span'),
            attr: {
              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
            },
            content: [
            orgname]
          }]
        }, {
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('span'),
            content: [
            OB.I18N.getLabel('OBPOS_CompanyPriceList')]
          }, {
            kind: B.KindJQuery('span'),
            attr: {
              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
            },
            content: [
            pricelistname]
          }]
        }, {
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('span'),
            content: [
            OB.I18N.getLabel('OBPOS_CompanyCurrency')]
          }, {
            kind: B.KindJQuery('span'),
            attr: {
              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
            },
            content: [
            currencyname]
          }]
        }, {
          kind: B.KindJQuery('div'),
          content: [{
            kind: B.KindJQuery('span'),
            content: [
            OB.I18N.getLabel('OBPOS_CompanyLocation')]
          }, {
            kind: B.KindJQuery('span'),
            attr: {
              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
            },
            content: [
            locationname]
          }]
        }]
      }).$el);
    }, this);

  };

}());

enyo.kind({
  name: 'OB.UI.Terminal',
  tag: 'div',
  classes: 'container',
  components: [{
    tag: 'div',
    classes: 'section',
    name: 'topsection',
    components: [{
      tag: 'div',
      classes: 'row',
      style: 'height: 50px; vertical-align: middle; display: table-cell;',
      components: [{
        tag: 'div',
        classes: 'span12',
        style: 'color: white; font-size: 16px;',
        components: [{
          tag: 'div',
          style: 'display: inline-block; vertical-align: middle; margin: 3px 0px 0px 0px;',
          components: [{
            name: 'online',
            style: 'display: inline-block; margin-left: 15px;',
            components: [{
              tag: 'span',
              style: 'display: inline-block; width: 20px; color: transparent; background-image: url("./img/login-connected.png"); background-repeat: no-repeat; background-position: 2px 3px;',
              content: '.',
            }, {
              tag: 'span',
              content: 'Online'
            }]
          }, {
            tag: 'div',
            name: 'terminal',
            style: 'display: inline-block; margin-left: 50px;'
          }, {
            tag: 'div',
            classes: 'dropdown',
            style: 'display: inline-block; margin-left: 50px;',
            components: [{
              tag: 'a',
              name: 'yourcompany',
              classes: 'btn-dropdown dropdown-toggle',
              attributes: {
                href: '#',
                'data-toggle': 'dropdown'
              }
            }, {
              tag: 'div',
              classes: 'dropdown-menu',
              style: 'color: black; width: 350px;',
              components: [{
                tag: 'div',
                style: 'height: 60px; background-repeat: no-repeat; background-position: center center; background-image: url("../../utility/ShowImageLogo?logo=yourcompanymenu");'
              }, {
                tag: 'div',
                name: 'yourcompanyproperties',
                style: 'display: block; padding: 10px; float: left; background-color: #FFF899; line-height: 23px;'

              }, {
                tag: 'div',
                style: 'clear: both;'
              }]
            }]
          }, {
            tag: 'div',
            style: 'display: inline-block; margin-left: 50px;',
            classes: 'dropdown',
            components: [{
              tag: 'a',
              name: 'loggeduser',
              classes: 'btn-dropdown dropdown-toggle',
              attributes: {
                'data-toggle': 'dropdown',
                href: '#'
              }
            }, {
              tag: 'div',
              name: 'loggeduserproperties',
              classes: 'dropdown-menu',
              style: 'color: black; padding: 0px; width: 350px;'
            }]
          }]

        }, {
          tag: 'div',
          style: 'display: inline-block; float: right;',
          components: [{
            tag: 'div',
            style: 'display: inline-block; float: left; margin: 4px 10px 0px 0px;',
            content: 'Openbravo Web POS'
          }, {
            tag: 'div',
            style: 'width: 30px; height: 30px; float: right; margin: 0px 12px 0px 0px;',
            components: [{
              tag: 'div',
              classes: 'top-right-logo'
            }]
          }, {
            tag: 'div',
            name: 'dialogsContainer'
          }]
        }]
      }]
    }, {
      tag: 'div',
      components: [{
        tag: 'div',
        name: 'containerLoading',
        components: [{
          tag: 'div',
          classes: 'POSLoadingCenteredBox',
          components: [{
            tag: 'div',
            classes: 'POSLoadingPromptLabel',
            content: 'Loading...'
          }, {
            tag: 'div',
            classes: 'POSLoadingProgressBar',
            components: [{
              tag: 'div',
              classes: 'POSLoadingProgressBarImg'
            }]
          }]
        }]
      }, {
        tag: 'div',
        makeId: function() {
          return 'containerWindow'
        },
        name: 'containerWindow'
      }]
    }]
  }],

  initComponents: function() {
    //this.terminal = terminal;
    this.inherited(arguments);
    debugger;

    this.terminal.on('change:context', function() {
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
        //        this.loggeduser.text('');
        //        this.loggeduserproperties.empty();
      }

    }, this);

    //    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function() {
    //      var name = '';
    //      var clientname = '';
    //      var orgname = '';
    //      var pricelistname = '';
    //      var currencyname = '';
    //      var locationname = '';
    //
    //      if (this.terminal.get('terminal')) {
    //        name = this.terminal.get('terminal')._identifier;
    //        clientname = this.terminal.get('terminal')['client' + OB.Constants.FIELDSEPARATOR + '_identifier'];
    //        orgname = this.terminal.get('terminal')['organization' + OB.Constants.FIELDSEPARATOR + '_identifier'];
    //      }
    //      if (this.terminal.get('pricelist')) {
    //        pricelistname = this.terminal.get('pricelist')._identifier;
    //        currencyname = this.terminal.get('pricelist')['currency' + OB.Constants.FIELDSEPARATOR + '_identifier'];
    //      }
    //      if (this.terminal.get('location')) {
    //        locationname = this.terminal.get('location')._identifier;
    //      }
    //
    //      this.yourterminal.text(name);
    //      this.yourcompany.text(orgname);
    //      this.yourcompanyproperties.empty().append(B({
    //        kind: B.KindJQuery('div'),
    //        content: [{
    //          kind: B.KindJQuery('div'),
    //          content: [{
    //            kind: B.KindJQuery('span'),
    //            content: [
    //            OB.I18N.getLabel('OBPOS_CompanyClient')]
    //          }, {
    //            kind: B.KindJQuery('span'),
    //            attr: {
    //              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
    //            },
    //            content: [
    //            clientname]
    //          }]
    //        }, {
    //          kind: B.KindJQuery('div'),
    //          content: [{
    //            kind: B.KindJQuery('span'),
    //            content: [
    //            OB.I18N.getLabel('OBPOS_CompanyOrg')]
    //          }, {
    //            kind: B.KindJQuery('span'),
    //            attr: {
    //              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
    //            },
    //            content: [
    //            orgname]
    //          }]
    //        }, {
    //          kind: B.KindJQuery('div'),
    //          content: [{
    //            kind: B.KindJQuery('span'),
    //            content: [
    //            OB.I18N.getLabel('OBPOS_CompanyPriceList')]
    //          }, {
    //            kind: B.KindJQuery('span'),
    //            attr: {
    //              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
    //            },
    //            content: [
    //            pricelistname]
    //          }]
    //        }, {
    //          kind: B.KindJQuery('div'),
    //          content: [{
    //            kind: B.KindJQuery('span'),
    //            content: [
    //            OB.I18N.getLabel('OBPOS_CompanyCurrency')]
    //          }, {
    //            kind: B.KindJQuery('span'),
    //            attr: {
    //              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
    //            },
    //            content: [
    //            currencyname]
    //          }]
    //        }, {
    //          kind: B.KindJQuery('div'),
    //          content: [{
    //            kind: B.KindJQuery('span'),
    //            content: [
    //            OB.I18N.getLabel('OBPOS_CompanyLocation')]
    //          }, {
    //            kind: B.KindJQuery('span'),
    //            attr: {
    //              'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'
    //            },
    //            content: [
    //            locationname]
    //          }]
    //        }]
    //      }).$el);
    //    }, this);
  }

});



enyo.kind({
  name: 'OB.UI.Terminal.UserWidget',
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
      tap: function() {
        console.log('tap');
        $('#profileDialog').modal('show'); //TODO: add profileDialog
      }
    }, {
      style: 'height: 5px;'
    }, {
      kind: 'OB.UI.MenuAction',
      label: 'Log Out',
      //TODO: OB.I18N.getLabel('OBPOS_LblProfile'),
      tap: function() {
        console.log('tap');
        $('#logoutDialog').modal('show'); //TODO: add profileDialog
      }
    }, {
      style: 'height: 5px;'
    }]
  }],
  initComponents: function() {
    this.inherited(arguments);
    debugger;
    this.$.username.setContent(this.username);
    this.$.role.setContent(this.role);
  }
});