/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $LAB, _, $, enyo, Backbone, CryptoJS, console */

OB = window.OB || {};
OB.Model = window.OB.Model || {};

OB.Model.Collection = Backbone.Collection.extend({
  constructor: function (data) {
    this.ds = data.ds;
    Backbone.Collection.prototype.constructor.call(this);
  },
  inithandler: function (init) {
    if (init) {
      init.call(this);
    }
  },
  exec: function (filter) {
    var me = this;
    if (this.ds) {
      this.ds.exec(filter, function (data, info) {
        var i;
        me.reset();
        me.trigger('info', info);
        if (data.exception) {
          OB.UTIL.showError(data.exception.message);
        } else {
          for (i in data) {
            if (data.hasOwnProperty(i)) {
              me.add(data[i]);
            }
          }
        }
      });
    }
  }
});

// Terminal model.
OB.Model.Terminal = Backbone.Model.extend({

  defaults: {
    terminal: null,
    context: null,
    permissions: null,
    businesspartner: null,
    location: null,
    pricelist: null,
    pricelistversion: null,
    currency: null,
    connectedToERP: null
  },

  initialize: function () {
    var me = this;

    this.router = new OB.Router();

    if (!Backbone.History.started) {
      Backbone.history.start();
    }
    this.router.terminal = this;

    this.router.route('login', 'login', this.renderLogin);
    this.router.route('main', 'main', this.renderMain);
  },

  renderLogin: function () {
    //      var loginWindow = new OB.OBPOSLogin.UI.Login({});
    //      loginWindow.renderInto(enyo.dom.byId('containerWindow'));
    //      loginWindow.postRenderActions();
    OB.POS.terminal.$.containerWindow.destroyComponents();
    OB.POS.terminal.$.containerWindow.createComponent({
      kind: OB.OBPOSLogin.UI.Login
    }).render();
  },


  renderMain: function () {
    if (!OB.UTIL.isSupportedBrowser()) {
      OB.POS.modelterminal.renderLogin();
      return false;
    }
    var me = OB.POS.modelterminal,
        params = {
        terminal: OB.POS.paramTerminal
        };

    OB.POS.modelterminal.loggingIn = true;

    OB.POS.modelterminal.off('terminal.loaded'); // Unregister previous events.
    OB.POS.modelterminal.on('terminal.loaded', function () {
      var oldOB = OB;

      $LAB.setGlobalDefaults({
        AppendTo: 'body'
      });
      if (!OB.POS.modelterminal.get('connectedToERP')) {
        OB.Format = JSON.parse(me.usermodel.get('formatInfo'));
        OB.POS.cleanWindows();
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=FinancialMgmtTaxRate&modelName=TaxRate&source=org.openbravo.retail.posterminal.master.TaxRate');

        //Models for discounts and promotions
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustment&modelName=Discount&source=org.openbravo.retail.posterminal.master.Discount');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartner&modelName=DiscountFilterBusinessPartner&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartnerGroup&modelName=DiscountFilterBusinessPartnerGroup&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProduct&modelName=DiscountFilterProduct&source=org.openbravo.retail.posterminal.master.DiscountFilterProduct');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProductCategory&modelName=DiscountFilterProductCategory&source=org.openbravo.retail.posterminal.master.DiscountFilterProductCategory');

        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS');
        return;
      }
      $LAB.script('../../org.openbravo.client.kernel/OBCLKER_Kernel/Application').wait(function () {
        var newFormat = OB.Format;
        _.extend(OB, oldOB);
        OB.Format = newFormat;
        OB.POS.cleanWindows();

        me.usermodel.set('formatInfo', JSON.stringify(OB.Format));
        OB.Dal.save(me.usermodel, function () {}, function () {
          window.console.error(arguments);
        });


        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=FinancialMgmtTaxRate&modelName=TaxRate&source=org.openbravo.retail.posterminal.master.TaxRate');

        //Models for discounts and promotions
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustment&modelName=Discount&source=org.openbravo.retail.posterminal.master.Discount');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartner&modelName=DiscountFilterBusinessPartner&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartnerGroup&modelName=DiscountFilterBusinessPartnerGroup&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProduct&modelName=DiscountFilterProduct&source=org.openbravo.retail.posterminal.master.DiscountFilterProduct');
        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProductCategory&modelName=DiscountFilterProductCategory&source=org.openbravo.retail.posterminal.master.DiscountFilterProductCategory');

        $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS');
      });
    });
    if (OB.POS.modelterminal.get('connectedToERP')) {
      new OB.DS.Request('org.openbravo.retail.posterminal.term.Labels').exec({
        languageId: window.localStorage.getItem('POSlanguageId')
      }, function (data) {
        OB.I18N.labels = data;

        new OB.DS.Request('org.openbravo.retail.posterminal.term.Terminal').exec(params, function (data) {
          if (data.exception) {
            OB.POS.navigate('login');
            if (OB.I18N.hasLabel(data.exception.message)) {
              OB.UTIL.showError(OB.I18N.getLabel(data.exception.message));
            } else {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorLoadingTerminal'));
            }
          } else if (data[0]) {
            me.set('terminal', data[0]);
            if (!me.usermodel) {
              OB.POS.modelterminal.setUserModelOnline(true);
            } else {
              me.trigger('terminal.loaded');
            }
          } else {
            OB.UTIL.showError("Terminal does not exists: " + params.terminal);
          }
        });
      });
    } else {
      //Offline mode, we get the terminal information from the local db
      me.set('terminal', JSON.parse(me.usermodel.get('terminalinfo')).terminal);
      me.trigger('terminal.loaded');
    }


  },


  loadModels: function (windowv, incremental) {
    if (!OB.POS.modelterminal.get('connectedToERP')) {
      return;
    }
    var windows, i, windowName, windowClass, datasources;
    var timestamp = 0;
    if (windowv) {
      windows = [windowv];
    } else {
      windows = OB.POS.windowObjs;
    }
    if (incremental && window.localStorage.getItem('lastUpdatedTimestamp')) {
      timestamp = window.localStorage.getItem('lastUpdatedTimestamp');
    }
    for (i = 0; i < windows.length; i++) {
      windowClass = windows[i].windowClass;
      windowName = windows[i].route;
      if (OB.DATA[windowName]) {
        // old way of defining datasources...
        datasources = OB.DATA[windowName];
      } else if (windowClass.prototype && windowClass.prototype.windowmodel && windowClass.prototype.windowmodel.prototype && windowClass.prototype.windowmodel.prototype.models) {
        datasources = windowClass.prototype.windowmodel.prototype.models;
      }

      _.extend(datasources, Backbone.Events);

      OB.Model.Util.loadModels(false, datasources, null, timestamp);
    }
  },


  registerWindow: function (windowp) {
    var datasources = [],
        windowClass, windowName = windowp.route,
        minTotalRefresh, minIncRefresh, lastTotalRefresh, lastIncRefresh, intervalTotal, intervalInc, now;
    OB.POS.windows.add(windowp);
    if (!OB.POS.windowObjs) {
      OB.POS.windowObjs = [];
    }
    OB.POS.windowObjs.push(windowp);


    minTotalRefresh = OB.POS.modelterminal.get('terminal').terminalType.minutestorefreshdatatotal * 60 * 1000;
    minIncRefresh = OB.POS.modelterminal.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
    lastTotalRefresh = window.localStorage.getItem('POSLastTotalRefresh');
    lastIncRefresh = window.localStorage.getItem('POSLastIncRefresh');
    if ((!minTotalRefresh && !minIncRefresh) || (!lastTotalRefresh && !lastIncRefresh)) {
      // If no configuration of the masterdata loading has been done, 
      // or an initial load has not been done, then always do 
      // a total refresh during the login
      this.loadModels(windowp, false);
    } else {
      now = new Date().getTime();
      intervalTotal = lastTotalRefresh ? (now - lastTotalRefresh - minTotalRefresh) : 0;
      intervalInc = lastIncRefresh ? (now - lastIncRefresh - minIncRefresh) : 0;
      if (intervalTotal > 0) {
        //It's time to do a full refresh
        this.loadModels(windowp, false);
      } else if (intervalInc > 0) {
        //It's time to do a partial refresh
        this.loadModels(windowp, true);
      } else {
        //A partial refresh is done just in case
        this.loadModels(windowp, true);
      }
    }

    this.router.route(windowName, windowName, function () {
      this.renderGenericWindow(windowName);
    });




    //TODO: load OB.DATA??? It should be done only if needed...
  },

  isWindowOnline: function (route) {
    var i, windows;
    windows = OB.POS.windows.toArray();
    for (i = 0; i < windows.length; i++) {
      if (windows[i].get('route') === route) {
        return windows[i].get('online');
      }
    }
    return false;
  },

  renderGenericWindow: function (windowName) {
    OB.UTIL.showLoading(true);
    var terminal = OB.POS.modelterminal.get('terminal'),
        windowClass;

    this.on('window:ready', function (w) {
      OB.POS.terminal.$.containerWindow.render();
      OB.UTIL.showLoading(false);
      //enyo.dispatcher.capture(OB.POS.terminal, false);
    }, this);

    windowClass = OB.POS.windows.where({
      route: windowName
    })[0].get('windowClass');

    OB.POS.terminal.$.containerWindow.destroyComponents();
    OB.POS.terminal.$.containerWindow.createComponent({
      kind: windowClass
    });
  },

  updateSession: function (user) {
    OB.Dal.find(OB.Model.Session, {
      'user': user.get('id')
    }, function (sessions) {
      var session;
      if (sessions.models.length === 0) {
        session = new OB.Model.Session();
        session.set('user', user.get('id'));
        session.set('terminal', OB.POS.paramTerminal);
        session.set('active', 'Y');
        OB.Dal.save(session, function () {}, function () {
          window.console.error(arguments);
        });
      } else {
        session = sessions.models[0];
        session.set('active', 'Y');
        OB.Dal.save(session, function () {}, function () {
          window.console.error(arguments);
        });
      }
      OB.POS.modelterminal.set('session', session.get('id'));
    }, function () {
      window.console.error(arguments);
    });
  },

  closeSession: function () {
    var sessionId = OB.POS.modelterminal.get('session');
    OB.Dal.get(OB.Model.Session, sessionId, function (session) {
      session.set('active', 'N');
      OB.Dal.save(session, function () {
        //All pending to be paid orders will be removed on logout
        OB.Dal.find(OB.Model.Order, {
          'session': session.get('id'),
          'hasbeenpaid': 'N'
        }, function (orders) {
          var i, j, order, orderlines, orderline, errorFunc = function () {
              window.console.error(arguments);
              };
          var triggerLogoutFunc = function () {
              OB.POS.modelterminal.triggerLogout();
              };
          if (orders.models.length === 0) {
            //If there are no orders to remove, a logout is triggered
            OB.POS.modelterminal.triggerLogout();
          }
          for (i = 0; i < orders.models.length; i++) {
            order = orders.models[i];
            OB.Dal.removeAll(OB.Model.Order, {
              'order': order.get('id')
            }, null, errorFunc);
            //Logout will only be triggered after last order
            OB.Dal.remove(order, i < orders.models.length - 1 ? null : triggerLogoutFunc, errorFunc);
          }
        }, function () {
          window.console.error(arguments);
        });
      }, function () {
        window.console.error(arguments);
      });
    }, function () {
      window.console.error(arguments);
    });
  },

  generate_sha1: function (theString) {
    return CryptoJS.enc.Hex.stringify(CryptoJS.SHA1(theString));
  },

  login: function (user, password, mode) {
    OB.UTIL.showLoading(true);
    var me = this;
    me.user = user;
    me.password = password;
    this.set('terminal', null);
    this.set('payments', null);
    this.set('paymentcash', null);
    this.set('context', null);
    this.set('permissions', null);
    this.set('businesspartner', null);
    this.set('location', null);
    this.set('pricelist', null);
    this.set('pricelistversion', null);
    this.set('currency', null);
    this.set('currencyPrecision', null);

    // Remove the pending orders that have not been paid
    //if (OB.Dal) { //TODO: check this...
    //  OB.Dal.removeAll(OB.Model.Order, {
    //    'hasbeenpaid': 'N'
    //  }, null, null);
    //}
    if (OB.POS.modelterminal.get('connectedToERP')) {

      var ajaxRequest = new enyo.Ajax({
        url: '../../org.openbravo.retail.posterminal/POSLoginHandler',
        cacheBust: false,
        method: 'POST',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: {
          'user': user,
          'password': password,
          'terminal': OB.POS.paramTerminal,
          'Command': 'DEFAULT',
          'IsAjaxCall': 1
        },
        success: function (inSender, inResponse) {
          var pos, baseUrl;
          if (inResponse && inResponse.showMessage) {
            me.triggerLoginFail(401, mode, inResponse);
            return;
          }
          //          pos = location.pathname.indexOf('login.jsp');
          //          baseUrl = window.location.pathname.substring(0, pos);
          //          window.location = baseUrl + OB.POS.hrefWindow(OB.POS.paramWindow);
          OB.POS.modelterminal.set('orgUserId', inResponse.userId);
          me.setUserModelOnline();

          OB.POS.navigate('main', {
            trigger: true
          });
        },
        fail: function (inSender, inResponse) {
          me.triggerLoginFail(inResponse, mode);
        }
      });
      ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
    } else {
      OB.POS.modelterminal.set('windowRegistered', undefined);
      OB.Dal.find(OB.Model.User, {
        'name': me.user
      }, function (users) {
        var user;
        if (users.models.length === 0) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineUserNotRegistered'));
        } else {
          if (users.models[0].get('password') === me.generate_sha1(me.password + users.models[0].get('created'))) {
            me.usermodel = users.models[0];
            me.set('orgUserId', users.models[0].id);
            me.updateSession(me.usermodel);
            OB.POS.navigate('main', {
              trigger: true
            });
          } else {
            alert(OB.I18N.getLabel('OBPOS_OfflinePasswordNotCorrect'));
            window.location.reload();
          }
        }
      }, function () {});
    }
  },

  setUserModelOnline: function (triggerTerminalLoaded) {
    var me = this;
    var trigger = triggerTerminalLoaded;
    OB.Dal.initCache(OB.Model.User, [], null, null);
    OB.Dal.initCache(OB.Model.Session, [], null, null);
    OB.Dal.find(OB.Model.User, {
      'name': me.user
    }, function (users) {
      var user, session, date, savedPass;
      if (users.models.length === 0) {
        date = new Date().toString();
        user = new OB.Model.User();
        user.set('name', me.user);
        user.set('id', OB.POS.modelterminal.get('orgUserId'));
        savedPass = me.generate_sha1(me.password + date);
        user.set('password', savedPass);
        user.set('created', date);
        OB.Dal.save(user, function () {}, function () {
          window.console.error(arguments);
        }, true);
        me.usermodel = user;
      } else {
        user = users.models[0];
        me.usermodel = user;
        if (me.password) {
          //The password will only be recomputed in case it was properly entered
          //(that is, if the call comes from the login page directly)
          savedPass = me.generate_sha1(me.password + user.get('created'));
          user.set('password', savedPass);
          user.set('created', date);
          OB.Dal.save(user, function () {}, function () {
            window.console.error(arguments);
          });
          me.usermodel = user;
        } else {
          user = users.models[0];
          me.usermodel = user;
          if (me.password) {
            //The password will only be recomputed in case it was properly entered
            //(that is, if the call comes from the login page directly)
            savedPass = me.generate_sha1(me.password + user.get('created'));
            user.set('password', savedPass);
          }
          OB.Dal.save(user, function () {}, function () {
            window.console.error(arguments);
          });
        }
        OB.Dal.save(user, function () {}, function () {
          window.console.error(arguments);
        });
      }
      me.updateSession(user);
      if (trigger) {
        me.trigger('terminal.loaded');
      }
    }, function () {
      window.console.error(arguments);
    });
  },

  logout: function () {
    var me = this;
    this.set('terminal', null);
    this.set('payments', null);
    this.set('paymentcash', null);
    this.set('context', null);
    this.set('permissions', null);
    this.set('bplocation', null);
    this.set('location', null);
    this.set('pricelist', null);
    this.set('pricelistversion', null);
    this.set('currency', null);
    this.set('currencyPrecision', null);

    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.retail.posterminal.service.logout',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        me.closeSession();
      },
      fail: function (inSender, inResponse) {
        me.closeSession();
      }
    });
    ajaxRequest.go().response('success').error('fail');
  },

  lock: function () {
    var me = this;
    this.set('terminal', null);
    this.set('payments', null);
    this.set('paymentcash', null);
    this.set('context', null);
    this.set('permissions', null);
    this.set('bplocation', null);
    this.set('location', null);
    this.set('pricelist', null);
    this.set('pricelistversion', null);
    this.set('currency', null);
    this.set('currencyPrecision', null);

    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.retail.posterminal.service.logout',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        me.triggerLogout();
      },
      fail: function (inSender, inResponse) {
        me.triggerLogout();
      }
    });
    ajaxRequest.go().response('success').error('fail');
  },

  load: function () {
    var termInfo, i, max;
    if (!OB.POS.modelterminal.get('connectedToERP')) {
      termInfo = JSON.parse(this.usermodel.get('terminalinfo'));
      this.set('payments', termInfo.payments);
      this.paymentnames = {};
      for (i = 0, max = termInfo.payments.length; i < max; i++) {
        this.paymentnames[termInfo.payments[i].payment.searchKey] = termInfo.payments[i];
      }
      this.set('paymentcash', termInfo.paymentcash);
      this.set('context', termInfo.context);
      this.set('permissions', termInfo.permissions);
      this.set('businesspartner', termInfo.businesspartner);
      this.set('location', termInfo.location);
      this.set('pricelist', termInfo.pricelist);
      this.set('pricelistversion', termInfo.pricelistversion);
      this.set('warehouses', termInfo.warehouses);
      this.set('writableOrganizations', termInfo.writableOrganizations);
      this.set('currency', termInfo.currency);
      this.set('currencyPrecision', termInfo.currencyPrecision);
      this.set('orgUserId', termInfo.orgUserId);
      this.set('loggedOffline', true);
      this.setDocumentSequence();
      this.triggerReady();
      return;
    }

    // reset all application state.
    //this.set('terminal', null);
    this.set('payments', null);
    this.set('paymentcash', null);
    this.set('context', null);
    this.set('permissions', null);
    this.set('businesspartner', null);
    this.set('location', null);
    this.set('pricelist', null);
    this.set('pricelistversion', null);
    this.set('currency', null);
    this.set('currencyPrecision', null);
    this.set('loggedOffline', false);

    // Starting app
    var me = this;
    var params = {
      terminal: OB.POS.paramTerminal
    };

    me.loadPayments();
    me.loadContext();
    me.loadPermissions();
    me.loadBP();
    me.loadLocation();
    me.loadPriceList();
    me.loadWarehouses();
    me.loadWritableOrganizations();
    me.loadPriceListVersion();
    me.loadCurrency();
    me.setDocumentSequence();
  },

  loadPayments: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.Payments').exec({
      pos: this.get('terminal').id
    }, function (data) {
      if (data) {
        var i, max, paymentlegacy, paymentcash, paymentcashcurrency;
        me.set('payments', data);
        me.paymentnames = {};
        for (i = 0, max = data.length; i < max; i++) {
          me.paymentnames[data[i].payment.searchKey] = data[i];
          if (data[i].payment.searchKey === 'OBPOS_payment.cash') {
            paymentlegacy = data[i].payment.searchKey;
          }
          if (data[i].paymentMethod.iscash) {
            paymentcash = data[i].payment.searchKey;
          }
          if (data[i].paymentMethod.iscash && data[i].paymentMethod.currency === me.get('terminal').currency) {
            paymentcashcurrency = data[i].payment.searchKey;
          }
        }
        // sets the default payment method
        me.set('paymentcash', paymentcashcurrency || paymentcash || paymentlegacy);
        me.triggerReady();
      }
    });
  },

  loadContext: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.Context').exec({}, function (data) {
      if (data[0]) {
        me.set('context', data[0]);
        me.triggerReady();
      }
    });
  },

  loadPermissions: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.RolePreferences').exec({}, function (data) {
      var i, max, separator, permissions = {};
      if (data) {
        for (i = 0, max = data.length; i < max; i++) {
          permissions[data[i].key] = data[i].value; // Add the permission value
          separator = data[i].key.indexOf('_');
          if (separator >= 0) {
            permissions[data[i].key.substring(separator + 1)] = data[i].value; // if key has a DB prefix, add also the permission value without this prefix
          }
        }
        me.set('permissions', permissions);
        me.triggerReady();
      }
    });
  },

  loadBP: function () {
    this.set('businesspartner', this.get('terminal').businessPartner);
  },

  loadLocation: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.Location').exec({
      org: this.get('terminal').organization
    }, function (data) {
      if (data[0]) {
        me.set('location', data[0]);
      }
    });
  },

  loadPriceList: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceList').exec({
      pricelist: this.get('terminal').priceList
    }, function (data) {
      if (data[0]) {
        me.set('pricelist', data[0]);
      }
    });
  },

  loadWarehouses: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.Warehouses').exec({
      organization: this.get('terminal').organization
    }, function (data) {
      if (data && data.exception) {
        //MP17
        me.set('warehouses', []);
        me.triggerReady();
      } else {
        me.set('warehouses', data);
        me.triggerReady();
      }
    });
  },

  loadWritableOrganizations: function () {
    var me = this;
    new OB.DS.Process('org.openbravo.retail.posterminal.term.WritableOrganizations').exec({

    }, function (data) {
      if (data.length > 0) {
        me.set('writableOrganizations', data);
        me.triggerReady();
      }
    });
  },

  loadPriceListVersion: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceListVersion').exec({
      pricelist: this.get('terminal').priceList
    }, function (data) {
      if (data[0]) {
        me.set('pricelistversion', data[0]);
        me.triggerReady();
      }
    });
  },

  loadCurrency: function () {
    var me = this;
    new OB.DS.Request('org.openbravo.retail.posterminal.term.Currency').exec({
      currency: this.get('terminal').currency
    }, function (data) {
      if (data[0]) {
        me.set('currency', data[0]);
        //Precision used by arithmetics operations is set using the currency
        OB.DEC.scale = data[0].pricePrecision;
        me.triggerReady();
      }
    });
  },

  setDocumentSequence: function () {
    var me = this;
    // Obtains the persisted document number (documentno of the last processed order)
    OB.Dal.find(OB.Model.DocumentSequence, {
      'posSearchKey': OB.POS.modelterminal.get('terminal').searchKey
    }, function (documentsequence) {
      var lastInternalDocumentSequence, lastInternalQuotationSequence, max, maxquote;
      if (documentsequence && documentsequence.length > 0) {
        lastInternalDocumentSequence = documentsequence.at(0).get('documentSequence');
        lastInternalQuotationSequence = documentsequence.at(0).get('quotationDocumentSequence');
        // Compares the persisted document number with the fetched from the server
        if (lastInternalDocumentSequence > OB.POS.modelterminal.get('terminal').lastDocumentNumber) {
          max = lastInternalDocumentSequence;
        } else {
          max = OB.POS.modelterminal.get('terminal').lastDocumentNumber;
        }
        if (lastInternalQuotationSequence > OB.POS.modelterminal.get('terminal').lastQuotationDocumentNumber) {
          maxquote = lastInternalQuotationSequence;
        } else {
          maxquote = OB.POS.modelterminal.get('terminal').lastQuotationDocumentNumber;
        }
        // Compares the maximum with the document number of the paid pending orders
        me.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
      } else {
        max = OB.POS.modelterminal.get('terminal').lastDocumentNumber;
        maxquote = OB.POS.modelterminal.get('terminal').lastQuotationDocumentNumber;
        // Compares the maximum with the document number of the paid pending orders
        me.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
      }

    }, function () {
      var max = OB.POS.modelterminal.get('terminal').lastDocumentNumber,
          maxquote = OB.POS.modelterminal.get('terminal').lastQuotationDocumentNumber;
      // Compares the maximum with the document number of the paid pending orders
      me.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
    });
  },

  compareDocSeqWithPendingOrdersAndSave: function (maxDocumentSequence, maxQuotationDocumentSequence) {
    var me = this,
        orderDocNo, quotationDocNo;
    // compare the last document number returned from the ERP with
    // the last document number of the unprocessed pending lines (if any)
    OB.Dal.find(OB.Model.Order, {}, function (fetchedOrderList) {
      var criteria, maxDocumentSequencePendingOrders;
      if (!fetchedOrderList || fetchedOrderList.length === 0) {
        // There are no pending orders, the initial document sequence
        // will be the one fetched from the database
        me.saveDocumentSequenceAndGo(maxDocumentSequence, maxQuotationDocumentSequence);
      } else {
        // There are pending orders. The document sequence will be set
        // to the maximum of the pending order document sequence and the
        // document sequence retrieved from the server
        maxDocumentSequencePendingOrders = me.getMaxDocumentSequenceFromPendingOrders(fetchedOrderList.models);
        if (maxDocumentSequencePendingOrders.orderDocNo > maxDocumentSequence) {
          orderDocNo = maxDocumentSequencePendingOrders.orderDocNo;
        } else {
          orderDocNo = maxDocumentSequence;
        }
        if (maxDocumentSequencePendingOrders.quotationDocNo > maxQuotationDocumentSequence) {
          quotationDocNo = maxDocumentSequencePendingOrders.quotationDocNo;
        } else {
          quotationDocNo = maxQuotationDocumentSequence;
        }
        me.saveDocumentSequenceAndGo(orderDocNo, quotationDocNo);
      }
    }, function () {
      // If c_order does not exist yet, go with the sequence
      // number fetched from the server
      me.saveDocumentSequenceAndGo(maxDocumentSequence, maxQuotationDocumentSequence);
    });
  },

  getMaxDocumentSequenceFromPendingOrders: function (pendingOrders) {
    var nPreviousOrders = pendingOrders.length,
        maxDocumentSequence = OB.POS.modelterminal.get('terminal').lastDocumentNumber,
        posDocumentNoPrefix = OB.POS.modelterminal.get('terminal').docNoPrefix,
        maxQuotationDocumentSequence = OB.POS.modelterminal.get('terminal').lastQuotationDocumentNumber,
        posQuotationDocumentNoPrefix = OB.POS.modelterminal.get('terminal').quotationDocNoPrefix,
        orderCompleteDocumentNo, orderDocumentSequence, i;
    for (i = 0; i < nPreviousOrders; i++) {
      orderCompleteDocumentNo = pendingOrders[i].get('documentNo');
      if (!pendingOrders[i].get('isQuotation')) {
        orderDocumentSequence = parseInt(orderCompleteDocumentNo.substr(posDocumentNoPrefix.length + 1), 10);
        if (orderDocumentSequence > maxDocumentSequence) {
          maxDocumentSequence = orderDocumentSequence;
        }
      } else {
        orderDocumentSequence = parseInt(orderCompleteDocumentNo.substr(posQuotationDocumentNoPrefix.length + 1), 10);
        if (orderDocumentSequence > maxQuotationDocumentSequence) {
          maxQuotationDocumentSequence = orderDocumentSequence;
        }
      }
    }
    return {
      orderDocNo: maxDocumentSequence,
      quotationDocNo: maxQuotationDocumentSequence
    };
  },

  saveDocumentSequenceAndGo: function (documentSequence, quotationDocumentSequence) {
    this.set('documentsequence', documentSequence);
    this.set('quotationDocumentSequence', quotationDocumentSequence);
    this.triggerReady();
  },

  saveDocumentSequenceInDB: function () {
    var me = this,
        modelterminal = OB.POS.modelterminal,
        documentSequence = modelterminal.get('documentsequence'),
        quotationDocumentSequence = modelterminal.get('quotationDocumentSequence'),
        criteria = {
        'posSearchKey': OB.POS.modelterminal.get('terminal').searchKey
        };
    OB.Dal.find(OB.Model.DocumentSequence, criteria, function (documentSequenceList) {
      var docSeq;
      if (documentSequenceList && documentSequenceList.length !== 0) {
        // There can only be one documentSequence model in the list (posSearchKey is unique)
        docSeq = documentSequenceList.models[0];
        // There exists already a document sequence, update it
        docSeq.set('documentSequence', documentSequence);
        docSeq.set('quotationDocumentSequence', quotationDocumentSequence);
      } else {
        // There is not a document sequence for the pos, create it
        docSeq = new OB.Model.DocumentSequence();
        docSeq.set('posSearchKey', OB.POS.modelterminal.get('terminal').searchKey);
        docSeq.set('documentSequence', documentSequence);
        docSeq.set('quotationDocumentSequence', quotationDocumentSequence);
      }
      OB.Dal.save(docSeq, null, function () {
        console.error(arguments);
      });
    });
  },

  triggerReady: function () {
    var undef, loadModelsIncFunc, loadModelsTotalFunc, minTotalRefresh, minIncRefresh;
    if (this.get('payments') && this.get('pricelistversion') && this.get('warehouses') && this.get('currency') && this.get('context') && this.get('writableOrganizations') && this.get('permissions') && (this.get('documentsequence') !== undef || this.get('documentsequence') === 0) && this.get('windowRegistered') !== undef) {
      OB.POS.modelterminal.loggingIn = false;
      if (OB.POS.modelterminal.get('connectedToERP')) {
        //In online mode, we save the terminal information in the local db
        this.usermodel.set('terminalinfo', JSON.stringify(this));
        OB.Dal.save(this.usermodel, function () {}, function () {
          window.console.error(arguments);
        });
      }
      minIncRefresh = OB.POS.modelterminal.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
      if (minIncRefresh) {
        loadModelsIncFunc = function () {
          console.log('Performing incremental masterdata refresh');
          OB.POS.modelterminal.loadModels(null, true);
          setTimeout(loadModelsIncFunc, minIncRefresh);
        };
        setTimeout(loadModelsIncFunc, minIncRefresh);
      }
      this.trigger('ready');
    }
  },

  triggerLogout: function () {
    this.trigger('logout');
  },

  triggerLoginSuccess: function () {
    this.trigger('loginsuccess');
  },

  triggerOnLine: function () {
    if (!OB.POS.modelterminal.loggingIn) {
      this.set('connectedToERP', true);
    }
  },

  triggerOffLine: function () {
    if (!OB.POS.modelterminal.loggingIn) {
      this.set('connectedToERP', false);
    }
  },

  triggerLoginFail: function (e, mode, data) {
    OB.UTIL.showLoading(false);
    if (mode === 'userImgPress') {
      this.trigger('loginUserImgPressfail', e);
    } else {
      this.trigger('loginfail', e, data);
    }
  },

  hasPermission: function (p) {
    return !this.get('context').role.manual || this.get('permissions')[p];
  },

  getPaymentName: function (key) {
    return this.paymentnames[key].payment._identifier;
  },

  hasPayment: function (key) {
    return this.paymentnames[key];
  }

});