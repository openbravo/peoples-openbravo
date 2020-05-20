/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Change',
  tag: 'span',
  classes: 'obObposPointOfSaleUiChange',
  setContent: function() {
    var size, length;

    this.inherited(arguments);

    length = this.content.length;
    if (length < 21) {
      size = 24;
    } else if (length < 39) {
      // (20, 24); (38, 14)
      size = Math.trunc(-0.55555 * length + 35.11111);
    } else {
      size = 14;
    }
    // This inline style is allowed
    this.setStyle('font-size: ' + size + 'px;');
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Payment',
  classes: 'obObposPointOfSaleUiPayment',
  published: {
    receipt: null
  },
  events: {
    onShowPopup: '',
    onPaymentActionPay: '',
    onRenderPaymentLine: ''
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
  getSelectedPayment: function() {
    if (this.receipt && this.receipt.get('selectedPayment')) {
      return this.receipt.get('selectedPayment');
    }
    return null;
  },
  setTotalPending: function(pending, symbol, currencySymbolAtTheRight) {
    this.$.totalpending.setContent(
      OB.I18N.formatCurrencyWithSymbol(
        pending,
        symbol,
        currencySymbolAtTheRight
      )
    );
  },
  setPrepaymentTotalPending: function(
    pending,
    symbol,
    currencySymbolAtTheRight
  ) {
    this.$.prepaymenttotalpending.setContent(
      OB.I18N.formatCurrencyWithSymbol(
        pending,
        symbol,
        currencySymbolAtTheRight
      )
    );
  },
  clearPaymentMethodSelect: function(inSender, inEvent) {
    this.$.paymentMethodSelect.setContent('');
    this.$.paymentMethodSelect.hide();
  },
  buttonStatusChanged: function(inSender, inEvent) {
    this.clearPaymentMethodSelect(inSender, inEvent);
    if (
      inEvent.value.status &&
      inEvent.value.status.indexOf('paymentMethodCategory.showitems.') === 0
    ) {
      this.doShowPopup({
        popup: 'modalPaymentsSelect',
        args: {
          idCategory: inEvent.value.status.substring(
            inEvent.value.status.lastIndexOf('.') + 1
          )
        }
      });
    } else {
      var payment, change, pending, isMultiOrders, paymentstatus, hasChange;
      payment =
        inEvent.value.payment ||
        OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
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
      if (
        inEvent.value.status === '' &&
        !inEvent.value.keyboard.hasActivePayment
      ) {
        this.$.exactbutton.hide();
      }
      isMultiOrders = this.model.isValidMultiOrderState();
      change = this.model.getChange();
      pending = this.model.getPending();
      if (!isMultiOrders) {
        if (!_.isNull(this.receipt)) {
          this.receipt.set('selectedPayment', payment.payment.searchKey);
          paymentstatus = this.receipt.getPaymentStatus();
          hasChange = OB.DEC.compare(this.receipt.getChange()) > 0;
        }
      } else {
        this.model
          .get('multiOrders')
          .set('selectedPayment', payment.payment.searchKey);
        paymentstatus = this.model.get('multiOrders').getPaymentStatus();
        hasChange =
          OB.DEC.compare(this.model.get('multiOrders').getChange()) > 0;
      }
      if (
        !_.isNull(change) &&
        change &&
        !(
          _.isUndefined(inEvent.value.payment) &&
          !_.isUndefined(OB.MobileApp.model.receipt.get('changePayments'))
        )
      ) {
        this.calculateChange(payment, change);
      } else if (!_.isNull(pending) && pending) {
        this.calculateChangeReset();
        this.setTotalPending(
          OB.DEC.mul(pending, payment.mulrate, payment.obposPosprecision),
          payment.symbol,
          payment.currencySymbolAtTheRight
        );
        this.setPrepaymentTotalPending(
          OB.DEC.mul(
            OB.DEC.sub(
              OB.DEC.add(this.model.getPrepaymentAmount(), pending),
              this.model.getTotal()
            ),
            payment.mulrate,
            payment.obposPosprecision
          ),
          payment.symbol,
          payment.currencySymbolAtTheRight
        );
      }
      if (
        paymentstatus &&
        inEvent.value.status !== '' &&
        !this.receipt.isCalculateReceiptLocked &&
        !this.receipt.isCalculateGrossLocked
      ) {
        this.checkValidPayments(paymentstatus, payment, hasChange);
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
  paymentChanged: function(inSender, inEvent) {
    if (!inEvent.amount && inEvent.payment) {
      this.$.paymentMethodSelect.setContent(
        OB.I18N.getLabel('OBPOS_PaymentsSelectedMethod', [
          inEvent.payment.payment._identifier
        ])
      );
      this.$.paymentMethodSelect.show();
    }
  },
  maxLimitAmountError: function(inSender, inEvent) {
    if (inEvent.show) {
      this.$.errorMaxlimitamount.setContent(
        OB.I18N.getLabel('OBPOS_PaymentMaxLimitAmount', [
          OB.I18N.formatCurrencyWithSymbol(
            inEvent.maxLimitAmount,
            inEvent.currency,
            inEvent.symbolAtRight
          )
        ])
      );
      this.$.errorMaxlimitamount.show();
    } else {
      this.$.errorMaxlimitamount.setContent('');
      this.$.errorMaxlimitamount.hide();
    }
    this.alignErrorMessages();
  },
  components: [
    {
      classes: 'obObposPointOfSaleUiPayment-container1',
      components: [
        {
          classes: 'obObposPointOfSaleUiPayment-container1-container1',
          components: [
            {
              classes:
                'obObposPointOfSaleUiPayment-container1-container1-container1'
            }
          ]
        },
        {
          classes: 'obObposPointOfSaleUiPayment-container1-container2',
          components: [
            {
              classes:
                'obObposPointOfSaleUiPayment-container1-container2-container1',
              components: [
                {
                  classes:
                    'obObposPointOfSaleUiPayment-container1-container2-container1-container1',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container1-container1-prepaymentLine',
                      name: 'prepaymentLine',
                      components: [
                        {
                          tag: 'span',
                          name: 'prepaymenttotalpending',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending'
                        },
                        {
                          tag: 'span',
                          name: 'prepaymenttotalpendinglbl',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpendinglbl'
                        },
                        {
                          tag: 'span',
                          name: 'prepaymentexactlbl',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymentexactlbl'
                        },
                        {
                          tag: 'span',
                          name: 'deliverychangelbl',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentLine-deliverychangelbl'
                        }
                      ]
                    },
                    {
                      name: 'paymentLine',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container1-container1-paymentLine',
                      components: [
                        {
                          tag: 'span',
                          name: 'totalpending',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-totalpending'
                        },
                        {
                          tag: 'span',
                          name: 'totalpendinglbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-totalpendinglbl'
                        },
                        {
                          tag: 'span',
                          name: 'remainingfortotallbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-remainingfortotallbl'
                        },
                        {
                          kind: 'OB.UI.RegularButton',
                          name: 'changebutton',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-changebutton',
                          ontap: 'actionChangeButton'
                        },
                        {
                          kind: 'OB.OBPOSPointOfSale.UI.Change',
                          name: 'change',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-change'
                        },
                        {
                          tag: 'span',
                          name: 'changelbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-changelbl'
                        },
                        {
                          tag: 'span',
                          name: 'overpayment',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-overpayment'
                        },
                        {
                          tag: 'span',
                          name: 'overpaymentlbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-overpaymentlbl'
                        },
                        {
                          tag: 'span',
                          name: 'exactlbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-exactlbl'
                        },
                        {
                          tag: 'span',
                          name: 'donezerolbl',
                          classes:
                            'obObposPointOfSaleUiPayment-paymentLine-donezerolbl'
                        }
                      ]
                    }
                  ]
                },
                {
                  classes:
                    'obObposPointOfSaleUiPayment-container1-container2-container1-container2',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container1-container2-container1',
                      components: [
                        {
                          classes:
                            'obObposPointOfSaleUiPayment-container1-container2-container1-container2-container1-element1'
                        },
                        {
                          kind: 'OB.UI.ScrollableTable',
                          classes:
                            'obObposPointOfSaleUiPayment-container1-container2-container1-container2-container1-payments',
                          name: 'payments',
                          renderEmpty: enyo.kind({
                            classes:
                              'obObposPointOfSaleUiPayment-payments-renderEmpty'
                          }),
                          skipLineRender: function(model) {
                            return model.get('changePayment');
                          },
                          renderLine: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine'
                        },
                        {
                          kind: 'OB.UI.ScrollableTable',
                          name: 'multiPayments',
                          classes:
                            'obObposPointOfSaleUiPayment-container1-container2-container1-container2-container1-multiPayments',
                          showing: false,
                          renderEmpty: enyo.kind({
                            classes:
                              'obObposPointOfSaleUiPayment-multiPayments-renderEmpty'
                          }),
                          skipLineRender: function(model) {
                            return model.get('changePayment');
                          },
                          renderLine: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine'
                        }
                      ]
                    }
                  ]
                },
                {
                  classes:
                    'obObposPointOfSaleUiPayment-container1-container2-container1-container3',
                  components: [
                    {
                      name: 'errorLabelArea',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container1-container3-errorLabelArea',
                      components: [
                        {
                          name: 'noenoughchangelbl',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-noenoughchangelbl',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'changeexceedlimit',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-changeexceedlimit',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'overpaymentnotavailable',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-overpaymentnotavailable',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'overpaymentexceedlimit',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-overpaymentexceedlimit',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'onlycashpaymentmethod',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-onlycashpaymentmethod',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'errorMaxlimitamount',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-errorMaxlimitamount',
                          showing: false,
                          type: 'error'
                        },
                        {
                          name: 'allAttributesNeedValue',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-allAttributesNeedValue',
                          type: 'error',
                          showing: false
                        },
                        {
                          name: 'paymentMethodSelect',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-paymentMethodSelect',
                          type: 'info',
                          showing: false
                        },
                        {
                          name: 'extrainfo',
                          classes:
                            'obObposPointOfSaleUiPayment-errorLabelArea-extrainfo',
                          type: 'info',
                          showing: false
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              classes:
                'obObposPointOfSaleUiPayment-container1-container2-container2',
              components: [
                {
                  classes:
                    'obObposPointOfSaleUiPayment-container1-container2-container2-container1',
                  components: [
                    {
                      name: 'prepaymentsbuttons',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container2-container1-prepaymentsbuttons',
                      components: [
                        {
                          name: 'prepaymentsexactbutton',
                          kind: 'OB.OBPOSPointOfSale.UI.PrepaymentsExactButton',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentsbuttons-prepaymentsexactbutton'
                        },
                        {
                          name: 'prepaymentsdeliverybutton',
                          kind:
                            'OB.OBPOSPointOfSale.UI.PrepaymentsDeliveryButton',
                          classes:
                            'obObposPointOfSaleUiPayment-prepaymentsbuttons-prepaymentsdeliverybutton'
                        }
                      ]
                    },
                    {
                      name: 'exactbutton',
                      kind: 'OB.OBPOSPointOfSale.UI.ExactButton',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container2-container1-exactbutton'
                    },
                    {
                      name: 'donebutton',
                      kind: 'OB.OBPOSPointOfSale.UI.DoneButton',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container2-container1-donebutton'
                    },
                    {
                      name: 'creditsalesaction',
                      kind: 'OB.OBPOSPointOfSale.UI.CreditButton',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container2-container1-creditsalesaction'
                    },
                    {
                      name: 'layawayaction',
                      kind: 'OB.OBPOSPointOfSale.UI.LayawayButton',
                      classes:
                        'obObposPointOfSaleUiPayment-container1-container2-container2-container1-layawayaction'
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ],

  receiptChanged: function() {
    this.$.payments.setCollection(this.receipt.get('payments'));
    this.$.multiPayments.setCollection(
      this.model.get('multiOrders').get('payments')
    );
    this.receipt.on(
      'change:bp',
      function(model) {
        var me = this;
        if (model.isCalculateReceiptLocked || model.isCalculateGrossLocked) {
          //We are processing the receipt, we cannot update pending yet
          return;
        }
        // If the business partner has been changed to the or from the anonymous customer, calculate the prepayment amount
        var calculateprepayments = OB.MobileApp.model.get('terminal')
            .terminalType.calculateprepayments,
          anonymousBP = OB.MobileApp.model.get('terminal').businessPartner,
          isClearWithProcessActive = OB.UTIL.ProcessController.isProcessActive(
            'clearWith'
          );
        if (
          calculateprepayments &&
          !isClearWithProcessActive &&
          OB.MobileApp.model.get('lastPaneShown') === 'payment' &&
          (model.get('bp').id === anonymousBP ||
            OB.UTIL.isNullOrUndefined(model.previousAttributes().bp) ||
            model.previousAttributes().bp.id === anonymousBP)
        ) {
          model.getPrepaymentAmount(function() {
            me.updatePending();
          });
        } else {
          this.updatePending();
        }
      },
      this
    );
    this.receipt.on(
      'disableDoneButton',
      function() {
        this.$.donebutton.setDisabled(true);
      },
      this
    );
    this.receipt.on(
      'updatePending',
      function(ignorePanel) {
        this.updatePending(ignorePanel);
      },
      this
    );
    this.receipt.on(
      'paymentCancel',
      function() {
        this.$.layawayaction.setDisabled(false);
        this.$.donebutton.setDisabled(false);
        this.$.creditsalesaction.setDisabled(false);
        OB.UTIL.showLoading(false);
      },
      this
    );
    this.model.get('multiOrders').on(
      'paymentCancel',
      function() {
        OB.UTIL.showLoading(false);
      },
      this
    );
    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function() {
        if (!this.model.get('leftColumnViewManager').isMultiOrder()) {
          this.updatePending();
        } else {
          this.updatePendingMultiOrders();
        }
      },
      this
    );
    this.updatePending();
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.updatePendingMultiOrders();
    }
    this.receipt.on(
      'change:orderType change:isLayaway change:payment change:documentNo',
      function(model) {
        if (this.model.get('leftColumnViewManager').isMultiOrder()) {
          this.updateCreditSalesAction();
          this.$.layawayaction.hide();
          return;
        }
        this.updateLayawayAction();
        this.updateCreditSalesAction();
      },
      this
    );
    this.updateExtraInfo('');
    this.receipt.on(
      'extrainfo',
      function(info) {
        this.updateExtraInfo(info);
      },
      this
    );
  },

  updateExtraInfo: function(info) {
    this.$.extrainfo.setContent(info || '');
    if (info && info.trim() !== '') {
      this.$.extrainfo.show();
    } else {
      this.$.extrainfo.hide();
    }
    this.alignErrorMessages();
  },

  updateLayawayAction: function(forceDisable) {
    var disable = forceDisable,
      paymentstatus = this.receipt.getPaymentStatus(),
      isLayaway =
        this.receipt.get('orderType') === 2 || this.receipt.get('isLayaway'),
      isMultiOrder = this.model.get('leftColumnViewManager').isMultiOrder();
    if (
      isMultiOrder ||
      paymentstatus.isNegative ||
      !isLayaway ||
      paymentstatus.done ||
      this.receipt.get('lines').length === 0
    ) {
      this.$.layawayaction.setLocalDisabled(false);
      this.$.layawayaction.hide();
    } else {
      var prepaymentAmount = this.receipt.get('obposPrepaymentamt'),
        receiptHasPrepaymentAmount =
          prepaymentAmount !== OB.DEC.Zero &&
          prepaymentAmount !== paymentstatus.totalAmt,
        pendingPrepayment = OB.DEC.sub(
          OB.DEC.add(prepaymentAmount, paymentstatus.pendingAmt),
          paymentstatus.totalAmt
        );
      this.$.layawayaction.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
      if (!disable && this.receipt.get('isLayaway')) {
        if (!this.receipt.isReversedPaid()) {
          disable = true;
        } else if (
          !_.find(this.receipt.get('payments').models, function(payment) {
            return !payment.get('isPrePayment');
          })
        ) {
          disable = true;
        }
      }
      this.$.layawayaction.setLocalDisabled(disable);
      if (receiptHasPrepaymentAmount && pendingPrepayment <= OB.DEC.Zero) {
        this.$.layawayaction.hide();
      } else {
        this.$.layawayaction.show();
      }
    }
  },

  updateCreditSalesAction: function() {
    if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
      return;
    }
    // The terminal allows to pay on credit
    var visible = OB.MobileApp.model.get('terminal').allowpayoncredit;
    // And not a voided layaway
    visible = visible && this.receipt.get('orderType') !== 3;
    // And receipt has not been paid
    visible = visible && !this.receipt.getPaymentStatus().done;
    // And Business Partner exists and is elegible to sell on credit.
    visible =
      visible &&
      this.receipt.get('bp') &&
      (this.receipt.get('bp').get('creditLimit') > 0 ||
        this.receipt.get('bp').get('creditUsed') < 0 ||
        this.receipt.getGross() < 0);

    if (visible) {
      this.$.creditsalesaction.show();
    } else {
      this.$.creditsalesaction.hide();
    }
  },
  actionChangeButton: function(inSender, inEvent) {
    var activemodel = this.activeModel(),
      change = activemodel.getChange();

    this.doShowPopup({
      popup: 'modalchange',
      args: {
        activemodel: activemodel,
        change: change,
        applyPaymentChange: function(paymentchange) {
          var paymentstatus, selectedPayment;

          this.applyPaymentChange(paymentchange);

          paymentstatus = this.receipt.getPaymentStatus();
          selectedPayment =
            OB.MobileApp.model.paymentnames[
              this.receipt.get('selectedPayment') ||
                OB.MobileApp.model.get('paymentcash')
            ];
          this.checkValidPayments(
            paymentstatus,
            selectedPayment,
            OB.DEC.compare(change) > 0
          );
        }.bind(this)
      }
    });

    return true;
  },
  calculateChangeReset: function() {
    this.applyPaymentChange(new OB.Payments.Change());
  },
  calculateChange: function(firstpayment, firstchange) {
    // payment is the first payment to use in the change calculation
    // change is > 0 and is in the document currency
    // Result vars...
    var paymentchange = new OB.Payments.Change(),
      usedpaymentsids = {};

    // Recursive function to calculate changes, payment by payment

    function calculateNextChange(payment, change) {
      var precision,
        changeLessThan,
        linkedSearchKey,
        changePayment,
        changePaymentRounded,
        linkedPayment;

      usedpaymentsids[payment.paymentMethod.id] = true; // mark this payment as used to avoid cycles.
      precision = payment.obposPosprecision;
      changeLessThan = payment.paymentMethod.changeLessThan;
      if (changeLessThan) {
        linkedSearchKey = payment.paymentMethod.changePaymentType;
        if (linkedSearchKey && !usedpaymentsids[linkedSearchKey]) {
          linkedPayment = OB.MobileApp.model.get('payments').find(function(p) {
            return p.paymentMethod.id === linkedSearchKey;
          });
          if (linkedPayment) {
            changePayment = OB.DEC.mul(change, payment.mulrate, precision);
            // Using 5 as rounding precision as a maximum precsion for all currencies
            changePaymentRounded = OB.DEC.mul(
              changeLessThan,
              Math.trunc(OB.DEC.div(changePayment, changeLessThan, 5)),
              precision
            );
            paymentchange.add({
              payment: payment,
              amount: changePaymentRounded,
              origAmount: OB.DEC.div(changePaymentRounded, payment.mulrate)
            });
            calculateNextChange(
              linkedPayment,
              OB.DEC.sub(
                change,
                OB.DEC.div(changePaymentRounded, payment.mulrate)
              )
            );
            return;
          }
        }
      }
      // No changeLessThan and no linked payment to continue,
      // Then add add change payment for the remaining change and exit
      paymentchange.add({
        payment: payment,
        amount: OB.DEC.mul(change, payment.mulrate, precision),
        origAmount: change
      });
    }

    // Ensure first payment is a cash payment
    if (!firstpayment.paymentMethod.iscash) {
      firstpayment = OB.MobileApp.model.get('payments').find(function(item) {
        return item.paymentMethod.iscash;
      });
    }

    if (firstpayment) {
      if (OB.MobileApp.model.get('terminal').multiChange) {
        // Here goes the logic to implement multi currency change
        calculateNextChange(firstpayment, firstchange);
      } else {
        // No multi currency change logic, add a simple change item and return
        paymentchange.add({
          payment: firstpayment,
          amount: OB.DEC.mul(
            firstchange,
            firstpayment.mulrate,
            firstpayment.obposPosprecision
          ),
          origAmount: firstchange
        });
      }
    }

    // Update receipt and UI with new calculations
    this.applyPaymentChange(paymentchange);
  },
  activeModel: function() {
    return this.model.isValidMultiOrderState() //
      ? this.model.get('multiOrders') //
      : this.receipt;
  },
  applyPaymentChange: function(paymentchange) {
    if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
      return;
    }
    // Set change calculation results
    this.activeModel().set('changePayments', paymentchange.payments);
    OB.MobileApp.model.set('changeReceipt', paymentchange.label);

    // Set change UI
    var showing = paymentchange.payments.length > 0;
    this.$.changebutton.setShowing(
      OB.MobileApp.model.get('terminal').multiChange && showing
    );
    this.$.change.setContent(paymentchange.label);
    this.$.change.setShowing(showing);
    this.$.changelbl.setShowing(showing);
  },
  checkEnoughMultiChange: function() {
    var failedPaymentMethods = [],
      activeModel = this.activeModel();

    if (
      !activeModel.get('multiOrdersList') &&
      OB.DEC.compare(activeModel.get('gross')) < 0
    ) {
      return []; // Change for returns is always valid.
    }

    activeModel.get('changePayments').forEach(function(itemchange) {
      var paymentMethod = OB.MobileApp.model.paymentnames[itemchange.key];
      if (
        OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
          OB.App.State.getState().Cashup.cashPaymentMethodInfo,
          paymentMethod.payment.id,
          OB.MobileApp.model.paymentnames,
          OB.UTIL.currency.webPOSDefaultCurrencyId(),
          OB.UTIL.currency.conversions
        ).foreignCurrentCash < itemchange.amountRounded
      ) {
        failedPaymentMethods.push(paymentMethod.payment._identifier);
      }
    });

    return failedPaymentMethods;
  },
  getOrigAmountChange: function(payment) {
    var changepayment = this.activeModel()
      .get('changePayments')
      .find(function(itemchange) {
        return itemchange.searchKey === payment.searchKey;
      });
    return changepayment
      ? OB.DEC.toBigDecimal(changepayment.origAmount).multiply(
          OB.DEC.toBigDecimal(payment.mulrate)
        )
      : 0;
  },
  updatePending: function(ignorePanel) {
    var execution = OB.UTIL.ProcessController.start('updatePending');
    if (!ignorePanel && OB.MobileApp.model.get('lastPaneShown') !== 'payment') {
      OB.UTIL.ProcessController.finish('updatePending', execution);
      return true;
    }
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      OB.UTIL.ProcessController.finish('updatePending', execution);
      return true;
    }
    if (this.receipt.get('lines').length === 0) {
      this.$.prepaymentLine.hide();
      this.$.paymentLine.hide();
      this.$.errorLabelArea.hide();
      this.$.prepaymentsbuttons.hide();
      this.$.exactbutton.hide();
      this.$.donebutton.hide();
      this.$.creditsalesaction.hide();
      this.$.layawayaction.hide();
      OB.UTIL.ProcessController.finish('updatePending', execution);
      return true;
    }
    var paymentstatus = this.receipt.getPaymentStatus(),
      prepaymentAmount = this.receipt.get('obposPrepaymentamt'),
      symbol = '',
      rate = OB.DEC.One,
      precision = null,
      symbolAtRight = true,
      receiptHasPrepaymentAmount =
        prepaymentAmount !== 0 && prepaymentAmount !== paymentstatus.totalAmt,
      pendingPrepayment = OB.DEC.sub(
        OB.DEC.add(prepaymentAmount, paymentstatus.pendingAmt),
        paymentstatus.totalAmt
      );

    this.$.paymentLine.show();

    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      symbol = OB.MobileApp.model.get('terminal').symbol;
      symbolAtRight = OB.MobileApp.model.get('terminal')
        .currencySymbolAtTheRight;
    }
    if (
      !_.isUndefined(this.receipt) &&
      !_.isUndefined(
        OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')]
      )
    ) {
      symbol =
        OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')]
          .symbol;
      rate =
        OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')]
          .mulrate;
      precision =
        OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')]
          .obposPosprecision;
      symbolAtRight =
        OB.MobileApp.model.paymentnames[this.receipt.get('selectedPayment')]
          .currencySymbolAtTheRight;
    }
    if (OB.DEC.compare(this.receipt.getChange()) > 0) {
      this.calculateChange(
        OB.MobileApp.model.paymentnames[
          this.receipt.get('selectedPayment') ||
            OB.MobileApp.model.get('paymentcash')
        ],
        this.receipt.getChange()
      );
    } else {
      this.calculateChangeReset();
    }
    this.checkValidPayments(
      paymentstatus,
      OB.MobileApp.model.paymentnames[
        this.receipt.get('selectedPayment') ||
          OB.MobileApp.model.get('paymentcash')
      ],
      OB.DEC.compare(this.receipt.getChange()) > 0
    );

    //Update styles based on the prepayment amount
    if (receiptHasPrepaymentAmount && paymentstatus.pendingAmt !== 0) {
      this.$.prepaymentLine.show();
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
        true
      );
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
        false
      );
      this.$.totalpending.addClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
      );
      this.$.totalpending.removeClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
      );
    } else {
      this.$.prepaymentLine.hide();
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
        false
      );
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
        true
      );
      this.$.totalpending.addClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
      );
      this.$.totalpending.removeClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
      );
    }

    this.$.prepaymenttotalpending.removeClass(
      'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
    );
    this.$.prepaymenttotalpending.removeClass(
      'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_alternativeColor'
    );

    if (
      OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
      this.receipt.get('obposPrepaymentlimitamt') < this.receipt.get('gross') &&
      pendingPrepayment > 0 &&
      receiptHasPrepaymentAmount
    ) {
      this.setPrepaymentTotalPending(
        OB.DEC.mul(pendingPrepayment, rate, precision),
        symbol,
        symbolAtRight
      );
      this.$.prepaymenttotalpending.show();
      this.$.prepaymenttotalpending.addClass(
        'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
      );
      this.$.prepaymenttotalpendinglbl.show();
      this.$.prepaymentexactlbl.hide();
      this.$.deliverychangelbl.hide();
      this.$.prepaymentsbuttons.show();
      this.$.exactbutton.hide();
    } else {
      this.$.prepaymenttotalpendinglbl.hide();
      if (pendingPrepayment === 0) {
        this.$.prepaymenttotalpending.hide();
        this.$.prepaymenttotalpending.addClass(
          'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
        );
        this.$.prepaymentexactlbl.show();
        this.$.deliverychangelbl.hide();
      } else {
        this.$.prepaymenttotalpending.show();
        this.$.prepaymentexactlbl.hide();
        if (this.receipt.get('prepaymentChangeMode')) {
          this.setPrepaymentTotalPending(
            OB.DEC.mul(this.receipt.getChange(), rate, precision),
            symbol,
            symbolAtRight
          );
          this.$.prepaymenttotalpending.addClass(
            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_alternativeColor'
          );
          this.$.deliverychangelbl.show();
          this.$.prepaymentLine.show();
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
            true
          );
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
            false
          );
          this.$.totalpending.addClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
          );
          this.$.totalpending.removeClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
          );
        } else {
          this.$.prepaymenttotalpending.addClass(
            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
          );
          this.$.deliverychangelbl.hide();
          this.$.prepaymentLine.hide();
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
            false
          );
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
            true
          );
          this.$.totalpending.addClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
          );
          this.$.totalpending.removeClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
          );
        }
      }
      if (
        !receiptHasPrepaymentAmount ||
        !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
      ) {
        this.$.prepaymentsbuttons.hide();
        this.$.exactbutton.show();
      } else {
        this.$.prepaymentsbuttons.show();
        this.$.exactbutton.hide();
      }
    }

    if (paymentstatus.pendingAmt <= 0 && paymentstatus.overpayment) {
      this.$.overpayment.setContent(
        OB.I18N.formatCurrencyWithSymbol(
          paymentstatus.overpayment,
          symbol,
          symbolAtRight
        )
      );
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (paymentstatus.pendingAmt <= 0 && paymentstatus.done) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      this.$.remainingfortotallbl.hide();
      if (
        !_.isEmpty(OB.MobileApp.model.paymentnames) ||
        this.receipt.get('orderType') === 3
      ) {
        this.$.donebutton.show();
      }
      this.updateCreditSalesAction();
      this.$.layawayaction.hide();
    } else {
      if (paymentstatus.pendingAmt < paymentstatus.totalAmt) {
        this.$.exactbutton.setLabel(
          OB.I18N.getLabel(this.$.exactbutton.i18nLabelRemaining)
        );
      } else {
        this.$.exactbutton.setLabel(
          OB.I18N.getLabel(this.$.exactbutton.i18nLabelTotal)
        );
      }
      this.setTotalPending(
        OB.DEC.mul(paymentstatus.pendingAmt, rate, precision),
        symbol,
        symbolAtRight
      );
      this.$.totalpending.show();
      if (paymentstatus.isNegative) {
        this.$.totalpendinglbl.setContent(
          OB.I18N.getLabel('OBPOS_ReturnRemaining')
        );
      } else {
        this.$.totalpendinglbl.setContent(
          OB.I18N.getLabel('OBPOS_PaymentsRemaining')
        );
      }
      if (receiptHasPrepaymentAmount && paymentstatus.pendingAmt !== 0) {
        this.$.remainingfortotallbl.show();
        this.$.totalpendinglbl.hide();
      } else {
        this.$.remainingfortotallbl.hide();
        this.$.totalpendinglbl.show();
      }

      if (
        OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
        !paymentstatus.isNegative &&
        !this.receipt.get('cancelLayaway')
      ) {
        this.$.donebutton.show();
      } else {
        this.$.donebutton.hide();
      }
      if (
        OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
        pendingPrepayment <= 0
      ) {
        this.$.prepaymentsdeliverybutton.hide();
      } else {
        this.$.prepaymentsdeliverybutton.show();
      }
      if (this.$.donebutton.drawerpreference) {
        this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.donebutton.drawerOpened = false;
      }
    }

    if (
      paymentstatus.pendingAmt <= 0 &&
      (paymentstatus.done || this.receipt.getGross() === 0)
    ) {
      this.$.exactbutton.hide();
      this.$.prepaymentsbuttons.hide();
      this.$.layawayaction.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        if (
          !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
        ) {
          this.$.exactbutton.show();
        }
      }
      this.updateLayawayAction();
    }
    if (
      paymentstatus.pendingAmt === 0 &&
      paymentstatus.done &&
      OB.DEC.compare(this.receipt.getChange()) <= 0 &&
      !paymentstatus.overpayment
    ) {
      if (this.receipt.getGross() === 0) {
        this.$.exactlbl.hide();
        this.$.donezerolbl.show();
      } else {
        this.$.donezerolbl.hide();
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
  updatePendingMultiOrders: function() {
    var execution = OB.UTIL.ProcessController.start('updatePendingMultiOrders'),
      multiOrders = this.model.get('multiOrders'),
      symbol = '',
      symbolAtRight = true,
      rate = OB.DEC.One,
      precision = null,
      selectedPayment,
      paymentStatus = multiOrders.getPaymentStatus(),
      prepaymentAmount = multiOrders.get('obposPrepaymentamt'),
      receiptHasPrepaymentAmount =
        prepaymentAmount !== 0 &&
        prepaymentAmount !==
          OB.DEC.add(
            multiOrders.get('total'),
            multiOrders.get('existingPayment')
          ) &&
        multiOrders.get('amountToLayaway') === 0,
      pendingPrepayment = OB.DEC.sub(
        prepaymentAmount,
        multiOrders.get('payment')
      );

    this.updateExtraInfo('');
    this.$.layawayaction.hide();
    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      symbol = OB.MobileApp.model.get('terminal').symbol;
      symbolAtRight = OB.MobileApp.model.get('terminal')
        .currencySymbolAtTheRight;
    }
    if (multiOrders.get('selectedPayment')) {
      selectedPayment =
        OB.MobileApp.model.paymentnames[multiOrders.get('selectedPayment')];
    } else {
      selectedPayment =
        OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
    }
    if (!_.isUndefined(selectedPayment)) {
      symbol = selectedPayment.symbol;
      rate = selectedPayment.mulrate;
      precision = selectedPayment.obposPosprecision;
      symbolAtRight = selectedPayment.currencySymbolAtTheRight;
    }

    //Update styles based on the prepayment amount
    if (
      receiptHasPrepaymentAmount &&
      OB.DEC.compare(
        OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))
      ) < 0
    ) {
      this.$.prepaymentLine.show();
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
        true
      );
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
        false
      );
      this.$.totalpending.addClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
      );
      this.$.totalpending.removeClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
      );
    } else {
      this.$.prepaymentLine.hide();
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
        false
      );
      this.$.paymentLine.addRemoveClass(
        'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
        true
      );
      this.$.totalpending.addClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
      );
      this.$.totalpending.removeClass(
        'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
      );
    }

    this.$.prepaymenttotalpending.removeClass(
      'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
    );
    this.$.prepaymenttotalpending.removeClass(
      'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_alternativeColor'
    );

    if (
      OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
      multiOrders.get('amountToLayaway') === 0 &&
      pendingPrepayment > 0 &&
      pendingPrepayment !==
        OB.DEC.sub(multiOrders.get('total'), multiOrders.get('payment'))
    ) {
      this.setPrepaymentTotalPending(
        OB.DEC.mul(pendingPrepayment, rate, precision),
        symbol,
        symbolAtRight
      );
      this.$.prepaymenttotalpending.show();
      this.$.prepaymenttotalpending.addClass(
        'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
      );
      this.$.prepaymenttotalpendinglbl.show();
      this.$.prepaymentexactlbl.hide();
      this.$.deliverychangelbl.hide();
      this.$.prepaymentsbuttons.show();
      this.$.exactbutton.hide();
    } else {
      this.$.prepaymenttotalpending.hide();
      this.$.prepaymenttotalpendinglbl.hide();
      if (pendingPrepayment === 0) {
        this.$.prepaymenttotalpending.hide();
        this.$.prepaymenttotalpending.addClass(
          'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
        );
        this.$.prepaymentexactlbl.show();
        this.$.deliverychangelbl.hide();
      } else {
        this.$.prepaymenttotalpending.show();
        this.$.prepaymentexactlbl.hide();
        if (multiOrders.get('prepaymentChangeMode')) {
          this.setPrepaymentTotalPending(
            OB.DEC.mul(multiOrders.getChange(), rate, precision),
            symbol,
            symbolAtRight
          );
          this.$.prepaymenttotalpending.addClass(
            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_alternativeColor'
          );
          this.$.deliverychangelbl.show();
          this.$.prepaymentLine.show();
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
            true
          );
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
            false
          );
          this.$.totalpending.addClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
          );
          this.$.totalpending.removeClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
          );
        } else {
          this.$.prepaymenttotalpending.addClass(
            'obObposPointOfSaleUiPayment-prepaymentLine-prepaymenttotalpending_defaultColor'
          );
          this.$.deliverychangelbl.hide();
          this.$.prepaymentLine.hide();
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_wPrepayment',
            false
          );
          this.$.paymentLine.addRemoveClass(
            'obObposPointOfSaleUiPayment-container1-container2-container1-paymentLine_woPrepayment',
            true
          );
          this.$.totalpending.addClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_bigFont'
          );
          this.$.totalpending.removeClass(
            'obObposPointOfSaleUiPayment-paymentLine-totalpending_smallFont'
          );
        }
      }

      if (
        !receiptHasPrepaymentAmount ||
        !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
      ) {
        this.$.prepaymentsbuttons.hide();
        this.$.exactbutton.show();
      } else {
        this.$.prepaymentsbuttons.show();
        this.$.exactbutton.hide();
      }
    }

    if (multiOrders.getChange()) {
      this.calculateChange(selectedPayment, multiOrders.getChange());
    } else {
      this.calculateChangeReset();
    }
    this.checkValidPayments(
      paymentStatus,
      selectedPayment,
      OB.DEC.compare(multiOrders.getChange()) > 0
    );
    //overpayment
    if (paymentStatus.overpayment) {
      this.$.overpayment.setContent(
        OB.I18N.formatCurrencyWithSymbol(
          paymentStatus.overpayment,
          symbol,
          symbolAtRight
        )
      );
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }

    if (
      multiOrders.get('multiOrdersList').length > 0 &&
      OB.DEC.compare(multiOrders.get('total')) >= 0 &&
      OB.DEC.compare(
        OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))
      ) >= 0
    ) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      this.$.remainingfortotallbl.hide();
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        this.$.donebutton.show();
      }
      this.updateCreditSalesAction();
    } else {
      if (
        OB.DEC.sub(multiOrders.get('total'), multiOrders.get('payment')) <
        multiOrders.get('total')
      ) {
        this.$.exactbutton.setLabel(
          OB.I18N.getLabel(this.$.exactbutton.i18nLabelRemaining)
        );
      } else {
        this.$.exactbutton.setLabel(
          OB.I18N.getLabel(this.$.exactbutton.i18nLabelTotal)
        );
      }
      this.setTotalPending(
        OB.DEC.mul(
          OB.DEC.sub(multiOrders.get('total'), multiOrders.get('payment')),
          rate,
          precision
        ),
        symbol,
        symbolAtRight
      );
      this.$.totalpending.show();
      if (
        receiptHasPrepaymentAmount &&
        OB.DEC.compare(
          OB.DEC.sub(multiOrders.get('total'), multiOrders.get('payment'))
        ) > 0
      ) {
        this.$.remainingfortotallbl.show();
        this.$.totalpendinglbl.hide();
      } else {
        this.$.remainingfortotallbl.hide();
        this.$.totalpendinglbl.show();
      }
      if (
        OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
        !paymentStatus.isNegative &&
        multiOrders.get('amountToLayaway') === 0
      ) {
        this.$.donebutton.show();
      } else {
        this.$.donebutton.hide();
      }
      if (
        OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
        pendingPrepayment <= 0
      ) {
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
    if (
      multiOrders.get('multiOrdersList').length > 0 &&
      OB.DEC.compare(multiOrders.get('total')) >= 0 &&
      (OB.DEC.compare(
        OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))
      ) >= 0 ||
        multiOrders.get('total') === 0)
    ) {
      this.$.exactbutton.hide();
      this.$.prepaymentsbuttons.hide();
    } else {
      if (!_.isEmpty(OB.MobileApp.model.paymentnames)) {
        if (
          !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
        ) {
          OB.debug('updatePendingMultiOrders: show exact button 1');
          this.$.exactbutton.show();
        }
      }
    }
    if (
      multiOrders.get('multiOrdersList').length > 0 &&
      OB.DEC.compare(multiOrders.get('total')) >= 0 &&
      OB.DEC.compare(
        OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))
      ) >= 0 &&
      !multiOrders.getChange() &&
      OB.DEC.compare(
        OB.DEC.sub(multiOrders.get('payment'), multiOrders.get('total'))
      ) <= 0
    ) {
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

  checkEnoughCashAvailable: function(
    paymentstatus,
    selectedPayment,
    scope,
    button,
    callback
  ) {
    var requiredCash,
      hasEnoughCash = true,
      hasAllEnoughCash = true,
      reversedPayments = [],
      currentSelectedPaymentCashAmount = OB.DEC.Zero,
      failedPaymentMethods = [],
      reversedCash;
    // Check slave cash
    this.checkSlaveCashAvailable(
      selectedPayment,
      this,
      function(currentCash) {
        var changeAmt, selectedChange;

        // If there are reverse payments search for those of cash payment method. It will be needed to check if there is enough cash to reverse those payments.
        if (paymentstatus.isReversal) {
          paymentstatus.payments.each(function(payment) {
            var paymentmethod =
              OB.POS.terminal.terminal.paymentnames[payment.get('kind')];
            if (
              !payment.get('isPrePayment') &&
              paymentmethod.paymentMethod.iscash
            ) {
              reversedCash = OB.DEC.sub(
                reversedPayments[payment.get('kind')] || OB.DEC.Zero,
                payment.get('origAmount')
              );
              reversedPayments[payment.get('kind')] = reversedCash;
              if (
                selectedPayment !== paymentmethod &&
                OB.DEC.compare(
                  OB.DEC.sub(
                    OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
                      OB.App.State.getState().Cashup.cashPaymentMethodInfo,
                      paymentmethod.payment.id,
                      OB.MobileApp.model.paymentnames,
                      OB.UTIL.currency.webPOSDefaultCurrencyId(),
                      OB.UTIL.currency.conversions
                    ).currentCash,
                    reversedCash
                  )
                ) < 0
              ) {
                hasEnoughCash = false;
              } else {
                currentSelectedPaymentCashAmount = reversedCash;
              }
            }
          });
        }

        if (hasEnoughCash) {
          if (
            OB.UTIL.isNullOrUndefined(selectedPayment) ||
            !selectedPayment.paymentMethod.iscash
          ) {
            requiredCash = OB.DEC.Zero;
          } else if (
            (button === 'Done' || button === 'Credit') &&
            !_.isUndefined(paymentstatus) &&
            paymentstatus.isNegative
          ) {
            requiredCash = OB.DEC.add(
              currentSelectedPaymentCashAmount,
              paymentstatus.pendingAmt
            );
            paymentstatus.payments.each(function(payment) {
              var paymentmethod;
              if (
                payment.get('kind') === selectedPayment.payment.searchKey &&
                !payment.get('isPrePayment') &&
                !payment.get('reversedPaymentId')
              ) {
                requiredCash = OB.DEC.add(
                  requiredCash,
                  payment.get('origAmount')
                );
              } else {
                OB.POS.terminal.terminal.paymentnames[payment.get('kind')];
                if (
                  paymentmethod &&
                  payment.get('amount') >
                    OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
                      OB.App.State.getState().Cashup.cashPaymentMethodInfo,
                      paymentmethod.payment.id,
                      OB.MobileApp.model.paymentnames,
                      OB.UTIL.currency.webPOSDefaultCurrencyId(),
                      OB.UTIL.currency.conversions
                    ).foreignCurrentCash &&
                  payment.get('isCash')
                ) {
                  hasAllEnoughCash = false;
                }
              }
            });
          } else if (!_.isUndefined(paymentstatus)) {
            selectedChange = this.activeModel()
              .get('changePayments')
              .find(function(item) {
                return item.key === selectedPayment.payment.searchKey;
              });
            changeAmt = selectedChange ? selectedChange.origAmount : 0;
            if (button === 'Layaway' || button === 'Credit') {
              requiredCash = OB.DEC.add(
                currentSelectedPaymentCashAmount,
                changeAmt
              );
            } else {
              requiredCash = OB.DEC.Zero; // Rely on checkEnoughMultiChange() for this case
            }
          }

          if (!_.isUndefined(requiredCash) && requiredCash === 0) {
            hasEnoughCash = true;
          } else if (!_.isUndefined(requiredCash)) {
            hasEnoughCash =
              OB.DEC.compare(OB.DEC.sub(currentCash, requiredCash)) >= 0;
          }

          failedPaymentMethods = this.checkEnoughMultiChange();
          hasEnoughCash = hasEnoughCash && failedPaymentMethods.length === 0;
        }

        if (
          hasEnoughCash &&
          (button === 'Layaway' ||
            button === 'Credit' ||
            (button === 'Done' && hasAllEnoughCash))
        ) {
          this.$.noenoughchangelbl.setContent(
            OB.I18N.getLabel('OBPOS_NoEnoughCash')
          );
          return callback.call(scope, true);
        } else {
          if (
            failedPaymentMethods.length > 0 &&
            OB.MobileApp.model.get('terminal').multiChange
          ) {
            this.$.noenoughchangelbl.setContent(
              OB.I18N.getLabel('OBPOS_NoEnoughCashMultiChange', [
                failedPaymentMethods.join(', ')
              ])
            );
          } else {
            this.$.noenoughchangelbl.setContent(
              OB.I18N.getLabel('OBPOS_NoEnoughCash')
            );
          }
          return callback.call(scope, false); // check failed.
        }
      }.bind(this)
    );
  },

  checkValidOverpayment: function(paymentstatus) {
    var requiredOverpayment = paymentstatus.overpayment,
      overPaymentUsed = _.last(paymentstatus.payments.models),
      overPaymentMethod =
        overPaymentUsed &&
        OB.MobileApp.model.paymentnames[overPaymentUsed.get('kind')]
          ? OB.MobileApp.model.paymentnames[overPaymentUsed.get('kind')]
              .paymentMethod
          : undefined;

    // Execute logic only if all the following requirements are met:
    //  * There is at least one payment added to the receipt
    //  * The payment method has a overpayment limit defined
    //  * There is an overpayment
    if (
      overPaymentMethod &&
      !OB.UTIL.isNullOrUndefined(overPaymentMethod.overpaymentLimit) &&
      !OB.UTIL.isNullOrUndefined(requiredOverpayment)
    ) {
      var overpaymentAmt = new BigDecimal(String(requiredOverpayment));
      var overpaymentLimit = new BigDecimal(
        String(overPaymentMethod.overpaymentLimit)
      );
      if (overpaymentAmt.compareTo(BigDecimal.prototype.ZERO) !== 0) {
        if (
          overpaymentLimit.compareTo(BigDecimal.prototype.ZERO) === 0 &&
          overpaymentLimit.compareTo(overpaymentAmt) < 0
        ) {
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

  checkValidCashChange: function(paymentstatus, selectedPayment) {
    if (!selectedPayment) {
      return false;
    }

    var requiredCash;

    if (
      OB.UTIL.isNullOrUndefined(selectedPayment) ||
      OB.UTIL.isNullOrUndefined(selectedPayment.paymentMethod.overpaymentLimit)
    ) {
      return true;
    }

    requiredCash = this.getOrigAmountChange(selectedPayment);
    if (!OB.UTIL.isNullOrUndefined(requiredCash)) {
      requiredCash = OB.DEC.toNumber(requiredCash);
    }
    if (requiredCash !== 0) {
      if (
        selectedPayment.paymentMethod.overpaymentLimit === 0 &&
        selectedPayment.paymentMethod.overpaymentLimit < requiredCash
      ) {
        this.$.changeexceedlimit.show();
        return false;
      } else if (
        selectedPayment.paymentMethod.overpaymentLimit < requiredCash
      ) {
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

  checkValidPaymentMethod: function(paymentstatus, selectedPayment) {
    if (!selectedPayment) {
      return false;
    }

    var change = this.model.getChange();
    var alternativeCashPayment;
    if (change && change > 0) {
      if (
        !selectedPayment.paymentMethod.iscash &&
        paymentstatus.payments.length > 0
      ) {
        alternativeCashPayment = _.find(paymentstatus.payments.models, function(
          item
        ) {
          if (item.get('isCash')) {
            return item;
          }
        });
        if (!alternativeCashPayment) {
          this.$.onlycashpaymentmethod.show();
          return false;
        }
      }
    }
    return true;
  },

  checkDrawerPreference: function() {
    var hasCashPayment,
      paymentList = this.model.get('multiOrders').get('payments');
    if (paymentList.length > 0) {
      hasCashPayment = _.find(paymentList.models, function(item) {
        if (item.get('isCash')) {
          return item;
        }
      });
    }
    if (
      _.isUndefined(hasCashPayment) &&
      this.model.get('multiOrders').selectedPayment !== 'OBPOS_payment.cash'
    ) {
      this.$.donebutton.drawerpreference = false;
      this.$.donebutton.drawerOpened = true;
      this.$.donebutton.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
    }
  },

  checkValidPayments: function(paymentstatus, selectedPayment, hasChange) {
    var resultOK,
      me = this;

    if (!selectedPayment) {
      return;
    }
    // Hide all error labels. Error labels are shown by check... functions
    if (!paymentstatus.overpayment) {
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
      if (selectedPayment.paymentMethod.iscash && hasChange) {
        resultOK = this.checkValidCashChange(paymentstatus, selectedPayment);
      } else {
        resultOK = undefined;
      }
    } else {
      resultOK = false;
    }
    if (resultOK || _.isUndefined(resultOK)) {
      if (
        hasChange ||
        ((paymentstatus.isNegative || paymentstatus.isReversal) &&
          !_.isNull(paymentstatus.pending))
      ) {
        // avoid checking for shared paymentMethod
        if (
          (hasChange && selectedPayment.paymentMethod.isshared) ||
          (JSON.parse(OB.UTIL.localStorage.getItem('isSafeBox')) &&
            selectedPayment.paymentMethod.issafebox)
        ) {
          resultOK = true;
        } else {
          resultOK = this.checkEnoughCashAvailable(
            paymentstatus,
            selectedPayment,
            this,
            'Done',
            function(success) {
              var lsuccess = success;
              if (lsuccess) {
                lsuccess = this.checkValidPaymentMethod(
                  paymentstatus,
                  selectedPayment
                );
              } else {
                this.$.noenoughchangelbl.show();
                this.$.donebutton.setLocalDisabled(true);
                this.$.exactbutton.setLocalDisabled(true);
                if (
                  OB.MobileApp.model.get('terminal').terminalType
                    .calculateprepayments
                ) {
                  this.$.prepaymentsdeliverybutton.setLocalDisabled(true);
                  this.$.prepaymentsexactbutton.setLocalDisabled(true);
                }
              }
              me.updateAddPaymentAction();
              this.setStatusButtons(lsuccess, 'Done');
              this.checkEnoughCashAvailable(
                paymentstatus,
                selectedPayment,
                this,
                'Layaway',
                function(success) {
                  this.setStatusButtons(success, 'Layaway');
                }
              );
              this.checkEnoughCashAvailable(
                paymentstatus,
                selectedPayment,
                this,
                'Credit',
                function(success) {
                  this.setStatusButtons(success, 'Credit');
                }
              );
            }
          );
        }
      } else if (!this.getAddPaymentAction()) {
        this.$.donebutton.setLocalDisabled(false);
        this.$.exactbutton.setLocalDisabled(false);
        if (
          OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
        ) {
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
    if (
      OB.MobileApp.model.hasPermission(
        'OBPOS_EnableSupportForProductAttributes',
        true
      ) &&
      paymentstatus.done &&
      !this.receipt.checkAllAttributesHasValue()
    ) {
      this.$.donebutton.setLocalDisabled(true);
      this.$.allAttributesNeedValue.show();
    }

    this.alignErrorMessages();
  },
  updateAddPaymentAction: function() {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      this.receipt.stopAddingPayments = !_.isEmpty(
        this.getShowingErrorMessages()
      );
    } else if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.model.get('multiOrders').stopAddingPayments = !_.isEmpty(
        this.getShowingErrorMessages()
      );
    }
  },
  getAddPaymentAction: function() {
    return this.model.get('leftColumnViewManager').isOrder()
      ? this.receipt.stopAddingPayments
      : this.model.get('multiOrders').stopAddingPayments;
  },
  alignErrorMessages: function() {
    if (
      OB.MobileApp.view.currentWindow === 'retail.pointofsale' &&
      typeof this.$.errorLabelArea !== 'undefined'
    ) {
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
        this.maxAnimateErrorInterval = setInterval(function() {
          clearInterval(this.animateErrorInterval);
          me.animateErrorMessages();
        }, delay + 1700 * this.showingCount);
      }
    }
  },
  animateErrorMessages: function() {
    // TODO: To be removed after new skin implementation
    if (
      OB.MobileApp.view.currentWindow === 'retail.pointofsale' &&
      typeof this.$.errorLabelArea !== 'undefined'
    ) {
      clearInterval(this.animateErrorInterval);
      // Not sure if this defaultStyle initialization is necessary as it is used once, and it is completely overrided when used
      var me = this,
        marginTop = 0,
        resizediStyle = '',
        initialTop = 0,
        defaultStyle =
          'position: absolute; bottom: 0px; height: 20px; color: #ff0000;';
      this.errorLabels = this.pushErrorMessagesToArray();
      this.showingCount = this.getShowingMessagesCount(this.errorLabels);
      this.$.errorLabelArea.show();
      this.firstShowingObject = this.getFirstShowingObject(this.errorLabels);
      if (this.firstShowingObject && this.showingCount > 1) {
        this.animateErrorInterval = setInterval(function() {
          marginTop = marginTop - 2;
          this.marginTop = marginTop;
          resizediStyle = 'margin-top: ' + marginTop + 'px';
          // This inline style is allowed
          me.firstShowingObject.addStyles(resizediStyle);
        }, 100);
      }
      if (this.showingCount === 1) {
        defaultStyle = 'margin-top: ' + initialTop + 'px';
        // This inline style is allowed
        this.firstShowingObject.addStyles(defaultStyle);
      }
    }
  },
  pushErrorMessagesToArray: function() {
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
  getFirstShowingObject: function(errorLabelArray) {
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
  getShowingMessagesCount: function(errorLabelArray) {
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
  resumeAnimation: function(inSender, inEvent) {
    if (
      inEvent.originator.type === 'error' ||
      inEvent.originator.type === 'info'
    ) {
      this.alignErrorMessages();
    }
  },
  pauseAnimation: function(inSender, inEvent) {
    if (inEvent.originator.type === 'error') {
      clearInterval(this.maxAnimateErrorInterval);
      clearInterval(this.animateErrorInterval);
      // This inline style is allowed
      inEvent.originator.addStyles(this.marginTop);
    }
  },
  dispalyErrorLabels: function(inSender, inEvent) {
    if (inEvent.originator.type === 'error') {
      var message = this.getShowingErrorMessages();
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        message
      );
      this.alignErrorMessages(false);
    }
  },
  getShowingErrorMessages: function() {
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
  setStatusButtons: function(resultOK, button) {
    // If there's a reverse payment and the reversed amount is not paid disable also the buttons
    var isMultiOrder = this.model.get('leftColumnViewManager').isMultiOrder(),
      statusOK = isMultiOrder ? true : this.receipt.isReversedPaid();
    if (button === 'Done') {
      if (resultOK) {
        var disableButton = !statusOK;
        this.$.donebutton.setLocalDisabled(disableButton);
        this.$.exactbutton.setLocalDisabled(false);
        if (
          OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
        ) {
          this.$.prepaymentsdeliverybutton.setLocalDisabled(false);
          this.$.prepaymentsexactbutton.setLocalDisabled(false);
        }
      } else {
        if (
          this.$.changeexceedlimit.showing ||
          this.$.overpaymentnotavailable.showing ||
          this.$.overpaymentexceedlimit.showing ||
          this.$.onlycashpaymentmethod.showing
        ) {
          this.$.noenoughchangelbl.hide();
        } else {
          this.$.noenoughchangelbl.show();
        }
        this.$.donebutton.setLocalDisabled(true);
        this.$.exactbutton.setLocalDisabled(true);
        if (
          OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
        ) {
          this.$.prepaymentsdeliverybutton.setLocalDisabled(true);
          this.$.prepaymentsexactbutton.setLocalDisabled(true);
        }
      }
    } else if (button === 'Layaway') {
      this.updateLayawayAction(resultOK ? false : true);
    } else if (button === 'Credit') {
      if (resultOK) {
        this.$.creditsalesaction.setLocalDisabled(false);
      } else {
        // If the ticket is a negative ticket, even when there's not enough cash, it must be possible to click on the 'Use Credit' button
        if (!isMultiOrder && this.receipt.isNegative()) {
          this.$.creditsalesaction.setLocalDisabled(false);
        } else {
          this.$.creditsalesaction.setLocalDisabled(true);
        }
      }
    }
  },

  checkSlaveCashAvailable: function(selectedPayment, scope, callback) {
    async function processCashMgmtMaster(cashMgntCallback) {
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.ProcessCashMgmtMaster',
        {
          cashUpId: OB.App.State.getState().Cashup.id,
          terminalSlave: OB.POS.modelterminal.get('terminal').isslave
        }
      );
      if (response && response.response && response.response.error) {
        // Error handler
        OB.log('error', response.response.error.message);
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashMgmtError'),
          OB.I18N.getLabel('OBPOS_ErrorServerGeneric') +
            response.response.error.message,
          [
            {
              label: OB.I18N.getLabel('OBPOS_LblRetry'),
              action: function() {
                processCashMgmtMaster(cashMgntCallback);
              }
            }
          ],
          {
            autoDismiss: false,
            onHideFunction: function() {
              cashMgntCallback(false, null);
            }
          }
        );
      } else {
        cashMgntCallback(true, response.response.data);
      }
    }

    var currentCash = OB.DEC.Zero;
    if (selectedPayment && selectedPayment.paymentMethod.iscash) {
      currentCash =
        OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
          OB.App.State.getState().Cashup.cashPaymentMethodInfo,
          selectedPayment.payment.id,
          OB.MobileApp.model.paymentnames,
          OB.UTIL.currency.webPOSDefaultCurrencyId(),
          OB.UTIL.currency.conversions
        ).currentCash || OB.DEC.Zero;
    }
    if (
      (OB.POS.modelterminal.get('terminal').ismaster ||
        OB.POS.modelterminal.get('terminal').isslave) &&
      selectedPayment.paymentMethod.iscash &&
      selectedPayment.paymentMethod.isshared
    ) {
      // Load current cashup info from slaves
      processCashMgmtMaster(function(success, data) {
        if (success) {
          _.each(data, function(pay) {
            if (pay.searchKey === selectedPayment.payment.searchKey) {
              currentCash = OB.DEC.add(
                currentCash,
                pay.startingCash +
                  pay.totalDeposits +
                  pay.totalSales -
                  pay.totalReturns -
                  pay.totalDrops
              );
            }
          });
        }
        callback.call(scope, currentCash);
      });
    } else {
      callback.call(scope, currentCash);
    }
  },

  initComponents: function() {
    this.inherited(arguments);
    this.$.errorLabelArea.render();
    this.$.prepaymenttotalpendinglbl.setContent(
      OB.I18N.getLabel('OBPOS_PaymentsRemainingDelivery')
    );
    this.$.prepaymentexactlbl.setContent(
      OB.I18N.getLabel('OBPOS_PaymentsExactDelivery')
    );
    this.$.deliverychangelbl.setContent(
      OB.I18N.getLabel('OBPOS_LblChangeForDelivery')
    );
    this.$.totalpendinglbl.setContent(
      OB.I18N.getLabel('OBPOS_PaymentsRemaining')
    );
    this.$.remainingfortotallbl.setContent(
      OB.I18N.getLabel('OBPOS_PaymentsRemainingTotal')
    );
    this.$.changelbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsChange'));
    this.$.overpaymentlbl.setContent(
      OB.I18N.getLabel('OBPOS_PaymentsOverpayment')
    );
    this.$.exactlbl.setContent(OB.I18N.getLabel('OBPOS_PaymentsExact'));
    this.$.donezerolbl.setContent(
      OB.I18N.getLabel('OBPOS_MsgPaymentAmountZero')
    );
    this.$.noenoughchangelbl.setContent(OB.I18N.getLabel('OBPOS_NoEnoughCash'));
    this.$.changeexceedlimit.setContent(
      OB.I18N.getLabel('OBPOS_ChangeLimitOverLimit')
    );
    this.$.overpaymentnotavailable.setContent(
      OB.I18N.getLabel('OBPOS_OverpaymentNotAvailable')
    );
    this.$.overpaymentexceedlimit.setContent(
      OB.I18N.getLabel('OBPOS_OverpaymentExcededLimit')
    );
    this.$.onlycashpaymentmethod.setContent(
      OB.I18N.getLabel('OBPOS_OnlyCashPaymentMethod')
    );
    this.$.allAttributesNeedValue.setContent(
      OB.I18N.getLabel('OBPOS_AllAttributesNeedValue')
    );
  },
  init: function(model) {
    this.model = model;
    if (_.isEmpty(OB.MobileApp.model.paymentnames)) {
      this.$.donebutton.show();
      this.$.exactbutton.hide();
      this.$.prepaymentsbuttons.hide();
    }
    this.model
      .get('multiOrders')
      .get('multiOrdersList')
      .on(
        'all',
        function(event) {
          if (this.model.isValidMultiOrderState()) {
            this.updatePendingMultiOrders();
          }
        },
        this
      );

    this.model.get('multiOrders').on(
      'change:payment change:total change:change change:prepaymentAmt paymentCancel',
      function() {
        this.updatePendingMultiOrders();
      },
      this
    );
    this.model.get('multiOrders').on(
      'disableDoneButton',
      function() {
        this.$.donebutton.setDisabled(true);
      },
      this
    );
    this.model.get('leftColumnViewManager').on(
      'change:currentView',
      function(changedModel) {
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
      },
      this
    );

    this.model.get('order').on(
      'change:isNegative change:doCancelAndReplace',
      function(model) {
        if (model.get('doCancelAndReplace')) {
          // Render the payments because it's possible that the amount must be shown with another
          // sign (depends on the gross and the isNegative properties)
          this.waterfall('onRenderPaymentLine');
        }
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  kind: 'OB.UI.Button',
  classes: 'obObposPointOfSaleUiProcessButton',
  processdisabled: false,
  localdisabled: false,
  disabled: false,
  isLocked: true,
  lastDisabledStatus: true,
  setLocalDisabled: function(value) {
    this.localdisabled = value;
    this.setDisabled(this.processdisabled || this.localdisabled);
  },
  disableButton: function() {
    this.isLocked = true;
    this.setDisabledIfSynchronized();
  },
  enableButton: function() {
    this.isLocked = false;
    this.setDisabledIfSynchronized();
  },
  setDisabled: function(value) {
    this.lastDisabledStatus = value;
    this.setDisabledIfSynchronized();
  },
  setDisabledIfSynchronized: function() {
    var value = this.lastDisabledStatus || this.isLocked || false;
    if (this.isLocked) {
      value = true;
    }
    if (
      OB.UTIL.ProcessController.getProcessesInExecByOBj(this).length > 0 &&
      !value
    ) {
      return true;
    }
    this.disabled = value; // for getDisabled() to return the correct value
    this.setAttribute('disabled', value); // to effectively turn the button enabled or disabled
  },
  initComponents: function() {
    var me = this;
    this.inherited(arguments);
    OB.POS.EventBus.on('UI_Enabled', function(state) {
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
  classes: 'obObposPointOfSaleUiDoneButton',
  i18nLabel: 'OBPOS_LblDone',
  drawerOpened: true,
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'cancelLayaway',
    'paymentDone',
    'tapDoneButton',
    'servicePriceCalculation'
  ],
  init: function(model) {
    this.model = model;
    this.setDisabledIfSynchronized();
    this.model.get('order').on(
      'change:openDrawer',
      function() {
        this.drawerpreference = this.model.get('order').get('openDrawer');

        if (this.drawerpreference) {
          this.drawerOpened = false;
          this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        } else {
          this.drawerOpened = true;
          this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
        }
      },
      this
    );
    this.model.get('multiOrders').on(
      'change:openDrawer',
      function() {
        this.drawerpreference = this.model.get('multiOrders').get('openDrawer');
        if (this.drawerpreference) {
          this.drawerOpened = false;
          this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        } else {
          this.drawerOpened = true;
          this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
        }
      },
      this
    );
  },
  blocked: false,
  tap: async function() {
    var myModel = this.owner.model,
      me = this,
      payments,
      isMultiOrder = myModel.get('leftColumnViewManager').isOrder()
        ? false
        : true,
      avoidPayment = false,
      orderDesc = '',
      execution;

    //*** Avoid double click ***
    if (this.getContent() === OB.I18N.getLabel('OBPOS_LblDone')) {
      if (this.owner.receipt && this.owner.receipt.getOrderDescription) {
        orderDesc = this.owner.receipt.getOrderDescription();
      }
      OB.info(
        'Time: ' +
          new Date() +
          '. Payment Button Pressed ( Status: ' +
          this.disabled +
          ') ' +
          orderDesc
      );
      if (me.blocked) {
        OB.error(
          'Time: ' +
            new Date() +
            '. Done button has been pressed 2 times and second execution is discarded ' +
            orderDesc
        );
        return;
      } else {
        me.blocked = true;
        setTimeout(function() {
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

    execution = OB.UTIL.ProcessController.start('tapDoneButton');

    if (!isMultiOrder) {
      await OB.App.State.Global.completeTicket({
        organization: OB.MobileApp.model.get('terminal').organization,
        cashUpId: OB.MobileApp.model.get('terminal').cashUpId,
        returnSequencePrefix: OB.MobileApp.model.get('terminal')
          .returnDocNoPrefix,
        quotationSequencePrefix: OB.MobileApp.model.get('terminal')
          .quotationDocNoPrefix,
        fullReturnInvoiceSequencePrefix: OB.MobileApp.model.get('terminal')
          .fullReturnInvoiceDocNoPrefix,
        simplifiedReturnInvoiceSequencePrefix: OB.MobileApp.model.get(
          'terminal'
        ).simplifiedReturnInvoiceDocNoPrefix,
        documentNumberSeperator: OB.Model.Order.prototype.includeDocNoSeperator
          ? '/'
          : '',
        documentNumberPadding: OB.MobileApp.model.get('terminal')
          .documentnoPadding,
        salesWithOneLineNegativeAsReturns: OB.MobileApp.model.get('permissions')
          .OBPOS_SalesWithOneLineNegativeAsReturns
      });

      if (OB.UTIL.RfidController.isRfidConfigured()) {
        OB.UTIL.RfidController.processRemainingCodes(
          OB.MobileApp.model.receipt
        );
        OB.UTIL.RfidController.updateEpcBuffers();
      }

      OB.MobileApp.model.orderList.deleteCurrentFromDatabase(
        OB.MobileApp.model.receipt
      );
      if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_alwaysCreateNewReceiptAfterPayReceipt',
          true
        )
      ) {
        OB.MobileApp.model.orderList.deleteCurrent(true);
      } else {
        OB.MobileApp.model.orderList.deleteCurrent();
      }
      OB.UTIL.ProcessController.finish('tapDoneButton', execution);
      return;
    }

    if (!OB.MobileApp.model.get('terminal').returns_anonymouscustomer) {
      if (isMultiOrder) {
        var orderList = myModel.get('multiOrders').get('multiOrdersList');
        orderList.forEach(function(receipt) {
          if (receipt.isAnonymousBlindReturn()) {
            avoidPayment = true;
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust')
            );
            return;
          }
        });
      } else {
        if (this.owner.receipt.isAnonymousBlindReturn()) {
          OB.UTIL.ProcessController.finish('tapDoneButton', execution);
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust')
          );
          return;
        }
      }
    }

    if (isMultiOrder) {
      var orders = this.owner.model.get('multiOrders').get('multiOrdersList')
        .models;
      orders.forEach(function(order) {
        if (
          order.get('orderType') === 2 &&
          order.get('bp').id ===
            OB.MobileApp.model.get('terminal').businessPartner &&
          !OB.MobileApp.model.get('terminal').layaway_anonymouscustomer
        ) {
          avoidPayment = true;
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_layawaysOrdersWithAnonimousCust')
          );
          return;
        }
      });
    }

    if (!avoidPayment) {
      if (isMultiOrder) {
        payments = this.owner.model.get('multiOrders').get('payments');
      } else {
        payments = this.owner.receipt.get('payments');
      }

      var errorMsgLbl,
        totalPaid = 0,
        totalToPaid = OB.DEC.abs(
          isMultiOrder
            ? this.owner.model.get('multiOrders').getTotal()
            : this.owner.receipt.getTotal()
        ),
        isReturnOrder = isMultiOrder ? false : this.owner.receipt.isNegative(),
        paymentsMultiOrders,
        paymentsOrder,
        checkPrepaymentUnderTheLimit;

      if (isMultiOrder) {
        var receipts = this.owner.model
          .get('multiOrders')
          .get('multiOrdersList').models;
        receipts.forEach(function(receipt) {
          paymentsMultiOrders = _.filter(
            receipt.get('payments').models,
            function(payment) {
              return (
                (OB.UTIL.isNullOrUndefined(payment.get('isReturnOrder'))
                  ? isReturnOrder
                  : payment.get('isReturnOrder')) !== isReturnOrder
              );
            }
          );
          if (paymentsMultiOrders.length > 0) {
            return;
          }
        });
      }

      paymentsOrder = _.filter(payments.models, function(payment) {
        return (
          (OB.UTIL.isNullOrUndefined(payment.get('isReturnOrder'))
            ? isReturnOrder
            : payment.get('isReturnOrder')) !== isReturnOrder
        );
      });

      if (
        paymentsOrder.length > 0 ||
        (isMultiOrder && paymentsMultiOrders.length > 0)
      ) {
        this.avoidCompleteReceipt = true;
        if (isReturnOrder) {
          errorMsgLbl = 'OBPOS_PaymentOnReturnReceipt';
        } else {
          errorMsgLbl = 'OBPOS_NegativePaymentOnReceipt';
        }
      }
      payments.each(function(payment) {
        if (
          me.alreadyPaid &&
          !(payment.has('paymentRounding') && payment.get('paymentRounding'))
        ) {
          me.avoidCompleteReceipt = true;
          errorMsgLbl = 'OBPOS_UnnecessaryPaymentAdded';
          return false;
        }
        if (
          !payment.get('isReversePayment') &&
          !payment.get('isReversed') &&
          !payment.get('isPrePayment')
        ) {
          totalPaid = OB.DEC.add(totalPaid, payment.get('origAmount'));
          if (totalPaid >= totalToPaid) {
            me.alreadyPaid = true;
          }
        }
      });
      if (this.avoidCompleteReceipt) {
        OB.UTIL.ProcessController.finish('tapDoneButton', execution);
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel(errorMsgLbl)
        );
        return;
      }

      payments.each(function(payment) {
        if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
          me.allowOpenDrawer = true;
        }
      });

      checkPrepaymentUnderTheLimit = function(callback) {
        var paymentStatus,
          prepaymentLimitAmount,
          pendingPrepayment,
          receiptHasPrepaymentAmount = true;

        function underTheLimitApprovalCallback() {
          myModel.get('order').set('completeTicket', true);
          if (callback && callback instanceof Function) {
            callback();
          }
        }

        if (!isMultiOrder) {
          paymentStatus = me.owner.receipt.getPaymentStatus();
          prepaymentLimitAmount = me.owner.receipt.get(
            'obposPrepaymentlimitamt'
          );
          receiptHasPrepaymentAmount =
            me.owner.receipt.get('orderType') !== 1 &&
            me.owner.receipt.get('orderType') !== 3;
          pendingPrepayment = OB.DEC.sub(
            OB.DEC.add(prepaymentLimitAmount, paymentStatus.pendingAmt),
            paymentStatus.totalAmt
          );
        } else {
          paymentStatus = me.owner.model.get('multiOrders').getPaymentStatus();
          prepaymentLimitAmount = me.owner.model
            .get('multiOrders')
            .get('obposPrepaymentlimitamt');
          pendingPrepayment = OB.DEC.sub(
            OB.DEC.add(prepaymentLimitAmount, paymentStatus.pendingAmt),
            OB.DEC.add(
              paymentStatus.totalAmt,
              me.owner.model.get('multiOrders').get('existingPayment')
            )
          );
        }
        receiptHasPrepaymentAmount =
          receiptHasPrepaymentAmount && prepaymentLimitAmount !== 0;
        if (
          OB.MobileApp.model.get('terminal').terminalType
            .calculateprepayments &&
          receiptHasPrepaymentAmount &&
          paymentStatus.totalAmt > 0 &&
          pendingPrepayment > 0
        ) {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_AllowPrepaymentUnderLimit',
              true
            )
          ) {
            var approval;
            if (!isMultiOrder) {
              approval = _.find(me.owner.receipt.get('approvals'), function(
                approval
              ) {
                return (
                  approval.approvalType &&
                  (approval.approvalType.approval ===
                    'OBPOS_approval.prepaymentUnderLimit' ||
                    approval.approvalType ===
                      'OBPOS_approval.prepaymentUnderLimit')
                );
              });
            } else {
              approval = myModel
                .get('multiOrders')
                .get('multiOrdersList')
                .every(function(order) {
                  var approval = _.find(order.get('approvals'), function(
                    approval
                  ) {
                    return (
                      approval.approvalType &&
                      (approval.approvalType.approval ===
                        'OBPOS_approval.prepaymentUnderLimit' ||
                        approval.approvalType ===
                          'OBPOS_approval.prepaymentUnderLimit')
                    );
                  });
                  return approval;
                });
            }
            if (!approval) {
              OB.UTIL.Approval.requestApproval(
                me.model,
                [
                  {
                    approval: 'OBPOS_approval.prepaymentUnderLimit',
                    message: 'OBPOS_approval.prepaymentUnderLimit'
                  }
                ],
                function(approved, supervisor, approvalType) {
                  if (approved) {
                    if (
                      OB.MobileApp.model.get('context').user.id ===
                      supervisor.get('id')
                    ) {
                      OB.UTIL.showConfirmation.display(
                        OB.I18N.getLabel('OBPOS_UnderpaymentWarningTitle'),
                        OB.I18N.getLabel('OBPOS_UnderpaymentWarningBody'),
                        [
                          {
                            label: OB.I18N.getLabel('OBMOBC_LblOk'),
                            isConfirmButton: true,
                            action: function(popup) {
                              var approvals,
                                approval = {
                                  approvalType: {
                                    approval:
                                      'OBPOS_approval.prepaymentUnderLimit',
                                    message:
                                      'OBPOS_approval.prepaymentUnderLimit'
                                  },
                                  userContact: supervisor.get('id'),
                                  created: new Date().getTime()
                                };

                              if (
                                myModel.get('leftColumnViewManager').isOrder()
                              ) {
                                approvals =
                                  me.owner.receipt.get('approvals') || [];
                                approvals.push(approval);
                                me.owner.receipt.set('approvals', approvals);
                              } else {
                                myModel
                                  .get('multiOrders')
                                  .get('multiOrdersList')
                                  .forEach(function(order) {
                                    approvals = order.get('approvals') || [];
                                    approvals.push(approval);
                                    order.set('approvals', approvals);
                                  });
                              }

                              popup.doHideThisPopup();
                              underTheLimitApprovalCallback();
                            }
                          },
                          {
                            label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                            action: function(popup) {
                              OB.UTIL.ProcessController.finish(
                                'tapDoneButton',
                                execution
                              );
                              popup.doHideThisPopup();
                            }
                          }
                        ]
                      );
                    } else {
                      underTheLimitApprovalCallback();
                    }
                  } else {
                    OB.UTIL.ProcessController.finish(
                      'tapDoneButton',
                      execution
                    );
                  }
                }
              );
            } else {
              underTheLimitApprovalCallback();
            }
          } else {
            OB.UTIL.ProcessController.finish('tapDoneButton', execution);
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_PrepaymentUnderLimit_NotAllowed', [
                prepaymentLimitAmount
              ])
            );
          }
        } else {
          underTheLimitApprovalCallback();
        }
      };

      if (!isMultiOrder) {
        if (this.drawerpreference && me.allowOpenDrawer) {
          if (this.drawerOpened) {
            checkPrepaymentUnderTheLimit(function() {
              if (me.owner.receipt.get('orderType') === 3) {
                //Cancel Layaway
                OB.UTIL.ProcessController.finish('tapDoneButton', execution);
                me.owner.receipt.trigger('cancelLayaway');
              } else {
                OB.UTIL.ProcessController.finish('tapDoneButton', execution);
                OB.MobileApp.view.setOriginalScanMode(
                  OB.MobileApp.view.scanMode
                );
                OB.UTIL.setScanningFocus(false);
                me.owner.model.get('order').trigger('paymentDone', false);
              }
              me.drawerOpened = false;
            });
          } else {
            OB.POS.hwserver.openDrawer(
              {
                openFirst: true,
                receipt: me.owner.receipt
              },
              OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales
            );
            this.drawerOpened = true;
            this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
            OB.UTIL.ProcessController.finish('tapDoneButton', execution);
          }
        } else {
          checkPrepaymentUnderTheLimit(function() {
            if (me.owner.receipt.get('orderType') === 3) {
              //Cancel Layaway
              OB.UTIL.ProcessController.finish('tapDoneButton', execution);
              me.owner.receipt.trigger('cancelLayaway');
            } else {
              OB.UTIL.ProcessController.finish('tapDoneButton', execution);
              OB.MobileApp.view.setOriginalScanMode(OB.MobileApp.view.scanMode);
              OB.UTIL.setScanningFocus(false);
              me.owner.receipt.trigger('paymentDone', me.allowOpenDrawer);
            }
          });
        }
      } else {
        if (this.drawerpreference && me.allowOpenDrawer) {
          if (this.drawerOpened) {
            checkPrepaymentUnderTheLimit(function() {
              OB.UTIL.ProcessController.finish('tapDoneButton', execution);
              OB.MobileApp.view.setOriginalScanMode(OB.MobileApp.view.scanMode);
              OB.UTIL.setScanningFocus(false);
              me.owner.model.get('multiOrders').trigger('paymentDone', false);
              me.owner.model.get('multiOrders').set('openDrawer', false);
              me.drawerOpened = false;
              me.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
            });
          } else {
            OB.POS.hwserver.openDrawer(
              {
                openFirst: true,
                receipt: me.owner.model.get('multiOrders')
              },
              OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales
            );
            this.drawerOpened = true;
            this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
            OB.UTIL.ProcessController.finish('tapDoneButton', execution);
          }
        } else {
          checkPrepaymentUnderTheLimit(function() {
            OB.UTIL.ProcessController.finish('tapDoneButton', execution);
            OB.MobileApp.view.setOriginalScanMode(OB.MobileApp.view.scanMode);
            OB.UTIL.setScanningFocus(false);
            me.owner.model
              .get('multiOrders')
              .trigger('paymentDone', me.allowOpenDrawer);
            me.owner.model.get('multiOrders').set('openDrawer', false);
          });
        }
      }
    } else {
      OB.UTIL.ProcessController.finish('tapDoneButton', execution);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PrepaymentsDeliveryButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  events: {
    onDeliveryPayment: ''
  },
  i18nLabel: 'OBPOS_PrepaymentsDeliveryButtonLbl',
  classes: 'obObposPointOfSaleUiPrepaymentsDeliveryButton',
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'servicePriceCalculation'
  ],
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doDeliveryPayment();
  },
  initComponents: function() {
    this.inherited(arguments);
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
  i18nLabel: 'OBPOS_PrepaymentsExactButtonLbl',
  classes: 'obObposPointOfSaleUiPrepaymentsExactButton',
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'servicePriceCalculation'
  ],
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doExactPayment();
  },
  initComponents: function() {
    this.inherited(arguments);
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
  i18nLabelTotal: 'OBPOS_PayTotalLbl',
  i18nLabelRemaining: 'OBPOS_PayRemainingLbl',
  classes: 'obObposPointOfSaleUiExactButton',
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'servicePriceCalculation'
  ],
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.owner.checkDrawerPreference();
    this.doExactPayment();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine',
  classes: 'obObposPointOfSaleUiRenderPaymentLine',
  handlers: {
    onRenderPaymentLine: 'renderPaymentLine'
  },
  components: [
    {
      classes: 'obObposPointOfSaleUiRenderPaymentLine-container1',
      components: [
        {
          classes:
            'obObposPointOfSaleUiRenderPaymentLine-container1-container1',
          components: [
            {
              classes:
                'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container1',
              components: [
                {
                  name: 'name',
                  classes:
                    'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container1-name'
                },
                {
                  name: 'amount',
                  classes:
                    'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container1-amount'
                }
              ]
            },
            {
              classes:
                'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container2',
              components: [
                {
                  name: 'info',
                  classes:
                    'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container2-info'
                },
                {
                  name: 'foreignAmount',
                  classes:
                    'obObposPointOfSaleUiRenderPaymentLine-container1-container1-container2-foreignAmount'
                }
              ]
            }
          ]
        },
        {
          classes:
            'obObposPointOfSaleUiRenderPaymentLine-container1-container2',
          components: [
            {
              name: 'removePayment',
              kind: 'OB.OBPOSPointOfSale.UI.RemovePayment',
              classes:
                'obObposPointOfSaleUiRenderPaymentLine-container1-container2-removePayment',
              hideButton: function() {
                this.hide();
                this.parent.addClass('u-displayNone');
              }
            }
          ]
        },
        {
          classes:
            'obObposPointOfSaleUiRenderPaymentLine-container1-container3',
          components: [
            {
              name: 'reversePayment',
              kind: 'OB.OBPOSPointOfSale.UI.ReversePayment',
              classes:
                'obObposPointOfSaleUiRenderPaymentLine-container1-container3-reversePayment',
              hideButton: function() {
                this.hide();
                this.parent.addClass('u-displayNone');
              }
            }
          ]
        }
      ]
    }
  ],
  renderPaymentLine: function(inSender, inEvent) {
    const payment = OB.MobileApp.model.paymentnames[this.model.get('kind')],
      receipt = this.owner.owner.owner.owner.owner.owner.model.get('order');
    this.addClass('obObposPointOfSaleUiRenderPaymentLine_renderPaymentLine');
    if (this.model.get('reversedPaymentId')) {
      this.$.name.setContent(
        (OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
          this.model.get('name')) + OB.I18N.getLabel('OBPOS_ReversedPayment')
      );
    } else if (this.model.get('isReversed')) {
      this.$.name.setContent(
        '*' +
          (OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
            this.model.get('name'))
      );
    } else {
      if (this.model.get('isPrePayment') && !this.model.get('paymentAmount')) {
        this.$.name.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
      } else {
        this.$.name.setContent(
          OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
            this.model.get('name')
        );
      }
    }
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      if (this.model.get('reversedPaymentId') || this.model.get('isReversed')) {
        this.$.amount.setContent(this.model.printCurrencyForeignAmount());
        this.$.foreignAmount.setContent(
          '(' + this.model.printCurrencyAmount() + ')'
        );
      } else {
        this.$.amount.setContent(
          this.model.printCurrencyForeignAmountWithSignum(receipt)
        );
        this.$.foreignAmount.setContent(
          '(' + this.model.printCurrencyAmount(receipt) + ')'
        );
      }
    } else {
      if (this.model.get('reversedPaymentId') || this.model.get('isReversed')) {
        this.$.amount.setContent(this.model.printAmount());
      } else {
        this.$.amount.setContent(this.model.printAmountWithSignum(receipt));
      }
      this.$.foreignAmount.setContent('');
    }
    if (this.model.get('description')) {
      this.$.info.setContent(this.model.get('description'));
    } else {
      if (this.model.get('paymentData')) {
        //legacy
        if (this.model.get('paymentData').Name) {
          this.model.get('paymentData').name = this.model.get(
            'paymentData'
          ).Name;
        }
        //end legacy
        this.$.info.setContent(this.model.get('paymentData').name);
      } else {
        this.$.info.setContent('');
      }
    }
    if (this.$.foreignAmount.content || this.$.info.content) {
      this.$.removePayment.addClass(
        'obObposPointOfSaleUiRenderPaymentLine-container1-container2-removePayment_withContent'
      );
    }
    if (
      (!receipt.get('isEditable') &&
        !receipt.get('paidOnCredit') &&
        payment &&
        !payment.paymentMethod.isreversable) ||
      this.model.get('isReversed') ||
      (this.model.get('isPrePayment') &&
        (this.model.get('reversedPaymentId') ||
          !this.model.get('paymentAmount') ||
          this.owner.owner.owner.owner.model
            .get('order')
            .get('doCancelAndReplace')))
    ) {
      this.$.removePayment.hideButton();
      this.$.reversePayment.hideButton();
    } else if (
      this.model.has('paymentRounding') &&
      this.model.get('paymentRounding')
    ) {
      this.$.removePayment.hideButton();
      if (this.model.get('isPaid') && !this.model.get('isReversePayment')) {
        this.$.reversePayment.removeClass(
          'obObposPointOfSaleUiReversePayment_iconReversePayment'
        );
        this.$.reversePayment.removeClass('obObposPointOfSaleUiReversePayment');
        this.$.reversePayment.addClass(
          'obObposPointOfSaleUiReversePaymentRounding'
        );
      } else {
        this.$.reversePayment.hideButton();
      }
    } else if (
      this.model.get('isPrePayment') &&
      OB.MobileApp.model.hasPermission('OBPOS_EnableReversePayments', true)
    ) {
      this.$.removePayment.hideButton();
      this.$.reversePayment.show();
    } else if (
      this.model.get('isPrePayment') &&
      !OB.MobileApp.model.hasPermission('OBPOS_EnableReversePayments', true)
    ) {
      this.$.removePayment.hideButton();
      this.$.reversePayment.hideButton();
    } else {
      this.$.removePayment.show();
      this.$.reversePayment.hideButton();
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.renderPaymentLine();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RemovePayment',
  kind: 'OB.UI.Button',
  classes:
    'obObposPointOfSaleUiRemovePayment obObposPointOfSaleUiRemovePayment_iconClearPayment',
  i18nLabel: 'OBMOBC_Remove',
  events: {
    onRemovePayment: ''
  },
  putDisabled: function(status) {
    if (status === false) {
      this.setDisabled(false);
      this.removeClass('disabled');
      this.disabled = false;
      return;
    }
    this.setDisabled(true);
    this.addClass('disabled');
    this.disabled = true;
  },
  tap: function() {
    var me = this;
    if (_.isUndefined(this.deleting) || this.deleting === false) {
      this.deleting = true;
      this.putDisabled(true);
      this.removeClass('obObposPointOfSaleUiRemovePayment_iconClearPayment');
      this.addClass('obObposPointOfSaleUiRemovePayment_iconLoading');
      this.bubble('onMaxLimitAmountError', {
        show: false,
        maxLimitAmount: 0,
        currency: '',
        symbolAtRight: true
      });
      this.doRemovePayment({
        payment: this.owner.model,
        removeCallback: function() {
          me.deleting = false;
          me.putDisabled(false);
          me.removeClass('obObposPointOfSaleUiRemovePayment_iconLoading');
          me.addClass('obObposPointOfSaleUiRemovePayment_iconClearPayment');
        }
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ReversePayment',
  kind: 'OB.UI.Button',
  classes:
    'obObposPointOfSaleUiReversePayment obObposPointOfSaleUiReversePayment_iconReversePayment',
  events: {
    onReversePayment: ''
  },
  tap: function() {
    var me = this;
    if (
      me.owner.model.has('paymentRounding') &&
      me.owner.model.get('paymentRounding')
    ) {
      return;
    }
    if (
      OB.MobileApp.model.get('terminal').id !==
      me.owner.model.get('oBPOSPOSTerminal')
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_LblReverse'),
        OB.I18N.getLabel('OBPOS_CrossReversePayment', [
          me.owner.model.get('oBPOSPOSTerminalSearchKey')
        ]),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isDefaultAction: true
          }
        ]
      );
      return;
    }
    OB.UTIL.showConfirmation.display(
      OB.I18N.getLabel('OBPOS_LblReverse'),
      OB.I18N.getLabel('OBPOS_ReverseConfirm'),
      [
        {
          label: OB.I18N.getLabel('OBMOBC_LblCancel'),
          isDefaultAction: false
        },
        {
          label: OB.I18N.getLabel('OBPOS_LblOk'),
          isDefaultAction: true,
          action: function(popup) {
            if (_.isUndefined(me.deleting) || me.deleting === false) {
              me.deleting = true;
              me.removeClass(
                'obObposPointOfSaleUiReversePayment_iconReversePayment'
              );
              me.addClass('obObposPointOfSaleUiReversePayment_iconLoading');
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
                reverseCallback: function() {
                  me.deleting = false;
                  me.removeClass(
                    'obobObposPointOfSaleUiReversePayment_iconLoading'
                  );
                  me.addClass(
                    'obObposPointOfSaleUiReversePayment_iconReversePayment'
                  );
                }
              });
            }
          }
        }
      ]
    );
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.CreditButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  i18nLabel: 'OBPOS_LblSellOnCredit',
  classes: 'obObposPointOfSaleUiCreditButton',
  permission: 'OBPOS_receipt.creditsales',
  events: {
    onShowPopup: ''
  },
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'payOnCredit',
    'paymentDone',
    'servicePriceCalculation'
  ],
  init: function(model) {
    this.model = model;
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setDisabled(!OB.MobileApp.model.hasPermission(this.permission));
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    var execution = OB.UTIL.ProcessController.start('payOnCredit');

    OB.UTIL.ProcessController.finish('payOnCredit', execution);
    if (
      !_.isNull(this.model.get('order').get('bp')) &&
      _.isNull(
        this.model
          .get('order')
          .get('bp')
          .get('locId')
      )
    ) {
      OB.UTIL.ProcessController.finish('payOnCredit', execution);
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_InformationTitle'),
        OB.I18N.getLabel('OBPOS_EmptyAddrBillToText'),
        [
          {
            label: OB.I18N.getLabel('OBPOS_LblOk')
          }
        ]
      );
      return true;
    }
    // Checking blind returned lines
    if (!OB.MobileApp.model.get('terminal').returns_anonymouscustomer) {
      if (this.model.get('leftColumnViewManager').isOrder()) {
        if (this.owner.receipt.isAnonymousBlindReturn()) {
          OB.UTIL.ProcessController.finish('payOnCredit', execution);
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust')
          );
          return;
        }
      } else {
        var orderList = this.model.get('multiOrders').get('multiOrdersList');
        orderList.forEach(function(receipt) {
          if (receipt.isAnonymousBlindReturn()) {
            OB.UTIL.ProcessController.finish('payOnCredit', execution);
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_returnServicesWithAnonimousCust')
            );
            return;
          }
        });
      }
    }
    var me = this,
      paymentstatus = this.model.get('order').getPaymentStatus(),
      process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.CheckBusinessPartnerCredit'
      );
    if (!paymentstatus.isReturn) {
      //this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
      process.exec(
        {
          businessPartnerId: this.model
            .get('order')
            .get('bp')
            .get('id'),
          totalPending: paymentstatus.pendingAmt
        },
        function(data) {
          if (data) {
            if (data.enoughCredit) {
              OB.UTIL.ProcessController.finish('payOnCredit', execution);
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
              OB.UTIL.ProcessController.finish('payOnCredit', execution);
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
            OB.UTIL.ProcessController.finish('payOnCredit', execution);
          }
          me.setDisabled(false);
        },
        function() {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_AllowSellOnCreditWhileOffline',
              true
            )
          ) {
            OB.UTIL.ProcessController.finish('payOnCredit', execution);
            me.doShowPopup({
              popup: 'modalEnoughCredit',
              args: {
                order: me.model.get('order'),
                message: 'OBPOS_Unabletocheckcredit'
              }
            });
          } else {
            OB.UTIL.ProcessController.finish('payOnCredit', execution);
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_SellingOnCreditHeader'),
              OB.I18N.getLabel('OBPOS_UnabletoSellOncredit'),
              [
                {
                  isConfirmButton: true,
                  label: OB.I18N.getLabel('OBMOBC_LblOk')
                }
              ]
            );
          }
        }
      );
      //    } else if (this.model.get('order').get('orderType') === 1) {
    } else if (paymentstatus.isReturn) {
      OB.UTIL.ProcessController.finish('payOnCredit', execution);
      this.doShowPopup({
        popup: 'modalEnoughCredit',
        args: {
          order: this.model.get('order')
        }
      });
    } else {
      OB.UTIL.ProcessController.finish('payOnCredit', execution);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LayawayButton',
  kind: 'OB.OBPOSPointOfSale.UI.ProcessButton',
  content: '',
  classes: 'obObposPointOfSaleUiLayawayButton',
  permission: 'OBPOS_receipt.layawayReceipt',
  processesToListen: [
    'calculateReceipt',
    'showPaymentTab',
    'addProduct',
    'addPayment',
    'updatePending',
    'updatePendingMultiOrders',
    'paymentDone',
    'servicePriceCalculation'
  ],
  updateVisibility: function(isVisible) {
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
  init: function(model) {
    this.model = model;
    this.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
  },
  tap: function() {
    var receipt = this.owner.receipt,
      negativeLines,
      me = this,
      myModel = this.owner.model,
      paymentStatus,
      prepaymentLayawayLimitAmount,
      receiptHasPrepaymentAmount,
      pendingPrepayment,
      hasPayments,
      allowApproval;
    var continueExecuting = function(
      receipt,
      negativeLines,
      me,
      myModel,
      payments
    ) {
      if (
        !_.isNull(receipt.get('bp')) &&
        _.isNull(
          myModel
            .get('order')
            .get('bp')
            .get('locId')
        )
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_InformationTitle'),
          OB.I18N.getLabel('OBPOS_EmptyAddrBillToText'),
          [
            {
              label: OB.I18N.getLabel('OBPOS_LblOk')
            }
          ]
        );
        return;
      }

      me.allowOpenDrawer = false;

      if (
        receipt.get('bp').id ===
          OB.MobileApp.model.get('terminal').businessPartner &&
        !OB.MobileApp.model.get('terminal').layaway_anonymouscustomer
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_layawaysOrdersWithAnonimousCust')
        );
        return;
      }

      if (!me.showing || me.disabled) {
        return true;
      }

      if (receipt.get('lines').length === 0) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_AvoidLayawayWithoutLines'));
        return;
      }

      if (myModel.get('leftColumnViewManager').isOrder()) {
        payments = receipt.get('payments');
      } else {
        payments = myModel.get('multiOrders').get('payments');
      }

      payments.each(function(payment) {
        if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
          me.allowOpenDrawer = true;
        }
      });
      if (receipt) {
        negativeLines = _.find(receipt.get('lines').models, function(line) {
          return line.get('qty') < 0;
        });
        if (negativeLines) {
          if (
            !OB.MobileApp.model.hasPermission(
              'OBPOS_AllowLayawaysNegativeLines',
              true
            )
          ) {
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed')
            );
            return true;
          } else if (receipt.get('payment') > 0) {
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_partiallyLayawaysWithNegLinesNotAllowed')
            );
            return true;
          }
        }
        if (receipt.get('generateInvoice')) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_noInvoiceIfLayaway'));
          receipt.setFullInvoice(false, false);
        }
      }
      receipt.trigger('paymentDone', me.allowOpenDrawer);
    };

    paymentStatus = receipt.getPaymentStatus();
    prepaymentLayawayLimitAmount = receipt.get('obposPrepaymentlaylimitamt');
    receiptHasPrepaymentAmount = prepaymentLayawayLimitAmount !== 0;
    hasPayments = paymentStatus.payments.length > 0;
    allowApproval = OB.MobileApp.model.hasPermission(
      'OBPOS_AllowPrepaymentUnderLimitLayaway',
      true
    );
    pendingPrepayment = OB.DEC.sub(
      OB.DEC.add(prepaymentLayawayLimitAmount, paymentStatus.pendingAmt),
      paymentStatus.totalAmt
    );
    if (
      OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
      receiptHasPrepaymentAmount &&
      paymentStatus.totalAmt > 0 &&
      pendingPrepayment > 0 &&
      hasPayments &&
      allowApproval
    ) {
      OB.UTIL.Approval.requestApproval(
        this.model,
        [
          {
            approval: 'OBPOS_approval.prepaymentUnderLimitLayaway',
            message: 'OBPOS_approval.prepaymentUnderLimit'
          }
        ],
        function(approved, supervisor, approvalType) {
          if (approved) {
            if (
              OB.MobileApp.model.get('context').user.id === supervisor.get('id')
            ) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBPOS_UnderpaymentWarningTitle'),
                OB.I18N.getLabel('OBPOS_UnderpaymentWarningBody'),
                [
                  {
                    label: OB.I18N.getLabel('OBMOBC_LblOk'),
                    isConfirmButton: true,
                    action: function(popup) {
                      var approvals = me.owner.receipt.get('approvals') || [],
                        approval = {
                          approvalType: {
                            approval:
                              'OBPOS_approval.prepaymentUnderLimitLayaway',
                            message: 'OBPOS_approval.prepaymentUnderLimit'
                          },
                          userContact: supervisor.get('id'),
                          created: new Date().getTime()
                        };
                      approvals.push(approval);
                      me.owner.receipt.set('approvals', approvals);
                      popup.doHideThisPopup();
                      continueExecuting(
                        receipt,
                        negativeLines,
                        me,
                        myModel,
                        paymentStatus.payments
                      );
                    }
                  },
                  {
                    label: OB.I18N.getLabel('OBMOBC_LblCancel')
                  }
                ]
              );
            } else {
              continueExecuting(
                receipt,
                negativeLines,
                me,
                myModel,
                paymentStatus.payments
              );
            }
          }
        }
      );
    } else {
      if (
        !allowApproval &&
        prepaymentLayawayLimitAmount > receipt.getPayment()
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_PrepaymentUnderLimit_NotAllowed', [
            prepaymentLayawayLimitAmount
          ])
        );
      } else {
        continueExecuting(
          receipt,
          negativeLines,
          me,
          myModel,
          paymentStatus.payments
        );
      }
    }
  }
});

