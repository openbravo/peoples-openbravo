/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo, Backbone, console, BigDecimal */

(function () {
  // initialize the WebPOS terminal model that extends the core terminal model. after this, OB.MobileApp.model will be available
  OB.Model.POSTerminal = new(OB.Model.Terminal.extend({

    setTerminalName: function (terminalName) {
      this.set('terminalName', terminalName);
      this.set('loginUtilsParams', {
        terminalName: terminalName
      });
    },

    initialize: function () {
      var me = this;

      me.set({
        appName: 'WebPOS',
        appModuleId: 'FF808181326CC34901326D53DBCF0018',
        supportsOffline: true,
        loginUtilsUrl: '../../org.openbravo.retail.posterminal.service.loginutils',
        loginHandlerUrl: '../../org.openbravo.retail.posterminal/POSLoginHandler',
        applicationFormatUrl: '../../org.openbravo.client.kernel/OBPOS_Main/ApplicationFormats',
        logoutUrlParams: window.localStorage.getItem('terminalAuthentication') === 'Y' ? {} : {
          terminal: OB.UTIL.getParameterByName("terminal")
        },
        logConfiguration: {
          deviceIdentifier: window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
          logPropertiesExtension: [

          function () {
            return {
              online: OB.MobileApp.model.get('connectedToERP')
            };
          }]
        },
        profileOptions: {
          showOrganization: false,
          showWarehouse: false,
          defaultProperties: {
            role: 'oBPOSDefaultPOSRole'
          }
        },
        // setting here the localDB, overrides the OB.MobileApp.model localDB default
        localDB: {
          size: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.size,
          name: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.name,
          displayName: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.displayName,
          version: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.major + '.' + OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.minor
        },
        logDBTrxThreshold: 300,
        logDBStmtThreshold: 1000
      });

      me.setTerminalName(window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));

      this.initActions(function () {
        me.setTerminalName(window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));
        me.set('logConfiguration', {
          deviceIdentifier: window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
          logPropertiesExtension: [

          function () {
            return {
              online: OB.MobileApp.model.get('connectedToERP')
            };
          }]
        });
      });

      this.addPropertiesLoader({
        properties: ['terminal'],
        loadFunction: function (terminalModel) {
          OB.info('Loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Terminal').exec(null, function (data) {
            if (data.exception) {
              if (OB.I18N.hasLabel(data.exception.message)) {
                OB.UTIL.showLoading(false);
                OB.UTIL.showConfirmation.display('Error', OB.I18N.getLabel(data.exception.message), [{
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true,
                  action: function () {
                    terminalModel.logout();
                    OB.UTIL.showLoading(true);
                  }
                }], {
                  onHideFunction: function () {
                    OB.UTIL.showLoading(true);
                    terminalModel.logout();
                  }
                });
              } else {
                var msg = "";
                if (data.exception.message !== undefined) {
                  msg = " Error: " + data.exception.message;
                }
                OB.UTIL.showConfirmation.display('Error', OB.I18N.getLabel('OBPOS_errorLoadingTerminal') + msg, [{
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true,
                  action: function () {
                    OB.UTIL.showLoading(true);
                    terminalModel.logout();
                  }
                }], {
                  onHideFunction: function () {
                    OB.UTIL.showLoading(true);
                    terminalModel.logout();
                  }
                });
              }
            } else if (data[0]) {
              // load the OB.MobileApp.model.get('terminal') attributes
              terminalModel.set(me.properties[0], data[0]);

              // update the local database with the document sequence received
              OB.MobileApp.model.saveDocumentSequence(OB.MobileApp.model.get('terminal').lastDocumentNumber, OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber);

              window.localStorage.setItem('terminalId', data[0].id);
              terminalModel.set('useBarcode', terminalModel.get('terminal').terminalType.usebarcodescanner);
              OB.MobileApp.view.scanningFocus(true);
              if (!terminalModel.usermodel) {
                terminalModel.setUserModelOnline();
              } else {
                terminalModel.propertiesReady(me.properties);
              }
              OB.UTIL.HookManager.executeHooks('OBPOS_TerminalLoadedFromBackend', {
                data: data[0]
              });
            } else {
              OB.UTIL.showError("Terminal does not exists: " + 'params.terminal');
            }
          }, function (data) {
            // connection error.
            OB.UTIL.Debug.execute(function () {
              console.error("Error while retrieving the terminal info " + data.exception);
            });
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['context'],
        sync: false,
        loadFunction: function (terminalModel) {
          OB.info('Loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.mobile.core.login.Context').exec({
            terminal: OB.MobileApp.model.get('terminalName'),
            ignoreForConnectionStatus: true
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
          OB.info('loading... ' + this.properties);
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
        properties: ['cashMgmtDepositEvents'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtDepositEvents').exec(null, function (data) {
            if (data) {
              terminalModel.set(me.properties[0], data);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['cashMgmtDropEvents'],
        loadFunction: function (terminalModel) {
          console.log('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtDropEvents').exec(null, function (data) {
            if (data) {
              terminalModel.set(me.properties[0], data);
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['businesspartner'],
        loadFunction: function (terminalModel) {
          OB.info('loading... ' + this.properties);
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
          OB.info('loading... ' + this.properties);
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
          OB.info('loading... ' + this.properties);
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
          OB.info('loading... ' + this.properties);
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
          OB.info('loading... ' + this.properties);
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
          OB.info('loading... ' + this.properties);
          var me = this;
          var params = {};
          var currentDate = new Date();
          params.terminalTime = currentDate;
          params.terminalTimeOffset = currentDate.getTimezoneOffset();
          new OB.DS.Request('org.openbravo.retail.posterminal.term.PriceListVersion').exec(params, function (data) {
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
          OB.info('loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Currency').exec(null, function (data) {
            if (data[0]) {
              terminalModel.set(me.properties[0], data[0]);
              //Precision used by arithmetics operations is set using the currency
              terminalModel.propertiesReady(me.properties);
            }
          });
        }
      });

      this.get('dataSyncModels').push({
        model: OB.Model.ChangedBusinessPartners,
        className: 'org.openbravo.retail.posterminal.CustomerLoader',
        criteria: {}
      });

      this.get('dataSyncModels').push({
        model: OB.Model.ChangedBPlocation,
        className: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
        criteria: {}
      });

      this.get('dataSyncModels').push({
        model: OB.Model.Order,
        className: 'org.openbravo.retail.posterminal.OrderLoader',
        timeout: 20000,
        criteria: {
          hasbeenpaid: 'Y'
        }
      });

      this.get('dataSyncModels').push({
        model: OB.Model.CashManagement,
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashMgmt',
        criteria: {
          'isbeingprocessed': 'N'
        }
      });

      this.get('dataSyncModels').push({
        model: OB.Model.CashUp,
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashClose',
        timeout: 600000,
        criteria: {},
        postProcessingFunction: function (data, callback) {
          OB.UTIL.initCashUp(function () {
            OB.UTIL.deleteCashUps(data);
            callback();
          });
        }
      });

      this.on('ready', function () {
        var terminal = this.get('terminal');
        OB.UTIL.initCashUp();
        // Set Hardware..
        OB.POS.hwserver = new OB.DS.HWServer(terminal.hardwareurl, terminal.scaleurl);

        // Set Arithmetic properties:
        OB.DEC.setContext(OB.UTIL.getFirstValidValue([me.get('currency').obposPosprecision, me.get('currency').pricePrecision]), BigDecimal.prototype.ROUND_HALF_UP);

        // Set disable promotion discount property
        OB.Dal.find(OB.Model.Discount, {
          _whereClause: "where m_offer_type_id in (" + OB.Model.Discounts.getManualPromotions() + ")"
        }, function (promos) {
          if (promos.length === 0) {
            me.set('isDisableDiscount', true);
          } else {
            me.set('isDisableDiscount', false);
          }
        }, function () {
          return true;
        });

        OB.UTIL.HookManager.executeHooks('OBPOS_LoadPOSWindow', {}, function () {
          OB.POS.navigate('retail.pointofsale');
        });

        if (me.get('loggedOffline') === true) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineLogin'));
        }

        OB.POS.hwserver.print(new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.WelcomeTemplate), {});
      });

      OB.Model.Terminal.prototype.initialize.call(me);

    },

    runSyncProcess: function (successCallback, errorCallback) {
      var me = this;

      function run() {
        OB.debug('Executing pre synch hook.');
        OB.UTIL.HookManager.executeHooks('OBPOS_PreSynchData', {}, function () {
          OB.UTIL.showI18NWarning('OBPOS_SynchronizingDataMessage', 'OBPOS_SynchronizationWasSuccessfulMessage');
          OB.debug('Synch all models.');
          OB.MobileApp.model.syncAllModels(function () {
            OB.UTIL.showI18NSuccess('OBPOS_SynchronizationWasSuccessfulMessage', 'OBPOS_SynchronizingDataMessage');
            OB.debug('Synch success.');
            if (successCallback) {
              successCallback();
            }
          }, function () {
            OB.warn("runSyncProcess failed. The WebPOS is most likely to be offline, but a real error could be present.");
            // OB.UTIL.showI18NError('OBPOS_SynchronizationFailedMessage', 'OBPOS_SynchronizingDataMessage');
            if (errorCallback) {
              errorCallback();
            }
          });
        });
      }
      if (window.localStorage.getItem('terminalAuthentication') === 'Y') {
        var process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckTerminalAuth');

        OB.trace('Checking authentication');

        process.exec({
          terminalName: window.localStorage.getItem('terminalName'),
          terminalKeyIdentifier: window.localStorage.getItem('terminalKeyIdentifier'),
          terminalAuthentication: window.localStorage.getItem('terminalAuthentication')
        }, function (data, message) {
          if (data && data.exception) {
            //ERROR or no connection
            OB.error(OB.I18N.getLabel('OBPOS_TerminalAuthError'));
          } else if (data && (data.isLinked === false || data.terminalAuthentication)) {
            if (data.isLinked === false) {
              window.localStorage.removeItem('terminalName');
              window.localStorage.removeItem('terminalKeyIdentifier');
            }
            if (data.terminalAuthentication) {
              window.localStorage.setItem('terminalAuthentication', data.terminalAuthentication);
            }
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_TerminalAuthChange'), OB.I18N.getLabel('OBPOS_TerminalAuthChangeMsg'), [{
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true,
              action: function () {
                OB.UTIL.showLoading(true);
                me.logout();
              }
            }], {
              onHideFunction: function () {
                OB.UTIL.showLoading(true);
                me.logout();
              }
            });
          } else {
            run();
          }
        });
      } else {
        run();
      }
    },

    returnToOnline: function () {

      //The session is fine, we don't need to warn the user
      //but we will attempt to send all pending orders automatically
      this.runSyncProcess();
    },

    renderMain: function () {
      if (!this.get('terminal')) {
        OB.UTIL.Debug.execute(function () {
          // show an error while in debug mode to help debugging and testing
          console.error("OB.MobileApp.model.get('terminal') properties have not been loaded");
        });
        return;
      }

      var i, paymentcashcurrency, paymentcash, paymentlegacy, max, me = this,
          defaultpaymentcash, defaultpaymentcashcurrency;

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
          if (this.get('payments')[i].paymentMethod.currency === this.get('terminal').currency) {
            paymentcashcurrency = this.get('payments')[i].payment.searchKey;
            if (this.get('payments')[i].paymentMethod.defaultCashPaymentMethod) {
              defaultpaymentcashcurrency = this.get('payments')[i].payment.searchKey;
            }
          }
          if (this.get('payments')[i].paymentMethod.defaultCashPaymentMethod) {
            defaultpaymentcash = this.get('payments')[i].payment.searchKey;
          }
        }
      }
      // sets the default payment method
      this.set('paymentcash', defaultpaymentcashcurrency || defaultpaymentcash || paymentcashcurrency || paymentcash || paymentlegacy);

      // add the currency converters
      _.each(OB.MobileApp.model.get('payments'), function (paymentMethod) {
        var fromCurrencyId = parseInt(OB.MobileApp.model.get('currency').id, 10);
        var toCurrencyId = parseInt(paymentMethod.paymentMethod.currency, 10);
        if (fromCurrencyId !== toCurrencyId) {
          OB.UTIL.currency.addConversion(toCurrencyId, fromCurrencyId, paymentMethod.rate);
          OB.UTIL.currency.addConversion(fromCurrencyId, toCurrencyId, paymentMethod.mulrate);
        }
      }, this);

      //OB.UTIL.initCashUp(OB.UTIL.calculateCurrentCash);
      OB.MobileApp.model.on('window:ready', function () {
        if (window.localStorage.getItem('terminalAuthentication') === 'Y') {
          var process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckTerminalAuth');
          process.exec({
            terminalName: window.localStorage.getItem('terminalName'),
            terminalKeyIdentifier: window.localStorage.getItem('terminalKeyIdentifier'),
            terminalAuthentication: window.localStorage.getItem('terminalAuthentication')
          }, function (data, message) {
            if (data && data.exception) {
              //ERROR or no connection
              OB.error(OB.I18N.getLabel('OBPOS_TerminalAuthError'));
            } else if (data && (data.isLinked === false || data.terminalAuthentication)) {
              if (data.isLinked === false) {
                window.localStorage.removeItem('terminalName');
                window.localStorage.removeItem('terminalKeyIdentifier');
              }
              if (data.terminalAuthentication) {
                window.localStorage.setItem('terminalAuthentication', data.terminalAuthentication);
              }
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_TerminalAuthChange'), OB.I18N.getLabel('OBPOS_TerminalAuthChangeMsg'), [{
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function () {
                  OB.UTIL.showLoading(true);
                  me.logout();
                }
              }], {
                onHideFunction: function () {
                  OB.UTIL.showLoading(true);
                  me.logout();
                }
              });
            }
          });
        }

      });

      // force the initialization of the document sequence info
      this.saveDocumentSequence();

      this.trigger('ready');
    },

    postLoginActions: function () {
      var me = this,
          loadModelsIncFunc;
      //MASTER DATA REFRESH
      var minIncRefresh = this.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
      if (minIncRefresh) {
        if (this.get('loggedUsingCache')) {
          OB.MobileApp.model.set('minIncRefreshSynchronized', false);
          OB.MobileApp.model.on('synchronized', function () {
            if (OB.MobileApp.model.get('minIncRefreshSynchronized')) {
              return;
            }
            OB.MobileApp.model.set('minIncRefreshSynchronized', true);
            OB.MobileApp.model.loadModels(null, true);
            if (me.get('loggedUsingCache')) {
              me.set('loggedUsingCache', false);
              me.renderTerminalMain();
            }
          });
        } else {
          if (me.get('loggedUsingCache')) {
            me.set('loggedUsingCache', false);
            me.renderTerminalMain();
          }
        }
        loadModelsIncFunc = function () {
          OB.MobileApp.model.loadModels(null, true);
        };
        setInterval(loadModelsIncFunc, minIncRefresh);
      } else if (me.get('loggedUsingCache')) {
        me.set('loggedUsingCache', false);
        me.renderTerminalMain();
      }
    },

    cleanSessionInfo: function () {
      this.cleanTerminalData();
    },

    preLoginActions: function () {
      this.cleanSessionInfo();
    },

    preLogoutActions: function () {
      if (OB.POS.hwserver !== undefined) {
        OB.POS.hwserver.print(new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.GoodByeTemplate), {});
      }
      this.cleanSessionInfo();
    },

    postCloseSession: function (session) {
      //All pending to be paid orders will be removed on logout
      OB.Dal.find(OB.Model.Order, {
        'session': session.get('id'),
        'hasbeenpaid': 'N'
      }, function (orders) {
        var i, j, order, orderlines, orderline, errorFunc = function () {
            OB.error(arguments);
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
        OB.error(arguments);
        OB.MobileApp.model.triggerLogout();
      });
    },

    // these variables will keep the minimum value that the document order could have
    // they feed from the local database, and the server
    documentnoThreshold: -1,
    quotationnoThreshold: -1,

    /**
     * Save the new values if are higher than the last knowwn values
     * - the minimum sequence number can only grow
     */
    saveDocumentSequence: function (documentnoSuffix, quotationnoSuffix) {
      var me = this;

      /**
       * If for whatever reason the maxSuffix is not the current order suffix (most likely, the server returning a higher docno value)
       * 1. if there is a current order
       * 2. and has no lines (no product added, etc)
       * 3. if the current order suffix is lower than the minNumbers
       * 4. delete the order
       */
      var synchronizeCurrentOrder = function () {
          var orderlist = OB.MobileApp.model.orderList;
          if (orderlist && orderlist.models.length > 0 && orderlist.current) {
            if (orderlist.current.get('lines') && orderlist.current.get('lines').length === 0) {
              if (orderlist.current.get('documentnoSuffix') <= me.documentnoThreshold) {
                orderlist.deleteCurrent();
              }
            }
          }
          };

      // verify that the values are higher than the local variables
      if (documentnoSuffix > this.documentnoThreshold || documentnoSuffix === 0) {
        this.documentnoThreshold = documentnoSuffix;
      }
      if (quotationnoSuffix > this.quotationnoThreshold || quotationnoSuffix === 0) {
        this.quotationnoThreshold = quotationnoSuffix;
      }

      // verify the database values
      OB.Dal.find(OB.Model.DocumentSequence, {
        'posSearchKey': this.get('terminal').searchKey
      }, function (documentSequenceList) {

        var docSeq;
        if (documentSequenceList && documentSequenceList.length > 0) {
          // There can only be one documentSequence model in the list (posSearchKey is unique)
          docSeq = documentSequenceList.models[0];
          // verify if the new values are higher and if it is not undefined or 0
          if (docSeq.get('documentSequence') > me.documentnoThreshold && documentnoSuffix !== 0) {
            me.documentnoThreshold = docSeq.get('documentSequence');
          }
          if (docSeq.get('quotationDocumentSequence') > me.quotationnoThreshold && quotationnoSuffix !== 0) {
            me.quotationnoThreshold = docSeq.get('quotationDocumentSequence');
          }
        } else {
          // There is not a document sequence for the pos, create it
          docSeq = new OB.Model.DocumentSequence();
          docSeq.set('posSearchKey', me.get('terminal').searchKey);
        }

        // update the database
        docSeq.set('documentSequence', me.documentnoThreshold);
        docSeq.set('quotationDocumentSequence', me.quotationnoThreshold);
        OB.Dal.save(docSeq, function () {
          synchronizeCurrentOrder();
        }, function () {
          // nothing to do
        });

      }, function () {
        OB.debug("The 'c_document_sequence' table is locked");
      });
    },

    /**
     * Updates the document sequence. This method should only be called when an order has been sent to the server
     * If the order is a quotation, only update the quotationno
     */
    updateDocumentSequenceWhenOrderSaved: function (documentnoSuffix, quotationnoSuffix) {
      if (quotationnoSuffix >= 0) {
        documentnoSuffix = -1;
      }
      this.saveDocumentSequence(documentnoSuffix, quotationnoSuffix);
    },

    // get the first document number available
    getLastDocumentnoSuffixInOrderlist: function () {
      var lastSuffix = null;
      if (OB.MobileApp.model.orderList.length > 0) {
        var i = OB.MobileApp.model.orderList.models.length - 1;
        while (lastSuffix === null && i >= 0) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (!order.get('isPaid') && !order.get('isQuotation') && order.get('docNoPrefix') === OB.MobileApp.model.get('terminal').docNoPrefix) {
            lastSuffix = order.get('documentnoSuffix');
          }
          i--;
        }
      }
      if (lastSuffix === null || lastSuffix < this.documentnoThreshold) {
        lastSuffix = this.documentnoThreshold;
      }
      return lastSuffix;
    },
    // get the first quotation number available
    getLastQuotationnoSuffixInOrderlist: function () {
      var lastSuffix = null;
      if (OB.MobileApp.model.orderList.length > 0) {
        var i = OB.MobileApp.model.orderList.models.length - 1;
        while (lastSuffix === null && i >= 0) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (order.get('isQuotation') && order.get('quotationDocNoPrefix') === OB.MobileApp.model.get('terminal').quotationDocNoPrefix) {
            lastSuffix = order.get('quotationnoSuffix');
          }
          i--;
        }
      }
      if (lastSuffix === null || lastSuffix < this.quotationnoThreshold) {
        lastSuffix = this.quotationnoThreshold;
      }
      return lastSuffix;
    },

    // call this method to get a new order document number
    getNextDocumentno: function () {
      var next = this.getLastDocumentnoSuffixInOrderlist() + 1;
      return {
        documentnoSuffix: next,
        documentNo: OB.MobileApp.model.get('terminal').docNoPrefix + '/' + OB.UTIL.padNumber(next, 7)
      };
    },
    // call this method to get a new quotation document number
    getNextQuotationno: function () {
      var next = this.getLastQuotationnoSuffixInOrderlist() + 1;
      return {
        quotationnoSuffix: next,
        documentNo: OB.MobileApp.model.get('terminal').quotationDocNoPrefix + '/' + OB.UTIL.padNumber(next, 7)
      };
    },

    getPaymentName: function (key) {
      if (this.paymentnames[key] && this.paymentnames[key].payment && this.paymentnames[key].payment._identifier) {
        return this.paymentnames[key].payment._identifier;
      }
      return null;
    },

    hasPayment: function (key) {
      return this.paymentnames[key];
    },

    isSafeToResetDatabase: function (callbackIsSafe, callbackIsNotSafe) {

      OB.Dal.find(OB.Model.Order, {
        hasbeenpaid: 'Y'
      }, function (models) {
        if (models.length > 0) {
          callbackIsNotSafe();
          return;
        }
        OB.Dal.find(OB.Model.CashManagement, {
          'isbeingprocessed': 'N'
        }, function (models) {
          if (models.length > 0) {
            callbackIsNotSafe();
            return;
          }
          OB.Dal.find(OB.Model.CashUp, {
            isbeingprocessed: 'Y'
          }, function (models) {
            if (models.length > 0) {
              callbackIsNotSafe();
              return;
            }

            OB.Dal.find(OB.Model.ChangedBusinessPartners, null, function (models) {
              if (models.length > 0) {
                callbackIsNotSafe();
                return;
              }
              callbackIsSafe();
            }, callbackIsSafe);
          }, callbackIsSafe);
        }, callbackIsSafe);
      }, callbackIsSafe);
    },

    databaseCannotBeResetAction: function () {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ResetNeededNotSafeTitle'), OB.I18N.getLabel('OBPOS_ResetNeededNotSafeMessage', [window.localStorage.getItem('terminalName')]));
    },

    dialog: null,
    preLoadContext: function (callback) {
      if (!window.localStorage.getItem('terminalKeyIdentifier') && !window.localStorage.getItem('terminalName') && window.localStorage.getItem('terminalAuthentication') === 'Y') {
        OB.UTIL.showLoading(false);
        if (OB.UI.ModalSelectTerminal) {
          this.dialog = OB.MobileApp.view.$.confirmationContainer.createComponent({
            kind: 'OB.UI.ModalSelectTerminal',
            name: 'modalSelectTerminal',
            callback: callback,
            context: this
          });
          this.dialog.show();
        }
      } else {
        callback();
      }
    },
    linkTerminal: function (terminalData, callback) {
      var params = this.get('loginUtilsParams') || {},
          me = this;
      params.command = 'preLoginActions';
      params.params = terminalData;
      new OB.OBPOSLogin.UI.LoginRequest({
        url: OB.MobileApp.model.get('loginUtilsUrl')
      }).response(this, function (inSender, inResponse) {
        if (inResponse.exception) {
          OB.UTIL.showConfirmation.display('Error', OB.I18N.getLabel(inResponse.exception), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isConfirmButton: true,
            action: function () {
              if (OB.UI.ModalSelectTerminal) {
                me.dialog = OB.MobileApp.view.$.confirmationContainer.createComponent({
                  kind: 'OB.UI.ModalSelectTerminal',
                  name: 'modalSelectTerminal',
                  callback: callback,
                  context: me
                });
                me.dialog.show();
              }
            }
          }], {
            onHideFunction: function () {
              if (OB.UI.ModalSelectTerminal) {
                me.dialog = OB.MobileApp.view.$.confirmationContainer.createComponent({
                  kind: 'OB.UI.ModalSelectTerminal',
                  name: 'modalSelectTerminal',
                  callback: callback,
                  context: me
                });
                me.dialog.show();
              }
            }
          });
        } else {
          OB.appCaption = inResponse.appCaption;
          me.setTerminalName(inResponse.terminalName);
          window.localStorage.setItem('terminalName', inResponse.terminalName);
          window.localStorage.setItem('terminalKeyIdentifier', inResponse.terminalKeyIdentifier);
          callback();
        }

      }).error(function (inSender, inResponse) {
        callback();
      }).go(params);
    },

    initActions: function (callback) {
      var params = this.get('loginUtilsParams') || {},
          me = this;
      params.command = 'initActions';
      new OB.OBPOSLogin.UI.LoginRequest({
        url: '../../org.openbravo.retail.posterminal.service.loginutils'
      }).response(this, function (inSender, inResponse) {
        window.localStorage.setItem('terminalAuthentication', inResponse.terminalAuthentication);
        me.setTerminalName(window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));
        callback();
      }).error(function (inSender, inResponse) {
        callback();
      }).go(params);
    }

  }))();

  OB.POS = {
    modelterminal: OB.MobileApp.model, // kept fot backward compatibility. Deprecation id: 27646
    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
    paramTerminal: window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
    hrefWindow: function (windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.MobileApp.model.get('terminalName')) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function (callback) {
      OB.MobileApp.model.logout();
    },
    lock: function (callback) {
      OB.MobileApp.model.lock();
    },
    windows: null,
    navigate: function (route) {
      OB.MobileApp.model.navigate(route);
    },
    registerWindow: function (window) {
      OB.MobileApp.windowRegistry.registerWindow(window);
    },
    cleanWindows: function () {
      OB.MobileApp.model.cleanWindows();
    }
  };

  OB.Constants = {
    FIELDSEPARATOR: '$',
    IDENTIFIER: '_identifier'
  };

  OB.Format = window.OB.Format || {};

  OB.I18N = window.OB.I18N || {};

  OB.I18N.labels = {};

}());