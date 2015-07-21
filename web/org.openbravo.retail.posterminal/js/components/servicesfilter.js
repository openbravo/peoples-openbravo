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
  name: 'OB.UI.SearchServicesFilter',
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
    var result = {},
        where = '',
        filters = [];

    // Only one product
    if (this.productId) {
      where = " and product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

      //including/excluding products
      where += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
      where += "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
      where += "or product.includeProducts is null) ";

      //including/excluding product categories
      where += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
      where += "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
      where += "or product.includeProductCategories is null)) ";

      filters.push(this.orderline.get('product').get('id'));
      filters.push(this.orderline.get('product').get('id'));
      filters.push(this.orderline.get('product').get('productCategory'));
      filters.push(this.orderline.get('product').get('productCategory'));
    }

    result.where = where;
    result.filters = filters;

    return result;
  },
  hqlCriteria: function () {
    return [{
      columns: [],
      operator: OB.Dal.FILTER,
      value: 'Services_Filter',
      params: [this.orderline.get('product').get('id'), this.orderline.get('product').get('productCategory')]
    }, {
      columns: ['ispack'],
      operator: 'equals',
      value: false,
      isId: true
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

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.MandatoryServicesFilter',
  filterName: 'MandatoryServicesFilter',
  published: {
    type: 'HIDDEN'
  },
  sqlFilter: function () {
    var result = {};

    result.where = " and product.proposalType = 'MP'";
    result.filters = [];

    return result;
  },
  hqlCriteria: function () {
    return [{
      columns: [],
      operator: OB.Dal.FILTER,
      value: 'Mandatory_Services',
      params: []
    }];
  }
});