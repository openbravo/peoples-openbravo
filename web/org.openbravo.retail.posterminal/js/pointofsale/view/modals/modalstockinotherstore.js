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
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores',
  classes: 'obObposPointOfSaleUiModalsModalStockInOtherStores',
  myId: 'ModalStockInOtherStores',
  published: {
    stockInfo: null
  },
  kind: 'OB.UI.Modal',
  header: '',
  body: {
    kind:
      'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.ListStockInOtherStores',
    name: 'stockDetailList',
    classes: 'obObposPointOfSaleUiModalsModalStockInOtherStores-stockDetailList'
  },
  executeOnHide: function() {
    this.stockInfo = null;
  },
  executeOnShow: function() {
    this.setStockInfo(this.args.stockInfo);
  },
  stockInfoChanged: function(oldValue) {
    if (this.stockInfo) {
      this.$.header.setContent(
        this.stockInfo.get('product').get('_identifier') +
          ' (' +
          this.stockInfo.get('product').get('uOMsymbol') +
          ')'
      );
      this.$.body.$.stockDetailList.setStockValuesOtherStores(
        this.stockInfo.get('organizations')
      );
    } else {
      this.$.body.$.stockDetailList.setStockValuesOtherStores(null);
    }
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.ListStockInOtherStores',
  classes:
    'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores row-fluid',
  published: {
    stockValuesOtherStores: null
  },
  components: [
    {
      classes:
        'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores-container1 span12',
      components: [
        {
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores-container1-element1'
        },
        {
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores-container1-container1',
          components: [
            {
              name: 'scrollListStockDetails',
              kind: 'OB.UI.ScrollableTable',
              classes:
                'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores-container1-container1-scrollListStockDetails',
              scrollAreaClasses:
                'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsListStockInOtherStores-container1-container1-scrollListStockDetails-scrollArea',
              renderLine:
                'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.StockInOtherStoresLine',
              renderEmpty: 'OB.UI.RenderEmpty'
            }
          ]
        }
      ]
    }
  ],
  stockValuesOtherStoresChanged: function(oldValue) {
    this.$.scrollListStockDetails.setCollection(this.stockValuesOtherStores);
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores.Components.StockInOtherStoresLine',
  classes:
    'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine',
  components: [
    {
      name: 'line',
      classes:
        'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-line',
      components: [
        {
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-line-organization',
          name: 'organization'
        },
        {
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-line-quantity',
          name: 'quantity'
        },
        {
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-line-container1'
        }
      ]
    },
    {
      classes:
        'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-container1',
      components: [
        {
          name: 'detail',
          classes:
            'obObposPointOfSaleUiModalsModalStockInOtherStoresComponentsStockInOtherStoresLine-container1-detail',
          allowHtml: true
        }
      ]
    }
  ],
  create: function() {
    var str = '';
    this.inherited(arguments);
    this.$.organization.setContent(this.model.get('organizationname'));
    this.$.quantity.setContent(this.model.get('organizationqty'));
    this.model.get('warehouses').each(function(model) {
      str +=
        '<b>' +
        model.get('warehouseqty') +
        ' </b> - ' +
        model.get('warehousename') +
        '<br/>';
    });
    this.$.detail.setContent(str);
  }
});
