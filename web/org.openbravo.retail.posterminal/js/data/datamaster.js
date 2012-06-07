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

  OB.DATA.BPs = function (context) {
    this._id = 'DataBPs';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
      'org.openbravo.retail.posterminal.master.BusinessPartner',
      OB.POS.modelterminal.get('terminal').client,
      OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.BPs.prototype, OB.DATA.Base);

  OB.DATA.ProductPrice = function (context, id) {
    this._id = 'DataProductPrice';
    this.context = context;
    this.ds = new OB.DS.DataSourceProductPrice(
      new OB.DS.Query(
      'org.openbravo.retail.posterminal.master.Product',
      OB.POS.modelterminal.get('terminal').client,
      OB.POS.modelterminal.get('terminal').organization),
      new OB.DS.Query(
      'org.openbravo.retail.posterminal.master.ProductPrice',
      OB.POS.modelterminal.get('terminal').client,
      OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {product: {}, productprice: {}};
  };
 _.extend(OB.DATA.ProductPrice.prototype, OB.DATA.Base);

  OB.DATA.Category = function (context, id) {
    this._id = 'DataCategory';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
        'org.openbravo.retail.posterminal.master.Category'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.Category.prototype, OB.DATA.Base);

  OB.DATA.TaxRate = function (context, id) {
    this._id = 'DataTaxRate';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
        'org.openbravo.retail.posterminal.master.TaxRate'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.TaxRate.prototype, OB.DATA.Base);

  OB.DATA.PaymentMethod = function (context) {
    this._id = 'DataPaymentMethod';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
      'org.openbravo.retail.posterminal.term.Payments',
      OB.POS.modelterminal.get('terminal').client,
      OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.PaymentMethod.prototype, OB.DATA.Base);

  // Offline based on WebSQL

  if(!window.openDatabase) {
    window.console.error("Your browser doesn't support WebSQL");
    return;
  }

  dbSuccess = function () {};

  dbError = function () {
    window.console.error(arguments);
  };

  dbSize = 10 * 1024 * 1024; // 10MB

  // Open database
  db = window.openDatabase('WEBPOS', '0.1', 'Openbravo Web POS', dbSize);

  // TODO: handle schema changes with changeVersion
  db.transaction(function (tx) {
    tx.executeSql('DROP TABLE IF EXISTS c_tax', null, dbSuccess, dbError);
  });

  fetchTaxes = function () {
    $.ajax({
      url: '../../org.openbravo.retail.posterminal.datasource/FinancialMgmtTaxRate',
      dataType: 'json',
      cache: false,

      error: function (jqXHR, textStatus, errorThrown) {
        window.console.log(arguments);
      },

      success: function (data, textStatus, jqXHR) {
        var q = 'INSERT INTO c_tax(c_tax_id, name, c_taxcategory_id, c_bp_taxcategory_id, rate, idx) VALUES (?,?,?,?,?,?)',
            idx = 0;
        db.transaction(function (tx) {
          _.each(data.response.data, function (item) {
            tx.executeSql(q, [item.id, item.name, item.taxCategory, item.businessPartnerTaxCategory, item.rate, idx], dbSuccess, dbError);
            idx++;
          });
        }, dbError, dbSuccess);
      }
    });
  };

  db.transaction(function (tx) {
    tx.executeSql('CREATE TABLE IF NOT EXISTS c_tax(c_tax_id TEXT PRIMARY KEY, name TEXT, c_taxcategory_id TEXT, c_bp_taxcategory_id TEXT, rate NUMERIC, idx NUMERIC)',
      null, dbSuccess, dbError);
  }, dbError, fetchTaxes);

  OB.DATA.OfflineDB = db;
});