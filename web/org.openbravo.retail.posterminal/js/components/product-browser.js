/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  name: 'OB.UI.ProductBrowser',
  classes: 'row-fluid',
  components: [{
    classes: 'span6',
    components: [{
      kind: 'OB.UI.BrowseProducts',
      name: 'browseProducts'
    }]
  }, {
    classes: 'span6',
    components: [{
      kind: 'OB.UI.BrowseCategories',
      name: 'browseCategories'
    }]
  }],
  init: function () {
    this.$.browseCategories.$.listCategories.categories.on('selected', function (category) {
      this.$.browseProducts.$.listProducts.loadCategory(category);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.BrowseCategories',
  style: 'overflow:auto; height: 612px; margin: 5px;',
  components: [{
    style: 'background-color: #ffffff; color: black; padding: 5px',
    components: [{
      kind: 'OB.UI.ListCategories',
      name: 'listCategories'
    }]
  }]
});

enyo.kind({
  name: 'OB.UI.BrowseProducts',
  style: 'margin: 5px;',
  components: [{
    style: 'background-color: #ffffff; color: black; padding: 5px',
    components: [{
      kind: 'OB.UI.ListProducts',
      name: 'listProducts'
    }]
  }]
});

enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OB.UI.CategoryListHeader',
  style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
  components: [{
    style: 'line-height: 27px; font-size: 18px; font-weight: bold;',
    name: 'title',
    content: OB.I18N.getLabel('OBPOS_LblCategories')
  }]
});

enyo.kind({
  name: 'OB.UI.ListCategories',
  components: [{
    name: 'categoryTable',
    scrollAreaMaxHeight: '540px',
    listStyle: 'list',
    kind: 'OB.UI.ScrollableTable',
    renderHeader: 'OB.UI.CategoryListHeader',
    renderEmpty: 'OB.UI.RenderEmpty',
    renderLine: 'OB.UI.RenderCategory'
  }],

  init: function () {
    var me = this;
    this.categories = new OB.Collection.ProductCategoryList();
    this.$.categoryTable.setCollection(this.categories);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackCategories(dataCategories, me) {
      if (dataCategories && dataCategories.length > 0) {
        me.categories.reset(dataCategories.models);
      } else {
        me.categories.reset();
      }
    }

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
  }
});

//This header is set dynamically
//use scrollableTableHeaderChanged_handler method of scrollableTable to manage changes
//me.$.productTable.setHeaderText(category.get('_identifier'));
enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OB.UI.ProductListHeader',
  style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
  components: [{
    style: 'line-height: 27px; font-size: 18px; font-weight: bold;',
    name: 'title'
  }],
  setHeader: function (valueToSet) {
    this.$.title.setContent(valueToSet);
  }
});

enyo.kind({
  name: 'OB.UI.ListProducts',
  events: {
    onAddProduct: ''
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'productTable',
    scrollAreaMaxHeight: '540px',
    renderHeader: 'OB.UI.ProductListHeader',
    renderEmpty: 'OB.UI.RenderEmpty',
    renderLine: 'OB.UI.RenderProduct'
  }],
  init: function () {
    this.inherited(arguments);
    this.products = new OB.Collection.ProductList();
    this.$.productTable.setCollection(this.products);
    this.products.on('click', function (model) {
      this.doAddProduct({
        product: model
      });
    }, this);
  },

  loadCategory: function (category) {
    var criteria, me = this;

    function successCallbackPrices(dataPrices, dataProducts) {
      if (dataPrices && dataPrices.length > 0) {
        _.each(dataPrices.models, function (currentPrice) {
          if (dataProducts.get(currentPrice.get('product'))) {
            dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
          }
        });
        _.each(dataProducts.models, function (currentProd) {
          if (currentProd.get('price') === undefined) {
            var price = new OB.Model.ProductPrice({
              'standardPrice': 0
            });
            dataProducts.get(currentProd.get('id')).set('price', price);
            OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
          }
        });
      } else {
        OB.UTIL.showWarning("OBDAL No prices found for products");
        _.each(dataProducts.models, function (currentProd) {
          var price = new OB.Model.ProductPrice({
            'standardPrice': 0
          });
          currentProd.set('price', price);
        });
      }
      me.products.reset(dataProducts.models);
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        criteria = {
          'priceListVersion': OB.POS.modelterminal.get('pricelistversion').id
        };
        OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
      } else {
        me.products.reset();
      }
      //      TODO
      me.$.productTable.getHeader().setHeader(category.get('_identifier'));
    }

    if (category) {
      criteria = {
        'productCategory': category.get('id')
      };
      OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
    } else {
      this.products.reset();
      this.title.text(OB.I18N.getLabel('OBPOS_LblNoCategory'));
    }
  }
});