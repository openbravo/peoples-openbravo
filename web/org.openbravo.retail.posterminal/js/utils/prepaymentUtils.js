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

  OB.UTIL.PrepaymentUtils.managePrepaymentChange = function (receipt, paymentAdded, payments, callback) {
    // This method is used both from single and multi order, so for any change in the future, just have it in mind
    if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && OB.MobileApp.model.hasPermission('OBPOS_GenerateChangeWithPrepayments', true)) {
      var addedPaymentAmount, amountBeforeLastPayment, newCashPaymentsAmount, calculatePrepayments = OB.MobileApp.model.get('terminal').terminalType.calculateprepayments,
          paymentStatus = receipt.getPaymentStatus(),
          prepaymentAmount = receipt.get('obposPrepaymentamt'),
          pendingPrepayment = OB.DEC.sub(OB.DEC.add(prepaymentAmount, paymentStatus.pendingAmt), paymentStatus.totalAmt),
          receiptHasPrepaymentAmount = receipt.get('obposPrepaymentlimitamt') < receipt.getTotal() && prepaymentAmount !== 0 && prepaymentAmount !== paymentStatus.totalAmt;

      if (paymentAdded.get('rate') && paymentAdded.get('rate') !== '1') {
        addedPaymentAmount = paymentAdded.get('origAmount');
      } else {
        addedPaymentAmount = paymentAdded.get('amount');
      }
      amountBeforeLastPayment = OB.DEC.sub(receipt.get('payment'), addedPaymentAmount);

      newCashPaymentsAmount = payments.reduce(function (sum, payment) {
        if (payment.get('isCash') && !payment.get('isPrepayment') && payment.get('origAmount') > 0) {
          return OB.DEC.add(sum, payment.get('origAmount'));
        }
        return sum;
      }, 0);

      if (newCashPaymentsAmount && calculatePrepayments && pendingPrepayment < 0 && receiptHasPrepaymentAmount && amountBeforeLastPayment < prepaymentAmount && receipt.get('payment') < receipt.getTotal()) {
        OB.MobileApp.view.waterfallDown('onShowPopup', {
          popup: 'modalDeliveryChange',
          args: {
            receipt: receipt,
            deliveryChange: Math.min(newCashPaymentsAmount, OB.DEC.abs(pendingPrepayment)),
            callback: function () {
              if (callback instanceof Function) {
                receipt.adjustPayment();
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

  };

}());