/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, Backbone */

(function () {

  OB.UTIL = window.OB.UTIL || {};

  function findAndSave(cashuptaxes, i, finishCallback) {

    if (i < cashuptaxes.length) {
      OB.Dal.find(OB.Model.TaxCashUp, {
        'cashup_id': cashuptaxes[i].cashupID,
        'name': cashuptaxes[i].taxName,
        'orderType': cashuptaxes[i].taxOrderType
      }, function (tax) {
        if (tax.length === 0) {
          OB.Dal.save(new OB.Model.TaxCashUp({
            name: cashuptaxes[i].taxName,
            amount: cashuptaxes[i].taxAmount,
            orderType: cashuptaxes[i].taxOrderType,
            cashup_id: cashuptaxes[i].cashupID
          }), function () {
            findAndSave(cashuptaxes, i + 1, finishCallback);
          }, null);
        } else {
          tax.at(0).set('amount', OB.DEC.add(tax.at(0).get('amount'), cashuptaxes[i].taxAmount));
          OB.Dal.save(tax.at(0), function () {
            findAndSave(cashuptaxes, i + 1, finishCallback);
          }, null);
        }
      });
    } else {
      if (finishCallback) {
        finishCallback();
      }
    }
  }

  function updateCashUpInfo(cashUp, receipt, j, callback) {
    var cashuptaxes, order, orderType, gross, i, taxOrderType, taxAmount, auxPay;
    if (j < receipt.length) {
      order = receipt[j];
      orderType = order.get('orderType');
      if (cashUp.length !== 0) {
        _.each(order.get('lines').models, function (line) {
          if (order.get('priceIncludesTax')) {
            gross = line.get('lineGrossAmount');
          } else {
            gross = line.get('discountedGross');
          }
          //Sales order: Positive line
          if (line.get('qty') > 0 && orderType !== 3 && !order.get('isLayaway')) {
            cashUp.at(0).set('netSales', OB.DEC.add(cashUp.at(0).get('netSales'), line.get('net')));
            cashUp.at(0).set('grossSales', OB.DEC.add(cashUp.at(0).get('grossSales'), gross));
            //Return from customer or Sales with return: Negative line
          } else if (line.get('qty') < 0 && orderType !== 3 && !order.get('isLayaway')) {
            cashUp.at(0).set('netReturns', OB.DEC.add(cashUp.at(0).get('netReturns'), -line.get('net')));
            cashUp.at(0).set('grossReturns', OB.DEC.add(cashUp.at(0).get('grossReturns'), -gross));
            //Void Layaway
          } else if (orderType === 3) {
            if (line.get('qty') > 0) {
              cashUp.at(0).set('netSales', OB.DEC.add(cashUp.at(0).get('netSales'), -line.get('net')));
              cashUp.at(0).set('grossSales', OB.DEC.add(cashUp.at(0).get('grossSales'), -gross));
            } else {
              cashUp.at(0).set('netReturns', OB.DEC.add(cashUp.at(0).get('netReturns'), line.get('net')));
              cashUp.at(0).set('grossReturns', OB.DEC.add(cashUp.at(0).get('grossReturns'), gross));
            }
          }
        });
        cashUp.at(0).set('totalRetailTransactions', OB.DEC.sub(cashUp.at(0).get('grossSales'), cashUp.at(0).get('grossReturns')));
        OB.Dal.save(cashUp.at(0), null, null);

        // group and sum the taxes
        cashuptaxes = [];
        order.get('lines').each(function (line, taxIndex) {
          var taxLines, taxLine;
          taxLines = line.get('taxLines');
          if (orderType === 1 || line.get('qty') < 0) {
            taxOrderType = 1;
          } else {
            taxOrderType = 0;
          }

          _.each(taxLines, function (taxLine) {
            if (line.get('qty') > 0 && orderType !== 3) {
              taxAmount = taxLine.amount;
            } else {
              taxAmount = -taxLine.amount;
            }
            cashuptaxes.push({
              taxName: taxLine.name,
              taxAmount: taxAmount,
              taxOrderType: taxOrderType.toString(),
              cashupID: cashUp.at(0).get('id')
            });
          });
        });

        OB.Dal.find(OB.Model.PaymentMethodCashUp, {
          'cashup_id': cashUp.at(0).get('id')
        }, function (payMthds) { //OB.Dal.find success
          _.each(order.get('payments').models, function (payment) {
            auxPay = payMthds.filter(function (payMthd) {
              return payMthd.get('searchKey') === payment.get('kind') && !payment.get('isPrePayment');
            })[0];
            if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
              return;
            }
            if (order.getGross() > 0 && (orderType === 0 || orderType === 2)) {
              auxPay.set('totalSales', OB.DEC.add(auxPay.get('totalSales'), payment.get('amount')));
            } else if (order.getGross() < 0 || orderType === 1) {
              auxPay.set('totalReturns', OB.DEC.add(auxPay.get('totalReturns'), payment.get('amount')));
            } else if (orderType === 3) {
              auxPay.set('totalSales', OB.DEC.sub(auxPay.get('totalSales'), payment.get('amount')));
            }
            OB.Dal.save(auxPay, null, null);
          }, this);
          findAndSave(cashuptaxes, 0, function () {
            OB.UTIL.composeCashupInfo(cashUp, null, null);
            updateCashUpInfo(cashUp, receipt, j + 1, callback);
          });
        });
      }
    } else if (typeof callback === 'function') {
      callback();
    }
  }

  OB.UTIL.cashUpReport = function (receipt, callback) {
    var auxPay, orderType, taxOrderType, taxAmount, gross;
    if (!Array.isArray(receipt)) {
      receipt = [receipt];
    }
    OB.Dal.find(OB.Model.CashUp, {
      'isprocessed': 'N'
    }, function (cashUp) {
      updateCashUpInfo(cashUp, receipt, 0, callback);
    });
  };

  OB.UTIL.deleteCashUps = function (cashUpModels) {
    var deleteCallback = function (models) {
        models.each(function (model) {
          //TODO: see the correct criterial for delete cashup
          OB.Dal.remove(model, null, function (tx, err) {
            OB.UTIL.showError(err);
          });
        });
        };
    _.each(cashUpModels.models, function (cashup) {
      var cashUpId = cashup.get('id');
      if (cashup.get('isprocessed') === 'Y') {
        OB.Dal.find(OB.Model.TaxCashUp, {
          cashup_id: cashUpId
        }, deleteCallback, null);
        OB.Dal.find(OB.Model.CashManagement, {
          cashup_id: cashUpId
        }, deleteCallback, null);
        OB.Dal.find(OB.Model.PaymentMethodCashUp, {
          cashup_id: cashUpId
        }, deleteCallback, null);
        OB.Dal.remove(cashup, null, function (tx, err) {
          OB.UTIL.showError(err);
        });
      }
    });
  };
  OB.UTIL.createNewCashupFromServer = function (cashup, callback) {
    OB.Dal.save(cashup, function () {

      // Create taxes
      _.each(cashup.get('cashTaxInfo'), function (taxCashup) {
        var taxModel = new OB.Model.TaxCashUp();
        taxModel.set(taxCashup);
        OB.Dal.save(taxModel, null, null, true);
      });

      //current cashup
      if (cashup.get('cashPaymentMethodInfo').length !== 0) {
        _.each(cashup.get('cashPaymentMethodInfo'), function (paymentMethodCashUp) {
          var paymentMethodCashUpModel = new OB.Model.PaymentMethodCashUp();
          paymentMethodCashUpModel.set(paymentMethodCashUp);
          OB.Dal.save(paymentMethodCashUpModel, null, null, true);
        });
      } else {
        OB.UTIL.initializePaymentMethodCashup(null, cashup);
      }

      if (callback) {
        callback();
      }
    }, function () {
      OB.MobileApp.model.get('terminal').cashUpId = cashup.get('id');
    }, true);

  };

  OB.UTIL.createNewCashup = function (callback) {
    // Create the cashup empty
    var uuid = OB.Dal.get_uuid();
    OB.Dal.save(new OB.Model.CashUp({
      id: uuid,
      netSales: OB.DEC.Zero,
      grossSales: OB.DEC.Zero,
      netReturns: OB.DEC.Zero,
      grossReturns: OB.DEC.Zero,
      totalRetailTransactions: OB.DEC.Zero,
      createdDate: new Date(),
      userId: OB.MobileApp.model.get('context').user.id,
      objToSend: null,
      cashTaxInfo: [],
      cashCloseInfo: [],
      isbeingprocessed: 'Y',
      posterminal: OB.MobileApp.model.get('terminal').id,
      isprocessed: 'N'
    }), function () {
      OB.MobileApp.model.get('terminal').cashUpId = uuid;
      // Get Info from the last Cashup
      //1. Search in local
      var criteria = {
        'isprocessed': 'Y',
        '_orderByClause': 'createdDate desc'
      };
      OB.Dal.find(OB.Model.CashUp, criteria, function (lastCashUp) {
        var lastCashUpPayments;
        if (lastCashUp.length !== 0) {
          lastCashUpPayments = JSON.parse(lastCashUp.at(0).get('objToSend')).cashCloseInfo;
          OB.UTIL.initializePaymentMethodCashup(lastCashUpPayments);
          if (callback) {
            callback();
          }
        } else {
          //2. Search in server
          new OB.DS.Process('org.openbravo.retail.posterminal.master.Cashup').exec({
            isprocessed: 'Y'
          }, function (data) {
            if (data[0]) {
              lastCashUp = new OB.Model.CashUp();
              lastCashUp.set(data[0]);
              lastCashUpPayments = lastCashUp.get('cashPaymentMethodInfo');
              //lastCashUpPayments = JSON.parse(lastCashUp.get('cashPaymentMethodInfo'));
            } else {
              // Set all  to 0
              lastCashUpPayments = null;
            }
            OB.UTIL.initializePaymentMethodCashup(lastCashUpPayments, null, true);
            if (callback) {
              callback();
            }
          }, function () {
            // error
            //console.error("OB.Model.CashUp fail");
          });
        }
      }, function () {
        // error
        //console.error("OB.Model.CashUp find");
      });
    }, function () {
      // error
      //console.error("OB.Model.CashUp fail");
    }, true);

  };
  OB.UTIL.initializePaymentMethodCashup = function (lastCashUpPayments, cashup, funcType) {
    _.each(OB.MobileApp.model.get('payments'), function (payment) {
      var startingCash = payment.currentBalance,
          pAux, cashupId;
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
          startingCash = pAux.paymentMethod.amountToKeep;
        }
      } else {
        startingCash = OB.DEC.Zero;
      }
      OB.Dal.save(new OB.Model.PaymentMethodCashUp({
        id: OB.Dal.get_uuid(),
        paymentmethod_id: payment.payment.id,
        searchKey: payment.payment.searchKey,
        name: payment.payment._identifier,
        startingCash: startingCash,
        totalSales: OB.DEC.Zero,
        totalReturns: OB.DEC.Zero,
        rate: payment.rate,
        isocode: payment.isocode,
        cashup_id: cashupId
      }), null, null, true);
    }, this);
  };

  OB.UTIL.initCashUp = function (callback) {

    //1. Search non processed cashup in local DB
    //2. Search non processed cashup in backoffice DB
    //2.1 Search payments and taxes
    //3. Create new Cashup
    //3.1 Using processed cashup info in local or using processed cashup info in Back office
    var criteria = {
      'isprocessed': 'N',
      '_orderByClause': 'createdDate desc'
    };
    OB.Dal.find(OB.Model.CashUp, criteria, function (cashUp) { //OB.Dal.find success
      if (cashUp.length === 0) {
        // Search in the backoffice
        new OB.DS.Process('org.openbravo.retail.posterminal.master.Cashup').exec({
          isprocessed: 'N'
        }, function (data) {
          // Found non processed cashups
          if (data[0]) {
            cashUp = new OB.Model.CashUp();
            cashUp.set(data[0]);
            OB.UTIL.createNewCashupFromServer(cashUp, function (callback) {
              OB.UTIL.calculateCurrentCash(callback);
            });
          } else {
            OB.UTIL.createNewCashup(callback);
          }
        });
      } else {
        if (!OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
          OB.MobileApp.model.get('terminal').cashUpId = cashUp.at(0).get('id');
        }
        if (callback) {
          callback();
        }
      }
    });
  };

  OB.UTIL.calculateCurrentCash = function (callback) {
    var me = this;
    OB.Dal.find(OB.Model.CashUp, {
      'isprocessed': 'N'
    }, function (cashUp) {
      OB.Dal.find(OB.Model.PaymentMethodCashUp, {
        'cashup_id': cashUp.at(0).get('id')
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
          OB.Dal.find(OB.Model.CashManagement, {
            'cashup_id': cashUp.at(0).get('id'),
            'paymentMethodId': paymentType.payment.id
          }, function (cashMgmts, args) {
            var startingCash = auxPay.get('startingCash'),
                rate = auxPay.get('rate'),
                totalSales = auxPay.get('totalSales'),
                totalReturns = auxPay.get('totalReturns'),
                cashMgmt = _.reduce(cashMgmts.models, function (accum, trx) {
                if (trx.get('type') === 'deposit') {
                  return OB.DEC.add(accum, trx.get('origAmount'));
                } else {
                  return OB.DEC.sub(accum, trx.get('origAmount'));
                }
              }, 0);

            var payment = OB.MobileApp.model.paymentnames[paymentType.payment.searchKey];
            cash = OB.DEC.add(OB.DEC.add(startingCash, OB.DEC.sub(totalSales, totalReturns)), cashMgmt);
            payment.currentCash = OB.UTIL.currency.toDefaultCurrency(payment.paymentMethod.currency, cash);
            payment.foreignCash = OB.UTIL.currency.toForeignCurrency(payment.paymentMethod.currency, cash);

            if (typeof callback === 'function') {
              callback();
            }
          }, null, {
            me: me
          });
        }, this);
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
      cashPaymentMethodInfo.totalReturn = auxPay.get('totalReturns');
      cashPaymentMethodInfo.rate = curModel.rate;
      cashPaymentMethodInfo.isocode = curModel.isocode;
      cashPaymentMethodInfo.paymentmethod_id = auxPay.get('paymentmethod_id');
      objToSend.get('cashPaymentMethodInfo').push(cashPaymentMethodInfo);
      cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    }, this);
  };

  OB.UTIL.saveComposeInfo = function (me, callback, objToSend, cashUp) {
    cashUp.at(0).set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend.set('userId', OB.MobileApp.model.get('context').user.id);
    cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    if (callback) {
      OB.Dal.save(cashUp.at(0), callback(me), null);
    } else {
      OB.Dal.save(cashUp.at(0), null, null);
    }
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

  OB.UTIL.composeCashupInfo = function (cashUp, me, callback) {
    var objToSend = new Backbone.Model({
      posterminal: OB.MobileApp.model.get('terminal').id,
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
      cashUpDate: ""
    });

    //process the payment method cash ups
    OB.Dal.find(OB.Model.PaymentMethodCashUp, {
      'cashup_id': cashUp.at(0).get('id'),
      '_orderByClause': 'name asc'
    }, function (payMthds) {
      OB.UTIL.getPaymethodCashUp(payMthds, objToSend, cashUp);

      //process the taxs cash ups
      OB.Dal.find(OB.Model.TaxCashUp, {
        'cashup_id': cashUp.at(0).get('id'),
        '_orderByClause': 'name asc'
      }, function (taxcashups) {
        OB.UTIL.getTaxCashUp(taxcashups, objToSend, cashUp);
        OB.UTIL.saveComposeInfo(me, callback, objToSend, cashUp);
      });
    });
  };
}());