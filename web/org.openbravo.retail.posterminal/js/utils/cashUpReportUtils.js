/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, Backbone, Promise */

(function () {

  OB.UTIL = window.OB.UTIL || {};

  function findAndSave(cashuptaxes, i, finishCallback, tx) {

    if (i < cashuptaxes.length) {
      OB.Dal.findInTransaction(tx, OB.Model.TaxCashUp, {
        'cashup_id': cashuptaxes[i].cashupID,
        'name': cashuptaxes[i].taxName,
        'orderType': cashuptaxes[i].taxOrderType
      }, function (tax) {
        if (tax.length === 0) {
          OB.Dal.saveInTransaction(tx, new OB.Model.TaxCashUp({
            name: cashuptaxes[i].taxName,
            amount: cashuptaxes[i].taxAmount,
            orderType: cashuptaxes[i].taxOrderType,
            cashup_id: cashuptaxes[i].cashupID
          }), function () {
            findAndSave(cashuptaxes, i + 1, finishCallback, tx);
          }, null);
        } else {
          tax.at(0).set('amount', OB.DEC.add(tax.at(0).get('amount'), cashuptaxes[i].taxAmount));
          OB.Dal.saveInTransaction(tx, tax.at(0), function () {
            findAndSave(cashuptaxes, i + 1, finishCallback, tx);
          }, null);
        }
      });
    } else {
      if (finishCallback) {
        finishCallback();
      }
    }
  }

  function updateCashUpInfo(cashUp, receipt, j, callback, tx) {
    var cashuptaxes, order, orderType, gross, i, taxOrderType, taxAmount, auxPay;
    var netSales = OB.DEC.Zero;
    var grossSales = OB.DEC.Zero;
    var netReturns = OB.DEC.Zero;
    var grossReturns = OB.DEC.Zero;
    var taxSales = OB.DEC.Zero;
    var taxReturns = OB.DEC.Zero;
    var ctaxSales;
    var maxtaxSales = OB.DEC.Zero;
    var ctaxReturns;
    var maxtaxReturns = OB.DEC.Zero;

    cashUp.at(0).set('transitionsToOnline', OB.UTIL.localStorage.getItem('transitionsToOnline'));
    cashUp.at(0).set('logclientErrors', OB.UTIL.localStorage.getItem('logclientErrors'));

    if (j < receipt.length && !receipt[j].has('obposIsDeleted')) {
      order = receipt[j];
      orderType = order.get('orderType');
      if (cashUp.length !== 0) {
        _.each(order.get('lines').models, function (line) {
          if (order.get('priceIncludesTax')) {
            gross = line.get('lineGrossAmount');
          } else {
            gross = line.get('discountedGross');
          }
          if (!(order.has('isQuotation') && order.get('isQuotation'))) {
            if (order.get('cancelLayaway')) {
              // Cancel Layaway
              netSales = OB.DEC.add(netSales, line.get('net'));
              grossSales = OB.DEC.add(grossSales, gross);
            } else if (order.get('voidLayaway')) {
              // Void Layaway
              netSales = OB.DEC.add(netSales, -line.get('net'));
              grossSales = OB.DEC.add(grossSales, -gross);
            } else if (!order.get('isLayaway')) {
              if (line.get('qty') > 0) {
                // Sales order: Positive line
                netSales = OB.DEC.add(netSales, line.get('net'));
                grossSales = OB.DEC.add(grossSales, gross);
              } else if (line.get('qty') < 0) {
                // Return from customer or Sales with return: Negative line
                netReturns = OB.DEC.add(netReturns, -line.get('net'));
                grossReturns = OB.DEC.add(grossReturns, -gross);
              }
            }
          }
        });
        cashUp.at(0).set('netSales', OB.DEC.add(cashUp.at(0).get('netSales'), netSales));
        cashUp.at(0).set('grossSales', OB.DEC.add(cashUp.at(0).get('grossSales'), grossSales));
        cashUp.at(0).set('netReturns', OB.DEC.add(cashUp.at(0).get('netReturns'), netReturns));
        cashUp.at(0).set('grossReturns', OB.DEC.add(cashUp.at(0).get('grossReturns'), grossReturns));
        cashUp.at(0).set('totalRetailTransactions', OB.DEC.sub(cashUp.at(0).get('grossSales'), cashUp.at(0).get('grossReturns')));
        OB.Dal.saveInTransaction(tx, cashUp.at(0), null, null);

        // group and sum the taxes
        cashuptaxes = [];
        order.get('lines').each(function (line, taxIndex) {
          var taxLines, taxLine;
          taxLines = line.get('taxLines');
          if (orderType === 1 || (line.get('qty') < 0 && !order.get('cancelLayaway') && !order.get('voidLayaway'))) {
            taxOrderType = '1';
          } else {
            taxOrderType = '0';
          }

          _.each(taxLines, function (taxLine) {
            if (!(order.has('isQuotation') && order.get('isQuotation'))) {
              if (order.get('cancelLayaway') || (line.get('qty') > 0 && !order.get('isLayaway'))) {
                taxAmount = taxLine.amount;
              } else if (order.get('voidLayaway') || (line.get('qty') < 0 && !order.get('isLayaway'))) {
                taxAmount = -taxLine.amount;
              }
            }

            if (!OB.UTIL.isNullOrUndefined(taxAmount)) {
              cashuptaxes.push({
                taxName: taxLine.name,
                taxAmount: taxAmount,
                taxOrderType: taxOrderType,
                cashupID: cashUp.at(0).get('id')
              });
            }
          });
        });

        // Calculate adjustment taxes
        _.each(cashuptaxes, function (t) {
          if (t.taxOrderType === '0') { // sale
            taxSales = OB.DEC.add(taxSales, t.taxAmount);
            if (t.taxAmount > maxtaxSales) {
              ctaxSales = t;
            }
          } else { // return
            taxReturns = OB.DEC.add(taxReturns, t.taxAmount);
            if (t.taxAmount > maxtaxReturns) {
              ctaxReturns = t;
            }
          }
        });
        // Do the adjustment
        if (ctaxSales) {
          ctaxSales.taxAmount = OB.DEC.add(ctaxSales.taxAmount, OB.DEC.sub(OB.DEC.sub(grossSales, netSales), taxSales));
        }
        if (ctaxReturns) {
          ctaxReturns.taxAmount = OB.DEC.add(ctaxReturns.taxAmount, OB.DEC.sub(OB.DEC.sub(grossReturns, netReturns), taxReturns));
        }


        OB.Dal.findInTransaction(tx, OB.Model.PaymentMethodCashUp, {
          'cashup_id': cashUp.at(0).get('id')
        }, function (payMthds) { //OB.Dal.find success
          //set all payment methods to false
          for (i = 0; i < payMthds.length; i++) {
            if (payMthds.at(i).get('usedInCurrentTrx') !== false) {
              payMthds.at(i).set('usedInCurrentTrx', false);
              OB.Dal.saveInTransaction(tx, payMthds.at(i), null, null);
            }
          }
          _.each(order.get('payments').models, function (payment) {
            auxPay = payMthds.filter(function (payMthd) {
              return (payMthd.get('searchKey') === payment.get('kind')) && !payment.get('isPrePayment');
            })[0];
            if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
              return;
            }
            if (payment.get('amount') < 0) {
              auxPay.set('totalReturns', OB.DEC.sub(auxPay.get('totalReturns'), payment.get('amount')));
            } else {
              auxPay.set('totalSales', OB.DEC.add(auxPay.get('totalSales'), payment.get('amount')));
            }
            //set used in transaction payment methods to true
            auxPay.set('usedInCurrentTrx', true);
            OB.Dal.saveInTransaction(tx, auxPay, null, null);
          }, this);
          findAndSave(cashuptaxes, 0, function () {
            OB.UTIL.composeCashupInfo(cashUp, null, function () {
              updateCashUpInfo(cashUp, receipt, j + 1, callback, tx);
            }, tx);
          }, tx);
        });
      }
    } else if (typeof callback === 'function') {
      if (OB.UTIL.isNullOrUndefined(cashUp.at(0).get('objToSend'))) {
        cashUp.at(0).set('objToSend', JSON.stringify(cashUp.models[0]));
      }
      cashUp.at(0).set('objToSend', JSON.stringify(cashUp.models[0]));
      callback(cashUp);
    }
  }

  OB.UTIL.cashUpReport = function (receipt, callback, tx) {
    var auxPay, orderType, taxOrderType, taxAmount, gross;
    if (!Array.isArray(receipt)) {
      receipt = [receipt];
    }

    OB.Dal.findInTransaction(tx, OB.Model.CashUp, {
      'isprocessed': 'N'
    }, function (cashUp) {
      updateCashUpInfo(cashUp, receipt, 0, callback, tx);
    });
  };

  OB.UTIL.deleteCashUps = function (cashUpModels, tx, callback) {
    var ids = [];
    var criteria;
    cashUpModels.each(function (cashup) {
      if (cashup.get('isprocessed') === 'Y') {
        ids.push("'" + cashup.get('id') + "'");
      }
    });
    if (ids.length > 0) {
      criteria = {
        whereClause: ' WHERE cashup_id in (' + ids.join() + ')'
      };
      OB.Dal.removeAllInTransaction(tx, OB.Model.TaxCashUp, criteria, function () {
        OB.Dal.removeAllInTransaction(tx, OB.Model.CashManagement, criteria, function () {
          OB.Dal.removeAllInTransaction(tx, OB.Model.PaymentMethodCashUp, criteria, function () {
            OB.Dal.removeAllInTransaction(tx, OB.Model.CashUp, criteria, function () {
              if (callback) {
                callback();
              }
            }, function (tx, err) {
              OB.UTIL.showError(err);
            });
          }, function (tx, err) {
            OB.UTIL.showError(err);
          });
        }, function (tx, err) {
          OB.UTIL.showError(err);
        });
      }, function (tx, err) {
        OB.UTIL.showError(err);
      });
    } else if (callback) {
      callback();
    }
  };
  OB.UTIL.createNewCashupFromServer = function (cashup, callback, oldCashupPaymentMethods) {
    OB.UTIL.localStorage.setItem('transitionsToOnline', 0);
    OB.UTIL.resetNumberOfLogClientErrors();
    var promises = [];
    OB.Dal.save(cashup, function () {
      OB.MobileApp.model.get('terminal').cashUpId = cashup.get('id');
      // Create taxes
      _.each(cashup.get('cashTaxInfo'), function (taxCashup) {
        var taxModel = new OB.Model.TaxCashUp();
        taxModel.set(taxCashup);
        promises.push(new Promise(function (resolve, reject) {
          OB.Dal.save(taxModel, function () {
            resolve();
          }, function () {
            OB.error(OB.I18N.getLabel('OBPOS_DalSaveError'));
            reject();
          }, true);
        }));
      });

      // Create Cash Management
      _.each(cashup.get('cashMgmInfo'), function (cashMgm) {
        var cashMgmModel = new OB.Model.CashManagement();
        cashMgmModel.set(cashMgm);
        promises.push(new Promise(function (resolve, reject) {
          OB.Dal.save(cashMgmModel, function () {
            resolve();
          }, function () {
            OB.error(OB.I18N.getLabel('OBPOS_DalSaveError'));
            reject();
          }, true);
        }));
      });

      //current cashup
      if (cashup.get('cashPaymentMethodInfo').length !== 0) {
        var paymentsAlreadyAdded = [];
        _.each(cashup.get('cashPaymentMethodInfo'), function (paymentMethodCashUp) {
          var paymentMethodCashUpModel = new OB.Model.PaymentMethodCashUp();
          paymentMethodCashUpModel.set(paymentMethodCashUp);
          var payments = OB.MobileApp.model.get('payments');
          var pAux = payments.filter(function (payMthd) {
            return paymentMethodCashUpModel.get('paymentmethod_id') === payMthd.payment.id;
          })[0];
          if (!pAux) {
            OB.info('Payment method not found. This is likely due to the fact that the payment method was disabled.');
            return;
          }
          paymentsAlreadyAdded.push(paymentMethodCashUpModel.get('paymentmethod_id'));
          if (pAux.payment.active === true || (pAux.payment.active === false && paymentMethodCashUpModel.get('totalSales') !== 0 && paymentMethodCashUpModel.get('totalReturns') !== 0 && paymentMethodCashUpModel.get('totalDepostis') !== 0 && paymentMethodCashUpModel.get('totalDrops') !== 0)) {
            promises.push(new Promise(function (resolve, reject) {
              OB.Dal.save(paymentMethodCashUpModel, function () {
                resolve();
              }, function () {
                OB.error(OB.I18N.getLabel('OBPOS_DalSaveError'));
                reject();
              }, true);
            }));
          }
        });

        // Process those payment method cashup stored in database and not included in the actual cashup
        if (oldCashupPaymentMethods && oldCashupPaymentMethods.length > 0) {
          _.each(oldCashupPaymentMethods.models, function (oldPaymentMethodCashup) {
            if (paymentsAlreadyAdded.indexOf(oldPaymentMethodCashup.get('paymentmethod_id')) === -1) {
              promises.push(new Promise(function (resolve, reject) {
                OB.Dal.save(oldPaymentMethodCashup, function () {
                  resolve();
                }, function () {
                  OB.error(OB.I18N.getLabel('OBPOS_DalSaveError'));
                  reject();
                }, true);
              }));
            }
          });
        }
      } else {
        OB.UTIL.initializePaymentMethodCashup(null, cashup, null, promises);
      }

      Promise.all(promises).then(callback);
    }, function () {
      OB.MobileApp.model.get('terminal').cashUpId = cashup.get('id');
    }, true);

  };

  OB.UTIL.createNewCashup = function (callback, lastCashupModel) {
    // Create the cashup empty
    OB.UTIL.localStorage.setItem('transitionsToOnline', 0);
    OB.UTIL.resetNumberOfLogClientErrors();
    var uuid = OB.UTIL.get_UUID();
    OB.Dal.save(new OB.Model.CashUp({
      id: uuid,
      netSales: OB.DEC.Zero,
      grossSales: OB.DEC.Zero,
      netReturns: OB.DEC.Zero,
      grossReturns: OB.DEC.Zero,
      totalRetailTransactions: OB.DEC.Zero,
      creationDate: (new Date()).toISOString(),
      userId: OB.MobileApp.model.get('context').user.id,
      objToSend: null,
      cashTaxInfo: [],
      cashCloseInfo: [],
      isbeingprocessed: 'Y',
      posterminal: OB.MobileApp.model.get('terminal').id,
      isprocessed: 'N'
    }), function () {
      OB.MobileApp.model.get('terminal').cashUpId = uuid;
      if (lastCashupModel && lastCashupModel.get('isprocessed') === 'Y') {
        var promises = [];
        OB.UTIL.initializePaymentMethodCashup(JSON.parse(lastCashupModel.get('objToSend')).cashCloseInfo, null, false, promises);
        Promise.all(promises).then(callback);
      } else {
        // Get Info from the last Cashup
        //1. Search in local
        var criteria = {
          'isprocessed': 'Y',
          '_orderByClause': 'creationDate desc'
        };
        OB.Dal.find(OB.Model.CashUp, criteria, function (lastCashUp) {
          var lastCashUpPayments;
          if (lastCashUp.length !== 0) {
            var promises = [];
            lastCashUpPayments = JSON.parse(lastCashUp.at(0).get('objToSend')).cashCloseInfo;
            OB.UTIL.initializePaymentMethodCashup(lastCashUpPayments, null, false, promises);
            Promise.all(promises).then(callback);
          } else {
            var service = 'org.openbravo.retail.posterminal.master.Cashup';
            //2. Search in server
            new OB.DS.Process(service).exec({
              isprocessed: 'Y'
            }, function (data) {
              var promises = [];
              if (data[0]) {
                lastCashUp = new OB.Model.CashUp();
                lastCashUp.set(data[0]);
                lastCashUpPayments = lastCashUp.get('cashPaymentMethodInfo');
                //lastCashUpPayments = JSON.parse(lastCashUp.get('cashPaymentMethodInfo'));
              } else {
                if (arguments[0].exception) {
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_NotEnoughDataInCache') + OB.I18N.getLabel('OBMOBC_LoadingErrorBody'), [{
                    label: OB.I18N.getLabel('OBMOBC_Reload'),
                    action: function () {
                      window.location.reload();
                    }
                  }], {
                    onShowFunction: function (popup) {
                      popup.$.headerCloseButton.hide();
                    },
                    autoDismiss: false
                  });
                  return;
                } else {
                  // Set all  to 0
                  lastCashUpPayments = null;
                }
              }
              OB.UTIL.initializePaymentMethodCashup(lastCashUpPayments, null, true, promises);
              Promise.all(promises).then(callback);
            }, function () {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_NotEnoughDataInCache') + OB.I18N.getLabel('OBMOBC_LoadingErrorBody'), [{
                label: OB.I18N.getLabel('OBMOBC_Reload'),
                action: function () {
                  window.location.reload();
                }
              }], {
                onShowFunction: function (popup) {
                  popup.$.headerCloseButton.hide();
                },
                autoDismiss: false
              });
            });
          }
        }, function () {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_NotEnoughDataInCache') + OB.I18N.getLabel('OBMOBC_LoadingErrorBody'), [{
            label: OB.I18N.getLabel('OBMOBC_Reload'),
            action: function () {
              window.location.reload();
            }
          }], {
            onShowFunction: function (popup) {
              popup.$.headerCloseButton.hide();
            },
            autoDismiss: false
          });
        });
      }
    }, function () {
      // error
      //console.error("OB.Model.CashUp fail");
    }, true);

  };

  // add to array of promise objects
  OB.UTIL.initializePaymentMethodCashup = function (lastCashUpPayments, cashup, funcType, promises) {
    var promisesArePassedIn = promises;
    promises = promises || [];

    _.each(OB.MobileApp.model.get('payments'), function (payment) {
      var startingCash = payment.currentBalance,
          pAux, cashupId, deposits = payment.payment.totalDeposits,
          drops = payment.payment.totalDrops;
      if (cashup) {
        cashupId = cashup.get('id');
      } else {
        cashupId = OB.MobileApp.model.get('terminal').cashUpId;
      }
      if (lastCashUpPayments) {
        pAux = lastCashUpPayments.filter(function (payMthd) {
          return payMthd.paymentTypeId === payment.payment.id;
        })[0];
        if (!OB.UTIL.isNullOrUndefined(pAux)) {
          // if the last cashup payments are read locally then their structure
          // is different from when reading from the server
          if (_.isObject(pAux.paymentMethod)) {
            startingCash = pAux.paymentMethod.amountToKeep;
          } else {
            startingCash = pAux.amountToKeep;
          }
        }
      }

      if (!deposits) {
        deposits = OB.DEC.Zero;
      }
      if (!drops) {
        drops = OB.DEC.Zero;
      }
      // If payment is active
      if (payment.payment.active === true || (payment.payment.active === false && deposits !== 0 && drops !== 0)) {
        // Set startingCash to zero on slave terminal and payment method is share
        if (OB.POS.modelterminal.get('terminal').isslave && payment.paymentMethod.isshared) {
          startingCash = OB.DEC.Zero;
        }
        promises.push(new Promise(function (resolve, reject) {
          OB.Dal.save(new OB.Model.PaymentMethodCashUp({
            id: OB.UTIL.get_UUID(),
            paymentmethod_id: payment.payment.id,
            searchKey: payment.payment.searchKey,
            name: payment.payment._identifier,
            startingCash: startingCash,
            totalSales: OB.DEC.Zero,
            totalReturns: OB.DEC.Zero,
            totalDeposits: deposits,
            totalDrops: drops,
            rate: payment.rate,
            isocode: payment.isocode,
            cashup_id: cashupId,
            lineNo: payment.lineNo,
            newPaymentMethod: true
          }), function () {
            resolve();
          }, function () {
            reject();
          }, true);
        }));
      }
    }, this);

    // promises parameter not passed, execute right away to preserve api
    if (!promisesArePassedIn && promises.length > 0) {
      Promise.all(promises);
    }
  };

  // 1. call server for cashup info
  // 2. when returns delete current cashup info 
  // 3. recreate
  OB.UTIL.rebuildCashupFromServer = function (callback, errorCallback) {
    var service = 'org.openbravo.retail.posterminal.master.Cashup';
    if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
      OB.UTIL.showLoading(true);
    }
    new OB.DS.Process(service).exec({
      isprocessed: 'N',
      isprocessedbo: 'N'
    }, function (data) {
      if (data && data.exception) {
        var msg = data.exception.message ? data.exception.message : 'Unexpected error. ';
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_TransactionFailedTitle'), OB.I18N.getLabel('OBMOBC_TransactionFailed', [msg]), [{
          isConfirmButton: true,
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            if (OB.MobileApp.view.currentWindow !== 'retail.pointofsale') {
              OB.POS.navigate('retail.pointofsale');
              return true;
            }
          }
        }]);
        if (errorCallback instanceof Function) {
          errorCallback();
        }
        return;
      }
      var afterDeleteCallback = function (cashupPaymentMethods) {
          // Found non processed cashups
          if (data[0]) {
            var cashUp = new OB.Model.CashUp();
            cashUp.set(data[0]);
            var cashUpCollection = new Backbone.Collection();
            cashUpCollection.push(cashUp);
            OB.UTIL.createNewCashupFromServer(cashUp, function () {
              OB.UTIL.composeCashupInfo(cashUpCollection, null, null);
              OB.UTIL.calculateCurrentCash(callback);
            }, cashupPaymentMethods);
          } else {
            OB.UTIL.createNewCashup(callback);
          }
          };
      // get actual payment method cashup from localdatabase
      OB.Dal.find(OB.Model.PaymentMethodCashUp, {}, function (cashupPaymentMethods) {
        // remove the current cashup
        OB.Dal.transaction(function (tx) {
          OB.Dal.removeAllInTransaction(tx, OB.Model.PaymentMethodCashUp);
          OB.Dal.removeAllInTransaction(tx, OB.Model.TaxCashUp);
          OB.Dal.removeAllInTransaction(tx, OB.Model.CashUp);
          OB.Dal.removeAllInTransaction(tx, OB.Model.CashManagement);
        }, null, function () {
          afterDeleteCallback(cashupPaymentMethods);
        });
      }, function () {
        afterDeleteCallback();
      });
    });
  };

  OB.UTIL.initCashUp = function (callback, errorCallback, skipSearchBackend, lastCashupModel) {

    //1. Search non processed cashup in local DB
    //2. Search non processed cashup in backoffice DB
    //2.1 Search payments and taxes
    //3. Create new Cashup
    //3.1 Using processed cashup info in local or using processed cashup info in Back office
    var criteria = {
      'isprocessed': 'N',
      '_orderByClause': 'creationDate desc'
    };
    if (!lastCashupModel) {
      OB.Dal.find(OB.Model.CashUp, criteria, function (cashUp) { //OB.Dal.find success
        if (cashUp.length === 0) {
          var processedCriteria = {
            'isprocessed': 'Y',
            '_orderByClause': 'creationDate desc'
          };
          OB.Dal.find(OB.Model.CashUp, processedCriteria, function (cashUpProcessed) {
            if (cashUpProcessed.length === 0) {
              if (!skipSearchBackend) {
                var service = 'org.openbravo.retail.posterminal.master.Cashup';
                // Search in the backoffice
                new OB.DS.Process(service).exec({
                  isprocessed: 'N',
                  isprocessedbo: 'N'
                }, function (data) {
                  // Found non processed cashups
                  if (data && data.exception) {
                    errorCallback();
                  } else if (data && _.isArray(data) && data.length > 0 && data[0]) {
                    cashUp = new OB.Model.CashUp();
                    cashUp.set(data[0]);
                    var cashUpCollection = new Backbone.Collection();
                    cashUpCollection.push(cashUp);
                    OB.UTIL.createNewCashupFromServer(cashUp, function () {
                      OB.UTIL.composeCashupInfo(cashUpCollection, null, null);
                      OB.UTIL.calculateCurrentCash(callback);
                    });
                  } else {
                    OB.UTIL.createNewCashup(callback);
                  }
                });
              } else {
                OB.UTIL.createNewCashup(callback);
              }
            } else {
              OB.UTIL.createNewCashup(callback);
            }
          });
        } else {
          OB.UTIL.createCashupPaymentMethods(cashUp.at(0), callback);
        }
      });
    } else {
      if (lastCashupModel.get('isprocessed') === 'Y') {
        OB.UTIL.createNewCashup(callback, lastCashupModel);
      } else {
        OB.UTIL.createCashupPaymentMethods(lastCashupModel, callback);
      }
    }

  };
  OB.UTIL.createCashupPaymentMethods = function (cashUp, callback) {

    if (!OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
      OB.MobileApp.model.get('terminal').cashUpId = cashUp.get('id');
    }
    OB.Dal.find(OB.Model.PaymentMethodCashUp, {
      'cashup_id': cashUp.get('id')
    }, function (lastCashUpPayments) {
      //We add new payment methods to local ddbb
      _.each(OB.MobileApp.model.get('payments'), function (payment) {
        var pAux, startingCash = payment.currentBalance;
        if (lastCashUpPayments) {
          pAux = lastCashUpPayments.filter(function (payMthd) {
            return payMthd.get('paymentmethod_id') === payment.payment.id;
          })[0];
          if (OB.UTIL.isNullOrUndefined(pAux) && payment.payment.active === true) {
            OB.Dal.save(new OB.Model.PaymentMethodCashUp({
              id: OB.UTIL.get_UUID(),
              paymentmethod_id: payment.payment.id,
              searchKey: payment.payment.searchKey,
              name: payment.payment._identifier,
              startingCash: startingCash,
              totalSales: OB.DEC.Zero,
              totalReturns: OB.DEC.Zero,
              totalDeposits: OB.DEC.Zero,
              totalDrops: OB.DEC.Zero,
              rate: payment.rate,
              isocode: payment.isocode,
              cashup_id: cashUp.get('id'),
              lineNo: payment.lineNo,
              newPaymentMethod: true
            }), null, null, true);
          } else if (!OB.UTIL.isNullOrUndefined(pAux)) {
            if (pAux.get("name") !== payment.payment._identifier) {
              pAux.set("name", payment.payment._identifier);
              OB.Dal.save(pAux);
            }
          }
        }
      }, function (lastCashUpPayments) {
        //We remove old payment methods from the local ddbb
        //_.each(lastCashUpPayments.models, function (lastCashUpPayment) {
        //  OB.UTIL.deleteUnactivePaymentMethod(lastCashUpPayment);
        //}, this);
      });

      //We do not need to wait to execute callback. Callback should not be affected by this process.
      if (callback) {
        callback();
      }
    }, function () { //in case of error
      if (callback) {
        callback();
      }
    }, this);

  };
  OB.UTIL.deleteUnactivePaymentMethod = function (lastCashUpPayment) {
    var pAux, payments = OB.MobileApp.model.get('payments');
    if (payments) {
      pAux = payments.filter(function (payMthd) {
        return lastCashUpPayment.get('paymentmethod_id') === payMthd.payment.id;
      })[0];
      if ((OB.UTIL.isNullOrUndefined(pAux) || pAux.get('active') === true) && lastCashUpPayment.get('totalReturns') === OB.DEC.Zero && lastCashUpPayment.get('totalSales') === OB.DEC.Zero && lastCashUpPayment.get('totalDepostis') === OB.DEC.Zero && lastCashUpPayment.get('totalDrops') === OB.DEC.Zero) {
        OB.Dal.remove(lastCashUpPayment, null, function (tx, err) {
          OB.UTIL.showError(err);
        });
      }
    }
  };
  OB.UTIL.sumCashManagementToCashup = function (payment, callback, tx) {
    if (!OB.UTIL.isNullOrUndefined(payment)) {
      var cashupId = payment.get('cashup_id'),
          criteria = {
          'cashup_id': cashupId,
          'paymentmethod_id': payment.get('paymentMethodId')
          };

      OB.Dal.findInTransaction(tx, OB.Model.PaymentMethodCashUp, criteria, function (paymentMethods) {

        var paymentMethod = paymentMethods.at(0),
            totalDeposits = paymentMethod.get('totalDeposits'),
            totalDrops = paymentMethod.get('totalDrops');
        totalDeposits = OB.DEC.add(totalDeposits, payment.get('totalDeposits'));
        paymentMethod.set('totalDeposits', totalDeposits);
        totalDrops = OB.DEC.add(totalDrops, payment.get('totalDrops'));
        paymentMethod.set('totalDrops', totalDrops);
        //set used in transaction payment methods to true
        paymentMethod.set('usedInCurrentTrx', true);
        OB.Dal.saveInTransaction(tx, paymentMethod, function (success) {
          // Success
          OB.Dal.findInTransaction(tx, OB.Model.CashUp, {
            'id': cashupId
          }, function (cashUpObj) {
            OB.UTIL.composeCashupInfo(cashUpObj, null, callback, tx);
          });
        }, function (error) {
          // Error
        });
      });
    } else {
      if (callback) {
        callback();
      }
    }
  };
  OB.UTIL.calculateCurrentCash = function (callback, tx, lastCashup) {
    var me = this;

    OB.Dal.findInTransaction(tx, OB.Model.CashUp, {
      'isprocessed': 'N'
    }, function (cashUp) {
      OB.Dal.findInTransaction(tx, OB.Model.PaymentMethodCashUp, {
        'cashup_id': cashUp.length === 1 ? cashUp.at(0).get('id') : (lastCashup ? lastCashup.get('id') : null)
      }, function (payMthds) { //OB.Dal.find success
        var payMthdsCash;
        _.each(OB.MobileApp.model.get('payments'), function (paymentType, index) {
          var cash = 0,
              auxPay = payMthds.filter(function (payMthd) {
              return payMthd.get('paymentmethod_id') === paymentType.payment.id;
            })[0];
          if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
            return;
          }
          auxPay.set('_id', paymentType.payment.searchKey);
          auxPay.set('isocode', paymentType.isocode);
          auxPay.set('paymentMethod', paymentType.paymentMethod);
          auxPay.set('id', paymentType.payment.id);
          var startingCash = auxPay.get('startingCash'),
              rate = auxPay.get('rate'),
              totalSales = auxPay.get('totalSales'),
              totalReturns = auxPay.get('totalReturns'),
              totalDeps = auxPay.get('totalDeposits'),
              totalDrops = auxPay.get('totalDrops'),
              payment = OB.MobileApp.model.paymentnames[paymentType.payment.searchKey];

          if (!totalDeps) {
            totalDeps = 0;
          }
          if (!totalDrops) {
            totalDrops = 0;
          }
          var cashMgmt = OB.DEC.sub(totalDeps, totalDrops);
          cash = OB.DEC.add(OB.DEC.add(startingCash, OB.DEC.sub(totalSales, totalReturns)), cashMgmt);
          payment.currentCash = OB.UTIL.currency.toDefaultCurrency(payment.paymentMethod.currency, cash);
          payment.foreignCash = OB.UTIL.currency.toForeignCurrency(payment.paymentMethod.currency, cash);
        }, this);
        if (typeof callback === 'function') {
          callback();
        }
      });
    });
  };

  OB.UTIL.getPaymethodCashUp = function (payMthds, objToSend, cashUp) {
    _.each(OB.MobileApp.model.get('payments'), function (curModel) {
      var cashPaymentMethodInfo = {
        paymentMethodId: 0,
        name: "",
        id: "",
        searchKey: "",
        startingCash: 0,
        totalSales: 0,
        totalReturns: 0,
        rate: 0,
        isocode: 0
      };
      cashPaymentMethodInfo.paymentMethodId = curModel.payment.id;
      cashPaymentMethodInfo.name = curModel.payment.name;
      cashPaymentMethodInfo.searchKey = curModel.payment.searchKey;
      var auxPay = payMthds.filter(function (payMthd) {
        return payMthd.get('paymentmethod_id') === curModel.payment.id;
      })[0];
      if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
        return;
      }
      cashPaymentMethodInfo.id = auxPay.get('id');
      cashPaymentMethodInfo.startingCash = auxPay.get('startingCash');
      cashPaymentMethodInfo.totalSales = auxPay.get('totalSales');
      cashPaymentMethodInfo.totalReturns = auxPay.get('totalReturns');
      cashPaymentMethodInfo.totalDeposits = auxPay.get('totalDeposits');
      cashPaymentMethodInfo.totalDrops = auxPay.get('totalDrops');
      cashPaymentMethodInfo.rate = curModel.rate;
      cashPaymentMethodInfo.isocode = curModel.isocode;
      cashPaymentMethodInfo.usedInCurrentTrx = auxPay.get('usedInCurrentTrx');
      cashPaymentMethodInfo.newPaymentMethod = auxPay.get('newPaymentMethod');
      cashPaymentMethodInfo.paymentmethod_id = auxPay.get('paymentmethod_id');
      objToSend.get('cashPaymentMethodInfo').push(cashPaymentMethodInfo);
      cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    }, this);
    var i;
    for (i = 0; i < payMthds.length; i++) {
      payMthds.at(i).set('newPaymentMethod', false);
      OB.Dal.save(payMthds.at(i), null, null, false);
    }
  };

  OB.UTIL.saveComposeInfo = function (me, callback, objToSend, cashUp, tx) {
    cashUp.at(0).set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend.set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend.set('organization', OB.MobileApp.model.get('terminal').organization);
    cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    OB.Dal.saveInTransaction(tx, cashUp.at(0), function () {
      if (callback) {
        callback(cashUp, me);
      }
    }, null);
  };

  OB.UTIL.getTaxCashUp = function (taxcashups, objToSend, cashUp) {
    _.each(taxcashups.models, function (currentTax) {
      var cashTaxInfo = {
        name: "",
        amount: 0,
        orderType: 0,
        cashupId: "",
        id: ""
      };
      cashTaxInfo.name = currentTax.get('name');
      cashTaxInfo.amount = currentTax.get('amount');
      cashTaxInfo.orderType = currentTax.get('orderType');
      cashTaxInfo.cashupId = currentTax.get('cashup_id');
      cashTaxInfo.id = currentTax.get('id');
      objToSend.get('cashTaxInfo').push(cashTaxInfo);
      cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    }, this);
  };

  OB.UTIL.composeCashupInfo = function (cashUp, me, callback, tx) {
    cashUp.at(0).set('lastcashupeportdate', OB.I18N.normalizeDate(new Date()));
    cashUp.at(0).set('transitionsToOnline', OB.UTIL.localStorage.getItem('transitionsToOnline'));
    cashUp.at(0).set('logclientErrors', OB.UTIL.localStorage.getItem('logclientErrors'));
    var objToSend = new Backbone.Model({
      posterminal: OB.MobileApp.model.get('terminal').id,
      posTerminal: OB.MobileApp.model.get('terminal').id,
      id: cashUp.at(0).get('id'),
      isprocessed: cashUp.at(0).get('isprocessed'),
      isbeingprocessed: cashUp.at(0).get('isbeingprocessed'),
      netSales: cashUp.at(0).get('netSales'),
      grossSales: cashUp.at(0).get('grossSales'),
      netReturns: cashUp.at(0).get('netReturns'),
      grossReturns: cashUp.at(0).get('grossReturns'),
      totalRetailTransactions: cashUp.at(0).get('totalRetailTransactions'),
      cashPaymentMethodInfo: [],
      cashTaxInfo: [],
      cashCloseInfo: [],
      cashUpDate: "",
      creationDate: (new Date(cashUp.at(0).get('creationDate'))).toISOString(),
      lastcashupeportdate: cashUp.at(0).get('lastcashupeportdate'),
      transitionsToOnline: cashUp.at(0).get('transitionsToOnline'),
      logclientErrors: cashUp.at(0).get('logclientErrors')
    });

    //process the payment method cash ups
    OB.Dal.findInTransaction(tx, OB.Model.PaymentMethodCashUp, {
      'cashup_id': cashUp.at(0).get('id'),
      '_orderByClause': 'name asc'
    }, function (payMthds) {
      OB.UTIL.getPaymethodCashUp(payMthds, objToSend, cashUp);

      //process the taxs cash ups
      OB.Dal.findInTransaction(tx, OB.Model.TaxCashUp, {
        'cashup_id': cashUp.at(0).get('id'),
        '_orderByClause': 'name asc'
      }, function (taxcashups) {
        OB.UTIL.getTaxCashUp(taxcashups, objToSend, cashUp);
        OB.UTIL.saveComposeInfo(me, callback, objToSend, cashUp, tx);
      });
    });
  };

  OB.UTIL.updateCashupCreationDate = function () {
    OB.Dal.get(OB.Model.CashUp, OB.MobileApp.model.get('terminal').cashUpId, function (cashup) {
      if (cashup) {
        cashup.set('creationDate', OB.I18N.normalizeDate(new Date()));
        OB.Dal.save(cashup);
      }
    });
  };
}());