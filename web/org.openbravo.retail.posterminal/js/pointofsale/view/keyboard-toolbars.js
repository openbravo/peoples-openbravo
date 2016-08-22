/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

OB.OBPOSPointOfSale.UI.ToolbarScan = {
  name: 'toolbarscan',
  buttons: [{
    command: 'code',
    i18nLabel: 'OBMOBC_KbCode',
    classButtonActive: 'btnactive-blue',
    idSufix: 'upcean'
  }],
  shown: function () {
    var keyboard = this.owner.owner;
    keyboard.showKeypad('basic');
    keyboard.showSidepad('sideenabled');
    keyboard.defaultcommand = 'code';
    keyboard.disableCommandKey(this, {
      disabled: true,
      commands: ['%']
    });
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
    keyboard.disableCommandKey(this, {
      disabled: true,
      commands: ['%']
    });
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
    onShowPopup: '',
    onClearPaymentSelect: ''
  },
  handlers: {
    onShowAllButtons: 'showAllButtons',
    onCloseAllPopups: 'closeAllPopups',
    onButtonPaymentChanged: 'paymentChanged',
    onButtonStatusChanged: 'buttonStatusChanged',
    onActionPay: 'actionPay'
  },
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
    name: 'OBPOS_UI_PaymentMethods'
  }],
  init: function (model) {
    this.model = model;
  },
  actionPay: function (inSender, inEvent) {
    this.bubble('onClearPaymentSelect');
    this.pay(inEvent.amount, inEvent.key, inEvent.name, inEvent.paymentMethod, inEvent.rate, inEvent.mulrate, inEvent.isocode, inEvent.options);
  },
  pay: function (amount, key, name, paymentMethod, rate, mulrate, isocode, options) {
    if (this.receipt.stopAddingPayments) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
      return;
    }
    if (options && options.percentaje) {
      var pending = this.model.getPending();
      if (mulrate && mulrate !== '1') {
        pending = OB.DEC.mul(pending, mulrate);
      }
      amount = OB.DEC.div(OB.DEC.mul(pending, amount), 100);
    }

    if (OB.DEC.compare(amount) > 0) {
      var provider, receiptToPay, me = this;
      if (this.model.get('leftColumnViewManager').isOrder()) {
        receiptToPay = this.receipt;
      }
      //multiorders doesn't allow to return
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        receiptToPay = this.model.get('multiOrders');
      }

      provider = receiptToPay.getTotal() > 0 ? paymentMethod.paymentProvider : paymentMethod.refundProvider;

      if (provider) {
        this.doShowPopup({
          popup: 'modalpayment',
          args: {
            'receipt': receiptToPay,
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
        // Calculate total amount to pay with selected PaymentMethod  
        var amountToPay = amount;
        if (receiptToPay.get("payments").length > 0) {
          receiptToPay.get("payments").each(function (item) {
            if (item.get("kind") === key) {
              amountToPay += item.get("amount");
            }
          });
        }
        // Check Max. Limit Amount
        if (paymentMethod.maxLimitAmount && amountToPay > paymentMethod.maxLimitAmount) {
          // Show error and abort payment
          this.bubble('onMaxLimitAmountError', {
            show: true,
            maxLimitAmount: paymentMethod.maxLimitAmount,
            currency: OB.MobileApp.model.paymentnames[key].symbol,
            symbolAtRight: OB.MobileApp.model.paymentnames[key].currencySymbolAtTheRight
          });
        } else {
          // Hide error and process payment
          this.bubble('onMaxLimitAmountError', {
            show: false,
            maxLimitAmount: 0,
            currency: '',
            symbolAtRight: true
          });
          this.model.addPayment(new OB.Model.PaymentLine({
            'kind': key,
            'name': name,
            'amount': amount,
            'rate': rate,
            'mulrate': mulrate,
            'isocode': isocode,
            'allowOpenDrawer': paymentMethod.allowopendrawer,
            'isCash': paymentMethod.iscash,
            'openDrawer': paymentMethod.openDrawer,
            'printtwice': paymentMethod.printtwice
          }));
        }
      }
    }
  },
  getButtonComponent: function (sidebutton) {
    if (sidebutton.i18nLabel) {
      sidebutton.label = OB.I18N.getLabel(sidebutton.i18nLabel);
    }
    return {
      kind: 'OB.UI.BtnSide',
      btn: {
        command: sidebutton.command,
        label: sidebutton.label,
        permission: sidebutton.permission,
        definition: {
          holdActive: true,
          permission: sidebutton.permission,
          stateless: sidebutton.stateless,
          action: sidebutton.action
        }
      }
    };
  },
  addPaymentButton: function (btncomponent, countbuttons, paymentsbuttons, dialogbuttons, payment) {
    if (countbuttons < paymentsbuttons) {
      this.createComponent(btncomponent);
    } else {
      this.addSideButton(btncomponent);
      dialogbuttons[payment.payment.searchKey] = payment.payment._identifier;
    }
  },
  addSideButton: function (btncomponent) {
    var hasSideButton = false,
        sideButtons = OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons,
        i;
    for (i = 0; i < sideButtons.length; i++) {
      if (_.isEqual(sideButtons[i].btn.command, btncomponent.btn.command)) {
        OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons.splice(i, 1, btncomponent);
        hasSideButton = true;
        break;
      }
    }
    if (!hasSideButton) {
      OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons.push(btncomponent);
    }
    this.owner.owner.addCommand(btncomponent.btn.command, btncomponent.btn.definition);
  },
  initComponents: function () {
    //TODO: modal payments
    var i, max, payments, paymentsdialog, paymentsbuttons, countbuttons, btncomponent, Btn, inst, cont, exactdefault, cashdefault, allpayments = {},
        me = this,
        paymentCategories = [],
        dialogbuttons = {};

    this.inherited(arguments);

    payments = OB.MobileApp.model.get('payments');

    // Count payment buttons checking payment method category  
    countbuttons = 0;
    enyo.forEach(payments, function (payment) {
      if (payment.paymentMethod.paymentMethodCategory) {
        var paymentCategory = null;
        paymentCategories.every(function (category) {
          if (category.id === payment.paymentMethod.paymentMethodCategory) {
            paymentCategory = category;
            return false;
          }
          return true;
        });
        if (paymentCategory === null) {
          countbuttons++;
          paymentCategories.push({
            id: payment.paymentMethod.paymentMethodCategory,
            name: payment.paymentMethod.paymentMethodCategory$_identifier
          });
        }
      } else {
        countbuttons++;
      }
    });

    paymentsdialog = countbuttons + this.sideButtons.length > 5;
    paymentsbuttons = paymentsdialog ? 4 : 5;
    countbuttons = 0;
    paymentCategories = [];

    enyo.forEach(payments, function (payment) {
      if (payment.paymentMethod.id === OB.MobileApp.model.get('terminal').terminalType.paymentMethod) {
        exactdefault = payment;
      }
      if (payment.payment.searchKey === OB.MobileApp.model.get('paymentcash')) {
        cashdefault = payment;
      }
      allpayments[payment.payment.searchKey] = payment;

      // Check for payment method category
      if (payment.paymentMethod.paymentMethodCategory) {
        btncomponent = null;
        if (paymentCategories.indexOf(payment.paymentMethod.paymentMethodCategory) === -1) {
          btncomponent = me.getButtonComponent({
            command: 'paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory,
            label: payment.paymentMethod.paymentMethodCategory$_identifier,
            stateless: false,
            action: function (keyboard, txt) {
              var options = {};
              if (_.last(txt) === '%') {
                options.percentaje = true;
              }
              var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
              if (_.isNaN(amount)) {
                OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
                return;
              }
              var buttonClass = keyboard.buttons['paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory].attributes['class'];
              if (me.currentPayment && buttonClass.indexOf('btnactive-green') > 0) {
                me.pay(amount, me.currentPayment.payment.searchKey, me.currentPayment.payment._identifier, me.currentPayment.paymentMethod, me.currentPayment.rate, me.currentPayment.mulrate, me.currentPayment.isocode, options);
              } else {
                me.doShowPopup({
                  popup: 'modalPaymentsSelect',
                  args: {
                    idCategory: payment.paymentMethod.paymentMethodCategory,
                    amount: amount,
                    options: options
                  }
                });
              }
            }
          });
          paymentCategories.push(payment.paymentMethod.paymentMethodCategory);
        }
      } else {
        btncomponent = this.getButtonComponent({
          command: payment.payment.searchKey,
          label: payment.payment._identifier + (payment.paymentMethod.paymentMethodCategory ? '*' : ''),
          permission: payment.payment.searchKey,
          stateless: false,
          action: function (keyboard, txt) {
            var options = {};
            if (_.last(txt) === '%') {
              options.percentaje = true;
            }
            var amount = OB.DEC.number(OB.I18N.parseNumber(txt));
            if (_.isNaN(amount)) {
              OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
              return;
            }
            me.pay(amount, payment.payment.searchKey, payment.payment._identifier, payment.paymentMethod, payment.rate, payment.mulrate, payment.isocode, options);
          }
        });
      }

      if (btncomponent !== null) {
        if (payment.paymentMethod.paymentMethodCategory) {
          me.addPaymentButton(btncomponent, countbuttons++, paymentsbuttons, dialogbuttons, {
            payment: {
              searchKey: 'paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory,
              _identifier: payment.paymentMethod.paymentMethodCategory$_identifier
            }
          });
        } else {
          me.addPaymentButton(btncomponent, countbuttons++, paymentsbuttons, dialogbuttons, payment);
        }
      }
    }, this);

    // Fallback assign of the payment for the exact command.
    exactdefault = exactdefault || cashdefault || payments[0];
    this.defaultPayment = exactdefault;

    enyo.forEach(this.sideButtons, function (sidebutton) {
      btncomponent = this.getButtonComponent(sidebutton);
      if (countbuttons++ < paymentsbuttons) {
        this.createComponent(btncomponent);
      } else {
        me.addSideButton(btncomponent);
        dialogbuttons[sidebutton.command] = sidebutton.label;
      }
    }, this);

    while (countbuttons++ < paymentsbuttons) {
      this.createComponent({
        kind: 'OB.UI.BtnSide',
        btn: {}
      });
    }

    if (paymentsdialog) {
      this.createComponent({
        name: 'btnMore',
        toolbar: this,
        dialogbuttons: dialogbuttons,
        kind: 'OB.OBPOSPointOfSale.UI.ButtonMore'
      });
    }

    this.createComponent({
      kind: 'OB.OBPOSPointOfSale.UI.ButtonSwitch',
      keyboard: this.keyboard
    });

    this.owner.owner.addCommand('cashexact', {
      action: function (keyboard, txt) {
        var status = keyboard.status.indexOf('paymentMethodCategory.showitems.') === 0 && me.currentPayment ? me.currentPayment.payment.searchKey : keyboard.status;
        if (status && !allpayments[status]) {
          // Is not a payment, so continue with the default path...
          keyboard.execCommand(status, null);
        } else {
          me.bubble('onClearPaymentSelect');
          // It is a payment...
          var exactpayment = allpayments[status] || exactdefault,
              amount = me.model.getPending(),
              altexactamount = me.receipt.get('exactpayment');
          // check if alternate exact amount must be applied based on the payment method selected.
          if (altexactamount && altexactamount[exactpayment.payment.searchKey]) {
            amount = altexactamount[exactpayment.payment.searchKey];
          }
          if (exactpayment.rate && exactpayment.rate !== '1') {
            amount = OB.DEC.div(amount, exactpayment.rate, 6);
          }

          if (amount > 0 && exactpayment && OB.MobileApp.model.hasPermission(exactpayment.payment.searchKey)) {
            me.pay(amount, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod, exactpayment.rate, exactpayment.mulrate, exactpayment.isocode);
          }
        }
      }
    });
  },
  showAllButtons: function () {
    this.$.OBPOS_UI_PaymentMethods.show();
  },
  closeAllPopups: function () {
    this.$.OBPOS_UI_PaymentMethods.hide();
  },
  paymentChanged: function (inSender, inEvent) {
    this.currentPayment = inEvent.payment;
  },
  buttonStatusChanged: function (inSender, inEvent) {
    var status = inEvent.value.status;
    if (this.showing && this.keyboard.lastStatus !== '' && status === '') {
      this.keyboard.setStatus(this.defaultPayment.payment.searchKey);
    }
  },
  shown: function () {
    var me = this,
        i, max, p, keyboard = this.owner.owner;
    keyboard.showKeypad('Coins-' + OB.MobileApp.model.get('currency').id); // shows the Coins/Notes panel for the terminal currency
    keyboard.showSidepad('sidedisabled');

    keyboard.disableCommandKey(this, {
      commands: ['%'],
      disabled: false
    });

    for (i = 0, max = OB.MobileApp.model.get('payments').length; i < max; i++) {
      p = OB.MobileApp.model.get('payments')[i];
      if (p.paymentMethod.id === OB.MobileApp.model.get('terminal').terminalType.paymentMethod) {
        keyboard.defaultcommand = OB.MobileApp.model.get('payments')[i].payment.searchKey;
        keyboard.setStatus(OB.MobileApp.model.get('payments')[i].payment.searchKey);
        break;
      }
    }

    if (keyboard && OB.MobileApp.model.get('paymentcash') && keyboard.status === '') {
      keyboard.defaultcommand = OB.MobileApp.model.get('paymentcash');
      keyboard.setStatus(OB.MobileApp.model.get('paymentcash'));
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  i18nHeader: 'OBPOS_MorePaymentsHeader',
  sideButtons: [],
  body: {
    classes: 'row-fluid',
    components: [{
      classes: 'span12',
      components: [{
        style: 'border-bottom: 1px solid #cccccc;',
        classes: 'row-fluid',
        components: [{
          name: 'buttonslist',
          classes: 'span12'
        }]
      }]
    }]
  },
  createPaymentButtons: function () {
    enyo.forEach(this.sideButtons, function (sidebutton) {
      sidebutton.btn.definition.includedInPopUp = true;
      this.$.body.$.buttonslist.createComponent(sidebutton, {
        owner: this.parent
      });
    }, this);
  },
  executeOnShow: function () {
    if (this.$.body.$.buttonslist.children.length !== this.sideButtons.length) {
      this.$.body.$.buttonslist.destroyComponents();
      this.createPaymentButtons();
    }
    return true;
  },
  init: function (model) {
    this.model = model;
    this.createPaymentButtons();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonMore',
  style: 'display:table; width:100%;',
  events: {
    onShowAllButtons: ''
  },
  handlers: {
    onButtonStatusChanged: 'buttonStatusChanged'
  },
  components: [{
    style: 'margin: 5px;',
    components: [{
      kind: 'OB.UI.Button',
      classes: 'btnkeyboard',
      name: 'btn',
      label: ''
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.btn.setContent(OB.I18N.getLabel('OBPOS_MorePayments'));
    this.activegreen = false;
  },
  tap: function () {
    this.doShowAllButtons();
  },
  buttonStatusChanged: function (inSender, inEvent) {
    var status = inEvent.value.status;
    if (this.activegreen) {
      this.$.btn.setContent(OB.I18N.getLabel('OBPOS_MorePayments'));
      this.$.btn.removeClass('btnactive-green');
      this.activegreen = false;
    }
    if (this.dialogbuttons[status]) {
      this.$.btn.setContent(OB.I18N.getLabel('OBPOS_MorePayments') + ' (' + this.dialogbuttons[status] + ')');
      this.$.btn.addClass('btnactive-green');
      this.activegreen = true;
    }
    OB.UTIL.createElipsisEffect(this.$.btn);
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