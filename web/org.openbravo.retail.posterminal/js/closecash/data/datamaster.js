/*global define,_,$*/

define(['utilities', 'datasource'], function () {

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

  OB.MODEL.Payment = Backbone.Model.extend({});

  OB.MODEL.PaymentList = Backbone.Collection.extend({
    model: OB.MODEL.Payment,
    url: '../../org.openbravo.retail.posterminal.datasource/OBPOS_App_Payment',
    parse: function (response, error) {
      if (response && response.response) {
        return response.response.data;
      }
    }
  });

  //OB.MODEL.Payments = new OB.MODEL.PaymentList;
  // Offline based on WebSQL

  if(!window.openDatabase) {
    window.console.error("Your browser doesn't support WebSQL");
    return;
  }

});