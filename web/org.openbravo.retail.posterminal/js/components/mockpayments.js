/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.MockPayment = Backbone.View.extend({
    tagName: 'div',
    initialize: function () {
      
      var me = this;
      var okbutton = OB.COMP.Button.extend({
        render: function() {
          this.$el.text('OK');
          return this;
        },
        clickEvent: function (e) {
          me.receipt.addPayment(new OB.MODEL.PaymentLine(
            {
              'kind':me.key,
              'name': me.name,
              'amount': me.amount
            }));
          me.$el.parents('.modal').filter(':first').modal('hide'); // If in a modal dialog, close it
        }        
      });
      this.$el.append(new okbutton().render().$el);
    },
    show: function (receipt, key, name, amount) {
      // this.$el.append($('<div/>').text(key + name + amount));
      this.receipt = receipt;
      this.key = key;
      this.name = name;
      this.amount = amount;
    }
  });  
      
  // register
  OB.POS.paymentProviders.MockPayment = OB.COMP.MockPayment;
}());