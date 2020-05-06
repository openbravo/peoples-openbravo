/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderCashPaymentsLine',
  classes: 'obObposCashUpUiRenderCashPaymentsLine',
  statics: {
    getLegacyCoins: function() {
      return new Backbone.Collection([
        {
          amount: 0.01,
          backcolor: '#f3bc9e'
        },
        {
          amount: 0.02,
          backcolor: '#f3bc9e'
        },
        {
          amount: 0.05,
          backcolor: '#f3bc9e'
        },
        {
          amount: 0.1,
          backcolor: '#f9e487'
        },
        {
          amount: 0.2,
          backcolor: '#f9e487'
        },
        {
          amount: 0.5,
          backcolor: '#f9e487'
        },
        {
          amount: 1,
          backcolor: '#e4e0e3',
          bordercolor: '#f9e487'
        },
        {
          amount: 2,
          backcolor: '#f9e487',
          bordercolor: '#e4e0e3'
        },
        {
          amount: 5,
          backcolor: '#bccdc5'
        },
        {
          amount: 10,
          backcolor: '#e9b7c3'
        },
        {
          amount: 20,
          backcolor: '#bac3de'
        },
        {
          amount: 50,
          backcolor: '#f9bb92'
        }
      ]);
    }
  },
  events: {
    onLineEditCash: '',
    onAddUnit: '',
    onSubUnit: '',
    onUpdateUnit: ''
  },
  handlers: {
    onNumberChange: 'numberChange',
    onNumberFocus: 'numberFocus'
  },
  components: [
    {
      classes: 'obObposCashUpUiRenderCashPaymentsLine-listItem',
      components: [
        {
          classes:
            'obObposCashUpUiRenderCashPaymentsLine-listItem-coinComponent',
          components: [
            {
              name: 'coin',
              kind: 'OB.UI.Button',
              avoidDoubleClick: false,
              classes:
                'obObposCashUpUiRenderCashPaymentsLine-listItem-coinComponent-coin',
              ontap: 'addUnit'
            }
          ]
        },
        {
          classes:
            'obObposCashUpUiRenderCashPaymentsLine-listItem-numberOfCoinsComponent',
          components: [
            {
              kind: 'OB.UI.FormElement',
              name: 'formElementNumberOfCoins',
              classes:
                'obObposCashUpUiRenderCashPaymentsLine-listItem-numberOfCoinsComponent-formElementNumberlinesQty',
              coreElement: {
                kind: 'OB.UI.FormElement.IntegerEditor',
                name: 'numberOfCoins',
                min: 0,
                forceNumberChangeAlways: true /* Ideally it should be true, but then the focus is lost each keypress due to changes in the model */,
                classes:
                  'obObposCashUpUiRenderCashPaymentsLine-listItem-numberOfCoinsComponent-numberlinesQty',
                i18nLabel: 'OBPOS_NumberOfItems'
              }
            }
          ]
        },
        {
          name: 'total',
          classes: 'obObposCashUpUiRenderCashPaymentsLine-listItem-total'
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.coin.setContent(OB.I18N.formatCurrency(this.model.get('coinValue')));
    //This inline style is allowed
    var style = '';
    if (this.model.get('bordercolor')) {
      style += ' border-color: ' + this.model.get('bordercolor') + ';';
    } else {
      style += ' border-color: ' + this.model.get('backcolor') + ';';
    }
    style += ' background-color: ' + this.model.get('backcolor') + ';';
    this.$.coin.addStyles(style);
    if (
      this.$.formElementNumberOfCoins.coreElement.getValue().toString() !==
      this.model.get('numberOfCoins').toString()
    ) {
      this.$.formElementNumberOfCoins.coreElement.setValue(
        this.model.get('numberOfCoins')
      );
    }
    this.$.total.setContent(
      OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount')))
    );
  },
  render: function() {
    var counted;
    this.inherited(arguments);
    counted = this.model.get('numberOfCoins');
    if (counted !== null && counted !== undefined) {
      if (
        this.$.formElementNumberOfCoins.coreElement.getValue().toString() !==
        counted.toString()
      ) {
        this.$.formElementNumberOfCoins.coreElement.setValue(counted);
      }
      this.$.total.setContent(
        OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount')))
      );
    }
    return this;
  },
  lineEditCash: function() {
    this.doLineEditCash();
  },
  addUnit: function() {
    this.doAddUnit();
  },
  subUnit: function() {
    this.doSubUnit();
  },
  numberChange: function(inSender, inEvent) {
    if (this.model.get('numberOfCoins') !== inEvent.value) {
      inEvent.originator = this;
      this.doUpdateUnit(inEvent);
    }
  },
  numberFocus: function(inSender, inEvent) {
    inEvent.originator = this;
    this.doLineEditCash(inEvent);
    this.$.formElementNumberOfCoins.inputField.$.numberQty.selectContents();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderTotal',
  classes: 'obObposCashupUiRenderTotal',
  printAmount: function(value) {
    this.setContent(OB.I18N.formatCurrency(value));
    if (OB.DEC.compare(value) < 0) {
      this.removeClass('obObposCashupUiRenderTotal_positive');
      this.addClass('obObposCashupUiRenderTotal_negative');
    } else {
      this.removeClass('obObposCashupUiRenderTotal_negative');
      this.addClass('obObposCashupUiRenderTotal_positive');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashPayments',
  classes: 'obObposCashupUiCashPayments',
  handlers: {
    onAddUnit: 'addUnit',
    onSubUnit: 'subUnit',
    onUpdateUnit: 'updateUnit',
    onLineEditCash: 'lineEditCash'
  },
  components: [
    {
      classes: 'obObposCashupUiCashPayments-wrapper',
      components: [
        {
          classes: 'obObposCashupUiCashPayments-wrapper-components',
          components: [
            {
              name: 'title',
              classes: 'obObposCashupUiCashPayments-wrapper-components-title',
              renderHeader: function(value, step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepCashPayments', [value]) +
                    OB.OBPOSCashUp.UI.CashUp.getTitleExtensions()
                );
              }
            },
            {
              classes: 'obObposCashupUiCashPayments-wrapper-components-body',
              components: [
                {
                  classes:
                    'obObposCashupUiCashPayments-wrapper-components-body-header',
                  components: [
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-header-element1',
                      initComponents: function() {
                        this.setContent(OB.I18N.getLabel('OBPOS_CoinType'));
                      }
                    },
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-header-element2',
                      initComponents: function() {
                        this.setContent(
                          OB.I18N.getLabel('OBPOS_NumberOfItems')
                        );
                      }
                    },
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-header-element3',
                      initComponents: function() {
                        this.setContent(OB.I18N.getLabel('OBPOS_AmountOfCash'));
                      }
                    }
                  ]
                },
                {
                  classes:
                    'obObposCashupUiCashPayments-wrapper-components-body-list',
                  components: [
                    {
                      name: 'paymentsList',
                      kind: 'OB.UI.ScrollableTable',
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-list-paymentsList',
                      renderLine: 'OB.OBPOSCashUp.UI.RenderCashPaymentsLine',
                      renderEmpty: 'OB.UI.RenderEmpty',
                      listStyle: 'list'
                    },
                    {
                      name: 'renderLoading',
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-list-renderLoading',
                      showing: false,
                      initComponents: function() {
                        this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
                      }
                    }
                  ]
                },
                {
                  classes:
                    'obObposCashupUiCashPayments-wrapper-components-body-footer',
                  components: [
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-footer-container1',
                      components: [
                        {
                          name: 'totalLbl',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container1-totalLbl',
                          initComponents: function() {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_LblExpected')
                            );
                          }
                        },
                        {
                          name: 'total',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container1-total',
                          kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                        }
                      ]
                    },
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-footer-container2',
                      components: [
                        {
                          name: 'countedLbl',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container2-countedLbl',
                          initComponents: function() {
                            this.setContent(OB.I18N.getLabel('OBPOS_Counted'));
                          }
                        },
                        {
                          name: 'counted',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container2-counted',
                          kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                        }
                      ]
                    },
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-footer-container4'
                    },
                    {
                      classes:
                        'obObposCashupUiCashPayments-wrapper-components-body-footer-container3',
                      components: [
                        {
                          name: 'differenceLbl',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container3-differenceLbl',
                          initComponents: function() {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_Remaining')
                            );
                          }
                        },
                        {
                          name: 'difference',
                          classes:
                            'obObposCashupUiCashPayments-wrapper-components-body-footer-container3-difference',
                          kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                        }
                      ]
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
  init: function(model) {
    this.inherited(arguments);

    this.model = model;

    this.model.on(
      'action:resetAllCoins',
      function(args) {
        this.resetAllCoins();
      },
      this
    );

    this.model.on(
      'action:SelectCoin',
      function(args) {
        this.selectCoin(args);
      },
      this
    );

    if (OB.MobileApp.model.get('useBarcode')) {
      OB.UTIL.setScanningFocus(true);
    }
  },
  printTotals: function() {
    this.$.counted.printAmount(this.payment.get('foreignCounted'));
    this.$.difference.printAmount(this.payment.get('foreignDifference'));
    if (this.payment.get('foreignDifference') <= 0) {
      this.$.differenceLbl.setContent(OB.I18N.getLabel('OBPOS_Remaining'));
    } else {
      this.$.differenceLbl.setContent(OB.I18N.getLabel('OBPOS_Surplus'));
    }
  },

  lineEditCash: function(inSender, inEvent) {
    this.setCoinsStatus(inEvent.originator);
  },

  setCoinsStatus: function(originator) {
    // reset previous status
    if (this.originator && this.originator.$.formElementNumberOfCoins) {
      this.originator.$.formElementNumberOfCoins.removeClass(
        'obObposCashUpUiRenderCashPaymentsLine-listItem-numberOfCoinsComponent-formElementNumberlinesQty_activeInKeypad'
      );
    }

    // set new status
    if (originator && originator !== this.originator) {
      this.originator = originator;
      this.originator.$.formElementNumberOfCoins.addClass(
        'obObposCashUpUiRenderCashPaymentsLine-listItem-numberOfCoinsComponent-formElementNumberlinesQty_activeInKeypad'
      );
      this.model.trigger('action:SetStatusCoin');
    } else {
      this.originator = null;
      this.model.trigger('action:ResetStatusCoin');
    }
  },
  selectCoin: function(args) {
    // args -> {keyboard: keyboard, txt: txt});
    if (this.originator) {
      // This function also resets the status
      this.addUnitToCollection(
        this.originator.model.get('coinValue'),
        parseInt(args.txt, 10)
      );
    }
  },
  addUnit: function(inSender, inEvent) {
    this.addUnitToCollection(inEvent.originator.model.get('coinValue'), 'add');
  },
  subUnit: function(inSender, inEvent) {
    this.addUnitToCollection(inEvent.originator.model.get('coinValue'), 'sub');
  },
  updateUnit: function(inSender, inEvent) {
    this.addUnitToCollection(
      inEvent.originator.model.get('coinValue'),
      inEvent.value
    );
  },
  addUnitToCollection: function(coinValue, amount) {
    var collection = this.$.paymentsList.collection;
    var lAmount, resetAmt, newAmount;

    if (amount === 'add') {
      lAmount = 1;
      resetAmt = false;
    } else if (amount === 'sub') {
      lAmount = -1;
      resetAmt = false;
    } else {
      lAmount = amount;
      resetAmt = true;
    }

    var totalCounted = 0;
    collection.each(function(coin) {
      if (coin.get('coinValue') === coinValue) {
        if (resetAmt) {
          newAmount = lAmount;
        } else {
          newAmount = coin.get('numberOfCoins') + lAmount;
        }
        if (newAmount >= 0) {
          coin.set('numberOfCoins', newAmount);
        }
      }
      coin.set(
        'totalAmount',
        OB.DEC.mul(coin.get('numberOfCoins'), coin.get('coinValue'))
      );
      totalCounted = OB.DEC.add(totalCounted, coin.get('totalAmount'));
    });
    this.payment.set('foreignCounted', totalCounted);
    var cTotalCounted = OB.UTIL.currency.toDefaultCurrency(
      this.payment.attributes.paymentMethod.currency,
      totalCounted
    );
    this.payment.set('counted', cTotalCounted);
    this.payment.set(
      'foreignDifference',
      OB.DEC.sub(totalCounted, this.payment.get('foreignExpected'))
    );
    this.printTotals();

    this.setCoinsStatus(null);
  },

  resetAllCoins: function() {
    var collection = this.$.paymentsList.collection;

    collection.each(function(coin) {
      coin.set('numberOfCoins', 0);
      coin.set('totalAmount', 0);
    });

    this.payment.set('foreignCounted', 0);
    this.payment.set('counted', 0);
    this.payment.set(
      'foreignDifference',
      OB.DEC.sub(0, this.payment.get('foreignExpected'))
    );
    this.printTotals();

    this.setCoinsStatus(null);
  },

  initPaymentToCount: function(payment) {
    this.payment = payment;

    this.$.title.renderHeader(
      payment.get('name'),
      this.model.stepNumber('OB.CashUp.CashPayments'),
      this.model.stepCount()
    );

    this.$.total.printAmount(this.payment.get('foreignExpected'));

    if (!this.payment.get('coinsCollection')) {
      this.$.paymentsList.hide();
      this.$.renderLoading.show();

      // First empty collection before loading.
      this.$.paymentsList.setCollection(new Backbone.Collection());
      this.payment.set('foreignCounted', 0);
      this.payment.set('counted', 0);
      this.payment.set(
        'foreignDifference',
        OB.DEC.sub(0, this.payment.get('foreignExpected'))
      );
      this.printTotals();

      this.setCoinsStatus(null);

      // Call to draw currencies.
      var currencyId = payment.get('paymentMethod').currency;
      var me = this;
      OB.Dal.find(
        OB.Model.CurrencyPanel,
        {
          currency: currencyId,
          _orderByClause: 'line'
        },
        function(coins) {
          var coinCol = new Backbone.Collection();

          if (
            coins.length === 0 &&
            payment.get('paymentMethod').currency === '102'
          ) {
            coins = OB.OBPOSCashUp.UI.RenderCashPaymentsLine.getLegacyCoins();
          }

          coins.each(function(coin) {
            var coinModel = new Backbone.Model();
            coinModel.set('numberOfCoins', 0);
            coinModel.set('totalAmount', 0);
            coinModel.set('coinValue', coin.get('amount'));
            coinModel.set('backcolor', coin.get('backcolor'));
            coinModel.set('bordercolor', coin.get('bordercolor'));
            coinCol.add(coinModel);
          });

          me.payment.set('coinsCollection', coinCol);
          me.$.paymentsList.setCollection(coinCol);
          me.payment.set('foreignCounted', 0);
          me.payment.set('counted', 0);
          me.payment.set(
            'foreignDifference',
            OB.DEC.sub(0, me.payment.get('foreignExpected'))
          );
          me.printTotals();

          me.setCoinsStatus(null);

          me.$.renderLoading.hide();
          me.$.paymentsList.show();
        }
      );
    } else {
      this.$.paymentsList.setCollection(this.payment.get('coinsCollection'));
      this.printTotals();

      this.setCoinsStatus(null);
    }
  },
  displayStep: function(model) {
    this.model = model;
    var payment = model.get('paymentList').at(model.get('substep'));

    // If the cashier is not trusty, hide expected and total amount that should be.
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.$.total.hide();
      this.$.totalLbl.hide();
      this.$.difference.hide();
      this.$.differenceLbl.hide();
    } else {
      this.$.total.show();
      this.$.totalLbl.show();
      this.$.difference.show();
      this.$.differenceLbl.show();
    }

    // this function is invoked when displayed.
    this.initPaymentToCount(payment);

    // Open drawer if allow open drawer. Already a cash method.
    if (payment.get('paymentMethod').allowopendrawer) {
      OB.POS.hwserver.openDrawer(
        false,
        OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
      );
    }
  }
});
