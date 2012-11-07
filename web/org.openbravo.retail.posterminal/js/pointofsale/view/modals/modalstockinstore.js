/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
  myId: 'ModalStockInStore',
  published: {
    stockInfo: null
  },
  kind: 'OB.UI.Modal',
  modalClass: 'modal',
  headerClass: 'modal-header',
  bodyClass: 'modal-header',
  header: '',
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStore',
    name: 'stockDetailList'
  },
  executeOnHide: function () {
    this.stockInfo = null;
  },
  executeOnShow: function(args){
    this.setStockInfo(args);
  },
  stockInfoChanged: function (oldValue) {
    if (this.stockInfo) {
      this.$.divheader.setContent(this.stockInfo.get('product').get('_identifier') + ' (' + this.stockInfo.get('product').get('uOMsymbol') + ')');
      this.$.body.$.stockDetailList.setStockValuesPerWarehouse(this.stockInfo.get('warehouses'));
    } else {
      this.$.body.$.stockDetailList.setStockValuesPerWarehouse(null);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStore',
  classes: 'row-fluid',
  published: {
    stockValuesPerWarehouse: null
  },
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;'
    }, {
      components: [{
        name: 'scrollListStockDetails',
        kind: 'OB.UI.ScrollableTable',
        scrollAreaMaxHeight: '400px',
        renderLine: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.StockInStoreLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],
  stockValuesPerWarehouseChanged: function (oldValue) {
    if (this.stockValuesPerWarehouse) {
      this.$.scrollListStockDetails.setCollection(this.stockValuesPerWarehouse);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.StockInStoreLine',
  classes: 'btnselect',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      style: 'float: left; width: 60%; font-size: 20px;',
      name: 'warehouse'
    }, {
      style: 'float: left; width: 25%; font-weight: bold; font-size: 20px;',
      name: 'quantity'
    }, {
      style: 'clear: both'
    }]
  }, {
    components: [{
      style: 'padding: 5px; padding-left: 10px;',
      name: 'detail',
      allowHtml: true
    }]
  }],
  create: function () {
    var str = "";
    this.inherited(arguments);
    this.$.warehouse.setContent(this.model.get('warehousename'));
    this.$.quantity.setContent(this.model.get('warehouseqty'));
    this.model.get('bins').each(function (model) {
      str += '<b>' + model.get('binqty') + '</b> - ' + model.get('binname') + '<br/>';
    });
    this.$.detail.setContent(str);
  }
});