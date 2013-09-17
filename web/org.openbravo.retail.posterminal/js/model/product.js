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

  var Product = OB.Data.ExtensibleModel.extend({
    modelName: 'Product',
    tableName: 'm_product',
    entityName: 'Product',
    source: 'org.openbravo.retail.posterminal.master.Product',
    dataLimit: 300,
    includeTerminalDate: true
  });

  Product.addProperties([{
    name: 'id',
    column: 'm_product_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'searchkey',
    column: 'searchkey',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'uPCEAN',
    column: 'upc',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'uOM',
    column: 'c_uom_id',
    type: 'TEXT'
  }, {
    name: 'uOMsymbol',
    column: 'c_uom_symbol',
    type: 'TEXT'
  }, {
    name: 'productCategory',
    column: 'm_product_category_id',
    type: 'TEXT'
  }, {
    name: 'taxCategory',
    column: 'c_taxcategory_id',
    type: 'TEXT'
  }, {
    name: 'img',
    column: 'img',
    type: 'TEXT'
  }, {
    name: 'description',
    column: 'description',
    type: 'TEXT'
  }, {
    name: 'obposScale',
    column: 'em_obpos_scale',
    type: 'TEXT'
  }, {
    name: 'groupProduct',
    column: 'em_obpos_groupedproduct',
    type: 'TEXT'
  }, {
    name: 'stocked',
    column: 'stocked',
    type: 'TEXT'
  }, {
    name: 'showstock',
    column: 'em_obpos_showstock',
    type: 'TEXT'
  }, {
    name: 'isGeneric',
    column: 'isGeneric',
    type: 'TEXT'
  }, {
    name: 'generic_product_id',
    column: 'generic_product_id',
    type: 'TEXT'
  }, {
    name: 'brand',
    column: 'brand',
    type: 'TEXT'
  }, {
    name: 'characteristicDescription',
    column: 'characteristicDescription',
    type: 'TEXT'
  }, {
    name: 'showchdesc',
    column: 'showchdesc',
    type: 'TEXT'
  }, {
    name: 'bestseller',
    column: 'bestseller',
    type: 'TEXT'
  }, {
    name: 'ispack',
    column: 'ispack',
    type: 'TEXT'
  }, {
    name: 'listPrice',
    column: 'listPrice',
    type: 'NUMERIC'
  }, {
    name: 'standardPrice',
    column: 'standardPrice',
    type: 'NUMERIC'
  }, {
    name: 'priceLimit',
    column: 'priceLimit',
    type: 'NUMERIC'
  }, {
    name: 'cost',
    column: 'cost',
    type: 'NUMERIC'
  }, {
    name: 'algorithm',
    column: 'algorithm',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: true,
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(Product);
}());