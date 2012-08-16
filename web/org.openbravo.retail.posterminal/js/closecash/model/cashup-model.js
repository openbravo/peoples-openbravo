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
    totalDifference: OB.DEC.Zero
  },
  init: function() {
    var me = this;

    this.set('step', 1);
    this.set('stepOfStep3', 0);

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('paymentList', this.getData('DataCloseCashPaymentMethod'));
    this.set('cashUpReport', this.getData('DataCashCloseReport'));

    this.set('totalExpected', _.reduce(this.get('paymentList').models, function(total, model) {
      return OB.DEC.add(total, model.get('expected'));
    }, 0));
    this.set('totalCounted', 0);

    this.get('paymentList').on('change:counted', function() {
      this.set('totalCounted', _.reduce(this.get('paymentList').models, function(total, model) {
        return model.get('counted') ? OB.DEC.add(total, model.get('counted')) : total;
      }, 0));
    }, this);

    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'N'
    }, function(pendingOrderList) {
      me.get('orderlist').reset(pendingOrderList.models);
    }, OB.UTIL.showError);
  },
  allowNext: function() {
    var step = this.get('step');

    if (step === 1 && this.get('orderlist').length === 0) {
      return true;
    }

    if (step === 2 && this.isAllCounted()) {
      //TODO: review logic
      return true;
    }
    
    //if (step === 3 && this.get('paymentList').at(this.get('stepOfStep3')).get('kept')) {
    if (step===3){
    //TODO: review logic
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
  }
});