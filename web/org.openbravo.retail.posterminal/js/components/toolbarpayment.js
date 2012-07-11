/*global Backbone, $, _ */

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
  
  OB.UI.ButtonSwitch = OB.COMP.Button.extend({
    className: 'btnkeyboard',

    clickEvent: function (e) {
      if (this.options.parent.keypad.name === 'coins') {
        this.options.parent.showKeypad(); // show index
      } else {
        this.options.parent.showKeypad('coins');
      }
      this.render();
    },
    render: function() {
      // this.$el.text(this.options.parent.keypad.label);
      this.$el.text('$,$,$,..');
      return this;
    }
    
  });
  
  OB.UI.ToolbarPayment = Backbone.View.extend({
    'tag': 'div',
    attributes: {'style': 'display:none'},
    
    
    initialize: function () {
      var i, max, payments, Btn, inst, cont;
      
      this.modalpayment = new OB.UI.ModalPayment({parent: this.options.parent}).render();
      $('body').append(this.modalpayment.$el);     
      
      payments = OB.POS.modelterminal.get('payments');
      
      for (i = 0, max = payments.length; i < max; i++) {
           
        // this.options.parent.addCommand(payments[i].searchKey, definition);
     
        Btn = OB.COMP.ButtonKey.extend({
          command: payments[i].searchKey,
          definition: getPayment(this.modalpayment, this.options.parent.receipt, payments[i].searchKey, payments[i]._identifier, payments[i].provider),
          classButtonActive: 'btnactive-green',
          permission: payments[i].searchKey,
          contentViewButton: [payments[i]._identifier]
        });
        inst = new Btn({parent: this.options.parent}).render();
        this.$el.append($('<div/>').attr({'style': 'display:table; width:100%'}).append(inst.$el));       
      }   
      
      while (i < 5) {
        inst = new OB.COMP.ButtonKey({parent: this.options.parent}).render();
        this.$el.append($('<div/>').attr({'style': 'display:table; width:100%'}).append(inst.$el));       
        i++;
      }
      
      // switch button..
      inst = new OB.UI.ButtonSwitch({parent: this.options.parent}).render();
      cont = $('<div/>').attr({'style': 'display:table; width:100%;'}).append($('<div/>').attr({'style': 'margin: 5px;'}).append(inst.$el));
      this.$el.append(cont);    
    }
  });

}());