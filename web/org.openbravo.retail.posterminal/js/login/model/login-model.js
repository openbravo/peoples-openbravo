/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo, Backbone, console, BigDecimal, localStorage */

(function () {
  // initialize the WebPOS terminal model that extends the core terminal model. after this, OB.MobileApp.model will be available
  OB.Model.POSTerminal = OB.Model.Terminal.extend({

    setTerminalName: function (terminalName) {
      this.set('terminalName', terminalName);
      this.set('loginUtilsParams', {
        terminalName: terminalName
      });
      // set the terminal only if it was empty. this variable is used to detect if the terminal changed
      if (terminalName && !window.localStorage.getItem('terminalName')) {
        window.localStorage.setItem('terminalName', terminalName);
      }
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
          version: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.dbVersion
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
        properties: ['terminal', 'payments', 'cashMgmtDepositEvents', 'cashMgmtDropEvents', 'businesspartner', 'location', 'pricelist', 'warehouses', 'writableOrganizations', 'pricelistversion', 'currency'],
        loadFunction: function (terminalModel) {
          OB.info('[terminal] Loading... ' + this.properties);
          var me = this,
              max, i, handleError;
          var params = {};
          var currentDate = new Date();
          params.terminalTime = currentDate;
          params.terminalTimeOffset = currentDate.getTimezoneOffset();

          handleError = function (data) {
            if (data && data.exception && data.exception.message && OB.I18N.hasLabel(data.exception.message)) {
              //Common error (not a random caught exception).
              // We might need to logout and login again to fix this.
              OB.UTIL.showConfirmation.display('Error', OB.I18N.getLabel('OBPOS_errorLoadingTerminal') + ' ' + OB.I18N.getLabel(data.exception.message), [{
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function () {
                  OB.UTIL.showLoggingOut(true);
                  terminalModel.logout();
                }
              }], {
                onShowFunction: function (popup) {
                  popup.$.headerCloseButton.hide();
                },
                autoDismiss: false
              });
            } else if (OB.MobileApp.model.get('isLoggingIn') === true) {
              var msg = OB.I18N.getLabel('OBPOS_errorLoadingTerminal') + ' ' + OB.I18N.getLabel('OBMOBC_LoadingErrorBody');
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), msg, [{
                label: OB.I18N.getLabel('OBMOBC_Reload'),
                action: function () {
                  window.location.reload();
                }
              }], {
                onShowFunction: function (popup) {
                  window.localStorage.removeItem('cacheAvailableForUser:' + OB.MobileApp.model.get('orgUserId'));
                  popup.$.headerCloseButton.hide();
                  OB.MobileApp.view.$.containerWindow.destroyComponents();
                },
                autoDismiss: false
              });
            }
          };
          new OB.DS.Request('org.openbravo.retail.posterminal.term.Terminal').exec(params, function (data) {
            if (data.exception) {
              handleError(data);
            } else if (data[0]) {
              // load the OB.MobileApp.model              
              for (i = 0, max = data.length; i < max; i++) {
                if (Object.keys(data[i])[0] === "businesspartner") {
                  terminalModel.set(Object.keys(data[i])[0], data[i][Object.keys(data[i])[0]].id);
                } else {
                  terminalModel.set(Object.keys(data[i])[0], data[i][Object.keys(data[i])[0]]);
                }
              }

              // update the local database with the document sequence received
              OB.MobileApp.model.saveDocumentSequence(OB.MobileApp.model.get('terminal').lastDocumentNumber, OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber, function () {
                if (OB.MobileApp.model.orderList) {
                  OB.MobileApp.model.orderList.synchronizeCurrentOrder();
                }
              });

              window.localStorage.setItem('terminalId', data[0].terminal.id);
              terminalModel.set('useBarcode', terminalModel.get('terminal').terminalType.usebarcodescanner);
              OB.MobileApp.view.scanningFocus(true);
              if (!terminalModel.usermodel) {
                OB.MobileApp.model.loadingErrorsActions("The terminal.usermodel should be loaded at this point");
              } else {
                terminalModel.propertiesReady(me.properties);
              }
              OB.UTIL.HookManager.executeHooks('OBPOS_TerminalLoadedFromBackend', {
                data: data[0].terminal.id
              });
            } else {
              OB.UTIL.showError("Terminal does not exists: " + 'params.terminal');
            }
          }, function (data) {
            // connection error.
            OB.UTIL.Debug.execute(function () {
              OB.error("Error while retrieving the terminal info ", data);
            });

            handleError(data);
          });
        }
      });

      this.addPropertiesLoader({
        properties: ['context'],
        sync: false,
        loadFunction: function (terminalModel) {
          OB.info('[terminal] Loading... ' + this.properties);
          var me = this;

          var rr, ajaxRequest2 = new enyo.Ajax({
            url: '../../org.openbravo.mobile.core.context',
            cacheBust: false,
            method: 'GET',
            handleAs: 'json',
            timeout: 20000,
            data: {
              ignoreForConnectionStatus: true
            },
            contentType: 'application/json;charset=utf-8',
            success: function (inSender, inResponse) {
              if (inResponse && inResponse.data) {
                terminalModel.set(me.properties[0], inResponse.data[0]);
                terminalModel.propertiesReady(me.properties);
              } else {
                //Something went wrong server-side
                if (OB.MobileApp.model.get('isLoggingIn') === true) {
                  var msg = OB.I18N.getLabel('OBMOBC_ContextErrorBody') + OB.I18N.getLabel('OBMOBC_LoadingErrorBody');
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), msg, [{
                    label: OB.I18N.getLabel('OBMOBC_Reload'),
                    action: function () {
                      window.location.reload();
                    }
                  }], {
                    onShowFunction: function (popup) {
                      window.localStorage.removeItem('cacheAvailableForUser:' + OB.MobileApp.model.get('orgUserId'));
                      popup.$.headerCloseButton.hide();
                    },
                    autoDismiss: false
                  });
                }
              }
            },
            fail: function (inSender, inResponse) {
              if (OB.MobileApp.model.get('isLoggingIn') === true) {
                var msg = OB.I18N.getLabel('OBMOBC_ContextErrorBody') + OB.I18N.getLabel('OBMOBC_LoadingErrorBody');
                OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), msg, [{
                  label: OB.I18N.getLabel('OBMOBC_Reload'),
                  action: function () {
                    window.location.reload();
                  }
                }], {
                  onShowFunction: function (popup) {
                    window.localStorage.removeItem('cacheAvailableForUser:' + OB.MobileApp.model.get('orgUserId'));
                    popup.$.headerCloseButton.hide();
                  },
                  autoDismiss: false
                });
              }
            }
          });
          rr = new OB.RR.Request({
            ajaxRequest: ajaxRequest2
          });
          rr.exec(ajaxRequest2.url);
        }
      });

      this.addPropertiesLoader({
        properties: ['rejectReasons'],
        loadFunction: function (terminalModel) {
          OB.info('[terminal] Loading... ' + this.properties);
          var me = this;
          new OB.DS.Request('org.openbravo.retail.posterminal.term.RejectReason').exec(null, function (data) {
            if (data && data.exception) {
              terminalModel.set(me.properties[0], []);
            } else {
              terminalModel.set(me.properties[0], data);
            }
            terminalModel.propertiesReady(me.properties);
          });
        }
      });

      this.get('dataSyncModels').push({
        name: 'Customer',
        model: OB.Model.ChangedBusinessPartners,
        className: 'org.openbravo.retail.posterminal.CustomerLoader',
        criteria: {},
        getIdentifier: function (model) {
          return JSON.parse(model.get('json'))._identifier;
        }
      });

      this.get('dataSyncModels').push({
        name: 'Customer Address',
        model: OB.Model.ChangedBPlocation,
        className: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
        criteria: {},
        getIdentifier: function (model) {
          return JSON.parse(model.get('json'))._identifier;
        }
      });

      this.get('dataSyncModels').push({
        name: 'Order',
        model: OB.Model.Order,
        className: 'org.openbravo.retail.posterminal.OrderLoader',
        timeout: 20000,
        timePerRecord: 1000,
        criteria: {
          hasbeenpaid: 'Y'
        },
        getIdentifier: function (model) {
          return model.documentNo;
        }
      });

      this.get('dataSyncModels').push({
        name: 'Cash Management',
        model: OB.Model.CashManagement,
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashMgmt',
        criteria: {
          'isbeingprocessed': 'N'
        },
        getIdentifier: function (model) {
          return model.get('type') + ': ' + model.get('user') + ' - ' + model.get('time');
        }
      });

      this.get('dataSyncModels').push({
        name: 'Cash Up',
        model: OB.Model.CashUp,
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashClose',
        timeout: 600000,
        timePerRecord: 10000,
        criteria: {},
        getIdentifier: function (model) {
          return model.creationDate;
        },
        changesPendingCriteria: {
          'isprocessed': 'Y'
        },
        postProcessingFunction: function (data, callback) {
          OB.UTIL.initCashUp(function () {
            var cashUpId = data.at(0).get('id');
            OB.Dal.find(OB.Model.CashUp, {
              id: cashUpId
            }, function (cU) {
              if (cU.length !== 0) {
                if (!cU.at(0).get('objToSend')) {
                  OB.UTIL.composeCashupInfo(data, null, function () {
                    OB.MobileApp.model.runSyncProcess();
                  });
                }
              }
            });
            // Get Cashup id, if objToSend is not filled compose and synchronize
            OB.UTIL.deleteCashUps(data);
            callback();
          });
        },
        // skip the syncing of the cashup if it is the same as the last one
        preSendModel: function (me, dataToSync) {
          if (dataToSync.length === 1 && this.model === OB.Model.CashUp && localStorage.lastCashupInfo === dataToSync.models[0].get('objToSend')) {
            localStorage.lastCashupInfo = dataToSync.models[0].get('objToSend');
            me.skipSyncModel = true;
          } else {
            localStorage.lastCashupInfo = dataToSync.models[0].get('objToSend');
          }
        }
      });

      this.on('ready', function () {
        OB.debug("next process: 'retail.pointofsale' window");

        var terminal = this.get('terminal');
        OB.UTIL.initCashUp(OB.UTIL.calculateCurrentCash);
        // Set Hardware..
        OB.POS.hwserver = new OB.DS.HWServer(terminal.hardwareurl, terminal.scaleurl);

        // Set Arithmetic properties:
        OB.DEC.setContext(OB.UTIL.getFirstValidValue([me.get('currency').obposPosprecision, me.get('currency').pricePrecision]), BigDecimal.prototype.ROUND_HALF_UP);

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
        OB.debug('runSyncProcess: executing pre synchronization hook');
        OB.UTIL.HookManager.executeHooks('OBPOS_PreSynchData', {}, function () {
          OB.debug('runSyncProcess: synchronize all models');
          OB.MobileApp.model.syncAllModels(function () {
            OB.info('runSyncProcess: synchronization successfully done');
            if (successCallback) {
              successCallback();
            }
          }, function () {
            OB.warn("runSyncProcess failed: the WebPOS is most likely to be offline, but a real error could be present.");
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
            OB.error("runSyncProcess", OB.I18N.getLabel('OBPOS_TerminalAuthError'));
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
      OB.debug("next process: trigger 'ready'");
      if (!this.get('terminal')) {
        OB.UTIL.Debug.execute(function () {
          // show an error while in debug mode to help debugging and testing
          throw "OB.MobileApp.model.get('terminal') properties have not been loaded";
        });
        OB.MobileApp.model.navigate('login');
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
              OB.error("renderMain", OB.I18N.getLabel('OBPOS_TerminalAuthError'));
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

      this.trigger('ready');

    },

    postLoginActions: function () {
      OB.debug("next process: renderTerminalMain");
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
              me.set('isLoggingIn', false);
              me.renderTerminalMain();
            }
          });
        } else {
          if (me.get('loggedUsingCache')) {
            me.set('loggedUsingCache', false);
            me.set('isLoggingIn', false);
            me.renderTerminalMain();
          }
        }
        loadModelsIncFunc = function () {
          OB.MobileApp.model.loadModels(null, true);
        };
        setInterval(loadModelsIncFunc, minIncRefresh);
      } else if (me.get('loggedUsingCache')) {
        me.set('loggedUsingCache', false);
        me.set('isLoggingIn', false);
        me.renderTerminalMain();
      }

      if (!this.sessionPing && this.get('terminal').sessionTimeout) {
        this.sessionPing = setInterval(function () {
          new OB.DS.Process('org.openbravo.mobile.core.login.ContextInformation').exec(null, function () {});
        }, this.get('terminal').sessionTimeout * 60 * 1000);
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
      OB.Dal.removeAll(OB.Model.Order, {
        'session': session.get('id'),
        'hasbeenpaid': 'N'
      }, function () {
        OB.MobileApp.model.triggerLogout();
      }, function () {
        OB.error("postCloseSession", arguments);
        OB.MobileApp.model.triggerLogout();
      });
      this.set('currentView', {
        name: 'order',
        params: null
      });
      localStorage.setItem('leftColumnCurrentView', JSON.stringify(this.get('currentView')));
    },

    // these variables will keep the minimum value that the document order could have
    // they feed from the local database, and the server
    documentnoThreshold: -1,
    quotationnoThreshold: -1,
    isSeqNoReadyEventSent: false,
    /**
     * Save the new values if are higher than the last known values
     * - the minimum sequence number can only grow
     */
    saveDocumentSequence: function (documentnoSuffix, quotationnoSuffix, callback, tx) {
      var me = this;

      if (me.restartingDocNo === true) {
        if (callback) {
          callback();
        }
        return;
      }

      // if the document sequence is trying to be initialized but it has already been initialized, do nothing
      if (documentnoSuffix === -1 && quotationnoSuffix === -1 && this.documentnoThreshold >= 0 && this.quotationnoThreshold >= 0) {
        if (callback) {
          callback();
        }
        return;
      }

      //If documentnoSuffix === 0 || quotationnoSuffix === 0, it means that we have restarted documentNo prefix, so we block this method while we save the new documentNo in localStorage
      if (documentnoSuffix === 0 || quotationnoSuffix === 0) {
        me.restartingDocNo = true;
      }

      // verify that the values are higher than the local variables
      if (documentnoSuffix > this.documentnoThreshold || documentnoSuffix === 0) {
        this.documentnoThreshold = documentnoSuffix;
      }
      if (quotationnoSuffix > this.quotationnoThreshold || quotationnoSuffix === 0) {
        this.quotationnoThreshold = quotationnoSuffix;
      }

      var processDocumentSequenceList = function (documentSequenceList) {

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

          // deprecation 27911 starts
          OB.MobileApp.model.set('documentsequence', me.getLastDocumentnoSuffixInOrderlist());
          OB.MobileApp.model.set('quotationDocumentSequence', me.getLastQuotationnoSuffixInOrderlist());
          if (!me.isSeqNoReadyEventSent) {
            me.isSeqNoReadyEventSent = true;
            me.trigger('seqNoReady');
          }
          // deprecation 27911 ends
          // update the database
          docSeq.set('documentSequence', me.documentnoThreshold);
          docSeq.set('quotationDocumentSequence', me.quotationnoThreshold);
          OB.Dal.saveInTransaction(tx, docSeq, function () {
            if (callback) {
              callback();
            }
            me.restartingDocNo = false;
          }, function () {
            me.restartingDocNo = false;
          });
          };

      // verify the database values
      OB.Dal.findInTransaction(tx, OB.Model.DocumentSequence, {
        'posSearchKey': this.get('terminal').searchKey
      }, processDocumentSequenceList, function () {
        me.restartingDocNo = false;
      });
    },

    /**
     * Updates the document sequence. This method should only be called when an order has been sent to the server
     * If the order is a quotation, only update the quotationno
     */
    updateDocumentSequenceWhenOrderSaved: function (documentnoSuffix, quotationnoSuffix, callback, tx) {
      if (quotationnoSuffix >= 0) {
        documentnoSuffix = -1;
      }
      this.saveDocumentSequence(documentnoSuffix, quotationnoSuffix, callback, tx);
    },

    // get the first document number available
    getLastDocumentnoSuffixInOrderlist: function () {
      var lastSuffix = null;
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 0) {
        var i = OB.MobileApp.model.orderList.models.length - 1;
        while (lastSuffix === null && i >= 0) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (!order.get('isPaid') && !order.get('isQuotation') && order.get('documentnoPrefix') === OB.MobileApp.model.get('terminal').docNoPrefix) {
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
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 0) {
        var i = OB.MobileApp.model.orderList.models.length - 1;
        while (lastSuffix === null && i >= 0) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (order.get('isQuotation') && order.get('quotationnoPrefix') === OB.MobileApp.model.get('terminal').quotationDocNoPrefix) {
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

    databaseCannotBeResetAction: function () {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ResetNeededNotSafeTitle'), OB.I18N.getLabel('OBPOS_ResetNeededNotSafeMessage'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        isConfirmButton: true,
        action: function () {
          OB.MobileApp.model.lock();
        }
      }], {
        onHideFunction: function () {
          OB.MobileApp.model.lock();
        }
      });
    },

    isUserCacheAvailable: function () {
      return true;
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
        //Save available servers and services and initialize Request Router layer
        if (inResponse.servers && inResponse.services) {
          localStorage.servers = JSON.stringify(inResponse.servers);
          localStorage.services = JSON.stringify(inResponse.services);
        }
        OB.RR.RequestRouter.initialize();

        me.setTerminalName(window.localStorage.getItem('terminalAuthentication') === 'Y' ? window.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));
        callback();
      }).error(function (inSender, inResponse) {
        callback();
      }).go(params);
    }
  });

  // from this point, OB.MobileApp.model will be available
  // the initialization is done to a dummy variable to allow the model to be extendable
  var initializeOBModelTerminal = new OB.Model.POSTerminal();

  OB.POS = {
    modelterminal: OB.MobileApp.model,
    // kept fot backward compatibility. Deprecation id: 27646
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

}());
var origTap = OB.UI.ProfileDialogApply.prototype.tap;
OB.UI.ProfileDialogApply.prototype.tap = _.wrap(OB.UI.ProfileDialogApply.prototype.tap, function (wrapped) {
  var tap = _.bind(origTap, this),
      widgetForm = this.owner.owner.$.bodyContent.$,
      newRoleId = widgetForm.roleList.getValue(),
      isDefault = widgetForm.defaultBox.checked,
      process = new OB.DS.Process('org.openbravo.retail.posterminal.Profile');
  if (!this.isActive) {
    return;
  }
  if (isDefault) {
    this.isActive = true;
    process.exec({
      role: newRoleId
    }, function (data) {
      if (data.success) {
        tap();
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorWhileSavingUser'));
      }
    }, function () {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
    });
  } else {
    tap();
  }
});