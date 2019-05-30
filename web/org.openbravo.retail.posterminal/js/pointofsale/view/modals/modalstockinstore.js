/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
  kind: 'OB.UI.Modal',
  classes: 'obObposPointOfSaleUiModalsModalStockInStore',
  myId: 'ModalStockInStore',
  published: {
    stockInfo: null
  },
  header: '',
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStore',
    name: 'stockDetailList',
    classes: 'obObposPointOfSaleUiModalsModalStockInStore-body-stockDetailList'
  },
  executeOnHide: function () {
    this.stockInfo = null;
  },
  executeOnShow: function () {
    this.setStockInfo(this.args.stockInfo);
  },
  stockInfoChanged: function (oldValue) {
    if (this.stockInfo) {
      this.$.header.setContent(this.stockInfo.get('product').get('_identifier') + ' (' + this.stockInfo.get('product').get('uOMsymbol') + ')');
      this.$.body.$.stockDetailList.setStockValuesPerWarehouse(this.stockInfo.get('warehouses'));
    } else {
      this.$.body.$.stockDetailList.setStockValuesPerWarehouse(null);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStore',
  classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsListStockInStore row-fluid',
  published: {
    stockValuesPerWarehouse: null
  },
  components: [{
    classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsListStockInStore-container1 span12',
    components: [{
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsListStockInStore-container1-element1'
    }, {
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsListStockInStore-container1-container2',
      components: [{
        name: 'scrollListStockDetails',
        kind: 'OB.UI.ScrollableTable',
        classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsListStockInStore-container1-container2-scrollListStockDetails',
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
  classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine',
  components: [{
    name: 'line',
    classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-line',
    components: [{
      name: 'warehouse',
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-line-warehouse'
    }, {
      name: 'quantity',
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-line-quantity'
    }, {
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-line-container3 u-clearBoth'
    }]
  }, {
    classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-container2',
    components: [{
      name: 'detail',
      classes: 'obObposPointOfSaleUiModalsModalStockInStoreComponentsStockInStoreLine-container2-detail',
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