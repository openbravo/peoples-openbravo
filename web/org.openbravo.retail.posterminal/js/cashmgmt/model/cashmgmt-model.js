/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, TestRegistry, Promise */

OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
OB.OBPOSCashMgmt.Model = OB.OBPOSCashMgmt.Model || {};
OB.OBPOSCashMgmt.UI = OB.OBPOSCashMgmt.UI || {};

// Window model
OB.OBPOSCashMgmt.Model.CashManagement = OB.Model.TerminalWindowModel.extend({
  models: [OB.Model.CashManagement],
  payments: null,
  init: function() {
    OB.error(
      'This init method should never be called for this model. Call initModels and loadModels instead'
    );
    this.initModels(function() {});
    this.loadModels(function() {});
  },
  pendingToSaveHaveCashManagementProvider: function() {
    var hasPayment = false;
    _.each(
      OB.App.State.Cashup.Utils.getCashManagementsInDraft(
        OB.App.State.getState().Cashup.cashPaymentMethodInfo
      ),
      function(drop) {
        var payment = _.find(OB.POS.modelterminal.get('payments'), function(p) {
          return (
            p.payment.id === drop.paymentMethodId &&
            p.paymentMethod.cashManagementProvider
          );
        });
        if (!OB.UTIL.isNullOrUndefined(payment) || drop.allowOnlyOne) {
          hasPayment = true;
        }
      }
    );
    return hasPayment;
  },
  initModels: function(initModelsCallback) {
    var me = this;

    this.on(
      'paymentDone',
      function(model, p, callback, errorCallback) {
        var execution = OB.UTIL.ProcessController.start('cashMngPaymentDone');
        // argument checks
        OB.UTIL.Debug.execute(function() {
          if (!me.payments) {
            OB.error(
              "The 'payments' variable has not been initialized (value: " +
                me.payments +
                "'"
            );
          }
        });

        var isError = !OB.UTIL.isNullOrUndefined(
          _.find(me.payments.models, function(pay) {
            return (
              !pay.get('issafebox') &&
              p.iscash &&
              p.id === pay.get('paymentmethod_id') &&
              p.type === 'drop' &&
              OB.DEC.sub(pay.get('total'), OB.DEC.mul(p.amount, p.rate)) < 0
            );
          })
        );
        if (isError) {
          OB.UTIL.ProcessController.finish('cashMngPaymentDone', execution);
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
          if (errorCallback) {
            errorCallback(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
          }
          return;
        }

        if (OB.DEC.mul(p.amount, p.rate) <= 0) {
          OB.UTIL.ProcessController.finish('cashMngPaymentDone', execution);
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_amtGreaterThanZero'));
          return;
        }
        var asyncToSyncWrapper = new Promise(async function(resolve, reject) {
          const cashup = OB.Dal.transform(
            OB.Model.CashUp,
            OB.App.State.getState().Cashup
          );
          var now = new Date();
          var addedCashMgmt = new OB.Model.CashManagement({
            id: OB.UTIL.get_UUID(),
            description: p.identifier + ' - ' + model.get('name'),
            amount: p.amount,
            origAmount: OB.DEC.mul(p.amount, p.rate),
            type: p.type,
            reasonId: model.get('id'),
            paymentMethodId: p.id,
            user: OB.MobileApp.model.get('context').user._identifier,
            userId: OB.MobileApp.model.get('context').user.id,
            creationDate: OB.I18N.normalizeDate(now),
            timezoneOffset: now.getTimezoneOffset(),
            isocode: p.isocode,
            glItem: p.glItem,
            cashup_id: cashup.get('id'),
            posTerminal: OB.MobileApp.model.get('terminal').id,
            isbeingprocessed: 'N',
            defaultProcess: p.defaultProcess,
            extendedType: p.extendedType
          });
          if (p.extendedProp || _.isObject(p.extendedProp)) {
            _.each(_.keys(p.extendedProp), function(key) {
              addedCashMgmt.set(key, p.extendedProp[key]);
            });
          }

          await OB.App.State.Cashup.createCashManagement({
            cashManagement: JSON.parse(JSON.stringify(addedCashMgmt))
          });

          var selectedPayment = me.payments.filter(function(payment) {
            return payment.get('paymentmethod_id') === p.id;
          })[0];
          if (selectedPayment.get('listdepositsdrops')) {
            selectedPayment.get('listdepositsdrops').push(addedCashMgmt);
            selectedPayment.trigger('change');
          } else {
            selectedPayment.set('listdepositsdrops', [addedCashMgmt]);
          }
          resolve();
        });

        asyncToSyncWrapper.then(
          function() {
            // Check pending drop to save have cash management provider
            execution.set(
              'hasPendigOp',
              me.pendingToSaveHaveCashManagementProvider()
            );
            if (execution.get('hasPendigOp')) {
              OB.UTIL.showWarning(
                OB.I18N.getLabel('OBPOS_MsgCloseOrDoneCashManagement')
              );
            }
            OB.UTIL.ProcessController.finish('cashMngPaymentDone', execution);
            if (callback) {
              callback();
            }
          },
          function() {
            OB.UTIL.ProcessController.finish('cashMngPaymentDone', execution);
            var errorMsg = 'Could not save payment information';
            if (errorCallback) {
              errorCallback(errorMsg);
            } else {
              OB.error(errorMsg);
            }
          }
        );
      },
      this
    );

    var makeDepositsFunction = function(me) {
      const cashManagementsInDraft = OB.App.State.Cashup.Utils.getCashManagementsInDraft(
        OB.App.State.getState().Cashup.cashPaymentMethodInfo
      );
      OB.info(
        '[CashMgmntSync][1] Cash management synchronization started. ' +
          cashManagementsInDraft.length +
          ' To be synched'
      );
      TestRegistry.CashMgmt = TestRegistry.CashMgmt || {};
      TestRegistry.CashMgmt.isCashDepositPrinted = false;

      OB.UTIL.showLoading(true);

      if (cashManagementsInDraft.length === 0) {
        // Nothing to do go to main window
        OB.info(
          '[CashMgmntSync] Cash managment synchronization exited. Nothing to sync'
        );
        OB.POS.navigate('retail.pointofsale');
        return true;
      }

      me.printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();

      TestRegistry.CashMgmt.isCashDepositPrinted = true;

      const cashManagementsToPrint = OB.App.State.Cashup.Utils.getCashManagementsInDraft(
        OB.App.State.getState().Cashup.cashPaymentMethodInfo
      );
      OB.App.State.Global.processCashManagements({
        parameters: {
          terminalName: OB.MobileApp.model.get('logConfiguration')
            .deviceIdentifier,
          cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId'),
          terminalPayments: OB.MobileApp.model.get('payments')
        }
      }).then(function() {
        OB.UTIL.showLoading(false);
        me.set('finished', true);
        if (OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')) {
          me.printCashMgmt.print(cashManagementsToPrint);
        }
      });
    };
    this.cancelDeposits = function(callback) {
      OB.App.State.Cashup.cancelCashManagements().then(callback);
    };
    this.on(
      'makeDeposits',
      function(receipt) {
        var me = this;
        if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          OB.MobileApp.model.setSynchronizedCheckpoint(function() {
            OB.UTIL.HookManager.executeHooks(
              'OBPOS_PreSaveCashManagements',
              {
                dropsdeps: OB.App.State.Cashup.Utils.getCashManagementsInDraft(
                  OB.App.State.getState().Cashup.cashPaymentMethodInfo
                )
              },
              function(args) {
                makeDepositsFunction(me);
              }
            );
          });
        } else {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PreSaveCashManagements',
            {
              dropsdeps: OB.App.State.Cashup.Utils.getCashManagementsInDraft(
                OB.App.State.getState().Cashup.cashPaymentMethodInfo
              )
            },
            function(args) {
              makeDepositsFunction(me);
            }
          );
        }
      },
      this
    );

    // effective entry point
    this.set('payments', new Backbone.Collection());
    this.set(
      'cashMgmtDropEvents',
      new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDropEvents'))
    );
    this.set(
      'cashMgmtDepositEvents',
      new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDepositEvents'))
    );

    initModelsCallback();
  },
  loadModels: function(loadModelsCallback) {
    var me = this;

    function updateCashMgmEvents(paymentMethodList) {
      var i, paymentMethodId;
      for (i = 0; i < me.get('cashMgmtDepositEvents').models.length; i++) {
        paymentMethodId = me
          .get('cashMgmtDepositEvents')
          .at(i)
          .get('paymentmethod');
        if (paymentMethodList.indexOf(paymentMethodId) === -1) {
          me.get('cashMgmtDepositEvents').remove(
            me.get('cashMgmtDepositEvents').at(i)
          );
        }
      }
      for (i = 0; i < me.get('cashMgmtDropEvents').models.length; i++) {
        paymentMethodId = me
          .get('cashMgmtDropEvents')
          .at(i)
          .get('paymentmethod');
        if (paymentMethodList.indexOf(paymentMethodId) === -1) {
          me.get('cashMgmtDropEvents').remove(
            me.get('cashMgmtDropEvents').at(i)
          );
        }
      }
    }

    function loadCashup(callback, args) {
      // argument checks
      OB.UTIL.Debug.execute(function() {
        if (!args) {
          OB.error("The 'args' variable must be provided. Use {} for nothing");
        }
      });

      var paymentMth;
      // synch logic
      var execution = OB.UTIL.ProcessController.start('cashMgmtLoadCashup');

      function finishSynch() {
        OB.UTIL.ProcessController.finish('cashMgmtLoadCashup', execution);
      }
      var i;
      var asyncToSyncWrapper = new Promise(function(resolve, reject) {
        const pays = new OB.Collection.PaymentMethodCashUpList();
        OB.App.State.getState().Cashup.cashPaymentMethodInfo.forEach(function(
          paymentMethod
        ) {
          const newPaymentMethod = {
            ...paymentMethod,
            paymentmethod_id: paymentMethod.paymentMethodId
          };
          pays.add(
            OB.Dal.transform(OB.Model.PaymentMethodCashUp, newPaymentMethod)
          );
        });

        me.set('listpaymentmethodid', []);

        me.payments = pays;
        for (i = 0; i < me.payments.length; i++) {
          if (me.payments.at(i).get('usedInCurrentTrx') !== false) {
            me.payments.at(i).set('usedInCurrentTrx', false);
          }
        }

        function updatePaymentMethod(pay) {
          return new Promise(function(resolve, reject) {
            paymentMth = OB.MobileApp.model
              .get('payments')
              .filter(function(payment) {
                return payment.payment.id === pay.get('paymentmethod_id');
              })[0].paymentMethod;

            if (
              OB.POS.modelterminal.get('terminal').isslave &&
              paymentMth.isshared
            ) {
              resolve();
              return;
            }

            // Set is this payment is defined in safe box
            pay.set('issafebox', paymentMth.issafebox);

            if (paymentMth.allowdeposits || paymentMth.allowdrops) {
              if (
                me
                  .get('listpaymentmethodid')
                  .indexOf(paymentMth.paymentMethod) === -1
              ) {
                me.get('listpaymentmethodid').push(paymentMth.paymentMethod);
              }

              const cashMgmts = new OB.Collection.CashManagementList();
              OB.App.State.Cashup.Utils.getCashManagements(
                OB.App.State.getState().Cashup.cashPaymentMethodInfo
              ).forEach(cashManagement =>
                cashMgmts.add(
                  OB.Dal.transform(OB.Model.CashManagement, cashManagement)
                )
              );

              if (cashMgmts.length > 0) {
                pay.listdepositsdrops = cashMgmts.models;
              }
              if (args.slavePayments) {
                // Accumulate slave payments
                _.each(args.slavePayments, function(slavePay) {
                  if (slavePay.searchKey === pay.get('searchKey')) {
                    pay.set(
                      'startingCash',
                      OB.DEC.add(pay.get('startingCash'), slavePay.startingCash)
                    );
                    pay.set(
                      'totalDeposits',
                      OB.DEC.add(
                        pay.get('totalDeposits'),
                        slavePay.totalDeposits
                      )
                    );
                    pay.set(
                      'totalDrops',
                      OB.DEC.add(pay.get('totalDrops'), slavePay.totalDrops)
                    );
                    pay.set(
                      'totalReturns',
                      OB.DEC.add(pay.get('totalReturns'), slavePay.totalReturns)
                    );
                    pay.set(
                      'totalSales',
                      OB.DEC.add(pay.get('totalSales'), slavePay.totalSales)
                    );
                  }
                });
              }
              me.get('payments').add(pay);
              resolve();
            } else {
              OB.UTIL.HookManager.executeHooks(
                'OBPOS_AddPaymentToCashManagement',
                {
                  context: me,
                  pay: pay,
                  paymentMethod: paymentMth
                },
                function(args) {
                  if (args.includePay) {
                    const cashMgmts = new OB.Collection.CashManagementList();
                    OB.App.State.Cashup.Utils.getCashManagements(
                      OB.App.State.getState().Cashup.cashPaymentMethodInfo
                    ).forEach(cashManagement =>
                      cashMgmts.add(
                        OB.Dal.transform(
                          OB.Model.CashManagement,
                          cashManagement
                        )
                      )
                    );

                    if (cashMgmts.length > 0) {
                      pay.set('listdepositsdrops', cashMgmts.models);
                    }
                    resolve();
                  } else {
                    resolve();
                  }
                }
              );
            }
          });
        }

        var paymentsToLoad = [];
        pays.forEach(function(pay) {
          if (OB.MobileApp.model.paymentnames[pay.get('searchKey')]) {
            paymentsToLoad.push(updatePaymentMethod(pay));
          }
        });
        Promise.all(paymentsToLoad).then(
          function() {
            updateCashMgmEvents(me.get('listpaymentmethodid'));
            resolve();
          },
          function(error) {
            OB.error(error.stack);
            reject();
          }
        );
      });

      asyncToSyncWrapper.then(
        function() {
          finishSynch();
          callback();
        },
        function(error) {
          OB.error(error.stack);
          OB.UTIL.ProcessController.finish('cashMgmtLoadCashup', execution);
        }
      );
    }

    async function loadSlaveCashup(callback) {
      // Load current cashup info from slaves
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.ProcessCashMgmtMaster',
        {
          cashUpId: OB.App.State.getState().Cashup.id,
          terminalSlave: OB.POS.modelterminal.get('terminal').isslave
        }
      );

      if (response && response.response && response.response.error) {
        // Error handler
        OB.log('error', response.response.error.message);
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashMgmtError'),
          OB.I18N.getLabel('OBPOS_ErrorServerGeneric') +
            response.response.error.message,
          [
            {
              label: OB.I18N.getLabel('OBPOS_LblRetry'),
              action: function() {
                loadSlaveCashup(callback);
              }
            }
          ],
          {
            autoDismiss: false,
            onHideFunction: function() {
              OB.POS.navigate('retail.pointofsale');
            }
          }
        );
      } else {
        callback(response.response.data);
      }
    }

    if (OB.POS.modelterminal.get('terminal').ismaster) {
      loadSlaveCashup(function(data) {
        loadCashup(loadModelsCallback, {
          slavePayments: data
        });
      });
    } else {
      // Load terminal cashup info (without slaves info)
      loadCashup(loadModelsCallback, {});
    }
  }
});
