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
    getLegacyCoins: function () {
      return new Backbone.Collection([{
        amount: 0.01,
        backcolor: '#f3bc9e'
      }, {
        amount: 0.02,
        backcolor: '#f3bc9e'
      }, {
        amount: 0.05,
        backcolor: '#f3bc9e'
      }, {
        amount: 0.10,
        backcolor: '#f9e487'
      }, {
        amount: 0.20,
        backcolor: '#f9e487'
      }, {
        amount: 0.50,
        backcolor: '#f9e487'
      }, {
        amount: 1,
        backcolor: '#e4e0e3',
        bordercolor: '#f9e487'
      }, {
        amount: 2,
        backcolor: '#f9e487',
        bordercolor: '#e4e0e3'
      }, {
        amount: 5,
        backcolor: '#bccdc5'
      }, {
        amount: 10,
        backcolor: '#e9b7c3'
      }, {
        amount: 20,
        backcolor: '#bac3de'
      }, {
        amount: 50,
        backcolor: '#f9bb92'
      }]);
    }
  },
  events: {
    onLineEditCash: '',
    onAddUnit: '',
    onSubUnit: ''
  },
  components: [{
    classes: 'obObposCashUpUiRenderCashPaymentsLine-container1',
    components: [{
      classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1 row-fluid',
      components: [{
        classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1 span12',
        components: [{
          name: 'coin',
          kind: 'OB.UI.MediumButton',
          avoidDoubleClick: false,
          classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-coin',
          ontap: 'addUnit'
        }, {
          name: 'qtyminus',
          kind: 'OB.UI.SmallButton',
          avoidDoubleClick: false,
          classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-qtyminus',
          content: '-',
          ontap: 'subUnit'
        }, {
          name: 'numberOfCoins',
          kind: 'OB.UI.MediumButton',
          classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-numberOfCoins',
          ontap: 'lineEdit'
        }, {
          name: 'qtyplus',
          kind: 'OB.UI.SmallButton',
          avoidDoubleClick: false,
          classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-qtyplus',
          content: '+',
          ontap: 'addUnit'
        }, {
          name: 'total',
          classes: 'obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-total'
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.coin.setContent(OB.I18N.formatCurrency(this.model.get('coinValue')));
    //This inline style is allowed
    var style = '';
    if (this.model.get('bordercolor')) {
      style += ' border:6px solid ' + this.model.get('bordercolor') + ';';
    }
    style += ' background-color:' + this.model.get('backcolor') + ';';
    this.$.coin.addStyles(style);
    this.$.numberOfCoins.setContent(this.model.get('numberOfCoins'));
    this.$.total.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount'))));
  },
  render: function () {
    var counted;
    this.inherited(arguments);
    counted = this.model.get('numberOfCoins');
    if (counted !== null && counted !== undefined) {
      this.$.numberOfCoins.setContent(counted);
      this.$.total.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount'))));
      this.adjustFontSize();
    }
    return this;
  },
  lineEdit: function () {
    this.doLineEditCash();
  },
  addUnit: function () {
    this.doAddUnit();
  },
  subUnit: function () {
    this.doSubUnit();
  },
  adjustFontSize: function () {
    var contentLengthCoins = this.$.numberOfCoins.getContent().toString().length;
    var contentLengthTotal = this.$.total.getContent().toString().length;
    var newFontSize = 16;
    if (contentLengthCoins >= 15) {
      newFontSize = 7;
    } else if (contentLengthCoins >= 11) {
      newFontSize = 8;
    } else if (contentLengthCoins >= 8) {
      newFontSize = 10;
    }
    var newStyle = "font-size: " + newFontSize + "px;";
    this.$.numberOfCoins.setStyle(newStyle);
    newFontSize = 16;
    if (contentLengthTotal > 18) {
      newFontSize = 12;
    }
    newStyle = "font-size: " + newFontSize + "px;";
    this.$.total.setStyle(newStyle);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderTotal',
  classes: 'obObposCashupUiRenderTotal',
  tag: 'span',
  printAmount: function (value) {
    this.setContent(OB.I18N.formatCurrency(value));
    this.addClass(OB.DEC.compare(value) < 0 ? 'obObposCashupUiRenderTotal_negative' : 'obObposCashupUiRenderTotal_positive');
    var contentLength = this.getContent().length;
    var newFontSize = 16;
    if (contentLength > 21) {
      newFontSize = 7;
    } else if (contentLength > 15) {
      newFontSize = 10;
    } else if (contentLength > 11) {
      newFontSize = 14;
    }
    this.applyStyle('font-size', newFontSize + 'px');
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashPayments',
  classes: 'obObposCashupUiCashPayments',
  handlers: {
    onAddUnit: 'addUnit',
    onSubUnit: 'subUnit',
    onLineEditCash: 'lineEditCash'
  },
  components: [{
    classes: 'obObposCashupUiCashPayments-container1',
    components: [{
      classes: 'obObposCashupUiCashPayments-container1-container1',
      components: [{
        classes: 'obObposCashupUiCashPayments-container1-container1-container1',
        components: [{
          classes: 'obObposCashupUiCashPayments-container1-container1-container1-container1 row-fluid',
          components: [{
            classes: 'obObposCashupUiCashPayments-container1-container1-container1-container1-container1 span12',
            components: [{
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              name: 'title',
              classes: 'obObposCashupUiCashPayments-container1-container1-container1-container1-container1-title',
              renderHeader: function (value, step, count) {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) + " " + OB.I18N.getLabel('OBPOS_LblStepCashPayments', [value]) + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
              }
            }]
          }]
        }, {
          classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2',
          components: [{
            classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1',
            components: [{
              classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container1 row-fluid',
              components: [{
                classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container1-container1 span12',
                components: [{
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container1-container1-element1',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_CoinType'));
                  }
                }, {
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container1-container1-element2',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_NumberOfItems'));
                  }
                }, {
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container1-container1-element3',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_AmountOfCash'));
                  }
                }]
              }]
            }, {
              classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container2',
              components: [{
                name: 'paymentsList',
                kind: 'OB.UI.ScrollableTable',
                classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container2-paymentsList',
                renderLine: 'OB.OBPOSCashUp.UI.RenderCashPaymentsLine',
                renderEmpty: 'OB.UI.RenderEmpty',
                scrollAreaMaxHeight: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container2-paymentsList-scrollArea',
                listStyle: 'list'
              }, {
                name: 'renderLoading',
                classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container2-renderLoading',
                showing: false,
                initComponents: function () {
                  this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
                }
              }]
            }, {
              classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3 row-fluid',
              components: [{
                classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1',
                components: [{
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container1',
                  components: [{
                    name: 'totalLbl',
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container1-totalLbl',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
                    }
                  }, {
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container1-container1',
                    components: [{
                      name: 'total',
                      classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container1-container1-total',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }, {
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container2',
                  components: [{
                    name: 'countedLbl',
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container2-countedLbl',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_Counted'));
                    }
                  }, {
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container2-container1',
                    components: [{
                      name: 'counted',
                      classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container2-container1-counted',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }, {
                  classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container3',
                  components: [{
                    name: 'differenceLbl',
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container3-differenceLbl',
                    style: 'padding: 10px 20px 10px 10px; display: table-cell;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_Remaining'));
                    }
                  }, {
                    classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container3-container1',
                    components: [{
                      name: 'difference',
                      classes: 'obObposCashupUiCashPayments-container1-container1-container1-container2-container1-container3-container1-container3-container1-difference',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }]
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  init: function (model) {
    this.inherited(arguments);

    this.model = model;

    this.model.on('action:resetAllCoins', function (args) {
      this.resetAllCoins();
    }, this);

    this.model.on('action:SelectCoin', function (args) {
      this.selectCoin(args);
    }, this);
  },
  printTotals: function () {
    this.$.counted.printAmount(this.payment.get('foreignCounted'));
    this.$.difference.printAmount(this.payment.get('foreignDifference'));
  },

  lineEditCash: function (inSender, inEvent) {
    this.setCoinsStatus(inEvent.originator);
  },

  setCoinsStatus: function (originator) {

    // reset previous status  
    if (this.originator && this.originator.$.numberOfCoins) {
      this.originator.$.numberOfCoins.removeClass('obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-numberOfCoins_mainColor');
    }

    // set new status
    if (originator && originator !== this.originator) {
      this.originator = originator;
      this.originator.$.numberOfCoins.addClass('obObposCashUpUiRenderCashPaymentsLine-container1-container1-container1-numberOfCoins_mainColor');
      this.model.trigger('action:SetStatusCoin');
    } else {
      this.originator = null;
      this.model.trigger('action:ResetStatusCoin');
    }
  },
  selectCoin: function (args) {
    // args -> {keyboard: keyboard, txt: txt});
    if (this.originator) {
      // This function also resets the status
      this.addUnitToCollection(this.originator.model.get('coinValue'), parseInt(args.txt, 10));
    }
  },
  addUnit: function (inSender, inEvent) {
    this.addUnitToCollection(inEvent.originator.model.get('coinValue'), 'add');
  },
  subUnit: function (inSender, inEvent) {
    this.addUnitToCollection(inEvent.originator.model.get('coinValue'), 'sub');
  },
  addUnitToCollection: function (coinValue, amount) {
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
    collection.each(function (coin) {
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
      coin.set('totalAmount', OB.DEC.mul(coin.get('numberOfCoins'), coin.get('coinValue')));
      totalCounted = OB.DEC.add(totalCounted, coin.get('totalAmount'));
    });
    this.payment.set('foreignCounted', totalCounted);
    var cTotalCounted = OB.UTIL.currency.toDefaultCurrency(this.payment.attributes.paymentMethod.currency, totalCounted);
    this.payment.set('counted', cTotalCounted);
    this.payment.set('foreignDifference', OB.DEC.sub(totalCounted, this.payment.get('foreignExpected')));
    this.printTotals();

    this.setCoinsStatus(null);
  },

  resetAllCoins: function () {
    var collection = this.$.paymentsList.collection;

    collection.each(function (coin) {
      coin.set('numberOfCoins', 0);
      coin.set('totalAmount', 0);
    });

    this.payment.set('foreignCounted', 0);
    this.payment.set('counted', 0);
    this.payment.set('foreignDifference', OB.DEC.sub(0, this.payment.get('foreignExpected')));
    this.printTotals();

    this.setCoinsStatus(null);
  },

  initPaymentToCount: function (payment) {
    this.payment = payment;

    this.$.title.renderHeader(payment.get('name'), this.model.stepNumber('OB.CashUp.CashPayments'), this.model.stepCount());

    this.$.total.printAmount(this.payment.get('foreignExpected'));

    if (!this.payment.get('coinsCollection')) {
      this.$.paymentsList.hide();
      this.$.renderLoading.show();

      // First empty collection before loading.
      this.$.paymentsList.setCollection(new Backbone.Collection());
      this.payment.set('foreignCounted', 0);
      this.payment.set('counted', 0);
      this.payment.set('foreignDifference', OB.DEC.sub(0, this.payment.get('foreignExpected')));
      this.printTotals();

      this.setCoinsStatus(null);

      // Call to draw currencies.
      var currencyId = payment.get('paymentMethod').currency;
      var me = this;
      OB.Dal.find(OB.Model.CurrencyPanel, {
        currency: currencyId,
        _orderByClause: 'line'
      }, function (coins) {
        var coinCol = new Backbone.Collection();

        if (coins.length === 0 && payment.get('paymentMethod').currency === '102') {
          coins = OB.OBPOSCashUp.UI.RenderCashPaymentsLine.getLegacyCoins();
        }

        coins.each(function (coin) {
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
        me.payment.set('foreignDifference', OB.DEC.sub(0, me.payment.get('foreignExpected')));
        me.printTotals();

        me.setCoinsStatus(null);

        me.$.renderLoading.hide();
        me.$.paymentsList.show();
      });
    } else {
      this.$.paymentsList.setCollection(this.payment.get('coinsCollection'));
      this.printTotals();

      this.setCoinsStatus(null);
    }

  },
  displayStep: function (model) {
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
      OB.POS.hwserver.openDrawer(false, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount);
    }
  }
});