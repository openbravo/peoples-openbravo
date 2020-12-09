/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo*/

enyo.kind({
  name: 'OB.UI.KeypadCoinsLegacy',
  padName: 'Coins-102',
  padPayment: 'OBPOS_payment.cash',
  classes: 'obUiKeypadCoinsLegacy',
  components: [
    {
      classes: 'obUiKeypadCoinsLegacy-container1',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container1-container1',
          components: [
            {
              kind: 'OB.UI.ButtonKey',
              name: 'OBKEY_legacy_A1',
              classButton:
                'obUiKeypadCoinsLegacy-container1-container1-obKeyLegacyA1 obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
              label: '/',
              command: '/'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container1-container2',
          components: [
            {
              kind: 'OB.UI.ButtonKey',
              classButton:
                'obUiKeypadCoinsLegacy-container1-container2-obKeyLegacyB1 obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
              name: 'OBKEY_legacy_B1',
              label: '*',
              command: '*'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container1-container3',
          components: [
            {
              kind: 'OB.UI.ButtonKey',
              classButton:
                'obUiKeypadCoinsLegacy-container1-container3-obKeyLegacyC1 obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
              name: 'OBKEY_legacy_C1',
              label: '%',
              command: '%'
            }
          ]
        }
      ]
    },
    {
      classes: 'obUiKeypadCoinsLegacy-container2',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container2-container1',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_A2',
              classes:
                'obUiKeypadCoinsLegacy-container2-container1-obKeyObposPaymentCashA2',
              amount: 10,
              coinClass: 'obUiPaymentButton_coin10'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container2-container2',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_B2',
              classes:
                'obUiKeypadCoinsLegacy-container2-container2-obKeyOposPaymentCashB2',
              amount: 20,
              coinClass: 'obUiPaymentButton_coin20'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container2-container3',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_C2',
              classes:
                'obUiKeypadCoinsLegacy-container2-container3-obKeyObposPaymentCashC2',
              amount: 50,
              coinClass: 'obUiPaymentButton_coin50'
            }
          ]
        }
      ]
    },
    {
      classes: 'obUiKeypadCoinsLegacy-container3',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container3-container1',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_A3',
              classes:
                'obUiKeypadCoinsLegacy-container3-container1-obKeyObposPaymentCashA3',
              amount: 1,
              coinClass: 'obUiPaymentButton_coin1'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container3-container2',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_B3',
              classes:
                'obUiKeypadCoinsLegacy-container3-container2-obKeyObposPaymentCashB3',
              amount: 2,
              coinClass: 'obUiPaymentButton_coin2'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container3-container3',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_C3',
              classes:
                'obUiKeypadCoinsLegacy-container3-container3-obKeyObposPaymentCashC3',
              amount: 5,
              coinClass: 'obUiPaymentButton_coin5'
            }
          ]
        }
      ]
    },
    {
      classes: 'obUiKeypadCoinsLegacy-container4',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container4-container1',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_A4',
              classes:
                'obUiKeypadCoinsLegacy-container4-container1-obKeyObposPaymentCashA4',
              amount: 0.1,
              coinClass: 'obUiPaymentButton_coin010'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container4-container2',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_B4',
              classes:
                'obUiKeypadCoinsLegacy-container4-container2-obKeyObposPaymentCashB4',
              amount: 0.2,
              coinClass: 'obUiPaymentButton_coin020'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container4-container3',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_C4',
              classes:
                'obUiKeypadCoinsLegacy-container4-container3-obKeyObposPaymentCashC4',
              amount: 0.5,
              coinClass: 'obUiPaymentButton_coin050'
            }
          ]
        }
      ]
    },
    {
      classes: 'obUiKeypadCoinsLegacy-container5',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container5-container1',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_A5',
              classes:
                'obUiKeypadCoinsLegacy-container5-container1-obKeyObposPaymentCashA5',
              amount: 0.01,
              coinClass: 'obUiPaymentButton_coin001'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container5-container2',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              paymenttype: 'OBPOS_payment.cash',
              name: 'OBKEY_OBPOS_payment.cash_B5',
              classes:
                'obUiKeypadCoinsLegacy-container5-container2-obKeyObposPaymentCashB5',
              amount: 0.02,
              coinClass: 'obUiPaymentButton_coin002'
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container5-container3',
          components: [
            {
              kind: 'OB.UI.PaymentButton',
              name: 'OBKEY_OBPOS_payment.cash_C5',
              classes:
                'obUiKeypadCoinsLegacy-container5-container3-obKeyObposPaymentCashC5',
              paymenttype: 'OBPOS_payment.cash',
              amount: 0.05,
              coinClass: 'obUiPaymentButton_coin005'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.label = OB.I18N.getLabel('OBPOS_KeypadCoins');
  }
});

