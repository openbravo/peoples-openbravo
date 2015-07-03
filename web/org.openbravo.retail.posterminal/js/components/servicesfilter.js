/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, */

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB_UI_SearchServicesFilter',
  filterName: 'ServicesFilter',
  published: {
    type: 'PANEL',
    productId: null,
    productList: null,
    orderline: null,
    orderlineList: null
  },
  handlers: {
    onAddProduct: 'addProduct'
  },
  sqlFilter: function () {
    var result = {
      where: null,
      filters: []
    };
    //TODO: missing websql filter
    //    if (this.productId) {
    //      // Only one product
    //      result.where = where + "= ?)";
    //      result.filters = [this.productId];
    //    } else if (this.productList) {
    //      // List of products
    //      var params = "",
    //          filters = [];
    //      this.productList.forEach(function (p) {
    //        if (params !== "") {
    //          params = params + ",";
    //        }
    //        params = params + "?";
    //        filters.push(p);
    //      });
    //      result.where = where + "in (" + params + "))";
    //      result.filters = filters;
    //    }
    return result;
  },
  hqlCriteria: function () {
    return [{
      columns: [],
      operator: OB.Dal.FILTER,
      value: 'Services_Filter',
      params: [this.orderline.get('product').get('id'), this.orderline.get('product').get('productCategory')]
    }];
  },
  lineAttributes: function () {

    var productList = [];

    if (this.orderline) {
      this.orderline.set('preserveId', true);
      productList.push({
        orderlineId: this.orderline.get('id'),
        productName: this.orderline.get('product').get('_identifier')
      });
    }

    return {
      relatedLines: productList
    };
  },
  initComponents: function () {
    this.inherited(arguments);
    this.caption = "Services for"; //TODO: use i18n labels
  }
});