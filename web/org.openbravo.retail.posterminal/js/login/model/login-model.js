/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm */

(function () {
  var executeWhenDOMReady;

  function triggerReady(models) {
    if (models._LoadOnline && OB.UTIL.queueStatus(models._LoadQueue || {})) {
      models.trigger('ready');
    }
  }

  // global components.
  OB = window.OB || {};

  OB.Model.POSTerminal = OB.Model.Terminal.extend({
    initialize: function () {
      this.set({
        appName: 'WebPOS',
        appModuleId: 'FF808181326CC34901326D53DBCF0018',
        terminalName: OB.UTIL.getParameterByName("terminal") || "POS-1",
        supportsOffline: true,
        loginUtilsUrl: '../../org.openbravo.retail.posterminal.service.loginutils',
        loginHandlerUrl: '../../org.openbravo.retail.posterminal/POSLoginHandler',
        profileOptions: {
          showOrganization: false,
          showWarehouse: false,
          defaultProperties: {
            role: 'oBPOSDefaultPOSRole'
          }
        },
        localDB: {
          size: 50 * 1024 * 1024,
          name: 'WEBPOS',
          displayName: 'Openbravo Web POS',
          version: '0.5'
        }
      });


      OB.Model.Terminal.prototype.initialize.call(this);
      OB.MobileApp.model.on('change:terminal', function () {
        // setting common datasource parameters based on terminal
        var t = OB.MobileApp.model.get('terminal');
        console.log('change terminal', t);
        if (!t) {
          return;
        }

        OB.DS.commonParams = OB.DS.commonParams || {};
        if (t.client) {
          OB.DS.commonParams.client = t.client;
        }
        if (t.organization) {
          OB.DS.commonParams.organization = t.organization;
        }

        if (t.id) {
          OB.DS.commonParams.pos = t.id;
        }
      });

      OB.MobileApp.model.on('change:terminalName', function () {
        OB.DS.commonParams = OB.DS.commonParams || {};
        OB.DS.commonParams.terminalName = OB.MobileApp.model.get('terminalName');
      });

      if (this.get('terminalName')) {
        OB.DS.commonParams = OB.DS.commonParams || {};
        OB.DS.commonParams.terminalName = this.get('terminalName');
      }

    },

    renderMain: function () {
      console.log('renderMain')
      if (!OB.UTIL.isSupportedBrowser()) {
        OB.MobileApp.model.renderLogin();
        return false;
      }
      var me = OB.MobileApp.model,
          params = {
          terminal: OB.POS.paramTerminal
          };

      OB.MobileApp.model.loggingIn = true;


      OB.DS.commonParams = {};

      OB.MobileApp.model.off('terminal.loaded'); // Unregister previous events.
      OB.MobileApp.model.on('terminal.loaded', function () {
        var oldOB = OB;
        // setting common datasource parameters based on terminal
        var t = me.get('terminal');
        OB.DS.commonParams = {
          client: t.client,
          organization: t.organization,
          pos: t.id
        };

        $LAB.setGlobalDefaults({
          AppendTo: 'body'
        });
        //   OB.POS.cleanWindows();
        if (!OB.MobileApp.model.get('connectedToERP')) {
          OB.Format = JSON.parse(me.usermodel.get('formatInfo'));

          me.load();
          // $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS');
          OB.POS.navigate('retail.pointofsale'); //TODO: this was in main.js, check it
          return;
        }
        $LAB.script('../../org.openbravo.client.kernel/OBCLKER_Kernel/Application').wait(function () {
          var newFormat = OB.Format;
          _.extend(OB, oldOB);
          OB.Format = newFormat;

          me.usermodel.set('formatInfo', JSON.stringify(OB.Format));
          OB.Dal.save(me.usermodel, function () {}, function () {
            window.console.error(arguments);
          });

          me.load();

          //    $LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS');
          OB.POS.navigate('retail.pointofsale'); //TODO: this was in main.js, check it
        });
      });
      if (OB.MobileApp.model.get('connectedToERP')) {

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
              OB.MobileApp.model.setUserModelOnline(true);
            } else {
              me.trigger('terminal.loaded');
            }
          } else {
            OB.UTIL.showError("Terminal does not exists: " + params.terminal);
          }
        });
      } else {
        //Offline mode, we get the terminal information from the local db
        me.set('terminal', JSON.parse(me.usermodel.get('terminalinfo')).terminal);
        me.trigger('terminal.loaded');
      }
    },

    load: function () {
      var termInfo, i, max;
      if (!OB.MobileApp.model.get('connectedToERP')) {
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
      new OB.DS.Request('org.openbravo.mobile.core.login.Context').exec({}, function (data) {
        if (data[0]) {
          me.set('context', data[0]);
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
        'posSearchKey': OB.MobileApp.model.get('terminal').searchKey
      }, function (documentsequence) {
        var lastInternalDocumentSequence, lastInternalQuotationSequence, max, maxquote;
        if (documentsequence && documentsequence.length > 0) {
          lastInternalDocumentSequence = documentsequence.at(0).get('documentSequence');
          lastInternalQuotationSequence = documentsequence.at(0).get('quotationDocumentSequence');
          // Compares the persisted document number with the fetched from the server
          if (lastInternalDocumentSequence > OB.MobileApp.model.get('terminal').lastDocumentNumber) {
            max = lastInternalDocumentSequence;
          } else {
            max = OB.MobileApp.model.get('terminal').lastDocumentNumber;
          }
          if (lastInternalQuotationSequence > OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber) {
            maxquote = lastInternalQuotationSequence;
          } else {
            maxquote = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber;
          }
          // Compares the maximum with the document number of the paid pending orders
          me.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
        } else {
          max = OB.MobileApp.model.get('terminal').lastDocumentNumber;
          maxquote = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber;
          // Compares the maximum with the document number of the paid pending orders
          me.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
        }

      }, function () {
        var max = OB.MobileApp.model.get('terminal').lastDocumentNumber,
            maxquote = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber;
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
          maxDocumentSequence = OB.MobileApp.model.get('terminal').lastDocumentNumber,
          posDocumentNoPrefix = OB.MobileApp.model.get('terminal').docNoPrefix,
          maxQuotationDocumentSequence = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber,
          posQuotationDocumentNoPrefix = OB.MobileApp.model.get('terminal').quotationDocNoPrefix,
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
          modelterminal = OB.MobileApp.model,
          documentSequence = modelterminal.get('documentsequence'),
          quotationDocumentSequence = modelterminal.get('quotationDocumentSequence'),
          criteria = {
          'posSearchKey': OB.MobileApp.model.get('terminal').searchKey
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
          docSeq.set('posSearchKey', OB.MobileApp.model.get('terminal').searchKey);
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

      if (this.get('payments') && this.get('pricelistversion') && this.get('warehouses') && this.get('currency') && this.get('context') && this.get('writableOrganizations') && (this.get('documentsequence') !== undef || this.get('documentsequence') === 0)) {
        OB.MobileApp.model.loggingIn = false;
        if (OB.MobileApp.model.get('connectedToERP')) {
          //In online mode, we save the terminal information in the local db
          this.usermodel.set('terminalinfo', JSON.stringify(this));
          OB.Dal.save(this.usermodel, function () {}, function () {
            window.console.error(arguments);
          });
        }
        minIncRefresh = OB.MobileApp.model.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
        if (minIncRefresh) {
          loadModelsIncFunc = function () {
            console.log('Performing incremental masterdata refresh');
            OB.MobileApp.model.loadModels(null, true);
            setTimeout(loadModelsIncFunc, minIncRefresh);
          };
          setTimeout(loadModelsIncFunc, minIncRefresh);
        }
        this.trigger('ready');
      }
    }
  });

  // var modelterminal= ;
  OB.POS = {
    modelterminal: new OB.Model.POSTerminal(),
    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
    paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
    //    terminal: new OB.UI.Terminal({
    //      test:'1',
    //      terminal: this.modelterminal
    //    }),
    hrefWindow: function (windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function (callback) {
      this.modelterminal.logout();
    },
    lock: function (callback) {
      this.modelterminal.lock();
    },
    windows: null,
    navigate: function (route) {
      //HACK -> when f5 in login page
      //the route to navigate is the same that we are.
      //Backbone doesn't navigates
      //With this hack allways navigate.
      if (route === Backbone.history.fragment) {
        Backbone.history.fragment = '';
      }
      this.modelterminal.router.navigate(route, {
        trigger: true
      });
    },
    registerWindow: function (window) {
      OB.MobileApp.windowRegistry.registerWindow(window);
    },
    cleanWindows: function () {
      this.modelterminal.cleanWindows();
    }
  };

  //  OB.POS.terminal = new OB.UI.Terminal({
  //    terminal: OB.POS.modelterminal
  //  });
  OB.POS.modelterminal.set('loginUtilsParams', {
    terminalName: OB.POS.paramTerminal
  });

  OB.Constants = {
    FIELDSEPARATOR: '$',
    IDENTIFIER: '_identifier'
  };

  OB.Format = window.OB.Format || {};

  OB.I18N = window.OB.I18N || {};

  OB.I18N.labels = {};

  executeWhenDOMReady = function () {
    if (document.readyState === "interactive" || document.readyState === "complete") {
      OB.POS.modelterminal.off('loginfail');
    } else {
      setTimeout(function () {
        executeWhenDOMReady();
      }, 50);
    }
  };
  executeWhenDOMReady();

}());