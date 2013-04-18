/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm, $LAB */

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
          version: '0.7'
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
        properties: ['payments', 'paymentcash'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Payments').exec({
            pos: terminalModel.get('terminal').id
          }, function (data) {
            if (data) {
              var i, max, paymentlegacy, paymentcash, paymentcashcurrency;
              terminalModel.set(me.properties[0], data);
              terminalModel.paymentnames = {};
              for (i = 0, max = data.length; i < max; i++) {
                terminalModel.paymentnames[data[i].payment.searchKey] = data[i];
                if (data[i].payment.searchKey === 'OBPOS_payment.cash') {
                  paymentlegacy = data[i].payment.searchKey;
                }
                if (data[i].paymentMethod.iscash) {
                  paymentcash = data[i].payment.searchKey;
                }
                if (data[i].paymentMethod.iscash && data[i].paymentMethod.currency === terminalModel.get('terminal').currency) {
                  paymentcashcurrency = data[i].payment.searchKey;
                }
              }
              // sets the default payment method
              terminalModel.set(me.properties[1], paymentcashcurrency || paymentcash || paymentlegacy);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['businesspartner'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          terminalModel.set('businesspartner', terminalModel.get('terminal').businessPartner);
          terminalModel.propertiesReady(this.properties);
        }
      });

      this.addPropertiesLoader({
        properties: ['location'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Location').exec({
            org: terminalModel.get('terminal').organization
          }, function (data) {
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
          new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceList').exec({
            pricelist: terminalModel.get('terminal').priceList
          }, function (data) {
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
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Warehouses').exec({
            organization: terminalModel.get('terminal').organization
          }, function (data) {
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
          new OB.DS.Process('org.openbravo.retail.posterminal.term.WritableOrganizations').exec({

          }, function (data) {
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
          new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceListVersion').exec({
            pricelist: terminalModel.get('terminal').priceList
          }, function (data) {
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
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Currency').exec({
            currency: terminalModel.get('terminal').currency
          }, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              //Precision used by arithmetics operations is set using the currency
              OB.DEC.scale = data[0].pricePrecision;
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['documentsequence', 'quotationDocumentSequence'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          terminalModel.setDocumentSequence();
        }
      });

      OB.Model.Terminal.prototype.initialize.call(this);
      OB.MobileApp.model.on('change:terminal', function () {
        // setting common datasource parameters based on terminal
        var t = OB.MobileApp.model.get('terminal');
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
      if (!OB.UTIL.isSupportedBrowser()) {
        OB.MobileApp.model.renderLogin();
        return false;
      }
      var me = OB.MobileApp.model,
          params = {
          terminal: OB.MobileApp.model.get('terminalName')
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

        if (me.get('loggedOffline')) {
          OB.Format = JSON.parse(me.usermodel.get('formatInfo'));

          me.load();
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

          //$LAB.script('../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS');
          //OB.POS.navigate('retail.pointofsale'); //TODO: this was in main.js, check it
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

            OB.MobileApp.model.set('useBarcode', OB.MobileApp.model.get('terminal').terminalType.usebarcodescanner);
            OB.MobileApp.view.scanningFocus(true);
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

    cleanSessionInfo: function () {
      this.set('terminal', null);
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

    //model.get('terminal') is NOT cleaned
    cleanTerminalData: function () {
      _.each(this.get('propertiesLoaders'), function (curPropertiesToLoadProcess) {
        _.each(curPropertiesToLoadProcess.properties, function (curProperty) {
          this.set(curProperty, null);
        }, this)
      }, this);
    },

    load: function () {
      var termInfo, i, max;
      if (this.get('loggedOffline')) {
        termInfo = JSON.parse(this.usermodel.get('terminalinfo'));

        //Load from termInfo
        _.each(this.get('propertiesLoaders'), function (curPropertiesToLoadProcess) {
          _.each(curPropertiesToLoadProcess.properties, function (curProperty) {
            this.set(curProperty, termInfo[curProperty]);
          }, this)
        }, this);

        //Not included into array
        this.set('permissions', termInfo.permissions);
        this.set('orgUserId', termInfo.orgUserId);
        this.setDocumentSequence();

        this.paymentnames = {};
        for (i = 0, max = termInfo.payments.length; i < max; i++) {
          this.paymentnames[termInfo.payments[i].payment.searchKey] = termInfo.payments[i];
        }

        this.allPropertiesLoaded();
        return;
      }

      //Set array properties as null except terminal
      this.cleanTerminalData();
      this.set('loggedOffline', false);


      //Loading the properties of the array
      console.log('Starting to load properties based on properties loaders', this.get('propertiesLoaders'));
      _.each(this.get('propertiesLoaders'), function (curProperty) {
        //each loadFunction will call to propertiesReady function. This function will trigger
        //allPropertiesLoaded when all of the loadFunctions are done.
        curProperty.loadFunction(this);
      }, this);
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
      this.propertiesReady(['documentsequence', 'quotationDocumentSequence']);
    },

    setDocumentSequence: function () {
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

    //DEVELOPER: this function will be automatically called when all the properties defined in
    //this.get('propertiesLoaders') are loaded. To indicate that a property is loaded
    //me.propertiesReady(properties) should be executed by loadFunction of each property
    allPropertiesLoaded: function () {
      console.log('properties has been loaded successfully', this.attributes);
      var undef, loadModelsIncFunc, loadModelsTotalFunc, minTotalRefresh, minIncRefresh;
      this.loggingIn = false;
      if (!this.get('loggedOffline')) {
        //In online mode, we save the terminal information in the local db
        this.usermodel.set('terminalinfo', JSON.stringify(this));
        OB.Dal.save(this.usermodel, function () {}, function () {
          window.console.error(arguments);
        });
      }
      minIncRefresh = this.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
      if (minIncRefresh) {
        loadModelsIncFunc = function () {
          this.loadModels(null, true);
          setTimeout(loadModelsIncFunc, minIncRefresh);
        };
        setTimeout(loadModelsIncFunc, minIncRefresh);
      }
      this.trigger('ready');
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