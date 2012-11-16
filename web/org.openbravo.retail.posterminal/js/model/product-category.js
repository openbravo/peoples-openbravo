/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var ProductCategory = Backbone.Model.extend({
    modelName: 'ProductCategory',
    tableName: 'm_product_category',
    entityName: 'ProductCategory',
    source: 'org.openbravo.retail.posterminal.master.Category',
    dataLimit: 300,
    properties: ['id', 'searchKey', 'name', 'img', '_identifier', '_idx'],
    propertyMap: {
      'id': 'm_product_category_id',
      'searchKey': 'value',
      'name': 'name',
      'img': 'ad_image_id',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_product_category (m_product_category_id TEXT PRIMARY KEY , value TEXT , name TEXT , ad_image_id TEXT , _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_product_category',
    insertStatement: 'INSERT INTO m_product_category(m_product_category_id, value, name, ad_image_id, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?)',
    updateStatement: '',
    createBestSellerCategory: function () {
      this.set('id', 'OBPOS_bestsellercategory');
      this.set('searchKey', 'bestseller');
      this.set('name', OB.I18N.getLabel('OBPOS_bestSellerCategory'));
      this.set('img', 'iconBestSellers');
      this.set('_identifier', this.get('name'));
    }
  });

  var ProductCategoryList = Backbone.Collection.extend({
    model: ProductCategory
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.ProductCategory = ProductCategory;
  window.OB.Collection.ProductCategoryList = ProductCategoryList;
}());