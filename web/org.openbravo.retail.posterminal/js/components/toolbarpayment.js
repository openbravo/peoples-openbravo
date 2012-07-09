/*global B, $, _ */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  function getPayment(modalpayment, receipt, key, name, provider) {
    return ({
      'permission': key,
      'stateless': provider,
      'action': function(txt) {
        var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
        amount = _.isNaN(amount) ? receipt.getPending() : amount;
        var providerview = OB.POS.paymentProviders[provider];
        if (providerview) {
          modalpayment.show(receipt, key, name, providerview, amount);
        } else {
          receipt.addPayment(new OB.Model.PaymentLine({
            'kind': key,
            'name': name,
            'amount': amount
          }));
        }
      }
    });
  }

  OB.UI.ToolbarPayment = function(options) {
    var i, max, payments;

    this.toolbar = [];
    this.receipt = options.modelorder;
    this.modalpayment = new OB.UI.ModalPayment(options).render();
    $('body').append(this.modalpayment.$el);

    payments = OB.POS.modelterminal.get('payments');

    for (i = 0, max = payments.length; i < max; i++) {
      this.toolbar.push({
        command: payments[i].searchKey,
        definition: getPayment(this.modalpayment, this.receipt, payments[i].searchKey, payments[i]._identifier, payments[i].provider),
        label: payments[i]._identifier
      });
    }
  };
}());