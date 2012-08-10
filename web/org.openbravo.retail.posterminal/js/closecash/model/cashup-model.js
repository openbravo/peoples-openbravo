/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

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
  defaults : {
    step: OB.DEC.Zero,
    allowedStep: OB.DEC.Zero,
    totalExpected: OB.DEC.Zero,
    totalCounted: OB.DEC.Zero,
    totalDifference: OB.DEC.Zero
  },
  init: function() {
    this.orderlist= new Backbone.Collection();
    this.payList = this.getData('DataCloseCashPaymentMethod');
    this.cashUpReport = this.getData('DataCashCloseReport');
  }
});