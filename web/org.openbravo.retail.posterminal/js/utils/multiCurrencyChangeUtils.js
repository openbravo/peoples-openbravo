/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

(function () {

  OB.UTIL.MultiCurrencyChange = {};

  OB.UTIL.MultiCurrencyChange.multiChange = function (paymentstatus, payment, context) {

    var amountInOtherCurrency, changeLessThan = payment.paymentMethod.changeLessThan,
        linkedChangePaymentId = payment.paymentMethod.changePaymentType,
        changeLabelContent = OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate), payment.symbol, payment.currencySymbolAtTheRight),
        linkedPaymentMethod;

    //Here should be the logic to implement multiChange // Might be interesting to make differences between 2 or more cash paymentMethods
    if (OB.MobileApp.model.get('terminal').multiChange && paymentstatus.changeAmt) {
      //If activated and there is change use the rules of change to calculate how to set the change
      if (changeLessThan && linkedChangePaymentId && OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate) % changeLessThan) {
        amountInOtherCurrency = OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate) % changeLessThan;

        linkedPaymentMethod = OB.MobileApp.model.get('payments').find(function (p) {
          return p.paymentMethod.id === linkedChangePaymentId;
        });
        changeLabelContent = OB.I18N.formatCurrencyWithSymbol(OB.DEC.sub(OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate), OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate) % changeLessThan), payment.symbol, payment.currencySymbolAtTheRight) + ' + ' + OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(amountInOtherCurrency, linkedPaymentMethod.mulrate), linkedPaymentMethod.symbol, linkedPaymentMethod.currencySymbolAtTheRight);
      }
    }
    context.$.change.setContent(changeLabelContent);
    OB.MobileApp.model.set('changeReceipt', changeLabelContent);
  };
}());