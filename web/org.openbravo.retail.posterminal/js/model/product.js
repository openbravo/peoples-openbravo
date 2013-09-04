/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
    includeTerminalDate: true,
    properties: ['id', 'searchkey', 'uPCEAN', 'uOM', 'uOMsymbol', 'productCategory', 'taxCategory', 'img', 'description', 'obposScale', 'groupProduct', 'stocked', 'showstock', 'isGeneric', 'generic_product_id', 'brand', 'characteristicDescription', 'showchdesc', 'bestseller', 'ispack', 'listPrice', 'standardPrice', 'priceLimit', 'cost', '_identifier', '_idx'],
    propertiesFilter: ['_identifier', 'searchkey', 'uPCEAN', 'characteristicDescription'],
    propertyMap: {
      'id': 'm_product_id',
      'uPCEAN': 'upc',
      'uOM': 'c_uom_id',
      'uOMsymbol': 'c_uom_symbol',
      'productCategory': 'm_product_category_id',
      'taxCategory': 'c_taxcategory_id',
      'img': 'img',
      'description': 'description',
      'obposScale': 'em_obpos_scale',
      'groupProduct': 'em_obpos_groupedproduct',
      'stocked': 'stocked',
      'showstock': 'em_obpos_showstock',
      'isGeneric': 'isGeneric',
      'generic_product_id': 'generic_product_id',
      'brand': 'brand',
      'characteristicDescription': 'characteristicDescription',
      'showchdesc': 'showchdesc',
      'bestseller': 'bestseller',
      'ispack': 'ispack',
      'listPrice': 'listPrice',
      'standardPrice': 'standardPrice',
      'priceLimit': 'priceLimit',
      'cost': 'cost',
      '_identifier': '_identifier',
      '_filter': '_filter',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_product (m_product_id TEXT PRIMARY KEY , searchkey TEXT , upc TEXT, c_uom_id TEXT, c_uom_symbol TEXT, m_product_category_id TEXT, c_taxcategory_id TEXT, img TEXT, description TEXT, em_obpos_scale TEXT, em_obpos_groupedproduct TEXT, brand TEXT, characteristicDescription TEXT, showchdesc TEXT, stocked TEXT, em_obpos_showstock TEXT, isGeneric TEXT, generic_product_id TEXT, bestseller TEXT, ispack TEXT, listPrice NUMERIC, standardPrice NUMERIC, priceLimit NUMERIC, cost NUMERIC, _identifier TEXT, _filter TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_product',
    insertStatement: 'INSERT INTO m_product(m_product_id, searchkey ,upc, c_uom_id, c_uom_symbol, m_product_category_id, c_taxcategory_id, img, description, em_obpos_scale, em_obpos_groupedproduct, stocked, em_obpos_showstock, isGeneric, generic_product_id, brand, characteristicDescription, showchdesc, bestseller, ispack, listPrice, standardPrice, priceLimit, cost, _identifier, _filter, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    updateStatement: ''
  });

  OB.Data.Registry.registerModel(Product);
}());