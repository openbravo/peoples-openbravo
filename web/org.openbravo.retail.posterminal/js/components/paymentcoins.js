/*global B, $ */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.PaymentCoins = OB.COMP.Payment.extend({
    paymentButtons: [
    OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 50,
      classcolor: 'btnlink-lightblue'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 20,
      classcolor: 'btnlink-lightpink'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 10,
      classcolor: 'btnlink-lightgreen'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 5,
      classcolor: 'btnlink-wheat'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 1,
      classcolor: 'btnlink-lightgreen'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 0.50,
      classcolor: 'btnlink-orange'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 0.20,
      classcolor: 'btnlink-gray'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 0.10,
      classcolor: 'btnlink-lightblue'
    }), OB.COMP.PaymentButton.extend({
      paymenttype: 'OBPOS_payment.cash',
      amount: 0.05,
      classcolor: 'btnlink-lightpink'
    })]
  });
}());