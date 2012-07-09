/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var getPayment = function (modalpayment, receipt, key, name, provider) {
    return ({
      'permission': key,
      'action': function (txt) {
        var providerview = OB.POS.paymentProviders[provider];
        if (providerview) {
          modalpayment.show(receipt, key, name, providerview, OB.DEC.number(OB.I18N.parseNumber(txt)));
        } else {
        var totalCounted = 0;
        var counted = 0;
        this.options.modeldaycash.paymentmethods.each(function(payment){
        if(payment.get('name')===name){
          payment.set('counted',OB.DEC.add(0,txt));
          counted=OB.DEC.add(0,txt);
        }
        totalCounted= OB.DEC.add(totalCounted,payment.get('counted'));
        });
        this.options.modeldaycash.set('totalCounted',totalCounted);
        this.options.modeldaycash.set('totalDifference',OB.DEC.sub(totalCounted,this.options.modeldaycash.get('totalExpected')));
        $('button[button*="allokbutton"]').css('visibility','hidden');
        $('button[button="okbutton"][key*="'+key+'"]').hide();
        $('div[searchKey*="'+key+'"]').text(counted.toString());
        $('div[searchKey*="'+key+'"]').show();
        if($('button[button="okbutton"][style!="display: none; "]').length===0){
          this.options.closenextbutton.$el.removeAttr('disabled');
        }
        }
      }
    });
  };

  OB.COMP.ToolbarCountCash = function (options) {
    var i, max, payments;

    this.toolbar = [];
    this.receipt = options.modelorder;
    this.modalpayment = new OB.UI.ModalPayment(options).render();
    $('body').append(this.modalpayment.$el);
    payments = OB.POS.modelterminal.get('payments');

    for (i = 0, max = payments.length; i < max; i++) {
      this.toolbar.push({command: payments[i].searchKey, definition: getPayment(this.modalpayment, this.receipt, payments[i].searchKey, payments[i]._identifier, payments[i].provider), label: payments[i]._identifier});
    }
  };
}());

