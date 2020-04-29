/*
 ************************************************************************************
 * Copyright (C) 2014-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _ */

OB.OBPOSCashUp = OB.OBPOSCashUp || {};
OB.OBPOSCashUp.Model = OB.OBPOSCashUp.Model || {};
OB.OBPOSCashUp.UI = OB.OBPOSCashUp.UI || {};

//Window model
OB.OBPOSCashUp.Model.CashUp = OB.Model.TerminalWindowModel.extend({
  initialStep: 1,
  finishButtonLabel: 'OBPOS_LblPostPrintClose',
  reportTitleLabel: 'OBPOS_LblStep4of4',
  models: [OB.Model.Order],
  defaults: {
    step: OB.DEC.Zero,
    allowedStep: OB.DEC.Zero,
    totalExpected: OB.DEC.Zero,
    totalCounted: OB.DEC.Zero,
    totalDifference: OB.DEC.Zero,
    pendingOrdersToProcess: false,
    otherInput: OB.DEC.Zero
  },
  cashupStepsDefinition: [
    {
      name: 'OB.CashUp.StepPendingOrders',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupStepPendingOrders',
      active: true
    },
    {
      name: 'OB.CashUp.Master',
      loaded: true,
      classes: 'cashupStepsDefinition-obCashupMaster',
      active: false
    },
    {
      name: 'OB.CashUp.CashPayments',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupCashPayments',
      active: true
    },
    {
      name: 'OB.CashUp.PaymentMethods',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupPaymentMethods',
      active: true
    },
    {
      name: 'OB.CashUp.CashToKeep',
      loaded: true,
      active: true,
      classes: 'cashupStepsDefinition-obCashupCashToKeep'
    },
    {
      name: 'OB.CashUp.PostPrintAndClose',
      loaded: true,
      classes: 'cashupStepsDefinition-obCashupPostPrintAndClose',
      active: true
    }
  ],
  init: function() {
    OB.error(
      'This init method should never be called for this model. Call initModels and loadModels instead'
    );
    this.initModels(function() {
      return this;
    });
    this.loadModels(function() {
      return this;
    });
  },
  initModels: function(initModelsCallback) {
    //Check for orders which are being processed in this moment.
    //cancel -> back to point of sale
    //Ok -> Continue closing without these orders
    var me = this,
      terminalSlave =
        !OB.POS.modelterminal.get('terminal').ismaster &&
        OB.POS.modelterminal.get('terminal').isslave,
      newstep,
      expected = 0,
      startings = [],
      cashUpReport,
      tempList = new Backbone.Collection(),
      activePaymentsList = [],
      finish,
      synch1 = false,
      synch2 = false,
      synch3 = false;

    this.cashupStepsDefinition[
      this.stepIndex('OB.CashUp.Master')
    ].active = OB.POS.modelterminal.get('terminal').ismaster;
    this.cashupStepsDefinition[
      this.stepIndex('OB.CashUp.StepPendingOrders')
    ].loaded = false;
    this.cashupStepsDefinition[
      this.stepIndex('OB.CashUp.CashPayments')
    ].loaded = false;
    this.cashupStepsDefinition[
      this.stepIndex('OB.CashUp.PaymentMethods')
    ].loaded = false;
    this.set('loadFinished', false);

    //steps
    this.set('step', this.initialStep);
    this.set('substep', 0);

    // Create steps instances
    this.cashupsteps = [];
    _.each(
      this.cashupStepsDefinition,
      function(s) {
        newstep = enyo.createFromKind(s.name);
        newstep.model = this;
        this.cashupsteps.push(newstep);
      },
      this
    );

    finish = function() {
      if (synch1 && synch2 && synch3) {
        me.finishLoad();
      }
    };

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('paymentList', new Backbone.Collection());
    const payMthds = new Backbone.Collection(
      OB.App.State.Cashup.Utils.getPaymentMethods()
    );

    //OB.Dal.find success
    // Get list of active payments
    _.each(OB.MobileApp.model.get('payments'), function(payment) {
      if (
        payment.payment.active === true &&
        payment.paymentMethod.countpaymentincashup
      ) {
        activePaymentsList.push(payment);
      }
    });
    _.each(
      activePaymentsList,
      function(payment, index) {
        expected = 0;
        var auxPay = payMthds.filter(function(payMthd) {
          return payMthd.get('paymentMethodId') === payment.payment.id;
        })[0];
        if (!auxPay) {
          //We cannot find this payment in local database, it must be a new payment method, we skip it.
          return;
        }

        // Not add shared payment to slave terminal
        if (
          !terminalSlave ||
          !OB.MobileApp.model.paymentnames[payment.payment.searchKey]
            .paymentMethod.isshared
        ) {
          auxPay.set('_id', payment.payment.searchKey);
          auxPay.set('isocode', payment.isocode);
          auxPay.set('paymentMethod', payment.paymentMethod);
          auxPay.set('id', payment.payment.id);
          if (auxPay.get('totalDeposits') === null) {
            auxPay.set('totalDeposits', 0);
          }
          if (auxPay.get('totalDrops') === null) {
            auxPay.set('totalDrops', 0);
          }
          var cStartingCash = auxPay.get('startingCash');
          var cTotalReturns = auxPay.get('totalReturns');
          var cTotalSales = auxPay.get('totalSales');
          var cTotalDeposits = OB.DEC.sub(
            auxPay.get('totalDeposits'),
            OB.DEC.abs(auxPay.get('totalDrops'))
          );
          expected = OB.DEC.add(
            OB.DEC.add(
              cStartingCash,
              OB.DEC.sub(cTotalSales, cTotalReturns, payment.obposPosprecision),
              payment.obposPosprecision
            ),
            cTotalDeposits,
            payment.obposPosprecision
          );
          var fromCurrencyId = auxPay.get('paymentMethod').currency;
          auxPay.set(
            'expected',
            OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, expected)
          );
          auxPay.set('foreignExpected', expected);
          var paymentShared =
            (OB.POS.modelterminal.get('terminal').ismaster ||
              OB.POS.modelterminal.get('terminal').isslave) &&
            OB.MobileApp.model.paymentnames[payment.payment.searchKey]
              .paymentMethod.isshared;
          if (paymentShared) {
            auxPay.set(
              'name',
              auxPay.get('name') +
                (paymentShared
                  ? OB.I18N.getLabel('OBPOS_LblPaymentMethodShared')
                  : '')
            );
          }
          tempList.add(auxPay);
        }

        if (index === activePaymentsList.length - 1) {
          if (terminalSlave && tempList.length === 0) {
            // Desactivate all steps
            me.cashupStepsDefinition[
              me.stepIndex('OB.CashUp.PaymentMethods')
            ].active = false;
          }
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_retail.cashupGroupExpectedPayment',
              true
            )
          ) {
            // Split payment methods
            var expectedList = _.filter(tempList.models, function(pm) {
                return pm.get('expected') !== 0;
              }),
              emptyList = _.filter(tempList.models, function(pm) {
                return pm.get('expected') === 0;
              });

            me.set(
              'paymentExpectedList',
              new Backbone.Collection(expectedList)
            );
            me.set('paymentEmptyList', new Backbone.Collection(emptyList));

            if (emptyList.length > 0) {
              emptyList[0].set('firstEmptyPayment', true);
            }
            var models = expectedList.concat(emptyList);

            me.get('paymentList').reset(models);
          } else {
            me.get('paymentList').reset(tempList.models);
          }
          // Active/Desactive CashPayments and CashToKeep tabs
          var i,
            cashPayments = false,
            cashToKeep = false,
            paymentsIndex = me.stepIndex('OB.CashUp.CashPayments'),
            toKeepIndex = me.stepIndex('OB.CashUp.CashToKeep');
          for (i = 0; i < tempList.length; i++) {
            if (me.cashupsteps[paymentsIndex].isSubstepAvailable(me, i)) {
              cashPayments = true;
            }
            if (me.cashupsteps[toKeepIndex].isSubstepAvailable(me, i)) {
              cashToKeep = true;
            }
          }
          me.cashupStepsDefinition[paymentsIndex].active = cashPayments;
          me.cashupStepsDefinition[toKeepIndex].active = cashToKeep;
          me.set(
            'totalExpected',
            _.reduce(
              me.get('paymentList').models,
              function(total, model) {
                return OB.DEC.add(total, model.get('expected'));
              },
              0
            )
          );
          me.set(
            'totalDifference',
            OB.DEC.sub(me.get('totalDifference'), me.get('totalExpected'))
          );
          me.setIgnoreStep3();
        }
      },
      this
    );
    me.cashupStepsDefinition[
      me.stepIndex('OB.CashUp.CashPayments')
    ].loaded = true;
    me.cashupStepsDefinition[
      me.stepIndex('OB.CashUp.PaymentMethods')
    ].loaded = true;
    synch1 = true;
    finish();

    this.set('cashUpReport', new Backbone.Collection());
    cashUpReport = new Backbone.Model(OB.App.State.Cashup.Utils.getCashup());

    initModelsCallback();

    cashUpReport.set('deposits', []);
    cashUpReport.set('drops', []);
    me.deposit = 0;
    me.drop = 0;
    const cashMgmts = new OB.Collection.CashManagementList();
    OB.App.State.Cashup.Utils.getCashManagements().forEach(cashManagement =>
      cashMgmts.add(OB.Dal.transform(OB.Model.CashManagement, cashManagement))
    );

    _.forEach(cashMgmts.models, function(cashMgmt, index) {
      var payment = _.filter(OB.MobileApp.model.get('payments'), function(pay) {
        return pay.payment.id === cashMgmt.get('paymentMethodId');
      })[0];
      cashMgmt.set('countInCashup', payment.paymentMethod.countpaymentincashup);
      cashMgmt.set(
        'searchKey',
        (cashMgmt.get('type') === 'deposit'
          ? 'cashMgmtDeposit'
          : 'cashMgmtDrop') +
          index +
          payment.payment.searchKey.replace('_', '') +
          cashMgmt.get('amount')
      );
      cashUpReport.get(cashMgmt.get('type') + 's').push(cashMgmt);
      me[cashMgmt.get('type')] = OB.DEC.add(
        me[cashMgmt.get('type')],
        cashMgmt.get('origAmount')
      );
    });
    cashUpReport.set('totalDeposits', me.deposit);
    cashUpReport.set('totalDrops', me.drop);
    delete me.deposit;
    delete me.drop;

    const cashup = OB.App.State.Cashup.Utils.getCashup();
    cashUpReport.set(
      'salesTaxes',
      new Backbone.Collection(
        cashup.cashTaxInfo.filter(tax => {
          return tax.orderType !== 1;
        })
      ).models
    );
    cashUpReport.set(
      'returnsTaxes',
      new Backbone.Collection(
        cashup.cashTaxInfo.filter(tax => {
          return tax.orderType === 1;
        })
      ).models
    );

    //OB.Dal.find success
    cashUpReport.set(
      'totalStartings',
      _.reduce(
        payMthds.models,
        function(accum, trx) {
          if (
            OB.MobileApp.model.paymentnames[trx.get('searchKey')] &&
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .countpaymentincashup
          ) {
            // Not accumulate shared payments on slave terminal
            if (
              terminalSlave &&
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.isshared
            ) {
              return accum;
            }
            var fromCurrencyId =
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.currency;
            var cStartingCash = OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              trx.get('startingCash')
            );
            return OB.DEC.add(accum, cStartingCash);
          }
          return accum;
        },
        0
      )
    );

    cashUpReport.set(
      'totalDeposits',
      _.reduce(
        payMthds.models,
        function(accum, trx) {
          if (OB.MobileApp.model.paymentnames[trx.get('searchKey')]) {
            // Not accumulate shared payments on slave terminal
            if (
              terminalSlave &&
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.isshared
            ) {
              return accum;
            }
            var fromCurrencyId =
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.currency;
            var cTotalDeposits = OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(trx.get('totalDeposits'), trx.get('totalSales'))
            );
            return OB.DEC.add(accum, cTotalDeposits);
          }
          return accum;
        },
        0
      )
    );

    cashUpReport.set(
      'totalDrops',
      _.reduce(
        payMthds.models,
        function(accum, trx) {
          if (OB.MobileApp.model.paymentnames[trx.get('searchKey')]) {
            // Not accumulate shared payments on slave terminal
            if (
              terminalSlave &&
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.isshared
            ) {
              return accum;
            }
            var fromCurrencyId =
              OB.MobileApp.model.paymentnames[trx.get('searchKey')]
                .paymentMethod.currency;
            var cTotalDrops = OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(trx.get('totalDrops'), trx.get('totalReturns'))
            );
            return OB.DEC.add(accum, cTotalDrops);
          }
          return accum;
        },
        0
      )
    );

    _.each(
      payMthds.models,
      function(p) {
        var auxPay = OB.MobileApp.model.get('payments').filter(function(pay) {
          return pay.payment.id === p.get('paymentmethod_id');
        })[0];
        if (!auxPay) {
          //We cannot find this payment in local database, it must be a new payment method, we skip it.
          return;
        }
        // Not add shared payment to slave terminal
        if (
          terminalSlave &&
          OB.MobileApp.model.paymentnames[auxPay.payment.searchKey]
            .paymentMethod.isshared
        ) {
          return;
        }

        if (
          !auxPay.paymentMethod.countpaymentincashup &&
          !auxPay.paymentMethod.isRounding
        ) {
          return;
        }

        var fromCurrencyId = auxPay.paymentMethod.currency,
          paymentShared =
            (OB.POS.modelterminal.get('terminal').ismaster ||
              OB.POS.modelterminal.get('terminal').isslave) &&
            OB.MobileApp.model.paymentnames[auxPay.payment.searchKey]
              .paymentMethod.isshared,
          paymentSharedStr = paymentShared
            ? OB.I18N.getLabel('OBPOS_LblPaymentMethodShared')
            : '';
        cashUpReport.get('deposits').push(
          new Backbone.Model({
            searchKey: p.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(0, p.get('totalSales'))
            ),
            amount: OB.DEC.add(0, p.get('totalSales')),
            description: p.get('name') + paymentSharedStr,
            currency: fromCurrencyId,
            isocode: auxPay.isocode,
            rate: p.get('rate'),
            countInCashup: auxPay.paymentMethod.countpaymentincashup
          })
        );
        cashUpReport.get('drops').push(
          new Backbone.Model({
            searchKey: p.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(0, p.get('totalReturns'))
            ),
            amount: OB.DEC.add(0, p.get('totalReturns')),
            description: p.get('name') + paymentSharedStr,
            currency: fromCurrencyId,
            isocode: auxPay.isocode,
            rate: p.get('rate'),
            countInCashup: auxPay.paymentMethod.countpaymentincashup
          })
        );
        if (auxPay.paymentMethod.countpaymentincashup) {
          startings.push(
            new Backbone.Model({
              searchKey: p.get('searchKey'),
              origAmount: OB.UTIL.currency.toDefaultCurrency(
                fromCurrencyId,
                p.get('startingCash')
              ),
              amount: OB.DEC.add(0, p.get('startingCash')),
              description:
                OB.I18N.getLabel('OBPOS_LblStarting') +
                ' ' +
                p.get('name') +
                paymentSharedStr,
              currency: fromCurrencyId,
              isocode: auxPay.isocode,
              rate: p.get('rate'),
              paymentId: p.get('paymentmethod_id'),
              countInCashup: auxPay.paymentMethod.countpaymentincashup
            })
          );
        }
      },
      this
    );
    cashUpReport.set('startings', startings);
    //FIXME: We are not sure if other finds are done.
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_EditCashupReport',
      {
        cashUpReport: cashUpReport
      },
      function(args) {
        me.get('cashUpReport').add(args.cashUpReport);
        synch2 = true;
        finish();
      }
    );

    this.get('paymentList').on(
      'change:counted',
      function(mod) {
        mod.set(
          'difference',
          OB.DEC.sub(mod.get('counted'), mod.get('expected'))
        );
        if (
          mod.get('foreignCounted') !== null &&
          mod.get('foreignCounted') !== undefined &&
          mod.get('foreignExpected') !== null &&
          mod.get('foreignExpected') !== undefined
        ) {
          mod.set(
            'foreignDifference',
            OB.DEC.sub(mod.get('foreignCounted'), mod.get('foreignExpected'))
          );
        }
        this.set(
          'totalCounted',
          _.reduce(
            this.get('paymentList').models,
            function(total, model) {
              return model.get('counted')
                ? OB.DEC.add(total, model.get('counted'))
                : total;
            },
            0
          ),
          0
        );
        if (mod.get('counted') === OB.DEC.Zero) {
          this.trigger('change:totalCounted');
        }
      },
      this
    );

    OB.Dal.find(
      OB.Model.Order,
      {
        hasbeenpaid: 'N'
      },
      function(pendingOrderList, me) {
        _.each(pendingOrderList.models, function(order) {
          order.set('ignoreCheckIfIsActiveOrder', true);
        });
        me.get('orderlist').reset(pendingOrderList.models);
        var indexStepPendingOrders = me.stepIndex(
          'OB.CashUp.StepPendingOrders'
        );
        me.cashupStepsDefinition[indexStepPendingOrders].active =
          pendingOrderList.length > 0;
        me.cashupStepsDefinition[indexStepPendingOrders].loaded = true;
        synch3 = true;
        finish();
      },
      function(tx, error) {
        OB.UTIL.showError(error);
      },
      this
    );

    this.printCashUp = new OB.OBPOSCashUp.Print.CashUp();
  },
  loadModels: function(loadModelsCallback) {
    loadModelsCallback();
  },
  finishLoad: function() {
    var finish = true;
    _.each(this.cashupStepsDefinition, function(step) {
      if (!step.loaded) {
        finish = false;
      }
    });
    if (finish && !this.get('loadFinished')) {
      this.set('step', this.getFirstStep());
      this.set('substep', 0);
      this.set('loadFinished', true);
    }
  },
  // Count real step
  stepCount: function() {
    var count = 0;
    _.each(this.cashupStepsDefinition, function(step) {
      if (step.active) {
        count++;
      }
    });
    return count;
  },
  // Get step index
  stepIndex: function(defName) {
    var index = -1;
    _.each(this.cashupStepsDefinition, function(step, indx) {
      if (step.name === defName) {
        index = indx;
      }
    });
    return index;
  },
  // Real step number
  stepNumber: function(defName) {
    var index = this.stepIndex(defName);
    var i,
      count = 0;
    for (i = 0; i <= index; i++) {
      if (this.cashupStepsDefinition[i].active) {
        count++;
      }
    }
    return count;
  },
  // Get first step available (step from 1..N)
  getFirstStep: function() {
    var i;
    for (i = 0; i < this.cashupStepsDefinition.length; i++) {
      if (this.cashupStepsDefinition[i].active) {
        return i + 1;
      }
    }
    return null;
  },
  // Next step (step from 1..N)
  getNextStep: function() {
    var i;
    for (i = this.get('step'); i < this.cashupStepsDefinition.length; i++) {
      if (this.cashupStepsDefinition[i].active) {
        return i + 1;
      }
    }
    return null;
  },
  // Previous (step from 1..N)
  getPreviousStep: function() {
    var i;
    for (i = this.get('step') - 2; i >= 0; i--) {
      if (this.cashupStepsDefinition[i].active) {
        return i + 1;
      }
    }
    return 0;
  },
  //Previous next
  allowNext: function() {
    return this.get('step') > 0
      ? this.cashupsteps[this.get('step') - 1].allowNext()
      : false;
  },
  allowPrevious: function() {
    return this.get('step') > this.getFirstStep();
  },
  setIgnoreStep3: function() {
    var result = null;
    _.each(
      this.get('paymentList').models,
      function(model) {
        if (model.get('paymentMethod').automatemovementtoother === false) {
          model.set('qtyToKeep', 0);
          if (result !== false) {
            result = true;
          }
        } else {
          //fix -> break
          result = false;
          return false;
        }
      },
      this
    );
    this.set('ignoreStep3', result);
  },
  showStep: function(leftpanel$) {
    var currentstep = this.get('step') - 1;
    var i;
    var stepcomponent;

    for (i = 0; i < this.cashupsteps.length; i++) {
      stepcomponent = this.cashupsteps[i].getStepComponent(leftpanel$);
      stepcomponent.setShowing(i === currentstep);
      if (i === currentstep) {
        stepcomponent.displayStep(this);
      }
    }
  },
  getStepToolbar: function() {
    var currentstep = this.get('step') - 1;
    return this.cashupsteps[currentstep].getToolbarName();
  },
  nextButtonI18NLabel: function() {
    var currentstep = this.get('step') - 1;
    if (this.cashupsteps[currentstep].nextFinishButton()) {
      return this.finishButtonLabel;
    }
    return 'OBPOS_LblNextStep';
  },
  isFinishedWizard: function(step) {
    // Adjust step to array index
    var postPrintAndClose = this.stepIndex('OB.CashUp.PostPrintAndClose');
    if (this.cashupStepsDefinition[postPrintAndClose].active) {
      return step === postPrintAndClose + 2;
    }
    return false;
  },
  getSubstepsLength: function(step) {
    return this.cashupsteps[step - 1].getSubstepsLength(this);
  },
  isSubstepAvailable: function(step, substep) {
    return this.cashupsteps[step - 1].isSubstepAvailable(this, substep);
  },
  verifyStep: function(leftpanel$, callback) {
    var currentstep = this.get('step') - 1;
    var stepcomponent = this.cashupsteps[currentstep].getStepComponent(
      leftpanel$
    );
    if (stepcomponent.verifyStep) {
      return stepcomponent.verifyStep(this, callback);
    }
    callback();
  },
  isPaymentMethodListVisible: function() {
    // Adjust step to array index
    return this.get('step') - 1 === this.stepIndex('OB.CashUp.CashPayments');
  },
  // Step 2: logic, expected vs counted
  countAll: function() {
    this.get('paymentList').each(function(model) {
      model.set('foreignCounted', OB.DEC.add(0, model.get('foreignExpected')));
      model.set('counted', OB.DEC.add(0, model.get('expected')));
    });
  },

  //step 3
  validateCashKeep: function(qty) {
    var result = {
      result: false,
      message: ''
    };
    if (qty !== undefined && qty !== null && typeof qty === 'number') {
      if (
        this.get('paymentList')
          .at(this.get('substep'))
          .get('foreignCounted') >= qty
      ) {
        result.result = true;
        result.message = '';
      } else {
        result.result = false;
        result.message = OB.I18N.getLabel('OBPOS_MsgMoreThanCounted');
      }
    } else {
      result.result = false;
      result.message = OB.I18N.getLabel('OBPOS_MsgNotValidNoToKeep');
    }
    if (!result.result) {
      this.get('paymentList')
        .at(this.get('substep'))
        .set('qtyToKeep', null);
    }
    return result;
  },

  //Step 4
  getCountCashSummary: function() {
    var countCashSummary,
      counter,
      enumConcepts,
      enumSecondConcepts,
      enumSummarys,
      i,
      model,
      value = OB.DEC.Zero,
      second = OB.DEC.Zero;

    //First we fix the qty to keep for non-automated payment methods
    _.each(this.get('paymentList').models, function(model) {
      var counted = model.get('foreignCounted');
      if (OB.UTIL.isNullOrUndefined(model.get('qtyToKeep'))) {
        model.set('qtyToKeep', counted);
      }
      if (counted < 0) {
        model.set('qtyToKeep', 0);
        return;
      }
      if (
        !model.get('isCashToKeepSelected') &&
        model.get('paymentMethod').keepfixedamount
      ) {
        model.set('qtyToKeep', model.get('paymentMethod').amount);
      }
      if (model.get('qtyToKeep') > counted) {
        model.set('qtyToKeep', counted);
      }
    });

    countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      qtyToKeepSummary: [],
      qtyToDepoSummary: [],
      totalCounted: this.get('totalCounted'),
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference'),
      totalQtyToKeep: _.reduce(
        this.get('paymentList').models,
        function(total, model) {
          if (model.get('qtyToKeep')) {
            var cQtyToKeep = OB.UTIL.currency.toDefaultCurrency(
              model.get('paymentMethod').currency,
              model.get('qtyToKeep')
            );
            return OB.DEC.add(total, cQtyToKeep);
          }
          return total;
        },
        0
      ),
      totalQtyToDepo: _.reduce(
        this.get('paymentList').models,
        function(total, model) {
          if (
            model.get('qtyToKeep') !== null &&
            model.get('qtyToKeep') !== undefined &&
            model.get('foreignCounted') !== null &&
            model.get('foreignCounted') !== undefined
          ) {
            var qtyToDepo = OB.DEC.sub(
              model.get('foreignCounted'),
              model.get('qtyToKeep')
            );
            var cQtyToDepo = OB.UTIL.currency.toDefaultCurrency(
              model.get('paymentMethod').currency,
              qtyToDepo
            );
            return OB.DEC.add(total, cQtyToDepo);
          }
          return total;
        },
        0
      )
    };

    enumSummarys = [
      'expectedSummary',
      'countedSummary',
      'differenceSummary',
      'qtyToKeepSummary',
      'qtyToDepoSummary'
    ];
    enumConcepts = [
      'expected',
      'counted',
      'difference',
      'qtyToKeep',
      'foreignCounted'
    ];
    enumSecondConcepts = [
      'foreignExpected',
      'foreignCounted',
      'foreignDifference',
      'qtyToKeep',
      'qtyToKeep'
    ];
    var sortedPays = _.sortBy(this.get('paymentList').models, function(p) {
      return p.get('name');
    });
    var fromCurrencyId, baseAmount;
    for (counter = 0; counter < 5; counter++) {
      for (i = 0; i < sortedPays.length; i++) {
        model = sortedPays[i];
        if (!model.get(enumConcepts[counter])) {
          countCashSummary[enumSummarys[counter]].push(
            new Backbone.Model({
              searchKey: model.get('searchKey'),
              name: model.get('name'),
              value: 0,
              second: 0,
              isocode: ''
            })
          );
        } else {
          fromCurrencyId = model.get('paymentMethod').currency;
          switch (enumSummarys[counter]) {
            case 'qtyToKeepSummary':
              if (
                model.get(enumSecondConcepts[counter]) !== null &&
                model.get(enumSecondConcepts[counter]) !== undefined
              ) {
                value = OB.UTIL.currency.toDefaultCurrency(
                  fromCurrencyId,
                  model.get(enumConcepts[counter])
                );
                second = model.get(enumSecondConcepts[counter]);
              }
              break;
            case 'qtyToDepoSummary':
              if (
                model.get(enumSecondConcepts[counter]) !== null &&
                model.get(enumSecondConcepts[counter]) !== undefined &&
                model.get('rate') !== '1'
              ) {
                second = OB.DEC.sub(
                  model.get(enumConcepts[counter]),
                  model.get(enumSecondConcepts[counter])
                );
              } else {
                second = OB.DEC.Zero;
              }
              if (
                model.get(enumSecondConcepts[counter]) !== null &&
                model.get(enumSecondConcepts[counter]) !== undefined
              ) {
                baseAmount = OB.DEC.sub(
                  model.get(enumConcepts[counter]),
                  model.get(enumSecondConcepts[counter])
                );
                value = OB.UTIL.currency.toDefaultCurrency(
                  fromCurrencyId,
                  baseAmount
                );
              } else {
                value = OB.DEC.Zero;
              }

              break;
            default:
              value = model.get(enumConcepts[counter]);
              second = model.get(enumSecondConcepts[counter]);
          }
          countCashSummary[enumSummarys[counter]].push(
            new Backbone.Model({
              searchKey: model.get('searchKey'),
              name: model.get('name'),
              value: value,
              second: second,
              isocode: model.get('isocode')
            })
          );
        }
      }
    }
    return countCashSummary;
  },
  additionalProperties: [],
  propertyFunctions: [],
  processAndFinishCashUp: function() {
    OB.UTIL.showLoading(true);
    var me = this;
    OB.Dal.transaction(function(tx) {
      const cashUp = new Backbone.Collection([
        OB.App.State.Cashup.Utils.getCashup()
      ]);

      var i,
        paymentMethodInfo,
        objToSend = {}; // created empty instead load from cashup, since in state is no longer stored the objToSend
      var now = new Date();
      objToSend.cashUpDate = OB.I18N.normalizeDate(now);
      objToSend.lastcashupeportdate = OB.I18N.normalizeDate(now);
      objToSend.timezoneOffset = now.getTimezoneOffset();
      for (i = 0; i < me.additionalProperties.length; i++) {
        objToSend[me.additionalProperties[i]] = me.propertyFunctions[i](
          OB.POS.modelterminal.get('terminal').id,
          cashUp.at(0)
        );
      }
      var cashCloseArray = [];
      objToSend.cashCloseInfo = cashCloseArray;
      _.each(
        me.get('paymentList').models,
        function(curModel) {
          var cashCloseInfo = {
            expected: 0,
            difference: 0,
            paymentTypeId: 0,
            paymentMethod: {}
          };
          // Set cashclose info
          cashCloseInfo.id = OB.UTIL.get_UUID();
          cashCloseInfo.paymentTypeId = curModel.get('id');
          cashCloseInfo.difference = curModel.get('difference');
          cashCloseInfo.foreignDifference = curModel.get('foreignDifference');
          cashCloseInfo.expected = curModel.get('expected');
          cashCloseInfo.foreignExpected = curModel.get('foreignExpected');
          paymentMethodInfo = curModel.get('paymentMethod');
          paymentMethodInfo.amountToKeep = curModel.get('qtyToKeep');
          cashCloseInfo.paymentMethod = paymentMethodInfo;
          objToSend.cashCloseInfo.push(cashCloseInfo);
        },
        me
      );
      objToSend.approvals = me.get('approvals');
      var cashMgmtIds = [];
      objToSend.cashMgmtIds = cashMgmtIds;

      const cashMgmts = new OB.Collection.CashManagementList();
      OB.App.State.Cashup.Utils.getCashManagements().forEach(cashManagement =>
        cashMgmts.add(OB.Dal.transform(OB.Model.CashManagement, cashManagement))
      );
      _.each(cashMgmts.models, function(cashMgmt) {
        objToSend.cashMgmtIds.push(cashMgmt.get('id'));
      });
      cashUp.at(0).set('userId', OB.MobileApp.model.get('context').user.id);
      objToSend.userId = OB.MobileApp.model.get('context').user.id;
      objToSend.isprocessed = 'Y';
      cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
      cashUp.at(0).set('isprocessed', 'Y');

      var callbackFinishedSuccess = function() {
        OB.UTIL.showLoading(true);
        me.set('finished', true);
        if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          OB.UTIL.localStorage.setItem(
            'lastProcessedCashup',
            cashUp.at(0).get('id')
          );
        }
        if (OB.MobileApp.model.hasPermission('OBPOS_print.cashup')) {
          var cashUpReport = new OB.Model.CashUp(),
            countCashSummary = me.getCountCashSummary();
          OB.UTIL.clone(me.get('cashUpReport').at(0), cashUpReport);
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_retail.cashupRemoveUnusedPayment',
              true
            )
          ) {
            var paymentWithMovement = [];
            if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
              OB.UTIL.cashupAddPaymentWithSummaryMovement(
                paymentWithMovement,
                countCashSummary.countedSummary
              );
              OB.UTIL.cashupAddPaymentWithSummaryMovement(
                paymentWithMovement,
                countCashSummary.differenceSummary
              );
              OB.UTIL.cashupAddPaymentWithSummaryMovement(
                paymentWithMovement,
                countCashSummary.qtyToKeepSummary
              );
              OB.UTIL.cashupAddPaymentWithSummaryMovement(
                paymentWithMovement,
                countCashSummary.qtyToDepoSummary
              );
            }
            OB.UTIL.cashupAddPaymentWithSummaryMovement(
              paymentWithMovement,
              countCashSummary.expectedSummary
            );
            OB.UTIL.cashupAddPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('startings')
            );
            OB.UTIL.cashupAddPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('drops')
            );
            OB.UTIL.cashupAddPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('deposits')
            );
            countCashSummary.expectedSummary = OB.UTIL.cashupGetPaymentWithMovement(
              paymentWithMovement,
              countCashSummary.expectedSummary
            );
            cashUpReport.set(
              'startings',
              OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                cashUpReport.get('startings')
              )
            );
            cashUpReport.set(
              'drops',
              OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                cashUpReport.get('drops')
              )
            );
            cashUpReport.set(
              'deposits',
              OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                cashUpReport.get('deposits')
              )
            );
            if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
              countCashSummary.countedSummary = OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                countCashSummary.countedSummary
              );
              countCashSummary.differenceSummary = OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                countCashSummary.differenceSummary
              );
              countCashSummary.qtyToKeepSummary = OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                countCashSummary.qtyToKeepSummary
              );
              countCashSummary.qtyToDepoSummary = OB.UTIL.cashupGetPaymentWithMovement(
                paymentWithMovement,
                countCashSummary.qtyToDepoSummary
              );
            }
          }

          me.printCashUp.print(cashUpReport, countCashSummary, true);
        }
      };
      var callbackFinishedWrongly = function() {
        me.set('finishedWrongly', true);
      };

      var synchronizedPreferenceValue;
      // prevent synchronized mode for cashups
      synchronizedPreferenceValue = OB.MobileApp.model.setSynchronizedPreference(
        false
      );
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PrePrintCashupHook',
        {
          cashupModel: me
        },
        function(args) {
          const filteredCashup = OB.App.State.Cashup.Utils.filterOnlyNeededDataForCompleteCashup(
            cashUp.at(0)
          );
          OB.App.State.Global.completeCashupAndCreateNew({
            completedCashupParams: {
              cashupWindowCashup: filteredCashup,
              terminalName: OB.MobileApp.model.get('logConfiguration')
                .deviceIdentifier,
              cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId')
            },
            newCashupParams: {
              currentDate: new Date(),
              userId: OB.MobileApp.model.get('context').user.id,
              terminalId: OB.MobileApp.model.get('terminal').id,
              terminalIsSlave: OB.POS.modelterminal.get('terminal').isslave,
              terminalPayments: OB.MobileApp.model.get('payments')
            }
          })
            .then(() => {
              OB.MobileApp.model.setSynchronizedPreference(
                synchronizedPreferenceValue
              );
              callbackFinishedSuccess();
            })
            .catch(e => {
              OB.error(e.stack);
              callbackFinishedWrongly();
            });
        }
      );
    });
  }
});

OB.OBPOSCashUp.Model.CashUpPartial = OB.OBPOSCashUp.Model.CashUp.extend({
  initialStep: 6,
  finishButtonLabel: 'OBPOS_LblPrintClose',
  reportTitleLabel: 'OBPOS_LblPartialCashUpTitle',
  isPartialCashup: true,

  processAndFinishCashUp: function() {
    if (OB.MobileApp.model.hasPermission('OBPOS_print.cashup')) {
      var me = this;
      this.printCashUp.print(
        this.get('cashUpReport').at(0),
        this.getCountCashSummary(),
        false,
        function() {
          me.set('finished', true);
        }
      );
    }
  },
  allowPrevious: function() {
    return false;
  },
  finishLoad: function() {
    var finish = true;
    _.each(this.cashupStepsDefinition, function(step) {
      if (!step.loaded) {
        finish = false;
      }
    });
    if (finish && !this.get('loadFinished')) {
      this.set('loadFinished', true);
    }
  }
});
