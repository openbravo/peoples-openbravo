/*global define,_,$*/

define(['utilities', 'datasource'], function () {

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
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.master.BusinessPartner', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
    this.loadparams = {};
  };
  _.extend(OB.DATA.BPs.prototype, OB.DATA.Base);

  OB.DATA.ProductPrice = function (context, id) {
    this._id = 'DataProductPrice';
    this.context = context;
    this.ds = new OB.DS.DataSourceProductPrice(
    new OB.DS.Query('org.openbravo.retail.posterminal.master.Product', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization), new OB.DS.Query('org.openbravo.retail.posterminal.master.ProductPrice', OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization));
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

  OB.DATA.TaxRate = function (context, id) {
    this._id = 'DataTaxRate';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query('org.openbravo.retail.posterminal.master.TaxRate'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.TaxRate.prototype, OB.DATA.Base);


  // Offline based on WebSQL
  if (!window.openDatabase) {
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

  fetchData = function (modelProto) {
    $.ajax({
      url: '../../org.openbravo.retail.posterminal.datasource/' + modelProto.entityName,
      dataType: 'json',
      cache: false,

      error: function (jqXHR, textStatus, errorThrown) {
        window.console.log(arguments);
      },

      success: function (data, textStatus, jqXHR) {
        var _idx = 0;
        db.transaction(function (tx) {
          _.each(data.response.data, function (item) {
            var values = [];
            _.each(modelProto.properties, function (prop) {
              if ('_idx' === prop) {
                return;
              }
              values.push(item[prop]);
            });
            values.push(_idx);
            //console.log(values.length);
            tx.executeSql(modelProto.insertStatement, values, dbSuccess, dbError);
            _idx++;
          });
        }, dbError, dbSuccess);
      }
    });
  };

  // TODO: handle schema changes with changeVersion
  db.transaction(function (tx) {
    _.each(_.keys(OB.Model), function (m) {
      var proto = OB.Model[m].prototype;
      tx.executeSql(proto.dropStatement, null, dbSuccess, dbError);
    });
  });

  _.each(_.keys(OB.Model), function (m) {
    var proto = OB.Model[m].prototype;
    db.transaction(function (tx) {
      tx.executeSql(proto.createStatement, null, dbSuccess, dbError);
    }, dbError, function (t) {
      fetchData(proto);
    });
  });

  OB.DATA.OfflineDB = db;
});