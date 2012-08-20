/*global OB, Backbone, _ */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
    pendingOrdersToProcess: false
  },
  init: function() {
    this.arePendingOrdersToBeProcess();
    this.set('step', 1);
    //Because step 3 is divided in several steps.
    this.set('stepOfStep3', 0);

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('paymentList', this.getData('DataCloseCashPaymentMethod'));

    this.set('cashUpReport', this.getData('DataCashCloseReport'));

    this.set('totalExpected', _.reduce(this.get('paymentList').models, function(total, model) {
      return OB.DEC.add(total, model.get('expected'));
    }, 0));
    this.set('totalDifference', OB.DEC.sub(this.get('totalDifference'), this.get('totalExpected')));
    this.set('totalCounted', 0);

    this.get('paymentList').on('change:counted', function(mod) {
      mod.set('difference', OB.DEC.sub(mod.get('counted'), mod.get('expected')));
      this.set('totalCounted', _.reduce(this.get('paymentList').models, function(total, model) {
        return model.get('counted') ? OB.DEC.add(total, model.get('counted')) : total;
      }, 0),
      0);
      //TODO
      if (mod.get('counted') === OB.DEC.Zero){
        this.trigger('change:totalCounted');
      }
    }, this);

    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'N'
    }, function(pendingOrderList, me) {
      me.get('orderlist').reset(pendingOrderList.models);
    }, OB.UTIL.showError, this);
  },
  arePendingOrdersToBeProcess: function(){
    OB.Dal.find(OB.Model.Order, {hasbeenpaid:'Y'}, function (fetchedOrderList, me) { //OB.Dal.find success
      var currentOrder = {};
      if (fetchedOrderList && fetchedOrderList.length !== 0) {
        me.set('pendingOrdersToProcess', true);
      }
    }, function () { //OB.Dal.find error
    }, this)
  },
  allowNext: function() {
    var step = this.get('step'),
        unfd;

    if (step === 1 && this.get('orderlist').length === 0  && !this.get('pendingOrdersToProcess')) {
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
    
    if (step === 4){
      this.get('cashUpReport').at(0).set('time',new Date());
      return true;
    }

    return false;
  },
  allowPrevious: function() {
    var step = this.get('step');

    if (step === 1) {
      return false;
    }
    return true;
  },
  isValidCashKeep: function() {
    var unfd;
    if (this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep') !== unfd && this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep') !== null) {
      if ($.isNumeric(this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep'))) {
        if (this.get('paymentList').at(this.get('stepOfStep3')).get('counted') >= this.get('paymentList').at(this.get('stepOfStep3')).get('qtyToKeep')) {
          return true;
        }
      }
    }
    return false;
  },
  validateCashKeep: function(qty) {
    var unfd, result = {
      result: false,
      message: ''
    };
    if (qty !== unfd && qty !== null && $.isNumeric(qty)) {
      if (this.get('paymentList').at(this.get('stepOfStep3')).get('counted') >= qty) {
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
  showPendingOrdersList: function() {
    return this.get('step') === 1;
  },
  showPaymentMethodList: function() {
    return this.get('step') === 2;
  },
  showCashToKeep: function() {
    return this.get('step') === 3;
  },
  showPostPrintClose: function() {
    return this.get('step') === 4;
  },

  // Step 2: logic, expected vs counted 
  countAll: function() {
    this.get('paymentList').each(function(model) {
      model.set('counted', OB.DEC.add(0, model.get('expected')));
    });
  },
  isAllCounted: function() {
    return _.reduce(this.get('paymentList').models, function(allCounted, model) {
      return allCounted && model.get('counted');
    }, true);
  },
  getCountCashSummary: function() {
    var countCashSummary, counter, enumConcepts, enumSummarys;
    countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      totalCounted: this.get('totalCounted') ,
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference')
    };
    enumSummarys = ['expectedSummary', 'countedSummary', 'differenceSummary'];
    enumConcepts = ['expected', 'counted', 'difference'];
    for (counter = 0; counter < 3; counter++) {
      _.each(this.get('paymentList').models, function(curModel) {
        if(!curModel.get(enumConcepts[counter])){
          countCashSummary[enumSummarys[counter]].push({
            name: curModel.get('name'),
            value: 0
          });
        }else{
        countCashSummary[enumSummarys[counter]].push({
          name: curModel.get('name'),
          value: curModel.get(enumConcepts[counter])
        });
        }
      }, this);
    }
    return countCashSummary;
  },
  printCashUp: function(){
    // Printing: TODO: refactor this when HW Manager is changed
    hwManager = new OB.COMP.HWManager({});
    hwManager.templatecashup = new OB.COMP.HWResource('res/printcashup.xml');
    hwManager.modeldaycash = {report: this.get('cashUpReport').at(0),
        summary: this.getCountCashSummary()};
    hwManager.printCashUp();
  },
processAndFinishCashUp: function(){
  //this._id = 'paymentCloseCash';
  var objToSend = {
      terminalId : OB.POS.modelterminal.get('terminal').id,
      cashCloseInfo : []
  },
  server = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashClose'),
  me = this;
  
  //Create the data that server expects
  _.each(this.get('paymentList').models, function(curModel) {
    var cashCloseInfo = {
        expected: 0,
        difference: 0,
        paymentTypeId: 0,
        paymentMethod: {}
      };
    cashCloseInfo.paymentTypeId = curModel.get('id');
    cashCloseInfo.difference = curModel.get('difference');
    cashCloseInfo.expected = curModel.get('expected');
    curModel.get('paymentMethod').amountToKeep = curModel.get('qtyToKeep');
    cashCloseInfo.paymentMethod = curModel.get('paymentMethod');
    objToSend.cashCloseInfo.push(cashCloseInfo);
  },this);
  this.printCashUp();
  console.log('Object to send to the server: ');
  console.log(objToSend);
  //ready to send to the server
  server.exec(objToSend, function(data, message){
    if (data && data.exception) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgFinishCloseError'));
    } else {
      console.log("cash up processed correctly. -> show modal");
      me.set("finished", true);//$('#modalFinishClose').modal('show');
    }
  });
}
});