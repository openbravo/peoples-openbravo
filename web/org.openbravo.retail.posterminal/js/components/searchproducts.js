/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.SearchProductHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  components: [{
    style: 'padding: 10px 10px 5px 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInput',
          name: 'productname',
          style: 'width: 100%;',
          onchange: 'searchAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }, {
      style: 'margin: 5px 0px 0px 0px;',
      components: [{
        kind: 'OB.UI.List',
        name: 'productcategory',
        classes: 'combo',
        style: 'width: 100%',
        renderHeader: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue('__all__');
            this.setContent(OB.I18N.getLabel('OBPOS_SearchAllCategories'));
          }
        }),
        renderLine: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue(this.model.get('id'));
            this.setContent(this.model.get('_identifier'));
          }
        }),
        renderEmpty: 'enyo.Control'
      }]
    }]
  }],
  setHeaderCollection: function (valueToSet) {
    this.$.productcategory.setCollection(valueToSet);
  },
  searchAction: function () {
    this.doSearchAction({
      productCat: this.$.productcategory.getValue(),
      productName: this.$.productname.getValue()
    });
  },
  clearAction: function () {
    this.$.productname.setValue('');
    this.$.productcategory.setSelected(0);
    this.doClearAction();
  }
});

enyo.kind({
  name: 'OB.UI.SearchProduct',
  published: {
    receipt: null
  },
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onAddProduct: ''
  },
  components: [{
    classes: 'row-fluid',
    components: [{
      classes: 'span12',
      components: [{
        classes: 'row-fluid',
        style: 'border-bottom: 1px solid #cccccc;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              kind: 'OB.UI.ScrollableTable',
              name: 'products',
              scrollAreaMaxHeight: '482px',
              renderHeader: 'OB.UI.SearchProductHeader',
              renderEmpty: 'OB.UI.RenderEmpty',
              renderLine: 'OB.UI.RenderProduct'
            }]
          }]
        }]
      }]
    }]
  }],
  init: function () {
    var me = this;
    this.inherited(arguments);
    this.categories = new OB.Collection.ProductCategoryList();
    this.products = new OB.Collection.ProductList();

    //first the main collection of the component
    this.$.products.setCollection(this.products);
    this.$.products.getHeader().setHeaderCollection(this.categories);

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

    this.products.on('click', function (model) {
      this.doAddProduct({
        product: model
      });
    }, this);

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
  },
  receiptChanged: function () {
    this.receipt.on('clear', function () {
      this.$.products.$.theader.$.searchProductHeader.$.productname.setContent('');
      this.$.products.$.theader.$.searchProductHeader.$.productcategory.setContent('');
      //A filter should be set before show products. -> Big data!!
      //this.products.exec({priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: {}});
    }, this);
  },
  clearAction: function (inSender, inEvent) {
    this.products.reset();
  },
  searchAction: function (inSender, inEvent) {
    var criteria = {},
        me = this;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    // Initializing combo of categories without filtering

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        me.products.reset(dataProducts.models);
        me.products.trigger('reset');
      } else {
        OB.UTIL.showWarning("No products found");
        me.products.reset();
      }
    }

    if (inEvent.productName) {
      criteria._filter = {
        operator: OB.Dal.CONTAINS,
        value: inEvent.productName
      };
    }
    if (inEvent.productCat && inEvent.productCat !== '__all__') {
      criteria.productCategory = inEvent.productCat;
    }
    OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
  }
});