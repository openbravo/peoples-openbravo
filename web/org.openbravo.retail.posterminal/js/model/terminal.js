/*global B, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.Model = window.OB.Model || {};

  OB.Model.Collection = Backbone.Collection.extend({
    constructor: function(data) {
      this.ds = data.ds;
      Backbone.Collection.prototype.constructor.call(this);
    },
    inithandler: function(init) {
      if (init) {
        init.call(this);
      }
    },
    exec: function(filter) {
      var me = this;
      if (this.ds) {
        this.ds.exec(filter, function(data, info) {
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
      currency: null
    },

    initialize: function() {
      var me = this;
      $(window).bind('online', function() {
        me.triggerOnLine();
      });
      $(window).bind('offline', function() {
        me.triggerOffLine();
      });
    },

    login: function(user, password, mode) {
      OB.UTIL.showLoading(true);
      var me = this;
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('businesspartner', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);
      this.set('currency', null);
      this.set('currencyPrecision', null);
      $.ajax({
        url: '../../org.openbravo.retail.posterminal/POSLoginHandler',
        data: {
          'user': user,
          'password': password,
          'terminal': OB.POS.paramTerminal,
          'Command': 'DEFAULT',
          'IsAjaxCall': 1
        },
        type: 'POST',
        success: function(data, textStatus, jqXHR) {
          var pos, baseUrl;
          if (data && data.showMessage) {
            me.triggerLoginFail(401, mode, data);
            return;
          }
          pos = location.pathname.indexOf('login.jsp');
          baseUrl = window.location.pathname.substring(0, pos);
          window.location = baseUrl + OB.POS.hrefWindow(OB.POS.paramWindow);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          me.triggerLoginFail(jqXHR.status, mode);
        }
      });
    },

    logout: function() {
      var me = this;
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('bplocation', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);
      this.set('currency', null);
      this.set('currencyPrecision', null);

      $.ajax({
        url: '../../org.openbravo.retail.posterminal.service.logout',
        contentType: 'application/json;charset=utf-8',
        dataType: 'json',
        type: 'GET',
        success: function(data, textStatus, jqXHR) {
          me.triggerLogout();
        },
        error: function(jqXHR, textStatus, errorThrown) {
          me.triggerLogout();
        }
      });
    },

    lock: function() {
      alert('Feature not yet implemented');
    },

    load: function() {

      // reset all application state.
      $(window).off('keypress');
      this.set('terminal', null);
      this.set('payments', null);
      this.set('context', null);
      this.set('permissions', null);
      this.set('businesspartner', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);
      this.set('currency', null);
      this.set('currencyPrecision', null);

      // Starting app
      var me = this;
      var params = {
        terminal: OB.POS.paramTerminal
      };

      new OB.DS.Request('org.openbravo.retail.posterminal.term.Terminal').exec(
      params, function(data) {
        if (data.exception) {
          me.logout();
        } else if (data[0]) {
          me.set('terminal', data[0]);
          me.loadPayments();
          me.loadContext();
          me.loadPermissions();
          me.loadBP();
          me.loadLocation();
          me.loadPriceList();
          me.loadPriceListVersion();
          me.loadCurrency();
          me.setDocumentSequence();
        } else {
          OB.UTIL.showError("Terminal does not exists: " + params.terminal);
        }
      });
    },

    loadPayments: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.Payments').exec({
        pos: this.get('terminal').id
      }, function(data) {
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

    loadContext: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.Context').exec({}, function(data) {
        if (data[0]) {
          me.set('context', data[0]);
          me.triggerReady();
        }
      });
    },

    loadPermissions: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.RolePreferences').exec({}, function(data) {
        var i, max, permissions = {};
        if (data) {
          for (i = 0, max = data.length; i < max; i++) {
            permissions[data[i].key] = data[i].value;
          }
          me.set('permissions', permissions);
          me.triggerReady();
        }
      });
    },

    loadBP: function() {
      this.set('businesspartner', this.get('terminal').businessPartner);
    },

    loadLocation: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.Location').exec({
        org: this.get('terminal').organization
      }, function(data) {
        if (data[0]) {
          me.set('location', data[0]);
        }
      });
    },

    loadPriceList: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceList').exec({
        pricelist: this.get('terminal').priceList
      }, function(data) {
        if (data[0]) {
          me.set('pricelist', data[0]);
        }
      });
    },

    loadPriceListVersion: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceListVersion').exec({
        pricelist: this.get('terminal').priceList
      }, function(data) {
        if (data[0]) {
          me.set('pricelistversion', data[0]);
          me.triggerReady();
        }
      });
    },

    loadCurrency: function() {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.Currency').exec({
        currency: this.get('terminal').currency
      }, function(data) {
        if (data[0]) {
          me.set('currency', data[0]);
          //Precision used by arithmetics operations is set using the currency
          OB.DEC.scale = data[0].pricePrecision;
          me.triggerReady();
        }
      });
    },

    setDocumentSequence: function() {
      var me = this,
          maxDocumentSequence, criteria = {
          'hasbeenpaid': 'N'
          };
      // compare the last document number returned from the ERP with
      // the last document number of the unprocessed pending lines (if any)
      OB.Dal.find(OB.Model.Order, criteria, function(fetchedOrderList) {
        var criteria;
        if (!fetchedOrderList || fetchedOrderList.length === 0) {
          // There are no pending orders, the initial document sequence
          // will be the one fetched from the database
          me.saveDocumentSequenceAndGo(OB.POS.modelterminal.get('terminal').lastDocumentNumber);
        } else {
          // There are pending orders. The document sequence will be set
          // to the maximum of the pending order document sequence and the
          // document sequence retrieved from the server
          maxDocumentSequence = me.getMaxDocumentSequenceFromPendingOrders(fetchedOrderList.models);
          me.saveDocumentSequenceAndGo(maxDocumentSequence);
        }
      }, function () {
        // If c_order does not exist yet, go with the sequence
        // number fetched from the server
        me.saveDocumentSequenceAndGo(OB.POS.modelterminal.get('terminal').lastDocumentNumber);
      });
    },

    getMaxDocumentSequenceFromPendingOrders: function(pendingOrders) {
      var nPreviousOrders = pendingOrders.length,
          maxDocumentSequence = OB.POS.modelterminal.get('terminal').lastDocumentNumber,
          posDocumentNoPrefix = OB.POS.modelterminal.get('terminal').docNoPrefix,
          orderCompleteDocumentNo, orderDocumentSequence, i;
      for (i = 0; i < nPreviousOrders; i++) {
        orderCompleteDocumentNo = pendingOrders[i].get('documentNo');
        orderDocumentSequence = parseInt(orderCompleteDocumentNo.substr(posDocumentNoPrefix.length + 1), 10);
        if (orderDocumentSequence > maxDocumentSequence) {
          maxDocumentSequence = orderDocumentSequence;
        }
      }
      return maxDocumentSequence;
    },

    saveDocumentSequenceAndGo: function(documentSequence) {
      this.set('documentsequence', documentSequence);
      this.triggerReady();
    },

    saveDocumentSequenceInDB: function() {
      var me = this,
          modelterminal = OB.POS.modelterminal,
          documentSequence = modelterminal.get('documentsequence'),
          criteria = {
          'posSearchKey': OB.POS.modelterminal.get('terminal').searchKey
          };
      OB.Dal.find(OB.Model.DocumentSequence, criteria, function(documentSequenceList) {
        var docSeq;
        if (documentSequenceList) {
          // There can only be one documentSequence model in the list (posSearchKey is unique)
          docSeq = documentSequenceList.models[0];
          // There exists already a document sequence, update it
          docSeq.set('documentSequence', documentSequence);
        } else {
          // There is not a document sequence for the pos, create it
          docSeq = new OB.Model.DocumentSequence();
          docSeq.set('posSearchKey', OB.POS.modelterminal.get('terminal').searchKey);
          docSeq.set('documentSequence', documentSequence);
        }
        OB.Dal.save(docSeq, null, null);
      });
    },

    triggerReady: function() {
      var undef;
      if (this.get('payments') && this.get('pricelistversion') && this.get('currency') && this.get('context') && this.get('permissions') && this.get('documentsequence') !== undef) {
        this.trigger('ready');
      }
    },

    triggerLogout: function() {
      this.trigger('logout');
    },

    triggerLoginSuccess: function() {
      this.trigger('loginsuccess');
    },

    triggerOnLine: function() {
      this.trigger('online');
    },

    triggerOffLine: function() {
      this.trigger('offline');
    },

    triggerLoginFail: function(e, mode, data) {
      OB.UTIL.showLoading(false);
      if (mode === 'userImgPress') {
        this.trigger('loginUserImgPressfail', e);
      } else {
        this.trigger('loginfail', e, data);
      }
    },

    hasPermission: function(p) {
      return this.get('context').role.clientAdmin || this.get('permissions')[p];
    },

    getPaymentName: function(key) {
      return this.paymentnames[key];
    },

    hasPayment: function(key) {
      return this.paymentnames[key];
    }

  });

}());