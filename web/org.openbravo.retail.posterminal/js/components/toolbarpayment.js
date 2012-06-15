/*global define, $ */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'model/order', 'model/terminal'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var getPayment = function (modalpayment, receipt, key, name, provider) {
    return ({
      'permission': key,
      'action': function (txt) {
        
        var providerview = OB.POS.paymentProviders[provider];
        if (providerview) {
          modalpayment.show(receipt, key, name, providerview, OB.DEC.number(parseFloat(txt, 10)));
        } else {
          receipt.addPayment(new OB.MODEL.PaymentLine(
            {
              'kind': key,
              'name': name,
              'amount': OB.DEC.number(parseFloat(txt, 10))
            }));
        }
      }
    });
  };

  OB.COMP.ToolbarPayment = function (options) {
    var i, max, payments;

    this.toolbar = [];
    this.receipt = options.modelorder;
    this.modalpayment = new OB.COMP.ModalPayment(options).render();
    $('body').append(this.modalpayment.$el);
    
    payments = OB.POS.modelterminal.get('payments');

    for (i = 0, max = payments.length; i < max; i++) {
      this.toolbar.push({command: payments[i].searchKey, definition: getPayment(this.modalpayment, this.receipt, payments[i].searchKey, payments[i]._identifier, payments[i].provider), label: payments[i]._identifier});
    }
  };
});