enyo.kind({
  name: 'OB.UI.PaymentButton',
  classes: 'obUiPaymentButton',
  components: [
    {
      kind: 'OB.UI.Button',
      classes: 'obUiPaymentButton-btn',
      avoidDoubleClick: false,
      name: 'btn',
      tap: function() {
        this.owner.addPayment();
      }
    }
  ],
  coinClass: 'obUiPaymentButton-generic',
  initComponents: function() {
    var btn;
    this.inherited(arguments);

    btn = this.$.btn;
    btn.setContent(this.label || OB.I18N.formatCoins(this.amount));
    if (this.background) {
      btn.applyStyle('background-color', this.background);
      btn.applyStyle(
        'border',
        '10px solid ' + (this.bordercolor || this.background)
      );
    } else {
      btn.addClass(this.coinClass);
    }
  },
  addPayment: function() {
    if (OB.MobileApp.model.hasPermission(this.paymenttype)) {
      var me = this,
        myWindowModel = this.owner.owner.owner.owner.owner.owner.owner.model;
      //FIXME: TOO MANY OWNERS
      var i,
        max,
        p,
        receipt = myWindowModel.get('order'),
        multiOrders = myWindowModel.get('multiOrders'),
        openDrawer = false,
        isCash = false,
        allowOpenDrawer = false,
        printtwice = false,
        paymentStatus = receipt.getPaymentStatus();
      for (
        i = 0, max = OB.MobileApp.model.get('payments').length;
        i < max;
        i++
      ) {
        p = OB.MobileApp.model.get('payments')[i];
        if (p.payment.searchKey === me.paymenttype) {
          if (p.paymentMethod.openDrawer) {
            openDrawer = p.paymentMethod.openDrawer;
          }
          if (p.paymentMethod.iscash) {
            isCash = p.paymentMethod.iscash;
          }
          if (p.paymentMethod.allowopendrawer) {
            allowOpenDrawer = p.paymentMethod.allowopendrawer;
          }
          if (p.paymentMethod.printtwice) {
            printtwice = p.paymentMethod.printtwice;
          }
          break;
        }
      }
      // Calculate total amount to pay with selected PaymentMethod
      var amountToPay = paymentStatus.isNegative ? -me.amount : me.amount;
      var receiptToPay = myWindowModel.isValidMultiOrderState()
        ? multiOrders
        : receipt;
      if (receiptToPay.get('payments').length > 0) {
        receiptToPay.get('payments').each(function(item) {
          if (item.get('kind') === me.paymenttype) {
            if (!paymentStatus.isNegative || item.get('isPrePayment')) {
              amountToPay += item.get('amount');
            } else {
              amountToPay -= item.get('amount');
            }
          }
        });
      }
      // Check Max. Limit Amount
      var paymentMethod =
        OB.POS.terminal.terminal.paymentnames[this.paymenttype];
      if (
        paymentMethod.paymentMethod.maxLimitAmount &&
        amountToPay > paymentMethod.paymentMethod.maxLimitAmount
      ) {
        // Show error and abort payment
        this.bubble('onMaxLimitAmountError', {
          show: true,
          maxLimitAmount: paymentMethod.paymentMethod.maxLimitAmount,
          currency: paymentMethod.symbol,
          symbolAtRight: paymentMethod.currencySymbolAtTheRight
        });
      } else {
        // Hide error and process payment
        this.bubble('onMaxLimitAmountError', {
          show: false,
          maxLimitAmount: 0,
          currency: '',
          symbolAtRight: true
        });
        myWindowModel.addPayment(
          new OB.Model.PaymentLine({
            kind: me.paymenttype,
            name: OB.MobileApp.model.getPaymentName(me.paymenttype),
            amount: OB.DEC.number(me.amount),
            rate: p.rate,
            mulrate: p.mulrate,
            isocode: p.isocode,
            isCash: isCash,
            allowOpenDrawer: allowOpenDrawer,
            openDrawer: openDrawer,
            printtwice: printtwice
          })
        );
      }
    }
  }
});
