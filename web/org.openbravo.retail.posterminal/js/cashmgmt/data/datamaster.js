/*global B, Backbone, _ , $ */

(function () {

  var db, dbSize, dbSuccess, dbError, fetchTaxes;

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DepositsDrops = function (context, id) {
    this._id = 'DataDepositsDrops';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.DepositsDrops.prototype, OB.DATA.Base);

  OB.DATA.CashMgmtPaymentMethod = function (context, id) {
    this._id = 'DataCashMgmtPaymentMethod';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.term.CashMgmtPayments', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.CashMgmtPaymentMethod.prototype, OB.DATA.Base);

  OB.DATA.DropEvents = function (context, id) {
    this._id = 'DataDropEvents';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.term.CashMgmtDropEvents', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.DropEvents.prototype, OB.DATA.Base);

  OB.DATA.DepositEvents = function (context, id) {
    this._id = 'DataDepositEvents';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.term.CashMgmtDepositEvents', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.DepositEvents.prototype, OB.DATA.Base);

}());