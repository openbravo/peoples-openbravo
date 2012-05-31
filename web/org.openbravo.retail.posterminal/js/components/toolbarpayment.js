/*global define, $ */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'model/order', 'model/terminal', 'components/commonbuttons', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  var getPayment = function (receipt, key, name) {
    return ({
      'permission': key,
      'action': function (txt) {
        receipt.addPayment(new OB.MODEL.PaymentLine(
          {
            'kind': key,
            'name': name,
            'amount': OB.DEC.number(parseFloat(txt, 10))
          }));
      }
    });
  };
  
  OB.COMP.ToolbarPayment = function (context) {
    var i, max, payments;
    
    this.toolbar = [];
    this.receipt = context.modelorder;
    payments = OB.POS.modelterminal.get('payments');
    
    for (i = 0, max = payments.length; i < max; i++) {
      this.toolbar.push({command: payments[i].searchKey, definition: getPayment(this.receipt, payments[i].searchKey, payments[i]._identifier), label: payments[i]._identifier}); 
    }
  };  
});

