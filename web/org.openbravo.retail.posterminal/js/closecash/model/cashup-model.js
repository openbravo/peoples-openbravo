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
  init: function () {
    var me = this;

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('payList', this.getData('DataCloseCashPaymentMethod'));
    this.set('cashUpReport', this.getData('DataCashCloseReport'));

    this.set('totalExpected', _.reduce(this.get('payList').models, function (total, model) {
      return OB.DEC.add(total, model.get('expected'));
    }, 0));

    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'N'
    }, function (pendingOrderList) {
      me.get('orderlist').reset(pendingOrderList.models);
    }, OB.UTIL.showError);
  }
});