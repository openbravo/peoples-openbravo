/*global B, Backbone, _ , $ */

(function () {

  OB.DATA.PaymentMethod = function (context, id) {
    this._id = 'DataPaymentMethod';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.Payments', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.PaymentMethod.prototype, OB.DATA.Base);

  OB.DATA.CloseCashPaymentMethod = function (context, id) {
    this._id = 'DataCloseCashPaymentMethod';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CloseCashPayments', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.CloseCashPaymentMethod.prototype, OB.DATA.Base);

  OB.DATA.CashCloseReport = function (context, id) {
    this._id = 'DataCashCloseReport';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Request('org.openbravo.retail.posterminal.term.CashCloseReport', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {
      pos: OB.POS.modelterminal.get('terminal').id
    };
  };
  _.extend(OB.DATA.CashCloseReport.prototype, OB.DATA.Base);

}());