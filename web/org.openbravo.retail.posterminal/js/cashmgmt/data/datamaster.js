/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, _ , $ */

(function () {

  var db, dbSize, dbSuccess, dbError, fetchTaxes;

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.Model = OB.Model || {};
  
  OB.Model.DepositsDrops = Backbone.Model.extend({
	  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops',
	  modelName: 'DataDepositsDrops',
	  online: true
  });
  
  OB.Model.CashMgmtPaymentMethod = Backbone.Model.extend({
	  source: 'org.openbravo.retail.posterminal.term.CashMgmtPayments',
	  modelName: 'DataCashMgmtPaymentMethod',
	  online: true
  });
  
  OB.Model.DropEvents = Backbone.Model.extend({
	  source: 'org.openbravo.retail.posterminal.term.CashMgmtDropEvents',
	  modelName: 'DataDropEvents',
	  online: true
  });
  
  OB.Model.DepositEvents = Backbone.Model.extend({
	  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositEvents',
	  modelName: 'DataDepositEvents',
	  online: true
  });
  // -------------------------------------------------------------------------------------------

  OB.DATA.DepositsDrops = function (context, id) {
    this._id = 'DataDepositsDrops';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.DepositsDrops.prototype, OB.DATA.Base);

  OB.DATA.CashMgmtPaymentMethod = function (context, id) {
    this._id = 'DataCashMgmtPaymentMethod';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtPayments', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.CashMgmtPaymentMethod.prototype, OB.DATA.Base);
  

  OB.DATA.DropEvents = function (context, id) {
    this._id = 'DataDropEvents';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtDropEvents', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.DropEvents.prototype, OB.DATA.Base);

  OB.DATA.DepositEvents = function (context, id) {
    this._id = 'DataDepositEvents';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CashMgmtDepositEvents', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.DepositEvents.prototype, OB.DATA.Base);

}());