/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStoreClickable',
  myId: 'ModalStockInStoreClickable',
  kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStoreClickable',
    name: 'stockDetailListClickable'
  },
  stockInfoChanged: function (oldValue) {
    if (this.stockInfo) {
      this.$.header.setContent(this.stockInfo.get('product').get('_identifier') + ' (' + this.stockInfo.get('product').get('uOMsymbol') + ')');
      this.$.body.$.stockDetailListClickable.setStockValuesPerWarehouse(this.stockInfo.get('warehouses'));
    } else {
      this.$.body.$.stockDetailListClickable.setStockValuesPerWarehouse(null);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStoreClickable',
  kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.ListStockInStore',
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;'
    }, {
      components: [{
        name: 'scrollListStockDetailsClickable',
        kind: 'OB.UI.ScrollableTable',
        scrollAreaMaxHeight: '400px',
        renderLine: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.StockInStoreLineClickable',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],
  stockValuesPerWarehouseChanged: function (oldValue) {
    if (this.stockValuesPerWarehouse) {
      this.$.scrollListStockDetailsClickable.setCollection(this.stockValuesPerWarehouse);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.StockInStoreLineClickable',
  kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore.Components.StockInStoreLine',
  classes: 'stockinstorelines',
  events: {
    onHideThisPopup: '',
    onWarehouseSelected: ''
  },
  tap: function () {
    this.doHideThisPopup();
    this.doWarehouseSelected({
      warehouseid: this.model.get('warehouseid'),
      warehousename: this.model.get('warehousename'),
      warehouseqty: this.model.get('warehouseqty')
    });
  }
});