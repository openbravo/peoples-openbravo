/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, $, _ */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  function payment(amount, modalpayment, receipt, key, name, paymentMethod) {
    if (OB.DEC.compare(amount) > 0) {
      if (paymentMethod.view) {
        modalpayment.show(receipt, key, name, paymentMethod, amount);
      } else {
        receipt.addPayment(new OB.Model.PaymentLine({
          'kind': key,
          'name': name,
          'amount': amount
        }));
      }
    }
  }

  function getPayment(modalpayment, receipt, key, name, paymentMethod) {
    return ({
      'permission': key,
      'stateless': false,
      'action': function(txt) {
        var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
        amount = _.isNaN(amount) ? receipt.getPending() : amount;
        payment(amount, modalpayment, receipt, key, name, paymentMethod);
      }
    });
  }

  OB.UI.ButtonSwitch_old = OB.COMP.Button.extend({
    className: 'btnkeyboard',

    initialize: function() {
      OB.COMP.Button.prototype.initialize.call(this); // super.initialize();
      this.options.parent.on('keypad', this.render, this);
    },
    clickEvent: function(e) {
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

  OB.UI.ToolbarPayment_old = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'display:none'
    },


    initialize: function() {
      var i, max, payments, Btn, inst, cont, receipt, defaultpayment, allpayments = {};

      var modalpayment = new OB.UI.ModalPayment({
        parent: this.options.parent
      }).render();
      $('body').append(modalpayment.$el);

      payments = OB.POS.modelterminal.get('payments');
      receipt = this.options.parent.receipt;

      for (i = 0, max = payments.length; i < max; i++) {

        // Data for cashexact command
        if (payments[i].payment.searchKey === 'OBPOS_payment.cash') {
          defaultpayment = payments[i];
        }
        allpayments[payments[i].payment.searchKey] = payments[i];

        Btn = OB.COMP.ButtonKey.extend({
          command: payments[i].payment.searchKey,
          definition: getPayment(modalpayment, receipt, payments[i].payment.searchKey, payments[i].payment._identifier, payments[i].paymentMethod),
          classButtonActive: 'btnactive-green',
          permission: payments[i].payment.searchKey,
          contentViewButton: [payments[i].payment._identifier]
        });
        inst = new Btn({
          parent: this.options.parent
        }).render();
        this.$el.append($('<div/>').attr({
          'style': 'display:table; width:100%'
        }).append(inst.$el));
      }

      this.options.parent.addCommand('cashexact', {
        'action': function(txt) {
          var exactpayment = allpayments[this.status] || defaultpayment;
          var amount = receipt.getPending();
          if (amount > 0 && exactpayment) {
            payment(amount, modalpayment, receipt, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod);
          }
        }
      });

      while (i < 5) {
        inst = new OB.COMP.ButtonKey({
          parent: this.options.parent
        }).render();
        this.$el.append($('<div/>').attr({
          'style': 'display:table; width:100%'
        }).append(inst.$el));
        i++;
      }

      // switch button..
      inst = new OB.UI.ButtonSwitch({
        parent: this.options.parent
      }).render();
      cont = $('<div/>').attr({
        'style': 'display:table; width:100%;'
      }).append($('<div/>').attr({
        'style': 'margin: 5px;'
      }).append(inst.$el));
      this.$el.append(cont);
    },
    shown: function() {
      this.options.parent.showKeypad('coins');
      this.options.parent.showSidepad('sidedisabled');
      this.options.parent.defaultcommand = 'OBPOS_payment.cash';
      this.options.parent.setStatus('OBPOS_payment.cash');
    }
  });


  enyo.kind({
    name: 'OB.UI.ToolbarPayment',
    toolbarName: 'toolbarpayment',
    //    pay: function(amount, modalpayment, receipt, key, name, paymentMethod) {
    //      if (OB.DEC.compare(amount) > 0) {
    //        if (paymentMethod.view) {
    //          modalpayment.show(receipt, key, name, paymentMethod, amount);
    //        } else {
    //          receipt.addPayment(new OB.Model.PaymentLine({
    //            'kind': key,
    //            'name': name,
    //            'amount': amount
    //          }));
    //        }
    //      }
    //    },
    getPayment: function(modalpayment, receipt, key, name, paymentMethod) {
      var me = this;
      function pay(amount, modalpayment, receipt, key, name, paymentMethod) {
        if (OB.DEC.compare(amount) > 0) {
          if (paymentMethod.view) {
            modalpayment.show(receipt, key, name, paymentMethod, amount);
          } else {
            receipt.addPayment(new OB.Model.PaymentLine({
              'kind': key,
              'name': name,
              'amount': amount
            }));
          }
        }
      }

      return ({
        'permission': key,
        'stateless': false,
        'action': function(txt) {
          var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
          amount = _.isNaN(amount) ? receipt.getPending() : amount;
          pay(amount, modalpayment, me.receipt, key, name, paymentMethod);
        }
      });
    },

    init: function() {
      console.log('init toolabar p');
      this.receipt = this.owner.owner.owner.model.get('order');
    },

    initComponents: function() {
      //TODO: modal payments
      var i, max, payments, Btn, inst, cont, defaultpayment, allpayments = {},
          modalpayment;

      this.inherited(arguments);

      payments = OB.POS.modelterminal.get('payments');



      enyo.forEach(payments, function(payment) {
        if (payment.payment.searchKey === 'OBPOS_payment.cash') {
          defaultpayment = payment;
        }
        allpayments[payment.payment.searchKey] = payment;

        //        Btn = OB.COMP.ButtonKey.extend({
        //          command: payments[i].payment.searchKey,
        //          definition: getPayment(modalpayment, receipt, payments[i].payment.searchKey, payments[i].payment._identifier, payments[i].paymentMethod),
        //          classButtonActive: 'btnactive-green',
        //          permission: payments[i].payment.searchKey,
        //          contentViewButton: [payments[i].payment._identifier]
        //        });
        this.createComponent({
          kind: 'OB.UI.BtnSide',
          btn: {
            command: payment.payment.searchKey,
            label: payment.payment._identifier,
            permission: payment.payment.searchKey,
            definition: this.getPayment(modalpayment, this.receipt, payment.payment.searchKey, payment.payment._identifier, payment.paymentMethod),
          }
        });
      }, this);

      for (i = payments.length - 1; i < 4; i++) {
        this.createComponent({
          kind: 'OB.UI.BtnSide',
          btn: {}
        })
      }

      this.createComponent({
        kind: 'OB.UI.ButtonSwitch',
        keyboard: this.keyboard
      });
    },
    shown: function() {
      var keyboard = this.owner.owner;
      keyboard.showKeypad('coins')
      keyboard.showSidepad('sidedisabled');

      //TODO: defaulting to cash using its hardcoded value, should be configurable
      keyboard.defaultcommand = 'OBPOS_payment.cash';
      keyboard.setStatus('OBPOS_payment.cash');
    }
  });

  enyo.kind({
    name: 'OB.UI.ButtonSwitch',

    style: 'display:table; width:100%;',
    components: [{
      style: 'margin: 5px;',
      components: [{
        kind: 'OB.UI.Button',
        classes: 'btnkeyboard',
        name: 'btn'
      }]
    }],
    setLabel: function() {
      this.$.btn.setContent(this.keyboard.state.get('keypadLabel'));
    },
    tap: function() {
      console.log('tap');
      var newKeypad = this.keyboard.state.get('keypadName') === 'coins' ? 'basic' : 'coins';
      this.keyboard.showKeypad(newKeypad);
    },

    create: function() {
      this.inherited(arguments);
      debugger;
      this.keyboard.state.on('change:keypadLabel', function() {
        this.setLabel();
      }, this);
      this.setLabel();
    }

  });

  OB.UI.ButtonSwitch_old = OB.COMP.Button.extend({
    className: 'btnkeyboard',

    initialize: function() {
      OB.COMP.Button.prototype.initialize.call(this); // super.initialize();
      this.options.parent.on('keypad', this.render, this);
    },
    clickEvent: function(e) {
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
}());