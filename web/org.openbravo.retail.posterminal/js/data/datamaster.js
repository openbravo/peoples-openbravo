/*global B, _, $*/

(function () {

  var db, dbSize, dbSuccess, dbError, fetchData;

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

  OB.DATA.BPs = function (context) {
    this._id = 'DataBPs';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.master.BusinessPartner', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').store));
    this.loadparams = {};
  };
  _.extend(OB.DATA.BPs.prototype, OB.DATA.Base);

  OB.DATA.ProductPrice = function (context, id) {
    this._id = 'DataProductPrice';
    this.context = context;
    this.ds = new OB.DS.DataSourceProductPrice(
    new OB.DS.Query('org.openbravo.retail.posterminal.master.Product', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').store), new OB.DS.Query('org.openbravo.retail.posterminal.master.ProductPrice', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').store));
    this.loadparams = {
      product: {},
      productprice: {}
    };
  };
  _.extend(OB.DATA.ProductPrice.prototype, OB.DATA.Base);

  OB.DATA.Category = function (context, id) {
    this._id = 'DataCategory';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.master.Category'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.Category.prototype, OB.DATA.Base);

}());