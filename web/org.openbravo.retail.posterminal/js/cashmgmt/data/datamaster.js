/*global B, Backbone, _ , $ */

(function () {

  var db, dbSize, dbSuccess, dbError, fetchTaxes;

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.Container = function (context) {
    this.context = context;
    this.datachildren = [];
  };

  OB.DATA.Container.prototype.append = function (child) {
    this.datachildren.push(child);
  };

  OB.DATA.Container.prototype.inithandler = function () {
    var i, max;
    for (i = 0, max = this.datachildren.length; i < max; i++) {
      this.datachildren[i].load();
    }
  };

  OB.DATA.Base = {
    load: function () {
       this.ds.load(this.loadparams);
    }
  };

  OB.DATA.DepositsDrops = function (context, id) {
	    this._id = 'DataDepositsDrops';
	    this.context = context;
	    this.ds = new OB.DS.DataSource(new OB.DS.Query(
	      'org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops',
	      OB.POS.modelterminal.get('terminal').client,
	      OB.POS.modelterminal.get('terminal').organization));
	    this.loadparams = {pos: OB.POS.modelterminal.get('terminal').id};
	  };
_.extend(OB.DATA.DepositsDrops.prototype, OB.DATA.Base);

OB.DATA.CashMgmtPaymentMethod = function (context, id) {
  this._id = 'DataCashMgmtPaymentMethod';
  this.context = context;
  this.ds = new OB.DS.DataSource(new OB.DS.Query(
    'org.openbravo.retail.posterminal.term.CashMgmtPayments',
    OB.POS.modelterminal.get('terminal').client,
    OB.POS.modelterminal.get('terminal').organization));
  this.loadparams = {pos: OB.POS.modelterminal.get('terminal').id};
};
_.extend(OB.DATA.CashMgmtPaymentMethod.prototype, OB.DATA.Base);

OB.DATA.DropEvents = function (context, id) {
  this._id = 'DataDropEvents';
  this.context = context;
  this.ds = new OB.DS.DataSource(new OB.DS.Query(
    'org.openbravo.retail.posterminal.term.CashMgmtDropEvents',
    OB.POS.modelterminal.get('terminal').client,
    OB.POS.modelterminal.get('terminal').organization));
  this.loadparams = {};
};
_.extend(OB.DATA.DropEvents.prototype, OB.DATA.Base);

OB.DATA.DepositEvents = function (context, id) {
  this._id = 'DataDepositEvents';
  this.context = context;
  this.ds = new OB.DS.DataSource(new OB.DS.Query(
    'org.openbravo.retail.posterminal.term.CashMgmtDepositEvents',
    OB.POS.modelterminal.get('terminal').client,
    OB.POS.modelterminal.get('terminal').organization));
  this.loadparams = {};
};
_.extend(OB.DATA.DepositEvents.prototype, OB.DATA.Base);

  if(!window.openDatabase) {
    window.console.error("Your browser doesn't support WebSQL");
    return;
  }

}());