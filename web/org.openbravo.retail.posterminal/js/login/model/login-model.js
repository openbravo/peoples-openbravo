/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo, BigDecimal, localStorage, setTimeout */

(function () {
  // initialize the WebPOS terminal model that extends the core terminal model. after this, OB.MobileApp.model will be available
  OB.Model.POSTerminal = OB.Model.Terminal.extend({

    setTerminalName: function (terminalName) {
      this.set('terminalName', terminalName);
      this.set('loginUtilsParams', {
        terminalName: terminalName
      });
      // set the terminal only if it was empty. this variable is used to detect if the terminal changed
      if (terminalName && !OB.UTIL.localStorage.getItem('terminalName')) {
        OB.UTIL.localStorage.setItem('terminalName', terminalName);
      }
    },

    initialize: function () {
      var me = this;

      me.set({
        appName: 'WebPOS',
        appModuleId: 'FF808181326CC34901326D53DBCF0018',
        appModulePrefix: 'OBPOS',
        supportsOffline: true,
        profileHandlerUrl: 'org.openbravo.retail.posterminal.ProfileUtilsServlet',
        loginUtilsUrl: '../../org.openbravo.retail.posterminal.service.loginutils',
        loginHandlerUrl: '../../org.openbravo.retail.posterminal/POSLoginHandler',
        applicationFormatUrl: '../../org.openbravo.mobile.core/OBPOS_Main/ApplicationFormats',
        logoutUrlParams: OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? {} : {
          terminal: OB.UTIL.getParameterByName("terminal")
        },
        logConfiguration: {
          deviceIdentifier: OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
          logPropertiesExtension: [

          function () {
            return {
              isOnline: OB.MobileApp.model.get('connectedToERP')
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
          displayName: OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase.displayName
        },
        logDBTrxThreshold: 300,
        logDBStmtThreshold: 1000,
        shouldExecuteBenchmark: true
      });

      me.setTerminalName(OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));

      OB.UTIL.HookManager.registerHook('OBMOBC_InitActions', function (args, c) {
        me.initActions(function () {
          me.setTerminalName(OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));
          me.set('logConfiguration', {
            deviceIdentifier: OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
            logPropertiesExtension: [

            function () {
              return {
                isOnline: OB.MobileApp.model.get('connectedToERP')
              };
            }]
          });
          args.cancelOperation = true;
          OB.UTIL.HookManager.callbackExecutor(args, c);
        });
      });

      this.addPropertiesLoader({
        properties: ['terminal'],
        loadFunction: function (terminalModel) {
          OB.info('[terminal] Loading... ' + this.properties);
          var me = this,
              max, i, handleError;
          var params = {};
          var currentDate = new Date();
          params.terminalTime = currentDate;
          params.terminalTimeOffset = currentDate.getTimezoneOffset();

          OB.DS.commonParams = OB.DS.commonParams || {};
          OB.DS.commonParams.terminalName = terminalModel.get('terminalName');

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
                  OB.UTIL.localStorage.removeItem('cacheAvailableForUser:' + OB.MobileApp.model.get('orgUserId'));
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
              var showTerminalModalError = function (errorMessage) {
                  OB.UTIL.showConfirmation.display('Error', OB.I18N.getLabel('OBPOS_errorLoadingTerminal') + ' ' + errorMessage, [{
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
                  };

              // load the OB.MobileApp.model              
              for (i = 0, max = data.length; i < max; i++) {
                if (Object.keys(data[i])[0] === "businesspartner") {
                  terminalModel.set(Object.keys(data[i])[0], data[i][Object.keys(data[i])[0]].id);
                } else if (Object.keys(data[i])[0] === "pricelist" && _.isNull(data[i][Object.keys(data[i])[0]])) {
                  showTerminalModalError(OB.I18N.getLabel('OBPOS_NoPriceList'));
                  return;
                } else {
                  terminalModel.set(Object.keys(data[i])[0], data[i][Object.keys(data[i])[0]]);
                }
              }

              OB.DS.commonParams = {
                client: terminalModel.get('terminal').client,
                organization: terminalModel.get('terminal').organization,
                pos: terminalModel.get('terminal').id,
                terminalName: terminalModel.get('terminalName')
              };

              // update the local database with the document sequence received
              OB.MobileApp.model.saveDocumentSequence(OB.MobileApp.model.get('terminal').lastDocumentNumber, OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber, OB.MobileApp.model.get('terminal').lastReturnDocumentNumber, function () {
                if (OB.MobileApp.model.orderList) {
                  OB.MobileApp.model.orderList.synchronizeCurrentOrder();
                }
              });

              OB.UTIL.localStorage.setItem('terminalId', data[0].terminal.id);
              terminalModel.set('useBarcode', terminalModel.get('terminal').terminalType.usebarcodescanner);
              terminalModel.set('useEmbededBarcode', terminalModel.get('terminal').terminalType.useembededbarcodescanner);
              if (!terminalModel.usermodel) {
                OB.MobileApp.model.loadingErrorsActions("The terminal.usermodel should be loaded at this point");
              } else if (OB.MobileApp.model.attributes.loadManifeststatus && OB.MobileApp.model.attributes.loadManifeststatus.type === 'error' && !OB.RR.RequestRouter.ignoreManifestLoadError()) {
                var error = OB.MobileApp.model.attributes.loadManifeststatus;
                OB.debug(error.reason + ' failed to load: ' + error.url);
                OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_TitleFailedAppCache'), enyo.format("%s %s: %s", OB.I18N.getLabel('OBPOS_FailedAppCache'), error.type, error.message), [{
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true
                }], {
                  autoDismiss: false,
                  onHideFunction: function (popup) {
                    OB.UTIL.showLoading(true);
                    terminalModel.set('terminalCorrectlyLoadedFromBackend', true);
                    terminalModel.propertiesReady(me.properties);
                  }
                });
              } else {
                terminalModel.set('terminalCorrectlyLoadedFromBackend', true);
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
          }, true, 5000);
        }
      });

      this.get('dataSyncModels').push({
        name: 'Customer',
        model: OB.Model.ChangedBusinessPartners,
        modelFunc: 'OB.Model.ChangedBusinessPartners',
        className: 'org.openbravo.retail.posterminal.CustomerLoader',
        criteria: {},
        getIdentifier: function (model) {
          return model.businessPartnerCategory_name + ' : ' + model._identifier;
        }
      });

      this.get('dataSyncModels').push({
        name: 'Customer Address',
        model: OB.Model.ChangedBPlocation,
        modelFunc: 'OB.Model.ChangedBPlocation',
        className: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
        criteria: {},
        getIdentifier: function (model) {
          return model.customerName + ' : ' + model.name;
        }
      });

      this.get('dataSyncModels').push({
        name: 'Order',
        model: OB.Model.Order,
        modelFunc: 'OB.Model.Order',
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
        name: 'Cancel Layaway',
        model: OB.Model.CancelLayaway,
        modelFunc: 'OB.Model.CancelLayaway',
        className: 'org.openbravo.retail.posterminal.CancelLayawayLoader',
        criteria: {},
        isPersistent: false
      });

      this.get('dataSyncModels').push({
        name: 'Cash Management',
        model: OB.Model.CashManagement,
        modelFunc: 'OB.Model.CashManagement',
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashMgmt',
        criteria: {
          'isbeingprocessed': 'N'
        },
        getIdentifier: function (model) {
          return model.type + ': ' + model.user + ' - ' + OB.I18N.formatDateISO(new Date(model.creationDate));
        }
      });

      this.get('dataSyncModels').push({
        name: 'Cash Up',
        model: OB.Model.CashUp,
        modelFunc: 'OB.Model.CashUp',
        isPersistent: true,
        className: 'org.openbravo.retail.posterminal.ProcessCashClose',
        timeout: 600000,
        timePerRecord: 10000,
        getIdentifier: function (model) {
          return OB.I18N.formatDateISO(new Date(model.creationDate));
        },
        getCriteria: function () {
          if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal')) || (OB.MobileApp.model.get('terminal') && (OB.MobileApp.model.get('terminal').ismaster || OB.MobileApp.model.get('terminal').isslave))) {
            return {};
          } else {
            return {
              isprocessed: 'Y'
            };
          }
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
            OB.UTIL.calculateCurrentCash();
            callback();
          }, null, true);
        },
        // skip the syncing of the cashup if it is the same as the last one
        preSendModel: function (me, dataToSync) {
          if (dataToSync.length === 0) {
            return;
          } else {
            if (dataToSync.length === 1 && this.model === OB.Model.CashUp && !OB.UTIL.isNullOrUndefined(OB.UTIL.localStorage.getItem('lastCashupSendInfo')) && OB.UTIL.localStorage.getItem('lastCashupSendInfo') === dataToSync.models[0].get('objToSend')) {
              me.skipSyncModel = true;
            }
            OB.UTIL.localStorage.setItem('lastCashupSendInfo', dataToSync.models[0].get('objToSend'));
          }
        },
        // keep track of successfull send
        successSendModel: function () {
          OB.UTIL.localStorage.setItem('lastCashupInfo', OB.UTIL.localStorage.getItem('lastCashupSendInfo'));
        }
      });

      this.on('ready', function () {
        OB.debug("next process: 'retail.pointofsale' window");
        if (this.get('terminal').currencyFormat) {
          OB.Format.formats.priceInform = this.get('terminal').currencyFormat;
        }
        // register models which are cached during synchronized transactions
        OB.MobileApp.model.addSyncCheckpointModel(OB.Model.Order);
        OB.MobileApp.model.addSyncCheckpointModel(OB.Model.PaymentMethodCashUp);
        OB.MobileApp.model.addSyncCheckpointModel(OB.Model.TaxCashUp);
        OB.MobileApp.model.addSyncCheckpointModel(OB.Model.CashUp);

        var terminal = this.get('terminal');
        OB.UTIL.initCashUp(function () {
          OB.UTIL.calculateCurrentCash(function () {
            OB.UTIL.HookManager.executeHooks('OBPOS_LoadPOSWindow', {}, function () {
              var nextWindow = OB.MobileApp.model.get('nextWindow');
              if (nextWindow) {
                OB.POS.navigate(nextWindow);
                OB.MobileApp.model.unset('nextWindow');
              } else {
                OB.POS.navigate(OB.MobileApp.model.get('defaultWindow'));
              }
            });
          }, null);
        }, function () {
          //There was an error when retrieving the cashup from the backend.
          // This means that there is a cashup saved as an error, and we don't have
          //the necessary information to have a working cashup in the client side.
          //We therefore need to logout
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashupErrors'), OB.I18N.getLabel('OBPOS_CashupErrorsMsg'), [{
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

        });
        // Set Hardware..
        OB.POS.hwserver = new OB.DS.HWServer(this.get('hardwareURL'), terminal.hardwareurl, terminal.scaleurl);

        OB.MobileApp.model.on('change:currentWindowState', function (model) {
          // If the hardware URL is set and the terminal uses RFID
          if (model.get('currentWindowState') === 'renderUI' && OB.UTIL.isNullOrUndefined(OB.UTIL.RfidController.get('rfidWebsocket')) && OB.UTIL.RfidController.isRfidConfigured()) {
            var protocol = OB.POS.hwserver.url.split('/')[0];
            var websocketServerLocation;
            if (protocol === 'http:') {
              websocketServerLocation = 'ws:' + OB.POS.hwserver.url.substring(protocol.length, OB.POS.hwserver.url.length).split('/printer')[0] + '/rfid';
              OB.UTIL.RfidController.set('isRFIDEnabled', true);
              OB.UTIL.RfidController.startRfidWebsocket(websocketServerLocation, 2000, 0, 5);
            } else if (protocol === 'https:') {
              websocketServerLocation = 'wss:' + OB.POS.hwserver.url.substring(protocol.length, OB.POS.hwserver.url.length).split('/printer')[0] + '/rfid';
              OB.UTIL.RfidController.set('isRFIDEnabled', true);
              OB.UTIL.RfidController.startRfidWebsocket(websocketServerLocation, 2000, 0, 5);
            } else {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_WrongHardwareManagerProtocol'));
            }
          }
        });

        OB.MobileApp.view.scanningFocus(false);

        // Set Arithmetic properties:
        OB.DEC.setContext(OB.UTIL.getFirstValidValue([me.get('currency').obposPosprecision, me.get('currency').pricePrecision]), BigDecimal.prototype.ROUND_HALF_UP);

        if (me.get('loggedOffline') === true) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineLogin'));
        }
      });

      OB.Model.Terminal.prototype.initialize.call(me);

    },

    addToListOfCallbacks: function (successCallback, errorCallback) {
      if (OB.UTIL.isNullOrUndefined(this.get('syncProcessCallbacks'))) {
        this.set('syncProcessCallbacks', []);
      }
      if (!OB.UTIL.isNullOrUndefined(successCallback)) {
        var list = this.get('syncProcessCallbacks');
        list.push({
          success: successCallback,
          error: errorCallback
        });
      }
    },

    runSyncProcess: function (successCallback, errorCallback) {
      var executeCallbacks, me = this;
      if (this.pendingSyncProcess) {
        this.addToListOfCallbacks(successCallback, errorCallback);
        return;
      }
      this.pendingSyncProcess = true;
      this.addToListOfCallbacks(successCallback, errorCallback);
      executeCallbacks = function (success, listOfCallbacks, callback) {
        if (listOfCallbacks.length === 0) {
          callback();
          listOfCallbacks = null;
          return;
        }
        var callbackToExe = listOfCallbacks.shift();
        if (success && callbackToExe.success) {
          callbackToExe.success();
        } else if (!success && callbackToExe.error) {
          callbackToExe.error();
        }
        executeCallbacks(success, listOfCallbacks, callback);
      };

      function run() {
        OB.debug('runSyncProcess: executing pre synchronization hook');
        OB.UTIL.HookManager.executeHooks('OBPOS_PreSynchData', {}, function () {
          OB.debug('runSyncProcess: synchronize all models');
          OB.MobileApp.model.syncAllModels(function () {
            OB.info('runSyncProcess: synchronization successfully done');
            executeCallbacks(true, me.get('syncProcessCallbacks'), function () {
              me.pendingSyncProcess = false;
            });
          }, function () {
            OB.warn("runSyncProcess failed: the WebPOS is most likely to be offline, but a real error could be present.");
            executeCallbacks(false, me.get('syncProcessCallbacks'), function () {
              me.pendingSyncProcess = false;
            });
          });
        });
      }
      if (OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y') {
        var process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckTerminalAuth');

        OB.trace('Checking authentication');

        process.exec({
          terminalName: OB.UTIL.localStorage.getItem('terminalName'),
          terminalKeyIdentifier: OB.UTIL.localStorage.getItem('terminalKeyIdentifier'),
          terminalAuthentication: OB.UTIL.localStorage.getItem('terminalAuthentication'),
          cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId')
        }, function (data) {
          if (data && data.exception) {
            //ERROR or no connection
            if (!OB.UTIL.isNullOrUndefined(OB.UTIL.localStorage.getItem('cacheSessionId'))) {
              OB.error("runSyncProcess", OB.I18N.getLabel('OBPOS_TerminalAuthError'));
              run();
            } else {
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
          } else if (data && (data.isLinked === false || data.terminalAuthentication)) {
            if (data.isLinked === false) {
              OB.info('POS Terminal configuration is not linked to a device anymore. Remove terminalKeyIdentifier from localStorage');
              OB.UTIL.localStorage.removeItem('terminalName');
              OB.UTIL.localStorage.removeItem('terminalKeyIdentifier');
            }
            if (data.terminalAuthentication) {
              OB.UTIL.localStorage.setItem('terminalAuthentication', data.terminalAuthentication);
            }
            if (data && data.errorReadingTerminalAuthentication) {
              OB.UTIL.showWarning(data.errorReadingTerminalAuthentication);
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
      this.postSyncProcessActions();
    },

    postSyncProcessActions: function () {
      if (OB.MobileApp.model.get('context').user && _.isUndefined(OB.MobileApp.model.get('context').user.isSalesRepresentative)) {
        OB.Dal.get(OB.Model.SalesRepresentative, OB.MobileApp.model.usermodel.get('id'), function (salesrepresentative) {
          if (!salesrepresentative) {
            OB.MobileApp.model.get('context').user.isSalesRepresentative = false;
          } else {
            OB.MobileApp.model.get('context').user.isSalesRepresentative = true;
          }
        }, function () {}, function () {
          OB.MobileApp.model.get('context').user.isSalesRepresentative = false;
        });
      }
    },

    returnToOnline: function () {
      if (OB.MobileApp.model.get('isLoggingIn')) {
        OB.MobileApp.model.on('change:isLoggingIn', function () {
          if (!OB.MobileApp.model.get('isLoggingIn')) {
            OB.MobileApp.model.off('change:isLoggingIn', null, this);
            this.runSyncProcess();
          }
        }, this);
      } else {
        //The session is fine, we don't need to warn the user
        //but we will attempt to send all pending orders automatically
        this.runSyncProcess();
      }
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
        if (!paymentlegacy && this.get('payments')[i].payment.searchKey === 'OBPOS_payment.cash') {
          paymentlegacy = this.get('payments')[i].payment.searchKey;
        }
        if (this.get('payments')[i].paymentMethod.iscash) {
          if (!paymentcash) {
            paymentcash = this.get('payments')[i].payment.searchKey;
          }
          if (this.get('payments')[i].paymentMethod.currency === this.get('terminal').currency) {
            if (!paymentcashcurrency) {
              paymentcashcurrency = this.get('payments')[i].payment.searchKey;
            }
            if (!defaultpaymentcashcurrency && this.get('payments')[i].paymentMethod.defaultCashPaymentMethod) {
              defaultpaymentcashcurrency = this.get('payments')[i].payment.searchKey;
            }
          }
          if (!defaultpaymentcash && this.get('payments')[i].paymentMethod.defaultCashPaymentMethod) {
            defaultpaymentcash = this.get('payments')[i].payment.searchKey;
          }
        }
      }
      // sets the default payment method
      this.set('paymentcash', defaultpaymentcashcurrency || defaultpaymentcash || paymentcashcurrency || paymentcash || paymentlegacy);

      // set if there's or not any payment method that is counted in cashup
      if (_.find(this.get('payments'), function (payment) {
        return payment.paymentMethod.countpaymentincashup;
      })) {
        this.set('hasPaymentsForCashup', true);
      } else {
        this.set('hasPaymentsForCashup', false);
      }

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
        // Send the last update of full and masterdata refresh
        OB.UTIL.sendLastTerminalStatusValues();
        // Check terminal authentication
        if (OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y') {
          var process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckTerminalAuth');
          process.exec({
            terminalName: OB.UTIL.localStorage.getItem('terminalName'),
            terminalKeyIdentifier: OB.UTIL.localStorage.getItem('terminalKeyIdentifier'),
            terminalAuthentication: OB.UTIL.localStorage.getItem('terminalAuthentication'),
            cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId')
          }, function (data) {
            if (data && data.exception) {
              //ERROR or no connection
              OB.error("renderMain", OB.I18N.getLabel('OBPOS_TerminalAuthError'));
            } else if (data && (data.isLinked === false || data.terminalAuthentication)) {
              if (data.isLinked === false) {
                OB.info('POS Terminal configuration is not linked to a device anymore. Remove terminalKeyIdentifier from localStorage');
                OB.UTIL.localStorage.removeItem('terminalName');
                OB.UTIL.localStorage.removeItem('terminalKeyIdentifier');
              }
              if (data.terminalAuthentication) {
                OB.UTIL.localStorage.setItem('terminalAuthentication', data.terminalAuthentication);
              }
              if (data && data.errorReadingTerminalAuthentication) {
                OB.UTIL.showWarning(data.errorReadingTerminalAuthentication);
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
        if (OB.UTIL.localStorage.getItem('synchronizedMessageId') && !this.checkProcessingMessageLocked) {
          this.checkProcessingMessageLocked = true;
          var me = this;
          if (!me.showSynchronizedDialog) {
            me.showSynchronizingDialog("CheckProcessingMessage");
          }
          var prcss = new OB.DS.Process('org.openbravo.retail.posterminal.CheckProcessingMessage');
          var checkProcessingMessage;
          checkProcessingMessage = function (counter) {
            prcss.exec({
              messageId: OB.UTIL.localStorage.getItem('synchronizedMessageId')
            }, function (data) {
              if (data && data.exception) {
                //ERROR or no connection
                OB.error("renderMain", data.exception.message);
              } else if (data) {
                if (data.status === "Processed") {
                  var orderId = JSON.parse(data.json).data[0].data[0].id;
                  var callback = function () {
                      me.checkProcessingMessageLocked = false;
                      OB.UTIL.localStorage.removeItem('synchronizedMessageId');
                      OB.UTIL.showLoading(false);
                      if (OB.MobileApp.model.showSynchronizedDialog) {
                        OB.MobileApp.model.hideSynchronizingDialog("CheckProcessingMessage");
                      }
                      };
                  OB.MobileApp.model.orderList.loadById(orderId);
                  if (orderId === OB.MobileApp.model.orderList.current.id) {
                    OB.UTIL.rebuildCashupFromServer(function () {
                      OB.MobileApp.model.orderList.saveCurrent();
                      OB.Dal.remove(OB.MobileApp.model.orderList.current, null, null);
                      OB.MobileApp.model.orderList.deleteCurrent();
                    });
                  }
                  callback();
                } else if (data.status === "Initial") {
                  //recursively check process status 
                  setTimeout(function () {
                    counter++;
                    OB.UTIL.showConfirmation.setText(OB.I18N.getLabel('OBMOBC_DataIsBeingProcessed') + " " + OB.I18N.getLabel('OBMOBC_NumOfRetries') + " " + counter);
                    checkProcessingMessage(counter);
                  }, 15000);

                } else if (data.status === "Error") {
                  //Show modal advising that there was an error
                  me.checkProcessingMessageLocked = false;
                  OB.UTIL.localStorage.removeItem('synchronizedMessageId');
                  if (OB.MobileApp.model.showSynchronizedDialog) {
                    OB.MobileApp.model.hideSynchronizingDialog("CheckProcessingMessage");
                  }
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_TransactionFailedTitle'), OB.I18N.getLabel('OBMOBC_TransactionFailed', [data.errorMessage]), [{
                    isConfirmButton: true,
                    label: OB.I18N.getLabel('OBMOBC_LblOk'),
                    action: function () {
                      if (OB && OB.POS) {
                        OB.POS.navigate('retail.pointofsale');
                        return true;
                      }
                    }
                  }]);
                } else {
                  me.checkProcessingMessageLocked = false;
                  OB.UTIL.localStorage.removeItem('synchronizedMessageId');
                  OB.UTIL.showLoading(false);
                  if (OB.MobileApp.model.showSynchronizedDialog) {
                    OB.MobileApp.model.hideSynchronizingDialog("CheckProcessingMessage");
                  }
                }
              }
            }, function (data) {
              //Continue retrying till we know the status of the message
              counter++;
              setTimeout(function () {
                OB.UTIL.showConfirmation.setText(OB.I18N.getLabel('OBMOBC_DataIsBeingProcessed') + " " + OB.I18N.getLabel('OBMOBC_NumOfRetries') + " " + counter);
                checkProcessingMessage(counter);
              }, 15000);

            }, undefined, 15000);
          };
          checkProcessingMessage(0);
        }
      });
      this.trigger('ready');
    },

    postLoginActions: function () {
      OB.debug("next process: renderTerminalMain");
      var loadModelsIncFunc;
      //MASTER DATA REFRESH
      var minIncRefresh = this.get('terminal').terminalType.minutestorefreshdatainc,
          minTotalRefresh = this.get('terminal').terminalType.minutestorefreshdatatotal * 60 * 1000,
          lastTotalRefresh = OB.UTIL.localStorage.getItem('POSLastTotalRefresh'),
          lastIncRefresh = OB.UTIL.localStorage.getItem('POSLastIncRefresh'),
          now = new Date().getTime(),
          intervalInc;

      // lastTotalRefresh should be used to set lastIncRefresh when it is null or minor.
      if (lastIncRefresh === null) {
        lastIncRefresh = lastTotalRefresh;
      } else {
        if (lastTotalRefresh > lastIncRefresh) {
          lastIncRefresh = lastTotalRefresh;
        }
      }
      intervalInc = lastIncRefresh ? (now - lastIncRefresh - minIncRefresh) : 0;
      minIncRefresh = (minIncRefresh > 99999 ? 99999 : minIncRefresh) * 60 * 1000;

      function setTerminalLockTimeout(sessionTimeoutMinutes, sessionTimeoutMilliseconds) {
        OB.debug("Terminal lock timer reset (" + sessionTimeoutMinutes + " minutes)");
        clearTimeout(this.timeoutId);
        this.timeoutId = setTimeout(function () {
          OB.warn("The terminal was not used for " + sessionTimeoutMinutes + " minutes. Locking the terminal");
          OB.MobileApp.model.lock();
        }, sessionTimeoutMilliseconds);
      }

      OB.POS.hwserver.showSelected(); // Show the selected printers
      if (minIncRefresh) {
        loadModelsIncFunc = function () {
          OB.MobileApp.model.set('secondsToRefreshMasterdata', 3);
          var counterIntervalId = null;
          counterIntervalId = setInterval(function () {
            OB.MobileApp.model.set('secondsToRefreshMasterdata', OB.MobileApp.model.get('secondsToRefreshMasterdata') - 1);
            if (OB.MobileApp.model.get('secondsToRefreshMasterdata') === 0) {
              clearInterval(counterIntervalId);

              OB.UTIL.startLoadingSteps();
              OB.MobileApp.model.set('isLoggingIn', true);
              OB.UTIL.showLoading(true);
              OB.MobileApp.model.on('incrementalModelsLoaded', function () {
                OB.MobileApp.model.off('incrementalModelsLoaded');
                OB.UTIL.showLoading(false);
                OB.MobileApp.model.set('isLoggingIn', false);
              });

              OB.MobileApp.model.loadModels(null, true);
            }
          }, 1000);

          OB.MobileApp.view.$.dialogsContainer.createComponent({
            kind: 'OB.UI.ModalAction',
            header: OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshed'),
            bodyContent: {
              content: OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshedMessage', [OB.MobileApp.model.get('secondsToRefreshMasterdata')])
            },
            bodyButtons: {
              kind: 'OB.UI.ModalDialogButton',
              content: OB.I18N.getLabel('OBMOBC_LblCancel'),
              tap: function () {
                OB.MobileApp.model.off('change:secondsToRefreshMasterdata');
                clearInterval(counterIntervalId);
                this.doHideThisPopup();
              }
            },
            autoDismiss: false,
            hideCloseButton: true,
            executeOnShow: function () {
              var reloadPopup = this;
              OB.MobileApp.model.on('change:secondsToRefreshMasterdata', function () {
                reloadPopup.$.bodyContent.$.control.setContent(OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshedMessage', [OB.MobileApp.model.get('secondsToRefreshMasterdata')]));
                if (OB.MobileApp.model.get('secondsToRefreshMasterdata') === 0) {
                  reloadPopup.hide();
                  OB.MobileApp.model.off('change:secondsToRefreshMasterdata');
                }
              });
            }
          }).show();

        };
        // in case there was no incremental load at login then schedule an incremental
        // load at the next expected time, which can be earlier than the standard interval
        if (intervalInc < 0 && OB.MobileApp.model.hasPermission('OBMOBC_NotAutoLoadIncrementalAtLogin', true)) {
          setTimeout(function () {
            loadModelsIncFunc();
            setInterval(loadModelsIncFunc, minIncRefresh);
          }, intervalInc * -1);
        } else {
          setInterval(loadModelsIncFunc, minIncRefresh);
        }
      }

      var sessionTimeoutMinutes = this.get('terminal').sessionTimeout;
      var serverPingMinutes = this.get('serverTimeout');
      if (!this.sessionPing && sessionTimeoutMinutes) {
        var sessionTimeoutMilliseconds = sessionTimeoutMinutes * 60 * 1000;
        this.sessionPing = setInterval(function () {
          new OB.DS.Process('org.openbravo.mobile.core.login.ContextInformation').exec(null, function () {});
        }, sessionTimeoutMilliseconds);

        // set the terminal lock timeout
        setTerminalLockTimeout(sessionTimeoutMinutes, sessionTimeoutMilliseconds);
        // FIXME: hack: inject javascript in the enyo.gesture.down so we can create a terminal timeout
        enyo.gesture.down = function (inEvent) {
          // start of Openbravo injected code
          setTerminalLockTimeout(sessionTimeoutMinutes, sessionTimeoutMilliseconds);
          // end of Openbravo injected code
          // cancel any hold since it's possible in corner cases to get a down without an up
          var e = this.makeEvent("down", inEvent);
          enyo.dispatch(e);
          this.downEvent = e;
        };
      } else if (!this.sessionPing && !OB.MobileApp.model.get('permissions').OBPOS_SessionExpiration) {
        var serverPingMilliseconds = serverPingMinutes * 60 * 1000;
        if (serverPingMinutes === 0) {
          return;
        } else if (serverPingMinutes === 1) {
          serverPingMilliseconds -= 30 * 1000;
        } else {
          serverPingMilliseconds -= 60 * 1000;
        }
        this.sessionPing = setInterval(function () {
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
            success: function (inSender, inResponse) {},
            fail: function (inSender, inResponse) {}
          });
          rr = new OB.RR.Request({
            ajaxRequest: ajaxRequest2
          });
          rr.exec(ajaxRequest2.url);
        }, serverPingMilliseconds);
      }
    },

    cleanSessionInfo: function () {
      this.cleanTerminalData();
    },

    preLoginActions: function () {
      this.cleanSessionInfo();
    },

    preLogoutActions: function (finalCallback) {
      var criteria = {},
          model;
      var me = this;

      function removeOneModel(model, collection, callback) {
        if (collection.length === 0) {
          if (callback && callback instanceof Function) {
            me.cleanSessionInfo();
            callback();
          }
          return;
        }

        model.deleteOrder(me, function () {
          collection.remove(model);
          removeOneModel(collection.at(0), collection, callback);
        });
      }

      function success(collection) {
        var i, j, removeCallback;
        if (collection.length > 0) {
          _.each(collection.models, function (model) {
            model.set('ignoreCheckIfIsActiveOrder', true);
          });
          removeOneModel(collection.at(0), collection, finalCallback);
        } else {
          if (finalCallback && finalCallback instanceof Function) {
            me.cleanSessionInfo();
            finalCallback();
          }
        }
      }

      function error() {
        OB.error("postCloseSession", arguments);
        OB.MobileApp.model.triggerLogout();
      }

      if (OB.MobileApp.model.get('isMultiOrderState')) {
        if (OB.MobileApp.model.multiOrders.checkMultiOrderPayment()) {
          return;
        }
      }
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 1) {
        if (OB.MobileApp.model.orderList.checkOrderListPayment()) {
          return;
        }
      } else if (OB.MobileApp.model.receipt && OB.MobileApp.model.receipt.get('lines').length > 0) {
        if (OB.MobileApp.model.receipt.checkOrderPayment()) {
          return;
        }
      }

      OB.UTIL.Approval.requestApproval(
      this, 'OBPOS_approval.removereceipts', function (approved, supervisor, approvalType) {
        if (approved) {
          //All pending to be paid orders will be removed on logout
          criteria.session = OB.MobileApp.model.get('session');
          criteria.hasbeenpaid = 'N';
          OB.Dal.find(OB.Model.Order, criteria, success, error);
        }
      });
    },

    postCloseSession: function (session) {
      var callback = function () {
          OB.MobileApp.model.triggerLogout();
          };
      OB.UTIL.localStorage.removeItem('leftColumnCurrentView');
      if (OB.POS.hwserver !== undefined) {
        OB.POS.hwserver.print(new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.GoodByeTemplate), {}, function () {
          callback();
        }, OB.DS.HWServer.DISPLAY);
      } else {
        callback();
      }
    },

    // these variables will keep the minimum value that the document order could have
    // they feed from the local database, and the server
    documentnoThreshold: -1,
    quotationnoThreshold: -1,
    returnnoThreshold: -1,
    isSeqNoReadyEventSent: false,
    /**
     * Save the new values if are higher than the last known values
     * - the minimum sequence number can only grow
     */
    saveDocumentSequence: function (documentnoSuffix, quotationnoSuffix, returnnoSuffix, callback, tx) {
      var me = this;

      if (me.restartingDocNo === true) {
        if (callback) {
          callback();
        }
        return;
      }

      // if the document sequence is trying to be initialized but it has already been initialized, do nothing
      if (documentnoSuffix === -1 && quotationnoSuffix === -1 && returnnoSuffix === -1 && this.documentnoThreshold >= 0 && this.quotationnoThreshold >= 0 && this.returnnoThreshold >= 0) {
        if (callback) {
          callback();
        }
        return;
      }

      //If documentnoSuffix === 0 || quotationnoSuffix === 0 || returnnoSuffix === 0, it means that we have restarted documentNo prefix, so we block this method while we save the new documentNo in localStorage
      if (documentnoSuffix === 0 || quotationnoSuffix === 0 || returnnoSuffix === 0) {
        me.restartingDocNo = true;
      }

      // verify that the values are higher than the local variables
      if (documentnoSuffix > this.documentnoThreshold || documentnoSuffix === 0) {
        this.documentnoThreshold = documentnoSuffix;
      }
      if (quotationnoSuffix > this.quotationnoThreshold || quotationnoSuffix === 0) {
        this.quotationnoThreshold = quotationnoSuffix;
      }
      if (returnnoSuffix > this.returnnoThreshold || returnnoSuffix === 0) {
        this.returnnoThreshold = returnnoSuffix;
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
            if (docSeq.get('returnDocumentSequence') > me.returnnoThreshold && returnnoSuffix !== 0) {
              me.returnnoThreshold = docSeq.get('returnDocumentSequence');
            }
          } else {
            // There is not a document sequence for the pos, create it
            docSeq = new OB.Model.DocumentSequence();
            docSeq.set('posSearchKey', me.get('terminal').searchKey);
          }

          // deprecation 27911 starts
          OB.MobileApp.model.set('documentsequence', me.getLastDocumentnoSuffixInOrderlist());
          OB.MobileApp.model.set('quotationDocumentSequence', me.getLastQuotationnoSuffixInOrderlist());
          OB.MobileApp.model.set('returnDocumentSequence', me.getLastReturnnoSuffixInOrderlist());
          if (!me.isSeqNoReadyEventSent) {
            me.isSeqNoReadyEventSent = true;
            me.trigger('seqNoReady');
          }
          // deprecation 27911 ends
          // update the database
          docSeq.set('documentSequence', me.documentnoThreshold);
          docSeq.set('quotationDocumentSequence', me.quotationnoThreshold);
          docSeq.set('returnDocumentSequence', me.returnnoThreshold);
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
     * If the order is a return, only update the returnno
     */
    updateDocumentSequenceWhenOrderSaved: function (documentnoSuffix, quotationnoSuffix, returnnoSuffix, callback, tx) {
      if (quotationnoSuffix >= 0) {
        documentnoSuffix = -1;
        returnnoSuffix = -1;
      } else if (returnnoSuffix >= 0) {
        documentnoSuffix = -1;
        quotationnoSuffix = -1;
      }
      this.saveDocumentSequence(documentnoSuffix, quotationnoSuffix, returnnoSuffix, callback, tx);
    },

    // get the first document number available
    getLastDocumentnoSuffixInOrderlist: function () {
      var lastSuffix = null;
      var i;
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 0) {
        for (i = 0; i < OB.MobileApp.model.orderList.length; i++) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (!order.get('isPaid') && !order.get('isQuotation') && order.get('documentnoPrefix') === OB.MobileApp.model.get('terminal').docNoPrefix) {
            if (OB.UTIL.isNullOrUndefined(lastSuffix) || (lastSuffix && order.get('documentnoSuffix') > lastSuffix)) {
              lastSuffix = order.get('documentnoSuffix');
            }
          }
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
      var i;
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 0) {
        for (i = 0; i < OB.MobileApp.model.orderList.length; i++) {
          var order = OB.MobileApp.model.orderList.models[i];
          if (order.get('isQuotation') && order.get('quotationnoPrefix') === OB.MobileApp.model.get('terminal').quotationDocNoPrefix) {
            if (OB.UTIL.isNullOrUndefined(lastSuffix) || (lastSuffix && order.get('quotationnoSuffix') > lastSuffix)) {
              lastSuffix = order.get('quotationnoSuffix');
            }
          }
        }
      }
      if (lastSuffix === null || lastSuffix < this.quotationnoThreshold) {
        lastSuffix = this.quotationnoThreshold;
      }
      return lastSuffix;
    },
    // get the first return number available
    getLastReturnnoSuffixInOrderlist: function () {
      var lastSuffix = null;
      var i;
      if (OB.MobileApp.model.orderList && OB.MobileApp.model.orderList.length > 0) {
        for (i = 0; i < OB.MobileApp.model.orderList.length; i++) {
          var order = OB.MobileApp.model.orderList.models[i];
          if ((order.getOrderType() === 1 || order.get('gross') < 0) && order.get('returnnoPrefix') === OB.MobileApp.model.get('terminal').returnDocNoPrefix) {
            if (OB.UTIL.isNullOrUndefined(lastSuffix) || (lastSuffix && order.get('returnnoSuffix') > lastSuffix)) {
              lastSuffix = order.get('returnnoSuffix');
            }
          }
        }
      }
      if (lastSuffix === null || lastSuffix < this.returnnoThreshold) {
        lastSuffix = this.returnnoThreshold;
      }
      return lastSuffix;
    },

    // call this method to get a new order document number
    getNextDocumentno: function () {
      var next = this.getLastDocumentnoSuffixInOrderlist() + 1;
      return {
        documentnoSuffix: next,
        documentNo: OB.MobileApp.model.get('terminal').docNoPrefix + (OB.Model.Order.prototype.includeDocNoSeperator ? '/' : '') + OB.UTIL.padNumber(next, 7)
      };
    },
    // call this method to get a new quotation document number
    getNextQuotationno: function () {
      var next = this.getLastQuotationnoSuffixInOrderlist() + 1;
      return {
        quotationnoSuffix: next,
        documentNo: OB.MobileApp.model.get('terminal').quotationDocNoPrefix + (OB.Model.Order.prototype.includeDocNoSeperator ? '/' : '') + OB.UTIL.padNumber(next, 7)
      };
    },
    // call this method to get a new Return document number
    getNextReturnno: function () {
      var next = this.getLastReturnnoSuffixInOrderlist() + 1;
      return {
        documentnoSuffix: next,
        documentNo: OB.MobileApp.model.get('terminal').returnDocNoPrefix + (OB.Model.Order.prototype.includeDocNoSeperator ? '/' : '') + OB.UTIL.padNumber(next, 7)
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
      if (!OB.UTIL.localStorage.getItem('terminalKeyIdentifier') && OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y') {
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
          me = this,
          key, parsedTerminalData = JSON.parse(terminalData);
      params.command = 'preLoginActions';
      params.params = terminalData;
      var terminalDataObject = JSON.parse(terminalData);
      for (key in terminalDataObject) {
        if (terminalDataObject.hasOwnProperty(key)) {
          params[key] = terminalDataObject[key];
        }
      }
      OB.warn('[TermAuth] Request to link terminal "' + parsedTerminalData.terminalKeyIdentifier + '" using user "' + parsedTerminalData.username + '" with cache session id "' + parsedTerminalData.cacheSessionId + '"');
      new OB.OBPOSLogin.UI.LoginRequest({
        url: OB.MobileApp.model.get('loginUtilsUrl')
      }).response(this, function (inSender, inResponse) {
        if (inResponse.exception || (inResponse.response && inResponse.response.error)) {
          var msg = inResponse.exception ? OB.I18N.getLabel(inResponse.exception) : inResponse.response.error.message;
          OB.UTIL.showConfirmation.display('Error', msg, [{
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
          if (me.get('logConfiguration') && me.get('logConfiguration').deviceIdentifier === null) {
            me.get('logConfiguration').deviceIdentifier = inResponse.terminalName;
          }
          OB.UTIL.localStorage.setItem('terminalName', inResponse.terminalName);
          OB.UTIL.localStorage.setItem('terminalKeyIdentifier', inResponse.terminalKeyIdentifier);
          OB.warn('[TermAuth] Terminal "' + parsedTerminalData.terminalKeyIdentifier + '" was successfully linked using user "' + parsedTerminalData.username + '" with cache session id "' + parsedTerminalData.cacheSessionId + '"');
          callback();
        }

      }).error(function () {
        callback();
      }).go(params);
    },

    initActions: function (callback) {
      var params = this.get('loginUtilsParams') || {},
          me = this;
      var cacheSessionId = null;
      if (OB.UTIL.localStorage.getItem('cacheSessionId') && OB.UTIL.localStorage.getItem('cacheSessionId').length === 32) {
        cacheSessionId = OB.UTIL.localStorage.getItem('cacheSessionId');
      }
      me.setTerminalName(OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));

      params.cacheSessionId = cacheSessionId;
      params.command = 'initActions';
      new OB.OBPOSLogin.UI.LoginRequest({
        url: '../../org.openbravo.retail.posterminal.service.loginutils'
      }).response(this, function (inSender, inResponse) {
        if (inResponse && inResponse.errorReadingTerminalAuthentication) {
          OB.UTIL.showWarning(inResponse.errorReadingTerminalAuthentication);
        }
        if (OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' && inResponse.terminalAuthentication === 'N') {
          if (OB.UTIL.localStorage.getItem('terminalKeyIdentifier')) {
            OB.info('Terminal Authentication has been disabled. Remove terminalKeyIdentifier from localStorage');
            OB.UTIL.localStorage.removeItem('terminalKeyIdentifier');
          }
        }
        OB.UTIL.localStorage.setItem('terminalAuthentication', inResponse.terminalAuthentication);
        if (!(OB.UTIL.localStorage.getItem('cacheSessionId') && OB.UTIL.localStorage.getItem('cacheSessionId').length === 32)) {
          OB.info('cacheSessionId is not defined and we will set the id generated in the backend: ' + inResponse.cacheSessionId);
          OB.UTIL.localStorage.setItem('cacheSessionId', inResponse.cacheSessionId);
          OB.UTIL.localStorage.setItem('LastCacheGeneration', new Date().getTime());
        }
        //Save available servers and services and initialize Request Router layer
        if (inResponse.servers) {
          OB.UTIL.localStorage.setItem('servers', JSON.stringify(inResponse.servers));
        }
        if (inResponse.services) {
          OB.UTIL.localStorage.setItem('services', JSON.stringify(inResponse.services));
        }
        OB.RR.RequestRouter.initialize();

        me.setTerminalName(OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"));
        callback();
      }).error(function () {
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
    paramTerminal: OB.UTIL.localStorage.getItem('terminalAuthentication') === 'Y' ? OB.UTIL.localStorage.getItem('terminalName') : OB.UTIL.getParameterByName("terminal"),
    hrefWindow: function (windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.MobileApp.model.get('terminalName')) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function () {
      OB.MobileApp.model.logout();
    },
    lock: function () {
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
OB.UTIL.HookManager.registerHook('OBMOBC_ProfileDialogApply', function (args, callbacks) {
  var widgetForm = args.profileDialogProp.owner.owner.$.bodyContent.$,
      newRoleId = widgetForm.roleList.getValue(),
      isDefault = widgetForm.defaultBox.checked,
      process = new OB.DS.Process('org.openbravo.retail.posterminal.Profile');
  if (isDefault) {
    args.profileDialogProp.isActive = true;
    process.exec({
      role: newRoleId
    }, function (data) {
      if (data.success) {
        OB.UTIL.HookManager.callbackExecutor(args, callbacks);
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorWhileSavingUser'));
      }
    }, function () {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
    });
  } else {
    OB.UTIL.HookManager.callbackExecutor(args, callbacks);
  }
});