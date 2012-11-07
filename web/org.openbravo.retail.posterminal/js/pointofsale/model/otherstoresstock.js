/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.UsedModels = OB.OBPOSPointOfSale.UsedModels || {};

  OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock = Backbone.Model.extend({
    defaults: {
      product: OB.Model.Product,
      qty: 0,
      organizations: OB.OBPOSPointOfSale.UsedModels.OrganizationStockList
    },
    initialize: function (serverData) {
      var organizations;
      if (serverData.product) {
        this.set('product', serverData.product);
      }
      this.set('qty', serverData.qty);
      organizations = new OB.OBPOSPointOfSale.UsedModels.OrganizationStockList(serverData.organizations);
      this.set('organizations', organizations);
    }
  });

  OB.OBPOSPointOfSale.UsedModels.OrganizationStock = Backbone.Model.extend({
    defaults: {
      organizationid: null,
      organizationname: null,
      organizationqty: 0,
      warehouses: OB.OBPOSPointOfSale.UsedModels.OrganizationWarehousesList
    },
    initialize: function (organization) {
      var warehouses;
      warehouses = new OB.OBPOSPointOfSale.UsedModels.OrganizationWarehousesList(organization.warehouses);
      this.set('organizationid', organization.organizationid);
      this.set('organizationname', organization.organizationname);
      this.set('organizationqty', organization.organizationqty);
      this.set('warehouses', warehouses);
    }
  });

  OB.OBPOSPointOfSale.UsedModels.OrganizationWarehouses = Backbone.Model.extend({
    defaults: {
      warehouseid: null,
      warehousename: null,
      warehouseqty: 0
    },
    initialize: function (warehouse) {
      this.set('warehouseid', warehouse.warehouseid);
      this.set('warehousename', warehouse.warehousename);
      this.set('warehouseqty', warehouse.warehouseqty);
    }
  });

  OB.OBPOSPointOfSale.UsedModels.OrganizationStockList = Backbone.Collection.extend({
    model: OB.OBPOSPointOfSale.UsedModels.OrganizationStock
  });

  OB.OBPOSPointOfSale.UsedModels.OrganizationWarehousesList = Backbone.Collection.extend({
    model: OB.OBPOSPointOfSale.UsedModels.OrganizationWarehouses
  });

}());