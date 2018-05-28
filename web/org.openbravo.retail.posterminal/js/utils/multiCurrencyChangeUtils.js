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

  OB.UTIL.MultiCurrencyChange.isActive = function () {
    return OB.MobileApp.model.get('terminal').multiChange;
  };

  OB.UTIL.MultiCurrencyChange.multiChange = function (paymentstatus, payment, context) {

    var changeLessThan = payment.paymentMethod.changeLessThan,
        changeAmountConverted = OB.DEC.mul(paymentstatus.changeAmt, payment.mulrate),
        amountInOtherCurrency, linkedChangePaymentId = payment.paymentMethod.changePaymentType,
        changeLessThanMaxAmount, changeLabelContent = OB.I18N.formatCurrencyWithSymbol(changeAmountConverted, payment.symbol, payment.currencySymbolAtTheRight),
        linkedPaymentMethod, paymentSearchKey = payment.payment.searchKey,
        linkedPaymentSearchKey, multiCurrencyChangePayments = {},
        linkedPaymentAmount;

    //Here should be the logic to implement multiChange // Might be interesting to make differences between 2 or more cash paymentMethods
    if (OB.UTIL.MultiCurrencyChange.isActive() && paymentstatus.changeAmt) {
      //If activated and there is change use the rules of change to calculate how to set the change
      if (changeLessThan && linkedChangePaymentId && changeAmountConverted % changeLessThan) {

        amountInOtherCurrency = changeAmountConverted % changeLessThan;
        changeLessThanMaxAmount = OB.DEC.sub(changeAmountConverted, amountInOtherCurrency);

        linkedPaymentMethod = OB.MobileApp.model.get('payments').find(function (p) {
          return p.paymentMethod.id === linkedChangePaymentId;
        });

        linkedPaymentSearchKey = linkedPaymentMethod.payment.searchKey;
        linkedPaymentAmount = OB.DEC.mul(OB.DEC.sub(paymentstatus.changeAmt, OB.DEC.mul(changeLessThanMaxAmount, payment.rate)), linkedPaymentMethod.mulrate);
        if (changeLessThanMaxAmount !== 0) {
          changeLabelContent = OB.I18N.formatCurrencyWithSymbol(changeLessThanMaxAmount, payment.symbol, payment.currencySymbolAtTheRight) + ' + ' + OB.I18N.formatCurrencyWithSymbol(linkedPaymentAmount, linkedPaymentMethod.symbol, linkedPaymentMethod.currencySymbolAtTheRight);
        } else {
          changeLabelContent = OB.I18N.formatCurrencyWithSymbol(linkedPaymentAmount, linkedPaymentMethod.symbol, linkedPaymentMethod.currencySymbolAtTheRight);
        }
        multiCurrencyChangePayments[paymentSearchKey] = OB.DEC.sub(changeAmountConverted, amountInOtherCurrency);
        multiCurrencyChangePayments[linkedPaymentMethod.payment.searchKey] = linkedPaymentAmount;

        OB.MobileApp.model.set('multiCurrencyChangePayments', multiCurrencyChangePayments);
      }
    }
    context.$.change.setContent(changeLabelContent);
    OB.MobileApp.model.set('changeReceipt', changeLabelContent);
  };

  OB.UTIL.MultiCurrencyChange.getChangePayments = function (key) {
    if (key) {
      return OB.MobileApp.model.get('multiCurrencyChangePayments')[key];
    } else {
      return OB.MobileApp.model.get('multiCurrencyChangePayments');
    }
  };

  OB.UTIL.MultiCurrencyChange.getChangePaymentsSize = function () {
    return Object.keys(OB.MobileApp.model.get('multiCurrencyChangePayments')).length;
  };

}());