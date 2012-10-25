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

  var Product = Backbone.Model.extend({
    modelName: 'Product',
    tableName: 'm_product',
    entityName: 'Product',
    source: 'org.openbravo.retail.posterminal.master.Product',
    dataLimit: 300,
    properties: ['id', 'uPCEAN', 'uOM', 'productCategory', 'taxCategory', 'img', 'obposScale', 'groupProduct', '_identifier', '_idx'],
    propertyMap: {
      'id': 'm_product_id',
      'uPCEAN': 'upc',
      'uOM': 'c_uom_id',
      'productCategory': 'm_product_category_id',
      'taxCategory': 'c_taxcategory_id',
      'img': 'img',
      'obposScale': 'em_obpos_scale',
      'groupProduct': 'em_obpos_groupedproduct',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_product (m_product_id TEXT PRIMARY KEY , upc TEXT, c_uom_id TEXT, m_product_category_id TEXT, c_taxcategory_id TEXT, img TEXT, em_obpos_scale TEXT, em_obpos_groupedproduct TEXT, _identifier TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_product',
    insertStatement: 'INSERT INTO m_product(m_product_id, upc, c_uom_id, m_product_category_id, c_taxcategory_id, img, em_obpos_scale, em_obpos_groupedproduct, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    updateStatement: ''
  });

  var ProductList = Backbone.Collection.extend({
    model: Product
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.Product = Product;
  window.OB.Collection.ProductList = ProductList;
}());