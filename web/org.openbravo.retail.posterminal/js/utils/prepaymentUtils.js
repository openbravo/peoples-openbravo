/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.PrepaymentUtils = OB.UTIL.PrepaymentUtils || {};

  OB.UTIL.PrepaymentUtils.managePrepaymentChange = function(
    receipt,
    payment,
    payments,
    callback
  ) {
    // This method is used both from single and multi order, so for any change in the future, just have it in mind
    var calculatePrepayments = OB.MobileApp.model.get('terminal').terminalType
      .calculateprepayments;
    if (
      calculatePrepayments &&
      OB.MobileApp.model.hasPermission(
        'OBPOS_GenerateChangeWithPrepayments',
        true
      )
    ) {
      var paymentStatus = receipt.getPaymentStatus(),
        paidAmount = receipt.getPaymentWithSign(),
        prepaymentAmount = receipt.get('obposPrepaymentamt');
      if (
        !paymentStatus.isNegative &&
        paidAmount < prepaymentAmount &&
        payment.get('isCash')
      ) {
        var paymentAmount, newPaidAmount;

        if (payment.get('rate') && payment.get('rate') !== '1') {
          paymentAmount = OB.DEC.div(
            payment.get('amount'),
            payment.get('mulrate')
          );
        } else {
          paymentAmount = payment.get('amount');
        }

        newPaidAmount = OB.DEC.add(paidAmount, paymentAmount);

        if (
          newPaidAmount > prepaymentAmount &&
          newPaidAmount < receipt.getGross()
        ) {
          OB.MobileApp.view.waterfallDown('onShowPopup', {
            popup: 'modalDeliveryChange',
            args: {
              receipt: receipt,
              deliveryChange: OB.DEC.sub(newPaidAmount, prepaymentAmount),
              callback: function() {
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
})();
