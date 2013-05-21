/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, console, enyo, Backbone, window, confirm, $LAB */

(function () {
  var executeWhenDOMReady;

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
          version: '0.7'
        }
      });

      this.addPropertiesLoader({
        properties: ['terminal'],
        loadFunction: function (terminalModel) {
          console.log('Loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Terminal').exec(null, function (data) {
            if (data.exception) {
              OB.POS.navigate('login');
              if (OB.I18N.hasLabel(data.exception.message)) {
                OB.UTIL.showError(OB.I18N.getLabel(data.exception.message));
              } else {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorLoadingTerminal'));
              }
            } else if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              terminalModel.set('useBarcode', terminalModel.get('terminal').terminalType.usebarcodescanner);
              OB.MobileApp.view.scanningFocus(true);
              if (!terminalModel.usermodel) {
                terminalModel.setUserModelOnline();
              } else {
                terminalModel.propertiesReady(me.properties);
              }
            } else {
              OB.UTIL.showError("Terminal does not exists: " + 'params.terminal');
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['context'],
        sync: false,
        loadFunction: function (terminalModel) {
          console.log('Loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.mobile.core.login.Context').exec({
            terminal: OB.MobileApp.model.get('terminalName')
          }, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['payments'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Payments').exec(null, function (data) {
            if (data) {
              var i, max, paymentlegacy, paymentcash, paymentcashcurrency;
              terminalModel.set(me.properties[0], data);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['businesspartner'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.BusinessPartner').exec(null, function (data) {
            if (data[0]) {
              //TODO set backbone model
              terminalModel.set(me.properties[0], data[0].id);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['location'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Location').exec(null, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['pricelist'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceList').exec(null, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['warehouses'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Warehouses').exec(null, function (data) {
            if (data && data.exception) {
              //MP17
              terminalModel.set(me.properties[0], []);
            } else {
              terminalModel.set(me.properties[0], data);
            }
            terminalModel.propertiesReady(me.properties);
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['writableOrganizations'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Process('org.openbravo.retail.posterminal.term.WritableOrganizations').exec(null, function (data) {
            if (data.length > 0) {
              terminalModel.set(me.properties[0], data);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['pricelistversion'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceListVersion').exec(null, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['currency'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Currency').exec(null, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              //Precision used by arithmetics operations is set using the currency
              OB.DEC.scale = data[0].pricePrecision;
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      OB.Model.Terminal.prototype.initialize.call(this);
    },

    returnToOnline: function () {

      //The session is fine, we don't need to warn the user
      //but we will attempt to send all pending orders automatically
      OB.Dal.find(OB.Model.ChangedBusinessPartners, null, function (customersChangedNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customersChangedNotProcessed || customersChangedNotProcessed.length === 0) {
          OB.UTIL.processPaidOrders(null);
          return;
        }
        successCallback = function () {
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_pendigDataOfCustomersProcessed'));
          OB.UTIL.processPaidOrders(null);
        };
        errorCallback = function () {
          //nothing to show
        };
        customersChangedNotProcessed.each(function (cus) {
          cus.set('json', enyo.json.parse(cus.get('json')));
        });
        OB.UTIL.processCustomers(customersChangedNotProcessed, successCallback, errorCallback);
      });
    },

    renderMain: function () {
      var i, paymentcashcurrency, paymentcash, paymentlegacy, minIncRefresh, max, loadModelsIncFunc;
      if (!OB.UTIL.isSupportedBrowser()) {
        OB.MobileApp.model.renderLogin();
        return false;
      }
      OB.DS.commonParams = OB.DS.commonParams || {};
      OB.DS.commonParams = {
        client: this.get('terminal').client,
        organization: this.get('terminal').organization,
        pos: this.get('terminal').id,
        terminalName: this.get('terminalName')
      };

      //LEGACY
      this.paymentnames = {};
      for (i = 0, max = this.get('payments').length; i < max; i++) {
        this.paymentnames[this.get('payments')[i].payment.searchKey] = this.get('payments')[i];
        if (this.get('payments')[i].payment.searchKey === 'OBPOS_payment.cash') {
          paymentlegacy = this.get('payments')[i].payment.searchKey;
        }
        if (this.get('payments')[i].paymentMethod.iscash) {
          paymentcash = this.get('payments')[i].payment.searchKey;
        }
        if (this.get('payments')[i].paymentMethod.iscash && this.get('payments')[i].paymentMethod.currency === this.get('terminal').currency) {
          paymentcashcurrency = this.get('payments')[i].payment.searchKey;
        }
      }
      // sets the default payment method
      this.set('paymentcash', paymentcashcurrency || paymentcash || paymentlegacy);

      //MASTER DATA REFRESH
      minIncRefresh = this.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
      if (minIncRefresh) {
        loadModelsIncFunc = function () {
          OB.MobileApp.model.loadModels(null, true);
          setTimeout(loadModelsIncFunc, minIncRefresh);
        };
        setTimeout(loadModelsIncFunc, minIncRefresh);
      }

      this.on('seqNoReady', function () {
        this.trigger('ready'); //NAVIGATE
      }, this);

      this.setDocumentSequence();
    },

    cleanSessionInfo: function () {
      this.cleanTerminalData();
    },

    preLoginActions: function () {
      this.cleanSessionInfo();
    },

    preLogoutActions: function () {
      this.cleanSessionInfo();
    },

    postCloseSession: function (session) {
      //All pending to be paid orders will be removed on logout
      OB.Dal.find(OB.Model.Order, {
        'session': session.get('id'),
        'hasbeenpaid': 'N'
      }, function (orders) {
        var i, j, order, orderlines, orderline, errorFunc = function () {
            window.console.error(arguments);
            };
        var triggerLogoutFunc = function () {
            OB.MobileApp.model.triggerLogout();
            };
        if (orders.models.length === 0) {
          //If there are no orders to remove, a logout is triggered
          OB.MobileApp.model.triggerLogout();
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
        OB.MobileApp.model.triggerLogout();
      });
    },

    compareDocSeqWithPendingOrdersAndSave: function (maxDocumentSequence, maxQuotationDocumentSequence) {
      var orderDocNo, quotationDocNo;
      // compare the last document number returned from the ERP with
      // the last document number of the unprocessed pending lines (if any)
      OB.Dal.find(OB.Model.Order, {}, function (fetchedOrderList) {
        var criteria, maxDocumentSequencePendingOrders;
        if (!fetchedOrderList || fetchedOrderList.length === 0) {
          // There are no pending orders, the initial document sequence
          // will be the one fetched from the database
          OB.MobileApp.model.saveDocumentSequenceAndGo(maxDocumentSequence, maxQuotationDocumentSequence);
        } else {
          // There are pending orders. The document sequence will be set
          // to the maximum of the pending order document sequence and the
          // document sequence retrieved from the server
          maxDocumentSequencePendingOrders = OB.MobileApp.model.getMaxDocumentSequenceFromPendingOrders(fetchedOrderList.models);
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
          OB.MobileApp.model.saveDocumentSequenceAndGo(orderDocNo, quotationDocNo);
        }
      }, function () {
        // If c_order does not exist yet, go with the sequence
        // number fetched from the server
        OB.MobileApp.model.saveDocumentSequenceAndGo(maxDocumentSequence, maxQuotationDocumentSequence);
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
      this.trigger('seqNoReady');
    },

    setDocumentSequence: function () {
      // Obtains the persisted document number (documentno of the last processed order)
      OB.Dal.find(OB.Model.DocumentSequence, {
        'posSearchKey': OB.MobileApp.model.terminalName
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
          OB.MobileApp.model.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
        } else {
          max = OB.MobileApp.model.get('terminal').lastDocumentNumber;
          maxquote = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber;
          // Compares the maximum with the document number of the paid pending orders
          OB.MobileApp.model.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
        }

      }, function () {
        var max = OB.MobileApp.model.get('terminal').lastDocumentNumber,
            maxquote = OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber;
        // Compares the maximum with the document number of the paid pending orders
        OB.MobileApp.model.compareDocSeqWithPendingOrdersAndSave(max, maxquote);
      });
    },

    saveDocumentSequenceInDB: function () {
      var me = this,
          documentSequence = this.get('documentsequence'),
          quotationDocumentSequence = this.get('quotationDocumentSequence'),
          criteria = {
          'posSearchKey': this.get('terminal').searchKey
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
          docSeq.set('posSearchKey', me.get('terminal').searchKey);
          docSeq.set('documentSequence', documentSequence);
          docSeq.set('quotationDocumentSequence', quotationDocumentSequence);
        }
        OB.Dal.save(docSeq, null, function () {
          window.console.error(arguments);
        });
      });
    },

    getPaymentName: function (key) {
      return this.paymentnames[key].payment._identifier;
    },

    hasPayment: function (key) {
      return this.paymentnames[key];
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
      return '?terminal=' + window.encodeURIComponent(OB.MobileApp.model.get('terminalName')) + '&window=' + window.encodeURIComponent(windowname);
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
    terminalName: OB.MobileApp.model.get('terminalName')
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