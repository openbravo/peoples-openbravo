/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

OB.OBPOSPointOfSale.UI.ToolbarScan = {
  name: 'toolbarscan',
  buttons: [{
    command: 'code',
    label: OB.I18N.getLabel('OBPOS_KbCode'),
    classButtonActive: 'btnactive-blue'
  }],
  shown: function () {
    var keyboard = this.owner.owner;
    keyboard.showKeypad('basic');
    keyboard.showSidepad('sideenabled');
    keyboard.defaultcommand = 'code';
  }
};


enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ToolbarPayment',
  components: [{
    name: 'modalpayment',
    kind: 'OB.UI.ModalPayment'
  }],
  published: {
    receipt: null
  },
  toolbarName: 'toolbarpayment',
  pay: function (amount, key, name, paymentMethod, rate, mulrate, isocode) {
    if (OB.DEC.compare(amount) > 0) {

      var provider;
      if (this.receipt.get('orderType') === 0) {
        provider = paymentMethod.paymentProvider;
      } else if (this.receipt.get('orderType') === 1) {
        provider = paymentMethod.refundProvider;
      } else {
        provider = null;
      }

      if (provider) {
        this.$.modalpayment.show(this.receipt, provider, key, name, paymentMethod, amount, rate, mulrate, isocode);
      } else {
        this.receipt.addPayment(new OB.Model.PaymentLine({
          'kind': key,
          'name': name,
          'amount': amount,
          'rate': rate,
          'mulrate': mulrate,
          'isocode': isocode
        }));
      }
    }
  },
  getPayment: function (key, name, paymentMethod, rate, mulrate, isocode) {
    var me = this;
    return ({
      'permission': key,
      'stateless': false,
      'action': function (keyboard, txt) {
        var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
        amount = _.isNaN(amount) ? me.receipt.getPending() : amount;
        me.pay(amount, key, name, paymentMethod, rate, mulrate, isocode);
      }
    });
  },

  initComponents: function () {
    //TODO: modal payments
    var i, max, payments, Btn, inst, cont, defaultpayment, allpayments = {},
        me = this;

    this.inherited(arguments);

    payments = OB.POS.modelterminal.get('payments');

    enyo.forEach(payments, function (payment) {
      if (payment.payment.searchKey === 'OBPOS_payment.cash') {
        defaultpayment = payment;
      }
      allpayments[payment.payment.searchKey] = payment;

      this.createComponent({
        kind: 'OB.UI.BtnSide',
        btn: {
          command: payment.payment.searchKey,
          label: payment.payment._identifier,
          permission: payment.payment.searchKey,
          definition: this.getPayment(payment.payment.searchKey, payment.payment._identifier, payment.paymentMethod, payment.rate, payment.mulrate, payment.isocode)
        }
      });
    }, this);

    for (i = payments.length - 1; i < 4; i++) {
      this.createComponent({
        kind: 'OB.UI.BtnSide',
        btn: {}
      });
    }

    this.createComponent({
      kind: 'OB.OBPOSPointOfSale.UI.ButtonSwitch',
      keyboard: this.keyboard
    });

    this.owner.owner.addCommand('cashexact', {
      action: function (keyboard, txt) {
        var exactpayment = allpayments[keyboard.status] || defaultpayment,
            amount = me.receipt.getPending();
        if (exactpayment.rate && exactpayment.rate !== '1') {
          amount = OB.DEC.div(me.receipt.getPending(), exactpayment.rate);
        }
        if (amount > 0 && exactpayment && OB.POS.modelterminal.hasPermission(exactpayment.payment.searchKey)) {
          me.pay(amount, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod, exactpayment.rate, exactpayment.mulrate, exactpayment.isocode);
        }
      }
    });
  },
  shown: function () {
    var keyboard = this.owner.owner;
    keyboard.showKeypad('coins');
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
  setLabel: function (lbl) {
    this.$.btn.setContent(lbl);
  },
  tap: function () {
    this.keyboard.showNextKeypad();
  },

  create: function () {
    this.inherited(arguments);
    this.keyboard.state.on('change:keypadNextLabel', function () {
      this.setLabel(this.keyboard.state.get('keypadNextLabel'));
    }, this);
    this.setLabel(this.keyboard.state.get('keypadNextLabel'));
  }
});