/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, $ */

OB.OBPOSCashUp = OB.OBPOSCashUp || {};
OB.OBPOSCashUp.Model = OB.OBPOSCashUp.Model || {};
OB.OBPOSCashUp.UI = OB.OBPOSCashUp.UI || {};

//Data models
OB.OBPOSCashUp.Model.CloseCashPaymentMethod = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CloseCashPayments',
  modelName: 'DataCloseCashPaymentMethod',
  online: true
});

OB.OBPOSCashUp.Model.CashCloseReport = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashCloseReport',
  modelName: 'DataCashCloseReport',
  online: true
});

//Window model
OB.OBPOSCashUp.Model.CashUp = OB.Model.TerminalWindowModel.extend({
  models: [OB.OBPOSCashUp.Model.CloseCashPaymentMethod, OB.OBPOSCashUp.Model.CashCloseReport, OB.Model.Order],
  defaults: {
    step: OB.DEC.Zero,
    allowedStep: OB.DEC.Zero,
    totalExpected: OB.DEC.Zero,
    totalCounted: OB.DEC.Zero,
    totalDifference: OB.DEC.Zero,
    pendingOrdersToProcess: false,
    otherInput: OB.DEC.Zero
  }, 
  cashupstepsdefinition: [
    'OB.CashUp.StepPendingOrders',    
    'OB.CashUp.CashPayments',
    'OB.CashUp.PaymentMethods',
    'OB.CashUp.CashToKeep',
    'OB.CashUp.PostPrintAndClose'                
  ],  
  init: function () {
    //Check for orders wich are being processed in this moment.
    //cancel -> back to point of sale
    //Ok -> Continue closing without these orders
    var undf, newstep;
    
    this.arePendingOrdersToBeProcess();

    //steps
    this.set('step', 1);
    this.set('substep', 0);
    
    // Create steps instances
    this.cashupsteps = [];
    _.each(this.cashupstepsdefinition, function (s) {
      newstep = enyo.createFromKind(s);
      newstep.model = this;
      this.cashupsteps.push(newstep);
    }, this);

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('paymentList', this.getData('DataCloseCashPaymentMethod'));
    this.convertExpected();
    this.setIgnoreStep3();
    this.set('cashUpReport', this.getData('DataCashCloseReport'));

    this.set('totalExpected', _.reduce(this.get('paymentList').models, function (total, model) {
      return OB.DEC.add(total, model.get('expected'));
    }, 0));
    this.set('totalDifference', OB.DEC.sub(this.get('totalDifference'), this.get('totalExpected')));

    this.get('paymentList').on('change:counted', function (mod) {
      mod.set('difference', OB.DEC.sub(mod.get('counted'), mod.get('expected')));
      if (mod.get('foreignCounted') !== null && mod.get('foreignCounted') !== undf && mod.get('foreignExpected') !== null && mod.get('foreignExpected') !== undf) {
        mod.set('foreignDifference', OB.DEC.sub(mod.get('foreignCounted'), mod.get('foreignExpected')));
      }
      this.set('totalCounted', _.reduce(this.get('paymentList').models, function (total, model) {
        return model.get('counted') ? OB.DEC.add(total, model.get('counted')) : total;
      }, 0), 0);
      if (mod.get('counted') === OB.DEC.Zero) {
        this.trigger('change:totalCounted');
      }
    }, this);

    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'N'
    }, function (pendingOrderList, me) {
      me.get('orderlist').reset(pendingOrderList.models);
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);

    this.printCashUp = new OB.OBPOSCashUp.Print.CashUp();
  },
  //Previous next
  allowNext: function () {
    return this.cashupsteps[this.get('step') - 1].allowNext();
  },
  allowPrevious: function () {
    return this.get('step') > 1;
  },
  setIgnoreStep3: function () {
    var result = null;
    _.each(this.get('paymentList').models, function (model) {
      if (model.get('paymentMethod').automatemovementtoother === false) {
        model.set('qtyToKeep', null);
        if (result !== false) {
          result = true;
        }
      } else {
        //fix -> break
        result = false;
        return false;
      }
    }, this);
    this.set('ignoreStep3', result);
  },
  showStep: function (leftpanel$) {    
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
  getStepToolbar: function () {
    var currentstep = this.get('step') - 1;
    return this.cashupsteps[currentstep].getToolbarName();
  },
  nextButtonI18NLabel: function () {
    var currentstep = this.get('step') - 1;
    return this.cashupsteps[currentstep].nextButtonI18NLabel();    
  },
  isFinishedWizard: function(step) {
    return step > this.cashupsteps.length;
  },
  getSubstepsLength: function (step) {
    return this.cashupsteps[step - 1].getSubstepsLength(this); 
  },
  isSubstepAvailable: function (step, substep) {
    return this.cashupsteps[step - 1].isSubstepAvailable(this, substep); 
  },
  verifyStep: function (leftpanel$, callback) {
    var currentstep = this.get('step') - 1;
    var stepcomponent = this.cashupsteps[currentstep].getStepComponent(leftpanel$);
    if (stepcomponent.verifyStep) {
      return stepcomponent.verifyStep(this, callback);
    } else {
      callback();
    }
  },
  
  //Step (pre) 1
  arePendingOrdersToBeProcess: function () {
    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'Y'
    }, function (fetchedOrderList, me) { //OB.Dal.find success
      var currentOrder = {};
      if (fetchedOrderList && fetchedOrderList.length !== 0) {
        me.set('pendingOrdersToProcess', true);
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
  },
  // Step 2: logic, expected vs counted 
  countAll: function () {
    this.get('paymentList').each(function (model) {
      model.set('foreignCounted', OB.DEC.add(0, model.get('foreignExpected')));
      model.set('counted', OB.DEC.add(0, model.get('expected')));
    });
  },

  //step 3
  validateCashKeep: function (qty) {
    var unfd, result = {
      result: false,
      message: ''
    };
    if (qty !== unfd && qty !== null && $.isNumeric(qty)) {
      if (this.get('paymentList').at(this.get('substep')).get('foreignCounted') >= qty) {
        result.result = true;
        result.message = '';
      } else {
        result.result = false;
        result.message = OB.I18N.getLabel('OBPOS_MsgMoreThanCounted');
      }
    } else {
      result.result = false;
      result.message = 'Not valid number to keep';
    }
    if (!result.result) {
      this.get('paymentList').at(this.get('substep')).set('qtyToKeep', null);
    }
    return result;
  },
  
  //Step 4
  getCountCashSummary: function () {
    var countCashSummary, counter, enumConcepts, enumSecondConcepts, enumSummarys, i, undf, model, value = OB.DEC.Zero,
        second = OB.DEC.Zero;
    countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      qtyToKeepSummary: [],
      qtyToDepoSummary: [],
      totalCounted: this.get('totalCounted'),
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference'),
      totalQtyToKeep: _.reduce(this.get('paymentList').models, function (total, model) {
        if (model.get('qtyToKeep')) {
          return OB.DEC.add(total, OB.DEC.mul(model.get('qtyToKeep'), model.get('rate')));
        } else {
          return total;
        }
      }, 0),
      totalQtyToDepo: _.reduce(this.get('paymentList').models, function (total, model) {
        if (model.get('qtyToKeep') !== null && model.get('qtyToKeep') !== undf) {
          return OB.DEC.add(total, OB.DEC.mul(OB.DEC.sub(model.get('foreignCounted'), model.get('qtyToKeep')), model.get('rate')));
        } else {
          return total;
        }
      }, 0)
    };
    enumSummarys = ['expectedSummary', 'countedSummary', 'differenceSummary', 'qtyToKeepSummary', 'qtyToDepoSummary'];
    enumConcepts = ['expected', 'counted', 'difference', 'qtyToKeep', 'foreignCounted'];
    enumSecondConcepts = ['foreignExpected', 'foreignCounted', 'foreignDifference', 'qtyToKeep', 'qtyToKeep'];
    for (counter = 0; counter < 5; counter++) {
      for (i = 0; i < this.get('paymentList').models.length; i++) {
        model = this.get('paymentList').models[i];
        if (!model.get(enumConcepts[counter])) {
          countCashSummary[enumSummarys[counter]].push({
            name: model.get('name'),
            value: 0,
            second: 0,
            isocode: ''
          });
        } else {
          switch (enumSummarys[counter]) {
          case 'qtyToKeepSummary':
            value = OB.DEC.mul(model.get(enumConcepts[counter]), model.get('rate'));
            second = model.get(enumSecondConcepts[counter]);
            break;
          case 'qtyToDepoSummary':
            if (model.get(enumSecondConcepts[counter]) !== null && model.get(enumSecondConcepts[counter]) !== undf && model.get('rate') !== '1') {
              second = OB.DEC.sub(model.get(enumConcepts[counter]), model.get(enumSecondConcepts[counter]));
            } else {
              second = OB.DEC.Zero;
            }
            if (model.get(enumSecondConcepts[counter]) !== null && model.get(enumSecondConcepts[counter]) !== undf) {
              value = OB.DEC.mul(OB.DEC.sub(model.get(enumConcepts[counter]), model.get(enumSecondConcepts[counter])), model.get('rate'));
            }

            break;
          default:
            value = model.get(enumConcepts[counter]);
            second = model.get(enumSecondConcepts[counter]);
          }
          countCashSummary[enumSummarys[counter]].push({
            name: model.get('name'),
            value: value,
            second: second,
            isocode: model.get('isocode')
          });
        }
      }
    }
    return countCashSummary;
  },
  processAndFinishCashUp: function () {
    var objToSend = {
      terminalId: OB.POS.modelterminal.get('terminal').id,
      cashUpId: OB.UTIL.get_UUID(),
      cashCloseInfo: []
    },
        server = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashClose'),
        me = this;

    OB.UTIL.showLoading(true);

    //Create the data that server expects
    _.each(this.get('paymentList').models, function (curModel) {
      var cashCloseInfo = {
        expected: 0,
        difference: 0,
        paymentTypeId: 0,
        paymentMethod: {}
      };
      cashCloseInfo.paymentTypeId = curModel.get('id');
      cashCloseInfo.difference = curModel.get('difference');
      cashCloseInfo.foreignDifference = curModel.get('foreignDifference');
      cashCloseInfo.expected = curModel.get('expected');
      cashCloseInfo.foreignExpected = curModel.get('foreignExpected');
      curModel.get('paymentMethod').amountToKeep = curModel.get('qtyToKeep');
      cashCloseInfo.paymentMethod = curModel.get('paymentMethod');
      objToSend.cashCloseInfo.push(cashCloseInfo);
    }, this);

    //ready to send to the server
    server.exec(objToSend, function (data) {
      if (data.error || data.exception) {
        OB.UTIL.showLoading(false);
        if (data.errorMessage) {
          me.set("errorMessage", data.errorMessage);
          me.set("errorDetail", data.errorDetail);
          me.set("errorNoNavigateToInitialScreen", data.errorNoNavigateToInitialScreen);
        }
        me.set("finishedWrongly", true);
      } else {
        // console.log("cash up processed correctly. -> show modal");
        OB.UTIL.showLoading(false);
        me.set('messages', data.messages);
        me.set('next', data.next);
        me.set("finished", true);
        if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashup')) {
          me.printCashUp.print(me.get('cashUpReport').at(0), me.getCountCashSummary());
        }
      }
    });
  },
  convertExpected: function () {
    _.each(this.get('paymentList').models, function (model) {
      model.set('foreignExpected', model.get('expected'));
      model.set('expected', OB.DEC.mul(model.get('expected'), model.get('rate')));
    }, this);
  }
});