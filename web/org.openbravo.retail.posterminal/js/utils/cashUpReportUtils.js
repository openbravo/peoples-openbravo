/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.UTILS = window.OB.UTILS || {};

  OB.UTIL.cashUpReport = function (receipt, sucessCallback) {
    var auxPay, orderType, taxOrderType, taxAmount;
    OB.Dal.find(OB.Model.CashUp, {
      'isbeingprocessed': 'N'
    }, function (cashUp) {
      orderType = receipt.get('orderType');
      if (cashUp.length !== 0) {
        if (orderType === 0 || orderType === 2) {
          cashUp.at(0).set('netSales', OB.DEC.add(cashUp.at(0).get('netSales'), receipt.get('net')));
          cashUp.at(0).set('grossSales', OB.DEC.add(cashUp.at(0).get('grossSales'), receipt.get('gross')));
        } else if (orderType === 1) {
          cashUp.at(0).set('netReturns', OB.DEC.add(cashUp.at(0).get('netReturns'), -receipt.get('net')));
          cashUp.at(0).set('grossReturns', OB.DEC.add(cashUp.at(0).get('grossReturns'), -receipt.get('gross')));
        } else if (orderType === 3) {
          cashUp.at(0).set('netSales', OB.DEC.add(cashUp.at(0).get('netSales'), -receipt.get('net')));
          cashUp.at(0).set('grossSales', OB.DEC.add(cashUp.at(0).get('grossSales'), -receipt.get('gross')));
        }
        cashUp.at(0).set('totalRetailTransactions', OB.DEC.sub(cashUp.at(0).get('grossSales'), cashUp.at(0).get('grossReturns')));
        OB.Dal.save(cashUp.at(0), null, null);
        for (var i in receipt.get('taxes')) {
          if (orderType === 3) {
            taxOrderType = 0;
          } else if (orderType === 2) {
            taxOrderType = 0;
          } else {
            taxOrderType = orderType;
          }
          if (orderType !== 1 && orderType !== 3) {
            taxAmount = receipt.get('taxes')[i].amount;
          } else {
            taxAmount = -receipt.get('taxes')[i].amount;
          }
          OB.Dal.find(OB.Model.TaxCashUp, {
            'tax_id': i,
            'orderType': taxOrderType.toString()
          }, function (tax) {
            if (tax.length === 0) {
              OB.Dal.save(new OB.Model.TaxCashUp({
                tax_id: i,
                name: receipt.get('taxes')[i].name,
                amount: taxAmount,
                orderType: taxOrderType.toString(),
                cashup_id: cashUp.at(0).get('id')
              }), null, null);
            } else {
              tax.at(0).set('amount', OB.DEC.add(tax.at(0).get('amount'), taxAmount));
              OB.Dal.save(tax.at(0), null, null);
            }
          });
        }
        OB.Dal.find(OB.Model.PaymentMethodCashUp, {
          'cashup_id': cashUp.at(0).get('id')
        }, function (payMthds) { //OB.Dal.find success
          _.each(receipt.get('payments').models, function (payment) {
            auxPay = payMthds.filter(function (payMthd) {
              return payMthd.get('searchKey') === payment.get('kind');
            })[0];
            if (orderType === 0 || orderType === 2) {
              auxPay.set('totalSales', OB.DEC.add(auxPay.get('totalSales'), payment.get('amount')));
            } else if (orderType === 1) {
              auxPay.set('totalReturns', OB.DEC.add(auxPay.get('totalReturns'), payment.get('amount')));
            } else if (orderType === 3) {
              auxPay.set('totalSales', OB.DEC.sub(auxPay.get('totalSales'), payment.get('amount')));
            }
            OB.Dal.save(auxPay, null, null);
          }, this);
        });
      }
    });
  };

  OB.UTIL.initCashUp = function (callback) {
    var criteria = {
      'isbeingprocessed': 'N'
    },
        lastCashUpPayments;
    OB.Dal.find(OB.Model.CashUp, criteria, function (cashUp) { //OB.Dal.find success
      var uuid;
      if (cashUp.length === 0) {
        criteria = {
          'isbeingprocessed': 'Y',
          '_orderByClause': 'createdDate desc'
        };
        OB.Dal.find(OB.Model.CashUp, criteria, function (lastCashUp) {
          if (lastCashUp.length !== 0) {
            lastCashUpPayments = JSON.parse(lastCashUp.at(0).get('objToSend')).cashCloseInfo;
          }
          uuid = OB.Dal.get_uuid();
          OB.MobileApp.model.get('terminal').cashUpId = uuid;
          OB.Dal.save(new OB.Model.CashUp({
            id: uuid,
            netSales: '0',
            grossSales: '0',
            netReturns: '0',
            grossReturns: '0',
            totalRetailTransactions: '0',
            createdDate: new Date(),
            objToSend: null,
            isbeingprocessed: 'N'
          }), function () {
            _.each(OB.POS.modelterminal.get('payments'), function (payment) {
              var startingCash = '0';
              if (lastCashUpPayments) {
                startingCash = lastCashUpPayments.filter(function (payMthd) {
                  return payMthd.paymentTypeId === payment.payment.id;
                })[0].paymentMethod.amountToKeep;
              }
              OB.Dal.save(new OB.Model.PaymentMethodCashUp({
                id: OB.Dal.get_uuid(),
                paymentmethod_id: payment.payment.id,
                searchKey: payment.payment.searchKey,
                name: payment.payment._identifier,
                startingCash: startingCash,
                totalSales: '0',
                totalReturns: '0',
                rate: payment.rate,
                cashup_id: uuid
              }), null, null, true);
            }, this);
            if (callback) {
              callback();
            }
          }, null, true);
        }, null, this);
      } else {
        OB.MobileApp.model.get('terminal').cashUpId = cashUp.at(0).get('id');
        if (callback) {
          callback();
        }
      }
    });
  };

}());