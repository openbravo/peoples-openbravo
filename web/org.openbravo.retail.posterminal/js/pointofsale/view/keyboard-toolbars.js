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

OB.OBPOSPointOfSale.UI.ToolbarDiscounts = {
  name: 'toolbardiscounts',
  buttons: [],
  shown: function () {
    var keyboard = this.owner.owner;
    keyboard.showKeypad('basic');
    keyboard.showSidepad('sideenabled');
    keyboard.defaultcommand = 'line:dto';
  }
};


enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ToolbarPayment',
  sideButtons: [],
  published: {
    receipt: null
  },
  toolbarName: 'toolbarpayment',
  events: {
    onShowPopup: ''
  },
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
        this.doShowPopup({
          popup: 'modalpayment',
          args: {
            'receipt': this.receipt,
            'provider': provider,
            'key': key,
            'name': name,
            'paymentMethod': paymentMethod,
            'amount': amount,
            'rate': rate,
            'mulrate': mulrate,
            'isocode': isocode
          }
        });
      } else {
        this.receipt.addPayment(new OB.Model.PaymentLine({
          'kind': key,
          'name': name,
          'amount': amount,
          'rate': rate,
          'mulrate': mulrate,
          'isocode': isocode,
          'openDrawer': paymentMethod.openDrawer
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

    enyo.forEach(this.sideButtons, function (sidebutton) {
      this.createComponent(sidebutton);
    }, this);

    for (i = payments.length + this.sideButtons.length - 1; i < 4; i++) {
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
        if (keyboard.status && !allpayments[keyboard.status]) {
          // Is not a payment, so continue with the default path...
          keyboard.execCommand(keyboard.status, null);
        } else {
          // It is a payment...
          var exactpayment = allpayments[keyboard.status] || defaultpayment,
              amount = me.receipt.getPending();
          if (exactpayment.rate && exactpayment.rate !== '1') {
            amount = OB.DEC.div(me.receipt.getPending(), exactpayment.rate);
          }
          if (amount > 0 && exactpayment && OB.POS.modelterminal.hasPermission(exactpayment.payment.searchKey)) {
            me.pay(amount, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod, exactpayment.rate, exactpayment.mulrate, exactpayment.isocode);
          }
        }
      }
    });
  },
  shown: function () {
    var me = this,
        i, max, p, keyboard = this.owner.owner;
    keyboard.showKeypad('coins');
    keyboard.showSidepad('sidedisabled');

    for (i = 0, max = OB.POS.modelterminal.get('payments').length; i < max; i++) {
      p = OB.POS.modelterminal.get('payments')[i];
      if (p.paymentMethod.id === OB.POS.modelterminal.get('terminal').terminalType.paymentMethod) {
        keyboard.defaultcommand = OB.POS.modelterminal.get('payments')[i].payment.searchKey;
        keyboard.setStatus(OB.POS.modelterminal.get('payments')[i].payment.searchKey);
        break;
      }
    }
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