/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
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
    onHidePopup: '',
    onClearPaymentSelect: ''
  },
  handlers: {
    onShowAllButtons: 'showAllButtons',
    onCloseAllPopups: 'closeAllPopups',
    onButtonPaymentChanged: 'paymentChanged',
    onButtonStatusChanged: 'buttonStatusChanged',
    onActionPay: 'actionPay'
  },
  morePaymentMethodsPopup: 'OBPOS_UI_PaymentMethods',
  init: function (model) {
    this.model = model;
  },
  actionPay: function (inSender, inEvent) {
    this.bubble('onClearPaymentSelect');
    this.pay(inEvent.amount, inEvent.key, inEvent.name, inEvent.paymentMethod, inEvent.rate, inEvent.mulrate, inEvent.isocode, inEvent.options);
  },
  getReceiptToPay: function () {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      return this.receipt;
    }
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      return this.model.get('multiOrders');
    }
    throw new Error('No receipt to pay.');
  },
  checkNoPaymentsAllowed: function () {
    // Checks to be done BEFORE payment provider is invoked.
    if (this.getReceiptToPay().stopAddingPayments) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
      return true;
    }
    if (this.model.get('leftColumnViewManager').isOrder() && this.receipt.get('isPaid') && OB.DEC.abs(this.receipt.getPrePaymentQty()) >= OB.DEC.abs(this.receipt.getTotal()) && !this.receipt.isNewReversed()) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotIntroducePayment'));
      return true;
    }
    return false;
  },
  payWithProviderGroup: function (keyboard, txt, providerGroup) {
    var amount;
    var input;

    if (!txt) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [txt]));
      return;
    }
    input = OB.DEC.number(OB.I18N.parseNumber(txt));
    if (_.isNaN(input)) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
      return;
    }
    var decimalInput = OB.DEC.toBigDecimal(input);
    if (decimalInput.scale() > OB.DEC.getScale()) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [txt]));
      return;
    }

    if (_.last(txt) === '%') {
      amount = OB.DEC.div(OB.DEC.mul(this.model.getPending(), input), 100);
    } else {
      amount = input;
    }

    this.payAmountWithProviderGroup(amount, providerGroup);
  },
  payAmountWithProviderGroup: function (amount, providerGroup) {

    if (this.checkNoPaymentsAllowed()) {
      return;
    }

    var changedAmount;
    var firstpayment = providerGroup._payments[0];
    if (firstpayment.mulrate && firstpayment.mulrate !== '1.000000000000') {
      changedAmount = OB.DEC.mul(amount, firstpayment.mulrate);
    } else {
      changedAmount = amount;
    }

    if (OB.DEC.compare(changedAmount) > 0) {
      //  -> payment or refund
      this.doShowPopup({
        popup: 'modalprovidergroup',
        args: {
          'receipt': this.getReceiptToPay(),
          'refund': this.getReceiptToPay().getPaymentStatus().isNegative,
          'amount': amount,
          'currency': firstpayment.isocode,
          'providerGroup': providerGroup,
          'providerinstance': enyo.createFromKind(providerGroup.provider.provider)
        }
      });
    }
  },

  pay: function (amount, key, name, paymentMethod, rate, mulrate, isocode, options, callback) {

    if (this.checkNoPaymentsAllowed()) {
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
      var provider, receiptToPay = this.getReceiptToPay(),
          me = this;

      if (!receiptToPay.getPaymentStatus().isNegative) {
        provider = paymentMethod.paymentProvider;
      } else {
        provider = paymentMethod.refundProvider;
      }

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
        var amountToPay = _.isUndefined(receiptToPay.get('paidInNegativeStatusAmt')) ? amount : -amount;
        if (receiptToPay.get("payments").length > 0) {
          receiptToPay.get("payments").each(function (item) {
            if (item.get("kind") === key) {
              if (_.isUndefined(receiptToPay.get('paidInNegativeStatusAmt')) || (!_.isUndefined(receiptToPay.get('paidInNegativeStatusAmt')) && item.get('isPrePayment'))) {
                amountToPay += item.get("amount");
              } else {
                amountToPay -= item.get("amount");
              }
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
          }), callback);
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
    if (btncomponent.btn.command.indexOf('paymentMethodCategory.showitems.') < 0) {
      btncomponent.btn.definition.canTap = function (callback) {
        OB.UTIL.HookManager.executeHooks('OBPOS_PrePaymentSelected', {
          paymentSelected: payment,
          receipt: btncomponent.btn.definition.scope.receipt,
          btnDefintion: btncomponent.btn.definition
        }, function (args) {
          if (args && args.cancellation && args.cancellation === true) {
            callback(false);
          } else {
            callback(true);
          }
        });
      };
    }

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
        providerGroups = {},
        dialogbuttons = {};

    this.inherited(arguments);

    payments = OB.MobileApp.model.get('payments');

    if (payments.length === 0) {
      this.sideButtons = [];
    }

    // Count payment buttons checking provider group and payment method category  
    countbuttons = 0;
    enyo.forEach(payments, function (payment) {
      if (payment.providerGroup) {
        if (!providerGroups[payment.providerGroup.id]) {
          providerGroups[payment.providerGroup.id] = {
            provider: payment.providerGroup,
            _button: null,
            _payments: []
          };
          countbuttons++;
        }
        providerGroups[payment.providerGroup.id]._payments.push(payment);
      } else if (payment.paymentMethod.paymentMethodCategory) {
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

    var sideButtonsCount = _.reduce(this.sideButtons, function (sum, item) {
      return sum + (OB.MobileApp.model.hasPermission(item.permission, true) ? 1 : 0);
    }, 0);
    paymentsdialog = (countbuttons + sideButtonsCount) > 5;
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

      OB.UTIL.HookManager.executeHooks('OBPOS_PreAddPaymentButton', {
        payment: payment,
        sidebuttons: this.sideButtons
      }, function (args) {
        if (args && args.cancelOperation) {
          return;
        }

        if (payment.providerGroup) {
          if (providerGroups[payment.providerGroup.id]._button) {
            btncomponent = null; // already added.
          } else {
            btncomponent = {
              kind: 'OB.UI.BtnSide',
              btn: {
                command: payment.providerGroup.id,
                label: payment.providerGroup._identifier,
                permission: payment.payment.searchKey,
                // Use the permissions of the FIRST payment in the provider group
                definition: {
                  holdActive: true,
                  permission: payment.payment.searchKey,
                  stateless: false,
                  action: function (keyboard, txt) {
                    me.payWithProviderGroup(keyboard, txt, providerGroups[payment.providerGroup.id]);
                  }
                }
              }
            };
            providerGroups[payment.providerGroup.id]._button = btncomponent;
          }
        } else if (payment.paymentMethod.paymentMethodCategory) { // Check for payment method category
          btncomponent = null;
          if (paymentCategories.indexOf(payment.paymentMethod.paymentMethodCategory) === -1) {
            btncomponent = me.getButtonComponent({
              command: 'paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory,
              label: payment.paymentMethod.paymentMethodCategory$_identifier,
              stateless: false,
              action: function (keyboard, txt) {
                var options = {},
                    amount = 0;
                if (txt) {
                  if (_.last(txt) === '%') {
                    options.percentaje = true;
                  }
                  amount = OB.DEC.number(OB.I18N.parseNumber(txt));
                  if (_.isNaN(amount)) {
                    OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
                    return;
                  }
                  var decimalAmount = OB.DEC.toBigDecimal(amount);
                  if (decimalAmount.scale() > OB.DEC.getScale()) {
                    OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [txt]));
                    return;
                  }
                }
                var buttonClass = keyboard.buttons['paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory].attributes['class'];
                if (me.currentPayment && buttonClass.indexOf('btnactive-green') > 0) {
                  me.pay(amount, me.currentPayment.payment.searchKey, me.currentPayment.payment._identifier, me.currentPayment.paymentMethod, me.currentPayment.rate, me.currentPayment.mulrate, me.currentPayment.isocode, options, undefined);
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
          btncomponent = me.getButtonComponent({
            command: payment.payment.searchKey,
            label: payment.payment._identifier + (payment.paymentMethod.paymentMethodCategory ? '*' : ''),
            permission: payment.payment.searchKey,
            stateless: false,
            action: function (keyboard, txt) {
              var options = {},
                  amount = 0;
              if (txt) {
                if (_.last(txt) === '%') {
                  options.percentaje = true;
                }
                amount = OB.DEC.number(OB.I18N.parseNumber(txt));
                if (_.isNaN(amount)) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
                  return;
                }
                var decimalAmount = OB.DEC.toBigDecimal(amount);
                if (decimalAmount.scale() > OB.DEC.getScale()) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [txt]));
                  return;
                }
              }
              me.pay(amount, payment.payment.searchKey, payment.payment._identifier, payment.paymentMethod, payment.rate, payment.mulrate, payment.isocode, options, null);
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
      });
    }, this);

    // Fallback assign of the payment for the exact command.
    exactdefault = exactdefault || cashdefault || payments[0];
    this.defaultPayment = exactdefault;

    enyo.forEach(this.sideButtons, function (sidebutton) {
      if (OB.MobileApp.model.hasPermission(sidebutton.permission, true)) {
        btncomponent = this.getButtonComponent(sidebutton);
        if (countbuttons++ < paymentsbuttons) {
          this.createComponent(btncomponent);
        } else {
          me.addSideButton(btncomponent);
          dialogbuttons[sidebutton.command] = sidebutton.label;
        }
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

    this.owner.owner.addCommand('cashdelivery', {
      action: function (keyboard, txt) {
        var status = keyboard.status.indexOf('paymentMethodCategory.showitems.') === 0 && me.currentPayment ? me.currentPayment.payment.searchKey : keyboard.status;
        if (status && !allpayments[status] && !providerGroups[status]) {
          // Is not a payment, so continue with the default path...
          keyboard.execCommand(status, null);
        } else {
          me.bubble('onClearPaymentSelect');
          var amount = me.model.getPending();
          if (providerGroups[status]) {
            // It is selected  a provider group
            me.payAmountWithProviderGroup(amount, providerGroups[status]);
          } else {
            var exactpayment = allpayments[status] || exactdefault;
            if (exactpayment.providerGroup) {
              // The exact payment belongs to a provider group so call the provider group payment
              me.payAmountWithProviderGroup(amount, providerGroups[exactpayment.providerGroup.id]);
            } else {
              // It is a regular payment
              var altexactamount = me.receipt.get('exactpayment'),
                  pendingPrepayment, total = me.model.getTotal();
              if (me.model.get('leftColumnViewManager').isMultiOrder()) {
                total = OB.DEC.add(total, me.model.get('multiOrders').get('existingPayment') ? me.model.get('multiOrders').get('existingPayment') : OB.DEC.Zero);
              }
              pendingPrepayment = OB.DEC.sub(OB.DEC.add(me.model.getPending(), me.model.getPrepaymentAmount()), total);
              // check if alternate exact amount must be applied based on the payment method selected.
              if (altexactamount && altexactamount[exactpayment.payment.searchKey]) {
                amount = altexactamount[exactpayment.payment.searchKey];
              }

              if (pendingPrepayment > 0 && pendingPrepayment < amount) {
                amount = pendingPrepayment;
              }
              if (exactpayment.rate && exactpayment.rate !== '1') {
                amount = OB.DEC.div(amount, exactpayment.rate);
              }

              if (amount > 0 && exactpayment && OB.MobileApp.model.hasPermission(exactpayment.payment.searchKey)) {
                me.pay(amount, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod, exactpayment.rate, exactpayment.mulrate, exactpayment.isocode);
              }
            }
          }
        }
      }
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
            amount = OB.DEC.div(amount, exactpayment.rate);
          }

          if (amount > 0 && exactpayment && OB.MobileApp.model.hasPermission(exactpayment.payment.searchKey)) {
            me.pay(amount, exactpayment.payment.searchKey, exactpayment.payment._identifier, exactpayment.paymentMethod, exactpayment.rate, exactpayment.mulrate, exactpayment.isocode);
          }
        }
      }
    });
  },
  showAllButtons: function () {
    this.doShowPopup({
      popup: this.morePaymentMethodsPopup,
      args: {
        toolbar: this
      }
    });
  },
  closeAllPopups: function () {
    this.doHidePopup({
      popup: this.morePaymentMethodsPopup
    });
  },
  paymentChanged: function (inSender, inEvent) {
    this.currentPayment = inEvent.payment;
    this.keyboard.payment = inEvent.payment ? inEvent.payment.payment.searchKey : null;
  },
  buttonStatusChanged: function (inSender, inEvent) {
    var me = this,
        status = inEvent.value.status,
        statusPayment = OB.MobileApp.model.paymentnames ? OB.MobileApp.model.paymentnames[status] : null,
        i;

    function setPaymentMethodInfo(payment) {
      me.keyboard.status = 'paymentMethodCategory.showitems.' + payment.paymentMethod.paymentMethodCategory;
      me.bubble('onPaymentChanged', {
        payment: payment,
        status: payment.payment.searchKey
      });
      me.keyboard.showKeypad(me.keyboard.namedkeypads[payment.payment.searchKey] || 'basic');
    }
    if (this.showing) {
      me.bubble('onPaymentChanged');
      if (this.keyboard.lastStatus !== '' && status === '') {
        var defaultPayment = this.defaultPayment;
        //check if defaultPayment is returnable
        if (me.receipt && me.receipt.getPaymentStatus().isNegative && !defaultPayment.paymentMethod.refundable) {
          //if default payment is not returnable, select cash by default
          for (i = 0; i < OB.MobileApp.model.get('payments').length; i++) {
            if (OB.MobileApp.model.get('payments')[i].paymentMethod.iscash && OB.MobileApp.model.get('payments')[i].paymentMethod.refundable) {
              defaultPayment = OB.MobileApp.model.get('payments')[i];
              break;
            }
            if (!defaultPayment.paymentMethod.refundable && OB.MobileApp.model.get('payments')[i].paymentMethod.refundable) {
              defaultPayment = OB.MobileApp.model.get('payments')[i];
            }
          }
        }
        if (defaultPayment.paymentMethod.paymentMethodCategory) {
          var searchKey = 'paymentMethodCategory.showitems.' + defaultPayment.paymentMethod.paymentMethodCategory;
          if (searchKey === this.keyboard.lastStatus) {
            this.keyboard.setStatus(searchKey);
          } else {
            var oldStatus = this.keyboard.status;
            setPaymentMethodInfo(defaultPayment);
            this.keyboard.status = oldStatus;
            this.keyboard.setStatus(defaultPayment.payment.searchKey);
          }
        } else {
          this.keyboard.setStatus(defaultPayment.payment.searchKey);
        }
      }
      if (statusPayment && statusPayment.paymentMethod.paymentMethodCategory) {
        setPaymentMethodInfo(statusPayment);
      }
    }
  },
  shown: function () {
    var me = this,
        refundablePayment, keypad, sideButton, keyboard = this.owner.owner,
        isReturnReceipt = (me.receipt && me.receipt.getPaymentStatus().isNegative) ? true : false;

    keyboard.disableCommandKey(this, {
      commands: ['%'],
      disabled: false
    });

    if (OB.MobileApp.model.get('payments').length) {
      // Enable/Disable Payment method based on refundable flag
      _.each(OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons, function (sideButton) {
        sideButton.active = true;
      });
      _.each(keyboard.activekeypads, function (keypad) {
        keypad.active = true;
      });
      _.each(OB.MobileApp.model.paymentnames, function (payment) {
        keyboard.disableCommandKey(me, {
          disabled: (isReturnReceipt ? !payment.paymentMethod.refundable : false),
          commands: [payment.payment.searchKey]
        });

        if (isReturnReceipt) {
          sideButton = _.find(OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons, function (sideBtn) {
            return sideBtn.btn.command === payment.payment.searchKey;
          });
          if (sideButton) {
            sideButton.active = payment.paymentMethod.refundable;
          }
          keypad = _.find(keyboard.activekeypads, function (keypad) {
            return keypad.payment === payment.payment.searchKey;
          });
          if (keypad) {
            keypad.active = payment.paymentMethod.refundable;
          }
        }
      });

      keyboard.showKeypad('Coins-' + OB.MobileApp.model.get('currency').id); // shows the Coins/Notes panel for the terminal currency
      keyboard.showSidepad('sidedisabled');
      keyboard.hasActivePayment = true;

      if (!isReturnReceipt || (isReturnReceipt && me.defaultPayment.paymentMethod.refundable)) {
        keyboard.defaultcommand = me.defaultPayment.payment.searchKey;
        keyboard.setStatus(me.defaultPayment.payment.searchKey);
      } else {
        refundablePayment = _.find(OB.MobileApp.model.get('payments'), function (payment) {
          return payment.paymentMethod.refundable;
        });
        if (refundablePayment) {
          keyboard.defaultcommand = refundablePayment.payment.searchKey;
          keyboard.setStatus(refundablePayment.payment.searchKey);
        } else {
          keyboard.hasActivePayment = false;
          keyboard.lastStatus = '';
          keyboard.defaultcommand = '';
          keyboard.setStatus('');
        }
      }
    }
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
  classButtonActive: 'btnactive-green',
  classButtonDisabled: 'btnkeyboard-inactive',
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
    if (!this.$.btn.hasClass(this.classButtonDisabled)) {
      this.doShowAllButtons();
    }
  },
  buttonStatusChanged: function (inSender, inEvent) {
    var status = inEvent.value.status;
    this.$.btn.removeClass(this.classButtonDisabled);
    if (this.activegreen) {
      this.$.btn.setContent(OB.I18N.getLabel('OBPOS_MorePayments'));
      this.$.btn.removeClass(this.classButtonActive);
      this.activegreen = false;
    }
    if (this.owner.showing && (this.dialogbuttons[status] || this.dialogbuttons[this.owner.keyboard.status])) {
      this.$.btn.setContent(OB.I18N.getLabel('OBPOS_MorePayments') + ' (' + (this.dialogbuttons[status] || this.dialogbuttons[this.owner.keyboard.status]) + ')');
      this.$.btn.addClass(this.classButtonActive);
      this.activegreen = true;
    }
    if (this.owner.showing) {
      if (_.filter(OB.OBPOSPointOfSale.UI.PaymentMethods.prototype.sideButtons, function (sideButton) {
        return sideButton.active;
      }).length === 0) {
        this.$.btn.removeClass(this.classButtonActive);
        this.$.btn.addClass(this.classButtonDisabled);
      }
    }
    OB.UTIL.createElipsisEffect(this.$.btn);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonSwitch',
  style: 'display:table; width:100%;',
  classButtonDisabled: 'btnkeyboard-inactive',
  handlers: {
    onButtonStatusChanged: 'buttonStatusChanged'
  },
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
    if (!this.$.btn.hasClass(this.classButtonDisabled)) {
      this.keyboard.showNextKeypad();
    }
  },
  buttonStatusChanged: function (inSender, inEvent) {
    if (inEvent.value.status === '' && this.keyboard.getActiveKeypads().length === 1) {
      this.$.btn.addClass(this.classButtonDisabled);
      this.$.btn.setDisabled(true);
    } else {
      this.$.btn.removeClass(this.classButtonDisabled);
      this.$.btn.setDisabled(false);
    }
  },
  create: function () {
    this.inherited(arguments);
    this.keyboard.state.on('change:keypadNextLabel', function () {
      this.setLabel(this.keyboard.state.get('keypadNextLabel'));
    }, this);
    this.setLabel(this.keyboard.state.get('keypadNextLabel'));
  }
});