enyo.kind({
  kind: 'enyo.Component',
  name: 'OB.Payments.Change',
  classes: 'obPaymentsChange',
  statics: {
    getChangeRounded: function(change) {
      var precision, roundingto, roundinggap;

      if (change.payment.changeRounding) {
        precision = change.payment.obposPosprecision;
        roundingto = change.payment.changeRounding.roundingto;
        roundinggap = change.payment.changeRounding.roundingdownlimit;
        // Using 5 as rounding precision as a maximum precsion for all currencies before rounding.
        // And after rounding using Math.trunc using the payment precision.
        return OB.DEC.mul(
          roundingto,
          Math.trunc(
            OB.DEC.div(
              OB.DEC.add(
                change.amount,
                OB.DEC.sub(roundingto, roundinggap, 5),
                5
              ),
              roundingto,
              5
            )
          ),
          precision
        );
      }
      return change.amount;
    }
  },
  create: function() {
    this.inherited(arguments);
    this.label = '';
    this.payments = [];
  },
  add: function(change) {
    // change.payment is the payment of the new change to add
    // change.amount is the change to add in the payment currency
    // change.origAmount is the change to add in the document currency
    var formattedRounded, amountRounded, paymentLabel;

    if (OB.DEC.compare(change.origAmount)) {
      // Add new change Payment
      // Calculate amountRounded only in case it is not forced by caller
      amountRounded = _.isNumber(change.amountRounded)
        ? change.amountRounded
        : OB.Payments.Change.getChangeRounded(change);
      formattedRounded = OB.I18N.formatCurrencyWithSymbol(
        amountRounded,
        change.payment.symbol,
        change.payment.currencySymbolAtTheRight
      );
      if (
        OB.DEC.compare(
          OB.DEC.sub(
            change.amount,
            amountRounded,
            change.payment.obposPosprecision
          )
        )
      ) {
        paymentLabel = OB.I18N.getLabel('OBPOS_OriginalAmount', [
          formattedRounded,
          OB.I18N.formatCurrencyWithSymbol(
            change.amount,
            change.payment.symbol,
            change.payment.currencySymbolAtTheRight
          )
        ]);
      } else {
        paymentLabel = formattedRounded;
      }
      if (this.label) {
        this.label += ' + ';
      }
      this.label += formattedRounded;
      this.payments.push({
        key: change.payment.payment.searchKey,
        amount: change.amount,
        amountRounded: amountRounded,
        origAmount: change.origAmount,
        label: paymentLabel
      });
    }
  }
});
