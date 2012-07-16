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
    
    initialize: function () {
      OB.COMP.Button.prototype.initialize.call(this); // super.initialize();     
      this.options.parent.on('keypad', this.render, this);
    },
    clickEvent: function (e) {
      if (this.options.parent.keypad.name === 'coins') {
        this.options.parent.showKeypad('index'); // show index
      } else {
        this.options.parent.showKeypad('coins');
      }
      this.render();
    },
    render: function() {
      // this.$el.text(this.options.parent.keypad.label);
      if (this.options.parent.keypad.name === 'coins') {
        this.$el.text(this.options.parent.keypads.index.label);
      } else {
        this.$el.text(this.options.parent.keypads.coins.label);
      }
      return this;
    }
    
  });
  
  OB.UI.ToolbarPayment = Backbone.View.extend({
    tagName: 'div',
    attributes: {'style': 'display:none'},
    
    
    initialize: function () {
      var i, max, payments, Btn, inst, cont, receipt, defaultpayment, allpayments = {};
      
      this.modalpayment = new OB.UI.ModalPayment({parent: this.options.parent}).render();
      $('body').append(this.modalpayment.$el);     
      
      payments = OB.POS.modelterminal.get('payments');
      receipt = this.options.parent.receipt;
       
      for (i = 0, max = payments.length; i < max; i++) {
           
        // Data for cashexact command
        if (payments[i].searchKey === 'OBPOS_payment.cash') {
          defaultpayment = payments[i];
        }
        allpayments[payments[i].searchKey] = payments[i];
     
        Btn = OB.COMP.ButtonKey.extend({
          command: payments[i].searchKey,
          definition: getPayment(this.modalpayment, receipt, payments[i].searchKey, payments[i]._identifier, payments[i].provider),
          classButtonActive: 'btnactive-green',
          permission: payments[i].searchKey,
          contentViewButton: [payments[i]._identifier]
        });
        inst = new Btn({parent: this.options.parent}).render();
        this.$el.append($('<div/>').attr({'style': 'display:table; width:100%'}).append(inst.$el));       
      }  
      
      this.options.parent.addCommand('cashexact', {
        'action': function(txt) {
          var exactpayment = allpayments[this.status] || defaultpayment;
          var amount = receipt.getPending();
          if (amount > 0 && exactpayment) {             
            receipt.addPayment(new OB.Model.PaymentLine({
              'kind': exactpayment.searchKey,
              'name': exactpayment._identifier,
              'amount': amount
            }));
          }
        }
      });        
      
      while (i < 5) {
        inst = new OB.COMP.ButtonKey({parent: this.options.parent}).render();
        this.$el.append($('<div/>').attr({'style': 'display:table; width:100%'}).append(inst.$el));       
        i++;
      }
      
      // switch button..
      inst = new OB.UI.ButtonSwitch({parent: this.options.parent}).render();
      cont = $('<div/>').attr({'style': 'display:table; width:100%;'}).append($('<div/>').attr({'style': 'margin: 5px;'}).append(inst.$el));
      this.$el.append(cont);    
    },
    shown: function() {
      this.options.parent.showKeypad('coins');
      this.options.parent.showSidepad('sidedisabled');
      this.options.parent.defaultcommand = 'OBPOS_payment.cash';
      this.options.parent.setStatus('OBPOS_payment.cash');
    }
  });

}());