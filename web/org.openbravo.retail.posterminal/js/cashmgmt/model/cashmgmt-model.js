/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, TestRegistry, enyo, Promise */

OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
OB.OBPOSCashMgmt.Model = OB.OBPOSCashMgmt.Model || {};
OB.OBPOSCashMgmt.UI = OB.OBPOSCashMgmt.UI || {};

// Window model
OB.OBPOSCashMgmt.Model.CashManagement = OB.Model.TerminalWindowModel.extend({
  models: [OB.Model.CashManagement],
  payments: null,
  init: function () {
    OB.error("This init method should never be called for this model. Call initModels and loadModels instead");
    this.initModels(function () {});
    this.loadModels(function () {});
  },
  initModels: function (initModelsCallback) {
    var me = this;

    this.depsdropstosave = new Backbone.Collection();
    this.depsdropstosave.on('paymentDone', function (model, p, callback) {
      // argument checks
      OB.UTIL.Debug.execute(function () {
        if (!me.payments) {
          OB.error("The 'payments' variable has not been initialized (value: " + me.payments + "'");
        }
      });
      var isError = false;
      me.payments.each(function (pay) {
        if (p.id === pay.get('paymentmethod_id')) {
          isError = (p.type === 'drop' && OB.DEC.sub(pay.get('total'), OB.DEC.mul(p.amount, p.rate)) < 0);
        }
      });

      if (isError) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        return;
      }

      if (OB.DEC.mul(p.amount, p.rate) <= 0) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_amtGreaterThanZero'));
        return;
      }
      var asyncToSyncWrapper = new Promise(function (resolve, reject) {
        OB.Dal.find(OB.Model.CashUp, {
          'isprocessed': 'N'
        }, function (cashUp) {
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
            cashup_id: cashUp.at(0).get('id'),
            posTerminal: OB.MobileApp.model.get('terminal').id,
            isbeingprocessed: 'N',
            defaultProcess: p.defaultProcess,
            extendedType: p.extendedType
          });
          if (p.extendedProp || _.isObject(p.extendedProp)) {
            _.each(_.keys(p.extendedProp), function (key) {
              addedCashMgmt.set(key, p.extendedProp[key]);
            });
          }
          me.depsdropstosave.add(addedCashMgmt);

          var selectedPayment = me.payments.filter(function (payment) {
            return payment.get('paymentmethod_id') === p.id;
          })[0];
          if (selectedPayment.get('listdepositsdrops')) {
            selectedPayment.get('listdepositsdrops').push(addedCashMgmt);
            selectedPayment.trigger('change');
          } else {
            selectedPayment.set('listdepositsdrops', [addedCashMgmt]);
          }
          resolve();
        }, reject, this);
      });

      asyncToSyncWrapper.then(function () {
        if (callback) {
          callback();
        }
      }, function () {
        OB.error("Could not save payment information");
      });
    }, this);

    var makeDepositsFunction = function (me) {
        TestRegistry.CashMgmt = TestRegistry.CashMgmt || {};
        TestRegistry.CashMgmt.isCashDepositPrinted = false;

        OB.UTIL.showLoading(true);

        if (me.depsdropstosave.length === 0) {
          // Nothing to do go to main window
          OB.POS.navigate('retail.pointofsale');
          return true;
        }

        me.printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();

        TestRegistry.CashMgmt.isCashDepositPrinted = true;

        function runSync() {
          if (OB.MobileApp.model.get('connectedToERP')) {
            OB.MobileApp.model.runSyncProcess(function () {
              OB.UTIL.showLoading(false);
              me.set("finished", true);
              if (OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')) {
                me.printCashMgmt.print(me.depsdropstosave.toJSON());
              }
            }, function () {
              if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
                // fail, remove everything and go away
                OB.Dal.removeAll(OB.Model.CashManagement, null, function () {
                  OB.UTIL.calculateCurrentCash();
                  me.depsdropstosave = new Backbone.Collection();
                });
              }
            });
          } else {
            OB.UTIL.showLoading(false);
            me.set("finished", true);
            if (OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')) {
              me.printCashMgmt.print(me.depsdropstosave.toJSON());
            }
          }
        }

        var paymentList = new Backbone.Collection(),
            found = false,
            i;

        function addAttributes(depdrop) {
          var payment = new OB.Model.PaymentMethodCashUp();
          if (depdrop.get('type') === 'deposit') {
            payment.set('paymentMethodId', depdrop.get('paymentMethodId'));
            payment.set('cashup_id', depdrop.get('cashup_id'));
            payment.set('totalDeposits', depdrop.get('amount'));
            payment.set('totalDrops', 0);
          } else {
            payment.set('paymentMethodId', depdrop.get('paymentMethodId'));
            payment.set('cashup_id', depdrop.get('cashup_id'));
            payment.set('totalDrops', depdrop.get('amount'));
            payment.set('totalDeposits', 0);
          }
          payment.set('newPaymentMethod', true);
          return payment;
        }
        _.each(me.depsdropstosave.models, function (depdrop) {
          if (paymentList.length > 0) {
            for (i = 0; i < paymentList.length; i++) {
              found = false;
              if (paymentList.models[i].get('paymentMethodId') === depdrop.get('paymentMethodId')) {
                var paymentMethod = paymentList.models[i],
                    totalDeposits = 0,
                    totalDrops = 0,
                    depos = paymentMethod.get('totalDeposits'),
                    drop = paymentMethod.get('totalDrops');
                if (depdrop.get('type') === 'deposit') {
                  totalDeposits = OB.DEC.add(depos, depdrop.get('amount'));
                  paymentMethod.set('totalDeposits', totalDeposits);
                } else {
                  totalDrops = OB.DEC.add(drop, depdrop.get('amount'));
                  paymentMethod.set('totalDrops', totalDrops);
                }
                found = true;
                break;
              }
            }
            if (!found) {
              paymentList.add(addAttributes(depdrop));
            }
          } else {
            paymentList.add(addAttributes(depdrop));
          }
        }, this);

        // Sending drops/deposits to backend
        var updateCashupAndAddCashupInfo = null;
        var updateCashupInfo = null;
        var setCashupObjectInCashMgmt;

        setCashupObjectInCashMgmt = function (depdrops, cashUp, index) {
          if (index === depdrops.length) {
            OB.UTIL.calculateCurrentCash();
            runSync();
          } else {
            var depdrop = depdrops[index];
            depdrop.set('cashUpReportInformation', JSON.parse(cashUp.models[0].get('objToSend')));
            depdrop.set('json', JSON.stringify(depdrop.serializeToJSON()));
            OB.Dal.save(depdrop, function () {
              setCashupObjectInCashMgmt(depdrops, cashUp, index + 1);
            }, function () {
              OB.UTIL.showLoading(false);
              me.set("finishedWrongly", true);
              return;
            }, true);
          }
        };

        updateCashupInfo = function (paymentList, index, cashUpReport, callback) {
          if (index === paymentList.length && callback) {
            callback(cashUpReport);
          } else {
            OB.UTIL.sumCashManagementToCashup(paymentList[index], function (cashUp) {
              updateCashupInfo(paymentList, index + 1, cashUp, callback);
            });
          }
        };

        updateCashupInfo(paymentList.models, 0, null, function (cashUpReport) {
          setCashupObjectInCashMgmt(me.depsdropstosave.models, cashUpReport, 0);
        });

        for (i = 0; i < paymentList.length; i++) {
          paymentList.at(i).set('newPaymentMethod', false);
        }
        };

    this.depsdropstosave.on('makeDeposits', function (receipt) {
      var me = this;
      if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
        OB.MobileApp.model.setSynchronizedCheckpoint(function () {
          OB.UTIL.HookManager.executeHooks('OBPOS_PreSaveCashManagements', {
            dropsdeps: me.depsdropstosave
          }, function (args) {
            makeDepositsFunction(me);
          });
        });
      } else {
        OB.UTIL.HookManager.executeHooks('OBPOS_PreSaveCashManagements', {
          dropsdeps: me.depsdropstosave
        }, function (args) {
          makeDepositsFunction(me);
        });
      }
    }, this);

    // effective entry point
    this.set('payments', new Backbone.Collection());
    this.set('cashMgmtDropEvents', new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDropEvents')));
    this.set('cashMgmtDepositEvents', new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDepositEvents')));

    if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
      OB.UTIL.rebuildCashupFromServer(function () {
        initModelsCallback();
      });
    } else {
      initModelsCallback();
    }
  },
  loadModels: function (loadModelsCallback) {
    var me = this;

    function updateCashMgmEvents(paymentMethodList) {
      var i, paymentMethodId;
      for (i = 0; i < me.get('cashMgmtDepositEvents').models.length; i++) {
        paymentMethodId = me.get('cashMgmtDepositEvents').at(i).get('paymentmethod');
        if (paymentMethodList.indexOf(paymentMethodId) === -1) {
          me.get('cashMgmtDepositEvents').remove(me.get('cashMgmtDepositEvents').at(i));
        }
      }
      for (i = 0; i < me.get('cashMgmtDropEvents').models.length; i++) {
        paymentMethodId = me.get('cashMgmtDropEvents').at(i).get('paymentmethod');
        if (paymentMethodList.indexOf(paymentMethodId) === -1) {
          me.get('cashMgmtDropEvents').remove(me.get('cashMgmtDropEvents').at(i));
        }
      }
    }

    function loadCashup(callback, args) {
      // argument checks
      OB.UTIL.Debug.execute(function () {
        if (!args) {
          OB.error("The 'args' variable must be provided. Use {} for nothing");
        }
      });

      var paymentMth;
      var criteria;
      // synch logic
      enyo.$.scrim.show();

      function finishSynch() {
        enyo.$.scrim.hide();
      }
      var i;
      var asyncToSyncWrapper = new Promise(function (resolve, reject) {
        OB.Dal.find(OB.Model.CashUp, {
          'isprocessed': 'N'
        }, function (cashUp) {
          OB.Dal.find(OB.Model.PaymentMethodCashUp, {
            'cashup_id': cashUp.at(0).get('id'),
            _orderByClause: 'searchKey desc'
          }, function (pays) {
            me.set('listpaymentmethodid', []);
            me.payments = pays;
            for (i = 0; i < me.payments.length; i++) {
              if (me.payments.at(i).get('usedInCurrentTrx') !== false) {
                me.payments.at(i).set('usedInCurrentTrx', false);
              }
            }

            function updatePaymentMethod(pay) {
              return new Promise(function (resolve, reject) {
                criteria = {
                  'paymentMethodId': pay.get('paymentmethod_id'),
                  'cashup_id': cashUp.at(0).get('id')
                };
                paymentMth = OB.MobileApp.model.get('payments').filter(function (payment) {
                  return payment.payment.id === pay.get('paymentmethod_id');
                })[0].paymentMethod;

                if (OB.POS.modelterminal.get('terminal').isslave && paymentMth.isshared) {
                  resolve();
                  return;
                }
                if (paymentMth.allowdeposits || paymentMth.allowdrops) {
                  if (me.get('listpaymentmethodid').indexOf(paymentMth.paymentMethod) === -1) {
                    me.get('listpaymentmethodid').push(paymentMth.paymentMethod);
                  }
                  OB.Dal.find(OB.Model.CashManagement, criteria, function (cashmgmt, pay) {
                    if (cashmgmt.length > 0) {
                      pay.set('listdepositsdrops', cashmgmt.models);
                    }
                    if (args.slavePayments) {
                      // Accumulate slave payments
                      _.each(args.slavePayments, function (slavePay) {
                        if (slavePay.searchKey === pay.get('searchKey')) {
                          pay.set('startingCash', OB.DEC.add(pay.get('startingCash'), slavePay.startingCash));
                          pay.set('totalDeposits', OB.DEC.add(pay.get('totalDeposits'), slavePay.totalDeposits));
                          pay.set('totalDrops', OB.DEC.add(pay.get('totalDrops'), slavePay.totalDrops));
                          pay.set('totalReturns', OB.DEC.add(pay.get('totalReturns'), slavePay.totalReturns));
                          pay.set('totalSales', OB.DEC.add(pay.get('totalSales'), slavePay.totalSales));
                        }
                      });
                    }
                    me.get('payments').add(pay);
                    resolve();
                  }, reject, pay);
                } else {
                  resolve();
                }
              });
            }

            var paymentsToLoad = [];
            pays.each(function (pay) {
              if (OB.MobileApp.model.paymentnames[pay.get('searchKey')]) {
                paymentsToLoad.push(updatePaymentMethod(pay));
              }
            });
            Promise.all(paymentsToLoad).then(function () {
              updateCashMgmEvents(me.get('listpaymentmethodid'));
              resolve();
            }, function () {
              OB.error("Could not load the payment method's information");
              reject();
            });

          }, reject);
        }, reject, this);
      });

      asyncToSyncWrapper.then(function () {
        finishSynch();
        callback();
      }, function () {
        OB.error("Could not load cashup related information");
      });
    }

    function loadSlaveCashup(callback) {
      // Load current cashup info from slaves
      new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmtMaster').exec({
        cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
        terminalSlave: OB.POS.modelterminal.get('terminal').isslave
      }, function (data) {
        if (data && data.exception) {
          // Error handler 
          OB.log('error', data.exception.message);
          OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashMgmtError'), OB.I18N.getLabel('OBPOS_ErrorServerGeneric') + data.exception.message, [{
            label: OB.I18N.getLabel('OBPOS_LblRetry'),
            action: function () {
              loadSlaveCashup(callback);
            }
          }], {
            autoDismiss: false,
            onHideFunction: function () {
              OB.POS.navigate('retail.pointofsale');
            }
          });
        } else {
          callback(data);
        }
      });
    }

    if (OB.POS.modelterminal.get('terminal').ismaster) {
      loadSlaveCashup(function (data) {
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