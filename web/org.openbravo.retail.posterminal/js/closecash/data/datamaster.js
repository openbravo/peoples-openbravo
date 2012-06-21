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

  OB.DATA.PaymentMethod = function (context, id) {
	    this._id = 'DataPaymentMethod';
	    this.context = context;
	    this.ds = new OB.DS.DataSource(new OB.DS.Query(
	      'org.openbravo.retail.posterminal.term.Payments',
	      OB.POS.modelterminal.get('terminal').client,
	      OB.POS.modelterminal.get('terminal').organization));
	    this.loadparams = {pos: OB.POS.modelterminal.get('terminal').id};
	  };
  _.extend(OB.DATA.PaymentMethod.prototype, OB.DATA.Base);

  OB.DATA.CloseCashPaymentMethod = function (context, id) {
	    this._id = 'DataCloseCashPaymentMethod';
	    this.context = context;
	    this.ds = new OB.DS.DataSource(new OB.DS.Query(
	      'org.openbravo.retail.posterminal.term.CloseCashPayments',
	      OB.POS.modelterminal.get('terminal').client,
	      OB.POS.modelterminal.get('terminal').organization));
	    this.loadparams = {pos: OB.POS.modelterminal.get('terminal').id};
	  };
_.extend(OB.DATA.CloseCashPaymentMethod.prototype, OB.DATA.Base);

//  OB.MODEL.Payment = Backbone.Model.extend({});
//
//  OB.MODEL.PaymentList = Backbone.Collection.extend({
//    model: OB.MODEL.Payment,
//    url: '../../org.openbravo.retail.posterminal.datasource/OBPOS_App_Payment',
//    parse: function (response, error) {
//      if (response && response.response) {
//        return response.response.data;
//      }
//    }
//  });

  //OB.MODEL.Payments = new OB.MODEL.PaymentList;
  // Offline based on WebSQL

  if(!window.openDatabase) {
    window.console.error("Your browser doesn't support WebSQL");
    return;
  }

}());