/*
 ************************************************************************************
 * Copyright (C) 2014-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

OB.OBPOSCloseCash = OB.OBPOSCloseCash || {};
OB.OBPOSCloseCash.Model = OB.OBPOSCloseCash.Model || {};
OB.OBPOSCloseCash.UI = OB.OBPOSCloseCash.UI || {};

//Window model
OB.OBPOSCloseCash.Model.CloseCash = OB.Model.TerminalWindowModel.extend({
  initialStep: 1,
  finishButtonLabel: 'OBPOS_LblPostPrintClose',
  reportTitleLabel: 'OBPOS_LblStep4of4',
  defaults: {
    step: OB.DEC.Zero,
    allowedStep: OB.DEC.Zero,
    totalExpected: OB.DEC.Zero,
    totalCounted: OB.DEC.Zero,
    totalDifference: OB.DEC.Zero,
    pendingOrdersToProcess: false,
    otherInput: OB.DEC.Zero
  },
  stepsDefinition: [],
  additionalProperties: [],
  propertyFunctions: [],
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
    this.set('loadFinished', false);

    // steps
    this.set('step', this.initialStep);
    this.set('substep', 0);

    // Create steps instances
    this.closeCashSteps = [];
    this.stepsDefinition.forEach(s => {
      let newstep = enyo.createFromKind(s.name);
      newstep.model = this;
      this.closeCashSteps.push(newstep);
    });

    this.set('paymentList', new Backbone.Collection());
    this.set('closeCashReport', new Backbone.Collection());

    // Start instance initial Process
    this.initializationProcess(initModelsCallback);

    this.printCloseCash = new OB.OBPOSCloseCash.Print.CloseCash();
  },
  initializationProcess: function(initModelsCallback) {
    this.set('loadFinished', true);
    initModelsCallback();
    this.finishLoad();
  },
  loadModels: function(loadModelsCallback) {
    loadModelsCallback();
  },
  finishLoad: function() {
    let finish = true;
    this.stepsDefinition.forEach(step => {
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
    let count = 0;
    this.stepsDefinition.forEach(step => {
      if (step.active) {
        count++;
      }
    });
    return count;
  },
  // Get step index
  stepIndex: function(defName) {
    let index = -1;
    this.stepsDefinition.forEach((step, indx) => {
      if (step.name === defName) {
        index = indx;
      }
    });
    return index;
  },
  // Real step number
  stepNumber: function(defName) {
    const index = this.stepIndex(defName);
    let count = 0;
    for (let i = 0; i <= index; i++) {
      if (this.stepsDefinition[i].active) {
        count++;
      }
    }
    return count;
  },
  // Get first step available (step from 1..N)
  getFirstStep: function() {
    for (let i = 0; i < this.stepsDefinition.length; i++) {
      if (this.stepsDefinition[i].active) {
        return i + 1;
      }
    }
    return null;
  },
  // Next step (step from 1..N)
  getNextStep: function() {
    for (let i = this.get('step'); i < this.stepsDefinition.length; i++) {
      if (this.stepsDefinition[i].active) {
        return i + 1;
      }
    }
    return null;
  },
  // Previous (step from 1..N)
  getPreviousStep: function() {
    for (let i = this.get('step') - 2; i >= 0; i--) {
      if (this.stepsDefinition[i].active) {
        return i + 1;
      }
    }
    return 0;
  },
  //Previous next
  allowNext: function() {
    return this.get('step') > 0
      ? this.closeCashSteps[this.get('step') - 1].allowNext()
      : false;
  },
  allowPrevious: function() {
    return this.get('step') > this.getFirstStep();
  },
  setIgnoreStep3: function() {
    let result;
    this.get('paymentList').models.forEach(model => {
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
    });
    this.set('ignoreStep3', result);
  },
  showStep: function(leftpanel$) {
    const currentstep = this.get('step') - 1;

    for (let i = 0; i < this.closeCashSteps.length; i++) {
      const stepcomponent = this.closeCashSteps[i].getStepComponent(leftpanel$);
      stepcomponent.setShowing(i === currentstep);
      if (i === currentstep) {
        stepcomponent.displayStep(this);
      }
    }
  },
  getStepToolbar: function() {
    const currentstep = this.get('step') - 1;
    return this.closeCashSteps[currentstep].getToolbarName();
  },
  nextButtonI18NLabel: function() {
    const currentstep = this.get('step') - 1;
    if (this.closeCashSteps[currentstep].nextFinishButton()) {
      return this.finishButtonLabel;
    }
    return 'OBPOS_LblNextStep';
  },
  isFinishedWizard: function(step) {
    // Adjust step to array index
    const postPrintAndClose = this.stepIndex('OB.CloseCash.PostPrintAndClose');
    if (this.stepsDefinition[postPrintAndClose].active) {
      return step === postPrintAndClose + 2;
    }
    return false;
  },
  getSubstepsLength: function(step) {
    return this.closeCashSteps[step - 1].getSubstepsLength(this);
  },
  isSubstepAvailable: function(step, substep) {
    return this.closeCashSteps[step - 1].isSubstepAvailable(this, substep);
  },
  verifyStep: function(leftpanel$, callback) {
    const currentstep = this.get('step') - 1,
      stepcomponent = this.closeCashSteps[currentstep].getStepComponent(
        leftpanel$
      );
    if (stepcomponent.verifyStep) {
      return stepcomponent.verifyStep(this, callback);
    }
    callback();
  },
  // [TODO] This maybe is something that could be generic function
  isPaymentMethodListVisible: function() {
    // Adjust step to array index
    return this.get('step') - 1 === this.stepIndex('OB.CashUp.CashPayments');
  },
  // Common steps
  // Step 2: Count all logic
  countAll: function() {
    this.get('paymentList').models.forEach(model => {
      model.set('foreignCounted', OB.DEC.add(0, model.get('foreignExpected')));
      model.set('counted', OB.DEC.add(0, model.get('expected')));
    });
  },
  // Step 3: validate cash to keep
  validateCashKeep: function(qty) {
    let result = {
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
  // Step 4:
  getCountCashSummary: function() {
    //First we fix the qty to keep for non-automated payment methods
    _.each(this.get('paymentList').models, function(model) {
      var counted = model.get('foreignCounted') || model.get('counted');
      if (
        !model.get('isCashToKeepSelected') &&
        model.get('paymentMethod').allowdontmove
      ) {
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
    // Calculate total quantity to keep and deposit for all the payment methods
    const totalQtyToKeep = this.get('paymentList').models.reduce(
        (total, model) => {
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
      totalQtyToDepo = this.get('paymentList').models.reduce((total, model) => {
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
      }, 0);

    // Initialize the summary
    let countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      qtyToKeepSummary: [],
      qtyToDepoSummary: [],
      totalCounted: this.get('totalCounted'),
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference'),
      totalQtyToKeep: totalQtyToKeep,
      totalQtyToDepo: totalQtyToDepo
    };

    const enumSummarys = [
      'expectedSummary',
      'countedSummary',
      'differenceSummary',
      'qtyToKeepSummary',
      'qtyToDepoSummary'
    ];
    const enumConcepts = [
      'expected',
      'counted',
      'difference',
      'qtyToKeep',
      'foreignCounted'
    ];
    const enumSecondConcepts = [
      'foreignExpected',
      'foreignCounted',
      'foreignDifference',
      'qtyToKeep',
      'qtyToKeep'
    ];
    const sortedPays = _.sortBy(this.get('paymentList').models, function(p) {
      return p.get('name');
    });
    // var fromCurrencyId, baseAmount;
    for (let counter = 0; counter < 5; counter++) {
      for (let i = 0; i < sortedPays.length; i++) {
        const model = sortedPays[i];
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
          const fromCurrencyId = model.get('paymentMethod').currency;
          let value = OB.DEC.Zero,
            second = OB.DEC.Zero,
            baseAmount;
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
  processAndFinish: function() {
    // Each instance should implement the ending process of the counting process
  },
  processAndFinishCloseCash: function() {
    this.processAndFinish();
  },
  setPaymentList: function(payMthds, validateSteps) {
    const terminalSlave =
      !OB.POS.modelterminal.get('terminal').ismaster &&
      OB.POS.modelterminal.get('terminal').isslave;

    this.set('paymentList', new Backbone.Collection());

    let activePaymentsList = [],
      tempList = new Backbone.Collection();

    // Get list of active payments
    OB.MobileApp.model.get('payments').forEach(payment => {
      if (
        payment.payment.active === true &&
        payment.paymentMethod.countpaymentincashup &&
        !payment.paymentMethod.issafebox
      ) {
        activePaymentsList.push(payment);
      }
    });

    if (activePaymentsList.length === 0) {
      this.stepsDefinition.forEach(step => {
        step.active =
          step.name !== 'OB.CloseCash.PostPrintAndClose' ? false : true;
      });
    }

    activePaymentsList.forEach((payment, index) => {
      let expected = 0;

      const auxPay = payMthds.filter(function(payMthd) {
        return payMthd.get('paymentMethodId') === payment.payment.id;
      })[0];

      //We cannot find this payment in local database, it must be a new payment method, we skip it.
      if (!auxPay) {
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
        const cStartingCash = auxPay.get('startingCash'),
          cTotalReturns = auxPay.get('totalReturns'),
          cTotalSales = auxPay.get('totalSales'),
          cTotalDeposits = OB.DEC.sub(
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
        const fromCurrencyId = auxPay.get('paymentMethod').currency;
        auxPay.set(
          'expected',
          OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, expected)
        );
        auxPay.set('foreignExpected', expected);
        const paymentShared =
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

      //Finally assign payment list
      if (index === activePaymentsList.length - 1) {
        if (
          OB.MobileApp.model.hasPermission(
            'OBPOS_retail.cashupGroupExpectedPayment',
            true
          )
        ) {
          // Split payment methods
          const expectedList = _.filter(tempList.models, function(pm) {
              return pm.get('expected') !== 0;
            }),
            emptyList = _.filter(tempList.models, function(pm) {
              return pm.get('expected') === 0;
            });

          this.set(
            'paymentExpectedList',
            new Backbone.Collection(expectedList)
          );
          this.set('paymentEmptyList', new Backbone.Collection(emptyList));

          if (emptyList.length > 0) {
            emptyList[0].set('firstEmptyPayment', true);
          }
          const models = expectedList.concat(emptyList);

          this.get('paymentList').reset(models);
        } else {
          this.get('paymentList').reset(tempList.models);
        }

        if (validateSteps) {
          if (terminalSlave && tempList.length === 0) {
            // Desactivate all steps
            this.stepsDefinition[
              this.stepIndex('OB.CloseCash.PaymentMethods')
            ].active = false;
          }

          // Active/Desactive CashPayments and CashToKeep tabs
          let cashPayments = false,
            cashToKeep = false;
          const paymentsIndex = this.stepIndex('OB.CloseCash.CashPayments'),
            toKeepIndex = this.stepIndex('OB.CloseCash.CashToKeep');
          for (let i = 0; i < tempList.length; i++) {
            if (
              this.closeCashSteps[paymentsIndex].isSubstepAvailable(this, i)
            ) {
              cashPayments = true;
            }
            if (this.closeCashSteps[toKeepIndex].isSubstepAvailable(this, i)) {
              cashToKeep = true;
            }
          }
          this.stepsDefinition[paymentsIndex].active = cashPayments;
          this.stepsDefinition[toKeepIndex].active = cashToKeep;
          this.set(
            'totalExpected',
            this.get('paymentList').models.reduce((total, model) => {
              return OB.DEC.add(total, model.get('expected'));
            }, 0)
          );
          this.set(
            'totalDifference',
            OB.DEC.sub(this.get('totalDifference'), this.get('totalExpected'))
          );
          this.setIgnoreStep3();
        }
      }
    });
  }
});
