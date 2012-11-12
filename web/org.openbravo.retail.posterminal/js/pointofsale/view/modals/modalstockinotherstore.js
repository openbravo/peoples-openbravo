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
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores',
  myId: 'ModalStockInOtherStores',
  published: {
    stockInfo: null
  },
  kind: 'OB.UI.Modal',
  header: '',
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.ListStockInOtherStores',
    name: 'stockDetailList'
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
      this.$.body.$.stockDetailList.setStockValuesOtherStores(this.stockInfo.get('organizations'));
    } else {
      this.$.body.$.stockDetailList.setStockValuesOtherStores(null);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.ListStockInOtherStores',
  classes: 'row-fluid',
  published: {
    stockValuesOtherStores: null
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
        renderLine: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.StockInOtherStoresLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],
  stockValuesOtherStoresChanged: function (oldValue) {
    this.$.scrollListStockDetails.setCollection(this.stockValuesOtherStores);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.StockInOtherStoresLine',
  classes: 'btnselect',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      style: 'float: left; width: 60%; font-size: 20px;',
      name: 'organization'
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
    this.$.organization.setContent(this.model.get('organizationname'));
    this.$.quantity.setContent(this.model.get('organizationqty'));
    this.model.get('warehouses').each(function (model) {
      str += '<b>' + model.get('warehouseqty') + ' </b> - ' + model.get('warehousename') + '<br/>';
    });
    this.$.detail.setContent(str);

  }
});