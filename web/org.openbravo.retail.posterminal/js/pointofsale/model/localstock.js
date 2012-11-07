/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone _ */

(function () {

  OB = window.OB || {};
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.UsedModels = OB.OBPOSPointOfSale.UsedModels || {};

  OB.OBPOSPointOfSale.UsedModels.LocalStock = Backbone.Model.extend({
    defaults: {
      product: OB.Model.Product,
      qty: 0,
      warehouses: OB.OBPOSPointOfSale.UsedModels.WarehouseStockDetailList
    },
    initialize: function (serverData) {
      var warehouses;
      if (serverData.product) {
        this.set('product', serverData.product);
      }
      this.set('qty', serverData.qty);
      warehouses = new OB.OBPOSPointOfSale.UsedModels.WarehouseStockDetailList(serverData.warehouses);
      this.set('warehouses', warehouses);
    },
    getWarehouseById: function (warId) {
      return _.find(this.get('warehouses').models, function (whmodel) {
        if (whmodel.get('warehouseid') === warId) {
          return warId;
        }
      });
    }
  });

  OB.OBPOSPointOfSale.UsedModels.WarehouseStockDetail = Backbone.Model.extend({
    defaults: {
      warehouseid: null,
      warehousename: null,
      warehouseqty: 0,
      bins: OB.OBPOSPointOfSale.UsedModels.BinStockDetailList
    },
    initialize: function (warehouse) {
      var bins;
      bins = new OB.OBPOSPointOfSale.UsedModels.BinStockDetailList(warehouse.bins);
      this.set('warehouseid', warehouse.warehouseid);
      this.set('warehousename', warehouse.warehousename);
      this.set('warehouseqty', warehouse.warehouseqty);
      this.set('bins', bins);
    }
  });

  OB.OBPOSPointOfSale.UsedModels.BinStockDetail = Backbone.Model.extend({
    defaults: {
      binid: null,
      binname: null,
      binqty: 0
    },
    initialize: function (bin) {
      this.set('binid', bin.binid);
      this.set('binname', bin.binname);
      this.set('binqty', bin.binqty);
    }
  });

  OB.OBPOSPointOfSale.UsedModels.WarehouseStockDetailList = Backbone.Collection.extend({
    model: OB.OBPOSPointOfSale.UsedModels.WarehouseStockDetail
  });

  OB.OBPOSPointOfSale.UsedModels.BinStockDetailList = Backbone.Collection.extend({
    model: OB.OBPOSPointOfSale.UsedModels.BinStockDetail
  });
}());