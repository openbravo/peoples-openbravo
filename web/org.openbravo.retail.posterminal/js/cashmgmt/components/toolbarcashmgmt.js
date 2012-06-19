/*global B, define, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var getPayment = function (receipt, key, name) {
    return ({
      'permission': key,
      'action': function (txt) {
        this.options.cashchangebutton.$el.click();
      }
    });
  };

  OB.COMP.ToolbarCashMgmt = function (context) {
    var i, max, payments;

    this.toolbar = [];
    this.receipt = context.modelorder;
    payments = OB.POS.modelterminal.get('payments');

    for (i = 0, max = payments.length; i < max; i++) {
      this.toolbar.push({command: payments[i].searchKey+' Deposit', definition: getPayment(this.receipt, payments[i].searchKey, payments[i]._identifier), label: payments[i]._identifier+' Deposit'});
      this.toolbar.push({command: payments[i].searchKey+' Drop', definition: getPayment(this.receipt, payments[i].searchKey, payments[i]._identifier), label: payments[i]._identifier+' Drop'});
    }
  };
}());

