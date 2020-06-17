/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, Backbone */

(function() {
  OB.UTIL = window.OB.UTIL || {};

  OB.UTIL.getPaymethodCashUp = function(payMthds, objToSend, cashUp, tx) {
    _.each(
      OB.MobileApp.model.get('payments'),
      function(curModel) {
        var cashPaymentMethodInfo = {
          paymentMethodId: 0,
          name: '',
          id: '',
          searchKey: '',
          startingCash: 0,
          totalSales: 0,
          totalReturns: 0,
          rate: 0,
          isocode: 0
        };
        cashPaymentMethodInfo.paymentMethodId = curModel.payment.id;
        cashPaymentMethodInfo.name = curModel.payment.name;
        cashPaymentMethodInfo.searchKey = curModel.payment.searchKey;
        var auxPay = payMthds.filter(function(payMthd) {
          return payMthd.get('paymentmethod_id') === curModel.payment.id;
        })[0];
        if (!auxPay) {
          //We cannot find this payment in local database, it must be a new payment method, we skip it.
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
      },
      this
    );
    var i;
    for (i = 0; i < payMthds.length; i++) {
      payMthds.at(i).set('newPaymentMethod', false);
      OB.Dal.saveInTransaction(tx, payMthds.at(i), null, null, false);
    }
  };

  OB.UTIL.saveComposeInfo = function(me, callback, objToSend, cashUp, tx) {
    cashUp.at(0).set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend.set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend.set(
      'organization',
      OB.MobileApp.model.get('terminal').organization
    );
    cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    OB.Dal.saveInTransaction(
      tx,
      cashUp.at(0),
      function() {
        if (callback) {
          callback(cashUp, me);
        }
      },
      null
    );
  };

  OB.UTIL.getTaxCashUp = function(taxcashups, objToSend, cashUp) {
    _.each(
      taxcashups.models,
      function(currentTax) {
        var cashTaxInfo = {
          name: '',
          amount: 0,
          orderType: 0,
          cashupId: '',
          id: ''
        };
        cashTaxInfo.name = currentTax.get('name');
        cashTaxInfo.amount = currentTax.get('amount');
        cashTaxInfo.orderType = currentTax.get('orderType');
        cashTaxInfo.cashupId = currentTax.get('cashup_id');
        cashTaxInfo.id = currentTax.get('id');
        objToSend.get('cashTaxInfo').push(cashTaxInfo);
        cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
      },
      this
    );
  };

  OB.UTIL.composeCashupInfo = function(cashUp, me, callback, tx) {
    cashUp.at(0).set('lastcashupeportdate', OB.I18N.normalizeDate(new Date()));
    cashUp
      .at(0)
      .set(
        'transitionsToOnline',
        OB.UTIL.localStorage.getItem('transitionsToOnline')
      );
    cashUp
      .at(0)
      .set('logclientErrors', OB.UTIL.localStorage.getItem('logclientErrors'));
    if (
      !OB.UTIL.isNullOrUndefined(
        OB.UTIL.localStorage.getItem('totalLatencyTime')
      )
    ) {
      cashUp
        .at(0)
        .set(
          'averageLatency',
          parseInt(OB.UTIL.localStorage.getItem('totalLatencyTime'), 10) /
            parseInt(OB.UTIL.localStorage.getItem('totalLatencyMeasures'), 10)
        );
    }
    if (
      !OB.UTIL.isNullOrUndefined(
        OB.UTIL.localStorage.getItem('totalUploadBandwidth')
      )
    ) {
      cashUp
        .at(0)
        .set(
          'averageUploadBandwidth',
          parseInt(OB.UTIL.localStorage.getItem('totalUploadBandwidth'), 10) /
            parseInt(OB.UTIL.localStorage.getItem('totalUploadMeasures'), 10)
        );
    }
    if (
      !OB.UTIL.isNullOrUndefined(
        OB.UTIL.localStorage.getItem('totalDownloadBandwidth')
      )
    ) {
      cashUp
        .at(0)
        .set(
          'averageDownloadBandwidth',
          parseInt(OB.UTIL.localStorage.getItem('totalDownloadBandwidth'), 10) /
            parseInt(OB.UTIL.localStorage.getItem('totalDownloadMeasures'), 10)
        );
    }
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
      cashUpDate: '',
      creationDate: new Date(cashUp.at(0).get('creationDate')).toISOString(),
      lastcashupeportdate: cashUp.at(0).get('lastcashupeportdate'),
      transitionsToOnline: cashUp.at(0).get('transitionsToOnline'),
      logclientErrors: cashUp.at(0).get('logclientErrors'),
      averageLatency: cashUp.at(0).get('averageLatency'),
      averageUploadBandwidth: cashUp.at(0).get('averageUploadBandwidth'),
      averageDownloadBandwidth: cashUp.at(0).get('averageDownloadBandwidth')
    });

    //process the payment method cash ups
    OB.Dal.findInTransaction(
      tx,
      OB.Model.PaymentMethodCashUp,
      {
        cashup_id: cashUp.at(0).get('id'),
        _orderByClause: 'name asc'
      },
      function(payMthds) {
        OB.UTIL.getPaymethodCashUp(payMthds, objToSend, cashUp, tx);

        //process the taxs cash ups
        OB.Dal.findInTransaction(
          tx,
          OB.Model.TaxCashUp,
          {
            cashup_id: cashUp.at(0).get('id'),
            _orderByClause: 'name asc'
          },
          function(taxcashups) {
            OB.UTIL.getTaxCashUp(taxcashups, objToSend, cashUp);
            OB.UTIL.saveComposeInfo(me, callback, objToSend, cashUp, tx);
          }
        );
      }
    );
  };

  OB.UTIL.closeCashAddPaymentWithMovement = function(
    paymentWithMovement,
    values
  ) {
    _.each(values, function(value) {
      var searchKey = value.get('searchKey');
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === searchKey;
      });
      if (!item && value.get('paymentMethodId')) {
        paymentWithMovement.push({
          searchKey: searchKey
        });
        var paymentMethod = _.find(OB.MobileApp.model.get('payments'), function(
          p
        ) {
          return p.payment.id === value.get('paymentMethodId');
        });
        if (paymentMethod) {
          searchKey = paymentMethod.payment.searchKey;
          item = _.find(paymentWithMovement, function(p) {
            return p.searchKey === searchKey;
          });
        }
      }
      if (!item && value.get('amount') !== 0) {
        paymentWithMovement.push({
          searchKey: searchKey
        });
      }
    });
  };

  OB.UTIL.closeCashAddPaymentWithSummaryMovement = function(
    paymentWithMovement,
    values
  ) {
    _.each(values, function(value) {
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === value.get('searchKey');
      });
      if (!item && value.get('value') !== 0) {
        paymentWithMovement.push({
          searchKey: value.get('searchKey')
        });
      }
    });
  };

  OB.UTIL.closeCashGetPaymentWithMovement = function(
    paymentWithMovement,
    values
  ) {
    var filtered = _.filter(values, function(value) {
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === value.get('searchKey');
      });
      return item !== undefined;
    });
    return filtered;
  };
})();
