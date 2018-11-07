/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo */

(function () {

  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.PrepaymentUtils = OB.UTIL.PrepaymentUtils || {};

  OB.UTIL.PrepaymentUtils.managePrepaymentChange = function (receipt, payment, payments, callback) {
    // This method is used both from single and multi order, so for any change in the future, just have it in mind
    var calculatePrepayments = OB.MobileApp.model.get('terminal').terminalType.calculateprepayments;
    if (calculatePrepayments && OB.MobileApp.model.hasPermission('OBPOS_GenerateChangeWithPrepayments', true)) {
      var paymentStatus = receipt.getPaymentStatus(),
          paidAmount = receipt.getPaymentWithSign();
      if (!paymentStatus.isNegative && paidAmount < receipt.getGross() && payment.get('isCash')) {
        var prepaymentAmount = receipt.get('obposPrepaymentamt'),
            pendingPrepayment = OB.DEC.sub(OB.DEC.add(prepaymentAmount, paymentStatus.pendingAmt), paymentStatus.totalAmt),
            receiptHasPrepaymentAmount = prepaymentAmount && prepaymentAmount !== paymentStatus.totalAmt,
            paymentAmount;

        if (payment.get('rate') && payment.get('rate') !== '1') {
          paymentAmount = payment.get('origAmount');
        } else {
          paymentAmount = payment.get('amount');
        }

        if (OB.DEC.add(paidAmount, paymentAmount) > prepaymentAmount) {
          OB.MobileApp.view.waterfallDown('onShowPopup', {
            popup: 'modalDeliveryChange',
            args: {
              receipt: receipt,
              deliveryChange: OB.DEC.sub(OB.DEC.add(paidAmount, paymentAmount), prepaymentAmount),
              callback: function () {
                receipt.adjustPayment();
                if (callback instanceof Function) {
                  callback(receipt);
                }
              }
            }
          });
        } else {
          if (callback instanceof Function) {
            callback(receipt);
          }
        }
      } else {
        if (callback instanceof Function) {
          callback(receipt);
        }
      }
    } else {
      if (callback instanceof Function) {
        callback(receipt);
      }
    }

  };

}());