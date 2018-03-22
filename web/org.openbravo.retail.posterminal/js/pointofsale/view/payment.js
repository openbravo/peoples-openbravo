/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Payment',
  published: {
    receipt: null
  },
  events: {
    onShowPopup: '',
    onPaymentActionPay: ''
  },
  handlers: {
    onButtonStatusChanged: 'buttonStatusChanged',
    onMaxLimitAmountError: 'maxLimitAmountError',
    onButtonPaymentChanged: 'paymentChanged',
    onClearPaymentMethodSelect: 'clearPaymentMethodSelect',
    ontap: 'dispalyErrorLabels',
    onmouseover: 'pauseAnimation',
    onmouseout: 'resumeAnimation'
  },
  getSelectedPayment: function () {
    if (this.receipt && this.receipt.get('selectedPayment')) {
      return this.receipt.get('selectedPayment');
    }
    return null;
  },
  setTotalPending: function (pending, mulrate, symbol, currencySymbolAtTheRight, inSender, inEvent) {
    this.$.totalpending.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(pending, mulrate), symbol, currencySymbolAtTheRight));
  },
  clearPaymentMethodSelect: function (inSender, inEvent) {
    this.$.paymentMethodSelect.setContent('');
    this.$.paymentMethodSelect.hide();
  },
  buttonStatusChanged: function (inSender, inEvent) {
    this.clearPaymentMethodSelect(inSender, inEvent);
    if (inEvent.value.status && inEvent.value.status.indexOf('paymentMethodCategory.showitems.') === 0) {
      this.doShowPopup({
        popup: 'modalPaymentsSelect',
        args: {
          idCategory: inEvent.value.status.substring(inEvent.value.status.lastIndexOf('.') + 1)
        }
      });
    } else {
      var payment, change, pending, isMultiOrders, paymentstatus;
      payment = inEvent.value.payment || OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
      this.$.noenoughchangelbl.hide();
      if (_.isUndefined(payment)) {
        return true;
      }
      // Clear limit amount error when click on PaymentMethod button
      if (OB.POS.terminal.terminal.paymentnames[inEvent.value.status]) {
        this.bubble('onMaxLimitAmountError', {
          show: false,
          maxLimitAmount: 0,
          currency: '',
          symbolAtRight: true
        });
      }
      if (inEvent.value.status === '' && !inEvent.value.keyboard.hasActivePayment) {
        this.$.exactbutton.hide();
      }
      isMultiOrders = this.model.isValidMultiOrderState();
      change = this.model.getChange();
      pending = this.model.getPending();
      if (!isMultiOrders) {
        if (!_.isNull(this.receipt)) {
          this.receipt.set('selectedPayment', payment.payment.searchKey);
          paymentstatus = this.receipt.getPaymentStatus();
        }
      } else {
        this.model.get('multiOrders').set('selectedPayment', payment.payment.searchKey);
        paymentstatus = this.model.get('multiOrders').getPaymentStatus();
      }

      if (!_.isNull(change) && change) {
        this.$.change.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(change, payment.mulrate), payment.symbol, payment.currencySymbolAtTheRight));
        OB.MobileApp.model.set('changeReceipt', OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(change, payment.mulrate), payment.symbol, payment.currencySymbolAtTheRight));
      } else if (!_.isNull(pending) && pending) {
        this.setTotalPending(pending, payment.mulrate, payment.symbol, payment.currencySymbolAtTheRight, inSender, inEvent);
      }
      if (paymentstatus && inEvent.value.status !== "" && !this.receipt.isCalculateReceiptLocked && !this.receipt.isCalculateGrossLocked) {
        this.checkValidPayments(paymentstatus, payment);
      }
      if (inEvent.value.amount) {
        this.doPaymentActionPay({
          amount: inEvent.value.amount,
          key: payment.payment.searchKey,
          name: payment.payment._identifier,
          paymentMethod: payment.paymentMethod,
          rate: payment.rate,
          mulrate: payment.mulrate,
          isocode: payment.isocode,
          options: inEvent.value.options
        });
      }
    }
  },
  paymentChanged: function (inSender, inEvent) {
    if (!inEvent.amount && inEvent.payment) {
      this.$.paymentMethodSelect.setContent(OB.I18N.getLabel('OBPOS_PaymentsSelectedMethod', [inEvent.payment.payment._identifier]));
      this.$.paymentMethodSelect.show();
    }
  },
  maxLimitAmountError: function (inSender, inEvent) {
    var maxHeight;
    if (inEvent.show) {
      this.$.errorMaxlimitamount.setContent(OB.I18N.getLabel('OBPOS_PaymentMaxLimitAmount', [OB.I18N.formatCurrencyWithSymbol(inEvent.maxLimitAmount, inEvent.currency, inEvent.symbolAtRight)]));
      this.$.errorMaxlimitamount.show();
    } else {
      this.$.errorMaxlimitamount.setContent('');
      this.$.errorMaxlimitamount.hide();
    }
    this.alignErrorMessages();
  },
  components: [{
    style: 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px; position: relative;',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12'
      }]
    }, {
      classes: 'row-fluid',
      components: [{
        classes: 'span9',
        components: [{
          style: 'padding: 10px 0px 0px 10px; height: 28px;',
          components: [{
            tag: 'span',
            name: 'totalpending',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'totalpendinglbl'
          }, {
            tag: 'span',
            name: 'change',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'changelbl'
          }, {
            tag: 'span',
            name: 'overpayment',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'overpaymentlbl'
          }, {
            tag: 'span',
            name: 'exactlbl'
          }, {
            tag: 'span',
            name: 'donezerolbl'
          }]
        }, {
          components: [{
            style: 'padding: 5px',
            components: [{
              style: 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'
            }, {
              kind: 'OB.UI.ScrollableTable',
              scrollAreaMaxHeight: '115px',
              style: 'height: 115px',
              name: 'payments',
              renderEmpty: enyo.kind({
                style: 'height: 36px'
              }),
              renderLine: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine'
            }, {
              kind: 'OB.UI.ScrollableTable',
              scrollAreaMaxHeight: '115px',
              style: 'height: 115px',
              name: 'multiPayments',
              showing: false,
              renderEmpty: enyo.kind({
                style: 'height: 36px'
              }),
              renderLine: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine'
            }, {
              style: 'overflow: hidden; height: 30px; margin-top: 3px; color: #ff0000; padding: 5px; position: relative;',
              name: 'errorLabelArea',
              components: [{
                name: 'noenoughchangelbl',
                showing: false,
                type: 'error'
              }, {
                name: 'changeexceedlimit',
                showing: false,
                type: 'error'
              }, {
                name: 'overpaymentnotavailable',
                showing: false,
                type: 'error'
              }, {
                name: 'overpaymentexceedlimit',
                showing: false,
                type: 'error'
              }, {
                name: 'onlycashpaymentmethod',
                showing: false,
                type: 'error'
              }, {
                name: 'errorMaxlimitamount',
                showing: false,
                type: 'error'
              }, {
                name: 'allAttributesNeedValue',
                type: 'error',
                showing: false
              }, {
                name: 'paymentMethodSelect',
                style: 'color: orange',
                type: 'info',
                showing: false
              }, {
                name: 'extrainfo',
                style: 'font-weight: bold; color: dodgerblue;',
                type: 'info',
                showing: false
              }]

            }]
          }]
        }]
      }, {
        classes: 'span3',
        components: [{
          name: 'donebutton',
          kind: 'OB.OBPOSPointOfSale.UI.DoneButton'
        }, {
          name: 'exactbutton',
          kind: 'OB.OBPOSPointOfSale.UI.ExactButton'
        }, {
          name: 'creditsalesaction',
          kind: 'OB.OBPOSPointOfSale.UI.CreditButton'
        }, {
          name: 'layawayaction',
          kind: 'OB.OBPOSPointOfSale.UI.LayawayButton'
        }]
      }]
    }]
  }],

  receiptChanged: function () {
    var me = this;
    this.$.payments.setCollection(this.receipt.get('payments'));
    this.$.multiPayments.setCollection(this.model.get('multiOrders').get('payments'));
    this.receipt.on('change:payment change:change calculategross change:bp change:gross', function () {
      if (this.receipt.isCalculateReceiptLocked || this.receipt.isCalculateGrossLocked) {
        //We are processing the receipt, we cannot update pending yet
        return;
      }
      this.updatePending();
    }, this);
    this.receipt.on('disableDoneButton', function () {
      this.$.donebutton.setDisabled(true);
    }, this);
    this.receipt.on('updatePending', function () {
      this.updatePending();
    }, this);
    this.receipt.on('paymentCancel', function () {
      this.$.layawayaction.setDisabled(false);
      this.$.donebutton.setDisabled(false);
      this.$.creditsalesaction.putDisabled(false);
      if (OB.MobileApp.view.openedPopup === null) {
        enyo.$.scrim.hide();
      }
      OB.UTIL.showLoading(false);
    }, this);
    this.model.get('multiOrders').on('paymentCancel', function () {
      if (OB.MobileApp.view.openedPopup === null) {
        enyo.$.scrim.hide();
      }
      OB.UTIL.showLoading(false);
    }, this);
    this.model.get('leftColumnViewManager').on('change:currentView', function () {
      if (!this.model.get('leftColumnViewManager').isMultiOrder()) {
        this.updatePending();
      } else {
        this.updatePendingMultiOrders();
      }
    }, this);
    this.updatePending();
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.updatePendingMultiOrders();
    }
    this.receipt.on('change:orderType change:isLayaway change:payment change:documentNo', function (model) {
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        this.updateCreditSalesAction();
        this.$.layawayaction.hide();
        return;
      }
      var payment = OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
      if ((model.get('orderType') === 2 || (model.get('isLayaway'))) && model.get('orderType') !== 3 && !model.getPaymentStatus().done && _.isUndefined(model.get('paidInNegativeStatusAmt'))) {
        this.$.layawayaction.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
        this.$.layawayaction.setDisabled(false);
        this.$.layawayaction.show();
      } else {
        this.$.layawayaction.hide();
      }
      this.updateCreditSalesAction();
    }, this);
    this.updateExtraInfo('');
    this.receipt.on('extrainfo', function (info) {
      this.updateExtraInfo(info);
    }, this);
  },

  updateExtraInfo: function (info) {
    this.$.extrainfo.setContent(info || '');
    if (info && info.trim() !== '') {
      this.$.extrainfo.show();
    } else {
      this.$.extrainfo.hide();
    }
    this.alignErrorMessages();
  },

  updateCreditSalesAction: function () {

    // The terminal allows to pay on credit
    var visible = OB.MobileApp.model.get('terminal').allowpayoncredit;
    // And is a loaded layaway or a regular order (no new layaway and no voided layaway)
    // this.receipt.get('orderType') === 2 --> New layaway 
    // this.receipt.get('orderType') === 3 --> Voided layaway 
    // this.receipt.get('isLayaway') --> Loaded layaway    
    visible = visible && ((this.receipt.get('isLayaway') || this.receipt.get('orderType') !== 2) && this.receipt.get('orderType') !== 3);
    // And receipt has not been paid
    visible = visible && !this.receipt.getPaymentStatus().done;
    // And Business Partner exists and is elegible to sell on credit.
    visible = visible && this.receipt.get('bp') && (this.receipt.get('bp').get('creditLimit') > 0 || this.receipt.get('bp').get('creditUsed') < 0 || this.receipt.getGross() < 0);

    if (visible) {
      this.$.creditsalesaction.show();
    } else {
      this.$.creditsalesaction.hide();
    }
  },

  updatePending: function () {
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      return true;
    }
    var paymentstatus = this.receipt.getPaymentStatus();
    var symbol = '',
        rate = OB.DEC.One,
        symbolAtRight = true,
        isCashType = true;

    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      symbol = OB.MobileApp.model.get('terminal').symbol;
      symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
    }
    if (!_.isUndefined(this.receipt) && !_.isUndefined(OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')])) {
      symbol = OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')].symbol;
      rate = OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')].mulrate;
      symbolAtRight = OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')].currencySymbolAtTheRight;
      isCashType = OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')].paymentMethod.iscash;
    }
    this.checkValidPayments(paymentstatus, OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment') || OB.MobileApp.model.get('paymentcash')]);
    if (paymentstatus.change) {
      this.$.change.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(this.receipt.getChange(), rate), symbol, symbolAtRight));
      OB.MobileApp.model.set('changeReceipt', OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(this.receipt.getChange(), rate), symbol, symbolAtRight));
      this.$.change.show();
      this.$.changelbl.show();
    } else {
      this.$.change.hide();
      this.$.changelbl.hide();
    }
    if (paymentstatus.overpayment) {
      this.$.overpayment.setContent(OB.I18N.formatCurrencyWithSymbol(paymentstatus.overpayment, symbol, symbolAtRight));
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (paymentstatus.done) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      if (!_.isEmpty(OB.MobileApp.model.paymentnames) || this.receipt.get('orderType') === 3) {
        this.$.donebutton.show();
      }
      this.updateCreditSalesAction();
      this.$.layawayaction.hide();
    } else {
      this.setTotalPending(this.receipt.getPending(), rate, symbol, symbolAtRight);
      this.$.totalpending.show();
      if (paymentstatus.isNegative) {
        this.$.totalpendinglbl.setContent(OB.I18N.getLabel('OBPOS_ReturnRemaining'));
      } else {
        this.$.totalpendinglbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsRemaining'));
      }
      this.$.totalpendinglbl.show();
      this.$.donebutton.hide();
      if (this.$.donebutton.drawerpreference) {
        this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.donebutton.drawerOpened = false;
      }
    }

    if (paymentstatus.done || this.receipt.getGross() === 0) {
      this.$.exactbutton.hide();
      this.$.layawayaction.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        this.$.exactbutton.show();
      }
      if ((this.receipt.get('orderType') === 2 || (this.receipt.get('isLayaway') && this.receipt.get('orderType') !== 3)) && _.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
        this.$.layawayaction.show();
      } else if (this.receipt.get('orderType') === 3 && _.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
        this.$.layawayaction.hide();
      } else if (!_.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
        this.$.layawayaction.hide();
        this.$.exactbutton.show();
      }
    }
    if (paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
      if (this.receipt.getGross() === 0) {
        this.$.exactlbl.hide();
        this.$.donezerolbl.show();
      } else {
        this.$.donezerolbl.hide();
        //        if (this.receipt.get('orderType') === 1 || this.receipt.get('orderType') === 3) {
        if (paymentstatus.isNegative) {
          this.$.exactlbl.setContent(OB.I18N.getLabel('OBPOS_ReturnExact'));
        } else {
          this.$.exactlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsExact'));
        }
        this.$.exactlbl.show();
      }
    } else {
      this.$.exactlbl.hide();
      this.$.donezerolbl.hide();
    }

    this.updateCreditSalesAction();
  },
  updatePendingMultiOrders: function () {
    var paymentstatus = this.model.get('multiOrders');
    var symbol = '',
        symbolAtRight = true,
        rate = OB.DEC.One,
        isCashType = true,
        selectedPayment;
    this.updateExtraInfo('');
    this.$.layawayaction.hide();
    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      symbol = OB.MobileApp.model.get('terminal').symbol;
      symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
    }
    if (paymentstatus.get('selectedPayment')) {
      selectedPayment = OB.MobileApp.model.paymentnames[paymentstatus.get('selectedPayment')];
    } else {
      selectedPayment = OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
    }
    if (!_.isUndefined(selectedPayment)) {
      symbol = selectedPayment.symbol;
      rate = selectedPayment.mulrate;
      symbolAtRight = selectedPayment.currencySymbolAtTheRight;
      isCashType = selectedPayment.paymentMethod.iscash;
    }
    this.checkValidPayments(paymentstatus.getPaymentStatus(), selectedPayment);
    if (paymentstatus.get('change')) {
      this.$.change.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(paymentstatus.get('change'), rate), symbol, symbolAtRight));
      OB.MobileApp.model.set('changeReceipt', OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(paymentstatus.get('change'), rate), symbol, symbolAtRight));
      this.$.change.show();
      this.$.changelbl.show();
    } else {
      this.$.change.hide();
      this.$.changelbl.hide();
    }
    //overpayment
    if (OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) > 0) {
      this.$.overpayment.setContent(OB.I18N.formatCurrency(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))));
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (paymentstatus.get('multiOrdersList').length > 0 && OB.DEC.compare(paymentstatus.get('total')) >= 0 && OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) >= 0) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        this.$.donebutton.show();
      }
      this.updateCreditSalesAction();
    } else {
      this.setTotalPending(OB.DEC.sub(paymentstatus.get('total'), paymentstatus.get('payment')), rate, symbol, symbolAtRight);
      this.$.totalpending.show();
      this.$.totalpendinglbl.show();
      this.$.donebutton.hide();
      if (this.$.donebutton.drawerpreference) {
        this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.donebutton.drawerOpened = false;
      }
    }

    this.updateCreditSalesAction();
    this.$.layawayaction.hide();
    if (paymentstatus.get('multiOrdersList').length > 0 && OB.DEC.compare(paymentstatus.get('total')) >= 0 && (OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) >= 0 || paymentstatus.get('total') === 0)) {
      this.$.exactbutton.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        this.$.exactbutton.show();
      }
    }
    if (paymentstatus.get('multiOrdersList').length > 0 && OB.DEC.compare(paymentstatus.get('total')) >= 0 && OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) >= 0 && !paymentstatus.get('change') && OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) <= 0) {
      if (paymentstatus.get('total') === 0) {
        this.$.exactlbl.hide();
        this.$.donezerolbl.show();
      } else {
        this.$.donezerolbl.hide();
        this.$.exactlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsExact'));
        this.$.exactlbl.show();
      }
    } else {
      this.$.exactlbl.hide();
      this.$.donezerolbl.hide();
    }
  },

  checkEnoughCashAvailable: function (paymentstatus, selectedPayment, scope, button, callback) {
    var requiredCash, hasEnoughCash = true,
        hasAllEnoughCash = true,
        reversedPayments = [],
        currentSelectedPaymentCashAmount = OB.DEC.Zero,
        reversedCash;
    // Check slave cash 
    this.checkSlaveCashAvailable(selectedPayment, this, function (currentCash) {
      // If there are reverse payments search for those of cash payment method. It will be needed to check if there is enough cash to reverse those payments.
      if (paymentstatus.isReversal) {
        paymentstatus.payments.each(function (payment) {
          var paymentmethod = OB.POS.terminal.terminal.paymentnames[payment.get('kind')];
          if (!payment.get('isPrePayment') && paymentmethod.paymentMethod.iscash) {
            reversedCash = OB.DEC.sub(reversedPayments[payment.get('kind')] || OB.DEC.Zero, payment.get('origAmount'));
            reversedPayments[payment.get('kind')] = reversedCash;
            if (selectedPayment !== paymentmethod && OB.DEC.compare(OB.DEC.sub(paymentmethod.currentCash, reversedCash)) < 0) {
              hasEnoughCash = false;
            } else {
              currentSelectedPaymentCashAmount = reversedCash;
            }
          }
        });
      }

      if (hasEnoughCash) {
        if (OB.UTIL.isNullOrUndefined(selectedPayment) || !selectedPayment.paymentMethod.iscash) {
          requiredCash = OB.DEC.Zero;
        } else if ((button === 'Done' || button === 'Credit') && !_.isUndefined(paymentstatus) && (paymentstatus.isNegative)) {
          requiredCash = OB.DEC.add(currentSelectedPaymentCashAmount, paymentstatus.pendingAmt);
          paymentstatus.payments.each(function (payment) {
            var paymentmethod;
            if (payment.get('kind') === selectedPayment.payment.searchKey && !payment.get('isPrePayment') && !payment.get('reversedPaymentId')) {
              requiredCash = OB.DEC.add(requiredCash, payment.get('origAmount'));
            } else {
              paymentmethod = OB.POS.terminal.terminal.paymentnames[payment.get('kind')];
              if (paymentmethod && payment.get('amount') > paymentmethod.currentCash && payment.get('isCash')) {
                hasAllEnoughCash = false;
              }
            }
          });
        } else if (!_.isUndefined(paymentstatus)) {
          if (button === 'Layaway' || button === 'Credit') {
            requiredCash = OB.DEC.add(currentSelectedPaymentCashAmount, paymentstatus.changeAmt);
          } else {
            requiredCash = OB.DEC.sub(OB.DEC.add(currentSelectedPaymentCashAmount, paymentstatus.changeAmt), paymentstatus.pendingAmt);
          }
        }

        if (!_.isUndefined(requiredCash) && requiredCash === 0) {
          hasEnoughCash = true;
        } else if (!_.isUndefined(requiredCash)) {
          hasEnoughCash = OB.DEC.compare(OB.DEC.sub(currentCash, requiredCash)) >= 0;
        }
      }

      if (hasEnoughCash && ((button === 'Layaway' || button === 'Credit') || (button === 'Done' && hasAllEnoughCash))) {
        return callback.call(scope, true);
      } else {
        return callback.call(scope, false); // check failed.
      }
    });
  },

  checkValidOverpayment: function (paymentstatus) {
    var requiredOverpayment = paymentstatus.overpayment,
        overPaymentUsed = _.last(paymentstatus.payments.models),
        overPaymentMethod = overPaymentUsed && OB.MobileApp.model.paymentnames[overPaymentUsed.get('kind')] ? OB.MobileApp.model.paymentnames[overPaymentUsed.get('kind')].paymentMethod : undefined;

    // Execute logic only if all the following requirements are met:
    //  * There is at least one payment added to the receipt
    //  * The payment method has a overpayment limit defined
    //  * There is an overpayment
    if (overPaymentMethod && !OB.UTIL.isNullOrUndefined(overPaymentMethod.overpaymentLimit) && !OB.UTIL.isNullOrUndefined(requiredOverpayment)) {
      var overpaymentAmt = new BigDecimal(String(requiredOverpayment));
      var overpaymentLimit = new BigDecimal(String(overPaymentMethod.overpaymentLimit));
      if (overpaymentAmt.compareTo(BigDecimal.prototype.ZERO) !== 0) {
        if (overpaymentLimit.compareTo(BigDecimal.prototype.ZERO) === 0 && overpaymentLimit.compareTo(overpaymentAmt) < 0) {
          this.$.overpaymentnotavailable.show();
          return false;
        } else if (overpaymentLimit.compareTo(overpaymentAmt) < 0) {
          this.$.overpaymentexceedlimit.show();
          return false;
        } else {
          return true;
        }
      } else if (overpaymentAmt.compareTo(BigDecimal.prototype.ZERO) === 0) {
        return true;
      }
    } else {
      return true;
    }
  },

  checkValidCashChange: function (paymentstatus, selectedPayment) {
    if (!selectedPayment) {
      return false;
    }

    var requiredCash;

    if (OB.UTIL.isNullOrUndefined(selectedPayment) || OB.UTIL.isNullOrUndefined(selectedPayment.paymentMethod.overpaymentLimit)) {
      return true;
    }

    requiredCash = paymentstatus.changeAmt;
    if (!OB.UTIL.isNullOrUndefined(requiredCash)) {
      requiredCash = OB.DEC.toNumber(requiredCash);
    }
    if (requiredCash !== 0) {
      if (selectedPayment.paymentMethod.overpaymentLimit === 0 && selectedPayment.paymentMethod.overpaymentLimit < requiredCash) {
        this.$.changeexceedlimit.show();
        return false;
      } else if (selectedPayment.paymentMethod.overpaymentLimit < requiredCash) {
        this.$.changeexceedlimit.show();
        return false;
      } else {
        return true;
      }
    } else if (requiredCash === 0) {
      return true;
    }
    return true;
  },

  checkValidPaymentMethod: function (paymentstatus, selectedPayment) {
    if (!selectedPayment) {
      return false;
    }

    var change = this.model.getChange();
    var check = true;
    var currentcash = selectedPayment.currentCash;
    var cashIsPresent = false;
    var alternativeCashPayment;
    var alternativePaymentInfo;
    if (change && change > 0) {
      if (!selectedPayment.paymentMethod.iscash && paymentstatus.payments.length > 0) {
        alternativeCashPayment = _.find(paymentstatus.payments.models, function (item) {
          if (item.get('isCash')) {
            return item;
          }
        });
        if (alternativeCashPayment) {
          alternativePaymentInfo = _.find(OB.MobileApp.model.get('payments'), function (defPayment) {
            if (defPayment.payment.searchKey === alternativeCashPayment.get('kind')) {
              return defPayment;
            }
          });
        }
        if (!alternativeCashPayment) {
          check = false;
          this.$.onlycashpaymentmethod.show();
        } else if (alternativePaymentInfo && alternativePaymentInfo.currentCash < change) {
          check = false;
          this.$.noenoughchangelbl.show();
        }
      } else {
        if (currentcash < change) {
          check = false;
          this.$.noenoughchangelbl.show();
        }
      }
    }
    return check;
  },

  checkValidPayments: function (paymentstatus, selectedPayment) {
    var resultOK, me = this;

    if (!selectedPayment) {
      return;
    }
    // Hide all error labels. Error labels are shown by check... functions
    if (_.isNull(paymentstatus.overpayment)) {
      this.$.changeexceedlimit.hide();
      this.$.overpaymentnotavailable.hide();
      this.$.overpaymentexceedlimit.hide();
      this.$.allAttributesNeedValue.hide();
    }
    this.$.noenoughchangelbl.hide();
    this.$.onlycashpaymentmethod.hide();

    // Do the checkins
    this.receipt.stopAddingPayments = !_.isEmpty(this.getShowingErrorMessages());
    if (this.checkValidOverpayment(paymentstatus)) {
      if (selectedPayment.paymentMethod.iscash && paymentstatus.changeAmt > 0) {
        resultOK = this.checkValidCashChange(paymentstatus, selectedPayment);
      } else {
        resultOK = undefined;
      }
    } else {
      resultOK = false;
    }
    if (resultOK || _.isUndefined(resultOK)) {
      if (!_.isNull(paymentstatus.change) || ((paymentstatus.isNegative || paymentstatus.isReversal) && !_.isNull(paymentstatus.pending))) {
        // avoid checking for shared paymentMethod
        if (paymentstatus.change && selectedPayment.paymentMethod.isshared) {
          resultOK = true;
        } else {
          resultOK = this.checkEnoughCashAvailable(paymentstatus, selectedPayment, this, 'Done', function (success) {
            var lsuccess = success;
            if (lsuccess) {
              lsuccess = this.checkValidPaymentMethod(paymentstatus, selectedPayment);
            } else {
              this.$.noenoughchangelbl.show();
              this.$.donebutton.setLocalDisabled(true);
              this.$.exactbutton.setLocalDisabled(true);
            }
            me.receipt.stopAddingPayments = !_.isEmpty(me.getShowingErrorMessages());
            this.setStatusButtons(lsuccess, 'Done');
            this.checkEnoughCashAvailable(paymentstatus, selectedPayment, this, 'Layaway', function (success) {
              this.setStatusButtons(success, 'Layaway');
            });
            this.checkEnoughCashAvailable(paymentstatus, selectedPayment, this, 'Credit', function (success) {
              this.setStatusButtons(success, 'Credit');
            });
          });
        }
      } else if (!this.receipt.stopAddingPayments) {
        this.$.donebutton.setLocalDisabled(false);
        this.$.exactbutton.setLocalDisabled(false);
        this.$.layawayaction.setLocalDisabled(false);
        this.$.creditsalesaction.setLocalDisabled(false);
      }

    } else {
      me.receipt.stopAddingPayments = !_.isEmpty(me.getShowingErrorMessages());
      // Finally set status of buttons
      this.setStatusButtons(resultOK, 'Done');
      this.setStatusButtons(resultOK, 'Layaway');
    }
    if (resultOK) {
      this.$.noenoughchangelbl.hide();
    }

    // check that all attributes has value
    if (OB.MobileApp.model.hasPermission('OBPOS_EnableSupportForProductAttributes', true) && paymentstatus.done && !this.receipt.checkAllAttributesHasValue()) {
      this.$.donebutton.setLocalDisabled(true);
      this.$.allAttributesNeedValue.show();
    }


    this.alignErrorMessages();
  },
  alignErrorMessages: function () {
    if (OB.MobileApp.view.currentWindow === 'retail.pointofsale' && typeof (this.$.errorLabelArea) !== 'undefined') {
      var me = this,
          delay = 1500;
      this.errorLabels = this.pushErrorMessagesToArray();
      this.showingCount = this.getShowingMessagesCount(this.errorLabels);
      clearInterval(this.maxAnimateErrorInterval);
      // 2 interval Max ,Min defined here
      // Min Interval Fuction Get Exexuted Based On the Max Interval 
      // Paramaters ,It will Reset After Every Max Interval 
      // In Order To Behave Like Animation Of The Text Error Messages 
      // To Fit into the Payment Area Where Error Messages To Be Shown
      me.animateErrorMessages();
      if (this.showingCount > 1) {
        this.maxAnimateErrorInterval = setInterval(function () {
          clearInterval(this.animateErrorInterval);
          me.animateErrorMessages();
        }, delay + 1700 * this.showingCount);
      }
    }

  },
  animateErrorMessages: function () {
    if (OB.MobileApp.view.currentWindow === 'retail.pointofsale' && typeof (this.$.errorLabelArea) !== 'undefined') {
      clearInterval(this.animateErrorInterval);
      var me = this,
          marginTop = 0,
          resizediStyle = '',
          initialTop = 0,
          defaultStyle = 'position: absolute; bottom: 0px; height: 20px; color: #ff0000;';
      this.errorLabels = this.pushErrorMessagesToArray();
      this.showingCount = this.getShowingMessagesCount(this.errorLabels);
      this.firstShowingObject = this.getFirstShowingObject(this.errorLabels);
      if (this.firstShowingObject && this.showingCount > 1) {
        this.animateErrorInterval = setInterval(function () {
          marginTop = marginTop - 2;
          this.marginTop = marginTop;
          resizediStyle = 'margin-top: ' + marginTop + 'px';
          me.firstShowingObject.addStyles(resizediStyle);
        }, 100);
      }
      if (this.showingCount === 1) {
        defaultStyle = 'margin-top: ' + initialTop + 'px';
        this.firstShowingObject.addStyles(defaultStyle);
      }
    }
  },
  pushErrorMessagesToArray: function () {
    var errorLabelArray = [];
    errorLabelArray.push(this.$.noenoughchangelbl);
    errorLabelArray.push(this.$.changeexceedlimit);
    errorLabelArray.push(this.$.overpaymentnotavailable);
    errorLabelArray.push(this.$.overpaymentexceedlimit);
    errorLabelArray.push(this.$.onlycashpaymentmethod);
    errorLabelArray.push(this.$.errorMaxlimitamount);
    errorLabelArray.push(this.$.allAttributesNeedValue);
    errorLabelArray.push(this.$.paymentMethodSelect);
    errorLabelArray.push(this.$.extrainfo);
    return errorLabelArray;
  },
  getFirstShowingObject: function (errorLabelArray) {
    var showingObj = '',
        i;
    for (i = 0; i < errorLabelArray.length; i++) {
      var arrayContent = errorLabelArray[i];
      if (arrayContent.showing) {
        showingObj = arrayContent;
        break;
      }
    }
    return showingObj;
  },
  getShowingMessagesCount: function (errorLabelArray) {
    var count = 0,
        i;
    for (i = 0; i < errorLabelArray.length; i++) {
      var arrayContent = errorLabelArray[i];
      if (arrayContent.showing) {
        count = count + 1;
      }
    }
    return count;
  },
  resumeAnimation: function (inSender, inEvent) {
    if (inEvent.originator.type === 'error' || inEvent.originator.type === 'info') {
      this.alignErrorMessages();
    }

  },
  pauseAnimation: function (inSender, inEvent) {
    if (inEvent.originator.type === 'error') {
      clearInterval(this.maxAnimateErrorInterval);
      clearInterval(this.animateErrorInterval);
      inEvent.originator.addStyles(this.marginTop);
    }

  },
  dispalyErrorLabels: function (inSender, inEvent) {
    if (inEvent.originator.type === 'error') {
      var message = this.getShowingErrorMessages();
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), message);
      this.alignErrorMessages(false);
    }
  },
  getShowingErrorMessages: function () {
    var msgToReturn = '';
    var count = 0,
        i;
    if (this.errorLabels) {
      for (i = 0; i < this.errorLabels.length; i++) {
        var arrayContent = this.errorLabels[i];
        if (arrayContent.showing && arrayContent.type === 'error') {
          count = count + 1;
          msgToReturn = msgToReturn + '\n' + count + ')' + arrayContent.content;
        }
      }
    }
    return msgToReturn;
  },
  setStatusButtons: function (resultOK, button) {
    if (button === 'Done') {
      if (resultOK) {
        this.$.donebutton.setLocalDisabled(false);
        this.$.exactbutton.setLocalDisabled(false);
      } else {
        if (this.$.changeexceedlimit.showing || this.$.overpaymentnotavailable.showing || this.$.overpaymentexceedlimit.showing || this.$.onlycashpaymentmethod.showing) {
          this.$.noenoughchangelbl.hide();
        } else {
          this.$.noenoughchangelbl.show();
        }
        this.$.donebutton.setLocalDisabled(true);
        this.$.exactbutton.setLocalDisabled(true);
      }
    } else if (button === 'Layaway') {
      if (resultOK) {
        this.$.layawayaction.setLocalDisabled(false);
      } else {
        this.$.layawayaction.setLocalDisabled(true);
      }
    } else if (button === 'Credit') {
      if (resultOK) {
        this.$.creditsalesaction.setLocalDisabled(false);
      } else {
        // If there is not enought cash to return and the user is doing a reverse payment (the negative cash payment has
        // been introduced), the "Use Credit" button must also be disabled
        // In a return or a positive ticket with negative payments, the "Use Credit" button can be enabled, because the cash
        // payment has not been added yet
        if (this.receipt.getPaymentStatus().isReversal) {
          this.$.creditsalesaction.setLocalDisabled(true);
        }
      }
    }
  },

  checkSlaveCashAvailable: function (selectedPayment, scope, callback) {

    function processCashMgmtMaster(cashMgntCallback) {
      new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmtMaster').exec({
        cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
        terminalSlave: OB.POS.modelterminal.get('terminal').isslave
      }, function (data) {
        if (data && data.exception) {
          // Error handler 
          OB.log('error', data.exception.message);
          OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashMgmtError'), OB.I18N.getLabel('OBPOS_ErrorServerGeneric') + data.exception.message, [{
            label: OB.I18N.getLabel('OBPOS_LblRetry'),
            action: function () {
              processCashMgmtMaster(cashMgntCallback);
            }
          }], {
            autoDismiss: false,
            onHideFunction: function () {
              cashMgntCallback(false, null);
            }
          });
        } else {
          cashMgntCallback(true, data);
        }
      });
    }

    var currentCash = OB.DEC.Zero;
    if (selectedPayment && selectedPayment.paymentMethod.iscash) {
      currentCash = selectedPayment.currentCash || OB.DEC.Zero;
    }
    if ((OB.POS.modelterminal.get('terminal').ismaster || OB.POS.modelterminal.get('terminal').isslave) && selectedPayment.paymentMethod.iscash && selectedPayment.paymentMethod.isshared) {
      // Load current cashup info from slaves
      processCashMgmtMaster(function (success, data) {
        if (success) {
          _.each(data, function (pay) {
            if (pay.searchKey === selectedPayment.payment.searchKey) {
              currentCash = OB.DEC.add(currentCash, pay.startingCash + pay.totalDeposits + pay.totalSales - pay.totalReturns - pay.totalDrops);
            }
          });
        }
        callback.call(scope, currentCash);
      });
    } else {
      callback.call(scope, currentCash);
    }
  },

  initComponents: function () {
    this.inherited(arguments);
    this.$.errorLabelArea.render();
    this.$.totalpendinglbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsRemaining'));
    this.$.changelbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsChange'));
    this.$.overpaymentlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsOverpayment'));
    this.$.exactlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsExact'));
    this.$.donezerolbl.setContent(OB.I18N.getLabel('OBPOS_MsgPaymentAmountZero'));
    this.$.noenoughchangelbl.setContent(OB.I18N.getLabel('OBPOS_NoEnoughCash'));
    this.$.changeexceedlimit.setContent(OB.I18N.getLabel('OBPOS_ChangeLimitOverLimit'));
    this.$.overpaymentnotavailable.setContent(OB.I18N.getLabel('OBPOS_OverpaymentNotAvailable'));
    this.$.overpaymentexceedlimit.setContent(OB.I18N.getLabel('OBPOS_OverpaymentExcededLimit'));
    this.$.onlycashpaymentmethod.setContent(OB.I18N.getLabel('OBPOS_OnlyCashPaymentMethod'));
    this.$.allAttributesNeedValue.setContent(OB.I18N.getLabel('OBPOS_AllAttributesNeedValue'));
  },
  init: function (model) {
    var me = this;
    this.model = model;
    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      this.$.donebutton.show();
      this.$.exactbutton.hide();
    }
    this.model.get('multiOrders').get('multiOrdersList').on('all', function (event) {
      if (this.model.isValidMultiOrderState()) {
        this.updatePendingMultiOrders();
      }
    }, this);

    this.model.get('multiOrders').on('change:payment change:total change:change paymentCancel', function () {
      this.updatePendingMultiOrders();
    }, this);
    this.model.get('multiOrders').on('disableDoneButton', function () {
      this.$.donebutton.setDisabled(true);
    }, this);
    this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
      if (changedModel.isOrder()) {
        this.$.multiPayments.hide();
        this.$.payments.show();
        return;
      }
      if (changedModel.isMultiOrder()) {
        this.$.multiPayments.show();
        this.$.payments.hide();
        return;
      }
    }, this);
    //    this.model.get('multiOrders').on('change:isMultiOrders', function () {
    //      if (!this.model.get('multiOrders').get('isMultiOrders')) {
    //        this.$.multiPayments.hide();
    //        this.$.payments.show();
    //      } else {
    //        this.$.payments.hide();
    //        this.$.multiPayments.show();
    //      }
    //    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  kind: 'OB.UI.RegularButton',
  style: 'width: 85%; max-width: 125px; float: right; margin: 5px 5px 10px 0px; height: 2.5em; display:block; clear: right; font-weight: normal; padding: 0px',
  processdisabled: false,
  localdisabled: false,
  setLocalDisabled: function (value) {
    this.localdisabled = value;
    this.setDisabled(this.processdisabled || this.localdisabled);
  },
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    OB.POS.EventBus.on('UI_Enabled', function (state) {
      me.processdisabled = !state;
      me.setDisabled(me.processdisabled || me.localdisabled);
    });
    me.processdisabled = !OB.POS.EventBus.isProcessEnabled();
    me.setDisabled(me.processdisabled || me.localdisabled);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.DoneButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  handlers: {
    synchronizing: 'isSynchronizing',
    synchronized: 'isSynchronized'
  },
  drawerOpened: true,
  isLocked: true,
  lasDisabledPetition: true,
  isSynchronizing: function () {
    this.isLocked = true;
    this.setDisabledIfSynchronized();
  },
  isSynchronized: function () {
    this.isLocked = false;
    this.setDisabledIfSynchronized();
  },
  setDisabled: function (value) {
    this.lasDisabledPetition = value;
    this.setDisabledIfSynchronized();
  },
  setDisabledIfSynchronized: function () {
    var value = this.lasDisabledPetition;
    // check arguments
    if (value === undefined) {
      // be sure that the value is always valid
      OB.UTIL.Debug.execute(function () {
        throw "The disabled value must be true or false";
      });
      value = false;
    }
    // force disabled is there are pending synchronizations
    if (this.isLocked) {
      value = true;
    }
    // if there are no not synchronized payments reversed and the full amount qty is paid by prePayment payments,
    // the button 'Done' will be desabled (except for the case of doing a cancel and replace)
    if (!value) {
      if (this.owner.receipt && this.owner.receipt.get('payments') && this.owner.receipt.get('payments').size() > 0) {
        if (!this.owner.receipt.get('doCancelAndReplace') && (this.owner.receipt.get('isLayaway') ? (OB.DEC.number(this.owner.receipt.getPayment()) < OB.DEC.sub(this.owner.receipt.getTotal(), this.owner.receipt.getCredit())) : (this.owner.receipt.getPrePaymentQty() === OB.DEC.sub(this.owner.receipt.getTotal(), this.owner.receipt.getCredit()))) && !this.owner.receipt.isNewReversed()) {
          value = true;
        }
      } else if (this.owner.receipt && this.owner.receipt.get('isPaid') && this.owner.receipt.getGross() === 0) {
        value = true;
      }
    }
    this.disabled = value; // for getDisabled() to return the correct value
    this.setAttribute('disabled', value); // to effectively turn the button enabled or disabled    
  },
  init: function (model) {
    this.model = model;
    this.setDisabledIfSynchronized();
    this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
    this.model.get('order').on('change:openDrawer', function () {
      this.drawerpreference = this.model.get('order').get('openDrawer');
      var me = this;

      if (this.drawerpreference) {
        this.drawerOpened = false;
        this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
      } else {
        this.drawerOpened = true;
        this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
      }
    }, this);
    this.model.get('multiOrders').on('change:openDrawer', function () {
      this.drawerpreference = this.model.get('multiOrders').get('openDrawer');
      if (this.drawerpreference) {
        this.drawerOpened = false;
        this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
      } else {
        this.drawerOpened = true;
        this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
      }
    }, this);
  },
  blocked: false,
  tap: function () {
    var myModel = this.owner.model,
        me = this,
        payments, isMultiOrder = myModel.get('leftColumnViewManager').isOrder() ? false : true,
        avoidPayment = false,
        orderDesc = '';

    // Avoid closing the order before receipt is being calculated
    if (this.owner.receipt.calculatingReceipt) {
      OB.UTIL.showI18NError('OBPOS_ReceiptBeingPrepared');
      return;
    }

    //*** Avoid double click ***
    if (this.getContent() === OB.I18N.getLabel('OBPOS_LblDone')) {
      if (this.owner.receipt && this.owner.receipt.getOrderDescription) {
        orderDesc = this.owner.receipt.getOrderDescription();
      }
      OB.info('Time: ' + new Date() + '. Payment Button Pressed ( Status: ' + this.disabled + ') ' + orderDesc);
      if (me.blocked) {
        OB.error('Time: ' + new Date() + '. Done button has been pressed 2 times and second execution is discarded ' + orderDesc);
        return;
      } else {
        me.blocked = true;
        setTimeout(function () {
          me.blocked = false;
        }, 1000);
      }
    }

    this.avoidCompleteReceipt = false;
    this.alreadyPaid = false;
    this.allowOpenDrawer = false;

    if (this.disabled) {
      return true;
    }

    if (isMultiOrder) {
      var receipts = this.owner.model.get('multiOrders').get('multiOrdersList').models;
      receipts.forEach(function (receipt) {
        if ((receipt.get('orderType') === 2 || receipt.get('orderType') === 3) && receipt.get('bp').id === OB.MobileApp.model.get('terminal').businessPartner && !OB.MobileApp.model.get('terminal').layaway_anonymouscustomer) {
          avoidPayment = true;
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_layawaysOrdersWithAnonimousCust'));
          return;
        }
      });
    }

    var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes("doneButton");

    if (!avoidPayment) {
      if (isMultiOrder) {
        payments = this.owner.model.get('multiOrders').get('payments');
      } else {
        payments = this.owner.receipt.get('payments');
      }

      var errorMsgLbl, totalPaid = 0,
          totalToPaid = OB.DEC.abs(isMultiOrder ? this.owner.model.get('multiOrders').getTotal() : this.owner.receipt.getTotal()),
          isReturnOrder = isMultiOrder ? false : this.owner.receipt.getPaymentStatus().isNegative;

      if (_.filter(payments.models, function (payment) {
        return (OB.UTIL.isNullOrUndefined(payment.get('isReturnOrder')) ? isReturnOrder : payment.get('isReturnOrder')) !== isReturnOrder;
      }).length > 0) {
        me.avoidCompleteReceipt = true;
        if (isReturnOrder) {
          errorMsgLbl = 'OBPOS_PaymentOnReturnReceipt';
        } else {
          errorMsgLbl = 'OBPOS_NegativePaymentOnReceipt';
        }
      }

      payments.each(function (payment) {
        if (me.alreadyPaid) {
          me.avoidCompleteReceipt = true;
          errorMsgLbl = 'OBPOS_UnnecessaryPaymentAdded';
          return false;
        }
        if (!payment.get('isReversePayment') && !payment.get('isReversed') && !payment.get('isPrePayment')) {
          totalPaid = OB.DEC.add(totalPaid, payment.get('origAmount'));
          if (totalPaid >= totalToPaid) {
            me.alreadyPaid = true;
          }
        }
      });
      if (this.avoidCompleteReceipt) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel(errorMsgLbl));
        OB.UTIL.SynchronizationHelper.finished(synchId, "doneButton");
        return;
      }

      payments.each(function (payment) {
        if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
          me.allowOpenDrawer = true;
        }
      });

      if (!isMultiOrder) {
        if (this.drawerpreference && this.allowOpenDrawer) {
          if (this.drawerOpened) {
            if (this.owner.receipt.get('orderType') === 3 && !this.owner.receipt.get('cancelLayaway')) {
              //Void Layaway
              this.owner.receipt.trigger('voidLayaway');
            } else if (this.owner.receipt.get('orderType') === 3) {
              //Cancel Layaway
              this.owner.receipt.trigger('cancelLayaway');
            } else {
              this.setDisabled(true);
              enyo.$.scrim.show();
              me.owner.model.get('order').trigger('paymentDone', false);
            }
            this.drawerOpened = false;
            this.setDisabled(true);
          } else {
            OB.POS.hwserver.openDrawer({
              openFirst: true,
              receipt: me.owner.receipt
            }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
            this.drawerOpened = true;
            this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
          }
        } else {
          if (this.owner.receipt.get('orderType') === 3 && !this.owner.receipt.get('cancelLayaway')) {
            //Void Layaway
            enyo.$.scrim.show();
            this.owner.receipt.trigger('voidLayaway');
          } else if (this.owner.receipt.get('orderType') === 3) {
            //Cancel Layaway
            this.owner.receipt.trigger('cancelLayaway');
          } else {
            this.setDisabled(true);
            enyo.$.scrim.show();
            me.owner.receipt.trigger('paymentDone', this.allowOpenDrawer);
          }
        }
      } else {
        if (this.drawerpreference && this.allowOpenDrawer) {
          if (this.drawerOpened) {
            enyo.$.scrim.show();
            this.owner.model.get('multiOrders').trigger('paymentDone', false);
            this.owner.model.get('multiOrders').set('openDrawer', false);
            this.drawerOpened = false;
            this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
          } else {
            OB.POS.hwserver.openDrawer({
              openFirst: true,
              receipt: me.owner.model.get('multiOrders')
            }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
            this.drawerOpened = true;
            this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
          }
        } else {
          enyo.$.scrim.show();
          this.owner.model.get('multiOrders').trigger('paymentDone', this.allowOpenDrawer);
          this.owner.model.get('multiOrders').set('openDrawer', false);
        }
      }
    }
    OB.UTIL.SynchronizationHelper.finished(synchId, "doneButton");
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ExactButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  events: {
    onExactPayment: ''
  },
  classes: 'btn-icon-adaptative btn-icon-check btnlink-green',
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doExactPayment();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine',
  classes: 'btnselect',
  components: [{
    style: 'color:white;',
    components: [{
      style: 'float: left; width: 85%;',
      components: [{
        components: [{
          name: 'name',
          style: 'float: left; width: 65%; padding: 5px 0px 0px 0px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'
        }, {
          name: 'amount',
          style: 'float: left; width: 35%; padding: 5px 0px 0px 0px; text-align: right;'
        }, {
          style: 'clear: both;'
        }]
      }, {
        components: [{
          name: 'info',
          style: 'float: left; width: 65%; padding: 5px 0px 0px 0px; font-size: smaller; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'
        }, {
          name: 'foreignAmount',
          style: 'float: left; width: 35%; padding: 5px 0px 0px 0px; font-size: smaller; text-align: right;'
        }, {
          style: 'clear: both;'
        }]
      }]
    }, {
      style: 'float: left; width: 15%; text-align: right;',
      components: [{
        name: 'removePayment',
        kind: 'OB.OBPOSPointOfSale.UI.RemovePayment',
        style: 'width: 75%; max-width: 50px; height: 25px; margin-left: 10%;'
      }]
    }, {
      style: 'float: left; width: 15%; text-align: right;',
      components: [{
        name: 'reversePayment',
        kind: 'OB.OBPOSPointOfSale.UI.ReversePayment',
        style: 'width: 75%; max-width: 50px; height: 25px; margin-left: 10%;'
      }]
    }, {
      style: 'clear: both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.addStyles('min-height: 29px;');
    if (this.model.get('reversedPaymentId')) {
      this.$.name.setContent((OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name')) + OB.I18N.getLabel('OBPOS_ReversedPayment'));
      this.$.amount.setContent(this.model.printAmount());
    } else if (this.model.get('isReversed')) {
      this.$.name.setContent('*' + (OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name')));
      this.$.amount.setContent(this.model.printAmount());
    } else {
      if (this.model.get('isPrePayment') && !this.model.get('paymentAmount')) {
        this.$.name.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
      } else {
        this.$.name.setContent(OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name'));
      }
      var receipt = this.owner.owner.owner.owner.model.get('order');
      this.$.amount.setContent(this.model.printAmountWithSignum(receipt));
    }
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignAmount.setContent(this.model.printForeignAmount());
    } else {
      this.$.foreignAmount.setContent('');
    }
    if (this.model.get('description')) {
      this.$.info.setContent(this.model.get('description'));
    } else {
      if (this.model.get('paymentData')) {
        //legacy
        if (this.model.get('paymentData').Name) {
          this.model.get('paymentData').name = this.model.get('paymentData').Name;
        }
        //end legacy
        this.$.info.setContent(this.model.get('paymentData').name);
      } else {
        this.$.info.setContent('');
      }
    }
    if (this.$.foreignAmount.content || this.$.info.content) {
      this.$.removePayment.style = this.$.removePayment.style + ' margin-top: 10px;';
    }
    if (this.model.get('isReversed') || (this.model.get('isPrePayment') && (this.model.get('reversedPaymentId') || !this.model.get('paymentAmount') || OB.MobileApp.model.receipt.get('doCancelAndReplace')))) {
      this.$.removePayment.hide();
      this.$.reversePayment.hide();
    } else if (this.model.get('isPrePayment') && OB.MobileApp.model.hasPermission('OBPOS_EnableReversePayments', true)) {
      this.$.removePayment.hide();
      this.$.reversePayment.show();
    } else if (this.model.get('isPrePayment') && !OB.MobileApp.model.hasPermission('OBPOS_EnableReversePayments', true)) {
      this.$.removePayment.hide();
      this.$.reversePayment.hide();
    } else {
      this.$.removePayment.show();
      this.$.reversePayment.hide();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RemovePayment',
  events: {
    onRemovePayment: ''
  },
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-darkgray btnlink-payment-clear btn-icon-small btn-icon-clearPayment',
  tap: function () {
    var me = this;
    if ((_.isUndefined(this.deleting) || this.deleting === false)) {
      this.deleting = true;
      this.removeClass('btn-icon-clearPayment');
      this.addClass('btn-icon-loading');
      this.bubble('onMaxLimitAmountError', {
        show: false,
        maxLimitAmount: 0,
        currency: '',
        symbolAtRight: true
      });
      this.doRemovePayment({
        payment: this.owner.model,
        removeCallback: function () {
          me.deleting = false;
          me.removeClass('btn-icon-loading');
          me.addClass('btn-icon-clearPayment');
        }
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ReversePayment',
  events: {
    onReversePayment: ''
  },
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-darkgray btnlink-payment-clear btn-icon-small btn-icon-reversePayment',
  tap: function () {
    var me = this;

    OB.UTIL.showConfirmation.display(
    OB.I18N.getLabel('OBPOS_LblReverse'), OB.I18N.getLabel('OBPOS_ReverseConfirm'), [{
      label: OB.I18N.getLabel('OBPOS_LblOk'),
      action: function (popup) {
        if ((_.isUndefined(me.deleting) || me.deleting === false)) {
          me.deleting = true;
          me.removeClass('btn-icon-reversePayment');
          me.addClass('btn-icon-loading');
          me.bubble('onMaxLimitAmountError', {
            show: false,
            maxLimitAmount: 0,
            currency: '',
            symbolAtRight: true
          });
          me.bubble('onClearPaymentSelect');
          popup.doHideThisPopup({
            args: {
              actionExecuted: true
            }
          });
          me.doReversePayment({
            payment: me.owner.model,
            sender: me,
            reverseCallback: function () {
              me.deleting = false;
              me.removeClass('btn-icon-loading');
              me.addClass('btn-icon-reversePayment');
            }
          });
        }
      }
    }, {
      label: OB.I18N.getLabel('OBMOBC_LblCancel')
    }]);


  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.CreditButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  i18nLabel: 'OBPOS_LblCreditSales',
  classes: 'btn-icon-small btnlink-green',
  permission: 'OBPOS_receipt.creditsales',
  events: {
    onShowPopup: ''
  },
  init: function (model) {
    this.model = model;
  },
  disabled: false,
  putDisabled: function (status) {
    if (status === false) {
      this.setDisabled(false);
      this.removeClass('disabled');
      this.disabled = false;
    } else {
      this.setDisabled(true);
      this.addClass('disabled');
      this.disabled = true;
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.putDisabled(!OB.MobileApp.model.hasPermission(this.permission));
  },
  tap: function () {
    if (this.disabled) {
      return true;
    }
    if (!_.isNull(this.model.get('order').get('bp')) && _.isNull(this.model.get('order').get('bp').get('locId'))) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_EmptyAddrBillToText'), [{
        label: OB.I18N.getLabel('OBPOS_LblOk')
      }]);
      return true;
    }
    this.putDisabled(true);
    var me = this,
        paymentstatus = this.model.get('order').getPaymentStatus(),
        process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckBusinessPartnerCredit');
    if (!paymentstatus.isReturn) {
      //this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes("creditButtonTap");
      process.exec({
        businessPartnerId: this.model.get('order').get('bp').get('id'),
        totalPending: this.model.get('order').getPending()
      }, function (data) {
        OB.UTIL.SynchronizationHelper.finished(synchId, "creditButtonTap");
        if (data) {
          if (data.enoughCredit) {
            me.doShowPopup({
              popup: 'modalEnoughCredit',
              args: {
                order: me.model.get('order')
              }
            });
            //this.setContent(OB.I18N.getLabel('OBPOS_LblCreditSales'));
          } else {
            var bpName = data.bpName;
            var actualCredit = data.actualCredit;
            me.doShowPopup({
              popup: 'modalNotEnoughCredit',
              args: {
                bpName: bpName,
                actualCredit: actualCredit
              }
            });
            //this.setContent(OB.I18N.getLabel('OBPOS_LblCreditSales'));
            //OB.UI.UTILS.domIdEnyoReference['modalNotEnoughCredit'].$.bodyContent.children[0].setContent();
          }
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorCreditSales'));
        }
        me.putDisabled(false);
      }, function () {
        OB.UTIL.SynchronizationHelper.finished(synchId, "creditButtonTap");
        me.doShowPopup({
          popup: 'modalEnoughCredit',
          args: {
            order: me.model.get('order'),
            message: 'OBPOS_Unabletocheckcredit'
          }
        });
      });
      //    } else if (this.model.get('order').get('orderType') === 1) {
    } else if (paymentstatus.isReturn) {
      var actualCredit;
      var creditLimit = this.model.get('order').get('bp').get('creditLimit');
      var creditUsed = this.model.get('order').get('bp').get('creditUsed');
      var totalPending = this.model.get('order').getPending();
      this.doShowPopup({
        popup: 'modalEnoughCredit',
        args: {
          order: this.model.get('order')
        }
      });
    }
  }
});
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LayawayButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  content: '',
  classes: 'btn-icon-small btnlink-green',
  permission: 'OBPOS_receipt.layawayReceipt',
  init: function (model) {
    this.model = model;
    this.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
  },
  updateVisibility: function (isVisible) {
    if (!OB.MobileApp.model.hasPermission(this.permission)) {
      this.hide();
      return;
    }
    if (!isVisible) {
      this.hide();
      return;
    }
    this.show();
  },
  setDisabled: function (value) {
    this.disabled = value;
    this.setAttribute('disabled', value);
  },
  tap: function () {
    var receipt = this.owner.receipt,
        negativeLines, me = this,
        myModel = this.owner.model,
        payments;
    if (!_.isNull(this.model.get('order').get('bp')) && _.isNull(myModel.get('order').get('bp').get('locId'))) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_EmptyAddrBillToText'), [{
        label: OB.I18N.getLabel('OBPOS_LblOk')
      }]);
      return;
    }

    this.allowOpenDrawer = false;

    if (receipt.get('bp').id === OB.MobileApp.model.get('terminal').businessPartner && !OB.MobileApp.model.get('terminal').layaway_anonymouscustomer) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_layawaysOrdersWithAnonimousCust'));
      return;
    }

    if (!this.showing || this.disabled) {
      return true;
    }

    if (myModel.get('leftColumnViewManager').isOrder()) {
      payments = this.owner.receipt.get('payments');
    } else {
      payments = this.owner.model.get('multiOrders').get('payments');
    }

    payments.each(function (payment) {
      if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
        me.allowOpenDrawer = true;
      }
    });
    if (receipt) {
      negativeLines = _.find(receipt.get('lines').models, function (line) {
        return line.get('qty') < 0;
      });
      if (negativeLines) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed'));
        return true;
      }
      if (receipt.get('generateInvoice')) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_noInvoiceIfLayaway'));
        receipt.set('generateInvoice', false);
      }
    }
    this.setDisabled(true);
    enyo.$.scrim.show();
    receipt.trigger('paymentDone', me.allowOpenDrawer);
  }
});