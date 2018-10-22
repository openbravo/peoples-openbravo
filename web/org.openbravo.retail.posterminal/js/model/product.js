/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
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
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product',
    initialize: function () {
      this.set('originalStandardPrice', this.get('standardPrice'));
    }
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
    name: 'uOMstandardPrecision',
    column: 'c_uom_standardprecision',
    type: 'NUMERIC'
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
    name: 'imgId',
    column: 'imgId',
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
  }, {
    name: 'currentStandardPrice',
    column: 'currentStandardPrice',
    type: 'NUMERIC'
  }, {
    name: 'productType',
    column: 'productType',
    type: 'TEXT'
  }, {
    name: 'includeProductCategories',
    column: 'includeProductCategories',
    type: 'TEXT'
  }, {
    name: 'includeProducts',
    column: 'includeProducts',
    type: 'TEXT'
  }, {
    name: 'printDescription',
    column: 'printDescription',
    type: 'BOOL'
  }, {
    name: 'oBPOSAllowAnonymousSale',
    column: 'oBPOSAllowAnonymousSale',
    type: 'BOOL'
  }, {
    name: 'returnable',
    column: 'returnable',
    type: 'BOOL'
  }, {
    name: 'overdueReturnDays',
    column: 'overdueReturnDays',
    type: 'NUMBER'
  }, {
    name: 'isPriceRuleBased',
    column: 'isPriceRuleBased',
    type: 'BOOL'
  }, {
    name: 'proposalType',
    column: 'proposalType',
    type: 'TEXT'
  }, {
    name: 'availableForMultiline',
    column: 'availableForMultiline',
    type: 'TEXT'
  }, {
    name: 'isLinkedToProduct',
    column: 'isLinkedToProduct',
    type: 'BOOL'
  }, {
    name: 'allowDeferredSell',
    column: 'allowDeferredSell',
    type: 'BOOL'
  }, {
    name: 'deferredSellMaxDays',
    column: 'deferredSellMaxDays',
    type: 'NUMBER'
  }, {
    name: 'quantityRule',
    column: 'quantityRule',
    type: 'TEXT'
  }, {
    name: 'isPrintServices',
    column: 'isPrintServices',
    type: 'BOOL'
  }, {
    name: 'obposEditablePrice',
    column: 'obposEditablePrice',
    type: 'BOOL'
  }, {
    name: 'hasAttributes',
    column: 'hasAttributes',
    type: 'BOOL'
  }, {
    name: 'isSerialNo',
    column: 'isSerialNo',
    type: 'BOOL'
  }, {
    name: 'productStatus',
    column: 'productStatus',
    type: 'TEXT'
  }, {
    name: 'productAssortmentStatus',
    column: 'productAssortmentStatus',
    type: 'TEXT'
  }]);

  Product.addIndex([{
    name: 'obpos_in_prodCat',
    columns: [{
      name: 'm_product_category_id',
      sort: 'desc'
    }]
  }, {
    name: 'obpos_in_bestseller',
    columns: [{
      name: 'bestseller',
      sort: 'asc'
    }]
  }, {
    name: 'obpos_in_upc',
    columns: [{
      name: 'upc',
      sort: 'asc'
    }]
  }, {
    name: 'obpos_in_productType',
    columns: [{
      name: 'productType',
      sort: 'asc'
    }]
  }, {
    name: 'obpos_in_productbrand',
    columns: [{
      name: 'brand',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(Product);
}());