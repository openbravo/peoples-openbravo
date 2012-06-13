/*global define, $, Backbone */

define(['datasource', 'utilities', 'utilitiesui'], function () {

  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};

  OB.MODEL.Collection = Backbone.Collection.extend({
    constructor: function (data) {
      this.ds = data.ds;
      Backbone.Collection.prototype.constructor.call(this);
    },
    inithandler : function (init) {
       if (init) {
         init.call(this);
       }
    },
    exec : function (filter) {
      var me = this;
      this.ds.exec(filter, function (data, info) {
        var i;
        me.reset();
        me.trigger('info', info);
        if (data.exception) {
          OB.UTIL.showError(data.exception.message);
        } else {
          for (i in data) {
            if(data.hasOwnProperty(i)) {
              me.add(data[i]);
            }
          }
        }
      });
    }
  });

  // Terminal model.

  OB.MODEL.Terminal = Backbone.Model.extend({

    defaults : {
      terminal: null,
      context: null,
      permissions: null,
      bplocation: null,
      location: null,
      pricelist: null,
      pricelistversion: null
    },

    login: function (user, password, mode) {
      var me = this;
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('bplocation', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);
      $.ajax({
        url: '../../secureApp/LoginHandler.html',
        data: {'user': user, 'password': password, 'Command': 'DEFAULT', 'IsAjaxCall': 1},
        type: 'POST',
        success: function (data, textStatus, jqXHR) {
          var pos, baseUrl;
          if(data && data.showMessage) {
            me.triggerLoginFail(401, mode);
            return;
          }
          pos = location.pathname.indexOf('login.jsp');
          baseUrl = window.location.pathname.substring(0, pos);
          window.location = baseUrl + OB.POS.hrefWindow(OB.POS.paramWindow);
        },
        error: function (jqXHR, textStatus, errorThrown) {
          me.triggerLoginFail(jqXHR.status, mode);
        }
      });
    },

    logout: function () {
      var me = this;
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('bplocation', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);

      $.ajax({
        url: '../../org.openbravo.service.retail.posterminal.logout',
        contentType: 'application/json;charset=utf-8',
        dataType: 'json',
        type: 'GET',
        success: function (data, textStatus, jqXHR) {
          me.triggerLogout();
        },
        error: function (jqXHR, textStatus, errorThrown) {
          me.triggerLogout();
        }
      });
    },

    lock: function () {
      alert('Feature not yet implemented');
    },

    load: function () {

      // reset all application state.
      $(window).off('keypress');
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('bplocation', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);

      // Starting app
      var me = this;
      var params = {
          terminal: OB.POS.paramTerminal
      };

      new OB.DS.Query('org.openbravo.retail.posterminal.term.Terminal').exec(
        params,
        function (data) {
          if (data.exception) {
            me.logout();
          } else if (data[0]) {
            me.set('terminal', data[0]);
            me.loadPayments();
            me.loadContext();
            me.loadPermissions();
            me.loadBP();
            me.loadBPLocation();
            me.loadLocation();
            me.loadPriceList();
            me.loadPriceListVersion();
            me.loadCurrency();
          } else {
            OB.UTIL.showError("Terminal does not exists: " + params.terminal);
          }
        }
      );
    },

    loadPayments: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.Payments').exec({ pos: this.get('terminal').id }
      , function (data) {
        if (data) {
          var i, max;
          me.set('payments', data);
          me.paymentnames = {};
          for (i = 0, max = data.length; i < max; i++) {
            me.paymentnames[data[i].searchKey] = data[i]._identifier;
          }
          me.triggerReady();
        }
      });
    },

    loadContext: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.Context').exec({}
      , function (data) {
        if (data[0]) {
          me.set('context', data[0]);
          me.triggerReady();
        }
      });
    },

    loadPermissions: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.Permissions').exec({}
      , function (data) {
        var i, max, permissions = {};
        if (data) {
          for (i = 0, max = data.length; i < max; i++) {
            permissions[data[i]['obposPos' + OB.Constants.FIELDSEPARATOR + '_identifier']] = true;
          }
          me.set('permissions', permissions);
          me.triggerReady();
        }
      });
    },

    loadBP: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.BusinessPartner').exec({
        bp: this.get('terminal').businessPartner
      }, function (data) {
        if (data[0]) {
          me.set('businesspartner', data[0]);
          me.triggerReady();
        }
      });
    },

    loadBPLocation: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.BusinessPartnerLocation').exec({
        bploc: this.get('terminal').partnerAddress
      }, function (data) {
        if (data[0]) {
          me.set('bplocation', data[0]);
        }
      });
    },

    loadLocation: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.Location').exec({
        org: this.get('terminal').organization
      }, function (data) {
        if (data[0]) {
          me.set('location', data[0]);
        }
      });
    },

    loadPriceList: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.PriceList').exec({
        pricelist: this.get('terminal').priceList
      }, function (data) {
        if (data[0]) {
          me.set('pricelist', data[0]);
        }
      });
    },

    loadPriceListVersion: function () {
      var me = this;
      new OB.DS.Query('org.openbravo.retail.posterminal.term.PriceListVersion').exec({
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
      new OB.DS.Query('org.openbravo.retail.posterminal.term.Currency').exec({
        currency: this.get('terminal').currency
      }, function (data) {
        if (data[0]) {
          me.set('currency', data[0]);
          me.triggerReady();
        }
      });
    },

    triggerReady: function () {
      if (this.get('payments') && this.get('pricelistversion') && this.get('currency') && this.get('businesspartner') && this.get('context') && this.get('permissions')) {
        this.trigger('ready');
      }
    },

    triggerLogout: function () {
      this.trigger('logout');
    },

    triggerLoginSuccess: function () {
      this.trigger('loginsuccess');
    },

    triggerLoginFail: function (e, mode) {
      if (mode === 'userImgPress') {
        this.trigger('loginUserImgPressfail', e);
      } else {
        this.trigger('loginfail', e);
      }
    },

    hasPermission: function (p) {
      return this.get('context').role.clientAdmin || this.get('permissions')[p];
    },

    getPaymentName: function (key) {
      return this.paymentnames[key];
    },

    hasPayment: function (key) {
      return this.paymentnames[key];
    }

  });

});