/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderCashPaymentsLine',
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
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        style: 'border-bottom: 1px solid #cccccc;',
        components: [{
          name: 'coin',
          kind: 'OB.UI.MediumButton',
          classes: 'btnlink-gray btnlink-cashup-edit',
          ontap: 'addUnit'
        }, {
          style: 'display:inline-block; width: 10%'
        }, {
          name: 'qtyminus',
          kind: 'OB.UI.SmallButton',
          style: 'width: 40px;',
          classes: 'btnlink-gray btnlink-cashup-edit',
          content: '-',
          ontap: 'subUnit'
        }, {
          name: 'numberOfCoins',
          kind: 'OB.UI.MediumButton',
          classes: 'btnlink-gray btnlink-cashup-edit',
          style: 'background-color: white; border: 1px solid lightgray; border-radius: 3px;',
          ontap: 'lineEdit'
        }, {
          name: 'qtyplus',
          kind: 'OB.UI.SmallButton',
          style: 'width: 40px;',
          classes: 'btnlink-gray btnlink-cashup-edit',
          content: '+',
          ontap: 'addUnit'
        }, {
          style: 'display:inline-block; width: 10%'
        }, {
          name: 'total',
          style: 'display:inline-block;padding: 10px 0px 10px 0px; width: 15%; text-align: center;'
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.coin.setContent(OB.I18N.formatCurrency(this.model.get('coinValue')));
    var style = 'float: left; width: 15%; text-align: center;';
    if (this.model.get('bordercolor')) {
      style += ' border:10px solid ' + this.model.get('bordercolor') + ';';
    }
    style += ' background-color:' + this.model.get('backcolor') + ';';
    this.$.coin.addStyles(style);
    this.$.numberOfCoins.setContent(this.model.get('numberOfCoins'));
    this.$.total.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount'))));
  },
  render: function () {
    var udfn, counted, foreignCounted;
    this.inherited(arguments);
    counted = this.model.get('numberOfCoins');
    if (counted !== null && counted !== udfn) {
      this.$.numberOfCoins.setContent(counted);
      this.$.total.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalAmount'))));
    }
  },
  lineEdit: function () {
    this.doLineEditCash();
  },
  addUnit: function () {
    this.doAddUnit();
  },
  subUnit: function () {
    this.doSubUnit();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderTotal',
  tag: 'span',
  style: 'font-weight: bold;',
  printAmount: function (value) {
    this.setContent(OB.I18N.formatCurrency(value));
    this.applyStyle('color', OB.DEC.compare(value) < 0 ? 'red' : 'black');
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashPayments',
  handlers: {
    onAddUnit: 'addUnit',
    onSubUnit: 'subUnit',
    onLineEditCash: 'lineEditCash'
  },
  components: [{
    classes: 'tab-pane',
    components: [{
      style: 'margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              name: 'title'
            }]
          }]
        }, {
          style: 'background-color: #ffffff; color: black;',
          components: [{
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc;',
                components: [{
                  style: 'padding: 10px 20px 10px 10px; float: left; width: 30%',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_CoinType'));
                  }
                }, {
                  style: 'padding: 10px 20px 10px 0px; float: left; width: 30%',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_NumberOfItems'));
                  }
                }, {
                  style: 'padding: 10px 0px 10px 0px;  float: left; width: 25%',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_AmountOfCash'));
                  }
                }]
              }]
            }, {
              style: 'overflow:auto; height: 454px;',
              components: [{
                name: 'paymentsList',
                kind: 'OB.UI.Table',
                renderLine: 'OB.OBPOSCashUp.UI.RenderCashPaymentsLine',
                renderEmpty: 'OB.UI.RenderEmpty',
                listStyle: 'list'
              }, {
                name: 'renderLoading',
                style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
                showing: false,
                initComponents: function () {
                  this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
                }
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; height: 70px;',
                components: [{
                  style: 'float:left; display: table-row;',
                  components: [{
                    name: 'totalLbl',
                    style: 'padding: 10px 20px 10px 10px; display: table-cell;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
                    }
                  }, {
                    style: 'padding: 10px 20px 10px 0px; display: table-cell;',
                    components: [{
                      name: 'total',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }, {
                  style: 'float:left; display: table-row;',
                  components: [{
                    name: 'countedLbl',
                    style: 'padding: 10px 20px 10px 10px; display: table-cell;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_Counted'));
                    }
                  }, {
                    style: 'padding: 10px 5px 10px 0px; display: table-cell;',
                    components: [{
                      name: 'counted',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }, {
                  style: 'float:left; display: table-row;',
                  components: [{
                    name: 'differenceLbl',
                    style: 'padding: 10px 20px 10px 10px; display: table-cell;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_Remaining'));
                    }
                  }, {
                    style: 'padding: 10px 5px 10px 0px; display: table-cell;',
                    components: [{
                      name: 'difference',
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
      this.originator.$.numberOfCoins.applyStyle('background-color', 'white');
    }

    // set new status
    if (originator && originator !== this.originator) {
      this.originator = originator;
      this.originator.$.numberOfCoins.applyStyle('background-color', '#6CB33F');
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
      totalCounted += coin.get('totalAmount');
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

    this.$.title.setContent(OB.I18N.getLabel('OBPOS_CashPaymentsTitle', [payment.get('name')]) + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());

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
        currency: currencyId
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
    var payment = model.get('paymentList').at(model.get('substep'));

    // this function is invoked when displayed.      
    this.initPaymentToCount(payment);

    // Open drawer if allow open drawer. Already a cash method.
    if (payment.get('paymentMethod').allowopendrawer) {
      OB.POS.hwserver.openDrawer();
      if (OB.MobileApp.model.get('permissions').OBPOS_closeDrawerBeforeContinue) {
        OB.MobileApp.model.set("isDrawerClosed", false);
        OB.POS.hwserver.isDrawerClosed(false, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount);
      }
    }
  }
});