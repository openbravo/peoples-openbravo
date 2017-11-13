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
  setPrepaymentTotalPending: function (pending, mulrate, symbol, currencySymbolAtTheRight) {
    this.$.prepaymenttotalpending.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(pending, mulrate), symbol, currencySymbolAtTheRight));
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
        this.setPrepaymentTotalPending(this.model.getPrepaymentAmount() + pending - this.model.getTotal(), payment.mulrate, payment.symbol, payment.currencySymbolAtTheRight, inSender, inEvent);
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
          style: 'padding: 0px 0px 0px 10px; height: 17px;',
          name: 'prepaymentLine',
          components: [{
            tag: 'span',
            name: 'prepaymenttotalpending',
            style: 'font-size: 20px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'prepaymenttotalpendinglbl'
          }, {
            tag: 'span',
            name: 'prepaymentexactlbl'
          }]
        }, {
          style: 'padding: 5px 0px 0px 10px; height: 17px;',
          name: 'paymentLine',
          components: [{
            tag: 'span',
            name: 'totalpending',
            style: 'font-weight: bold;'
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
          name: 'prepaymentsbuttons',
          style: 'width: 85%; max-width: 125px; float: right; margin: 5px 5px 10px 0px; clear: right; font-weight: normal; padding: 0px',
          components: [{
            name: 'prepaymentsexactbutton',
            kind: 'OB.OBPOSPointOfSale.UI.PrepaymentsExactButton',
            components: [{
              name: 'prepaymentsexactbuttonicon',
              classes: 'btn-icon-doubleCheck',
              style: 'display: block'
            }, {
              name: 'prepaymentsexactbuttonlbl',
              style: 'font-size: 10px; font-weight: bold; color: black; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; position: relative;'
            }]
          }, {
            name: 'prepaymentsdeliverybutton',
            kind: 'OB.OBPOSPointOfSale.UI.PrepaymentsDeliveryButton',
            components: [{
              name: 'prepaymentsdeliverybuttonicon',
              classes: 'btn-icon-check',
              style: 'display: block'
            }, {
              name: 'prepaymentsdeliverybuttonlbl',
              style: 'font-size: 10px; font-weight: bold; color: black; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; position: relative;'
            }]
          }]
        }, {
          name: 'exactbutton',
          kind: 'OB.OBPOSPointOfSale.UI.ExactButton'
        }, {
          name: 'donebutton',
          kind: 'OB.OBPOSPointOfSale.UI.DoneButton'
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
    this.receipt.on('change:payment change:change calculategross change:bp change:gross change:prepaymentLimitAmt', function () {
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
      this.receipt.unset('paymentDone');
      OB.UTIL.showLoading(false);
    }, this);
    this.model.get('multiOrders').on('paymentCancel', function () {
      if (OB.MobileApp.view.openedPopup === null) {
        enyo.$.scrim.hide();
      }
      this.model.get('multiOrders').unset('paymentDone');
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
      this.updateLayawayAction();
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

  updateLayawayAction: function (forceDisable) {
    var disable = forceDisable || !(this.model.get('leftColumnViewManager').isMultiOrder() ? true : this.receipt.isReversedPaid());
    if ((this.receipt.get('orderType') === 2 || (this.receipt.get('isLayaway') && this.receipt.get('orderType') !== 3)) && !this.receipt.getPaymentStatus().done && _.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
      this.$.layawayaction.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
      if (!disable && this.receipt.get('isLayaway') && this.receipt.get('orderType') !== 3) {
        if (!_.find(this.receipt.get('payments').models, function (payment) {
          return !payment.get('isPrePayment');
        })) {
          disable = true;
        }
      }
      this.$.layawayaction.setLocalDisabled(disable);
      this.$.layawayaction.show();
    } else if (this.receipt.get('orderType') === 3 && _.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
      this.$.layawayaction.setLocalDisabled(false);
      this.$.layawayaction.hide();
    } else if (!_.isUndefined(this.receipt.get('paidInNegativeStatusAmt'))) {
      this.$.layawayaction.setLocalDisabled(false);
      this.$.layawayaction.hide();
      this.$.exactbutton.show();
    }
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
    var execution = OB.UTIL.ProcessController.start('updatePending');
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      OB.UTIL.ProcessController.finish('updatePending', execution);
      return true;
    }
    var paymentstatus = this.receipt.getPaymentStatus(),
        prepaymentAmount = this.receipt.get('prepaymentAmt'),
        symbol = '',
        rate = OB.DEC.One,
        symbolAtRight = true,
        isCashType = true,
        receiptHasPrepaymentAmount = prepaymentAmount !== 0 && prepaymentAmount !== paymentstatus.totalAmt,
        pendingPrepayment = OB.DEC.sub(OB.DEC.add(prepaymentAmount, paymentstatus.pendingAmt), paymentstatus.totalAmt);

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

    //Update styles based on the prepayment amount
    if (receiptHasPrepaymentAmount && !paymentstatus.done) {
      this.$.prepaymentLine.show();
      this.$.paymentLine.addRemoveClass('paymentline-w-prepayment', true);
      this.$.paymentLine.addRemoveClass('paymentline-wo-prepayment', false);
      this.$.totalpending.applyStyle('font-size', '18px');
    } else {
      this.$.prepaymentLine.hide();
      this.$.paymentLine.addRemoveClass('paymentline-w-prepayment', false);
      this.$.paymentLine.addRemoveClass('paymentline-wo-prepayment', true);
      this.$.totalpending.applyStyle('font-size', '24px');
    }

    if ((OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && this.receipt.get('prepaymentLimitAmt') < this.receipt.get('gross') && pendingPrepayment > 0 && receiptHasPrepaymentAmount)) {
      this.setPrepaymentTotalPending(pendingPrepayment, rate, symbol, symbolAtRight);
      this.$.prepaymenttotalpending.show();
      this.$.prepaymenttotalpendinglbl.show();
      this.$.prepaymentexactlbl.hide();
      this.$.prepaymentsbuttons.show();
      this.$.exactbutton.hide();
    } else {
      this.$.prepaymenttotalpending.hide();
      this.$.prepaymenttotalpendinglbl.hide();
      this.$.prepaymentexactlbl.show();
      if (!receiptHasPrepaymentAmount) {
        this.$.prepaymentsbuttons.hide();
        this.$.exactbutton.show();
      } else {
        this.$.prepaymentsbuttons.show();
        this.$.exactbutton.hide();
      }
    }

    if (paymentstatus.change) {
      this.$.change.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(this.receipt.getChange(), rate), symbol, symbolAtRight));
      OB.MobileApp.model.set('changeReceipt', OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(this.receipt.getChange(), rate), symbol, symbolAtRight));
      this.$.change.show();
      this.$.changelbl.show();
    } else {
      this.$.change.hide();
      this.$.changelbl.hide();
    }
    if (this.receipt.getPending() <= 0 && paymentstatus.overpayment) {
      this.$.overpayment.setContent(OB.I18N.formatCurrencyWithSymbol(paymentstatus.overpayment, symbol, symbolAtRight));
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (this.receipt.getPending() <= 0 && paymentstatus.done) {
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

      if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && !this.receipt.getPaymentStatus().isNegative) {
        this.$.donebutton.show();
      } else {
        this.$.donebutton.hide();
      }
      if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && pendingPrepayment <= 0) {
        this.$.prepaymentsdeliverybutton.hide();
      } else {
        this.$.prepaymentsdeliverybutton.show();
      }
      if (this.$.donebutton.drawerpreference) {
        this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.donebutton.drawerOpened = false;
      }
    }

    if (this.receipt.getPending() <= 0 && (paymentstatus.done || this.receipt.getGross() === 0)) {
      this.$.exactbutton.hide();
      this.$.prepaymentsbuttons.hide();
      this.$.layawayaction.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        if (!OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
          this.$.exactbutton.show();
        }
      }
      this.updateLayawayAction();
    }
    if (this.receipt.getPending() === 0 && paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
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
    OB.UTIL.ProcessController.finish('updatePending', execution);
  },
  updatePendingMultiOrders: function () {
    var paymentstatus = this.model.get('multiOrders');
    var execution = OB.UTIL.ProcessController.start('updatePendingMultiOrders'),
        multiOrders = this.model.get('multiOrders'),
        symbol = '',
        symbolAtRight = true,
        rate = OB.DEC.One,
        isCashType = true,
        selectedPayment, paymentStatus = multiOrders.getPaymentStatus(),
        prepaymentAmount = paymentstatus.get('prepaymentAmt'),
        receiptHasPrepaymentAmount = prepaymentAmount !== 0 && prepaymentAmount !== OB.DEC.add(paymentstatus.get('total'), paymentstatus.get('existingPayment')),
        pendingPrepayment = OB.DEC.sub(OB.DEC.sub(prepaymentAmount, (paymentstatus.get('existingPayment') ? paymentstatus.get('existingPayment') : 0)), paymentstatus.get('payment'));

    this.updateExtraInfo('');
    this.$.layawayaction.hide();
    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      symbol = OB.MobileApp.model.get('terminal').symbol;
      symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
    }
    if (multiOrders.get('selectedPayment')) {
      selectedPayment = OB.MobileApp.model.paymentnames[multiOrders.get('selectedPayment')];
    } else {
      selectedPayment = OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
    }
    if (!_.isUndefined(selectedPayment)) {
      symbol = selectedPayment.symbol;
      rate = selectedPayment.mulrate;
      symbolAtRight = selectedPayment.currencySymbolAtTheRight;
      isCashType = selectedPayment.paymentMethod.iscash;
    }

    //Update styles based on the prepayment amount
    if (receiptHasPrepaymentAmount && OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) < 0) {
      this.$.prepaymentLine.show();
      this.$.paymentLine.addRemoveClass('paymentline-w-prepayment', true);
      this.$.paymentLine.addRemoveClass('paymentline-wo-prepayment', false);
      this.$.totalpending.applyStyle('font-size', '18px');
    } else {
      this.$.prepaymentLine.hide();
      this.$.paymentLine.addRemoveClass('paymentline-w-prepayment', false);
      this.$.paymentLine.addRemoveClass('paymentline-wo-prepayment', true);
      this.$.totalpending.applyStyle('font-size', '24px');
    }
    if (pendingPrepayment > 0 && pendingPrepayment !== paymentstatus.get('total') - paymentstatus.get('payment')) {
      this.setPrepaymentTotalPending(pendingPrepayment, rate, symbol, symbolAtRight);
      this.$.prepaymenttotalpending.show();
      this.$.prepaymenttotalpendinglbl.show();
      this.$.prepaymentexactlbl.hide();
      this.$.prepaymentsbuttons.show();
      this.$.exactbutton.hide();
    } else {
      this.$.prepaymenttotalpending.hide();
      this.$.prepaymenttotalpendinglbl.hide();
      this.$.prepaymentexactlbl.show();
      if (!receiptHasPrepaymentAmount) {
        this.$.prepaymentsbuttons.hide();
        this.$.exactbutton.show();
      } else {
        this.$.prepaymentsbuttons.show();
        this.$.exactbutton.hide();
      }

    }

    this.checkValidPayments(paymentStatus, selectedPayment);
    if (multiOrders.get('change')) {
      this.$.change.setContent(OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(multiOrders.get('change'), rate), symbol, symbolAtRight));
      OB.MobileApp.model.set('changeReceipt', OB.I18N.formatCurrencyWithSymbol(OB.DEC.mul(multiOrders.get('change'), rate), symbol, symbolAtRight));
      this.$.change.show();
      this.$.changelbl.show();
    } else {
      this.$.change.hide();
      this.$.changelbl.hide();
    }
    //overpayment
    if (paymentStatus.overpayment) {
      this.$.overpayment.setContent(OB.I18N.formatCurrencyWithSymbol(paymentStatus.overpayment, symbol, symbolAtRight));
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (multiOrders.get('multiOrdersList').length > 0 && OB.DEC.compare(multiOrders.get('total')) >= 0 && OB.DEC.compare(OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))) >= 0) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        this.$.donebutton.show();
      }
      this.updateCreditSalesAction();
    } else {
      this.setTotalPending(OB.DEC.sub(multiOrders.get('total'), multiOrders.get('payment')), rate, symbol, symbolAtRight);
      this.$.totalpending.show();
      this.$.totalpendinglbl.show();
      if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && paymentstatus.get('prepaymentLimitAmt') < paymentstatus.get('total') + paymentstatus.get('existingPayment')) {
        this.$.donebutton.show();
      } else {
        this.$.donebutton.hide();
      }
      if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && pendingPrepayment <= 0) {
        this.$.prepaymentsdeliverybutton.hide();
      } else {
        this.$.prepaymentsdeliverybutton.show();
      }
      if (this.$.donebutton.drawerpreference) {
        this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.donebutton.drawerOpened = false;
      }
    }

    this.updateCreditSalesAction();
    this.$.layawayaction.hide();
    if (multiOrders.get('multiOrdersList').length > 0 && OB.DEC.compare(multiOrders.get('total')) >= 0 && (OB.DEC.compare(OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))) >= 0 || multiOrders.get('total') === 0)) {
      this.$.exactbutton.hide();
      this.$.prepaymentsbuttons.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        if (!OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
          OB.debug('updatePendingMultiOrders: show exact button 1');
          this.$.exactbutton.show();
        }
      }
    }
    if (multiOrders.get('multiOrdersList').length > 0 && OB.DEC.compare(multiOrders.get('total')) >= 0 && OB.DEC.compare(OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))) >= 0 && !multiOrders.get('change') && OB.DEC.compare(OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))) <= 0) {
      if (multiOrders.get('total') === 0) {
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
    OB.UTIL.ProcessController.finish('updatePendingMultiOrders', execution);
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

  checkDrawerPreference: function () {
    var hasCashPayment, paymentList = this.model.get('multiOrders').get('payments');
    if (paymentList.length > 0) {
      hasCashPayment = _.find(paymentList.models, function (item) {
        if (item.get('isCash')) {
          return item;
        }
      });
    }
    if (_.isUndefined(hasCashPayment) && this.model.get('multiOrders').selectedPayment !== 'OBPOS_payment.cash') {
      this.$.donebutton.drawerpreference = false;
      this.$.donebutton.drawerOpened = true;
      this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
    }
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
    this.updateAddPaymentAction();
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
              if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
                this.$.prepaymentsdeliverybutton.setLocalDisabled(true);
                this.$.prepaymentsexactbutton.setLocalDisabled(true);
              }
            }
            me.updateAddPaymentAction();
            this.setStatusButtons(lsuccess, 'Done');
            this.checkEnoughCashAvailable(paymentstatus, selectedPayment, this, 'Layaway', function (success) {
              this.setStatusButtons(success, 'Layaway');
            });
            this.checkEnoughCashAvailable(paymentstatus, selectedPayment, this, 'Credit', function (success) {
              this.setStatusButtons(success, 'Credit');
            });
          });
        }
      } else if (!this.getAddPaymentAction()) {
        // Disable the 'Done' button if the synchronized paid amount is higher than the amount to pay and
        // there's no reverse payment, the total amount is not zero (or is zero and is a synchronized ticket)
        // and is not a C&R flow
        var total = OB.DEC.sub(this.model.get('order').getGross(), this.model.get('order').getCredit()),
            disableDoneButton = (!total && !this.model.get('order').get('isPaid')) || paymentstatus.isReversal || this.model.get('order').get('doCancelAndReplace') ? false : this.model.get('order').getPrePaymentQty() >= total;
        this.$.donebutton.setLocalDisabled(disableDoneButton);
        this.$.exactbutton.setLocalDisabled(false);
        if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
          this.$.prepaymentsdeliverybutton.setLocalDisabled(false);
          this.$.prepaymentsexactbutton.setLocalDisabled(false);
        }
        this.$.creditsalesaction.setLocalDisabled(false);
        this.updateLayawayAction();
      }

    } else {
      me.updateAddPaymentAction();
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
  updateAddPaymentAction: function () {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      this.receipt.stopAddingPayments = !_.isEmpty(this.getShowingErrorMessages());
    } else if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.model.get('multiOrders').stopAddingPayments = !_.isEmpty(this.getShowingErrorMessages());
    }
  },
  getAddPaymentAction: function () {
    return this.model.get('leftColumnViewManager').isOrder() ? this.receipt.stopAddingPayments : this.model.get('multiOrders').stopAddingPayments;
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
    // If there's a reverse payment and the reversed amount is not paid disable also the buttons
    var statusOK = this.model.get('leftColumnViewManager').isMultiOrder() ? true : this.receipt.isReversedPaid();
    if (button === 'Done') {
      // If there are no not synchronized payments reversed and the full amount qty is paid by prePayment payments,
      // the button 'Done' will be disabled (except for the case of doing a cancel and replace).
      // If the ticket is synchronized and the gross is zero, is also disabled.
      if (statusOK && ((this.model.get('leftColumnViewManager').isOrder() && (this.receipt.get('isPaid') || this.receipt.get('isLayaway')) && !this.receipt.isNewReversed() && OB.DEC.abs(this.receipt.getPrePaymentQty()) >= OB.DEC.abs(this.receipt.getTotal()) && !this.receipt.get('doCancelAndReplace') && this.receipt.get('orderType') !== 3) || (this.receipt.get('isPaid') && this.receipt.getGross() === 0))) {
        statusOK = false;
      }
      if (resultOK) {
        var disableButton = !statusOK;
        this.$.donebutton.setLocalDisabled(disableButton);
        this.$.exactbutton.setLocalDisabled(false);
        if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
          this.$.prepaymentsdeliverybutton.setLocalDisabled(false);
          this.$.prepaymentsexactbutton.setLocalDisabled(false);
        }
      } else {
        if (this.$.changeexceedlimit.showing || this.$.overpaymentnotavailable.showing || this.$.overpaymentexceedlimit.showing || this.$.onlycashpaymentmethod.showing) {
          this.$.noenoughchangelbl.hide();
        } else {
          this.$.noenoughchangelbl.show();
        }
        this.$.donebutton.setLocalDisabled(true);
        this.$.exactbutton.setLocalDisabled(true);
        if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
          this.$.prepaymentsdeliverybutton.setLocalDisabled(true);
          this.$.prepaymentsexactbutton.setLocalDisabled(true);
        }
      }
    } else if (button === 'Layaway') {
      this.updateLayawayAction(resultOK ? false : true);
    } else if (button === 'Credit') {
      if (resultOK && statusOK) {
        this.$.creditsalesaction.setLocalDisabled(false);
      } else {
        // If the ticket is a negative ticket, even when there's not enough cash, it must be possible to click on the 'Use Credit' button
        if (this.receipt.getPaymentStatus().isNegative) {
          this.$.creditsalesaction.setLocalDisabled(false);
        } else {
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
    this.$.prepaymenttotalpendinglbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsRemainingDelivery'));
    this.$.prepaymentexactlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsExactDelivery'));
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
      this.$.prepaymentsbuttons.hide();
    }
    this.model.get('multiOrders').get('multiOrdersList').on('all', function (event) {
      if (this.model.isValidMultiOrderState()) {
        this.updatePendingMultiOrders();
      }
    }, this);

    this.model.get('multiOrders').on('change:payment change:total change:change change:prepaymentAmt paymentCancel', function () {
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
  drawerOpened: true,
  isLocked: true,
  lasDisabledPetition: true,
  processesToListen: ['showPaymentTab', 'updatePending', 'updatePendingMultiOrders', 'cancelLayaway'],
  disableButton: function () {
    this.isLocked = true;
    this.setDisabledIfSynchronized();
  },
  enableButton: function () {
    this.isLocked = false;
    this.setDisabledIfSynchronized();
  },
  setDisabled: function (value) {
    this.lasDisabledPetition = value;
    if (value) {
      this.disableButton();
    } else {
      this.enableButton();
    }
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
    if (!OB.MobileApp.model.get('terminal').returns_anonymouscustomer) {
      if (isMultiOrder) {
        var orderList = myModel.get('multiOrders').get('multiOrdersList');
        orderList.forEach(function (receipt) {
          if (receipt.isAnonymousBlindReturn()) {
            avoidPayment = true;
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust'));
            return;
          }
        });
      } else {
        if (this.owner.receipt.isAnonymousBlindReturn()) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust'));
          return;
        }
      }
    }

    enyo.$.scrim.show();

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

    var continueExecution = function () {
        myModel.get('order').set('donePressed', true);

        if (myModel.get('leftColumnViewManager').isOrder()) {
          me.owner.receipt.get('lines').forEach(function (line) {
            if (line.get('obposCanbedelivered')) {
              line.set('obposIspaid', true);
            }
          });
          if (Math.abs(me.owner.receipt.get('payment')) < Math.abs(me.owner.receipt.get('gross')) && !me.owner.receipt.get('paidOnCredit')) {
            me.owner.receipt.set('hasPrepayment', true);
          }
        }

        if (!avoidPayment) {
          if (isMultiOrder) {
            payments = me.owner.model.get('multiOrders').get('payments');
          } else {
            payments = me.owner.receipt.get('payments');
          }

          var errorMsgLbl, totalPaid = 0,
              totalToPaid = OB.DEC.abs(isMultiOrder ? me.owner.model.get('multiOrders').getTotal() : me.owner.receipt.getTotal()),
              isReturnOrder = isMultiOrder ? false : me.owner.receipt.getPaymentStatus().isNegative;

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
          if (me.avoidCompleteReceipt) {
            enyo.$.scrim.hide();
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel(errorMsgLbl));
            return;
          }

          payments.each(function (payment) {
            if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
              me.allowOpenDrawer = true;
            }
          });

          if (!isMultiOrder) {
            if (me.drawerpreference && me.allowOpenDrawer) {
              if (me.drawerOpened) {
                if (me.owner.receipt.get('orderType') === 3 && !me.owner.receipt.get('cancelLayaway')) {
                  //Void Layaway
                  me.owner.receipt.trigger('voidLayaway');
                } else if (me.owner.receipt.get('orderType') === 3) {
                  //Cancel Layaway
                  me.owner.receipt.trigger('cancelLayaway');
                } else {
                  me.setDisabled(true);
                  me.owner.model.get('order').trigger('paymentDone', false);
                  OB.UTIL.setScanningFocus(false);
                }
                me.drawerOpened = false;
                me.setDisabled(true);
              } else {
                OB.POS.hwserver.openDrawer({
                  openFirst: true,
                  receipt: me.owner.receipt
                }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
                me.drawerOpened = true;
                me.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
                enyo.$.scrim.hide();
              }
            } else {
              if (me.owner.receipt.get('orderType') === 3 && !me.owner.receipt.get('cancelLayaway')) {
                //Void Layaway
                me.owner.receipt.trigger('voidLayaway');
              } else if (me.owner.receipt.get('orderType') === 3) {
                //Cancel Layaway
                me.owner.receipt.trigger('cancelLayaway');
              } else {
                me.setDisabled(true);
                me.owner.receipt.trigger('paymentDone', me.allowOpenDrawer);
                OB.UTIL.setScanningFocus(false);
              }
            }
          } else {
            if (me.drawerpreference && me.allowOpenDrawer) {
              if (me.drawerOpened) {
                me.owner.model.get('multiOrders').trigger('paymentDone', false);
                OB.UTIL.setScanningFocus(false);
                me.owner.model.get('multiOrders').set('openDrawer', false);
                me.drawerOpened = false;
                me.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
              } else {
                OB.POS.hwserver.openDrawer({
                  openFirst: true,
                  receipt: me.owner.model.get('multiOrders')
                }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
                me.drawerOpened = true;
                me.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
                enyo.$.scrim.hide();
              }
            } else {
              me.owner.model.get('multiOrders').trigger('paymentDone', me.allowOpenDrawer);
              OB.UTIL.setScanningFocus(false);
              me.owner.model.get('multiOrders').set('openDrawer', false);
            }
          }
        }
        },
        paymentStatus, prepaymentLimitAmount, pendingPrepayment, receiptHasPrepaymentAmount = true;

    if (myModel.get('leftColumnViewManager').isOrder()) {
      this.owner.receipt.getPrepaymentAmount();
      paymentStatus = this.owner.receipt.getPaymentStatus();
      prepaymentLimitAmount = this.owner.receipt.get('prepaymentLimitAmt');
      receiptHasPrepaymentAmount = this.owner.receipt.get('orderType') !== 1 && this.owner.receipt.get('orderType') !== 3;
      pendingPrepayment = OB.DEC.sub(OB.DEC.add(prepaymentLimitAmount, paymentStatus.pendingAmt), paymentStatus.totalAmt);
    } else {
      paymentStatus = this.owner.model.get('multiOrders').getPaymentStatus();
      prepaymentLimitAmount = this.owner.model.get('multiOrders').get('prepaymentLimitAmt');
      pendingPrepayment = OB.DEC.sub(OB.DEC.add(prepaymentLimitAmount, paymentStatus.pendingAmt), OB.DEC.add(paymentStatus.totalAmt, this.owner.model.get('multiOrders').get('existingPayment')));
    }
    receiptHasPrepaymentAmount = receiptHasPrepaymentAmount && prepaymentLimitAmount !== 0;
    if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments && receiptHasPrepaymentAmount && pendingPrepayment > 0) {
      if (OB.MobileApp.model.hasPermission('OBPOS_AllowPrepaymentUnderLimit', true)) {
        var approval = _.find(me.owner.receipt.get('approvals'), function (approval) {
          return approval.approvalType && approval.approvalType.approval === 'OBPOS_approval.prepaymentUnderLimit';
        });
        if (!approval) {
          OB.UTIL.Approval.requestApproval(
          me.model, [{
            approval: 'OBPOS_approval.prepaymentUnderLimit',
            message: 'OBPOS_approval.prepaymentUnderLimit'
          }], function (approved, supervisor, approvalType) {
            if (approved) {
              continueExecution();
            }
          });
        } else {
          continueExecution();
        }
      } else {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_PrepaymentUnderLimit_NotAllowed', [prepaymentLimitAmount]));
      }
    } else {
      continueExecution();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PrepaymentsDeliveryButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  events: {
    onDeliveryPayment: ''
  },
  classes: 'btn-icon-adaptative btnlink-green',
  style: 'width: calc(50% - 5px); margin: 0px 5px 0px 0px; clear: unset',
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doDeliveryPayment();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.children[1].setContent(OB.I18N.getLabel('OBPOS_PrepaymentsDeliveryButtonLbl'));
    if (!OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
      this.hide();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PrepaymentsExactButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  events: {
    onExactPayment: ''
  },
  classes: 'btn-icon-adaptative btnlink-green',
  style: 'width: calc(50% - 5px); margin: 0px 0px 0px 5px;',
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doExactPayment();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.children[1].setContent(OB.I18N.getLabel('OBPOS_PrepaymentsExactButtonLbl'));
    if (!OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
      this.hide();
    }
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
    this.owner.checkDrawerPreference();
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
  i18nLabel: 'OBPOS_LblSellOnCredit',
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
    // Checking blind returned lines
    if (!OB.MobileApp.model.get('terminal').returns_anonymouscustomer) {
      if (this.model.get('leftColumnViewManager').isOrder()) {
        if (this.owner.receipt.isAnonymousBlindReturn()) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust'));
          return;
        }
      } else {
        var orderList = this.model.get('multiOrders').get('multiOrdersList');
        orderList.forEach(function (receipt) {
          if (receipt.isAnonymousBlindReturn()) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust'));
            return;
          }
        });
      }
    }
    this.putDisabled(true);
    var me = this,
        paymentstatus = this.model.get('order').getPaymentStatus(),
        process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckBusinessPartnerCredit');
    if (!paymentstatus.isReturn) {
      //this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
      process.exec({
        businessPartnerId: this.model.get('order').get('bp').get('id'),
        totalPending: this.model.get('order').getPending()
      }, function (data) {
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
        if (OB.MobileApp.model.hasPermission('OBPOS_AllowSellOnCreditWhileOffline', true)) {
          me.doShowPopup({
            popup: 'modalEnoughCredit',
            args: {
              order: me.model.get('order'),
              message: 'OBPOS_Unabletocheckcredit'
            }
          });
        } else {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_SellingOnCreditHeader'), OB.I18N.getLabel('OBPOS_UnabletoSellOncredit'), [{
            isConfirmButton: true,
            label: OB.I18N.getLabel('OBMOBC_LblOk')
          }]);
        }
        me.putDisabled(false);
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
        if (!OB.MobileApp.model.hasPermission('OBPOS_AllowLayawaysNegativeLines', true)) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed'));
          return true;
        } else if (receipt.get('payment') > 0) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_partiallyLayawaysWithNegLinesNotAllowed'));
          return true;
        }
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