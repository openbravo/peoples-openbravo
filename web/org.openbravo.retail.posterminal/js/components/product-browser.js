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
    scrollAreaMaxHeight: '574px',
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

    function successCallbackBestSellerProducts(products, context) {
      if (products && products.length > 0) {
        var virtualBestSellerCateg = new OB.Model.ProductCategory();
        virtualBestSellerCateg.createBestSellerCategory();
        context.categories.add(virtualBestSellerCateg, {
          at: 0
        });
      }
      context.me.categories.reset(context.categories.models);
    }

    function successCallbackCategories(dataCategories, me) {
      var bestSellerCriteria, context;
      if (dataCategories && dataCategories.length > 0) {
        //search products
        bestSellerCriteria = {
          'bestseller': 'true'
        };
        context = {
          me: me,
          categories: dataCategories
        };
        OB.Dal.find(OB.Model.Product, bestSellerCriteria, successCallbackBestSellerProducts, errorCallback, context);
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
    onAddProduct: '',
    onShowLeftSubWindow: ''
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'productTable',
    scrollAreaMaxHeight: '574px',
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

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        me.products.reset(dataProducts.models);
      } else {
        me.products.reset();
      }
      //      TODO
      me.$.productTable.getHeader().setHeader(category.get('_identifier'));
    }

    if (category) {
      if (category.get('id') === 'OBPOS_bestsellercategory') {
        criteria = {
          'bestseller': 'true'
        };
      } else {
        criteria = {
          'productCategory': category.get('id')
        };
      }
      OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
    } else {
      this.products.reset();
      this.title.text(OB.I18N.getLabel('OBPOS_LblNoCategory'));
    }
  }
});