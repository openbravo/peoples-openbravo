/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, $ */

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
OB.OBPOSCashUp.Model.CashUp = OB.Model.WindowModel.extend({
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
  init: function () {
    //Check for orders wich are being processed in this moment.
    //cancel -> back to point of sale
    //Ok -> Continue closing without these orders
    var undf;
    this.arePendingOrdersToBeProcess();

    //steps
    this.set('step', 1);
    //Because step 3 is divided in several steps.
    this.set('stepOfStep3', 0);

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
    var step = this.get('step'),
        unfd;

    if (step === 1 && this.get('orderlist').length === 0 && !this.get('pendingOrdersToProcess')) {
      return true;
    }

    if (step === 2 && this.isAllCounted()) {
      //TODO: review logic
      return true;
    }

    if (step === 3 && this.isValidCashKeep()) {
      //TODO: review logic
      return true;
    }

    if (step === 4) {
      this.get('cashUpReport').at(0).set('time', new Date());
      return true;
    }

    return false;
  },
  allowPrevious: function () {
    var step = this.get('step');

    if (step === 1) {
      return false;
    }
    return true;
  },
  setIgnoreStep3: function () {
    var result = null;
    _.each(this.get('paymentList').models, function (model) {
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
    }, this);
    this.set('ignoreStep3', result);
  },
  isStep3Needed: function (stepOfStep3) {
    return (this.get('paymentList').at(stepOfStep3).get('paymentMethod').automatemovementtoother === false) ? false : true;
  },
  showPendingOrdersList: function () {
    return this.get('step') === 1;
  },
  showPaymentMethodList: function () {
    return this.get('step') === 2;
  },
  showCashToKeep: function () {
    return this.get('step') === 3;
  },
  showPostPrintClose: function () {
    return this.get('step') === 4;
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
  isAllCounted: function () {
    var udfn;
    return _.reduce(this.get('paymentList').models, function (allCounted, model) {
      return allCounted && model.get('counted') !== null && model.get('counted') !== udfn;
    }, true);
  },
  //step 3
  validateCashKeep: function (qty) {
    var unfd, result = {
      result: false,
      message: ''
    };
    if (qty !== unfd && qty !== null && $.isNumeric(qty)) {
      if (this.get('paymentList').at(this.get('stepOfStep3')).get('foreignCounted') >= qty) {
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
      this.get('paymentList').at(this.get('stepOfStep3')).set('qtyToKeep', null);
    }
    return result;
  },
  isValidCashKeep: function () {
    var unfd;
    if (this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep') !== unfd && this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep') !== null) {
      if ($.isNumeric(this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep'))) {
        if (this.get('paymentList').at(this.get('stepOfStep3')).get('foreignCounted') >= this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep')) {
          return true;
        }
      }
    }
    return false;
  },
  //Step 4
  getCountCashSummary: function () {
    var countCashSummary, counter, enumConcepts, enumSummarys, i;
    countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      totalCounted: this.get('totalCounted'),
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference')
    };
    enumSummarys = ['expectedSummary', 'countedSummary', 'differenceSummary'];
    enumConcepts = ['expected', 'counted', 'difference'];
    for (counter = 0; counter < 3; counter++) {
      for (i = 0; i < this.get('paymentList').models.length; i++) {
        if (!this.get('paymentList').models[i].get(enumConcepts[counter])) {
          countCashSummary[enumSummarys[counter]].push({
            name: this.get('paymentList').models[i].get('name'),
            value: 0,
            foreignExpected: 0,
            foreignCounted: 0,
            foreignDifference: 0,
            isocode: this.get('paymentList').models[i].get('isocode')
          });
        } else {
          countCashSummary[enumSummarys[counter]].push({
            name: this.get('paymentList').models[i].get('name'),
            value: this.get('paymentList').models[i].get(enumConcepts[counter]),
            foreignExpected: this.get('paymentList').models[i].get('foreignExpected'),
            foreignCounted: this.get('paymentList').models[i].get('foreignCounted'),
            foreignDifference: this.get('paymentList').models[i].get('foreignDifference'),
            isocode: this.get('paymentList').models[i].get('isocode')
          });
        }
      }
    }
    return countCashSummary;
  },
  processAndFinishCashUp: function () {
    var objToSend = {
      terminalId: OB.POS.modelterminal.get('terminal').id,
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
      if (data.error) {
        OB.UTIL.showLoading(false);
        me.set("finishedWrongly", true);
      } else {
        // console.log("cash up processed correctly. -> show modal");
        OB.UTIL.showLoading(false);
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