/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.OBPOSPointOfSale.UI.ToolbarScan = {
  name: 'toolbarscan',
  buttons: [{
    command: 'code',
    label: OB.I18N.getLabel('OBPOS_KbCode'),
    classButtonActive: 'btnactive-blue'
  }],
  shown: function() {
    var keyboard = this.owner.owner;
    keyboard.showKeypad('basic')
    keyboard.showSidepad('sideenabled');
    keyboard.defaultcommand = 'code';
  }
};


enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ToolbarPayment',
  published: {
    receipt: null
  },
  toolbarName: 'toolbarpayment',
  pay: function(amount, modalpayment, key, name, paymentMethod) {
    if (OB.DEC.compare(amount) > 0) {
      if (paymentMethod.view) {
        modalpayment.show(this.receipt, key, name, paymentMethod, amount);
      } else {
        this.receipt.addPayment(new OB.Model.PaymentLine({
          'kind': key,
          'name': name,
          'amount': amount
        }));
      }
    }
  },
  getPayment: function(modalpayment, key, name, paymentMethod) {
    var me = this;

    return ({
      'permission': key,
      'stateless': false,
      'action': function(txt) {
        var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
        amount = _.isNaN(amount) ? me.receipt.getPending() : amount;
        me.pay(amount, modalpayment, me.receipt, key, name, paymentMethod);
      }
    });
  },

  initComponents: function() {
    //TODO: modal payments
    var i, max, payments, Btn, inst, cont, defaultpayment, allpayments = {},
        modalpayment, me = this;

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
          definition: this.getPayment(modalpayment, payment.payment.searchKey, payment.payment._identifier, payment.paymentMethod),
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
      kind: 'OB.OBPOSPointOfSale.UI.ButtonSwitch',
      keyboard: this.keyboard
    });

    this.owner.owner.addCommand('cashexact', {
      action: function(txt) {
        var receipt = me.owner.owner.owner.model.get('order');

        var exactpayment = allpayments[this.status] || defaultpayment;
        var amount = receipt.getPending();
        if (amount > 0 && exactpayment) {
          me.pay(amount, modalpayment, receipt, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod);
        }
      }
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
  name: 'OB.OBPOSPointOfSale.UI.ButtonSwitch',

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
    this.keyboard.state.on('change:keypadLabel', function() {
      this.setLabel();
    }, this);
    this.setLabel();
  }
});